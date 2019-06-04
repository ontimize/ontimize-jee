/*
 * Copyright (c) 2001-2008 Caucho Technology, Inc. All rights reserved. The Apache Software License, Version 1.1 Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 1. Redistributions of source code must retain the above copyright notice, this list of conditions and
 * the following disclaimer. 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution. 3. The end-user documentation included with the redistribution, if any, must include the following acknowlegement: "This
 * product includes software developed by the Caucho Technology (http://www.caucho.com/)." Alternately, this acknowlegement may appear in the software itself, if and wherever such
 * third-party acknowlegements normally appear. 4. The names "Burlap", "Resin", and "Caucho" must not be used to endorse or promote products derived from this software without
 * prior written permission. For written permission, please contact info@caucho.com. 5. Products derived from this software may not be called "Resin" nor may "Resin" appear in
 * their names without prior written permission of Caucho Technology. THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL CAUCHO TECHNOLOGY OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * @author Scott Ferguson
 */

package com.caucho.hessian.io.serializer;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.HessianUnshared;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.IOExceptionWrapper;

import sun.misc.Unsafe;

/**
 * Serializing an object for known object types.
 */
public class UnsafeSerializer extends AbstractSerializer {

	private static final Logger													logger			= LoggerFactory.getLogger(UnsafeSerializer.class);
	private static final Unsafe													UNSAFE;
	private static final WeakHashMap<Class<?>, SoftReference<UnsafeSerializer>>	SERIALIZER_MAP	= new WeakHashMap<>();
	private static boolean														isEnabled;

	private Field[]																fields;
	private FieldSerializer[]													fieldSerializers;

	public static boolean isEnabled() {
		return UnsafeSerializer.isEnabled;
	}

	public UnsafeSerializer(Class<?> cl) {
		this.introspect(cl);
	}

	public static UnsafeSerializer create(Class<?> cl) {
		synchronized (UnsafeSerializer.SERIALIZER_MAP) {
			SoftReference<UnsafeSerializer> baseRef = UnsafeSerializer.SERIALIZER_MAP.get(cl);

			UnsafeSerializer base = baseRef != null ? baseRef.get() : null;

			if (base == null) {
				if (cl.isAnnotationPresent(HessianUnshared.class)) {
					base = new UnsafeUnsharedSerializer(cl);
				} else {
					base = new UnsafeSerializer(cl);
				}

				baseRef = new SoftReference<>(base);
				UnsafeSerializer.SERIALIZER_MAP.put(cl, baseRef);
			}

			return base;
		}
	}

	protected void introspect(Class<?> cl) {
		ArrayList<Field> primitiveFields = new ArrayList<>();
		ArrayList<Field> compoundFields = new ArrayList<>();

		for (; cl != null; cl = cl.getSuperclass()) {
			Field[] fields = cl.getDeclaredFields();

			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];

				if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
					continue;
				}

				// XXX: could parameterize the handler to only deal with public
				field.setAccessible(true);

				if (field.getType().isPrimitive() || (field.getType().getName().startsWith("java.lang.") && !field.getType().equals(Object.class))) {
					primitiveFields.add(field);
				} else {
					compoundFields.add(field);
				}
			}
		}

		ArrayList<Field> fields = new ArrayList<>();
		fields.addAll(primitiveFields);
		fields.addAll(compoundFields);

		this.fields = new Field[fields.size()];
		fields.toArray(this.fields);

		this.fieldSerializers = new FieldSerializer[this.fields.length];

		for (int i = 0; i < this.fields.length; i++) {
			this.fieldSerializers[i] = UnsafeSerializer.getFieldSerializer(this.fields[i]);
		}
	}

	@Override
	public void writeObject(Object obj, AbstractHessianOutput out) throws IOException {
		if (out.addRef(obj)) {
			return;
		}

		Class<?> cl = obj.getClass();

		int ref = out.writeObjectBegin(cl.getName());

		if (ref >= 0) {
			this.writeInstance(obj, out);
		} else if (ref == -1) {
			this.writeDefinition20(out);
			out.writeObjectBegin(cl.getName());
			this.writeInstance(obj, out);
		} else {
			this.writeObject10(obj, out);
		}
	}

	@Override
	protected void writeObject10(Object obj, AbstractHessianOutput out) throws IOException {
		for (int i = 0; i < this.fields.length; i++) {
			Field field = this.fields[i];

			out.writeString(field.getName());

			this.fieldSerializers[i].serialize(out, obj);
		}

		out.writeMapEnd();
	}

	private void writeDefinition20(AbstractHessianOutput out) throws IOException {
		out.writeClassFieldLength(this.fields.length);

		for (int i = 0; i < this.fields.length; i++) {
			Field field = this.fields[i];

			out.writeString(field.getName());
		}
	}

	@Override
	final public void writeInstance(Object obj, AbstractHessianOutput out) throws IOException {
		try {
			FieldSerializer[] fieldSerializers = this.fieldSerializers;
			int length = fieldSerializers.length;

			for (int i = 0; i < length; i++) {
				fieldSerializers[i].serialize(out, obj);
			}
		} catch (RuntimeException e) {
			UnsafeSerializer.logger.error(e.getMessage() + "\n class: " + obj.getClass().getName(), e);
			throw new RuntimeException(e.getMessage() + "\n class: " + obj.getClass().getName(), e);
		} catch (IOException e) {
			UnsafeSerializer.logger.error(e.getMessage() + "\n class: " + obj.getClass().getName(), e);
			throw new IOExceptionWrapper(e.getMessage() + "\n class: " + obj.getClass().getName(), e);
		}
	}

	private static FieldSerializer getFieldSerializer(Field field) {
		Class<?> type = field.getType();

		if (boolean.class.equals(type)) {
			return new BooleanFieldSerializer(field);
		} else if (byte.class.equals(type)) {
			return new ByteFieldSerializer(field);
		} else if (char.class.equals(type)) {
			return new CharFieldSerializer(field);
		} else if (short.class.equals(type)) {
			return new ShortFieldSerializer(field);
		} else if (int.class.equals(type)) {
			return new IntFieldSerializer(field);
		} else if (long.class.equals(type)) {
			return new LongFieldSerializer(field);
		} else if (double.class.equals(type)) {
			return new DoubleFieldSerializer(field);
		} else if (float.class.equals(type)) {
			return new FloatFieldSerializer(field);
		} else if (String.class.equals(type)) {
			return new StringFieldSerializer(field);
		} else if (java.util.Date.class.equals(type) || java.sql.Date.class.equals(type) || java.sql.Timestamp.class.equals(type) || java.sql.Time.class.equals(type)) {
			return new DateFieldSerializer(field);
		} else {
			return new ObjectFieldSerializer(field);
		}
	}

	abstract static class FieldSerializer {

		abstract void serialize(AbstractHessianOutput out, Object obj) throws IOException;
	}

	final static class ObjectFieldSerializer extends FieldSerializer {

		private final Field	_field;
		private final long	_offset;

		ObjectFieldSerializer(Field field) {
			this._field = field;
			this._offset = UnsafeSerializer.UNSAFE.objectFieldOffset(field);

			if (this._offset == Unsafe.INVALID_FIELD_OFFSET) {
				throw new IllegalStateException();
			}
		}

		@Override
		final void serialize(AbstractHessianOutput out, Object obj) throws IOException {
			try {
				Object value = UnsafeSerializer.UNSAFE.getObject(obj, this._offset);

				out.writeObject(value);
			} catch (RuntimeException e) {
				throw new RuntimeException(e.getMessage() + "\n field: " + this._field.getDeclaringClass().getName() + '.' + this._field.getName(), e);
			} catch (IOException e) {
				throw new IOExceptionWrapper(e.getMessage() + "\n field: " + this._field.getDeclaringClass().getName() + '.' + this._field.getName(), e);
			}
		}
	}

	final static class BooleanFieldSerializer extends FieldSerializer {

		private final Field	field;
		private final long	offset;

		BooleanFieldSerializer(Field field) {
			this.field = field;
			this.offset = UnsafeSerializer.UNSAFE.objectFieldOffset(field);

			if (this.offset == Unsafe.INVALID_FIELD_OFFSET) {
				throw new IllegalStateException();
			}
		}

		@Override
		void serialize(AbstractHessianOutput out, Object obj) throws IOException {
			boolean value = UnsafeSerializer.UNSAFE.getBoolean(obj, this.offset);

			out.writeBoolean(value);
		}
	}

	final static class ByteFieldSerializer extends FieldSerializer {

		private final Field	field;
		private final long	offset;

		ByteFieldSerializer(Field field) {
			this.field = field;
			this.offset = UnsafeSerializer.UNSAFE.objectFieldOffset(field);

			if (this.offset == Unsafe.INVALID_FIELD_OFFSET) {
				throw new IllegalStateException();
			}
		}

		@Override
		final void serialize(AbstractHessianOutput out, Object obj) throws IOException {
			int value = UnsafeSerializer.UNSAFE.getByte(obj, this.offset);

			out.writeInt(value);
		}
	}

	final static class CharFieldSerializer extends FieldSerializer {

		private final Field	field;
		private final long	offset;

		CharFieldSerializer(Field field) {
			this.field = field;
			this.offset = UnsafeSerializer.UNSAFE.objectFieldOffset(field);

			if (this.offset == Unsafe.INVALID_FIELD_OFFSET) {
				throw new IllegalStateException();
			}
		}

		@Override
		final void serialize(AbstractHessianOutput out, Object obj) throws IOException {
			char value = UnsafeSerializer.UNSAFE.getChar(obj, this.offset);

			out.writeString(String.valueOf(value));
		}
	}

	final static class ShortFieldSerializer extends FieldSerializer {

		private final Field	field;
		private final long	offset;

		ShortFieldSerializer(Field field) {
			this.field = field;
			this.offset = UnsafeSerializer.UNSAFE.objectFieldOffset(field);

			if (this.offset == Unsafe.INVALID_FIELD_OFFSET) {
				throw new IllegalStateException();
			}
		}

		@Override
		final void serialize(AbstractHessianOutput out, Object obj) throws IOException {
			int value = UnsafeSerializer.UNSAFE.getShort(obj, this.offset);

			out.writeInt(value);
		}
	}

	final static class IntFieldSerializer extends FieldSerializer {

		private final Field	field;
		private final long	offset;

		IntFieldSerializer(Field field) {
			this.field = field;
			this.offset = UnsafeSerializer.UNSAFE.objectFieldOffset(field);

			if (this.offset == Unsafe.INVALID_FIELD_OFFSET) {
				throw new IllegalStateException();
			}
		}

		@Override
		final void serialize(AbstractHessianOutput out, Object obj) throws IOException {
			int value = UnsafeSerializer.UNSAFE.getInt(obj, this.offset);

			out.writeInt(value);
		}
	}

	final static class LongFieldSerializer extends FieldSerializer {

		private final Field	field;
		private final long	offset;

		LongFieldSerializer(Field field) {
			this.field = field;
			this.offset = UnsafeSerializer.UNSAFE.objectFieldOffset(field);

			if (this.offset == Unsafe.INVALID_FIELD_OFFSET) {
				throw new IllegalStateException();
			}
		}

		@Override
		final void serialize(AbstractHessianOutput out, Object obj) throws IOException {
			long value = UnsafeSerializer.UNSAFE.getLong(obj, this.offset);

			out.writeLong(value);
		}
	}

	final static class FloatFieldSerializer extends FieldSerializer {

		private final Field	field;
		private final long	offset;

		FloatFieldSerializer(Field field) {
			this.field = field;
			this.offset = UnsafeSerializer.UNSAFE.objectFieldOffset(field);

			if (this.offset == Unsafe.INVALID_FIELD_OFFSET) {
				throw new IllegalStateException();
			}
		}

		@Override
		final void serialize(AbstractHessianOutput out, Object obj) throws IOException {
			double value = UnsafeSerializer.UNSAFE.getFloat(obj, this.offset);

			out.writeDouble(value);
		}
	}

	final static class DoubleFieldSerializer extends FieldSerializer {

		private final Field	field;
		private final long	offset;

		DoubleFieldSerializer(Field field) {
			this.field = field;
			this.offset = UnsafeSerializer.UNSAFE.objectFieldOffset(field);

			if (this.offset == Unsafe.INVALID_FIELD_OFFSET) {
				throw new IllegalStateException();
			}
		}

		@Override
		final void serialize(AbstractHessianOutput out, Object obj) throws IOException {
			double value = UnsafeSerializer.UNSAFE.getDouble(obj, this.offset);

			out.writeDouble(value);
		}
	}

	final static class StringFieldSerializer extends FieldSerializer {

		private final Field	field;
		private final long	offset;

		StringFieldSerializer(Field field) {
			this.field = field;
			this.offset = UnsafeSerializer.UNSAFE.objectFieldOffset(field);

			if (this.offset == Unsafe.INVALID_FIELD_OFFSET) {
				throw new IllegalStateException();
			}
		}

		@Override
		final void serialize(AbstractHessianOutput out, Object obj) throws IOException {
			String value = (String) UnsafeSerializer.UNSAFE.getObject(obj, this.offset);

			out.writeString(value);
		}
	}

	final static class DateFieldSerializer extends FieldSerializer {

		private final Field	field;
		private final long	offset;

		DateFieldSerializer(Field field) {
			this.field = field;
			this.offset = UnsafeSerializer.UNSAFE.objectFieldOffset(field);

			if (this.offset == Unsafe.INVALID_FIELD_OFFSET) {
				throw new IllegalStateException();
			}
		}

		@Override
		void serialize(AbstractHessianOutput out, Object obj) throws IOException {
			java.util.Date value = (java.util.Date) UnsafeSerializer.UNSAFE.getObject(obj, this.offset);

			if (value == null) {
				out.writeNull();
			} else {
				out.writeUTCDate(value.getTime());
			}
		}
	}

	static {
		boolean isEnabled = false;
		Unsafe unsafe = null;

		try {
			Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
			Field theUnsafe = null;
			for (Field field : unsafeClass.getDeclaredFields()) {
				if ("theUnsafe".equals(field.getName())) {
					theUnsafe = field;
				}
			}

			if (theUnsafe != null) {
				theUnsafe.setAccessible(true);
				unsafe = (Unsafe) theUnsafe.get(null);
			}

			isEnabled = unsafe != null;

			String unsafeProp = System.getProperty("com.caucho.hessian.unsafe");

			if ("false".equals(unsafeProp)) {
				isEnabled = false;
			}
		} catch (Exception e) {
			UnsafeSerializer.logger.trace(e.toString(), e);
		}

		UNSAFE = unsafe;
		UnsafeSerializer.isEnabled = isEnabled;
	}
}

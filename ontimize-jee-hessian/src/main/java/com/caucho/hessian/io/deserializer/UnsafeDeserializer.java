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

package com.caucho.hessian.io.deserializer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.HessianFieldException;
import com.caucho.hessian.io.IOExceptionWrapper;

import sun.misc.Unsafe;

/**
 * Serializing an object for known object types.
 */
public class UnsafeDeserializer extends AbstractMapDeserializer {

	private static final Logger	logger	= LoggerFactory.getLogger(UnsafeDeserializer.class);

	private static boolean		isEnabled;
	@SuppressWarnings("restriction")
	private static Unsafe		unsafe;

	protected static Unsafe getUnsafe() {
		return UnsafeDeserializer.unsafe;
	}

	private final Class<?>								type;
	private final HashMap<String, FieldDeserializer>	fieldMap;
	private final Method								readResolve;

	public UnsafeDeserializer(Class<?> cl) {
		this.type = cl;
		this.fieldMap = this.getFieldMap(cl);

		this.readResolve = this.getReadResolve(cl);

		if (this.readResolve != null) {
			this.readResolve.setAccessible(true);
		}
	}

	public static boolean isEnabled() {
		return UnsafeDeserializer.isEnabled;
	}

	@Override
	public Class<?> getType() {
		return this.type;
	}

	@Override
	public boolean isReadResolve() {
		return this.readResolve != null;
	}

	@Override
	public Object readMap(AbstractHessianInput in) throws IOException {
		try {
			Object obj = this.instantiate();

			return this.readMap(in, obj);
		} catch (IOException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IOExceptionWrapper(this.type.getName() + ":" + e.getMessage(), e);
		}
	}

	@Override
	public Object[] createFields(int len) {
		return new FieldDeserializer[len];
	}

	@Override
	public Object createField(String name) {
		Object reader = this.fieldMap.get(name);

		if (reader == null) {
			reader = NullFieldDeserializer.DESER;
		}

		return reader;
	}

	@Override
	public Object readObject(AbstractHessianInput in, Object[] fields) throws IOException {
		try {
			Object obj = this.instantiate();

			return this.readObject(in, obj, (FieldDeserializer[]) fields);
		} catch (IOException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IOExceptionWrapper(this.type.getName() + ":" + e.getMessage(), e);
		}
	}

	@Override
	public Object readObject(AbstractHessianInput in, String[] fieldNames) throws IOException {
		try {
			Object obj = this.instantiate();

			return this.readObject(in, obj, fieldNames);
		} catch (IOException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IOExceptionWrapper(this.type.getName() + ":" + e.getMessage(), e);
		}
	}

	/**
	 * Returns the readResolve method
	 */
	protected Method getReadResolve(Class<?> cl) {
		for (; cl != null; cl = cl.getSuperclass()) {
			Method[] methods = cl.getDeclaredMethods();

			for (int i = 0; i < methods.length; i++) {
				Method method = methods[i];

				if ("readResolve".equals(method.getName()) && (method.getParameterTypes().length == 0)) {
					return method;
				}
			}
		}

		return null;
	}

	public Object readMap(AbstractHessianInput in, Object obj) throws IOException {
		try {
			int ref = in.addRef(obj);

			while (!in.isEnd()) {
				Object key = in.readObject();

				FieldDeserializer deser = this.fieldMap.get(key);

				if (deser != null) {
					deser.deserialize(in, obj);
				} else {
					in.readObject();
				}
			}

			in.readMapEnd();

			Object resolve = this.resolve(in, obj);

			if (obj != resolve) {
				in.setRef(ref, resolve);
			}

			return resolve;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOExceptionWrapper(e);
		}
	}

	public Object readObject(AbstractHessianInput in, Object obj, FieldDeserializer[] fields) throws IOException {
		try {
			int ref = in.addRef(obj);

			for (FieldDeserializer reader : fields) {
				reader.deserialize(in, obj);
			}

			Object resolve = this.resolve(in, obj);

			if (obj != resolve) {
				in.setRef(ref, resolve);
			}

			return resolve;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOExceptionWrapper(obj.getClass().getName() + ":" + e, e);
		}
	}

	public Object readObject(AbstractHessianInput in, Object obj, String[] fieldNames) throws IOException {
		try {
			int ref = in.addRef(obj);

			for (String fieldName : fieldNames) {
				FieldDeserializer reader = this.fieldMap.get(fieldName);

				if (reader != null) {
					reader.deserialize(in, obj);
				} else {
					in.readObject();
				}
			}

			Object resolve = this.resolve(in, obj);

			if (obj != resolve) {
				in.setRef(ref, resolve);
			}

			return resolve;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOExceptionWrapper(obj.getClass().getName() + ":" + e, e);
		}
	}

	protected Object resolve(AbstractHessianInput in, Object obj) throws Exception {
		// if there's a readResolve method, call it
		try {
			if (this.readResolve != null) {
				return this.readResolve.invoke(obj, new Object[0]);
			}
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof Exception) {
				throw (Exception) e.getCause();
			} else {
				throw e;
			}
		}

		return obj;
	}

	@SuppressWarnings("restriction")
	protected Object instantiate() throws Exception {
		return UnsafeDeserializer.unsafe.allocateInstance(this.type);
	}

	/**
	 * Creates a map of the classes fields.
	 */
	protected HashMap<String, FieldDeserializer> getFieldMap(Class<?> cl) {
		HashMap<String, FieldDeserializer> fieldMap = new HashMap<>();

		for (; cl != null; cl = cl.getSuperclass()) {
			Field[] fields = cl.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];

				if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
					continue;
				} else if (fieldMap.get(field.getName()) != null) {
					continue;
				}

				// XXX: could parameterize the handler to only deal with public
				try {
					field.setAccessible(true);
				} catch (Exception e) {
					UnsafeDeserializer.logger.error(null, e);
				}

				Class<?> type = field.getType();
				FieldDeserializer deser;

				if (String.class.equals(type)) {
					deser = new StringFieldDeserializer(field);
				} else if (byte.class.equals(type)) {
					deser = new ByteFieldDeserializer(field);
				} else if (char.class.equals(type)) {
					deser = new CharFieldDeserializer(field);
				} else if (short.class.equals(type)) {
					deser = new ShortFieldDeserializer(field);
				} else if (int.class.equals(type)) {
					deser = new IntFieldDeserializer(field);
				} else if (long.class.equals(type)) {
					deser = new LongFieldDeserializer(field);
				} else if (float.class.equals(type)) {
					deser = new FloatFieldDeserializer(field);
				} else if (double.class.equals(type)) {
					deser = new DoubleFieldDeserializer(field);
				} else if (boolean.class.equals(type)) {
					deser = new BooleanFieldDeserializer(field);
				} else if (java.sql.Date.class.equals(type)) {
					deser = new SqlDateFieldDeserializer(field);
				} else if (java.sql.Timestamp.class.equals(type)) {
					deser = new SqlTimestampFieldDeserializer(field);
				} else if (java.sql.Time.class.equals(type)) {
					deser = new SqlTimeFieldDeserializer(field);
				} else {
					deser = new ObjectFieldDeserializer(field);
				}

				fieldMap.put(field.getName(), deser);
			}
		}

		return fieldMap;
	}

	public abstract static class FieldDeserializer {

		protected abstract void deserialize(AbstractHessianInput in, Object obj) throws IOException;
	}

	public static class NullFieldDeserializer extends FieldDeserializer {

		static NullFieldDeserializer DESER = new NullFieldDeserializer();

		@Override
		protected void deserialize(AbstractHessianInput in, Object obj) throws IOException {
			in.readObject();
		}
	}

	public static class ObjectFieldDeserializer extends FieldDeserializer {

		private final Field	field;
		private final long	offset;

		@SuppressWarnings("restriction")
		public ObjectFieldDeserializer(Field field) {
			this.field = field;
			this.offset = UnsafeDeserializer.unsafe.objectFieldOffset(this.field);
		}

		@Override
		@SuppressWarnings("restriction")
		protected void deserialize(AbstractHessianInput in, Object obj) throws IOException {
			Object value = null;

			try {
				value = in.readObject(this.field.getType());

				UnsafeDeserializer.unsafe.putObject(obj, this.offset, value);
			} catch (Exception e) {
				UnsafeDeserializer.logDeserializeError(this.field, obj, value, e);
			}
		}
	}

	public static class BooleanFieldDeserializer extends FieldDeserializer {

		private final Field	field;
		private final long	offset;

		@SuppressWarnings("restriction")
		public BooleanFieldDeserializer(Field field) {
			this.field = field;
			this.offset = UnsafeDeserializer.unsafe.objectFieldOffset(this.field);
		}

		@Override
		@SuppressWarnings("restriction")
		protected void deserialize(AbstractHessianInput in, Object obj) throws IOException {
			boolean value = false;

			try {
				value = in.readBoolean();

				UnsafeDeserializer.unsafe.putBoolean(obj, this.offset, value);
			} catch (Exception e) {
				UnsafeDeserializer.logDeserializeError(this.field, obj, value, e);
			}
		}
	}

	public static class ByteFieldDeserializer extends FieldDeserializer {

		private final Field	field;
		private final long	offset;

		@SuppressWarnings("restriction")
		public ByteFieldDeserializer(Field field) {
			this.field = field;
			this.offset = UnsafeDeserializer.unsafe.objectFieldOffset(this.field);
		}

		@Override
		@SuppressWarnings("restriction")
		protected void deserialize(AbstractHessianInput in, Object obj) throws IOException {
			int value = 0;

			try {
				value = in.readInt();
				UnsafeDeserializer.unsafe.putByte(obj, this.offset, (byte) value);
			} catch (Exception e) {
				UnsafeDeserializer.logDeserializeError(this.field, obj, value, e);
			}
		}
	}

	public static class CharFieldDeserializer extends FieldDeserializer {

		private final Field	field;
		private final long	offset;

		@SuppressWarnings("restriction")
		public CharFieldDeserializer(Field field) {
			this.field = field;
			this.offset = UnsafeDeserializer.unsafe.objectFieldOffset(this.field);
		}

		@Override
		@SuppressWarnings("restriction")
		protected void deserialize(AbstractHessianInput in, Object obj) throws IOException {
			String value = null;

			try {
				value = in.readString();

				char ch;

				if ((value != null) && (value.length() > 0)) {
					ch = value.charAt(0);
				} else {
					ch = 0;
				}

				UnsafeDeserializer.unsafe.putChar(obj, this.offset, ch);
			} catch (Exception e) {
				UnsafeDeserializer.logDeserializeError(this.field, obj, value, e);
			}
		}
	}

	public static class ShortFieldDeserializer extends FieldDeserializer {

		private final Field	_field;
		private final long	_offset;

		@SuppressWarnings("restriction")
		public ShortFieldDeserializer(Field field) {
			this._field = field;
			this._offset = UnsafeDeserializer.unsafe.objectFieldOffset(this._field);
		}

		@Override
		@SuppressWarnings("restriction")
		protected void deserialize(AbstractHessianInput in, Object obj) throws IOException {
			int value = 0;

			try {
				value = in.readInt();

				UnsafeDeserializer.unsafe.putShort(obj, this._offset, (short) value);
			} catch (Exception e) {
				UnsafeDeserializer.logDeserializeError(this._field, obj, value, e);
			}
		}
	}

	public static class IntFieldDeserializer extends FieldDeserializer {

		private final Field	field;
		private final long	offset;

		@SuppressWarnings("restriction")
		public IntFieldDeserializer(Field field) {
			this.field = field;
			this.offset = UnsafeDeserializer.unsafe.objectFieldOffset(this.field);
		}

		@Override
		@SuppressWarnings("restriction")
		protected void deserialize(AbstractHessianInput in, Object obj) throws IOException {
			int value = 0;

			try {
				value = in.readInt();

				UnsafeDeserializer.unsafe.putInt(obj, this.offset, value);
			} catch (Exception e) {
				UnsafeDeserializer.logDeserializeError(this.field, obj, value, e);
			}
		}
	}

	public static class LongFieldDeserializer extends FieldDeserializer {

		private final Field	field;
		private final long	offset;

		@SuppressWarnings("restriction")
		public LongFieldDeserializer(Field field) {
			this.field = field;
			this.offset = UnsafeDeserializer.unsafe.objectFieldOffset(this.field);
		}

		@Override
		@SuppressWarnings("restriction")
		protected void deserialize(AbstractHessianInput in, Object obj) throws IOException {
			long value = 0;

			try {
				value = in.readLong();

				UnsafeDeserializer.unsafe.putLong(obj, this.offset, value);
			} catch (Exception e) {
				UnsafeDeserializer.logDeserializeError(this.field, obj, value, e);
			}
		}
	}

	public static class FloatFieldDeserializer extends FieldDeserializer {

		private final Field	_field;
		private final long	_offset;

		@SuppressWarnings("restriction")
		public FloatFieldDeserializer(Field field) {
			this._field = field;
			this._offset = UnsafeDeserializer.unsafe.objectFieldOffset(this._field);
		}

		@Override
		protected void deserialize(AbstractHessianInput in, Object obj) throws IOException {
			double value = 0;

			try {
				value = in.readDouble();

				UnsafeDeserializer.unsafe.putFloat(obj, this._offset, (float) value);
			} catch (Exception e) {
				UnsafeDeserializer.logDeserializeError(this._field, obj, value, e);
			}
		}
	}

	public static class DoubleFieldDeserializer extends FieldDeserializer {

		private final Field	field;
		private final long	offset;

		@SuppressWarnings("restriction")
		public DoubleFieldDeserializer(Field field) {
			this.field = field;
			this.offset = UnsafeDeserializer.unsafe.objectFieldOffset(this.field);
		}

		@Override
		@SuppressWarnings("restriction")
		protected void deserialize(AbstractHessianInput in, Object obj) throws IOException {
			double value = 0;

			try {
				value = in.readDouble();

				UnsafeDeserializer.unsafe.putDouble(obj, this.offset, value);
			} catch (Exception e) {
				UnsafeDeserializer.logDeserializeError(this.field, obj, value, e);
			}
		}
	}

	public static class StringFieldDeserializer extends FieldDeserializer {

		private final Field	field;
		private final long	offset;

		@SuppressWarnings("restriction")
		public StringFieldDeserializer(Field field) {
			this.field = field;
			this.offset = UnsafeDeserializer.unsafe.objectFieldOffset(this.field);
		}

		@Override
		@SuppressWarnings("restriction")
		protected void deserialize(AbstractHessianInput in, Object obj) throws IOException {
			String value = null;

			try {
				value = in.readString();

				UnsafeDeserializer.unsafe.putObject(obj, this.offset, value);
			} catch (Exception e) {
				UnsafeDeserializer.logDeserializeError(this.field, obj, value, e);
			}
		}
	}

	public static class SqlDateFieldDeserializer extends FieldDeserializer {

		private final Field	field;
		private final long	offset;

		@SuppressWarnings("restriction")
		public SqlDateFieldDeserializer(Field field) {
			this.field = field;
			this.offset = UnsafeDeserializer.unsafe.objectFieldOffset(this.field);
		}

		@Override
		@SuppressWarnings("restriction")
		protected void deserialize(AbstractHessianInput in, Object obj) throws IOException {
			java.sql.Date value = null;
			try {
				java.util.Date date = (java.util.Date) in.readObject();
				if (date != null) {
					value = new java.sql.Date(date.getTime());
				}
				UnsafeDeserializer.unsafe.putObject(obj, this.offset, value);
			} catch (Exception e) {
				UnsafeDeserializer.logDeserializeError(this.field, obj, value, e);
			}
		}
	}

	public static class SqlTimestampFieldDeserializer extends FieldDeserializer {

		private final Field	field;
		private final long	offset;

		@SuppressWarnings("restriction")
		public SqlTimestampFieldDeserializer(Field field) {
			this.field = field;
			this.offset = UnsafeDeserializer.unsafe.objectFieldOffset(this.field);
		}

		@Override
		@SuppressWarnings("restriction")
		protected void deserialize(AbstractHessianInput in, Object obj) throws IOException {
			java.sql.Timestamp value = null;

			try {
				java.util.Date date = (java.util.Date) in.readObject();
				if (date != null) {
					value = new java.sql.Timestamp(date.getTime());
				}
				UnsafeDeserializer.unsafe.putObject(obj, this.offset, value);
			} catch (Exception e) {
				UnsafeDeserializer.logDeserializeError(this.field, obj, value, e);
			}
		}
	}

	public static class SqlTimeFieldDeserializer extends FieldDeserializer {

		private final Field	field;
		private final long	offset;

		@SuppressWarnings("restriction")
		public SqlTimeFieldDeserializer(Field field) {
			this.field = field;
			this.offset = UnsafeDeserializer.unsafe.objectFieldOffset(this.field);
		}

		@Override
		@SuppressWarnings("restriction")
		protected void deserialize(AbstractHessianInput in, Object obj) throws IOException {
			java.sql.Time value = null;

			try {
				java.util.Date date = (java.util.Date) in.readObject();

				if (date != null) {
					value = new java.sql.Time(date.getTime());
				}
				UnsafeDeserializer.unsafe.putObject(obj, this.offset, value);
			} catch (Exception e) {
				UnsafeDeserializer.logDeserializeError(this.field, obj, value, e);
			}
		}
	}

	protected static void logDeserializeError(Field field, Object obj, Object value, Throwable e) throws IOException {
		String fieldName = field.getDeclaringClass().getName() + "." + field.getName();

		if (e instanceof HessianFieldException) {
			throw (HessianFieldException) e;
		} else if (e instanceof IOException) {
			throw new HessianFieldException(fieldName + ": " + e.getMessage(), e);
		}

		if (value != null) {
			throw new HessianFieldException(fieldName + ": " + value.getClass().getName() + " (" + value + ")" + " cannot be assigned to '" + field.getType().getName() + "'", e);
		}
		throw new HessianFieldException(fieldName + ": " + field.getType().getName() + " cannot be assigned from null", e);
	}

	static {
		boolean isEnabled = false;

		try {
			Class<?> unsafe = Class.forName("sun.misc.Unsafe");
			Field theUnsafe = null;
			for (Field field : unsafe.getDeclaredFields()) {
				if ("theUnsafe".equals(field.getName())) {
					theUnsafe = field;
				}
			}

			if (theUnsafe != null) {
				theUnsafe.setAccessible(true);
				UnsafeDeserializer.unsafe = (Unsafe) theUnsafe.get(null);
			}

			isEnabled = UnsafeDeserializer.unsafe != null;

			String unsafeProp = System.getProperty("com.caucho.hessian.unsafe");

			if ("false".equals(unsafeProp)) {
				isEnabled = false;
			}
		} catch (Exception e) {
			UnsafeDeserializer.logger.trace(e.toString(), e);
		}

		UnsafeDeserializer.isEnabled = isEnabled;
	}
}

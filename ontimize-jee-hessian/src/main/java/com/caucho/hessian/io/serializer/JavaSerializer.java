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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.HessianUnshared;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.IOExceptionWrapper;

/**
 * Serializing an object for known object types.
 */
public class JavaSerializer extends AbstractSerializer {

	private static final Logger													logger			= LoggerFactory.getLogger(JavaSerializer.class);
	private static final WeakHashMap<Class<?>, SoftReference<JavaSerializer>>	SERIALIZER_MAP	= new WeakHashMap<>();

	private Field[]																fields;
	private FieldSerializer[]													fieldSerializers;

	private Object																writeReplaceFactory;
	private final Method														writeReplace;

	public JavaSerializer(Class<?> cl) {
		this.introspect(cl);

		this.writeReplace = JavaSerializer.getWriteReplace(cl);

		if (this.writeReplace != null) {
			this.writeReplace.setAccessible(true);
		}
	}

	public static Serializer create(Class<?> cl) {
		synchronized (JavaSerializer.SERIALIZER_MAP) {
			SoftReference<JavaSerializer> baseRef = JavaSerializer.SERIALIZER_MAP.get(cl);

			JavaSerializer base = baseRef != null ? baseRef.get() : null;

			if (base == null) {
				if (cl.isAnnotationPresent(HessianUnshared.class)) {
					base = new JavaUnsharedSerializer(cl);
				} else {
					base = new JavaSerializer(cl);
				}

				baseRef = new SoftReference<>(base);
				JavaSerializer.SERIALIZER_MAP.put(cl, baseRef);
			}

			return base;
		}
	}

	protected void introspect(Class<?> cl) {
		if (this.writeReplace != null) {
			this.writeReplace.setAccessible(true);
		}

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
			this.fieldSerializers[i] = JavaSerializer.getFieldSerializer(this.fields[i].getType());
		}
	}

	/**
	 * Returns the writeReplace method
	 */
	public static Method getWriteReplace(Class<?> cl) {
		for (; cl != null; cl = cl.getSuperclass()) {
			Method[] methods = cl.getDeclaredMethods();

			for (int i = 0; i < methods.length; i++) {
				Method method = methods[i];

				if ("writeReplace".equals(method.getName()) && (method.getParameterTypes().length == 0)) {
					return method;
				}
			}
		}

		return null;
	}

	/**
	 * Returns the writeReplace method
	 */
	public Method getWriteReplace(Class<?> cl, Class<?> param) {
		for (; cl != null; cl = cl.getSuperclass()) {
			for (Method method : cl.getDeclaredMethods()) {
				if ("writeReplace".equals(method.getName()) && (method.getParameterTypes().length == 1) && param.equals(method.getParameterTypes()[0])) {
					return method;
				}
			}
		}

		return null;
	}

	@Override
	public void writeObject(Object obj, AbstractHessianOutput out) throws IOException {
		if (out.addRef(obj)) {
			return;
		}

		Class<?> cl = obj.getClass();

		try {
			if (this.writeReplace != null) {
				Object repl;

				if (this.writeReplaceFactory != null) {
					repl = this.writeReplace.invoke(this.writeReplaceFactory, obj);
				} else {
					repl = this.writeReplace.invoke(obj);
				}

				// out.removeRef(obj);

				/*
				 * out.writeObject(repl); out.replaceRef(repl, obj);
				 */

				// hessian/3a5a
				int ref = out.writeObjectBegin(cl.getName());

				if (ref < -1) {
					this.writeObject10(repl, out);
				} else {
					if (ref == -1) {
						this.writeDefinition20(out);
						out.writeObjectBegin(cl.getName());
					}

					this.writeInstance(repl, out);
				}

				return;
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			// log.log(Level.FINE, e.toString(), e);
			throw new RuntimeException(e);
		}

		int ref = out.writeObjectBegin(cl.getName());

		if (ref < -1) {
			this.writeObject10(obj, out);
		} else {
			if (ref == -1) {
				this.writeDefinition20(out);
				out.writeObjectBegin(cl.getName());
			}

			this.writeInstance(obj, out);
		}
	}

	@Override
	protected void writeObject10(Object obj, AbstractHessianOutput out) throws IOException {
		for (int i = 0; i < this.fields.length; i++) {
			Field field = this.fields[i];

			out.writeString(field.getName());

			this.fieldSerializers[i].serialize(out, obj, field);
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
	public void writeInstance(Object obj, AbstractHessianOutput out) throws IOException {
		try {
			for (int i = 0; i < this.fields.length; i++) {
				Field field = this.fields[i];

				this.fieldSerializers[i].serialize(out, obj, field);
			}
		} catch (RuntimeException e) {
			throw new RuntimeException(e.getMessage() + "\n class: " + obj.getClass().getName() + " (object=" + obj.getClass().getName() + ")", e);
		} catch (IOException e) {
			throw new IOExceptionWrapper(e.getMessage() + "\n class: " + obj.getClass().getName() + " (object=" + obj.getClass().getName() + ")", e);
		}
	}

	public Field[] getFields() {
		return this.fields;
	}

	public void setFields(Field[] fields) {
		this.fields = fields;
	}

	public void setFieldSerializers(FieldSerializer[] fieldSerializers) {
		this.fieldSerializers = fieldSerializers;
	}

	public static FieldSerializer getFieldSerializer(Class<?> type) {
		if (int.class.equals(type) || byte.class.equals(type) || short.class.equals(type)) {
			return IntFieldSerializer.SER;
		} else if (long.class.equals(type)) {
			return LongFieldSerializer.SER;
		} else if (double.class.equals(type) || float.class.equals(type)) {
			return DoubleFieldSerializer.SER;
		} else if (boolean.class.equals(type)) {
			return BooleanFieldSerializer.SER;
		} else if (String.class.equals(type)) {
			return StringFieldSerializer.SER;
		} else if (java.util.Date.class.equals(type) || java.sql.Date.class.equals(type) || java.sql.Timestamp.class.equals(type) || java.sql.Time.class.equals(type)) {
			return DateFieldSerializer.SER;
		} else {
			return FieldSerializer.SER;
		}
	}

	public static class FieldSerializer {

		static final FieldSerializer SER = new FieldSerializer();

		protected void serialize(AbstractHessianOutput out, Object obj, Field field) throws IOException {
			Object value = null;

			try {
				value = field.get(obj);
			} catch (IllegalAccessException e) {
				JavaSerializer.logger.info(e.toString(), e);
			}

			try {
				out.writeObject(value);
			} catch (RuntimeException e) {
				throw new RuntimeException(e.getMessage() + "\n field: " + field.getDeclaringClass().getName() + '.' + field.getName(), e);
			} catch (IOException e) {
				throw new IOExceptionWrapper(e.getMessage() + "\n field: " + field.getDeclaringClass().getName() + '.' + field.getName(), e);
			}
		}
	}

	public static class BooleanFieldSerializer extends FieldSerializer {

		static final FieldSerializer SER = new BooleanFieldSerializer();

		@Override
		protected void serialize(AbstractHessianOutput out, Object obj, Field field) throws IOException {
			boolean value = false;

			try {
				value = field.getBoolean(obj);
			} catch (IllegalAccessException e) {
				JavaSerializer.logger.info(e.toString(), e);
			}

			out.writeBoolean(value);
		}
	}

	public static class IntFieldSerializer extends FieldSerializer {

		static final FieldSerializer SER = new IntFieldSerializer();

		@Override
		protected void serialize(AbstractHessianOutput out, Object obj, Field field) throws IOException {
			int value = 0;

			try {
				value = field.getInt(obj);
			} catch (IllegalAccessException e) {
				JavaSerializer.logger.info(e.toString(), e);
			}

			out.writeInt(value);
		}
	}

	public static class LongFieldSerializer extends FieldSerializer {

		static final FieldSerializer SER = new LongFieldSerializer();

		@Override
		protected void serialize(AbstractHessianOutput out, Object obj, Field field) throws IOException {
			long value = 0;

			try {
				value = field.getLong(obj);
			} catch (IllegalAccessException e) {
				JavaSerializer.logger.info(e.toString(), e);
			}

			out.writeLong(value);
		}
	}

	public static class DoubleFieldSerializer extends FieldSerializer {

		static final FieldSerializer SER = new DoubleFieldSerializer();

		@Override
		protected void serialize(AbstractHessianOutput out, Object obj, Field field) throws IOException {
			double value = 0;

			try {
				value = field.getDouble(obj);
			} catch (IllegalAccessException e) {
				JavaSerializer.logger.info(e.toString(), e);
			}

			out.writeDouble(value);
		}
	}

	public static class StringFieldSerializer extends FieldSerializer {

		static final FieldSerializer SER = new StringFieldSerializer();

		@Override
		protected void serialize(AbstractHessianOutput out, Object obj, Field field) throws IOException {
			String value = null;

			try {
				value = (String) field.get(obj);
			} catch (IllegalAccessException e) {
				JavaSerializer.logger.info(e.toString(), e);
			}

			out.writeString(value);
		}
	}

	public static class DateFieldSerializer extends FieldSerializer {

		static final FieldSerializer SER = new DateFieldSerializer();

		@Override
		protected void serialize(AbstractHessianOutput out, Object obj, Field field) throws IOException {
			java.util.Date value = null;

			try {
				value = (java.util.Date) field.get(obj);
			} catch (IllegalAccessException e) {
				JavaSerializer.logger.info(e.toString(), e);
			}

			if (value == null) {
				out.writeNull();
			} else {
				out.writeUTCDate(value.getTime());
			}
		}
	}
}

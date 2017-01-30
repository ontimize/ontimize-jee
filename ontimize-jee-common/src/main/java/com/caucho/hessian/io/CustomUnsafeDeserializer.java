/**
 * CustomUnsafeDeserializer.java 18-jul-2013
 *
 *
 *
 */
package com.caucho.hessian.io;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * The Class CustomUnsafeDeserializer.
 *
 * @author <a href=""></a>
 */
public class CustomUnsafeDeserializer extends UnsafeDeserializer {

	/**
	 * Instancia un nuevo custom unsafe deserializer.
	 *
	 * @param cl
	 *            cl
	 */
	public CustomUnsafeDeserializer(final Class<?> cl) {
		super(cl);
	}

	/**
	 * Creates a map of the classes fields.
	 *
	 * @param cl
	 *            cl
	 * @return field map
	 */
	@Override
	protected HashMap<String, FieldDeserializer> getFieldMap(Class<?> cl) {
		HashMap<String, FieldDeserializer> fieldMap = new HashMap<String, FieldDeserializer>();

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
				} catch (Throwable e) {
					// do nothing
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
					deser = new CustomSqlDateFieldDeserializer(field);
				} else if (java.sql.Timestamp.class.equals(type)) {
					deser = new CustomSqlTimestampFieldDeserializer(field);
				} else if (java.sql.Time.class.equals(type)) {
					deser = new CustomSqlTimeFieldDeserializer(field);
				} else {
					deser = new ObjectFieldDeserializer(field);
				}

				fieldMap.put(field.getName(), deser);
			}
		}

		return fieldMap;
	}

	/**
	 * @return the _unsafe
	 */
	private static Object getUnsafe() {
		try {
			Field field = UnsafeDeserializer.class.getDeclaredField("_unsafe");
			field.setAccessible(true);
			return field.get(null);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 *
	 * @param f
	 * @return
	 */
	private static long getObjectFieldOffset(final Field f) {
		try {
			Object unsafe = CustomUnsafeDeserializer.getUnsafe();
			Method declaredMethod = unsafe.getClass().getDeclaredMethod("objectFieldOffset", new Class<?>[] { Field.class });
			return (Long) declaredMethod.invoke(unsafe, f);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 *
	 *
	 * @param ob
	 * @param l
	 * @param ob2
	 */
	private static void putObject(final Object ob, final long l, final Object ob2) {
		try {
			Object unsafe = CustomUnsafeDeserializer.getUnsafe();
			Method declaredMethod = unsafe.getClass().getDeclaredMethod("putObject", new Class<?>[] { Object.class, Long.TYPE, Object.class });
			declaredMethod.invoke(unsafe, ob, l, ob2);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Sobrecargada para soportar nulos
	 *
	 * @author <a href=""></a>
	 *
	 */
	private static class CustomSqlDateFieldDeserializer extends FieldDeserializer {
		/** el campo */
		private final Field	field;
		/** el offset */
		private final long	offset;

		/**
		 * constructor
		 *
		 * @param field
		 */
		CustomSqlDateFieldDeserializer(final Field field) {
			this.field = field;
			this.offset = CustomUnsafeDeserializer.getObjectFieldOffset(this.field);
		}

		/**
		 *
		 * @param in
		 * @param obj
		 * @throws IOException
		 */
		@Override
		void deserialize(final AbstractHessianInput in, final Object obj) throws IOException {
			java.sql.Date value = null;

			try {
				java.util.Date date = (java.util.Date) in.readObject();
				if (date != null) {
					value = new java.sql.Date(date.getTime());
				}
				CustomUnsafeDeserializer.putObject(obj, this.offset, value);
			} catch (Exception e) {
				UnsafeDeserializer.logDeserializeError(this.field, obj, value, e);
			}
		}
	}

	/**
	 * Sobrecargada para soportar nulos
	 *
	 * @author <a href=""></a>
	 *
	 */
	private static class CustomSqlTimestampFieldDeserializer extends FieldDeserializer {
		/** el campo */
		private final Field	field;
		/** el offset */
		private final long	offset;

		/**
		 * Constructor
		 *
		 * @param field
		 */
		CustomSqlTimestampFieldDeserializer(final Field field) {
			this.field = field;
			this.offset = CustomUnsafeDeserializer.getObjectFieldOffset(this.field);
		}

		/**
		 *
		 * @param in
		 * @param obj
		 * @throws IOException
		 */
		@Override
		void deserialize(final AbstractHessianInput in, final Object obj) throws IOException {
			java.sql.Timestamp value = null;

			try {
				java.util.Date date = (java.util.Date) in.readObject();
				if (date != null) {
					value = new java.sql.Timestamp(date.getTime());
				}

				CustomUnsafeDeserializer.putObject(obj, this.offset, value);
			} catch (Exception e) {
				UnsafeDeserializer.logDeserializeError(this.field, obj, value, e);
			}
		}
	}

	/**
	 * Sobrecargada para soportar nulos
	 *
	 * @author <a href=""></a>
	 *
	 */
	private static class CustomSqlTimeFieldDeserializer extends FieldDeserializer {
		/** el campo */
		private final Field	field;
		/** el offset */
		private final long	offset;

		/**
		 * constructor
		 *
		 * @param field
		 */
		CustomSqlTimeFieldDeserializer(final Field field) {
			this.field = field;
			this.offset = CustomUnsafeDeserializer.getObjectFieldOffset(this.field);
		}

		/**
		 *
		 * @param in
		 * @param obj
		 * @throws IOException
		 */
		@Override
		void deserialize(final AbstractHessianInput in, final Object obj) throws IOException {
			java.sql.Time value = null;

			try {
				java.util.Date date = (java.util.Date) in.readObject();
				if (date != null) {
					value = new java.sql.Time(date.getTime());
				}

				CustomUnsafeDeserializer.putObject(obj, this.offset, value);
			} catch (Exception e) {
				UnsafeDeserializer.logDeserializeError(this.field, obj, value, e);
			}
		}
	}
}

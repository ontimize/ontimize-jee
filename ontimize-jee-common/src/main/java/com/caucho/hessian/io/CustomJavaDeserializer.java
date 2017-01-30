/**
 * CustomJavaDeserializer.java 17/07/2013
 *
 *
 *
 */
package com.caucho.hessian.io;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * The Class CustomJavaDeserializer.
 *
 * @author <a href=""></a>
 */
public class CustomJavaDeserializer extends JavaDeserializer {

	/**
	 * Instancia un nuevo custom java deserializer.
	 *
	 * @param cl
	 *            cl
	 */
	public CustomJavaDeserializer(final Class<?> cl) {
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
	protected HashMap<String, FieldDeserializer> getFieldMap(Class cl) {
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
	 * Sobrecargada para soportar nulos
	 *
	 * @author <a href=""></a>
	 *
	 */
	private static class CustomSqlDateFieldDeserializer extends FieldDeserializer {
		/** el campo */
		private final Field	field;

		/**
		 * Constructor
		 *
		 * @param field
		 */
		CustomSqlDateFieldDeserializer(final Field field) {
			this.field = field;
		}

		/**
		 *
		 *
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

				this.field.set(obj, value);
			} catch (Exception e) {
				JavaDeserializer.logDeserializeError(this.field, obj, value, e);
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
		private final Field	field;

		/**
		 * Constructor
		 *
		 * @param field
		 */
		CustomSqlTimestampFieldDeserializer(final Field field) {
			this.field = field;
		}

		/**
		 *
		 *
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

				this.field.set(obj, value);
			} catch (Exception e) {
				JavaDeserializer.logDeserializeError(this.field, obj, value, e);
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

		/**
		 * Constructor
		 *
		 * @param field
		 */
		CustomSqlTimeFieldDeserializer(final Field field) {
			this.field = field;
		}

		/**
		 *
		 *
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

				this.field.set(obj, value);
			} catch (Exception e) {
				JavaDeserializer.logDeserializeError(this.field, obj, value, e);
			}
		}
	}

}

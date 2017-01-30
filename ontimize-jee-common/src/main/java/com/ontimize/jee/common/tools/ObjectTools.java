/**
 * ObjectTools.java 31/07/2013
 *
 *
 *
 */
package com.ontimize.jee.common.tools;

import java.util.List;
import java.util.Map;

/**
 * Utilidades de objetos.
 *
 * @author <a href=""></a>
 */
public final class ObjectTools {

	/**
	 * Instantiates a new object tools.
	 */
	private ObjectTools() {
		super();
	}

	/**
	 * Si dos objetos comparables son diferentes lanza excepción.
	 *
	 * @param oba
	 *            oba
	 * @param obb
	 *            obb
	 * @throws ObjectNotEqualsException
	 *             de object not equals exception
	 */
	public static void isEquals(final Object oba, final Object obb) throws ObjectNotEqualsException {
		boolean equals = true;
		if (oba == null) {
			equals = (obb == null);
		} else {
			equals = oba.equals(obb);
		}
		if (!equals) {
			throw new ObjectNotEqualsException();
		}
	}

	/**
	 * Si dos objetos comparables son diferentes devuelve false.
	 *
	 * @param oba
	 *            oba
	 * @param obb
	 *            obb
	 * @return true, if successful
	 */
	public static boolean safeIsEquals(final Object oba, final Object obb) {
		boolean equals = true;
		if (oba == null) {
			equals = (obb == null);
		} else {
			equals = oba.equals(obb);
		}
		return equals;
	}

	/**
	 * returns first not null.
	 *
	 * @param <T>
	 *            the generic type
	 * @param values
	 *            the values
	 * @return the t
	 */
	public static <T> T coalesce(T... values) {
		if (values == null) {
			return null;
		}
		for (T ob : values) {
			if (ob != null) {
				return ob;
			}
		}
		return null;
	}

	/**
	 * Check if in this map exists this object (null included), case insensitive.
	 *
	 * @param map
	 *            the map
	 * @param object
	 *            the object
	 * @return true, if successful
	 */
	public static boolean containsIgnoreCase(Map<?, ?> map, String object) {
		if (map == null) {
			return false;
		}
		for (Object o : map.keySet()) {
			if (ObjectTools.safeIsEquals(o, object) || ((o != null) && (object != null) && o.toString().toUpperCase().equals(object.toUpperCase()))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if in this list exists this object (null included), case insensitice.
	 *
	 * @param list
	 *            the list
	 * @param object
	 *            the object
	 * @return true, if successful
	 */
	public static boolean containsIgnoreCase(List<?> list, String object) {
		if (list == null) {
			return false;
		}
		for (Object o : list) {
			if (ObjectTools.safeIsEquals(o, object) || ((o != null) && (object != null) && o.toString().toUpperCase().equals(object.toUpperCase()))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Decode.
	 *
	 * @param values
	 *            the values
	 * @return the object
	 */
	public static Object decode(Object... values) {
		if ((values == null) || (values.length < 2)) {
			return null;
		}
		Object check = values[0];
		for (int i = 1; i < (values.length - 1); i += 2) {
			if (ObjectTools.safeIsEquals(check, values[i])) {
				return values[i + 1];
			}
		}
		return values[values.length - 1];
	}

	/**
	 * Checks if is in.
	 *
	 * @param check
	 *            the check
	 * @param options
	 *            the options
	 * @return true, if is in
	 */
	public static boolean isIn(Object check, Object... options) {
		for (Object op : options) {
			if (ObjectTools.safeIsEquals(check, op)) {
				return true;
			}
		}
		return false;
	}
}

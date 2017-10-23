package com.ontimize.jee.common.tools;

/**
 * The Class ConcatTools.
 */
public final class ConcatTools {

	/**
	 * Concat.
	 *
	 * @param objects
	 *            objects
	 * @return the string
	 */
	public static String concat(final Object... objects) {
		return ConcatTools.concatWithNullValue("null", objects);
	}

	/**
	 * Concat with null value.
	 *
	 * @param nullValue
	 *            the null value
	 * @param objects
	 *            the objects
	 * @return the string
	 */
	public static String concatWithNullValue(String nullValue, final Object... objects) {
		final StringBuilder sb = new StringBuilder();
		if (objects != null) {
			for (final Object o : objects) {
				sb.append(ConcatTools.normalizeObject(nullValue, o));
			}
		}
		return sb.toString();
	}

	/**
	 * Normalize object.
	 *
	 * @param nullValue
	 *            the null value
	 * @param o
	 *            o
	 * @return the object
	 */
	private static Object normalizeObject(String nullValue, final Object o) {
		Object ret = o;
		if (o == null) {
			ret = nullValue;
		}
		return ret;
	}

	/**
	 * Instantiates a new concat tools.
	 */
	private ConcatTools() {
		// Do nothing
	}
}

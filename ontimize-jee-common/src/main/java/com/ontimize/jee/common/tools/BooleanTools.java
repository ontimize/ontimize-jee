package com.ontimize.jee.common.tools;

/**
 * The Class BooleanTools.
 */
public final class BooleanTools {

	/**
	 * Instantiates a new boolean tools.
	 */
	private BooleanTools() {
		super();
	}

	/**
	 * To boolean.
	 *
	 * @param ob
	 *            the ob
	 * @param valueIfNull
	 *            the value if null
	 * @return the boolean
	 */
	public static Boolean toBoolean(Object ob, Boolean valueIfNull) {
		if (ob == null) {
			return valueIfNull;
		}
		if (ob instanceof String) {
			return Boolean.valueOf((String) ob);
		} else if (ob instanceof Number) {
			return ((Number) ob).intValue() > 0;
		} else if (ob instanceof Boolean) {
			return (Boolean) ob;
		} else {
			throw new IllegalArgumentException(ob.getClass().getName());
		}
	}

}

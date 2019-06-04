package com.ontimize.jee.common.tools;

/**
 * The Class ArraysTools.
 */
public final class ArraysTools {

	/**
	 * Instantiates a new arrays tools.
	 */
	private ArraysTools() {
		super();
	}

	/**
	 * Contains.
	 *
	 * @param args
	 *            the args
	 * @param toCheck
	 *            the to check
	 * @return true, if successful
	 */
	public static boolean contains(Object[] args, String toCheck) {
		for (Object ob : args) {
			if (ObjectTools.safeIsEquals(ob, toCheck)) {
				return true;
			}
		}
		return false;
	}
}

/*
 *
 */
package com.ontimize.jee.common.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * The Class ListUtils.
 */
public class ListTools {

	/**
	 * Elimina los valores duplicados de una lista.
	 *
	 * @param <T>
	 *            the generic type
	 * @param in
	 *            the in
	 * @return the list
	 */
	public static <T> List<T> removeDuplicates(List<T> in) {
		HashSet<T> set = new HashSet<T>(in);
		return new ArrayList<T>(set);
	}

	/**
	 * Metodo de ayuda para saber si un objeto esta contenido en un array de objetos.
	 *
	 * @param value
	 *            the value
	 * @param test
	 *            the test
	 * @return true, if successful
	 */
	public static boolean in(final Object value, final Object... test) {
		if (value == null) {
			return false;
		}
		for (Object ob : test) {
			if (value.equals(ob)) {
				return true;
			}
		}
		return false;
	}
}

/**
 * EntityEnumTranslator.java 15-oct-2014
 *
 * Copyright 2014 Imatia.
 */
package com.ontimize.jee.common.integration;

/**
 * The Interface EntityEnumTranslator.
 *
 * @author <a href="luis.garcia@imatia.com">Luis Garcia</a>
 * @param <T>
 *            the generic type
 * @param <S>
 *            the generic type
 */
public interface EntityEnumTranslator<S extends EntityEnum, T extends EntityEnum> {

	/**
	 * Translate.
	 *
	 * @param s
	 *            the s
	 * @return the t
	 */
	T translate(S s);
}

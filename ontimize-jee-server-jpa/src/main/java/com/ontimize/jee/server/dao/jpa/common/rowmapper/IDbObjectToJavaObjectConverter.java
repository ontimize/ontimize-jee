/**
 * IDbObjectToJavaObjectConverter.java 17/03/2014
 *
 * Copyright 2014 IMATIA. Departamento de Sistemas
 */
package com.ontimize.jee.server.dao.jpa.common.rowmapper;

import com.ontimize.jee.server.dao.jpa.common.rowmapper.exceptions.RowMapperException;

/**
 * The Interface IDbObjectToJavaObjectConverter.
 *
 * @author <a href="">Sergio Padin</a>
 */
public interface IDbObjectToJavaObjectConverter {

	/**
	 * Convert.
	 *
	 * @param input
	 *            input
	 * @param toType
	 *            to type
	 * @return the object
	 * @throws RowMapperException
	 *             de row mapper exception
	 */
	Object convert(Object input, Class<?> toType) throws RowMapperException;
}

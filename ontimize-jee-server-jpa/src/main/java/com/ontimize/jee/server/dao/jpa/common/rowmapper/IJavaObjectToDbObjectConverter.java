/**
 * IDbObjectToJavaObjectConverter.java 17/03/2014
 *
 * Copyright 2014 IMATIA.
 * Departamento de Sistemas
 */
package com.ontimize.jee.server.dao.jpa.common.rowmapper;

/**
 * The Interface IDbObjectToJavaObjectConverter.
 * 
 * @author <a href="">Sergio Padin</a>
 */
public interface IJavaObjectToDbObjectConverter {

    /**
     * Convert.
     * 
     * @param input
     *            input
     * @param toDbType
     *            to db type
     * @return the object
     */
    Object convert(Object input, String toDbType);
}

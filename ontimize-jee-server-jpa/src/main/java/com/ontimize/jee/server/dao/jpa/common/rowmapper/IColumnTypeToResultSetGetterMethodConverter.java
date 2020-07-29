/**
 * ColumnTypeToResultSetGetterMethodConverter.java 17/03/2014
 *
 * Copyright 2014 IMATIA. Departamento de Sistemas
 */
package com.ontimize.jee.server.dao.jpa.common.rowmapper;

/**
 * The Interface ColumnTypeToResultSetGetterMethodConverter.
 *
 * @author <a href="">Sergio Padin</a>
 */
public interface IColumnTypeToResultSetGetterMethodConverter {

    /**
     * Obtiene result set getter method from db column type.
     * @param dbColumnType db column type
     * @return result set getter method from db column type
     */
    String getResultSetGetterMethodFromDBColumnType(String dbColumnType);

}

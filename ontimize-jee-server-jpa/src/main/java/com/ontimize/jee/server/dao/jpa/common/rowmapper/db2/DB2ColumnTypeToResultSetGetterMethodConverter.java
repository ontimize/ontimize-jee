/**
 * DB2ColumnTypeToResultSetGetterMethodConverter.java 17/03/2014
 *
 * Copyright 2014 IMATIA.
 * Departamento de Sistemas
 */
package com.ontimize.jee.server.dao.jpa.common.rowmapper.db2;

import java.util.HashMap;
import java.util.Map;

import com.ontimize.jee.server.dao.jpa.common.rowmapper.IColumnTypeToResultSetGetterMethodConverter;

/**
 * The Class DB2ColumnTypeToResultSetGetterMethodConverter.
 * 
 * @author <a href="">Sergio Padin</a>
 */
public class DB2ColumnTypeToResultSetGetterMethodConverter implements IColumnTypeToResultSetGetterMethodConverter {

    private static final String STRING_TYPE = "VARCHAR";
    private static final String CLOB_TYPE = "CLOB";
    private static final String DATE_TYPE = "DATE";
    private static final String TIME_TYPE = "TIME";
    private static final String TIMESTAMP_TYPE = "TIMESTAMP";

    private final static Map<String, String> columnTypeToGetter;

    static {
        columnTypeToGetter = new HashMap<String, String>();
        DB2ColumnTypeToResultSetGetterMethodConverter.columnTypeToGetter.put(DB2ColumnTypeToResultSetGetterMethodConverter.STRING_TYPE, "getString");
        DB2ColumnTypeToResultSetGetterMethodConverter.columnTypeToGetter.put(DB2ColumnTypeToResultSetGetterMethodConverter.CLOB_TYPE, "getClob");
        DB2ColumnTypeToResultSetGetterMethodConverter.columnTypeToGetter.put(DB2ColumnTypeToResultSetGetterMethodConverter.DATE_TYPE, "getDate");
        DB2ColumnTypeToResultSetGetterMethodConverter.columnTypeToGetter.put(DB2ColumnTypeToResultSetGetterMethodConverter.TIME_TYPE, "getTime");
        DB2ColumnTypeToResultSetGetterMethodConverter.columnTypeToGetter.put(DB2ColumnTypeToResultSetGetterMethodConverter.TIMESTAMP_TYPE, "getTimestamp");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResultSetGetterMethodFromDBColumnType(final String dbColumnType) {
        String getter = DB2ColumnTypeToResultSetGetterMethodConverter.columnTypeToGetter.get(dbColumnType);
        if (getter == null) {
            getter = "getObject";
        }
        return getter;
    }

}

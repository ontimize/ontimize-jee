package com.ontimize.jee.server.dao.common;

import java.math.BigDecimal;
import java.sql.Types;

public class SQLTypeMap {

    /**
     * Translates a data type from an integer (java.sql.Types value) to a string that represents the
     * corresponding class.
     * @param type The java.sql.Types value to convert to a string representation.
     * @return The class name that corresponds to the given java.sql.Types value, or "java.lang.Object"
     *         if the type has no known mapping.
     */
    public static Class<?> convert(int type) {
        Class<?> result = Object.class;

        switch (type) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                result = String.class;
                break;

            case Types.NUMERIC:
            case Types.DECIMAL:
                result = BigDecimal.class;
                break;

            case Types.BIT:
                result = Boolean.class;
                break;

            case Types.TINYINT:
                result = Byte.class;
                break;

            case Types.SMALLINT:
                result = Short.class;
                break;

            case Types.INTEGER:
                result = Integer.class;
                break;

            case Types.BIGINT:
                result = Long.class;
                break;

            case Types.REAL:
                result = Double.class;
                break;

            case Types.FLOAT:
                result = Float.class;
                break;
            case Types.DOUBLE:
                result = Double.class;
                break;

            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                result = byte[].class;
                break;

            case Types.DATE:
                result = java.sql.Date.class;
                break;

            case Types.TIME:
                result = java.sql.Time.class;
                break;

            case Types.TIMESTAMP:
                result = java.sql.Timestamp.class;
                break;
        }

        return result;
    }

}

package com.ontimize.jee.common.db.sql;

import java.sql.Types;

public class MySQLSQLHandler extends AbstractSQLHandler {

    /**
     * Returns a string with the SQL type for MySQL's database.
     * @param SQLtype Int with the SQL Type
     * @return a <code>String</code>
     */
    @Override
    public String getSQLTypeName(int sqlType) throws Exception {

        String toret = this.sqlNumericType(sqlType);
        if (toret == null) {
            toret = this.sqlDateAndTimeType(sqlType);
            if (toret == null) {
                toret = this.sqlStringType(sqlType);
                if (toret == null) {
                    toret = this.sqlOtherType(sqlType);
                }
            }

        }

        if (toret != null) {
            return toret;
        }

        throw new Exception("Invalid type");
    }

    protected String sqlNumericType(int sqlType) {

        switch (sqlType) {

            case Types.BIT: // -7
            case Types.BOOLEAN: // 16
                return "tinyint(1)";

            case Types.TINYINT: // -6
            case Types.SMALLINT: // 5
                return "smallint";

            case Types.BIGINT: // -5
                return "bigint";

            case Types.NUMERIC: // 2
            case Types.DECIMAL: // 3
                return "decimal";

            case Types.INTEGER: // 4
                return "integer";

            case Types.FLOAT: // 6
            case Types.DOUBLE: // 8
                return "double";

            case Types.REAL: // 7
                return "float";

            default:
                return null;
        }

    }

    protected String sqlDateAndTimeType(int sqlType) {

        switch (sqlType) {

            case Types.DATE: // 91
                return "date";

            case Types.TIME: // 92
                return "time";

            case Types.TIMESTAMP: // 93
                return "datetime";

            default:
                return null;
        }

    }

    protected String sqlStringType(int sqlType) {

        switch (sqlType) {

            case Types.LONGVARCHAR: // -1
                return "mediumtext";

            case Types.CHAR: // 1
                return "char";

            case Types.VARCHAR: // 12
                return "varchar";

            case Types.CLOB: // 2005
                return "longtext";

            default:
                return null;
        }

    }

    protected String sqlOtherType(int sqlType) {
        switch (sqlType) {

            case Types.LONGVARBINARY: // -4
            case Types.NULL: // 0
            case Types.DATALINK: // 70
                return "mediumblob";

            case Types.VARBINARY: // -3
                return "varbinary";

            case Types.BINARY: // -2
                return "binary";

            case Types.OTHER: // 1111
            case Types.JAVA_OBJECT: // 2000
            case Types.DISTINCT: // 2001
            case Types.STRUCT: // 2002
            case Types.ARRAY: // 2003
            case Types.BLOB: // 2004
                return "longblob";

            case Types.REF: // 2006
                return "mediumblob";

            default:
                return null;
        }

    }

}

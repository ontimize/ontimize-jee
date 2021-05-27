package com.ontimize.jee.common.db.sql;

import java.sql.Types;

public class PostgreSQLHandler extends AbstractSQLHandler {

    /**
     * Returns a string with the SQL type for Postgre's database.
     * @param sqlType Int with the SQL Type
     * @return a <code>String</code>
     */
    @Override
    public String getSQLTypeName(int sqlType) throws Exception {

        String sqlTypeName = getCommonSQLTypeName(sqlType);
        if (sqlTypeName != null) {
            return sqlTypeName;
        }

        sqlTypeName = getByteaSQLTypeName(sqlType);
        if (sqlTypeName != null) {
            return sqlTypeName;
        }

        switch (sqlType) {
            case Types.CLOB:
                return "text";

            default:
                throw new Exception("Invalid type");
        }
    }

    protected String getCommonSQLTypeName(int sqlType) {
        String sqlTypeName = getTextSQLTypeName(sqlType);
        if (sqlTypeName != null) {
            return sqlTypeName;
        }

        sqlTypeName = getNumberSQLTypeName(sqlType);
        if (sqlTypeName != null) {
            return sqlTypeName;
        }

        sqlTypeName = getBooleanSQLTypeName(sqlType);
        if (sqlTypeName != null) {
            return sqlTypeName;
        }

        sqlTypeName = getDateSQLTypeName(sqlType);
        if (sqlTypeName != null) {
            return sqlTypeName;
        }
        return null;
    }

    protected String getTextSQLTypeName(int sqlType) {
        String result;
        switch (sqlType) {
            case Types.CHAR: // 1
                result = "char";
                break;
            case Types.LONGVARCHAR: // -1
                result = "text";
                break;
            case Types.VARCHAR: // 12
                result = "varchar"; // text too
                break;
            default:
                result = null;
                break;
        }
        return result;
    }

    protected String getNumberSQLTypeName(int sqlType) {
        String result;
        switch (sqlType) {
            case Types.BIGINT: // -5
                result = "bigint";
                break;
            case Types.DECIMAL: // 3
                result = "numeric";
                break;
            case Types.DOUBLE: // 8
                result = "double precision"; // money too
                break;
            case Types.FLOAT: // 6
                result = "double precision";
                break;
            case Types.INTEGER: // 4
                result = "integer"; // oid too
                break;
            case Types.NUMERIC: // 2
                result = "numeric";
                break;
            case Types.REAL: // 7
                result = "real";
                break;
            case Types.SMALLINT: // 5
                result = "smallint";
                break;
            case Types.TINYINT: // -6
                result = "smallint";
                break;
            default:
                result = null;
                break;
        }
        return result;
    }

    protected String getBooleanSQLTypeName(int sqlType) {
        String result;
        switch (sqlType) {
            case Types.BIT: // -7
                result = "boolean"; // bool too
                break;
            case Types.BOOLEAN: // 16
                result = "boolean";
                break;
            default:
                result = null;
                break;
        }
        return result;
    }

    protected String getDateSQLTypeName(int sqlType) {
        String result;
        switch (sqlType) {
            case Types.DATE: // 91
                result = "date";
                break;
            case Types.TIME: // 92
                result = "time"; // timetz too
                break;

            case Types.TIMESTAMP: // 93
                result = "timestamp";
                break;
            default:
                result = null;
                break;
        }
        return result;
    }

    protected String getByteaSQLTypeName(int sqlType) {
        String result;
        switch (sqlType) {
            case Types.ARRAY:
                result = "bytea";
                break;
            case Types.BINARY: // -2
                result = "bytea";
                break;
            case Types.BLOB:
                result = "bytea";
                break;
            case Types.DATALINK:
                result = "bytea";
                break;
            case Types.LONGVARBINARY: // -4
                result = "bytea";
                break;
            case Types.NULL:
                result = "bytea";
                break;
            case Types.VARBINARY: // -3
                result = "bytea";
                break;
            default:
                result = null;
                break;
        }
        return result;
    }

}

package com.ontimize.jee.core.common.db.sql;

import java.sql.Types;

public class OracleSQLHandler extends AbstractSQLHandler {

    /**
     * Returns a string with the SQL type for Oracle's database.
     * @param sqlType Int with the SQL Type
     * @return a <code>String</code>
     */
    @Override
    public String getSQLTypeName(int sqlType) throws Exception {

        String sqlTypeName = getCommonSQLTypeName(sqlType);
        if (sqlTypeName != null) {
            return sqlTypeName;
        }

        sqlTypeName = getBlobSQLTypeName(sqlType);
        if (sqlTypeName != null) {
            return sqlTypeName;
        }

        switch (sqlType) {
            case Types.BINARY: // -2
                return "raw";

            case Types.CLOB:
                return "CLOB";

            case Types.VARBINARY: // -3
                return "raw";

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
                result = "CLOB";
                break;
            case Types.VARCHAR: // 12
                result = "varchar2(255)"; // text too
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
                result = "number(38)";
                break;
            case Types.DECIMAL:
                result = "Number(10)";
                break;
            case Types.DOUBLE: // 8
                result = "double precision"; // money too
                break;
            case Types.FLOAT: // 6
                result = "float";
                break;
            case Types.INTEGER: // 4
                result = "integer"; // oid too
                break;
            case Types.NUMERIC: // 2
                result = "number";
                break;
            case Types.REAL: // 7
                result = "real";
                break;
            case Types.SMALLINT: // 5
                result = "number(5)";
                break;
            case Types.TINYINT: // -6
                result = "number(3)";
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
                result = "number(1)"; // bool too
                break;
            case Types.BOOLEAN: // 16
                result = "number(1)";
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
                result = "date"; // timetz too
                break;
            case Types.TIMESTAMP: // 93
                result = "date";
                break;
            default:
                result = null;
                break;
        }
        return result;
    }

    protected String getBlobSQLTypeName(int sqlType) {
        String result;
        switch (sqlType) {
            case Types.ARRAY:
                result = "blob";
                break;
            case Types.BLOB:
                result = "BLOB";
                break;
            case Types.DATALINK:
                result = "BLOB";
                break;
            case Types.DISTINCT:
                result = "BLOB";
                break;
            case Types.LONGVARBINARY: // -4
                result = "BLOB";
                break;
            case Types.NULL:
                result = "BLOB";
                break;
            case Types.REF:
                result = "BLOB";
                break;
            default:
                result = null;
                break;
        }
        return result;
    }

}

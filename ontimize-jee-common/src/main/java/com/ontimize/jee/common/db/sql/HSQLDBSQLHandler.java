package com.ontimize.jee.common.db.sql;

import java.sql.Types;

public class HSQLDBSQLHandler extends AbstractSQLHandler {

    public HSQLDBSQLHandler() {
    }

    /**
     * Returns a string with the SQL type for HSQLDB's database.
     * @param SQLtype Int with the SQL Type
     * @return a <code>String</code>
     */
    @Override
    public String getSQLTypeName(int SQLtype) throws Exception {
        switch (SQLtype) {

            case Types.ARRAY:
                return "LONGVARBINARY";

            case Types.BIGINT:
                return "BIGINT";

            case Types.BINARY:
                return "BINARY";

            case Types.BIT:
                return "BOOLEAN";

            case Types.BLOB:
                return "LONGVARBINARY";

            case Types.BOOLEAN:
                return "BOOLEAN";

            case Types.CHAR: // '\001'
                return "CHAR";

            case Types.CLOB:
                return "LONGVARCHAR";

            case Types.DATE: // '['
                return "DATE";

            case Types.DECIMAL: // '\003'
                return "DECIMAL";

            case Types.DOUBLE: // '\b'
                return "DOUBLE";

            case Types.FLOAT:
                return "DOUBLE";

            case Types.INTEGER: // '\004'
                return "INTEGER";

            case Types.LONGVARBINARY:
                return "LONGVARBINARY";

            case Types.LONGVARCHAR:
                return "LONGVARCHAR";

            case Types.NUMERIC: // '\002'
                return "NUMERIC";

            case Types.REAL: // '\007'
                return "REAL";

            case Types.SMALLINT: // '\005'
                return "SMALLINT";

            case Types.TIME: // '\\'
                return "TIME";

            case Types.TIMESTAMP: // ']'
                return "TIMESTAMP";

            case Types.TINYINT:
                return "SMALLINT";

            case Types.VARCHAR: // '\f'
                return "VARCHAR";

            case Types.VARBINARY:
                return "VARBINARY";

            default:
                throw new Exception("Invalid type");
        }
    }

}

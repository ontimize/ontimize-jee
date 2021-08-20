package com.ontimize.jee.common.db.sql;

import java.sql.Types;

public class SQLServerSQLHandler extends AbstractSQLHandler {

    public SQLServerSQLHandler() {
    }

    /**
     * Returns a string with the SQL type for SQL Server's database.
     * @param SQLtype Int with the SQL Type
     * @return a <code>String</code>
     */
    @Override
    public String getSQLTypeName(int SQLtype) throws Exception {

        switch (SQLtype) {

            case Types.ARRAY:
                return "image";

            case Types.BIGINT:
                return "bigint";

            case Types.BINARY:
                return "varbinary(MAX)";

            case Types.BIT:
                return "bit";

            case Types.BLOB:
                return "binary(50)";

            case Types.BOOLEAN:
                return "bit";

            case Types.CHAR: // '\001'
                return "char(10)";

            case Types.DATE: // '['
                return "datetime";

            case Types.DECIMAL: // '\003'
                return "decimal(18, 0)";

            case Types.DOUBLE: // '\b'
                return "float";

            case Types.INTEGER: // '\004'
                return "int";

            case Types.LONGVARBINARY:
                return "image";

            case Types.LONGVARCHAR:
                return "text";

            case Types.NUMERIC: // '\002'
                return "numeric(18,0)";

            case Types.REAL: // '\007'
                return "real";

            case Types.SMALLINT: // '\005'
                return "smallint";

            case Types.TIME: // '\\'
                return "DATETIME";

            case Types.TIMESTAMP: // ']'
                return "TIMESTAMP";

            case Types.FLOAT: // '\006'
                return "float";

            case Types.VARCHAR: // '\f'
                return "varchar(MAX)";

            case Types.VARBINARY:
                return "varbinary(MAX)";

            case Types.CLOB:
                return "TEXT";

            default:
                throw new Exception("Invalid type");
        }
    }

}

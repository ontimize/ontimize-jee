package com.ontimize.jee.common.db.sql;

import com.ontimize.jee.common.db.SQLStatementBuilder;

public class SQLHandlerFactory {

    /**
     * The type of SQLHandler for Postgre's database.
     */
    public static final int POSTGRE = 0;

    /**
     * The type of SQLHandler for Oracle's database.
     */
    public static final int ORACLE = 1;

    /**
     * The type of SQLHandler for SQL Server's database.
     */
    public static final int SQLSERVER = 2;

    /**
     * The type of SQLHandler for HSQLDB's database.
     */
    public static final int HSQLDB = 3;

    /**
     * The type of SQLHandler for Access's database.
     */
    public static final int ACCESS = 4;

    /**
     * The type of SQLHandler for MySQL's database
     */
    public static final int MYSQL = 5;

    /**
     * Returns the SQLHandler of the type that is indicated.
     * @param handlerType The type of SQLHandler.
     * @return a <code>SQLHandler</code> of the indicated type.
     */
    public static SQLHandler instanceSQLHandler(int handlerType) {
        switch (handlerType) {
            case POSTGRE:
                return new PostgreSQLHandler();
            case ORACLE:
                return new OracleSQLHandler();
            case SQLSERVER:
                return new SQLServerSQLHandler();
            case HSQLDB:
                return new HSQLDBSQLHandler();
            case ACCESS:
                return new AccessSQLHandler();
            case MYSQL:
                return new MySQLSQLHandler();
            default:
                return null;
        }
    }

    public static SQLHandler instanceSQLHandler(String handlerType) {
        if (SQLStatementBuilder.POSTGRES_HANDLER.equalsIgnoreCase(handlerType)) {
            return new PostgreSQLHandler();
        } else if (SQLStatementBuilder.ORACLE_HANDLER.equalsIgnoreCase(handlerType)) {
            return new OracleSQLHandler();
        } else if (SQLStatementBuilder.SQLSERVER_HANDLER.equalsIgnoreCase(handlerType)) {
            return new SQLServerSQLHandler();
        } else if (SQLStatementBuilder.HSQLDB_HANDLER.equalsIgnoreCase(handlerType)) {
            return new HSQLDBSQLHandler();
        } else if (SQLStatementBuilder.ACCESS_HANDLER.equalsIgnoreCase(handlerType)) {
            return new AccessSQLHandler();
        } else if (SQLStatementBuilder.MYSQL_HANDLER.equalsIgnoreCase(handlerType)) {
            return new MySQLSQLHandler();
        } else {
            return null;
        }
    }

}

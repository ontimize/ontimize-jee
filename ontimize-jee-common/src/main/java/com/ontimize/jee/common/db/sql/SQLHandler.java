package com.ontimize.jee.common.db.sql;

import java.sql.Connection;
import java.util.List;

/**
 * Interface to be implemented by all database handlers. Two methods are different because they
 * manage the parameter <code>connection</code> in a different mode.
 *
 * @author Imatia Innovation
 */
public interface SQLHandler {

    public static final String OPEN_PARENTHESIS = "(";

    public static final String CLOSE_PARENTHESIS = ")";

    /**
     * Return the SQL Statement to create the table.
     * @param table The <code>SQLTableCreation</code> table with whole information to create it.
     * @throws Exception
     * @return a <code>String</code> with the SQL statement
     */
    public String createStatementCreateTable(SQLTableCreation table) throws Exception;

    /**
     * Return the SQL Statement to drop the table.
     * @param table The <code>SQLTableDrop</code> table with whole information to drop it.
     * @throws Exception
     * @return a <code>String</code> with the SQL statement
     */
    public String createStatementDropTable(SQLTableDrop table) throws Exception;

    /**
     * Return the SQL Statement to alter the table.
     * @param table The <code>SQLTableAlter</code> table with whole information to alter it.
     * @throws Exception
     * @return a <code>String</code> with the SQL statement
     */
    public String createStatementAlterTable(SQLTableAlter table) throws Exception;

    /**
     * This method creates a database table. At first, autocommit mode in connection is fixed to false,
     * then its SQL statements are grouped into transactions that are terminated by a call to either the
     * method <code>commit</code> or the method <code>rollback</code>. Moreover, Exceptions are also
     * managed by this method. <br>
     * <br>
     * <u>Call diagram of this method:</u><br>
     * <br>
     * <b><code> -connection.setAutoCommit(false);<br> ...<br> ...<br> -createSQLTableTransactional<br> ...<br> ...<br> -connection.setAutoCommit(true);<br>
     * </code></b><br>
     * @param sqlTables list with all sqlTables
     * @param connection the connection
     */
    public ISQLInfo createSQLTable(List sqlTables, Connection connection);

    /**
     * This method drops a database table. At first, autocommit mode in connection is fixed to false,
     * then its SQL statements are grouped into transactions that are terminated by a call to either the
     * method <code>commit</code> or the method <code>rollback</code>. Moreover, Exceptions are also
     * managed by this method. <br>
     * <br>
     * <u>Call diagram of this method:</u><br>
     * <br>
     * <b><code> -connection.setAutoCommit(false);<br> ...<br> ...<br> -dropSQLTableTransactional<br> ...<br> ...<br> -connection.setAutoCommit(true);<br>
     * </code></b><br>
     * @param sqlTables list with all sqlTables
     * @param connection the connection
     */
    public ISQLInfo dropSQLTable(List sqlTables, Connection connection);

    /**
     * This method alters a database table. At first, autocommit mode in connection is fixed to false,
     * then its SQL statements are grouped into transactions that are terminated by a call to either the
     * method <code>commit</code> or the method <code>rollback</code>. Moreover, Exceptions are also
     * managed by this method. <br>
     * <br>
     * <u>Call diagram of this method:</u><br>
     * <br>
     * <b><code> -connection.setAutoCommit(false);<br> ...<br> ...<br> -alterSQLTableTransactional<br> ...<br> ...<br> -connection.setAutoCommit(true);<br>
     * </code></b><br>
     * @param sqlTables list with all sqlTables
     * @param connection the connection
     */
    public ISQLInfo alterSQLTable(List sqlTables, Connection connection);

    /**
     * This method creates a database table in transactional mode. <br>
     * <br>
     * <u>Call diagram of this method:</u><br>
     * <br>
     * <b><code> -execute sql to create all tables
     * </code></b><br>
     * @param sqlTables list with all sqlTables
     * @param connection the connection
     */
    public ISQLInfo createSQLTableTransactional(List sqlTables, Connection connection) throws Exception;

    /**
     * This method drops a database table in transactional mode. <br>
     * <br>
     * <u>Call diagram of this method:</u><br>
     * <br>
     * <b><code> -execute sql to drop all tables </code></b><br>
     * @param sqlTables list with all sqlTables
     * @param connection the connection
     */
    public ISQLInfo dropSQLTableTransactional(List sqlTables, Connection connection) throws Exception;

    /**
     * This method alters a database table in transactional mode. <br>
     * <br>
     * <u>Call diagram of this method:</u><br>
     * <br>
     * <b><code> -execute sql to alter all tables </code></b><br>
     * @param sqlTables list with all sqlTables
     * @param connection the connection
     */
    public ISQLInfo alterSQLTableTransactional(List sqlTables, Connection connection) throws Exception;

    /**
     * This method executes a serial of SQL actions(create,drop or alter) over a database table. At
     * first, autocommit mode in connection is fixed to false, then its SQL statements are grouped into
     * transactions that are terminated by a call to either the method <code>commit</code> or the method
     * <code>rollback</code>. Moreover, Exceptions are also managed by this method. <br>
     * <br>
     * <u>Call diagram of this method:</u><br>
     * <br>
     * <b><code> -connection.setAutoCommit(false);<br> ...<br> ...<br>
     * -executeSQLTableTransactional<br> ...<br> ...<br> -connection.setAutoCommit(true);<br> </code></b><br>
     * @param sqlTables list with all sqlTables
     * @param connection the connection
     */
    public ISQLInfo executeSQLTable(List sqlTables, Connection connection);

    /**
     * This method executes a serial of SQL actions(create,drop or alter) over a database table in
     * transactional mode. <br>
     * <br>
     * @param sqlTables list with all sqlTables
     * @param connection the connection
     */
    public ISQLInfo executeSQLTableTransactional(List sqlTables, Connection connection) throws Exception;

    public String getSQLTypeName(int sqlType) throws Exception;

}

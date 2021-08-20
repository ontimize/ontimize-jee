package com.ontimize.jee.common.db.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

public abstract class AbstractSQLHandler implements SQLHandler {

    private static final Logger logger = LoggerFactory.getLogger(SQLHandler.class);

    public static boolean DEBUG = false;

    protected static final String CREATE_TABLE = "CREATE TABLE ";

    protected static final String DROP_TABLE = "DROP TABLE ";

    protected static final String ALTER_TABLE = "ALTER TABLE ";

    protected static final String DEFAULT = "DEFAULT";

    protected static final String NOT_NULL = "NOT NULL";

    protected static final String ADD = " ADD ";

    protected static final String DROP = " DROP ";

    protected static final String COLUMN = "COLUMN ";

    @Override
    public ISQLInfo executeSQLTable(List sqlTables, Connection connection) {
        ISQLInfo sqlInfo = null;
        try {
            connection.setAutoCommit(false);
            if ((sqlTables != null) && (sqlTables.size() > 0)) {
                sqlInfo = this.executeSQLTableTransactional(sqlTables, connection);
                connection.commit();
            }
        } catch (SQLException e) {
            AbstractSQLHandler.logger.error("Error in SQL query: {}", e.getMessage(), e);
            try {
                connection.rollback();
            } catch (Exception eRollback) {
                AbstractSQLHandler.logger.error(null, eRollback);
            }
        } catch (SQLHandlerException ex) {
            AbstractSQLHandler.logger.error(null, ex);
            try {
                connection.rollback();
            } catch (Exception eRollback) {
                AbstractSQLHandler.logger.error(null, eRollback);
            }
            sqlInfo = ex.getSQLInfo();
        } catch (Exception ex) {
            AbstractSQLHandler.logger.error(null, ex);
            try {
                connection.rollback();
            } catch (Exception eRollback) {
                AbstractSQLHandler.logger.error(null, eRollback);
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (Exception ex) {
                AbstractSQLHandler.logger.error(null, ex);
            }
        }
        return sqlInfo;
    }

    @Override
    public ISQLInfo executeSQLTableTransactional(List sqlTables, Connection connection) throws Exception {
        ISQLInfo sqlInfo = new SQLInfo();

        if ((sqlTables != null) && (sqlTables.size() > 0)) {
            for (int i = 0; i < sqlTables.size(); i++) {
                Object table = sqlTables.get(i);
                try {
                    String statement = null;
                    if (table instanceof SQLTableCreation) {
                        SQLTableCreation creation = (SQLTableCreation) table;
                        statement = this.createSQLTable(creation, connection);
                    }
                    if (table instanceof SQLTableDrop) {
                        SQLTableDrop creation = (SQLTableDrop) sqlTables.get(i);
                        statement = this.dropSQLTable(creation, connection);
                    }
                    if (table instanceof SQLTableAlter) {
                        SQLTableAlter creation = (SQLTableAlter) sqlTables.get(i);
                        statement = this.alterSQLTable(creation, connection);
                    }
                    sqlInfo.addSQLStatement(statement);
                } catch (Exception ex) {
                    sqlInfo.setCode(ISQLInfo.ERROR);

                    if (ex instanceof SQLStatementException) {
                        sqlInfo.setErrorSQLStatement(((SQLStatementException) ex).getSQLStatement());
                    }

                    SQLHandlerException exception = new SQLHandlerException(ex, sqlInfo);
                    throw exception;
                }
            }
        }
        return sqlInfo;
    }

    @Override
    public ISQLInfo createSQLTable(List sqlTables, Connection connection) {
        ISQLInfo sqlInfo = null;
        try {
            connection.setAutoCommit(false);
            if ((sqlTables != null) && (sqlTables.size() > 0)) {
                sqlInfo = this.createSQLTableTransactional(sqlTables, connection);
                connection.commit();
            }
        } catch (SQLException e) {
            AbstractSQLHandler.logger.error("Error in SQL query {}", e.getMessage(), e);
            try {
                connection.rollback();
            } catch (Exception eRollback) {
                AbstractSQLHandler.logger.error(null, eRollback);
            }
        } catch (SQLHandlerException ex) {
            AbstractSQLHandler.logger.error(null, ex);
            try {
                connection.rollback();
            } catch (Exception eRollback) {
                AbstractSQLHandler.logger.error(null, eRollback);
            }
            sqlInfo = ex.getSQLInfo();
        } catch (Exception ex) {
            AbstractSQLHandler.logger.error(null, ex);
            try {
                connection.rollback();
            } catch (Exception eRollback) {
                AbstractSQLHandler.logger.error(null, eRollback);
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (Exception ex) {
                AbstractSQLHandler.logger.error(null, ex);
            }
        }
        return sqlInfo;
    }

    @Override
    public ISQLInfo dropSQLTable(List sqlTables, Connection connection) {
        ISQLInfo sqlInfo = null;
        try {
            connection.setAutoCommit(false);
            if ((sqlTables != null) && (sqlTables.size() > 0)) {
                sqlInfo = this.dropSQLTableTransactional(sqlTables, connection);
                connection.commit();
            }
        } catch (SQLException e) {
            AbstractSQLHandler.logger.error("Error in SQL query {}", e.getMessage(), e);
            try {
                connection.rollback();
            } catch (Exception eRollback) {
                AbstractSQLHandler.logger.error(null, eRollback);
            }
        } catch (SQLHandlerException ex) {
            AbstractSQLHandler.logger.error(null, ex);
            try {
                connection.rollback();
            } catch (Exception eRollback) {
                AbstractSQLHandler.logger.error(null, eRollback);
            }
            sqlInfo = ex.getSQLInfo();
        } catch (Exception ex) {
            AbstractSQLHandler.logger.error(null, ex);
            try {
                connection.rollback();
            } catch (Exception eRollback) {
                AbstractSQLHandler.logger.error(null, eRollback);
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (Exception ex) {
                AbstractSQLHandler.logger.error(null, ex);
            }
        }
        return sqlInfo;
    }

    @Override
    public ISQLInfo alterSQLTable(List sqlTables, Connection connection) {
        ISQLInfo sqlInfo = null;
        try {
            connection.setAutoCommit(false);
            if ((sqlTables != null) && (sqlTables.size() > 0)) {
                sqlInfo = this.alterSQLTableTransactional(sqlTables, connection);
                connection.commit();
            }
        } catch (SQLException e) {
            AbstractSQLHandler.logger.error("Error in SQL query: {}", e.getMessage(), e);
            try {
                connection.rollback();
            } catch (Exception eRollback) {
                AbstractSQLHandler.logger.error(null, eRollback);
            }
        } catch (SQLHandlerException ex) {
            AbstractSQLHandler.logger.error(null, ex);
            try {
                connection.rollback();
            } catch (Exception eRollback) {
                AbstractSQLHandler.logger.error(null, eRollback);
            }
            sqlInfo = ex.getSQLInfo();
        } catch (Exception ex) {
            AbstractSQLHandler.logger.error(null, ex);
            try {
                connection.rollback();
            } catch (Exception eRollback) {
                AbstractSQLHandler.logger.error(null, eRollback);
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (Exception ex) {
                AbstractSQLHandler.logger.error(null, ex);
            }
        }
        return sqlInfo;
    }

    @Override
    public ISQLInfo createSQLTableTransactional(List sqlTables, Connection connection) throws Exception {
        ISQLInfo sqlInfo = new SQLInfo();
        if ((sqlTables != null) && (sqlTables.size() > 0)) {
            for (int i = 0; i < sqlTables.size(); i++) {
                SQLTableCreation creation = (SQLTableCreation) sqlTables.get(i);
                try {
                    String statement = this.createSQLTable(creation, connection);
                    sqlInfo.addSQLStatement(statement);
                } catch (Exception ex) {
                    sqlInfo.setCode(ISQLInfo.ERROR);

                    if (ex instanceof SQLStatementException) {
                        sqlInfo.setErrorSQLStatement(((SQLStatementException) ex).getSQLStatement());
                    }

                    SQLHandlerException exception = new SQLHandlerException(ex, sqlInfo);
                    throw exception;
                }
            }
        }
        return sqlInfo;
    }

    @Override
    public ISQLInfo dropSQLTableTransactional(List sqlTables, Connection connection) throws Exception {
        ISQLInfo sqlInfo = new SQLInfo();
        if ((sqlTables != null) && (sqlTables.size() > 0)) {
            for (int i = 0; i < sqlTables.size(); i++) {
                SQLTableDrop creation = (SQLTableDrop) sqlTables.get(i);
                try {
                    String statement = this.dropSQLTable(creation, connection);
                    sqlInfo.addSQLStatement(statement);
                } catch (Exception ex) {
                    sqlInfo.setCode(ISQLInfo.ERROR);

                    if (ex instanceof SQLStatementException) {
                        sqlInfo.setErrorSQLStatement(((SQLStatementException) ex).getSQLStatement());
                    }

                    SQLHandlerException exception = new SQLHandlerException(ex, sqlInfo);
                    throw exception;
                }
            }
        }
        return sqlInfo;
    }

    @Override
    public ISQLInfo alterSQLTableTransactional(List sqlTables, Connection connection) throws Exception {
        ISQLInfo sqlInfo = new SQLInfo();
        if ((sqlTables != null) && (sqlTables.size() > 0)) {
            for (int i = 0; i < sqlTables.size(); i++) {
                SQLTableAlter creation = (SQLTableAlter) sqlTables.get(i);
                try {
                    String statement = this.alterSQLTable(creation, connection);
                    sqlInfo.addSQLStatement(statement);
                } catch (Exception ex) {
                    sqlInfo.setCode(ISQLInfo.ERROR);

                    if (ex instanceof SQLStatementException) {
                        sqlInfo.setErrorSQLStatement(((SQLStatementException) ex).getSQLStatement());
                    }

                    SQLHandlerException exception = new SQLHandlerException(ex, sqlInfo);
                    throw exception;
                }
            }
        }
        return sqlInfo;
    }

    /**
     * Execute the SQL Statement.
     * @param sql String which contains the SQL Statement.
     * @param con The connection.
     * @throws Exception
     */
    protected static void executeSQL(String sql, Connection con) throws Exception {
        Statement statement = null;
        try {
            statement = con.createStatement();
            statement.execute(sql);
        } catch (Exception ex) {
            AbstractSQLHandler.logger.error(null, ex);
            throw ex;
        } finally {
            try {
                statement.close();
            } catch (Exception ex) {
                AbstractSQLHandler.logger.error(null, ex);
            }
        }
    }

    /**
     * Creates the table in the database using ANSI standard
     * @param table
     * @param connection
     * @throws Exception
     */

    public String createSQLTable(SQLTableCreation table, Connection connection) throws Exception {
        String statement = null;
        try {
            statement = this.createStatementCreateTable(table);
            return this.executeStatement(statement, connection);
        } catch (SQLException sqlEx) {
            throw new SQLStatementException(sqlEx, statement);
        }
    }

    /**
     * Drop the table in the database using ANSI standard
     * @param table
     * @param connection
     * @throws Exception
     */

    public String dropSQLTable(SQLTableDrop table, Connection connection) throws Exception {
        String statement = null;
        try {
            statement = this.createStatementDropTable(table);
            return this.executeStatement(statement, connection);
        } catch (SQLException sqlEx) {
            throw new SQLStatementException(sqlEx, statement);
        }
    }

    /**
     * Alter the table in the database using ANSI standard
     * @param table
     * @param connection
     * @throws Exception
     */

    public String alterSQLTable(SQLTableAlter table, Connection connection) throws Exception {
        String statement = null;
        try {
            statement = this.createStatementAlterTable(table);
            return this.executeStatement(statement, connection);
        } catch (SQLException sqlEx) {
            throw new SQLStatementException(sqlEx, statement);
        }
    }

    public String executeStatement(String statement, Connection connection) throws Exception {
        AbstractSQLHandler.logger.debug("SQL Statement: {}", statement);
        AbstractSQLHandler.executeSQL(statement, connection);
        return statement;
    }

    @Override
    public String createStatementCreateTable(SQLTableCreation table) throws Exception {
        // StringBuilder bufferSQL = new StringBuilder(CREATE_TABLE);
        StringBuilder bufferSQL = new StringBuilder();

        bufferSQL.append(table.getCreateTableInstruction());

        String primaryKey = this.isMultiplePrimaryKey(table);

        bufferSQL.append(table.getTableName()).append(OPEN_PARENTHESIS);
        List columns = table.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            SQLColumn actualColumn = (SQLColumn) columns.get(i);
            bufferSQL.append(this.getDefinitionColumn(actualColumn, primaryKey == null ? true : false));
            if (i != (columns.size() - 1)) {
                bufferSQL.append(",");
            }
        }
        if (primaryKey != null) {
            bufferSQL.append(",").append(primaryKey);
        }

        List constraints = table.getConstraints();
        if ((constraints != null) && (constraints.size() > 0)) {
            for (int i = 0; i < constraints.size(); i++) {
                if (constraints.get(i) instanceof SQLConstraint) {
                    bufferSQL.append(",").append(constraints.get(i).toString());
                }
            }
        }
        bufferSQL.append(CLOSE_PARENTHESIS);// CREATE_TABLE
        return bufferSQL.toString();
    }

    protected String getDefinitionColumn(SQLColumn column, boolean includePrimaryKey) throws Exception {
        StringBuilder bufferColumn = new StringBuilder(column.getColumnName());
        bufferColumn.append(" ").append(this.getSQLTypeName(column.getColumnSQLType()));
        if (column.getColumnSQLSize() >= 0) {
            bufferColumn.append("(" + column.getColumnSQLSize() + ")");
        }

        if (column.isUnique()) {
            bufferColumn.append(" ").append(SQLUnique.UNIQUE);
        }

        if (column.isRequired()) {
            bufferColumn.append(" ").append(AbstractSQLHandler.NOT_NULL);
        }

        if (column.isPrimaryKey() && includePrimaryKey) {
            bufferColumn.append(" ").append(SQLPrimaryKey.PRIMARY_KEY);
        }

        Object value = column.getDefaultValue();
        if (value != null) {
            bufferColumn.append(" ").append(AbstractSQLHandler.DEFAULT);
            bufferColumn.append(" ").append(this.getStringDefaultValue(value, column));
        }

        return bufferColumn.toString();
    }

    protected String getStringDefaultValue(Object value, SQLColumn column) {
        StringBuilder buffer = new StringBuilder();
        if ((value instanceof String) || (value instanceof Date)) {
            buffer.append("'");
            buffer.append(value.toString());
            buffer.append("'");
        } else {
            return value.toString();
        }
        return buffer.toString();
    }

    public String isMultiplePrimaryKey(SQLTableCreation table) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(SQLPrimaryKey.PRIMARY_KEY).append(OPEN_PARENTHESIS);

        boolean exist = false;
        List columnList = table.getColumns();
        for (int i = 0; i < columnList.size(); i++) {
            SQLColumn column = (SQLColumn) columnList.get(i);
            if (column.isPrimaryKey()) {
                if (exist) {
                    buffer.append(column.getColumnName()).append(CLOSE_PARENTHESIS);
                    return buffer.toString();
                } else {
                    exist = true;
                    buffer.append(column.getColumnName()).append(",");
                }
            }
        }

        return null;
    }

    @Override
    public String createStatementDropTable(SQLTableDrop table) throws Exception {
        StringBuilder bufferSQL = new StringBuilder(AbstractSQLHandler.DROP_TABLE);
        bufferSQL.append(table.getTableName());
        return bufferSQL.toString();
    }

    @Override
    public String createStatementAlterTable(SQLTableAlter table) throws Exception {
        StringBuilder bufferSQL = new StringBuilder(AbstractSQLHandler.ALTER_TABLE);
        bufferSQL.append(table.getTableName());
        if (table.isAdd()) {
            bufferSQL.append(AbstractSQLHandler.ADD);
        } else {
            bufferSQL.append(AbstractSQLHandler.DROP);
        }

        if (table instanceof SQLTableAlterColumn) {
            bufferSQL.append(this.getCreateStatementAlterTableColumn((SQLTableAlterColumn) table));
        } else {
            bufferSQL.append(this.getCreateStatementAlterTableConstraint((SQLTableAlterConstraint) table));
        }

        return bufferSQL.toString();
    }

    public String getCreateStatementAlterTableColumn(SQLTableAlterColumn table) throws Exception {
        StringBuilder bufferSQL = new StringBuilder();

        if (table.isAdd()) {
            SQLColumn actualColumn = table.getColumn();
            bufferSQL.append(this.getDefinitionColumn(actualColumn, false));
        } else {
            bufferSQL.append(AbstractSQLHandler.COLUMN);
            bufferSQL.append(table.getColumn().getColumnName());
        }
        return bufferSQL.toString();
    }

    public String getCreateStatementAlterTableConstraint(SQLTableAlterConstraint table) throws Exception {
        StringBuilder bufferSQL = new StringBuilder();

        if (table.isAdd()) {
            SQLConstraint constraint = table.getConstraint();
            return constraint.toString();
        } else {
            // DROP
            String constraintName = table.getConstraint().getConstraintName();
            if (constraintName != null) {
                bufferSQL.append(SQLConstraint.CONSTRAINT);
                bufferSQL.append(constraintName);
            }

        }
        return bufferSQL.toString();
    }

}

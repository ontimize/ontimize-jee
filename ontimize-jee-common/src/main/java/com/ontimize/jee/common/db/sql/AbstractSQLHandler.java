package com.ontimize.jee.common.db.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSQLHandler implements SQLHandler {

	private static final Logger logger = LoggerFactory.getLogger(AbstractSQLHandler.class);

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
			if ((sqlTables != null) && (!sqlTables.isEmpty())) {
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
	public ISQLInfo executeSQLTableTransactional(List sqlTables, Connection connection) throws SQLHandlerException {

		ISQLInfo sqlInfo = new SQLInfo();

		if ((sqlTables != null) && (!sqlTables.isEmpty())) {
			for (Object table : sqlTables) {
				this.executeSQLTable(connection, sqlInfo, table);
			}
		}
		return sqlInfo;
	}

	private void executeSQLTable(Connection connection, ISQLInfo sqlInfo, Object table) throws SQLHandlerException {
		try {
			String statement = null;
			if (table instanceof SQLTableCreation) {
				SQLTableCreation creation = (SQLTableCreation) table;
				statement = this.createSQLTable(creation, connection);
			}
			if (table instanceof SQLTableDrop) {
				SQLTableDrop creation = (SQLTableDrop) table;
				statement = this.dropSQLTable(creation, connection);
			}
			if (table instanceof SQLTableAlter) {
				SQLTableAlter creation = (SQLTableAlter) table;
				statement = this.alterSQLTable(creation, connection);
			}
			sqlInfo.addSQLStatement(statement);
		} catch (Exception ex) {
			sqlInfo.setCode(ISQLInfo.ERROR);

			if (ex instanceof SQLStatementException) {
				sqlInfo.setErrorSQLStatement(((SQLStatementException) ex).getSQLStatement());
			}

			throw new SQLHandlerException(ex, sqlInfo);
		}
	}

	@Override
	public ISQLInfo createSQLTable(List sqlTables, Connection connection) {
		ISQLInfo sqlInfo = null;
		try {
			connection.setAutoCommit(false);
			if ((sqlTables != null) && (!sqlTables.isEmpty())) {
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
			if ((sqlTables != null) && (!sqlTables.isEmpty())) {
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
			if ((sqlTables != null) && (!sqlTables.isEmpty())) {
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
		if ((sqlTables != null) && (!sqlTables.isEmpty())) {
			for (Object sqlTable : sqlTables) {
				SQLTableCreation creation = (SQLTableCreation) sqlTable;
				try {
					String statement = this.createSQLTable(creation, connection);
					sqlInfo.addSQLStatement(statement);
				} catch (Exception ex) {
					sqlInfo.setCode(ISQLInfo.ERROR);

					if (ex instanceof SQLStatementException) {
						sqlInfo.setErrorSQLStatement(((SQLStatementException) ex).getSQLStatement());
					}

					throw new SQLHandlerException(ex, sqlInfo);
				}
			}
		}
		return sqlInfo;
	}

	@Override
	public ISQLInfo dropSQLTableTransactional(List sqlTables, Connection connection) throws Exception {
		ISQLInfo sqlInfo = new SQLInfo();
		if ((sqlTables != null) && (!sqlTables.isEmpty())) {
			for (Object sqlTable : sqlTables) {
				SQLTableDrop creation = (SQLTableDrop) sqlTable;
				try {
					String statement = this.dropSQLTable(creation, connection);
					sqlInfo.addSQLStatement(statement);
				} catch (Exception ex) {
					sqlInfo.setCode(ISQLInfo.ERROR);

					if (ex instanceof SQLStatementException) {
						sqlInfo.setErrorSQLStatement(((SQLStatementException) ex).getSQLStatement());
					}

					throw new SQLHandlerException(ex, sqlInfo);
				}
			}
		}
		return sqlInfo;
	}

	@Override
	public ISQLInfo alterSQLTableTransactional(List sqlTables, Connection connection) throws Exception {
		ISQLInfo sqlInfo = new SQLInfo();
		if ((sqlTables != null) && (!sqlTables.isEmpty())) {
			for (Object sqlTable : sqlTables) {
				SQLTableAlter creation = (SQLTableAlter) sqlTable;
				try {
					String statement = this.alterSQLTable(creation, connection);
					sqlInfo.addSQLStatement(statement);
				} catch (Exception ex) {
					sqlInfo.setCode(ISQLInfo.ERROR);

					if (ex instanceof SQLStatementException) {
						sqlInfo.setErrorSQLStatement(((SQLStatementException) ex).getSQLStatement());
					}

					throw new SQLHandlerException(ex, sqlInfo);
				}
			}
		}
		return sqlInfo;
	}

	/**
	 * Execute the SQL Statement.
	 *
	 * @param sql String which contains the SQL Statement.
	 * @param con The connection.
	 * @throws Exception
	 */
	protected static void executeSQL(String sql, Connection con) throws SQLException {

		try (Statement statement = con.createStatement()) {
			statement.execute(sql);
		} catch (SQLException ex) {
			String message = ex.getMessage();
			AbstractSQLHandler.logger.error("Error: {}", message);
			throw new SQLException(ex.getCause());
		}

	}

	/**
	 * Creates the table in the database using ANSI standard
	 *
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
	 *
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
	 *
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

	public String executeStatement(String statement, Connection connection) throws SQLException {
		AbstractSQLHandler.logger.debug("SQL Statement: {}", statement);
		AbstractSQLHandler.executeSQL(statement, connection);
		return statement;
	}

	@Override
	public String createStatementCreateTable(SQLTableCreation table) throws Exception {
		StringBuilder bufferSQL = new StringBuilder();

		bufferSQL.append(table.getCreateTableInstruction());

		String primaryKey = this.isMultiplePrimaryKey(table);

		bufferSQL.append(table.getTableName()).append(SQLHandler.OPEN_PARENTHESIS);
		List<SQLColumn> columns = table.getColumns();
		for (int i = 0; i < columns.size(); i++) {
			SQLColumn actualColumn = columns.get(i);
			bufferSQL.append(this.getDefinitionColumn(actualColumn, (primaryKey == null)));
			if (i != (columns.size() - 1)) {
				bufferSQL.append(",");
			}
		}
		if (primaryKey != null) {
			bufferSQL.append(",").append(primaryKey);
		}

		List<SQLConstraint> constraints = table.getConstraints();
		if ((constraints != null) && (!constraints.isEmpty())) {
			for (Object constraint : constraints) {
				if (constraint instanceof SQLConstraint) {
					bufferSQL.append(",").append(constraint.toString());
				}
			}
		}
		bufferSQL.append(SQLHandler.CLOSE_PARENTHESIS);// CREATE_TABLE
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
			bufferColumn.append(" ").append(this.getStringDefaultValue(value));
		}

		return bufferColumn.toString();
	}

	protected String getStringDefaultValue(Object value) {
		StringBuilder buffer = new StringBuilder();
		if (!(value instanceof String) && !(value instanceof Date)) {
			return value.toString();
		}
		buffer.append("'");
		buffer.append(value.toString());
		buffer.append("'");
		return buffer.toString();
	}

	public String isMultiplePrimaryKey(SQLTableCreation table) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(SQLPrimaryKey.PRIMARY_KEY).append(SQLHandler.OPEN_PARENTHESIS);

		boolean exist = false;
		List<SQLColumn> columnList = table.getColumns();
		for (SQLColumn column : columnList) {
			if (column.isPrimaryKey()) {
				if (exist) {
					buffer.append(column.getColumnName()).append(SQLHandler.CLOSE_PARENTHESIS);
					return buffer.toString();
				}
				exist = true;
				buffer.append(column.getColumnName()).append(",");
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

	public String getCreateStatementAlterTableConstraint(SQLTableAlterConstraint table) {
		StringBuilder bufferSQL = new StringBuilder();

		if (table.isAdd()) {
			SQLConstraint constraint = table.getConstraint();
			return constraint.toString();
		}
		// DROP
		String constraintName = table.getConstraint().getConstraintName();
		if (constraintName != null) {
			bufferSQL.append(SQLConstraint.CONSTRAINT);
			bufferSQL.append(constraintName);
		}
		return bufferSQL.toString();
	}

}

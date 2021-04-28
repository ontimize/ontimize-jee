package com.ontimize.jee.common.db.sql;

import java.sql.SQLException;

public class SQLStatementException extends Exception {

    /**
     * String that contains a SQL Statement.
     */
    protected String statement = null;

    /**
     * Constructs a new SQLStatementException adding to the SQLException the SQL Statement that has
     * caused the exception.
     * @param t The SQLException.
     * @param statement String with the SQL Statement.
     */
    public SQLStatementException(SQLException t, String statement) {
        super(t);
        this.statement = statement;
    }

    /**
     * Returns the SQL Statement
     * @return a <code>String</code> with the SQL Statement.
     */
    public String getSQLStatement() {
        return this.statement;
    }

}

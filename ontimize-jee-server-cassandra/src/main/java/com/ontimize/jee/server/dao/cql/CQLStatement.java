package com.ontimize.jee.server.dao.cql;

import java.util.List;

public class CQLStatement {

    protected String cQLStatement = null;

    protected List values = null;

    public CQLStatement(String cQLStatement) {
        this.cQLStatement = cQLStatement;
    }

    public CQLStatement(String cQLStatement, List values) {
        this.cQLStatement = cQLStatement;
        this.values = values;
    }

    /**
     * Returns the string that be used to create the SQL Statement
     * @return a String
     */
    public String getCQLStatement() {
        return this.cQLStatement;
    }

    /**
     * Returns a List of the required values for SQL Statement
     * @return the required values or null
     */
    public List getValues() {
        return this.values;
    }

    /**
     * Add a values List at beginning of values List for SQL Statement
     * @param values
     */
    public void addValues(List values) {
        this.values.addAll(0, values);
    }

}

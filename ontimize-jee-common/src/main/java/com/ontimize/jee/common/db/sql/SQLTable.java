package com.ontimize.jee.common.db.sql;

/**
 * Class to define the properties for the table to create in the database
 */
public class SQLTable {

    /**
     * The name of the table.
     */
    protected String tableName = null;

    /**
     * Creates a new <code>SQLTable</code> indicating the name of the table
     * @param tableName The name of the table.
     */
    public SQLTable(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Returns the name of the table.
     * @return a <code>String</code> with the name of the table
     */
    public String getTableName() {
        return this.tableName;
    }

}

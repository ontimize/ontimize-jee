package com.ontimize.jee.core.common.db.sql;

public class SQLTableAlterColumn extends SQLTableAlter {

    /**
     * The column that are going to be modified in the database table.
     */
    protected SQLColumn column;

    /**
     * Creates a new <code>SQLTableAlterColumn</code> indicating the name of the table to be altered and
     * the kind of operation to realize with the parameter add. If it is true the operation will be ADD
     * COLUMN and if the parameter is false the operation will be DROP COLUMN.
     * @param tableName The name of the table.
     * @param add The value of the parameter add. Indicates add or drop.
     */
    public SQLTableAlterColumn(String tableName, boolean add) {
        super(tableName, add);
    }

    /**
     * Creates a new <code>SQLTableAlterColumn</code> indicating the name of the table to be altered,
     * the column that are going to be modified in the database table and the kind of operation to
     * realize with the parameter add. If it is true the operation will be ADD COLUMN and if the
     * parameter is false the operation will be DROP COLUMN.
     * @param tableName The name of the table.
     * @param column The SQLColumn with whole definition of it.
     * @param add The value of the parameter add. Indicates add or drop.
     */
    public SQLTableAlterColumn(String tableName, SQLColumn column, boolean add) {
        super(tableName, add);
        this.column = column;
    }

    /**
     * Returns the column that are going to be modified in the database table.
     * @return a <code>SQLColumn</code>
     */
    public SQLColumn getColumn() {
        return this.column;
    }

    /**
     * Sets the column to be modified in the database table.
     * @param column The SQLColumn to be modified.
     */
    public void setColumn(SQLColumn column) {
        this.column = column;
    }

}

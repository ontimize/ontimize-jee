package com.ontimize.jee.common.db.sql;

import java.util.ArrayList;
import java.util.List;

public class SQLTableCreation extends SQLTable {

    /**
     * The list of <code>SQLColum</code> with the columns that are going to be applied in the creation
     * of the table
     */
    protected List columns;

    /**
     * The list of <code>SQLConstraint</code> with the constraints that are going to be applied in the
     * creation of the table
     */
    protected List constraints;

    /**
     * Creates a new <code>SQLTableCreation</code> indicating the name of the constraint and the columns
     * in where the constraint is applied.
     * @param tableName The name of the table
     * @param columns The list of columns that are going to be applied in the creation of the table
     * @param constraints The list of constraints that are going to be applied in the creation of the
     *        table
     */
    public SQLTableCreation(String tableName, List columns, List constraints) {
        super(tableName);
        this.columns = columns;
        this.constraints = constraints;
    }

    /**
     * Returns a list with all constraints that affect to the database table.
     * @return a <code>List</code> with the constraints.
     */
    public List getConstraints() {
        return this.constraints;
    }

    /**
     * Returns a list with all columns of the database table.
     * @return a <code>List</code> with the columns
     */
    public List getColumns() {
        return this.columns;
    }

    /**
     * Adds a new constraint to the collection of constraints of the database table.
     * @param constraint The constraint to be added.
     */
    public void addConstraints(SQLConstraint constraint) {
        if (constraint == null) {
            this.constraints = new ArrayList();
        }
        if (constraint != null) {
            this.constraints.add(constraint);
        }
    }

    /**
     * Adds a new column to the collection of columns of the database table.
     * @param column The column to be added.
     */
    public void addColumn(SQLColumn column) {
        if (this.columns == null) {
            this.columns = new ArrayList();
        }
        if (this.columns != null) {
            this.columns.add(column);
        }
    }

    /**
     * This method returns the starting of the SQL statement of Create Table.
     * @return a <code>String</code> whit the statement.
     */
    public String getCreateTableInstruction() {
        return "CREATE TABLE ";
    }

}

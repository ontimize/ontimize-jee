package com.ontimize.jee.core.common.db.sql;

public class SQLTableAlterConstraint extends SQLTableAlter {

    /**
     * The constraint that are going to be introduced into the database table.
     */
    protected SQLConstraint constraint;

    /**
     * Creates a new <code>SQLTableAlterConstraint</code> indicating the name of the table to be altered
     * and the kind of operation to realize with the parameter add. If it is true the operation will be
     * ADD CONSTRAINT and if the parameter is false the operation will be DROP CONSTRAINT.
     * @param tableName The name of the table.
     * @param add The value of the parameter add. Indicates add or drop.
     */
    public SQLTableAlterConstraint(String tableName, boolean add) {
        super(tableName, add);
    }

    /**
     * Creates a new <code>SQLTableAlterConstraint</code> indicating the name of the table to be
     * altered, the constraint that are going to be modified in the database table and the kind of
     * operation to realize with the parameter add. If it is true the operation will be ADD CONSTRAINT
     * and if the parameter is false the operation will be DROP CONSTRAINT.
     * @param tableName The name of the table.
     * @param add The value of the parameter add. Indicates add or drop.
     * @param constraint The SQLConstraint with whole definition of it.
     */
    public SQLTableAlterConstraint(String tableName, boolean add, SQLConstraint constraint) {
        super(tableName, add);
        this.constraint = constraint;
    }

    /**
     * Returns the constraint that are going to be introduced into the database table.
     * @return a <code>SQLConstraint</code>
     */
    public SQLConstraint getConstraint() {
        return this.constraint;
    }

    /**
     * Set the constraint that are going to be introduced into the database table.
     * @param constraint The SQLConstraint with whole definition of it.
     */
    public void setConstraint(SQLConstraint constraint) {
        this.constraint = constraint;
    }

}

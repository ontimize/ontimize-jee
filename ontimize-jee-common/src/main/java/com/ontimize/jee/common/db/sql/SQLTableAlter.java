package com.ontimize.jee.common.db.sql;

public class SQLTableAlter extends SQLTable {

    /**
     * Parameter that indicates if character of the modification. If it is true the operation to be
     * realized is ADD and if it is false the operation to be realized is DROP
     */
    protected boolean add = false;

    /**
     * Creates a new <code>SQLTableAlter</code> indicating the name of the table to be altered and the
     * kind of operation to realize with the parameter add. If it is true the operation will be ADD and
     * if the parameter is false the operation will be DROP.
     * @param tableName The name of the table.
     * @param add Value of the parameter add.
     */
    public SQLTableAlter(String tableName, boolean add) {
        super(tableName);
        this.add = add;
    }

    /**
     * Returns as the parameter add is set. If it is true indicates that the operation is ADD, in the
     * other side, if it is false indicates that the operation is DROP
     * @return a <code>boolean</code>
     */
    public boolean isAdd() {
        return this.add;
    }

    /**
     * Sets the value of the parameter add.
     * @param add
     */
    public void setAdd(boolean add) {
        this.add = add;
    }

}

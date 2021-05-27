package com.ontimize.jee.common.db.util;

import java.io.Serializable;

public class DBFunctionName implements Serializable {

    /**
     * If isolated is true this is a function that must be executed in the select without other columns
     * (like count(*))
     */
    boolean isolated = false;

    /**
     * Name of the function
     */
    protected String name = null;

    // protected List groupByColumns = null;

    public DBFunctionName(String name) {
        this(name, false);
    }

    public DBFunctionName(String name, boolean isolated) {
        if (name == null) {
            throw new IllegalArgumentException(this.getClass().toString() + " Function name cant be null ");
        }
        this.name = name;
        this.isolated = isolated;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public boolean isIsolated() {
        return this.isolated;
    }

    public void setIsolated(boolean isolated) {
        this.isolated = isolated;
    }

    // public List getGroupByColumns() {
    // return groupByColumns;
    // }

    // public void setGroupByColumns(List groupByColumns) {
    // this.groupByColumns = groupByColumns;
    // }

}

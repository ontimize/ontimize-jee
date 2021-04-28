package com.ontimize.jee.common.db.util;

public class CountDBFunctionName extends DBFunctionName {

    protected String column;

    public CountDBFunctionName() {
        super("count(*) as \"COUNT\"", true);
        this.column = "*";
    }

    public CountDBFunctionName(String column) {
        super("count(" + column + ") as \"COUNT\"", true);
        this.column = column;
    }

    public String getColumn() {
        return this.column;
    }

}

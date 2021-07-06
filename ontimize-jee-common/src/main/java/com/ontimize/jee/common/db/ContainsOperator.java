package com.ontimize.jee.common.db;

public class ContainsOperator extends SQLStatementBuilder.BasicOperator {

    public static final SQLStatementBuilder.BasicOperator CONTAINS_OP = new SQLStatementBuilder.BasicOperator(
            " Contains ");

    public static final SQLStatementBuilder.BasicOperator NOT_CONTAINS_OP = new SQLStatementBuilder.BasicOperator(
            " Not Contains ");

    public ContainsOperator(String s) {
        super(s);
    }

}

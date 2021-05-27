package com.ontimize.jee.common.db.query;

import com.ontimize.jee.common.db.SQLStatementBuilder;

import java.io.Serializable;
import java.util.List;

public class QueryExpression implements Serializable {

    protected SQLStatementBuilder.Expression expression = null;

    protected String[] cols = null;

    protected boolean[] queryColumns = null;

    protected String entity = null;

    public QueryExpression(SQLStatementBuilder.Expression ex, String entity, String[] cols, boolean[] queryCols) {
        this.expression = ex;
        if (cols != null) {
            this.cols = cols;
        } else {
            this.cols = new String[0];
        }
        if (queryCols != null) {
            this.queryColumns = queryCols;
        } else {
            this.queryColumns = new boolean[0];
        }
        this.entity = entity;
    }

    public QueryExpression(SQLStatementBuilder.Expression ex, String entity, List cols, List queryCols) {

        this.expression = ex;
        this.entity = entity;

        if (cols == null) {
            return;
        }
        if (cols.size() != queryCols.size()) {
            return;
        }

        this.cols = new String[cols.size()];
        this.queryColumns = new boolean[queryCols.size()];
        for (int i = 0, a = cols.size(); i < a; i++) {
            this.cols[i] = new String((String) cols.get(i));
            this.queryColumns[i] = ((Boolean) queryCols.get(i)).booleanValue();
        }

    }

    public void setCols(String[] cols) {
        this.cols = cols;
        this.queryColumns = new boolean[cols.length];
        for (int i = 0; i < this.queryColumns.length; i++) {
            this.queryColumns[i] = true;
        }
    }

    public String getEntity() {
        return this.entity;
    }

    public SQLStatementBuilder.Expression getExpression() {
        return this.expression;
    }

    public void setExpression(SQLStatementBuilder.Expression expression) {
        this.expression = expression;
    }

    public java.util.List getQueryColumns() {
        java.util.List v = new java.util.ArrayList();
        for (int i = 0; i < this.cols.length; i++) {
            if (this.queryColumns[i]) {
                v.add(this.cols[i]);
            }
        }
        return v;
    }

    public java.util.List getCols() {
        java.util.List v = new java.util.ArrayList();
        for (int i = 0; i < this.cols.length; i++) {
            v.add(this.cols[i]);
        }
        return v;
    }

    public java.util.List getColumnToQuery() {
        java.util.List v = new java.util.ArrayList();
        for (int i = 0; i < this.queryColumns.length; i++) {
            v.add(new Boolean(this.queryColumns[i]));
        }
        return v;
    }

    @Override
    public boolean equals(Object e) {
        if (this.expression == null) {
            return false;
        }
        if (this.expression.getLeftOperand() == null) {
            return false;
        }
        if (this.expression.getRightOperand() == null) {
            return false;
        }
        if (this.expression.getOperator() == null) {
            return false;
        }

        if (e instanceof SQLStatementBuilder.Expression) {
            if (((SQLStatementBuilder.Expression) e).getLeftOperand() == null) {
                return false;
            }
            if (((SQLStatementBuilder.Expression) e).getRightOperand() == null) {
                return false;
            }
            if (((SQLStatementBuilder.Expression) e).getOperator() == null) {
                return false;
            }

            return this.expression.getLeftOperand()
                .toString()
                .equals(((SQLStatementBuilder.Expression) e).getLeftOperand().toString())
                    && this.expression.getRightOperand()
                        .toString()
                        .equals(((SQLStatementBuilder.Expression) e).getRightOperand().toString())
                    && this.expression.getOperator()
                        .toString()
                        .equals(((SQLStatementBuilder.Expression) e).getOperator().toString());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}

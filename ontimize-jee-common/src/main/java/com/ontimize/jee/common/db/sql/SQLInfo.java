package com.ontimize.jee.common.db.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SQLInfo implements ISQLInfo, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(SQLInfo.class);

    protected String errorSQLStatement = null;

    protected List sqlStatements = new ArrayList();

    protected int code = OK;

    // public void SLQInfo() {
    // sqlStatements=new Vector();
    // code = ISQLInfo.ok;
    // }

    public SQLInfo() {
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public void setCode(int i) {
        this.code = i;
    }

    @Override
    public List getSQLStatements() {
        return this.sqlStatements;
    }

    @Override
    public void addSQLStatement(String statement) {
        if (this.sqlStatements.add(statement)) {
            SQLInfo.logger.debug(null);
        }
    }

    public String removeSQLStatement(int index) {
        if ((this.sqlStatements != null) && (index < this.sqlStatements.size())) {
            String str = (String) this.sqlStatements.get(index);
            this.sqlStatements.remove(index);
            return str;
        }
        return "";
    }

    @Override
    public void appendSQLInfo(ISQLInfo sqlInfo) {

        List list = sqlInfo.getSQLStatements();
        for (int i = 0; i < list.size(); i++) {
            this.addSQLStatement((String) list.get(i));
        }

    }

    @Override
    public String getErrorSQLStatement() {
        return this.errorSQLStatement;
    }

    @Override
    public void setErrorSQLStatement(String statement) {
        this.errorSQLStatement = statement;
    }

}

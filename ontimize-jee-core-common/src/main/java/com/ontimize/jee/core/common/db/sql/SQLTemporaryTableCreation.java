package com.ontimize.jee.core.common.db.sql;

import java.util.List;

public class SQLTemporaryTableCreation extends SQLTableCreation {

    public SQLTemporaryTableCreation(String tableName, List columns, List constraints) {
        super(tableName, columns, constraints);

    }

    @Override
    public String getCreateTableInstruction() {
        return "CREATE TEMPORARY TABLE ";
    }

}

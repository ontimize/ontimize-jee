package com.ontimize.jee.common.db.handler;

import com.ontimize.jee.common.db.SQLStatementBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;

public class HSQLDBSQLStatementHandler extends DefaultSQLStatementHandler {

    static final Logger logger = LoggerFactory.getLogger(SQLStatementBuilder.class);

    public static final String LIMIT = " LIMIT ";

    public static final String OFFSET = " OFFSET ";

    @Override
    public boolean isPageable() {
        return true;
    }

    @Override
    public SQLStatementBuilder.SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
            List wildcards,
            List columnSorting, int recordCount, boolean descending,
            boolean forceDistinct) {
        return super.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, 0,
                descending, forceDistinct);
    }

    @Override
    public SQLStatementBuilder.SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
            List wildcards,
            List columnSorting, int recordCount, int offset,
            boolean descending, boolean forceDistinct) {
        StringBuilder sql = new StringBuilder();
        List vValues = new ArrayList();
        if ((columnSorting != null) && !requestedColumns.isEmpty()) {
            for (int i = 0; i < columnSorting.size(); i++) {
                if (!requestedColumns.contains(columnSorting.get(i).toString())) {
                    requestedColumns.add(columnSorting.get(i).toString());
                }
            }
        }

        sql.append(this.createSelectQuery(table, requestedColumns, forceDistinct));

        String cond = this.createQueryConditions(conditions, wildcards, vValues);
        if (cond != null) {
            sql.append(cond);
        }
        if ((columnSorting != null) && (!columnSorting.isEmpty())) {
            String sort = this.createSortStatement(columnSorting, descending);
            sql.append(sort);
        }

        if (recordCount >= 0) {
            sql.append(HSQLDBSQLStatementHandler.LIMIT);
            sql.append(recordCount);
        }

        if (offset >= 0) {
            sql.append(HSQLDBSQLStatementHandler.OFFSET);
            sql.append(offset);
        }

        HSQLDBSQLStatementHandler.logger.debug(sql.toString());
        return new SQLStatementBuilder.SQLStatement(sql.toString(), vValues);
    }

    @Override
    public SQLStatementBuilder.SQLStatement createLeftJoinSelectQueryPageable(String mainTable, String subquery,
            String secondaryTable,
            List mainKeys, List secondaryKeys,
            List mainTableRequestedColumns, List secondaryTableRequestedColumns, Map mainTableConditions,
            Map secondaryTableConditions, List wildcards,
            List columnSorting, boolean forceDistinct, boolean descending, int recordNumber, int startIndex) {

        SQLStatementBuilder.SQLStatement stSQL = this.createLeftJoinSelectQuery(mainTable, subquery, secondaryTable,
                mainKeys,
                secondaryKeys, mainTableRequestedColumns, secondaryTableRequestedColumns,
                mainTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending);

        StringBuilder stSQLString = new StringBuilder(stSQL.getSQLStatement());
        List vValues = stSQL.getValues();

        if (recordNumber >= 0) {
            stSQLString.append(HSQLDBSQLStatementHandler.LIMIT);
            stSQLString.append(recordNumber);
        }

        if (startIndex >= 0) {
            stSQLString.append(HSQLDBSQLStatementHandler.OFFSET);
            stSQLString.append(startIndex);
        }

        HSQLDBSQLStatementHandler.logger.debug(stSQLString.toString());
        return new SQLStatementBuilder.SQLStatement(stSQLString.toString(), vValues);
    }

    @Override
    public String convertPaginationStatement(String sqlTemplate, int startIndex, int recordNumber) {
        StringBuilder sql = new StringBuilder(sqlTemplate);

        if (recordNumber >= 0) {
            sql.append(HSQLDBSQLStatementHandler.LIMIT);
            sql.append(recordNumber);
        }

        if (startIndex >= 0) {
            sql.append(HSQLDBSQLStatementHandler.OFFSET);
            sql.append(startIndex);
        }

        return sql.toString();
    }

}

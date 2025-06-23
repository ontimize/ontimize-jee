package com.ontimize.jee.common.db.handler;

import com.ontimize.jee.common.db.SQLStatementBuilder;
import com.ontimize.jee.common.db.sql.SQLHandler;
import com.ontimize.jee.common.db.sql.SQLServerSQLHandler;
import com.ontimize.jee.common.dto.EntityResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SQLServerSQLStatementHandler extends DefaultSQLStatementHandler {

    static final Logger logger = LoggerFactory.getLogger(SQLStatementBuilder.class);

    public static final String GENERATED_KEY_COLUMN_NAME = "GENERATED_KEYS";

    public static final String TOP_100_PERCENT = " TOP 100 PERCENT ";

    public static final String CAST_FUNCTION = "CAST";

    private SQLHandler sqlHandler = new SQLServerSQLHandler();

    public SQLHandler getSqlHandler() {
        return this.sqlHandler;
    }

    public void setSqlHandler(SQLHandler sqlHandler) {
        this.sqlHandler = sqlHandler;
    }

    @Override
    public boolean isPageable() {
        return false;
    }

    @Override
    public boolean isDelimited() {
        return true;
    }

    @Override
    protected String createSelectClause(boolean forceDistinct) {
        StringBuilder sqlSelectClause = new StringBuilder(super.createSelectClause(forceDistinct));
        sqlSelectClause.append(SQLServerSQLStatementHandler.TOP_100_PERCENT);
        return sqlSelectClause.toString();
    }

    @Override
    public SQLStatementBuilder.SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
            List wildcards,
            List columnSorting, int recordCount, boolean descending,
            boolean forceDistinct) {
        StringBuilder sql = new StringBuilder();
        List vValues = new ArrayList();
        if ((columnSorting != null) && !requestedColumns.isEmpty()) {
            for (int i = 0; i < columnSorting.size(); i++) {
                if (!requestedColumns.contains(columnSorting.get(i).toString())) {
                    requestedColumns.add(columnSorting.get(i).toString());
                }
            }
        }
        sql.append(this.createSelectQuery(table, requestedColumns, recordCount, forceDistinct));
        String cond = this.createQueryConditions(conditions, wildcards, vValues);
        if (cond != null) {
            sql.append(cond);
        }
        if ((columnSorting != null) && !columnSorting.isEmpty()) {
            String sort = this.createSortStatement(columnSorting, descending);
            sql.append(sort);
        }

        SQLServerSQLStatementHandler.logger.debug(sql.toString());
        return new SQLStatementBuilder.SQLStatement(sql.toString(), vValues);
    }

    protected String createSelectQuery(String table, List askedColumns, int recordsNumber, boolean forceDistinct) {
        StringBuilder sStringQuery = new StringBuilder(SQLStatementBuilder.SELECT);
        String tableaux = table.toLowerCase();
        if (forceDistinct) {
            sStringQuery.append(SQLStatementBuilder.DISTINCT);
        }
        if (recordsNumber >= 0) {
            sStringQuery.append(SQLStatementBuilder.TOP);
            sStringQuery.append(" ");
            sStringQuery.append(recordsNumber);
            sStringQuery.append(" ");
        }
        if ((askedColumns == null) || (askedColumns.isEmpty())) {
            sStringQuery.append(SQLStatementBuilder.ASTERISK);
            sStringQuery.append(SQLStatementBuilder.FROM);
            if ((table.indexOf("SELECT") < 0) && (table.indexOf("Select") < 0) && (table.indexOf("select") < 0)
                    && this.checkColumnName(table)) {
                sStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                sStringQuery.append(table);
                sStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
            } else {
                sStringQuery.append(table);
            }
        } else {
            // If attributes is empty, then create the query with all requested
            // columns.
            // Requested columns number
            int attributesNumber = askedColumns.size();
            for (int i = 0; i < attributesNumber; i++) {
                Object oColumn = askedColumns.get(i);
                if (oColumn == null) {
                    continue;
                }

                // In the last attribute the comma is not added
                String sColumn = oColumn.toString();
                // If some conflicted character is contained then use brackets
                boolean bBrackets = this.checkColumnName(sColumn);
                if (i < (attributesNumber - 1)) {
                    if (bBrackets) {
                        sStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                        sStringQuery.append(sColumn);
                        sStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                        sStringQuery.append(SQLStatementBuilder.COMMA);
                    } else {
                        sStringQuery.append(sColumn);
                        sStringQuery.append(SQLStatementBuilder.COMMA);
                    }
                } else {
                    if (bBrackets) {
                        sStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                        sStringQuery.append(sColumn);
                        sStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                        sStringQuery.append(SQLStatementBuilder.FROM);
                        if ((table.indexOf("SELECT") < 0) && (table.indexOf("Select") < 0)
                                && (table.indexOf("select") < 0) && this.checkColumnName(table)) {
                            sStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                            sStringQuery.append(table);
                            sStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                        } else {
                            sStringQuery.append(table);
                        }
                    } else {
                        // The last attribute does not include the COMMA
                        sStringQuery.append(sColumn);
                        sStringQuery.append(SQLStatementBuilder.FROM);
                        if ((tableaux.indexOf("select") < 0) && this.checkColumnName(table)
                                && (tableaux.indexOf("join") < 0)) {
                            sStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
                            sStringQuery.append(table);
                            sStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
                        } else {
                            sStringQuery.append(table);
                        }
                    }
                }
            }
        }
        return sStringQuery.toString();
    }

    @Override
    protected SQLStatementBuilder.SQLStatement createLeftJoinSelectQueryPageable(String mainTable, String subquery,
            String secondaryTable,
            List mainKeys, List secondaryKeys,
            List mainTableRequestedColumns, List secondaryTableRequestedColumns, Map mainTableConditions,
            Map secondaryTableConditions, List wildcards,
            List columnSorting, boolean forceDistinct, boolean descending, int recordCount) {
        // TODO Auto-generated method stub
        SQLStatementBuilder.SQLStatement stSQL = super.createLeftJoinSelectQuery(mainTable, subquery, secondaryTable,
                mainKeys,
                secondaryKeys, mainTableRequestedColumns,
                secondaryTableRequestedColumns, mainTableConditions, secondaryTableConditions, wildcards, columnSorting,
                forceDistinct, descending);

        StringBuilder stSQLString = new StringBuilder(stSQL.getSQLStatement());
        List vValues = stSQL.getValues();

        int startIndex = stSQLString.indexOf(SQLServerSQLStatementHandler.TOP_100_PERCENT);

        stSQLString.replace(startIndex, startIndex + SQLServerSQLStatementHandler.TOP_100_PERCENT.length(),
                "TOP " + recordCount + " ");

        HSQLDBSQLStatementHandler.logger.debug(stSQLString.toString());
        return new SQLStatementBuilder.SQLStatement(stSQLString.toString(), vValues);
    }

    // since 5.2079EN-0.5
    @Override
    public SQLStatementBuilder.SQLStatement createCountQuery(String table, Map conditions, List wildcards,
            List countColumns) {
        // SQLServer does not support count for several columns -> count(colA ||
        // colB)
        if ((countColumns != null) && (countColumns.size() > 1)) {
            // force to use count(*)
            countColumns.clear();
        }
        return super.createCountQuery(table, conditions, wildcards, countColumns);
    }

    @Override
    protected void changeGenerateKeyNames(EntityResult result, List columnNames) {
        if ((columnNames != null) && (columnNames.size() == 1)) {
            String columnName = (String) columnNames.get(0);
            this.changeColumnName(result, SQLServerSQLStatementHandler.GENERATED_KEY_COLUMN_NAME, columnName);
        }
    }

    @Override
    public String convertPaginationStatement(String sqlTemplate, int startIndex, int recordNumber) {
        throw new RuntimeException("TODO: Implements convertPaginationStatement");
        // return super.convertPaginationStatement(sqlTemplate, startIndex, recordNumber);
    }

    @Override
    public String addCastStatement(final String expression, final int fromSqlType, final int toSqlType) {
        try {
            return SQLServerSQLStatementHandler.CAST_FUNCTION + "(" + expression + " AS " + sqlHandler.getSQLTypeName(toSqlType) + ")";
        } catch (Exception e) {
            SQLServerSQLStatementHandler.logger.error("Could not add cast statement", e);
            return expression;
        }
    }

    @Override
    public SQLStatementBuilder.SQLStatement createTemporalTableStatement(String tableName, SQLStatementBuilder.SQLStatement selectSql, TemporalTableScope scope) {
        String prefix = (scope == TemporalTableScope.GLOBAL) ? "##" : "#";
        String sentence = "SELECT * INTO " + prefix + tableName + " FROM (" + selectSql.getSQLStatement() + ") AS origen;";
        List<Object> sentenceValues = selectSql.getValues();
        return new SQLStatementBuilder.SQLStatement(sentence, sentenceValues);
    }

    @Override
    public SQLStatementBuilder.SQLStatement dropTemporalTableStatement(String tableName, TemporalTableScope scope) {
        String prefix = (scope == TemporalTableScope.GLOBAL) ? "##" : "#";
        String sentence = "DROP TABLE IF EXISTS " + prefix + tableName + ";";
        return new SQLStatementBuilder.SQLStatement(sentence);

    }
}

package com.ontimize.jee.common.db.handler;

import com.ontimize.jee.common.db.LocalePair;
import com.ontimize.jee.common.db.SQLStatementBuilder;
import com.ontimize.jee.core.common.dto.EntityResult;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface SQLStatementHandler {

    public void addSpecialCharacters(char[] c);

    public SQLStatementBuilder.SQLStatement createCountQuery(String table, Map conditions, List wildcards,
            List countColumns);

    public SQLStatementBuilder.SQLStatement createDeleteQuery(String table, Map keysValues);

    public SQLStatementBuilder.SQLStatement createInsertQuery(String table, Map attributes);

    public SQLStatementBuilder.SQLStatement createJoinSelectQuery(String principalTable, String secondaryTable,
            List principalKeys,
            List secondaryKeys, List principalTableRequestedColumns,
            List secondaryTableRequestedColumns, Map principalTableConditions,
            Map secondaryTableConditions, List wildcards, List columnSorting,
            boolean forceDistinct);

    public SQLStatementBuilder.SQLStatement createJoinSelectQuery(String mainTable, String secondaryTable,
            List mainKeys,
            List secondaryKeys, List mainTableRequestedColumns,
            List secondaryTableRequestedColumns, Map mainTableConditions, Map secondaryTableConditions,
            List wildcards, List columnSorting, boolean forceDistinct,
            boolean descending);

    public SQLStatementBuilder.SQLStatement createJoinFromSubselectsQuery(String primaryAlias, String secondaryAlias,
            String primaryQuery,
            String secondaryQuery, List primaryKeys,
            List secondaryKeys, List primaryTableRequestedColumns, List secondaryTableRequestedColumns,
            Map primaryTableConditions, Map secondaryTableConditions,
            List wildcards, List columnSorting, boolean forceDistinct, boolean descending);

    public SQLStatementBuilder.SQLStatement createLeftJoinSelectQuery(String mainTable, String subquery,
            String secondaryTable,
            List mainKeys, List secondaryKeys, List mainTableRequestedColumns,
            List secondaryTableRequestedColumns, Map mainTableConditions, Map secondaryTableConditions,
            List wildcards, List columnSorting, boolean forceDistinct,
            boolean descending);

    public SQLStatementBuilder.SQLStatement createLeftJoinSelectQueryPageable(String mainTable, String subquery,
            String secondaryTable,
            List mainKeys, List secondaryKeys,
            List mainTableRequestedColumns, List secondaryTableRequestedColumns, Map mainTableConditions,
            Map secondaryTableConditions, List wildcards,
            List columnSorting, boolean forceDistinct, boolean descending, int recordNumber, int startIndex);

    public SQLStatementBuilder.SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
            List wildcards);

    public SQLStatementBuilder.SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
            List wildcards,
            List columnSorting);

    public SQLStatementBuilder.SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
            List wildcards,
            List columnSorting, boolean descending);

    public SQLStatementBuilder.SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
            List wildcards,
            List columnSorting, boolean descending,
            boolean forceDistinct);

    public SQLStatementBuilder.SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
            List wildcards,
            List columnSorting, int recordCount);

    public SQLStatementBuilder.SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
            List wildcards,
            List columnSorting, int recordCount, boolean descending);

    public SQLStatementBuilder.SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
            List wildcards,
            List columnSorting, int recordCount, boolean descending,
            boolean forceDistinct);

    public SQLStatementBuilder.SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
            List wildcards,
            List columnSorting, int recordCount, int offset);

    public SQLStatementBuilder.SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
            List wildcards,
            List columnSorting, int recordCount, int offset,
            boolean descending);

    public SQLStatementBuilder.SQLStatement createSelectQuery(String table, List requestedColumns, Map conditions,
            List wildcards,
            List columnSorting, int recordCount, int offset,
            boolean descending, boolean forceDistinct);

    public SQLStatementBuilder.SQLStatement createUpdateQuery(String table, Map attributesValues, Map keysValues);

    public SQLStatementBuilder.SQLConditionValuesProcessor getQueryConditionsProcessor();

    public SQLStatementBuilder.SQLNameEval getSQLNameEval();

    public String qualify(String col, String table);

    public void setSQLConditionValuesProcessor(SQLStatementBuilder.SQLConditionValuesProcessor processor);

    public void setSQLNameEval(SQLStatementBuilder.SQLNameEval eval);

    public boolean isUseAsInSubqueries();

    public void setUseAsInSubqueries(boolean useAsInSubqueries);

    public boolean checkColumnName(String columnName);

    public String createQueryConditionsWithoutWhere(Map conditions, List wildcard, List values);

    public boolean isPageable();

    public boolean isDelimited();

    public void resultSetToEntityResult(ResultSet resultSet, EntityResult entityResult, List columnNames)
            throws Exception;

    public void resultSetToEntityResult(ResultSet resultSet, EntityResult entityResult, int recordNumber, int offset,
            boolean delimited, List columnNames) throws Exception;

    public void generatedKeysToEntityResult(ResultSet resultSet, EntityResult entityResult, List generatedKeys)
            throws Exception;

    public void setObject(int index, Object value, PreparedStatement preparedStatement, boolean truncDates)
            throws SQLException;

    public String addMultilanguageLeftJoinTables(String table, List tables, LinkedHashMap hOtherLocaleTablesKey,
            LocalePair localeId) throws SQLException;

    public String addInnerMultilanguageColumns(String subSqlQuery, List attributtes, Map hLocaleTablesAV);

    public String addOuterMultilanguageColumns(String sqlQuery, String table, Map hLocaleTablesAV);

    public String addOuterMultilanguageColumnsPageable(String sqlQuery, String table, Map hLocaleTablesAV);

    public String createSortStatement(List sortColumns);

    public String createSortStatement(List sortColumns, boolean b);

    /**
     * Convert a query statement in a pagination query statement
     * @param sqlTemplate
     * @param startIndex
     * @param recordNumber
     * @return
     */
    public String convertPaginationStatement(String statement, int startIndex, int recordNumber);

}

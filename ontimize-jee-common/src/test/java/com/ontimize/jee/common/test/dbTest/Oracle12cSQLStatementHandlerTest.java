package com.ontimize.jee.common.test.dbTest;

import com.ontimize.jee.common.db.handler.Oracle12cSQLStatementHandler;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class Oracle12cSQLStatementHandlerTest {

    @InjectMocks
    Oracle12cSQLStatementHandler oracle12cSQLStatementHandler;


    @Nested
    class CreateSelectQuery {
        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_descending_is_true_and_forceDistinct_is_false_expect_select_query() {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            int recordCount = 1;
            int offset = 1;
            boolean descending = true;
            boolean forceDistinct = false;

            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = oracle12cSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, offset, descending, forceDistinct);
            var expected = "SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC  LIMIT 1 OFFSET 1";

            assertEquals(expected, result.getSQLStatement().trim());
        }
    }


    @Nested
    class AddOuterMultilanguageColumnsPageable {

        @Test
        void when_receive_sqlQuery_and_table_and_hLocaleTablesAV_expect_add_outer_multilanguage_columns_pageable() {
            var sqlQuery = " from";
            String table = "my table";
            HashMap hLocaleTablesAV = new HashMap();

            hLocaleTablesAV.put("field1", "value1");

            var result = oracle12cSQLStatementHandler.addOuterMultilanguageColumnsPageable(sqlQuery, table, hLocaleTablesAV);
            var expected = ", my table.value1 from";

            assertEquals(expected, result.trim());
        }
    }


    @Nested
    class CreateLeftJoinSelectQueryPageable {

        @Test
        void when_receive_mainTable_and_subquery_and_secondaryTable_and_mainKeys_and_secondaryKeys_and_mainTableRequestedColumns_and_secondaryTableRequestedColumns_and_mainTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_is_false_and_descending_is_false_and_recordNumber_and_startIndex_expect_LeftJoinSelect_query_pageable() {

            String mainTable = "mainTable";
            String subquery = "subquery";
            String secondaryTable = "secondaryTable";
            ArrayList mainKeys = new ArrayList();
            ArrayList secondaryKeys = new ArrayList();
            ArrayList mainTableRequestedColumns = new ArrayList();
            ArrayList secondaryTableRequestedColumns = new ArrayList();
            HashMap mainTableConditions = new HashMap();
            HashMap secondaryTableConditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            boolean forceDistinct = false;
            boolean descending = false;
            int recordNumber = 0;
            int startIndex = 0;


            mainKeys.add("mainKeys");
            secondaryKeys.add("secondaryTable");
            mainTableRequestedColumns.add("mainTableRequestedColumns");
            secondaryTableRequestedColumns.add("secondaryTableRequestedColumns");
            mainTableConditions.put("field1", "value1");
            secondaryTableConditions.put("field2", "value2");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting");

            var result = oracle12cSQLStatementHandler.createLeftJoinSelectQueryPageable(mainTable, subquery, secondaryTable, mainKeys, secondaryKeys, mainTableRequestedColumns, secondaryTableRequestedColumns, mainTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending, recordNumber, startIndex);
            var expected = "SELECT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting OFFSET 0 ROWS FETCH NEXT 0 ROWS ONLY";

            assertEquals(expected, result.getSQLStatement().trim());
        }
}

    @Nested
    class ConvertPaginationStatement{

        @Test
        void when_receive_sqlTemplate_and_starIndex_and_recordNumber_expected_convert_pagination_statement(){
            String sqlTemplate =  "sqlTemplate";
            int starIndex = 1;
            int recordNumber =1;

            var result = oracle12cSQLStatementHandler.convertPaginationStatement(sqlTemplate,starIndex,recordNumber);
            var expected= "sqlTemplate OFFSET 1 ROWS FETCH NEXT 1 ROWS ONLY";

            assertEquals(expected, result.trim());
        }
    }

}


package com.ontimize.jee.common.test.dbTest;

import com.ontimize.jee.common.db.handler.PostgresSQLStatementHandler;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PostgresSQLStatementHandlerTest {


    @InjectMocks
    PostgresSQLStatementHandler postgresSQLStatementHandler;


    @Nested
    class CreateSelectQuery {

        @Test
        void when_receive_one_table_expect_select_query() {
            var table = "my-table";
            HashMap conditions = null;
            ArrayList wildcards = null;
            ArrayList requestedColumns = null;

            var result = postgresSQLStatementHandler.createSelectQuery(table, null, null, null);
            var expected = "SELECT  *  FROM  [my-table]";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_descending_is_false_and_forceDistinct_is_false_expect_select_query() {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            int recordCount = 1;
            int offset = 1;
            boolean descending = false;
            boolean forceDistinct = false;

            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = postgresSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, offset, descending, forceDistinct);
            var expected = "SELECT * FROM ( SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1) WHERE ROWNUM<= 2";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_descending_is_true_and_forceDistinct_is_true_expect_select_query() {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            int recordCount = 1;
            int offset = 1;
            boolean descending = true;
            boolean forceDistinct = true;

            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = postgresSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, offset, descending, forceDistinct);
            var expected = "SELECT * FROM ( SELECT  DISTINCT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC ) WHERE ROWNUM<= 2";

            assertEquals(expected, result.getSQLStatement().trim());
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

            var result = postgresSQLStatementHandler.createLeftJoinSelectQueryPageable(mainTable, subquery, secondaryTable, mainKeys, secondaryKeys, mainTableRequestedColumns, secondaryTableRequestedColumns, mainTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending, recordNumber, startIndex);
            var expected = "SELECT * FROM ( SELECT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting) WHERE ROWNUM<= 0";

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

            var result = postgresSQLStatementHandler.convertPaginationStatement(sqlTemplate,starIndex,recordNumber);
            var expected= "sqlTemplate LIMIT 1 OFFSET 1";

            assertEquals(expected, result.trim());
        }
    }


}
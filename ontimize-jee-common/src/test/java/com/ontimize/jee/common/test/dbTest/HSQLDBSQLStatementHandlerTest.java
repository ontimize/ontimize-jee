package com.ontimize.jee.common.test.dbTest;

import com.ontimize.jee.common.db.handler.HSQLDBSQLStatementHandler;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class HSQLDBSQLStatementHandlerTest {

    @InjectMocks
    HSQLDBSQLStatementHandler hsqldbsqlStatementHandler;

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class CreateSelectQuery {

        @Test
        void when_receive_one_table_expect_select_query() {
            var table = "my-table";
            HashMap conditions = null;
            ArrayList wildcards = null;
            ArrayList requestedColumns = null;

            var result = hsqldbsqlStatementHandler.createSelectQuery(table, null, null, null);
            var expected = "SELECT  *  FROM  [my-table]";

            assertEquals(expected, result.getSQLStatement().trim());
        }


        @ParameterizedTest
        @MethodSource("addDataCreateSelectQuery")
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_descending_and_forceDistinct_expect_select_query(boolean descending, boolean forceDistinct, String expected) {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            int recordCount = 1;
            int offset = 1;


            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = hsqldbsqlStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, offset, descending, forceDistinct);

            assertEquals(expected, result.getSQLStatement().trim());
        }

        Stream<Arguments> addDataCreateSelectQuery() {
            return Stream.of(
                    Arguments.of(true, false, "SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC  LIMIT 1 OFFSET 1"),
                    Arguments.of(true, true, "SELECT  DISTINCT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC  LIMIT 1 OFFSET 1"),
                    Arguments.of(false, false, "SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 LIMIT 1 OFFSET 1"),
                    Arguments.of(false, true, "SELECT  DISTINCT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 LIMIT 1 OFFSET 1")
            );
        }

    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class CreateLeftJoinSelectQueryPageable {
        @ParameterizedTest
        @MethodSource("addDataCreateJoinSelectQuery")
        void when_receive_mainTable_and_subquery_and_secondaryTable_and_mainKeys_and_secondaryKeys_and_mainTableRequestedColumns_and_secondaryTableRequestedColumns_and_mainTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_is_false_and_descending_is_false_and_recordNumber_and_startIndex_expect_LeftJoinSelect_query_pageable(boolean descending, boolean forceDistinct, String expected) {

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

            var result = hsqldbsqlStatementHandler.createLeftJoinSelectQueryPageable(mainTable, subquery, secondaryTable, mainKeys, secondaryKeys, mainTableRequestedColumns, secondaryTableRequestedColumns, mainTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending, recordNumber, startIndex);

            assertEquals(expected, result.getSQLStatement().trim());
        }

        Stream<Arguments> addDataCreateJoinSelectQuery() {
            return Stream.of(
                    Arguments.of(true, false, "SELECT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting DESC  LIMIT 0 OFFSET 0"),
                    Arguments.of(true, true, "SELECT  DISTINCT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting DESC  LIMIT 0 OFFSET 0"),
                    Arguments.of(false, false, "SELECT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting LIMIT 0 OFFSET 0"),
                    Arguments.of(false, true, "SELECT  DISTINCT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting LIMIT 0 OFFSET 0")
            );
        }
    }

    @Nested
    class ConvertPaginationStatement {

        @Test
        void when_receive_sqlTemplate_and_starIndex_and_recordNumber_expected_convert_pagination_statement() {
            String sqlTemplate = "sqlTemplate";
            int starIndex = 1;
            int recordNumber = 1;

            var result = hsqldbsqlStatementHandler.convertPaginationStatement(sqlTemplate, starIndex, recordNumber);
            var expected = "sqlTemplate LIMIT 1 OFFSET 1";

            assertEquals(expected, result.trim());
        }
    }

}
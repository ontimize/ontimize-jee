package com.ontimize.jee.common.test.dbTest;

import com.ontimize.jee.common.db.handler.Oracle12cSQLStatementHandler;
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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class Oracle12cSQLStatementHandlerTest {

    @InjectMocks
    Oracle12cSQLStatementHandler oracle12cSQLStatementHandler;


    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class CreateSelectQuery {

        @Test
        void when_receive_one_table_expect_select_query() {
            var table = "my-table";
            HashMap conditions = null;
            ArrayList wildcards = null;
            ArrayList requestedColumns = null;

            var result = oracle12cSQLStatementHandler.createSelectQuery(table, null, null, null);
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

            var result = oracle12cSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, offset, descending, forceDistinct);

            assertEquals(expected, result.getSQLStatement().trim());
        }

        Stream<Arguments> addDataCreateSelectQuery() {
            return Stream.of(
                    Arguments.of(true, false, "SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC  OFFSET 1 ROWS FETCH NEXT 1 ROWS ONLY"),
                    Arguments.of(true, true, "SELECT  DISTINCT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC  OFFSET 1 ROWS FETCH NEXT 1 ROWS ONLY"),
                    Arguments.of(false, false, "SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 OFFSET 1 ROWS FETCH NEXT 1 ROWS ONLY"),
                    Arguments.of(false, true, "SELECT  DISTINCT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 OFFSET 1 ROWS FETCH NEXT 1 ROWS ONLY")
            );
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

            var result = oracle12cSQLStatementHandler.createLeftJoinSelectQueryPageable(mainTable, subquery, secondaryTable, mainKeys, secondaryKeys, mainTableRequestedColumns, secondaryTableRequestedColumns, mainTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending, recordNumber, startIndex);

            assertEquals(expected, result.getSQLStatement().trim());
        }

        Stream<Arguments> addDataCreateJoinSelectQuery() {
            return Stream.of(
                    Arguments.of(true, false, "SELECT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting DESC  OFFSET 0 ROWS FETCH NEXT 0 ROWS ONLY"),
                    Arguments.of(true, true, "SELECT  DISTINCT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting DESC  OFFSET 0 ROWS FETCH NEXT 0 ROWS ONLY"),
                    Arguments.of(false, false, "SELECT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting OFFSET 0 ROWS FETCH NEXT 0 ROWS ONLY"),
                    Arguments.of(false, true, "SELECT  DISTINCT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting OFFSET 0 ROWS FETCH NEXT 0 ROWS ONLY")
            );
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


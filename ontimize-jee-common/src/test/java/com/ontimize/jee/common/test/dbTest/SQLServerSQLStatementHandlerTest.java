package com.ontimize.jee.common.test.dbTest;

import com.ontimize.jee.common.db.handler.SQLServerSQLStatementHandler;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class SQLServerSQLStatementHandlerTest {

    @InjectMocks
    SQLServerSQLStatementHandler sqlServerSQLStatementHandler;


    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class CreateSelectQuery {

        @Test
        void when_receive_one_table_expect_select_query() {
            var table = "my-table";
            HashMap conditions = null;
            ArrayList wildcards = null;
            ArrayList requestedColumns = null;

            var result = sqlServerSQLStatementHandler.createSelectQuery(table, null, null, null);
            var expected = "SELECT  TOP 100 PERCENT  *  FROM  [my-table]";

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

            var result = sqlServerSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, offset, descending, forceDistinct);

            assertEquals(expected, result.getSQLStatement().trim());
        }

        Stream<Arguments> addDataCreateSelectQuery() {
            return Stream.of(
                    Arguments.of(true, false, "SELECT  TOP  2 requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC"),
                    Arguments.of(true, true, "SELECT  DISTINCT  TOP  2 requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC"),
                    Arguments.of(false, false, "SELECT  TOP  2 requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1"),
                    Arguments.of(false, true, "SELECT  DISTINCT  TOP  2 requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1")
            );
        }

    }


    @Nested
    class CreateCountQuery {
        @Test
        void when_receive_one_table_expect_count_query() {
            var table = "my-table";
            HashMap conditions = null;
            ArrayList wildcards = null;
            ArrayList countColumns = null;

            var result = sqlServerSQLStatementHandler.createCountQuery(table, null, null, null);
            var expected = "SELECT  COUNT(*) AS \"TotalRecordNumber\"  FROM  [my-table]";

            assertEquals(expected, result.getSQLStatement().trim());

        }


        @Test
        void when_receive_one_table_and_conditions_expect_count_query() {
            var table = "my-table";
            HashMap conditions = null;
            ArrayList wildcards = null;
            ArrayList countColumns = null;

            conditions = new HashMap();
            conditions.put("field1", "value1");

            var result = sqlServerSQLStatementHandler.createCountQuery(table, conditions, null, null);
            var expected = "SELECT  COUNT(*) AS \"TotalRecordNumber\"  FROM  [my-table]   WHERE field1 = ?";

            assertEquals(expected, result.getSQLStatement().trim());

        }


        @Test
        void when_receive_table_and_conditions_and_wildcards_expect_count_query() {
            var table = "my-table";
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList countColumns = null;


            conditions.put("field1", "value1");
            wildcards.add("wildcard1");

            var result = sqlServerSQLStatementHandler.createCountQuery(table, conditions, wildcards, null);
            var expected = "SELECT  COUNT(*) AS \"TotalRecordNumber\"  FROM  [my-table]   WHERE field1 = ?";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_conditions_and_wildcards_and_countColumns_expect_count_query() {
            var table = "my-table";
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList countColumns = new ArrayList();


            conditions.put("field1", "value1");
            wildcards.add("wildcard1");
            countColumns.add("countColumns1");

            var result = sqlServerSQLStatementHandler.createCountQuery(table, conditions, wildcards, countColumns);
            var expected = "SELECT  COUNT( countColumns1 )  AS \"TotalRecordNumber\"  FROM  [my-table]   WHERE field1 = ?";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_countColumns_expect_count_query() {
            var table = "my-table";
            HashMap conditions = null;
            ArrayList wildcards = null;
            ArrayList countColumns = new ArrayList();

            countColumns.add("countColumns1");

            var result = sqlServerSQLStatementHandler.createCountQuery(table, null, null, countColumns);
            var expected = "SELECT  COUNT( countColumns1 )  AS \"TotalRecordNumber\"  FROM  [my-table]";

            assertEquals(expected, result.getSQLStatement().trim());
        }

    }


    @Nested
    class ConvertPaginationStatement{

        @Test
        void when_receive_sqlTemplate_and_starIndex_and_recordNumber_expected_RuntimeException(){
            String sqlTemplate =  "sqlTemplate";
            int starIndex = 1;
            int recordNumber =1;

            assertThrows(RuntimeException.class,()->sqlServerSQLStatementHandler.convertPaginationStatement(sqlTemplate,starIndex,recordNumber));
        }
    }

}
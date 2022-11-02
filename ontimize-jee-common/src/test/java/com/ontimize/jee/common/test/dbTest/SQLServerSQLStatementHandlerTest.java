package com.ontimize.jee.common.test.dbTest;

import com.ontimize.jee.common.db.handler.SQLServerSQLStatementHandler;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class SQLServerSQLStatementHandlerTest {

    @InjectMocks
    SQLServerSQLStatementHandler sqlServerSQLStatementHandler;


    @Nested
    class CreateSelectQuery {


        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_descending_is_false_and_forceDistinct_is_false_expect_select_query() {

            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            int recordCount = 1;
            boolean descending = false;
            boolean forceDistinct = false;

            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = sqlServerSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, descending, forceDistinct);
            var expected = "SELECT  TOP  1 requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

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
            boolean descending = true;
            boolean forceDistinct = true;

            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = sqlServerSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, descending, forceDistinct);
            var expected = "SELECT  DISTINCT  TOP  1 requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC";

            assertEquals(expected, result.getSQLStatement().trim());
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
package com.ontimize.jee.common.test.dbTest;

import com.ontimize.jee.common.db.LocalePair;
import com.ontimize.jee.common.db.SQLStatementBuilder;
import com.ontimize.jee.common.db.handler.DefaultSQLStatementHandler;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DefaultSQLStatementHandlerTest {


    boolean useAsInSubqueries = true;

    @InjectMocks
    DefaultSQLStatementHandler defaultSQLStatementHandler;

    @Mock
    ResultSet resultSet;


    EntityResult entityResult;

    @Test
    void checkColumnName_when_receive_columnName_expect_check_columnName() {
        var result = this.defaultSQLStatementHandler.checkColumnName("COLUMN1");
        assertFalse(result);

        result = this.defaultSQLStatementHandler.checkColumnName("MY COLUMN");
        assertTrue(result);

        result = this.defaultSQLStatementHandler.checkColumnName("COLUMN1 as MYCOLUMN");
        assertFalse(result);

        result = this.defaultSQLStatementHandler.checkColumnName("COLUMN1 as MY COLUMN");
        assertTrue(result);

        result = this.defaultSQLStatementHandler.checkColumnName("MY COLUM 1 as MY ALIAS COLUMN 1");
        assertTrue(result);


    }

    @Nested
    class CreateUpdateQuery {

        @Test
        void when_receive_table_and_attributes_expect_update_query() {
            String table = "table1";
            HashMap attributes = new HashMap();

            attributes.put("field1", "value1");

            var result = defaultSQLStatementHandler.createUpdateQuery(table, attributes, null);
            var expected = "UPDATE table1 SET field1 = ?";

            assertEquals(expected, result.getSQLStatement().trim());

        }

        @Test
        void when_receive_table_and_attributes_and_keysValues_expect_update_query() {
            String table = "table1";
            HashMap attributes = new HashMap();
            HashMap keysValues = new HashMap();

            attributes.put("field1", "value1");
            keysValues.put("keys1", "values1");

            var result = defaultSQLStatementHandler.createUpdateQuery(table, attributes, null);
            var expected = "UPDATE table1 SET field1 = ?";

            assertEquals(expected, result.getSQLStatement().trim());

        }
    }

    @Nested
    class CreateDeleteQuery {
        @Test
        void when_receive_table_expect_delete_query() {
            String table = "table1";

            var result = defaultSQLStatementHandler.createDeleteQuery(table, null);
            var expected = "DELETE  FROM table1";

            assertEquals(expected, result.getSQLStatement().trim());

        }

        @Test
        void when_receive_table_and_keysValues_expect_delete_query() {
            String table = "table1";
            HashMap keysValues = new HashMap();

            keysValues.put("field1", "value1");

            var result = defaultSQLStatementHandler.createDeleteQuery(table, keysValues);
            var expected = "DELETE  FROM table1  WHERE field1 = ?";

            assertEquals(expected, result.getSQLStatement().trim());

        }
    }

    @Nested
    class CreateInsertQuery {

        @Test
        void when_receive_table_and_attibutes_expect_insert_query() {
            String table = "table1";
            HashMap attributes = new HashMap();

            attributes.put("field1", "value1");

            var result = defaultSQLStatementHandler.createInsertQuery(table, attributes);
            var expected = "INSERT INTO table1 ( field1 )  VALUES ( ?  )";

            assertEquals(expected, result.getSQLStatement().trim());
        }
    }

    @Nested
    class CreateQueryConditionsWithoutWhere {


        @Test
        void when_receive_conditions_expect_query_without_where() {
            HashMap conditions = new HashMap();
            ArrayList wildcard = new ArrayList();
            ArrayList values = new ArrayList();

            conditions.put("field1", "value1");

            var result = defaultSQLStatementHandler.createQueryConditionsWithoutWhere(conditions, wildcard, values);
            var expected = "field1 = ?";

            assertEquals(expected, result.trim());
        }

        @Test
        void when_receive_conditions_and_wildcard_expect_query_without_where() {
            HashMap conditions = new HashMap();
            ArrayList wildcard = new ArrayList();
            ArrayList values = new ArrayList();

            conditions.put("field1", "value1");
            conditions.put("field2", "value2");
            wildcard.add("wildcard1");

            var result = defaultSQLStatementHandler.createQueryConditionsWithoutWhere(conditions, wildcard, values);
            var expected = "field1 = ?  AND field2 = ?";

            assertEquals(expected, result.trim());
        }

        @Test
        void when_receive_conditions_and_wildcard_and_values_expect_query_without_where() {
            HashMap conditions = new HashMap();
            ArrayList wildcard = new ArrayList();
            ArrayList values = new ArrayList();

            conditions.put("field1", "value1");
            wildcard.add("wildcard1");
            values.add("values1");

            var result = defaultSQLStatementHandler.createQueryConditionsWithoutWhere(conditions, wildcard, values);
            var expected = "field1 = ?";

            assertEquals(expected, result.trim());
        }

        @Test
        void when_receive_conditions_but_not_wildcard_and_values_expect_Exception_NullPointerException() {
            HashMap conditions = new HashMap();

            conditions.put("field1", "value1");

            assertThrows(NullPointerException.class, () -> defaultSQLStatementHandler.createQueryConditionsWithoutWhere(conditions, null, null));
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

            var result = defaultSQLStatementHandler.createCountQuery(table, null, null, null);
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

            var result = defaultSQLStatementHandler.createCountQuery(table, conditions, null, null);
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

            var result = defaultSQLStatementHandler.createCountQuery(table, conditions, wildcards, null);
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

            var result = defaultSQLStatementHandler.createCountQuery(table, conditions, wildcards, countColumns);
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

            var result = defaultSQLStatementHandler.createCountQuery(table, null, null, countColumns);
            var expected = "SELECT  COUNT( countColumns1 )  AS \"TotalRecordNumber\"  FROM  [my-table]";

            assertEquals(expected, result.getSQLStatement().trim());
        }

    }

    @Nested
    class CreateSelectQuery {
        @Test
        void when_receive_one_table_expect_select_query() {
            var table = "my-table";
            HashMap conditions = null;
            ArrayList wildcards = null;
            ArrayList requestedColumns = null;

            var result = defaultSQLStatementHandler.createSelectQuery(table, null, null, null);
            var expected = "SELECT  *  FROM  [my-table]";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_expect_select_query() {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = null;
            ArrayList wildcards = null;

            requestedColumns.add("requestedColumns1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, null, null);
            var expected = "SELECT requestedColumns1 FROM  [my-table]";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_and_conditions_expect_select_query() {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = null;

            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, null);
            var expected = "SELECT requestedColumns1 FROM  [my-table]   WHERE field1 = ?";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_expect_select_query() {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();

            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards);
            var expected = "SELECT requestedColumns1 FROM  [my-table]   WHERE field1 = ?";

            assertEquals(expected, result.getSQLStatement().trim());
        }


        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_expect_select_query() {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            int recordCount = 1;

            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting);
            var expected = "SELECT requestedColumns1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_expect_select_query() {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            int recordCount = 10;

            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount);
            var expected = "SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_descending_is_true_expect_select_query() {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            int recordCount = 10;
            boolean descending = true;

            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, descending);
            var expected = "SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_descending_is_false_expect_select_query() {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            int recordCount = 10;
            boolean descending = false;

            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, descending);
            var expected = "SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

            assertEquals(expected, result.getSQLStatement().trim());
        }


        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_descending_is_true_and_forceDistinct_is_false_expect_select_query() {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            int recordCount = 10;
            boolean descending = true;
            boolean forceDistinct = false;

            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, descending, forceDistinct);
            var expected = "SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_descending_is_true_and_forceDistinct_is_true_expect_select_query() {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            int recordCount = 10;
            boolean descending = true;
            boolean forceDistinct = true;

            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, descending, forceDistinct);
            var expected = "SELECT  DISTINCT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_descending_is_false_and_forceDistinct_is_false_expect_select_query() {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            int recordCount = 10;
            boolean descending = false;
            boolean forceDistinct = false;

            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, descending, forceDistinct);
            var expected = "SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_descending_is_false_and_forceDistinct_is_true_expect_select_query() {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            int recordCount = 10;
            boolean descending = false;
            boolean forceDistinct = true;

            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, descending, forceDistinct);
            var expected = "SELECT  DISTINCT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_offset_expect_select_query() {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            int recordCount = 10;
            int offset = 5;

            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, offset);
            var expected = "SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_offset_and_descending_is_false_expect_select_query() {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            int recordCount = 10;
            int offset = 5;
            boolean descending = false;

            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, offset, descending);
            var expected = "SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

            assertEquals(expected, result.getSQLStatement().trim());
        }


        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_offset_and_descending_is_true_expect_select_query() {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            int recordCount = 10;
            int offset = 5;
            boolean descending = true;

            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, offset, descending);
            var expected = "SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC";

            assertEquals(expected, result.getSQLStatement().trim());
        }


        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_sum_offset_and_descending_is_false_and_forceDistinct_is_false_expect_select_query() {
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

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount + offset, descending, forceDistinct);
            var expected = "SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_sum_offset_and_descending_is_false_and_forceDistinct_is_true_expect_select_query() {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            int recordCount = 1;
            int offset = 1;
            boolean descending = false;
            boolean forceDistinct = true;

            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount + offset, descending, forceDistinct);
            var expected = "SELECT  DISTINCT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_sum_offset_and_descending_is_true_and_forceDistinct_is_true_expect_select_query() {
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

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount + offset, descending, forceDistinct);
            var expected = "SELECT  DISTINCT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_sum_offset_and_descending_is_true_and_forceDistinct_is_false_expect_select_query() {
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

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount + offset, descending, forceDistinct);
            var expected = "SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_descending_is_true_expect_select_query() {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            boolean descending = true;


            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, descending);
            var expected = "SELECT requestedColumns1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC";


            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_descending_is_false_expect_select_query() {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            boolean descending = false;


            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, descending);
            var expected = "SELECT requestedColumns1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_descending_is_true_and_forceDistinct_is_false_expect_select_query() {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            boolean descending = true;
            boolean forceDistinct = false;

            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, descending, forceDistinct);
            var expected = "SELECT requestedColumns1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_descending_is_true_and_forceDistinct_is_true_expect_select_query() {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            boolean descending = true;
            boolean forceDistinct = true;

            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, descending, forceDistinct);
            var expected = "SELECT  DISTINCT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_descending_is_false_and_forceDistinct_is_false_expect_select_query() {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            boolean descending = false;
            boolean forceDistinct = false;

            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, descending, forceDistinct);
            var expected = "SELECT requestedColumns1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_descending_is_false_and_forceDistinct_is_true_expect_select_query() {
            var table = "my-table";
            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            boolean descending = false;
            boolean forceDistinct = true;

            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, descending, forceDistinct);
            var expected = "SELECT  DISTINCT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

            assertEquals(expected, result.getSQLStatement().trim());
        }
    }

    @Nested
    class CreateSortStatement {

        @Test
        void when_receive_sortColumns_expect_SortStatement() {
            ArrayList sortColumns = new ArrayList();

            sortColumns.add("sortColumns");

            var result = defaultSQLStatementHandler.createSortStatement(sortColumns);
            var expected = "ORDER BY sortColumns";

            assertEquals(expected, result.trim());
        }


        @Test
        void when_receive_sortColumns_and_descending_is_true_expect_SortStatement() {
            ArrayList sortColumns = new ArrayList();
            boolean descending = true;

            sortColumns.add("sortColumns");

            var result = defaultSQLStatementHandler.createSortStatement(sortColumns, descending);
            var expected = "ORDER BY sortColumns DESC";

            assertEquals(expected, result.trim());
        }

        @Test
        void when_receive_sortColumns_and_descending_is_false_expect_SortStatement() {
            ArrayList sortColumns = new ArrayList();
            boolean descending = false;

            sortColumns.add("sortColumns");

            var result = defaultSQLStatementHandler.createSortStatement(sortColumns, descending);
            var expected = "ORDER BY sortColumns";

            assertEquals(expected, result.trim());
        }

    }

    @Nested
    class CreateJoinSelectQuery {


        @Test
        void when_receive_principalTable_and_secondaryTable_and_principalKeys_and_secondaryKeys_and_principalTableRequestedColumns_and_secondaryTableRequestedColumns_and_principalTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_is_false_and_descending_is_false_expect_JoinSelect_query() {

            String principalTable = "principalTable";
            String secondaryTable = "secondaryTable";
            ArrayList principalKeys = new ArrayList();
            ArrayList secondaryKeys = new ArrayList();
            ArrayList principalTableRequestedColumns = new ArrayList();
            ArrayList secondaryTableRequestedColumns = new ArrayList();
            HashMap principalTableConditions = new HashMap();
            HashMap secondaryTableConditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            boolean forceDistinct = false;
            boolean descending = false;

            principalKeys.add("principalKeys");
            secondaryKeys.add("secondaryTable");
            principalTableRequestedColumns.add("principalTableRequestedColumns");
            secondaryTableRequestedColumns.add("secondaryTableRequestedColumns");
            principalTableConditions.put("field1", "value1");
            secondaryTableConditions.put("field2", "value2");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting");

            var result = defaultSQLStatementHandler.createJoinSelectQuery(principalTable, secondaryTable, principalKeys, secondaryKeys, principalTableRequestedColumns, secondaryTableRequestedColumns, principalTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending);
            var expected = "SELECT principalTable.principalTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns " + "FROM principalTable,secondaryTable WHERE  principalTable.principalKeys=secondaryTable.secondaryTable AND  " + "secondaryTable.field2 = ?  AND principalTable.field1 = ?  ORDER BY columnSorting";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_principalTable_and_secondaryTable_and_principalKeys_and_secondaryKeys_and_principalTableRequestedColumns_and_secondaryTableRequestedColumns_and_principalTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_is_true_and_descending_is_false_expect_JoinSelect_query() {

            String principalTable = "principalTable";
            String secondaryTable = "secondaryTable";
            ArrayList principalKeys = new ArrayList();
            ArrayList secondaryKeys = new ArrayList();
            ArrayList principalTableRequestedColumns = new ArrayList();
            ArrayList secondaryTableRequestedColumns = new ArrayList();
            HashMap principalTableConditions = new HashMap();
            HashMap secondaryTableConditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            boolean forceDistinct = true;
            boolean descending = false;

            principalKeys.add("principalKeys");
            secondaryKeys.add("secondaryTable");
            principalTableRequestedColumns.add("principalTableRequestedColumns");
            secondaryTableRequestedColumns.add("secondaryTableRequestedColumns");
            principalTableConditions.put("field1", "value1");
            secondaryTableConditions.put("field2", "value2");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting");

            var result = defaultSQLStatementHandler.createJoinSelectQuery(principalTable, secondaryTable, principalKeys, secondaryKeys, principalTableRequestedColumns, secondaryTableRequestedColumns, principalTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending);
            var expected = "SELECT  DISTINCT principalTable.principalTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM principalTable,secondaryTable WHERE  principalTable.principalKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND principalTable.field1 = ?  ORDER BY columnSorting";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_principalTable_and_secondaryTable_and_principalKeys_and_secondaryKeys_and_principalTableRequestedColumns_and_secondaryTableRequestedColumns_and_principalTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_is_true_and_descending_is_true_expect_JoinSelect_query() {

            String principalTable = "principalTable";
            String secondaryTable = "secondaryTable";
            ArrayList principalKeys = new ArrayList();
            ArrayList secondaryKeys = new ArrayList();
            ArrayList principalTableRequestedColumns = new ArrayList();
            ArrayList secondaryTableRequestedColumns = new ArrayList();
            HashMap principalTableConditions = new HashMap();
            HashMap secondaryTableConditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            boolean forceDistinct = true;
            boolean descending = true;

            principalKeys.add("principalKeys");
            secondaryKeys.add("secondaryTable");
            principalTableRequestedColumns.add("principalTableRequestedColumns");
            secondaryTableRequestedColumns.add("secondaryTableRequestedColumns");
            principalTableConditions.put("field1", "value1");
            secondaryTableConditions.put("field2", "value2");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting");

            var result = defaultSQLStatementHandler.createJoinSelectQuery(principalTable, secondaryTable, principalKeys, secondaryKeys, principalTableRequestedColumns, secondaryTableRequestedColumns, principalTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending);
            var expected = "SELECT  DISTINCT principalTable.principalTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM principalTable,secondaryTable WHERE  principalTable.principalKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND principalTable.field1 = ?  ORDER BY columnSorting DESC";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_principalTable_and_secondaryTable_and_principalKeys_and_secondaryKeys_and_principalTableRequestedColumns_and_secondaryTableRequestedColumns_and_principalTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_is_false_and_descending_is_true_expect_JoinSelect_query() {

            String principalTable = "principalTable";
            String secondaryTable = "secondaryTable";
            ArrayList principalKeys = new ArrayList();
            ArrayList secondaryKeys = new ArrayList();
            ArrayList principalTableRequestedColumns = new ArrayList();
            ArrayList secondaryTableRequestedColumns = new ArrayList();
            HashMap principalTableConditions = new HashMap();
            HashMap secondaryTableConditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            boolean forceDistinct = false;
            boolean descending = true;

            principalKeys.add("principalKeys");
            secondaryKeys.add("secondaryTable");
            principalTableRequestedColumns.add("principalTableRequestedColumns");
            secondaryTableRequestedColumns.add("secondaryTableRequestedColumns");
            principalTableConditions.put("field1", "value1");
            secondaryTableConditions.put("field2", "value2");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting");

            var result = defaultSQLStatementHandler.createJoinSelectQuery(principalTable, secondaryTable, principalKeys, secondaryKeys, principalTableRequestedColumns, secondaryTableRequestedColumns, principalTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending);
            var expected = "SELECT principalTable.principalTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM principalTable,secondaryTable WHERE  principalTable.principalKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND principalTable.field1 = ?  ORDER BY columnSorting DESC";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_mainTable_and_secondaryTable_and_mainKeys_and_secondaryKeys_and_mainTableRequestedColumns_and_secondaryTableRequestedColumns_and_mainTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_is_false_and_descending_is_false_expect_JoinSelect_query() {

            String mainTable = "mainTable";
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


            mainKeys.add("mainKeys");
            secondaryKeys.add("secondaryTable");
            mainTableRequestedColumns.add("mainTableRequestedColumns");
            secondaryTableRequestedColumns.add("secondaryTableRequestedColumns");
            mainTableConditions.put("field1", "value1");
            secondaryTableConditions.put("field2", "value2");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting");

            var result = defaultSQLStatementHandler.createJoinSelectQuery(mainTable, secondaryTable, mainKeys, secondaryKeys, mainTableRequestedColumns, secondaryTableRequestedColumns, mainTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending);
            var expected = "SELECT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM mainTable,secondaryTable WHERE  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_mainTable_and_secondaryTable_and_mainKeys_and_secondaryKeys_and_mainTableRequestedColumns_and_secondaryTableRequestedColumns_and_mainTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_is_true_and_descending_is_false_expect_JoinSelect_query() {

            String mainTable = "mainTable";
            String secondaryTable = "secondaryTable";
            ArrayList mainKeys = new ArrayList();
            ArrayList secondaryKeys = new ArrayList();
            ArrayList mainTableRequestedColumns = new ArrayList();
            ArrayList secondaryTableRequestedColumns = new ArrayList();
            HashMap mainTableConditions = new HashMap();
            HashMap secondaryTableConditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            boolean forceDistinct = true;
            boolean descending = false;


            mainKeys.add("mainKeys");
            secondaryKeys.add("secondaryTable");
            mainTableRequestedColumns.add("mainTableRequestedColumns");
            secondaryTableRequestedColumns.add("secondaryTableRequestedColumns");
            mainTableConditions.put("field1", "value1");
            secondaryTableConditions.put("field2", "value2");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting");

            var result = defaultSQLStatementHandler.createJoinSelectQuery(mainTable, secondaryTable, mainKeys, secondaryKeys, mainTableRequestedColumns, secondaryTableRequestedColumns, mainTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending);
            var expected = "SELECT  DISTINCT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM mainTable,secondaryTable WHERE  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_mainTable_and_secondaryTable_and_mainKeys_and_secondaryKeys_and_mainTableRequestedColumns_and_secondaryTableRequestedColumns_and_mainTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_is_true_and_descending_is_true_expect_JoinSelect_query() {

            String mainTable = "mainTable";
            String secondaryTable = "secondaryTable";
            ArrayList mainKeys = new ArrayList();
            ArrayList secondaryKeys = new ArrayList();
            ArrayList mainTableRequestedColumns = new ArrayList();
            ArrayList secondaryTableRequestedColumns = new ArrayList();
            HashMap mainTableConditions = new HashMap();
            HashMap secondaryTableConditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            boolean forceDistinct = true;
            boolean descending = true;


            mainKeys.add("mainKeys");
            secondaryKeys.add("secondaryTable");
            mainTableRequestedColumns.add("mainTableRequestedColumns");
            secondaryTableRequestedColumns.add("secondaryTableRequestedColumns");
            mainTableConditions.put("field1", "value1");
            secondaryTableConditions.put("field2", "value2");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting");

            var result = defaultSQLStatementHandler.createJoinSelectQuery(mainTable, secondaryTable, mainKeys, secondaryKeys, mainTableRequestedColumns, secondaryTableRequestedColumns, mainTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending);
            var expected = "SELECT  DISTINCT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM mainTable,secondaryTable WHERE  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting DESC";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_mainTable_and_secondaryTable_and_mainKeys_and_secondaryKeys_and_mainTableRequestedColumns_and_secondaryTableRequestedColumns_and_mainTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_is_false_and_descending_is_true_expect_JoinSelect_query() {

            String mainTable = "mainTable";
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
            boolean descending = true;


            mainKeys.add("mainKeys");
            secondaryKeys.add("secondaryTable");
            mainTableRequestedColumns.add("mainTableRequestedColumns");
            secondaryTableRequestedColumns.add("secondaryTableRequestedColumns");
            mainTableConditions.put("field1", "value1");
            secondaryTableConditions.put("field2", "value2");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting");

            var result = defaultSQLStatementHandler.createJoinSelectQuery(mainTable, secondaryTable, mainKeys, secondaryKeys, mainTableRequestedColumns, secondaryTableRequestedColumns, mainTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending);
            var expected = "SELECT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM mainTable,secondaryTable WHERE  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting DESC";

            assertEquals(expected, result.getSQLStatement().trim());
        }


    }

    @Nested
    class Qualify {

        @Test
        void when_receive_col_expect_col_with_ExpressionKey_expect_col_with_ExpressionKey() {
            String col = "EXPRESSION_KEY_UNIQUE_IDENTIFIER";
            String table = "";

            var result = defaultSQLStatementHandler.qualify(col, table);
            var expected = col + table;

            assertEquals(expected.trim(), result);
        }

        @Test
        void when_receive_col_expect_col_with_ExpressionKey_expect_col_with_ExpressionKey_true() {
            String col = "EXPRESSION_KEY_UNIQUE_IDENTIFIER";

            var result = col.equals(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY);

            assertTrue(result);
        }

        @Test
        void when_receive_col_expect_col_with_FilterKey_expect_col_with_FilterKey_true() {
            String col = "FILTER_KEY_UNIQUE_IDENTIFIER";

            var result = col.equals(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.FILTER_KEY);

            assertTrue(result);
        }

        @Test
        void when_receive_col_and_table_expect_col_expect_thirth_codepath_of_qualify() {
            String col = "table.[table].[table].";
            String table = "";

            var result = defaultSQLStatementHandler.qualify(col, table);
            var expected = col;

            assertEquals(expected, result);
        }


        @Test
        void when_receive_col_and_table_expect_col_expect_fourth_codepath_of_qualify() {
            String col = "[table].[col]";
            String table = "";

            var result = defaultSQLStatementHandler.qualify(col, table);
            var expected = col;

            assertEquals(expected, result);
        }


        @Test
        void when_receive_col_and_table_expect_col_expect_fifth_codepath_of_qualify() {
            String col = "table.[col]";
            String table = "";

            var result = defaultSQLStatementHandler.qualify(col, table);
            var expected = col;

            assertEquals(expected, result);
        }

        @Test
        void when_receive_col_and_table_expect_col_expect_sixth_codepath_of_qualify() {
            String col = ".[table ]";
            String table = "";

            var result = defaultSQLStatementHandler.qualify(col, table);
            var expected = col;

            assertEquals(expected, result);
        }


        @Test
        void when_receive_col_and_table_expect_col_expect_other_codepath_qualify() {
            String col = "col";
            String table = "table";

            var result = defaultSQLStatementHandler.qualify(col, table);
            var expected = "table.col";

            assertEquals(expected, result.trim());

        }

    }

    @Nested
    class CreateJoinFromSubselectsQuery {

        @Test
        void when_receive_primaryAlias_and_secondaryAlias_and_primaryQuery_and_secondaryQuery_and_primaryKeys_and_secondaryKeys_and_primaryTableRequestedColumns_and_secondaryTableRequestedColumns_and_primaryTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_is_false_and_descending_is_false_expect_JoinFromSubSelect_query() {

            String primaryAlias = "primaryAlias";
            String secondaryAlias = "secondaryAlias";
            String primaryQuery = "primaryQuery";
            String secondaryQuery = "secondaryQuery";
            ArrayList primaryKeys = new ArrayList();
            ArrayList secondaryKeys = new ArrayList();
            ArrayList primaryTableRequestedColumns = new ArrayList();
            ArrayList secondaryTableRequestedColumns = new ArrayList();
            HashMap primaryTableConditions = new HashMap();
            HashMap secondaryTableConditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            boolean forceDistinct = false;
            boolean descending = false;


            primaryKeys.add("primaryKeys");
            secondaryKeys.add("secondaryKeys");
            primaryTableRequestedColumns.add("primaryTableRequestedColumns");
            secondaryTableRequestedColumns.add("secondaryTableRequestedColumns");
            primaryTableConditions.put("field1", "value1");
            secondaryTableConditions.put("field2", "value2");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting");

            var result = defaultSQLStatementHandler.createJoinFromSubselectsQuery(primaryAlias, secondaryAlias, primaryQuery, secondaryQuery, primaryKeys, secondaryKeys, primaryTableRequestedColumns, secondaryTableRequestedColumns, primaryTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending);
            var expected = "SELECT primaryAlias.primaryTableRequestedColumns , secondaryAlias.secondaryTableRequestedColumns FROM (primaryQuery) primaryAlias INNER JOIN (secondaryQuery) secondaryAlias ON  primaryAlias.primaryKeys=secondaryAlias.secondaryKeys AND  secondaryQuery.field2 = ?  AND primaryAlias.field1 = ?  ORDER BY columnSorting";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_primaryAlias_and_secondaryAlias_and_primaryQuery_and_secondaryQuery_and_primaryKeys_and_secondaryKeys_and_primaryTableRequestedColumns_and_secondaryTableRequestedColumns_and_primaryTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_is_true_and_descending_is_true_expect_JoinFromSubSelect_query() {

            String primaryAlias = "primaryAlias";
            String secondaryAlias = "secondaryAlias";
            String primaryQuery = "primaryQuery";
            String secondaryQuery = "secondaryQuery";
            ArrayList primaryKeys = new ArrayList();
            ArrayList secondaryKeys = new ArrayList();
            ArrayList primaryTableRequestedColumns = new ArrayList();
            ArrayList secondaryTableRequestedColumns = new ArrayList();
            HashMap primaryTableConditions = new HashMap();
            HashMap secondaryTableConditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            boolean forceDistinct = true;
            boolean descending = true;


            primaryKeys.add("primaryKeys");
            secondaryKeys.add("secondaryKeys");
            primaryTableRequestedColumns.add("primaryTableRequestedColumns");
            secondaryTableRequestedColumns.add("secondaryTableRequestedColumns");
            primaryTableConditions.put("field1", "value1");
            secondaryTableConditions.put("field2", "value2");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting");

            var result = defaultSQLStatementHandler.createJoinFromSubselectsQuery(primaryAlias, secondaryAlias, primaryQuery, secondaryQuery, primaryKeys, secondaryKeys, primaryTableRequestedColumns, secondaryTableRequestedColumns, primaryTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending);
            var expected = "SELECT  DISTINCT primaryAlias.primaryTableRequestedColumns , secondaryAlias.secondaryTableRequestedColumns FROM (primaryQuery) primaryAlias INNER JOIN (secondaryQuery) secondaryAlias ON  primaryAlias.primaryKeys=secondaryAlias.secondaryKeys AND  secondaryQuery.field2 = ?  AND primaryAlias.field1 = ?  ORDER BY columnSorting DESC";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_primaryAlias_and_secondaryAlias_and_primaryQuery_and_secondaryQuery_and_primaryKeys_and_secondaryKeys_and_primaryTableRequestedColumns_and_secondaryTableRequestedColumns_and_primaryTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_is_true_and_descending_is_false_expect_JoinFromSubSelect_query() {

            String primaryAlias = "primaryAlias";
            String secondaryAlias = "secondaryAlias";
            String primaryQuery = "primaryQuery";
            String secondaryQuery = "secondaryQuery";
            ArrayList primaryKeys = new ArrayList();
            ArrayList secondaryKeys = new ArrayList();
            ArrayList primaryTableRequestedColumns = new ArrayList();
            ArrayList secondaryTableRequestedColumns = new ArrayList();
            HashMap primaryTableConditions = new HashMap();
            HashMap secondaryTableConditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            boolean forceDistinct = true;
            boolean descending = false;


            primaryKeys.add("primaryKeys");
            secondaryKeys.add("secondaryKeys");
            primaryTableRequestedColumns.add("primaryTableRequestedColumns");
            secondaryTableRequestedColumns.add("secondaryTableRequestedColumns");
            primaryTableConditions.put("field1", "value1");
            secondaryTableConditions.put("field2", "value2");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting");

            var result = defaultSQLStatementHandler.createJoinFromSubselectsQuery(primaryAlias, secondaryAlias, primaryQuery, secondaryQuery, primaryKeys, secondaryKeys, primaryTableRequestedColumns, secondaryTableRequestedColumns, primaryTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending);
            var expected = "SELECT  DISTINCT primaryAlias.primaryTableRequestedColumns , secondaryAlias.secondaryTableRequestedColumns FROM (primaryQuery) primaryAlias INNER JOIN (secondaryQuery) secondaryAlias ON  primaryAlias.primaryKeys=secondaryAlias.secondaryKeys AND  secondaryQuery.field2 = ?  AND primaryAlias.field1 = ?  ORDER BY columnSorting";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_primaryAlias_and_secondaryAlias_and_primaryQuery_and_secondaryQuery_and_primaryKeys_and_secondaryKeys_and_primaryTableRequestedColumns_and_secondaryTableRequestedColumns_and_primaryTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_is_false_and_descending_is_true_expect_JoinFromSubSelect_query() {

            String primaryAlias = "primaryAlias";
            String secondaryAlias = "secondaryAlias";
            String primaryQuery = "primaryQuery";
            String secondaryQuery = "secondaryQuery";
            ArrayList primaryKeys = new ArrayList();
            ArrayList secondaryKeys = new ArrayList();
            ArrayList primaryTableRequestedColumns = new ArrayList();
            ArrayList secondaryTableRequestedColumns = new ArrayList();
            HashMap primaryTableConditions = new HashMap();
            HashMap secondaryTableConditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();
            boolean forceDistinct = false;
            boolean descending = true;


            primaryKeys.add("primaryKeys");
            secondaryKeys.add("secondaryKeys");
            primaryTableRequestedColumns.add("primaryTableRequestedColumns");
            secondaryTableRequestedColumns.add("secondaryTableRequestedColumns");
            primaryTableConditions.put("field1", "value1");
            secondaryTableConditions.put("field2", "value2");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting");

            var result = defaultSQLStatementHandler.createJoinFromSubselectsQuery(primaryAlias, secondaryAlias, primaryQuery, secondaryQuery, primaryKeys, secondaryKeys, primaryTableRequestedColumns, secondaryTableRequestedColumns, primaryTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending);
            var expected = "SELECT primaryAlias.primaryTableRequestedColumns , secondaryAlias.secondaryTableRequestedColumns FROM (primaryQuery) primaryAlias INNER JOIN (secondaryQuery) secondaryAlias ON  primaryAlias.primaryKeys=secondaryAlias.secondaryKeys AND  secondaryQuery.field2 = ?  AND primaryAlias.field1 = ?  ORDER BY columnSorting DESC";

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

            var result = defaultSQLStatementHandler.createLeftJoinSelectQueryPageable(mainTable, subquery, secondaryTable, mainKeys, secondaryKeys, mainTableRequestedColumns, secondaryTableRequestedColumns, mainTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending, recordNumber, startIndex);
            var expected = "SELECT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_mainTable_and_subquery_and_secondaryTable_and_mainKeys_and_secondaryKeys_and_mainTableRequestedColumns_and_secondaryTableRequestedColumns_and_mainTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_is_false_and_descending_is_true_and_recordNumber_and_startIndex_expect_LeftJoinSelect_query_pageable() {

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
            boolean descending = true;
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

            var result = defaultSQLStatementHandler.createLeftJoinSelectQueryPageable(mainTable, subquery, secondaryTable, mainKeys, secondaryKeys, mainTableRequestedColumns, secondaryTableRequestedColumns, mainTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending, recordNumber, startIndex);
            var expected = "SELECT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting DESC";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_mainTable_and_subquery_and_secondaryTable_and_mainKeys_and_secondaryKeys_and_mainTableRequestedColumns_and_secondaryTableRequestedColumns_and_mainTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_is_true_and_descending_is_true_and_recordNumber_and_startIndex_expect_LeftJoinSelect_query_pageable() {

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
            boolean forceDistinct = true;
            boolean descending = true;
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

            var result = defaultSQLStatementHandler.createLeftJoinSelectQueryPageable(mainTable, subquery, secondaryTable, mainKeys, secondaryKeys, mainTableRequestedColumns, secondaryTableRequestedColumns, mainTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending, recordNumber, startIndex);
            var expected = "SELECT  DISTINCT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting DESC";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_mainTable_and_subquery_and_secondaryTable_and_mainKeys_and_secondaryKeys_and_mainTableRequestedColumns_and_secondaryTableRequestedColumns_and_mainTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_is_true_and_descending_is_false_and_recordNumber_and_startIndex_expect_LeftJoinSelect_query_pageable() {

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
            boolean forceDistinct = true;
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

            var result = defaultSQLStatementHandler.createLeftJoinSelectQueryPageable(mainTable, subquery, secondaryTable, mainKeys, secondaryKeys, mainTableRequestedColumns, secondaryTableRequestedColumns, mainTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending, recordNumber, startIndex);
            var expected = "SELECT  DISTINCT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting";

            assertEquals(expected, result.getSQLStatement().trim());
        }


    }

    @Nested
    class CreateLeftJoinSelectQuery {

        @Test
        void when_receive_mainTable_and_subquery_and_secondaryTable_and_mainKeys_and_secondaryKeys_and_mainTableRequestedColumns_and_secondaryTableRequestedColumns_and_mainTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_is_false_and_descending_is_false_expect_LeftJoinSelect_query() {

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


            mainKeys.add("mainKeys");
            secondaryKeys.add("secondaryTable");
            mainTableRequestedColumns.add("mainTableRequestedColumns");
            secondaryTableRequestedColumns.add("secondaryTableRequestedColumns");
            mainTableConditions.put("field1", "value1");
            secondaryTableConditions.put("field2", "value2");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting");

            var result = defaultSQLStatementHandler.createLeftJoinSelectQuery(mainTable, subquery, secondaryTable, mainKeys, secondaryKeys, mainTableRequestedColumns, secondaryTableRequestedColumns, mainTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending);
            var expected = "SELECT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_mainTable_and_subquery_and_secondaryTable_and_mainKeys_and_secondaryKeys_and_mainTableRequestedColumns_and_secondaryTableRequestedColumns_and_mainTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_is_false_and_descending_is_true_expect_LeftJoinSelect_query() {

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
            boolean descending = true;


            mainKeys.add("mainKeys");
            secondaryKeys.add("secondaryTable");
            mainTableRequestedColumns.add("mainTableRequestedColumns");
            secondaryTableRequestedColumns.add("secondaryTableRequestedColumns");
            mainTableConditions.put("field1", "value1");
            secondaryTableConditions.put("field2", "value2");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting");

            var result = defaultSQLStatementHandler.createLeftJoinSelectQuery(mainTable, subquery, secondaryTable, mainKeys, secondaryKeys, mainTableRequestedColumns, secondaryTableRequestedColumns, mainTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending);
            var expected = "SELECT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting DESC";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_mainTable_and_subquery_and_secondaryTable_and_mainKeys_and_secondaryKeys_and_mainTableRequestedColumns_and_secondaryTableRequestedColumns_and_mainTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_is_true_and_descending_is_true_expect_LeftJoinSelect_query() {

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
            boolean forceDistinct = true;
            boolean descending = true;

            mainKeys.add("mainKeys");
            secondaryKeys.add("secondaryTable");
            mainTableRequestedColumns.add("mainTableRequestedColumns");
            secondaryTableRequestedColumns.add("secondaryTableRequestedColumns");
            mainTableConditions.put("field1", "value1");
            secondaryTableConditions.put("field2", "value2");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting");

            var result = defaultSQLStatementHandler.createLeftJoinSelectQuery(mainTable, subquery, secondaryTable, mainKeys, secondaryKeys, mainTableRequestedColumns, secondaryTableRequestedColumns, mainTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending);
            var expected = "SELECT  DISTINCT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting DESC";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_mainTable_and_subquery_and_secondaryTable_and_mainKeys_and_secondaryKeys_and_mainTableRequestedColumns_and_secondaryTableRequestedColumns_and_mainTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_is_true_and_descending_is_false_expect_LeftJoinSelect_query() {

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
            boolean forceDistinct = true;
            boolean descending = false;


            mainKeys.add("mainKeys");
            secondaryKeys.add("secondaryTable");
            mainTableRequestedColumns.add("mainTableRequestedColumns");
            secondaryTableRequestedColumns.add("secondaryTableRequestedColumns");
            mainTableConditions.put("field1", "value1");
            secondaryTableConditions.put("field2", "value2");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting");

            var result = defaultSQLStatementHandler.createLeftJoinSelectQuery(mainTable, subquery, secondaryTable, mainKeys, secondaryKeys, mainTableRequestedColumns, secondaryTableRequestedColumns, mainTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending);
            var expected = "SELECT  DISTINCT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting";

            assertEquals(expected, result.getSQLStatement().trim());

        }

    }


    @Nested
    class ResultSetToEntityResult {

        @Test
        void when_receive_resultSet_and_entityResult_and_recordNumber_and_offset_and_delimited_is_true_and_columnNames_expected_ResultSetToEntity_Result() throws Exception {

            entityResult = new EntityResultMapImpl();
            int recordNumber = 1;
            int offset = 0;
            boolean delimited = true;
            ArrayList columnNames = new ArrayList();

            columnNames.add("columnNames");

            ResultSetMetaData resultSetMetaDatamock = Mockito.mock(ResultSetMetaData.class);

            Mockito.doReturn(resultSetMetaDatamock).when(resultSet).getMetaData();
            Mockito.doReturn(1).when(resultSetMetaDatamock).getColumnCount();
            Mockito.doReturn("columnNames").when(resultSetMetaDatamock).getColumnLabel(1);
            Mockito.doReturn(1).when(resultSetMetaDatamock).getColumnType(1);
            Mockito.doReturn(true).doReturn(false).when(resultSet).next();

            defaultSQLStatementHandler.resultSetToEntityResult(resultSet, entityResult, recordNumber, offset, delimited, columnNames);

            assertEquals(1, entityResult.calculateRecordNumber());
        }
    }

    @Disabled
    @Nested
    class GeneratedKeysToEntityResult {

        @Test
        void when_receive_resultSet_and_entityResult_and_generatedKeys_expected_generate_keys_off_entityresult_result() throws Exception {
            entityResult = new EntityResultMapImpl();
            ArrayList generatedKeys = new ArrayList();

            generatedKeys.add("UNO");

            ResultSetMetaData resultSetMetaDatamock = Mockito.mock(ResultSetMetaData.class);

            Mockito.doReturn(resultSetMetaDatamock).when(resultSet).getMetaData();
            Mockito.doReturn("COLUMN_NAMES").when(resultSetMetaDatamock).getColumnLabel(1);
            Mockito.doReturn(1).when(resultSetMetaDatamock).getColumnType(1);
            Mockito.doReturn(1).when(resultSetMetaDatamock).getColumnCount();
            Mockito.doReturn(true).doReturn(false).when(resultSet).next();

            defaultSQLStatementHandler.generatedKeysToEntityResult(resultSet, entityResult, generatedKeys);

            assertEquals(1, entityResult.calculateRecordNumber());


        }

    }

    @Disabled
    @Nested
    class SetObject {
        /*public void setObject(int index, Object value, PreparedStatement preparedStatement, boolean truncDates)
                throws SQLException {
            if (value == null) {
                DefaultSQLStatementHandler.logger.debug(" setObject {} with NULL", index);
                preparedStatement.setObject(index, value);
              }*/
        @Mock
        Connection connection;

        @Test
        void when_receive_index_and_value_is_null_and_preparedStatement_and_truncDates_expect_set_object() throws SQLException {
            int index = 1;
            Object value = null;
            boolean truncDates = false;

            //Mockito.doReturn("select * from table where fiedl1=?").when(connection).createStatement();

            PreparedStatement preparedStatement = connection.prepareStatement("a");


            //Mockito.doReturn(1).when(preparedStatement).executeQuery().getObject(1);
            //Mockito.doReturn(null).when(preparedStatement).setObject(1,null);


            defaultSQLStatementHandler.setObject(index, value, preparedStatement, truncDates);

            assertEquals(null, preparedStatement.toString());

        }


    }

    @Nested
    class AddMultilanguageLeftJoinTables {
        @Test
        void when_receive_table_and_tables_and_keys_and_localeId_expect_count_query() throws SQLException {

            String table = "my-table";
            ArrayList tables = new ArrayList();
            LinkedHashMap keys = new LinkedHashMap();
            LocalePair localeId = new LocalePair("field2", "value2");

            tables.add("wildcard1");
            keys.put("field1", "value1");

            var result = defaultSQLStatementHandler.addMultilanguageLeftJoinTables(table, tables, keys, localeId);
            var expected = "my-table LEFT JOIN wildcard1 ON wildcard1.field1 = my-table.value1 AND wildcard1.field2 = ?";

            assertEquals(expected, result.trim());
        }
    }

    @Nested
    class AddInnerMultilanguageColumns {

        @Test
        void when_receive_subSqlQuery_and_attributes_and_hLocaleTablesAV_expect_inner_multilanguage_columns() {
            var subSqlQuery = " from";
            ArrayList attributes = new ArrayList();
            HashMap hLocaleTablesAV = new HashMap();

            attributes.add("attr1");
            hLocaleTablesAV.put("field1", "value1");

            var result = defaultSQLStatementHandler.addInnerMultilanguageColumns(subSqlQuery, attributes, hLocaleTablesAV);
            var expected = ", field1 AS value1 from";

            assertEquals(expected, result.trim());

        }
    }

    @Nested
    class AddOuterMultilanguageColumns {

        @Test
        void when_receive_sqlQuery_and_table_and_hLocaleTablesAV_expect_add_outer_multilanguage_columns() {
            var sqlQuery = " from";
            String table = "my table";
            HashMap hLocaleTablesAV = new HashMap();

            hLocaleTablesAV.put("field1", "value1");

            var result = defaultSQLStatementHandler.addOuterMultilanguageColumns(sqlQuery, table, hLocaleTablesAV);
            var expected = ", my table.value1 from";

            assertEquals(expected, result.trim());


        }


    }


}


package com.ontimize.jee.common.test.dbTest;

import com.ontimize.jee.common.db.handler.DefaultSQLStatementHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DefaultSQLStatementHandlerTest {


    boolean useAsInSubqueries = true;

    @InjectMocks
    DefaultSQLStatementHandler defaultSQLStatementHandler;


    @Test
    void createCountQuery_when_receive_one_table_expect_count_query() {
        var table = "my-table";
        HashMap conditions = null;
        ArrayList wildcards = null;
        ArrayList countColumns = null;

        var result = this.defaultSQLStatementHandler.createCountQuery(table, null, null, null);
        var expected = "SELECT  COUNT(*) AS \"TotalRecordNumber\"  FROM  [my-table]";

        assertEquals(expected, result.getSQLStatement().trim());

    }


    @Test
    void createCountQuery_when_receive_one_table_and_conditions_expect_count_query() {
        var table = "my-table";
        HashMap conditions = null;
        ArrayList wildcards = null;
        ArrayList countColumns = null;

        conditions = new HashMap();
        conditions.put("field1", "value1");

        var result = this.defaultSQLStatementHandler.createCountQuery(table, conditions, null, null);
        var expected = "SELECT  COUNT(*) AS \"TotalRecordNumber\"  FROM  [my-table]   WHERE field1 = ?";

        assertEquals(expected, result.getSQLStatement().trim());

    }


    @Test
    void createCountQuery_when_receive_table_and_conditions_and_wildcards_expect_count_query() {
        var table = "my-table";
        HashMap conditions = new HashMap();
        ArrayList wildcards = new ArrayList();
        ArrayList countColumns = null;


        conditions.put("field1", "value1");
        wildcards.add("wildcard1");

        var result = this.defaultSQLStatementHandler.createCountQuery(table, conditions, wildcards, null);
        var expected = "SELECT  COUNT(*) AS \"TotalRecordNumber\"  FROM  [my-table]   WHERE field1 = ?";

        assertEquals(expected, result.getSQLStatement().trim());
    }

    @Test
    void createCountQuery_when_receive_table_and_conditions_and_wildcards_and_countColumns_expect_count_query() {
        var table = "my-table";
        HashMap conditions = new HashMap();
        ArrayList wildcards = new ArrayList();
        ArrayList countColumns = new ArrayList();


        conditions.put("field1", "value1");
        wildcards.add("wildcard1");
        countColumns.add("countColumns1");

        var result = this.defaultSQLStatementHandler.createCountQuery(table, conditions, wildcards, countColumns);
        var expected = "SELECT  COUNT( countColumns1 )  AS \"TotalRecordNumber\"  FROM  [my-table]   WHERE field1 = ?";

        assertEquals(expected, result.getSQLStatement().trim());
    }


    @Test
    void createSelectQuery_when_receive_one_table_expect_select_query() {
        var table = "my-table";
        HashMap conditions = null;
        ArrayList wildcards = null;
        ArrayList requestedColumns = null;

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, null, null, null);
        var expected = "SELECT  *  FROM  [my-table]";

        assertEquals(expected, result.getSQLStatement().trim());
    }

    @Test
    void createSelectQuery_when_receive_table_and_requestedColumns_expect_select_query() {
        var table = "my-table";
        ArrayList requestedColumns = new ArrayList();
        HashMap conditions = null;
        ArrayList wildcards = null;

        requestedColumns.add("requestedColumns1");

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, null, null);
        var expected = "SELECT requestedColumns1 FROM  [my-table]";

        assertEquals(expected, result.getSQLStatement().trim());
    }

    @Test
    void createSelectQuery_when_receive_table_and_requestedColumns_and_conditions_expect_select_query() {
        var table = "my-table";
        ArrayList requestedColumns = new ArrayList();
        HashMap conditions = new HashMap();
        ArrayList wildcards = null;

        requestedColumns.add("requestedColumns1");
        conditions.put("field1", "value1");

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, null);
        var expected = "SELECT requestedColumns1 FROM  [my-table]   WHERE field1 = ?";

        assertEquals(expected, result.getSQLStatement().trim());
    }

    @Test
    void createSelectQuery_when_receive_table_and_requestedColumns_and_conditions_and_wildcards_expect_select_query() {
        var table = "my-table";
        ArrayList requestedColumns = new ArrayList();
        HashMap conditions = new HashMap();
        ArrayList wildcards = new ArrayList();

        requestedColumns.add("requestedColumns1");
        conditions.put("field1", "value1");
        wildcards.add("wildcards1");

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards);
        var expected = "SELECT requestedColumns1 FROM  [my-table]   WHERE field1 = ?";

        assertEquals(expected, result.getSQLStatement().trim());
    }


    @Test
    void createSelectQuery_when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_expect_select_query() {
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

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting);
        var expected = "SELECT requestedColumns1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

        assertEquals(expected, result.getSQLStatement().trim());
    }

    @Test
    void createSelectQuery_when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_expect_select_query() {
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

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount);
        var expected = "SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

        assertEquals(expected, result.getSQLStatement().trim());
    }

    @Test
    void createSelectQuery_when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_descending_is_true_expect_select_query() {
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

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, descending);
        var expected = "SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC";

        assertEquals(expected, result.getSQLStatement().trim());
    }

    @Test
    void createSelectQuery_when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_descending_is_false_expect_select_query() {
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

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, descending);
        var expected = "SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

        assertEquals(expected, result.getSQLStatement().trim());
    }


    @Test
    void createSelectQuery_when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_descending_is_true_and_forceDistinct_is_false_expect_select_query() {
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

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, descending, forceDistinct);
        var expected = "SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC";

        assertEquals(expected, result.getSQLStatement().trim());
    }

    @Test
    void createSelectQuery_when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_descending_is_true_and_forceDistinct_is_true_expect_select_query() {
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

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, descending, forceDistinct);
        var expected = "SELECT  DISTINCT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC";

        assertEquals(expected, result.getSQLStatement().trim());
    }

    @Test
    void createSelectQuery_when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_descending_is_false_and_forceDistinct_is_false_expect_select_query() {
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

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, descending, forceDistinct);
        var expected = "SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

        assertEquals(expected, result.getSQLStatement().trim());
    }

    @Test
    void createSelectQuery_when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_descending_is_false_and_forceDistinct_is_true_expect_select_query() {
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

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, descending, forceDistinct);
        var expected="SELECT  DISTINCT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

        assertEquals(expected, result.getSQLStatement().trim());
    }

    @Test
    void createSelectQuery_when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_offset_expect_select_query() {
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

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, offset);
        var expected = "SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

        assertEquals(expected, result.getSQLStatement().trim());
    }

    @Test
    void createSelectQuery_when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_offset_and_descending_is_false_expect_select_query() {
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

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, offset, descending);
        var expected="SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

        assertEquals(expected, result.getSQLStatement().trim());
    }


    @Test
    void createSelectQuery_when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_offset_and_descending_is_true_expect_select_query() {
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

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, offset, descending);
        var expected="SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC";

        assertEquals(expected, result.getSQLStatement().trim());
    }


    @Test
    void createSelectQuery_when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_sum_offset_and_descending_is_false_and_forceDistinct_is_false_expect_select_query() {
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

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount + offset, descending, forceDistinct);
        var expected="SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

        assertEquals(expected, result.getSQLStatement().trim());
    }

    @Test
    void createSelectQuery_when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_sum_offset_and_descending_is_false_and_forceDistinct_is_true_expect_select_query() {
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

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount + offset, descending, forceDistinct);
        var expected="SELECT  DISTINCT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

        assertEquals(expected, result.getSQLStatement().trim());
    }

    @Test
    void createSelectQuery_when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_sum_offset_and_descending_is_true_and_forceDistinct_is_true_expect_select_query() {
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

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount + offset, descending, forceDistinct);
        var expected= "SELECT  DISTINCT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC";

        assertEquals(expected, result.getSQLStatement().trim());
    }

    @Test
    void createSelectQuery_when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_sum_offset_and_descending_is_true_and_forceDistinct_is_false_expect_select_query() {
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

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount + offset, descending, forceDistinct);
        var expected= "SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC";

        assertEquals(expected, result.getSQLStatement().trim());
    }

    @Test
    void createSelectQuery_when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_descending_is_true_expect_select_query() {
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

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, descending);
        var expected="SELECT requestedColumns1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC";


        assertEquals(expected, result.getSQLStatement().trim());
    }

    @Test
    void createSelectQuery_when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_descending_is_false_expect_select_query() {
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

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, descending);
        var expected="SELECT requestedColumns1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

        assertEquals(expected, result.getSQLStatement().trim());
    }

    @Test
    void createSelectQuery_when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_descending_is_true_and_forceDistinct_is_false_expect_select_query() {
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

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, descending, forceDistinct);
        var expected="SELECT requestedColumns1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC";

        assertEquals(expected, result.getSQLStatement().trim());
    }

    @Test
    void createSelectQuery_when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_descending_is_true_and_forceDistinct_is_true_expect_select_query() {
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

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, descending, forceDistinct);
        var expected="SELECT  DISTINCT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC";

        assertEquals(expected, result.getSQLStatement().trim());
    }

    @Test
    void createSelectQuery_when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_descending_is_false_and_forceDistinct_is_false_expect_select_query() {
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

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, descending, forceDistinct);
        var expected= "SELECT requestedColumns1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

        assertEquals(expected, result.getSQLStatement().trim());
    }

    @Test
    void createSelectQuery_when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_descending_is_false_and_forceDistinct_is_true_expect_select_query() {
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

        var result = this.defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, descending, forceDistinct);
        var expected="SELECT  DISTINCT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1";

        assertEquals(expected, result.getSQLStatement().trim());
    }

    @Test
    void createSortStatement_when_receive_sortColumns_expect_SortStatement() {
        ArrayList sortColumns = new ArrayList();

        sortColumns.add("sortColumns");

        var result = this.defaultSQLStatementHandler.createSortStatement(sortColumns);
        var expected="ORDER BY sortColumns";

        assertEquals(expected, result.trim());
    }


    @Test
    void createSortStatement_when_receive_sortColumns_and_descending_is_true_expect_SortStatement() {
        ArrayList sortColumns = new ArrayList();
        boolean descending = true;

        sortColumns.add("sortColumns");

        var result = this.defaultSQLStatementHandler.createSortStatement(sortColumns, descending);
        var expected= "ORDER BY sortColumns DESC";

        assertEquals(expected, result.trim());
    }

    @Test
    void createSortStatement_when_receive_sortColumns_and_descending_is_false_expect_SortStatement() {
        ArrayList sortColumns = new ArrayList();
        boolean descending = false;

        sortColumns.add("sortColumns");

        var result = this.defaultSQLStatementHandler.createSortStatement(sortColumns, descending);
        var expected= "ORDER BY sortColumns";

        assertEquals(expected, result.trim());
    }


    @Test
    void createQueryConditionsWithoutWhere_when_receive_conditions_expect_query_without_where() {
        HashMap conditions = new HashMap();
        ArrayList wildcard = new ArrayList();
        ArrayList values = new ArrayList();

        conditions.put("field1", "value1");

        var result = this.defaultSQLStatementHandler.createQueryConditionsWithoutWhere(conditions, wildcard, values);
        var expected= "field1 = ?";

        assertEquals(expected, result.trim());
    }

    @Test
    void createQueryConditionsWithoutWhere_when_receive_conditions_and_wildcard_expect_query_without_where() {
        HashMap conditions = new HashMap();
        ArrayList wildcard = new ArrayList();
        ArrayList values = new ArrayList();

        conditions.put("field1", "value1");
        conditions.put("field2", "value2");
        wildcard.add("wildcard1");

        var result = this.defaultSQLStatementHandler.createQueryConditionsWithoutWhere(conditions, wildcard, values);
        var expected= "field1 = ?  AND field2 = ?";

        assertEquals(expected, result.trim());
    }

    @Test
    void createQueryConditionsWithoutWhere_when_receive_conditions_and_wildcard_and_values_expect_query_without_where() {
        HashMap conditions = new HashMap();
        ArrayList wildcard = new ArrayList();
        ArrayList values = new ArrayList();

        conditions.put("field1", "value1");
        wildcard.add("wildcard1");
        values.add("values1");

        var result = this.defaultSQLStatementHandler.createQueryConditionsWithoutWhere(conditions, wildcard, values);
        var expected= "field1 = ?";

        assertEquals(expected, result.trim());
    }

    @Test
    void createQueryConditionsWithoutWhere_when_receive_conditions_but_not_wildcard_and_values_expect_Exception_NullPointerException() {
        HashMap conditions = new HashMap();

        conditions.put("field1", "value1");

        assertThrows(NullPointerException.class, () -> this.defaultSQLStatementHandler.createQueryConditionsWithoutWhere(conditions, null, null));
    }

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

    @Test
    void createInsertQuery_when_receive_table_expect_insert_query() {
        String table = "table1";
        HashMap attributes = new HashMap();

        attributes.put("field1", "value1");

        var result = this.defaultSQLStatementHandler.createInsertQuery(table, attributes);
        var expected="INSERT INTO table1 ( field1 )  VALUES ( ?  )";

        assertEquals(expected, result.getSQLStatement().trim());
    }

    @Test
    void createInsertQuery_when_receive_table_and_attibutes_expect_insert_query() {
        String table = "table1";
        HashMap attributes = new HashMap();

        attributes.put("field1", "value1");

        var result = this.defaultSQLStatementHandler.createInsertQuery(table, attributes);
        var expected= "INSERT INTO table1 ( field1 )  VALUES ( ?  )";

        assertEquals(expected, result.getSQLStatement().trim());
    }

    @Test
    void createUpdateQuery_when_receive_table_and_attributes_expect_update_query() {
        String table = "table1";
        HashMap attributes = new HashMap();
        //HashMap keysValues = null;

        attributes.put("field1", "value1");

        var result = this.defaultSQLStatementHandler.createUpdateQuery(table, attributes, null);
        var expected= "UPDATE table1 SET field1 = ?";

        assertEquals(expected, result.getSQLStatement().trim());

    }


}
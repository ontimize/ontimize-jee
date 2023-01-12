package com.ontimize.jee.common.test.dbTest;

import com.ontimize.jee.common.db.LocalePair;
import com.ontimize.jee.common.db.handler.DefaultSQLStatementHandler;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.gui.LongString;
import com.ontimize.jee.common.util.remote.BytesBlock;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DefaultSQLStatementHandlerTest {

    @InjectMocks
    DefaultSQLStatementHandler defaultSQLStatementHandler;

    @Mock
    ResultSet resultSet;


    EntityResult entityResult;

    @Test
    void checkColumnName_when_receive_columnName_expect_check_columnName() {
        boolean result = this.defaultSQLStatementHandler.checkColumnName("COLUMN1");
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
    void convertPaginationStatement_when_receive_sqlTemplate_and_startIndex_and_recordNumber_expect_convert_pagination_statement() {
        String sqlTemplate = "sqlTemplate";
        int startIndex = 1;
        int recordNumber = 1;

        String result = defaultSQLStatementHandler.convertPaginationStatement(sqlTemplate, startIndex, recordNumber);
        String expected = "sqlTemplate";

        assertEquals(expected, result);
    }

    @Nested
    class CreateUpdateQuery {

        @Test
        void when_receive_table_and_attributes_expect_update_query() {
            String table = "table1";
            Map<String, String> attributes = new HashMap<>();

            attributes.put("field1", "value1");

            var result = defaultSQLStatementHandler.createUpdateQuery(table, attributes, null);
            var expected = "UPDATE table1 SET field1 = ?";

            assertEquals(expected, result.getSQLStatement().trim());

        }

        @Test
        void when_receive_table_and_attributes_and_keysValues_expect_update_query() {
            String table = "table1";
            Map<String, String> attributes = new HashMap<>();

            attributes.put("field1", "value1");

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
            Map<String, String> keysValues = new HashMap<>();

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
            Map<String, String> attributes = new HashMap<>();

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
            Map<String, String> conditions = new HashMap<>();
            List<String> wildcard = new ArrayList<>();
            List<String> values = new ArrayList<>();

            conditions.put("field1", "value1");

            var result = defaultSQLStatementHandler.createQueryConditionsWithoutWhere(conditions, wildcard, values);
            var expected = "field1 = ?";

            assertEquals(expected, result.trim());
        }

        @Test
        void when_receive_conditions_and_wildcard_expect_query_without_where() {
            Map<String, String> conditions = new HashMap<>();
            List<String> wildcard = new ArrayList<>();
            List<String> values = new ArrayList<>();

            conditions.put("field1", "value1");
            conditions.put("field2", "value2");
            wildcard.add("wildcard1");

            var result = defaultSQLStatementHandler.createQueryConditionsWithoutWhere(conditions, wildcard, values);
            var expected = "field1 = ?  AND field2 = ?";

            assertEquals(expected, result.trim());
        }

        @Test
        void when_receive_conditions_and_wildcard_and_values_expect_query_without_where() {
            Map<String, String> conditions = new HashMap<>();
            List<String> wildcard = new ArrayList<>();
            List<String> values = new ArrayList<>();

            conditions.put("field1", "value1");
            wildcard.add("wildcard1");
            values.add("values1");

            var result = defaultSQLStatementHandler.createQueryConditionsWithoutWhere(conditions, wildcard, values);
            var expected = "field1 = ?";

            assertEquals(expected, result.trim());
        }

        @Test
        void when_receive_conditions_but_not_wildcard_and_values_expect_Exception_NullPointerException() {
            Map<String, String> conditions = new HashMap<>();

            conditions.put("field1", "value1");

            assertThrows(NullPointerException.class, () -> defaultSQLStatementHandler.createQueryConditionsWithoutWhere(conditions, null, null));
        }
    }

    @Nested
    class CreateCountQuery {
        @Test
        void when_receive_one_table_expect_count_query() {
            var table = "my-table";
            var result = defaultSQLStatementHandler.createCountQuery(table, null, null, null);
            var expected = "SELECT  COUNT(*) AS \"TotalRecordNumber\"  FROM  [my-table]";

            assertEquals(expected, result.getSQLStatement().trim());

        }


        @Test
        void when_receive_one_table_and_conditions_expect_count_query() {
            var table = "my-table";
            Map<String, String> conditions = new HashMap<>();
            conditions.put("field1", "value1");

            var result = defaultSQLStatementHandler.createCountQuery(table, conditions, null, null);
            var expected = "SELECT  COUNT(*) AS \"TotalRecordNumber\"  FROM  [my-table]   WHERE field1 = ?";

            assertEquals(expected, result.getSQLStatement().trim());

        }


        @Test
        void when_receive_table_and_conditions_and_wildcards_expect_count_query() {
            var table = "my-table";
            Map<String, String> conditions = new HashMap<>();
            List<String> wildcards = new ArrayList<>();


            conditions.put("field1", "value1");
            wildcards.add("wildcard1");

            var result = defaultSQLStatementHandler.createCountQuery(table, conditions, wildcards, null);
            var expected = "SELECT  COUNT(*) AS \"TotalRecordNumber\"  FROM  [my-table]   WHERE field1 = ?";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_conditions_and_wildcards_and_countColumns_expect_count_query() {
            var table = "my-table";
            Map<String, String> conditions = new HashMap<>();
            List<String> wildcards = new ArrayList<>();
            List<String> countColumns = new ArrayList<>();


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
            List<String> countColumns = new ArrayList<>();

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

            var result = defaultSQLStatementHandler.createSelectQuery(table, null, null, null);
            var expected = "SELECT  *  FROM  [my-table]";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_expect_select_query() {
            var table = "my-table";
            List<String> requestedColumns = new ArrayList<>();
            requestedColumns.add("requestedColumns1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, null, null);
            var expected = "SELECT requestedColumns1 FROM  [my-table]";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_and_conditions_expect_select_query() {
            var table = "my-table";
            List<String> requestedColumns = new ArrayList<>();
            Map<String, String> conditions = new HashMap<>();

            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, null);
            var expected = "SELECT requestedColumns1 FROM  [my-table]   WHERE field1 = ?";

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @Test
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_expect_select_query() {
            var table = "my-table";
            List<String> requestedColumns = new ArrayList<>();
            Map<String, String> conditions = new HashMap<>();
            List<String> wildcards = new ArrayList<>();

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
            List<String> requestedColumns = new ArrayList<>();
            Map<String, String> conditions = new HashMap<>();
            List<String> wildcards = new ArrayList<>();
            List<String> columnSorting = new ArrayList<>();

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
            List<String> requestedColumns = new ArrayList<>();
            Map<String, String> conditions = new HashMap<>();
            List<String> wildcards = new ArrayList<>();
            List<String> columnSorting = new ArrayList<>();
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
            List<String> requestedColumns = new ArrayList<>();
            Map<String, String> conditions = new HashMap<>();
            List<String> wildcards = new ArrayList<>();
            List<String> columnSorting = new ArrayList<>();
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
            List<String> requestedColumns = new ArrayList<>();
            Map<String, String>conditions = new HashMap<>();
            List<String> wildcards = new ArrayList<>();
            List<String> columnSorting = new ArrayList<>();
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
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_offset_expect_select_query() {
            var table = "my-table";
            List<String> requestedColumns = new ArrayList<>();
            Map<String, String>conditions = new HashMap<>();
            List<String> wildcards = new ArrayList<>();
            List<String> columnSorting = new ArrayList<>();
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
            List<String> requestedColumns = new ArrayList<>();
            Map<String, String>conditions = new HashMap<>();
            List<String> wildcards = new ArrayList<>();
            List<String> columnSorting = new ArrayList<>();
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
            List<String> requestedColumns = new ArrayList<>();
            Map<String, String>conditions = new HashMap<>();
            List<String> wildcards = new ArrayList<>();
            List<String> columnSorting = new ArrayList<>();
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


    }


    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class CreateSelectQueryWithParameterizedTest {

        @ParameterizedTest
        @MethodSource("addDataCreateSelectQuery")
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_and_descending_and_forceDistinct_expect_select_query(boolean descending, boolean forceDistinct, String expected) {
            var table = "my-table";
            List<String> requestedColumns = new ArrayList<>();
            Map<String, String>conditions = new HashMap<>();
            List<String> wildcards = new ArrayList<>();
            List<String> columnSorting = new ArrayList<>();
            int recordCount = 10;


            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount, descending, forceDistinct);

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @ParameterizedTest
        @MethodSource("addDataCreateSelectQuery")
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_recordCount_sum_offset_and_descending_and_forceDistinct_expect_select_query(boolean descending, boolean forceDistinct, String expected) {
            var table = "my-table";
            List<String> requestedColumns = new ArrayList<>();
            Map<String, String>conditions = new HashMap<>();
            List<String> wildcards = new ArrayList<>();
            List<String> columnSorting = new ArrayList<>();
            int recordCount = 10;
            int offset = 1;


            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, recordCount + offset, descending, forceDistinct);

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @ParameterizedTest
        @MethodSource("addDataCreateSelectQueryWithDescending")
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_descending_expect_select_query(boolean descending, String expected) {
            var table = "my-table";
            List<String> requestedColumns = new ArrayList<>();
            Map<String, String>conditions = new HashMap<>();
            List<String> wildcards = new ArrayList<>();
            List<String> columnSorting = new ArrayList<>();


            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, descending);

            assertEquals(expected, result.getSQLStatement().trim());
        }

        @ParameterizedTest
        @MethodSource("addDataCreateSelectQueryWithoutRecordCount")
        void when_receive_table_and_requestedColumns_and_conditions_and_wildcards_and_columnsSorting_and_descending_and_forceDistinct_expect_select_query(boolean descending, boolean forceDistinct, String expected) {
            var table = "my-table";
            List<String> requestedColumns = new ArrayList<>();
            Map<String, String>conditions = new HashMap<>();
            List<String> wildcards = new ArrayList<>();
            List<String> columnSorting = new ArrayList<>();


            requestedColumns.add("requestedColumns1");
            conditions.put("field1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting1");

            var result = defaultSQLStatementHandler.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, descending, forceDistinct);

            assertEquals(expected, result.getSQLStatement().trim());
        }


        Stream<Arguments> addDataCreateSelectQuery() {
            return Stream.of(
                    Arguments.of(true, false, "SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC"),
                    Arguments.of(true, true, "SELECT  DISTINCT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC"),
                    Arguments.of(false, false, "SELECT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1"),
                    Arguments.of(false, true, "SELECT  DISTINCT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1")
            );
        }

        Stream<Arguments> addDataCreateSelectQueryWithDescending() {
            return Stream.of(
                    Arguments.of(true, "SELECT requestedColumns1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC"),
                    Arguments.of(false, "SELECT requestedColumns1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1")
            );
        }


        Stream<Arguments> addDataCreateSelectQueryWithoutRecordCount() {
            return Stream.of(
                    Arguments.of(true, false, "SELECT requestedColumns1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC"),
                    Arguments.of(true, true, "SELECT  DISTINCT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1 DESC"),
                    Arguments.of(false, false, "SELECT requestedColumns1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1"),
                    Arguments.of(false, true, "SELECT  DISTINCT requestedColumns1 , columnSorting1 FROM  [my-table]   WHERE field1 = ?  ORDER BY columnSorting1")
            );
        }

    }

    @Nested
    class CreateSortStatement {

        @Test
        void when_receive_sortColumns_expect_SortStatement() {
            List<String> sortColumns = new ArrayList<>();

            sortColumns.add("sortColumns");

            var result = defaultSQLStatementHandler.createSortStatement(sortColumns);
            var expected = "ORDER BY sortColumns";

            assertEquals(expected, result.trim());
        }


        @Test
        void when_receive_sortColumns_and_descending_is_true_expect_SortStatement() {
            List<String> sortColumns = new ArrayList<>();
            boolean descending = true;

            sortColumns.add("sortColumns");

            var result = defaultSQLStatementHandler.createSortStatement(sortColumns, descending);
            var expected = "ORDER BY sortColumns DESC";

            assertEquals(expected, result.trim());
        }

        @Test
        void when_receive_sortColumns_and_descending_is_false_expect_SortStatement() {
            List<String> sortColumns = new ArrayList<>();
            boolean descending = false;

            sortColumns.add("sortColumns");

            var result = defaultSQLStatementHandler.createSortStatement(sortColumns, descending);
            var expected = "ORDER BY sortColumns";

            assertEquals(expected, result.trim());
        }

    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class CreateJoinSelectQuery {

        @ParameterizedTest
        @MethodSource("addDataCreateJoinSelectQuery")
        void when_receive_principalTable_and_secondaryTable_and_principalKeys_and_secondaryKeys_and_principalTableRequestedColumns_and_secondaryTableRequestedColumns_and_principalTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_and_descending_expect_JoinSelect_query(boolean descending, boolean forceDistinct, String expected) {

            String principalTable = "principalTable";
            String secondaryTable = "secondaryTable";
            List<String> principalKeys = new ArrayList<>();
            List<String> secondaryKeys = new ArrayList<>();
            List<String> principalTableRequestedColumns = new ArrayList<>();
            List<String> secondaryTableRequestedColumns = new ArrayList<>();
            Map<String, String>principalTableConditions = new HashMap<>();
            Map<String, String>secondaryTableConditions = new HashMap<>();
            List<String> wildcards = new ArrayList<>();
            List<String> columnSorting = new ArrayList<>();


            principalKeys.add("principalKeys");
            secondaryKeys.add("secondaryTable");
            principalTableRequestedColumns.add("principalTableRequestedColumns");
            secondaryTableRequestedColumns.add("secondaryTableRequestedColumns");
            principalTableConditions.put("field1", "value1");
            secondaryTableConditions.put("field2", "value2");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting");

            var result = defaultSQLStatementHandler.createJoinSelectQuery(principalTable, secondaryTable, principalKeys, secondaryKeys, principalTableRequestedColumns, secondaryTableRequestedColumns, principalTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending);

            assertEquals(expected, result.getSQLStatement().trim());


        }

        Stream<Arguments> addDataCreateJoinSelectQuery() {
            return Stream.of(
                    Arguments.of(true, false, "SELECT principalTable.principalTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM principalTable,secondaryTable WHERE  principalTable.principalKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND principalTable.field1 = ?  ORDER BY columnSorting DESC"),
                    Arguments.of(true, true, "SELECT  DISTINCT principalTable.principalTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM principalTable,secondaryTable WHERE  principalTable.principalKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND principalTable.field1 = ?  ORDER BY columnSorting DESC"),
                    Arguments.of(false, false, "SELECT principalTable.principalTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM principalTable,secondaryTable WHERE  principalTable.principalKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND principalTable.field1 = ?  ORDER BY columnSorting"),
                    Arguments.of(false, true, "SELECT  DISTINCT principalTable.principalTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM principalTable,secondaryTable WHERE  principalTable.principalKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND principalTable.field1 = ?  ORDER BY columnSorting")
            );
        }


        @ParameterizedTest
        @MethodSource("addDataCreateJoinSelectQueryWithMainTable")
        void when_receive_mainTable_and_secondaryTable_and_mainKeys_and_secondaryKeys_and_mainTableRequestedColumns_and_secondaryTableRequestedColumns_and_mainTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_and_descending_expect_JoinSelect_query(boolean descending, boolean forceDistinct, String expected) {

            String mainTable = "mainTable";
            String secondaryTable = "secondaryTable";
            List<String> mainKeys = new ArrayList<>();
            List<String> secondaryKeys = new ArrayList<>();
            List<String> mainTableRequestedColumns = new ArrayList<>();
            List<String> secondaryTableRequestedColumns = new ArrayList<>();
            Map<String, String>mainTableConditions = new HashMap<>();
            Map<String, String>secondaryTableConditions = new HashMap<>();
            List<String> wildcards = new ArrayList<>();
            List<String> columnSorting = new ArrayList<>();


            mainKeys.add("mainKeys");
            secondaryKeys.add("secondaryTable");
            mainTableRequestedColumns.add("mainTableRequestedColumns");
            secondaryTableRequestedColumns.add("secondaryTableRequestedColumns");
            mainTableConditions.put("field1", "value1");
            secondaryTableConditions.put("field2", "value2");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting");

            var result = defaultSQLStatementHandler.createJoinSelectQuery(mainTable, secondaryTable, mainKeys, secondaryKeys, mainTableRequestedColumns, secondaryTableRequestedColumns, mainTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending);

            assertEquals(expected, result.getSQLStatement().trim());
        }

        Stream<Arguments> addDataCreateJoinSelectQueryWithMainTable() {
            return Stream.of(
                    Arguments.of(true, false, "SELECT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM mainTable,secondaryTable WHERE  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting DESC"),
                    Arguments.of(true, true, "SELECT  DISTINCT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM mainTable,secondaryTable WHERE  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting DESC"),
                    Arguments.of(false, false, "SELECT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM mainTable,secondaryTable WHERE  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting"),
                    Arguments.of(false, true, "SELECT  DISTINCT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM mainTable,secondaryTable WHERE  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting")
            );
        }


    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class Qualify {


        @ParameterizedTest
        @MethodSource("addDataQualify")
        void when_receive_col_and_table_expect_col_with_ExpressionKEy(String col, String table) {

            String result = defaultSQLStatementHandler.qualify(col, table);
            assertEquals(col, result);
        }

        Stream<Arguments> addDataQualify() {
            return Stream.of(
                    Arguments.of("table.[table].[table].", ""),
                    Arguments.of("[table].[col]", ""),
                    Arguments.of("table.[col]", ""),
                    Arguments.of(".[table ]", "")
            );
        }


        @Test
        void when_receive_col_expect_col_with_ExpressionKey_expect_col_with_ExpressionKey() {
            String col = "EXPRESSION_KEY_UNIQUE_IDENTIFIER";
            String table = "";

            var result = defaultSQLStatementHandler.qualify(col, table);
            var expected = col + table;

            assertEquals(expected.trim(), result);
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
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class CreateJoinFromSubselectsQuery {


        @ParameterizedTest
        @MethodSource("addDataCreateJoinFromSubselectsQuery")
        void when_receive_primaryAlias_and_secondaryAlias_and_primaryQuery_and_secondaryQuery_and_primaryKeys_and_secondaryKeys_and_primaryTableRequestedColumns_and_secondaryTableRequestedColumns_and_primaryTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_and_descending_expect_JoinFromSubSelect_query(boolean descending, boolean forceDistinct, String expected) {

            String primaryAlias = "primaryAlias";
            String secondaryAlias = "secondaryAlias";
            String primaryQuery = "primaryQuery";
            String secondaryQuery = "secondaryQuery";
            List<String> primaryKeys = new ArrayList<>();
            List<String> secondaryKeys = new ArrayList<>();
            List<String> primaryTableRequestedColumns = new ArrayList<>();
            List<String> secondaryTableRequestedColumns = new ArrayList<>();
            Map<String, String>primaryTableConditions = new HashMap<>();
            Map<String, String>secondaryTableConditions = new HashMap<>();
            List<String> wildcards = new ArrayList<>();
            List<String> columnSorting = new ArrayList<>();


            primaryKeys.add("primaryKeys");
            secondaryKeys.add("secondaryKeys");
            primaryTableRequestedColumns.add("primaryTableRequestedColumns");
            secondaryTableRequestedColumns.add("secondaryTableRequestedColumns");
            primaryTableConditions.put("field1", "value1");
            secondaryTableConditions.put("field2", "value2");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting");

            var result = defaultSQLStatementHandler.createJoinFromSubselectsQuery(primaryAlias, secondaryAlias, primaryQuery, secondaryQuery, primaryKeys, secondaryKeys, primaryTableRequestedColumns, secondaryTableRequestedColumns, primaryTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending);

            assertEquals(expected, result.getSQLStatement().trim());
        }

        Stream<Arguments> addDataCreateJoinFromSubselectsQuery() {
            return Stream.of(
                    Arguments.of(true, false, "SELECT primaryAlias.primaryTableRequestedColumns , secondaryAlias.secondaryTableRequestedColumns FROM (primaryQuery) primaryAlias INNER JOIN (secondaryQuery) secondaryAlias ON  primaryAlias.primaryKeys=secondaryAlias.secondaryKeys AND  secondaryQuery.field2 = ?  AND primaryAlias.field1 = ?  ORDER BY columnSorting DESC"),
                    Arguments.of(true, true, "SELECT  DISTINCT primaryAlias.primaryTableRequestedColumns , secondaryAlias.secondaryTableRequestedColumns FROM (primaryQuery) primaryAlias INNER JOIN (secondaryQuery) secondaryAlias ON  primaryAlias.primaryKeys=secondaryAlias.secondaryKeys AND  secondaryQuery.field2 = ?  AND primaryAlias.field1 = ?  ORDER BY columnSorting DESC"),
                    Arguments.of(false, false, "SELECT primaryAlias.primaryTableRequestedColumns , secondaryAlias.secondaryTableRequestedColumns FROM (primaryQuery) primaryAlias INNER JOIN (secondaryQuery) secondaryAlias ON  primaryAlias.primaryKeys=secondaryAlias.secondaryKeys AND  secondaryQuery.field2 = ?  AND primaryAlias.field1 = ?  ORDER BY columnSorting"),
                    Arguments.of(false, true, "SELECT  DISTINCT primaryAlias.primaryTableRequestedColumns , secondaryAlias.secondaryTableRequestedColumns FROM (primaryQuery) primaryAlias INNER JOIN (secondaryQuery) secondaryAlias ON  primaryAlias.primaryKeys=secondaryAlias.secondaryKeys AND  secondaryQuery.field2 = ?  AND primaryAlias.field1 = ?  ORDER BY columnSorting")
            );
        }

    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class CreateLeftJoinSelectQueryPageable {


        @ParameterizedTest
        @MethodSource("addDataCreateJoinSelectsQueryPageable")
        void when_receive_mainTable_and_subquery_and_secondaryTable_and_mainKeys_and_secondaryKeys_and_mainTableRequestedColumns_and_secondaryTableRequestedColumns_and_mainTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_is_false_and_descending_is_false_and_recordNumber_and_startIndex_expect_LeftJoinSelect_query_pageable(boolean descending, boolean forceDistinct, String expected) {

            String mainTable = "mainTable";
            String subquery = "subquery";
            String secondaryTable = "secondaryTable";
            List<String> mainKeys = new ArrayList<>();
            List<String> secondaryKeys = new ArrayList<>();
            List<String> mainTableRequestedColumns = new ArrayList<>();
            List<String> secondaryTableRequestedColumns = new ArrayList<>();
            Map<String, String>mainTableConditions = new HashMap<>();
            Map<String, String>secondaryTableConditions = new HashMap<>();
            List<String> wildcards = new ArrayList<>();
            List<String> columnSorting = new ArrayList<>();
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

            assertEquals(expected, result.getSQLStatement().trim());

        }


        Stream<Arguments> addDataCreateJoinSelectsQueryPageable() {
            return Stream.of(
                    Arguments.of(true, false, "SELECT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting DESC"),
                    Arguments.of(true, true, "SELECT  DISTINCT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting DESC"),
                    Arguments.of(false, false, "SELECT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting"),
                    Arguments.of(false, true, "SELECT  DISTINCT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting")
            );
        }


    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class CreateLeftJoinSelectQuery {

        @ParameterizedTest
        @MethodSource("addDataCreateJoinSelectQuery")
        void when_receive_mainTable_and_subquery_and_secondaryTable_and_mainKeys_and_secondaryKeys_and_mainTableRequestedColumns_and_secondaryTableRequestedColumns_and_mainTableConditions_and_secondaryTableConditions_and_wildcards_and_columnSorting_and_forceDistinct_and_descending_expect_LeftJoinSelect_query(boolean descending, boolean forceDistinct, String expected) {

            String mainTable = "mainTable";
            String subquery = "subquery";
            String secondaryTable = "secondaryTable";
            List<String> mainKeys = new ArrayList<>();
            List<String> secondaryKeys = new ArrayList<>();
            List<String> mainTableRequestedColumns = new ArrayList<>();
            List<String> secondaryTableRequestedColumns = new ArrayList<>();
            Map<String, String>mainTableConditions = new HashMap<>();
            Map<String, String>secondaryTableConditions = new HashMap<>();
            List<String> wildcards = new ArrayList<>();
            List<String> columnSorting = new ArrayList<>();


            mainKeys.add("mainKeys");
            secondaryKeys.add("secondaryTable");
            mainTableRequestedColumns.add("mainTableRequestedColumns");
            secondaryTableRequestedColumns.add("secondaryTableRequestedColumns");
            mainTableConditions.put("field1", "value1");
            secondaryTableConditions.put("field2", "value2");
            wildcards.add("wildcards1");
            columnSorting.add("columnSorting");

            var result = defaultSQLStatementHandler.createLeftJoinSelectQuery(mainTable, subquery, secondaryTable, mainKeys, secondaryKeys, mainTableRequestedColumns, secondaryTableRequestedColumns, mainTableConditions, secondaryTableConditions, wildcards, columnSorting, forceDistinct, descending);

            assertEquals(expected, result.getSQLStatement().trim());
        }


        Stream<Arguments> addDataCreateJoinSelectQuery() {
            return Stream.of(
                    Arguments.of(true, false, "SELECT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting DESC"),
                    Arguments.of(true, true, "SELECT  DISTINCT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting DESC"),
                    Arguments.of(false, false, "SELECT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting"),
                    Arguments.of(false, true, "SELECT  DISTINCT mainTable.mainTableRequestedColumns , secondaryTable.secondaryTableRequestedColumns FROM (subquery)mainTable LEFT JOIN secondaryTable ON  mainTable.mainKeys=secondaryTable.secondaryTable AND  secondaryTable.field2 = ?  AND mainTable.field1 = ?  ORDER BY columnSorting")
            );
        }


    }

    @Nested
    class ResultSetToEntityResult {

        @Test
        void when_receive_resultSet_and_entityResult_and_recordNumber_and_offset_and_delimited_is_true_and_columnNames_expected_ResultSet_To_EntityResult() throws Exception {

            entityResult = new EntityResultMapImpl();
            int recordNumber = 1;
            int offset = 0;
            boolean delimited = true;
            List<String> columnNames = new ArrayList<>();

            columnNames.add("column1");

            ResultSetMetaData resultSetMetaDatamock = Mockito.mock(ResultSetMetaData.class);

            Mockito.doReturn(resultSetMetaDatamock).when(resultSet).getMetaData();
            Mockito.doReturn(1).when(resultSetMetaDatamock).getColumnCount();
            Mockito.doReturn("column1").when(resultSetMetaDatamock).getColumnLabel(1);
            Mockito.doReturn(1).when(resultSetMetaDatamock).getColumnType(1);
            Mockito.doReturn(true).doReturn(false).when(resultSet).next();
            Mockito.doReturn("valueColumn1").when(resultSet).getObject("column1");

            defaultSQLStatementHandler.resultSetToEntityResult(resultSet, entityResult, recordNumber, offset, delimited, columnNames);

            assertEquals(1, entityResult.calculateRecordNumber());
        }
    }


     @Nested
    class GeneratedKeysToEntityResult {
        @Test
        void when_receive_resultSet_and_entityResult_and_generatedKeys_expected_change_entityResult_key_to_uppercase() throws Exception {
            entityResult = new EntityResultMapImpl();
            List<String> generatedKeys = new ArrayList<>();

            generatedKeys.add("COLUMN1");

            ResultSetMetaData resultSetMetaDatamock = Mockito.mock(ResultSetMetaData.class);


            Mockito.doReturn(resultSetMetaDatamock).when(resultSet).getMetaData();
            Mockito.doReturn(1).when(resultSetMetaDatamock).getColumnCount();
            Mockito.doReturn("column1").when(resultSetMetaDatamock).getColumnLabel(1);
            Mockito.doReturn(1).when(resultSetMetaDatamock).getColumnType(1);
            Mockito.doReturn(true).doReturn(false).when(resultSet).next();
            Mockito.doReturn("valueColumn1").when(resultSet).getObject("column1");

            defaultSQLStatementHandler.generatedKeysToEntityResult(resultSet, entityResult, generatedKeys);

            assertTrue(entityResult.containsKey(generatedKeys.get(0)));
        }

         @Test
         void when_receive_resultSet_and_entityResult_and_generatedKeys_expected_change_entityResult_key_to_lowercase() throws Exception {
             entityResult = new EntityResultMapImpl();
             List<String> generatedKeys = new ArrayList<>();

             generatedKeys.add("column1");

             ResultSetMetaData resultSetMetaDatamock = Mockito.mock(ResultSetMetaData.class);


             Mockito.doReturn(resultSetMetaDatamock).when(resultSet).getMetaData();
             Mockito.doReturn(1).when(resultSetMetaDatamock).getColumnCount();
             Mockito.doReturn("COLUMN1").when(resultSetMetaDatamock).getColumnLabel(1);
             Mockito.doReturn(1).when(resultSetMetaDatamock).getColumnType(1);
             Mockito.doReturn(true).doReturn(false).when(resultSet).next();
             Mockito.doReturn("valueColumn1").when(resultSet).getObject("COLUMN1");

             defaultSQLStatementHandler.generatedKeysToEntityResult(resultSet, entityResult, generatedKeys);

             assertTrue(entityResult.containsKey(generatedKeys.get(0)));
         }

    }


    @Nested
    class SetObject {

        @Mock
        PreparedStatement preparedStatement;
        @Mock
        Blob blob;
        @Mock
        Clob clob;
        @Mock
        Timestamp timestamp;
        @Mock
        Time time;
        @Mock
        Date date;

        @Test
        void when_receive_index_and_value_and_preparedStatement_and_truncDates_expect_set_object() throws SQLException {

            boolean truncDates = false;

            defaultSQLStatementHandler.setObject(1, null, preparedStatement, truncDates);
            defaultSQLStatementHandler.setObject(2, blob, preparedStatement, truncDates);
            defaultSQLStatementHandler.setObject(3, clob, preparedStatement, truncDates);

            verify(preparedStatement).setObject(1, null);
            verify(preparedStatement).setBlob(2, blob);
            verify(preparedStatement).setClob(3, clob);

            byte[] bytes = new byte[]{};
            BytesBlock bytesBlock = new BytesBlock(bytes);
            defaultSQLStatementHandler.setObject(4, bytesBlock, preparedStatement, false);
            verify(preparedStatement).setBinaryStream(eq(4), isA(ByteArrayInputStream.class), eq(0));

            LongString longString = new LongString("");
            defaultSQLStatementHandler.setObject(5, longString, preparedStatement, truncDates);
            verify(preparedStatement).setCharacterStream(eq(5), isA(StringReader.class), eq(0));

            String string = "";
            defaultSQLStatementHandler.setObject(6, string, preparedStatement, truncDates);
            verify(preparedStatement).setObject(6, string);

            defaultSQLStatementHandler.setObject(7, null, preparedStatement, truncDates);
            verify(preparedStatement).setObject(7, null);

            defaultSQLStatementHandler.setObject(8, timestamp, preparedStatement, truncDates);
            verify(preparedStatement).setTimestamp(8, timestamp);

            defaultSQLStatementHandler.setObject(9, time, preparedStatement, truncDates);
            verify(preparedStatement).setTime(9, time);

            defaultSQLStatementHandler.setObject(10, date, preparedStatement, truncDates);
            verify(preparedStatement).setDate(10, date);

        }
    }

    @Nested
    class AddMultilanguageLeftJoinTables {
        @Test
        void when_receive_table_and_tables_and_keys_and_localeId_expect_count_query() throws SQLException {

            String table = "my-table";
            List<String> tables = new ArrayList<>();
            LinkedHashMap<String, String> keys = new LinkedHashMap<>();
            LocalePair<String, Object> localeId = new LocalePair<>("field2", "value2");

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
            List<String> attributes = new ArrayList<>();
            Map<String, String>hLocaleTablesAV = new HashMap<>();

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
            Map<String, String>hLocaleTablesAV = new HashMap<>();

            hLocaleTablesAV.put("field1", "value1");

            var result = defaultSQLStatementHandler.addOuterMultilanguageColumns(sqlQuery, table, hLocaleTablesAV);
            var expected = ", my table.value1 from";

            assertEquals(expected, result.trim());


        }


    }

    @Nested
    class AddOuterMultilanguageColumnsPageable {

        @Test
        void when_receive_sqlQuery_and_table_and_hLocaleTablesAV_expect_add_outer_multilanguage_columns_pageable() {
            var sqlQuery = " from";
            String table = "my table";
            Map<String, String>hLocaleTablesAV = new HashMap<>();

            hLocaleTablesAV.put("field1", "value1");

            var result = defaultSQLStatementHandler.addOuterMultilanguageColumnsPageable(sqlQuery, table, hLocaleTablesAV);
            var expected = ", my table.value1 from";

            assertEquals(expected, result.trim());
        }
    }

}


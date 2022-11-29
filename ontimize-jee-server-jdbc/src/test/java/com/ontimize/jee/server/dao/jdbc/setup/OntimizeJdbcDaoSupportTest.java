package com.ontimize.jee.server.dao.jdbc.setup;

import com.ontimize.jee.common.db.SQLStatementBuilder;
import com.ontimize.jee.common.db.handler.DefaultSQLStatementHandler;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.server.dao.ISQLQueryAdapter;
import com.ontimize.jee.server.dao.jdbc.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OntimizeJdbcDaoSupportTest {


    @InjectMocks
    OntimizeJdbcDaoSupport ontimizeJdbcDaoSupport;

    @Mock
    QueryTemplateInformation queryTemplateInformation;

    @Mock
    JdbcTemplate jdbcTemplate;


    @Mock
    EntityResult entityResult;


    @Nested
    class Query {


        @Test
        void query_when_receive_keysValues_and_attributes_and_sort_and_queryId_and_queryAdapter_expect_query() {

            HashMap keysValues = new HashMap();
            ArrayList attributes = new ArrayList();
            ArrayList sort = new ArrayList();
            String queryId = "queryId";

            keysValues.put("key1", "value1");
            attributes.add("column1");
            sort.add("sort1");

            entityResult = new EntityResultMapImpl();
            ReflectionTestUtils.setField(ontimizeJdbcDaoSupport, "compiled", true);

            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();

            requestedColumns.add("column1");
            conditions.put("key1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("sort1");

            DefaultSQLStatementHandler statementHandler = Mockito.mock(DefaultSQLStatementHandler.class);
            ontimizeJdbcDaoSupport.setStatementHandler(statementHandler);

            OntimizeTableMetaDataContext tableMetaDataContext = ontimizeJdbcDaoSupport.getTableMetaDataContext();

            ReflectionTestUtils.setField(tableMetaDataContext, "tableName", "my-table");

            SQLStatementBuilder.SQLStatement stSQL = Mockito.mock(SQLStatementBuilder.SQLStatement.class);

            Mockito.doReturn(stSQL).when(statementHandler).createSelectQuery(tableMetaDataContext.getTableName(), requestedColumns, conditions, new ArrayList<>(), columnSorting);
            entityResult = ontimizeJdbcDaoSupport.query(keysValues, attributes, sort, queryId);

            String sqlQuery = "SELECT column1 FROM  [my-table]   WHERE key1 = ?  ORDER BY sort1";
            ArrayList vValues = new ArrayList();
            vValues.add(1);

            ArgumentPreparedStatementSetter pss = Mockito.mock(ArgumentPreparedStatementSetter.class);
            EntityResultResultSetExtractor entityResultResultSetExtractor = new EntityResultResultSetExtractor(ontimizeJdbcDaoSupport.getStatementHandler(), queryTemplateInformation, attributes);

            verify(jdbcTemplate).query(sqlQuery, pss, entityResultResultSetExtractor);


        }

        @Test
        void query_when_receive_keysValues_and_attributes_and_sort_and_queryId_and_queryAdapter_expect_error_query() {

            HashMap keysValues = new HashMap();
            ArrayList attributes = new ArrayList();
            ArrayList sort = new ArrayList();
            String queryId = "queryId";

            keysValues.put("key1", "value1");
            attributes.add("attributes1");
            sort.add("sort1");

            ReflectionTestUtils.setField(ontimizeJdbcDaoSupport, "compiled", true);

            //

            //Mockito.doReturn("my-table").when(tableMetaDataContext).getTableName();
            //ReflectionTestUtils.setField(ontimizeJdbcDaoSupport, "tableMetaDataContext", tableMetaDataContext);

            var result = ontimizeJdbcDaoSupport.query(keysValues, attributes, sort, queryId, null);
            System.out.println("result: " + result);

            var expected = "EntityResult:  ERROR CODE RETURN:  : {}";
            Assertions.assertEquals(expected, result.toString().trim());


        }


    }

    @Nested
    class PaginationQuery {


        @Mock
        PageableInfo pageableInfo;

        @Test
        void when_receive_keysValues_and_attributes_and_sort_and_queryId_and_queryAdapter_expect_paginationQuery() {

            Map keysValues = new HashMap();
            List attributes = new ArrayList();
            List sort = new ArrayList();
            String queryId = "queryId";
            List orderBy = new ArrayList();
            int recordNumber = 1;
            int startIndex = 1;


            ISQLQueryAdapter queryAdapter = Mockito.mock(ISQLQueryAdapter.class);

            keysValues.put("key1", "value1");
            attributes.add("column1");
            sort.add("sort1");
            orderBy.add("orderBy");

            ReflectionTestUtils.setField(ontimizeJdbcDaoSupport, "compiled", true);

            ArrayList requestedColumns = new ArrayList();
            HashMap conditions = new HashMap();
            ArrayList wildcards = new ArrayList();
            ArrayList columnSorting = new ArrayList();

            requestedColumns.add("column1");
            conditions.put("key1", "value1");
            wildcards.add("wildcards1");
            columnSorting.add("sort1");

            DefaultSQLStatementHandler statementHandler = Mockito.mock(DefaultSQLStatementHandler.class);
            ontimizeJdbcDaoSupport.setStatementHandler(statementHandler);

            OntimizeTableMetaDataContext tableMetaDataContext = ontimizeJdbcDaoSupport.getTableMetaDataContext();

            ReflectionTestUtils.setField(tableMetaDataContext, "tableName", "my-table");

            SQLStatementBuilder.SQLStatement stSQL = Mockito.mock(SQLStatementBuilder.SQLStatement.class);

            pageableInfo = new PageableInfo(recordNumber, startIndex);
            Mockito.doReturn(stSQL).when(statementHandler).createSelectQuery(tableMetaDataContext.getTableName(), requestedColumns, conditions, new ArrayList<>(), columnSorting);
            entityResult = ontimizeJdbcDaoSupport.paginationQuery(keysValues, attributes, recordNumber, startIndex, orderBy, queryId, null);



            //var result = ontimizeJdbcDaoSupport.paginationQuery(keysValues, attributes, recordNumber, startIndex, orderBy, queryId, null);
            System.out.println("result: " + entityResult);


        }
    }

}
package com.ontimize.jee.server.dao.jdbc.setup;

import com.ontimize.jee.common.db.SQLStatementBuilder;
import com.ontimize.jee.common.db.handler.DefaultSQLStatementHandler;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.server.dao.ISQLQueryAdapter;
import com.ontimize.jee.server.dao.jdbc.OntimizeJdbcDaoSupport;
import com.ontimize.jee.server.dao.jdbc.OntimizeTableMetaDataContext;
import com.ontimize.jee.server.dao.jdbc.PageableInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;

@ExtendWith(MockitoExtension.class)
class OntimizeJdbcDaoSupportTest {


    @InjectMocks
    OntimizeJdbcDaoSupport ontimizeJdbcDaoSupport;






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


            String value = "SELECT column1 FROM  [my-table]   WHERE key1 = ?  ORDER BY sort1";


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

            System.out.println(entityResult.toString());





            /*
            Mockito.doReturn("my-table").when(tableMetaDataContext).getTableName();
            ReflectionTestUtils.setField(ontimizeJdbcDaoSupport, "tableMetaDataContext", tableMetaDataContext);
            ontimizeJdbcDaoSupport.setStatementHandler(defaultSQLStatementHandler);
            String sqlQuery = "SELECT attributes1 FROM  [my-table]   WHERE key1 = ?  ORDER BY sort1";

            EntityResultResultSetExtractor entityResultResultSetExtractor = spy(new EntityResultResultSetExtractor(ontimizeJdbcDaoSupport.getStatementHandler(), queryTemplateInformation, attributes));

            ArrayList vValues = new ArrayList();
            vValues.add(1);

            Mockito.doReturn(entityResult).when(jdbcTemplate).query((PreparedStatementCreator) Mockito.any(), Mockito.any(),Mockito.any());

            var result = ontimizeJdbcDaoSupport.query(keysValues,attributes,sort,queryId);


            System.out.println("result: " + result.toString());

            var expected = "EntityResult:  ERROR CODE RETURN:  : {}";
            //Assertions.assertEquals(expected, result.toString().trim());

            //Mockito.verify(jdbcTemplate).query(sqlQuery,pss,entityResultResultSetExtractor);*/

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

        OntimizeTableMetaDataContext tableMetaDataContext = ontimizeJdbcDaoSupport.getTableMetaDataContext();

        @Mock
        PageableInfo pageableInfo;

        @Test
        void when_receive_keysValues_and_attributes_and_sort_and_queryId_and_queryAdapter_expect_paginationQuery() {

            HashMap keysValues = new HashMap();
            ArrayList attributes = new ArrayList();
            ArrayList sort = new ArrayList();
            String queryId = "queryId";
            ArrayList orderBy = new ArrayList();
            int recordNumber = 1;
            int startIndex = 1;
            ISQLQueryAdapter queryAdapter;

            keysValues.put("key1", "value1");
            attributes.add("attributes1");
            sort.add("sort1");
            orderBy.add("orderBy");

            ReflectionTestUtils.setField(ontimizeJdbcDaoSupport, "compiled", true);

            // Mockito.doReturn("my-table").when(tableMetaDataContext).getTableName();
            //ReflectionTestUtils.setField(ontimizeJdbcDaoSupport, "tableMetaDataContext", tableMetaDataContext);

            var result = ontimizeJdbcDaoSupport.paginationQuery(keysValues, attributes, recordNumber, startIndex, orderBy, queryId, null);
            System.out.println("result: " + result);


        }
    }

}
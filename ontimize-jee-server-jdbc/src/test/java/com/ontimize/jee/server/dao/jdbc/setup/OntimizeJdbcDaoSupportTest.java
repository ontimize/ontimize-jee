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
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class OntimizeJdbcDaoSupportTest {


    @InjectMocks
    OntimizeJdbcDaoSupport ontimizeJdbcDaoSupport;

    @Mock
    OntimizeTableMetaDataContext tableMetaDataContext;

    @Mock
    SQLStatementBuilder.SQLStatement stSQL;

    @Mock
    JdbcTemplate jdbcTemplate;

    @Mock
    DefaultSQLStatementHandler defaultSQLStatementHandler;

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
            attributes.add("attributes1");
            sort.add("sort1");

            entityResult = new EntityResultMapImpl();

            ReflectionTestUtils.setField(ontimizeJdbcDaoSupport, "compiled", true);

            QueryTemplateInformation queryTemplateInformation = Mockito.mock(QueryTemplateInformation.class);
            DefaultSQLStatementHandler defaultSQLStatementHandler = Mockito.mock(DefaultSQLStatementHandler.class);

            String id = "id";
            String value = "SELECT attributes1 FROM  [my-table]   WHERE key1 = ?  ORDER BY sort1";
            List<AmbiguousColumnType> ambiguousColumns = new ArrayList<>();
            List<FunctionColumnType> functionColumns = new ArrayList<>();
            List<String> validColumns = new ArrayList<>();
            validColumns.add("column1");
            List<OrderColumnType> orderColumns = new ArrayList<>();

            ontimizeJdbcDaoSupport.addQueryTemplateInformation(id,value,ambiguousColumns,functionColumns,validColumns,orderColumns);

            //stSQL = ontimizeJdbcDaoSupport.composeQuerySql(queryId, attributes, keysValues, sort, null, null);


            


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

            /*
            preguntas: por que no puedo usar un mock de defaultSQLStatementHandler ?????
            si quito la linea:
                        ReflectionTestUtils.setField(ontimizeJdbcDaoSupport, "tableMetaDataContext", tableMetaDataContext);
            da error
             hay otra manera? /
             */

            DefaultSQLStatementHandler defaultSQLStatementHandler = Mockito.mock(DefaultSQLStatementHandler.class);

            Mockito.doReturn("my-table").when(tableMetaDataContext).getTableName();
            ReflectionTestUtils.setField(ontimizeJdbcDaoSupport, "tableMetaDataContext", tableMetaDataContext);
            ontimizeJdbcDaoSupport.setStatementHandler(new DefaultSQLStatementHandler());

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

            Mockito.doReturn("my-table").when(tableMetaDataContext).getTableName();
            ReflectionTestUtils.setField(ontimizeJdbcDaoSupport, "tableMetaDataContext", tableMetaDataContext);
            ontimizeJdbcDaoSupport.setStatementHandler(new DefaultSQLStatementHandler());

            var result = ontimizeJdbcDaoSupport.paginationQuery(keysValues, attributes, recordNumber, startIndex, orderBy, queryId, null);
            System.out.println("result: " + result);


        }
    }

}
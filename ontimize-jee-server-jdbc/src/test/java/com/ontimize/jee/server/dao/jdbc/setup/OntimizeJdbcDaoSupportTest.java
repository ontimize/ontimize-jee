package com.ontimize.jee.server.dao.jdbc.setup;

import com.ontimize.jee.common.db.SQLStatementBuilder;
import com.ontimize.jee.common.db.handler.DefaultSQLStatementHandler;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.tools.Chronometer;
import com.ontimize.jee.server.dao.ISQLQueryAdapter;
import com.ontimize.jee.server.dao.common.ConfigurationFile;
import com.ontimize.jee.server.dao.jdbc.EntityResultResultSetExtractor;
import com.ontimize.jee.server.dao.jdbc.OntimizeJdbcDaoSupport;
import com.ontimize.jee.server.dao.jdbc.OntimizeTableMetaDataContext;
import com.ontimize.jee.server.dao.jdbc.QueryTemplateInformation;
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

@ExtendWith(MockitoExtension.class)
class OntimizeJdbcDaoSupportTest {


    @InjectMocks
    OntimizeJdbcDaoSupport ontimizeJdbcDaoSupport;


    @Nested
    class Query {

        @Mock
        OntimizeTableMetaDataContext ontimizeTableMetaDataContextMock;

        @Test
        void query_when_receive_keysValues_and_attributes_and_sort_and_queryId_and_queryAdapter_expect_query() {

            HashMap keysValues = new HashMap();
            ArrayList attributes = new ArrayList();
            ArrayList sort = new ArrayList();
            String queryId = "queryId";

            keysValues.put("key1", "value1");
            attributes.add("attributes1");
            sort.add("sort1");

            ReflectionTestUtils.setField(ontimizeJdbcDaoSupport, "compiled", true);

            QueryTemplateInformation queryTemplateInformationMock = Mockito.mock(QueryTemplateInformation.class);

            Mockito.doReturn("my-table").when(ontimizeTableMetaDataContextMock).getTableName();
            ReflectionTestUtils.setField(ontimizeJdbcDaoSupport, "tableMetaDataContext", ontimizeTableMetaDataContextMock);
            ontimizeJdbcDaoSupport.setStatementHandler(new DefaultSQLStatementHandler());

            //sqlQuery: SELECT attributes1 FROM  [my-table]   WHERE key1 = ?  ORDER BY sort1

            var result = ontimizeJdbcDaoSupport.query(keysValues, attributes, sort, queryId, null);
            System.out.println("result: " + result);
            // result: EntityResult:  ERROR CODE RETURN:  : {}

            var expected = "EntityResult:  ERROR CODE RETURN:  : {}";
            Assertions.assertEquals(expected, result.toString().trim());


        }
    }

}
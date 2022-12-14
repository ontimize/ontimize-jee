//package com.ontimize.jee.server.dao.jdbc.setup;
//
//import com.ontimize.jee.common.db.AdvancedEntityResult;
//import com.ontimize.jee.common.db.SQLStatementBuilder;
//import com.ontimize.jee.common.db.handler.DefaultSQLStatementHandler;
//import com.ontimize.jee.common.dto.EntityResult;
//import com.ontimize.jee.common.dto.EntityResultMapImpl;
//import com.ontimize.jee.server.dao.ISQLQueryAdapter;
//import com.ontimize.jee.server.dao.jdbc.*;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import javax.persistence.EntityResult;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.mockito.Mockito.verify;
//
//@ExtendWith(MockitoExtension.class)
//class OntimizeJdbcDaoSupportTest {
//
//
//    @InjectMocks
//    OntimizeJdbcDaoSupport ontimizeJdbcDaoSupport;
//
//    @Mock
//    QueryTemplateInformation queryTemplateInformation;
//
//    @Mock
//    JdbcTemplate jdbcTemplate;
//
//
//    @Mock
//    EntityResult entityResult;
//
//
//    @Nested
//    @Disabled
//    class Query {
//
//
//        @Test
//        void query_when_receive_keysValues_and_attributes_and_sort_and_queryId_and_queryAdapter_expect_query() {
//
//            HashMap keysValues = new HashMap();
//            ArrayList attributes = new ArrayList();
//            ArrayList sort = new ArrayList();
//            String queryId = "queryId";
//
//            keysValues.put("key1", "value1");
//            attributes.add("column1");
//            sort.add("sort1");
//
//            entityResult = new EntityResultMapImpl();
//            ReflectionTestUtils.setField(ontimizeJdbcDaoSupport, "compiled", true);
//
//            ArrayList requestedColumns = new ArrayList();
//            HashMap conditions = new HashMap();
//            ArrayList wildcards = new ArrayList();
//            ArrayList columnSorting = new ArrayList();
//
//            requestedColumns.add("column1");
//            conditions.put("key1", "value1");
//            wildcards.add("wildcards1");
//            columnSorting.add("sort1");
//
//            DefaultSQLStatementHandler statementHandler = Mockito.mock(DefaultSQLStatementHandler.class);
//            ontimizeJdbcDaoSupport.setStatementHandler(statementHandler);
//
//            OntimizeTableMetaDataContext tableMetaDataContext = ontimizeJdbcDaoSupport.getTableMetaDataContext();
//
//            ReflectionTestUtils.setField(tableMetaDataContext, "tableName", "my-table");
//
//            SQLStatementBuilder.SQLStatement stSQL = Mockito.mock(SQLStatementBuilder.SQLStatement.class);
//
//            Mockito.doReturn(stSQL).when(statementHandler).createSelectQuery(tableMetaDataContext.getTableName(), requestedColumns, conditions, new ArrayList<>(), columnSorting);
//            entityResult = ontimizeJdbcDaoSupport.query(keysValues, attributes, sort, queryId);
//
//            String sqlQuery = "SELECT column1 FROM  [my-table]   WHERE key1 = ?  ORDER BY sort1";
//            ArrayList vValues = new ArrayList();
//            vValues.add(1);
//
//            ArgumentPreparedStatementSetter pss = Mockito.mock(ArgumentPreparedStatementSetter.class);
//            EntityResultResultSetExtractor entityResultResultSetExtractor = new EntityResultResultSetExtractor(ontimizeJdbcDaoSupport.getStatementHandler(), queryTemplateInformation, attributes);
//
//            verify(jdbcTemplate).query(sqlQuery, pss, entityResultResultSetExtractor);
//
//
//        }
//
//
//    }
//
//    @Nested
//    @Disabled
//    class PaginationQuery {
//
//
//        @Mock
//        PageableInfo pageableInfo;
//
//        @Mock
//        ArgumentPreparedStatementSetter pss;
//        @Mock
//        AdvancedEntityResult advancedER;
//
//        @Test
//        void when_receive_keysValues_and_attributes_and_sort_and_queryId_and_queryAdapter_expect_paginationQuery() {
//
//            Map keysValues = new HashMap();
//            List attributes = new ArrayList();
//            List sort = new ArrayList();
//            String queryId = "queryId";
//            List orderBy = new ArrayList();
//            int recordNumber = 1;
//            int startIndex = 1;
//
//
//            ISQLQueryAdapter queryAdapter = Mockito.mock(ISQLQueryAdapter.class);
//
//            keysValues.put("key1", "value1");
//            attributes.add("column1");
//            sort.add("sort1");
//            orderBy.add("columnSort");
//
//            ReflectionTestUtils.setField(ontimizeJdbcDaoSupport, "compiled", true);
//
//            ArrayList requestedColumns = new ArrayList();
//            HashMap conditions = new HashMap();
//            ArrayList wildcards = new ArrayList();
//            ArrayList columnSorting = new ArrayList();
//
//            requestedColumns.add("column1");
//            conditions.put("key1", "value1");
//            wildcards.add("wildcards1");
//            columnSorting.add("columnSort");
//
//            DefaultSQLStatementHandler statementHandler = Mockito.mock(DefaultSQLStatementHandler.class);
//            ontimizeJdbcDaoSupport.setStatementHandler(statementHandler);
//
//            OntimizeTableMetaDataContext tableMetaDataContext = ontimizeJdbcDaoSupport.getTableMetaDataContext();
//
//            ReflectionTestUtils.setField(tableMetaDataContext, "tableName", "my-table");
//
//            SQLStatementBuilder.SQLStatement stSQL = Mockito.mock(SQLStatementBuilder.SQLStatement.class);
//
//            pageableInfo = new PageableInfo(recordNumber, startIndex);
//            Mockito.doReturn(stSQL).when(statementHandler).createSelectQuery(tableMetaDataContext.getTableName(), requestedColumns, conditions, new ArrayList<>(), columnSorting, recordNumber, startIndex);
//
//            String sqlQuery = "SELECT column1 FROM  [my-table]   WHERE key1 = ?  ORDER BY sort1";
//            ArrayList vValues = new ArrayList();
//            vValues.add(1);
//            pss = new ArgumentPreparedStatementSetter(vValues.toArray());
//            advancedER = ontimizeJdbcDaoSupport.paginationQuery(keysValues, attributes, recordNumber, startIndex, orderBy, queryId, null);
//
//            AdvancedEntityResultResultSetExtractor advancedEntityResultResultSetExtractor = new AdvancedEntityResultResultSetExtractor(ontimizeJdbcDaoSupport.getStatementHandler(), queryTemplateInformation, attributes, recordNumber, startIndex);
//            advancedER = jdbcTemplate.query(sqlQuery, pss, advancedEntityResultResultSetExtractor);
//
//            verify(jdbcTemplate).query(sqlQuery, pss, advancedEntityResultResultSetExtractor);
//
//
//			//advancedER.setTotalRecordCount(ontimizeJdbcDaoSupport.getQueryRecordNumber(keysValues, queryId));
//
//            //var result = ontimizeJdbcDaoSupport.paginationQuery(keysValues, attributes, recordNumber, startIndex, orderBy, queryId, null);
//            System.out.println("result: " + entityResult);
//
//
//        }
//    }
//
//}
package com.ontimize.jee.server.dao.jdbc.setup;

import com.ontimize.jee.common.db.handler.DefaultSQLStatementHandler;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.server.dao.jdbc.EntityResultResultSetExtractor;
import com.ontimize.jee.server.dao.jdbc.OntimizeJdbcDaoSupport;
import com.ontimize.jee.server.dao.jdbc.OntimizeTableMetaDataContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class OntimizeJdbcDaoSupportTest {

    @InjectMocks
    OntimizeJdbcDaoSupport ontimizeJdbcDaoSupport;

    @Test
    @DisplayName("Cuando el método query reciba keyValues, attributes, sort, queryId y queryAdapdter, devuelva un EntityResult")
    void query_when_receive_keysValues_and_attributes_and_sort_and_queryId_and_queryAdapter_expect_query() {

        ArgumentCaptor<String> sqlQuery = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ArgumentPreparedStatementSetter> pss = ArgumentCaptor.forClass(ArgumentPreparedStatementSetter.class);
        String checkSQLQuery = " SELECT column1 FROM  [my-table]   WHERE column3 = ?  AND column2 = ? ";

        Map<String,Object> keysValues = Stream.of(new Object[][] {{ "column2", 2 }, { "column3", 3 },}).collect(Collectors.toMap(data -> (String) data[0], data -> (Integer) data[1]));
        List<String> attributes = new ArrayList<>(Arrays.asList("column1"));
        List<String> sort = new ArrayList<>();
        String queryId = "queryId";

        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);

        ontimizeJdbcDaoSupport.setJdbcTemplate(jdbcTemplate);
        ontimizeJdbcDaoSupport.setStatementHandler(new DefaultSQLStatementHandler());
        ReflectionTestUtils.setField(ontimizeJdbcDaoSupport, "compiled", true);
        OntimizeTableMetaDataContext tableMetaDataContext = ontimizeJdbcDaoSupport.getTableMetaDataContext();
        ReflectionTestUtils.setField(tableMetaDataContext, "tableName", "my-table");

        EntityResult entityResult = ontimizeJdbcDaoSupport.query(keysValues, attributes, sort, queryId);
        Mockito.verify(jdbcTemplate).query(sqlQuery.capture(), pss.capture(), Mockito.any(EntityResultResultSetExtractor.class));
        assertEquals(checkSQLQuery, sqlQuery.getValue());
        Object[] args = (Object[]) (ReflectionTestUtils.getField(pss.getValue(), "args"));
        assertEquals(3, args[0]);
        assertEquals(2, args[1]);
    }
}

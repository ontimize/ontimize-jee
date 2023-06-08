package com.ontimize.jee.server.dao.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ontimize.jee.common.dto.EntityResult;

/**
 * @author Enrique Alvarez Pereira <enrique.alvarez@imatia.com>
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = InMemoryDatabaseConfig.class)
@TestInstance(Lifecycle.PER_CLASS)
public class JdbcDaoUnityTest {

    @Resource
    private RepositoryUnityTestDao repositoryUnityTestDao;

    @Resource
    private InMemoryDatabaseStructure databaseStructure;

    @Autowired
    private DataSource dataSource;

    /**
     * Initialize the database, create tables and insert records
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    @BeforeAll
    public void init() throws SQLException, ClassNotFoundException, IOException {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement();) {
            databaseStructure.databaseStructureCreation(statement, connection);
        }
    }

    /**
     * Delete database tables
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    @AfterAll
    public void destroy() throws SQLException, ClassNotFoundException, IOException {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement();) {
            databaseStructure.databaseDelete(statement, connection);
        }
    }

    /**
     * Create a connection
     * @return connection object
     * @throws SQLException
     */
    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Inserts a records in table
     * @return EntityResult with the inserted data
     */
    private EntityResult jdbcInsert(Map<String, Object> attributesValues) {
        return repositoryUnityTestDao.insert(attributesValues);
    }

    @Test
    public void testInsert() {
        Map<String, Object> attributesValues = new HashMap();
        attributesValues.put("employeeid", 1026);
        attributesValues.put("name", "Jhon");
        attributesValues.put("email", "jdoe@example.com");

        jdbcInsert(attributesValues);

        Map<String, Object> keysValues = new HashMap();
        keysValues.put("name", "Jhon");

        List<Object> attributes = new ArrayList();
        attributes.add("employeeid");
        attributes.add("name");
        attributes.add("email");
        attributes.add("uniqueid");

        EntityResult er = jdbcQuery(keysValues, attributes, null, null);
        assertEquals(25, er.getRecordValues(0).get("uniqueid"));
    }

    /**
     * Return the queried data
     * @param keysValues The keys values
     * @param attributes The attributes to return
     * @param orderBy The field to order by
     * @param queryId The name of the query
     * @return EntityResult with the data queried
     */
    private EntityResult jdbcQuery(Map<String, Object> keysValues, List<Object> attributes, List<Object> orderBy,
            String queryId) {
        return repositoryUnityTestDao.query(keysValues, attributes, orderBy, queryId);
    }

    @Test
    public void testQuery() {
        Map<String, Object> keysValues = new HashMap();
        keysValues.put("employeeid", 1001);

        List<Object> attributes = new ArrayList();
        attributes.add("employeeid");
        attributes.add("name");
        attributes.add("email");

        List<Object> orderBy = new ArrayList();
        orderBy.add("name");

        // Get one record
        assertEquals(1001, jdbcQuery(keysValues, attributes, null, null).getRecordValues(0).get("employeeid"));

        // Get all records ordered by name
        assertEquals("Anitra", jdbcQuery(null, attributes, orderBy, null).getRecordValues(0).get("name"));

        // Get all records
        assertEquals(25, jdbcQuery(null, attributes, null, null).calculateRecordNumber());
    }

    @Test
    public void testInnerJoin() {
        Map<String, Object> keysValues = new HashMap();
        keysValues.put("accountid", 103);

        List<Object> attributes = new ArrayList();
        attributes.add("employeeid");
        attributes.add("name");
        attributes.add("email");

        EntityResult eR = jdbcQuery(keysValues, attributes, null, RepositoryUnityTestDao.INNER_JOIN);
        assertEquals("Valentin", eR.getRecordValues(0).get("name"));
    }

    @Test
    public void testAmbiguousColumns() {
        Map<String, Object> keysValues = new HashMap();
        keysValues.put("name", "T3");

        List<Object> attributes = new ArrayList();
        attributes.add("employeeid");
        attributes.add("name");
        attributes.add("email");

        EntityResult eR = jdbcQuery(keysValues, attributes, null, RepositoryUnityTestDao.AMBIGUOUS_COLUMNS);

        assertEquals(1003, eR.getRecordValues(0).get("employeeid"));
    }

    @Test
    public void testOrderBy() {
        Map<String, Object> keysValues = new HashMap();
        List<Object> attributes = new ArrayList();
        attributes.add("employeeid");
        attributes.add("name");
        attributes.add("email");

        List<Object> orderBy = new ArrayList();
        orderBy.add("name");

        EntityResult eR = jdbcQuery(keysValues, attributes, orderBy, RepositoryUnityTestDao.ORDERBY);

        assertEquals(1009, eR.getRecordValues(0).get("employeeid"));

        eR = jdbcQuery(keysValues, attributes, null, RepositoryUnityTestDao.ORDERCOLUMN);

        assertEquals("Anitra", eR.getRecordValues(0).get("name"));

    }

    @Test
    public void testWhereConcat() {
        Map<String, Object> keysValues = new HashMap();
        keysValues.put("name", "DuBuque Group");
        List<Object> attributes = new ArrayList();
        attributes.add("accountid");
        attributes.add("name");
        attributes.add("balance");

        EntityResult eR = jdbcQuery(keysValues, attributes, null, RepositoryUnityTestDao.WHERE_CONCAT);

        assertEquals(103, eR.getRecordValues(0).get("accountid"));
    }

    @Test
    public void testNestedTables() {
        Map<String, Object> keysValues = new HashMap();
        keysValues.put("name", "Paddy");
        List<Object> attributes = new ArrayList();
        attributes.add("accountid");
        attributes.add("name");
        attributes.add("balance");

        EntityResult eR = jdbcQuery(keysValues, attributes, null, RepositoryUnityTestDao.NESTED_TABLES);

        assertEquals(102, eR.getRecordValues(0).get("accountid"));
    }

    @Test
    public void testFunctionColumns() {
        Map<String, Object> keysValues = new HashMap();
        List<Object> attributes = new ArrayList();
        attributes.add("accountid");
        attributes.add("name");
        attributes.add("balance");

        EntityResult eR = jdbcQuery(keysValues, attributes, null, RepositoryUnityTestDao.FUNCTION_COLUMNS);

        assertNull(eR.getRecordValues(0).get("accountid"));
    }

    /**
     * Return the records paginated
     * @param keysValues The keys values
     * @param orderBy The field to order by
     * @return EntityResult with the data queried
     */
    private EntityResult jdbcPaginationQuery(Map<String, Object> keysValues, List<Object> orderBy) {
        List<Object> attributes = new ArrayList();
        attributes.add("employeeid");
        attributes.add("name");
        attributes.add("email");

        return repositoryUnityTestDao.paginationQuery(keysValues, attributes, 5, 0, orderBy, null, null);
    }

    @Test
    public void testPaginationQuery() {
        List<Object> orderBy = new ArrayList();
        orderBy.add("name");
        EntityResult eR = jdbcPaginationQuery(null, orderBy);

        // Get the number recorded
        assertEquals(5, eR.calculateRecordNumber());

        // Get the first value ordered by name
        assertEquals("Anitra", eR.getRecordValues(0).get("name"));

        // Get the first value
        assertEquals("Jeramie", jdbcPaginationQuery(null, null).getRecordValues(0).get("name"));
    }

    /**
     * Update one record in table
     * @return EntityResult with the updated data
     */
    private EntityResult jdbcUpdate() {

        Map<String, Object> keysValues = new HashMap();
        Map<String, Object> attributes = new HashMap();

        keysValues.put("employeeid", 1001);
        attributes.put("name", "Smith");

        return repositoryUnityTestDao.update(attributes, keysValues);
    }

    @Test
    public void testUpdate() {
        jdbcUpdate();
        Map<String, Object> keysValues = new HashMap();
        keysValues.put("employeeid", 1001);

        List<Object> attributes = new ArrayList();
        attributes.add("employeeid");
        attributes.add("name");
        attributes.add("email");

        // Get the updated value
        assertEquals("Smith", jdbcQuery(keysValues, attributes, null, null).getRecordValues(0).get("name"));
    }

    /**
     * Delete one record in table
     * @return EntityResult with the deleted data
     */
    private EntityResult jdbcDelete() {
        Map<String, Object> keysValues = new HashMap();
        keysValues.put("employeeid", 1026);
        return repositoryUnityTestDao.delete(keysValues);
    }

    @Test
    public void testDelete() {
        jdbcDelete();

        Map<String, Object> keysValues = new HashMap();
        keysValues.put("employeeid", 1026);

        List<Object> attributes = new ArrayList();
        attributes.add("employeeid");
        attributes.add("name");
        attributes.add("email");

        // Dont find the deleted value
        assertNull(jdbcQuery(keysValues, attributes, null, null).get("employeeid"));
    }

}

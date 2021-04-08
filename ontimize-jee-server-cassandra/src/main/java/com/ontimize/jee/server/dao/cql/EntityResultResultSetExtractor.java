package com.ontimize.jee.server.dao.cql;

import java.util.List;

import org.springframework.cassandra.core.ResultSetExtractor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.exceptions.DriverException;
import com.ontimize.dto.EntityResult;
import com.ontimize.dto.EntityResultMapImpl;
import com.ontimize.jee.server.dao.cql.handler.CQLStatementHandler;

/**
 * The Class EntityResultResultSetExtractor.
 */
public class EntityResultResultSetExtractor implements ResultSetExtractor<EntityResult> {

    /** The sql handler. */
    private final CQLStatementHandler cqlHandler;

    private final CassandraQueryTemplateInformation queryTemplateInformation;

    private final List<String> attributes;

    /**
     * Instantiates a new entity result result set extractor.
     * @param sqlHandler the sql handler
     * @param list
     */
    public EntityResultResultSetExtractor(final CQLStatementHandler cqlHandler,
            final CassandraQueryTemplateInformation queryTemplateInformation, final List<String> attributes) {
        super();
        this.cqlHandler = cqlHandler;
        this.queryTemplateInformation = queryTemplateInformation;
        this.attributes = attributes;
    }

    /**
     * Instantiates a new entity result result set extractor.
     * @param sqlHandler the sql handler
     * @param list
     */
    public EntityResultResultSetExtractor(CQLStatementHandler cqlHandler,
            CassandraQueryTemplateInformation queryTemplateInformation) {
        this(cqlHandler, queryTemplateInformation, null);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.cassandra.core.ResultSetExtractor#extractData(java.sql .ResultSet)
     */
    @Override
    public EntityResult extractData(ResultSet rs) throws DriverException, DataAccessException {
        EntityResult er = new EntityResultMapImpl(EntityResult.OPERATION_SUCCESSFUL, EntityResult.DATA_RESULT);
        try {
            this.cqlHandler.resultSetToEntityResult(rs, er, this.attributes);
        } catch (Exception e) {
            throw new DataRetrievalFailureException(null, e);
        }
        return er;
    }

    /**
     * Gets the query template information.
     * @return the query template information
     */
    public CassandraQueryTemplateInformation getCassandraQueryTemplateInformation() {
        return this.queryTemplateInformation;
    }

}

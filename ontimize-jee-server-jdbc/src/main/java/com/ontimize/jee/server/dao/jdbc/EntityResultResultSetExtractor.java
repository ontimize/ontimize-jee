package com.ontimize.jee.server.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.ontimize.db.EntityResult;
import com.ontimize.db.handler.SQLStatementHandler;
import com.ontimize.jee.common.tools.Chronometer;

/**
 * The Class EntityResultResultSetExtractor.
 */
public class EntityResultResultSetExtractor implements ResultSetExtractor<EntityResult> {

    /** The CONSTANT logger */
    private static final Logger logger = LoggerFactory.getLogger(EntityResultResultSetExtractor.class);

    /** The sql handler. */
    private final SQLStatementHandler sqlHandler;

    private final QueryTemplateInformation queryTemplateInformation;

    private final List<?> attributes;

    /**
     * Instantiates a new entity result result set extractor.
     * @param sqlHandler the sql handler
     * @param list
     */
    public EntityResultResultSetExtractor(final SQLStatementHandler sqlHandler,
            final QueryTemplateInformation queryTemplateInformation, final List<?> attributes) {
        super();
        this.sqlHandler = sqlHandler;
        this.queryTemplateInformation = queryTemplateInformation;
        this.attributes = attributes;
    }

    /**
     * Instantiates a new entity result result set extractor.
     * @param sqlHandler the sql handler
     * @param list
     */
    public EntityResultResultSetExtractor(SQLStatementHandler sqlHandler,
            QueryTemplateInformation queryTemplateInformation) {
        this(sqlHandler, queryTemplateInformation, null);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.jdbc.core.ResultSetExtractor#extractData(java.sql .ResultSet)
     */
    @Override
    public EntityResult extractData(ResultSet rs) throws SQLException, DataAccessException {
        Chronometer chrono = new Chronometer().start();
        EntityResult er = new EntityResult(EntityResult.OPERATION_SUCCESSFUL, EntityResult.DATA_RESULT);
        try {
            EntityResultResultSetExtractor.logger.trace("ResultSet fetchSize=" + rs.getFetchSize());
            this.sqlHandler.resultSetToEntityResult(rs, er, this.attributes);
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new DataRetrievalFailureException(null, e);
        } finally {
            EntityResultResultSetExtractor.logger.trace("Time consumed in extractData= {} ms", chrono.stopMs());
        }
        return er;
    }

    /**
     * Gets the query template information.
     * @return the query template information
     */
    public QueryTemplateInformation getQueryTemplateInformation() {
        return this.queryTemplateInformation;
    }

}

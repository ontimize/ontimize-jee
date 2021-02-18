package com.ontimize.jee.server.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.ontimize.db.AdvancedEntityResultMapImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.ontimize.db.AdvancedEntityResult;
import com.ontimize.dto.EntityResult;
import com.ontimize.db.handler.SQLStatementHandler;

/**
 * The Class EntityResultResultSetExtractor.
 */
public class AdvancedEntityResultResultSetExtractor implements ResultSetExtractor<AdvancedEntityResult> {

    /** The sql handler. */
    private final SQLStatementHandler sqlHandler;

    private final QueryTemplateInformation queryTemplateInformation;

    private final List<?> attributes;

    private final int recordNumber;

    private final int offset;

    /**
     * Instantiates a new entity result result set extractor.
     * @param sqlHandler the sql handler
     * @param list
     */
    public AdvancedEntityResultResultSetExtractor(final SQLStatementHandler sqlHandler,
            final QueryTemplateInformation queryTemplateInformation, final List<?> attributes, final int recordNumber,
            final int offset) {
        super();
        this.sqlHandler = sqlHandler;
        this.queryTemplateInformation = queryTemplateInformation;
        this.attributes = attributes;
        this.offset = offset;
        this.recordNumber = recordNumber;
    }

    /**
     * Instantiates a new entity result result set extractor.
     * @param sqlHandler the sql handler
     * @param list
     */
    public AdvancedEntityResultResultSetExtractor(SQLStatementHandler sqlHandler,
            QueryTemplateInformation queryTemplateInformation, final int recordNumber, final int offset) {
        this(sqlHandler, queryTemplateInformation, null, recordNumber, offset);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.jdbc.core.ResultSetExtractor#extractData(java.sql .ResultSet)
     */
    @Override
    public AdvancedEntityResult extractData(ResultSet resultSet) throws SQLException, DataAccessException {
        AdvancedEntityResult entityResult = new AdvancedEntityResultMapImpl(EntityResult.OPERATION_SUCCESSFUL,
                EntityResult.DATA_RESULT);
        entityResult.setStartRecordIndex(this.offset);
        try {
            if (this.sqlHandler.isPageable()) {
                this.sqlHandler.resultSetToEntityResult(resultSet, entityResult, this.attributes);
            } else {
                this.sqlHandler.resultSetToEntityResult(resultSet, entityResult, this.recordNumber, this.offset,
                        this.sqlHandler.isDelimited(), this.attributes);
            }
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new DataRetrievalFailureException(null, e);
        }
        return entityResult;
    }

    /**
     * Gets the query template information.
     * @return the query template information
     */
    public QueryTemplateInformation getQueryTemplateInformation() {
        return this.queryTemplateInformation;
    }

}

package com.ontimize.jee.server.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.ontimize.db.EntityResult;
import com.ontimize.db.handler.SQLStatementHandler;

/**
 * The Class EntityResultResultSetExtractor.
 */
public class EntityResultResultSetExtractor implements ResultSetExtractor<EntityResult> {

	/** The sql handler. */
	private final SQLStatementHandler		sqlHandler;
	private final QueryTemplateInformation	queryTemplateInformation;
	private final List<?>					attributes;

	/**
	 * Instantiates a new entity result result set extractor.
	 *
	 * @param sqlHandler
	 *            the sql handler
	 * @param list
	 */
	public EntityResultResultSetExtractor(final SQLStatementHandler sqlHandler, final QueryTemplateInformation queryTemplateInformation, final List<?> attributes) {
		super();
		this.sqlHandler = sqlHandler;
		this.queryTemplateInformation = queryTemplateInformation;
		this.attributes = attributes;
	}

	/**
	 * Instantiates a new entity result result set extractor.
	 *
	 * @param sqlHandler
	 *            the sql handler
	 * @param list
	 */
	public EntityResultResultSetExtractor(SQLStatementHandler sqlHandler, QueryTemplateInformation queryTemplateInformation) {
		this(sqlHandler, queryTemplateInformation, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.jdbc.core.ResultSetExtractor#extractData(java.sql .ResultSet)
	 */
	@Override
	public EntityResult extractData(ResultSet rs) throws SQLException, DataAccessException {
		EntityResult er = new EntityResult(EntityResult.OPERATION_SUCCESSFUL, EntityResult.DATA_RESULT);
		try {
			this.sqlHandler.resultSetToEntityResult(rs, er, this.attributes);
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			throw new DataRetrievalFailureException(null, e);
		}
		return er;
	}

	/**
	 * Gets the query template information.
	 *
	 * @return the query template information
	 */
	public QueryTemplateInformation getQueryTemplateInformation() {
		return this.queryTemplateInformation;
	}

}

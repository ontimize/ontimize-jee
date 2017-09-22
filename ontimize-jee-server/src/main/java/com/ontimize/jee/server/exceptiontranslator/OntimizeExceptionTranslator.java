package com.ontimize.jee.server.exceptiontranslator;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedRuntimeException;

import com.caucho.hessian.util.IExceptionTranslator;
import com.ontimize.jee.common.exceptions.IParametrizedException;
import com.ontimize.jee.common.exceptions.NoTraceOntimizeJEEException;
import com.ontimize.jee.common.tools.ExceptionTools;
import com.ontimize.jee.common.tools.ReflectionTools;

public class OntimizeExceptionTranslator implements IExceptionTranslator, com.ontimize.jee.server.exceptiontranslator.IExceptionTranslator {

	private static final Logger			logger	= LoggerFactory.getLogger(OntimizeExceptionTranslator.class);

	private DBErrorMessagesTranslator dbErrorMessagesTranslator;

	@Override
	public Throwable translateException(Throwable original) {
		OntimizeExceptionTranslator.logger.error(null, original);
		if (original instanceof InvocationTargetException) {
			original = ((InvocationTargetException) original).getTargetException();
		}
		if (original instanceof NestedRuntimeException) {
			original = ((NestedRuntimeException) original).getMostSpecificCause();
		}
		if (original instanceof SQLException) {
			return new NoTraceOntimizeJEEException(this.getSqlErrorMessage(((SQLException) original)));
		}
		SQLException someSQLException = ExceptionTools.lookForInDepth(original, SQLException.class);
		if (someSQLException != null) {
			return new NoTraceOntimizeJEEException(this.getSqlErrorMessage(someSQLException));
		}
		if (original instanceof IParametrizedException) {
			IParametrizedException oee = (IParametrizedException) original;
			try {
				return ReflectionTools.newInstance(original.getClass(), oee.getMessage(), oee.getMessageParameters(), null, oee.getMessageType(), false, false);
			} catch (Exception ex) {
				return new NoTraceOntimizeJEEException(oee.getMessage(), null, oee.getMessageParameters(), null, false, false);
			}
		}
		return new NoTraceOntimizeJEEException(original.getMessage());
	}

	public void setDbErrorMessagesTranslator(DBErrorMessagesTranslator dbErrorMessagesTranslator) {
		this.dbErrorMessagesTranslator = dbErrorMessagesTranslator;
	}

	public DBErrorMessagesTranslator getDbErrorMessagesTranslator() {
		return this.dbErrorMessagesTranslator;
	}

	/**
	 *
	 * @param sqlException
	 *            the exception
	 * @return the associated message
	 */
	public String getSqlErrorMessage(SQLException sqlException) {
		if (this.dbErrorMessagesTranslator == null) {
			return sqlException.getMessage();
		}
		int code = sqlException.getErrorCode();
		String sqlState = sqlException.getSQLState();
		String message = this.dbErrorMessagesTranslator.getVendorCodeMessage(code);
		if (message != null) {
			return message;
		} else {
			if (sqlState != null) {
				message = this.dbErrorMessagesTranslator.getSQLStateMessage(sqlState);
				if (message != null) {
					return message;
				} else {
					return sqlException.getMessage();
				}
			}
		}
		return sqlException.getMessage();
	}
}

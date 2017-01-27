package com.ontimize.jee.server.exceptiontranslator;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.springframework.core.NestedRuntimeException;

import com.caucho.hessian.util.IExceptionTranslator;
import com.ontimize.jee.common.exceptions.NoTraceOntimizeJEEException;
import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.tools.ExceptionTools;
import com.ontimize.jee.common.tools.ReflectionTools;

public class OntimizeExceptionTranslator implements IExceptionTranslator, com.ontimize.jee.server.exceptiontranslator.IExceptionTranslator {

	private DBErrorMessagesTranslator	dbErrorMessagesTranslator;

	@Override
	public Throwable translateException(Throwable original) {
		if (original instanceof InvocationTargetException) {
			original = ((InvocationTargetException) original).getTargetException();
		}
		if (original instanceof NestedRuntimeException) {
			original = ((NestedRuntimeException) original).getMostSpecificCause();
		}
		if (original instanceof SQLException) {
			return new NoTraceOntimizeJEEException(this.getSqlErrorMessage(((SQLException) original)));
		}
		if (original instanceof OntimizeJEEException) {
			try {
				OntimizeJEEException oee = (OntimizeJEEException) original;
				return ReflectionTools.newInstance(original.getClass(), oee.getMessage(), null, oee.getMsgParameters(), false, false);
			} catch (Exception ex) {
				// do nothing
			}
		}
		if (original instanceof OntimizeJEERuntimeException) {
			try {
				OntimizeJEERuntimeException oee = (OntimizeJEERuntimeException) original;
				return ReflectionTools.newInstance(original.getClass(), oee.getMessage(), null, oee.getMsgParameters(), false, false);
			} catch (Exception ex) {
				// do nothing
			}
		}

		SQLException someSQLException = ExceptionTools.lookForInDepth(original, SQLException.class);
		if (someSQLException != null) {
			return new NoTraceOntimizeJEEException(this.getSqlErrorMessage(someSQLException));
		}
		return new NoTraceOntimizeJEEException(original);
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
		String message = null;
		if (message == null) {
			message = this.dbErrorMessagesTranslator.getVendorCodeMessage(code);
		}
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

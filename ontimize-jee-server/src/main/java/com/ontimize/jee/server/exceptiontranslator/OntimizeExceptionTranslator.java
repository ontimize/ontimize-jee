package com.ontimize.jee.server.exceptiontranslator;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedRuntimeException;
import org.springframework.remoting.RemoteAccessException;

import com.ontimize.jee.common.exceptions.IParametrizedException;
import com.ontimize.jee.common.exceptions.NoTraceOntimizeJEEException;
import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.tools.ExceptionTools;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.common.tools.proxy.InvalidDelegateException;

public class OntimizeExceptionTranslator
        implements com.ontimize.jee.server.exceptiontranslator.IExceptionTranslator {

    private static final Logger logger = LoggerFactory.getLogger(OntimizeExceptionTranslator.class);

    private DBErrorMessagesTranslator dbErrorMessagesTranslator;

    public Throwable translateException(Throwable original) {
        OntimizeExceptionTranslator.logger.error(null, original);

        SQLException someSQLException = ExceptionTools.lookForInDepth(original, SQLException.class);
        if (someSQLException != null) {
            return new NoTraceOntimizeJEEException(this.getSqlErrorMessage(someSQLException));
        }
        Throwable error = this.rescueCorrectExceptionToClient(original);
        if (error instanceof IParametrizedException) {
            IParametrizedException oee = (IParametrizedException) error;
            ReflectionTools.setFieldValue(oee, "cause", null);
            return error;
        }
        return new NoTraceOntimizeJEEException(error.getMessage());
    }

    protected Throwable rescueCorrectExceptionToClient(Throwable error) {
        if ((error instanceof InvalidDelegateException) && (error.getCause() != null)) {
            return this.rescueCorrectExceptionToClient(error.getCause());
        } else if ((error instanceof RemoteAccessException) && (error.getCause() != null)) {
            return this.rescueCorrectExceptionToClient(error.getCause());
        } else if ((error instanceof OntimizeJEERuntimeException) && (error.getCause() != null)
                && (error.getMessage() == null)) {
            return this.rescueCorrectExceptionToClient(error.getCause());
        } else if ((error instanceof OntimizeJEEException) && (error.getCause() != null)
                && (error.getMessage() == null)) {
            return this.rescueCorrectExceptionToClient(error.getCause());
        } else if ((error instanceof InvocationTargetException)
                && (((InvocationTargetException) error).getTargetException() != null) && (error.getMessage() == null)) {
            return this.rescueCorrectExceptionToClient(((InvocationTargetException) error).getTargetException());
        } else if ((error instanceof NestedRuntimeException)
                && (((NestedRuntimeException) error).getMostSpecificCause() != null) && (error.getMessage() == null)) {
            return this.rescueCorrectExceptionToClient(((NestedRuntimeException) error).getMostSpecificCause());
        }
        return error;
    }

    public void setDbErrorMessagesTranslator(DBErrorMessagesTranslator dbErrorMessagesTranslator) {
        this.dbErrorMessagesTranslator = dbErrorMessagesTranslator;
    }

    public DBErrorMessagesTranslator getDbErrorMessagesTranslator() {
        return this.dbErrorMessagesTranslator;
    }

    /**
     * @param sqlException the exception
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

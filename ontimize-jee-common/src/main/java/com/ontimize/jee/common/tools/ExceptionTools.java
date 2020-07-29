package com.ontimize.jee.common.tools;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

import org.springframework.core.NestedRuntimeException;
import org.springframework.remoting.RemoteAccessException;

import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.tools.proxy.InvalidDelegateException;

public class ExceptionTools {

    /**
     * Introspects the <code>Throwable</code> to locate some cause that matchs with received class.
     * @param original
     * @param classToLookFor
     * @return
     */
    public static <T extends Throwable> T lookForInDepth(Throwable original, Class<T> classToLookFor) {
        Throwable th = original;
        while (th != null) {
            if (classToLookFor.isAssignableFrom(th.getClass())) {
                return (T) th;
            }
            th = th.getCause();
        }
        return null;
    }

    /**
     * Return if the input Throwable contains in depht some cause that matchs with received class.
     * @param original
     * @param classToLookFor
     * @return
     */
    public static boolean containsInDepth(Throwable original, Class<? extends Throwable> classToLookFor) {
        return ExceptionTools.lookForInDepth(original, classToLookFor) != null;
    }

    /**
     * Returns the stack trace of a throwable
     * @param throwable
     * @return
     */
    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    /**
     * Clean for wrapped exceptions, considering the first one "acceptable" to client.
     * @param error
     * @return
     */
    public static Throwable rescueCorrectExceptionToClient(Throwable error) {
        if ((error instanceof InvalidDelegateException) && (error.getCause() != null)) {
            return rescueCorrectExceptionToClient(error.getCause());
        } else if ((error instanceof RemoteAccessException) && (error.getCause() != null)) {
            return rescueCorrectExceptionToClient(error.getCause());
        } else if ((error instanceof OntimizeJEERuntimeException) && (error.getCause() != null)
                && (error.getMessage() == null)) {
            return rescueCorrectExceptionToClient(error.getCause());
        } else if ((error instanceof OntimizeJEEException) && (error.getCause() != null)
                && (error.getMessage() == null)) {
            return rescueCorrectExceptionToClient(error.getCause());
        } else if ((error instanceof InvocationTargetException)
                && (((InvocationTargetException) error).getTargetException() != null) && (error.getMessage() == null)) {
            return rescueCorrectExceptionToClient(((InvocationTargetException) error).getTargetException());
        } else if ((error instanceof NestedRuntimeException)
                && (((NestedRuntimeException) error).getMostSpecificCause() != null) && (error.getMessage() == null)) {
            return rescueCorrectExceptionToClient(((NestedRuntimeException) error).getMostSpecificCause());
        } else if (error instanceof UndeclaredThrowableException) {
            return rescueCorrectExceptionToClient(((UndeclaredThrowableException) error).getUndeclaredThrowable());
        }
        return error;
    }

}

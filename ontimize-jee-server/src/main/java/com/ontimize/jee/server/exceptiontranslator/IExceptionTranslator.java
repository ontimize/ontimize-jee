package com.ontimize.jee.server.exceptiontranslator;

/**
 * The Interface IExceptionTranslator.
 */
public interface IExceptionTranslator {

    /**
     * Translate exception.
     * @param original the original
     * @return the throwable
     */
    Throwable translateException(Throwable original);

}

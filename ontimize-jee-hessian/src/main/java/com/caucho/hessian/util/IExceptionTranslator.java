package com.caucho.hessian.util;

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

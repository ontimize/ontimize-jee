/**
 *
 */
package com.ontimize.jee.common.tools.proxy;

/**
 * The Class InvalidDelegateException.
 */
public class InvalidDelegateException extends Exception {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new invalid delegate exception.
     */
    public InvalidDelegateException() {
    }

    /**
     * Instantiates a new invalid delegate exception.
     * @param message the message
     */
    public InvalidDelegateException(String message) {
        super(message);
    }

    /**
     * Instantiates a new invalid delegate exception.
     * @param cause the cause
     */
    public InvalidDelegateException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new invalid delegate exception.
     * @param message the message
     * @param cause the cause
     */
    public InvalidDelegateException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new invalid delegate exception.
     * @param message the message
     * @param cause the cause
     * @param enableSuppression the enable suppression
     * @param writableStackTrace the writable stack trace
     */
    public InvalidDelegateException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}

package com.ontimize.jee.common.security;

/**
 * Custom Exception.
 *
 * @author Imatia Innovation
 */
public class NotInPeriodException extends GeneralSecurityException {

    public NotInPeriodException(String message) {
        super(message);
    }

}

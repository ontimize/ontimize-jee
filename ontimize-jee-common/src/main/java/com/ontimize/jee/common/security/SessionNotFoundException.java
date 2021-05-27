package com.ontimize.jee.common.security;

/**
 * Custom Exception.
 *
 * @author Imatia Innovation
 */
public class SessionNotFoundException extends GeneralSecurityException {

    public SessionNotFoundException(String s) {
        super(s);
    }

}

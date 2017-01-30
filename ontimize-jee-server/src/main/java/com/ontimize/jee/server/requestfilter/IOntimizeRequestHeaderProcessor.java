/**
 * IOntimizeRequestHeaderProcessor.java 20-oct-2014
 * 
 * Copyright 2014 Imatia.
 */
package com.ontimize.jee.server.requestfilter;

/**
 * The Interface IOntimizeRequestHeaderProcessor.
 * 
 * @author <a href="luis.garcia@imatia.com">Luis Garcia</a>
 */
public interface IOntimizeRequestHeaderProcessor {

    /**
     * Process header.
     * 
     * @param requestHeaderProvider
     *            the request header provider
     */
    void processHeader(final RequestHeaderProvider requestHeaderProvider);

}

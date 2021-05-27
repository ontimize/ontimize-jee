/**
 * OntimizeRequestHeaderProcessorFilter.java 20-oct-2014
 *
 * Copyright 2014 Imatia.
 */
package com.ontimize.jee.server.requestfilter;

import java.util.List;

/**
 * The Class OntimizeRequestHeaderProcessorFilter.
 *
 * @author <a href="luis.garcia@imatia.com">Luis Garcia</a>
 */
public class OntimizeRequestHeaderProcessorFilters {

    /** The header processors. */
    private List<IOntimizeRequestHeaderProcessor> headerProcessors;

    /**
     * Gets the header processors.
     * @return the header processors
     */
    public List<IOntimizeRequestHeaderProcessor> getHeaderProcessors() {
        return this.headerProcessors;
    }

    /**
     * Sets the header processors.
     * @param headerProcessors the new header processors
     */
    public void setHeaderProcessors(List<IOntimizeRequestHeaderProcessor> headerProcessors) {
        this.headerProcessors = headerProcessors;
    }

}

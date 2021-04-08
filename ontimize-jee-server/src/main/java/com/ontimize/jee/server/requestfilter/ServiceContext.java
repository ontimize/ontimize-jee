/**
 * ServiceContext.java 20-oct-2014
 *
 * Copyright 2014 Imatia.
 */
package com.ontimize.jee.server.requestfilter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class ServiceContext.
 *
 * @author <a href="luis.garcia@imatia.com">Luis Garcia</a>
 */
public class ServiceContext {

    /** The context. */
    private final Map<String, Object> context = Collections.synchronizedMap(new HashMap<String, Object>());

    /**
     * Gets the context property.
     * @param <T> the generic type
     * @param key the key
     * @return the context property
     */
    @SuppressWarnings("unchecked")
    public <T> T getContextProperty(final String key) {
        return (T) this.context.get(key);
    }

    /**
     * Sets the context property.
     * @param <T> the generic type
     * @param key the key
     * @param value the value
     */
    public <T> void setContextProperty(final String key, final T value) {
        this.context.put(key, value);
    }

    /**
     * Reset.
     */
    public void reset() {
        this.context.clear();
    }

}

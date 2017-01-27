/**
 *
 */
package com.ontimize.jee.server.dao.common;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class ExtraWhereSection.
 */
public class WhereSection {

    /** The Constant BASE_WHERE_SECTION_KEY. */
    public static final String BASE_WHERE_SECTION_KEY = "#WHERE_SECTION#";

    /** The key. */
    private String key;

    /** The key values. */
    private Map<Object, Object> keyValues = new HashMap<Object, Object>();

    /**
     * Instantiates a new extra where section.
     */
    public WhereSection() {
        this.key = WhereSection.BASE_WHERE_SECTION_KEY;
    }

    /**
     * Instantiates a new extra where section.
     *
     * @param key
     *            the key
     * @param keyValues
     *            the key values
     */
    public WhereSection(String key, Map<Object, Object> keyValues) {
        super();
        this.key = key;
        this.keyValues = keyValues;
    }

    /**
     * Gets the key.
     *
     * @return the key
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Sets the key.
     *
     * @param key
     *            the new key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Gets the key values.
     *
     * @return the key values
     */
    public Map<Object, Object> getKeyValues() {
        return this.keyValues;
    }

    /**
     * Sets the key values.
     *
     * @param keyValues
     *            the key values
     */
    public void setKeyValues(Map<Object, Object> keyValues) {
        this.keyValues = keyValues;
    }
}

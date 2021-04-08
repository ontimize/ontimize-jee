package com.ontimize.jee.common.dao;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class UpdateOperation.
 */
public class UpdateOperation implements IOperation {

    /** The values to update. */
    private Map<?, ?> valuesToUpdate;

    /** The filter. */
    private Map<?, ?> filter;

    /**
     * Instantiates a new update operation.
     * @param valuesToUpdate the values to update
     * @param filter the filter
     */
    public UpdateOperation(Map<?, ?> valuesToUpdate, Map<?, ?> filter) {
        this();
        this.valuesToUpdate = valuesToUpdate;
        this.filter = filter;
    }

    /**
     * Instantiates a new update operation.
     */
    public UpdateOperation() {
        super();
        this.valuesToUpdate = new HashMap<>();
        this.filter = new HashMap<>();
    }

    /**
     * Gets the values to update.
     * @return the valuesToUpdate
     */
    public Map<?, ?> getValuesToUpdate() {
        return this.valuesToUpdate;
    }

    /**
     * Sets the values to update.
     * @param valuesToUpdate the valuesToUpdate to set
     */
    public void setValuesToUpdate(Map<?, ?> valuesToUpdate) {
        this.valuesToUpdate = valuesToUpdate;
    }

    /**
     * Gets the filter.
     * @return the filter
     */
    public Map<?, ?> getFilter() {
        return this.filter;
    }

    /**
     * Sets the filter.
     * @param filter the filter to set
     */
    public void setFilter(Map<?, ?> filter) {
        this.filter = filter;
    }

    public void updateValuesToUpdate(Map<?, ?> attributesValuesToUpdate) {
        ((Map<Object, Object>) this.valuesToUpdate).putAll(attributesValuesToUpdate);
    }

    /**
     * Update value to update.
     * @param columnIdentifier the column identifier
     * @param value the value
     */
    public void updateValueToUpdate(String columnIdentifier, Object value) {
        ((Map<Object, Object>) this.valuesToUpdate).put(columnIdentifier, value);

    }

}

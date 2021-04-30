package com.ontimize.jee.common.dao;

import java.util.Map;

/**
 * The Class InsertOperation.
 */
public class InsertOperation implements IOperation {

    /** The values to insert. */
    private Map<?, ?> valuesToInsert;

    /**
     * Instantiates a new insert operation.
     * @param valuesToInsert the values to insert
     */
    public InsertOperation(Map<?, ?> valuesToInsert) {
        super();
        this.valuesToInsert = valuesToInsert;
    }

    /**
     * Instantiates a new insert operation.
     */
    public InsertOperation() {
        super();
    }

    /**
     * Gets the values to insert.
     * @return the values to insert
     */
    public Map<?, ?> getValuesToInsert() {
        return this.valuesToInsert;
    }

    /**
     * Sets the values to insert.
     * @param valuesToInsert the values to insert
     */
    public void setValuesToInsert(Map<?, ?> valuesToInsert) {
        this.valuesToInsert = valuesToInsert;
    }

    /**
     * Update values to insert.
     * @param attributesValuesToUpdate the attributes values to update
     */
    public void updateValuesToInsert(Map<?, ?> attributesValuesToUpdate) {
        ((Map<Object, Object>) this.valuesToInsert).putAll(attributesValuesToUpdate);
    }

    /**
     * Update value to insert.
     * @param columnIdentifier the column identifier
     * @param value the value
     */
    public void updateValueToInsert(Object columnIdentifier, Object value) {
        ((Map<Object, Object>) this.valuesToInsert).put(columnIdentifier, value);

    }

}

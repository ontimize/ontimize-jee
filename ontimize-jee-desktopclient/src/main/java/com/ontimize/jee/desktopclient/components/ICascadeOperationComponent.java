package com.ontimize.jee.desktopclient.components;

import com.ontimize.jee.common.dao.ICascadeOperationContainer;

/**
 * The Interface ICascadeOperationComponent. Debe ser implementada por los elementos con un
 * ICascadeOperationAttribute
 */
public interface ICascadeOperationComponent {

    /**
     * Clear cascade operation cache.
     */
    void clearCascadeOperationCache();

    /**
     * Gets the cascade operations.
     * @return the cascade operations
     */
    ICascadeOperationContainer getCascadeOperations();

    /**
     * Checks for operations.
     * @return true, if successful
     */
    boolean hasOperations();

}

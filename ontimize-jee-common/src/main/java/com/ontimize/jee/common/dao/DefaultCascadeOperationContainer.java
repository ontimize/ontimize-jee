package com.ontimize.jee.common.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class CascadeOperationTableAttribute.
 */
public class DefaultCascadeOperationContainer implements ICascadeOperationContainer, Serializable {

    private static final long serialVersionUID = 1L;

    /** The delete operations. */
    private List<DeleteOperation> deleteOperations;

    /** The insert operations. */
    private List<InsertOperation> insertOperations;

    /** The update operations. */
    private List<UpdateOperation> updateOperations;

    /**
     * Instantiates a new cascade operation table attribute.
     */
    public DefaultCascadeOperationContainer() {
        super();
        this.deleteOperations = new ArrayList<>();
        this.insertOperations = new ArrayList<>();
        this.updateOperations = new ArrayList<>();
    }

    /**
     * Instantiates a new default cascade operation container.
     * @param insertOperations the insert operations
     * @param updateOperations the update operations
     * @param deleteOperations the delete operations
     */
    public DefaultCascadeOperationContainer(List<InsertOperation> insertOperations,
            List<UpdateOperation> updateOperations, List<DeleteOperation> deleteOperations) {
        super();
        this.insertOperations = insertOperations;
        this.updateOperations = updateOperations;
        this.deleteOperations = deleteOperations;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.ontimize.jee.common.services.cascadeoperation.ICascadeOperationAttribute#getDeleteOperations(
     * )
     */
    @Override
    public List<DeleteOperation> getDeleteOperations() {
        return this.deleteOperations;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.ontimize.jee.common.services.cascadeoperation.ICascadeOperationAttribute#getInsertOperations(
     * )
     */
    @Override
    public List<InsertOperation> getInsertOperations() {
        return this.insertOperations;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.ontimize.jee.common.services.cascadeoperation.ICascadeOperationAttribute#getUpdateOperations(
     * )
     */
    @Override
    public List<UpdateOperation> getUpdateOperations() {
        return this.updateOperations;
    }

    /**
     * Sets the delete operations.
     * @param deleteOperations the new delete operations
     */
    public void setDeleteOperations(List<DeleteOperation> deleteOperations) {
        this.deleteOperations = deleteOperations;
    }

    /**
     * Sets the insert operations.
     * @param insertOperations the new insert operations
     */
    public void setInsertOperations(List<InsertOperation> insertOperations) {
        this.insertOperations = insertOperations;
    }

    /**
     * Sets the update operations.
     * @param updateOperations the new update operations
     */
    public void setUpdateOperations(List<UpdateOperation> updateOperations) {
        this.updateOperations = updateOperations;
    }

    @Override
    public boolean hasOperations() {
        return (this.getInsertOperations().size() > 0) || (this.getUpdateOperations().size() > 0)
                || (this.getDeleteOperations().size() > 0);
    }

}

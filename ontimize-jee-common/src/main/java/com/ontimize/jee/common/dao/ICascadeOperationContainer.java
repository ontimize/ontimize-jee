package com.ontimize.jee.common.dao;

import java.util.List;

public interface ICascadeOperationContainer {

	/**
	 * Gets the delete operations.
	 *
	 * @return the delete operations
	 */
	List<DeleteOperation> getDeleteOperations();

	/**
	 * Gets the insert operations.
	 *
	 * @return the insert operations
	 */
	List<InsertOperation> getInsertOperations();

	/**
	 * Gets the update operations.
	 *
	 * @return the update operations
	 */
	List<UpdateOperation> getUpdateOperations();

	/**
	 * Checks for operations.
	 *
	 * @return true, if successful
	 */
	boolean hasOperations();
}

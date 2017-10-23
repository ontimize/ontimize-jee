package com.ontimize.jee.common.dao;

import java.util.Map;

/**
 * The Class DeleteOperation.
 */
public class DeleteOperation implements IOperation {

	/** The filter. */
	private Map<?, ?> filter;

	/**
	 * Instantiates a new delete operation.
	 *
	 * @param filter
	 *            the filter
	 */
	public DeleteOperation(Map<?, ?> filter) {
		super();
		this.filter = filter;
	}

	/**
	 * Instantiates a new delete operation.
	 *
	 */
	public DeleteOperation() {
		super();
	}

	/**
	 * Gets the filter.
	 *
	 * @return the filter
	 */
	public Map<?, ?> getFilter() {
		return this.filter;
	}

	/**
	 * Sets the filter.
	 *
	 * @param filter
	 *            the filter
	 */
	public void setFilter(Map<?, ?> filter) {
		this.filter = filter;
	}

}

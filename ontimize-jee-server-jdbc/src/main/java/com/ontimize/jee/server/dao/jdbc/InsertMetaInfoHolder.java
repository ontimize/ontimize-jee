package com.ontimize.jee.server.dao.jdbc;

import java.util.List;

/**
 * The Class InsertMetaInfoHolder.
 */
public class InsertMetaInfoHolder {

	/** The values. */
	private final List<Object>	values;

	/** The insert string. */
	private final String		insertString;

	/** The insert types. */
	private final int[]			insertTypes;

	/**
	 * Instantiates a new insert meta info holder.
	 *
	 * @param values
	 *            the values
	 * @param insertString
	 *            the insert string
	 * @param insertTypes
	 *            the insert types
	 */
	public InsertMetaInfoHolder(List<Object> values, String insertString, int[] insertTypes) {
		super();
		this.values = values;
		this.insertString = insertString;
		this.insertTypes = insertTypes;
	}

	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	public List<Object> getValues() {
		return this.values;
	}

	/**
	 * Gets the insert string.
	 *
	 * @return the insertString
	 */
	public String getInsertString() {
		return this.insertString;
	}

	/**
	 * Gets the insert types.
	 *
	 * @return the insertTypes
	 */
	public int[] getInsertTypes() {
		return this.insertTypes;
	}

}

package com.ontimize.jee.server.dao;

import com.ontimize.jee.server.dao.common.SQLTypeMap;

/**
 * The Class DaoProperty.
 */
public class DaoProperty {

	/** The property name. */
	private String		propertyName;

	/** The property class. */
	private Class<?>	propertyClass;

	/**
	 * The Constructor.
	 */
	public DaoProperty() {
		super();
	}

	/**
	 * The Constructor.
	 *
	 * @param propertyName
	 *            the property name
	 * @param propertyClass
	 *            the property class
	 */
	public DaoProperty(String propertyName, Class<?> propertyClass) {
		this();
		this.propertyName = propertyName;
		this.propertyClass = propertyClass;
	}

	/**
	 * The Constructor.
	 *
	 * @param propertyName
	 *            the property name
	 * @param sqlType
	 *            the sql type
	 */
	public DaoProperty(String propertyName, int sqlType) {
		this(propertyName, SQLTypeMap.convert(sqlType));
	}

	/**
	 * Sets the property class.
	 *
	 * @param propertyClass
	 *            the property class
	 */
	public void setPropertyClass(Class<?> propertyClass) {
		this.propertyClass = propertyClass;
	}

	/**
	 * Sets the property name.
	 *
	 * @param propertyName
	 *            the property name
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	/**
	 * Gets the property class.
	 *
	 * @return the property class
	 */
	public Class<?> getPropertyClass() {
		return this.propertyClass;
	}

	/**
	 * Gets the property name.
	 *
	 * @return the property name
	 */
	public String getPropertyName() {
		return this.propertyName;
	}

	/**
	 * Sets the sql type.
	 *
	 * @param sqlType
	 *            the sql type
	 */
	public void setSqlType(int sqlType) {
		this.setPropertyClass(SQLTypeMap.convert(sqlType));
	}

}

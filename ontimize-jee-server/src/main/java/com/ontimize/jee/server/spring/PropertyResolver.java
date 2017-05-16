package com.ontimize.jee.server.spring;

import java.util.Properties;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;

import com.ontimize.jee.common.spring.parser.AbstractPropertyResolver;
import com.ontimize.jee.common.tools.CheckingTools;

/**
 * The Class DatabasePropertyResolver.
 */
public class PropertyResolver<T> extends AbstractPropertyResolver<T> implements InitializingBean {

	/** The property name. */
	private String		property;

	/** The properties bean name. */
	private Properties	properties;

	/**
	 * The Constructor.
	 */
	public PropertyResolver() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {
		CheckingTools.failIfEmptyString(this.property, "Property not found");
		CheckingTools.failIfNull(this.properties, "Properties bean not found");
	}

	public String getProperty() {
		return this.property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	/**
	 * Sets the properties bean.
	 *
	 * @param propertiesBean
	 *            the properties bean
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	/**
	 * Gets the properties bean.
	 *
	 * @return the properties bean
	 */
	public Properties getProperties() {
		return this.properties;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 * @throws DataAccessException
	 *             the data access exception
	 */
	@Override
	public T getValue() throws DataAccessException {
		Object object = this.properties.get(this.property);
		return (T) (object);
	}
}

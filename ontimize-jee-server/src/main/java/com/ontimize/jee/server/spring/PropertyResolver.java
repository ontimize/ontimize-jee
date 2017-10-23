package com.ontimize.jee.server.spring;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;

import com.ontimize.jee.common.spring.parser.AbstractPropertyResolver;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.common.tools.ObjectTools;

/**
 * The Class PropertyResolver.
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
		Object object = this.resolveEnvVars(String.valueOf(this.properties.get(this.property)));
		return (T) object;
	}

	protected String resolveEnvVars(String input) {
		if (null == input) {
			return null;
		}
		Pattern p = Pattern.compile("\\$\\{(\\w+)\\}|\\$(\\w+)");
		Matcher m = p.matcher(input);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String envVarName = null == m.group(1) ? m.group(2) : m.group(1);
			String envVarValue = ObjectTools.coalesce(System.getProperty(envVarName), System.getenv(envVarName));
			m.appendReplacement(sb, null == envVarValue ? "" : Matcher.quoteReplacement(envVarValue));
		}
		m.appendTail(sb);
		return sb.toString();
	}
}

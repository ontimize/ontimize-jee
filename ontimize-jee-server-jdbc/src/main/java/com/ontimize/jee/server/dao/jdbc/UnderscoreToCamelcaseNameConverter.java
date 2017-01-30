package com.ontimize.jee.server.dao.jdbc;

import javax.sql.DataSource;

import org.springframework.util.StringUtils;

import com.ontimize.jee.server.dao.common.INameConverter;

/**
 * The Class UnderscoreToCamelcaseNameConverter.
 */
public class UnderscoreToCamelcaseNameConverter implements INameConverter {

	/** The upper case. */
	private boolean	upperCase;

	/**
	 * Instantiates a new underscore to camelcase name converter.
	 */
	public UnderscoreToCamelcaseNameConverter() {
		super();
		this.upperCase = false;
	}

	/**
	 * Convert to db.
	 *
	 * @param beanClass
	 *            the bean class
	 * @param beanProperty
	 *            the bean property
	 * @param dataSource
	 *            the data source
	 * @return the string
	 */
	@Override
	public String convertToDb(Class<?> beanClass, String beanProperty, DataSource dataSource) {
		String res = this.underscoreName(beanProperty);
		if (this.isUpperCase()) {
			res = res.toUpperCase();
		}
		return res;
	}

	/**
	 * Convert to bean.
	 *
	 * @param beanClass
	 *            the bean class
	 * @param dbColumn
	 *            the db column
	 * @param dataSource
	 *            the data source
	 * @return the string
	 */
	@Override
	public String convertToBean(Class<?> beanClass, String dbColumn, DataSource dataSource) {
		return this.camelCaseName(dbColumn);
	}

	/**
	 * Convert a name in camelCase to an underscored name in lower case. Any upper case letters are converted to lower case with a preceding
	 * underscore.
	 *
	 * @param name
	 *            the string containing original name
	 * @return the converted name
	 */
	private String underscoreName(String name) {
		if (!StringUtils.hasLength(name)) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		result.append(name.substring(0, 1).toLowerCase());
		for (int i = 1; i < name.length(); i++) {
			String s = name.substring(i, i + 1);
			String slc = s.toLowerCase();
			if (!s.equals(slc)) {
				result.append("_").append(slc);
			} else {
				result.append(s);
			}
		}
		return result.toString();
	}

	/**
	 * Convert a name in camelCase to an underscored name in lower case. Any upper case letters are converted to lower case with a preceding
	 * underscore.
	 *
	 * @param name
	 *            the string containing original name
	 * @return the converted name
	 */
	private String camelCaseName(String name) {
		if (!StringUtils.hasLength(name)) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		boolean nextUpperCase = false;
		for (int i = 0; i < name.length(); i++) {
			char s = name.charAt(i);
			if ('_' == s) {
				nextUpperCase = true;
			} else {
				if (nextUpperCase) {
					s = Character.toUpperCase(s);
					nextUpperCase = false;
				} else {
					s = Character.toLowerCase(s);
				}
				result.append(s);
			}
		}
		return result.toString();
	}

	/**
	 * Sets the upper case.
	 *
	 * @param upperCase
	 *            the new upper case
	 */
	public void setUpperCase(boolean upperCase) {
		this.upperCase = upperCase;
	}

	/**
	 * Checks if is upper case.
	 *
	 * @return true, if is upper case
	 */
	public boolean isUpperCase() {
		return this.upperCase;
	}
}

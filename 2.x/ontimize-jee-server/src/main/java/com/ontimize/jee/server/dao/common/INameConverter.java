package com.ontimize.jee.server.dao.common;

import javax.sql.DataSource;

/**
 * The Interface IBeanPropertyToDBConverter. Encargada de convertir los nombres de las propiedades de los beans a nombres de base de datos y viceversa.
 */
public interface INameConverter {

	/**
	 * Convert to db name.
	 *
	 * @param beanClass
	 *            the bean class
	 * @param beanProperty
	 *            the bean property
	 * @param dataSource
	 *            the data source
	 * @return the string
	 */
	String convertToDb(Class<?> beanClass, String beanProperty, DataSource dataSource);

	/**
	 * Convert to db name.
	 *
	 * @param beanClass
	 *            the bean class
	 * @param dbColumn
	 *            the db column
	 * @param dataSource
	 *            the data source
	 * @return the string
	 */
	String convertToBean(Class<?> beanClass, String dbColumn, DataSource dataSource);

}

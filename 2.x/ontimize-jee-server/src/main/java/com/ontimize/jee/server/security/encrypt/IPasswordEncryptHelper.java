package com.ontimize.jee.server.security.encrypt;

import java.util.Map;

/**
 * The Interface IPasswordEncryptHelper.
 */
public interface IPasswordEncryptHelper {

	/**
	 * Encrypt map.
	 *
	 * @param columnName
	 *            the column name
	 * @param keysValues
	 *            the keys values
	 * @return the map
	 */
	Map<?, ?> encryptMap(String columnName, Map<?, ?> keysValues);

	/**
	 * Encrypt.
	 *
	 * @param pass
	 *            the pass
	 * @return the string
	 */
	String encrypt(String pass);
}

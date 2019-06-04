package com.ontimize.jee.common.settings;

/**
 * The Interface ISettingsHelper.
 */
public interface ISettingsHelper {

	/**
	 * Gets the string.
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the string
	 */
	String getString(String key, String defaultValue);

	/**
	 * Gets the integer.
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the integer
	 */
	Integer getInteger(String key, Integer defaultValue);

	/**
	 * Gets the boolean.
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the boolean
	 */
	Boolean getBoolean(String key, Boolean defaultValue);

	/**
	 * Gets the long.
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the long
	 */
	Long getLong(String key, Long defaultValue);

	/**
	 * Gets the double.
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the double
	 */
	Double getDouble(String key, Long defaultValue);
}

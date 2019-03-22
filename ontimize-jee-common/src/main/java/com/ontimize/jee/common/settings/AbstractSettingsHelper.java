package com.ontimize.jee.common.settings;

import org.omg.CORBA.portable.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import com.ontimize.jee.common.tools.ObjectTools;
import com.ontimize.jee.common.tools.ParseUtilsExtended;

/**
 * The Class SettingsHelper.
 */
public abstract class AbstractSettingsHelper implements ISettingsHelper {

	/** The Constant logger. */
	private static final Logger	logger			= LoggerFactory.getLogger(AbstractSettingsHelper.class);

	/** The Constant SETTING_KEY. */
	protected final static String	SETTING_KEY		= "SET_KEY";

	/** The Constant SETTING_VALUE. */
	protected final static String	SETTING_VALUE	= "SET_VALUE";

	/**
	 * Instantiates a new settings helper.
	 */
	public AbstractSettingsHelper() {
		super();
	}

	/**
	 * Query.
	 *
	 * @param key
	 *            the key
	 * @return the entity result
	 * @throws ApplicationException
	 *             the application exception
	 * @throws OntimizeJEEException
	 */
	protected abstract String query(String key) throws OntimizeJEEException;

	/**
	 * Gets the string.
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the string
	 */
	@Override
	public String getString(String key, String defaultValue) {
		try {
			String resValue = this.query(key);
			return ObjectTools.coalesce(resValue, defaultValue);
		} catch (Exception e) {
			AbstractSettingsHelper.logger.warn("E_LOOKING_FOR_PROPERTY_WITH_KEY_{}", key, e);
			return defaultValue;
		}
	}

	/**
	 * Gets the integer.
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the integer
	 */
	@Override
	public Integer getInteger(String key, Integer defaultValue) {
		return ParseUtilsExtended.getInteger(this.getString(key, null), defaultValue);
	}

	/**
	 * Gets the boolean.
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the boolean
	 */
	@Override
	public Boolean getBoolean(String key, Boolean defaultValue) {
		return ParseUtilsExtended.getBoolean(this.getString(key, null), defaultValue);
	}

	/**
	 * Gets the long.
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the long
	 */
	@Override
	public Long getLong(String key, Long defaultValue) {
		return ParseUtilsExtended.getLong(this.getString(key, null), defaultValue);
	}

	/**
	 * Gets the double.
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the double
	 */
	@Override
	public Double getDouble(String key, Long defaultValue) {
		return ParseUtilsExtended.getDouble(this.getString(key, null), defaultValue);
	}

}

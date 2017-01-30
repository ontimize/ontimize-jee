package com.ontimize.jee.server.services.preferences;

import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

/**
 * The Interface IRemoteApplicationPreferencesEngine.
 */
public interface IRemoteApplicationPreferencesEngine {

	/** The Constant KEY_USER_PREFERENCE. */
	static final String	KEY_USER_PREFERENCE	= "user_preference";

	/**
	 * Method to get the preferences remotely.
	 *
	 * @param user
	 *            String with the user.
	 * @param preferenceName
	 *            String with the preference name.
	 * @return a <code>String</code> with the preferences.
	 * @throws OntimizeJEERuntimeException
	 *             the ontimize jee runtime exception
	 */
	String getPreference(String user, String preferenceName) throws OntimizeJEERuntimeException;

	/**
	 * Gets the default preference.
	 *
	 * @param preferenceName
	 *            the preference name
	 * @return the default preference
	 * @throws OntimizeJEERuntimeException
	 *             the ontimize jee runtime exception
	 */
	String getDefaultPreference(String preferenceName) throws OntimizeJEERuntimeException;

	/**
	 * Method to set the preferences remotely.
	 *
	 * @param user
	 *            String with the user.
	 * @param preferenceName
	 *            String with the preference name.
	 * @param value
	 *            String with the preference value.
	 * @throws OntimizeJEERuntimeException
	 *             the ontimize jee runtime exception
	 */
	void setPreference(String user, String preferenceName, String value) throws OntimizeJEERuntimeException;

	/**
	 * Sets the default preference.
	 *
	 * @param preferenceName
	 *            the preference name
	 * @param value
	 *            the value
	 * @throws OntimizeJEERuntimeException
	 *             the ontimize jee runtime exception
	 */
	void setDefaultPreference(String preferenceName, String value) throws OntimizeJEERuntimeException;

	/**
	 * Method to save the preferences stored into the buffer to the preferences file remotely.
	 *
	 * @throws OntimizeJEERuntimeException
	 *             the ontimize jee runtime exception
	 */
	void savePreferences() throws OntimizeJEERuntimeException;

	/**
	 * Method to load the preferences from the preferences file remotely.
	 *
	 * @throws OntimizeJEERuntimeException
	 *             the ontimize jee runtime exception
	 */
	void loadPreferences() throws OntimizeJEERuntimeException;

}

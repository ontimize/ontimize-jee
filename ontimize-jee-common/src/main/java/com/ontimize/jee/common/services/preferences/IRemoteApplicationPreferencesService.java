package com.ontimize.jee.common.services.preferences;

import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

/**
 * The Interface IRemoteApplicationPreferencesService.
 */
public interface IRemoteApplicationPreferencesService {

    /**
     * Gets the remote preference.
     * @param preferenceName the preference name
     * @return the remote preference
     * @throws OntimizeJEERuntimeException the ontimize jee runtime exception
     */
    String getPreference(String preferenceName) throws OntimizeJEERuntimeException;

    /**
     * Sets the remote preference.
     * @param preferenceName the preference name
     * @param value the value
     * @throws OntimizeJEERuntimeException the ontimize jee runtime exception
     */
    void setPreference(String preferenceName, String value) throws OntimizeJEERuntimeException;

    /**
     * Sets the default preference.
     * @param preferenceName the preference name
     * @param value the value
     * @throws OntimizeJEERuntimeException the ontimize jee runtime exception
     */
    void setDefaultPreference(String preferenceName, String value) throws OntimizeJEERuntimeException;

    /**
     * Gets the default preference.
     * @param preferenceName the preference name
     * @throws OntimizeJEERuntimeException the ontimize jee runtime exception
     */
    String getDefaultPreference(String preferenceName) throws OntimizeJEERuntimeException;

    /**
     * Save remote preferences.
     * @throws OntimizeJEERuntimeException the ontimize jee runtime exception
     */
    void savePreferences() throws OntimizeJEERuntimeException;

    /**
     * Load remote preferences.
     * @throws OntimizeJEERuntimeException the ontimize jee runtime exception
     */
    void loadPreferences() throws OntimizeJEERuntimeException;

}

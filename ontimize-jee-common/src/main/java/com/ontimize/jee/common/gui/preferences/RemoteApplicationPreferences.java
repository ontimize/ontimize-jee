package com.ontimize.jee.common.gui.preferences;

public interface RemoteApplicationPreferences extends java.io.Serializable, java.rmi.Remote {

    public static final String KEY_USER_PREFERENCE = "user_preference";

    /**
     * Method to get the preferences remotely.
     * @param sessionId The session ID.
     * @param user String with the user.
     * @param preferenceName String with the preference name.
     * @return a <code>String</code> with the preferences.
     * @throws Exception
     */
    public String getRemotePreference(int sessionId, String user, String preferenceName) throws Exception;

    /**
     * Method to set the preferences remotely.
     * @param sessionId The session ID.
     * @param user String with the user.
     * @param preferenceName String with the preference name.
     * @param value String with the preference value.
     * @throws Exception
     */
    public void setRemotePreference(int sessionId, String user, String preferenceName, String value) throws Exception;

    /**
     * Method to save the preferences stored into the buffer to the preferences file remotely.
     * @param sessionId The session ID.
     * @throws Exception
     */
    public void saveRemotePreferences(int sessionId) throws Exception;

    /**
     * Method to load the preferences from the preferences file remotely.
     * @param sessionId The session ID.
     * @throws Exception
     */
    public void loadRemotePreferences(int sessionId) throws Exception;

    // public boolean isRemotePreference(String preferenceName) throws
    // Exception;

}

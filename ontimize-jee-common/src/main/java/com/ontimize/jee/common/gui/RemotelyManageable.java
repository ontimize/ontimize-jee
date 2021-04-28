package com.ontimize.jee.common.gui;

import com.ontimize.jee.core.common.locator.SessionInfo;
import com.ontimize.jee.core.common.util.logging.IRemoteLogManager;

import java.rmi.Remote;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

public interface RemotelyManageable extends Remote {

    public static final String CONNECTION_INFO = "connectioninfo";

    public static final String IS_MULTICONNECTION = "ismulticonnection";

    public static final String MULTICONNECTION_IDLIST = "multiconnectionidlist";

    public static final String MULTICONNECTION_INFO = "multiconnectioninfo";

    public static final String LOADED_ENTITIES = "loaded_entities";

    public static final String ADMINISTRATION_PERMISSION = "Administration";

    /**
     * This method starts a new remote session. It returns a String that are going to be the ID that
     * allows to perform any remote action in the server.
     * @param user String with the user.
     * @param password String with the password.
     * @return a <code>String</code> with the administrator id of the remote session.
     * @throws Exception
     */
    public String startAdministrationRemoteSession(String user, String password) throws Exception;

    /**
     * This method close the user's session with the given sessionID.
     * @param id String with the administrator id of the remote session.
     * @param sessionId int with the session id of the user's session.
     * @return a <code>boolean</code> with the result of the operation.
     * @throws Exception
     */
    public boolean closeSession(String id, int sessionId) throws Exception;

    /**
     * This method obtains a list with all the start times of all connections.
     * @param id String with the administrator id of the remote session.
     * @return a <code>Map</code> with the values.
     * @throws Exception
     */
    public Map getStartSessionTime(String id) throws Exception;

    /**
     * This method returns the main information of all available user's sessions. The information
     * available of each session is:
     * <ul>
     * <li><b>SessionID.</b> The sessionID
     * <li><b>User.</b> The user of the session.
     * <li><b>InitSessionDate.</b> The time of the beginning of the session.
     * <li><b>From.</b> The host where the session has been started.
     * <li><b>LastAccess.</b> The time of the last access.
     * </ul>
     * @param id String with the administrator id of the remote session.
     * @return a <code>SessionInfo[]</code> with the information of the sessions.
     * @throws Exception
     */
    public SessionInfo[] getSessionInfo(String id) throws Exception;

    /**
     * Method that returns system information about the connection with the database. The keys to
     * request information are stored into static variables in the class RemoteUtilities. The available
     * keys are:
     * <p>
     * <Table BORDER=1 CELLPADDING=3 CELLSPACING=3 RULES=ROWS * FRAME=BOX>
     * <tr>
     * <td><b>key</td>
     * <td><b>meaning</td>
     * </tr>
     * <tr>
     * <td>CONNECTION_INFO</td>
     * <td>To get the connection information when exist only one connection.</td>
     * </tr>
     * <tr>
     * <td>IS_MULTICONNECTION</td>
     * <td>To know if multiconnection is available. Returns a String "true" or "false".</td>
     * </tr>
     * <tr>
     * <td>MULTICONNECTION_IDLIST</td>
     * <td>To get the list of Strings with the identification of the connections.</td>
     * </tr>
     * <tr>
     * <td>MULTICONNECTION_INFO</td>
     * <td>To get the connection information of all connections available.</td>
     * </tr>
     * </Table>
     * <p>
     * NOTE: the connection information contains into the Map returned is:
     * <ul>
     * <li>the number of connections with the database.
     * <li>the number of locked connections.
     * <li>the amount of locked time.
     * <li>the maximum locked time.
     * </ul>
     * @param id String with the administrator id of the remote session.
     * @param key String indicating what information you wish to request.
     * @return a <code>Map</code> with the values.
     * @throws Exception
     */
    public Map getSystemInfo(String id, String key) throws Exception;

    /**
     * This method returns a Map with the current list of users's IDs.
     * @param id String with the administrator id of the remote session.
     * @return a <code>Map</code> with the user's IDs.
     * @throws Exception
     */
    public Map getUserIds(String id) throws Exception;

    public void stopServer(String id) throws Exception;

    public void restartServer(String id) throws Exception;

    /**
     * Method that finishes the remote session.
     * @param id String with the administrator id of the remote session.
     * @throws Exception
     */
    public void finishAdministrationRemoteSession(String id) throws Exception;

    /**
     * Method that recovers the n characters indicated from the log file. If a -1 is passed as parameter
     * the method returns the entire log file.
     * @param nCharacters int with the number of characters that will be recovered.
     * @return a <code>String</code> with the characters of the log file.
     * @throws Exception
     */
    public String getLastNCharactersFromLogFile(int nCharacters) throws Exception;

    /**
     * unimplemented.
     * @return
     * @throws Exception
     */
    public boolean ping() throws Exception;

    /**
     * Method to set the DEBUG Server variables.
     * @param id String with the administrator id of the remote session.
     * @param v String with the name of the variable.
     * @param b Boolean with the value.
     * @throws Exception
     */
    public void setDEBUGValueRemote(String id, String v, boolean b) throws Exception;

    /**
     * Method to obtain the current value of the DEBUG Server variables.
     * @param id String with the administrator id of the remote session.
     * @param v String with the name of the variable.
     * @return a <code>boolean</code> with the value of the indicated variable.
     * @throws Exception
     */
    public boolean getDEBUGValueRemote(String id, String v) throws Exception;

    /**
     * Method to get remotely the Server Preferences file.
     * @param id String with the administrator id of the remote session.
     * @return a <code>Properties</code> with the preferences.
     * @throws Exception
     */
    public Properties getRemotePreferencesFile(String id) throws Exception;

    /**
     * Method to set remotely the Server Preferences file.
     * @param id String with the administrator id of the remote session.
     * @param prop The properties file to be established. This file will contain the structure of pair
     *        values: key-value.
     * @throws Exception
     */
    public void setRemotePreferencesFile(String id, Properties prop) throws Exception;

    public void removeEntity(String id, String entityName) throws Exception;

    public void closeLockedConnections(String id, String databaseId) throws Exception;

    public IRemoteLogManager getRemoteLogManager(String id) throws Exception;

}

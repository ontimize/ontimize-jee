package com.ontimize.jee.core.common.gui;

import com.ontimize.jee.core.common.dto.EntityResult;
import com.ontimize.jee.core.common.security.PermissionGroupInfo;
import com.ontimize.jee.core.common.security.PermissionInfo;

import java.rmi.Remote;
import java.util.Map;

public interface ServerPermissionManager extends Remote {

    public static final String ENTITY_LIST_KEY = "ENTITY_LIST";

    public static final String USER_PROFILE_PERMISSIONS_KEY = "PROFILE_PERMISSIONS";

    public static final String USER_PERMISSIONS_KEY = "USER_PERMISSIONS";

    /**
     * Sets the server permissions value
     * @param keys Keys to identify the user
     * @param sessionId User session identifier
     * @param permissionXML XML value to describes the server permissions
     * @return
     * @throws Exception
     */
    public EntityResult setServerPermissions(Map keys, int sessionId, StringBuffer permissionXML)
            throws Exception;

    /**
     * Get the permission definition for a specified profile
     * @param profileKeys Keys to identify a profile
     * @param sessionId User session identifier
     * @return
     * @throws Exception
     */
    public EntityResult getUserProfileServerPermissions(Map profileKeys, int sessionId) throws Exception;

    public EntityResult setUserProfileServerPermissions(Map profileKeys, StringBuffer permissions, int sessionId)
            throws Exception;

    /**
     * Get the user permissions
     * @param userKeys Keys to identify the user
     * @param sessionId User session identifier
     * @return
     * @throws Exception
     */
    public EntityResult getServerPermissions(Map userKeys, int sessionId) throws Exception;

    public EntityResult getEntityList(int sessionId) throws Exception;

    /**
     * Checks if the specified user has permissions to execute an action in the specified entity
     * @param entity Entity name
     * @param action Action to check
     * @param sessionId User session identifier
     * @param time
     * @return
     * @throws Exception
     */
    public boolean checkActionPermission(String entity, String action, int sessionId, long time) throws Exception;

    public PermissionInfo getPermissionInfo(String entity, String action, int sessionId) throws Exception;

    public PermissionGroupInfo[] getPermissionGroupsInfo() throws Exception;

}

package com.ontimize.jee.common.security;

import com.ontimize.jee.common.dto.EntityResult;

import java.rmi.Remote;
import java.util.Map;

/**
 * Remote Interface to manage client permissions.
 *
 * @author Imatia Innovation
 */
public interface ClientPermissionManager extends Remote {

    public static final String PERMISSIONS_KEY = "ClientPermissionKey";

    /**
     * Gets an object representing client permissions.<br>
     * @param userKeys This parameters allow to identify the user. In local locator is not needed and
     *        can be a null value.
     * @param sessionId A client sessionId
     * @return An {@link EntityResult} object
     * @throws Exception if an error occurs
     */
    public EntityResult getClientPermissions(Map userKeys, int sessionId) throws Exception;

    /**
     * Sets the client permissions. Creates a {@link ClientSecurityManager} object with all permissions
     * information for the specified user.
     * @param userKeys Keys to identify the user
     * @param sessionId User session identifier
     * @throws Exception if an error occurs
     */
    public void installClientPermissions(Map userKeys, int sessionId) throws Exception;

    public long getTime() throws Exception;

}

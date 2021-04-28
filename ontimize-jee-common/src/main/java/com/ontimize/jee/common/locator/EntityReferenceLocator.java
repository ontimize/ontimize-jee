package com.ontimize.jee.common.locator;

import com.ontimize.jee.core.common.db.Entity;
import com.ontimize.jee.core.common.gui.ClientWatch;

import java.rmi.Remote;

/**
 * This interface defines the methods that must be implemented by a object that provides entity
 * references.<br>
 * This interface extends Remote to provide RMI support. In this way is easy to implement a
 * reference remote locator.<br>
 * Security must be implemented in all classes that use this interface.<br>
 * The method {@link #getEntityReference(String)} has not parameters to identify the user to avoid
 * that all application components need to know this values.
 *
 * @see SecureEntityReferenceLocator
 * @version 1.1 20/05/2001
 */

public interface EntityReferenceLocator extends Remote {

    /**
     * Method to start a client session. Entity reference locator returns an integer value that must be
     * used in all entity requests.
     * @param user User name
     * @param password User password
     * @param client Object to checks if this session is open
     * @return Integer value not negative used to identifier this client and allows him to get entity
     *         references
     * @throws Exception
     */
    public int startSession(String user, String password, ClientWatch client) throws Exception;

    /**
     * This method gets an entity reference.
     * @param entityName Entity name
     * @return
     * @throws Exception
     */
    public Entity getEntityReference(String entityName) throws Exception;

    /**
     * Method to know the client session identifier. This is only used in client side. In the server
     * side these methods throw an exception.
     * @return Client session identifier.
     * @throws Exception
     */
    public int getSessionId() throws Exception;

    /**
     * This method finishes a client session. When this method is called, the entity reference locator
     * does not provide more entity references to this client identifier.
     * @param id Client session identifier
     * @throws Exception
     */
    public void endSession(int id) throws Exception;

}

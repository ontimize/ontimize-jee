package com.ontimize.jee.core.common.locator;

import com.ontimize.jee.core.common.db.Entity;

/**
 * This interface extends {@EntityReferenceLocator} to provide security
 *
 */

public interface SecureEntityReferenceLocator extends EntityReferenceLocator {

    public String ACCESS_DENIED = "Access denied";

    /**
     * This method replaces to {@link EntityReferenceLocator#getEntityReference(String)} and gets entity
     * references with security
     * @param entity Requested entity name
     * @param user Name of the user who is asking for the reference
     * @param sessionId Session identifier for the user who is asking the reference
     * @return Entity, or null if this entity no exist or the user authentification is wrong
     * @throws Exception
     */
    public Entity getEntityReference(String entity, String user, int sessionId) throws Exception;

    /**
     * Return true if the user 'user' with session identifier 'id' has a current session opened
     * @param user
     * @param id
     * @return
     * @throws Exception
     */
    public boolean hasSession(String user, int id) throws Exception;

}

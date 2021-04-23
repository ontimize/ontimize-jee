package com.ontimize.jee.core.common.db;

import java.rmi.Remote;
import java.util.Vector;


public interface SecureEntity extends Remote {

    /**
     * This method returns a chain list, which identifies the actions that the entity realizes. These
     * actions can be controlled by one server security manager ({@link ServerSecurityManager}. If it is
     * used with classes which uses XML, it is not able to contain blank spaces
     * @return
     * @throws Exception
     */
    public Vector getActions() throws Exception;

    public String getName() throws Exception;

    public String getDescription() throws Exception;

}

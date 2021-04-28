package com.ontimize.jee.common.gui;

import java.rmi.Remote;

/**
 * Client interface to export and execute calls against the server and test the connection.
 */
public interface ClientWatch extends Remote {

    public int getId() throws Exception;

}

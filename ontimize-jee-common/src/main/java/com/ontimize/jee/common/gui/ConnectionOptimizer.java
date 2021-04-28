package com.ontimize.jee.common.gui;

import com.ontimize.jee.core.common.dto.EntityResult;

import java.rmi.Remote;

/**
 * This interface is used to set the compression level for an EntityResult in a query
 */
public interface ConnectionOptimizer extends Remote {

    /**
     * This method test the connection speed an return an EntityResult to check the line
     * @param sizeInBytes
     * @param compressed
     * @return
     * @throws Exception
     */
    public EntityResult testConnectionSpeed(int sizeInBytes, boolean compressed) throws Exception;

    /**
     * Sets the value of the user data compression threshold (minimum number of bytes to apply
     * compression on data transmissions)
     * @param user User name
     * @param id User session identifier
     * @param compression Compression threshold value
     * @throws Exception
     */
    public void setDataCompressionThreshold(String user, int id, int compression) throws Exception;

    /**
     * Gets the value of the data compression threshold for the specified user
     * @param sessionId User session identifier
     * @return
     * @throws Exception
     */
    public int getDataCompressionThreshold(int sessionId) throws Exception;

}

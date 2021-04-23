package com.ontimize.jee.core.common.db;

import com.ontimize.jee.core.common.util.remote.BytesBlock;

import java.rmi.Remote;
import java.util.Map;

public interface FileManagementEntity extends Remote {

    /**
     * The file size is returned if it is known. Return -1 when it is an unknown value. Before calls
     * this method the method prepareToTransfer must be called
     * @param transferId
     * @return
     * @throws Exception
     */
    public long getSize(String transferId) throws Exception;

    /**
     * Get the file extension. If the file has not extension return null
     * @param transferId
     * @return
     * @throws Exception
     */
    public String getExtension(String transferId) throws Exception;

    /**
     * Indicates the entity must prepare for transferring file content.
     * @param column
     * @param keys
     * @param sessionId
     * @return A random string identifying the transfer process. This identifier is used in the next
     *         requests for this file content
     * @throws Exception
     */
    public String prepareToTransfer(String column, Map keys, int sessionId) throws Exception;

    /**
     * Indicates the entity must prepare for receiving file content.
     * @param column
     * @param keys
     * @param sessionId
     * @return A random string identifying the transfer process. This identifier is used in the next
     *         requests to receive the bytes blocks
     * @throws Exception
     */
    public String prepareToReceive(String column, Map keys, int sessionId) throws Exception;

    public String prepareToTransfer(Map cv, int sessionId) throws Exception;

    public String prepareToReceive(Map keys, String fileName, String fileDescription, int sessionId)
            throws Exception;

    public String prepareToReceive(Map keys, String fileName, String originalFilePath, String fileDescription,
            int sessionId) throws Exception;

    /**
     * Gets a {@link BytesBlock} with the bytes from offset and with the specified length. Return null
     * if it is the end of the file. The prepareToTransfer method must be called previously to get the
     * 'transferId' value.
     * @param transferId
     * @param offset
     * @param lenght
     * @param sessionId
     * @return
     * @throws Exception
     */
    public BytesBlock getBytes(String transferId, int offset, int lenght, int sessionId) throws Exception;

    public void putBytes(String transferId, BytesBlock bytesBlock, int sessionId) throws Exception;

    /**
     * Indicates that the transfer process is finished
     * @param transferId
     * @param sessionId
     * @return
     * @throws Exception
     */
    public boolean finishReceiving(String transferId, int sessionId) throws Exception;

    /**
     * Cancels the file reception
     * @param transferId
     * @param sessionId
     * @throws Exception
     */
    public void cancelReceiving(String transferId, int sessionId) throws Exception;

    public boolean deleteAttachmentFile(Map keys, int sessionId) throws Exception;

}

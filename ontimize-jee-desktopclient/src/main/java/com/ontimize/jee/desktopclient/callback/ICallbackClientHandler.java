package com.ontimize.jee.desktopclient.callback;

import java.io.IOException;

import com.ontimize.jee.common.exceptions.OntimizeJEEException;

/**
 * The Interface IWebSocketHandler.
 */
public interface ICallbackClientHandler {

    /**
     * Send message.
     * @param messageType the message type
     * @param ob the ob
     * @param receiver the receiver
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void sendMessage(Integer messageType, Object ob) throws OntimizeJEEException;

    /**
     * Send message.
     * @param messageType the message type
     * @param messageSubtype the message subtype
     * @param ob the ob
     * @throws OntimizeJEEException the ontimize jee exception
     */
    void sendMessage(Integer messageType, String messageSubtype, Object ob) throws OntimizeJEEException;

    /**
     * Adds the web socket message listener.
     * @param listener the listener
     */
    void addCallbackEventListener(ICallbackEventListener listener);

    /**
     * Removes the web socket event listener.
     * @param remoteOperationDelegate the remote operation delegate
     */
    void removeCallbackEventListener(ICallbackEventListener remoteOperationDelegate);

}

package com.ontimize.jee.desktopclient.locator.websocket;

import java.io.IOException;

import com.ontimize.jee.common.exceptions.OntimizeJEEException;

/**
 * The Interface IWebSocketHandler.
 */
public interface IWebSocketClientHandler {

	/**
	 * Send message.
	 *
	 * @param messageType
	 *            the message type
	 * @param ob
	 *            the ob
	 * @param receiver
	 *            the receiver
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void sendMessage(Integer messageType, Object ob) throws OntimizeJEEException;

	/**
	 * Send message.
	 *
	 * @param messageType
	 *            the message type
	 * @param messageSubtype
	 *            the message subtype
	 * @param ob
	 *            the ob
	 * @throws OntimizeJEEException
	 *             the ontimize jee exception
	 */
	void sendMessage(Integer messageType, String messageSubtype, Object ob) throws OntimizeJEEException;

	/**
	 * Adds the web socket message listener.
	 *
	 * @param listener
	 *            the listener
	 */
	void addWebSocketEventListener(IWebSocketEventListener listener);

	/**
	 * Removes the web socket event listener.
	 *
	 * @param remoteOperationDelegate
	 *            the remote operation delegate
	 */
	void removeWebSocketEventListener(IWebSocketEventListener remoteOperationDelegate);
}

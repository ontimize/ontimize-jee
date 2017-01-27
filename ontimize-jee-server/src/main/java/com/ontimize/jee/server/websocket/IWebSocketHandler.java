package com.ontimize.jee.server.websocket;

import java.io.IOException;

import org.springframework.web.socket.WebSocketSession;

/**
 * The Interface IWebSocketHandler.
 */
public interface IWebSocketHandler {

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
	void sendMessage(Integer messageType, String messageSubtype, Object ob, WebSocketSession receiver) throws IOException;

	/**
	 * Adds the web socket message listener.
	 *
	 * @param listener
	 *            the listener
	 */
	void addWebSocketEventListener(IWebSocketEventListener listener);
}

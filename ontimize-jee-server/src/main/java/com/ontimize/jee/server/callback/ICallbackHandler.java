package com.ontimize.jee.server.callback;

import java.io.IOException;
import java.util.List;

/**
 * The Interface IWebSocketHandler.
 */
public interface ICallbackHandler {

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
	void sendMessage(Integer messageType, String messageSubtype, Object ob, CallbackSession... receiver);

	void sendMessage(Integer messageType, String messageSubtype, Object ob, CallbackSession receiver) throws IOException;

	void sendBroadcastMessage(Integer messageType, String messageSubtype, Object ob);

	List<CallbackSession> getSessionsForUser(String userLogin);

	/**
	 * Adds the web socket message listener.
	 *
	 * @param listener
	 *            the listener
	 */
	void addCallbackEventListener(ICallbackEventListener listener);
}

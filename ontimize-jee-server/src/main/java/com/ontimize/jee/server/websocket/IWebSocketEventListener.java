package com.ontimize.jee.server.websocket;

import org.springframework.web.socket.WebSocketSession;

import com.ontimize.jee.common.websocket.WebsocketWrapperMessage;

/**
 * The listener interface for receiving IWebSocketMessage events. The class that is interested in processing a IWebSocketMessage event implements this interface, and the object
 * created with that class is registered with a component using the component's <code>addIWebSocketMessageListener<code> method. When the IWebSocketMessage event occurs, that
 * object's appropriate method is invoked.
 *
 * @see IWebSocketMessageEvent
 */
public interface IWebSocketEventListener {

	/**
	 * On web socket message received.
	 *
	 * @param from
	 *            the from
	 * @param message
	 *            the message
	 */
	void onWebSocketMessageReceived(WebSocketSession from, WebsocketWrapperMessage message);
}

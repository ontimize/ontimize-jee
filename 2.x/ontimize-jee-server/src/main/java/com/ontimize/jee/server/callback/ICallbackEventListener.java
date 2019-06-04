package com.ontimize.jee.server.callback;

import com.ontimize.jee.common.callback.CallbackWrapperMessage;

/**
 * The listener interface for receiving IWebSocketMessage events. The class that is interested in processing a IWebSocketMessage event implements this interface, and the object
 * created with that class is registered with a component using the component's <code>addIWebSocketMessageListener<code> method. When the IWebSocketMessage event occurs, that
 * object's appropriate method is invoked.
 *
 * @see IWebSocketMessageEvent
 */
public interface ICallbackEventListener {

	/**
	 * On web socket message received.
	 *
	 * @param from
	 *            the from
	 * @param message
	 *            the message
	 */
	void onCallbackMessageReceived(CallbackSession from, CallbackWrapperMessage message);
}

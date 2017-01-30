package com.ontimize.jee.server.services.remoteoperation;

import com.ontimize.jee.common.websocket.WebsocketWrapperMessage;

/**
 * The Interface IRemoteOperation.
 */
public interface IRemoteOperation extends Runnable {

	/**
	 * Inits the operation.
	 *
	 * @param parameters
	 *            the parameters
	 */
	void init(Object parameters);

	/**
	 * Sets the listener.
	 *
	 * @param listener
	 *            the new listener
	 */
	void setListener(IRemoteOperationListener listener);

	/**
	 * On custom message received.
	 *
	 * @param msg
	 *            the msg
	 * @return the object
	 */
	Object onCustomMessageReceived(WebsocketWrapperMessage msg);

	/**
	 * On cancel received.
	 */
	void onCancelReceived();

	/**
	 * The main task logic must be implemented here
	 *
	 * @return the object
	 */
	Object execute();

}

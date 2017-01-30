package com.ontimize.jee.server.services.remoteoperation;

/**
 * The listener interface for receiving IRemoteOperation events. The class that
 * is interested in processing a IRemoteOperation event implements this
 * interface, and the object created with that class is registered with a
 * component using the component's
 * <code>addIRemoteOperationListener<code> method. When
 * the IRemoteOperation event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see IRemoteOperationEvent
 */
public interface IRemoteOperationListener {

	/**
	 * On operation finished.
	 * 
	 * @param result
	 *            the result
	 */
	void onOperationFinished(Object result);

	/**
	 * On operation error.
	 * 
	 * @param result
	 *            the result
	 */
	void onOperationError(Object result);

	/**
	 * On operation step.
	 * 
	 * @param currentStep
	 *            the current step
	 * @param maxStep
	 *            the max step
	 * @param estimatedTime
	 *            the estimated time
	 * @param message
	 *            the message
	 * @param parameters
	 *            the parameters
	 */
	void onOperationStep(int currentStep, int maxStep, long estimatedTime, String message, Object parameters);
}

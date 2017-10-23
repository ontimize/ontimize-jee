package com.ontimize.jee.server.services.remoteoperation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.ontimize.jee.common.callback.CallbackWrapperMessage;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.services.remoteoperation.RemoteOperationCancelMessage;
import com.ontimize.jee.common.services.remoteoperation.RemoteOperationErrorMessage;
import com.ontimize.jee.common.services.remoteoperation.RemoteOperationFinishMessage;
import com.ontimize.jee.common.services.remoteoperation.RemoteOperationStatusMessage;
import com.ontimize.jee.common.services.remoteoperation.RemoteOperationStatuses;
import com.ontimize.jee.common.services.remoteoperation.RemoteOperationStatuses.RemoteOperationStatus;
import com.ontimize.jee.server.callback.CallbackSession;
import com.ontimize.jee.server.callback.ICallbackHandler;

/**
 * The Class RemoteOperationDelegate.
 */
public class RemoteOperationDelegate implements IRemoteOperationListener {

	/** The logger. */
	private static final Logger		logger	= LoggerFactory.getLogger(RemoteOperationDelegate.class);

	/** The operation. */
	private IRemoteOperation		operation;

	/** The session. */
	private final CallbackSession	session;

	/** The status. */
	private RemoteOperationStatus	status;

	private ICallbackHandler		webSocketHandler;
	private String					operationId;

	/**
	 * Instantiates a new remote operation delegate.
	 *
	 * @param context
	 *
	 * @param session
	 *            the session
	 * @param className
	 *            the class name
	 * @param parameters
	 *            the parameters
	 */
	public RemoteOperationDelegate(ApplicationContext context, CallbackSession session, String operationId, String className, Object parameters) {
		super();
		try {
			this.session = session;
			this.operationId = operationId;
			this.webSocketHandler = context.getBean(ICallbackHandler.class);
			this.operation = (IRemoteOperation) Class.forName(className).newInstance();
			this.operation.setListener(this);
			this.operation.init(parameters);

			// TODO incluir la capa de autorización
			// ProxyFactory proxyFactory = new ProxyFactory(this.operation);
			// //falta rellenar el MethdSecurityInterceptor
			// proxyFactory.addAdvice(new MethodSecurityInterceptor());
			// this.operation = (IRemoteOperation) proxyFactory.getProxy();

			// this.remoteOperationManager = (IRemoteOperationManager) this.context.getAutowireCapableBeanFactory().createBean(
			// DefaultRemoteOperationManager.class);
			// configureBean(this.remoteOperationManager, "defaultRemoteOperationManager");

			this.status = RemoteOperationStatus.WAITING;
			this.onOperationStep(-1, -1, -1, null, null);
		} catch (Exception e) {
			throw new OntimizeJEERuntimeException(e);
		}
	}

	/**
	 * Execute.
	 */
	public void execute() {
		this.operation.run();
		this.status = RemoteOperationStatus.RUNNING;
		this.onOperationStep(-1, -1, -1, null, null);
	}

	/**
	 * On custom message.
	 *
	 * @param msg
	 *            the msg
	 */
	public void onCustomMessage(CallbackWrapperMessage msg) {
		Object res = this.operation.onCustomMessageReceived(msg);
		if (res != null) {
			try {
				this.webSocketHandler.sendMessage(msg.getType(), msg.getSubtype(), res, this.session);
			} catch (Exception error) {
				RemoteOperationDelegate.logger.error(null, error);
			}
		}
	}

	/**
	 * On cancel.
	 *
	 * @param msg
	 *            the msg
	 */
	public void onCancel(RemoteOperationCancelMessage msg) {
		this.operation.onCancelReceived();
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.remoteoperation.IRemoteOperationListener #onOperationFinished(java.lang.Object)
	 */
	@Override
	public void onOperationFinished(Object result) {
		this.status = RemoteOperationStatus.FINISHED;
		RemoteOperationFinishMessage msg = new RemoteOperationFinishMessage(false, result);
		try {
			this.webSocketHandler.sendMessage(RemoteOperationStatuses.WEBSOCKET_MESSAGE_TYPE_FINISH, this.operationId, msg, this.session);
		} catch (Exception error) {
			RemoteOperationDelegate.logger.error(null, error);
		}
	}

	public IRemoteOperation getOperation() {
		return this.operation;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.remoteoperation.IRemoteOperationListener #onOperationError(java.lang.Object)
	 */
	@Override
	public void onOperationError(Object result) {
		this.status = RemoteOperationStatus.FINISHED;
		RemoteOperationFinishMessage msg = new RemoteOperationFinishMessage(true, result);
		try {
			this.webSocketHandler.sendMessage(RemoteOperationStatuses.WEBSOCKET_MESSAGE_TYPE_FINISH, this.operationId, msg, this.session);
		} catch (Exception e) {
			RemoteOperationDelegate.logger.error(null, e);
			this.error(e);
		}
	}

	private void error(Exception error) {
		try {
			this.webSocketHandler.sendMessage(RemoteOperationStatuses.WEBSOCKET_MESSAGE_TYPE_ERROR, this.operationId, new RemoteOperationErrorMessage(error), this.session);
		} catch (Exception e) {
			RemoteOperationDelegate.logger.error(null, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.remoteoperation.IRemoteOperationListener #onOperationStep(int, int, java.lang.String, java.lang.Object)
	 */
	@Override
	public void onOperationStep(int currentStep, int maxSteps, long estimatedTime, String message, Object extraInformation) {
		RemoteOperationStatusMessage msg = new RemoteOperationStatusMessage(this.status, currentStep, maxSteps, estimatedTime, message, extraInformation);
		try {
			this.webSocketHandler.sendMessage(RemoteOperationStatuses.WEBSOCKET_MESSAGE_TYPE_STATUS, this.operationId, msg, this.session);
		} catch (Exception error) {
			RemoteOperationDelegate.logger.error(null, error);
			this.error(error);
		}
	}

}

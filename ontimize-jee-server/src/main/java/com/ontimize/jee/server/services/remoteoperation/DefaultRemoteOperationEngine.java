package com.ontimize.jee.server.services.remoteoperation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.ontimize.jee.common.callback.CallbackWrapperMessage;
import com.ontimize.jee.common.services.remoteoperation.RemoteOperationCancelMessage;
import com.ontimize.jee.common.services.remoteoperation.RemoteOperationRequestMessage;
import com.ontimize.jee.common.services.remoteoperation.RemoteOperationStatuses;
import com.ontimize.jee.server.callback.CallbackSession;
import com.ontimize.jee.server.callback.ICallbackEventListener;
import com.ontimize.jee.server.callback.ICallbackHandler;

/**
 * The Class DefaultRemoteOperationManager.
 */
public class DefaultRemoteOperationEngine implements IRemoteOperationEngine, InitializingBean, ICallbackEventListener {

	/** The logger. */
	private static Logger											logger	= LoggerFactory.getLogger(DefaultRemoteOperationEngine.class);
	private final ThreadPoolExecutor								executorService;

	/** The sessions. */
	private final Map<RemoteOperationId, RemoteOperationDelegate>	sessions;

	/** The running sessions. */
	@Autowired(required = false)
	private ICallbackHandler										callbackHandler;
	@Autowired
	private ApplicationContext										context;

	/**
	 * Instantiates a new default remote operation manager.
	 */
	public DefaultRemoteOperationEngine() {
		this.sessions = new HashMap<>();
		this.executorService = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.callbackHandler == null) {
			DefaultRemoteOperationEngine.logger.warn("No websocket handler defined, remoteoperation progress will not work!");
		} else {
			this.callbackHandler.addCallbackEventListener(this);
		}
	}

	@Override
	public void onCallbackMessageReceived(CallbackSession from, CallbackWrapperMessage message) {
		if (!RemoteOperationStatuses.REMOTE_OPERATION_MESSAGE_TYPE_RANGE.contains(message.getType())) {
			return;
		}
		RemoteOperationDelegate delegate = this.sessions.get(new RemoteOperationId(from, message.getSubtype()));

		if (RemoteOperationStatuses.WEBSOCKET_MESSAGE_TYPE_REQUEST.equals(message.getType())) {
			RemoteOperationRequestMessage requestMessage = message.getMessage(RemoteOperationRequestMessage.class);
			delegate = new RemoteOperationDelegate(this.context, from, message.getSubtype(), requestMessage.getOperationClassName(), requestMessage.getParameters());
			this.sessions.put(new RemoteOperationId(from, message.getSubtype()), delegate);
			this.executorService.submit(new RemoteOperationTask(delegate));

		} else if (RemoteOperationStatuses.WEBSOCKET_MESSAGE_TYPE_CANCEL.equals(message.getType())) {
			RemoteOperationCancelMessage cancelMessage = message.getMessage(RemoteOperationCancelMessage.class);
			delegate.onCancel(cancelMessage);

		} else {
			delegate.onCustomMessage(message);
		}
	}

	/**
	 * Sets the max running thread number.
	 *
	 * @param maxRunningThreadNumber
	 *            the new max running thread number
	 */
	@Override
	public void setMaxRunningThreadNumber(int maxRunningThreadNumber) {
		this.executorService.setCorePoolSize(maxRunningThreadNumber);
		this.executorService.setMaximumPoolSize(maxRunningThreadNumber);
	}

	/**
	 * Gets the max running thread number.
	 *
	 * @return the max running thread number
	 */
	public int getMaxRunningThreadNumber() {
		return this.executorService.getMaximumPoolSize();
	}

	public static class RemoteOperationId {

		private final CallbackSession	session;
		private final String			id;

		public RemoteOperationId(CallbackSession session, String id) {
			super();
			this.session = session;
			this.id = id;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + (this.id == null ? 0 : this.id.hashCode());
			result = (prime * result) + (this.session == null ? 0 : this.session.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (this.getClass() != obj.getClass()) {
				return false;
			}
			RemoteOperationId other = (RemoteOperationId) obj;
			if (this.id == null) {
				if (other.id != null) {
					return false;
				}
			} else if (!this.id.equals(other.id)) {
				return false;
			}
			if (this.session == null) {
				if (other.session != null) {
					return false;
				}
			} else if (!this.session.equals(other.session)) {
				return false;
			}
			return true;
		}

	}

	public static class RemoteOperationTask implements Runnable {

		private final RemoteOperationDelegate delegate;

		public RemoteOperationTask(RemoteOperationDelegate delegate) {
			super();
			this.delegate = delegate;
		}

		@Override
		public void run() {
			Thread.currentThread().setName("RemoteOperation - " + this.delegate.getOperation().getClass().getName());
			this.delegate.execute();
		}

	}
}

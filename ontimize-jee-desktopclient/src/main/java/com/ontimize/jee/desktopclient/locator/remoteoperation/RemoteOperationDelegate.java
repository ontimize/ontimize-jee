package com.ontimize.jee.desktopclient.locator.remoteoperation;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.common.callback.CallbackWrapperMessage;
import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import com.ontimize.jee.common.services.remoteoperation.RemoteOperationCancelMessage;
import com.ontimize.jee.common.services.remoteoperation.RemoteOperationErrorMessage;
import com.ontimize.jee.common.services.remoteoperation.RemoteOperationFinishMessage;
import com.ontimize.jee.common.services.remoteoperation.RemoteOperationRequestMessage;
import com.ontimize.jee.common.services.remoteoperation.RemoteOperationStatusMessage;
import com.ontimize.jee.common.services.remoteoperation.RemoteOperationStatuses;
import com.ontimize.jee.common.tools.Pair;
import com.ontimize.jee.desktopclient.callback.ICallbackClientHandler;
import com.ontimize.jee.desktopclient.callback.ICallbackEventListener;
import com.ontimize.jee.desktopclient.spring.BeansFactory;

/**
 * The Class RemoteOperationDelegate.
 */
public class RemoteOperationDelegate implements ICallbackEventListener {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(RemoteOperationDelegate.class);

    /** The class name. */
    private final String className;

    /** The parameters. */
    private final Map<String, Object> parameters;

    /** The listener. */
    private final IRemoteOperationListener<?> listener;

    /** The finished. */
    private final String operationId;

    private final ICallbackClientHandler webSocketClientHandler;

    /**
     * Instantiates a new remote operation delegate.
     * @param className the class name
     * @param parameters the parameters
     * @param listener the listener
     */
    public RemoteOperationDelegate(String operationId, String className, Map<String, Object> parameters,
            IRemoteOperationListener listener) {
        super();
        this.operationId = operationId;
        this.className = className;
        this.parameters = parameters;
        this.listener = listener;
        this.webSocketClientHandler = BeansFactory.getBean(ICallbackClientHandler.class);
    }

    public void run() {
        this.webSocketClientHandler.addCallbackEventListener(this);
        try {
            this.webSocketClientHandler.sendMessage(RemoteOperationStatuses.WEBSOCKET_MESSAGE_TYPE_REQUEST,
                    this.operationId,
                    new RemoteOperationRequestMessage(this.className, this.parameters));
        } catch (Exception error) {
            this.close();
            RemoteOperationDelegate.logger.error(null, error);
            if (this.listener != null) {
                this.listener.onError(error);
            }
        }
    }

    private void close() {
        this.webSocketClientHandler.removeCallbackEventListener(this);
    }

    @Override
    public void onCallbackMessageReceived(CallbackWrapperMessage message) {
        if (this.operationId.equals(message.getSubtype())) {
            if (RemoteOperationStatuses.WEBSOCKET_MESSAGE_TYPE_STATUS.equals(message.getType())) {
                RemoteOperationStatusMessage statusMessage = message.getMessage(RemoteOperationStatusMessage.class);
                if (this.listener != null) {
                    this.listener.onStatusUpdated(statusMessage);
                }

            } else if (RemoteOperationStatuses.WEBSOCKET_MESSAGE_TYPE_FINISH.equals(message.getType())) {
                RemoteOperationFinishMessage finishMessage = message.getMessage(RemoteOperationFinishMessage.class);
                this.close();
                if (this.listener != null) {
                    this.listener.onFinish(finishMessage);
                }

            } else if (RemoteOperationStatuses.WEBSOCKET_MESSAGE_TYPE_ERROR.equals(message.getType())) {
                RemoteOperationErrorMessage errorMessage = message.getMessage(RemoteOperationErrorMessage.class);
                if (this.listener != null) {
                    this.listener.onError(errorMessage.getCause());
                }
                this.close();
            } else {
                Pair<Integer, Object> response = this.listener.onUnknowMessage(message);
                if (response != null) {
                    try {
                        this.webSocketClientHandler.sendMessage(response.getFirst(), this.operationId,
                                response.getSecond());
                    } catch (Exception error) {
                        RemoteOperationDelegate.logger.error(null, error);
                    }
                }
            }
        }
    }

    /**
     * Cancel.
     * @throws OntimizeJEEException
     */
    public void cancel() throws OntimizeJEEException {
        this.webSocketClientHandler.sendMessage(RemoteOperationStatuses.WEBSOCKET_MESSAGE_TYPE_CANCEL, this.operationId,
                new RemoteOperationCancelMessage());
    }

}

package com.ontimize.jee.desktopclient.locator.remoteoperation;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.jee.common.callback.CallbackWrapperMessage;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.services.remoteoperation.RemoteOperationFinishMessage;
import com.ontimize.jee.common.services.remoteoperation.RemoteOperationStatusMessage;
import com.ontimize.jee.common.tools.Pair;
import com.ontimize.jee.desktopclient.components.task.OSwingWorker;
import com.ontimize.jee.desktopclient.components.task.WorkerStatusInfo;
import com.ontimize.locator.EntityReferenceLocator;
import com.ontimize.locator.UtilReferenceLocator;
import com.ontimize.util.operation.RemoteOperationManager;

/**
 * The Class AbstractRemoteOperationSwingWorker.
 *
 * @param <T> the generic type
 * @param <V> the value type
 */
public class RemoteOperationSwingWorker<T, V> extends OSwingWorker<T, V> {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(RemoteOperationSwingWorker.class);

    /** The delegate. */
    private RemoteOperationDelegate delegate;

    /** The remote class name. */
    protected String remoteClassName;

    /** The parameters. */
    protected Map<String, Object> parameters;

    /**
     * Instantiates a new abstract remote operation swing worker.
     * @param remoteClassName the remote class name
     * @param parameters the parameters
     */
    public RemoteOperationSwingWorker(String remoteClassName, Map<String, Object> parameters) {
        super();
        this.remoteClassName = remoteClassName;
        this.parameters = parameters;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    protected T doInBackground() throws Exception {
        EntityReferenceLocator referenceLocator = ApplicationManager.getApplication().getReferenceLocator();
        int sessionId = referenceLocator.getSessionId();
        RemoteOperationManager manager = ((UtilReferenceLocator) referenceLocator).getRemoteOperationManager(sessionId);
        IRemoteOperationListener<T> remoteOperationListener = new RemoteOperationListener<>();
        synchronized (remoteOperationListener) {
            this.delegate = ((WebsocketRemoteOperationManager) manager).run(this.remoteClassName, this.parameters,
                    sessionId, remoteOperationListener);
            // free memory
            this.parameters = null;
            remoteOperationListener.wait();
            return remoteOperationListener.getResult();
        }
    }

    /**
     * Update status.
     * @param status the status
     */
    public void updateStatus(RemoteOperationStatusMessage status) {
        RemoteOperationSwingWorker.this
            .fireStatusUpdate(new WorkerStatusInfo(status.getMessage(), status.getEstimatedTime(),
                    status.getCurrentStep() < 0 ? 0
                            : ((Double) ((status.getCurrentStep() / (double) status.getMaxSteps()) * 100)).intValue()));
    }

    /**
     * Unknow message received.
     * @param message the message
     * @return the object
     */
    public Pair<Integer, Object> unknowMessageReceived(CallbackWrapperMessage message) {
        return null;
    }

    /**
     * Gets the remote class name.
     * @return the remote class name
     */
    public String getRemoteClassName() {
        return this.remoteClassName;
    }

    /**
     * Gets the delegate.
     * @return the delegate
     */
    public RemoteOperationDelegate getDelegate() {
        return this.delegate;
    }

    /**
     * The listener interface for receiving remoteOperation events. The class that is interested in
     * processing a remoteOperation event implements this interface, and the object created with that
     * class is registered with a component using the component's <code>addRemoteOperationListener<code>
     * method. When the remoteOperation event occurs, that object's appropriate method is invoked.
     *
     * @param <T> the generic type
     * @see RemoteOperationEvent
     */
    protected class RemoteOperationListener<U> implements IRemoteOperationListener<U> {

        /** The result. */
        private U result;

        /** The error. */
        private Throwable error;

        /*
         * (non-Javadoc)
         *
         * @see
         * com.ontimize.jee.desktopclient.locator.remoteoperation.IRemoteOperationListener#onUnknowMessage(
         * java.lang.Object)
         */
        @Override
        public Pair<Integer, Object> onUnknowMessage(CallbackWrapperMessage message) {
            return RemoteOperationSwingWorker.this.unknowMessageReceived(message);
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * com.ontimize.jee.desktopclient.locator.remoteoperation.IRemoteOperationListener#onStatusUpdated(
         * com.ontimize.jee.common.services.remoteoperation .RemoteOperationStatusMessage)
         */
        @Override
        public void onStatusUpdated(RemoteOperationStatusMessage status) {
            RemoteOperationSwingWorker.this.updateStatus(status);
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * com.ontimize.jee.desktopclient.locator.remoteoperation.IRemoteOperationListener#onError(java.lang
         * .Throwable)
         */
        @Override
        public void onError(Throwable t) {
            RemoteOperationSwingWorker.logger.error(null, t);
            this.error = t;
            synchronized (this) {
                this.notify();
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * com.ontimize.jee.desktopclient.locator.remoteoperation.IRemoteOperationListener#onFinish(com.
         * ontimize.jee.common.services.remoteoperation. RemoteOperationFinishMessage)
         */
        @Override
        public void onFinish(RemoteOperationFinishMessage status) {
            if (status.getResult() instanceof Throwable) {
                this.error = (Throwable) status.getResult();
                RemoteOperationSwingWorker.logger.error(null, this.error);
            } else {
                this.result = (U) status.getResult();
            }
            synchronized (this) {
                this.notify();
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see com.ontimize.jee.desktopclient.locator.remoteoperation.IRemoteOperationListener#getResult()
         */
        @Override
        public U getResult() throws OntimizeJEERuntimeException {
            if (this.error != null) {
                if (this.error instanceof OntimizeJEERuntimeException) {
                    throw (OntimizeJEERuntimeException) this.error;
                }
                throw new OntimizeJEERuntimeException(this.error);
            }
            return this.result;
        }

    }

}

package com.ontimize.jee.desktopclient.locator.remoteoperation;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.util.operation.RemoteOperationManager;

/**
 * The Class RemoteOperationManagerProxyHandler.
 */
public class WebsocketRemoteOperationManager implements RemoteOperationManager {

    private static final AtomicLong ID_GENERATOR = new AtomicLong();

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(WebsocketRemoteOperationManager.class);

    /**
     * Instantiates a new remote operation manager proxy handler.
     */
    public WebsocketRemoteOperationManager() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.util.operation.RemoteOperationManager#run(java.lang.String, java.util.HashMap,
     * int)
     */
    @Override
    public String run(final String clase, final HashMap parameters, int sessionId) {
        throw new OntimizeJEERuntimeException("Cast to WebsocketRemoteOperationManager and use listener");
    }

    /**
     * Run.
     * @param clase the clase
     * @param parameters the parameters
     * @param sessionId the session id
     * @param listener the listener
     * @return the remote operation delegate
     * @throws Exception the exception
     */
    public RemoteOperationDelegate run(final String clase, final Map<String, Object> parameters, int sessionId,
            IRemoteOperationListener<?> listener) {
        RemoteOperationDelegate delegate = new RemoteOperationDelegate(
                String.valueOf(WebsocketRemoteOperationManager.ID_GENERATOR.incrementAndGet()), clase, parameters,
                listener);
        delegate.run();
        return delegate;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.util.operation.RemoteOperationManager#hasRequired(java.lang .String, int)
     */
    @Deprecated
    @Override
    public boolean hasRequired(String token, int sessionId) throws Exception {
        throw new OntimizeJEERuntimeException("Use delegate");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.util.operation.RemoteOperationManager#getRequired(java.lang .String, int)
     */
    @Deprecated
    @Override
    public HashMap getRequired(String token, int sessionId) throws Exception {
        throw new OntimizeJEERuntimeException("Use delegate");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.util.operation.RemoteOperationManager#setRequired(java.lang .String,
     * java.util.HashMap, int)
     */
    @Deprecated
    @Override
    public void setRequired(String token, HashMap required, int sessionId) throws Exception {
        throw new OntimizeJEERuntimeException("Use delegate");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.util.operation.RemoteOperationManager#cancel(java.lang.String , int)
     */
    @Deprecated
    @Override
    public void cancel(String token, int sessionId) throws Exception {
        throw new OntimizeJEERuntimeException("Use delegate");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.util.operation.RemoteOperationManager#isFinished(java.lang .String, int)
     */
    @Deprecated
    @Override
    public boolean isFinished(String token, int sessionId) throws Exception {
        throw new OntimizeJEERuntimeException("Use listener");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.util.operation.RemoteOperationManager#getResult(java.lang .String, int)
     */
    @Deprecated
    @Override
    public Hashtable getResult(String token, int sessionId) throws Exception {
        throw new OntimizeJEERuntimeException("Use listener");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.util.operation.RemoteOperationManager#getStatus(java.lang .String, int)
     */
    @Deprecated
    @Override
    public int getStatus(String token, int sessionId) throws Exception {
        throw new OntimizeJEERuntimeException("Use listener");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.util.operation.RemoteOperationManager#
     * getCurrentExecutionInformation(java.lang.String, int)
     */
    @Deprecated
    @Override
    public Hashtable getCurrentExecutionInformation(String token, int sessionId) throws Exception {
        throw new OntimizeJEERuntimeException("Use listener");
    }

}

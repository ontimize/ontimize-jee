package com.ontimize.jee.desktopclient.locator.remoteoperation;

import com.ontimize.jee.common.callback.CallbackWrapperMessage;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.services.remoteoperation.RemoteOperationFinishMessage;
import com.ontimize.jee.common.services.remoteoperation.RemoteOperationStatusMessage;
import com.ontimize.jee.common.tools.Pair;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving IRemoteOperation events. The class that is interested in
 * processing a IRemoteOperation event implements this interface, and the object created with that
 * class is registered with a component using the component's
 * <code>addIRemoteOperationListener<code> method. When the IRemoteOperation event occurs, that
 * object's appropriate method is invoked.
 *
 * @param <T> the result type
 * @see IRemoteOperationEvent
 */
public interface IRemoteOperationListener<T> {

    /**
     * Invoked when on status update occurs.
     * @param status the status
     */
    void onStatusUpdated(RemoteOperationStatusMessage status);

    /**
     * Inoked when on error occurs.
     * @param t the error
     */
    void onError(Throwable t);

    /**
     * Inoked when an unknow message arrives.
     * @param message the response mesage
     * @return the message type and de response Object
     */
    Pair<Integer, Object> onUnknowMessage(CallbackWrapperMessage message);

    /**
     * On finish.
     * @param status the status
     */
    void onFinish(RemoteOperationFinishMessage status);

    /**
     * Debe devolver el resultado de la operación o lanzar una excepción en caso de error.
     * @return the result
     * @throws OntimizeJEERuntimeException the ontimize jee exception
     */
    T getResult() throws OntimizeJEERuntimeException;

}

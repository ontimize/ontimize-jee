package com.ontimize.jee.desktopclient.callback;

import com.ontimize.jee.common.callback.CallbackWrapperMessage;

/**
 * The listener interface for receiving IWebSocketEvent events. The class that is interested in
 * processing a IWebSocketEvent event implements this interface, and the object created with that
 * class is registered with a component using the component's <code>addIWebSocketEventListener<code>
 * method. When the IWebSocketEvent event occurs, that object's appropriate method is invoked.
 *
 * @see IWebSocketEventEvent
 */
public interface ICallbackEventListener {

    /**
     * On web socket message.
     * @param type the type
     * @param message the message
     */
    void onCallbackMessageReceived(CallbackWrapperMessage message);

}

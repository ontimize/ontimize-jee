package com.ontimize.jee.desktopclient.components.messaging;

import java.util.Map;

import com.ontimize.jee.common.tools.MessageType;

/**
 * Using this interface can be listen to user messages (dialog messages) lanched by UFormExt, UForm
 * and UException.
 *
 * @see IMessageEvent
 */
public interface IMessageListener {

    /**
     * New message.
     * @param source the source
     * @param message the message
     * @param messageType the message type
     * @param extraData the extra data
     */
    void newMessage(Object source, String message, MessageType messageType, Map<?, ?> extraData);

}

package com.ontimize.jee.desktopclient.components.messaging;

import java.awt.Component;

import org.slf4j.Logger;

import com.ontimize.jee.common.tools.MessageType;

/**
 * The Class UMessageManager.
 */
public interface IMessageManager {

    void showExceptionMessage(final Throwable error, final Logger logger);

    void showExceptionMessage(final Throwable error, final Logger logger, String messageIfNullMessage);

    void showExceptionMessage(Throwable error, Component parent, final Logger logger);

    void showExceptionMessage(Throwable error, Component parent, final Logger logger, String messageIfNullMessage);

    Throwable getCauseException(Throwable error);

    int showMessage(Component parent, String untranslatedMessage, MessageType messageType, Object args[]);

    int showMessage(Component parent, String untranslatedMessage, MessageType messageType, Object args[],
            boolean blocking);

    int showMessage(Component parent, String translatedMessage, MessageType messageType);

    int showMessage(Component parent, String translatedMessage, MessageType messageType, boolean blocking);

    int showMessage(Component parent, String untranslatedMessage, String untranslatedDetail, MessageType messageType,
            Object args[]);

    int showMessage(Component parent, String untranslatedMessage, String untranslatedDetail, MessageType messageType,
            Object args[], boolean blocking);

    int showMessage(Component parent, String translatedMessage, String translatedDetail, MessageType messageType);

    int showMessage(Component parent, String translatedMessage, String translatedDetail, MessageType messageType,
            boolean blocking);

    void registerMessageListener(IMessageListener listener);

    void removeMessageListener(IMessageListener listener);

}

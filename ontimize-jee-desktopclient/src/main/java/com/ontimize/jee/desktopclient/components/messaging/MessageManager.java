/*
 *
 */
package com.ontimize.jee.desktopclient.components.messaging;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.MessageDialog;
import com.ontimize.jee.common.exceptions.IParametrizedException;
import com.ontimize.jee.common.tools.MessageType;
import com.ontimize.jee.common.tools.ObjectWrapper;
import com.ontimize.jee.desktopclient.components.SwingTools;
import com.ontimize.jee.desktopclient.components.WindowTools;

/**
 * The Class UMessageManager.
 */
public class MessageManager implements IMessageManager {

    private static final Logger logger = LoggerFactory.getLogger(MessageManager.class);

    /** The use advanced messaging. */
    public static boolean USE_ADVANCED_MESSAGING = false;

    /** The message listeners. */
    protected List<IMessageListener> messageListeners;

    /**
     * Instantiates a new UMessageManager.
     */
    public MessageManager() {
        super();
    }

    /** The manager. */
    private static IMessageManager manager;

    /**
     * Gets the message manager.
     * @return the message manager
     */
    public static IMessageManager getMessageManager() {
        if (MessageManager.manager == null) {
            MessageManager.manager = new MessageManager();
        }
        return MessageManager.manager;
    }

    /**
     * Sets the message manager.
     * @param manager the new message manager
     */
    public static void setMessageManager(IMessageManager manager) {
        MessageManager.manager = manager;
    }

    /**
     * Muestra el mensaje de la excepcion al usuario.
     * @param error the error
     * @param logger the logger
     */
    @Override
    public void showExceptionMessage(final Throwable error, final Logger logger) {
        this.showExceptionMessage(error, logger, null);
    }

    /**
     * Show exception message.
     * @param error the t
     * @param logger the logger
     * @param messageIfNullMessage the message if null message
     */
    @Override
    public void showExceptionMessage(final Throwable error, final Logger logger, String messageIfNullMessage) {
        this.showExceptionMessage(error, WindowTools.getActiveWindow(), logger, messageIfNullMessage);
    }

    @Override
    public void showExceptionMessage(Throwable error, Component parent, final Logger logger) {
        this.showExceptionMessage(error, parent, logger, null);
    }

    /**
     * Show message.
     * @param error the t
     * @param parent the parent
     * @param logger the logger
     */
    @Override
    public void showExceptionMessage(Throwable error, Component parent, final Logger logger,
            String messageIfNullMessage) {
        if (logger != null) {
            MessageManager.logger.error(null, error);
        }

        if (this.isSilent(error)) {
            return;
        }
        Throwable causeError = this.getCauseException(error);

        String untranslatedMessage = causeError.getMessage();
        Object[] messageParameters = this.getMessageParameters(causeError);
        MessageType messageType = this.getMessageType(causeError);
        boolean blocking = this.isMessageBlocking(causeError);
        if (untranslatedMessage == null) {
            untranslatedMessage = messageIfNullMessage;
        }
        this.showMessage(parent, untranslatedMessage, messageType, messageParameters, blocking);
    }

    /**
     * Gets the cause exception.
     * @param error the error
     * @return the cause exception
     */
    @Override
    public Throwable getCauseException(Throwable error) {
        if ((error instanceof InvocationTargetException)
                && (((InvocationTargetException) error).getTargetException() != null)) {
            return this.getCauseException(((InvocationTargetException) error).getTargetException());
        }

        if (this.containsError(error, SocketException.class)) {
            return new Exception("E_CONNECTING_TO_SERVER", error);
        }
        return error;
    }

    protected boolean containsError(Throwable error, Class classToLookFor) {
        while (error != null) {
            if (classToLookFor.isInstance(error)) {
                return true;
            }
            if (error.getCause() != null) {
                error = error.getCause();
            } else {
                error = null;
            }
        }
        return false;
    }

    /**
     * Show message.
     * @param parent the parent
     * @param untranslatedMessage the untranslated message
     * @param messageType the message type
     * @param args the args
     * @return the int
     */
    @Override
    public int showMessage(Component parent, String untranslatedMessage, MessageType messageType, Object args[]) {
        String translatedMessage = this.translateMessage(parent, untranslatedMessage, args);
        return this.showMessage(parent, translatedMessage, messageType);

    }

    /**
     * Show message.
     * @param parent the parent
     * @param untranslatedMessage the untranslated message
     * @param messageType the message type
     * @param args the args
     * @param blocking the blocking
     * @return the int
     */
    @Override
    public int showMessage(Component parent, String untranslatedMessage, MessageType messageType, Object args[],
            boolean blocking) {
        String translatedMessage = this.translateMessage(parent, untranslatedMessage, args);
        return this.showMessage(parent, translatedMessage, messageType, blocking);

    }

    /**
     * Show message.
     * @param parent the parent
     * @param translatedMessage the translated message
     * @param messageType the message type
     * @return the int
     */
    @Override
    public int showMessage(Component parent, String translatedMessage, MessageType messageType) {
        return this.showMessage(parent, translatedMessage, messageType, this.isMessageBlocking(messageType));
    }

    /**
     * Show message.
     * @param parent the parent
     * @param translatedMessage the translated message
     * @param messageType the message type
     * @param blocking the blocking
     * @return the int
     */
    @Override
    public int showMessage(Component parent, String translatedMessage, MessageType messageType, boolean blocking) {
        return this.showMessage(parent, translatedMessage, null, messageType, blocking);
    }

    /**
     * Show message.
     * @param parent the parent
     * @param untranslatedMessage the untranslated message
     * @param messageType the message type
     * @param args the args
     * @return the int
     */
    @Override
    public int showMessage(Component parent, String untranslatedMessage, String untranslatedDetail,
            MessageType messageType, Object args[]) {
        String translatedMessage = this.translateMessage(parent, untranslatedMessage, args);
        String translatedDetail = this.translateMessage(parent, untranslatedDetail, args);
        return this.showMessage(parent, translatedMessage, translatedDetail, messageType);

    }

    /**
     * Show message.
     * @param parent the parent
     * @param untranslatedMessage the untranslated message
     * @param messageType the message type
     * @param args the args
     * @param blocking the blocking
     * @return the int
     */
    @Override
    public int showMessage(Component parent, String untranslatedMessage, String untranslatedDetail,
            MessageType messageType, Object args[], boolean blocking) {
        String translatedMessage = this.translateMessage(parent, untranslatedMessage, args);
        String translatedDetail = this.translateMessage(parent, untranslatedDetail, args);
        return this.showMessage(parent, translatedMessage, translatedDetail, messageType, blocking);

    }

    /**
     * Show message.
     * @param parent the parent
     * @param translatedMessage the translated message
     * @param messageType the message type
     * @return the int
     */
    @Override
    public int showMessage(Component parent, String translatedMessage, String translatedDetail,
            MessageType messageType) {
        return this.showMessage(parent, translatedMessage, translatedDetail, messageType,
                this.isMessageBlocking(messageType));
    }

    /**
     * Show message with detail
     * @param parent
     * @param translatedMessage
     * @param translatedDetail
     * @param messageType
     * @param blocking
     * @return
     */
    @Override
    public int showMessage(Component parent, String translatedMessage, String translatedDetail, MessageType messageType,
            boolean blocking) {
        try {
            if (parent == null) {
                parent = WindowTools.getActiveWindow();
            }
            if ((blocking) && (!MessageManager.USE_ADVANCED_MESSAGING || messageType.equals(MessageType.QUESTION))) {
                return this.oldMessageStyle(WindowTools.getWindowAncestor(parent), translatedMessage, messageType,
                        translatedDetail, this.getResourceBundle(parent));
            }
            ImageIcon ico = this.getMessageIcon(messageType);
            ToastMessage toastMessage = new ToastMessage(parent, blocking, translatedMessage, translatedDetail, ico,
                    ToastMessage.MESSAGE_TIME_SHORT, null,
                    blocking ? ToastBlockingPanel.class : ToastNoblockingPanel.class, null, null, null);
            Toast.showMessage(toastMessage);
            return 0;
        } finally {
            this.fireMessageEvent(translatedMessage, messageType, parent);
        }
    }

    /**
     * Shows a {@link MessageDialog} with the information collects from entry parameters.
     * @param w the w
     * @param translatedMessage the translated message
     * @param messageType the type of message to be displayed: <code>ERROR_MESSAGE</code>,
     *        <code>INFORMATION_MESSAGE</code>, <code>WARNING_MESSAGE</code>,
     *        <code>QUESTION_MESSAGE</code>, or <code>PLAIN_MESSAGE</code>
     * @param detail a <code>String</code> with more detailed information that can be shown in the
     *        <code>MessageDialog</code> to press a detail button. If detail parameter is null, the
     *        detail button will not be shown
     * @param bundle the bundle
     * @return an integer indicating the option chosen by the user, or <code>CLOSED_OPTION</code> if the
     *         user closed the dialog
     * @see MessageDialog
     */
    protected int oldMessageStyle(final Window w, final String translatedMessage, final MessageType messageType,
            final String detail, final ResourceBundle bundle) {
        final ObjectWrapper<Integer> wrapper = new ObjectWrapper<>();
        SwingTools.invokeInEDTh(new Runnable() {

            @Override
            public void run() {
                int res = MessageManager.this.oldMessageStyleInEDTh(w, translatedMessage, messageType, detail, bundle);
                wrapper.setValue(res);
            }
        });
        return wrapper.getValue();
    }

    protected int oldMessageStyleInEDTh(Window w, String translatedMessage, MessageType messageType, String detail,
            ResourceBundle bundle) {
        int res = JOptionPane.CLOSED_OPTION;
        if (w instanceof Dialog) {
            switch (messageType) {
                case QUESTION:
                    res = MessageDialog.showMessage((Dialog) w, translatedMessage, detail, JOptionPane.QUESTION_MESSAGE,
                            JOptionPane.YES_NO_OPTION, bundle, (Object[]) null);
                    break;
                case INFORMATION:
                    res = MessageDialog.showMessage((Dialog) w, translatedMessage, detail,
                            JOptionPane.INFORMATION_MESSAGE, bundle, (Object[]) null);
                    break;
                case ERROR:
                    res = MessageDialog.showMessage((Dialog) w, translatedMessage, detail, JOptionPane.ERROR_MESSAGE,
                            bundle, (Object[]) null);
                    break;
                case WARNING:
                    res = MessageDialog.showMessage((Dialog) w, translatedMessage, detail, JOptionPane.WARNING_MESSAGE,
                            bundle, (Object[]) null);
                    break;
            }
        } else if (w instanceof Frame) {
            switch (messageType) {
                case QUESTION:
                    res = MessageDialog.showMessage((Frame) w, translatedMessage, detail, JOptionPane.QUESTION_MESSAGE,
                            JOptionPane.YES_NO_OPTION, bundle, (Object[]) null);
                    break;
                case INFORMATION:
                    res = MessageDialog.showMessage((Frame) w, translatedMessage, detail,
                            JOptionPane.INFORMATION_MESSAGE, bundle, (Object[]) null);
                    break;
                case ERROR:
                    res = MessageDialog.showMessage((Frame) w, translatedMessage, detail, JOptionPane.ERROR_MESSAGE,
                            bundle, (Object[]) null);
                    break;
                case WARNING:
                    res = MessageDialog.showMessage((Frame) w, translatedMessage, detail, JOptionPane.WARNING_MESSAGE,
                            bundle, (Object[]) null);
                    break;
            }
        }
        return res;
    }

    /**
     * Checks if is u exception.
     * @param t the t
     * @return true, if is u exception
     */
    protected boolean isParametrizedException(Throwable t) {
        return t instanceof IParametrizedException;
    }

    /**
     * Checks if is message blocking.
     * @param t the t
     * @return true, if is message blocking
     */
    protected boolean isMessageBlocking(Throwable t) {
        return this.isParametrizedException(t) ? ((IParametrizedException) t).isMessageBlocking() : true;
    }

    /**
     * Checks if is message blocking.
     * @param messageType the message type
     * @return true, if is message blocking
     */
    protected boolean isMessageBlocking(MessageType messageType) {
        boolean blocking = true;
        switch (messageType) {
            case INFORMATION:
                blocking = false;
                break;
            case WARNING:
                blocking = true;
                break;
            case ERROR:
                blocking = true;
                break;
            case QUESTION:
                blocking = true;
        }
        return blocking;
    }

    /**
     * Gets the message icon.
     * @param type the type
     * @return the message icon
     */
    protected ImageIcon getMessageIcon(MessageType type) {
        ImageIcon ico = ToastMessage.ICON_ERROR;
        switch (type) {
            case INFORMATION:
                ico = ToastMessage.ICON_INFORMATION;
                break;
            case WARNING:
                ico = ToastMessage.ICON_WARNING;
                break;
            case ERROR:
                ico = ToastMessage.ICON_ERROR;
                break;
            default:
                ico = ToastMessage.ICON_ERROR;
                break;
        }
        return ico;
    }

    /**
     * Checks if is silent.
     * @param t the t
     * @return true, if is silent
     */
    protected boolean isSilent(Throwable t) {
        return this.isParametrizedException(t) && ((IParametrizedException) t).isSilent();
    }

    /**
     * Gets the message parameters.
     * @param t the t
     * @return the message parameters
     */
    protected Object[] getMessageParameters(Throwable t) {
        return this.isParametrizedException(t) ? ((IParametrizedException) t).getMessageParameters() : null;
    }

    /**
     * Gets the message type form equivalence.
     * @param t the t
     * @return the message type form equivalence
     */
    protected MessageType getMessageType(Throwable t) {
        MessageType messageType = MessageType.ERROR;
        if (this.isParametrizedException(t)) {
            messageType = ((IParametrizedException) t).getMessageType();
        }
        return messageType;
    }

    /**
     * Register message listener.
     * @param listener the listener
     */
    @Override
    public void registerMessageListener(IMessageListener listener) {
        if (this.messageListeners == null) {
            this.messageListeners = new ArrayList<>();
        }
        this.messageListeners.add(listener);
    }

    /**
     * Removes the message listener.
     * @param listener the listener
     */
    @Override
    public void removeMessageListener(IMessageListener listener) {
        if (this.messageListeners != null) {
            this.messageListeners.remove(listener);
        }
    }

    /**
     * Notify message listeners.
     * @param translatedMesage the translated mesage
     * @param messageType the message type
     * @param parent the parent
     */
    protected void fireMessageEvent(String translatedMesage, MessageType messageType, Component parent) {
        if ((this.messageListeners != null) && (this.messageListeners.size() > 0)) {
            Form form = this.tryToGetForm(parent);
            Map<?, ?> dataFieldValues = null;
            if (form != null) {
                try {
                    dataFieldValues = form.getDataFieldValues(true);
                } catch (Exception e) {
                    MessageManager.logger.trace(null, e);
                    // Protect from when some table in mutating
                    dataFieldValues = form.getDataFieldValues(false);
                }
                for (IMessageListener listener : this.messageListeners) {
                    listener.newMessage(form, translatedMesage, messageType, dataFieldValues);
                }
            }
        }
    }

    /**
     * Try to get form.
     * @param component the component
     * @return the form
     */
    protected Form tryToGetForm(Component component) {
        if (component instanceof Window) {
            return this.tryToGetFormTopToDown(component);
        } else {
            return this.tryToGetFormDownToTop(component);
        }
    }

    /**
     * Try to get form down to top.
     * @param component the component
     * @return the form
     */
    protected Form tryToGetFormDownToTop(Component component) {
        if (component == null) {
            return null;
        }
        if (component instanceof Form) {
            return (Form) component;
        }

        Container parent = component.getParent();
        if (parent == null) {
            return null;
        } else {
            return this.tryToGetFormDownToTop(parent);
        }

    }

    /**
     * Try to get form top to down.
     * @param component the component
     * @return the form
     */
    protected Form tryToGetFormTopToDown(Component component) {
        if (component instanceof Form) {
            return (Form) component;
        }
        if (component instanceof Container) {
            Container container = (Container) component;
            Component[] components = container.getComponents();
            for (Component son : components) {
                Form res = this.tryToGetFormTopToDown(son);
                if (res != null) {
                    return res;
                }
            }
        }
        return null;
    }

    /**
     * Gets the resource bundle.
     * @param c the c
     * @return the resource bundle
     */
    protected ResourceBundle getResourceBundle(Component c) {
        Form f = this.tryToGetForm(c);
        if (f != null) {
            return f.getResourceBundle();
        } else {
            return ApplicationManager.getApplicationBundle();
        }
    }

    /**
     * Translate message.
     * @param component the component
     * @param message the message
     * @param args the args
     * @return the string
     */
    protected String translateMessage(Component component, String message, Object[] args) {
        try {
            return ApplicationManager.getTranslation(message, this.getResourceBundle(component), args);
        } catch (Exception e) {
            MessageManager.logger.trace(null, e);
            return message;
        }
    }

    /**
     * Translate message type.
     * @param oldMsgType the old msg type
     * @return the message type
     */
    public static MessageType translateMessageType(int oldMsgType) {
        MessageType res = MessageType.INFORMATION;
        switch (oldMsgType) {
            case Form.ERROR_MESSAGE:
                res = MessageType.ERROR;
                break;
            case Form.WARNING_MESSAGE:
                res = MessageType.WARNING;
                break;
            case Form.INFORMATION_MESSAGE:
                res = MessageType.INFORMATION;
                break;
            case Form.QUESTION_MESSAGE:
                res = MessageType.QUESTION;
                break;
        }
        return res;
    }

}

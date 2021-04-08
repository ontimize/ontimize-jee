package com.ontimize.jee.desktopclient.components.messaging;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.i18n.Internationalization;
import com.ontimize.jee.desktopclient.components.WindowTools;
import com.ontimize.util.AWTUtilities;

/**
 * Muestra información al usuario al estilo del Toast de android. Se muestra una ventana que no
 * bloquea la interacción del usuario y desaparece automáticamente pasado un tiempo.
 */
public class Toast implements Runnable, ActionListener {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(Toast.class);

    /** Mantiene la lista de mensajes que se van encolando para presentar. */
    protected ArrayList<ToastMessage> messageList;

    /** Hilo que se encarga de consultar la cola de mensajes y mostrar la ventana. */
    protected Thread toastThread;

    /** Temporizador para cerrar automáticamente la ventana. */
    protected Timer hideToastTimer;

    /** The current dialog. */
    protected JDialog currentDialog;

    /**
     * Instantiates a new u toast.
     */
    public Toast() {
        super();
        this.messageList = new ArrayList<>();
        this.hideToastTimer = new Timer(0, this);
        this.start();
    }

    /**
     * Inicia el hilo de atención de mensajes.
     */
    protected void start() {
        if (this.toastThread == null) {
            this.toastThread = new Thread(this, "Toast Thread");
            this.toastThread.start();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        while (true) {
            try {
                final ToastMessage msg = this.getNextMessage();
                final AbstractToastPanel panel = this.getPanel(msg);
                final JDialog currentDialog = this.createCurrentDialog(panel,
                        msg.isBlocking() ? ModalityType.APPLICATION_MODAL : ModalityType.MODELESS,
                        msg.getWindowBounds(),
                        msg.getParentWindow());

                // Set text content and bundle it
                panel.setMessage(msg);
                if (panel instanceof Internationalization) {
                    ((Internationalization) panel).setResourceBundle(ApplicationManager.getApplicationBundle());
                }

                // Show Dialog
                if (msg.isBlocking()) {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            Toast.this.establishCurrentDialogSize(currentDialog, panel, msg);
                            currentDialog.setVisible(true);
                        }
                    });
                } else {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            Toast.this.establishCurrentDialogSize(currentDialog, panel, msg);
                            Toast.this.setToastVisibleWithAnimation(currentDialog, true);
                        }
                    });
                    this.hideToastTimer.setInitialDelay(msg.getAnimationTime() + (13 * 80));
                    synchronized (this.hideToastTimer) {
                        this.hideToastTimer.restart();
                        this.hideToastTimer.wait();
                    }
                }
                this.disposeDialog(currentDialog);
            } catch (Exception e) {
                Toast.logger.error(null, e);
            }
        }
    }

    /**
     * Creates the current dialog.
     * @param content the content
     * @param modalityType the modality type
     * @param bounds the bounds
     * @param parent the parent
     * @return
     */
    protected JDialog createCurrentDialog(JComponent content, ModalityType modalityType, Rectangle bounds,
            Window parent) {
        Window parentWindow = parent == null ? WindowTools.getActiveWindow() : parent;

        JDialog dialog = new JDialog(modalityType != ModalityType.MODELESS ? parentWindow : null);
        ((JComponent) dialog.getContentPane()).setOpaque(true);
        ((JComponent) dialog.getContentPane()).setBackground(Color.black);
        dialog.setUndecorated(true);
        dialog.setAlwaysOnTop(true);
        dialog.getContentPane().setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().add(content, BorderLayout.CENTER);
        AWTUtilities.setWindowShape(dialog,
                new RoundRectangle2D.Float(0, 0, dialog.getWidth(), dialog.getHeight(), 5, 5));

        dialog.setModalityType(modalityType);

        this.currentDialog = dialog;
        return dialog;
    }

    /**
     * Dispose current dialog.
     */
    protected void disposeDialog(JDialog currentDialog) {
        Component oldPanel = ((BorderLayout) currentDialog.getContentPane().getLayout())
            .getLayoutComponent(BorderLayout.CENTER);
        if (oldPanel != null) {
            currentDialog.getContentPane().remove(oldPanel);
        }
        currentDialog.dispose();
    }

    protected void establishCurrentDialogSize(JDialog currentDialog, AbstractToastPanel panel, ToastMessage msg) {
        if (msg.getWindowBounds() == null) {
            currentDialog.pack();
        } else {
            currentDialog.setBounds(msg.getWindowBounds());
        }
        currentDialog.setLocationRelativeTo(msg.getParentWindow());
    }

    /**
     * Gets the panel from cache panel.
     * @param msg the msg
     * @return the panel from cache panel
     * @throws IllegalArgumentException the illegal argument exception
     * @throws SecurityException the security exception
     * @throws InstantiationException the instantiation exception
     * @throws IllegalAccessException the illegal access exception
     * @throws InvocationTargetException the invocation target exception
     * @throws NoSuchMethodException the no such method exception
     */
    protected AbstractToastPanel getPanel(ToastMessage msg)
            throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        return (AbstractToastPanel) msg.getPanelClass().getConstructor().newInstance();
    }

    /**
     * Se invoca al caducar el timer para cerrar la ventana.
     * @param e the e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        this.hideToastTimer.stop();
        // Hacemos que la ventana desaparezca suavemente
        this.setToastVisibleWithAnimation(this.currentDialog, false);
    }

    /**
     * Sets the toast visible with animation.
     * @param visible the new toast visible with animation
     */
    protected synchronized void setToastVisibleWithAnimation(final JDialog currentDialog, final boolean visible) {
        // Vamos a hacer que se desvanezca
        final Timer t = new Timer(0, null);
        final ActionListener ac = new ActionListener() {

            float initialAlfpha = visible ? 0.2f : 0.8f;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (visible) {
                    currentDialog.setVisible(true);
                }
                AWTUtilities.setWindowOpacity(currentDialog, this.initialAlfpha);
                this.initialAlfpha += (visible ? 1 : -1) * 0.05f;
                if ((!visible && (this.initialAlfpha < 0.1f)) || (visible && (this.initialAlfpha > 0.9f))) {
                    t.stop();
                    synchronized (t) {
                        t.notify();
                    }
                }
            }
        };
        t.addActionListener(ac);
        t.setInitialDelay(0);
        t.setDelay(80);

        new Thread(new Runnable() {

            @Override
            public void run() {
                synchronized (t) {
                    try {
                        t.restart();
                        t.wait();
                    } catch (InterruptedException e1) {
                        Toast.logger.trace(null, e1);
                    }
                }
                if (!visible) {
                    currentDialog.setVisible(false);
                    // Avisamos al hilo principal de que puede seguir mostrando mensajes
                    synchronized (Toast.this.hideToastTimer) {
                        Toast.this.hideToastTimer.notify();
                    }
                }
            }

        }, "closing/showing toast thread").start();
    }

    /**
     * Añade un nuevo mensaje para ser visualizado, será el hilo el que lo recoja cuando sea su turno.
     * @param msg the msg
     */
    public void enqueue(ToastMessage msg) {
        if (msg != null) {
            synchronized (this.messageList) {
                this.messageList.add(msg);
                this.messageList.notify();
            }
        }
    }

    /**
     * Devuelve el siguiente mensaje. En caso de que no haya mensajes pendientes, espera.
     * @return the next message
     */
    protected ToastMessage getNextMessage() {
        synchronized (this.messageList) {
            if (this.messageList.size() == 0) {
                try {
                    this.messageList.wait();
                } catch (InterruptedException e) {
                    Toast.logger.error(null, e);
                }
            }
            return this.messageList.remove(0);
        }
    }

    /** The toast. */
    static volatile Toast toast;

    /**
     * Show message.
     * @param msg the msg
     */
    public static void showMessage(ToastMessage msg) {
        if (Toast.toast == null) {
            Toast.toast = new Toast();
        }
        Toast.toast.enqueue(msg);
    }

}

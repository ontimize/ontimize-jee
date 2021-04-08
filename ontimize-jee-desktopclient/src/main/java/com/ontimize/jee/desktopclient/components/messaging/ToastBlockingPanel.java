package com.ontimize.jee.desktopclient.components.messaging;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.i18n.Internationalization;
import com.ontimize.jee.desktopclient.components.WindowTools;

/**
 * The Class UToastBlockingPanel.
 */
public class ToastBlockingPanel extends ToastNoblockingPanel implements ActionListener, Internationalization {

    /** The continue button. */
    protected JButton continueButton;

    /** The button panel. */
    protected JPanel buttonPanel;

    /**
     * Instantiates a new u toast blocking panel.
     */
    public ToastBlockingPanel() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.utilmize.client.gui.toast.UToastNoblockingPanel#init()
     */
    @Override
    protected void init() {
        super.init();
        this.createButtonPanel();

        this.continueButton = new JButton(ApplicationManager.getTranslation("msg.button.ok"));
        this.continueButton.addActionListener(this);
        this.buttonPanel.add(this.continueButton, BorderLayout.CENTER);

        this.add(this.buttonPanel, new GridBagConstraints(0, 2, 2, 1, 1, 0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(2, 0, 3, 3), 0, 0));
    }

    /**
     * Creates the button panel.
     */
    private void createButtonPanel() {
        this.buttonPanel = new JPanel();
        this.buttonPanel.setOpaque(false);
        this.buttonPanel.setLayout(new BorderLayout());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.utilmize.client.gui.toast.UToastNoblockingPanel#getResponse(com.utilmize.client.gui.toast.
     * UToastMessage)
     */
    @Override
    public Object getResponse(ToastMessage message) {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        WindowTools.getWindowAncestor(this).setVisible(false);
    }

    @Override
    public void setComponentLocale(Locale l) {
        // Do nothing
    }

    @Override
    public void setResourceBundle(ResourceBundle resourceBundle) {
        this.continueButton.setText(ApplicationManager.getTranslation("msg.button.ok", resourceBundle));
    }

    @Override
    public Vector getTextsToTranslate() {
        return null;
    }

}

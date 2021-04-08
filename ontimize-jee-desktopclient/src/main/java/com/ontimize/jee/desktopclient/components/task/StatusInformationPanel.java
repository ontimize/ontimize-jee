/*
 *
 */
package com.ontimize.jee.desktopclient.components.task;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.font.FontRenderContext;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.ApplicationManager.CancelOperationDialog;
import com.ontimize.gui.images.ImageManager;

/**
 * The Class StatusInformationPanel.
 */
public class StatusInformationPanel extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(StatusInformationPanel.class);

    /** The font. */
    public static Font font = CancelOperationDialog.font;

    /** The foreground. */
    public static Color foreground = CancelOperationDialog.foreground;

    /** The background. */
    public static Color background = CancelOperationDialog.background;

    /** The border color. */
    public static Color borderColor = CancelOperationDialog.borderColor;

    // Para que cargue la fuente y la primera vez no tarde en mostrarlo
    {
        new Thread("load font thread") {

            @Override
            public void run() {
                StatusInformationPanel.font.createGlyphVector(new FontRenderContext(null, true, true), "_");
            }
        }.start();
    }

    /** The Constant Q_CANCEL_OPERATION. */
    protected static final String Q_CANCEL_OPERATION = "applicationmanager.cancel_operation";

    /** The Constant ESTIMATED_TIME_REMAINING. */
    protected static final String ESTIMATED_TIME_REMAINING = "applicationmanager.estimated_time";

    /** The Constant SECONDS. */
    protected static final String SECONDS = " s";

    /** The Constant UNKNOWN. */
    protected static final String UNKNOWN = "unknown";

    /** The Constant CANCEL. */
    protected static final String CANCEL = "application.cancel";

    /** The alpha. */
    public static float ALPHA = 0.9f;

    /** The cancel button. */
    protected JButton cancelButton;

    /** The state. */
    protected JLabel state;

    /** The estimated time. */
    protected JLabel estimatedTime;

    /** The progress bar. */
    protected JProgressBar progressBar;

    /**
     * Instantiates a new status information panel.
     */
    public StatusInformationPanel() {
        super();
        this.init();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize() {
        return super.getPreferredSize();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JComponent#getMinimumSize()
     */
    @Override
    public Dimension getMinimumSize() {
        return super.getMinimumSize();
    }

    /**
     * Inits the.
     */
    private void init() {
        this.setOpaque(false);
        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StatusInformationPanel.borderColor, 1),
                BorderFactory.createEmptyBorder(20, 10, 0, 10)));

        this.progressBar = new StatusInformationProgressBar();
        this.progressBar.setStringPainted(false);

        this.cancelButton = new JButton(ApplicationManager.getTranslation(StatusInformationPanel.CANCEL));
        this.cancelButton.setMargin(new Insets(0, 2, 0, 2));
        this.cancelButton.setIcon(ImageManager.getIcon(ImageManager.CANCEL));

        this.setBackground(StatusInformationPanel.background);
        this.setOpaque(true);

        JLabel lIco = new JLabel(); // TODO poner un icono de operacion?
        lIco.setBorder(new EmptyBorder(2, 8, 2, 8));

        this.state = new JLabel();
        this.state.setForeground(StatusInformationPanel.foreground);
        this.state.setFont(StatusInformationPanel.font);

        this.estimatedTime = new JLabel();
        this.estimatedTime.setForeground(StatusInformationPanel.foreground);
        this.estimatedTime.setFont(StatusInformationPanel.font);

        this.add(lIco, new GridBagConstraints(0, 0, 1, 2, 0.0D, 0.0D, 10, 0, new Insets(1, 1, 1, 1), 0, 0));
        this.add(this.estimatedTime,
                new GridBagConstraints(1, 0, 2, 1, 1.0D, 0.0D, 13, 0, new Insets(6, 1, 1, 5), 0, 0));
        this.add(this.state, new GridBagConstraints(1, 1, 2, 1, 1.0D, 0.0D, 17, 2, new Insets(5, 1, 5, 1), 0, 0));
        this.add(this.progressBar, new GridBagConstraints(1, 2, 1, 1, 1.0D, 0.0D, 17, 2, new Insets(1, 1, 5, 5), 0, 0));
        this.add(this.cancelButton,
                new GridBagConstraints(0, 3, 2, 1, 0.0D, 0.0D, 10, 0, new Insets(1, 2, 5, 5), 0, 0));

        this.cancelButton.setOpaque(false);
    }

    /**
     * Sets the status.
     * @param info the new status
     */
    public void setStatus(WorkerStatusInfo info) {
        if (info.getState() != null) {
            this.state.setText(ApplicationManager.getTranslation(info.getState()));
        } else {
            this.state.setText("");
        }
        if (info.getEstimatedTime() != null) {
            this.estimatedTime.setText(ApplicationManager
                .getTranslation(StatusInformationPanel.ESTIMATED_TIME_REMAINING) + " "
                    + (int) (info.getEstimatedTime() / 1000D) + StatusInformationPanel.SECONDS);
        } else {
            this.estimatedTime.setText("");
        }
        if (info.getProgress() != null) {
            this.progressBar.setIndeterminate(false);
            this.progressBar.setMaximum(100);
            this.progressBar.setMinimum(0);
            this.progressBar.setValue(info.getProgress());
            this.progressBar.setStringPainted(true);
        } else {
            this.progressBar.setStringPainted(false);
            this.progressBar.setIndeterminate(true);
        }
    }

    /**
     * Sobrecargado para hacer el efecto translúcido.
     * @param g the g
     */
    @Override
    public void paintComponent(java.awt.Graphics g) {
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
        Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, StatusInformationPanel.ALPHA));
        if (this.isBackgroundSet()) {
            Color c = this.getBackground();
            g2.setColor(c);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
        super.paintComponent(g2);
        g2.setComposite(oldComposite);
    }

    /**
     * Gets the cancel button.
     * @return the cancel button
     */
    public AbstractButton getCancelButton() {
        return this.cancelButton;
    }

    // Megachapuza para hacer coincidir el UI de la barra con la de ontimize
    private static class StatusInformationProgressBar extends JProgressBar {

        protected boolean customIndeterminate = true;

        protected int updateTime = 300;

        boolean increasing = true;

        Runnable indeterminateTask = new Runnable() {

            @Override
            public void run() {
                while (StatusInformationProgressBar.this.customIndeterminate
                        && StatusInformationProgressBar.this.isShowing()) {
                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {

                            @Override
                            public void run() {
                                synchronized (StatusInformationProgressBar.this) {
                                    if (!StatusInformationProgressBar.this.customIndeterminate) {
                                        return;
                                    }

                                    StatusInformationProgressBar.this.setMinimum(0);
                                    StatusInformationProgressBar.this.setMaximum(
                                            StatusInformationProgressBar.this.getWidth());
                                    // Paint progress panel
                                    int value = StatusInformationProgressBar.this.getValue();
                                    if ((value < StatusInformationProgressBar.this
                                        .getWidth()) && StatusInformationProgressBar.this.increasing) {
                                        StatusInformationProgressBar.this.setValue(value + 5);
                                    } else {
                                        StatusInformationProgressBar.this.increasing = false;
                                        StatusInformationProgressBar.this.setValue(value - 5);
                                        if (value <= 0) {
                                            StatusInformationProgressBar.this.increasing = true;
                                        }
                                    }
                                }
                                StatusInformationProgressBar.this.repaint();
                            }
                        });
                        Thread.sleep(StatusInformationProgressBar.this.updateTime);
                    } catch (Exception e) {
                        StatusInformationPanel.logger.trace(null, e);
                    }
                }
            }
        };

        public StatusInformationProgressBar() {
            super();
            this.addHierarchyListener(new HierarchyListener() {

                @Override
                public void hierarchyChanged(HierarchyEvent e) {
                    if (((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) > 0)
                            && StatusInformationProgressBar.this.isShowing()) {
                        if (StatusInformationProgressBar.this.customIndeterminate) {
                            new Thread(StatusInformationProgressBar.this.indeterminateTask, "indeterminate thread")
                                .start();
                        }
                    }
                }
            });
        }

        @Override
        public void setIndeterminate(boolean newValue) {
            if (newValue == this.customIndeterminate) {
                return;
            }
            synchronized (this) {
                this.customIndeterminate = newValue;
                super.setIndeterminate(false);
                if (this.customIndeterminate) {
                    new Thread(this.indeterminateTask, "indeterminate thread").start();
                }
            }
        }

    }

}

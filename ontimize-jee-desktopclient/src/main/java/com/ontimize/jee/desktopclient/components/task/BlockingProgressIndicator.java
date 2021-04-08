package com.ontimize.jee.desktopclient.components.task;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.RootPaneContainer;
import javax.swing.Timer;

/**
 * The Class BlockingProgressIndicator.
 */
public class BlockingProgressIndicator extends JPanel
        implements ActionListener, WorkerStatusUpdateListener, PropertyChangeListener, MouseListener,
        MouseMotionListener, FocusListener {

    public static int MAX_SIZE_WIDTH = 300;

    public static int MAX_SIZE_HEIGHT = 175;

    public static final String PROPERTY_HIDE_INMEDIATLY = "hideinmediatly";

    /** The fps. */
    public static int FPS = 30;

    /** The final alpha. */
    public static float FINAL_ALPHA = 0.5f;

    /** The show timer. */
    protected ShowPanelTimer showTimer;

    /** The hide timer. */
    protected HidePanelTimer hideTimer;

    /** The information panel. */
    protected StatusInformationPanel informationPanel;

    /** The alpha. */
    protected float alpha;

    /** The current container. */
    protected Container currentContainer;

    /** The container resized listener. */
    protected ComponentListener containerResizedListener;

    /** The current worker. */
    protected OSwingWorker<?, ?> currentWorker;

    /**
     * Instantiates a new blocking progress indicator.
     *
     */
    public BlockingProgressIndicator() {
        this(300);
    }

    /**
     * Instantiates a new blocking progress indicator.
     * @param fxTime Tiempo que durará el efecto en milisegundos
     */
    public BlockingProgressIndicator(int fxTime) {
        super();
        this.init(fxTime);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addFocusListener(this);
    }

    /**
     * Inicializa el componente.
     * @param fxTime Tiempo que durará el efecto en milisegundos
     */
    protected void init(int fxTime) {
        this.setBackground(Color.BLACK);
        this.setOpaque(false);
        this.informationPanel = this.createInformationPanel();
        this.alpha = 1f;

        double numIterationsTmp = (fxTime / 1000.0) * BlockingProgressIndicator.FPS;
        int interval = (int) (fxTime / numIterationsTmp);
        int numIterations = (int) Math.ceil(fxTime / (double) interval);

        this.showTimer = new ShowPanelTimer(interval, numIterations);
        this.hideTimer = new HidePanelTimer(interval, numIterations);

        this.setLayout(null);
        this.add(this.informationPanel);

        this.containerResizedListener = new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                BlockingProgressIndicator.this.positionInformationPanel();
            }
        };
    }

    /** The previous glass component. */
    private Component previousGlassComponent = null;

    /**
     * Inicia la a nimación para mostrar el panel deslizante.
     * @param worker the worker
     * @param container the container
     * @param runWorker the run worker
     * @param cancellable the cancellable
     */
    public void startShowRelativeTo(OSwingWorker<?, ?> worker, final RootPaneContainer container, boolean runWorker,
            boolean cancellable) {
        this.currentContainer = (Container) container;
        this.currentContainer.addComponentListener(this.containerResizedListener);
        this.previousGlassComponent = container.getGlassPane();
        this.currentWorker = worker;
        this.setCancellable(cancellable);

        worker.addStatusUpdateListener(this);
        worker.addPropertyChangeListener(this);
        this.informationPanel.setStatus(new WorkerStatusInfo(null, null, null));

        this.positionInformationPanel();
        this.alpha = 0f;
        this.hideTimer.stop();

        if (((RootPaneContainer) BlockingProgressIndicator.this.currentContainer)
            .getGlassPane() != BlockingProgressIndicator.this) {
            ((RootPaneContainer) BlockingProgressIndicator.this.currentContainer)
                .setGlassPane(BlockingProgressIndicator.this);
            ((RootPaneContainer) BlockingProgressIndicator.this.currentContainer).getGlassPane().setVisible(true);
        }
        this.showTimer.start();
        if (runWorker) {
            worker.execute();
        }
    }

    /**
     * Inicia la animación para ocultar el panel deslizante.
     */
    public void startHide() {
        this.showTimer.stop();
        this.hideTimer.start();
        this.currentContainer.removeComponentListener(this.containerResizedListener);
        this.currentWorker.removeStatusUpdateListener(BlockingProgressIndicator.this);
        this.currentWorker.removePropertyChangeListener(BlockingProgressIndicator.this);
    }

    /**
     * Actualiza la posición del panel deslizante con el offset solicitado.
     *
     */
    protected void positionInformationPanel() {
        Dimension preferredSize = this.informationPanel.getPreferredSize();
        int preferredWidth = preferredSize.width;
        int preferredHeight = preferredSize.height;
        if (preferredWidth < BlockingProgressIndicator.MAX_SIZE_WIDTH) {
            preferredWidth = BlockingProgressIndicator.MAX_SIZE_WIDTH;
        }
        if (preferredHeight < BlockingProgressIndicator.MAX_SIZE_HEIGHT) {
            preferredHeight = BlockingProgressIndicator.MAX_SIZE_HEIGHT;
        }
        int offsetX = (this.currentContainer.getWidth() - preferredWidth) / 2;
        int offsetY = (this.currentContainer.getHeight() - preferredHeight) / 2;
        this.informationPanel.setBounds(offsetX, offsetY, preferredWidth, preferredHeight);

    }

    /**
     * Creates the information panel.
     * @return the status information panel
     */
    protected StatusInformationPanel createInformationPanel() {
        StatusInformationPanel panel = new StatusInformationPanel();
        panel.setVisible(true);
        panel.getCancelButton().addActionListener(this);
        return panel;
    }

    /**
     * Escucha el botón cancelar.
     * @param e the e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        this.currentWorker.cancel(true);
        this.startHide();
    }

    /**
     * Escucha actualizaciones de estado del worker.
     * @param source the source
     * @param info the info
     */
    @Override
    public void statusUpdated(OSwingWorker<?, ?> source, WorkerStatusInfo info) {
        if (this.currentWorker == source) {
            this.informationPanel.setStatus(info);
        }
    }

    /**
     * Escucha cuando termina el worker para esconder la ventana.
     * @param evt the evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (this.currentWorker == evt.getSource()) {
            if ("state".equals(evt.getPropertyName())) {
                if ((evt.getNewValue() == OSwingWorker.StateValue.DONE) && this.isVisible()) {
                    this.startHide();
                }
            } else if (BlockingProgressIndicator.PROPERTY_HIDE_INMEDIATLY.equals(evt.getPropertyName())) {
                this.hideTimer.stop();
            }
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
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.alpha));
        if (this.isBackgroundSet()) {
            Color c = this.getBackground();
            g2.setColor(c);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
        super.paintComponent(g2);
        g2.setComposite(oldComposite);
    }

    /**
     * Timer para mostrar.
     */
    protected class ShowPanelTimer extends Timer implements ActionListener {

        /** The num iterations. */
        protected int numIterations;

        /** The alpha increment. */
        protected float alphaIncrement;

        /**
         * Instantiates a new show panel timer.
         * @param interval Intervalo de tiempo entre eventos
         * @param numIterations Número de iteraciones
         */
        ShowPanelTimer(int interval, int numIterations) {
            // first param is callback interval in milliseconds
            super(interval, null);
            this.numIterations = numIterations;
            this.alphaIncrement = BlockingProgressIndicator.FINAL_ALPHA / numIterations;
            this.addActionListener(this);
        }

        /**
         * Para el <code>Timer</code> y envía los eventos a los escuchadores.
         *
         * @see #start
         */
        @Override
        public void stop() {
            if (this.isRunning()) {
                super.stop();
                BlockingProgressIndicator.this.alpha = BlockingProgressIndicator.FINAL_ALPHA;
                BlockingProgressIndicator.this.repaint();
            }
        }

        /**
         * Actualiza la visualización.
         * @param e the e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!BlockingProgressIndicator.this.isVisible()) {
                this.stop();
                return;
            }

            boolean stop = true;
            if (BlockingProgressIndicator.this.alpha < BlockingProgressIndicator.FINAL_ALPHA) {
                stop = false;
                BlockingProgressIndicator.this.alpha += this.alphaIncrement;
            }
            if (stop) {
                this.stop();
            } else {
                BlockingProgressIndicator.this.repaint();
            }
        }

    }

    /**
     * Timer para esconder.
     */
    protected class HidePanelTimer extends Timer implements ActionListener {

        /** The num iterations. */
        protected double numIterations;

        /** The alpha increment. */
        protected float alphaIncrement;

        /**
         * Instantiates a new hide panel timer.
         * @param interval Tiempo entre iteraciones.
         * @param numIterations Número de iteraciones.
         */
        HidePanelTimer(int interval, int numIterations) {
            // first param is callback interval in milliseconds
            super(interval, null); // call back in millis
            this.numIterations = numIterations;
            this.alphaIncrement = BlockingProgressIndicator.FINAL_ALPHA / numIterations;
            this.addActionListener(this);
        }

        /**
         * Para el <code>Timer</code> y envía los eventos a los escuchadores.
         *
         * @see #start
         */
        @Override
        public void stop() {
            BlockingProgressIndicator.this.setVisible(false);
            ((RootPaneContainer) BlockingProgressIndicator.this.currentContainer)
                .setGlassPane(BlockingProgressIndicator.this.previousGlassComponent);

            if (this.isRunning()) {
                super.stop();
                BlockingProgressIndicator.this.alpha = 0.0f;
                BlockingProgressIndicator.this.repaint();
            }
        }

        /**
         * Actualiza la visualización.
         * @param e the e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean stop = true;

            if (BlockingProgressIndicator.this.alpha > 0.0f) {
                stop = false;
                BlockingProgressIndicator.this.alpha -= this.alphaIncrement;
                if (BlockingProgressIndicator.this.alpha < 0.0f) {
                    BlockingProgressIndicator.this.alpha = 0.0f;
                }
            }

            if (stop) {
                BlockingProgressIndicator.this.hideTimer.stop();
            } else {
                BlockingProgressIndicator.this.repaint();
            }
        }

    }

    /**
     * Sets the cancellable.
     * @param cancellable the new cancellable
     */
    public void setCancellable(boolean cancellable) {
        this.informationPanel.getCancelButton().setVisible(cancellable);
    }

    @Override
    public void setVisible(boolean v) {
        if (v) {
            this.requestFocus();
        }
        super.setVisible(v);
    }

    @Override
    public void focusLost(FocusEvent fe) {
        if (this.isVisible()) {
            this.requestFocus();
        }
    }

    @Override
    public void focusGained(FocusEvent fe) {
        // Do nothing
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Do nothing
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Do nothing
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Do nothing
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Do nothing
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Do nothing
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Do nothing
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Do nothing
    }

}

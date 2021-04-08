package com.ontimize.jee.desktopclient.components.messaging;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;

import javax.swing.JPanel;

/**
 * The Class AbstractToastPanel.
 */
public abstract class AbstractToastPanel extends JPanel {

    /** The alpha. */
    protected float alpha;

    /**
     * Instantiates a new abstract toast panel.
     */
    public AbstractToastPanel() {
        super();
        this.alpha = 0.1f;
        this.setBackground(Color.decode("#222222"));
    }

    /**
     * Notifica que el panel debe cubir sus componentes con la información del <code>message</code>.
     * @param message the new message
     */
    public abstract void setMessage(ToastMessage message);

    /**
     * Se invoca una vez se esconde el panel para obtener una respuesta en caso de que la huebiera.
     * @param message the message
     * @return the response
     */
    public abstract Object getResponse(ToastMessage message);

    /**
     * Sets the background alpha.
     * @param alpha the new background alpha
     */
    public void setBackgroundAlpha(float alpha) {
        this.alpha = alpha;
        this.repaint();
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
        g2.setComposite(oldComposite);
        super.paintComponent(g2);
    }

}

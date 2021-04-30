package com.ontimize.jee.desktopclient.components.sliderbar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;

import com.ontimize.jee.desktopclient.components.sliderbar.SliderBar.SlideBarMode;

/**
 * Panel that contains both the title/header part and the content part.
 *
 */

public class SlideBarSection extends JPanel {

    public int minComponentHeight = 40;

    public int minComponentWidth = 350;

    public JPanel titlePanel = new JPanel();

    private final SliderBar sliderBarOwner;

    private final JComponent contentPane; // sliderbar section's content

    private final ArrowPanel arrowPanel;

    private int calculatedHeight;

    /**
     * Construct a new sliderbar section with the specified owner and model.
     * @param owner - SliderBar
     * @param model
     */
    public SlideBarSection(SliderBar owner, String text, JComponent component, Icon icon) {
        if (owner.thisMode == SliderBar.SlideBarMode.INNER_LEVEL) {
            this.minComponentHeight = 30;
        } else {
            this.minComponentHeight = 40;
        }
        this.contentPane = component;
        this.sliderBarOwner = owner;
        this.titlePanel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {

                if (SlideBarSection.this != SlideBarSection.this.sliderBarOwner.getCurrentSection()) {
                    if (SlideBarSection.this.sliderBarOwner.getCurrentSection() != null) {
                        SlideBarSection.this.sliderBarOwner.getCurrentSection().collapse(true);
                    }

                    SlideBarSection.this.expand(); // expand this!
                } else {
                    SlideBarSection.this.collapse(true);
                }
            }
        });

        // absolute layout
        this.setLayout(new BorderLayout());

        this.add(this.titlePanel, BorderLayout.NORTH);

        JLabel sliderbarLabel = new JLabel(text);
        sliderbarLabel.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 2));
        this.titlePanel.setLayout(new BorderLayout());
        this.titlePanel.setPreferredSize(new Dimension(this.getPreferredSize().width, this.minComponentHeight));
        this.titlePanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        this.arrowPanel = new ArrowPanel(SwingConstants.EAST);
        this.arrowPanel.setPreferredSize(new Dimension(40, 40));

        if (this.sliderBarOwner.showArrow) {
            // add into tab panel the arrow and labels.
            this.titlePanel.add(this.arrowPanel, BorderLayout.EAST);
        }

        this.titlePanel.add(new JLabel(icon), BorderLayout.WEST);
        this.titlePanel.add(sliderbarLabel);
        this.setMinimumSize(new Dimension(this.minComponentWidth, this.minComponentHeight));

        this.add(component, BorderLayout.CENTER);
        this.revalidate();
    }

    public void expand() {
        this.sliderBarOwner.setCurrentSection(this);
        this.arrowPanel.changeDirection(SwingConstants.SOUTH);
        this.arrowPanel.updateUI();

        this.calculatedHeight = -1;
        this.calculatedHeight = this.sliderBarOwner.getSize().height;

        if (this.sliderBarOwner.animate) {
            /**
             * ANIMATION BIT
             */
            SlideBarAnimation anim = new SlideBarAnimation(this, 200);

            anim.setStartValue(this.minComponentHeight);
            anim.setEndValue(this.calculatedHeight);
            anim.start();
        } else {
            if (this.sliderBarOwner.thisMode == SlideBarMode.INNER_LEVEL) {
                this.calculatedHeight = 1000;

                Dimension d = new Dimension(Integer.MAX_VALUE, this.calculatedHeight);
                this.setMaximumSize(d);
                this.sliderBarOwner.setPreferredSize(d);
                this.contentPane.setVisible(true);
                this.revalidate();
            } else {
                this.setMaximumSize(new Dimension(Integer.MAX_VALUE, this.calculatedHeight));
                this.contentPane.setVisible(true);
                this.revalidate();
            }
        }
    }

    public void collapse(boolean animate) {
        // remove reference
        if (this.sliderBarOwner.getCurrentSection() == SlideBarSection.this) {
            this.sliderBarOwner.setCurrentSection(null);
        }

        this.arrowPanel.changeDirection(SwingConstants.EAST);
        this.arrowPanel.updateUI();

        if (animate && this.sliderBarOwner.animate) {
            SlideBarAnimation anim = new SlideBarAnimation(this, 200);

            anim.setStartValue(this.calculatedHeight);
            anim.setEndValue(this.minComponentHeight);
            anim.start();
        } else {
            if (this.sliderBarOwner.thisMode == SlideBarMode.INNER_LEVEL) {
                this.setMaximumSize(new Dimension(Integer.MAX_VALUE, this.titlePanel.getPreferredSize().height));
                this.contentPane.setVisible(false);
                this.revalidate();

            } else {
                this.setMaximumSize(new Dimension(Integer.MAX_VALUE, this.titlePanel.getPreferredSize().height));
                this.contentPane.setVisible(false);
                this.revalidate();
            }
        }
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(this.minComponentWidth, this.minComponentHeight);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(this.minComponentWidth, this.minComponentHeight);
    }

    public class ArrowPanel extends JPanel implements SwingConstants {

        protected int direction;

        private final Color shadow;

        private final Color darkShadow;

        private final Color highlight;

        public ArrowPanel(int direction) {
            this(direction, UIManager.getColor("control"), UIManager.getColor("controlShadow"),
                    UIManager.getColor("controlDkShadow"), UIManager.getColor("controlLtHighlight"));
        }

        public ArrowPanel(int direction, Color background, Color shadow, Color darkShadow, Color highlight) {
            super();
            this.setRequestFocusEnabled(false);
            this.setDirection(direction);
            this.setBackground(background);
            this.shadow = shadow;
            this.darkShadow = darkShadow;
            this.highlight = highlight;
        }

        /**
         * Returns the direction of the arrow.
         * @param direction the direction of the arrow; one of {@code SwingConstants.NORTH},
         *        {@code SwingConstants.SOUTH}, {@code SwingConstants.EAST} or {@code SwingConstants.WEST}
         */
        public int getDirection() {
            return this.direction;
        }

        /**
         * Sets the direction of the arrow.
         * @param direction the direction of the arrow; one of of {@code SwingConstants.NORTH},
         *        {@code SwingConstants.SOUTH}, {@code SwingConstants.EAST} or {@code SwingConstants.WEST}
         */
        public void setDirection(int dir) {
            this.direction = dir;
        }

        @Override
        public void paint(Graphics g) {
            Color origColor;
            int w, h, size;

            w = this.getSize().width;
            h = this.getSize().height;
            origColor = g.getColor();

            g.setColor(this.getBackground());
            g.fillRect(1, 1, w - 2, h - 2);

            // If there's no room to draw arrow, bail
            if ((h < 5) || (w < 5)) {
                g.setColor(origColor);
                return;
            }

            // Draw the arrow
            size = Math.min((h - 4) / 3, (w - 4) / 3);
            size = Math.max(size, 2);
            this.paintTriangle(g, (w - size) / 2, (h - size) / 2, size, this.direction, false);

            g.setColor(origColor);
        }

        /**
         * Paints a triangle.
         */
        public void paintTriangle(Graphics g, int x, int y, int size, int direction, boolean isEnabled) {
            Color oldColor = g.getColor();
            int mid, i, j;

            j = 0;
            size = Math.max(size, 2);
            mid = (size / 2) - 1;

            g.translate(x, y);
            if (isEnabled) {
                g.setColor(this.darkShadow);
            } else {
                g.setColor(this.shadow);
            }

            switch (direction) {
                case NORTH:
                    this.paintTriangleNorth(g, size, isEnabled, mid);
                    break;
                case SOUTH:
                    this.paintTriangleSouth(g, size, isEnabled, mid, j);
                    break;
                case WEST:
                    this.paintTriangleWest(g, size, isEnabled, mid);
                    break;
                case EAST:
                    this.paintTriangleEast(g, size, isEnabled, mid, j);
                    break;
            }
            g.translate(-x, -y);
            g.setColor(oldColor);
        }

        private void paintTriangleEast(Graphics g, int size, boolean isEnabled, int mid, int j) {
            int i;
            if (!isEnabled) {
                g.translate(1, 1);
                g.setColor(this.highlight);
                for (i = size - 1; i >= 0; i--) {
                    g.drawLine(j, mid - i, j, mid + i);
                    j++;
                }
                g.translate(-1, -1);
                g.setColor(this.shadow);
            }

            j = 0;
            for (i = size - 1; i >= 0; i--) {
                g.drawLine(j, mid - i, j, mid + i);
                j++;
            }
        }

        private void paintTriangleWest(Graphics g, int size, boolean isEnabled, int mid) {
            int i;
            for (i = 0; i < size; i++) {
                g.drawLine(i, mid - i, i, mid + i);
            }
            if (!isEnabled) {
                g.setColor(this.highlight);
                g.drawLine(i, (mid - i) + 2, i, mid + i);
            }
        }

        private void paintTriangleSouth(Graphics g, int size, boolean isEnabled, int mid, int j) {
            int i;
            if (!isEnabled) {
                g.translate(1, 1);
                g.setColor(this.highlight);
                for (i = size - 1; i >= 0; i--) {
                    g.drawLine(mid - i, j, mid + i, j);
                    j++;
                }
                g.translate(-1, -1);
                g.setColor(this.shadow);
            }

            j = 0;
            for (i = size - 1; i >= 0; i--) {
                g.drawLine(mid - i, j, mid + i, j);
                j++;
            }
        }

        private void paintTriangleNorth(Graphics g, int size, boolean isEnabled, int mid) {
            int i;
            for (i = 0; i < size; i++) {
                g.drawLine(mid - i, i, mid + i, i);
            }
            if (!isEnabled) {
                g.setColor(this.highlight);
                g.drawLine((mid - i) + 2, i, mid + i, i);
            }
        }

        public void changeDirection(int d) {
            this.setDirection(d);
        }

    }

    public JComponent getContentPane() {
        return this.contentPane;
    }

}

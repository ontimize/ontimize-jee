package com.ontimize.jee.desktopclient.components.sliderbar;

import java.awt.Cursor;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class SliderBar extends JPanel {

    // The preferred initial width of the slider bar
    private static int PREFERRED_WIDTH = 300;

    // box layout to contain slider bar sections arranged vertically
    private final BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);

    // the currently expanded section
    private SlideBarSection currentSection = null;

    SlideBarMode thisMode;

    boolean showArrow;

    boolean animate = false;

    public SliderBar(SlideBarMode mode, boolean showArrow, int preferredWidth, boolean animate) {
        this.showArrow = showArrow;
        this.thisMode = mode;
        this.animate = animate;
        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
        this.setLayout(this.boxLayout);
        this.setPreferredSize(new Dimension(preferredWidth, this.getPreferredSize().height));
        this.setFocusable(false);
        this.revalidate();
    }

    public void addSection(SlideBarSection newSection) {
        this.add(newSection);
        newSection.collapse(false);
    }

    public boolean isCurrentExpandedSection(SlideBarSection section) {
        return (section != null) && (this.currentSection != null) && section.equals(this.currentSection);
    }

    public SlideBarMode getMode() {
        return this.thisMode;
    }

    public SlideBarSection getCurrentSection() {
        return this.currentSection;
    }

    public void setCurrentSection(SlideBarSection section) {
        this.currentSection = section;
    }

    public enum SlideBarMode {

        TOP_LEVEL, INNER_LEVEL;

    }

}

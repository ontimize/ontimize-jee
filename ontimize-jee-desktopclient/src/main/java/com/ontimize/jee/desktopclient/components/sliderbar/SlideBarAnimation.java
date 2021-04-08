package com.ontimize.jee.desktopclient.components.sliderbar;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlideBarAnimation implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(SlideBarAnimation.class);

    // start value (typically in pixels)
    protected int startValue = 0;

    // end value (typically in pixels)
    protected int endValue = 0;

    // duration over which the animation takes place
    protected long durationMillis = 0;

    // a value (difference of start and end values) that corresponds to value per millisecond
    protected double valuePerMilli = 0.0;

    // The ctm of the last performed animation operation
    protected long startMillis;

    protected Timer timer;

    protected double value = 0;

    private SlideBarSection sliderBarSection;

    /**
     * Constructor where you specify <i>time</i> between the two pixel values.
     * @param startValue
     * @param endValue
     * @param durationMillis
     */
    public SlideBarAnimation(int startValue, int endValue, int durationMillis) {
        this.startValue = startValue;
        this.endValue = endValue;
        this.durationMillis = durationMillis;

        // create the value per millis.
        this.valuePerMilli = (double) (endValue - startValue) / (double) durationMillis;
    }

    /**
     * Constructor where you specify <i>value/ms</i> between the two pixel values.
     * @param startValue
     * @param endValue
     * @param durationMillis
     */
    public SlideBarAnimation(int startValue, int endValue, double valuePerMilli) {
        this.startValue = startValue;
        this.endValue = endValue;
        this.valuePerMilli = valuePerMilli;
    }

    public SlideBarAnimation(int durationMs) {
        this.durationMillis = durationMs;
    }

    public SlideBarAnimation(SlideBarSection sliderbarSection, int durationMs) {
        this(durationMs);
        this.sliderBarSection = sliderbarSection;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        // get ctm
        long ctm = System.currentTimeMillis();

        // get difference of this ctm with the last ctm
        double millisPassed = ctm - this.startMillis;

        /**
         * This may be 0 if millisPassed is small enough
         */
        double i = millisPassed * this.valuePerMilli;
        if (Double.compare(i, 0.0d) == 0) {
            SlideBarAnimation.logger.debug("WARNING: Animation is incrementing by zero... potential infinite loop");
        }

        this.value += i;

        // replace old ctm with new one.
        this.startMillis = ctm;

        if ((this.startValue < this.endValue) && (this.value >= this.endValue)) {
            this.value = Math.min(this.value, this.endValue);
            this.render((int) this.value);
            this.stop();
        } else if ((this.startValue > this.endValue) && (this.value <= this.endValue)) {
            this.value = Math.max(this.value, this.endValue);
            this.render((int) this.value);
            this.stop();
        } else {
            this.render((int) this.value);
        }
    }

    public void start() {
        this.startMillis = System.currentTimeMillis();
        this.value = this.startValue;
        this.timer = new Timer(50, this);
        this.starting();
        this.timer.restart();
    }

    public void stop() {
        this.timer.stop();
        this.stopped();
    }

    public int getEndValue() {
        return this.endValue;
    }

    public void setEndValue(int endValue) {
        this.endValue = endValue;
        if (this.durationMillis > 0) {
            this.valuePerMilli = (double) (endValue - this.startValue) / (double) this.durationMillis;
        }
    }

    public int getStartValue() {
        return this.startValue;
    }

    public void setStartValue(int startValue) {
        this.startValue = startValue;
        if (this.durationMillis > 0) {
            this.valuePerMilli = (double) (this.endValue - startValue) / (double) this.durationMillis;
        }
    }

    public void starting() {
        this.sliderBarSection.getContentPane().setVisible(true);
    }

    protected void render(int value) {
        this.sliderBarSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, value));
        this.sliderBarSection.getContentPane().setVisible(true);
        this.sliderBarSection.revalidate();
    }

    public void stopped() {
        this.sliderBarSection.getContentPane().setVisible(true);
        this.sliderBarSection.revalidate();
    }

}

package com.ontimize.jee.common.tools;

/**
 * The Class Chronometer.
 */
public class Chronometer {

    private static final double MS_TO_SECONDS = 1000d;

    /** The start. */
    private long start;

    /** The last elapsed. */
    private long lastElapsed;

    /** The stop. */
    private long stop;

    /**
     * Instantiates a new chronometer.
     */
    public Chronometer() {
        this.start = 0;
        this.lastElapsed = 0;
        this.stop = 0;
    }

    /**
     * Comienza el cronómetro.
     */
    public Chronometer start() {
        this.start = System.currentTimeMillis();
        this.lastElapsed = 0;
        this.stop = 0;
        return this;
    }

    /**
     * Devuelve el tiempo entre el último elapsed (o el begin) y ahora.
     * @return the long
     */
    public long elapsedMs() {
        long tmp = System.currentTimeMillis();
        if (this.lastElapsed == 0) {
            this.lastElapsed = tmp;
            return this.lastElapsed - this.start;
        }
        long res = tmp - this.lastElapsed;
        this.lastElapsed = tmp;
        return res;
    }

    public double elapsedSeconds() {
        return Chronometer.toSeconds(this.elapsedMs());
    }

    /**
     * Devuelve el tiempo desde el principio.
     * @return the long
     */
    public long timeFromStartMs() {
        return System.currentTimeMillis() - this.start;
    }

    public double timeFromStartSeconds() {
        return Chronometer.toSeconds(this.timeFromStartMs());
    }

    /**
     * Devuelve el tiempo desde el último elaspsed o desde el principio.
     * @return the long
     */
    public long timeFromlastElapsedMs() {
        return System.currentTimeMillis() - (this.lastElapsed == 0 ? this.start : this.lastElapsed);
    }

    public double timeFromLastElapsedSeconds() {
        return Chronometer.toSeconds(this.timeFromlastElapsedMs());
    }

    /**
     * Finaliza el cronómetro y devuelve el tiempo desde el principio.
     * @return the long
     */
    public long stopMs() {
        this.stop = System.currentTimeMillis();
        return this.stop - this.start;
    }

    public double stopSeconds() {
        return Chronometer.toSeconds(this.stopMs());
    }

    /**
     * Devuelve el tiempo entre el final y el inicio del cronómetro.
     * @return the measure time
     */
    public long getMeasureTimeMs() {
        return this.stop - this.start;
    }

    public double getMeasureTimeSeconds() {
        return Chronometer.toSeconds(this.getMeasureTimeMs());
    }

    /**
     * Returns nano time in seconds
     * @param nanoTime
     * @return
     */
    public static double toSeconds(long nanoTime) {
        return nanoTime / Chronometer.MS_TO_SECONDS;
    }

}

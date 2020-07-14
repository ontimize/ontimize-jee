package com.ontimize.jee.common.tools;

/**
 * The Class Chronometer.
 */
public class ChronometerNano {

    /** The Constant NANO_TO_SECONDS. */
    private static final double NANO_TO_SECONDS = 1000000000d;

    /** The Constant NANO_TO_MS. */
    private static final double NANO_TO_MS = 1000000d;

    /** The start. */
    private long start;

    /** The last elapsed. */
    private long lastElapsed;

    /** The stop. */
    private long stop;

    /**
     * Instantiates a new chronometer.
     */
    public ChronometerNano() {
        this.start = 0;
        this.lastElapsed = 0;
        this.stop = 0;
    }

    /**
     * Comienza el cronómetro.
     * @return the chronometer nano
     */
    public ChronometerNano start() {
        this.start = System.nanoTime();
        this.lastElapsed = 0;
        this.stop = 0;
        return this;
    }

    /**
     * Devuelve el tiempo entre el último elapsed (o el begin) y ahora.
     * @return the long
     */
    public long elapsed() {
        long tmp = System.nanoTime();
        if (this.lastElapsed == 0) {
            this.lastElapsed = tmp;
            return this.lastElapsed - this.start;
        }
        long res = tmp - this.lastElapsed;
        this.lastElapsed = tmp;
        return res;
    }

    /**
     * Elapsed seconds.
     * @return the double
     */
    public double elapsedSeconds() {
        return ChronometerNano.toSeconds(this.elapsed());
    }

    /**
     * Elapsed ms.
     * @return the double
     */
    public double elapsedMs() {
        return ChronometerNano.toMs(this.elapsed());
    }

    /**
     * Devuelve el tiempo desde el principio.
     * @return the long
     */
    public long timeFromStart() {
        return System.nanoTime() - this.start;
    }

    /**
     * Time from start seconds.
     * @return the double
     */
    public double timeFromStartSeconds() {
        return ChronometerNano.toSeconds(this.timeFromStart());
    }

    /**
     * Time from start ms.
     * @return the double
     */
    public double timeFromStartMs() {
        return ChronometerNano.toMs(this.timeFromStart());
    }

    /**
     * Devuelve el tiempo desde el último elaspsed o desde el principio.
     * @return the long
     */
    public long timeFromLastElapsed() {
        return System.nanoTime() - (this.lastElapsed == 0 ? this.start : this.lastElapsed);
    }

    /**
     * Time from last elapsed seconds.
     * @return the double
     */
    public double timeFromLastElapsedSeconds() {
        return ChronometerNano.toSeconds(this.timeFromLastElapsed());
    }

    /**
     * Time fromlast elapsed ms.
     * @return the double
     */
    public double timeFromlastElapsedMs() {
        return ChronometerNano.toMs(this.timeFromLastElapsed());
    }

    /**
     * Finaliza el cronómetro y devuelve el tiempo desde el principio.
     * @return the long
     */
    public long stop() {
        this.stop = System.nanoTime();
        return this.stop - this.start;
    }

    /**
     * Stop seconds.
     * @return the double
     */
    public double stopSeconds() {
        return ChronometerNano.toSeconds(this.stop());
    }

    /**
     * Stop ms.
     * @return the double
     */
    public double stopMs() {
        return ChronometerNano.toMs(this.stop());
    }

    /**
     * Devuelve el tiempo entre el final y el inicio del cronómetro.
     * @return the measure time
     */
    public long getMeasureTime() {
        return this.stop - this.start;
    }

    /**
     * Gets the measure time seconds.
     * @return the measure time seconds
     */
    public double getMeasureTimeSeconds() {
        return ChronometerNano.toSeconds(this.getMeasureTime());
    }

    /**
     * Gets the measure time ms.
     * @return the measure time ms
     */
    public double getMeasureTimeMs() {
        return ChronometerNano.toMs(this.getMeasureTime());
    }

    /**
     * Returns nano time in seconds.
     * @param nanoTime the nano time
     * @return the double
     */
    public static double toSeconds(long nanoTime) {
        return nanoTime / ChronometerNano.NANO_TO_SECONDS;
    }

    /**
     * To ms.
     * @param nanoTime the nano time
     * @return the double
     */
    public static double toMs(long nanoTime) {
        return nanoTime / ChronometerNano.NANO_TO_MS;
    }

}

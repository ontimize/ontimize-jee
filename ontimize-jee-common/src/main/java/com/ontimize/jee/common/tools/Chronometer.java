package com.ontimize.jee.common.tools;

/**
 * The Class Chronometer.
 */
public class Chronometer {

	/** The Constant NANO_TO_S. */
	public static final long	NANO_TO_S	= 1000000000l;

	/** The Constant NANO_TO_MS. */
	public static final long	NANO_TO_MS	= 1000000l;

	/** The start. */
	private long	start;

	/** The last elapsed. */
	private long	lastElapsed;

	/** The stop. */
	private long	stop;

	/**
	 * Instantiates a new chronometer.
	 */
	public Chronometer() {
		this.start = 0;
		this.lastElapsed = 0;
		this.stop = 0;
	}

	/**
	 * Comienza el cronometro.
	 *
	 * @return the chronometer
	 */
	public Chronometer start() {
		this.start = System.nanoTime();
		this.lastElapsed = 0;
		this.stop = 0;
		return this;
	}

	/**
	 * Devuelve el tiempo entre el último elapsed (o el begin) y ahora.
	 *
	 * @return the long
	 */
	public long elapsed() {
		long tmp = System.nanoTime();
		if (this.lastElapsed == 0) {
			this.lastElapsed = tmp;
			return this.lastElapsed - this.start;
		} else {
			long res = tmp - this.lastElapsed;
			this.lastElapsed = tmp;
			return res;
		}
	}

	/**
	 * Elapsed ms.
	 *
	 * @return the double
	 */
	public double elapsedMs() {
		return Chronometer.toMs(this.elapsed());
	}

	/**
	 * Elapsed seconds.
	 *
	 * @return the double
	 */
	public double elapsedSeconds() {
		return Chronometer.toSeconds(this.elapsed());
	}

	/**
	 * Devuelve el tiempo desde el principio.
	 *
	 * @return the long
	 */
	public long timeFromStart() {
		return System.nanoTime()-this.start;
	}

	/**
	 * Time from start ms.
	 *
	 * @return the double
	 */
	public double timeFromStartMs() {
		return Chronometer.toMs(this.timeFromStart());
	}

	/**
	 * Time from start seconds.
	 *
	 * @return the double
	 */
	public double timeFromStartSeconds() {
		return Chronometer.toSeconds(this.timeFromStart());
	}

	/**
	 * Devuelve el tiempo desde el último elaspsed o desde el principio.
	 *
	 * @return the long
	 */
	public long timeFromLastElapsed() {
		return System.nanoTime()- ((this.lastElapsed == 0)?this.start:this.lastElapsed);
	}

	/**
	 * Time from last elapsed ms.
	 *
	 * @return the double
	 */
	public double timeFromLastElapsedMs() {
		return Chronometer.toMs(this.timeFromLastElapsed());
	}

	/**
	 * Time from last elapsed seconds.
	 *
	 * @return the double
	 */
	public double timeFromLastElapsedSeconds() {
		return Chronometer.toSeconds(this.timeFromLastElapsed());
	}

	/**
	 * Finaliza el cronómetro y devuelve el tiempo desde el principio.
	 *
	 * @return the long
	 */
	public long stop() {
		this.stop = System.nanoTime();
		return this.stop - this.start;
	}

	/**
	 * Stop ms.
	 *
	 * @return the double
	 */
	public double stopMs() {
		return Chronometer.toMs(this.stop());
	}

	/**
	 * Stop seconds.
	 *
	 * @return the double
	 */
	public double stopSeconds() {
		return Chronometer.toSeconds(this.stop());
	}

	/**
	 * Devuelve el tiempo entre el final y el inicio del cronómetro.
	 *
	 * @return the measure time
	 */
	public long getMeasureTime() {
		return this.stop - this.start;
	}

	/**
	 * Returns nano time in seconds.
	 *
	 * @param nanoTime
	 *            the nano time
	 * @return the double
	 */
	public static double toSeconds(long nanoTime) {
		return nanoTime / (double) Chronometer.NANO_TO_S;
	}

	/**
	 * To milliseconds.
	 *
	 * @param nanoTime
	 *            the nano time
	 * @return the double
	 */
	public static double toMs(long nanoTime) {
		return nanoTime / (double) Chronometer.NANO_TO_MS;
	}

}

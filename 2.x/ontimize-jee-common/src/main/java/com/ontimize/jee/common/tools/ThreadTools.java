package com.ontimize.jee.common.tools;

/**
 * The Class ThreadTools.
 */
public final class ThreadTools {

	/**
	 * Instantiates a new thread tools.
	 */
	private ThreadTools() {
		super();
	}

	/**
	 * Sleep.
	 *
	 * @param time
	 *            the time
	 */
	public static void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// do nothing
		}
	}

}

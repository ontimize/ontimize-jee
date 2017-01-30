package com.ontimize.jee.common.tools;

public class ExceptionTools {

	/**
	 * Introspects the <code>Throwable</code> to locate some cause that matchs with received class.
	 *
	 * @param original
	 * @param classToLookFor
	 * @return
	 */
	public static <T extends Throwable> T lookForInDepth(Throwable original, Class<T> classToLookFor) {
		Throwable th = original;
		while (th != null) {
			if (classToLookFor.isAssignableFrom(th.getClass())) {
				return (T)th;
			}
			th = th.getCause();
		}
		return null;
	}

	/**
	 * Return if the input Throwable contains in depht some cause that matchs with received class.
	 *
	 * @param original
	 * @param classToLookFor
	 * @return
	 */
	public static boolean containsInDepth(Throwable original, Class<? extends Throwable> classToLookFor) {
		return ExceptionTools.lookForInDepth(original, classToLookFor) != null;
	}
}

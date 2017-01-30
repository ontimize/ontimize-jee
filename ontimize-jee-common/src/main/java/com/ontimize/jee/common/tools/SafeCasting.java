/**
 * SafeCasting.java 18-jul-2013
 *
 *
 *
 */
package com.ontimize.jee.common.tools;

/**
 * Clase de ayuda para hacer cast seguros entre tipos primitivos.
 *
 * @author <a href=""></a>
 */
public final class SafeCasting {

	/**
	 * Instantiates a new safe casting.
	 */
	private SafeCasting() {
		super();
	}

    /**
     * Hace un cast seguro de long a int.
     *
     * @param l
     *            l
     * @return the int
     */
    public static int longToInt(final long l) {
        if ((l < Integer.MIN_VALUE) || (l > Integer.MAX_VALUE)) {
            throw new IllegalArgumentException(l + " cannot be cast to int without changing its value.");
        }
        return Long.valueOf(l).intValue();
    }

    /**
     * Hace un cast seguro de int a char.
     *
     * @param i
     *            i
     * @return the char
     */
    public static char intToChar(final int i) {
        if ((i < Character.MIN_VALUE) || (i > Character.MAX_VALUE)) {
            throw new IllegalArgumentException(i + " cannot be cast to char without changing its value.");
        }
        return (char) i;
    }
}

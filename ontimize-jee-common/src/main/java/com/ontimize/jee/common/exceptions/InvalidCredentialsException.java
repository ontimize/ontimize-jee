/**
 * InvalidCredentialsException.java 18-abr-2013
 *
 *
 *
 */
package com.ontimize.jee.common.exceptions;

/**
 * The Class InvalidCredentialsException.
 *
 * @author <a href="user@email.com">Author</a>
 */
public class InvalidCredentialsException extends RuntimeException {

	private static final long serialVersionUID = -390998553089824417L;

	/**
	 * Instantiates a new invalid credentials exception.
	 */
	public InvalidCredentialsException() {
		super();
	}

	/**
	 * Instantiates a new invalid credentials exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public InvalidCredentialsException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new invalid credentials exception.
	 *
	 * @param message
	 *            the message
	 */
	public InvalidCredentialsException(final String message) {
		super(message);
	}

	/**
	 * Instantiates a new invalid credentials exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public InvalidCredentialsException(final Throwable cause) {
		super(cause);
	}

}

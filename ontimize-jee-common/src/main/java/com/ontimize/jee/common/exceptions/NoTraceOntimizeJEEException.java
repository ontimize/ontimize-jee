package com.ontimize.jee.common.exceptions;

import com.ontimize.jee.common.tools.MessageType;

/**
 * The Class NoTraceOntimizeJEEException.
 */
public class NoTraceOntimizeJEEException extends OntimizeJEEException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new ontimize jee exception.
	 */
	public NoTraceOntimizeJEEException() {
		this((String) null);
	}

	/**
	 * Instantiates a new ontimize jee exception.
	 *
	 * @param message
	 *            the message
	 */
	public NoTraceOntimizeJEEException(String message) {
		this(message, (Object[]) null);
	}

	/**
	 * Instantiates a new ontimize jee exception.
	 *
	 * @param message
	 *            the message
	 * @param msgParameters
	 *            the msg parameters
	 */
	public NoTraceOntimizeJEEException(String message, Object[] msgParameters) {
		this(message, null, msgParameters, null, false, false);
	}

	/**
	 * Instantiates a new ontimize jee exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public NoTraceOntimizeJEEException(Throwable cause) {
		this(cause.getMessage(), cause, NoTraceOntimizeJEEException.getMessageParams(cause), NoTraceOntimizeJEEException.getMessageType(cause),
		        NoTraceOntimizeJEEException.isBlocking(cause), NoTraceOntimizeJEEException.isSilent(cause));
	}

	private static Object[] getMessageParams(Throwable cause) {
		if ((cause instanceof IParametrizedException) && (((IParametrizedException) cause).getMessageParameters() != null)) {
			return ((IParametrizedException) cause).getMessageParameters();
		}
		return null;

	}

	private static MessageType getMessageType(Throwable cause) {
		if ((cause instanceof IParametrizedException) && (((IParametrizedException) cause).getMessageType() != null)) {
			return ((IParametrizedException) cause).getMessageType();
		}
		return null;
	}

	private static boolean isBlocking(Throwable cause) {
		if (cause instanceof IParametrizedException) {
			return ((IParametrizedException) cause).isMessageBlocking();
		}
		return false;
	}

	private static boolean isSilent(Throwable cause) {
		if (cause instanceof IParametrizedException) {
			return ((IParametrizedException) cause).isSilent();
		}
		return false;
	}

	/**
	 * Instantiates a new ontimize jee exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public NoTraceOntimizeJEEException(String message, Throwable cause) {
		this(message, cause, (Object[]) null, null, false, false);
	}

	/**
	 * Instantiates a new ontimize jee exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 * @param msgParameters
	 *            the msg parameters
	 */
	public NoTraceOntimizeJEEException(String message, Throwable cause, Object[] msgParameters, MessageType type, boolean msgBocking, boolean silent) {
		super(message, cause, msgParameters, type, msgBocking, silent, false, false);
	}

	/**
	 * Instantiates a new ontimize jee exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 * @param msgParameters
	 *            the msg parameters
	 */
	public NoTraceOntimizeJEEException(String message, Throwable cause, Object[] msgParameters, MessageType type, boolean msgBocking, boolean silent, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, msgParameters, type, msgBocking, silent, enableSuppression, writableStackTrace);
	}

	/**
	 * Instantiates a new no trace ontimize jee exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public NoTraceOntimizeJEEException(OntimizeJEERuntimeException cause) {
		this(cause.getMessage(), null, cause.getMessageParameters(), cause.getMessageType(), cause.isMessageBlocking(), cause.isSilent());
	}

	/**
	 * Instantiates a new no trace ontimize jee exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public NoTraceOntimizeJEEException(OntimizeJEEException cause) {
		this(cause.getMessage(), null, cause.getMessageParameters(), cause.getMessageType(), cause.isMessageBlocking(), cause.isSilent());
	}
}

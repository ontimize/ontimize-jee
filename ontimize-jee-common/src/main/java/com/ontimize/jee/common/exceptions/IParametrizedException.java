package com.ontimize.jee.common.exceptions;

import com.ontimize.jee.common.tools.MessageType;

/**
 * The Interface IParametrizedException.
 */
public interface IParametrizedException {

	/**
	 * Checks if is message blocking.
	 *
	 * @return true, if is message blocking
	 */
	boolean isMessageBlocking();

	/**
	 * Checks if is silent.
	 *
	 * @return true, if is silent
	 */
	boolean isSilent();

	/**
	 * Gets the msg parameters.
	 *
	 * @return the msg parameters
	 */
	Object[] getMsgParameters();

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	String getMessage();

	/**
	 * Gets the message type.
	 *
	 * @return the message type
	 */
	MessageType getMessageType();

}
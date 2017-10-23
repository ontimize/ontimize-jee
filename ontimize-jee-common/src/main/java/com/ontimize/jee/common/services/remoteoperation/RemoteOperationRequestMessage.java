package com.ontimize.jee.common.services.remoteoperation;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * The Class RemoteOperationRequestMessage.
 */
public class RemoteOperationRequestMessage implements Serializable {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/** The operation class name. */
	private String				operationClassName;

	/** The parameters. */
	private Map<String, Object>	parameters;

	/**
	 * Instantiates a new remote operation request message.
	 */
	public RemoteOperationRequestMessage() {
		super();
	}

	/**
	 * Instantiates a new remote operation request message.
	 *
	 * @param operationClassName
	 *            the operation class name
	 * @param parameters
	 *            the parameters
	 */
	public RemoteOperationRequestMessage(String operationClassName, Map<String, Object> parameters) {
		super();
		this.operationClassName = operationClassName;
		this.parameters = parameters;
	}

	/**
	 * Gets the operation class name.
	 *
	 * @return the operationClassName
	 */
	public String getOperationClassName() {
		return this.operationClassName;
	}

	/**
	 * Sets the operation class name.
	 *
	 * @param operationClassName
	 *            the operationClassName to set
	 */
	public void setOperationClassName(String operationClassName) {
		this.operationClassName = operationClassName;
	}

	/**
	 * Gets the parameters.
	 *
	 * @return the parameters
	 */
	public Map<String, Object> getParameters() {
		return this.parameters;
	}

	/**
	 * Sets the parameters.
	 *
	 * @param parameters
	 *            the parameters to set
	 */
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

}

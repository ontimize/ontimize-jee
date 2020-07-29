package com.ontimize.jee.server.soa;

import java.io.Serializable;

/**
 * The Class ServiceExceptionDetails.
 */
public class ServiceExceptionDetails implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The fault code. */
    private String faultCode;

    /** The fault message. */
    private String faultMessage;

    /**
     * Instantiates a new service exception details.
     */
    public ServiceExceptionDetails() {
    }

    /**
     * Gets the fault code.
     * @return the fault code
     */
    public String getFaultCode() {
        return this.faultCode;
    }

    /**
     * Sets the fault code.
     * @param faultCode the new fault code
     */
    public void setFaultCode(String faultCode) {
        this.faultCode = faultCode;
    }

    /**
     * Gets the fault message.
     * @return the fault message
     */
    public String getFaultMessage() {
        return this.faultMessage;
    }

    /**
     * Sets the fault message.
     * @param faultMessage the new fault message
     */
    public void setFaultMessage(String faultMessage) {
        this.faultMessage = faultMessage;
    }

}

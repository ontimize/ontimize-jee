package com.ontimize.jee.server.soa;

import java.io.Serializable;

/**
 * The Class ServiceException.
 */
public class ServiceException extends Exception implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The fault details. */
    private final ServiceExceptionDetails faultDetails[];

    /**
     * Instantiates a new service exception.
     * @param faultDetails the fault details
     */
    public ServiceException(ServiceExceptionDetails faultDetails[]) {
        this.faultDetails = faultDetails;
    }

    /**
     * Instantiates a new service exception.
     * @param message the message
     * @param faultDetails the fault details
     */
    public ServiceException(String message, ServiceExceptionDetails faultDetails[]) {
        super(message);
        this.faultDetails = faultDetails;
    }

    /**
     * Gets the fault details.
     * @return the fault details
     */
    public ServiceExceptionDetails[] getFaultDetails() {
        return this.faultDetails;
    }

}

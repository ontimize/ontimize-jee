package com.ontimize.jee.common.services.remoteoperation;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * The Class RemoteOperationErrorMessage.
 */
public class RemoteOperationErrorMessage implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The error. */
    private Throwable cause;

    public RemoteOperationErrorMessage() {
        super();
    }

    /**
     * Instantiates a new remote operation error message.
     * @param cause the cause
     */
    public RemoteOperationErrorMessage(Throwable cause) {
        super();
        this.cause = cause;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    /**
     * Gets the cause.
     * @return the cause
     */
    public Throwable getCause() {
        return this.cause;
    }

    /**
     * Sets the cause.
     * @param cause the new cause
     */
    public void setCause(Throwable cause) {
        this.cause = cause;
    }

}

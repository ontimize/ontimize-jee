package com.ontimize.jee.common.services.remoteoperation;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * The Class RemoteOperationCancelMessage.
 */
public class RemoteOperationCancelMessage implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    private String cause;

    /**
     * Instantiates a new remote operation cancel message.
     */
    public RemoteOperationCancelMessage() {
        super();
    }

    public RemoteOperationCancelMessage(String cause) {
        super();
        this.cause = cause;
    }

    public String getCause() {
        return this.cause;
    }

    public void setCause(String cause) {
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

}

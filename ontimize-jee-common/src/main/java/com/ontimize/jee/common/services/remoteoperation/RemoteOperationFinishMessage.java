package com.ontimize.jee.common.services.remoteoperation;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * The Class RemoteOperationFinishMessage.
 */
public class RemoteOperationFinishMessage implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The error. */
    private boolean error;

    /** The result. */
    private Object result;

    public RemoteOperationFinishMessage() {
        super();
    }

    /**
     * Instantiates a new remote operation finish message.
     * @param error the error
     * @param result the result
     */
    public RemoteOperationFinishMessage(boolean error, Object result) {
        super();
        this.error = error;
        this.result = result;
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
     * Checks if is error.
     * @return the error
     */
    public boolean isError() {
        return this.error;
    }

    /**
     * Sets the error.
     * @param error the error to set
     */
    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * Gets the result.
     * @return the result
     */
    public Object getResult() {
        return this.result;
    }

    /**
     * Sets the result.
     * @param result the result to set
     */
    public void setResult(Object result) {
        this.result = result;
    }

}

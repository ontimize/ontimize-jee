package com.ontimize.jee.common.services.remoteoperation;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.ontimize.jee.common.services.remoteoperation.RemoteOperationStatuses.RemoteOperationStatus;

// TODO: Auto-generated Javadoc
/**
 * The Class RemoteOperationStatusMessage.
 */
public class RemoteOperationStatusMessage implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The status. */
    private RemoteOperationStatus status;

    /** The current step. */
    private int currentStep;

    /** The max steps. */
    private int maxSteps;

    /** The estimated time. */
    private long estimatedTime;

    /** The message. */
    private String message;

    /** The current execution information. */
    private Object extraInformation;

    public RemoteOperationStatusMessage() {
        super();
    }

    /**
     * Instantiates a new remote operation status message.
     * @param status the status
     * @param currentStep the current step
     * @param maxSteps the max steps
     * @param estimatedTime the estimated time
     * @param message the message
     * @param extraInformation the extra information
     */
    public RemoteOperationStatusMessage(RemoteOperationStatus status, int currentStep, int maxSteps, long estimatedTime,
            String message, Object extraInformation) {
        super();
        this.status = status;
        this.currentStep = currentStep;
        this.maxSteps = maxSteps;
        this.message = message;
        this.extraInformation = extraInformation;
        this.estimatedTime = estimatedTime;
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
     * Gets the status.
     * @return the status
     */
    public RemoteOperationStatus getStatus() {
        return this.status;
    }

    /**
     * Sets the status.
     * @param status the status to set
     */
    public void setStatus(RemoteOperationStatus status) {
        this.status = status;
    }

    /**
     * Gets the current step.
     * @return the currentStep
     */
    public int getCurrentStep() {
        return this.currentStep;
    }

    /**
     * Sets the current step.
     * @param currentStep the currentStep to set
     */
    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    /**
     * Gets the max steps.
     * @return the maxSteps
     */
    public int getMaxSteps() {
        return this.maxSteps;
    }

    /**
     * Sets the max steps.
     * @param maxSteps the maxSteps to set
     */
    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    /**
     * Gets the message.
     * @return the message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Sets the message.
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the extra information.
     * @return the extraInformation
     */
    public Object getExtraInformation() {
        return this.extraInformation;
    }

    /**
     * Sets the extra information.
     * @param extraInformation the extraInformation to set
     */
    public void setExtraInformation(Object extraInformation) {
        this.extraInformation = extraInformation;
    }

    /**
     * Gets the estimated time.
     * @return the estimatedTime
     */
    public long getEstimatedTime() {
        return this.estimatedTime;
    }

    /**
     * Sets the estimated time.
     * @param estimatedTime the estimatedTime to set
     */
    public void setEstimatedTime(long estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

}

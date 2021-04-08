/*
 *
 */
package com.ontimize.jee.desktopclient.components.task;

/**
 * The Class WorkerStatusInfo.
 */
public class WorkerStatusInfo {

    /** The state. */
    private final String state;

    /** The estimated time. */
    private final Long estimatedTime;

    /** The progress. */
    private final Integer progress;

    /**
     * Instantiates a new worker status info.
     * @param state the state
     * @param estimatedTime the estimated time
     * @param progress the progress
     */
    public WorkerStatusInfo(String state, Long estimatedTime, Integer progress) {
        super();
        this.state = state;
        this.estimatedTime = estimatedTime;
        this.progress = progress;
    }

    /**
     * Gets the state.
     * @return the state
     */
    public String getState() {
        return this.state;
    }

    /**
     * Gets the estimated time.
     * @return the estimated time
     */
    public Long getEstimatedTime() {
        return this.estimatedTime;
    }

    /**
     * Gets the progress.
     * @return the progress
     */
    public Integer getProgress() {
        return this.progress;
    }

}

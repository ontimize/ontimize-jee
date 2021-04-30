package com.ontimize.jee.desktopclient.components.taskmanager;

public enum TaskStatus {

    /** The on prepare. */
    ON_PREPARE("Preparing", false),
    /** The downloading. */
    RUNNING("Downloading", false),
    /** The paused. */
    PAUSED("Paused", false),
    /** The completed. */
    COMPLETED("Complete", true),
    /** The cancelled. */
    CANCELLED("Cancelled", true),
    /** The error. */
    ERROR("Error", true);

    /** The name. */
    String name;

    /** The finish state. */
    private boolean finishState;

    /**
     * Instantiates a new status.
     * @param name the name
     * @param finishState the finish state
     */
    TaskStatus(String name, boolean finishState) {
        this.name = name;
        this.finishState = finishState;
    }

    /**
     * Checks if is finish state.
     * @return true, if is finish state
     */
    public boolean isFinishState() {
        return this.finishState;
    }

    /**
     * Gets the name.
     * @return the name
     */
    public String getName() {
        return this.name;
    }

}

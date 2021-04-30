/*
 *
 */
package com.ontimize.jee.desktopclient.components.task;

/**
 * The Class SwingWorkerStatus.
 */
public class SwingWorkerStatus {

    /**
     * The Enum SwingWorkerStatusType.
     */
    public enum SwingWorkerStatusType {

        /** The start. */
        START,
        /** The error. */
        ERROR,
        /** The update status. */
        UPDATE_STATUS,
        /** The cancel. */
        CANCEL,
        /** The INTERMEDIAT e_ stat e_1. */
        INTERMEDIATE_STATE_1,
        /** The INTERMEDIAT e_ stat e_2. */
        INTERMEDIATE_STATE_2,
        /** The ok. */
        OK

    }

    /** The status type. */
    protected SwingWorkerStatusType statusType;

    /** The data. */
    protected Object data;

    /**
     * Instantiates a new swing worker status.
     * @param statusType the status type
     * @param data the data
     */
    public SwingWorkerStatus(SwingWorkerStatusType statusType, Object data) {
        this.statusType = statusType;
        this.data = data;
    }

    /**
     * Gets the status type.
     * @return the status type
     */
    public SwingWorkerStatusType getStatusType() {
        return this.statusType;
    }

    /**
     * Gets the data.
     * @return the data
     */
    public Object getData() {
        return this.data;
    }

}

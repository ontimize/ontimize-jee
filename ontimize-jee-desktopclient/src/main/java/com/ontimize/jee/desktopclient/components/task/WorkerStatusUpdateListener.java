/*
 *
 */
package com.ontimize.jee.desktopclient.components.task;

/**
 * The listener interface for receiving workerStatusUpdate events. The class that is interested in
 * processing a workerStatusUpdate event implements this interface, and the object created with that
 * class is registered with a component using the component's
 * <code>addWorkerStatusUpdateListener<code> method. When the workerStatusUpdate event occurs, that
 * object's appropriate method is invoked.
 *
 * @see WorkerStatusUpdateEvent
 */
public interface WorkerStatusUpdateListener {

    /**
     * Invoked when status update occurs.
     * @param source the source
     * @param info the info
     */
    void statusUpdated(OSwingWorker<?, ?> source, WorkerStatusInfo info);

}

/**
 * TaskControl.java 04-oct-2016
 *
 * Copyright 2016 INDITEX. Departamento de Sistemas
 */
package com.ontimize.jee.webclient.export.executor;

/**
 * Proporciona operaciones de control de un task.
 *
 *
 */
public interface TaskControl {

    /**
     * Chequea si cancelled.
     * @return true, si cancelled
     */
    boolean isCancelled();

    /**
     * Update progress.
     * @param workDone work done
     * @param max max
     */
    void updateProgress(double workDone, double max);

    /**
     * Update progress.
     * @param workDone work done
     * @param max max
     */
    void updateProgress(long workDone, long max);

    /**
     * Update title.
     * @param title title
     */
    void updateTitle(String title);

    /**
     * Update message.
     * @param message message
     */
    void updateMessage(String message);

}

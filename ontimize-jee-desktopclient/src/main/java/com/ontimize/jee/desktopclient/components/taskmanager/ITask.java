package com.ontimize.jee.desktopclient.components.taskmanager;

import java.util.Observable;

/**
 * The Interface ITask.
 */
public interface ITask {

    /**
     * Gets the name.
     * @return the name
     */
    String getName();

    /**
     * The description.
     * @return the description
     */
    String getDescription();

    /**
     * Gets the size.
     * @return the size
     */
    Number getSize();

    /**
     * Gets the progress.
     * @return the progress
     */
    Number getProgress();

    /**
     * Gets the status.
     * @return the status
     */
    TaskStatus getStatus();

    /**
     * Pause.
     */
    void pause();

    /**
     * Resume.
     */
    void resume();

    /**
     * Cancel.
     */
    void cancel();

    /**
     * Gets the observable.
     * @return the observable
     */
    Observable getObservable();

    /**
     * On task clicked.
     */
    void onTaskClicked();

    /**
     * Checks if is finished.
     * @return true, if is finished
     */
    boolean isFinished();

    /**
     * Checks if is pausable.
     * @return true, if is pausable
     */
    boolean isPausable();

    /**
     * Checks if is cancellable.
     * @return true, if is cancellable
     */
    boolean isCancellable();

    /**
     * Checks for result details.
     * @return true, if successful
     */
    boolean hasResultDetails();

}

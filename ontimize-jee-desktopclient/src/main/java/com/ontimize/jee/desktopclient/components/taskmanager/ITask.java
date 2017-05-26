package com.ontimize.jee.desktopclient.components.taskmanager;

import java.util.Observable;

/**
 * The Interface ITask.
 */
public interface ITask {

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	String getName();

	/** The description. */
	String getDescription();

	/**
	 * Gets the size.
	 *
	 * @return the size
	 */
	Number getSize();

	/**
	 * Gets the progress.
	 *
	 * @return the progress
	 */
	Number getProgress();

	/**
	 * Gets the status.
	 *
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
	 *
	 * @return the observable
	 */
	Observable getObservable();

	/**
	 * On task clicked.
	 */
	void onTaskClicked();
}

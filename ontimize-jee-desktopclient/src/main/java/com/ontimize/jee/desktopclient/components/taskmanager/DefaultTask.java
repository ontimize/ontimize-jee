package com.ontimize.jee.desktopclient.components.taskmanager;

import java.util.Observable;

public class DefaultTask extends Observable implements ITask {

	private String		name;
	private String		description;
	private Number		size;
	private Number		progress;
	private TaskStatus	status;

	public DefaultTask() {
		super();
		this.status = TaskStatus.ON_PREPARE;
	}

	public DefaultTask(String name) {
		this();
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
		this.notifyChanged();
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
		this.notifyChanged();
	}

	@Override
	public Number getSize() {
		return this.size;
	}

	public void setSize(Number size) {
		this.size = size;
		this.notifyChanged();
	}

	@Override
	public Number getProgress() {
		return this.progress;
	}

	public void setProgress(Number progress) {
		this.progress = progress;
		this.notifyChanged();
	}

	@Override
	public TaskStatus getStatus() {
		return this.status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
		this.notifyChanged();
	}

	public void updateProgress(Number progress, TaskStatus status) {
		this.progress = progress;
		this.status = status;
		this.notifyChanged();
	}

	public void updateProgress(Number progress, String description, TaskStatus status) {
		this.progress = progress;
		this.status = status;
		this.description = description;
		this.notifyChanged();
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void cancel() {

	}

	@Override
	public Observable getObservable() {
		return this;
	}

	@Override
	public void onTaskClicked() {

	}

	protected void notifyChanged() {
		this.setChanged();
		this.notifyObservers();
	}

}

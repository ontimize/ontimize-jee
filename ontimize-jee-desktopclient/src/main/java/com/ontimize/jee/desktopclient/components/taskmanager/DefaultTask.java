package com.ontimize.jee.desktopclient.components.taskmanager;

import java.util.Observable;

import com.ontimize.jee.common.tools.ObjectTools;

/**
 * The Class DefaultTask.
 */
public class DefaultTask extends Observable implements ITask {

    /** The name. */
    private String name;

    /** The description. */
    private String description;

    /** The size. */
    private Number size;

    /** The progress. */
    private Number progress;

    /** The status. */
    private TaskStatus status;

    /**
     * Instantiates a new default task.
     */
    public DefaultTask() {
        super();
        this.status = TaskStatus.ON_PREPARE;
    }

    /**
     * Instantiates a new default task.
     * @param name the name
     */
    public DefaultTask(String name) {
        this();
        this.name = name;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.desktopclient.components.taskmanager.ITask#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
        this.notifyChanged();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.desktopclient.components.taskmanager.ITask#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the description.
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
        this.notifyChanged();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.desktopclient.components.taskmanager.ITask#getSize()
     */
    @Override
    public Number getSize() {
        return this.size;
    }

    /**
     * Sets the size.
     * @param size the new size
     */
    public void setSize(Number size) {
        this.size = size;
        this.notifyChanged();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.desktopclient.components.taskmanager.ITask#getProgress()
     */
    @Override
    public Number getProgress() {
        return this.progress;
    }

    /**
     * Sets the progress.
     * @param progress the new progress
     */
    public void setProgress(Number progress) {
        this.progress = progress;
        this.notifyChanged();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.desktopclient.components.taskmanager.ITask#getStatus()
     */
    @Override
    public TaskStatus getStatus() {
        return this.status;
    }

    /**
     * Sets the status.
     * @param status the new status
     */
    public void setStatus(TaskStatus status) {
        this.status = status;
        this.notifyChanged();
    }

    /**
     * Update progress.
     * @param progress the progress
     * @param status the status
     */
    public void updateProgress(Number progress, TaskStatus status) {
        this.progress = progress;
        this.status = status;
        this.notifyChanged();
    }

    /**
     * Update progress.
     * @param progress the progress
     * @param description the description
     * @param status the status
     */
    public void updateProgress(Number progress, String description, TaskStatus status) {
        this.progress = progress;
        this.status = status;
        this.description = description;
        this.notifyChanged();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.desktopclient.components.taskmanager.ITask#pause()
     */
    @Override
    public void pause() {
        // do nothing
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.desktopclient.components.taskmanager.ITask#resume()
     */
    @Override
    public void resume() {
        // do nothing
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.desktopclient.components.taskmanager.ITask#cancel()
     */
    @Override
    public void cancel() {
        // do nothing
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.desktopclient.components.taskmanager.ITask#getObservable()
     */
    @Override
    public Observable getObservable() {
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.desktopclient.components.taskmanager.ITask#onTaskClicked()
     */
    @Override
    public void onTaskClicked() {
        // Do nothing
    }

    /**
     * Notify changed.
     */
    protected void notifyChanged() {
        this.setChanged();
        this.notifyObservers();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.desktopclient.components.taskmanager.ITask#isFinished()
     */
    @Override
    public boolean isFinished() {
        return ObjectTools.isIn(this.getStatus(), TaskStatus.COMPLETED, TaskStatus.ERROR);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.desktopclient.components.taskmanager.ITask#isPausable()
     */
    @Override
    public boolean isPausable() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.desktopclient.components.taskmanager.ITask#isCancellable()
     */
    @Override
    public boolean isCancellable() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.desktopclient.components.taskmanager.ITask#hasResultDetails()
     */
    @Override
    public boolean hasResultDetails() {
        return false;
    }

}

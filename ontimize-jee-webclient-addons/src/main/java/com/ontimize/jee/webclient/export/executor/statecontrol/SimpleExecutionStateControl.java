/**
 * SimpleExecutionStateControl.java 15-nov-2017
 *
 * Copyright 2017 INDITEX. Departamento de Sistemas
 */
package com.ontimize.jee.webclient.export.executor.statecontrol;

import com.ontimize.jee.webclient.export.executor.ExecutionStateControl;
import com.ontimize.jee.webclient.export.executor.callback.OnFailed;
import com.ontimize.jee.webclient.export.executor.callback.OnSucceeded;

import javafx.concurrent.WorkerStateEvent;


/**
 * The Class SimpleExecutionStateControl.
 *
 * @author <a href="albertovl@inditex.com">Alberto Valina Lema</a>
 * @param <T> tipo generico
 */
public class SimpleExecutionStateControl<T> implements ExecutionStateControl<T> {

    private final OnSucceeded<T> onSucceeded;

    private final OnFailed onFailed;

    /**
     * Instancia un nuevo simple execution state control.
     * @param onSucceeded on succeeded
     * @param onFailed on failed
     */
    public SimpleExecutionStateControl(final OnSucceeded<T> onSucceeded, final OnFailed onFailed) {
        super();
        this.onSucceeded = onSucceeded;
        this.onFailed = onFailed;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.inditex.aqsw.libjfxcomponents.base.executor.ExecutionStateControl#onSucceeded(javafx.
     * concurrent. WorkerStateEvent, java.lang.Object)
     */
    @Override
    public void onSucceeded(final WorkerStateEvent workerStateEvent, final T value) {
        if (this.onSucceeded != null) {
            this.onSucceeded.run(value);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.inditex.aqsw.libjfxcomponents.base.executor.ExecutionStateControl#onFailed(javafx.concurrent.
     * WorkerStateEvent, java.lang.Throwable)
     */
    @Override
    public void onFailed(final WorkerStateEvent workerStateEvent, final Throwable throwable) {
        if (this.onFailed != null) {
            this.onFailed.run(throwable);
        }
    }

}

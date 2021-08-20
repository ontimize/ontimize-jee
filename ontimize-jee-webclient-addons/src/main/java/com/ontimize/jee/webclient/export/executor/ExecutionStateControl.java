/**
 * ExecutionStateControl.java 04-oct-2016
 *
 * Copyright 2016 INDITEX. Departamento de Sistemas
 */
package com.ontimize.jee.webclient.export.executor;

import javafx.concurrent.WorkerStateEvent;

/**
 * Define la gestion del control de los diferentes estados por los que pasa una ejecucion.
 *
 * @param <T> tipo generico
 */
public interface ExecutionStateControl<T> {

    /**
     * On succeeded.
     * @param workerStateEvent worker state event
     * @param value value
     */
    default void onSucceeded(final WorkerStateEvent workerStateEvent,
            final T value) {

    }

    /**
     * On failed.
     * @param workerStateEvent worker state event
     * @param throwable throwable
     */
    default void onFailed(final WorkerStateEvent workerStateEvent,
            final Throwable throwable) {

    }

    /**
     * On cancelled.
     * @param workerStateEvent worker state event
     */
    default void onCancelled(final WorkerStateEvent workerStateEvent) {

    }

    /**
     * On running.
     * @param workerStateEvent worker state event
     */
    default void onRunning(final WorkerStateEvent workerStateEvent) {

    }

}

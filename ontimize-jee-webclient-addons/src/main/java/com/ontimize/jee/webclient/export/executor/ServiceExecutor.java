/**
 * ServiceExecutor.java 04-oct-2016
 *
 * Copyright 2016 INDITEX. Departamento de Sistemas
 */
package com.ontimize.jee.webclient.export.executor;


import com.ontimize.jee.webclient.export.executor.callback.OnFailed;
import com.ontimize.jee.webclient.export.executor.callback.OnSucceeded;

/**
 * Ejecutor de tareas. Permite conocer el estado de ejecucion de la tarea y controlar el flujo de
 * ejecucion.
 *
 *
 */
public interface ServiceExecutor {

    /**
     * Execute.
     * @param <T> tipo generico
     * @param callable callable
     * @param workerConfigurer worker configurer
     * @param executionStateControl execution state control
     * @param workerListeners worker listeners
     * @return the execution result
     */
    <T> ExecutionResult<T> execute(CallableTask<T> callable, WorkerConfigurer<T> workerConfigurer,
            ExecutionStateControl<T> executionStateControl, WorkerListener... workerListeners);

    /**
     * Execute.
     * @param <T> tipo generico
     * @param callable callable
     * @param onSucceeded on succeeded
     * @param onFailed on failed
     * @param workerListeners worker listeners
     * @return the execution result
     */
    <T> ExecutionResult<T> execute(CallableTask<T> callable, OnSucceeded<T> onSucceeded, OnFailed onFailed,
            WorkerListener... workerListeners);

}

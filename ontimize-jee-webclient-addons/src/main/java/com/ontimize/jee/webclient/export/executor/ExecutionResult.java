/**
 * ExecutionResult.java 04-oct-2016
 *
 * Copyright 2016 INDITEX. Departamento de Sistemas
 */
package com.ontimize.jee.webclient.export.executor;

import java.util.concurrent.Future;

import javafx.concurrent.Worker;

/**
 * Define el resultado de una ejecucion.
 *
 * @param <T> tipo generico
 */
public class ExecutionResult<T> {

    /** The worker. */
    private Worker<T> worker;

    /** The future. */
    private Future<T> future;

    /**
     * Instancia un nuevo execution result.
     */
    public ExecutionResult() {
        // default constructor
    }

    /**
     * Instancia un nuevo execution result.
     * @param worker worker
     * @param future future
     */
    public ExecutionResult(final Worker<T> worker, final Future<T> future) {
        super();
        this.worker = worker;
        this.future = future;
    }

    /**
     * Obtiene worker.
     * @return worker
     */
    public Worker<T> getWorker() {
        return this.worker;
    }

    /**
     * Establece worker.
     * @param worker nuevo worker
     */
    public void setWorker(final Worker<T> worker) {
        this.worker = worker;
    }

    /**
     * Obtiene future.
     * @return future
     */
    public Future<T> getFuture() {
        return this.future;
    }

    /**
     * Establece future.
     * @param future nuevo future
     */
    public void setFuture(final Future<T> future) {
        this.future = future;
    }

}

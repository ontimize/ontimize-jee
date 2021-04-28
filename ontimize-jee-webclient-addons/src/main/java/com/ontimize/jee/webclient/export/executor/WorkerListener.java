/**
 * WorkerListener.java 04-oct-2016
 *
 * Copyright 2016 INDITEX. Departamento de Sistemas
 */
package com.ontimize.jee.webclient.export.executor;

import javafx.concurrent.Worker;

/**
 * Define eventos referidos a un {@link Worker}
 *
 *
 */
public interface WorkerListener {

    /**
     * On start.
     * @param <T> tipo generico
     * @param worker worker
     */
    <T> void onStart(Worker<T> worker);

    /**
     * On finish.
     * @param <T> tipo generico
     * @param worker worker
     */
    <T> void onFinish(Worker<T> worker);

}

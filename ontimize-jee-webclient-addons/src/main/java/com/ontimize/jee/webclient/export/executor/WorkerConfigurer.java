/**
 * WorkerConfigurer.java 04-oct-2016
 *
 * Copyright 2016 INDITEX. Departamento de Sistemas
 */
package com.ontimize.jee.webclient.export.executor;

import javafx.concurrent.Worker;

/**
 * Permite configurar un {@link Worker}
 *
 * @param <T> tipo generico
 */
public interface WorkerConfigurer<T> {

    /**
     * Configure.
     * @param worker worker
     */
    void configure(Worker<T> worker);

}

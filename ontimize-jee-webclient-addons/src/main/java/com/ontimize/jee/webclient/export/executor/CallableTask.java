/**
 * CallableTask.java 04-oct-2016
 *
 * Copyright 2016 INDITEX. Departamento de Sistemas
 */
package com.ontimize.jee.webclient.export.executor;

import java.util.concurrent.Future;

/**
 * Define la ejecucion de un task y la forma de recupear el resultado mediante un {@link Future}. Se
 * proporciona un {@link TaskControl} con el ofrecer control sobre dicha tarea.
 *
 * @param <T> tipo generico
 */
@FunctionalInterface
public interface CallableTask<T> {

    /**
     * Call.
     * @param taskControl task control
     * @return the future
     */
    Future<T> call(TaskControl taskControl);

}

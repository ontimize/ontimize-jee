/**
 * OnSucceeded.java 15-nov-2017
 *
 * Copyright 2017 INDITEX. Departamento de Sistemas
 */
package com.ontimize.jee.webclient.export.executor.callback;

/**
 * The Interface OnSucceeded.
 *
 * @author <a href="albertovl@inditex.com">Alberto Valina Lema</a>
 * @param <T> tipo generico
 */
@FunctionalInterface
public interface OnSucceeded<T> {

    /**
     * On succeeded.
     * @param value value
     */
    void run(final T value);

}

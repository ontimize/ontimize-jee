/**
 * OnSucceeded.java 15-nov-2017
 *
 * Copyright 2017 INDITEX. Departamento de Sistemas
 */
package com.ontimize.jee.webclient.export.executor.callback;

/**
 * The Interface OnFailed.
 *
 * @author <a href="albertovl@inditex.com">Alberto Valina Lema</a>
 */
@FunctionalInterface
public interface OnFailed {

    /**
     * Run.
     * @param throwable throwable
     */
    void run(final Throwable throwable);

}

/**
 * AsyncResult.java 18-ene-2017
 *
 * Copyright 2017 INDITEX. Departamento de Sistemas
 */

package com.ontimize.jee.webclient.export.executor;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * The Class AsyncResult. Clase basada en el org.springframework.scheduling.annotation.AsyncResult
 * para ser usada en el ambito de JFXComponents, sin necesidad de incluir dependencia con Spring.
 *
 * @author <a href="ReneBL@servicioexterno.inditex.com">Rene Balay Lorenzo</a>
 * @param <V> tipo de valor
 */
public class AsyncResult<V> implements Future<V> {

    private final V value;

    /**
     * Create a new AsyncResult holder.
     * @param value the value to pass through
     */
    public AsyncResult(final V value) {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCancelled() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDone() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V get() {
        return this.value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V get(final long timeout, final TimeUnit unit) {
        return this.value;
    }

}

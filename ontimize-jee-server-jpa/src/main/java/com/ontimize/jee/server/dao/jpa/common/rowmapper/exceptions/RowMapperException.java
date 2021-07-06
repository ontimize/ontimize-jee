/**
 * RowMapperException.java 04/04/2014
 *
 * Copyright 2014 IMATIA. Departamento de Sistemas
 */
package com.ontimize.jee.server.dao.jpa.common.rowmapper.exceptions;

/**
 * The Class RowMapperException.
 *
 * @author <a href="">Sergio Padin</a>
 */
public class RowMapperException extends Exception {

    private static final long serialVersionUID = 5681554360262899245L;

    /**
     * Instancia un nuevo row mapper exception.
     */
    public RowMapperException() {
        super();
    }

    /**
     * Instancia un nuevo row mapper exception.
     * @param message message
     * @param cause cause
     */
    public RowMapperException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Instancia un nuevo row mapper exception.
     * @param message message
     */
    public RowMapperException(final String message) {
        super(message);
    }

    /**
     * Instancia un nuevo row mapper exception.
     * @param cause cause
     */
    public RowMapperException(final Throwable cause) {
        super(cause);
    }

}

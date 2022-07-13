package com.ontimize.jee.webclient.export.exception;

import com.ontimize.jee.common.exceptions.OntimizeJEEException;

/**
 * The Class ExportException.
 */
public class ExportException extends OntimizeJEEException {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new export exception.
     *
     * @param reason the reason
     */
    public ExportException(String reason) {
        super(reason);
    }

    /**
     * Instantiates a new export exception.
     */
    public ExportException() {
        super();
    }

    /**
     * Instantiates a new export exception.
     *
     * @param string the string
     * @param parent the parent
     */
    public ExportException(String string, Exception parent) {
        super(string, parent);
    }

    /**
     * Instantiates a new export exception.
     *
     * @param parent the parent
     */
    public ExportException(Exception parent) {
        super(parent);
    }

}
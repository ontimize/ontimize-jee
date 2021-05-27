/**
 * ExportResult.java 22-jun-2017
 *
 * Copyright 2017 Imatia.com
 */
package com.ontimize.jee.webclient.export.util;

/**
 * The Class ExportResult.
 *
 * @author <a href="daniel.grana@imatia.com">Daniel Grana</a>
 */
public class ExportResult {

    /** The Constant RESULT_CODE_OK. */
    public static final int RESULT_CODE_OK = 0;

    /** The Constant RESULT_CODE_ERROR. */
    public static final int RESULT_CODE_ERROR = -1;

    private int code;

    private String message;

    /**
     * Instancia un nuevo export result.
     */
    public ExportResult() {
    }

    /**
     * Instancia un nuevo export result.
     * @param code code
     */
    public ExportResult(final int code) {
        this.code = code;
    }

    /**
     * Instancia un nuevo export result.
     * @param code code
     * @param message message
     */
    public ExportResult(final int code, final String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Obtiene result code.
     * @return result code
     */
    public int getResultCode() {
        return this.code;
    }

    /**
     * Establece result code.
     * @param code nuevo result code
     */
    public void setResultCode(final int code) {
        this.code = code;
    }

    /**
     * Obtiene message.
     * @return message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Establece message.
     * @param message nuevo message
     */
    public void setMessage(final String message) {
        this.message = message;
    }

}

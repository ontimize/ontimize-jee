/**
 * PaginationResult.java 07-may-2018
 *
 * Copyright 2018 Imatia.com
 */
package com.ontimize.jee.webclient.export.pagination;

/**
 * The Class PaginationResult.
 *
 * @author <a href="rubenab@imatia.com">Rubén Anido</a>
 * @param <B> tipo de la colección que tendrá el listado de los resultados
 */
public class PaginationResult<B> {

    private long totalSize;

    private int start;

    private B result;

    /**
     * Instancia un nuevo pagination result.
     * @param totalSize total size
     * @param result result
     */
    public PaginationResult(final long totalSize, final int start, final B result) {
        this.totalSize = totalSize;
        this.start = start;
        this.result = result;
    }

    /**
     * Obtiene total size.
     * @return total size
     */
    public long getTotalSize() {
        return this.totalSize;
    }

    /**
     * Establece total size.
     * @param totalSize nuevo total size
     */
    public void setTotalSize(final long totalSize) {
        this.totalSize = totalSize;
    }

    /**
     * Obtiene start.
     * @return start
     */
    public int getStart() {
        return this.start;
    }

    /**
     * Establece start.
     * @param start nuevo start
     */
    public void setStart(final int start) {
        this.start = start;
    }

    /**
     * Obtiene result.
     * @return result
     */
    public B getResult() {
        return this.result;
    }

    /**
     * Establece result.
     * @param result nuevo result
     */
    public void setResult(final B result) {
        this.result = result;
    }

}

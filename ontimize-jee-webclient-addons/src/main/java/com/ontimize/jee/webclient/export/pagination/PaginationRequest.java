/**
 * PaginationRequest.java 06-jun-2018
 *
 * Copyright 2018 Imatia.com
 */
package com.ontimize.jee.webclient.export.pagination;

import javafx.scene.control.IndexRange;

/**
 * The Class PaginationRequest.
 *
 * @author <a href="rubenab@imatia.com">Rubén Anido</a>
 */
public class PaginationRequest {

    private final IndexRange rowsRange;

    private Integer currentPage = 0;

    private Integer rowsPerPage = 0;

    private long totalSize = 0;

    /**
     * Instancia un nuevo pagination request.
     * @param currentPage current page
     * @param rowsPerPage rows per page
     * @param totalSize total size
     */
    public PaginationRequest(final Integer currentPage, final Integer rowsPerPage, final long totalSize) {
        this.currentPage = currentPage;
        this.rowsPerPage = rowsPerPage;
        this.totalSize = totalSize;
        this.rowsRange = getIndexRange();

    }

    /**
     * Obtiene current page.
     * @return current page
     */
    public Integer getCurrentPage() {
        if (this.currentPage == null) {
            this.currentPage = 0;
        }
        return this.currentPage;
    }

    /**
     * Obtiene rows per page.
     * @return rows per page
     */
    public Integer getRowsPerPage() {
        if (this.rowsPerPage == null) {
            this.rowsPerPage = 0;
        }
        return this.rowsPerPage;
    }

    /**
     * Obtiene total size.
     * @return total size
     */
    public long getTotalSize() {
        if (this.totalSize < 0) {
            this.totalSize = 0;
        }
        return this.totalSize;
    }

    /**
     * Obtiene rows range.
     * @return rows range
     */
    public IndexRange getRowsRange() {
        return this.rowsRange;
    }

    /**
     * Obtiene index range.
     * @return index range
     */
    protected IndexRange getIndexRange() {
        final int end = (getCurrentPage() * getRowsPerPage()) - 1;
        final int init = (end - getRowsPerPage()) + 1;

        return new IndexRange(init, end);
    }

}

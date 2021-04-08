package com.ontimize.jee.server.dao.jdbc;

/**
 * The Class PageableInfo.
 */
public class PageableInfo {

    /** The record number. */
    private final int recordNumber;

    /** The start index. */
    private final int startIndex;

    /**
     * Instantiates a new pageable info.
     * @param recordNumber the record number
     * @param startIndex the start index
     */
    public PageableInfo(int recordNumber, int startIndex) {
        super();
        this.recordNumber = recordNumber;
        this.startIndex = startIndex;
    }

    /**
     * Gets the record number.
     * @return the record number
     */
    public int getRecordNumber() {
        return this.recordNumber;
    }

    /**
     * Gets the start index.
     * @return the start index
     */
    public int getStartIndex() {
        return this.startIndex;
    }

}

package com.ontimize.jee.common.db;


import com.ontimize.jee.common.dto.EntityResult;

/**
 * Extended <code>EntityResult</code> that adds information about number of records and index of
 * first record of <code>ResultSet</code> contained.
 *
 * @see Table#QUERY_ROWS
 * @see PageFetcher
 * @author Imatia Innovation
 */
public interface AdvancedEntityResult extends EntityResult {

    int getStartRecordIndex();

    /**
     * Gets the total query records count
     * @return
     */
    public int getTotalRecordCount();

    /**
     * Sets the total query records count
     * @param totalQueryRecords
     */
    void setTotalRecordCount(int totalQueryRecords);

    /**
     * Gets the total records number that the object contains.
     * @return The object records number. When the object is empty return 0
     */
    int getCurrentRecordCount();

    void setStartRecordIndex(int startRecordIndex);

}

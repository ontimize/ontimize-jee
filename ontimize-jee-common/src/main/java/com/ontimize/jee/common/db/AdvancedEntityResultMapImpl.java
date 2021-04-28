package com.ontimize.jee.common.db;

import com.ontimize.jee.core.common.dto.EntityResultMapImpl;

public class AdvancedEntityResultMapImpl extends EntityResultMapImpl implements AdvancedEntityResult {

    int totalQueryRecordsNumber = 0;

    int startRecordIndex = 0;

    public void setStartRecordIndex(int startRecordIndex) {
        this.startRecordIndex = startRecordIndex;
    }

    public int getStartRecordIndex() {
        return startRecordIndex;
    }

    /**
     * Creates an AdvancedEntityResult with code value 'cod', with type 'type' and the message 'm'
     * @param cod
     * @param type
     * @param m
     */
    public AdvancedEntityResultMapImpl(int cod, int type, String m) {
        super(cod, type, m);
    }

    /**
     * Creates an AdvancedEntityResult with code 'cod' and type 'type'
     * @param cod
     * @param type
     */
    public AdvancedEntityResultMapImpl(int cod, int type) {
        super(cod, type);
    }

    /**
     * Creates an AdvancedEntityResult with code 'cod', type 'type' and with the message 'm', and the
     * total query records number equals to 'totalQueryRecords'
     * @param cod
     * @param type
     * @param m
     * @param totalQueryRecords
     */
    public AdvancedEntityResultMapImpl(int cod, int type, String m, int totalQueryRecords) {
        super(cod, type, m);
        this.setTotalRecordCount(totalQueryRecords);
    }

    /**
     * Creates an AdvancedEntityResult with code 'cod', type 'type', and the total query records number
     * equals to 'totalQueryRecords'
     * @param cod
     * @param type
     * @param totalQueryRecords
     */
    public AdvancedEntityResultMapImpl(int cod, int type, int totalQueryRecords) {
        super(cod, type);
        this.setTotalRecordCount(totalQueryRecords);
    }

    /**
     * Gets the total query records count
     * @return
     */
    public int getTotalRecordCount() {
        return this.totalQueryRecordsNumber;
    }

    /**
     * Sets the total query records count
     * @param totalQueryRecords
     */
    public void setTotalRecordCount(int totalQueryRecords) {
        totalQueryRecordsNumber = totalQueryRecords;
    }

    /**
     * Gets the total records number that the object contains.
     * @return The object records number. When the object is empty return 0
     */
    public int getCurrentRecordCount() {
        return this.calculateRecordNumber();
    }

}

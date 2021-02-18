package com.ontimize.jee.server.dao;

import java.util.Date;
import java.util.Map;

import com.ontimize.dto.EntityResult;

public interface IDownDateHelper {

    public static final String DOWNDATE_DEFAULT_ATTR = "DOWNDATE_DEFAULT";

    /**
     * Ensures to unsubscribe element referenced by received dao + keysvalues, ensuring to UPDATE
     * doenDate column
     * @param daoHelper DAOHelper used to UPDATE
     * @param dao DAO of element to downdate
     * @param downDateColumn Downdate column identifier
     * @param downDate Downdate, if not specified, current date will be used
     * @param keysValues Keys that identify element under received DAO
     * @return
     */
    EntityResult downRecord(DefaultOntimizeDaoHelper daoHelper, IOntimizeDaoSupport dao, String downDateColumn,
            Date downDate, Map<?, ?> keysValues);

    /**
     * Ensures to subscribe element referenced by received dao + keysvalues, ensuring to UPDATE doenDate
     * column
     * @param daoHelper DAOHelper used to UPDATE
     * @param dao DAO of element to downdate
     * @param downDateColumn Downdate column identifier
     * @param downDate Downdate, if not specified, current date will be used
     * @param keysValues Keys that identify element under received DAO
     * @return
     */
    EntityResult upRecord(DefaultOntimizeDaoHelper daoHelper, IOntimizeDaoSupport dao, String downDateColumn,
            Map<?, ?> keysValues);

    /**
     * Ensures to complete filters according to filters received. <br>
     * By default, only active records (downdate is null), y some filter about downdate then only this
     * ones. <br>
     * If daoKeyColumn is present, then ignore filter, assuming it is consulting explicit record.
     * @return true if contains downdate filter or it has been added
     * @param keysValues
     * @param downDateColumn
     * @return
     */
    boolean checkDowndateQueryKeys(Map keysValues, String downDateColumn, String... daoKeyColumn);

}

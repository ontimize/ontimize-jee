package com.ontimize.jee.common.db;

import java.util.List;
import java.util.Map;
import java.util.Vector;

public interface AdvancedEntity extends Entity {

    /**
     * @deprecated
     * @param kv
     * @param attributes
     * @param sessionId
     * @param recordNumber
     * @param startIndex
     * @param orderBy
     * @param desc
     * @return
     * @throws Exception
     */

    @Deprecated
    public AdvancedEntityResult query(Map kv, Vector attributes, int sessionId, int recordNumber, int startIndex,
            String orderBy, boolean desc) throws Exception;

    /**
     * @param kv a <code>Map</code> specifying conditions that must comply the set of records returned.
     *        Cannot be null.
     * @param attributes a list of columns that must be recovered for each record returned. Cannot be
     *        null. If empty, all attributes will be returned.
     * @param sessionId a integer identifying the user or session that performs the action.
     * @param recordNumber a integer establishing the number of records that will be returned.
     * @param startIndex a integer establishing the position of the first record that will be returned.
     * @param orderBy a <code>Vector</code> of String or {@link SQLStatementBuilder.SQLOrder} objects in
     *        where the column orderer is established.
     * @return
     * @throws Exception
     */
    public AdvancedEntityResult query(Map kv, List attributes, int sessionId, int recordNumber, int startIndex,
            List orderBy) throws Exception;

}

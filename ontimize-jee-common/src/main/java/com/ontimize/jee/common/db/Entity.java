package com.ontimize.jee.common.db;

import com.ontimize.jee.common.dto.EntityResult;

import java.util.List;
import java.util.Map;

/**
 * This interface defines the basic methods that must be implemented by any class (entity) that
 * provides and receives data from the Ontimize client classes (for example, forms and trees).
 * <p>
 * Defines four methods corresponding to the basic operations that generally can be done when
 * managing data:<br>
 * <ul>
 * <li>query</li>
 * <li>insert</li>
 * <li>update</li>
 * <li>delete</li>
 * </ul>
 * <p>
 * Classes that implement these methods must ensure that the argument objects passed to the methods
 * are not modified. This interface extends java.rmi.Remote so that methods of this interface can be
 * called using RMI, but this is not required, and instances of objects can be used locally.
 *
 * @author Imatia Innovation S.L.
 */
public interface Entity extends java.rmi.Remote {

    /**
     * This method must implement a standard query operation returning the set of data that matches the
     * conditions specified by the <code>keysValues</code> parameter. The <code>attributes</code>
     * defines which attributes (or columns if data is obtained from a database) must be recovered, and
     * <code>keysValues</code> specifies which set of records must be recovered.
     * <p>
     * @param keysValues a Map specifying conditions that must comply the set of records returned.
     *        Cannot be null.
     * @param attributes a list of columns or attributes that must be recovered for each record
     *        returned. Cannot be null. If empty, all attributes should be returned.
     * @param sessionId a integer identifying the user or session that performs the action.
     * @return a EntityResult with the resulting set of data. This result can be empty if no results
     *         exist, and if an error has ocurred this will be indicated in the result.
     * @throws Exception if any exception occurs
     */
    public EntityResult query(Map keysValues, List attributes, int sessionId) throws Exception;

    /**
     * This method must implement a standard insert operation with the data contained in
     * <code>attributesValues</code> parameter.<br>
     * <p>
     * @param attributesValues a Map specifying pairs of key-value corresponding to the attribute (or
     *        column of a table in a database) and the value that must be stored.
     * @param sessionId a integer identifying the user or session that performs the action.
     * @return a EntityResult. This result will have an error code if error has ocurred.
     * @throws Exception if any exception occurs
     */
    public EntityResult insert(Map attributesValues, int sessionId) throws Exception;

    /**
     * This method must implement a standard update operation with the data specified in
     * <code>attributesValues</code> over the set of records defined by <code>keysValues</code>. So,
     * only the records that comply with the conditions specified by <code>keysValues</code> will be
     * updated to the new values specified by <code>attributesValues</code>.
     * <p>
     * @param attributesValues the data for updating the records to. The keys specify the attributes (or
     *        columns) and the values, the values for these columns.
     * @param keysValues the conditions that the records to be updated must fulfill. The keys specify
     *        the attributes (or columns) and the values, the values for these columns.
     * @param sessionId a integer identifying the user or session that performs the action.
     * @return a EntityResult. This result will have an error code if error has occurred.
     * @throws Exception if any exception occurs
     */
    public EntityResult update(Map attributesValues, Map keysValues, int sessionId) throws Exception;

    /**
     * This method must implement a standard delete operation over the set of records defined by
     * <code>keysValues</code>. So, only the records that comply with the conditions specified by
     * <code>keysValues</code> will be deleted.
     * <p>
     * @param keysValues the conditions that the records to be deleted must fulfill. The keys specify
     *        the attributes (or columns) and the values, the values for these columns.
     * @param sessionId a integer identifying the user or session that performs the action.
     * @return a EntityResult. This result will have an error code if error has occurred.
     * @throws Exception if any exception occurs
     */
    public EntityResult delete(Map keysValues, int sessionId) throws Exception;

}

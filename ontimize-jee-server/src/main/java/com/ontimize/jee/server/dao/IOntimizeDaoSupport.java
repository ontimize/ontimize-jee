package com.ontimize.jee.server.dao;

import java.util.List;
import java.util.Map;

import com.ontimize.db.EntityResult;

public interface IOntimizeDaoSupport {

	/** The Constant DEFAULT_QUERY_KEY. */
	static final String DEFAULT_QUERY_KEY = "default";

	/**
	 * This method must implement a standard query operation returning the set of data that matches the conditions specified by the <code>keysValues</code> parameter. The
	 * <code>attributes</code> defines which attributes (or columns if data is obtained from a database) must be recovered, and <code>keysValues</code> specifies which set of
	 * records must be recovered. <p>
	 *
	 * @param keysValues
	 *            a Hashtable specifying conditions that must comply the set of records returned. Cannot be null.
	 * @param attributes
	 *            a list of columns or attributes that must be recovered for each record returned. Cannot be null. If empty, all attributes should be returned.
	 * @param sort
	 *            the sort
	 * @param queryId
	 *            the query id
	 * @return a EntityResult with the resulting set of data. This result can be empty if no results exist, and if an error has ocurred this will be indicated in the result.
	 */
	EntityResult query(Map<?, ?> keysValues, List<?> attributes, List<?> sort, String queryId);

	<T> List<T> query(Map<?, ?> keysValues, List<?> sort, String queryId, Class<T> clazz);

	/**
	 * This method must implement a standard insert operation with the data contained in <code>attributesValues</code> parameter.<br> <p>
	 *
	 * @param attributesValues
	 *            a Hashtable specifying pairs of key-value corresponding to the attribute (or column of a table in a database) and the value that must be stored.
	 * @return a EntityResult. This result will have an error code if error has ocurred.
	 */
	EntityResult insert(Map<?, ?> attributesValues);

	/**
	 * This method must implement a standard update operation with the data specified in <code>attributesValues</code> over the set of records defined by <code>keysValues</code>.
	 * So, only the records that comply with the conditions specified by <code>keysValues</code> will be updated to the new values specified by <code>attributesValues</code>. <p>
	 *
	 * @param attributesValues
	 *            the data for updating the records to. The keys specify the attributes (or columns) and the values, the values for these columns.
	 * @param keysValues
	 *            the conditions that the records to be updated must fulfill. The keys specify the attributes (or columns) and the values, the values for these columns.
	 * @return a EntityResult. This result will have an error code if error has occurred.
	 */
	EntityResult update(Map<?, ?> attributesValues, Map<?, ?> keysValues);

	/**
	 * This method must implement a standard delete operation over the set of records defined by <code>keysValues</code>. So, only the records that comply with the conditions
	 * specified by <code>keysValues</code> will be deleted. <p>
	 *
	 * @param keysValues
	 *            the conditions that the records to be deleted must fulfill. The keys specify the attributes (or columns) and the values, the values for these columns.
	 * @return a EntityResult. This result will have an error code if error has occurred.
	 */
	EntityResult delete(Map<?, ?> keysValues);

	/**
	 * Same action that delete but without validating the delete key
	 *
	 * @param keysValues
	 * @return
	 */
	EntityResult unsafeDelete(Map<?, ?> keysValues);

	/**
	 * Same action that update but without validating the update key
	 *
	 * @param attributesValues
	 * @param keysValues
	 * @return
	 */
	EntityResult unsafeUpdate(Map<?, ?> attributesValues, Map<?, ?> keysValues);

	/**
	 * Do execute insert batch.
	 *
	 * @param batch
	 *            the batch
	 * @return the int[] arrays of rows affected in every insert
	 */
	int[] insertBatch(Map<String, Object>[] batch);

	/**
	 * Get the propertis for Create Update Delete
	 *
	 * @return
	 */
	List<DaoProperty> getCudProperties();

	/**
	 * Force dao reload
	 */
	void reload();
}

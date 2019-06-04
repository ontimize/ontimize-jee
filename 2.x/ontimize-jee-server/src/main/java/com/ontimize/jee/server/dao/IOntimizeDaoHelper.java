/**
 * UserServerInformation.java 17/07/2013
 *
 *
 *
 */
package com.ontimize.jee.server.dao;

import java.util.List;
import java.util.Map;

import com.ontimize.db.AdvancedEntityResult;
import com.ontimize.db.EntityResult;
import com.ontimize.db.SQLStatementBuilder;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

/**
 * Obtiene la información del usuario con la que está trabajando el servidor.
 *
 * @author <a href=""></a>
 */
public interface IOntimizeDaoHelper {

	/**
	 * @param kv
	 *            a <code>Map<Object,Object></code> specifying conditions that must comply the set of records returned. Cannot be null.
	 * @param attributes
	 *            a list of columns that must be recovered for each record returned. Cannot be null. If empty, all attributes will be returned.
	 * @param recordNumber
	 *            a integer establishing the number of records that will be returned.
	 * @param startIndex
	 *            a integer establishing the position of the first record that will be returned.
	 * @param orderBy
	 *            a <code>List<Object></code> of String or {@link SQLStatementBuilder.SQLOrder} objects in where the column orderer is established.
	 * @return
	 * @throws Exception
	 */
	AdvancedEntityResult paginationQuery(IOntimizeDaoSupport repository, Map<?, ?> kv, List<?> attributes, int recordNumber, int startIndex, List<?> orderBy, String queryId)
			throws OntimizeJEERuntimeException;

	AdvancedEntityResult paginationQuery(IOntimizeDaoSupport repository, Map<?, ?> kv, List<?> attributes, int recordNumber, int startIndex, List<?> orderBy, String queryId,
			ISQLQueryAdapter adapter) throws OntimizeJEERuntimeException;

	/**
	 * This method must implement a standard query operation returning the set of data that matches the conditions specified by the <code>keysValues</code> parameter. The
	 * <code>attributes</code> defines which attributes (or columns if data is obtained from a database) must be recovered, and <code>keysValues</code> specifies which set of
	 * records must be recovered. <p>
	 *
	 * @param repository
	 *            DAO repository to look for.
	 * @param keysValues
	 *            a Map<Object,Object> specifying conditions that must comply the set of records returned. Cannot be null.
	 * @param attributes
	 *            a list of columns or attributes that must be recovered for each record returned. Cannot be null. If empty, all attributes should be returned.
	 * @param queryId
	 *            Query identifier to use under DAO repository.
	 * @return a EntityResult with the resulting set of data. This result can be empty if no results exist, and if an error has ocurred this will be indicated in the result.
	 * @throws Exception
	 *             if any exception occurs
	 */
	EntityResult query(IOntimizeDaoSupport repository, Map<?, ?> keysValues, List<?> attributes, String queryId) throws OntimizeJEERuntimeException;

	EntityResult query(IOntimizeDaoSupport repository, Map<?, ?> keysValues, List<?> attributes, String queryId, ISQLQueryAdapter adapter) throws OntimizeJEERuntimeException;

	/**
	 * This method must implement a standard query operation returning the set of data that matches the conditions specified by the <code>keysValues</code> parameter. The
	 * <code>attributes</code> defines which attributes (or columns if data is obtained from a database) must be recovered, and <code>keysValues</code> specifies which set of
	 * records must be recovered. <p>
	 *
	 * @param repository
	 *            DAO repository to look for.
	 * @param keysValues
	 *            a Map<Object,Object> specifying conditions that must comply the set of records returned. Cannot be null.
	 * @param attributes
	 *            a list of columns or attributes that must be recovered for each record returned. Cannot be null. If empty, all attributes should be returned.
	 * @param attributes
	 *            a list of columns to sort result. Can be null. If empty, no sort applyed.
	 * @param queryId
	 *            Query identifier to use under DAO repository.
	 * @return a EntityResult with the resulting set of data. This result can be empty if no results exist, and if an error has ocurred this will be indicated in the result.
	 * @throws Exception
	 *             if any exception occurs
	 */
	EntityResult query(IOntimizeDaoSupport dao, Map<?, ?> keysValues, List<?> attributes, List<?> sort, String queryId);

	EntityResult query(IOntimizeDaoSupport dao, Map<?, ?> keysValues, List<?> attributes, List<?> sort, String queryId, ISQLQueryAdapter adapter);

	/**
	 * Query using especific repository.
	 *
	 * @param <T>
	 *            the generic type of bean to be adquired
	 * @param repository
	 *            the repository to be queried
	 * @param keysValues
	 *            the keys values to filter
	 * @param sort
	 *            the sort
	 * @param queryId
	 *            the query id to use
	 * @param clazz
	 *            the clazz
	 * @return the list
	 */
	<T> List<T> query(IOntimizeDaoSupport repository, Map<?, ?> keysValues, List<?> sort, String queryId, Class<T> clazz);

	<T> List<T> query(IOntimizeDaoSupport repository, Map<?, ?> keysValues, List<?> sort, String queryId, Class<T> clazz, ISQLQueryAdapter adapter);

	/**
	 * This method must implement a standard insert operation with the data contained in <code>attributesValues</code> parameter.<br> <p>
	 *
	 * @param attributesValues
	 *            a Map<Object,Object> specifying pairs of key-value corresponding to the attribute (or column of a table in a database) and the value that must be stored.
	 * @return a EntityResult. This result will have an error code if error has ocurred.
	 * @throws Exception
	 *             if any exception occurs
	 */
	EntityResult insert(IOntimizeDaoSupport repository, Map<?, ?> attributesValues) throws OntimizeJEERuntimeException;

	/**
	 * This method must implement a standard update operation with the data specified in <code>attributesValues</code> over the set of records defined by <code>keysValues</code>.
	 * So, only the records that comply with the conditions specified by <code>keysValues</code> will be updated to the new values specified by <code>attributesValues</code>. <p>
	 *
	 * @param attributesValues
	 *            the data for updating the records to. The keys specify the attributes (or columns) and the values, the values for these columns.
	 * @param keysValues
	 *            the conditions that the records to be updated must fulfill. The keys specify the attributes (or columns) and the values, the values for these columns.
	 * @return a EntityResult. This result will have an error code if error has occurred.
	 * @throws Exception
	 *             if any exception occurs
	 */
	EntityResult update(IOntimizeDaoSupport repository, Map<?, ?> attributesValues, Map<?, ?> keysValues) throws OntimizeJEERuntimeException;

	/**
	 * This method must implement a standard delete operation over the set of records defined by <code>keysValues</code>. So, only the records that comply with the conditions
	 * specified by <code>keysValues</code> will be deleted. <p>
	 *
	 * @param keysValues
	 *            the conditions that the records to be deleted must fulfill. The keys specify the attributes (or columns) and the values, the values for these columns.
	 * @return a EntityResult. This result will have an error code if error has occurred.
	 * @throws Exception
	 *             if any exception occurs
	 */
	EntityResult delete(IOntimizeDaoSupport repository, Map<?, ?> keysValues) throws OntimizeJEERuntimeException;

}

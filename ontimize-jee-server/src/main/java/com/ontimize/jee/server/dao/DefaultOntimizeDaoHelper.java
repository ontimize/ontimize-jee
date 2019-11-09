package com.ontimize.jee.server.dao;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.ontimize.db.AdvancedEntityResult;
import com.ontimize.db.EntityResult;
import com.ontimize.gui.TableMultipleValue;
import com.ontimize.gui.field.EntityFunctionAttribute;
import com.ontimize.gui.field.MultipleReferenceDataFieldAttribute;
import com.ontimize.gui.field.MultipleTableAttribute;
import com.ontimize.gui.field.ReferenceFieldAttribute;
import com.ontimize.gui.table.ExtendedTableAttribute;
import com.ontimize.gui.table.TableAttribute;
import com.ontimize.jee.common.dao.DeleteOperation;
import com.ontimize.jee.common.dao.ICascadeOperationContainer;
import com.ontimize.jee.common.dao.IOperation;
import com.ontimize.jee.common.dao.InsertOperation;
import com.ontimize.jee.common.dao.UpdateOperation;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.services.user.UserInformation;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.server.dao.common.attributedispatcher.EntityFunctionAttributeDispatcher;
import com.ontimize.jee.server.dao.common.attributedispatcher.IAttributeDispatcher;
import com.ontimize.jee.server.dao.common.attributedispatcher.MultipleReferenceDataFieldAttributeDispatcher;
import com.ontimize.jee.server.dao.common.attributedispatcher.ReferenceFieldAttributeDispatcher;
import com.ontimize.jee.server.dao.common.attributedispatcher.TableAttributeDispatcher;

/**
 * The Class DefaultOntimizeServiceImpl.
 */
@Component
@Lazy(value = true)
public class DefaultOntimizeDaoHelper implements IOntimizeDaoHelper, ApplicationContextAware {

	/** The logger. */
	private static final Logger								logger	= LoggerFactory.getLogger(DefaultOntimizeDaoHelper.class);

	/** The application context. */
	protected ApplicationContext							applicationContext;
	/** The Constant registeredDispatcher. */
	private final Map<Class<?>, IAttributeDispatcher<?>>	registeredDispatchers;

	public DefaultOntimizeDaoHelper() {
		super();
		this.registeredDispatchers = new HashMap<>();
		this.registerAttributeDispatcher(ReferenceFieldAttribute.class, new ReferenceFieldAttributeDispatcher());
		this.registerAttributeDispatcher(EntityFunctionAttribute.class, new EntityFunctionAttributeDispatcher());
		this.registerAttributeDispatcher(MultipleReferenceDataFieldAttribute.class, new MultipleReferenceDataFieldAttributeDispatcher());
		this.registerAttributeDispatcher(TableAttribute.class, new TableAttributeDispatcher());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext (org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/**
	 * Gets the application context.
	 *
	 * @return the application context
	 */
	public ApplicationContext getApplicationContext() {
		return this.applicationContext;
	}

	public AdvancedEntityResult paginationQuery(IOntimizeDaoSupport dao, Map<?, ?> keysValues, List<?> attributes, int recordNumber, int startIndex, List<?> orderBy) {
		return this.paginationQuery(dao, keysValues, attributes, recordNumber, startIndex, orderBy, IOntimizeDaoSupport.DEFAULT_QUERY_KEY);
	}
	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.common.services.ontimize.IOntimizeService#advancedQuery (java.lang.String, java.util.Map, java.util.List, int, int, java.util.List)
	 */
	@Override
	public AdvancedEntityResult paginationQuery(IOntimizeDaoSupport dao, Map<?, ?> keysValues, List<?> attributes, int recordNumber, int startIndex, List<?> orderBy,
			String queryId) {
		return this.paginationQuery(dao, keysValues, attributes, recordNumber, startIndex, orderBy, queryId, null);
	}

	@Override
	public AdvancedEntityResult paginationQuery(IOntimizeDaoSupport dao, Map<?, ?> keysValues, List<?> attributes, int recordNumber, int startIndex, List<?> orderBy,
			String queryId, ISQLQueryAdapter adapter) {
		CheckingTools.failIfNull(dao, "Null dao");

		List<?> vProcessMultipleAttributes = new ArrayList<>();
		List<?> vMultipleTableAttributes = this.processMultipleTableAttribute(attributes, (List<Object>) vProcessMultipleAttributes);
		vMultipleTableAttributes = this.processMultipleAttributeKey(vMultipleTableAttributes);

		List<?> vAttributes = new ArrayList<>(vMultipleTableAttributes);

		AdvancedEntityResult erResult = dao.paginationQuery(keysValues, vAttributes, recordNumber, startIndex, orderBy, queryId);

		if (!erResult.isWrong()) {
			// TODO
			// this.queryOtherEntities(new ArrayList<>(vMultipleTableAttributes), erResult)
			// erResult = this.deleteMultipleTableAttributesColumns(erResult, attributes, vProcessMultipleAttributes)
		}

		return erResult;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.common.services.ontimize.IOntimizeService#query(java.lang .String, java.util.Map, java.util.List)
	 */
	public EntityResult query(IOntimizeDaoSupport dao, Map<?, ?> keysValues, List<?> attributes) {
		return this.query(dao, keysValues, attributes, IOntimizeDaoSupport.DEFAULT_QUERY_KEY);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.common.services.ontimize.IOntimizeService#query(java.lang .String, java.util.Map, java.util.List)
	 */
	public EntityResult query(IOntimizeDaoSupport dao, Map<?, ?> keysValues, List<?> attributes, List<?> sort) {
		return this.query(dao, keysValues, attributes, sort, IOntimizeDaoSupport.DEFAULT_QUERY_KEY);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.common.services.ontimize.IOntimizeService#query(java.lang .String, java.util.Map, java.util.List)
	 */
	@Override
	public EntityResult query(IOntimizeDaoSupport dao, Map<?, ?> keysValues, List<?> attributes, String queryId) {
		return this.query(dao, keysValues, attributes, (List<?>) null, queryId);
	}

	@Override
	public EntityResult query(IOntimizeDaoSupport dao, Map<?, ?> keysValues, List<?> attributes, List<?> sort, String queryId) {
		return this.query(dao, keysValues, attributes, sort, queryId, null);
	}

	@Override
	public EntityResult query(IOntimizeDaoSupport repository, Map<?, ?> keysValues, List<?> attributes, String queryId, ISQLQueryAdapter adapter)
			throws OntimizeJEERuntimeException {
		return this.query(repository, keysValues, attributes, null, queryId, adapter);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.common.services.ontimize.IOntimizeService#query(java.lang .String, java.util.Map, java.util.List)
	 */
	@Override
	public EntityResult query(IOntimizeDaoSupport dao, Map<?, ?> keysValues, List<?> attributes, List<?> sort, String queryId, ISQLQueryAdapter adapter) {
		CheckingTools.failIfNull(dao, "Null dao");

		List<?> vProcessMultipleAttributes = new ArrayList<>();
		List<?> vMultipleTableAttributes = this.processMultipleTableAttribute(attributes, (List<Object>) vProcessMultipleAttributes);
		vMultipleTableAttributes = this.processMultipleAttributeKey(vMultipleTableAttributes);

		List<?> vAttributes = new ArrayList<>(vMultipleTableAttributes);

		EntityResult erResult = dao.query(keysValues, vAttributes, sort, queryId, adapter);

		if (!erResult.isWrong()) {
			this.queryOtherEntities(new ArrayList<>(vMultipleTableAttributes), erResult);
			erResult = this.deleteMultipleTableAttributesColumns(erResult, attributes, vProcessMultipleAttributes);
		}
		return erResult;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.core.IOntimizeService#query(com.ontimize.jee.server.dao.IOntimizeDaoSupport, java.util.Map, java.util.List, java.lang.String,
	 * java.lang.Class)
	 */
	@Override
	public <T> List<T> query(IOntimizeDaoSupport dao, Map<?, ?> keysValues, List<?> sort, String queryId, Class<T> clazz) {
		return this.query(dao, keysValues, sort, queryId, clazz, null);
	}

	@Override
	public <T> List<T> query(IOntimizeDaoSupport dao, Map<?, ?> keysValues, List<?> sort, String queryId, Class<T> clazz, ISQLQueryAdapter adapter) {
		CheckingTools.failIfNull(dao, "Null dao");
		return dao.query(keysValues, sort, queryId, clazz);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.common.services.ontimize.IOntimizeService#insert(java. lang.String, java.util.Map)
	 */
	@Override
	public EntityResult insert(IOntimizeDaoSupport dao, Map<?, ?> attributesValues) {
		CheckingTools.failIfNull(dao, "Null dao");

		// Primero vemos que valores son para la entidad principal y cuales para propagar a otras entidades
		Map<Object, Object> avToInsert = new HashMap<>();
		Map<?, ICascadeOperationContainer> avToPropagate = new HashMap<>();
		this.extactPropagableValues(attributesValues, avToInsert, avToPropagate);

		EntityResult resGeneratedKeys = dao.insert(avToInsert);

		EntityResult resOtherEntities = this.propagateToOtherEntities(resGeneratedKeys, avToInsert, avToPropagate);
		resOtherEntities.putAll(resGeneratedKeys);
		return resOtherEntities;
	}

	/**
	 * Propagate to other entities.
	 *
	 * @param generatedValuesInParentEntity
	 *            the generated keys
	 * @param attributesValuesReceivedInParentEntity
	 *            the attributes values
	 * @return the entity result
	 */
	public EntityResult propagateToOtherEntities(Map<?, ?> generatedValuesInParentEntity, Map<?, ?> attributesValuesReceivedInParentEntity,
			Map<?, ICascadeOperationContainer> attributesValuesToPropagate) {
		EntityResult result = new EntityResult();
		for (Entry<?, ICascadeOperationContainer> entry : attributesValuesToPropagate.entrySet()) {
			// El orden de propagación será primero deletes, luego updates y finalmente insert
			List<IOperation> operations = new ArrayList<>();
			operations.addAll(entry.getValue().getDeleteOperations());
			operations.addAll(entry.getValue().getUpdateOperations());
			operations.addAll(entry.getValue().getInsertOperations());

			for (IOperation operation : operations) {
				for (Entry<Class<?>, IAttributeDispatcher<?>> entryDispatcher : this.registeredDispatchers.entrySet()) {
					Class<?> dispatcherClass = entryDispatcher.getKey();
					if (dispatcherClass.isAssignableFrom(entry.getKey().getClass())) {
						IAttributeDispatcher<Object> dispatcher = (IAttributeDispatcher<Object>) entryDispatcher.getValue();
						Map<?, ?> generatedValues = null;
						if (operation instanceof InsertOperation) {
							generatedValues = dispatcher.processInsertAttribute(entry.getKey(), (InsertOperation) operation, generatedValuesInParentEntity,
									attributesValuesReceivedInParentEntity, this.applicationContext);
						} else if (operation instanceof UpdateOperation) {
							generatedValues = dispatcher.processUpdateAttribute(entry.getKey(), (UpdateOperation) operation, generatedValuesInParentEntity,
									attributesValuesReceivedInParentEntity, this.applicationContext);
						} else if (operation instanceof DeleteOperation) {
							generatedValues = dispatcher.processDeleteAttribute(entry.getKey(), (DeleteOperation) operation, generatedValuesInParentEntity,
									attributesValuesReceivedInParentEntity, this.applicationContext);
						}

						if (generatedValues != null) {
							result.putAll(generatedValues);
						}

					}
				}
			}
		}
		return result;
	}

	/**
	 * Extact propagable values.
	 *
	 * @param valuesToCheck
	 *            the values to check
	 * @param nonPropagableValues
	 *            the non propagable values
	 * @param propagableValues
	 *            the propagable values
	 */
	public void extactPropagableValues(Map<?, ?> valuesToCheck, Map<?, ?> nonPropagableValues, Map<?, ICascadeOperationContainer> propagableValues) {
		// Primero vemos que valores son para la entidad principal y cuales para propagar a otras entidades
		for (Entry<?, ?> entry : valuesToCheck.entrySet()) {
			if (entry.getValue() instanceof ICascadeOperationContainer) {
				((Map<Object, ICascadeOperationContainer>) propagableValues).put(entry.getKey(), (ICascadeOperationContainer) entry.getValue());
			} else {
				((Map<Object, Object>) nonPropagableValues).put(entry.getKey(), entry.getValue());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.common.services.ontimize.IOntimizeService#update(java. lang.String, java.util.Map, java.util.Map)
	 */
	@Override
	public EntityResult update(IOntimizeDaoSupport dao, Map<?, ?> attributesValues, Map<?, ?> keysValues) {
		CheckingTools.failIfNull(dao, "Null dao");

		// Primero vemos que valores son para la entidad principal y cuales para propagar a otras entidades
		Map<Object, Object> avUpdate = new HashMap<>();
		Map<?, ICascadeOperationContainer> avToPropagate = new HashMap<>();
		this.extactPropagableValues(attributesValues, avUpdate, avToPropagate);

		EntityResult updateResult = new EntityResult();

		if (!avUpdate.isEmpty()) {
			updateResult = dao.update(avUpdate, keysValues);
		}

		avUpdate.putAll(keysValues);
		this.propagateToOtherEntities(updateResult, avUpdate, avToPropagate);

		return updateResult;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.common.services.ontimize.IOntimizeService#delete(java. lang.String, java.util.Map)
	 */
	@Override
	public EntityResult delete(IOntimizeDaoSupport dao, Map<?, ?> keysValues) {
		CheckingTools.failIfNull(dao, "Null dao");
		return dao.delete(keysValues);
	}

	/**
	 * Gets the dao reference.
	 *
	 * @param repositoryName
	 *            the repository name
	 * @return the dao reference
	 */
	protected IOntimizeDaoSupport getDaoReference(String repositoryName) {
		return (IOntimizeDaoSupport) this.applicationContext.getBean(repositoryName);
	}

	/**
	 * Processes all the MultipleTableAttribute contained in the Vector <code>list</code>. All other objects are added to the resulting Vector with no changes. The
	 * MultipleTableAttribute objects are replaced by their attribute plus a list of ExtendedTableAttribute objects corresponding to the keys of the MultipleTableAttribute object.
	 *
	 * @param list
	 *            the list
	 * @param processedMultipleAttributes
	 *            the processed multiple attributes
	 * @return a new Vector with the processed objects.
	 */
	protected List<?> processMultipleTableAttribute(List<?> list, List<Object> processedMultipleAttributes) {
		List<Object> vOutput = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			Object o = list.get(i);
			if (o instanceof MultipleTableAttribute) {
				MultipleTableAttribute aD = (MultipleTableAttribute) o;
				processedMultipleAttributes.add(aD);
				vOutput.add(aD.getAttribute());
				Enumeration<?> enu = aD.keys();
				while (enu.hasMoreElements()) {
					Object oKey = enu.nextElement();
					ExtendedTableAttribute eTa = aD.getExtendedTableAttribute((String) oKey);
					vOutput.add(eTa);
				}
			} else {
				vOutput.add(o);
			}
		}
		return vOutput;
	}

	/**
	 * Processes MultipleReferenceDataFieldAttribute objects contained in <code>list</code>. <p> For each MultipleReferenceDataFieldAttribute found, the objects in
	 * MultipleReferenceDataFieldAttribute.getCods() are added to the resulting Vector
	 *
	 * @param list
	 *            the list
	 * @return the processed list as a new Vector
	 */
	public List<?> processMultipleAttributeKey(List<?> list) {
		List<Object> destination = new ArrayList<>();
		List<Object> processedMultipleKeysAttributes = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			Object cActual = list.get(i);
			if (cActual instanceof MultipleReferenceDataFieldAttribute) {
				List<?> listCods = ((MultipleReferenceDataFieldAttribute) cActual).getCods();
				processedMultipleKeysAttributes.add(cActual);
				for (int j = 0; j < listCods.size(); j++) {
					Object column = listCods.get(j);
					if (!list.contains(column)) {
						destination.add(column);
					}
				}
			}
			destination.add(cActual);
		}
		return destination;
	}

	/**
	 * This method performs a query for each object of any of the following types contained in the Vector <code>attributes</code>: <ul> <li>ReferenceFieldAttribute</li>
	 * <li>TableAttribute</li> <li>MultipleReferenceDataFieldAttribute</li> </ul> <p> A query is performed for each record in the argument <code>result</code>. <p> For
	 * ReferenceFieldAttribute and MultipleReferenceDataFieldAttribute objects, the object information (entity, columns, cods, attrs) is used to perform the query. <p> For
	 * TableAttribute objects, if the entity specified in the object is defined in this entity's 'TableAttributeEntities' parameter, then the TableAttribute object information (
	 * parentkeys) is used when performed the query, that is, the keysValues passed to the Entity's query method will contain for each record in <code>result</code> the values for
	 * each column in the parentkeys. <p> If the entity specified in the TableAttribute object is not defined in 'TableAttributeEntities', then the keys of this entity are used.
	 * <p> Also, for TableAttribute objects, if 'other_entities' parameter is defined, only those objects with entity name matches any of the names defined by this parameter, will
	 * lead to a query execution. If the <code>result</code> contains more records than the value of <code>limitQueryOthersEntities</code> field, only the first
	 * <code>limitQueryOthersEntities</code> will be queried. <p> When querying other entities, if this and these entities are instances of PrivilegedSecurityEntity, and this
	 * entity is queried with a privileged Id, a privileged Id is set and used with other entities when querying. <p> The resulting data is added to the <code>result</code> object
	 * using as keys, the associated objects in <code>attributes</code>. <p> So, if a ReferenteFieldAttribute is found, a query is performed and the result is added to
	 * <code>result</code> using as key the ReferenceFieldAttribute. <p>
	 *
	 * @param attributes
	 *            the attributes
	 * @param result
	 *            With the column names without the alias. If there is no data for column names then use the alias values.
	 * @return the same <code>result</code> object received as argument with additional data
	 * @see TableAttribute
	 * @see ReferenceFieldAttribute
	 */
	public EntityResult queryOtherEntities(List<?> attributes, EntityResult result) {
		// TODO ver si se pueden lanzar hilos para cada petición para
		// paralelizar las consultas
		List<?> vCloneAttributes = new ArrayList<>(attributes);
		if (result.isEmpty()) {
			return result;
		}
		for (Object oAttribute : vCloneAttributes) {
			for (Entry<Class<?>, IAttributeDispatcher<?>> entry : this.registeredDispatchers.entrySet()) {
				Class<?> key = entry.getKey();
				if (key.isAssignableFrom(oAttribute.getClass())) {
					IAttributeDispatcher<Object> dispatcher = (IAttributeDispatcher<Object>) entry.getValue();
					dispatcher.processQueryAttribute(oAttribute, result, this.applicationContext);
				}
			}
		}
		return result;
	}

	/**
	 * Processes the EntityResult <code>res</code> creating TableMultipleValue objects .
	 *
	 * @param result
	 *            the result
	 * @param originalAttributes
	 *            the original attributes
	 * @param processedMultipleAttributes
	 *            the processed multiple attributes
	 * @return the entity result
	 */
	protected EntityResult deleteMultipleTableAttributesColumns(EntityResult result, List<?> originalAttributes, List<?> processedMultipleAttributes) {
		if (result.isEmpty()) {
			return result;
		}
		List<ExtendedTableAttribute> deleteAttributeTable = new ArrayList<>();

		for (int i = 0; i < processedMultipleAttributes.size(); i++) {
			MultipleTableAttribute aD = (MultipleTableAttribute) processedMultipleAttributes.get(i);
			List<TableMultipleValue> vData = new ArrayList<>(0);
			for (int j = 0; j < result.calculateRecordNumber(); j++) {
				Hashtable<?, ?> hCurrent = result.getRecordValues(j);
				TableMultipleValue vMT = new TableMultipleValue(hCurrent.get(aD.getAttribute()));
				Enumeration<?> enu = aD.keys();
				while (enu.hasMoreElements()) {
					Object oKey = enu.nextElement();
					ExtendedTableAttribute atT = aD.getExtendedTableAttribute((String) oKey);
					deleteAttributeTable.add(atT);
					Object object = hCurrent.get(atT);
					// This object can be null if the otherEntities list does
					// not contains the entity
					if (object != null) {
						vMT.put(oKey, hCurrent.get(atT));
					}
				}
				vData.add(vMT);
			}
			result.put(aD, vData);

			if (result.containsKey(aD.getAttribute())) {
				result.remove(aD.getAttribute());
			}
		}

		for (int i = 0; i < deleteAttributeTable.size(); i++) {
			Object c = deleteAttributeTable.get(i);
			if (originalAttributes.contains(c)) {
				continue;
			}
			if (result.containsKey(c)) {
				result.remove(c);
			}
		}
		return result;
	}

	/**
	 * Register attribute dispatcher.
	 *
	 * @param <T>
	 *            the generic type
	 * @param key
	 *            the key
	 * @param dispatcher
	 *            the dispatcher
	 */
	public <T> void registerAttributeDispatcher(Class<T> key, IAttributeDispatcher<T> dispatcher) {
		this.registeredDispatchers.put(key, dispatcher);
	}

	public <T> IAttributeDispatcher unRegisterAttributeDispatcher(Class<T> key) {
		return this.registeredDispatchers.remove(key);
	}

	public Map<Class<?>, IAttributeDispatcher<?>> getAttributeDispatchers() {
		return this.registeredDispatchers;
	}

	/**
	 * Get the current user from the {@link SecurityContextHolder}
	 *
	 * @return
	 */
	public UserInformation getUser() {
		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			return null;
		}
		return (UserInformation) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

}

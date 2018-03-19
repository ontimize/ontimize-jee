/**
 *
 */
package com.ontimize.jee.server.dao.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import javax.xml.bind.JAXB;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import com.ontimize.db.AdvancedEntityResult;
import com.ontimize.db.EntityResult;
import com.ontimize.db.NullValue;
import com.ontimize.db.SQLStatementBuilder;
import com.ontimize.db.SQLStatementBuilder.BasicExpression;
import com.ontimize.db.SQLStatementBuilder.BasicField;
import com.ontimize.db.SQLStatementBuilder.Expression;
import com.ontimize.db.SQLStatementBuilder.Field;
import com.ontimize.db.SQLStatementBuilder.SQLOrder;
import com.ontimize.gui.MultipleValue;
import com.ontimize.gui.SearchValue;
import com.ontimize.gui.field.MultipleTableAttribute;
import com.ontimize.gui.field.ReferenceFieldAttribute;
import com.ontimize.gui.table.TableAttribute;
import com.ontimize.jee.common.naming.I18NNaming;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.common.tools.ParseUtilsExtended;
import com.ontimize.jee.common.tools.streamfilter.ReplaceTokensFilterReader;
import com.ontimize.jee.server.dao.DaoProperty;
import com.ontimize.jee.server.dao.IOntimizeDaoSupport;
import com.ontimize.jee.server.dao.common.ConfigurationFile;
import com.ontimize.jee.server.dao.common.ExtraWhereSections;
import com.ontimize.jee.server.dao.common.WhereSection;
import com.ontimize.jee.server.dao.jpa.QueryTemplateInformation.Syntax;
import com.ontimize.jee.server.dao.jpa.common.MappingInfo;
import com.ontimize.jee.server.dao.jpa.common.MappingInfoUtils;
import com.ontimize.jee.server.dao.jpa.common.rowmapper.IRowMapper;
import com.ontimize.jee.server.dao.jpa.common.rowmapper.IRowMapperProvider;
import com.ontimize.jee.server.dao.jpa.common.rowmapper.base.BaseRowMapper;
import com.ontimize.jee.server.dao.jpa.dataconversors.DataConversorsUtil;
import com.ontimize.jee.server.dao.jpa.ql.ValueToQLLiteralProcessor;
import com.ontimize.jee.server.dao.jpa.ql.jpql.DefaultJPQLConditionValuesProcessor;
import com.ontimize.jee.server.dao.jpa.ql.jpql.ExtendedJPQLConditionValuesProcessor;
import com.ontimize.jee.server.dao.jpa.ql.literalprocessors.common.DefaultValueToQLLiteralProcessor;
import com.ontimize.jee.server.dao.jpa.ql.sql.DefaultSQLConditionValuesProcessor;
import com.ontimize.jee.server.dao.jpa.ql.sql.ExtendedSQLConditionValuesProcessor;
import com.ontimize.jee.server.dao.jpa.setup.AmbiguousColumnType;
import com.ontimize.jee.server.dao.jpa.setup.FunctionColumnType;
import com.ontimize.jee.server.dao.jpa.setup.JpaEntitySetupType;
import com.ontimize.jee.server.dao.jpa.setup.QueryType;

/**
 * The Class OntimizeJpaDaoSupport.
 *
 * @author sergio.padin
 */
public class OntimizeJpaDaoSupport implements ApplicationContextAware, IOntimizeDaoSupport {

	/** The Constant SELECT. */
	private static final String							SELECT							= "SELECT";

	/** The Constant FROM. */
	private static final String							FROM							= "FROM";

	/** The Constant SPACE. */
	private static final Character						SPACE							= ' ';

	private static final String							PARAM_PREFIX					= "param";

	/** The Constant PLACEHOLDER_ORDER. */
	private static final String							PLACEHOLDER_ORDER				= "#ORDER#";
	/** The Constant PLACEHOLDER_ORDER_CONCAT. */
	private static final String							PLACEHOLDER_ORDER_CONCAT		= "#ORDER_CONCAT#";
	/** The Constant PLACEHOLDER_WHERE. */
	private static final String							PLACEHOLDER_WHERE				= "#WHERE#";
	/** The Constant PLACEHOLDER_WHERE_CONCAT. */
	private static final String							PLACEHOLDER_WHERE_CONCAT		= "#WHERE_CONCAT#";
	/** The Constant PLACEHOLDER_COLUMNS. */
	private static final String							PLACEHOLDER_COLUMNS				= "#COLUMNS#";
	/** The Constant PLACEHOLDER_COLUMNS. */
	private static final String							PLACEHOLDER_COLUMNS_CONCAT		= "#COLUMNS_CONCAT#";

	/** The Constant logger. */
	private static final Logger							logger							= LoggerFactory.getLogger(OntimizeJpaDaoSupport.class);

	/** The entity manager. */
	@PersistenceContext
	private EntityManager								entityManager;

	/** The application context. */
	private ApplicationContext							applicationContext;

	/** The entity bean name. */
	private String										entityBeanName					= null;

	/** The entity bean class. */
	private Class<?>									entityBeanClass					= null;

	/** if true then conf file is loaded. */
	private boolean										loaded							= false;

	/** Configuration file. */
	private String										configurationFile				= null;

	/** Configuration file placeholder. */
	private String										configurationFilePlaceholder	= null;

	/** Queries. */
	private final Map<String, QueryTemplateInformation>	sqlQueries						= new HashMap<>();

	/** The jpql condition values processor. */
	private DefaultJPQLConditionValuesProcessor			jpqlConditionValuesProcessor	= new ExtendedJPQLConditionValuesProcessor();

	/** The sql condition values processor. */
	private DefaultSQLConditionValuesProcessor			sqlConditionValuesProcessor		= new ExtendedSQLConditionValuesProcessor();

	/** The jpql value to ql literal processor. */
	private ValueToQLLiteralProcessor					jpqlValueToQLLiteralProcessor	= new DefaultValueToQLLiteralProcessor();

	/** The sql value to ql literal processor. */
	private ValueToQLLiteralProcessor					sqlValueToQLLiteralProcessor	= new DefaultValueToQLLiteralProcessor();

	/** The row mapper provider. */
	private IRowMapperProvider							rowMapperProvider;

	/**
	 * The Constructor.
	 */
	public OntimizeJpaDaoSupport() {
		super();
	}

	/**
	 * The Constructor.
	 *
	 * @param configurationFile
	 *            the configuration file
	 */
	public OntimizeJpaDaoSupport(final String configurationFile) {
		super();
		this.configurationFile = configurationFile;
	}

	/**
	 * The Constructor.
	 *
	 * @param configurationFile
	 *            the configuration file
	 * @param configurationFilePlaceholder
	 *            the configuration file placeholder
	 */
	public OntimizeJpaDaoSupport(final String configurationFile, final String configurationFilePlaceholder) {
		super();
		this.configurationFile = configurationFile;
		this.configurationFilePlaceholder = configurationFilePlaceholder;
	}

	@Override
	public AdvancedEntityResult paginationQuery(Map<?, ?> keysValues, List<?> attributes, int recordNumber, int startIndex, List<?> orderBy, String queryId) {
		// TODO
		return null;
	}

	/**
	 * Query.
	 *
	 * @param keysValues
	 *            the keys values
	 * @param attributes
	 *            the attributes
	 * @param sort
	 *            the sort
	 * @param queryId
	 *            the query id
	 * @return the entity result
	 * @see com.ontimize.jee.server.dao.IOntimizeDaoSupport#query(java.util.Map, java.util.List, java.util.List, java.lang.String)
	 */
	@Override
	public EntityResult query(final Map<?, ?> keysValues, final List<?> attributes, final List<?> sort, final String queryId) {
		try {
			this.check();
			final Class<?> queryEntityClass = this.getQueryEntityClass();
			// Get valid attributes
			final List<String> validAttributes = this.adaptAttributes(attributes);

			return this.innerQuery(keysValues, validAttributes, sort, queryId, EntityResult.class, queryEntityClass, false);
		} catch (final Exception e) {
			OntimizeJpaDaoSupport.logger.error(e.getMessage(), e);
			return new EntityResult(EntityResult.OPERATION_WRONG, EntityResult.OPERATION_WRONG, e.getMessage());
		}
	}

	/**
	 *
	 * @see com.ontimize.jee.server.dao.IOntimizeDaoSupport#query(java.util.Map, java.util.List, java.lang.String, java.lang.Class)
	 */
	@Override
	public <T> List<T> query(final Map<?, ?> keysValues, final List<?> sort, final String queryId, final Class<T> clazz) {
		try {
			this.check();
			return this.innerQuery(keysValues, null, sort, queryId, List.class, clazz, true);
		} catch (SQLException e) {
			OntimizeJpaDaoSupport.logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected <T> T innerQuery(final Map<?, ?> keysValues, final List<String> validAttributes, final List<?> sort, final String queryId, Class<T> resultStyleClass,
			Class<?> queryResultClass, boolean failIfTemplateInfoNotMatch) throws SQLException, Exception {

		// Process ORDER BY columns
		final List<Object> validSort = this.adaptSort(sort);

		final QueryTemplateInformation queryTemplateInformation = this.getQueryTemplateInformation(queryId);
		Query selectQuery = null;

		List<?> totalData = null;
		if ((queryTemplateInformation == null) && (((queryId != null) && queryId.equalsIgnoreCase(IOntimizeDaoSupport.DEFAULT_QUERY_KEY)) || (queryId == null))) {
			if (queryResultClass == null) {
				return this.prepareEmptyResult(resultStyleClass);
			}
			final String query = this.prepareQuery(QueryTemplateInformation.Syntax.JPQL, null, validAttributes, keysValues, validSort, queryResultClass);
			selectQuery = this.entityManager.createQuery(query);
			this.adaptQuery(selectQuery);
			totalData = selectQuery.getResultList();
		} else if ((queryTemplateInformation != null) && queryTemplateInformation.getSyntax().equals(QueryTemplateInformation.Syntax.JPQL)) { // JPA query style
			if (queryResultClass == null) {
				return this.prepareEmptyResult(resultStyleClass);
			}
			final String query = this.prepareQuery(queryTemplateInformation.getSyntax(), queryTemplateInformation, validAttributes, keysValues, validSort, queryResultClass);
			if (queryTemplateInformation.getResultClass().equals(queryResultClass)) {
				selectQuery = this.entityManager.createQuery(query);
				this.adaptQuery(selectQuery);
				totalData = selectQuery.getResultList();
			} else {
				throw new RuntimeException("Query return type doesn't match expected result return type");
			}

		} else if ((queryTemplateInformation != null) && queryTemplateInformation.getSyntax().equals(QueryTemplateInformation.Syntax.SQL)) { // SQL query style
			final String query = this.prepareQuery(queryTemplateInformation.getSyntax(), queryTemplateInformation, validAttributes, keysValues, validSort, queryResultClass);
			// if return type is this entity and no column mapping specified then we delegate in jpa support
			if (queryTemplateInformation.getResultClass().equals(queryResultClass)) {
				if ((queryTemplateInformation.getMappingInfo().getColumnMappings().size() == 0) && !queryTemplateInformation.getMappingInfo().getReturnTypeClassNameIsPrimitive()) {
					selectQuery = this.entityManager.createNativeQuery(query, queryResultClass);
					this.adaptQuery(selectQuery);
					totalData = selectQuery.getResultList();
				} else {
					selectQuery = this.entityManager.createNativeQuery(query);
					this.adaptQuery(selectQuery);
					totalData = this.executeQuery(selectQuery, OntimizeJpaUtils.getSelectColumnNames(query), queryTemplateInformation);
				}

			} else if (failIfTemplateInfoNotMatch) {
				throw new RuntimeException("Query return type doesn't match expected result return type");
			} else {// the return type is another, defined mapping in configuration file
				selectQuery = this.entityManager.createNativeQuery(query);
				this.adaptQuery(selectQuery);
				totalData = this.executeQuery(selectQuery, OntimizeJpaUtils.getSelectColumnNames(query), queryTemplateInformation);
			}
		} else {
			if (EntityResult.class.isAssignableFrom(resultStyleClass)) {
				EntityResult result = new EntityResult(EntityResult.OPERATION_WRONG, EntityResult.OPERATION_WRONG, I18NNaming.MC_ERROR_QUERY_TYPE_NOT_KNOWN);
				Object[] parameters = { queryId };
				result.setMessageParameters(parameters);
				return (T) result;
			} else {
				throw new RuntimeException("Query type not known: " + queryId);
			}
		}

		return this.prepareResult(validAttributes, resultStyleClass, queryResultClass, selectQuery, totalData);
	}

	private <T> T prepareResult(final List<String> validAttributes, Class<T> resultStyleClass, Class<?> queryResultClass, Query selectQuery, List<?> totalData) throws Exception {
		if (EntityResult.class.isAssignableFrom(resultStyleClass)) {
			EntityResult result = null;
			if ((totalData != null) && (totalData.size() > 0)) {

				result = OntimizeJpaUtils.transformListToEntityResultBeans(totalData, validAttributes);

				try {
					this.entityManager.getMetamodel().entity(queryResultClass);
					result.setColumnSQLTypes(OntimizeJpaUtils.getSQLTypes(selectQuery, validAttributes, queryResultClass, this.entityManager));
				} catch (IllegalArgumentException iae) {
					OntimizeJpaDaoSupport.logger.trace(null, iae);
					result.setColumnSQLTypes(OntimizeJpaUtils.getSQLTypes(selectQuery, validAttributes, queryResultClass));
				}

				result = this.unadaptAttributesInResult(result, validAttributes);
				result = this.adaptEntityResult(result);

			} else {
				result = OntimizeJpaUtils.transformListToEntityResultBeans(null, null);
			}
			return (T) result;
		} else {
			return (T) totalData;
		}
	}

	private <T> T prepareEmptyResult(Class<T> resultStyleClass) {
		if (EntityResult.class.isAssignableFrom(resultStyleClass)) {
			return (T) this.newEntityResultErrorQueryEntityClassNull();
		} else {
			throw new RuntimeException("Entity class is null");
		}
	}

	/**
	 * Execute query.
	 *
	 * Override this method for optimizations purposes when transforming query result data to final result type.
	 *
	 * @param <T>
	 *            the generic type
	 * @param selectQuery
	 *            the select query
	 * @param columnNames
	 *            the column names
	 * @param queryTemplateInformation
	 *            the query template information
	 * @return the list
	 * @throws SQLException
	 *             the SQL exception
	 */
	protected <T> List<T> executeQuery(final Query selectQuery, final List<String> columnNames, final QueryTemplateInformation queryTemplateInformation) throws SQLException {
		final List<?> result = selectQuery.getResultList();
		final List<T> translated = new ArrayList<>();
		final Class<?> resultClass = queryTemplateInformation.getResultClass();
		final MappingInfo mappingInfo = queryTemplateInformation.getMappingInfo();
		for (final Object o : result) {
			if ((o != null) && resultClass.isAssignableFrom(o.getClass())) {
				translated.add((T) o);
			} else if (o != null) {
				final Class<?> entityBeanClass = this.getQueryEntityClass();
				IRowMapper<T> rowMapper = new BaseRowMapper<>(mappingInfo, entityBeanClass, (Class<T>) resultClass);
				if (this.rowMapperProvider != null) {
					rowMapper = this.rowMapperProvider.getRowMapper(mappingInfo, entityBeanClass, (Class<T>) resultClass);
				}
				try {
					T res = null;
					if (o instanceof Object[]) {
						res = rowMapper.mapObjectArray((Object[]) o, columnNames);
					} else {
						res = rowMapper.mapObjectArray(new Object[] { o }, columnNames);
					}
					translated.add(res);
				} catch (final SQLException e) {
					throw e;
				}
			}
		}
		return translated;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.dao.IOntimizeDaoSupport#insert(java.util.Map)
	 */
	@Override
	public EntityResult insert(final Map<?, ?> attributesValues) {
		this.check();

		// TODO add support to SQL inserts

		final Map<String, ?> attributesValuesAdapted = this.adaptAttributesValues(attributesValues);

		final Class<?> cl = this.getQueryEntityClass();
		final EntityType<?> entity = this.entityManager.getMetamodel().entity(cl);

		if (entity != null) {
			Map<String, ?> attibutesValuesBeansAsociationsConverted = OntimizeJpaUtils.reorganizeAttributesInBeansIfNecessary(this.entityManager, entity, attributesValuesAdapted);

			final Object entityBean = BeanUtils.instantiate(cl);
			for (final Entry<String, ?> e : attibutesValuesBeansAsociationsConverted.entrySet()) {
				final Method meth = BeanUtils.findDeclaredMethodWithMinimalParameters(cl, MappingInfoUtils.buildSetterMethodName(e.getKey(), null));
				if (meth == null) {
					OntimizeJpaDaoSupport.logger.warn("Key '" + e.getKey() + "' does not have a setter method declared on bean " + cl.getName());
					continue;
				}
				try {
					final Class<?>[] parameterTypes = meth.getParameterTypes();
					if (parameterTypes.length != 1) {
						throw new IllegalArgumentException("this method has too many arguments");
					} else {
						final Object converted = DataConversorsUtil.convert(e.getValue(), parameterTypes[0]);
						meth.invoke(entityBean, converted);
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
					OntimizeJpaDaoSupport.logger.error(e1.getMessage(), e1);
				}
			}
			this.entityManager.persist(entityBean);

			EntityResult result = null;

			try {
				// nombre de la columna PK
				final SingularAttribute<?, ?> id = entity.getId(entity.getIdType().getJavaType());
				// valor de la PK
				final Object idValue = this.entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entityBean);
				final List<String> attrs = Arrays.asList(id.getName());

				// construimos el resultado
				final Hashtable<String, Object> idMap = new Hashtable<>();
				idMap.put(id.getName(), idValue);
				result = new EntityResult(idMap);

				result.setColumnSQLTypes(OntimizeJpaUtils.getSQLTypes(null, attrs, cl, this.entityManager));

				result = this.unadaptAttributesInResult(result, attrs);
				result = this.adaptEntityResult(result);
			} catch (final Exception e1) {
				OntimizeJpaDaoSupport.logger.error(e1.getMessage(), e1);
				result = new EntityResult(EntityResult.OPERATION_WRONG, EntityResult.OPERATION_WRONG, e1.getMessage());
			}
			return result;
		} else {
			return new EntityResult(EntityResult.OPERATION_WRONG, EntityResult.OPERATION_WRONG, "entity " + cl.getCanonicalName() + " is not jpa persistent");
		}

	}

	private boolean checkFilterValueUniqueness(final Object value) {
		if (value instanceof SearchValue) {
			return ((SearchValue) value).getCondition() == SearchValue.EQUAL;
		} else if (value instanceof BasicExpression) {
			return false;
		}
		return true;
	}

	/**
	 * Considerations: if keysValues doesn't contains all entity been updated primary keys, then this method will delegate in unsafeUpdate
	 *
	 * @see com.ontimize.jee.server.dao.IOntimizeDaoSupport#update(java.util.Map, java.util.Map)
	 */
	@Override
	public EntityResult update(final Map<?, ?> attributesValues, final Map<?, ?> keysValues) {
		this.check();

		// try safe update going through primary key
		final Map<String, ?> validAttributes = this.adaptAttributesValues(attributesValues);
		final Map<?, ?> kvWithoutReferenceAttributes = this.processReferenceDataFieldAttributes(keysValues);
		final Map<Object, Object> kvValidKeysValues = new HashMap<>();
		final Map<?, ?> processMultipleValueAttributes = this.processMultipleValueAttributes(kvWithoutReferenceAttributes);
		if (processMultipleValueAttributes != null) {
			kvValidKeysValues.putAll(processMultipleValueAttributes);
		}
		final EntityResult result = new EntityResult();
		if (validAttributes.size() == 0) {
			OntimizeJpaDaoSupport.logger.warn("nothing to update");
			result.setCode(EntityResult.OPERATION_SUCCESSFUL_SHOW_MESSAGE);
			result.setMessage(I18NNaming.M_IT_HAS_NOT_CHANGED_ANY_RECORD);
			return result;
		}

		final Class<?> cl = this.getQueryEntityClass();

		final EntityType<Object> entity = (EntityType<Object>) this.entityManager.getMetamodel().entity(cl);

		if (entity != null) { // entity exists try JPQL update
			Set<SingularAttribute<? super Object, ?>> singularAttributes = entity.getSingularAttributes();

			List<String> keys = new ArrayList<>();
			for (Object o : kvValidKeysValues.keySet()) {
				keys.add(o.toString());
			}

			boolean allInkeys = true;
			for (SingularAttribute<? super Object, ?> sattr : singularAttributes) {
				if (sattr.isId()) {
					Object value = keysValues.get(sattr.getName());
					if (!keys.contains(sattr.getName()) || !this.checkFilterValueUniqueness(value)) {
						allInkeys = false;
					} else {
						keys.remove(sattr.getName());
					}
				}
			}
			if (allInkeys) {

				final String query = this.prepareQuery(QueryTemplateInformation.Syntax.JPQL, null, null, keysValues, null, cl);
				Query selectQuery = this.entityManager.createQuery(query);
				this.adaptQuery(selectQuery);
				List<?> totalData = selectQuery.getResultList();
				if ((totalData != null) && (totalData.size() == 1)) {
					Map<String, ?> attibutesValuesBeansAsociationsConverted = OntimizeJpaUtils.reorganizeAttributesInBeansIfNecessary(this.entityManager, entity, validAttributes);
					Object entityBean = totalData.get(0);
					for (final Entry<String, ?> e : attibutesValuesBeansAsociationsConverted.entrySet()) {
						final Method meth = BeanUtils.findDeclaredMethodWithMinimalParameters(cl, MappingInfoUtils.buildSetterMethodName(e.getKey(), null));
						if (meth == null) {
							OntimizeJpaDaoSupport.logger.warn("Key '" + e.getKey() + "' does not have a setter method declared on bean " + cl.getName());
							continue;
						}
						try {
							Object valueToSet = e.getValue();
							Attribute<? super Object, ?> attribute = entity.getAttribute(e.getKey());
							if ((attribute != null) && attribute.getPersistentAttributeType().equals(PersistentAttributeType.EMBEDDED)) {
								// if it is an embedded attribute then we must merge values, from e.getValue()
								Object currentValue = null;
								final Method meth2 = BeanUtils.findDeclaredMethodWithMinimalParameters(cl, MappingInfoUtils.buildGetterMethodName(e.getKey(), null));
								if (meth2 == null) {
									OntimizeJpaDaoSupport.logger
									.warn("Key '" + e.getKey() + "' does not have a getter method for embedded attributed declared on bean " + cl.getName());
								} else {
									currentValue = meth2.invoke(entityBean);
								}
								valueToSet = OntimizeJpaUtils.mergeEmbeddedBean(currentValue, e.getValue(), validAttributes, e.getKey());
							}
							final Class<?>[] parameterTypes = meth.getParameterTypes();
							if (parameterTypes.length != 1) {
								throw new IllegalArgumentException("this method has too many arguments");
							} else {
								final Object converted = DataConversorsUtil.convert(valueToSet, parameterTypes[0]);
								meth.invoke(entityBean, converted);
							}
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
							OntimizeJpaDaoSupport.logger.error(e1.getMessage(), e1);
						}
					}
					this.entityManager.flush();
					result.setCode(EntityResult.OPERATION_SUCCESSFUL);
					result.setMessage(I18NNaming.M_IT_HAS_CHANGED_N_RECORDS, Integer.toString(1));
					return result;

				} else if ((totalData != null) && (totalData.size() > 1)) {
					return new EntityResult(EntityResult.OPERATION_WRONG, EntityResult.OPERATION_WRONG,
							"something happened persisting changes in entity " + cl.getCanonicalName() + ", more than one result returned with primary keys");
				} else {
					OntimizeJpaDaoSupport.logger.warn("nothing to update");
					result.setCode(EntityResult.OPERATION_SUCCESSFUL_SHOW_MESSAGE);
					result.setMessage(I18NNaming.M_IT_HAS_NOT_CHANGED_ANY_RECORD);
					return result;
				}
			} else {
				OntimizeJpaDaoSupport.logger.warn("Trying to do a save update without all entity identifiers, handling with unsafeupdate");
			}

		} else {
			OntimizeJpaDaoSupport.logger.warn("Trying to do a save update without an entity that is not jpa persistent, handling with unsafeupdate");
		}

		return this.unsafeUpdate(attributesValues, keysValues);

	}

	/**
	 * Considerations: if keysValues doesn't contains all entity been updated primary keys, then this method will delegate in unsafeDelete
	 *
	 * @see com.ontimize.jee.server.dao.IOntimizeDaoSupport#delete(java.util.Map)
	 */
	@Override
	public EntityResult delete(final Map<?, ?> keysValues) {
		this.check();
		// try safe update going through primary key
		final Map<?, ?> kvWithoutReferenceAttributes = this.processReferenceDataFieldAttributes(keysValues);
		final Hashtable<Object, Object> kvValidKeysValues = new Hashtable<>();
		final Map<?, ?> processMultipleValueAttributes = this.processMultipleValueAttributes(kvWithoutReferenceAttributes);
		if (processMultipleValueAttributes != null) {
			kvValidKeysValues.putAll(processMultipleValueAttributes);
		}

		final Class<?> cl = this.getQueryEntityClass();

		final EntityType<Object> entity = (EntityType<Object>) this.entityManager.getMetamodel().entity(cl);

		if (entity != null) { // entity exists try JPQL update
			Set<SingularAttribute<? super Object, ?>> singularAttributes = entity.getSingularAttributes();

			List<String> keys = new ArrayList<>();
			for (Object o : kvValidKeysValues.keySet()) {
				keys.add(o.toString());
			}

			boolean allInkeys = true;
			for (SingularAttribute<? super Object, ?> sattr : singularAttributes) {
				if (sattr.isId()) {
					final Object value = keysValues.get(sattr.getName());
					if (!keys.contains(sattr.getName()) || !this.checkFilterValueUniqueness(value)) {
						allInkeys = false;
					} else {
						keys.remove(sattr.getName());
					}
				}
			}
			if (allInkeys && !keys.isEmpty()) {
				final String query = this.prepareQuery(QueryTemplateInformation.Syntax.JPQL, null, null, keysValues, null, cl);
				Query selectQuery = this.entityManager.createQuery(query);
				this.adaptQuery(selectQuery);
				List<?> totalData = selectQuery.getResultList();
				final EntityResult result = new EntityResult();
				if ((totalData != null) && (totalData.size() == 1)) {
					Object entityBean = totalData.get(0);
					this.entityManager.remove(entityBean);
					this.entityManager.flush();
					result.setCode(EntityResult.OPERATION_SUCCESSFUL);
					result.setMessage(I18NNaming.M_IT_HAS_CHANGED_N_RECORDS, Integer.toString(1));
					return result;

				} else if ((totalData != null) && (totalData.size() > 1)) {
					return new EntityResult(EntityResult.OPERATION_WRONG, EntityResult.OPERATION_WRONG,
							"something happened deleting entity " + cl.getCanonicalName() + ", more than one result returned with primary keys");
				} else {
					OntimizeJpaDaoSupport.logger.warn("nothing to delete");
					result.setCode(EntityResult.OPERATION_SUCCESSFUL_SHOW_MESSAGE);
					result.setMessage(I18NNaming.M_IT_HAS_NOT_CHANGED_ANY_RECORD);
					return result;
				}
			} else {
				OntimizeJpaDaoSupport.logger.warn("Trying to do a save delete without an entity that is not jpa persistent, handling with unsafedelete");
			}
		} else {
			OntimizeJpaDaoSupport.logger.warn("Trying to do a save delete without an entity that is not jpa persistent, handling with unsafedelete");
		}

		return this.unsafeDelete(keysValues);
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see com.ontimize.jee.server.dao.IOntimizeDaoSupport#unsafeDelete(java.util.Map)
	 */
	@Override
	public EntityResult unsafeDelete(final Map<?, ?> keysValues) {
		this.check();
		final Map<?, ?> kvWithoutReferenceAttributes = this.processReferenceDataFieldAttributes(keysValues);
		final Hashtable<Object, Object> kvValidKeysValues = new Hashtable<>();
		final Map<?, ?> processMultipleValueAttributes = this.processMultipleValueAttributes(kvWithoutReferenceAttributes);
		if (processMultipleValueAttributes != null) {
			kvValidKeysValues.putAll(processMultipleValueAttributes);
		}

		final Class<?> cl = this.getQueryEntityClass();

		// TODO add support to SQL deletes

		final EntityType<?> entity = this.entityManager.getMetamodel().entity(cl);
		if (entity != null) { // entity exists try JPQL delete
			final String clName = cl.getCanonicalName();
			final Character alias = StringUtils.substringAfterLast(clName, ".").toLowerCase().charAt(0);
			final StringBuilder uQuery = new StringBuilder("DELETE FROM");
			this.appendSpaceIfNecessary(uQuery).append(clName);
			this.appendSpaceIfNecessary(uQuery).append(alias);
			this.appendSpaceIfNecessary(uQuery);
			final String cond = this.createWhereCondition(Syntax.JPQL, kvValidKeysValues, null);
			uQuery.append(SQLStatementBuilder.WHERE);
			this.appendSpaceIfNecessary(uQuery);
			uQuery.append(cond);
			final Query query = this.entityManager.createQuery(uQuery.toString());
			final int executeUpdate = query.executeUpdate();
			final EntityResult result = new EntityResult();
			if (executeUpdate == 0) {
				result.setCode(EntityResult.OPERATION_SUCCESSFUL_SHOW_MESSAGE);
				result.setMessage(I18NNaming.M_IT_HAS_NOT_CHANGED_ANY_RECORD);
				return result;
			} else {
				result.setCode(EntityResult.OPERATION_SUCCESSFUL);
				result.setMessage(I18NNaming.M_IT_HAS_DELETED_N_RECORDS, Integer.toString(executeUpdate));
				return result;
			}
		} else {
			return new EntityResult(EntityResult.OPERATION_WRONG, EntityResult.OPERATION_WRONG, "entity " + cl.getCanonicalName() + " is not jpa persistent");
		}
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see com.ontimize.jee.server.dao.IOntimizeDaoSupport#unsafeUpdate(java.util.Map, java.util.Map)
	 */
	@Override
	public EntityResult unsafeUpdate(final Map<?, ?> attributesValues, final Map<?, ?> keysValues) {
		this.check();
		final Map<String, ?> validAttributes = this.adaptAttributesValues(attributesValues);
		final Map<?, ?> kvWithoutReferenceAttributes = this.processReferenceDataFieldAttributes(keysValues);
		final Hashtable<Object, Object> kvValidKeysValues = new Hashtable<>();
		final Map<?, ?> processMultipleValueAttributes = this.processMultipleValueAttributes(kvWithoutReferenceAttributes);
		if (processMultipleValueAttributes != null) {
			kvValidKeysValues.putAll(processMultipleValueAttributes);
		}
		final EntityResult result = new EntityResult();
		if (validAttributes.size() == 0) {
			OntimizeJpaDaoSupport.logger.warn("nothing to update");
			result.setCode(EntityResult.OPERATION_SUCCESSFUL_SHOW_MESSAGE);
			result.setMessage(I18NNaming.M_IT_HAS_NOT_CHANGED_ANY_RECORD);
			return result;
		}

		final Class<?> cl = this.getQueryEntityClass();

		// TODO add support to SQL updates
		final EntityType<?> entity = this.entityManager.getMetamodel().entity(cl);

		if (entity != null) { // entity exists try JPQL update
			final Set<String> bck = new HashSet<>(validAttributes.keySet());
			for (Iterator<String> keys = bck.iterator(); keys.hasNext();) {
				String key = keys.next();
				try {
					final Method meth = BeanUtils.findDeclaredMethodWithMinimalParameters(cl, MappingInfoUtils.buildSetterMethodName(key, null));
					if (meth == null) {
						validAttributes.remove(key);
						OntimizeJpaDaoSupport.logger.warn("Key '" + key + "' does not have a setter method declared on bean " + cl.getName());

					}
				} catch (IllegalArgumentException iae) {
					OntimizeJpaDaoSupport.logger.trace(null, iae);
					return new EntityResult(EntityResult.OPERATION_WRONG, EntityResult.OPERATION_WRONG,
							"entity " + cl.getCanonicalName() + " has not unique method for attribute " + key);
				}

			}
			if (validAttributes.isEmpty()) {
				OntimizeJpaDaoSupport.logger.warn("nothing to update");
				result.setCode(EntityResult.OPERATION_SUCCESSFUL_SHOW_MESSAGE);
				result.setMessage(I18NNaming.M_IT_HAS_NOT_CHANGED_ANY_RECORD);
				return result;
			}

			final String clName = cl.getCanonicalName();
			final Character alias = StringUtils.substringAfterLast(clName, ".").toLowerCase().charAt(0);
			final StringBuilder uQuery = new StringBuilder("UPDATE");
			this.appendSpaceIfNecessary(uQuery).append(clName);
			this.appendSpaceIfNecessary(uQuery).append(alias);
			this.appendSpaceIfNecessary(uQuery);
			final Map<String, Object> parametersToAdd = this.addSet(uQuery, validAttributes, Character.toString(alias));
			this.appendSpaceIfNecessary(uQuery);
			final String cond = this.createWhereCondition(Syntax.JPQL, kvValidKeysValues, null);
			uQuery.append(SQLStatementBuilder.WHERE);
			this.appendSpaceIfNecessary(uQuery);
			uQuery.append(cond);
			final Query query = this.entityManager.createQuery(uQuery.toString());
			this.addParametersToQuery(query, parametersToAdd);
			final int executeUpdate = query.executeUpdate();
			if (executeUpdate == 0) {
				result.setCode(EntityResult.OPERATION_SUCCESSFUL_SHOW_MESSAGE);
				result.setMessage(I18NNaming.M_IT_HAS_NOT_CHANGED_ANY_RECORD);
				return result;
			} else {
				result.setCode(EntityResult.OPERATION_SUCCESSFUL);
				result.setMessage(I18NNaming.M_IT_HAS_CHANGED_N_RECORDS, Integer.toString(executeUpdate));
				return result;
			}
		} else {
			return new EntityResult(EntityResult.OPERATION_WRONG, EntityResult.OPERATION_WRONG, "entity " + cl.getCanonicalName() + " is not jpa persistent");
		}
	}

	private void addParametersToQuery(final Query query, final Map<String, Object> parametersToAdd) {
		for (final Entry<String, Object> entry : parametersToAdd.entrySet()) {
			// check if parameter is boolean and entry data is different.
			Parameter param = query.getParameter(entry.getKey());
			if (param.getParameterType().equals(Boolean.class) && !entry.getValue().getClass().equals(Boolean.class)) {
				query.setParameter(entry.getKey(), ParseUtilsExtended.getBoolean(entry.getValue()));
			} else if (entry.getValue() instanceof NullValue) {
				query.setParameter(param, null);
			} else {
				query.setParameter(param, entry.getValue());
			}
		}
	}

	/**
	 * Adds the set.
	 *
	 * @param uQuery
	 *            the u query
	 * @param validAttributes
	 *            the valid attributes
	 * @param alias
	 *            the alias
	 */
	private Map<String, Object> addSet(final StringBuilder uQuery, final Map<String, ?> validAttributes, final String alias) {
		uQuery.append("SET");
		int i = 0;
		final Map<String, Object> parameters = new HashMap<>();
		for (final Entry<String, ?> entry : validAttributes.entrySet()) {
			this.appendSpaceIfNecessary(uQuery).append(alias).append('.').append(entry.getKey()).append('=');
			uQuery.append(":").append(OntimizeJpaDaoSupport.PARAM_PREFIX).append(i);
			parameters.put(OntimizeJpaDaoSupport.PARAM_PREFIX + i, entry.getValue());
			if (i < (validAttributes.entrySet().size() - 1)) {
				uQuery.append(", ");
			}

			i++;
		}
		return parameters;
	}

	/**
	 *
	 * @see com.ontimize.jee.server.dao.IOntimizeDaoSupport#insertBatch(java.util.Map[])
	 */
	@Override
	public int[] insertBatch(final Map<String, Object>[] batch) {
		this.check();
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Checks if is loaded.
	 *
	 * @return true, if checks if is loaded
	 */
	public boolean isLoaded() {
		return this.loaded;
	}

	/**
	 * Sets the entity manager.
	 *
	 * @param entityManager
	 *            the entity manager
	 */
	public void setEntityManager(final EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	/**
	 * Sets the configuration file.
	 *
	 * @param configurationFile
	 *            the configuration file
	 */
	public void setConfigurationFile(final String configurationFile) {
		this.configurationFile = configurationFile;
	}

	/**
	 * Sets the configuration file placeholder.
	 *
	 * @param configurationFilePlaceholder
	 *            the configuration file placeholder
	 */
	public void setConfigurationFilePlaceholder(final String configurationFilePlaceholder) {
		this.configurationFilePlaceholder = configurationFilePlaceholder;
	}

	/**
	 * Sets the jpql condition values processor.
	 *
	 * @param jpqlConditionValuesProcessor
	 *            the jpql condition values processor
	 */
	public void setJpqlConditionValuesProcessor(final DefaultJPQLConditionValuesProcessor jpqlConditionValuesProcessor) {
		this.jpqlConditionValuesProcessor = jpqlConditionValuesProcessor;
		if ((this.jpqlConditionValuesProcessor != null) && (this.jpqlValueToQLLiteralProcessor != null)) {
			this.jpqlConditionValuesProcessor.setValueToQLLiteralProcessor(this.jpqlValueToQLLiteralProcessor);
		}
	}

	/**
	 * Sets the sql condition values processor.
	 *
	 * @param sqlConditionValuesProcessor
	 *            the sql condition values processor
	 */
	public void setSqlConditionValuesProcessor(final DefaultSQLConditionValuesProcessor sqlConditionValuesProcessor) {
		this.sqlConditionValuesProcessor = sqlConditionValuesProcessor;
		if ((this.sqlConditionValuesProcessor != null) && (this.sqlValueToQLLiteralProcessor != null)) {
			this.sqlConditionValuesProcessor.setValueToQLLiteralProcessor(this.sqlValueToQLLiteralProcessor);
		}
	}

	/**
	 * Sets the row mapper provider.
	 *
	 * @param rowMapperProvider
	 *            the row mapper provider
	 */
	public void setRowMapperProvider(final IRowMapperProvider rowMapperProvider) {
		this.rowMapperProvider = rowMapperProvider;
	}

	/**
	 * Sets the jpql value to ql literal processor.
	 *
	 * @param valueToQLLiteralProcessor
	 *            the jpql value to ql literal processor
	 */
	public void setJpqlValueToQLLiteralProcessor(final ValueToQLLiteralProcessor valueToQLLiteralProcessor) {
		this.jpqlValueToQLLiteralProcessor = valueToQLLiteralProcessor;
		if ((this.jpqlConditionValuesProcessor != null) && (this.jpqlValueToQLLiteralProcessor != null)) {
			this.jpqlConditionValuesProcessor.setValueToQLLiteralProcessor(this.jpqlValueToQLLiteralProcessor);
		}
	}

	/**
	 * Sets the sql value to ql literal processor.
	 *
	 * @param valueToQLLiteralProcessor
	 *            the sql value to ql literal processor
	 */
	public void setSqlValueToQLLiteralProcessor(final ValueToQLLiteralProcessor valueToQLLiteralProcessor) {
		this.sqlValueToQLLiteralProcessor = valueToQLLiteralProcessor;
		if ((this.sqlConditionValuesProcessor != null) && (this.sqlValueToQLLiteralProcessor != null)) {
			this.sqlConditionValuesProcessor.setValueToQLLiteralProcessor(this.sqlValueToQLLiteralProcessor);
		}
	}

	/**
	 * Sets the entity bean class.
	 *
	 * @param entityBeanClass
	 *            the entity bean class
	 */
	public void setEntityBeanClass(final Class<?> entityBeanClass) {
		this.entityBeanClass = entityBeanClass;
	}

	/**
	 * Sets the entity bean name.
	 *
	 * @param entityBeanName
	 *            the entity bean name
	 */
	public void setEntityBeanName(final String entityBeanName) {
		this.entityBeanName = entityBeanName;
	}

	/*
	 * Prepare query
	 */
	/**
	 * Prepare query.
	 *
	 * @param syntax
	 *            the syntax
	 * @param queryTemplateInformation
	 *            the query template information
	 * @param attributes
	 *            the attributes
	 * @param keysValues
	 *            the keys values
	 * @param sort
	 *            the sort
	 * @param beanClass
	 *            the bean class
	 * @return the string
	 */
	protected String prepareQuery(final Syntax syntax, final QueryTemplateInformation queryTemplateInformation, final List<String> attributes, final Map<?, ?> keysValues,
			final List<Object> sort, final Class<?> beanClass) {

		List<String> vattributes = this.processReferenceDataFieldAttributes(attributes);

		final Map<?, ?> kvWithoutReferenceAttributes = this.processReferenceDataFieldAttributes(keysValues);
		Map<Object, Object> kvValidKeysValues = new HashMap<>();
		final Map<?, ?> processMultipleValueAttributes = this.processMultipleValueAttributes(kvWithoutReferenceAttributes);
		if (processMultipleValueAttributes != null) {
			kvValidKeysValues.putAll(processMultipleValueAttributes);
		}

		StringBuilder ql = new StringBuilder();
		String template = null;
		List<String> whereKeys = this.findWhereKeys(kvValidKeysValues);

		String beanPrefix = null;
		if ((queryTemplateInformation == null) && syntax.equals(Syntax.JPQL)) {
			// use entity
			beanPrefix = this.getBeanPrefix(beanClass);
			if (beanPrefix != null) {
				ql = this.addSelect(ql, beanClass);
				ql = this.addFrom(ql, beanClass);
				for (String whereKey : whereKeys) {
					this.appendSpaceIfNecessary(ql).append(whereKey);
				}
				this.appendSpaceIfNecessary(ql).append(OntimizeJpaDaoSupport.PLACEHOLDER_ORDER);
			} else {
				ql.append("ERROR_BAD_ENTITY_DEFINITION");
			}
			template = ql.toString();
		} else if ((queryTemplateInformation != null) && syntax.equals(Syntax.JPQL)) {
			template = queryTemplateInformation.getSqlTemplate();
		} else if (queryTemplateInformation != null) {// for SQL queries (DEFAULT SYNTAX)
			template = queryTemplateInformation.getSqlTemplate();
			if (vattributes != null) {
				vattributes = this.applySQLTransformations(queryTemplateInformation, vattributes); // disambiguity and function columns
			} else {
				vattributes = new ArrayList<>();
			}
			kvValidKeysValues = this.applySQLTransformations(queryTemplateInformation, kvValidKeysValues);// disambiguity and function columns
			final StringBuilder sbColumns = new StringBuilder();
			// columns
			for (final String ob : vattributes) {
				sbColumns.append(ob);
				sbColumns.append(SQLStatementBuilder.COMMA);
			}
			if (!vattributes.isEmpty()) {
				for (int i = 0; i < SQLStatementBuilder.COMMA.length(); i++) {
					sbColumns.deleteCharAt(sbColumns.length() - 1);
				}
			}
			template = template.replaceAll(OntimizeJpaDaoSupport.PLACEHOLDER_COLUMNS_CONCAT, sbColumns.length() == 0 ? "" : SQLStatementBuilder.COMMA + " " + sbColumns.toString());
			template = template.replaceAll(OntimizeJpaDaoSupport.PLACEHOLDER_COLUMNS, sbColumns.toString());
		} else {
			throw new IllegalArgumentException("query template cannot be null");
		}

		// Where
		for (String whereKey : whereKeys) {
			final String cond = this.createWhereCondition(whereKey, syntax, kvValidKeysValues, beanPrefix);
			String whereKeyConcat = this.buildWhereKeyConcat(whereKey);
			template = template.replaceAll(whereKeyConcat, cond.length() == 0 ? "" : SQLStatementBuilder.AND + " " + cond);
			template = template.replaceAll(whereKey, cond.length() == 0 ? "" : SQLStatementBuilder.WHERE + " " + cond);
		}

		// Order by
		final String order = this.createSort(syntax, sort, beanPrefix);
		template = template.replaceAll(OntimizeJpaDaoSupport.PLACEHOLDER_ORDER_CONCAT, order.length() == 0 ? "" : SQLStatementBuilder.COMMA + " " + order);
		template = template.replaceAll(OntimizeJpaDaoSupport.PLACEHOLDER_ORDER, order.length() == 0 ? "" : SQLStatementBuilder.ORDER_BY + " " + order);
		return template;
	}

	private String buildWhereKeyConcat(String whereKey) {
		if ((whereKey == null) || whereKey.equals(OntimizeJpaDaoSupport.PLACEHOLDER_WHERE) || whereKey.equals(OntimizeJpaDaoSupport.PLACEHOLDER_WHERE_CONCAT)) {
			return OntimizeJpaDaoSupport.PLACEHOLDER_WHERE_CONCAT;
		} else {
			if (whereKey.endsWith("#")) {
				return whereKey.substring(0, whereKey.length() - 1) + "_CONCAT#";
			} else {
				return whereKey + "_CONCAT";
			}
		}
	}

	private List<String> findWhereKeys(Map<?, ?> keysValues) {
		List<String> result = new ArrayList<>();
		Object object = keysValues.get(ExtraWhereSections.EXTRA_WHERE_SECTIONS);
		if (object instanceof ExtraWhereSections) {
			for (String key : ((ExtraWhereSections) object).getWhereSections().keySet()) {
				result.add(key);
			}
		}
		result.add(OntimizeJpaDaoSupport.PLACEHOLDER_WHERE);
		return result;
	}

	/**
	 * Creates the sort.
	 *
	 * @param syntax
	 *            the syntax
	 * @param sort
	 *            the sort
	 * @param beanPrefix
	 *            the bean prefix
	 * @return the string
	 */
	private String createSort(final Syntax syntax, final List<Object> sort, String beanPrefix) {
		if ((sort == null) || (sort.size() == 0)) {
			return "";
		}
		final StringBuilder sb = new StringBuilder();
		int i = 0;
		for (final Object o : sort) {
			if (syntax.equals(Syntax.JPQL) && (beanPrefix != null)) {
				sb.append(beanPrefix + ".");
			}
			if (o instanceof String) {
				sb.append((String) o).append(" ASC ");
			} else if (o instanceof SQLOrder) {
				final SQLOrder sqlOrder = (SQLOrder) o;
				sb.append(sqlOrder.getColumnName());
				if (sqlOrder.isAscendent()) {
					sb.append(" ASC ");
				} else {
					sb.append(" DESC ");
				}
			}
			if (i < (sort.size() - 1)) {
				sb.append(", ");
			}
			i++;
		}
		return sb.toString();
	}

	/**
	 * Creates the where condition.
	 *
	 * @param whereKey
	 *
	 * @param syntax
	 *            the syntax
	 * @param kvValidKeysValues
	 *            the kv valid keys values
	 * @param beanPrefix
	 *            the bean prefix
	 * @return the string
	 */
	private String createWhereCondition(String whereKey, final Syntax syntax, final Map<Object, Object> kvValidKeysValues, String beanPrefix) {
		if ((whereKey != null) && (kvValidKeysValues != null)) {
			Object object = kvValidKeysValues.get(ExtraWhereSections.EXTRA_WHERE_SECTIONS);
			if (object instanceof ExtraWhereSections) {
				WhereSection whereSection = ((ExtraWhereSections) object).getWhereSections().get(whereKey);
				if (whereSection != null) {
					return this.createWhereCondition(syntax, whereSection.getKeyValues(), beanPrefix);
				}
			}
		}
		if (((whereKey == null) || whereKey.equals(OntimizeJpaDaoSupport.PLACEHOLDER_WHERE)) && (kvValidKeysValues != null)) {
			Map<Object, Object> map = new HashMap<>(kvValidKeysValues);
			map.remove(ExtraWhereSections.EXTRA_WHERE_SECTIONS);
			return this.createWhereCondition(syntax, map, beanPrefix);
		} else {
			return "";
		}
	}

	/**
	 * Creates the where condition.
	 *
	 * @param syntax
	 *            the syntax
	 * @param kvValidKeysValues
	 *            the kv valid keys values
	 * @param beanPrefix
	 *            the bean prefix
	 * @return the string
	 */
	private String createWhereCondition(final Syntax syntax, final Map<Object, Object> kvValidKeysValues, String beanPrefix) {
		if ((kvValidKeysValues == null) || (kvValidKeysValues.size() == 0)) {
			return "";
		}
		if (syntax.equals(Syntax.JPQL)) {
			if (this.jpqlConditionValuesProcessor != null) {
				this.jpqlConditionValuesProcessor.setBeanPrefix(beanPrefix);
				return this.jpqlConditionValuesProcessor.createQueryConditions(kvValidKeysValues, new ArrayList<String>());
			}
		} else if (syntax.equals(Syntax.SQL)) {
			if (this.sqlConditionValuesProcessor != null) {
				return this.sqlConditionValuesProcessor.createQueryConditions(kvValidKeysValues, new ArrayList<String>());
			}
		}
		return "";
	}

	/**
	 * Adds the from.
	 *
	 * @param jpql
	 *            the jpql
	 * @param beanClass
	 *            the bean class
	 * @return the string builder
	 */
	private StringBuilder addFrom(final StringBuilder jpql, final Class<?> beanClass) {
		final EntityType<?> entity = this.entityManager.getMetamodel().entity(beanClass);
		if (entity != null) {
			final String name = entity.getName();
			this.appendSpaceIfNecessary(jpql).append(OntimizeJpaDaoSupport.FROM);
			if (name != null) {
				this.appendSpaceIfNecessary(jpql).append(name);
				this.appendSpaceIfNecessary(jpql).append(name.charAt(0));
			} else {
				this.appendSpaceIfNecessary(jpql).append(beanClass.getCanonicalName());
				this.appendSpaceIfNecessary(jpql).append("t");
			}
			return jpql;
		} else {
			return jpql.append("ERROR_BAD_ENTITY_DEFINITION");
		}
	}

	/**
	 * Gets first letter of bean
	 *
	 * @param beanClass
	 *            the bean class
	 * @return the first letter of the bean
	 */
	protected String getBeanPrefix(final Class<?> beanClass) {
		String beanPrefix = null;
		EntityType<?> entity = this.entityManager.getMetamodel().entity(beanClass);
		if (entity != null) {
			String entityName = entity.getName();
			if (entityName != null) {
				beanPrefix = String.valueOf(entityName.charAt(0));
			} else {
				beanPrefix = "t";
			}
		}
		return beanPrefix;
	}

	/**
	 * Adds the select.
	 *
	 * @param jpql
	 *            the jpql
	 * @param beanClass
	 *            the bean class
	 * @return the string builder
	 */
	private StringBuilder addSelect(final StringBuilder jpql, final Class<?> beanClass) {
		final EntityType<?> entity = this.entityManager.getMetamodel().entity(beanClass);
		if (entity != null) {
			final String name = entity.getName();
			this.appendSpaceIfNecessary(jpql).append(OntimizeJpaDaoSupport.SELECT);
			if (name != null) {
				this.appendSpaceIfNecessary(jpql).append(name.charAt(0));
			} else {
				this.appendSpaceIfNecessary(jpql).append("t");
			}
			return jpql;
		} else {
			return jpql.append("ERROR_BAD_ENTITY_DEFINITION");
		}
	}

	/**
	 * Append space if necessary.
	 *
	 * @param jpql
	 *            the jpql
	 * @return the string builder
	 */
	private StringBuilder appendSpaceIfNecessary(final StringBuilder jpql) {
		return jpql.append(OntimizeJpaDaoSupport.SPACE);
	}

	/**
	 * Processes the ReferenceFieldAttribute objects contained in <code>keysValues</code>. <p> Returns a hashtable containing all the objects contained in the argument
	 * <code>keysValues</code> except in the case of keys that are ReferenceFieldAttribute objects, which are replaced by ((ReferenceFieldAttribute)object).getAttr() <p>
	 *
	 * @param keysValues
	 *            the keysValues to process
	 * @return a hashtable containing the processed objects
	 */
	private Map<?, ?> processReferenceDataFieldAttributes(final Map<?, ?> keysValues) {
		if (keysValues == null) {
			return null;
		}
		final Map<Object, Object> res = new HashMap<>();
		for (final Entry<?, ?> entry : keysValues.entrySet()) {
			final Object oKey = entry.getKey();
			final Object oValue = entry.getValue();
			if (oKey instanceof ReferenceFieldAttribute) {
				final String attr = ((ReferenceFieldAttribute) oKey).getAttr();
				res.put(attr, oValue);
			} else if ((oKey instanceof String) && ((String) oKey).equalsIgnoreCase(ExtraWhereSections.EXTRA_WHERE_SECTIONS)) {
				if (oValue instanceof ExtraWhereSections) {
					ExtraWhereSections ews = (ExtraWhereSections) oValue;
					Map<String, WhereSection> whereSections = ews.getWhereSections();
					for (Entry<String, WhereSection> wsentry : whereSections.entrySet()) {
						WhereSection value = wsentry.getValue();
						value.setKeyValues((Map<Object, Object>) this.processReferenceDataFieldAttributes(value.getKeyValues()));
					}
					res.put(oKey, oValue);
				}

			} else {
				res.put(oKey, oValue);
			}
		}
		return res;
	}

	/**
	 * Processes the ReferenceFieldAttribute objects contained in <code>list</code>. <p> Returns a List containing all the objects in the argument <code>list</code> except in the
	 * case of keys that are ReferenceFieldAttribute objects, which are maintained but also ((ReferenceFieldAttribute)object).getAttr() is added <p>
	 *
	 * @param list
	 *            the list to process
	 * @return a vector containing the processed objects
	 */
	private List<String> processReferenceDataFieldAttributes(final List<?> list) {
		if (list == null) {
			return null;
		}
		final List<String> res = new ArrayList<>();
		for (final Object ob : list) {
			if (ob instanceof String) {
				// Add the attribute
				if (!res.contains(ob)) {
					res.add((String) ob);
				}
			} else
				// If the attribute is ReferenceFieldAttribute add the string to
				if ((ob instanceof ReferenceFieldAttribute) && !res.contains(((ReferenceFieldAttribute) ob).getAttr())) {
					res.add(((ReferenceFieldAttribute) ob).getAttr());
				}
		}
		return res;
	}

	/**
	 * Apply template prefix.
	 *
	 * @param templateInformation
	 *            the template information
	 * @param vValidAttributes
	 *            the v valid attributes
	 * @return the list
	 */
	private List<String> applySQLTransformations(final QueryTemplateInformation templateInformation, final List<String> vValidAttributes) {
		final List<AmbiguousColumnType> ambiguousColumns = templateInformation.getAmbiguousColumns();
		final List<FunctionColumnType> functionColumns = templateInformation.getFunctionColumns();
		final MappingInfo mappingInfo = templateInformation.getMappingInfo();

		final List<String> res = new ArrayList<>(vValidAttributes.size());
		if (mappingInfo == null) {
			return res;
		}
		for (final String va : vValidAttributes) {
			String ob = OntimizeJpaUtils.getDBColumnName(mappingInfo, va);
			if (ob == null) {
				continue;
			}
			boolean transformed = false;
			if (ambiguousColumns != null) {
				for (final AmbiguousColumnType ambiguosColumn : ambiguousColumns) {
					if (ob.toUpperCase().equals(ambiguosColumn.getName().toUpperCase())) {
						final String dbName = ambiguosColumn.getDatabaseName() == null ? ambiguosColumn.getName() : ambiguosColumn.getDatabaseName();
						final StringBuilder sb = new StringBuilder();
						sb.append(ambiguosColumn.getPrefix());
						sb.append(".");
						sb.append(dbName);
						sb.append(SQLStatementBuilder.AS);
						sb.append(ambiguosColumn.getName());
						res.add(sb.toString());
						transformed = true;
						break;
					}
				}
			}
			if (!transformed && (functionColumns != null)) {
				for (final FunctionColumnType functionColumn : functionColumns) {
					if (ob.toUpperCase().equals(functionColumn.getName().toUpperCase())) {
						final StringBuilder sb = new StringBuilder();
						sb.append(SQLStatementBuilder.OPEN_PARENTHESIS);
						sb.append(functionColumn.getValue());
						sb.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
						sb.append(SQLStatementBuilder.AS);
						sb.append(functionColumn.getName());
						res.add(sb.toString());
						transformed = true;
						break;
					}
				}
			}
			if (!transformed) {
				res.add(ob);
			}
		}
		return res;
	}

	/**
	 * Apply template prefix.
	 *
	 * @param templateInformation
	 *            the template information
	 * @param kvValidKeysValues
	 *            the kv valid keys values
	 * @return the hashtable
	 */
	protected Hashtable<Object, Object> applySQLTransformations(final QueryTemplateInformation templateInformation, final Map<Object, Object> kvValidKeysValues) {
		final List<AmbiguousColumnType> ambiguousColumns = templateInformation.getAmbiguousColumns();
		final List<FunctionColumnType> functionColumns = templateInformation.getFunctionColumns();
		final MappingInfo mappingInfo = templateInformation.getMappingInfo();

		final Hashtable<Object, Object> res = new Hashtable<>();
		for (final Entry<Object, Object> kvEntry : kvValidKeysValues.entrySet()) {
			if (kvEntry.getKey() instanceof String) {
				String ob = (String) kvEntry.getKey();
				if (ob.equalsIgnoreCase(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY)) {
					Expression val = this.applySQLTransformations((Expression) kvEntry.getValue(), mappingInfo, ambiguousColumns, functionColumns);
					if (val != null) {
						res.put(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY, val);
					}
				} else if (ob.equals(ExtraWhereSections.EXTRA_WHERE_SECTIONS)) {
					Object value = kvEntry.getValue();
					if (value instanceof ExtraWhereSections) {
						Map<String, WhereSection> whereSections = ((ExtraWhereSections) value).getWhereSections();
						for (Entry<String, WhereSection> entry : whereSections.entrySet()) {
							entry.getValue().setKeyValues(this.applySQLTransformations(templateInformation, entry.getValue().getKeyValues()));
						}
						res.put(ExtraWhereSections.EXTRA_WHERE_SECTIONS, value);
					}
				} else {
					res.put(this.transformColumnName(ob, mappingInfo, ambiguousColumns, functionColumns), kvEntry.getValue());

				}
			} else {
				res.put(kvEntry.getKey(), kvEntry.getValue());
			}
		}
		return res;
	}

	protected Expression applySQLTransformations(Expression value, MappingInfo mappingInfo, List<AmbiguousColumnType> ambiguousColumns, List<FunctionColumnType> functionColumns) {
		if (value != null) {
			Object leftOperand = value.getLeftOperand();
			if (leftOperand instanceof Field) {
				leftOperand = new BasicField(this.transformColumnName(leftOperand.toString(), mappingInfo, ambiguousColumns, functionColumns));
			} else if (leftOperand instanceof Expression) {
				leftOperand = this.applySQLTransformations((Expression) leftOperand, mappingInfo, ambiguousColumns, functionColumns);
			}

			Object rightOperand = value.getRightOperand();
			if (rightOperand instanceof Field) {
				rightOperand = new BasicField(this.transformColumnName(rightOperand.toString(), mappingInfo, ambiguousColumns, functionColumns));
			} else if (rightOperand instanceof Expression) {
				rightOperand = this.applySQLTransformations((Expression) rightOperand, mappingInfo, ambiguousColumns, functionColumns);
			}

			value.setLeftOperand(leftOperand);
			value.setRightOperand(rightOperand);
			return value;
		}
		return null;
	}

	/**
	 * Transform column name.
	 *
	 * @param ob
	 *            the ob
	 * @param mappingInfo
	 *            the mapping info
	 * @param ambiguousColumns
	 *            the ambiguous columns
	 * @param functionColumns
	 *            the function columns
	 * @return the string
	 */
	protected String transformColumnName(String ob, final MappingInfo mappingInfo, final List<AmbiguousColumnType> ambiguousColumns,
			final List<FunctionColumnType> functionColumns) {
		if (mappingInfo != null) {
			String obTemp = OntimizeJpaUtils.getDBColumnName(mappingInfo, ob);
			if (obTemp == null) {
				obTemp = ob;
			}
			boolean transformed = false;
			if (ambiguousColumns != null) {
				for (final AmbiguousColumnType ambiguosColumn : ambiguousColumns) {
					if (obTemp.toString().toUpperCase().equals(ambiguosColumn.getName().toUpperCase())) {
						final String dbName = ambiguosColumn.getDatabaseName() == null ? obTemp.toString() : ambiguosColumn.getDatabaseName();
						obTemp = ambiguosColumn.getPrefix() + "." + dbName;
						transformed = true;
						break;
					}
				}
			}
			if (!transformed && (functionColumns != null)) {
				for (final FunctionColumnType functionColumn : functionColumns) {
					if (obTemp.toString().toUpperCase().equals(functionColumn.getName().toUpperCase())) {
						obTemp = functionColumn.getValue();
						transformed = true;
						break;
					}
				}
			}
			return obTemp;

		}
		return ob;

	}

	/**
	 * Processes the MultipleValue objects contained in <code>keysValues</code>. Returns a new Hashtable with the same data as <code>keysValues</code> except that MultipleValue
	 * objects are deleted and the key-value pairs of these objects are added to the new Hashtable.
	 *
	 * @param keysValues
	 *            the keys values
	 * @return a new Hashtable with MultipleValue objects replaced by their key-value pairs
	 */
	private Map<?, ?> processMultipleValueAttributes(final Map<?, ?> keysValues) {
		if (keysValues == null) {
			return null;
		}
		final Map<Object, Object> res = new HashMap<>();
		for (final Entry<?, ?> entry : keysValues.entrySet()) {
			final Object oKey = entry.getKey();
			final Object oValue = entry.getValue();
			if (oValue instanceof MultipleValue) {
				final Enumeration<?> mvKeys = ((MultipleValue) oValue).keys();
				while (mvKeys.hasMoreElements()) {
					final Object iMvKeyM = mvKeys.nextElement();
					final Object oMvValue = ((MultipleValue) oValue).get(iMvKeyM);
					res.put(iMvKeyM, oMvValue);
				}
			} else {
				res.put(oKey, oValue);
			}
		}
		return res;
	}

	/**
	 * Gets the template query.
	 *
	 * @param id
	 *            the id
	 * @return the template query
	 */
	public QueryTemplateInformation getQueryTemplateInformation(final String id) {
		this.check();
		return this.sqlQueries.get(id);
	}

	/*
	 * utility methods
	 */

	/**
	 * Gets the query entity class.
	 *
	 * @return the query entity class
	 */
	private Class<?> getQueryEntityClass() {
		return this.entityBeanClass;
	}

	/**
	 * New entity result error query entity class null.
	 *
	 * @return the entity result
	 */
	private EntityResult newEntityResultErrorQueryEntityClassNull() {
		return new EntityResult(EntityResult.OPERATION_WRONG, EntityResult.OPERATION_WRONG, "MC_ERROR_ENTITY_CLASS_IS_NULL");
	}

	/*
	 * Filtering and adapting
	 */
	/**
	 * Adapt attributes.
	 *
	 * @param attributes
	 *            the attributes
	 * @return the list< string>
	 */
	private List<String> adaptAttributes(final List<?> attributes) {
		final List<String> validAttr = new ArrayList<>();
		if (attributes != null) {
			for (final Object att : attributes) {
				if (att != null) {
					final String adapted = this.adaptAttribute(att);
					if (adapted != null) {
						validAttr.add(adapted);
					}
				}
			}
		}
		return validAttr;
	}

	/**
	 * Adapt attributes values.
	 *
	 * @param attributesValues
	 *            the attributes values
	 * @return the map< string,?>
	 */
	private Map<String, ?> adaptAttributesValues(final Map<?, ?> attributesValues) {
		final Map<String, Object> validAttr = new HashMap<>();
		if (attributesValues != null) {
			for (final Entry<?, ?> attEnt : attributesValues.entrySet()) {
				final String adapted = this.adaptAttribute(attEnt.getKey());
				if (adapted != null) {
					validAttr.put(adapted, attEnt.getValue());
				}
			}
		}
		return validAttr;
	}

	/**
	 * Adapt sort.
	 *
	 * @param sort
	 *            the sort
	 * @return the list< object>
	 */
	private List<Object> adaptSort(final List<?> sort) {
		final List<Object> validSort = new ArrayList<>();
		if (sort != null) {
			for (final Object s : sort) {
				if (s instanceof SQLOrder) {
					final String adapted = this.adaptAttribute(((SQLOrder) s).getColumnName());
					if (adapted != null) {
						final SQLOrder sOrder = new SQLOrder(adapted, ((SQLOrder) s).isAscendent());
						validSort.add(sOrder);
					}
				} else {
					final String adapted = this.adaptAttribute(s);
					if (adapted != null) {
						validSort.add(adapted);
					}
				}

			}
		}
		return validSort;
	}

	/**
	 * Adapt attribute.
	 *
	 * @param attribute
	 *            the attribute
	 * @return the string
	 */
	protected String adaptAttribute(final Object attribute) {
		if (attribute instanceof TableAttribute) {
			return null;
		}
		if (attribute instanceof MultipleTableAttribute) {
			return null;
		}
		// TODO else controlar los demas tipos de keys que nos pueden pasar aqui
		// TODO check if entity bean contains attribute, if it doesn't return null;
		return attribute.toString();
	}

	/**
	 * Unadapt attributes in result.
	 *
	 * @param result
	 *            the result
	 * @param validAttributes
	 *            the valid attributes
	 * @return the entity result
	 */
	private EntityResult unadaptAttributesInResult(final EntityResult result, final List<String> validAttributes) {
		final boolean keepOriginal = (validAttributes == null) || (validAttributes.size() == 0);
		final Map<String, Object> toAdd = new Hashtable<>();
		if (validAttributes != null) {
			for (final Object entryObject : result.entrySet()) {
				if (entryObject instanceof Entry) {
					final Entry<?, ?> entry = (Entry<?, ?>) entryObject;
					for (final String vatt : validAttributes) {
						if (entry.getKey().equals(vatt)) {
							final String unadapted = this.unadaptAttribute(vatt);
							toAdd.put(unadapted, entry.getValue());
							break;
						}
					}
				}
			}
		}
		if (!keepOriginal) {
			result.clear();
		}
		result.putAll(toAdd);
		return result;
	}

	/**
	 * Unadapt attribute.
	 *
	 * @param attribute
	 *            the attribute
	 * @return the string
	 */
	protected String unadaptAttribute(final String attribute) {
		return attribute;
	}

	/**
	 * Adapt entity result.
	 *
	 * @param result
	 *            the result
	 * @return the entity result
	 */
	protected EntityResult adaptEntityResult(final EntityResult result) {
		return result;
	}

	/**
	 * Adapt query.
	 *
	 * @param selectQuery
	 *            the select query
	 */
	protected void adaptQuery(final Query selectQuery) {
		// TODO set hints (variable for jpa implementations)

		// org.hibernate.timeout Query timeout in seconds ( eg. new Integer(10) )

		// org.hibernate.fetchSize Number of rows fetched by the JDBC driver per roundtrip ( eg. new Integer(50) )

		// org.hibernate.comment Add a comment to the SQL query, useful for the DBA ( e.g. new
		// String("fetch all orders in 1 statement") )

		// org.hibernate.cacheable Whether or not a query is cacheable ( eg. new Boolean(true) ), defaults to false

		// org.hibernate.cacheMode Override the cache mode for this query ( eg. CacheMode.REFRESH )

		// org.hibernate.cacheRegion Cache region of this query ( eg. new String("regionName") )

		// org.hibernate.readOnly Entities retrieved by this query will be loaded in a read-only mode where Hibernate
		// will never dirty-check them or make changes persistent ( eg. new Boolean(true) ), default to false

		// org.hibernate.flushMode Flush mode used for this query (useful to pass Hibernate specific flush modes, in
		// particular MANUAL).

		// org.hibernate.cacheMode Cache mode used for this query
	}

	/*
	 * LOADING CONFIGURATION
	 */
	/**
	 * Check whether this operation has been compiled already; lazily compile it if not already compiled. <p>
	 */
	protected void check() {
		if (!this.isLoaded()) {
			OntimizeJpaDaoSupport.logger.debug("jpa conf not compiled before execution - invoking compile");
			this.load();
		}
	}

	/**
	 * Load.
	 */
	private void load() {
		if (!this.isLoaded()) {
			final ConfigurationFile annotation = this.getClass().getAnnotation(ConfigurationFile.class);
			if (annotation != null) {
				this.loadConfigurationFile(annotation.configurationFile(), annotation.configurationFilePlaceholder());
			} else if (this.configurationFile != null) {
				this.loadConfigurationFile(this.configurationFile, this.configurationFilePlaceholder);
			} else {
				// we suppose entityBeanName is already defined so, at least, we can do basic crud operations
				if ((this.entityBeanName != null) && (this.entityBeanClass == null)) {
					try {
						this.entityBeanClass = Class.forName(this.entityBeanName);
					} catch (final ClassNotFoundException e) {
						throw new InvalidDataAccessApiUsageException(I18NNaming.M_ERROR_LOADING_JPA_ENTITY, e);
					}
				} else if (this.entityBeanClass != null) {
					this.entityBeanName = this.entityBeanClass.getCanonicalName();
				} else {
					throw new InvalidDataAccessApiUsageException(I18NNaming.M_ERROR_LOADING_JPA_ENTITY);
				}
			}

			this.onLoadInternal();
			this.loaded = true;
			if (OntimizeJpaDaoSupport.logger.isDebugEnabled()) {
				OntimizeJpaDaoSupport.logger.debug("Bean configuration for [" + this.entityBeanName + "] compiled");
			}
		}
	}

	/**
	 * On load internal.
	 */
	protected void onLoadInternal() {
		// by default this method does nothing
	}

	/**
	 * Load the configuration file.
	 *
	 * @param path
	 *            the path
	 * @param pathToPlaceHolder
	 *            the path to place holder
	 * @throws InvalidDataAccessApiUsageException
	 *             the invalid data access api usage exception
	 */
	protected void loadConfigurationFile(final String path, final String pathToPlaceHolder) throws InvalidDataAccessApiUsageException {

		try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);) {
			Reader reader = null;
			if (pathToPlaceHolder != null) {
				try (InputStream isPlaceHolder = Thread.currentThread().getContextClassLoader().getResourceAsStream(pathToPlaceHolder);) {
					final Properties prop = new Properties();
					prop.load(isPlaceHolder);
					reader = new ReplaceTokensFilterReader(new InputStreamReader(is), new HashMap<String, String>((Map) prop));
				}
			} else {
				reader = new InputStreamReader(is);
			}

			final JpaEntitySetupType setup = JAXB.unmarshal(reader, JpaEntitySetupType.class);
			this.entityBeanName = setup.getBean();
			try {
				this.entityBeanClass = Class.forName(this.entityBeanName);
			} catch (final ClassNotFoundException e) {
				throw new InvalidDataAccessApiUsageException(I18NNaming.M_ERROR_LOADING_JPA_ENTITY, e);
			}
			if (setup.getQueries() != null) {
				for (final QueryType query : setup.getQueries().getQuery()) {
					final Class<?> returnTypeClass = query.getReturn() != null ? Class.forName(query.getReturn().getReturnType()) : this.entityBeanClass;
					final MappingInfo mappingInfo = MappingInfoUtils.buildMappingInfo(query, this.entityBeanClass, returnTypeClass);
					this.addQueryTemplateInformation(query.getId(), query.getSentence().getValue(), Syntax.valueOf(query.getSyntax()), returnTypeClass, mappingInfo,
							query.getAmbiguousColumns() != null ? query.getAmbiguousColumns().getAmbiguousColumn() : new ArrayList<AmbiguousColumnType>(),
									query.getFunctionColumns() != null ? query.getFunctionColumns().getFunctionColumn() : new ArrayList<FunctionColumnType>());
				}
			}
			final String entityManagerName = setup.getEntitymanager();
			if (!CheckingTools.isStringEmpty(entityManagerName)) {
				this.setEntityManager((EntityManager) this.applicationContext.getBean(entityManagerName));
			}
		} catch (final IOException e) {
			throw new InvalidDataAccessApiUsageException(I18NNaming.M_ERROR_LOADING_CONFIGURATION_FILE, e);
		} catch (final ClassNotFoundException e) {
			throw new InvalidDataAccessApiUsageException(I18NNaming.M_ERROR_LOADING_CONFIGURATION_FILE, e);
		}

	}

	/**
	 * Adds a query.
	 *
	 * @param id
	 *            the id
	 * @param value
	 *            the value
	 * @param syntax
	 *            the syntax
	 * @param resultClass
	 *            the result class
	 * @param mappingInfo
	 *            the mapping info
	 * @param ambiguousColumns
	 *            the ambiguous columns
	 * @param functionColumns
	 *            the function columns
	 */
	public void addQueryTemplateInformation(final String id, final String value, final Syntax syntax, final Class<?> resultClass, final MappingInfo mappingInfo,
			final List<AmbiguousColumnType> ambiguousColumns, final List<FunctionColumnType> functionColumns) {
		this.sqlQueries.put(id, new QueryTemplateInformation(value, syntax, resultClass, mappingInfo, ambiguousColumns, functionColumns));
	}

	/**
	 * Sets the application context.
	 *
	 * @param applicationContext
	 *            the new application context
	 * @throws BeansException
	 *             the beans exception
	 */
	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
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

	@Override
	public List<DaoProperty> getCudProperties() {
		// TODO implement me please!
		return null;
	}

	@Override
	public void reload() {
		// TODO Auto-generated method stub

	}

}

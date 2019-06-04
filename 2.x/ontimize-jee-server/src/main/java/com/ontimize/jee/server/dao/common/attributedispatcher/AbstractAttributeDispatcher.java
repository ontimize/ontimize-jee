package com.ontimize.jee.server.dao.common.attributedispatcher;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import com.ontimize.db.EntityResult;
import com.ontimize.jee.common.services.ServiceTools;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.server.spring.SpringTools;

/**
 * The Class AbstractAttributeDispatcher.
 *
 * @param <T>
 *            the generic type
 */
public abstract class AbstractAttributeDispatcher<T> implements IAttributeDispatcher<T> {

	/**
	 * Invoca la consulta al servicio correspondiente que indica el nombre de la entidad.
	 *
	 * @param applicationContext
	 *            the application context
	 * @param entityName
	 *            the entity name
	 * @param filter
	 *            the filter
	 * @param attributes
	 *            the attributes
	 * @return the entity result
	 */
	protected EntityResult invokeQuery(ApplicationContext applicationContext, String entityName, Map<?, ?> filter, List<?> attributes) {
		return this.invoke(applicationContext, entityName, IAttributeDispatcher.QUERY_SUFFIX, filter, attributes);
	}

	/**
	 * Invoke insert.
	 *
	 * @param applicationContext
	 *            the application context
	 * @param entityName
	 *            the entity name
	 * @param filter
	 *            the filter
	 * @return the entity result
	 */
	protected EntityResult invokeInsert(ApplicationContext applicationContext, String entityName, Map<?, ?> filter) {
		return this.invoke(applicationContext, entityName, IAttributeDispatcher.INSERT_SUFFIX, filter);
	}

	/**
	 * Invoke update.
	 *
	 * @param applicationContext
	 *            the application context
	 * @param entityName
	 *            the entity name
	 * @param attr
	 *            the attr
	 * @param keys
	 *            the keys
	 * @return the entity result
	 */
	protected EntityResult invokeUpdate(ApplicationContext applicationContext, String entityName, Map<?, ?> attr, Map<?, ?> keys) {
		return this.invoke(applicationContext, entityName, IAttributeDispatcher.UPDATE_SUFFIX, attr, keys);
	}

	/**
	 * Invoke update.
	 *
	 * @param applicationContext
	 *            the application context
	 * @param entityName
	 *            the entity name
	 * @param keys
	 *            the keys
	 * @return the entity result
	 */
	protected EntityResult invokeDelete(ApplicationContext applicationContext, String entityName, Map<?, ?> keys) {
		return this.invoke(applicationContext, entityName, IAttributeDispatcher.DELETE_SUFFIX, keys);
	}

	/**
	 * Invoke.
	 *
	 * @param applicationContext
	 *            the application context
	 * @param entityName
	 *            the entity name
	 * @param methodSufix
	 *            the method sufix
	 * @param parameters
	 *            the parameters
	 * @return the entity result
	 */
	protected EntityResult invoke(ApplicationContext applicationContext, String entityName, String methodSufix, Object... parameters) {
		String methodName = ServiceTools.extractServiceMethodPrefixFromEntityName(entityName) + StringUtils.capitalize(methodSufix);
		String serviceName = ServiceTools.extractServiceFromEntityName(entityName);
		String queryId = ServiceTools.extractQueryIdFromEntityName(entityName);
		Object service = applicationContext.getBean(serviceName);
		service = SpringTools.getTargetObject(service, Object.class);
		if (IAttributeDispatcher.QUERY_SUFFIX.equals(methodSufix) && (queryId != null) && !queryId.isEmpty()) {
			Object[] params = Arrays.copyOf(parameters, parameters.length + 1);
			params[parameters.length] = queryId;
			return (EntityResult) ReflectionTools.invoke(service, methodName, params);
		} else {
			return (EntityResult) ReflectionTools.invoke(service, methodName, parameters);
		}
	}
}

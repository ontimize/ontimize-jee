/**
 * EntitiesDefinitionsUtils.java 18/03/2014
 *
 * Copyright 2014 IMATIA. Departamento de Sistemas
 */
package com.ontimize.jee.server.dao.jpa.common;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import com.ontimize.jee.server.dao.jpa.setup.ColumnMapping;
import com.ontimize.jee.server.dao.jpa.setup.QueryType;

/**
 * The Class EntitiesDefinitionsUtils.
 *
 * @author <a href="">Sergio Padin</a>
 */
public final class MappingInfoUtils {

	private static final String ENTITY_SENTENCE_NAME_SEPARATOR = "/";

	/**
	 * Validate entity definicion.
	 *
	 * @param entityDefinition
	 *            entity definition
	 * @throws RuntimeException
	 *             de runtime exception
	 */
	public static void validateEntityDefinicion(final QueryType entityDefinition, final String entityClazz) throws RuntimeException {
		Class<?> entityClass;
		if ((entityClazz == null) || entityClazz.trim().isEmpty()) {
			throw new RuntimeException("Some entity definition has its clazz not specified");
		}
		try {
			entityClass = Class.forName(entityClazz);
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException("Entity definition has its clazz wrongly specified '" + entityClazz + "' it does not exists!");
		}

		final List<ColumnMapping> columnMapping = entityDefinition.getReturn().getColumnMapping();
		if ((columnMapping == null) || columnMapping.isEmpty()) {
			throw new RuntimeException("Some column mapping for entity '" + entityClazz + "' must be specified");
		}
		for (final ColumnMapping cm : columnMapping) {
			if ((cm.getDbColumn() == null) || cm.getDbColumn().trim().isEmpty()) {
				throw new RuntimeException("DBColumn for column mapping for entity '" + entityClazz + "' must be specified");
			} else {
				if (cm.getBeanAttribute() == null) {
					if (((cm.getBeanAttributeGetterMethod() == null) || cm.getBeanAttributeGetterMethod().trim().isEmpty()) && ((cm.getBeanAttributeSetterMethod() == null) || cm
					        .getBeanAttributeSetterMethod().trim().isEmpty())) {
						throw new RuntimeException(
						        "Bean-attribute and/or bean-atribute-getter-method and/or bean-atribute-setter-method for column mapping for entity '" + entityClazz + "', must be specified");
					}
				} else if (cm.getBeanAttribute().trim().isEmpty()) {
					throw new RuntimeException("Bean-attribute for column mapping for entity '" + entityClazz + "', if attribute is specified, then it must contain something");
				} else {
					final String getter = MappingInfoUtils.buildGetterMethodName(cm.getBeanAttribute().trim(), cm.getBeanAttributeGetterMethod());
					final String setter = MappingInfoUtils.buildSetterMethodName(cm.getBeanAttribute().trim(), cm.getBeanAttributeSetterMethod());
					boolean getterFound = false;
					boolean setterFound = false;
					final Method[] methods = entityClass.getMethods();
					for (final Method m : methods) {
						if (!getterFound && m.getName().equals(getter) && ((m.getParameterTypes() == null) || (m.getParameterTypes().length == 0))) {
							getterFound = true;
						}
						if (!setterFound && m.getName().equals(setter) && (m.getParameterTypes() != null) && (m.getParameterTypes().length == 1)) {
							setterFound = true;
						}
					}
					if (getterFound && !setterFound) {
						throw new RuntimeException("Wrong column mapping (dbcolumn='" + cm
						        .getDbColumn() + "'), no method found for entity '" + entityClazz + "' that matches setter method '" + setter + "' with one argument");
					} else if (!getterFound && setterFound) {
						throw new RuntimeException("Wrong column mapping (dbcolumn='" + cm
						        .getDbColumn() + "'), no method found for entity '" + entityClazz + "' that matches getter method '" + getter + "' with none argument, and this field is insertable or updatable");
					} else if (!getterFound && !setterFound) {
						throw new RuntimeException("Wrong column mapping (dbcolumn='" + cm
						        .getDbColumn() + "'), any method found for entity '" + entityClazz + "' that matches neither setter method '" + setter + "' with one argument, nor getter method '" + getter + "' with none argument");
					}

				}
			}

		}

		// TODO more validations here

	}

	/**
	 * Construye el mapping info.
	 *
	 * @param query
	 *            the query
	 * @param entityClass
	 *            entity class
	 * @param returnType
	 *            return type
	 * @return the mapping info
	 */
	public static MappingInfo buildMappingInfo(final QueryType query, final Class<?> entityClass, final Class<?> returnType) {
		MappingInfo mappingInfo = null;
		try {
			mappingInfo = new MappingInfo();
			if (query.getReturn() != null) {
				if ((query.getReturn().getReturnType() != null) && (returnType != null) && returnType.isAssignableFrom(Class.forName(query.getReturn().getReturnType()))) {
					mappingInfo.setReturnTypeClassName(query.getReturn().getReturnType());
					mappingInfo.setReturnTypeClassNameIsPrimitive(MappingInfoUtils.isPrimitive(query.getReturn().getReturnType()));
					mappingInfo.setReturnTypePrimitiveColumnMapping(query.getReturn().getReturnDbColumn());
					mappingInfo.setReturnTypePrimitiveColumnMappingType(query.getReturn().getReturnDbType());
				} else if (returnType != null) {
					throw new IllegalArgumentException("return type '" + returnType.getCanonicalName() + "' is not assignable from '" + query.getReturn().getReturnType() + "'");
				}
				if (query.getReturn().getColumnMapping() != null) {
					for (final ColumnMapping cm : query.getReturn().getColumnMapping()) {
						mappingInfo.addColumnMapping(cm);
					}
				}
			} else {
				if (returnType.isAssignableFrom(entityClass)) {
					mappingInfo.setReturnTypeClassName(entityClass.getCanonicalName());
					mappingInfo.setReturnTypeClassNameIsPrimitive(false);
				} else {
					throw new IllegalArgumentException("return type '" + returnType.getCanonicalName() + "' is not assignable from '" + entityClass.getCanonicalName() + "'");
				}
			}

			return mappingInfo;
		} catch (final ClassNotFoundException e) {
			throw new IllegalArgumentException("return type '" + query.getReturn().getReturnType() + "' is a not known class '", e);
		}
	}

	/**
	 * Chequea si primitive.
	 *
	 * @param returnType2
	 *            return type2
	 * @return the boolean
	 */
	public static Boolean isPrimitive(final String returnType2) {
		try {
			if (returnType2.equalsIgnoreCase("byte[]")) {
				return true;
			}
			final Class<?> forName = Class.forName(returnType2);
			if (Integer.class.isAssignableFrom(forName) || Long.class.isAssignableFrom(forName) || Double.class.isAssignableFrom(forName) || Float.class
			        .isAssignableFrom(forName) || Short.class.isAssignableFrom(forName) || Character.class.isAssignableFrom(forName) || String.class
			                .isAssignableFrom(forName) || Date.class.isAssignableFrom(forName) || Byte.class.isAssignableFrom(forName) || Boolean.class.isAssignableFrom(forName)) {
				return true;
			}
			return false;
		} catch (final ClassNotFoundException e) {
			throw new IllegalArgumentException("return Type not known", e);
		}

	}

	/**
	 * Normalize sentence name.
	 *
	 * @param entityClass
	 *            entity class
	 * @param sentenceName
	 *            sentence name
	 * @return the string
	 */
	public static String normalizeSentenceName(final Class<?> entityClass, final String sentenceName) {
		final String className = entityClass.getCanonicalName();
		if (sentenceName.startsWith(className)) {
			return sentenceName;
		}
		return className + MappingInfoUtils.ENTITY_SENTENCE_NAME_SEPARATOR + sentenceName;
	}

	/**
	 * Denormalize sentence name.
	 *
	 * @param entityClass
	 *            entity class
	 * @param sentenceName
	 *            sentence name
	 * @return the string
	 */
	public static String denormalizeSentenceName(final Class<?> entityClass, final String sentenceName) {
		final String entityClassName = entityClass.getCanonicalName() + MappingInfoUtils.ENTITY_SENTENCE_NAME_SEPARATOR;
		if (sentenceName.startsWith(entityClassName)) {
			return sentenceName.replace(entityClassName, "");
		}
		return sentenceName;
	}

	/**
	 * Construye el setter method name.
	 *
	 * @param entityAttribute
	 *            entity attribute
	 * @param entityAttributeSetterMethod
	 *            entity attribute setter method
	 * @return the string
	 */
	public static String buildSetterMethodName(final String entityAttribute, final String entityAttributeSetterMethod) {
		if ((entityAttributeSetterMethod != null) && !entityAttributeSetterMethod.trim().isEmpty()) {
			return entityAttributeSetterMethod;
		}
		if (entityAttribute.length() > 0) {
			return "set" + Character.toUpperCase(entityAttribute.charAt(0)) + entityAttribute.substring(1);
		}
		return "set";
	}

	/**
	 * Construye el getter method name.
	 *
	 * @param entityAttribute
	 *            entity attribute
	 * @param entityAttributeGetterMethod
	 *            entity attribute getter method
	 * @return the string
	 */
	public static String buildGetterMethodName(final String entityAttribute, final String entityAttributeGetterMethod) {
		if ((entityAttributeGetterMethod != null) && !entityAttributeGetterMethod.trim().isEmpty()) {
			return entityAttributeGetterMethod;
		}
		if (entityAttribute.length() > 0) {
			return "get" + Character.toUpperCase(entityAttribute.charAt(0)) + entityAttribute.substring(1);
		}
		return "get";
	}

	/**
	 * Instancia un nuevo entities definitions utils.
	 */
	private MappingInfoUtils() {
		// do nothing
	}

}

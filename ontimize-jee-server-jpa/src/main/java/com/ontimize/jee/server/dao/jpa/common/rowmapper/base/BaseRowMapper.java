/**
 * BaseRowMapper.java 17/03/2014
 *
 * Copyright 2014 IMATIA. Departamento de Sistemas
 */
package com.ontimize.jee.server.dao.jpa.common.rowmapper.base;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.common.tools.ConcatTools;
import com.ontimize.jee.server.dao.jpa.common.MappingInfo;
import com.ontimize.jee.server.dao.jpa.common.MappingInfoUtils;
import com.ontimize.jee.server.dao.jpa.common.rowmapper.IColumnTypeToResultSetGetterMethodConverter;
import com.ontimize.jee.server.dao.jpa.common.rowmapper.IDbObjectToJavaObjectConverter;
import com.ontimize.jee.server.dao.jpa.common.rowmapper.IRowMapper;
import com.ontimize.jee.server.dao.jpa.common.rowmapper.exceptions.RowMapperException;
import com.ontimize.jee.server.dao.jpa.setup.ColumnMapping;

/**
 * The Class BaseRowMapper.
 *
 * @author <a href="">Sergio Padin</a>
 * @param <T> tipo de la entidad sobre la que se esta realizando la query
 * @param <S> tipo del resultado de la query
 */
public class BaseRowMapper<T, S> implements IRowMapper<S> {

    /** The Constant BREAK_LINE. */
    private static final char BREAK_LINE = '\n';

    /** The Constant COULD_NOT_CREATE_RETURN_TYPE. */
    private static final String COULD_NOT_CREATE_RETURN_TYPE = "could not create return type";

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(BaseRowMapper.class);

    /** The entity class. */
    private Class<T> entityClass;

    /** The return type. */
    private final Class<S> returnType;

    /** The mapping info. */
    private final MappingInfo mappingInfo;

    /** The column type to result set getter method converter. */
    private IColumnTypeToResultSetGetterMethodConverter columnTypeToResultSetGetterMethodConverter;

    /** The db object to java object converter. */
    private IDbObjectToJavaObjectConverter dbObjectToJavaObjectConverter;

    /** The allow partial bean mapping. */
    private boolean allowPartialBeanMapping = true;

    /**
     * Instancia un nuevo base row mapper.
     * @param mappingInfo the mapping info
     * @param entityClass entity class
     * @param returnType return type
     */
    public BaseRowMapper(final MappingInfo mappingInfo, final Class<T> entityClass, final Class<S> returnType) {
        this.entityClass = entityClass;
        this.returnType = returnType;
        this.mappingInfo = mappingInfo;
    }

    /**
     * Instancia un nuevo base row mapper.
     * @param mappingInfo the mapping info
     * @param entityClass entity class
     * @param returnType return type
     * @param columnTypeToResultSetGetterMethodConverter column type to result set getter method
     *        converter
     * @param dbObjectToJavaObjectConverter db object to java object converter
     */
    public BaseRowMapper(final MappingInfo mappingInfo, final Class<T> entityClass, final Class<S> returnType,
            final IColumnTypeToResultSetGetterMethodConverter columnTypeToResultSetGetterMethodConverter,
            final IDbObjectToJavaObjectConverter dbObjectToJavaObjectConverter) {
        this.entityClass = entityClass;
        this.returnType = returnType;
        this.mappingInfo = mappingInfo;
        this.columnTypeToResultSetGetterMethodConverter = columnTypeToResultSetGetterMethodConverter;
        this.dbObjectToJavaObjectConverter = dbObjectToJavaObjectConverter;
    }

    /**
     * Map row.
     * @param rs the rs
     * @param rowNum the row num
     * @return the s
     * @throws SQLException the SQL exception
     * @see com.ontimize.jee.server.dao.jpa.common.rowmapper.IRowMapper#mapRow(java.sql.ResultSet, int)
     */
    @Override
    public S mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        if (this.entityClass != null) {
            if (this.mappingInfo != null) {
                try {
                    if ((this.mappingInfo.getReturnTypeClassNameIsPrimitive() != null)
                            && !this.mappingInfo.getReturnTypeClassNameIsPrimitive()) {
                        final Object targetEntityObject = this
                            .buildInstanceIfPossible(this.mappingInfo.getReturnTypeClassName());
                        this.buildEntityFromRow(rs, rowNum, this.mappingInfo, this.returnType, targetEntityObject);
                        return (S) targetEntityObject;
                    }
                    final String columnName = this.mappingInfo.getReturnTypePrimitiveColumnMapping();
                    final String returnTypePrimitiveColumnMappingType = this.mappingInfo
                        .getReturnTypePrimitiveColumnMappingType();
                    final String getterMethod = this
                        .buildResultSetGetterMethodForType(returnTypePrimitiveColumnMappingType);
                    return this.getObjectConverted(rs, rowNum, getterMethod, columnName, this.returnType);
                } catch (final RuntimeException e) {
                    throw new SQLException(new RowMapperException(e));
                } catch (final NoSuchMethodException e) {
                    throw new SQLException(new RowMapperException(e));
                } catch (final IllegalAccessException e) {
                    throw new SQLException(new RowMapperException(e));
                } catch (final InvocationTargetException e) {
                    throw new SQLException(new RowMapperException(e));
                } catch (final ClassNotFoundException e) {
                    throw new SQLException(new RowMapperException(e));
                } catch (final RowMapperException e) {
                    throw new SQLException(e);
                }
            } else {
                throw new SQLException(new RowMapperException("there is no mappingInfo "));
            }
        } else {
            throw new SQLException(new RowMapperException("there is no entity class specified"));
        }

    }

    /**
     * Map object array.
     * @param data the data
     * @param columnNames the column names
     * @return the s
     * @throws SQLException the SQL exception
     * @see com.ontimize.jee.server.dao.jpa.common.rowmapper.IRowMapper#mapObjectArray(java.lang.Object[],
     *      java.util.List)
     */
    @Override
    public S mapObjectArray(final Object[] data, final List<String> columnNames) throws SQLException {
        if (this.entityClass != null) {
            if (this.mappingInfo != null) {
                final List<ColumnMapping> columnMappings = this.mappingInfo.getColumnMappings();
                if (!this.allowPartialBeanMapping
                        && ((this.mappingInfo.getReturnTypePrimitiveColumnMapping() == null)
                                || this.mappingInfo.getReturnTypePrimitiveColumnMapping()
                                    .isEmpty())
                        && ((data.length != columnMappings.size())
                                || ((columnNames.size() > 0) && (columnNames.size() != data.length)))) {
                    throw new SQLException(new RowMapperException(
                            "the mapping info is incompatible with query result different number of columns"));
                }

                try {
                    if ((this.mappingInfo.getReturnTypeClassNameIsPrimitive() != null)
                            && !this.mappingInfo.getReturnTypeClassNameIsPrimitive()) {
                        final Object targetEntityObject = this
                            .buildInstanceIfPossible(this.mappingInfo.getReturnTypeClassName());

                        this.buildEntityFromObjectArray(data, columnNames, this.mappingInfo, this.returnType,
                                targetEntityObject);

                        return (S) targetEntityObject;
                    }
                    final String columnName = this.mappingInfo.getReturnTypePrimitiveColumnMapping();
                    final String returnTypePrimitiveColumnMappingType = this.mappingInfo
                        .getReturnTypePrimitiveColumnMappingType();
                    final String getterMethod = this
                        .buildResultSetGetterMethodForType(returnTypePrimitiveColumnMappingType);
                    return this.getObjectConverted(data[0], getterMethod, columnName, this.returnType);
                } catch (final RuntimeException e) {
                    throw new SQLException(new RowMapperException(e));
                } catch (final NoSuchMethodException e) {
                    throw new SQLException(new RowMapperException(e));
                } catch (final IllegalAccessException e) {
                    throw new SQLException(new RowMapperException(e));
                } catch (final InvocationTargetException e) {
                    throw new SQLException(new RowMapperException(e));
                } catch (final ClassNotFoundException e) {
                    throw new SQLException(new RowMapperException(e));
                } catch (final RowMapperException e) {
                    throw new SQLException(e);
                }
            } else {
                throw new SQLException(new RowMapperException("there is no mappingInfo "));
            }
        } else {
            throw new SQLException(new RowMapperException("there is no entity class specified"));
        }

    }

    /**
     * Construye el instance if possible.
     * @param returnTypeClassName return type class name
     * @return the object
     * @throws IllegalArgumentException de illegal argument exception
     */
    private Object buildInstanceIfPossible(final String returnTypeClassName) throws IllegalArgumentException {
        try {
            final String returnTypeName = this.returnType.getCanonicalName();
            if (returnTypeClassName.equalsIgnoreCase(returnTypeName)) {
                return this.returnType.newInstance();
            }
            throw new IllegalArgumentException(ConcatTools.concat("specified returnType '", returnTypeName,
                    "' doesn't match sentence return type '", returnTypeClassName, "'"));
        } catch (final InstantiationException e) {
            this.handleException(e, null, null, null, (String) null);
            throw new IllegalArgumentException(BaseRowMapper.COULD_NOT_CREATE_RETURN_TYPE, e);
        } catch (final IllegalAccessException e) {
            this.handleException(e, null, null, null, (String) null);
            throw new IllegalArgumentException(BaseRowMapper.COULD_NOT_CREATE_RETURN_TYPE, e);
        }

    }

    /**
     * Construye el entity from row.
     * @param rs rs
     * @param rowNum row num
     * @param mappingInfo mapping info
     * @param targetEntityClass target entity class
     * @param targetEntityObject target entity object
     * @throws NoSuchMethodException de no such method exception
     * @throws IllegalAccessException de illegal access exception
     * @throws InvocationTargetException de invocation target exception
     * @throws ClassNotFoundException de class not found exception
     * @throws IllegalArgumentException de illegal argument exception
     * @throws RowMapperException de row mapper exception
     */
    private void buildEntityFromRow(final ResultSet rs, final int rowNum, final MappingInfo mappingInfo,
            final Class<S> targetEntityClass, final Object targetEntityObject)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException,
            IllegalArgumentException, RowMapperException {
        if (mappingInfo.getColumnMappings() != null) {
            for (final ColumnMapping cm : mappingInfo.getColumnMappings()) {
                this.apply(rs, rowNum, this.buildResultSetGetterMethodForType(cm.getDbColumnType()), cm.getDbColumn(),
                        targetEntityClass, targetEntityObject,
                        MappingInfoUtils.buildSetterMethodName(cm.getBeanAttribute(),
                                cm.getBeanAttributeSetterMethod()),
                        cm.getBeanAttributeType());

            }
        }

    }

    /**
     * Builds the entity from object array.
     * @param data the data
     * @param columnNames the column names
     * @param mappingInfo the mapping info
     * @param targetEntityClass the target entity class
     * @param targetEntityObject the target entity object
     * @throws NoSuchMethodException the no such method exception
     * @throws IllegalAccessException the illegal access exception
     * @throws InvocationTargetException the invocation target exception
     * @throws ClassNotFoundException the class not found exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws RowMapperException the row mapper exception
     */
    private void buildEntityFromObjectArray(final Object[] data, final List<String> columnNames,
            final MappingInfo mappingInfo, final Class<S> targetEntityClass,
            final Object targetEntityObject)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException,
            IllegalArgumentException, RowMapperException {
        if (mappingInfo.getColumnMappings() != null) {
            int i = 0;
            for (final ColumnMapping cm : mappingInfo.getColumnMappings()) {
                Object dataObject = null;
                boolean doIt = false;
                // if there are columnNames defined then find the object of that column
                if (columnNames.size() > 0) {
                    int j = 0;
                    final String mappingDBColumn = cm.getDbColumn();
                    for (final Object o : data) {
                        final String colName = columnNames.get(j);
                        if (colName.equalsIgnoreCase(mappingDBColumn)) {
                            dataObject = o;
                            doIt = true;
                            break;
                        }
                        j++;
                    }
                } else {
                    // else try going in order
                    dataObject = data[i];
                    doIt = true;
                }
                if (doIt) {
                    this.apply(dataObject, this.buildResultSetGetterMethodForType(cm.getDbColumnType()),
                            cm.getDbColumn(), targetEntityClass, targetEntityObject,
                            MappingInfoUtils.buildSetterMethodName(cm.getBeanAttribute(),
                                    cm.getBeanAttributeSetterMethod()),
                            cm.getBeanAttributeType());
                }
                i++;
            }
        }

    }

    /**
     * Construye el result set getter method for type.
     * @param dbColumnType db column type
     * @return the string
     */
    private String buildResultSetGetterMethodForType(final String dbColumnType) {
        if (this.columnTypeToResultSetGetterMethodConverter != null) {
            return this.columnTypeToResultSetGetterMethodConverter
                .getResultSetGetterMethodFromDBColumnType(dbColumnType);
        }
        BaseRowMapper.logger.warn("There is no columnType to result set getter converter defined");
        return null;
    }

    /**
     * Apply.
     * @param rs rs
     * @param rowNum row num
     * @param rsMethod rs method
     * @param columnName column name
     * @param targetClass target class
     * @param targetObject target object
     * @param targetMethod target method
     * @param typeClassName type class name
     * @throws NoSuchMethodException de no such method exception
     * @throws IllegalAccessException de illegal access exception
     * @throws InvocationTargetException de invocation target exception
     * @throws ClassNotFoundException de class not found exception
     * @throws IllegalArgumentException de illegal argument exception
     * @throws RowMapperException de row mapper exception
     */
    private void apply(final ResultSet rs, final int rowNum, final String rsMethod, final String columnName,
            final Class<?> targetClass, final Object targetObject,
            final String targetMethod, final String typeClassName)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException,
            IllegalArgumentException, RowMapperException {
        String getterMethod = null;
        Object dbObject = null;
        try {
            getterMethod = rsMethod;
            if (getterMethod == null) {
                getterMethod = "getObject";
            }
            final Method method = ResultSet.class.getMethod(getterMethod, String.class);
            dbObject = method.invoke(rs, columnName);

            final Class<?> targetAttributeType = Class.forName(typeClassName);
            final Method method2 = targetClass.getMethod(targetMethod, targetAttributeType);
            Object entityAttributeObject = dbObject;
            if (this.dbObjectToJavaObjectConverter != null) {
                entityAttributeObject = this.dbObjectToJavaObjectConverter.convert(dbObject, targetAttributeType);
            }
            method2.invoke(targetObject, entityAttributeObject);
        } catch (final NoSuchMethodException e) {
            this.handleException(e, columnName, getterMethod, dbObject, typeClassName);
            throw e;
        } catch (final SecurityException e) {
            this.handleException(e, columnName, getterMethod, dbObject, typeClassName);
            throw e;
        } catch (final IllegalAccessException e) {
            this.handleException(e, columnName, getterMethod, dbObject, typeClassName);
            throw e;
        } catch (final IllegalArgumentException e) {
            this.handleException(e, columnName, getterMethod, dbObject, typeClassName);
            throw e;
        } catch (final InvocationTargetException e) {
            this.handleException(e, columnName, getterMethod, dbObject, typeClassName);
            throw e;
        } catch (final ClassNotFoundException e) {
            this.handleException(e, columnName, getterMethod, dbObject, typeClassName);
            throw e;
        }
    }

    /**
     * Apply.
     * @param dbObject the db object
     * @param rsMethod the rs method
     * @param columnName the column name
     * @param targetClass the target class
     * @param targetObject the target object
     * @param targetMethod the target method
     * @param typeClassName the type class name
     * @throws NoSuchMethodException the no such method exception
     * @throws IllegalAccessException the illegal access exception
     * @throws InvocationTargetException the invocation target exception
     * @throws ClassNotFoundException the class not found exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws RowMapperException the row mapper exception
     */
    private void apply(final Object dbObject, final String rsMethod, final String columnName,
            final Class<?> targetClass, final Object targetObject, final String targetMethod,
            final String typeClassName)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException,
            IllegalArgumentException, RowMapperException {
        try {
            final Class<?> targetAttributeType = Class.forName(typeClassName);
            final Method method2 = targetClass.getMethod(targetMethod, targetAttributeType);
            Object entityAttributeObject = dbObject;
            if (this.dbObjectToJavaObjectConverter != null) {
                entityAttributeObject = this.dbObjectToJavaObjectConverter.convert(dbObject, targetAttributeType);
            }
            method2.invoke(targetObject, entityAttributeObject);
        } catch (final NoSuchMethodException e) {
            this.handleException(e, columnName, "", dbObject, typeClassName);
            throw e;
        } catch (final SecurityException e) {
            this.handleException(e, columnName, "", dbObject, typeClassName);
            throw e;
        } catch (final IllegalAccessException e) {
            this.handleException(e, columnName, "", dbObject, typeClassName);
            throw e;
        } catch (final IllegalArgumentException e) {
            this.handleException(e, columnName, "", dbObject, typeClassName);
            throw e;
        } catch (final InvocationTargetException e) {
            this.handleException(e, columnName, "", dbObject, typeClassName);
            throw e;
        } catch (final ClassNotFoundException e) {
            this.handleException(e, columnName, "", dbObject, typeClassName);
            throw e;
        }
    }

    /**
     * Obtiene object converted.
     * @param <R> tipo generico
     * @param rs rs
     * @param rowNum row num
     * @param rsMethod rs method
     * @param columnName column name
     * @param returnTypeClass return type class
     * @return object converted
     * @throws NoSuchMethodException de no such method exception
     * @throws IllegalAccessException de illegal access exception
     * @throws IllegalArgumentException de illegal argument exception
     * @throws InvocationTargetException de invocation target exception
     * @throws RowMapperException de row mapper exception
     */
    private <R> R getObjectConverted(final ResultSet rs, final int rowNum, final String rsMethod,
            final String columnName, final Class<R> returnTypeClass)
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            RowMapperException {
        String getterMethod = null;
        Object dbObject = null;
        try {
            getterMethod = rsMethod;
            if (getterMethod == null) {
                getterMethod = "getObject";
            }
            final Method method = ResultSet.class.getMethod(getterMethod, String.class);
            dbObject = method.invoke(rs, columnName);

            Object returnObject = dbObject;
            if (this.dbObjectToJavaObjectConverter != null) {
                returnObject = this.dbObjectToJavaObjectConverter.convert(dbObject, returnTypeClass);
            }
            return (R) returnObject;
        } catch (final NoSuchMethodException e) {
            this.handleException(e, columnName, getterMethod, dbObject, this.toCanonicalName(returnTypeClass));
            throw e;
        } catch (final SecurityException e) {
            this.handleException(e, columnName, getterMethod, dbObject, this.toCanonicalName(returnTypeClass));
            throw e;
        } catch (final IllegalAccessException e) {
            this.handleException(e, columnName, getterMethod, dbObject, this.toCanonicalName(returnTypeClass));
            throw e;
        } catch (final IllegalArgumentException e) {
            this.handleException(e, columnName, getterMethod, dbObject, this.toCanonicalName(returnTypeClass));
            throw e;
        } catch (final InvocationTargetException e) {
            this.handleException(e, columnName, getterMethod, dbObject, this.toCanonicalName(returnTypeClass));
            throw e;
        } catch (final RuntimeException e) {
            this.handleException(e, columnName, getterMethod, dbObject, this.toCanonicalName(returnTypeClass));
            throw e;
        }
    }

    /**
     * Gets the object converted.
     * @param <R> the generic type
     * @param dbObject the db object
     * @param rsMethod the rs method
     * @param columnName the column name
     * @param returnTypeClass the return type class
     * @return the object converted
     * @throws IllegalArgumentException the illegal argument exception
     * @throws RowMapperException the row mapper exception
     */
    private <R> R getObjectConverted(final Object dbObject, final String rsMethod, final String columnName,
            final Class<R> returnTypeClass)
            throws IllegalArgumentException, RowMapperException {
        try {
            Object returnObject = dbObject;
            if (this.dbObjectToJavaObjectConverter != null) {
                returnObject = this.dbObjectToJavaObjectConverter.convert(dbObject, returnTypeClass);
            }
            return (R) returnObject;
        } catch (final SecurityException e) {
            this.handleException(e, columnName, "", dbObject, this.toCanonicalName(returnTypeClass));
            throw e;
        } catch (final IllegalArgumentException e) {
            this.handleException(e, columnName, "", dbObject, this.toCanonicalName(returnTypeClass));
            throw e;
        } catch (final RuntimeException e) {
            this.handleException(e, columnName, "", dbObject, this.toCanonicalName(returnTypeClass));
            throw e;
        }
    }

    /**
     * Establece entity class.
     * @param entityClass nuevo entity class
     */
    public void setEntityClass(final Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Establece column type to result set getter method converter.
     * @param columnTypeToResultSetGetterMethodConverter nuevo column type to result set getter method
     *        converter
     */
    public void setColumnTypeToResultSetGetterMethodConverter(
            final IColumnTypeToResultSetGetterMethodConverter columnTypeToResultSetGetterMethodConverter) {
        this.columnTypeToResultSetGetterMethodConverter = columnTypeToResultSetGetterMethodConverter;
    }

    /**
     * Establece db object to java object converter.
     * @param dbObjectToJavaObjectConverter nuevo db object to java object converter
     */
    public void setDbObjectToJavaObjectConverter(final IDbObjectToJavaObjectConverter dbObjectToJavaObjectConverter) {
        this.dbObjectToJavaObjectConverter = dbObjectToJavaObjectConverter;
    }

    /**
     * To canonical name.
     * @param clazz clazz
     * @return the string
     */
    private String toCanonicalName(final Class<?> clazz) {
        return clazz != null ? clazz.getCanonicalName() : "NULL";
    }

    /**
     * Handle exception.
     * @param exception exception
     * @param columName colum name
     * @param rsGetterMethod rs getter method
     * @param columnObject column object
     * @param classToReturn class to return
     */
    protected void handleException(final Exception exception, final String columName, final String rsGetterMethod,
            final Object columnObject, final String classToReturn) {
        BaseRowMapper.logger.error(ConcatTools.concat("error converting database row to entity:",
                exception.getMessage(), BaseRowMapper.BREAK_LINE, "Related data: columnName='",
                columName, "', rsGetterMethod='", rsGetterMethod, "', columnObjectClass='",
                columnObject != null ? columnObject.getClass().getCanonicalName() : "NULL",
                "', classToReturn='", classToReturn, "'"));
    }

    /**
     * Sets the allow partial bean mapping.
     * @param allowPartialBeanMapping the new allow partial bean mapping
     */
    public void setAllowPartialBeanMapping(final boolean allowPartialBeanMapping) {
        this.allowPartialBeanMapping = allowPartialBeanMapping;
    }

    /**
     * Checks if is allow partial bean mapping.
     * @return true, if is allow partial bean mapping
     */
    public boolean isAllowPartialBeanMapping() {
        return this.allowPartialBeanMapping;
    }

}

/**
 * DB2RowMapperProvider.java 17/03/2014
 *
 * Copyright 2014 IMATIA. Departamento de Sistemas
 */
package com.ontimize.jee.server.dao.jpa.common.rowmapper.base;

import com.ontimize.jee.server.dao.jpa.common.MappingInfo;
import com.ontimize.jee.server.dao.jpa.common.rowmapper.IColumnTypeToResultSetGetterMethodConverter;
import com.ontimize.jee.server.dao.jpa.common.rowmapper.IDbObjectToJavaObjectConverter;
import com.ontimize.jee.server.dao.jpa.common.rowmapper.IRowMapper;
import com.ontimize.jee.server.dao.jpa.common.rowmapper.IRowMapperProvider;

/**
 * The Class DB2RowMapperProvider.
 *
 * @author <a href="">Sergio Padin</a>
 */
public class BaseRowMapperProvider implements IRowMapperProvider {

    private IColumnTypeToResultSetGetterMethodConverter columnTypeToResultSetGetterMethodConverter;

    private IDbObjectToJavaObjectConverter dbObjectToJavaObjectConverter;

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> IRowMapper<T> getRowMapper(final MappingInfo mappingInfo, final Class<T> entityClass) {
        final BaseRowMapper<T, T> rowMapper = new BaseRowMapper<>(mappingInfo, entityClass, entityClass);
        rowMapper.setColumnTypeToResultSetGetterMethodConverter(this.columnTypeToResultSetGetterMethodConverter);
        rowMapper.setDbObjectToJavaObjectConverter(this.dbObjectToJavaObjectConverter);
        return rowMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S, T> IRowMapper<T> getRowMapper(final MappingInfo mappingInfo, final Class<S> entityClass,
            final Class<T> returnTypeClass) {
        final BaseRowMapper<S, T> rowMapper = new BaseRowMapper<>(mappingInfo, entityClass, returnTypeClass);
        rowMapper.setColumnTypeToResultSetGetterMethodConverter(this.columnTypeToResultSetGetterMethodConverter);
        rowMapper.setDbObjectToJavaObjectConverter(this.dbObjectToJavaObjectConverter);
        return rowMapper;
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

}

/**
 * IRowMapperProvider.java 17/03/2014
 *
 * Copyright 2014 IMATIA. Departamento de Sistemas
 */
package com.ontimize.jee.server.dao.jpa.common.rowmapper;

import com.ontimize.jee.server.dao.jpa.common.MappingInfo;

/**
 * The Interface IRowMapperProvider.
 *
 * @author <a href="">Sergio Padin</a>
 */
public interface IRowMapperProvider {

    /**
     * Obtiene row mapper.
     * @param <T> tipo generico
     * @param mappingInfo the mapping info
     * @param entityClass entity class
     * @return row mapper
     */
    <T> IRowMapper<T> getRowMapper(MappingInfo mappingInfo, Class<T> entityClass);

    /**
     * Obtiene row mapper.
     * @param <S> tipo generico
     * @param <T> tipo generico
     * @param mappingInfo the mapping info
     * @param entityClass entity class
     * @param returnTypeClass return type class
     * @return row mapper
     */
    <S, T> IRowMapper<T> getRowMapper(MappingInfo mappingInfo, Class<S> entityClass, Class<T> returnTypeClass);

}

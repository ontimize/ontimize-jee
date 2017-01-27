/**
 *
 */
package com.ontimize.jee.server.dao.jpa.common.rowmapper;

import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

/**
 * The Interface IRowMapper.
 *
 * @param <S>
 *            the generic type representing bean mapping
 */
public interface IRowMapper<S> extends RowMapper<S> {

    /**
     * Map object array.
     *
     * @param data
     *            the data
     * @param columnNames
     *            the column names
     * @return the s
     * @throws SQLException
     *             the SQL exception
     */
    S mapObjectArray(Object[] data, List<String> columnNames) throws SQLException;

}
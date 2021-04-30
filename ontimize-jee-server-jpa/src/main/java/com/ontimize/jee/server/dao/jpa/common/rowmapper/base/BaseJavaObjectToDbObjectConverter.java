/**
 * DB2DbObjectToJavaObjectConverter.java 17/03/2014
 *
 * Copyright 2014 IMATIA. Departamento de Sistemas
 */
package com.ontimize.jee.server.dao.jpa.common.rowmapper.base;

import java.sql.Types;

import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.support.SqlLobValue;

import com.ontimize.jee.server.dao.jpa.common.rowmapper.IJavaObjectToDbObjectConverter;

/**
 * The Class DB2DbObjectToJavaObjectConverter.
 *
 * @author <a href="">Sergio Padin</a>
 */
public class BaseJavaObjectToDbObjectConverter implements IJavaObjectToDbObjectConverter {

    private static String CLOB_TYPE = "CLOB";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object convert(final Object input, final String toDbType) {

        if ((input instanceof String) && toDbType.equalsIgnoreCase(BaseJavaObjectToDbObjectConverter.CLOB_TYPE)) {
            return new SqlParameterValue(Types.CLOB, new SqlLobValue((String) input));
        }
        return input;
    }

}

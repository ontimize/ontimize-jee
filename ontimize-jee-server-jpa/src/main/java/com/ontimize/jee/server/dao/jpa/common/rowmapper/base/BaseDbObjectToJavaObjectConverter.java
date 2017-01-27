/**
 * DB2DbObjectToJavaObjectConverter.java 17/03/2014
 *
 * Copyright 2014 IMATIA. Departamento de Sistemas
 */
package com.ontimize.jee.server.dao.jpa.common.rowmapper.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.server.dao.jpa.common.rowmapper.IDbObjectToJavaObjectConverter;
import com.ontimize.jee.server.dao.jpa.common.rowmapper.exceptions.RowMapperException;

/**
 * The Class DB2DbObjectToJavaObjectConverter.
 *
 * @author <a href="">Sergio Padin</a>
 */
public class BaseDbObjectToJavaObjectConverter implements IDbObjectToJavaObjectConverter {

	private static final Logger	logger	= LoggerFactory.getLogger(BaseDbObjectToJavaObjectConverter.class);

	/**
	 * {@inheritDoc}
	 *
	 * @throws RowMapperException
	 */
	@Override
	public Object convert(final Object input, final Class<?> toType) throws RowMapperException {

		if ((input instanceof Clob) && toType.equals(String.class)) {
			try {
				final InputStream in = ((Clob) input).getAsciiStream();
				final StringWriter w = new StringWriter();
				IOUtils.copy(in, w);
				return w.toString();
			} catch (final SQLException e) {
				throw new RowMapperException("Clob couln't be converted to String", e);
			} catch (final IOException e) {
				throw new RowMapperException("Clob couln't be converted to String", e);
			}
		} else if ((input instanceof Blob) && toType.equals(byte[].class)) {
			try {
				return ((Blob) input).getBytes(0, (int) ((Blob) input).length());
			} catch (SQLException e) {
				BaseDbObjectToJavaObjectConverter.logger.error(null, e);
			}
		} else if (input instanceof Number) {
			// TODO check if new Object can receive original Number
			if (toType.equals(Double.class)) {
				return ((Number) input).doubleValue();
			} else if ((toType.equals(Long.class))) {
				return ((Number) input).longValue();
			} else if ((toType.equals(Integer.class))) {
				return ((Number) input).intValue();
			} else if ((toType.equals(BigInteger.class))) {
				return BigInteger.valueOf(((Number) input).longValue());
			} else if ((toType.equals(BigDecimal.class))) {
				return BigDecimal.valueOf(((Number) input).doubleValue());
			} else if ((toType.equals(Short.class))) {
				return ((Number) input).shortValue();
			} else if ((toType.equals(Float.class))) {
				return ((Number) input).floatValue();
			}
		}

		return input;
	}

}

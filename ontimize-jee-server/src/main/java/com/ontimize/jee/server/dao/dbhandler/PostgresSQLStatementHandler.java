package com.ontimize.jee.server.dao.dbhandler;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.db.EntityResult;

public class PostgresSQLStatementHandler extends com.ontimize.db.handler.PostgresSQLStatementHandler {

    private static final Logger logger = LoggerFactory.getLogger(PostgresSQLStatementHandler.class);

    public PostgresSQLStatementHandler() {
        super();
    }

    @Override
    protected Object getResultSetValue(ResultSet resultSet, String columnName, int columnType) throws Exception {
        if (columnType == Types.ARRAY) {
            Array value = resultSet.getArray(columnName);
            return value == null ? null : value.getArray();
        }
        return super.getResultSetValue(resultSet, columnName, columnType);
    }

    /**
     * Reads an array of bytes from the stream <code>in</code>.
     * @param in
     * @return a BytesBlock object containing the byte array or null if the stream is empty
     */
    @Override
    protected Object readBinaryStream(InputStream in) {
        ByteArrayOutputStream bOut = null;

        if (in == null) {
            return null;
        } else {
            BufferedInputStream bInputStream = null;
            try {
                bOut = new ByteArrayOutputStream(256);
                bInputStream = new BufferedInputStream(in);
                byte[] byteAux = new byte[1];
                while (bInputStream.read(byteAux) > 0) {
                    bOut.write(byteAux);
                }
                bInputStream.close();
                // Now creates a byte array.
                byte[] arrayBytes = bOut.toByteArray();
                if (arrayBytes.length > 0) {
                    return arrayBytes;
                } else {
                    return null;
                }
            } catch (Exception error) {
                PostgresSQLStatementHandler.logger.error(null, error);
                return null;
            } finally {
                try {
                    if (bOut != null) {
                        bOut.close();
                    }
                    if (bInputStream != null) {
                        bInputStream.close();
                    }
                } catch (Exception e) {
                    PostgresSQLStatementHandler.logger.trace(null, e);
                }
            }
        }
    }

    @Override
    public boolean checkColumnName(String columnName) {
        boolean superCheck = super.checkColumnName(columnName);

        // Fix error, for instance: columnName = "(CASE WHEN USR_DOWN_DATE is null THEN 'S' ELSE 'N' END)"
        if (superCheck && columnName.trim().startsWith("(") && columnName.trim().endsWith(")")) {
            return false;
        }
        return superCheck;
    }

}

package com.ontimize.jee.server.dao.dbhandler;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLServerSQLStatementHandler extends com.ontimize.jee.common.db.handler.SQLServerSQLStatementHandler {

    private static final Logger logger = LoggerFactory.getLogger(SQLServerSQLStatementHandler.class);

    public SQLServerSQLStatementHandler() {
        super();
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
                SQLServerSQLStatementHandler.logger.error(null, error);
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
                    SQLServerSQLStatementHandler.logger.trace(null, e);
                }
            }
        }
    }

}

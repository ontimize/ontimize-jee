package com.ontimize.jee.server.dao.dbhandler;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Oracle12cSQLStatementHandler extends com.ontimize.db.handler.Oracle12cSQLStatementHandler {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(Oracle12cSQLStatementHandler.class);


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
            } catch (Exception e) {
                Oracle12cSQLStatementHandler.logger.error("ERROR", e);
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
                    Oracle12cSQLStatementHandler.logger.trace(null, e);
                }
            }
        }
    }

    @Override
    public boolean checkColumnName(String columnName) {
        boolean superCheck = super.checkColumnName(columnName);

        // Fix error, for instance: columnName = "(con.NOMBRE_CONTACTO || ' ' || con.APELLIDO1_CONTACTO || '
        // ' || con.APELLIDO2_CONTACTO)"
        if (superCheck && columnName.trim().startsWith("(") && columnName.trim().endsWith(")")) {
            return false;
        }
        return superCheck;
    }

}

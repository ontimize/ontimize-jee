/**
 * CustomHessianURLConnection.java 18-abr-2013
 *
 *
 *
 */
package com.ontimize.jee.common.hessian;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.caucho.hessian.client.AbstractHessianConnection;
import com.caucho.hessian.client.HessianConnectionException;
import com.ontimize.jee.common.exceptions.InvalidCredentialsException;
import com.ontimize.jee.common.naming.I18NNaming;
import com.ontimize.jee.common.tools.SafeCasting;

/**
 * URLConnection personalizada para conectar con Hessian.
 *
 * @author <a href="user@email.com">Author</a>
 *
 *         Use apache httpclient version
 */
@Deprecated
public class OntimizeHessianURLConnection extends AbstractHessianConnection {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(OntimizeHessianURLConnection.class);

    /** The _url. */
    private final URI url;

    /** The _conn. */
    private URLConnection conn;

    /** The _status code. */
    private HttpStatus statusCode;

    /** The _status message. */
    private String statusMessage;

    /**
     * Instantiates a new custom hessian url connection.
     * @param url the url
     * @param conn the conn
     */
    OntimizeHessianURLConnection(final URI url, final URLConnection conn) {
        this.url = url;
        this.conn = conn;
    }

    /**
     * Adds a HTTP header.
     * @param key the key
     * @param value the value
     */
    @Override
    public void addHeader(final String key, final String value) {
        this.conn.setRequestProperty(key, value);
    }

    /**
     * Returns the output stream for the request.
     * @return the output stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        return this.conn.getOutputStream();
    }

    /**
     * Sends the request.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void sendRequest() throws IOException {
        if (this.conn instanceof HttpURLConnection) {
            HttpURLConnection httpConn = (HttpURLConnection) this.conn;

            this.statusCode = HttpStatus.INTERNAL_SERVER_ERROR;

            try {
                this.statusCode = HttpStatus.valueOf(httpConn.getResponseCode());
            } catch (Exception e) {
                OntimizeHessianURLConnection.logger.debug(e.getMessage(), e);
            }

            this.parseResponseHeaders(httpConn);

            InputStream is = null;

            switch (this.statusCode) {
                case OK:
                    break;

                case FORBIDDEN:
                    throw new InvalidCredentialsException(I18NNaming.E_AUTH_PASSWORD_NOT_MATCH);

                default:
                    StringBuilder sb = new StringBuilder();
                    int ch;

                    try {
                        is = httpConn.getInputStream();

                        if (is != null) {
                            while ((ch = is.read()) >= 0) {
                                sb.append(SafeCasting.intToChar(ch));
                            }

                            is.close();
                        }

                        is = httpConn.getErrorStream();
                        if (is != null) {
                            while ((ch = is.read()) >= 0) {
                                sb.append(SafeCasting.intToChar(ch));
                            }
                        }

                        this.statusMessage = sb.toString();
                    } catch (FileNotFoundException e) {
                        throw new HessianConnectionException("HessianProxy cannot connect to '" + this.url, e);
                    } catch (IOException e) {
                        if (is == null) {
                            throw new HessianConnectionException(this.statusCode + ": " + e, e);
                        }
                        throw new HessianConnectionException(this.statusCode + ": " + sb, e);
                    }

                    if (is != null) {
                        is.close();
                    }

                    throw new HessianConnectionException(this.statusCode + ": " + sb.toString());
            }

        }
    }

    /**
     * Parsea a response headers.
     * @param conn the conn
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void parseResponseHeaders(final HttpURLConnection conn) throws IOException {
        // do nothing right now
    }

    /**
     * Returns the status code.
     * @return the status code
     */
    @Override
    public int getStatusCode() {
        return this.statusCode.value();
    }

    /**
     * Returns the status string.
     * @return the status message
     */
    @Override
    public String getStatusMessage() {
        return this.statusMessage;
    }

    /**
     * Returns the InputStream to the result.
     * @return the input stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return this.conn.getInputStream();
    }

    /**
     * Close/free the connection.
     */
    @Override
    public void close() {
        URLConnection conn = this.conn;
        this.conn = null;

        if (conn instanceof HttpURLConnection) {
            ((HttpURLConnection) conn).disconnect();
        }
    }

    public URLConnection getUnderlinedConnection() {
        return this.conn;
    }

}

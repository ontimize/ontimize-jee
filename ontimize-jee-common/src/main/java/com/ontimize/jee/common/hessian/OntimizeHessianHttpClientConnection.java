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
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
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
 */
public class OntimizeHessianHttpClientConnection extends AbstractHessianConnection {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(OntimizeHessianHttpClientConnection.class);

    static ThreadPoolExecutor POOL = new ThreadPoolExecutor(50, 50, 5, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(100));

    /** The _conn. */
    private final CloseableHttpClient client;

    /** The _status code. */
    private HttpStatus statusCode;

    /** The _status message. */
    private String statusMessage;

    private final HttpPost request;

    private Future<HttpResponse> futureResponse;

    /**
     * Instantiates a new custom hessian url connection.
     * @param url the url
     * @param context
     * @param conn the conn
     */
    OntimizeHessianHttpClientConnection(final HttpPost request, final CloseableHttpClient client) {
        super();
        this.request = request;
        this.client = client;
    }

    /**
     * Adds a HTTP header.
     * @param key the key
     * @param value the value
     */
    @Override
    public void addHeader(final String key, final String value) {
        this.request.addHeader(key, value);
    }

    /**
     * Returns the output stream for the request.
     * @return the output stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        PipedInputStream snk = new PipedInputStream();
        PipedOutputStream os = new PipedOutputStream(snk);
        this.request.setEntity(new InputStreamEntity(snk, ContentType.create("x-application/hessian")));
        this.futureResponse = OntimizeHessianHttpClientConnection.POOL.submit(new Callable<HttpResponse>() {

            @Override
            public HttpResponse call() throws Exception {
                return OntimizeHessianHttpClientConnection.this.client
                    .execute(OntimizeHessianHttpClientConnection.this.request);
            }
        });
        return os;
    }

    /**
     * Sends the request.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void sendRequest() throws IOException {

        this.statusCode = HttpStatus.INTERNAL_SERVER_ERROR;

        HttpResponse response;
        try {
            response = this.futureResponse.get();
        } catch (Exception error) {
            throw new IOException(error);
        }
        try {
            this.statusCode = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
        } catch (Exception e) {
            OntimizeHessianHttpClientConnection.logger.debug(e.getMessage(), e);
        }

        // try {
        // OntimizeHessianHttpClientSessionProcessorFactory.getHttpProcessor().process(response,
        // this.context);
        // } catch (HttpException error) {
        // OntimizeHessianHttpClientConnection.logger.error(null, error);
        // }

        // this.parseResponseHeaders(httpConn);

        InputStream is = null;

        switch (this.statusCode) {
            case OK:
                break;

            case FORBIDDEN:
            case UNAUTHORIZED:
                throw new InvalidCredentialsException(I18NNaming.E_AUTH_PASSWORD_NOT_MATCH);
            case FOUND:// oauth2 authentication
                throw new InvalidCredentialsException(I18NNaming.E_AUTH_PASSWORD_NOT_MATCH);
            default:
                StringBuilder sb = new StringBuilder();
                int ch;

                try {
                    is = response.getEntity().getContent();

                    if (is != null) {
                        while ((ch = is.read()) >= 0) {
                            sb.append(SafeCasting.intToChar(ch));
                        }

                        is.close();
                    }

                    this.statusMessage = sb.toString();
                } catch (FileNotFoundException e) {
                    throw new HessianConnectionException("HessianProxy cannot connect to '" + this.request.getURI(), e);
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
        try {
            return this.futureResponse.get().getEntity().getContent();
        } catch (Exception error) {
            throw new IOException(error);
        }
    }

    /**
     * Close/free the connection.
     */
    @Override
    public void close() {
        try {
            this.client.close();
        } catch (IOException error) {
            OntimizeHessianHttpClientConnection.logger.error(null, error);
        }
    }

}

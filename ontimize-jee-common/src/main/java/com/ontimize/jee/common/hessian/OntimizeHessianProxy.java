package com.ontimize.jee.common.hessian;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;

import org.apache.http.client.NonRepeatableRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.client.HessianConnection;
import com.caucho.hessian.client.HessianProxy;
import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.client.HessianRuntimeException;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.ontimize.jee.common.exceptions.InvalidCredentialsException;
import com.ontimize.jee.common.security.ILoginProvider;

public class OntimizeHessianProxy extends HessianProxy {

    private static final Logger logger = LoggerFactory.getLogger(OntimizeHessianProxy.class);

    public OntimizeHessianProxy(URI url, HessianProxyFactory factory) {
        super(url, factory);
    }

    public OntimizeHessianProxy(URI url, HessianProxyFactory factory, Class<?> type) {
        super(url, factory, type);
    }

    @Override
    protected OntimizeHessianProxyFactory getFactory() {
        return (OntimizeHessianProxyFactory) this.factory;
    }

    /**
     * Sends the HTTP request to the Hessian connection.
     */
    @Override
    protected HessianConnection sendRequest(String methodName, Object[] args) throws IOException {
        try {
            return this.internalSendRequest(methodName, args);
        } catch (IOException exception) {
            Throwable cause = exception;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            if (cause instanceof NonRepeatableRequestException) {
                // significa que intento autenticar
                if (this.relogin()) {
                    return this.internalSendRequest(methodName, args);
                }
            }
            throw exception;
        } catch (InvalidCredentialsException ex) {
            if (this.relogin()) {
                return this.internalSendRequest(methodName, args);
            }
            throw ex;
        }
    }

    protected boolean relogin() {
        ILoginProvider loginProvider = this.getFactory().getLoginProvider();
        if (loginProvider != null) {
            try {
                loginProvider.doLogin(this.getURL());
                return true;
            } catch (Exception error) {
                OntimizeHessianProxy.logger.error(null, error);
            }
        }
        return false;
    }

    private HessianConnection internalSendRequest(String methodName, Object[] args) throws IOException {
        HessianConnection conn = null;

        conn = this.getFactory().getConnectionFactory().open(this.getURL());
        if ((args != null) && (args.length > 0) && (args[args.length - 1] instanceof InputStream)
                && (conn instanceof OntimizeHessianURLConnection) && (((OntimizeHessianURLConnection) conn)
                    .getUnderlinedConnection() instanceof HttpURLConnection)) {
            ((HttpURLConnection) ((OntimizeHessianURLConnection) conn).getUnderlinedConnection())
                .setChunkedStreamingMode(0);
        }
        boolean isValid = false;
        OutputStream os = null;

        try {
            this.addRequestHeaders(conn);

            try {
                os = conn.getOutputStream();
            } catch (Exception e) {
                throw new HessianRuntimeException(e);
            }

            AbstractHessianOutput out = this.getFactory().getHessianOutput(os);
            out.call(methodName, args);
            out.flush();
            if (conn instanceof OntimizeHessianHttpClientConnection) { // TODO repensar alternativa
                os.close();
            }
            conn.sendRequest();
            isValid = true;
            return conn;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (Exception e) {
                OntimizeHessianProxy.logger.info(e.toString(), e);
            }

            try {
                if (!isValid && (conn != null)) {
                    conn.close();
                }
            } catch (Exception e) {
                OntimizeHessianProxy.logger.info(e.toString(), e);
            }
        }
    }

}

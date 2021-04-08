package com.ontimize.jee.server.hessian;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.caucho.HessianClientInterceptor;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;
import org.springframework.remoting.caucho.SimpleHessianServiceExporter;
import org.springframework.remoting.support.RemoteExporter;
import org.springframework.util.Assert;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.util.NestedServletException;

import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.HessianRemoteResolver;
import com.caucho.hessian.io.SerializerFactory;
import com.caucho.hessian.server.HessianSkeleton;
import com.caucho.hessian.util.IExceptionTranslator;

/**
 * Servlet-API-based HTTP request handler that exports the specified service bean as Hessian service
 * endpoint, accessible via a Hessian proxy.
 *
 * <p>
 * <b>Note:</b> Spring also provides an alternative version of this exporter, for Sun's JRE 1.6 HTTP
 * server: {@link SimpleHessianServiceExporter}.
 *
 * <p>
 * Hessian is a slim, binary RPC protocol. For information on Hessian, see the
 * <a href="http://www.caucho.com/hessian">Hessian website</a>. <b>Note: As of Spring 4.0, this
 * exporter requires Hessian 4.0 or above.</b>
 *
 * <p>
 * Hessian services exported with this class can be accessed by any Hessian client, as there isn't
 * any special handling involved.
 *
 * @author Juergen Hoeller
 * @since 13.05.2003
 * @see HessianClientInterceptor
 * @see HessianProxyFactoryBean
 * @see org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter
 * @see org.springframework.remoting.rmi.RmiServiceExporter
 */
public class OntimizeHessianExporter extends RemoteExporter implements InitializingBean, HttpRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(OntimizeHessianExporter.class);

    public static final String CONTENT_TYPE_HESSIAN = "application/x-hessian";

    private SerializerFactory serializerFactory = new SerializerFactory();

    private HessianRemoteResolver remoteResolver;

    private HessianSkeleton skeleton;

    private IExceptionTranslator exceptionTranslator;

    public OntimizeHessianExporter() {
        super();
    }

    @Override
    public void afterPropertiesSet() {
        this.prepare();
    }

    public void prepare() {
        this.checkService();
        this.checkServiceInterface();
        this.skeleton = new HessianSkeleton(this.getProxyForService(), this.getServiceInterface(),
                this.getExceptionTranslator());
    }

    public IExceptionTranslator getExceptionTranslator() {
        return this.exceptionTranslator;
    }

    public void setExceptionTranslator(IExceptionTranslator exceptionTranslator) {
        this.exceptionTranslator = exceptionTranslator;
    }

    /**
     * Processes the incoming Hessian request and creates a Hessian response.
     */
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!"POST".equals(request.getMethod())) {
            throw new HttpRequestMethodNotSupportedException(request.getMethod(), new String[] { "POST" },
                    "HessianServiceExporter only supports POST requests");
        }

        response.setContentType(OntimizeHessianExporter.CONTENT_TYPE_HESSIAN);
        try {
            this.invoke(request.getInputStream(), response.getOutputStream());
        } catch (Exception ex) {
            throw new NestedServletException("Hessian skeleton invocation failed", ex);
        }
    }

    /**
     * Specify the Hessian SerializerFactory to use.
     * <p>
     * This will typically be passed in as an inner bean definition of type
     * {@code com.caucho.hessian.io.SerializerFactory}, with custom bean property values applied.
     */
    public void setSerializerFactory(SerializerFactory serializerFactory) {
        this.serializerFactory = (serializerFactory != null ? serializerFactory : new SerializerFactory());
    }

    /**
     * Set whether to send the Java collection type for each serialized collection. Default is "true".
     */
    public void setSendCollectionType(boolean sendCollectionType) {
        this.serializerFactory.setSendCollectionType(sendCollectionType);
    }

    /**
     * Set whether to allow non-serializable types as Hessian arguments and return values. Default is
     * "true".
     */
    public void setAllowNonSerializable(boolean allowNonSerializable) {
        this.serializerFactory.setAllowNonSerializable(allowNonSerializable);
    }

    /**
     * Specify a custom HessianRemoteResolver to use for resolving remote object references.
     */
    public void setRemoteResolver(HessianRemoteResolver remoteResolver) {
        this.remoteResolver = remoteResolver;
    }

    /**
     * Perform an invocation on the exported object.
     * @param inputStream the request stream
     * @param outputStream the response stream
     * @throws Exception
     * @throws Throwable if invocation failed
     */
    public void invoke(InputStream inputStream, OutputStream outputStream) throws Exception {
        Assert.notNull(this.skeleton, "Hessian exporter has not been initialized");
        this.doInvoke(this.skeleton, inputStream, outputStream);
    }

    /**
     * Actually invoke the skeleton with the given streams.
     * @param skeleton the skeleton to invoke
     * @param inputStream the request stream
     * @param outputStream the response stream
     * @throws Exception
     * @throws Throwable if invocation failed
     */
    protected void doInvoke(HessianSkeleton skeleton, InputStream inputStream, OutputStream outputStream)
            throws Exception {

        ClassLoader originalClassLoader = this.overrideThreadContextClassLoader();
        try {
            InputStream isToUse = inputStream;
            OutputStream osToUse = outputStream;

            if (!isToUse.markSupported()) {
                isToUse = new BufferedInputStream(isToUse);
                isToUse.mark(1);
            }

            int code = isToUse.read();
            int major;
            int minor;

            AbstractHessianInput in;
            AbstractHessianOutput out;

            if (code == 'H') {
                // Hessian 2.0 stream
                major = isToUse.read();
                minor = isToUse.read();
                if (major != 0x02) {
                    throw new IOException("Version " + major + '.' + minor + " is not understood");
                }
                in = new Hessian2Input(isToUse);
                out = new Hessian2Output(osToUse);
                in.readCall();
            } else if (code == 'C') {
                // Hessian 2.0 call... for some reason not handled in HessianServlet!
                isToUse.reset();
                in = new Hessian2Input(isToUse);
                out = new Hessian2Output(osToUse);
                in.readCall();
            } else {
                throw new IOException("Expected 'H'/'C' (Hessian 2.0) in hessian input at " + code);
            }

            if (this.serializerFactory != null) {
                in.setSerializerFactory(this.serializerFactory);
                out.setSerializerFactory(this.serializerFactory);
            }
            if (this.remoteResolver != null) {
                in.setRemoteResolver(this.remoteResolver);
            }

            try {
                skeleton.invoke(in, out);
            } finally {
                try {
                    in.close();
                    isToUse.close();
                } catch (IOException ex) {
                    OntimizeHessianExporter.logger.trace(null, ex);
                }
                try {
                    out.close();
                    osToUse.close();
                } catch (IOException ex) {
                    OntimizeHessianExporter.logger.trace(null, ex);
                }
            }
        } finally {
            this.resetThreadContextClassLoader(originalClassLoader);
        }
    }

}

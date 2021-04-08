/**
 * CustomSpnegoHessianProxyFactory.java 18-abr-2013
 *
 *
 *
 */
package com.ontimize.jee.common.hessian;

import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.caucho.hessian.client.HessianConnectionFactory;
import com.caucho.hessian.client.HessianProxy;
import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.HessianRemoteObject;
import com.ontimize.jee.common.security.ILoginProvider;
import com.ontimize.jee.common.session.HeaderAttribute;
import com.ontimize.jee.common.session.HeaderAttributesProvider;
import com.ontimize.jee.common.session.StaticHeaderAttributesProvider;

/**
 * Factoria personalizada para conectar con servicios hessian.
 *
 * @author <a href="user@email.com">Author</a>
 */
public class OntimizeHessianProxyFactory extends HessianProxyFactory implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(OntimizeHessianProxyFactory.class);

    /** The header attributes provider. */
    private HeaderAttributesProvider headerAttributesProvider;

    private ApplicationContext applicationContext;

    private ILoginProvider loginProvider;

    /**
     * Instantiates a new custom spnego hessian proxy factory.
     */
    public OntimizeHessianProxyFactory() {
        super();
        this.headerAttributesProvider = new StaticHeaderAttributesProvider();
        // this.setDebug(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HessianConnectionFactory createHessianConnectionFactory() {
        return new OntimizeHessianHttpClientConnectionFactory();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.caucho.hessian.client.HessianProxyFactory#getHessianOutput(java.io.OutputStream)
     */
    @Override
    public AbstractHessianOutput getHessianOutput(final OutputStream os) {
        return super.getHessianOutput(os);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUser(String user) {
        if ("".equals(user)) {
            return;
        }
        super.setUser(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPassword(String password) {
        if ("".equals(password)) {
            return;
        }
        super.setPassword(password);
    }

    /**
     * Sets the header attributes provider.
     * @param headerAttributesProvider the new header attributes provider
     */
    public void setHeaderAttributesProvider(HeaderAttributesProvider headerAttributesProvider) {
        this.headerAttributesProvider = headerAttributesProvider;
    }

    /**
     * Creates a new proxy with the specified URL. The returned object is a proxy with the interface
     * specified by api.
     *
     * <pre>
     *  String url = "http://localhost:8080/ejb/hello"); HelloHome hello = (HelloHome) factory.create(HelloHome.class, url);
     * </pre>
     *
     * @param api the interface the proxy class needs to implement
     * @param url the URL where the client object is located.
     * @param loader the loader
     * @return a proxy to the object with the specified interface.
     */
    @Override
    public Object create(Class<?> api, URI url, ClassLoader loader) {
        if (api == null) {
            throw new NullPointerException("api must not be null for HessianProxyFactory.create()");
        }
        InvocationHandler handler = null;

        handler = new OntimizeHessianProxy(url, this, api);

        return Proxy.newProxyInstance(loader, new Class[] { api, HessianRemoteObject.class }, handler);
    }

    /**
     * Adds the header attribute.
     * @param headerAttribute the header attribute
     */
    public void addHeaderAttribute(HeaderAttribute headerAttribute) {
        this.headerAttributesProvider.addHeaderAttribute(headerAttribute);
    }

    /**
     * Removes the header attribute.
     * @param headerAttribute the header attribute
     * @return true, if successful
     */
    public boolean removeHeaderAttribute(HeaderAttribute headerAttribute) {
        return this.headerAttributesProvider.removeHeaderAttribute(headerAttribute);
    }

    /**
     * Gets the header attributes provider.
     * @return the header attributes provider
     */
    public HeaderAttributesProvider getHeaderAttributesProvider() {
        return this.headerAttributesProvider;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }

    public void setLoginProvider(ILoginProvider loginProvider) {
        this.loginProvider = loginProvider;
    }

    public ILoginProvider getLoginProvider() {
        if (this.loginProvider != null) {
            return this.loginProvider;
        }
        if (this.getApplicationContext() != null) {
            try {
                return this.getApplicationContext().getBean(ILoginProvider.class);
            } catch (NoSuchBeanDefinitionException nofound) {
                OntimizeHessianProxyFactory.logger.warn("No IloginProvider bean for negociate", nofound);
            }
        }
        return null;
    }

}

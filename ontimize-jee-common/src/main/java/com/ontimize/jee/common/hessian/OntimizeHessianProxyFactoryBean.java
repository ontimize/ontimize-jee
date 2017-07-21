package com.ontimize.jee.common.hessian;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.RemoteConnectFailureException;
import org.springframework.remoting.RemoteLookupFailureException;
import org.springframework.remoting.RemoteProxyFailureException;
import org.springframework.remoting.support.RemoteAccessor;
import org.springframework.util.Assert;

import com.caucho.hessian.HessianException;
import com.caucho.hessian.client.HessianConnectionException;
import com.caucho.hessian.client.HessianConnectionFactory;
import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.client.HessianRuntimeException;
import com.caucho.hessian.io.SerializerFactory;

/**
 * Se encarga de anteponer el host del servidor donde se va a desplegar el servidor en función de una propiedad del sistema. La propiedad será: com.ontimize.services.baseUrl Es
 * decir, la url del servicio será: <pre> serviceUrl = System.getProperty("com.ontimize.services.baseUrl")+serviceRelativeUrl </pre>
 *
 * @author joaquin.romero
 *
 */
public class OntimizeHessianProxyFactoryBean extends RemoteAccessor implements MethodInterceptor, FactoryBean<Object>, InitializingBean {

	private static final Logger	logger				= LoggerFactory.getLogger(OntimizeHessianProxyFactoryBean.class);

	public static final String	SERVICES_BASE_URL	= "com.ontimize.services.baseUrl";

	private HessianProxyFactory	proxyFactory		= new HessianProxyFactory();
	private Object				hessianProxy;
	/** The service relative url. */
	protected String			serviceRelativeUrl;
	private Object				serviceProxy;
	private String				serviceUrl;

	/**
	 * Instantiates a new ontimize hessian proxy factory bean.
	 */
	public OntimizeHessianProxyFactoryBean() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.remoting.caucho.HessianProxyFactoryBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {
		// do nothing
	}

	protected void checkProxyBuild() {
		if (this.serviceProxy == null) {
			this.setHessian2Reply(true);
			this.setHessian2Request(true);
			if ((this.getServiceRelativeUrl() != null) && (this.getServiceRelativeUrl().length() > 0)) {
				String base = System.getProperty(OntimizeHessianProxyFactoryBean.SERVICES_BASE_URL);
				if (base != null) {
					if ((base.charAt(base.length() - 1) != '/') && (this.getServiceRelativeUrl().charAt(0) != '/')) {
						base = base + '/';
					}
					this.setServiceUrl(base + this.getServiceRelativeUrl());
				}
			}
			this.prepare();
			this.serviceProxy = new ProxyFactory(this.getServiceInterface(), this).getProxy(this.getBeanClassLoader());
		}
	}

	/**
	 * Initialize the Hessian proxy for this interceptor.
	 *
	 * @throws RemoteLookupFailureException
	 *             if the service URL is invalid
	 */
	public void prepare() throws RemoteLookupFailureException {
		try {
			this.hessianProxy = this.createHessianProxy(this.proxyFactory);
		} catch (MalformedURLException | URISyntaxException ex) {
			throw new RemoteLookupFailureException("Service URL [" + this.getServiceUrl() + "] is invalid", ex);
		}
	}

	/**
	 * Create the Hessian proxy that is wrapped by this interceptor.
	 *
	 * @param proxyFactory
	 *            the proxy factory to use
	 * @return the Hessian proxy
	 * @throws MalformedURLException
	 *             if thrown by the proxy factory
	 * @throws URISyntaxException
	 * @see com.caucho.hessian.client.HessianProxyFactory#create
	 */
	protected Object createHessianProxy(HessianProxyFactory proxyFactory) throws MalformedURLException, URISyntaxException {
		Assert.notNull(this.getServiceInterface(), "'serviceInterface' is required");
		return proxyFactory.create(this.getServiceInterface(), this.getServiceUrl(), this.getBeanClassLoader());
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		if (this.hessianProxy == null) {
			throw new IllegalStateException("HessianClientInterceptor is not properly initialized - " + "invoke 'prepare' before attempting any operations");
		}

		ClassLoader originalClassLoader = this.overrideThreadContextClassLoader();
		try {
			return invocation.getMethod().invoke(this.hessianProxy, invocation.getArguments());
		} catch (InvocationTargetException ex) {
			OntimizeHessianProxyFactoryBean.logger.debug("version checking", ex);
			Throwable targetEx = ex.getTargetException();
			// Hessian 4.0 check: another layer of InvocationTargetException.
			if (targetEx instanceof InvocationTargetException) {
				targetEx = ((InvocationTargetException) targetEx).getTargetException();
			}
			if (targetEx instanceof HessianConnectionException) {
				throw this.convertHessianAccessException(targetEx);
			} else if ((targetEx instanceof HessianException) || (targetEx instanceof HessianRuntimeException)) {
				Throwable cause = targetEx.getCause();
				throw this.convertHessianAccessException(cause != null ? cause : targetEx);
			} else if (targetEx instanceof UndeclaredThrowableException) {
				UndeclaredThrowableException utex = (UndeclaredThrowableException) targetEx;
				throw this.convertHessianAccessException(utex.getUndeclaredThrowable());
			} else {
				throw targetEx;
			}
		} catch (Throwable ex) {
			throw new RemoteProxyFailureException("Failed to invoke Hessian proxy for remote service [" + this.getServiceUrl() + "]", ex);
		} finally {
			this.resetThreadContextClassLoader(originalClassLoader);
		}
	}

	/**
	 * Convert the given Hessian access exception to an appropriate Spring RemoteAccessException.
	 *
	 * @param ex
	 *            the exception to convert
	 * @return the RemoteAccessException to throw
	 */
	protected RemoteAccessException convertHessianAccessException(Throwable ex) {
		if ((ex instanceof HessianConnectionException) || (ex instanceof ConnectException)) {
			return new RemoteConnectFailureException("Cannot connect to Hessian remote service at [" + this.getServiceUrl() + "]", ex);
		} else {
			return new RemoteAccessException("Cannot access Hessian remote service at [" + this.getServiceUrl() + "]", ex);
		}
	}

	@Override
	public Object getObject() {
		this.checkProxyBuild();
		return this.serviceProxy;
	}

	@Override
	public Class<?> getObjectType() {
		return this.getServiceInterface();
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	/**
	 * Set the HessianProxyFactory instance to use. If not specified, a default HessianProxyFactory will be created. <p>Allows to use an externally configured factory instance, in
	 * particular a custom HessianProxyFactory subclass.
	 */
	public void setProxyFactory(HessianProxyFactory proxyFactory) {
		this.proxyFactory = (proxyFactory != null ? proxyFactory : new HessianProxyFactory());
	}

	/**
	 * Specify the Hessian SerializerFactory to use. <p>This will typically be passed in as an inner bean definition of type {@code com.caucho.hessian.io.SerializerFactory}, with
	 * custom bean property values applied.
	 */
	public void setSerializerFactory(SerializerFactory serializerFactory) {
		this.proxyFactory.setSerializerFactory(serializerFactory);
	}

	/**
	 * Set whether to send the Java collection type for each serialized collection. Default is "true".
	 */
	public void setSendCollectionType(boolean sendCollectionType) {
		this.proxyFactory.getSerializerFactory().setSendCollectionType(sendCollectionType);
	}

	/**
	 * Set whether to allow non-serializable types as Hessian arguments and return values. Default is "true".
	 */
	public void setAllowNonSerializable(boolean allowNonSerializable) {
		this.proxyFactory.getSerializerFactory().setAllowNonSerializable(allowNonSerializable);
	}

	/**
	 * Set whether overloaded methods should be enabled for remote invocations. Default is "false".
	 *
	 * @see com.caucho.hessian.client.HessianProxyFactory#setOverloadEnabled
	 */
	public void setOverloadEnabled(boolean overloadEnabled) {
		this.proxyFactory.setOverloadEnabled(overloadEnabled);
	}

	/**
	 * Set the username that this factory should use to access the remote service. Default is none. <p>The username will be sent by Hessian via HTTP Basic Authentication.
	 *
	 * @see com.caucho.hessian.client.HessianProxyFactory#setUser
	 */
	public void setUsername(String username) {
		this.proxyFactory.setUser(username);
	}

	/**
	 * Set the password that this factory should use to access the remote service. Default is none. <p>The password will be sent by Hessian via HTTP Basic Authentication.
	 *
	 * @see com.caucho.hessian.client.HessianProxyFactory#setPassword
	 */
	public void setPassword(String password) {
		this.proxyFactory.setPassword(password);
	}

	/**
	 * Set whether Hessian's debug mode should be enabled. Default is "false".
	 *
	 * @see com.caucho.hessian.client.HessianProxyFactory#setDebug
	 */
	public void setDebug(boolean debug) {
		this.proxyFactory.setDebug(debug);
	}

	/**
	 * Set whether to use a chunked post for sending a Hessian request.
	 *
	 * @see com.caucho.hessian.client.HessianProxyFactory#setChunkedPost
	 */
	public void setChunkedPost(boolean chunkedPost) {
		this.proxyFactory.setChunkedPost(chunkedPost);
	}

	/**
	 * Specify a custom HessianConnectionFactory to use for the Hessian client.
	 */
	public void setConnectionFactory(HessianConnectionFactory connectionFactory) {
		this.proxyFactory.setConnectionFactory(connectionFactory);
	}

	/**
	 * Set the socket connect timeout to use for the Hessian client.
	 *
	 * @see com.caucho.hessian.client.HessianProxyFactory#setConnectTimeout
	 */
	public void setConnectTimeout(long timeout) {
		this.proxyFactory.setConnectTimeout(timeout);
	}

	/**
	 * Set the timeout to use when waiting for a reply from the Hessian service.
	 *
	 * @see com.caucho.hessian.client.HessianProxyFactory#setReadTimeout
	 */
	public void setReadTimeout(long timeout) {
		this.proxyFactory.setReadTimeout(timeout);
	}

	/**
	 * Set whether version 2 of the Hessian protocol should be used for parsing requests and replies. Default is "false".
	 *
	 * @see com.caucho.hessian.client.HessianProxyFactory#setHessian2Request
	 */
	public void setHessian2(boolean hessian2) {
		this.proxyFactory.setHessian2Request(hessian2);
		this.proxyFactory.setHessian2Reply(hessian2);
	}

	/**
	 * Set whether version 2 of the Hessian protocol should be used for parsing requests. Default is "false".
	 *
	 * @see com.caucho.hessian.client.HessianProxyFactory#setHessian2Request
	 */
	public void setHessian2Request(boolean hessian2) {
		this.proxyFactory.setHessian2Request(hessian2);
	}

	/**
	 * Set whether version 2 of the Hessian protocol should be used for parsing replies. Default is "false".
	 *
	 * @see com.caucho.hessian.client.HessianProxyFactory#setHessian2Reply
	 */
	public void setHessian2Reply(boolean hessian2) {
		this.proxyFactory.setHessian2Reply(hessian2);
	}

	/**
	 * Set the URL of this remote accessor's target service. The URL must be compatible with the rules of the particular remoting provider.
	 */
	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	/**
	 * Return the URL of this remote accessor's target service.
	 */
	public String getServiceUrl() {
		return this.serviceUrl;
	}

	/**
	 * Sets the service relative url.
	 *
	 * @param serviceRelativeUrl
	 *            the new service relative url
	 */
	public void setServiceRelativeUrl(String serviceRelativeUrl) {
		this.serviceRelativeUrl = serviceRelativeUrl;
	}

	/**
	 * Gets the service relative url.
	 *
	 * @return the service relative url
	 */
	public String getServiceRelativeUrl() {
		return this.serviceRelativeUrl;
	}
}

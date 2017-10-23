// Fix bug when returning an InputStream

package com.caucho.hessian.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.URI;

import org.apache.http.client.NonRepeatableRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.HessianProtocolException;
import com.ontimize.jee.common.exceptions.InvalidCredentialsException;
import com.ontimize.jee.common.hessian.OntimizeHessianHttpClientConnection;
import com.ontimize.jee.common.hessian.OntimizeHessianProxyFactory;
import com.ontimize.jee.common.hessian.OntimizeHessianURLConnection;
import com.ontimize.jee.common.security.ILoginProvider;

/**
 * Proxy implementation for Hessian clients. Applications will generally use HessianProxyFactory to create proxy clients.
 */
public class OntimizeHessianProxy extends HessianProxy {

	private static final Logger logger = LoggerFactory.getLogger(OntimizeHessianProxy.class);

	/**
	 * Protected constructor for subclassing
	 */
	public OntimizeHessianProxy(URI url, HessianProxyFactory factory) {
		super(url, factory, null);
	}

	/**
	 * Protected constructor for subclassing
	 */
	public OntimizeHessianProxy(URI url, HessianProxyFactory factory, Class<?> type) {
		super(url, factory, type);
	}

	protected OntimizeHessianProxyFactory getFactory() {
		return (OntimizeHessianProxyFactory) this.factory;
	}

	/**
	 * Handles the object invocation.
	 *
	 * @param proxy
	 *            the proxy object to invoke
	 * @param method
	 *            the method to call
	 * @param args
	 *            the arguments to the proxy object
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String mangleName;

		synchronized (this.getMangleMap()) {
			mangleName = this.getMangleMap().get(method);
		}

		if (mangleName == null) {
			String methodName = method.getName();
			Class<?>[] params = method.getParameterTypes();

			// equals and hashCode are special cased
			if (methodName.equals("equals") && (params.length == 1) && params[0].equals(Object.class)) {
				Object value = args[0];
				if ((value == null) || !Proxy.isProxyClass(value.getClass())) {
					return Boolean.FALSE;
				}

				Object proxyHandler = Proxy.getInvocationHandler(value);

				if (!(proxyHandler instanceof OntimizeHessianProxy)) {
					return Boolean.FALSE;
				}

				OntimizeHessianProxy handler = (OntimizeHessianProxy) proxyHandler;

				// JOK: este es el motivo de sobrescribir el metodo: el URL.equals que hay en la version original es lentisimo en segun que
				// circunstancias, es necesario cambiarlo por URI.equals
				return Boolean.valueOf(this.getURL().equals(handler.getURL()));
			} else if (methodName.equals("hashCode") && (params.length == 0)) {
				return Integer.valueOf(this.getURL().hashCode());
			} else if (methodName.equals("getHessianType")) {
				return proxy.getClass().getInterfaces()[0].getName();
			} else if (methodName.equals("getHessianURL")) {
				return this.getURL().toString();
			} else if (methodName.equals("toString") && (params.length == 0)) {
				return "HessianProxy[" + this.getURL() + "]";
			}

			if (!this.getFactory().isOverloadEnabled()) {
				mangleName = method.getName();
			} else {
				mangleName = this.mangleName(method);
			}

			synchronized (this.getMangleMap()) {
				this.getMangleMap().put(method, mangleName);
			}
		}

		InputStream is = null;
		HessianConnection conn = null;
		try {
			OntimizeHessianProxy.logger.trace("Hessian[{}] calling {}", this.getURL(), mangleName);

			conn = this.sendRequest(mangleName, args);

			is = conn.getInputStream();

			AbstractHessianInput in;

			int code = is.read();

			if (code == 'H') {
				int major = is.read();
				int minor = is.read();
				OntimizeHessianProxy.logger.info("Major: {} , Minor: {}", major, minor);
				in = this.getFactory().getHessian2Input(is);

				Object value = in.readReply(method.getReturnType());

				/* + */if (value instanceof InputStream) {
					/* + */value = new ResultInputStream(conn, is, in, (InputStream) value);
					/* + */is = null;
					/* + */conn = null;
					/* + */}
				return value;
			} else if (code == 'r') {
				int major = is.read();
				int minor = is.read();
				OntimizeHessianProxy.logger.info("Major: {} , Minor: {}", major, minor);

				in = this.getFactory().getHessianInput(is);

				in.startReplyBody();

				Object value = in.readObject(method.getReturnType());

				if (value instanceof InputStream) {
					value = new ResultInputStream(conn, is, in, (InputStream) value);
					is = null;
					conn = null;
				} else {
					in.completeReply();
				}

				return value;
			} else {
				throw new HessianProtocolException("'" + (char) code + "' is an unknown code");
			}
		} catch (HessianProtocolException e) {
			throw new HessianRuntimeException(e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
				OntimizeHessianProxy.logger.info(e.toString(), e);
			}

			try {
				if (conn != null) {
					conn.destroy();
				}
			} catch (Exception e) {
				OntimizeHessianProxy.logger.info(e.toString(), e);
			}
		}
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
		if ((args != null) && (args.length > 0) && (args[args.length - 1] instanceof InputStream) && (conn instanceof OntimizeHessianURLConnection) && (((OntimizeHessianURLConnection) conn)
		        .getUnderlinedConnection() instanceof HttpURLConnection)) {
			((HttpURLConnection) ((OntimizeHessianURLConnection) conn).getUnderlinedConnection()).setChunkedStreamingMode(0);
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

			AbstractHessianOutput out;

			out = this.getFactory().getHessianOutput(os);

			out.call(methodName, args);
			out.flush();
			if (conn instanceof OntimizeHessianHttpClientConnection) {
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
					conn.destroy();
				}
			} catch (Exception e) {
				OntimizeHessianProxy.logger.info(e.toString(), e);
			}
		}
	}

}

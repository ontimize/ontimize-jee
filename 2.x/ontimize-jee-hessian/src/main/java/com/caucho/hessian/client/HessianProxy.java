/*
 * The Apache Software License, Version 1.1 Copyright (c) 2001-2004 Caucho Technology, Inc. All rights reserved. Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 1. Redistributions of source code must retain the above copyright notice, this list of conditions and
 * the following disclaimer. 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution. 3. The end-user documentation included with the redistribution, if any, must include the following acknowlegement: "This
 * product includes software developed by the Caucho Technology (http://www.caucho.com/)." Alternately, this acknowlegement may appear in the software itself, if and wherever such
 * third-party acknowlegements normally appear. 4. The names "Hessian", "Resin", and "Caucho" must not be used to endorse or promote products derived from this software without
 * prior written permission. For written permission, please contact info@caucho.com. 5. Products derived from this software may not be called "Resin" nor may "Resin" appear in
 * their names without prior written permission of Caucho Technology. THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL CAUCHO TECHNOLOGY OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * @author Scott Ferguson
 */

package com.caucho.hessian.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URLConnection;
import java.util.WeakHashMap;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.HessianProtocolException;
import com.caucho.hessian.io.HessianRemote;
import com.caucho.services.server.AbstractSkeleton;

/**
 * Proxy implementation for Hessian clients. Applications will generally use HessianProxyFactory to create proxy clients.
 */
public class HessianProxy implements InvocationHandler, Serializable {

	private static final Logger					logger				= LoggerFactory.getLogger(HessianProxy.class);

	private static final long					serialVersionUID	= 1L;
	protected HessianProxyFactory				factory;

	private final WeakHashMap<Method, String>	mangleMap			= new WeakHashMap<>();

	private final Class<?>						type;
	private final URI							url;

	/**
	 * Protected constructor for subclassing
	 */
	public HessianProxy(URI url, HessianProxyFactory factory) {
		this(url, factory, null);
	}

	/**
	 * Protected constructor for subclassing
	 */
	public HessianProxy(URI url, HessianProxyFactory factory, Class<?> type) {
		this.factory = factory;
		this.url = url;
		this.type = type;
	}

	/**
	 * Returns the proxy's URL.
	 */
	public URI getURL() {
		return this.url;
	}

	protected HessianProxyFactory getFactory() {
		return this.factory;
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

		synchronized (this.mangleMap) {
			mangleName = this.mangleMap.get(method);
		}

		if (mangleName == null) {
			Object res = this.invokeSpecialCases(proxy, method, args);
			if (res != null) {
				return res;
			}

			if (!this.factory.isOverloadEnabled()) {
				mangleName = method.getName();
			} else {
				mangleName = this.mangleName(method);
			}

			synchronized (this.mangleMap) {
				this.mangleMap.put(method, mangleName);
			}
		}

		InputStream is = null;
		HessianConnection conn = null;

		try {
			HessianProxy.logger.trace("Hessian[{}] calling {}", this.url, mangleName);
			conn = this.sendRequest(mangleName, args);
			is = this.getInputStream(conn);
			AbstractHessianInput in;

			int code = is.read();

			if (code != 'H') {
				throw new HessianProtocolException("'" + (char) code + "' is an unknown code");
			}
			int major = is.read();
			int minor = is.read();
			HessianProxy.logger.debug("Major: {} , Minor: {}", major, minor);
			in = this.factory.getHessian2Input(is);

			Object value = in.readReply(method.getReturnType());
			if (value instanceof InputStream) {
				is = null;
				conn = null;
			}
			return value;
		} catch (HessianProtocolException e) {
			throw new HessianRuntimeException(e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
				HessianProxy.logger.trace(e.toString(), e);
			}

			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				HessianProxy.logger.trace(e.toString(), e);
			}
		}
	}

	protected Object invokeSpecialCases(Object proxy, Method method, Object[] args) {
		String methodName = method.getName();
		Class<?>[] params = method.getParameterTypes();

		// equals and hashCode are special cased
		if ("equals".equals(methodName) && (params.length == 1) && params[0].equals(Object.class)) {
			Object value = args[0];
			if ((value == null) || !Proxy.isProxyClass(value.getClass())) {
				return Boolean.FALSE;
			}
			Object proxyHandler = Proxy.getInvocationHandler(value);

			if (!(proxyHandler instanceof HessianProxy)) {
				return Boolean.FALSE;
			}
			HessianProxy handler = (HessianProxy) proxyHandler;
			return Boolean.valueOf(this.getURL().equals(handler.getURL()));
		} else if (methodName.equals("hashCode") && (params.length == 0)) {
			return new Integer(this.url.hashCode());
		} else if (methodName.equals("getHessianType")) {
			return proxy.getClass().getInterfaces()[0].getName();
		} else if (methodName.equals("getHessianURL")) {
			return this.url.toString();
		} else if (methodName.equals("toString") && (params.length == 0)) {
			return "HessianProxy[" + this.url + "]";
		}
		return null;
	}

	protected InputStream getInputStream(HessianConnection conn) throws IOException {
		InputStream is = conn.getInputStream();

		if ("deflate".equals(conn.getContentEncoding())) {
			is = new InflaterInputStream(is, new Inflater(true));
		}

		return is;
	}

	protected String mangleName(Method method) {
		Class<?>[] param = method.getParameterTypes();

		if ((param == null) || (param.length == 0)) {
			return method.getName();
		}
		return AbstractSkeleton.mangleName(method, false);
	}

	/**
	 * Sends the HTTP request to the Hessian connection.
	 */
	protected HessianConnection sendRequest(String methodName, Object[] args) throws IOException {
		HessianConnection conn = null;

		conn = this.factory.getConnectionFactory().open(this.url);
		boolean isValid = false;

		try {
			this.addRequestHeaders(conn);

			OutputStream os = null;

			try {
				os = conn.getOutputStream();
			} catch (Exception e) {
				throw new HessianRuntimeException(e);
			}

			AbstractHessianOutput out = this.factory.getHessianOutput(os);

			out.call(methodName, args);
			out.flush();

			conn.sendRequest();

			isValid = true;

			return conn;
		} finally {
			if (!isValid && (conn != null)) {
				conn.close();
			}
		}
	}


	/**
	 * Method that allows subclasses to add request headers such as cookies. Default implementation is empty.
	 */
	protected void addRequestHeaders(HessianConnection conn) {
		conn.addHeader("Content-Type", "x-application/hessian");
		conn.addHeader("Accept-Encoding", "deflate");

		String basicAuth = this.factory.getBasicAuth();

		if (basicAuth != null) {
			conn.addHeader("Authorization", basicAuth);
		}
	}

	public WeakHashMap<Method, String> getMangleMap() {
		return this.mangleMap;
	}

	/**
	 * Method that allows subclasses to parse response headers such as cookies. Default implementation is empty.
	 *
	 * @param conn
	 */
	protected void parseResponseHeaders(URLConnection conn) {}

	public Object writeReplace() {
		return new HessianRemote(this.type.getName(), this.url.toString());
	}

	static class ResultInputStream extends InputStream {

		private HessianConnection		_conn;
		private InputStream				_connIs;
		private AbstractHessianInput	_in;
		private InputStream				_hessianIs;

		ResultInputStream(HessianConnection conn, InputStream is, AbstractHessianInput in, InputStream hessianIs) {
			this._conn = conn;
			this._connIs = is;
			this._in = in;
			this._hessianIs = hessianIs;
		}

		@Override
		public int read() throws IOException {
			if (this._hessianIs != null) {
				int value = this._hessianIs.read();

				if (value < 0) {
					this.close();
				}

				return value;
			}
			return -1;
		}

		@Override
		public int read(byte[] buffer, int offset, int length) throws IOException {
			if (this._hessianIs != null) {
				int value = this._hessianIs.read(buffer, offset, length);

				if (value < 0) {
					this.close();
				}

				return value;
			}
			return -1;
		}

		@Override
		public void close() throws IOException {
			HessianConnection conn = this._conn;
			this._conn = null;

			InputStream connIs = this._connIs;
			this._connIs = null;

			AbstractHessianInput in = this._in;
			this._in = null;

			InputStream hessianIs = this._hessianIs;
			this._hessianIs = null;

			try {
				if (hessianIs != null) {
					hessianIs.close();
				}
			} catch (Exception e) {
				HessianProxy.logger.trace(e.toString(), e);
			}

			try {
				if (in != null) {
					in.completeReply();
					in.close();
				}
			} catch (Exception e) {
				HessianProxy.logger.trace(e.toString(), e);
			}

			try {
				if (connIs != null) {
					connIs.close();
				}
			} catch (Exception e) {
				HessianProxy.logger.trace(e.toString(), e);
			}

			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				HessianProxy.logger.trace(e.toString(), e);
			}
		}
	}

}

/*
 * Copyright (c) 2001-2008 Caucho Technology, Inc. All rights reserved. The Apache Software License, Version 1.1 Redistribution and use in source and binary forms, with or without
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

package com.caucho.hessian.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.SerializerFactory;
import com.caucho.services.server.Service;
import com.caucho.services.server.ServiceContext;

/**
 * Servlet for serving Hessian services.
 */
public class HessianServlet extends HttpServlet {

	private static Class<?>				homeAPI;
	private static Object				homeImpl;

	private static Class<?>				objectAPI;
	private static Object				objectImpl;

	private static HessianSkeleton		homeSkeleton;
	private static HessianSkeleton		objectSkeleton;

	private static SerializerFactory	serializerFactory;

	public HessianServlet() {}

	@Override
	public String getServletInfo() {
		return "Hessian Servlet";
	}

	/**
	 * Sets the home api.
	 */
	public static void setHomeAPI(Class<?> api) {
		HessianServlet.homeAPI = api;
	}

	/**
	 * Sets the home implementation
	 */
	public static void setHome(Object home) {
		HessianServlet.homeImpl = home;
	}

	/**
	 * Sets the object api.
	 */
	public static void setObjectAPI(Class<?> api) {
		HessianServlet.objectAPI = api;
	}

	/**
	 * Sets the object implementation
	 */
	public static void setObject(Object object) {
		HessianServlet.objectImpl = object;
	}

	/**
	 * Sets the service class.
	 */
	public static void setService(Object service) {
		HessianServlet.setHome(service);
	}

	/**
	 * Sets the api-class.
	 */
	public static void setAPIClass(Class<?> api) {
		HessianServlet.setHomeAPI(api);
	}

	/**
	 * Gets the api-class.
	 */
	public static Class<?> getAPIClass() {
		return HessianServlet.homeAPI;
	}

	/**
	 * Sets the serializer factory.
	 */
	public static void setSerializerFactory(SerializerFactory factory) {
		HessianServlet.serializerFactory = factory;
	}

	/**
	 * Gets the serializer factory.
	 */
	public SerializerFactory getSerializerFactory() {
		if (HessianServlet.serializerFactory == null) {
			HessianServlet.serializerFactory = new SerializerFactory();
		}

		return HessianServlet.serializerFactory;
	}

	/**
	 * Sets the serializer send collection java type.
	 */
	public void setSendCollectionType(boolean sendType) {
		this.getSerializerFactory().setSendCollectionType(sendType);
	}

	/**
	 * Sets the debugging flag.
	 */
	public void setDebug(boolean isDebug) {}

	/**
	 * Sets the debugging log name.
	 */
	public void setLogName(String name) {
		// _log = logger.getLogger(name);
	}

	/**
	 * Initialize the service, including the service object.
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		try {
			if (HessianServlet.homeImpl != null) {
			} else if (this.getInitParameter("home-class") != null) {
				String className = this.getInitParameter("home-class");

				Class<?> homeClass = this.loadClass(className);

				HessianServlet.homeImpl = homeClass.newInstance();

				this.init(HessianServlet.homeImpl);
			} else if (this.getInitParameter("service-class") != null) {
				String className = this.getInitParameter("service-class");

				Class<?> homeClass = this.loadClass(className);

				HessianServlet.homeImpl = homeClass.newInstance();

				this.init(HessianServlet.homeImpl);
			} else {
				if (this.getClass().equals(HessianServlet.class)) {
					throw new ServletException("server must extend HessianServlet");
				}

				HessianServlet.homeImpl = this;
			}

			if (HessianServlet.homeAPI != null) {
			} else if (this.getInitParameter("home-api") != null) {
				String className = this.getInitParameter("home-api");

				HessianServlet.homeAPI = this.loadClass(className);
			} else if (this.getInitParameter("api-class") != null) {
				String className = this.getInitParameter("api-class");

				HessianServlet.homeAPI = this.loadClass(className);
			} else if (HessianServlet.homeImpl != null) {
				HessianServlet.homeAPI = this.findRemoteAPI(HessianServlet.homeImpl.getClass());

				if (HessianServlet.homeAPI == null) {
					HessianServlet.homeAPI = HessianServlet.homeImpl.getClass();
				}

				HessianServlet.homeAPI = HessianServlet.homeImpl.getClass();
			}

			if (HessianServlet.objectImpl != null) {
			} else if (this.getInitParameter("object-class") != null) {
				String className = this.getInitParameter("object-class");

				Class<?> objectClass = this.loadClass(className);

				HessianServlet.objectImpl = objectClass.newInstance();

				this.init(HessianServlet.objectImpl);
			}

			if (HessianServlet.objectAPI != null) {
			} else if (this.getInitParameter("object-api") != null) {
				String className = this.getInitParameter("object-api");

				HessianServlet.objectAPI = this.loadClass(className);
			} else if (HessianServlet.objectImpl != null) {
				HessianServlet.objectAPI = HessianServlet.objectImpl.getClass();
			}

			HessianServlet.homeSkeleton = new HessianSkeleton(HessianServlet.homeImpl, HessianServlet.homeAPI, null);

			if (HessianServlet.objectAPI != null) {
				HessianServlet.homeSkeleton.setObjectClass(HessianServlet.objectAPI);
			}

			if (HessianServlet.objectImpl != null) {
				HessianServlet.objectSkeleton = new HessianSkeleton(HessianServlet.objectImpl, HessianServlet.objectAPI, null);
				HessianServlet.objectSkeleton.setHomeClass(HessianServlet.homeAPI);
			} else {
				HessianServlet.objectSkeleton = HessianServlet.homeSkeleton;
			}

			if ("true".equals(this.getInitParameter("debug"))) {
			}

			if ("false".equals(this.getInitParameter("send-collection-type"))) {
				this.setSendCollectionType(false);
			}
		} catch (ServletException e) {
			throw e;
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	private Class<?> findRemoteAPI(Class<?> implClass) {
		// hessian/34d0
		return null;

		/*
		 * if (implClass == null || implClass.equals(GenericService.class)) return null; Class []interfaces = implClass.getInterfaces(); if (interfaces.length == 1) return
		 * interfaces[0]; return findRemoteAPI(implClass.getSuperclass());
		 */
	}

	private Class<?> loadClass(String className) throws ClassNotFoundException {
		ClassLoader loader = this.getContextClassLoader();

		if (loader != null) {
			return Class.forName(className, false, loader);
		}
		return Class.forName(className);
	}

	protected ClassLoader getContextClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	private void init(Object service) throws ServletException {
		if (!this.getClass().equals(HessianServlet.class)) {
		} else if (service instanceof Service) {
			((Service) service).init(this.getServletConfig());
		} else if (service instanceof Servlet) {
			((Servlet) service).init(this.getServletConfig());
		}
	}

	/**
	 * Execute a request. The path-info of the request selects the bean. Once the bean's selected, it will be applied.
	 */
	@Override
	public void service(ServletRequest request, ServletResponse response) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		if (!"POST".equals(req.getMethod())) {
			res.setStatus(500); // , "Hessian Requires POST");
			PrintWriter out = res.getWriter();

			res.setContentType("text/html");
			out.println("<h1>Hessian Requires POST</h1>");

			return;
		}

		String serviceId = req.getPathInfo();
		String objectId = req.getParameter("id");
		if (objectId == null) {
			objectId = req.getParameter("ejbid");
		}

		ServiceContext.begin(req, res, serviceId, objectId);

		try {
			InputStream is = request.getInputStream();
			OutputStream os = response.getOutputStream();

			response.setContentType("x-application/hessian");

			SerializerFactory serializerFactory = this.getSerializerFactory();

			this.invoke(is, os, objectId, serializerFactory);
		} catch (RuntimeException e) {
			throw e;
		} catch (ServletException e) {
			throw e;
		} catch (Exception e) {
			throw new ServletException(e);
		} finally {
			ServiceContext.end();
		}
	}

	protected void invoke(InputStream is, OutputStream os, String objectId, SerializerFactory serializerFactory) throws Exception {
		if (objectId != null) {
			HessianServlet.objectSkeleton.invoke(is, os, serializerFactory);
		} else {
			HessianServlet.homeSkeleton.invoke(is, os, serializerFactory);
		}
	}

	protected Hessian2Input createHessian2Input(InputStream is) {
		return new Hessian2Input(is);
	}

	static class LogWriter extends Writer {

		private final Logger		_log;
		private final StringBuilder	_sb	= new StringBuilder();

		LogWriter(Logger log) {
			this._log = log;
		}

		public void write(char ch) {
			if ((ch == '\n') && (this._sb.length() > 0)) {
				this._log.info(this._sb.toString());
				this._sb.setLength(0);
			} else {
				this._sb.append(ch);
			}
		}

		@Override
		public void write(char[] buffer, int offset, int length) {
			for (int i = 0; i < length; i++) {
				char ch = buffer[offset + i];

				if ((ch == '\n') && (this._sb.length() > 0)) {
					this._log.info(this._sb.toString());
					this._sb.setLength(0);
				} else {
					this._sb.append(ch);
				}
			}
		}

		@Override
		public void flush() {}

		@Override
		public void close() {}
	}
}

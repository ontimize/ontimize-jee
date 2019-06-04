/*
 * Copyright (c) 2001-2004 Caucho Technology, Inc. All rights reserved. The Apache Software License, Version 1.1 Redistribution and use in source and binary forms, with or without
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

package com.caucho.services.server;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Proxy class for Hessian services.
 */
abstract public class AbstractSkeleton {

	private final Class<?>					apiClass;
	private Class<?>						homeClass;
	private Class<?>						objectClass;

	private final HashMap<String, Method>	methodMap	= new HashMap<>();

	/**
	 * Create a new hessian skeleton.
	 *
	 * @param apiClass
	 *            the API interface
	 */
	protected AbstractSkeleton(Class<?> apiClass) {
		this.apiClass = apiClass;

		Method[] methodList = apiClass.getMethods();

		for (int i = 0; i < methodList.length; i++) {
			Method method = methodList[i];

			if (this.methodMap.get(method.getName()) == null) {
				this.methodMap.put(method.getName(), methodList[i]);
			}

			Class<?>[] param = method.getParameterTypes();
			String mangledName = method.getName() + "__" + param.length;
			this.methodMap.put(mangledName, methodList[i]);

			this.methodMap.put(AbstractSkeleton.mangleName(method, false), methodList[i]);
		}
	}

	/**
	 * Returns the API class of the current object.
	 */
	public String getAPIClassName() {
		return this.apiClass.getName();
	}

	/**
	 * Returns the API class of the factory/home.
	 */
	public String getHomeClassName() {
		if (this.homeClass != null) {
			return this.homeClass.getName();
		} else {
			return this.getAPIClassName();
		}
	}

	/**
	 * Sets the home API class.
	 */
	public void setHomeClass(Class<?> homeAPI) {
		this.homeClass = homeAPI;
	}

	/**
	 * Returns the API class of the object URLs
	 */
	public String getObjectClassName() {
		if (this.objectClass != null) {
			return this.objectClass.getName();
		} else {
			return this.getAPIClassName();
		}
	}

	/**
	 * Sets the object API class.
	 */
	public void setObjectClass(Class<?> objectAPI) {
		this.objectClass = objectAPI;
	}

	/**
	 * Returns the method by the mangled name.
	 *
	 * @param mangledName
	 *            the name passed by the protocol
	 */
	protected Method getMethod(String mangledName) {
		return this.methodMap.get(mangledName);
	}

	/**
	 * Creates a unique mangled method name based on the method name and the method parameters.
	 *
	 * @param method
	 *            the method to mangle
	 * @param isFull
	 *            if true, mangle the full classname
	 *
	 * @return a mangled string.
	 */
	public static String mangleName(Method method, boolean isFull) {
		StringBuilder sb = new StringBuilder();

		sb.append(method.getName());

		Class<?>[] params = method.getParameterTypes();
		for (int i = 0; i < params.length; i++) {
			sb.append('_');
			sb.append(AbstractSkeleton.mangleClass(params[i], isFull));
		}

		return sb.toString();
	}

	/**
	 * Mangles a classname.
	 */
	public static String mangleClass(Class<?> cl, boolean isFull) {
		String name = cl.getName();

		if ("boolean".equals(name) || name.equals("java.lang.Boolean")) {
			return "boolean";
		} else if ("int".equals(name) || "java.lang.Integer".equals(name) || "short".equals(name) || "java.lang.Short".equals(name) || "byte".equals(name) || "java.lang.Byte"
				.equals(name)) {
			return "int";
		} else if ("long".equals(name) || "java.lang.Long".equals(name)) {
			return "long";
		} else if ("float".equals(name) || "java.lang.Float".equals(name) || "double".equals(name) || "java.lang.Double".equals(name)) {
			return "double";
		} else if ("java.lang.String".equals(name) || "com.caucho.util.CharBuffer".equals(name) || "char".equals(name) || "java.lang.Character".equals(name) || "java.io.Reader"
				.equals(name)) {
			return "string";
		} else if ("java.util.Date".equals(name) || "com.caucho.util.QDate".equals(name)) {
			return "date";
		} else if (InputStream.class.isAssignableFrom(cl) || "[B".equals(name)) {
			return "binary";
		} else if (cl.isArray()) {
			return "[" + AbstractSkeleton.mangleClass(cl.getComponentType(), isFull);
		} else if ("org.w3c.dom.Node".equals(name) || "org.w3c.dom.Element".equals(name) || "org.w3c.dom.Document".equals(name)) {
			return "xml";
		} else if (isFull) {
			return name;
		} else {
			int p = name.lastIndexOf('.');
			if (p > 0) {
				return name.substring(p + 1);
			} else {
				return name;
			}
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[" + this.apiClass.getName() + "]";
	}
}

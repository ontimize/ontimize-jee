/*
 * Copyright (c) 2001-2009 Caucho Technology, Inc. All rights reserved. The Apache Software License, Version 1.1 Redistribution and use in source and binary forms, with or without
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.HessianFactory;
import com.caucho.hessian.io.HessianInputFactory;
import com.caucho.hessian.io.SerializerFactory;
import com.caucho.hessian.util.IExceptionTranslator;
import com.caucho.services.server.AbstractSkeleton;
import com.caucho.services.server.ServiceContext;

/**
 * Proxy class for Hessian services.
 */
public class HessianSkeleton extends AbstractSkeleton {

	private static final Logger			log				= LoggerFactory.getLogger(HessianSkeleton.class);

	private boolean						isDebug;

	private final HessianInputFactory	inputFactory	= new HessianInputFactory();
	private HessianFactory				hessianFactory	= new HessianFactory();

	private Object						service;

	private IExceptionTranslator		exceptionTranslator;

	/**
	 * Create a new hessian skeleton.
	 *
	 * @param service
	 *            the underlying service object.
	 * @param apiClass
	 *            the API interface
	 */
	public HessianSkeleton(Object service, Class<?> apiClass, IExceptionTranslator exceptionTranslator) {
		super(apiClass);
		this.exceptionTranslator = exceptionTranslator;
		if (service == null) {
			service = this;
		}

		this.service = service;

		if (!apiClass.isAssignableFrom(service.getClass())) {
			throw new IllegalArgumentException("Service " + service + " must be an instance of " + apiClass.getName());
		}
		this.exceptionTranslator = exceptionTranslator;
	}

	/**
	 * Create a new hessian skeleton.
	 *
	 * @param service
	 *            the underlying service object.
	 * @param apiClass
	 *            the API interface
	 */
	public HessianSkeleton(Class<?> apiClass, IExceptionTranslator exceptionTranslator) {
		super(apiClass);

	}

	public void setDebug(boolean isDebug) {
		this.isDebug = isDebug;
	}

	public boolean isDebug() {
		return this.isDebug;
	}

	public void setHessianFactory(HessianFactory factory) {
		this.hessianFactory = factory;
	}

	/**
	 * Invoke the object with the request from the input stream.
	 *
	 * @param in
	 *            the Hessian input stream
	 * @param out
	 *            the Hessian output stream
	 */
	public void invoke(InputStream is, OutputStream os) throws Exception {
		this.invoke(is, os, null);
	}

	/**
	 * Invoke the object with the request from the input stream.
	 *
	 * @param in
	 *            the Hessian input stream
	 * @param out
	 *            the Hessian output stream
	 */
	public void invoke(InputStream is, OutputStream os, SerializerFactory serializerFactory) throws Exception {
		boolean isDebug = false;

		HessianInputFactory.HeaderType header = this.inputFactory.readHeader(is);

		AbstractHessianInput in;
		AbstractHessianOutput out;

		switch (header) {
			case CALL_1_REPLY_1:
			case CALL_1_REPLY_2:
				throw new IOException("Invalid protocol version");
			case HESSIAN_2:
				in = this.hessianFactory.createHessian2Input(is);
				in.readCall();
				out = this.hessianFactory.createHessian2Output(os);
				break;

			default:
				throw new IllegalStateException(header + " is an unknown Hessian call");
		}

		if (serializerFactory != null) {
			in.setSerializerFactory(serializerFactory);
			out.setSerializerFactory(serializerFactory);
		}

		try {
			this.invoke(this.service, in, out);
		} finally {
			in.close();
			out.close();

			if (isDebug) {
				os.close();
			}
		}
	}

	/**
	 * Invoke the object with the request from the input stream.
	 *
	 * @param in
	 *            the Hessian input stream
	 * @param out
	 *            the Hessian output stream
	 */
	public void invoke(AbstractHessianInput in, AbstractHessianOutput out) throws Exception {
		this.invoke(this.service, in, out);
	}

	/**
	 * Invoke the object with the request from the input stream.
	 *
	 * @param in
	 *            the Hessian input stream
	 * @param out
	 *            the Hessian output stream
	 */
	public void invoke(Object service, AbstractHessianInput in, AbstractHessianOutput out) throws Exception {
		ServiceContext context = ServiceContext.getContext();

		// backward compatibility for some frameworks that don't read
		// the call type first
		in.skipOptionalCall();

		// Hessian 1.0 backward compatibility
		String header;
		while ((header = in.readHeader()) != null) {
			Object value = in.readObject();

			context.addHeader(header, value);
		}

		String methodName = in.readMethod();
		int argLength = in.readMethodArgLength();

		Method method;

		method = this.getMethod(methodName + "__" + argLength);

		if (method == null) {
			method = this.getMethod(methodName);
		}

		if (method != null) {
		} else if ("_hessian_getAttribute".equals(methodName)) {
			String attrName = in.readString();
			in.completeCall();

			String value = null;

			if ("java.api.class".equals(attrName)) {
				value = this.getAPIClassName();
			} else if ("java.home.class".equals(attrName)) {
				value = this.getHomeClassName();
			} else if ("java.object.class".equals(attrName)) {
				value = this.getObjectClassName();
			}

			out.writeReply(value);
			out.close();
			return;
		} else {
			out.writeFault("NoSuchMethodException", this.escapeMessage("The service has no method named: " + in.getMethod()), null);
			out.close();
			return;
		}

		Class<?>[] args = method.getParameterTypes();

		if ((argLength != args.length) && (argLength >= 0)) {
			out.writeFault("NoSuchMethod", this.escapeMessage("method " + method + " argument length mismatch, received length=" + argLength), null);
			out.close();
			return;
		}

		Object[] values = new Object[args.length];

		for (int i = 0; i < args.length; i++) {
			// XXX: needs Marshal object
			values[i] = in.readObject(args[i]);
		}

		Object result = null;

		boolean success = false;
		String escapeMessage = null;
		try {
			result = method.invoke(service, values);
			success = true;
		} catch (Exception e) {
			Throwable e1 = e;
			if (e1 instanceof InvocationTargetException) {
				e1 = ((InvocationTargetException) e).getTargetException();
			}

			HessianSkeleton.log.info("{} {} ", this, e1.toString(), e1);

			if (this.exceptionTranslator != null) {
				e1 = this.exceptionTranslator.translateException(e1);
			}

			escapeMessage = this.escapeMessage(e1.getMessage());
			out.writeFault("ServiceException", escapeMessage, e1);
			out.close();
			return;
		} finally {
			if (context != null) {
				context.addHeader("Method", method.getName());
				context.addHeader("Values", values);
				if (!success) {
					ServiceContext.getContext().addHeader("ServiceException", escapeMessage);
				}
			}
		}

		// The complete call needs to be after the invoke to handle a
		// trailing InputStream
		in.completeCall();

		out.writeReply(result);

		out.close();
	}

	private String escapeMessage(String msg) {
		if (msg == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();

		int length = msg.length();
		for (int i = 0; i < length; i++) {
			char ch = msg.charAt(i);

			switch (ch) {
				case '<':
					sb.append("&lt;");
					break;
				case '>':
					sb.append("&gt;");
					break;
				case 0x0:
					sb.append("&#00;");
					break;
				case '&':
					sb.append("&amp;");
					break;
				default:
					sb.append(ch);
					break;
			}
		}

		return sb.toString();
	}

}

/*
 * Copyright (c) 2001-2004 Caucho Technology, Inc. All rights reserved. The Apache Software License, Version 1.1 Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 1. Redistributions of source code must retain the above copyright notice, this list of conditions and
 * the following disclaimer. 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution. 3. The end-user documentation included with the redistribution, if any, must include the following acknowlegement: "This
 * product includes software developed by the Caucho Technology (http://www.caucho.com/)." Alternately, this acknowlegement may appear in the software itself, if and wherever such
 * third-party acknowlegements normally appear. 4. The names "Burlap", "Resin", and "Caucho" must not be used to endorse or promote products derived from this software without
 * prior written permission. For written permission, please contact info@caucho.com. 5. Products derived from this software may not be called "Resin" nor may "Resin" appear in
 * their names without prior written permission of Caucho Technology. THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL CAUCHO TECHNOLOGY OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * @author Scott Ferguson
 */

package com.caucho.hessian.io.serializer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.io.AbstractHessianOutput;

/**
 * Serializing an object for known object types.
 */
public class BeanSerializer extends AbstractSerializer {

	private static final Logger	log	= LoggerFactory.getLogger(BeanSerializer.class);

	private final Method[]		methods;
	private final String[]		names;

	private Object				writeReplaceFactory;
	private Method				writeReplace;

	public BeanSerializer(Class<?> cl, ClassLoader loader) {
		this.introspectWriteReplace(cl, loader);

		ArrayList<Method> primitiveMethods = new ArrayList<>();
		ArrayList<Method> compoundMethods = new ArrayList<>();

		for (; cl != null; cl = cl.getSuperclass()) {
			Method[] methods = cl.getDeclaredMethods();

			for (int i = 0; i < methods.length; i++) {
				Method method = methods[i];

				if (Modifier.isStatic(method.getModifiers())) {
					continue;
				}

				if (method.getParameterTypes().length != 0) {
					continue;
				}

				String name = method.getName();

				if (!name.startsWith("get")) {
					continue;
				}

				Class<?> type = method.getReturnType();

				if (type.equals(void.class)) {
					continue;
				}

				if (this.findSetter(methods, name, type) == null) {
					continue;
				}

				// XXX: could parameterize the handler to only deal with public
				method.setAccessible(true);

				if (type.isPrimitive() || (type.getName().startsWith("java.lang.") && !type.equals(Object.class))) {
					primitiveMethods.add(method);
				} else {
					compoundMethods.add(method);
				}
			}
		}

		ArrayList<Method> methodList = new ArrayList<>();
		methodList.addAll(primitiveMethods);
		methodList.addAll(compoundMethods);

		Collections.sort(methodList, new MethodNameCmp());

		this.methods = new Method[methodList.size()];
		methodList.toArray(this.methods);

		this.names = new String[this.methods.length];

		for (int i = 0; i < this.methods.length; i++) {
			String name = this.methods[i].getName();

			name = name.substring(3);

			int j = 0;
			for (; (j < name.length()) && Character.isUpperCase(name.charAt(j)); j++) {
			}

			if (j == 1) {
				name = name.substring(0, j).toLowerCase(Locale.ENGLISH) + name.substring(j);
			} else if (j > 1) {
				name = name.substring(0, j - 1).toLowerCase(Locale.ENGLISH) + name.substring(j - 1);
			}

			this.names[i] = name;
		}
	}

	private void introspectWriteReplace(Class<?> cl, ClassLoader loader) {
		try {
			String className = cl.getName() + "HessianSerializer";

			Class<?> serializerClass = Class.forName(className, false, loader);

			Object serializerObject = serializerClass.newInstance();

			Method writeReplace = this.getWriteReplace(serializerClass, cl);

			if (writeReplace != null) {
				this.writeReplaceFactory = serializerObject;
				this.writeReplace = writeReplace;

				return;
			}
		} catch (ClassNotFoundException e) {
			BeanSerializer.log.trace(null, e);
		} catch (Exception e) {
			BeanSerializer.log.trace(null, e);
		}

		this.writeReplace = this.getWriteReplace(cl);
	}

	/**
	 * Returns the writeReplace method
	 */
	protected Method getWriteReplace(Class<?> cl) {
		for (; cl != null; cl = cl.getSuperclass()) {
			Method[] methods = cl.getDeclaredMethods();

			for (int i = 0; i < methods.length; i++) {
				Method method = methods[i];

				if ("writeReplace".equals(method.getName()) && (method.getParameterTypes().length == 0)) {
					return method;
				}
			}
		}

		return null;
	}

	/**
	 * Returns the writeReplace method
	 */
	protected Method getWriteReplace(Class<?> cl, Class<?> param) {
		for (; cl != null; cl = cl.getSuperclass()) {
			for (Method method : cl.getDeclaredMethods()) {
				if ("writeReplace".equals(method.getName()) && (method.getParameterTypes().length == 1) && param.equals(method.getParameterTypes()[0])) {
					return method;
				}
			}
		}

		return null;
	}

	@Override
	public void writeObject(Object obj, AbstractHessianOutput out) throws IOException {
		if (out.addRef(obj)) {
			return;
		}

		Class<?> cl = obj.getClass();

		try {
			if (this.writeReplace != null) {
				Object repl;

				if (this.writeReplaceFactory != null) {
					repl = this.writeReplace.invoke(this.writeReplaceFactory, obj);
				} else {
					repl = this.writeReplace.invoke(obj);
				}

				// out.removeRef(obj);

				out.writeObject(repl);

				out.replaceRef(repl, obj);

				return;
			}
		} catch (Exception e) {
			BeanSerializer.log.trace(e.toString(), e);
		}

		int ref = out.writeObjectBegin(cl.getName());

		if (ref < -1) {
			// Hessian 1.1 uses a map

			for (int i = 0; i < this.methods.length; i++) {
				Object value = null;

				try {
					value = this.methods[i].invoke(obj, (Object[]) null);
				} catch (Exception e) {
					BeanSerializer.log.info(e.toString(), e);
				}

				out.writeString(this.names[i]);

				out.writeObject(value);
			}

			out.writeMapEnd();
		} else {
			if (ref == -1) {
				out.writeInt(this.names.length);

				for (int i = 0; i < this.names.length; i++) {
					out.writeString(this.names[i]);
				}

				out.writeObjectBegin(cl.getName());
			}

			for (int i = 0; i < this.methods.length; i++) {
				Object value = null;

				try {
					value = this.methods[i].invoke(obj, (Object[]) null);
				} catch (Exception e) {
					BeanSerializer.log.trace(e.toString(), e);
				}

				out.writeObject(value);
			}
		}
	}

	/**
	 * Finds any matching setter.
	 */
	private Method findSetter(Method[] methods, String getterName, Class<?> arg) {
		String setterName = "set" + getterName.substring(3);

		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];

			if (!method.getName().equals(setterName)) {
				continue;
			}

			if (!method.getReturnType().equals(void.class)) {
				continue;
			}

			Class<?>[] params = method.getParameterTypes();

			if ((params.length == 1) && params[0].equals(arg)) {
				return method;
			}
		}

		return null;
	}

	static class MethodNameCmp implements Comparator<Method> {

		@Override
		public int compare(Method a, Method b) {
			return a.getName().compareTo(b.getName());
		}
	}
}

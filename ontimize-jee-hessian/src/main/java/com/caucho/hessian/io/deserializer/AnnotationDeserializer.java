/*
 * Copyright (c) 2001-2008 Caucho Technology, Inc. All rights reserved. The Apache Software License, Version 1.1 Redistribution and use in source and binary forms, with or without
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

package com.caucho.hessian.io.deserializer;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.HashMap;

import com.caucho.hessian.HessianException;
import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.IOExceptionWrapper;

/**
 * Deserializing a java annotation for known object types.
 */
public class AnnotationDeserializer extends AbstractMapDeserializer {

	private final Class<?> annType;

	public AnnotationDeserializer(Class<?> annType) {
		this.annType = annType;
	}

	@Override
	public Class<?> getType() {
		return this.annType;
	}

	@Override
	public Object readMap(AbstractHessianInput in) throws IOException {
		try {
			int ref = in.addRef(null);

			HashMap<String, Object> valueMap = new HashMap<>(8);

			while (!in.isEnd()) {
				String key = in.readString();
				Object value = in.readObject();

				valueMap.put(key, value);
			}

			in.readMapEnd();

			return Proxy.newProxyInstance(this.annType.getClassLoader(), new Class[] { this.annType }, new AnnotationInvocationHandler(this.annType, valueMap));

		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOExceptionWrapper(e);
		}
	}

	@Override
	public Object readObject(AbstractHessianInput in, Object[] fields) throws IOException {
		String[] fieldNames = (String[]) fields;

		try {
			in.addRef(null);

			HashMap<String, Object> valueMap = new HashMap<>(8);

			for (int i = 0; i < fieldNames.length; i++) {
				String name = fieldNames[i];

				valueMap.put(name, in.readObject());
			}

			return Proxy.newProxyInstance(this.annType.getClassLoader(), new Class[] { this.annType }, new AnnotationInvocationHandler(this.annType, valueMap));

		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new HessianException(this.annType.getName() + ":" + e, e);
		}
	}
}

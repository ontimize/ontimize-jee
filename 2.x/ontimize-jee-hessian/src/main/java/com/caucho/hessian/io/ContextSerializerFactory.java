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

package com.caucho.hessian.io;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.HessianException;
import com.caucho.hessian.io.deserializer.AbstractDeserializer;
import com.caucho.hessian.io.deserializer.BasicDeserializer;
import com.caucho.hessian.io.deserializer.Deserializer;
import com.caucho.hessian.io.deserializer.JavaDeserializer;
import com.caucho.hessian.io.deserializer.SqlDateDeserializer;
import com.caucho.hessian.io.deserializer.StackTraceElementDeserializer;
import com.caucho.hessian.io.serializer.AbstractSerializer;
import com.caucho.hessian.io.serializer.BasicSerializer;
import com.caucho.hessian.io.serializer.ByteArraySerializer;
import com.caucho.hessian.io.serializer.ClassSerializer;
import com.caucho.hessian.io.serializer.InetAddressSerializer;
import com.caucho.hessian.io.serializer.Serializer;
import com.caucho.hessian.io.serializer.SqlDateSerializer;

/**
 * The classloader-specific Factory for returning serialization
 */
public class ContextSerializerFactory {

	private static final Logger																log							= LoggerFactory.getLogger(ContextSerializerFactory.class);

	private static final Map<ClassLoader, SoftReference<ContextSerializerFactory>>	CONTEXT_REF_MAP				= new WeakHashMap<>();

	private static final ClassLoader														SYSTEM_CLASS_LOADER;

	private static Map<String, Serializer>											staticSerializerMap;
	private static Map<String, Deserializer>										staticDeserializerMap;
	private static Map<String, Deserializer>										staticClassNameMap;

	private ContextSerializerFactory														parent;
	private final WeakReference<ClassLoader>												loaderRef;

	private final Set<String>														serializerFiles				= new HashSet<>();
	private final Set<String>														deserializerFiles			= new HashSet<>();

	private final Map<String, Serializer>											serializerClassMap			= new HashMap<>();

	private final Map<String, Serializer>											customSerializerMap			= new ConcurrentHashMap<>();

	private final Map<Class<?>, Serializer>											serializerInterfaceMap		= new HashMap<>();

	private final Map<String, Deserializer>											deserializerClassMap		= new HashMap<>();

	private final Map<String, Deserializer>											deserializerClassNameMap	= new HashMap<>();

	private final Map<String, Deserializer>											customDeserializerMap		= new ConcurrentHashMap<>();

	private final Map<Class<?>, Deserializer>										deserializerInterfaceMap	= new HashMap<>();

	public ContextSerializerFactory(ContextSerializerFactory parent, ClassLoader loader) {
		if (loader == null) {
			loader = ContextSerializerFactory.SYSTEM_CLASS_LOADER;
		}

		this.loaderRef = new WeakReference<>(loader);

		this.init();
	}

	public static ContextSerializerFactory create() {
		return ContextSerializerFactory.create(Thread.currentThread().getContextClassLoader());
	}

	public static ContextSerializerFactory create(ClassLoader loader) {
		synchronized (ContextSerializerFactory.CONTEXT_REF_MAP) {
			SoftReference<ContextSerializerFactory> factoryRef = ContextSerializerFactory.CONTEXT_REF_MAP.get(loader);

			ContextSerializerFactory factory = null;

			if (factoryRef != null) {
				factory = factoryRef.get();
			}

			if (factory == null) {
				ContextSerializerFactory parent = null;

				if (loader != null) {
					parent = ContextSerializerFactory.create(loader.getParent());
				}

				factory = new ContextSerializerFactory(parent, loader);
				factoryRef = new SoftReference<>(factory);

				ContextSerializerFactory.CONTEXT_REF_MAP.put(loader, factoryRef);
			}

			return factory;
		}
	}

	public ClassLoader getClassLoader() {
		WeakReference<ClassLoader> loaderRef = this.loaderRef;

		if (loaderRef != null) {
			return loaderRef.get();
		} else {
			return null;
		}
	}

	/**
	 * Returns the serializer for a given class.
	 */
	public Serializer getSerializer(String className) {
		Serializer serializer = this.serializerClassMap.get(className);

		if (serializer == AbstractSerializer.NULL) {
			return null;
		} else {
			return serializer;
		}
	}

	/**
	 * Returns a custom serializer the class
	 *
	 * @param cl
	 *            the class of the object that needs to be serialized.
	 *
	 * @return a serializer object for the serialization.
	 */
	public Serializer getCustomSerializer(Class<?> cl) {
		Serializer serializer = this.customSerializerMap.get(cl.getName());

		if (serializer == AbstractSerializer.NULL) {
			return null;
		} else if (serializer != null) {
			return serializer;
		}

		try {
			Class<?> serClass = Class.forName(cl.getName() + "HessianSerializer", false, cl.getClassLoader());

			Serializer ser = (Serializer) serClass.newInstance();

			this.customSerializerMap.put(cl.getName(), ser);

			return ser;
		} catch (ClassNotFoundException e) {
			ContextSerializerFactory.log.trace(null, e);
		} catch (Exception e) {
			throw new HessianException(e);
		}

		this.customSerializerMap.put(cl.getName(), AbstractSerializer.NULL);

		return null;
	}

	/**
	 * Returns the deserializer for a given class.
	 */
	public Deserializer getDeserializer(String className) {
		Deserializer deserializer = this.deserializerClassMap.get(className);

		if (deserializer == AbstractDeserializer.NULL) {
			return null;
		} else {
			return deserializer;
		}
	}

	/**
	 * Returns a custom deserializer the class
	 *
	 * @param cl
	 *            the class of the object that needs to be deserialized.
	 *
	 * @return a deserializer object for the deserialization.
	 */
	public Deserializer getCustomDeserializer(Class<?> cl) {
		Deserializer deserializer = this.customDeserializerMap.get(cl.getName());

		if (deserializer == AbstractDeserializer.NULL) {
			return null;
		} else if (deserializer != null) {
			return deserializer;
		}

		try {
			Class<?> serClass = Class.forName(cl.getName() + "HessianDeserializer", false, cl.getClassLoader());

			Deserializer ser = (Deserializer) serClass.newInstance();

			this.customDeserializerMap.put(cl.getName(), ser);

			return ser;
		} catch (ClassNotFoundException e) {
			ContextSerializerFactory.log.trace(null, e);
		} catch (Exception e) {
			throw new HessianException(e);
		}

		this.customDeserializerMap.put(cl.getName(), AbstractDeserializer.NULL);

		return null;
	}

	/**
	 * Initialize the factory
	 */
	private void init() {
		if (this.parent != null) {
			this.serializerFiles.addAll(this.parent.serializerFiles);
			this.deserializerFiles.addAll(this.parent.deserializerFiles);

			this.serializerClassMap.putAll(this.parent.serializerClassMap);
			this.deserializerClassMap.putAll(this.parent.deserializerClassMap);
		}

		if (this.parent == null) {
			this.serializerClassMap.putAll(ContextSerializerFactory.staticSerializerMap);
			this.deserializerClassMap.putAll(ContextSerializerFactory.staticDeserializerMap);
			this.deserializerClassNameMap.putAll(ContextSerializerFactory.staticClassNameMap);
		}

		HashMap<Class<?>, Class<?>> classMap;

		classMap = new HashMap<>();
		this.initSerializerFiles("META-INF/hessian/serializers", this.serializerFiles, classMap, Serializer.class);

		for (Map.Entry<Class<?>, Class<?>> entry : classMap.entrySet()) {
			try {
				Serializer ser = (Serializer) entry.getValue().newInstance();

				if (entry.getKey().isInterface()) {
					this.serializerInterfaceMap.put(entry.getKey(), ser);
				} else {
					this.serializerClassMap.put(entry.getKey().getName(), ser);
				}
			} catch (Exception e) {
				throw new HessianException(e);
			}
		}

		classMap = new HashMap<>();
		this.initSerializerFiles("META-INF/hessian/deserializers", this.deserializerFiles, classMap, Deserializer.class);

		for (Map.Entry<Class<?>, Class<?>> entry : classMap.entrySet()) {
			try {
				Deserializer ser = (Deserializer) entry.getValue().newInstance();

				if (entry.getKey().isInterface()) {
					this.deserializerInterfaceMap.put(entry.getKey(), ser);
				} else {
					this.deserializerClassMap.put(entry.getKey().getName(), ser);
				}
			} catch (Exception e) {
				throw new HessianException(e);
			}
		}
	}

	private void initSerializerFiles(String fileName, Set<String> fileList, Map<Class<?>, Class<?>> classMap, Class<?> type) {
		try {
			ClassLoader classLoader = this.getClassLoader();

			// on systems with the security manager enabled, the system classloader
			// is null
			if (classLoader == null) {
				return;
			}

			Enumeration<URL> iter = classLoader.getResources(fileName);
			while (iter.hasMoreElements()) {
				URL url = iter.nextElement();

				if (fileList.contains(url.toString())) {
					continue;
				}

				fileList.add(url.toString());

				InputStream is = null;
				try {
					is = url.openStream();

					Properties props = new Properties();
					props.load(is);

					for (Map.Entry<Object, Object> entry : props.entrySet()) {
						String apiName = (String) entry.getKey();
						String serializerName = (String) entry.getValue();

						Class<?> apiClass = null;
						Class<?> serializerClass = null;

						try {
							apiClass = Class.forName(apiName, false, classLoader);
						} catch (ClassNotFoundException e) {
							ContextSerializerFactory.log.info("{}: {} is not available in this context: {}", url, apiName, this.getClassLoader(), e);
							continue;
						}

						try {
							serializerClass = Class.forName(serializerName, false, classLoader);
						} catch (ClassNotFoundException e) {
							ContextSerializerFactory.log.info("{}: {} is not available in this context: {}", url, serializerName, this.getClassLoader(), e);
							continue;
						}

						if (!type.isAssignableFrom(serializerClass)) {
							throw new HessianException(url + ": " + serializerClass.getName() + " is invalid because it does not implement " + type.getName());
						}

						classMap.put(apiClass, serializerClass);
					}
				} finally {
					if (is != null) {
						is.close();
					}
				}
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new HessianException(e);
		}
	}

	private static void addBasic(Class<?> cl, String typeName, int type) {
		ContextSerializerFactory.staticSerializerMap.put(cl.getName(), new BasicSerializer(type));

		Deserializer deserializer = new BasicDeserializer(type);
		ContextSerializerFactory.staticDeserializerMap.put(cl.getName(), deserializer);
		ContextSerializerFactory.staticClassNameMap.put(typeName, deserializer);
	}

	static {
		ContextSerializerFactory.staticSerializerMap = new HashMap<>();
		ContextSerializerFactory.staticDeserializerMap = new HashMap<>();
		ContextSerializerFactory.staticClassNameMap = new HashMap<>();

		ContextSerializerFactory.addBasic(void.class, "void", BasicSerializer.NULL);

		ContextSerializerFactory.addBasic(Boolean.class, "boolean", BasicSerializer.BOOLEAN);
		ContextSerializerFactory.addBasic(Byte.class, "byte", BasicSerializer.BYTE);
		ContextSerializerFactory.addBasic(Short.class, "short", BasicSerializer.SHORT);
		ContextSerializerFactory.addBasic(Integer.class, "int", BasicSerializer.INTEGER);
		ContextSerializerFactory.addBasic(Long.class, "long", BasicSerializer.LONG);
		ContextSerializerFactory.addBasic(Float.class, "float", BasicSerializer.FLOAT);
		ContextSerializerFactory.addBasic(Double.class, "double", BasicSerializer.DOUBLE);
		ContextSerializerFactory.addBasic(Character.class, "char", BasicSerializer.CHARACTER_OBJECT);
		ContextSerializerFactory.addBasic(String.class, "string", BasicSerializer.STRING);
		ContextSerializerFactory.addBasic(Object.class, "object", BasicSerializer.OBJECT);
		ContextSerializerFactory.addBasic(java.util.Date.class, "date", BasicSerializer.DATE);

		ContextSerializerFactory.addBasic(boolean.class, "boolean", BasicSerializer.BOOLEAN);
		ContextSerializerFactory.addBasic(byte.class, "byte", BasicSerializer.BYTE);
		ContextSerializerFactory.addBasic(short.class, "short", BasicSerializer.SHORT);
		ContextSerializerFactory.addBasic(int.class, "int", BasicSerializer.INTEGER);
		ContextSerializerFactory.addBasic(long.class, "long", BasicSerializer.LONG);
		ContextSerializerFactory.addBasic(float.class, "float", BasicSerializer.FLOAT);
		ContextSerializerFactory.addBasic(double.class, "double", BasicSerializer.DOUBLE);
		ContextSerializerFactory.addBasic(char.class, "char", BasicSerializer.CHARACTER);

		ContextSerializerFactory.addBasic(boolean[].class, "[boolean", BasicSerializer.BOOLEAN_ARRAY);
		ContextSerializerFactory.addBasic(byte[].class, "[byte", BasicSerializer.BYTE_ARRAY);
		ContextSerializerFactory.staticSerializerMap.put(byte[].class.getName(), ByteArraySerializer.SER);
		ContextSerializerFactory.addBasic(short[].class, "[short", BasicSerializer.SHORT_ARRAY);
		ContextSerializerFactory.addBasic(int[].class, "[int", BasicSerializer.INTEGER_ARRAY);
		ContextSerializerFactory.addBasic(long[].class, "[long", BasicSerializer.LONG_ARRAY);
		ContextSerializerFactory.addBasic(float[].class, "[float", BasicSerializer.FLOAT_ARRAY);
		ContextSerializerFactory.addBasic(double[].class, "[double", BasicSerializer.DOUBLE_ARRAY);
		ContextSerializerFactory.addBasic(char[].class, "[char", BasicSerializer.CHARACTER_ARRAY);
		ContextSerializerFactory.addBasic(String[].class, "[string", BasicSerializer.STRING_ARRAY);
		ContextSerializerFactory.addBasic(Object[].class, "[object", BasicSerializer.OBJECT_ARRAY);

		Deserializer objectDeserializer = new JavaDeserializer(Object.class);
		ContextSerializerFactory.staticDeserializerMap.put("object", objectDeserializer);
		ContextSerializerFactory.staticClassNameMap.put("object", objectDeserializer);

		ContextSerializerFactory.staticSerializerMap.put(Class.class.getName(), new ClassSerializer());

		ContextSerializerFactory.staticDeserializerMap.put(Number.class.getName(), new BasicDeserializer(BasicSerializer.NUMBER));

		/*
		 * for (Class cl : new Class[] { BigDecimal.class, File.class, ObjectName.class }) { _staticSerializerMap.put(cl, StringValueSerializer.SER); _staticDeserializerMap.put(cl,
		 * new StringValueDeserializer(cl)); } _staticSerializerMap.put(ObjectName.class, StringValueSerializer.SER); try { _staticDeserializerMap.put(ObjectName.class, new
		 * StringValueDeserializer(ObjectName.class)); } catch (Throwable e) { }
		 */

		ContextSerializerFactory.staticSerializerMap.put(InetAddress.class.getName(), InetAddressSerializer.create());

		ContextSerializerFactory.staticSerializerMap.put(java.sql.Date.class.getName(), new SqlDateSerializer());
		ContextSerializerFactory.staticSerializerMap.put(java.sql.Time.class.getName(), new SqlDateSerializer());
		ContextSerializerFactory.staticSerializerMap.put(java.sql.Timestamp.class.getName(), new SqlDateSerializer());

		ContextSerializerFactory.staticDeserializerMap.put(java.sql.Date.class.getName(), new SqlDateDeserializer(java.sql.Date.class));
		ContextSerializerFactory.staticDeserializerMap.put(java.sql.Time.class.getName(), new SqlDateDeserializer(java.sql.Time.class));
		ContextSerializerFactory.staticDeserializerMap.put(java.sql.Timestamp.class.getName(), new SqlDateDeserializer(java.sql.Timestamp.class));

		// hessian/3bb5
		ContextSerializerFactory.staticDeserializerMap.put(StackTraceElement.class.getName(), new StackTraceElementDeserializer());

		ClassLoader systemClassLoader = null;
		try {
			systemClassLoader = ClassLoader.getSystemClassLoader();
		} catch (Exception e) {
			ContextSerializerFactory.log.trace(null, e);
		}

		SYSTEM_CLASS_LOADER = systemClassLoader;
	}
}

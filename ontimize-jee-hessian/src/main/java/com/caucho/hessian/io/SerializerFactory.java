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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.io.deserializer.AnnotationDeserializer;
import com.caucho.hessian.io.deserializer.ArrayDeserializer;
import com.caucho.hessian.io.deserializer.BasicDeserializer;
import com.caucho.hessian.io.deserializer.ClassDeserializer;
import com.caucho.hessian.io.deserializer.CollectionDeserializer;
import com.caucho.hessian.io.deserializer.Deserializer;
import com.caucho.hessian.io.deserializer.EnumDeserializer;
import com.caucho.hessian.io.deserializer.EnumerationDeserializer;
import com.caucho.hessian.io.deserializer.InputStreamDeserializer;
import com.caucho.hessian.io.deserializer.IteratorDeserializer;
import com.caucho.hessian.io.deserializer.JavaDeserializer;
import com.caucho.hessian.io.deserializer.MapDeserializer;
import com.caucho.hessian.io.deserializer.ObjectDeserializer;
import com.caucho.hessian.io.deserializer.RemoteDeserializer;
import com.caucho.hessian.io.deserializer.UnsafeDeserializer;
import com.caucho.hessian.io.serializer.AnnotationSerializer;
import com.caucho.hessian.io.serializer.ArraySerializer;
import com.caucho.hessian.io.serializer.BasicSerializer;
import com.caucho.hessian.io.serializer.CalendarSerializer;
import com.caucho.hessian.io.serializer.CollectionSerializer;
import com.caucho.hessian.io.serializer.EnumSerializer;
import com.caucho.hessian.io.serializer.EnumerationSerializer;
import com.caucho.hessian.io.serializer.InetAddressSerializer;
import com.caucho.hessian.io.serializer.InputStreamSerializer;
import com.caucho.hessian.io.serializer.IteratorSerializer;
import com.caucho.hessian.io.serializer.JavaSerializer;
import com.caucho.hessian.io.serializer.MapSerializer;
import com.caucho.hessian.io.serializer.ObjectSerializer;
import com.caucho.hessian.io.serializer.RemoteSerializer;
import com.caucho.hessian.io.serializer.Serializer;
import com.caucho.hessian.io.serializer.ThrowableSerializer;
import com.caucho.hessian.io.serializer.UnsafeSerializer;
import com.caucho.hessian.io.serializer.WriteReplaceSerializer;

/**
 * Factory for returning serialization methods.
 */
public class SerializerFactory extends AbstractSerializerFactory {

	private static final Logger														log							= LoggerFactory.getLogger(SerializerFactory.class);

	private static final ClassLoader												SYSTEM_CLASSLOADER;

	private static final HashMap<String, Deserializer>								STATIC_TYPE_MAP;

	private static final WeakHashMap<ClassLoader, SoftReference<SerializerFactory>>	DEFAULT_FACTORY_REF_MAP		= new WeakHashMap<>();

	private final ContextSerializerFactory											contextFactory;
	private final WeakReference<ClassLoader>										loaderRef;

	protected Serializer															defaultSerializer;

	// Additional factories
	protected ArrayList<AbstractSerializerFactory>									factories					= new ArrayList<>();

	protected CollectionSerializer													collectionSerializer;
	protected MapSerializer															mapSerializer;

	private Deserializer															hashMapDeserializer;
	private Deserializer															arrayListDeserializer;
	private ConcurrentHashMap<Class<?>, Serializer>									cachedSerializerMap;
	private ConcurrentHashMap<Class<?>, Deserializer>								cachedDeserializerMap;
	private HashMap<String, Deserializer>											cachedTypeDeserializerMap;

	private boolean																	isAllowNonSerializable;
	private final boolean															isEnableUnsafeSerializer	= UnsafeSerializer.isEnabled() && UnsafeDeserializer.isEnabled();

	public SerializerFactory() {
		this(Thread.currentThread().getContextClassLoader());
	}

	public SerializerFactory(ClassLoader loader) {
		this.loaderRef = new WeakReference<>(loader);

		this.contextFactory = ContextSerializerFactory.create(loader);
	}

	public static SerializerFactory createDefault() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();

		synchronized (SerializerFactory.DEFAULT_FACTORY_REF_MAP) {
			SoftReference<SerializerFactory> factoryRef = SerializerFactory.DEFAULT_FACTORY_REF_MAP.get(loader);

			SerializerFactory factory = null;

			if (factoryRef != null) {
				factory = factoryRef.get();
			}

			if (factory == null) {
				factory = new SerializerFactory();

				factoryRef = new SoftReference<>(factory);

				SerializerFactory.DEFAULT_FACTORY_REF_MAP.put(loader, factoryRef);
			}

			return factory;
		}
	}

	public ClassLoader getClassLoader() {
		return this.loaderRef.get();
	}

	/**
	 * Set true if the collection serializer should send the java type.
	 */
	public void setSendCollectionType(boolean isSendType) {
		if (this.collectionSerializer == null) {
			this.collectionSerializer = new CollectionSerializer();
		}

		this.collectionSerializer.setSendJavaType(isSendType);

		if (this.mapSerializer == null) {
			this.mapSerializer = new MapSerializer();
		}

		this.mapSerializer.setSendJavaType(isSendType);
	}

	/**
	 * Adds a factory.
	 */
	public void addFactory(AbstractSerializerFactory factory) {
		this.factories.add(factory);
	}

	/**
	 * If true, non-serializable objects are allowed.
	 */
	public void setAllowNonSerializable(boolean allow) {
		this.isAllowNonSerializable = allow;
	}

	/**
	 * If true, non-serializable objects are allowed.
	 */
	public boolean isAllowNonSerializable() {
		return this.isAllowNonSerializable;
	}

	/**
	 * Returns the serializer for a class.
	 *
	 * @param cl
	 *            the class of the object that needs to be serialized.
	 *
	 * @return a serializer object for the serialization.
	 */
	public Serializer getObjectSerializer(Class<?> cl) throws HessianProtocolException {
		Serializer serializer = this.getSerializer(cl);

		if (serializer instanceof ObjectSerializer) {
			return ((ObjectSerializer) serializer).getObjectSerializer();
		}
		return serializer;
	}

	/**
	 * Returns the serializer for a class.
	 *
	 * @param cl
	 *            the class of the object that needs to be serialized.
	 *
	 * @return a serializer object for the serialization.
	 */
	@Override
	public Serializer getSerializer(Class<?> cl) throws HessianProtocolException {
		Serializer serializer;

		if (this.cachedSerializerMap != null) {
			serializer = this.cachedSerializerMap.get(cl);

			if (serializer != null) {
				return serializer;
			}
		}

		serializer = this.loadSerializer(cl);

		if (this.cachedSerializerMap == null) {
			this.cachedSerializerMap = new ConcurrentHashMap<>(8);
		}

		this.cachedSerializerMap.put(cl, serializer);

		return serializer;
	}

	protected Serializer loadSerializer(Class<?> cl) throws HessianProtocolException {
		Serializer serializer = null;

		for (int i = 0; (this.factories != null) && (i < this.factories.size()); i++) {
			AbstractSerializerFactory factory;

			factory = this.factories.get(i);

			serializer = factory.getSerializer(cl);

			if (serializer != null) {
				return serializer;
			}
		}

		serializer = this.contextFactory.getSerializer(cl.getName());

		if (serializer != null) {
			return serializer;
		}

		ClassLoader loader = cl.getClassLoader();

		if (loader == null) {
			loader = SerializerFactory.SYSTEM_CLASSLOADER;
		}

		ContextSerializerFactory factory = null;

		factory = ContextSerializerFactory.create(loader);

		serializer = factory.getCustomSerializer(cl);

		if (serializer != null) {
			return serializer;
		}

		if (HessianRemoteObject.class.isAssignableFrom(cl)) {
			return new RemoteSerializer();
		} else if (InetAddress.class.isAssignableFrom(cl)) {
			return InetAddressSerializer.create();
		} else if (JavaSerializer.getWriteReplace(cl) != null) {
			Serializer baseSerializer = this.getDefaultSerializer(cl);

			return new WriteReplaceSerializer(cl, this.getClassLoader(), baseSerializer);
		} else if (Map.class.isAssignableFrom(cl)) {
			if (this.mapSerializer == null) {
				this.mapSerializer = new MapSerializer();
			}
			return this.mapSerializer;
		} else if (Collection.class.isAssignableFrom(cl)) {
			if (this.collectionSerializer == null) {
				this.collectionSerializer = new CollectionSerializer();
			}
			return this.collectionSerializer;
		} else if (cl.isArray()) {
			return new ArraySerializer();
		} else if (Throwable.class.isAssignableFrom(cl)) {
			return new ThrowableSerializer(cl, this.getClassLoader());
		} else if (InputStream.class.isAssignableFrom(cl)) {
			return new InputStreamSerializer();
		} else if (Iterator.class.isAssignableFrom(cl)) {
			return IteratorSerializer.create();
		} else if (Calendar.class.isAssignableFrom(cl)) {
			return CalendarSerializer.SER;
		} else if (Enumeration.class.isAssignableFrom(cl)) {
			return EnumerationSerializer.create();
		} else if (Enum.class.isAssignableFrom(cl)) {
			return new EnumSerializer(cl);
		} else if (Annotation.class.isAssignableFrom(cl)) {
			return new AnnotationSerializer(cl);
		}

		return this.getDefaultSerializer(cl);
	}

	/**
	 * Returns the default serializer for a class that isn't matched directly. Application can override this method to produce bean-style serialization instead of field
	 * serialization.
	 *
	 * @param cl
	 *            the class of the object that needs to be serialized.
	 *
	 * @return a serializer object for the serialization.
	 */
	protected Serializer getDefaultSerializer(Class<?> cl) {
		if (this.defaultSerializer != null) {
			return this.defaultSerializer;
		}

		if (!Serializable.class.isAssignableFrom(cl) && !this.isAllowNonSerializable) {
			throw new IllegalStateException("Serialized class " + cl.getName() + " must implement java.io.Serializable");
		}

		if (this.isEnableUnsafeSerializer && (JavaSerializer.getWriteReplace(cl) == null)) {
			return UnsafeSerializer.create(cl);
		}
		return JavaSerializer.create(cl);
	}

	/**
	 * Returns the deserializer for a class.
	 *
	 * @param cl
	 *            the class of the object that needs to be deserialized.
	 *
	 * @return a deserializer object for the serialization.
	 */
	@Override
	public Deserializer getDeserializer(Class<?> cl) throws HessianProtocolException {
		Deserializer deserializer;

		if (this.cachedDeserializerMap != null) {
			deserializer = this.cachedDeserializerMap.get(cl);

			if (deserializer != null) {
				return deserializer;
			}
		}

		deserializer = this.loadDeserializer(cl);

		if (this.cachedDeserializerMap == null) {
			this.cachedDeserializerMap = new ConcurrentHashMap<>(8);
		}

		this.cachedDeserializerMap.put(cl, deserializer);

		return deserializer;
	}

	protected Deserializer loadDeserializer(Class<?> cl) throws HessianProtocolException {
		Deserializer deserializer = null;

		for (int i = 0; (deserializer == null) && (this.factories != null) && (i < this.factories.size()); i++) {
			AbstractSerializerFactory factory;
			factory = this.factories.get(i);

			deserializer = factory.getDeserializer(cl);
		}

		if (deserializer != null) {
			return deserializer;
		}

		// XXX: need test
		deserializer = this.contextFactory.getDeserializer(cl.getName());

		if (deserializer != null) {
			return deserializer;
		}

		ContextSerializerFactory factory = null;

		if (cl.getClassLoader() != null) {
			factory = ContextSerializerFactory.create(cl.getClassLoader());
		} else {
			factory = ContextSerializerFactory.create(SerializerFactory.SYSTEM_CLASSLOADER);
		}

		deserializer = factory.getCustomDeserializer(cl);

		if (deserializer != null) {
			return deserializer;
		}

		if (Collection.class.isAssignableFrom(cl)) {
			deserializer = new CollectionDeserializer(cl);
		} else if (Map.class.isAssignableFrom(cl)) {
			deserializer = new MapDeserializer(cl);
		} else if (Iterator.class.isAssignableFrom(cl)) {
			deserializer = IteratorDeserializer.create();
		} else if (Annotation.class.isAssignableFrom(cl)) {
			deserializer = new AnnotationDeserializer(cl);
		} else if (cl.isInterface()) {
			deserializer = new ObjectDeserializer(cl);
		} else if (cl.isArray()) {
			deserializer = new ArrayDeserializer(cl.getComponentType());
		} else if (Enumeration.class.isAssignableFrom(cl)) {
			deserializer = EnumerationDeserializer.create();
		} else if (Enum.class.isAssignableFrom(cl)) {
			deserializer = new EnumDeserializer(cl);
		} else if (Class.class.equals(cl)) {
			deserializer = new ClassDeserializer(this.getClassLoader());
		} else {
			deserializer = this.getDefaultDeserializer(cl);
		}

		return deserializer;
	}

	/**
	 * Returns a custom serializer the class
	 *
	 * @param cl
	 *            the class of the object that needs to be serialized.
	 *
	 * @return a serializer object for the serialization.
	 */
	protected Deserializer getCustomDeserializer(Class<?> cl) {
		try {
			Class<?> serClass = Class.forName(cl.getName() + "HessianDeserializer", false, cl.getClassLoader());
			Deserializer ser = (Deserializer) serClass.newInstance();
			return ser;
		} catch (ClassNotFoundException e) {
			SerializerFactory.log.trace(e.toString(), e);
			return null;
		} catch (Exception e) {
			SerializerFactory.log.info(e.toString(), e);
			return null;
		}
	}

	/**
	 * Returns the default serializer for a class that isn't matched directly. Application can override this method to produce bean-style serialization instead of field
	 * serialization.
	 *
	 * @param cl
	 *            the class of the object that needs to be serialized.
	 *
	 * @return a serializer object for the serialization.
	 */
	protected Deserializer getDefaultDeserializer(Class<?> cl) {
		if (InputStream.class.equals(cl)) {
			return InputStreamDeserializer.DESER;
		}

		if (this.isEnableUnsafeSerializer) {
			return new UnsafeDeserializer(cl);
		}
		return new JavaDeserializer(cl);
	}

	/**
	 * Reads the object as a list.
	 */
	public Object readList(AbstractHessianInput in, int length, String type) throws HessianProtocolException, IOException {
		Deserializer deserializer = this.getDeserializer(type);

		if (deserializer != null) {
			return deserializer.readList(in, length);
		}
		return new CollectionDeserializer(ArrayList.class).readList(in, length);
	}

	/**
	 * Reads the object as a map.
	 */
	public Object readMap(AbstractHessianInput in, String type) throws HessianProtocolException, IOException {
		Deserializer deserializer = this.getDeserializer(type);

		if (deserializer != null) {
			return deserializer.readMap(in);
		} else if (this.hashMapDeserializer != null) {
			return this.hashMapDeserializer.readMap(in);
		} else {
			this.hashMapDeserializer = new MapDeserializer(HashMap.class);

			return this.hashMapDeserializer.readMap(in);
		}
	}

	/**
	 * Reads the object as a map.
	 */
	public Object readObject(AbstractHessianInput in, String type, String[] fieldNames) throws HessianProtocolException, IOException {
		Deserializer deserializer = this.getDeserializer(type);

		if (deserializer != null) {
			return deserializer.readObject(in, fieldNames);
		} else if (this.hashMapDeserializer != null) {
			return this.hashMapDeserializer.readObject(in, fieldNames);
		} else {
			this.hashMapDeserializer = new MapDeserializer(HashMap.class);

			return this.hashMapDeserializer.readObject(in, fieldNames);
		}
	}

	/**
	 * Reads the object as a map.
	 */
	public Deserializer getObjectDeserializer(String type, Class<?> cl) throws HessianProtocolException {
		Deserializer reader = this.getObjectDeserializer(type);

		if ((cl == null) || cl.equals(reader.getType()) || cl.isAssignableFrom(reader.getType()) || reader.isReadResolve() || HessianHandle.class
				.isAssignableFrom(reader.getType())) {
			return reader;
		}

		SerializerFactory.log.info("hessian: expected deserializer '{}' at '{}' ({})", cl.getName(), type, reader.getType().getName());

		return this.getDeserializer(cl);
	}

	/**
	 * Reads the object as a map.
	 */
	public Deserializer getObjectDeserializer(String type) throws HessianProtocolException {
		Deserializer deserializer = this.getDeserializer(type);

		if (deserializer != null) {
			return deserializer;
		} else if (this.hashMapDeserializer != null) {
			return this.hashMapDeserializer;
		} else {
			this.hashMapDeserializer = new MapDeserializer(HashMap.class);

			return this.hashMapDeserializer;
		}
	}

	/**
	 * Reads the object as a map.
	 */
	public Deserializer getListDeserializer(String type, Class<?> cl) throws HessianProtocolException {
		Deserializer reader = this.getListDeserializer(type);

		if ((cl == null) || cl.equals(reader.getType()) || cl.isAssignableFrom(reader.getType())) {
			return reader;
		}

		SerializerFactory.log.info("hessian: expected '{}' at '{}' ({})", cl.getName(), type, reader.getType().getName());

		return this.getDeserializer(cl);
	}

	/**
	 * Reads the object as a map.
	 */
	public Deserializer getListDeserializer(String type) throws HessianProtocolException {
		Deserializer deserializer = this.getDeserializer(type);

		if (deserializer != null) {
			return deserializer;
		} else if (this.arrayListDeserializer != null) {
			return this.arrayListDeserializer;
		} else {
			this.arrayListDeserializer = new CollectionDeserializer(ArrayList.class);

			return this.arrayListDeserializer;
		}
	}

	/**
	 * Returns a deserializer based on a string type.
	 */
	public Deserializer getDeserializer(String type) throws HessianProtocolException {
		if ((type == null) || "".equals(type)) {
			return null;
		}

		Deserializer deserializer;

		if (this.cachedTypeDeserializerMap != null) {
			synchronized (this.cachedTypeDeserializerMap) {
				deserializer = this.cachedTypeDeserializerMap.get(type);
			}

			if (deserializer != null) {
				return deserializer;
			}
		}

		deserializer = SerializerFactory.STATIC_TYPE_MAP.get(type);
		if (deserializer != null) {
			return deserializer;
		}

		if (type.startsWith("[")) {
			Deserializer subDeserializer = this.getDeserializer(type.substring(1));

			if (subDeserializer != null) {
				deserializer = new ArrayDeserializer(subDeserializer.getType());
			} else {
				deserializer = new ArrayDeserializer(Object.class);
			}
		} else {
			try {
				Class<?> cl = Class.forName(type, false, this.getClassLoader());
				deserializer = this.getDeserializer(cl);
			} catch (Exception e) {
				SerializerFactory.log.warn("Hessian/Burlap: '{}' is an unknown class in {}:\n", type, this.getClassLoader(), e);
			}
		}

		if (deserializer != null) {
			if (this.cachedTypeDeserializerMap == null) {
				this.cachedTypeDeserializerMap = new HashMap<>(8);
			}

			synchronized (this.cachedTypeDeserializerMap) {
				this.cachedTypeDeserializerMap.put(type, deserializer);
			}
		}

		return deserializer;
	}

	private static void addBasic(Class<?> cl, String typeName, int type) {
		Deserializer deserializer = new BasicDeserializer(type);

		SerializerFactory.STATIC_TYPE_MAP.put(typeName, deserializer);
	}

	static {
		STATIC_TYPE_MAP = new HashMap<>();

		SerializerFactory.addBasic(void.class, "void", BasicSerializer.NULL);

		SerializerFactory.addBasic(Boolean.class, "boolean", BasicSerializer.BOOLEAN);
		SerializerFactory.addBasic(Byte.class, "byte", BasicSerializer.BYTE);
		SerializerFactory.addBasic(Short.class, "short", BasicSerializer.SHORT);
		SerializerFactory.addBasic(Integer.class, "int", BasicSerializer.INTEGER);
		SerializerFactory.addBasic(Long.class, "long", BasicSerializer.LONG);
		SerializerFactory.addBasic(Float.class, "float", BasicSerializer.FLOAT);
		SerializerFactory.addBasic(Double.class, "double", BasicSerializer.DOUBLE);
		SerializerFactory.addBasic(Character.class, "char", BasicSerializer.CHARACTER_OBJECT);
		SerializerFactory.addBasic(String.class, "string", BasicSerializer.STRING);
		SerializerFactory.addBasic(StringBuilder.class, "string", BasicSerializer.STRING_BUILDER);
		SerializerFactory.addBasic(Object.class, "object", BasicSerializer.OBJECT);
		SerializerFactory.addBasic(java.util.Date.class, "date", BasicSerializer.DATE);

		SerializerFactory.addBasic(boolean.class, "boolean", BasicSerializer.BOOLEAN);
		SerializerFactory.addBasic(byte.class, "byte", BasicSerializer.BYTE);
		SerializerFactory.addBasic(short.class, "short", BasicSerializer.SHORT);
		SerializerFactory.addBasic(int.class, "int", BasicSerializer.INTEGER);
		SerializerFactory.addBasic(long.class, "long", BasicSerializer.LONG);
		SerializerFactory.addBasic(float.class, "float", BasicSerializer.FLOAT);
		SerializerFactory.addBasic(double.class, "double", BasicSerializer.DOUBLE);
		SerializerFactory.addBasic(char.class, "char", BasicSerializer.CHARACTER);

		SerializerFactory.addBasic(boolean[].class, "[boolean", BasicSerializer.BOOLEAN_ARRAY);
		SerializerFactory.addBasic(byte[].class, "[byte", BasicSerializer.BYTE_ARRAY);
		SerializerFactory.addBasic(short[].class, "[short", BasicSerializer.SHORT_ARRAY);
		SerializerFactory.addBasic(int[].class, "[int", BasicSerializer.INTEGER_ARRAY);
		SerializerFactory.addBasic(long[].class, "[long", BasicSerializer.LONG_ARRAY);
		SerializerFactory.addBasic(float[].class, "[float", BasicSerializer.FLOAT_ARRAY);
		SerializerFactory.addBasic(double[].class, "[double", BasicSerializer.DOUBLE_ARRAY);
		SerializerFactory.addBasic(char[].class, "[char", BasicSerializer.CHARACTER_ARRAY);
		SerializerFactory.addBasic(String[].class, "[string", BasicSerializer.STRING_ARRAY);
		SerializerFactory.addBasic(Object[].class, "[object", BasicSerializer.OBJECT_ARRAY);

		Deserializer objectDeserializer = new JavaDeserializer(Object.class);
		SerializerFactory.STATIC_TYPE_MAP.put("object", objectDeserializer);
		SerializerFactory.STATIC_TYPE_MAP.put(HessianRemote.class.getName(), RemoteDeserializer.DESER);

		ClassLoader systemClassLoader = null;
		try {
			systemClassLoader = ClassLoader.getSystemClassLoader();
		} catch (Exception e) {
			SerializerFactory.log.trace(null, e);
		}

		SYSTEM_CLASSLOADER = systemClassLoader;
	}
}

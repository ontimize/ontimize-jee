package com.ontimize.jee.common.tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;

import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

/**
 * Utility class for reflection stuff
 *
 */
public final class ReflectionTools {
	private static final Map<Class<?>, Class<?>>	primitiveWrapperMap	= new HashMap<Class<?>, Class<?>>();
	static {
		ReflectionTools.primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
		ReflectionTools.primitiveWrapperMap.put(Byte.TYPE, Byte.class);
		ReflectionTools.primitiveWrapperMap.put(Character.TYPE, Character.class);
		ReflectionTools.primitiveWrapperMap.put(Short.TYPE, Short.class);
		ReflectionTools.primitiveWrapperMap.put(Integer.TYPE, Integer.class);
		ReflectionTools.primitiveWrapperMap.put(Long.TYPE, Long.class);
		ReflectionTools.primitiveWrapperMap.put(Double.TYPE, Double.class);
		ReflectionTools.primitiveWrapperMap.put(Float.TYPE, Float.class);
		ReflectionTools.primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
	}

	/**
	 * Private constructor
	 */
	private ReflectionTools() {
		// do nothing
	}

	/**
	 * Check all interfaces of <code>theClass</code> that implements <code>theInterface</code>
	 *
	 * @param theClass
	 * @param theInterface
	 * @return
	 */
	public static Class<?>[] getInterfacesExtending(Class<?> theClass, Class<?> theInterface) {
		if (theClass == null) {
			return null;
		}
		List<Class<?>> interfaces = ClassUtils.getAllInterfaces(theClass);
		if (theInterface == null) {
			return interfaces.toArray(new Class<?>[0]);
		}
		List<Class<?>> res = new ArrayList<Class<?>>();
		for (Class<?> interfaceToCheck : interfaces) {
			if (theInterface.isAssignableFrom(interfaceToCheck)) {
				res.add(interfaceToCheck);
			}
		}
		return res.toArray(new Class<?>[0]);
	}

	/**
	 * Check all interfaces of <code>theClass</code>
	 *
	 * @param theClass
	 * @param theInterface
	 * @return
	 */
	public static Class<?>[] getInterfacesExtending(Class<?> theClass) {
		return ReflectionTools.getInterfacesExtending(theClass, null);
	}

	/**
	 * Create a new instance of a class based on its name
	 *
	 * @param className
	 * @return
	 */
	public static <T> T newInstance(String className, Class<T> returnType) {
		try {
			return returnType.cast(Class.forName(className).newInstance());
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new OntimizeJEERuntimeException(e);
		}
	}

	/**
	 * Create a new instance of a class based on its name
	 *
	 * @param className
	 * @return
	 */
	public static <T> T newInstance(String className, Class<T> returnType, Object... args) {
		return returnType.cast(ReflectionTools.newInstance(className, args));
	}

	/**
	 * Create a new instance of a class based on its name
	 *
	 * @param className
	 * @return
	 */
	public static Object newInstance(String className, Object... args) {
		Class<?> theClass = null;
		try {
			theClass = Class.forName(className);
		} catch (Exception e) {
			throw new OntimizeJEERuntimeException(e);
		}
		return ReflectionTools.newInstance(theClass, args);
	}

	/**
	 * Create a new instance of a class based on its name
	 *
	 * @param className
	 * @return
	 */
	public static <T> T newInstance(Class<T> theClass, Object... args) {
		try {
			Constructor<?>[] constructors = theClass.getConstructors();
			Constructor<?> ctr = ReflectionTools.findConstructor(constructors, args);
			if (ctr != null) {
				ctr.setAccessible(true);
				return theClass.cast(ctr.newInstance(args));
			}
			constructors = theClass.getDeclaredConstructors();
			ctr = ReflectionTools.findConstructor(constructors, args);
			if (ctr != null) {
				ctr.setAccessible(true);
				return theClass.cast(ctr.newInstance(args));
			}
		} catch (Exception e) {
			throw new OntimizeJEERuntimeException(e);
		}
		throw new OntimizeJEERuntimeException("Can't find constructor for " + theClass.getName());
	}

	private static Constructor<?> findConstructor(Constructor<?>[] constructors, Object... args) {
		for (Constructor<?> ctr : constructors) {
			if (ctr.getParameterTypes().length == args.length) {
				if (ctr.getParameterTypes().length == 0) {
					return ctr;
				}
				boolean isValid = true;
				for (int i = 0; i < ctr.getParameterTypes().length; i++) {
					Class<?> cl = ctr.getParameterTypes()[i];
					if (args[i] != null) {
						if (cl.isAssignableFrom(args[i].getClass())) {
						} else if (cl.isPrimitive() && ReflectionTools.primitiveWrapperMap.get(cl).isAssignableFrom(args[i].getClass())) {
						} else {
							isValid = false;
							break;
						}
					}
				}
				if (isValid) {
					return ctr;
				}
			}
		}
		return null;
	}

	/**
	 * Returns a method from a {@link Class} by name. Note!!: no polimorphims allowed in the class for the method
	 *
	 * @param name
	 */
	public static Method getMethodByName(Class<?> theClass, String name) {
		return ReflectionTools.getMethodByNameAndParatemerNumber(theClass, name, -1);
	}

	/**
	 * Returns a method from a {@link Class} by name. Note!!: no polimorphims allowed in the class for the method
	 *
	 * @param name
	 */
	public static Method getMethodByNameAndParatemerNumber(Class<?> theClass, String name, int numParameters) {
		Method[] methods = theClass.getMethods();
		Method method = ReflectionTools.findMethod(methods, name, numParameters);
		if (method != null) {
			return method;
		}
		// search in declared methods
		for (Class<?> innerClass = theClass; innerClass != null; innerClass = innerClass.getSuperclass()) {
			Method[] declaredMethods = innerClass.getDeclaredMethods();
			method = ReflectionTools.findMethod(declaredMethods, name, numParameters);
			if (method != null) {
				return method;
			}

		}

		throw new OntimizeJEERuntimeException(String.format("No method %s found in class %s", name,
				Proxy.class.isAssignableFrom(theClass) ? Arrays.toString(theClass.getInterfaces()) : theClass));
	}

	private static Method findMethod(Method[] methods, String name, int numParameters) {
		for (Method method : methods) {
			if (name.equals(method.getName()) && ((numParameters == -1) || (method.getParameterTypes().length == numParameters))) {
				return method;
			}
		}
		return null;
	}

	public static Object invoke(Object toInvoke, String methodName, Object... parameters) {
		Method method = ReflectionTools.getMethodByNameAndParatemerNumber((toInvoke instanceof Class) ? (Class<?>) toInvoke : toInvoke.getClass(), methodName,
				parameters == null ? 0 : parameters.length);
		try {
			method.setAccessible(true);
			return method.invoke((toInvoke instanceof Class) ? null : toInvoke, parameters);
		} catch (Exception e) {
			throw new OntimizeJEERuntimeException(e);
		}
	}

	/**
	 * Return a {@link Field} reference in the class or superclasses
	 *
	 * @param cl
	 * @param fieldName
	 * @return
	 */
	public static Field getField(Class<?> cl, String fieldName) {

		for (Class<?> innerClass = cl; innerClass != null; innerClass = innerClass.getSuperclass()) {
			try {
				return innerClass.getDeclaredField(fieldName);
			} catch (Exception e) {
				// do nothing
			}
		}
		throw new OntimizeJEERuntimeException("Field " + fieldName + " not found in" + cl);
	}

	/**
	 * Establish thee value of a class field by reflection
	 *
	 * @param toInvoke
	 * @param fieldName
	 * @param valueToSet
	 */
	public static void setFieldValue(Object toInvoke, String fieldName, Object valueToSet) {
		try {
			Field field = ReflectionTools.getField(toInvoke.getClass(), fieldName);
			field.setAccessible(true);
			field.set(toInvoke, valueToSet);
		} catch (Exception e) {
			throw new OntimizeJEERuntimeException(e);
		}
	}

	/**
	 * Get the value of a class field by reflection
	 *
	 * @param toInvoke
	 * @param fieldName
	 * @return
	 */
	public static Object getFieldValue(Object toInvoke, String fieldName) {
		try {
			Field field = ReflectionTools.getField(toInvoke.getClass(), fieldName);
			field.setAccessible(true);
			return field.get(toInvoke);
		} catch (Exception e) {
			throw new OntimizeJEERuntimeException(e);
		}
	}

	/**
	 * <p> Gets a <code>List</code> of all interfaces implemented by the given class and its superclasses. </p>
	 *
	 * <p> The order is determined by looking through each interface in turn as declared in the source file and following its hierarchy up. Then each superclass is considered in
	 * the same way. Later duplicates are ignored, so the order is maintained. </p>
	 *
	 * @param cls
	 *            the class to look up, may be <code>null</code>
	 * @return the <code>List</code> of interfaces in order, <code>null</code> if null input
	 */
	public static List<Class<?>> getAllInterfaces(Class<?> cls) {
		if (cls == null) {
			return null;
		}

		List<Class<?>> interfacesFound = new ArrayList<Class<?>>();
		ReflectionTools.getAllInterfaces(cls, interfacesFound);

		return interfacesFound;
	}

	/**
	 * Get the interfaces for the specified class.
	 *
	 * @param cls
	 *            the class to look up, may be <code>null</code>
	 * @param interfacesFound
	 *            the <code>Set</code> of interfaces for the class
	 * @return the all interfaces
	 */
	private static void getAllInterfaces(Class<?> cls, List<Class<?>> interfacesFound) {
		while (cls != null) {
			Class<?>[] interfaces = cls.getInterfaces();

			for (int i = 0; i < interfaces.length; i++) {
				if (!interfacesFound.contains(interfaces[i])) {
					interfacesFound.add(interfaces[i]);
					ReflectionTools.getAllInterfaces(interfaces[i], interfacesFound);
				}
			}

			cls = cls.getSuperclass();
		}
	}

	/**
	 * Gets the properties.
	 *
	 * @param clazz
	 *            the clazz
	 * @return the properties
	 */
	public static List<String> getProperties(Class<?> clazz, boolean includeTransient) {
		List<String> res = new ArrayList<>();
		Field[] declaredFields = clazz.getDeclaredFields();
		for (Field f : declaredFields) {
			if (!Modifier.isTransient(f.getModifiers()) || (Modifier.isTransient(f.getModifiers()) && includeTransient)) {
				res.add(f.getName());
			}
		}
		return res;
	}

	/**
	 * Gets the enum constant.
	 *
	 * @param enumClassName
	 *            the enum class name
	 * @param enumConstantName
	 *            the enum constant name
	 * @return the enum constant
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 */
	public static Object getEnumConstant(String enumClassName, String enumConstantName) {
		try {
			Class<?> c = Class.forName(enumClassName);
			return ReflectionTools.getEnumConstant(c, enumConstantName);
		} catch (ClassNotFoundException e) {
			throw new OntimizeJEERuntimeException(e);
		}
	}

	/**
	 * Gets the enum constant.
	 *
	 * @param enumClass
	 *            the enum class
	 * @param enumConstantName
	 *            the enum constant name
	 * @return the enum constant
	 */
	private static Object getEnumConstant(Class<?> enumClass, String enumConstantName) {
		List<?> list = Arrays.asList(enumClass.getEnumConstants());
		for (Object ob : list) {
			if (ob.toString().equals(enumConstantName)) {
				return ob;
			}
		}
		throw new OntimizeJEERuntimeException("Enum constant " + enumConstantName + " not found in" + enumClass);
	}

}

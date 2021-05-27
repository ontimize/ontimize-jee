package com.ontimize.jee.common.tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

/**
 * Utility class for reflection stuff
 *
 */
public final class ReflectionTools {

    private static final Logger logger = LoggerFactory.getLogger(ReflectionTools.class);

    /**
     * Maps primitive <code>Class</code>es to their corresponding wrapper <code>Class</code>.
     */
    private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<>();
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
     * Maps wrapper <code>Class</code>es to their corresponding primitive types.
     */
    private static final Map<Class<?>, Class<?>> wrapperPrimitiveMap = new HashMap<>();
    static {
        for (Iterator<Class<?>> it = ReflectionTools.primitiveWrapperMap.keySet().iterator(); it.hasNext();) {
            Class<?> primitiveClass = it.next();
            Class<?> wrapperClass = ReflectionTools.primitiveWrapperMap.get(primitiveClass);
            if (!primitiveClass.equals(wrapperClass)) {
                ReflectionTools.wrapperPrimitiveMap.put(wrapperClass, primitiveClass);
            }
        }
    }

    /**
     * Private constructor
     */
    private ReflectionTools() {
        // do nothing
    }

    /**
     * Check all interfaces of <code>theClass</code> that implements <code>theInterface</code>
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
        List<Class<?>> res = new ArrayList<>();
        for (Class<?> interfaceToCheck : interfaces) {
            if (theInterface.isAssignableFrom(interfaceToCheck)) {
                res.add(interfaceToCheck);
            }
        }
        return res.toArray(new Class<?>[0]);
    }

    /**
     * Check all interfaces of <code>theClass</code>
     * @param theClass
     * @param theInterface
     * @return
     */
    public static Class<?>[] getInterfacesExtending(Class<?> theClass) {
        return ReflectionTools.getInterfacesExtending(theClass, null);
    }

    /**
     * Create a new instance of a class based on its name
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
     * @param className
     * @return
     */
    public static <T> T newInstance(String className, Class<T> returnType, Object... args) {
        return returnType.cast(ReflectionTools.newInstance(className, args));
    }

    /**
     * Create a new instance of a class based on its name
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

    /**
     * Find constructor.
     * @param constructors the constructors
     * @param args the args
     * @return the constructor
     */
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
                        } else if (cl.isPrimitive()
                                && ReflectionTools.primitiveWrapperMap.get(cl).isAssignableFrom(args[i].getClass())) {
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
     * Returns a method from a {@link Class} by name. Note!!: no polimorphims allowed in the class for
     * the method
     * @param name
     */
    public static Method getMethodByName(Class<?> theClass, String name) {
        return ReflectionTools.getMethodByNameAndParatemerNumber(theClass, name, -1);
    }

    /**
     * Returns a method from a {@link Class} by name. Note!!: no polimorphims allowed in the class for
     * the method
     * @param theClass the the class
     * @param name the name
     * @param numParameters the num parameters
     * @return the method by name and paratemer number
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

        throw new OntimizeJEERuntimeException(
                String.format("No method %s found in class %s", name,
                        Proxy.class.isAssignableFrom(theClass) ? Arrays.toString(theClass.getInterfaces()) : theClass));
    }

    /**
     * Returns a method from a {@link Class} by name and parameters classes.
     * @param theClass the the class
     * @param name the name
     * @param numParameters the num parameters
     * @return the method by name and paratemer number
     */
    public static Method getMethodByNameAndParatemers(Class<?> theClass, String name, Object... parameters) {
        Method[] methods = theClass.getMethods();
        Method method = ReflectionTools.findMethod(methods, name, parameters);
        if (method != null) {
            return method;
        }
        // search in declared methods
        for (Class<?> innerClass = theClass; innerClass != null; innerClass = innerClass.getSuperclass()) {
            Method[] declaredMethods = innerClass.getDeclaredMethods();
            method = ReflectionTools.findMethod(declaredMethods, name, parameters);
            if (method != null) {
                return method;
            }

        }
        throw new OntimizeJEERuntimeException("No method " + name + " found in class " + theClass);
    }

    /**
     * Find method.
     * @param methods the methods
     * @param name the name
     * @param numParameters the num parameters
     * @return the method
     */
    private static Method findMethod(Method[] methods, String name, int numParameters) {
        for (Method method : methods) {
            if (name.equals(method.getName())
                    && ((numParameters == -1) || (method.getParameterTypes().length == numParameters))) {
                return method;
            }
        }
        return null;
    }

    /**
     * Find method.
     * @param methods the methods
     * @param name the name
     * @param numParameters the num parameters
     * @return the method
     */
    private static Method findMethod(Method[] methods, String name, Object... parameterOrdered) {
        if ((parameterOrdered == null) || (parameterOrdered.length == 0)) {
            return ReflectionTools.findMethod(methods, name, 0);
        }
        for (Method method : methods) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if ((parameterTypes.length == parameterOrdered.length) && method.getName().equals(name)) {
                boolean valid = true;
                for (int i = 0; i < parameterTypes.length; i++) {
                    if (!ReflectionTools.isAssignable(
                            parameterOrdered[i] == null ? null : parameterOrdered[i].getClass(), parameterTypes[i],
                            true)) {
                        valid = false;
                    }
                }
                if (valid) {
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * Invoke.
     * @param toInvoke the to invoke
     * @param methodName the method name
     * @param parameters the parameters
     * @return the object
     */
    public static Object invoke(Object toInvoke, String methodName, Object... parameters) {
        // Method method =
        // ReflectionTools.getMethodByNameAndParatemerNumber((toInvoke
        // instanceof Class) ? (Class<?>) toInvoke : toInvoke.getClass(),
        // methodName, parameters == null ? 0 : parameters.length);
        Method method = ReflectionTools.getMethodByNameAndParatemers(
                toInvoke instanceof Class ? (Class<?>) toInvoke : toInvoke.getClass(), methodName, parameters);
        try {
            method.setAccessible(true);
            return method.invoke(toInvoke instanceof Class ? null : toInvoke, parameters);
        } catch (Exception e) {
            throw new OntimizeJEERuntimeException(e);
        }
    }

    /**
     * Return a {@link Field} reference in the class or superclasses.
     * @param cl the cl
     * @param fieldName the field name
     * @return the field
     */
    public static Field getField(Class<?> cl, String fieldName) {

        for (Class<?> innerClass = cl; innerClass != null; innerClass = innerClass.getSuperclass()) {
            try {
                return innerClass.getDeclaredField(fieldName);
            } catch (Exception error) {
                ReflectionTools.logger.trace(null, error);
                // do nothing
                for (Class<?> interfaceClass : innerClass.getInterfaces()) {
                    try {
                        return interfaceClass.getDeclaredField(fieldName);
                    } catch (Exception err) {
                        ReflectionTools.logger.trace(null, err);
                    }
                }
            }
        }

        throw new OntimizeJEERuntimeException("Field " + fieldName + " not found in" + cl);
    }

    /**
     * Establish the value of a class field by reflection.
     * @param toInvoke the to invoke
     * @param fieldName the field name
     * @param valueToSet the value to set
     */
    public static void setFieldValue(Object toInvoke, String fieldName, Object valueToSet) {
        try {
            Field field = ReflectionTools
                .getField(toInvoke instanceof Class ? (Class<?>) toInvoke : toInvoke.getClass(), fieldName);
            field.setAccessible(true);
            field.set(toInvoke instanceof Class ? null : toInvoke, valueToSet);
        } catch (Exception e) {
            throw new OntimizeJEERuntimeException(e);
        }
    }

    /**
     * Get the value of a class field by reflection.
     * @param toInvoke the to invoke
     * @param fieldName the field name
     * @return the field value
     */
    public static Object getFieldValue(Object toInvoke, String fieldName) {
        try {
            Field field = ReflectionTools
                .getField(toInvoke instanceof Class ? (Class<?>) toInvoke : toInvoke.getClass(), fieldName);
            field.setAccessible(true);
            return field.get(toInvoke instanceof Class ? null : toInvoke);
        } catch (Exception error) {
            throw new OntimizeJEERuntimeException(error);
        }
    }

    /**
     * <p>
     * Gets a <code>List</code> of all interfaces implemented by the given class and its superclasses.
     * </p>
     *
     * <p>
     * The order is determined by looking through each interface in turn as declared in the source file
     * and following its hierarchy up. Then each superclass is considered in the same way. Later
     * duplicates are ignored, so the order is maintained.
     * </p>
     * @param cls the class to look up, may be <code>null</code>
     * @return the <code>List</code> of interfaces in order, <code>null</code> if null input
     */
    public static List<Class<?>> getAllInterfaces(Class<?> cls) {
        if (cls == null) {
            return null;
        }

        List<Class<?>> interfacesFound = new ArrayList<>();
        ReflectionTools.getAllInterfaces(cls, interfacesFound);

        return interfacesFound;
    }

    /**
     * Get the interfaces for the specified class.
     * @param cls the class to look up, may be <code>null</code>
     * @param interfacesFound the <code>Set</code> of interfaces for the class
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
     * @param clazz the clazz
     * @return the properties
     */
    public static List<String> getProperties(Class<?> clazz, boolean includeTransient) {
        List<String> res = new ArrayList<>();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field f : declaredFields) {
            if (!Modifier.isTransient(f.getModifiers())
                    || (Modifier.isTransient(f.getModifiers()) && includeTransient)) {
                res.add(f.getName());
            }
        }
        return res;
    }

    /**
     * Gets the enum constant.
     * @param enumClassName the enum class name
     * @param enumConstantName the enum constant name
     * @return the enum constant
     * @throws ClassNotFoundException the class not found exception
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
     * @param enumClass the enum class
     * @param enumConstantName the enum constant name
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

    /**
     * <p>
     * Checks if one <code>Class</code> can be assigned to a variable of another <code>Class</code>.
     * </p>
     *
     * <p>
     * Unlike the {@link Class#isAssignableFrom(java.lang.Class)} method, this method takes into account
     * widenings of primitive classes and <code>null</code>s.
     * </p>
     *
     * <p>
     * Primitive widenings allow an int to be assigned to a long, float or double. This method returns
     * the correct result for these cases.
     * </p>
     *
     * <p>
     * <code>Null</code> may be assigned to any reference type. This method will return
     * <code>true</code> if <code>null</code> is passed in and the toClass is non-primitive.
     * </p>
     *
     * <p>
     * Specifically, this method tests whether the type represented by the specified <code>Class</code>
     * parameter can be converted to the type represented by this <code>Class</code> object via an
     * identity conversion widening primitive or widening reference conversion. See
     * <em><a href="http://java.sun.com/docs/books/jls/">The Java Language Specification</a></em> ,
     * sections 5.1.1, 5.1.2 and 5.1.4 for details.
     * </p>
     * @param cls the Class to check, may be null
     * @param toClass the Class to try to assign into, returns false if null
     * @param autoboxing whether to use implicit autoboxing/unboxing between primitives and wrappers
     * @return <code>true</code> if assignment possible
     */
    public static boolean isAssignable(Class<?> cls, Class<?> toClass, boolean autoboxing) {
        if (toClass == null) {
            return false;
        }
        // have to check for null, as isAssignableFrom doesn't
        if (cls == null) {
            return !toClass.isPrimitive();
        }
        // autoboxing:
        if (autoboxing) {
            if (cls.isPrimitive() && !toClass.isPrimitive()) {
                cls = ReflectionTools.primitiveToWrapper(cls);
                if (cls == null) {
                    return false;
                }
            }
            if (toClass.isPrimitive() && !cls.isPrimitive()) {
                cls = ReflectionTools.wrapperToPrimitive(cls);
                if (cls == null) {
                    return false;
                }
            }
        }
        if (cls.equals(toClass)) {
            return true;
        }
        if (cls.isPrimitive()) {
            return ReflectionTools.isAssignablePrimitive(cls, toClass);
        }
        return toClass.isAssignableFrom(cls);
    }

    public static boolean isAssignablePrimitive(Class<?> cls, Class<?> toClass) {
        if (!toClass.isPrimitive()) {
            return false;
        }
        if (Integer.TYPE.equals(cls)) {
            return Long.TYPE.equals(toClass) || Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
        }
        if (Long.TYPE.equals(cls)) {
            return Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
        }
        if (Boolean.TYPE.equals(cls)) {
            return false;
        }
        if (Double.TYPE.equals(cls)) {
            return false;
        }
        if (Float.TYPE.equals(cls)) {
            return Double.TYPE.equals(toClass);
        }
        if (Character.TYPE.equals(cls)) {
            return Integer.TYPE.equals(toClass) || Long.TYPE.equals(toClass) || Float.TYPE.equals(toClass)
                    || Double.TYPE.equals(toClass);
        }
        if (Short.TYPE.equals(cls)) {
            return Integer.TYPE.equals(toClass) || Long.TYPE.equals(toClass) || Float.TYPE.equals(toClass)
                    || Double.TYPE.equals(toClass);
        }
        if (Byte.TYPE.equals(cls)) {
            return Short.TYPE.equals(toClass) || Integer.TYPE.equals(toClass) || Long.TYPE.equals(toClass)
                    || Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
        }
        // should never get here
        return false;
    }

    /**
     * <p>
     * Converts the specified primitive Class object to its corresponding wrapper Class object.
     * </p>
     * @param cls the class to convert, may be null
     * @return the wrapper class for <code>cls</code> or <code>cls</code> if <code>cls</code> is not a
     *         primitive. <code>null</code> if null input.
     */
    private static Class<?> primitiveToWrapper(Class<?> cls) {
        Class<?> convertedClass = cls;
        if ((cls != null) && cls.isPrimitive()) {
            convertedClass = ReflectionTools.primitiveWrapperMap.get(cls);
        }
        return convertedClass;
    }

    /**
     * <p>
     * Converts the specified wrapper class to its corresponding primitive class.
     * </p>
     *
     * <p>
     * This method is the counter part of <code>primitiveToWrapper()</code>. If the passed in class is a
     * wrapper class for a primitive type, this primitive type will be returned (e.g.
     * <code>Integer.TYPE</code> for <code>Integer.class</code>). For other classes, or if the parameter
     * is <b>null</b>, the return value is <b>null</b>.
     * </p>
     * @param cls the class to convert, may be <b>null</b>
     * @return the corresponding primitive type if <code>cls</code> is a wrapper class, <b>null</b>
     *         otherwise
     * @see #primitiveToWrapper(Class)
     */
    public static Class wrapperToPrimitive(Class cls) {
        return ReflectionTools.wrapperPrimitiveMap.get(cls);
    }

    public static Class<?> classForName(String string) {
        try {
            return Class.forName(string);
        } catch (ClassNotFoundException error) {
            throw new OntimizeJEERuntimeException(error);
        }
    }

    /**
     * Return all fields, own class fields, included inherited from superclasses
     * @param obj
     */
    public static List<Field> getAllFields(Class initialClass) {
        List<Field> fieldList = new ArrayList<>();
        Class tmpClass = initialClass;
        while (tmpClass != null) {
            fieldList.addAll(Arrays.asList(tmpClass.getDeclaredFields()));
            tmpClass = tmpClass.getSuperclass();
        }
        return fieldList;
    }

}

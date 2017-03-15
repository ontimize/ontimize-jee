package com.ontimize.jee.server.dao.jpa;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Imatia Innovation
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class JPAUtils {
    private final static Logger logger = LoggerFactory.getLogger(JPAUtils.class);

    public static Object createInstanceofClass(final String className) {
        try {
            final Class classInstance = Class.forName(className);
            return JPAUtils.createInstanceofClass(classInstance);
        } catch (final Exception ex) {
			JPAUtils.logger.error(null, ex);
        }
        return null;
    }

    public static Object createInstanceofClass(final Class classInstance) {
        try {
            final Constructor cons = classInstance.getConstructor();
            return cons.newInstance();
        } catch (final Exception ex) {
			JPAUtils.logger.error(null, ex);
        }
        return null;
    }

    public static Object executeMethod(final String className, final String methodName, final Object instance, final Class[] parameters, final Object[] values) {
        try {
            final Class classInstance = Class.forName(className);
            final Method method = classInstance.getMethod(methodName, parameters);
            return method.invoke(classInstance, values);
        } catch (final Exception e) {
			JPAUtils.logger.error(null, e);
        }
        return null;
    }

    public static void executeSetMethod(final Class classInstance, final String methodName, final Object instance, final Class parameters, final Object values) throws Exception {
        Method method = null;
        try {
            method = classInstance.getMethod(methodName, new Class[] { parameters });
            JPAUtils.invokeSetValueDeclaredMethod(method, instance, values);
        } catch (final Exception ex) {
            String msg = "There was an error invoking set method: \"" + method + "\" with the value: \"" + values + "\".";
            if (method != null) {
                msg = "There was an error invoking set method: \"" + method.getName() + "\" with value: \"" + values + "\"." +
                        " The method expects a paratemer \"" + method.getParameterTypes()[0] + "\" and received a \"" + values.getClass() + "\"";
            }
            final Exception myEx = new Exception(msg, ex);
            JPAUtils.logger.error(myEx.getMessage(), myEx);
            throw myEx;
        }
    }

    public static Object executeGetMethod(final Class classInstance, final String methodName, final Object instance) throws Exception {
        try {
            final Method method = classInstance.getMethod(methodName);
            return JPAUtils.invokeGetValueDeclaredMethod(method, instance);
        } catch (final Exception ex) {
            final String msg = "There was an error invoking set method: \"" + methodName + "\".";
            final Exception myEx = new Exception(msg, ex);
            JPAUtils.logger.error(myEx.getMessage(), myEx);
        }
        return null;
    }

    public static Object invokeGetValueDeclaredMethod(final Method method, final Object beanObject) throws Exception {
        if (method != null) {
            return method.invoke(beanObject);
        } else {
			// throw new Exception("GET method must not be null");
			return null;
        }
    }

    public static Object invokeSetValueDeclaredMethod(final Method method, final Object value, Object setValue) throws Exception {
        if (method != null) {
            final Class methodClass = method.getParameterTypes()[0];
            if (setValue != null) {
                final Class setValueClass = setValue.getClass();
                if (!methodClass.equals(setValueClass)) {
                    if (methodClass.equals(Double.class) && setValueClass.equals(Float.class)) {
                        setValue = new Double(((Number) setValue).doubleValue());
                    } else {
                        if (methodClass.equals(Long.class) && setValueClass.equals(Integer.class)) {
                            setValue = new Long(((Number) setValue).intValue());
                        } else {
                            if (methodClass.equals(Integer.class) && setValueClass.equals(Short.class)) {
                                setValue = new Integer(((Number) setValue).shortValue());
                            }
                        }
                    }
                }
            }
            return method.invoke(value, new Object[] { setValue });
        } else {
            throw new Exception("Invoke SET method: the method must not be null");
        }
    }

    /**
     * If the bean only have one field as a @Id, return this field.
     *
     * @param beanClass
     * @return
     * @throws IntrospectionException
     */
    public static Field getKeyField(final Class<?> beanClass) throws Exception {
        final PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(beanClass).getPropertyDescriptors();
        final int len = propertyDescriptors.length;
        for (int i = 0; i < len; i++) {
            if (propertyDescriptors[i].getName().equalsIgnoreCase("class")) {
                continue;
            }
            final Field field = JPAUtils.getDeclaredFieldByName(beanClass, propertyDescriptors[i].getName());
            if (field != null) {
                Annotation annotation = field.getAnnotation(Id.class);
                if (annotation != null) {
                    return field;
                } else {
                    annotation = field.getAnnotation(EmbeddedId.class);
                    if (annotation != null) {
                        throw new Exception("It isn't possible use the method:  getKeyField(Class<?> beanClass) with beans with multiple key.");
                    }
                }

                final Method getMethod = propertyDescriptors[i].getReadMethod();
                if (getMethod != null) {
                    annotation = getMethod.getAnnotation(Id.class);
                    if (annotation == null) {
                        annotation = getMethod.getAnnotation(EmbeddedId.class);
                        if (annotation != null) {
                            throw new Exception("It isn't possible use the method:  getKeyField(Class<?> beanClass) with beans with multiple key.");
                        }
                    } else {
                        return field;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Return the list of fields in "beanClass" with @Id annotation.
     *
     * @param beanClass
     *            Some class.
     * @return the list of fields in "beanClass" with @Id annotation.
     * @throws IntrospectionException
     */
    public static List<Field> getKeyFields(final Class<?> beanClass) throws IntrospectionException {
        final List<Field> ids = new ArrayList<Field>();
        final PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(beanClass).getPropertyDescriptors();
        final int len = propertyDescriptors.length;
        for (int i = 0; i < len; i++) {
            if (propertyDescriptors[i].getName().equalsIgnoreCase("class")) {
                continue;
            }
            final Field field = JPAUtils.getDeclaredFieldByName(beanClass, propertyDescriptors[i].getName());
            if (field != null) {
                Annotation annotation = field.getAnnotation(Id.class);
                if (annotation != null) {
                    ids.add(field);
                } else {// Added all fields of @EmbeddedId class
                    annotation = field.getAnnotation(EmbeddedId.class);
                    if (annotation != null) {
                        final List<Field> embeddedFields = JPAUtils.getFieldsOfClass(field.getType());
                        if (embeddedFields != null) {
                            ids.addAll(embeddedFields);
                        }
                    }
                }

                final Method getMethod = propertyDescriptors[i].getReadMethod();
                if (getMethod != null) {
                    annotation = getMethod.getAnnotation(Id.class);
                    if (annotation != null) {
                        ids.add(field);
                    } else {
                        annotation = getMethod.getAnnotation(EmbeddedId.class);
                        if (annotation != null) {
                            final List<Field> embeddedFields = JPAUtils.getFieldsOfClass(field.getType());
                            if (embeddedFields != null) {
                                ids.addAll(embeddedFields);
                            }
                        }
                    }
                }
            }
        }
        return ids;
    }

    /**
     * Return the list of colums in "beanClass" with @Id annotation.
     *
     * @param beanClass
     *            Some class.
     * @return the list of colums in "beanClass" with @Id annotation.
     * @throws IntrospectionException
     */
    public static List<Object> getKeyColumnNames(final Class<?> beanClass) throws IntrospectionException {
        final List<Object> ids = new ArrayList<Object>();
        final PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(beanClass).getPropertyDescriptors();
        final int len = propertyDescriptors.length;
        for (int i = 0; i < len; i++) {
            if (propertyDescriptors[i].getName().equalsIgnoreCase("class")) {
                continue;
            }
            final Field field = JPAUtils.getDeclaredFieldByName(beanClass, propertyDescriptors[i].getName());
            final Method getMethod = propertyDescriptors[i].getReadMethod();
            if (field != null) {
                Annotation annotation = field.getAnnotation(Id.class);
                if ((annotation == null) && (getMethod != null)) {
                    annotation = getMethod.getAnnotation(Id.class);
                }
                if (annotation != null) {
                    ids.add(field.getName());
                    continue;
                }
                annotation = field.getAnnotation(EmbeddedId.class);
                if ((annotation == null) && (getMethod != null)) {
                    annotation = getMethod.getAnnotation(EmbeddedId.class);
                }
                if (annotation != null) {
                    final List<String> embeddedColums = JPAUtils.getColumnNames(field.getType());
                    for (int j = 0; j < embeddedColums.size(); j++) {
                        ids.add(field.getName() + OntimizeJpaNaming.DOT + embeddedColums.get(j));
                    }
                }
            }
        }
        return ids;
    }

    /**
     * Return the class implements the multiple key of some bean or null if the bean hasn't multiple key.
     *
     * @param beanClass
     * @return
     * @throws IntrospectionException
     */
    public static Class getKeyClassBean(final Class beanClass) throws IntrospectionException {
        final IdClass annotation = (IdClass) beanClass.getAnnotation(IdClass.class);
        if (annotation != null) {
            return annotation.value();
        } else {
            final Field idField = JPAUtils.getEmbeddedIdField(beanClass);
            if (idField != null) {
                return idField.getType();
            }
        }
        return null;
    }

    /**
     * If 'beanClass' has a multiple key this method get the instance representing its key with the values saved in kv.
     *
     * @param kv
     * @param beanClass
     * @return
     * @throws Exception
     */
    public static Object getPrimaryKeyObjectToClass(final Hashtable kv, final Class beanClass) throws Exception {
        final Class pkClass = JPAUtils.getKeyClassBean(beanClass);

        if (pkClass != null) {// MULTIPLE KEY
            return JPAUtils.getPrimaryKeyInstance(kv, pkClass);
        } else {// In this case the key must have only one value. -> SIMPLE KEY
            if (kv.size() == 1) {
                final Enumeration enumKeys = kv.keys();
                while (enumKeys.hasMoreElements()) {
                    final Object oKey = enumKeys.nextElement();
                    Object oValue = kv.get(oKey);
                    if (!(oValue instanceof Serializable)) {
                        oValue = oValue.toString();
                    }
                    return oValue;
                }
            }
        }
        return null;
    }

    /**
     * If 'beanClass' has a multiple key this method get the instance representing its key with the values saved in kv.
     *
     * @param kv
     * @param pkClass
     * @return
     * @throws Exception
     */
    public static Object getPrimaryKeyInstance(final Hashtable kv, final Class pkClass) throws Exception {
        if (pkClass != null) {// MULTIPLE KEY
            final Object pkInstance = JPAUtils.createInstanceofClass(pkClass);
            final Enumeration<Object> keys = kv.keys();
            while (keys.hasMoreElements()) {
                // TODO Review if it allow more than one dot in a name of primary key column.
                // E.g. Now we can use: procedurecassid or id.procedureclassid, but not more than one dot:
                // id.id.procedureclassid
                final String attId = keys.nextElement().toString();
                final int pos = attId.lastIndexOf(OntimizeJpaNaming.DOT);
                String right = attId;
                if (pos > 0) {
                    right = attId.substring(pos + 1);
                }
                final Method method = JPAUtils.getDeclaredSetMethod(right, pkInstance);
                if (method != null) {
                    final Object value = kv.get(attId);
                    try {
                        JPAUtils.invokeSetValueDeclaredMethod(method, pkInstance, value);
                    } catch (final Exception ex) {
                        throw ex;
                    }
                } else {
                    throw new Exception(OntimizeJpaNaming.EXCEPTION_CREATE_MULTIPLE_PRIMARY_KEY);
                }
            }
            return pkInstance;
        }
        return null;
    }

    /**
     * Get the field with @EmbeddedId annotation or null if ther isn't any field with @EmbeddedId annotation.
     *
     * @param beanClass
     * @return
     * @throws IntrospectionException
     */
    public static Field getEmbeddedIdField(final Class beanClass) throws IntrospectionException {
        final Field value = null;
        final PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(beanClass).getPropertyDescriptors();
        final int len = propertyDescriptors.length;
        for (int i = 0; i < len; i++) {
            if (propertyDescriptors[i].getName().equalsIgnoreCase("class")) {
                continue;
            }
            final Field field = JPAUtils.getDeclaredFieldByName(beanClass, propertyDescriptors[i].getName());
            if (field != null) {
                EmbeddedId annotation = field.getAnnotation(EmbeddedId.class);
                if (annotation == null) {
                    final Method getMethod = propertyDescriptors[i].getReadMethod();
                    if (getMethod != null) {
                        annotation = getMethod.getAnnotation(EmbeddedId.class);
                    }
                }
                if (annotation != null) {
                    return field;
                }
            }
        }
        return value;
    }

    /**
     * Get the field <code>name</code>.
     *
     * @param beanClass
     * @return
     * @throws IntrospectionException
     */
    public static Field getDeclaredFieldByName(final Class beanClass, final String name) {
        Field field = null;
        try {
            field = beanClass.getDeclaredField(name);
        } catch (final java.lang.NoSuchFieldException e1) {
            final Class beanClassSuper = beanClass.getSuperclass();
            if ((beanClassSuper != null) && !(beanClassSuper.equals(Object.class))) {
                field = JPAUtils.getDeclaredFieldByName(beanClassSuper, name);
            }
        }
        return field;
    }

    /**
     * Get the names of columns of "beanClass". Only the @Column, @Transient, @OneToOne and @ManyToOne attributes are
     * included.
     * If "beanClass" have an @EmbeddedId the attributes of class embedded are included in the list of columns.
     *
     * @param beanClass
     *            The class of a bean.
     * @return The names of attributes of "beanClass".
     * @throws IntrospectionException
     */
    public static List<String> getColumnNames(final Class<?> beanClass) throws IntrospectionException {
        final List<String> columns = new ArrayList<String>();
        final PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(beanClass).getPropertyDescriptors();

        final int len = propertyDescriptors.length - 1;
        for (int i = len; i >= 0; i--) {
            if (propertyDescriptors[i].getName().equalsIgnoreCase("class")) {
                continue;
            }
            final Field field = JPAUtils.getDeclaredFieldByName(beanClass, propertyDescriptors[i].getName());
            final Method getMethod = propertyDescriptors[i].getReadMethod();
            if (field != null) {
                if ((field.getAnnotation(OneToMany.class) != null) || (getMethod.getAnnotation(OneToMany.class) != null)
                        || (field.getAnnotation(ManyToMany.class) != null) || (getMethod.getAnnotation(ManyToMany.class) != null)) {
                    continue;
                }
                // FIXME (Sergio) coger esta informacion del metamodel de JPA (porque puede que el mapeo no se hiciese con anotaciones)
                Annotation annotation = field.getAnnotation(EmbeddedId.class);
                if ((annotation == null) && (getMethod != null)) {
                    annotation = getMethod.getAnnotation(EmbeddedId.class);
                }
                if (annotation != null) {
                    final List<String> embeddedColums = JPAUtils.getColumnNames(field.getType());
                    for (int j = 0; j < embeddedColums.size(); j++) {
                        columns.add(field.getName() + OntimizeJpaNaming.DOT + embeddedColums.get(j));
                    }
                    continue;
                }

                annotation = field.getAnnotation(Embedded.class);
                if ((annotation == null) && (getMethod != null)) {
                    annotation = getMethod.getAnnotation(Embedded.class);
                }
                if (annotation != null) {
                    final List<String> embeddedColums = JPAUtils.getColumnNames(field.getType());
                    for (int j = 0; j < embeddedColums.size(); j++) {
                        columns.add(field.getName() + OntimizeJpaNaming.DOT + embeddedColums.get(j));
                    }
                    continue;
                }

                // if attribute/method hasn't any annotation or it has a @Column or @Transient or @OneToOne or
                // @ManyToOne.
                columns.add(field.getName());
            }
        }
        return columns;
    }

    /**
     * Get the read method of the attribute "column" of the bean "bean".
     *
     * @param column
     *            The name of an attribute class.
     * @param bean
     *            An instance of bean.
     * @return The read method of the attribute "column" of the bean "bean", or null if "column" isn't a field of
     *         "bean".
     * @throws Exception
     */
    public static Method getDeclaredGetMethod(final String column, final Object bean) throws Exception {
        final Class beanClass = bean.getClass();
        final PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(beanClass).getPropertyDescriptors();
        final int len = propertyDescriptors.length - 1;
        for (int i = len; i >= 0; i--) {
            try {
                if (propertyDescriptors[i].getName().toLowerCase().equals(column.toLowerCase())) {
                    return propertyDescriptors[i].getReadMethod();
                }
            } catch (final Exception e) {
                JPAUtils.logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * Get the write method of the attribute "column" of the bean "bean".
     *
     * @param column
     *            The name of an attribute class.
     * @param bean
     *            An instance of bean.
     * @return The write method of the attribute "column" of the bean "bean", or null if "column" isn't a field of
     *         "bean".
     * @throws Exception
     */
    public static Method getDeclaredSetMethod(final String column, final Object bean) throws Exception {
        final Class beanClass = bean.getClass();
        final PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(beanClass).getPropertyDescriptors();
        final int len = propertyDescriptors.length - 1;
        for (int i = len; i >= 0; i--) {
            try {
                if (propertyDescriptors[i].getName().toLowerCase().equals(column.toLowerCase())) {
                    return propertyDescriptors[i].getWriteMethod();
                }
            } catch (final Exception e) {
                JPAUtils.logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * Get the class of attribute "column" in the bean class "beanClass".
     *
     * @param colum
     *            The name of some field of beanClass.
     * @param beanClass
     *            Some class.
     * @return the class of attribute "column" in the bean class "beanClass".
     * @throws Exception
     */
    public static Class getFieldClass(final String colum, final Class beanClass) {
        try {
            final int posDot = colum.indexOf(OntimizeJpaNaming.DOT);
            if (posDot > 0) {
                final Field declaredField = beanClass.getDeclaredField(colum.substring(0, posDot));
                return JPAUtils.getFieldClass(colum.substring(posDot + 1), declaredField.getType());
            }
            final Field declaredField = beanClass.getDeclaredField(colum);
            if (declaredField != null) {
                return declaredField.getType();
            }
        } catch (final java.lang.NoSuchFieldException e1) {
            final Class beanClassSuper = beanClass.getSuperclass();
            if ((beanClassSuper != null) && !(beanClassSuper.equals(Object.class))) {
                return JPAUtils.getFieldClass(colum, beanClassSuper);
            }
        }
        return null;
    }

    /**
     * Return the list of Fields in the class aClass
     *
     * @param aClass
     * @return
     */
    private static List<Field> getFieldsOfClass(final Class<?> aClass) throws IntrospectionException {
        final List<Field> fields = new ArrayList<Field>();
        final PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(aClass).getPropertyDescriptors();
        final int len = propertyDescriptors.length - 1;
        for (int i = len; i >= 0; i--) {
            if (propertyDescriptors[i].getName().equalsIgnoreCase("class")) {
                continue;
            }
            final Field aField = JPAUtils.getDeclaredFieldByName(aClass, propertyDescriptors[i].getName());
            if (aField != null) {
                fields.add(aField);
            }
        }
        return fields;
    }// Method method = classInstance.getMethod(methodName);

    /**
     * Get the name of field in beanClass represents nameColumn of table in database.
     * If the bean has an @EmbeddedId the column will be looking for into the class of primary key.
     *
     * @param beanClass
     *            The class of any bean.
     * @param nameColumn
     *            The name of column in data base.
     * @return The name of field of bean with the @Column(name='nameColumn').
     * @throws IntrospectionException
     */
    public static Object getAttributeNameColumn(final Class<?> beanClass, final String nameColumn) throws IntrospectionException {
        return JPAUtils.getAttributeNameColumn(beanClass, nameColumn, false);
    }

    /**
     * Get the name of field in beanClass represents nameColumn of table in database.
     * If the bean has an @EmbeddedId the column will be looking for into the class of primary key.
     *
     * @param beanClass
     *            The class of any bean.
     * @param nameColumn
     *            The name of column in data base.
     * @param checkUpdate
     *            If is true only include updatable and insertable fields.
     * @return The name of field of bean with the @Column(name='nameColumn').
     * @throws IntrospectionException
     */
    public static Object getAttributeNameColumn(final Class<?> beanClass, final String nameColumn, final boolean checkUpdate) throws IntrospectionException {
        final PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(beanClass).getPropertyDescriptors();
        final int len = propertyDescriptors.length - 1;
        for (int i = len; i >= 0; i--) {
            if (propertyDescriptors[i].getName().equalsIgnoreCase("class")) {
                continue;
            }
            final Field field = JPAUtils.getDeclaredFieldByName(beanClass, propertyDescriptors[i].getName());
            final Method getMethod = propertyDescriptors[i].getReadMethod();
            if (field != null) {
                if ((field.getAnnotations().length <= 0) && (getMethod.getAnnotations().length <= 0)
                        && nameColumn.equals(field.getName())) {
                    return field.getName();
                }

                Column annotation = field.getAnnotation(Column.class);
                if ((annotation == null) && (getMethod != null)) {
                    annotation = getMethod.getAnnotation(Column.class);
                }
                if ((annotation != null) && nameColumn.equals(annotation.name())) {
                    if (checkUpdate) {
                        if (annotation.updatable() && annotation.insertable()) {
                            return field.getName();
                        }
                    } else {
                        return field.getName();
                    }
                    continue;
                }

                EmbeddedId annotaEmb = field.getAnnotation(EmbeddedId.class);
                if ((annotaEmb == null) && (getMethod != null)) {
                    annotaEmb = getMethod.getAnnotation(EmbeddedId.class);
                }
                if (annotaEmb != null) {
                    final Object attributeName = JPAUtils.getAttributeNameColumn(field.getType(), nameColumn);
                    if (attributeName != null) {
                        return attributeName;
                    }
                }

                // If the attribute has an annotation different of Column or EmbeddedId
                if ((getMethod != null)
                        && (field.getAnnotation(OneToMany.class) == null) && (getMethod.getAnnotation(OneToMany.class) == null)
                        && (field.getAnnotation(ManyToOne.class) == null) && (getMethod.getAnnotation(ManyToOne.class) == null)
                        && (field.getAnnotation(ManyToMany.class) == null) && (getMethod.getAnnotation(ManyToMany.class) == null)
                        && nameColumn.equals(field.getName())) {
                    return field.getName();
                }
            }
        }
        return null;
    }

    /**
     * Get the name of field in beanClass represents nameColumn of table in database.
     *
     * @param beanClass
     *            The class of any bean.
     * @param nameColumn
     *            The name of column in data base.
     * @return The name of field of bean with the @JoinColumn(name='nameColumn').
     * @throws IntrospectionException
     */
    public static Object getAttributeNameJoinColumn(final Class beanClass, final String nameColumn) throws IntrospectionException {
        final PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(beanClass).getPropertyDescriptors();
        final int len = propertyDescriptors.length - 1;
        for (int i = len; i >= 0; i--) {
            if (propertyDescriptors[i].getName().equalsIgnoreCase("class")) {
                continue;
            }
            final Field field = JPAUtils.getDeclaredFieldByName(beanClass, propertyDescriptors[i].getName());
            if (field != null) {
                JoinColumn annotation = field.getAnnotation(JoinColumn.class);
                if (annotation == null) {
                    final Method getMethod = propertyDescriptors[i].getReadMethod();
                    if (getMethod != null) {
                        annotation = getMethod.getAnnotation(JoinColumn.class);
                    }
                }
                if (annotation != null) {
                    if (nameColumn.equals(annotation.name())) {
                        return field.getName();
                    }
                } else {
                    JoinColumns annotationJoins = field.getAnnotation(JoinColumns.class);
                    if (annotationJoins == null) {
                        final Method getMethod = propertyDescriptors[i].getReadMethod();
                        if (getMethod != null) {
                            annotationJoins = getMethod.getAnnotation(JoinColumns.class);
                        }
                    }
                    if (annotationJoins != null) {
                        final JoinColumn[] joins = annotationJoins.value();
                        final int max_joins = joins.length;
                        for (int j = 0; j < max_joins; j++) {
                            if (nameColumn.equals(joins[j].name())) {
                                return field.getName();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get the list with the names of @Transient fields in bean <code>beanClass</code>.
     *
     * @param beanClass
     * @return
     * @throws Exception
     */
    public static List<Object> getTransientColumnsName(final Class beanClass) throws Exception {
        final List<Object> transientList = new ArrayList<Object>();
        final PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(beanClass).getPropertyDescriptors();
        final int len = propertyDescriptors.length - 1;
        for (int i = len; i >= 0; i--) {
            if (propertyDescriptors[i].getName().equalsIgnoreCase("class")) {
                continue;
            }
            final Field field = JPAUtils.getDeclaredFieldByName(beanClass, propertyDescriptors[i].getName());
            if (field != null) {
                Annotation annotation = field.getAnnotation(Transient.class);
                if (annotation == null) {
                    final Method getMethod = propertyDescriptors[i].getReadMethod();
                    if (getMethod != null) {
                        annotation = getMethod.getAnnotation(Transient.class);
                    }
                }
                if (annotation != null) {
                    transientList.add(field.getName());
                }
            }
        }
        return transientList;
    }

    /**
     * Get the list with the names of @Transient fields in bean <code>entityName</code>.
     *
     * @param entityName
     *            The type of bean.
     * @return
     * @throws Exception
     */
    public static List<Object> getTransientColumnsName(final String entityName) throws Exception {
        try {
            return JPAUtils.getTransientColumnsName(Class.forName(entityName));
        } catch (final ClassNotFoundException ce) {
			JPAUtils.logger.debug("getTransientColumnsName -> NO FOUND CLASS", ce);
            return new ArrayList<Object>();
        }
    }

    /**
     * Verify if the field <code>column</code> of bean <code>beanClass</code> was declarated with @Transient annotation.
     *
     * @param column
     * @param beanClass
     * @return
     */
    public static boolean checkIsTransient(final String column, final Class<?> beanClass) {
        final Field field = JPAUtils.getDeclaredFieldByName(beanClass, column);
        if (field != null) {
            final Annotation annotation = field.getAnnotation(Transient.class);
            if (annotation != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the relations between columns of 'entityClass' bean and columns of other beans (ManyToOne).
     *
     * @param entityClass
     * @param fieldName
     * @param otherBeanClass
     * @param annotaJoinColumn
     * @throws Exception
     */
    // one bean
    protected static Hashtable<Object, Object> getAttributesRelation(final String entityClass,
            final String fieldName, final Class otherBeanClass, final JoinColumn annotaJoinColumn) throws Exception {
        final Hashtable<Object, Object> otherBeanKeys = new Hashtable<Object, Object>();
        String nameColumn = annotaJoinColumn.referencedColumnName();
        if ((nameColumn == null) || (nameColumn.length() <= 0)) {
            nameColumn = annotaJoinColumn.name();
        }
        if ((nameColumn == null) || (nameColumn.length() <= 0)) {
            nameColumn = fieldName;
        }
        if ((nameColumn != null) && (nameColumn.length() > 0)) {
            Object otherBeanAttr = null;
            String keyOtherBean = fieldName + OntimizeJpaNaming.DOT;
            final Field embId = JPAUtils.getEmbeddedIdField(otherBeanClass);
            if (embId != null) {
                otherBeanAttr = JPAUtils.getAttributeNameColumn(embId.getType(), nameColumn);
                if (otherBeanAttr != null) {
                    keyOtherBean += embId.getName() + OntimizeJpaNaming.DOT + otherBeanAttr.toString();
                }
            } else {
                otherBeanAttr = JPAUtils.getAttributeNameColumn(otherBeanClass, nameColumn);
                if (otherBeanAttr == null) {
                    otherBeanAttr = JPAUtils.getAttributeNameJoinColumn(otherBeanClass, nameColumn);
                }
                if (otherBeanAttr != null) {
                    keyOtherBean += otherBeanAttr.toString();
                }
            }
            if (otherBeanAttr != null) {
                otherBeanKeys.put(keyOtherBean, otherBeanAttr);
            } else {
                final Exception e = new Exception("It wasn't possible find any attribute of" +
                        " bean \"" + otherBeanClass.getName() + "\" related with bean \"" + entityClass + "\"" +
                        " throw the @ManyToOne relation \"" + fieldName + "\".");
                JPAUtils.logger.error(e.getMessage(), e);
                throw e;
            }
        }
        return otherBeanKeys;
    }

    /**
     * Get the relations between columns of EntityClass bean and columns of other beans (ManyToOne).
     *
     * @param entityClass
     * @param queryEntityClass
     * @param fieldName
     * @param otherBeanClass
     * @param annotaJoinColumn
     */
    // TWO different BEANS
    protected static Hashtable<Object, Object> getAttributesRelation(final String entityClass, final String queryEntityClass,
            final String fieldName, final Class otherBeanClass, final JoinColumn annotaJoinColumn) throws Exception {
        final Hashtable<Object, Object> otherBeanKeys = new Hashtable<Object, Object>();
        String nameColumn = annotaJoinColumn.referencedColumnName();
        if ((nameColumn == null) || (nameColumn.length() <= 0)) {
            nameColumn = annotaJoinColumn.name();
        }
        if ((nameColumn == null) || (nameColumn.length() <= 0)) {
            nameColumn = fieldName;
        }
        if ((nameColumn != null) && (nameColumn.length() > 0)) {
            Object beanAttr = JPAUtils.getAttributeNameColumn(Class.forName(queryEntityClass), annotaJoinColumn.name());
            if (beanAttr == null) {
                beanAttr = JPAUtils.getAttributeNameJoinColumn(Class.forName(entityClass), annotaJoinColumn.name());
            }
            if (beanAttr != null) {
                final Object otherBeanAttr = JPAUtils.getAttributeNameColumn(otherBeanClass, nameColumn);
                if (otherBeanAttr != null) {
                    otherBeanKeys.put(beanAttr, otherBeanAttr);
                }
            }
        }
        return otherBeanKeys;
    }

    /**
     * Get the name of columns, insertColumns, updateColumn using the JPA annotations of beans.
     *
     * @param entityClass
     *            The EntityBean of some Entity.
     * @param queryEntityClass
     *            The QueryEntityBean of some Entity.
     * @return The name of columns, insertColumns, updateColumns
     * @throws Exception
     */
    //    public static Hashtable<Object, Object> getAutoconfigureColumns(final String entityClass, final String queryEntityClass) throws Exception {
    //        return JPAUtils.getAutoconfigureColumns(entityClass, queryEntityClass, OntimizeJPANaming.LevelsAutoconfigure.all);
    //    }

    /**
     * Get the name of columns, insertColumns, updateColumn using the JPA annotations of beans.
     *
     * @param entityClass
     *            The EntityBean of some Entity.
     * @param queryEntityClass
     *            The QueryEntityBean of some Entity.
     * @param level
     *            Indicate if all fields of related beans are included as columns.
     * @return The name of columns, insertColumns, updateColumns
     * @throws Exception
     */
    //    public static Hashtable<Object, Object> getAutoconfigureColumns(final String entityClass, final String queryEntityClass, final DefaultParametersJpa.LevelsAutoconfigure level) throws Exception {
    //        if (!entityClass.equals(queryEntityClass)) {// Two beans
    //            final Hashtable<Object, Object> ret = JPAUtils.getAutoconfigureColumnsView(entityClass, queryEntityClass);
    //            final Vector queryColumns = new Vector();
    //            queryColumns.addAll(JPAUtils.getColumnNames(Class.forName(queryEntityClass)));
    //            ret.put(TableEntity.COLUMNS, queryColumns);
    //            return ret;
    //        } else {
    //            return JPAUtils.getAutoconfigureColumns(entityClass, level);
    //        }
    //    }

    /**
     * Get the name of columns, insertColumns, updateColumn using the JPA annotations of beans.
     *
     * @param entityClass
     *            The EntityBean of some Entity.
     * @param level
     *            Indicate if all fields of related beans are included as columns.
     * @return The name of columns, insertColumns, updateColumns
     * @throws Exception
     */
    // One BEAN
    //    protected static Hashtable<Object, Object> getAutoconfigureColumns(final String entityClass, final DefaultParametersJpa.LevelsAutoconfigure level) throws Exception {
    //        Hashtable<Object, Object> ret = null;
    //        final Vector columns = new Vector();
    //        final Vector insertColumns = new Vector();
    //        final Vector updateColumns = new Vector();
    //        final Class beanClass = Class.forName(entityClass);
    //        final PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(beanClass).getPropertyDescriptors();
    //        for (int i = 0; i < propertyDescriptors.length; i++) {
    //            if (propertyDescriptors[i].getName().equalsIgnoreCase("class")) {
    //                continue;
    //            }
    //            final Field field = JPAUtils.getDeclaredFieldByName(beanClass, propertyDescriptors[i].getName());
    //            // The annotations can be over the attribute declaration or over the get accessor method.
    //            final Method getMethod = propertyDescriptors[i].getReadMethod();
    //            if (field != null) {
    //                if ((field.getAnnotation(ManyToMany.class) != null) || (field.getAnnotation(OneToMany.class) != null)
    //                        || (getMethod.getAnnotation(ManyToMany.class) != null) || (getMethod.getAnnotation(OneToMany.class) != null)) {
    //                    continue;
    //                }
    //
    //                final String fieldName = field.getName();
    //                // There isn't any anotation in the attribute and get method
    //                if ((field.getAnnotations().length <= 0) && (getMethod.getAnnotations().length <= 0)) {
    //                    columns.add(fieldName);
    //                    insertColumns.add(fieldName);
    //                    updateColumns.add(fieldName);
    //                    continue;
    //                }
    //
    //                Annotation annotation = field.getAnnotation(EmbeddedId.class);
    //                if ((annotation == null) && (getMethod != null)) {
    //                    annotation = getMethod.getAnnotation(EmbeddedId.class);
    //                }
    //                if (annotation != null) {
    //                    final Hashtable<Object, Object> embeddedColums = JPAUtils.getAutoconfigureColumns(field.getType().getName(), level);
    //                    JPAUtils.addDotColumns(fieldName, columns, insertColumns, null, embeddedColums);
    //                    continue;
    //                }
    //
    //                annotation = field.getAnnotation(Embedded.class);
    //                if ((annotation == null) && (getMethod != null)) {
    //                    annotation = getMethod.getAnnotation(Embedded.class);
    //                }
    //                if (annotation != null) {
    //                    final Hashtable<Object, Object> embeddedColums = JPAUtils.getAutoconfigureColumns(field.getType().getName(), level);
    //                    JPAUtils.addDotColumns(fieldName, columns, insertColumns, updateColumns, embeddedColums);
    //                    continue;
    //                }
    //
    //                // If the attribute is Transient it will be considered a column but we only use it at bean level not at
    //                // database level.
    //                annotation = field.getAnnotation(Transient.class);
    //                if ((annotation == null) && (getMethod != null)) {
    //                    annotation = getMethod.getAnnotation(Transient.class);
    //                }
    //                if (annotation != null) {
    //                    columns.add(fieldName);
    //                    continue;
    //                }
    //
    //                annotation = field.getAnnotation(Column.class);
    //                if ((annotation == null) && (getMethod != null)) {
    //                    annotation = getMethod.getAnnotation(Column.class);
    //                }
    //                if (annotation != null) {
    //                    columns.add(fieldName);
    //                    if (((Column) annotation).insertable()) {
    //                        insertColumns.add(fieldName);
    //                    }
    //                    if (((Column) annotation).updatable()) {
    //                        updateColumns.add(fieldName);
    //                    }
    //                    continue;
    //                }
    //
    //                annotation = field.getAnnotation(OneToOne.class);
    //                if ((annotation == null) && (getMethod != null)) {
    //                    annotation = getMethod.getAnnotation(OneToOne.class);
    //                }
    //                if (annotation != null) {
    //                    Annotation annotaJoinColumns = field.getAnnotation(JoinColumns.class);
    //                    if ((annotaJoinColumns == null) && (getMethod != null)) {
    //                        annotaJoinColumns = getMethod.getAnnotation(JoinColumns.class);
    //                    }
    //                    if (annotaJoinColumns == null) {
    //                        annotaJoinColumns = field.getAnnotation(JoinColumn.class);
    //                        if ((annotaJoinColumns == null) && (getMethod != null)) {
    //                            annotaJoinColumns = getMethod.getAnnotation(JoinColumn.class);
    //                        }
    //                    }
    //                    if (annotaJoinColumns != null) {
    //                        Class otherBeanClass = ((OneToOne) annotation).targetEntity();
    //                        if ((otherBeanClass == null) || otherBeanClass.equals(void.class)) {
    //                            otherBeanClass = field.getType();
    //                        }
    //
    //                        Hashtable<Object, Object> colums = new Hashtable<Object, Object>();
    //                        colums = JPAUtils.getAutoconfigureRelatedBeanJoinColumns(otherBeanClass, annotaJoinColumns, fieldName);
    //                        if (level.equals(DefaultParametersJpa.LevelsAutoconfigure.main)) {
    //                            JPAUtils.addDotColumns(fieldName, columns, insertColumns, updateColumns, colums);
    //                        } else {
    //                            JPAUtils.addDotColumns(fieldName, null, insertColumns, null, colums);
    //                            colums.putAll(JPAUtils.getAutoconfigureRelatedBeanColumns(otherBeanClass, level));
    //                            JPAUtils.addDotColumns(fieldName, columns, null, updateColumns, colums);
    //                        }
    //                    }
    //                    continue;
    //                }
    //
    //                annotation = field.getAnnotation(ManyToOne.class);
    //                if ((annotation == null) && (getMethod != null)) {
    //                    annotation = getMethod.getAnnotation(ManyToOne.class);
    //                }
    //                if (annotation != null) {
    //                    Class otherBeanClass = ((ManyToOne) annotation).targetEntity();
    //                    if ((otherBeanClass == null) || otherBeanClass.equals(void.class)) {
    //                        otherBeanClass = field.getType();
    //                    }
    //                    Annotation annotaJoinColumns = field.getAnnotation(JoinColumns.class);
    //                    if ((annotaJoinColumns == null) && (getMethod != null)) {
    //                        annotaJoinColumns = getMethod.getAnnotation(JoinColumns.class);
    //                    }
    //                    if (annotaJoinColumns == null) {
    //                        annotaJoinColumns = field.getAnnotation(JoinColumn.class);
    //                        if ((annotaJoinColumns == null) && (getMethod != null)) {
    //                            annotaJoinColumns = getMethod.getAnnotation(JoinColumn.class);
    //                        }
    //                    }
    //                    if (annotaJoinColumns != null) {
    //                        Hashtable<Object, Object> colums = new Hashtable<Object, Object>();
    //                        colums = JPAUtils.getAutoconfigureRelatedBeanJoinColumns(otherBeanClass, annotaJoinColumns, fieldName);
    //                        if (level.equals(DefaultParametersJpa.LevelsAutoconfigure.main)) {
    //                            JPAUtils.addDotColumns(fieldName, columns, insertColumns, updateColumns, colums);
    //                        } else {
    //                            JPAUtils.addDotColumns(fieldName, null, insertColumns, null, colums);
    //                            colums.putAll(JPAUtils.getAutoconfigureRelatedBeanColumns(otherBeanClass, level));
    //                            JPAUtils.addDotColumns(fieldName, columns, null, updateColumns, colums);
    //                        }
    //                    }
    //                    continue;
    //                }
    //
    //                // The field have any other Annotation
    //                if (!columns.contains(fieldName)) {
    //                    columns.add(fieldName);
    //                }
    //                if (!insertColumns.contains(fieldName)) {
    //                    insertColumns.add(fieldName);
    //                }
    //                if (!updateColumns.contains(fieldName)) {
    //                    updateColumns.add(fieldName);
    //                }
    //            }
    //        }
    //        ret = new Hashtable<Object, Object>();
    //        ret.put(TableEntity.COLUMNS, columns);
    //        ret.put(TableEntity.INSERT_COLUMNS, insertColumns);
    //        ret.put(TableEntity.UPDATE_COLUMNS, updateColumns);
    //        return ret;
    //    }

    /**
     * Added to <code>columns</code> the Strings in field "Columns" of <code>embeddColums</code> preceded by "
     * <code>fieldName.</code>".
     * The same to <code>insertColumns</code> and <code>updateColumns</code>.
     *
     * @param fieldName
     *            The begin of new column.
     * @param columns
     *            A list of String instances.
     * @param insertColumns
     *            A list of String instances.
     * @param updateColumns
     *            A list of String instances.
     * @param embeddedColums
     *            A Hashtable with new String instances to added to lists.
     * @return
     */
    //    public static Hashtable<Object, Object> addDotColumns(final String fieldName,
    //            final Vector columns, final Vector insertColumns, final Vector updateColumns, final Hashtable<Object, Object> embeddedColums) {
    //        if ((embeddedColums == null) || (embeddedColums.size() <= 0)) {
    //            return new Hashtable<Object, Object>();
    //        }
    //
    //        final List columnsOther = (List) embeddedColums.get(TableEntity.COLUMNS);
    //        String aCol = null;
    //        if (columns != null) {
    //            for (int k = 0; (columnsOther != null) && (k < columnsOther.size()); k++) {
    //                aCol = fieldName + OntimizeJPANaming.DOT + columnsOther.get(k);
    //                if (!columns.contains(aCol)) {
    //                    columns.add(aCol);
    //                }
    //            }
    //        }
    //        if (insertColumns != null) {
    //            final List insertOther = (List) embeddedColums.get(TableEntity.INSERT_COLUMNS);
    //            for (int k = 0; (insertOther != null) && (k < insertOther.size()); k++) {
    //                aCol = fieldName + OntimizeJPANaming.DOT + insertOther.get(k);
    //                if (!insertColumns.contains(aCol)) {
    //                    insertColumns.add(aCol);
    //                }
    //            }
    //        }
    //        if (updateColumns != null) {
    //            final List updateOther = (List) embeddedColums.get(TableEntity.UPDATE_COLUMNS);
    //            for (int k = 0; (updateOther != null) && (k < updateOther.size()); k++) {
    //                aCol = fieldName + OntimizeJPANaming.DOT + updateOther.get(k);
    //                if (!updateColumns.contains(aCol)) {
    //                    updateColumns.add(aCol);
    //                }
    //            }
    //        }
    //
    //        final Hashtable<Object, Object> ret = new Hashtable<Object, Object>();
    //        if (columns != null) {
    //            ret.put(TableEntity.COLUMNS, columns);
    //        }
    //        if (insertColumns != null) {
    //            ret.put(TableEntity.INSERT_COLUMNS, insertColumns);
    //        }
    //        if (updateColumns != null) {
    //            ret.put(TableEntity.UPDATE_COLUMNS, updateColumns);
    //        }
    //        return ret;
    //    }

    /**
     * This method is called to "Hashtable<Object, Object> getAutoconfigureColumns(String entityClass)" to get
     * the names of attributes of beans related with @ManyToOne or [@OneToOne with @JoinColum].
     *
     * @param relatedBean
     *            The class of some bean
     * @return The name of fields in "relatedBean".
     * @throws Exception
     */
    // One BEAN
    //    protected static Hashtable<Object, Object> getAutoconfigureRelatedBeanColumns(final Class relatedBean) throws Exception {
    //        return JPAUtils.getAutoconfigureRelatedBeanColumns(relatedBean, DefaultParametersJpa.LevelsAutoconfigure.all);
    //    }

    /**
     * This method is called to "Hashtable<Object, Object> getAutoconfigureColumns(String entityClass)" to get
     * the names of attributes of beans related with @ManyToOne or [@OneToOne with @JoinColum].
     *
     * @param relatedBean
     *            The class of some bean
     * @param level
     *            Indicate if all fields of related beans are included as columns.
     * @return The name of fields in "relatedBean".
     * @throws Exception
     */
    //    protected static Hashtable<Object, Object> getAutoconfigureRelatedBeanColumns(final Class relatedBean, final DefaultParametersJpa.LevelsAutoconfigure level) throws Exception {
    //        Hashtable<Object, Object> ret = null;
    //        final Vector columns = new Vector();
    //        final Vector insertColumns = new Vector();
    //        final Vector updateColumns = new Vector();
    //
    //        final PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(relatedBean).getPropertyDescriptors();
    //        for (int i = 0; i < propertyDescriptors.length; i++) {
    //            if (propertyDescriptors[i].getName().equalsIgnoreCase("class")) {
    //                continue;
    //            }
    //            final Field field = JPAUtils.getDeclaredFieldByName(relatedBean, propertyDescriptors[i].getName());
    //            final Method getMethod = propertyDescriptors[i].getReadMethod();
    //            if (field != null) {
    //                if ((field.getAnnotation(ManyToMany.class) != null) || (field.getAnnotation(OneToOne.class) != null)
    //                        || (field.getAnnotation(OneToMany.class) != null) || (field.getAnnotation(ManyToOne.class) != null)
    //                        || (getMethod.getAnnotation(ManyToMany.class) != null) || (getMethod.getAnnotation(OneToOne.class) != null)
    //                        || (getMethod.getAnnotation(OneToMany.class) != null) || (getMethod.getAnnotation(ManyToOne.class) != null)) {
    //                    continue;
    //                }
    //
    //                final String fieldName = field.getName();
    //                if ((field.getAnnotation(Id.class) != null) || (getMethod.getAnnotation(Id.class) != null)) {
    //                    columns.add(fieldName);
    //                    insertColumns.add(fieldName);
    //                    updateColumns.add(fieldName);
    //                    continue;
    //                }
    //
    //                if ((field.getAnnotation(EmbeddedId.class) != null) || (getMethod.getAnnotation(EmbeddedId.class) != null)) {
    //                    final Hashtable<Object, Object> embeddedColums = JPAUtils.getAutoconfigureColumns(field.getType().getName(), DefaultParametersJpa.LevelsAutoconfigure.main);
    //                    final List columnsOther = (List) embeddedColums.get(TableEntity.COLUMNS);
    //                    for (int k = 0; k < columnsOther.size(); k++) {
    //                        columns.add(fieldName + DefaultParametersJpa.DOT + columnsOther.get(k));
    //                    }
    //                    final List insertOther = (List) embeddedColums.get(TableEntity.INSERT_COLUMNS);
    //                    for (int k = 0; k < insertOther.size(); k++) {
    //                        insertColumns.add(fieldName + DefaultParametersJpa.DOT + insertOther.get(k));
    //                    }
    //
    //                    continue;
    //                }
    //
    //                Annotation annotation = field.getAnnotation(Embedded.class);
    //                if ((annotation == null) && (getMethod != null)) {
    //                    annotation = getMethod.getAnnotation(Embedded.class);
    //                }
    //                if (annotation != null) {
    //                    final Hashtable<Object, Object> embeddedColums = JPAUtils.getAutoconfigureColumns(field.getType().getName(), DefaultParametersJpa.LevelsAutoconfigure.main);
    //                    JPAUtils.addDotColumns(fieldName, columns, insertColumns, updateColumns, embeddedColums);
    //                    continue;
    //                }
    //
    //                if (level.equals(DefaultParametersJpa.LevelsAutoconfigure.all)) {
    //                    // There isn't any anotation in the attribute and get method
    //                    if ((field.getAnnotations().length <= 0) && (getMethod.getAnnotations().length <= 0)) {
    //                        columns.add(fieldName);
    //                        insertColumns.add(fieldName);
    //                        updateColumns.add(fieldName);
    //                        continue;
    //                    }
    //
    //                    if ((field.getAnnotation(Column.class) != null) || (getMethod.getAnnotation(Column.class) != null)
    //                            || (field.getAnnotation(JoinColumn.class) != null) || (getMethod.getAnnotation(JoinColumn.class) != null)) {
    //                        columns.add(fieldName);
    //                        insertColumns.add(fieldName);
    //                        updateColumns.add(fieldName);
    //                        continue;
    //                    }
    //
    //                    // The field have any other attribute annotation (E.g. @Temporal)
    //                    columns.add(fieldName);
    //                    insertColumns.add(fieldName);
    //                    updateColumns.add(fieldName);
    //                }
    //            }
    //        }
    //        ret = new Hashtable<Object, Object>();
    //        ret.put(TableEntity.COLUMNS, columns);
    //        ret.put(TableEntity.INSERT_COLUMNS, insertColumns);
    //        ret.put(TableEntity.UPDATE_COLUMNS, updateColumns);
    //        return ret;
    //    }

    /**
     * Get the fields of relatedBean which represent the database columns equals of 'referencedColumnName' of JoinColum
     * annotation "annota".
     *
     * @param relatedBean
     * @param annota
     *            JoinColumn or JoinColumns annotation
     * @return Get the fields of relatedBean which represent the database columns equals of 'referencedColumnName' of
     *         JoinColum annotation "annota".
     * @throws Exception
     */
    //    protected static Hashtable<Object, Object> getAutoconfigureRelatedBeanJoinColumns(final Class relatedBean, final Annotation annota, final String fieldName) throws Exception {
    //        Hashtable<Object, Object> ret = null;
    //        final Vector columns = new Vector();
    //        final Vector insertColumns = new Vector();
    //        final Vector updateColumns = new Vector();
    //
    //        JoinColumn[] joins = null;
    //
    //        if (annota instanceof JoinColumns) {
    //            joins = ((JoinColumns) annota).value();
    //        }
    //
    //        if (annota instanceof JoinColumn) {
    //            joins = new JoinColumn[1];
    //            joins[0] = ((JoinColumn) annota);
    //        }
    //
    //        for (int i = 0; i < joins.length; i++) {
    //            final JoinColumn aJoin = joins[i];
    //            final String dbColumnName = aJoin.referencedColumnName();
    //            Object nameField = null;
    //            if ((dbColumnName != null) && (dbColumnName.length() > 0)) {
    //                nameField = JPAUtils.getAttributeNameColumn(relatedBean, dbColumnName, true);
    //            } else {// If you don't indicate referenceColumnName the relation is to other bean primaryKey
    //                final List<Object> list = JPAUtils.getKeyColumnNames(relatedBean);
    //                if (list.size() == 1) {
    //                    nameField = list.get(0);
    //                }
    //            }
    //            if (nameField != null) {
    //                columns.add(nameField);
    //                if (aJoin.insertable()) {
    //                    insertColumns.add(nameField);
    //                }
    //                if (aJoin.updatable()) {
    //                    updateColumns.add(nameField);
    //                }
    //            }
    //
    //        }
    //
    //        ret = new Hashtable<Object, Object>();
    //        ret.put(TableEntity.COLUMNS, columns);
    //        ret.put(TableEntity.INSERT_COLUMNS, insertColumns);
    //        ret.put(TableEntity.UPDATE_COLUMNS, updateColumns);
    //        return ret;
    //    }

    /**
     * Get the name of columns, insertColumns, updateColumn using the JPA annotations of beans.
     * This method is used when entity have two different beans.
     *
     * @param entityClass
     *            The EntityBean of some Entity.
     * @param queryEntityClass
     *            The QueryEntityBean of some Entity.
     * @return The name of columns, insertColumns, updateColumns
     * @throws Exception
     */
    // TWO BEANS
    //    public static Hashtable<Object, Object> getAutoconfigureColumnsView(final String entityClass, final String queryEntityClass) throws Exception {
    //        Hashtable<Object, Object> ret = null;
    //        final Vector insertColumns = new Vector();
    //        final Vector updateColumns = new Vector();
    //        final Class beanClass = Class.forName(entityClass);
    //        final PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(beanClass).getPropertyDescriptors();
    //        for (int i = 0; i < propertyDescriptors.length; i++) {
    //            if (propertyDescriptors[i].getName().equalsIgnoreCase("class")) {
    //                continue;
    //            }
    //            final Field field = JPAUtils.getDeclaredFieldByName(beanClass, propertyDescriptors[i].getName());
    //            // The annotations can be over the attribute declaration or over the get accessor method.
    //            final Method getMethod = propertyDescriptors[i].getReadMethod();
    //            if (field != null) {
    //                if ((field.getAnnotation(ManyToMany.class) != null) || (field.getAnnotation(OneToMany.class) != null)
    //                        || (getMethod.getAnnotation(ManyToMany.class) != null) || (getMethod.getAnnotation(OneToMany.class) != null)) {
    //                    continue;
    //                }
    //
    //                final String fieldName = field.getName();
    //
    //                Annotation annotation = field.getAnnotation(EmbeddedId.class);
    //                // If the attribute hasn't the annotation, it could be over the get accesor method.
    //                if ((annotation == null) && (getMethod != null)) {
    //                    annotation = getMethod.getAnnotation(EmbeddedId.class);
    //                }
    //                if (annotation != null) {
    //                    final Hashtable<Object, Object> embeddedColums = JPAUtils.getAutoconfigureColumnsView(field.getType().getName(), queryEntityClass);
    //                    // Only get the insert colums, because we can't modify the primay key
    //                    insertColumns.addAll((List) embeddedColums.get(TableEntity.INSERT_COLUMNS));
    //                    continue;
    //                }
    //
    //                annotation = field.getAnnotation(Embedded.class);
    //                // If the attribute hasn't the annotation, it could be over the get accesor method.
    //                if ((annotation == null) && (getMethod != null)) {
    //                    annotation = getMethod.getAnnotation(Embedded.class);
    //                }
    //                if (annotation != null) {
    //                    final Hashtable<Object, Object> embeddedColums = JPAUtils.getAutoconfigureColumnsView(field.getType().getName(), queryEntityClass);
    //                    insertColumns.addAll((List) embeddedColums.get(TableEntity.INSERT_COLUMNS));
    //                    updateColumns.addAll((List) embeddedColums.get(TableEntity.UPDATE_COLUMNS));
    //                    continue;
    //                }
    //
    //                // If the attribute is Transient it will be considered a column but we only use it at bean level not at
    //                // database level.
    //                annotation = field.getAnnotation(Transient.class);
    //                if ((annotation == null) && (getMethod != null)) {
    //                    annotation = getMethod.getAnnotation(Transient.class);
    //                }
    //                if (annotation != null) {
    //                    insertColumns.add(fieldName);
    //                    updateColumns.add(fieldName);
    //                    continue;
    //                }
    //
    //                annotation = field.getAnnotation(Column.class);
    //                if ((annotation == null) && (getMethod != null)) {
    //                    annotation = getMethod.getAnnotation(Column.class);
    //                }
    //                if (annotation != null) {
    //                    if (((Column) annotation).insertable()) {
    //                        insertColumns.add(fieldName);
    //                    }
    //                    if (((Column) annotation).updatable()) {
    //                        updateColumns.add(fieldName);
    //                    }
    //                    continue;
    //                }
    //
    //                annotation = field.getAnnotation(OneToOne.class);
    //                if ((annotation == null) && (getMethod != null)) {
    //                    annotation = getMethod.getAnnotation(OneToOne.class);
    //                }
    //                if (annotation != null) {
    //                    Annotation annotaJoinColumns = field.getAnnotation(JoinColumns.class);
    //                    if ((annotaJoinColumns == null) && (getMethod != null)) {
    //                        annotaJoinColumns = getMethod.getAnnotation(JoinColumns.class);
    //                    }
    //                    if (annotaJoinColumns != null) {
    //                        final JoinColumn[] colums = ((JoinColumns) annotaJoinColumns).value();
    //                        for (int j = 0; j < colums.length; j++) {
    //                            final Object attributeName = JPAUtils.getAttributeNameColumn(Class.forName(queryEntityClass), colums[j].name());
    //                            if (colums[j].insertable()) {
    //                                insertColumns.add(attributeName);
    //                            }
    //                            if (colums[j].updatable()) {
    //                                updateColumns.add(attributeName);
    //                            }
    //                        }
    //                    } else {
    //                        annotaJoinColumns = field.getAnnotation(JoinColumn.class);
    //                        if ((annotaJoinColumns == null) && (getMethod != null)) {
    //                            annotaJoinColumns = getMethod.getAnnotation(JoinColumn.class);
    //                        }
    //                        if (annotaJoinColumns != null) {
    //                            final Object attributeName = JPAUtils.getAttributeNameColumn(Class.forName(queryEntityClass), ((JoinColumn) annotaJoinColumns).name());
    //                            if (((JoinColumn) annotaJoinColumns).insertable()) {
    //                                insertColumns.add(attributeName);
    //                            }
    //                            if (((JoinColumn) annotaJoinColumns).updatable()) {
    //                                updateColumns.add(attributeName);
    //                            }
    //                        }
    //                    }
    //                    continue;
    //                }
    //
    //                annotation = field.getAnnotation(ManyToOne.class);
    //                if ((annotation == null) && (getMethod != null)) {
    //                    annotation = getMethod.getAnnotation(ManyToOne.class);
    //                }
    //                if (annotation != null) {
    //                    Annotation annotaJoinColumns = field.getAnnotation(JoinColumns.class);
    //                    if ((annotaJoinColumns == null) && (getMethod != null)) {
    //                        annotaJoinColumns = getMethod.getAnnotation(JoinColumns.class);
    //                    }
    //                    if (annotaJoinColumns != null) {
    //                        final JoinColumn[] colums = ((JoinColumns) annotaJoinColumns).value();
    //                        for (int j = 0; j < colums.length; j++) {
    //                            final Object attributeName = JPAUtils.getAttributeNameColumn(Class.forName(queryEntityClass), colums[j].name());
    //                            if (colums[j].insertable()) {
    //                                insertColumns.add(attributeName);
    //                            }
    //                            if (colums[j].updatable()) {
    //                                updateColumns.add(attributeName);
    //                            }
    //                        }
    //                    } else {
    //                        annotaJoinColumns = field.getAnnotation(JoinColumn.class);
    //                        if ((annotaJoinColumns == null) && (getMethod != null)) {
    //                            annotaJoinColumns = getMethod.getAnnotation(JoinColumn.class);
    //                        }
    //                        if (annotaJoinColumns != null) {
    //                            final Object attributeName = JPAUtils.getAttributeNameColumn(Class.forName(queryEntityClass), ((JoinColumn) annotaJoinColumns).name());
    //                            if (attributeName != null) {
    //                                if (((JoinColumn) annotaJoinColumns).insertable()) {
    //                                    insertColumns.add(attributeName);
    //                                }
    //                                if (((JoinColumn) annotaJoinColumns).updatable()) {
    //                                    updateColumns.add(attributeName);
    //                                }
    //                            }
    //                        }
    //                    }
    //                    continue;
    //                }
    //                // There isn't any anotation in the attribute and get method, or there isn't @JoinColum/s in ManyToOne
    //                insertColumns.add(fieldName);
    //                updateColumns.add(fieldName);
    //            }
    //        }
    //        ret = new Hashtable<Object, Object>();
    //        ret.put(TableEntity.INSERT_COLUMNS, insertColumns);
    //        ret.put(TableEntity.UPDATE_COLUMNS, updateColumns);
    //        return ret;
    //    }

    /**
     * Get the messagse to show to the client side when an exception had ocurred in query/update/insert/delete
     * operation.
     * If the exception was thrown from ABL engine, return a Hastable with one of those rules expeptions and a list with
     * all of them.
     *
     * @param se
     *            An exception during database operation (query/update/insert/delete operation). This exception could
     *            come from from ABL engine.
     * @param msgDefault
     *            Default message form client. It depends to current database operation.
     *
     * @return Get the messagse to show to the client side when an exception had ocurred in query/update/insert/delete
     *         operation.
     *         If the exception was thrown from ABL system, return a Hastable with one of those rules expeptions and a
     *         list with all of them.
     */
    public static Hashtable<String, Object> getExceptionMessage(final Exception se, final String msgDefault) {
        final Hashtable<String, Object> messages = new Hashtable<String, Object>();
        String msg = se.getMessage();
        Throwable cause = se.getCause();
        while ((cause != null) && (cause.getCause() != null)) {
            cause = cause.getCause();
            msg = cause.getMessage();
        }
        if (cause != null) {
            msg = cause.getMessage();
        }

        // try {
        // if (cause instanceof ConstraintException) {
        // final ConstraintException cex = (ConstraintException) cause;
        // final List<ConstraintFailure> failures = cex.getConstraintFailures();
        // if (failures.size() >= 1) {
        // msg = failures.get(0).getConstraintMessage();
        // final List<String> allConstraintMsg = new ArrayList<String>();
        // for (final ConstraintFailure failure : cex.getConstraintFailures()) {
        // final String msgCode = failure.getConstraintMessage();
        // if ((msgCode != null) && (msgCode.length() > 0)) {
        // allConstraintMsg.add(msgCode);
        // }
        // }
        // if (allConstraintMsg.size() > 0) {
        // messages.put(DefaultParametersJpa.LIST_ABL_CONSTRAINT_MESSAGES, allConstraintMsg);
        // }
        // }
        // }
        // } catch (final Exception ex) {
        // if (JPAUtils.DEBUG) {
        // ex.printStackTrace();
        // }
        // } catch (final Error er) {
        // if (JPAUtils.DEBUG) {
        // er.printStackTrace();
        // }
        // }
        if ((msg == null) || (msg.length() <= 0)) {
            msg = msgDefault;
        }
        messages.put(OntimizeJpaNaming.EXCEPTION_MESSAGE_DB_ERROR, msg);
        return messages;
    }

    // /**
    // * If there is columns with '.' change the values of these columns to references of beans.
    // * E.g.: If attributesValues = {profile.idprofile = 3, profile.name="admin"} then we change these two colums with
    // the instance of Profiles
    // * with idProfile=3 and name="admin" ->attributesValues = {profile = Profile(3,admin)}
    // *
    // * @param attributesValues A list of name columns.
    // * @param entityBean The main bean of any Entity.
    // * @param em The transaction reference
    // * @return A new list of attributesValues with ManyToOne related beans.
    // * @throws Exception
    // */
    // public static Hashtable checkEmbeddedAttributesValuesOneBean(Hashtable<Object, Object> attributesValues,
    // EntityManager em, String entityBean, String queryEntityBean, int operation) throws Exception{
    // return HibernateUtils.checkEmbeddedAttributesValuesOneBean(attributesValues, em, entityBean, queryEntityBean,
    // operation);
    // }
    //
    // /**
    // * Check if the values in "attributesValues" represents the information of a related bean with "entityClass" bean.
    // * @param attributesValues The information from client.
    // * @param manyToOneRelations The relations @ManyToOne of an entity class bean.
    // * @param em Transaction
    // * @param entityClass Some entity EntityClass bean.
    // * @param queyEntityClass Some entity QueryEntityClass bean.
    // * @return A hastable with valid values of "attributesValues"
    // * @throws Exception
    // */
    // public static Hashtable checkManyOneAttributesValues(Hashtable<Object, Object> attributesValues,
    // Hashtable<Object, BeanManyToOneDetails> manyToOneRelations, EntityManager em, String entityClass, String
    // queyEntityClass) throws Exception{
    // Hashtable validAttributesValues = (Hashtable) attributesValues.clone();
    // if(entityClass.equals(queyEntityClass))//One bean
    // return checkManyOneAttributesValuesOneBean(validAttributesValues, entityClass, em);
    // else return checkManyOneAttributesValuesTwoBeans(validAttributesValues, manyToOneRelations, em);
    // }
    //
    // /**
    // * If there is columns with '.' change the values of these columns to references of beans.
    // * E.g.: If attributesValues = {profile.idprofile = 3, profile.name="admin"} then we change these two colums with
    // the instance of Profiles
    // * with idProfile=3 and name="admin" ->attributesValues = {profile = Profile(3,admin)}
    // *
    // * @param attributesValues A list of name columns.
    // * @param entityBean The main bean of any Entity.
    // * @param em The transaction reference
    // * @return A new list of attributesValues with ManyToOne related beans.
    // * @throws Exception
    // */
    // public static Hashtable checkManyOneAttributesValuesOneBean(
    // Hashtable<Object, Object> attributesValues, String entityBean, EntityManager em) throws Exception{
    // return HibernateUtils.checkManyOneAttributesValuesOneBean(attributesValues, entityBean, em);
    // }
    //
    //
    // /**
    // * Check if the values in "attributesValues" represents the information of a related bean with "entityClass" bean.
    // * @param oneToOneRelations The relations @OneToOne of an entity class bean.
    // * @param em The transaction reference
    // * @param entityClass Some entity EntityClass bean.
    // * @param operation Indicate if the operation is a query/update/delete.
    // * @return A hastable with valid values of "attributesValues".
    // * @throws Exception
    // */
    // public static Hashtable checkOneOneAttributesValues( Hashtable validAttValues, Hashtable<Object,
    // BeanManyToOneDetails> oneToOneRelations,
    // EntityManager em, String entityClass, String queryEntityClass, int operation) throws Exception{
    // return HibernateUtils.checkOneOneAttributesValues(validAttValues, oneToOneRelations, em, entityClass,
    // queryEntityClass, operation);
    // }
    //
    // /**
    // * If there is columns with '.' change the values of these columns to references of beans.
    // * E.g.: If attributesValues = {profile.idprofile = 3, profile.name="admin"} then we change these two colums with
    // the instance of Profiles
    // * with idProfile=3 and name="admin" ->attributesValues = {profile = Profile(3,admin)}
    // *
    // * @param attributesValues A list of name columns.
    // * @param entityBean The main bean of any Entity.
    // * @param em The transaction reference
    // * @return A new list of attributesValues with ManyToOne related beans.
    // * @throws Exception
    // */
    // public static Hashtable checkOneOneAttributesValuesOneBean(
    // Hashtable<Object, Object> attributesValues, String entityBean, EntityManager em, int operation) throws Exception{
    // return HibernateUtils.checkOneOneAttributesValuesOneBean(attributesValues, entityBean, em, operation);
    // }

    /**
     * Get a list of embedded attributes in entityClass.
     *
     * @param entityClass
     * @return Get a list of embedded attributes in entityClass.
     */
    public static List<Object> getEmbeddedAttributes(final String entityClass) throws Exception {
        final List<Object> ret = new ArrayList<Object>();
        final Class beanClass = Class.forName(entityClass);
        final PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(beanClass).getPropertyDescriptors();
        for (int i = 0; i < propertyDescriptors.length; i++) {
            if (propertyDescriptors[i].getName().equalsIgnoreCase("class")) {
                continue;
            }

            final Field field = JPAUtils.getDeclaredFieldByName(beanClass, propertyDescriptors[i].getName());
            final Method getMethod = propertyDescriptors[i].getReadMethod();
            if (field != null) {
                final String fieldName = field.getName();
                Annotation annotaManyToOne = field.getAnnotation(Embedded.class);
                if ((annotaManyToOne == null) && (getMethod != null)) {
                    annotaManyToOne = getMethod.getAnnotation(Embedded.class);
                }
                if (annotaManyToOne != null) {
                    ret.add(fieldName);
                }
            }
        }
        return ret;
    }

    /**
     * Get a list with attributes of EntityClass which have a ManyToOne relation with another bean (the bean in
     * ManyToOne annotations).
     *
     * @param entityClass
     * @return A Hashtable with information of beans related with EntityClass bean
     */
    public static List<Object> getManyToOneAttributes(final String entityClass) throws Exception {
        final List<Object> ret = new ArrayList<Object>();
        final Class beanClass = Class.forName(entityClass);
        final PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(beanClass).getPropertyDescriptors();
        for (int i = 0; i < propertyDescriptors.length; i++) {
            if (propertyDescriptors[i].getName().equalsIgnoreCase("class")) {
                continue;
            }

            final Field field = JPAUtils.getDeclaredFieldByName(beanClass, propertyDescriptors[i].getName());
            final Method getMethod = propertyDescriptors[i].getReadMethod();
            if (field != null) {
                final String fieldName = field.getName();
                Annotation annotaManyToOne = field.getAnnotation(ManyToOne.class);
                if ((annotaManyToOne == null) && (getMethod != null)) {
                    annotaManyToOne = getMethod.getAnnotation(ManyToOne.class);
                }
                if (annotaManyToOne != null) {
                    ret.add(fieldName);
                }

                Annotation annotaEmbedded = field.getAnnotation(Embedded.class);
                if ((annotaEmbedded == null) && (getMethod != null)) {
                    annotaEmbedded = getMethod.getAnnotation(Embedded.class);
                }
                if (annotaEmbedded != null) {
                    final List embeddedManyOne = JPAUtils.getManyToOneAttributes(field.getType().getName());
                    for (int j = 0; j < embeddedManyOne.size(); j++) {
                        final String aCol = fieldName + OntimizeJpaNaming.DOT + embeddedManyOne.get(j);
                        if (!ret.contains(aCol)) {
                            ret.add(aCol);
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Get a list with attributes of EntityClass which have a OneToOne relation with another bean (the bean in ManyToOne
     * annotations).
     *
     * @param entityClass
     * @return A Hashtable with information of beans related with EntityClass bean
     */
    public static List<Object> getOneToOneAttributes(final String entityClass) throws Exception {
        final List<Object> ret = new ArrayList<Object>();
        final Class beanClass = Class.forName(entityClass);
        final PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(beanClass).getPropertyDescriptors();
        for (int i = 0; i < propertyDescriptors.length; i++) {
            if (propertyDescriptors[i].getName().equalsIgnoreCase("class")) {
                continue;
            }

            final Field field = JPAUtils.getDeclaredFieldByName(beanClass, propertyDescriptors[i].getName());
            final Method getMethod = propertyDescriptors[i].getReadMethod();
            if (field != null) {
                final String fieldName = field.getName();
                Annotation annotaManyToOne = field.getAnnotation(OneToOne.class);
                if ((annotaManyToOne == null) && (getMethod != null)) {
                    annotaManyToOne = getMethod.getAnnotation(OneToOne.class);
                }
                if (annotaManyToOne != null) {
                    if ((field.getAnnotation(JoinColumn.class) != null) || (getMethod.getAnnotation(JoinColumn.class) != null)
                            || (field.getAnnotation(JoinColumns.class) != null) || (getMethod.getAnnotation(JoinColumns.class) != null)) {
                        ret.add(fieldName);
                    }
                }

                Annotation annotaEmbedded = field.getAnnotation(Embedded.class);
                if ((annotaEmbedded == null) && (getMethod != null)) {
                    annotaEmbedded = getMethod.getAnnotation(Embedded.class);
                }
                if (annotaEmbedded != null) {
                    final List embeddedManyOne = JPAUtils.getOneToOneAttributes(field.getType().getName());
                    for (int j = 0; j < embeddedManyOne.size(); j++) {
                        final String aCol = fieldName + OntimizeJpaNaming.DOT + embeddedManyOne.get(j);
                        if (!ret.contains(aCol)) {
                            ret.add(aCol);
                        }
                    }
                }
            }
        }
        return ret;
    }

    public static Object cloneBean(final Object bean) throws Exception {
        final Class beanClass = bean.getClass();
        final Object cloneBean = JPAUtils.createInstanceofClass(beanClass);

        final PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(beanClass).getPropertyDescriptors();
        final int len = propertyDescriptors.length;
        for (int i = 0; i < len; i++) {
            if (propertyDescriptors[i].getName().equalsIgnoreCase("class")) {
                continue;
            }
            final Field field = JPAUtils.getDeclaredFieldByName(beanClass, propertyDescriptors[i].getName());
            if (field != null) {
                final Method getMethod = propertyDescriptors[i].getReadMethod();
                final Method setMethod = propertyDescriptors[i].getWriteMethod();

                Annotation annotation = field.getAnnotation(EmbeddedId.class);
                if (annotation == null) {
                    annotation = getMethod.getAnnotation(EmbeddedId.class);
                }
                if (annotation != null) {
                    final Object id = JPAUtils.cloneBean(getMethod.invoke(bean));
                    setMethod.invoke(cloneBean, id);
                    continue;
                }

                setMethod.invoke(cloneBean, getMethod.invoke(bean));
            }
        }

        return cloneBean;
    }

}


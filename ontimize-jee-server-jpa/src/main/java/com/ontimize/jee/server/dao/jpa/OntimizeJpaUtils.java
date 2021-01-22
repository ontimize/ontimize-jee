/**
 *
 */
package com.ontimize.jee.server.dao.jpa;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import javax.persistence.metamodel.Type.PersistenceType;

import org.springframework.beans.BeanUtils;

import com.ontimize.db.EntityResult;
import com.ontimize.db.NullValue;
import com.ontimize.jee.common.tools.EntityResultTools;
import com.ontimize.jee.server.dao.jpa.common.MappingInfo;
import com.ontimize.jee.server.dao.jpa.common.MappingInfoUtils;
import com.ontimize.jee.server.dao.jpa.dataconversors.DataConversorsUtil;
import com.ontimize.jee.server.dao.jpa.setup.ColumnMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class OntimizeJpaUtils.
 */
public final class OntimizeJpaUtils {

    private static final Logger logger = LoggerFactory.getLogger(OntimizeJpaUtils.class);

    /**
     * Instantiates a new ontimize jpa utils.
     */
    private OntimizeJpaUtils() {
        // do nothing
    }

    /**
     * Get an EntityResult with the information loading in "data". This method is called from
     * transformListToEntityResult(List<Object> data, List<Object> columnList)
     * @param data the data
     * @param columnList the column list; if its null, creates an EntityResult with ColumnNames from
     *        bean
     * @return the entity result
     * @throws Exception the exception
     */
    public static EntityResult transformListToEntityResultBeans(final List<?> data, List<String> columnList)
            throws Exception {
        if ((data == null) || (data.size() == 0)) {
            return new EntityResult(EntityResult.OPERATION_SUCCESSFUL_SHOW_MESSAGE, EntityResult.NODATA_RESULT);
        }

        if ((columnList == null) || (columnList.size() == 0)) {// Query all fields
            columnList = new ArrayList<>();
            columnList.addAll(JPAUtils.getColumnNames(data.get(0).getClass()));
        }

        EntityResult result = EntityResultTools.createEmptyEntityResult(columnList);

        for (final Object obj : data) {
            for (final String col : columnList) {
                if (col != null) {
                    final String column = col.toString();
                    try {
                        final Object methodReturned = OntimizeJpaUtils.getAttributeBean(column, obj);
                        ((List) result.get(column)).add(methodReturned);
                    } catch (final Exception ex) {
                        OntimizeJpaUtils.logger.error(null, ex);
                        ((List) result.get(column)).add(null);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get an EntityResult with the information loading in "data". This method is called from
     * transformListToEntityResult(Object data, List<Object> columnList)
     * @param data the data
     * @param columnList the column list; if its null, creates an EntityResult with ColumnNames from
     *        bean
     * @return the entity result
     * @throws Exception the exception
     */
    public static EntityResult transformListToEntityResultBeans(final Object data, List<String> columnList)
            throws Exception {
        if (data == null) {
            return new EntityResult(EntityResult.OPERATION_SUCCESSFUL_SHOW_MESSAGE, EntityResult.NODATA_RESULT);
        }

        if ((columnList == null) || (columnList.size() == 0)) {// Query all fields
            columnList = new ArrayList<>();
            columnList.addAll(JPAUtils.getColumnNames(data.getClass()));
        }

        EntityResult result = com.ontimize.db.EntityResultTools.createEmptyEntityResult(columnList);

        for (final String col : columnList) {
            if (col != null) {
                final String column = col.toString();
                try {
                    final Object methodReturned = OntimizeJpaUtils.getAttributeBean(column, data);
                    ((List) result.get(column)).add(methodReturned);
                } catch (final Exception ex) {
                    OntimizeJpaUtils.logger.error(null, ex);
                    ((List) result.get(column)).add(null);
                }
            }
        }
        return result;
    }

    /**
     * Get the value of 'attribute' field in 'aBean' instance.
     * @param attribute The name of field is going to query.
     * @param aBean The instance of a bean is going to query.
     * @return The value of setAttribute method of aBean.
     * @throws Exception the exception
     */
    public static Object getAttributeBean(final String attribute, final Object aBean) throws Exception {
        final int pos = attribute.indexOf(OntimizeJpaNaming.DOT);
        if (pos > 0) {
            final Object bean = OntimizeJpaUtils.getAttributeBeanLast(attribute.substring(0, pos), aBean);
            if (bean == null) {
                return null;
            }
            return OntimizeJpaUtils.getAttributeBean(attribute.substring(pos + 1), bean);
        }
        return OntimizeJpaUtils.getAttributeBeanLast(attribute, aBean);
    }

    /**
     * Get the value of 'attribute' field in 'aBean' instance. This method is called from
     * getAttributeBean and getAttributeMapBean.
     * @param attribute the attribute
     * @param aBean the a bean
     * @return the attribute bean last
     * @throws Exception the exception
     */
    protected static Object getAttributeBeanLast(final String attribute, final Object aBean) throws Exception {
        return OntimizeJpaUtils.changeDataTypeGet(
                JPAUtils.invokeGetValueDeclaredMethod(JPAUtils.getDeclaredGetMethod(attribute, aBean), aBean));
    }

    /**
     * Changes values from byte[] to BytesBlock and from NullValue to "null".
     * @param object Some value to set some attribute of a bean.
     * @return The same value of <code>object</code> in other format.
     */
    public static Object changeDataTypeGet(final Object object) {
        return object;
    }

    /**
     * Gets the SQL types.
     * @param selectQuery the select query
     * @param validAttributes the valid attributes
     * @param anEntityClass the an entity class
     * @param em the em
     * @return the SQL types
     * @throws Exception the exception
     */
    public static Map<String, Integer> getSQLTypes(final Query selectQuery, final List<String> validAttributes,
            final Class<?> anEntityClass, final EntityManager em)
            throws Exception {

        final EntityType<?> entity = em.getMetamodel().entity(anEntityClass);

        final Map<String, Integer> sqlTypes = new HashMap<>();
        for (final String vat : validAttributes) {
            // TODO what to do with non singular attributesor if they have a DOT
            // FIXME quitar el try-catch, es muy lento
            try {
                final SingularAttribute<?, ?> singularAttribute = entity.getSingularAttribute(vat);
                if (singularAttribute != null) {
                    sqlTypes.put(vat, Integer.valueOf(OntimizeJpaUtils.getJPASQLType(singularAttribute.getType())));
                }
            } catch (IllegalArgumentException e) {
                OntimizeJpaUtils.logger.trace("Unable to locate SingularAttribute in OntimizeJPAUtils.getSQLTypes", e);
                sqlTypes.put(vat, java.sql.Types.JAVA_OBJECT);
            }

        }

        return sqlTypes;
    }

    /**
     * Gets the SQL types.
     * @param selectQuery the select query
     * @param validAttributes the valid attributes
     * @param anEntityClass the an entity class
     * @return the SQL types
     * @throws Exception the exception
     */
    public static Map<String, Integer> getSQLTypes(final Query selectQuery, final List<String> validAttributes,
            final Class<?> anEntityClass) throws Exception {

        final Map<String, Integer> sqlTypes = new HashMap<>();
        for (final String vat : validAttributes) {

            sqlTypes.put(vat, java.sql.Types.JAVA_OBJECT);

        }

        return sqlTypes;
    }

    /**
     * Gets an SQL type corresponds to some Hibernate Type.
     * @param aType the a type
     * @return the JPASQL type
     */
    public static int getJPASQLType(final Type aType) {
        // TODO
        return java.sql.Types.JAVA_OBJECT;
    }

    /**
     * Gets the select column names.
     * @param selectQuery the select query
     * @return the select column names
     */
    public static List<String> getSelectColumnNames(final String selectQuery) {
        final List<String> cols = new ArrayList<>();

        // find from
        int fromIdx = selectQuery.indexOf("from");
        if (fromIdx < 0) {
            fromIdx = selectQuery.indexOf("FROM");
        }
        // find select
        int selectIdx = selectQuery.indexOf("select");
        if (selectIdx < 0) {
            selectIdx = selectQuery.indexOf("SELECT");
        }

        String str = null;
        if (selectIdx > 0) {
            // get select columns
            if ((fromIdx > 0) && (fromIdx > (selectIdx + 6))) {
                str = selectQuery.substring(selectIdx + 6, fromIdx);
            } else {
                str = selectQuery.substring(selectIdx + 6);
            }
            final String[] columns = str.split(",");
            // get col name or alias
            for (int i = 0; i < columns.length; i++) {
                final String col = columns[i].trim();
                int asIdx = col.indexOf(" as ");
                if (asIdx < 0) {
                    asIdx = col.indexOf(" AS ");
                }
                if (asIdx > 0) {
                    cols.add(col.substring(asIdx + 4).trim());
                } else {
                    cols.add(col.trim());
                }
            }
            // check cols doew not contains *
            for (final String col : cols) {
                if (col.contains("*")) {
                    return new ArrayList<>();
                }
            }
        }

        return cols;
    }

    /**
     * Gets the DB column name.
     * @param mappingInfo the mapping info
     * @param column the column
     * @return the DB column name
     */
    public static String getDBColumnName(MappingInfo mappingInfo, String column) {
        List<ColumnMapping> columnMappings = mappingInfo.getColumnMappings();
        if (columnMappings != null) {
            for (ColumnMapping cm : columnMappings) {
                if ((column != null) && column.equals(cm.getBeanAttribute())) {
                    return cm.getDbColumn();
                }
            }
        }
        return null;
    }

    public static Object mergeEmbeddedBean(Object currentValue, Object value, Map<String, ?> validAttributes,
            String embeddedBeanAttName) {
        Object result = value;
        if ((currentValue != null) && (value != null)) {
            Map<String, Object> tmp = new HashMap<>();
            Map<String, Map<String, Object>> organizedRelatedBeansAttributes = OntimizeJpaUtils
                .organizeDottedAttributesIntoHierarchy(validAttributes, tmp);
            Map<String, Object> map = organizedRelatedBeansAttributes.get(embeddedBeanAttName);
            if ((map != null) && (map.size() > 0)) {
                for (Entry<String, Object> entry : map.entrySet()) {
                    try {
                        String attToCopy = entry.getKey();
                        Method meth = BeanUtils.findDeclaredMethodWithMinimalParameters(value.getClass(),
                                MappingInfoUtils.buildGetterMethodName(attToCopy, null));
                        if (meth == null) {
                            OntimizeJpaUtils.logger.error("Key '" + attToCopy
                                    + "' does not have a getter method for embedded attributed declared on bean "
                                    + value.getClass());
                        } else {
                            Object valueToSet = meth.invoke(value);
                            Method meth2 = BeanUtils.findDeclaredMethodWithMinimalParameters(currentValue.getClass(),
                                    MappingInfoUtils.buildSetterMethodName(attToCopy, null));
                            if (meth2 == null) {
                                OntimizeJpaUtils.logger
                                    .error("Key '" + attToCopy
                                            + "' does not have a setter method for embedded attributed declared on bean "
                                            + currentValue.getClass());
                            } else {
                                meth2.invoke(currentValue, valueToSet);
                            }
                        }
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        OntimizeJpaUtils.logger.error("Error trying reflexion over embedded bean", e);
                    }
                }
                result = currentValue;
            }
        }
        return result;
    }

    /**
     * Reorganize attributes in beans if necessary. It checks if there are attributes in
     * attributesValues that should load an entity into a relation of the current entity.
     * @param attributesValues the attributes values
     * @param entity the entity
     * @param entityManager the entity manager
     * @return the map
     */
    public static Map<String, ?> reorganizeAttributesInBeansIfNecessary(EntityManager entityManager,
            ManagedType<?> entity, Map<String, ?> attributesValues) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Map<String, Object>> organizedRelatedBeansAttributes = OntimizeJpaUtils
            .organizeDottedAttributesIntoHierarchy(attributesValues, result);
        result.putAll(OntimizeJpaUtils.loadEntityRelationsPresentInAttributes(entityManager, entity,
                organizedRelatedBeansAttributes));
        return result;
    }

    /**
     * Load entity relations present in attributes. It loads 'entity' relation using
     * 'organizedRelatedBeansAttributes' information
     * @param entityManager the entity manager
     * @param entity the entity
     * @param organizedRelatedBeansAttributes the organized related beans attributes
     * @return the map
     */
    private static Map<String, Object> loadEntityRelationsPresentInAttributes(EntityManager entityManager,
            ManagedType<?> entity,
            Map<String, Map<String, Object>> organizedRelatedBeansAttributes) {
        Map<String, Object> result = new HashMap<>();
        for (Entry<String, Map<String, Object>> entry : organizedRelatedBeansAttributes.entrySet()) {
            Object beanValue = OntimizeJpaUtils.toBeanEntityRelation(entityManager, entity, entry.getKey(),
                    entry.getValue());
            if (beanValue != null) { // it means attribute relation exists in entity
                result.put(entry.getKey(), beanValue);
            }
            // else ignore because it is not relevant for persistence
        }
        return result;
    }

    /**
     * To bean entity relation. It checks if 'entity' has a relation attribute bean named
     * 'relationAttName', if so, it tries to load its bean using 'relationAttValues'
     * @param entityManager the entity manager
     * @param entity the entity
     * @param key the relationAttName
     * @param value the relationAttValues
     * @return the object bean or null it 'entity' doesn't have a relation named 'relationAttName'
     */
    private static Object toBeanEntityRelation(EntityManager entityManager, ManagedType<?> entity,
            String relationAttName, Map<String, Object> relationAttValues) {
        Attribute<?, ?> declaredAttribute = entity.getDeclaredAttribute(relationAttName);
        if ((declaredAttribute == null) && (entity instanceof IdentifiableType)) { // search in super types it it
                                                                                   // contains an attribute named
                                                                                   // 'relationAttName'
            IdentifiableType<?> supertype = ((IdentifiableType<?>) entity).getSupertype();
            while ((supertype != null) && (declaredAttribute == null)) {
                declaredAttribute = supertype.getDeclaredAttribute(relationAttName);
                supertype = supertype.getSupertype();
            }
        }
        if (declaredAttribute != null) { // it an attribute named 'relationAttName' is found
            Object beanValue = new NullValue();
            if (declaredAttribute.getPersistentAttributeType().equals(PersistentAttributeType.BASIC)) {
                // TODO que hacer en este caso?
            } else if (declaredAttribute.getPersistentAttributeType().equals(PersistentAttributeType.MANY_TO_ONE)
                    || declaredAttribute.getPersistentAttributeType()
                        .equals(PersistentAttributeType.ONE_TO_ONE)) {
                Object beanLoaded = OntimizeJpaUtils.loadEntityBean(entityManager, declaredAttribute.getJavaType(),
                        relationAttValues);
                if (beanLoaded != null) {
                    beanValue = beanLoaded;
                }
            } else if (declaredAttribute.getPersistentAttributeType().equals(PersistentAttributeType.EMBEDDED)) {
                Object embeddedBean = BeanUtils.instantiate(declaredAttribute.getJavaType());
                Set<SingularAttribute<? super Object, ?>> singularAttributes = ((ManagedType<Object>) declaredAttribute
                    .getDeclaringType()).getSingularAttributes();
                // Here a little BIG RECURSION!! WARNING, Be careful!!
                Map<String, Object> reorganizedAttributes = (Map<String, Object>) OntimizeJpaUtils
                    .reorganizeAttributesInBeansIfNecessary(entityManager,
                            entityManager.getMetamodel().managedType(declaredAttribute.getJavaType()),
                            relationAttValues);

                for (SingularAttribute<? super Object, ?> sattr : singularAttributes) {
                    if (sattr.getPersistentAttributeType().equals(PersistentAttributeType.BASIC)
                            || sattr.getPersistentAttributeType()
                                .equals(PersistentAttributeType.MANY_TO_ONE)
                            || sattr.getPersistentAttributeType()
                                .equals(PersistentAttributeType.ONE_TO_ONE)
                            || sattr.getPersistentAttributeType().equals(PersistentAttributeType.EMBEDDED)) {
                        // if basic then just set its value
                        final Method meth = BeanUtils.findDeclaredMethodWithMinimalParameters(
                                declaredAttribute.getJavaType(),
                                MappingInfoUtils.buildSetterMethodName(sattr.getName(), null));
                        if (meth == null) {
                            throw new RuntimeException("Embedded class '" + declaredAttribute.getJavaType()
                                .getCanonicalName() + "' does not have a setter method declared on bean for attribute "
                                    + sattr.getName());
                        }
                        try {
                            final Class<?>[] parameterTypes = meth.getParameterTypes();
                            if (parameterTypes.length != 1) {
                                throw new IllegalArgumentException(
                                        "this method has too many arguments: '" + meth + "'");
                            } else {
                                final Object converted = DataConversorsUtil
                                    .convert(reorganizedAttributes.get(sattr.getName()), parameterTypes[0]);
                                meth.invoke(embeddedBean, converted);
                            }
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
                            throw new RuntimeException(e1);
                        }
                    }
                    // else ignor attribute, not managed by our implementation
                }
                beanValue = embeddedBean;
            }
            return beanValue;
        }
        return null;
    }

    /**
     * Load bean. try loading a reference to a bean using its primary key, but if it it not able to
     * identify its primary key in relationAttValues then it does a query
     *
     * TODO: MIRAR LA POSIBILIDAD DE POSTERGAR LA CARGA/BUSQUEDA DEL BEAN HASTA EL MOMENTO DE LA
     * INSERCION DEVOLVIENDO UN PROXY ASI NO TENEMOS PORQUE TENER AQUI EL entityManager
     * @param entityManager the entity manager
     * @param javaType the java type
     * @param relationAttValues the relation att values
     * @return the object
     */
    private static Object loadEntityBean(EntityManager entityManager, Class<?> javaType,
            Map<String, Object> relationAttValues) {
        EntityType<Object> entity = (EntityType<Object>) entityManager.getMetamodel().entity(javaType);
        if (entity != null) {
            Object pKey = OntimizeJpaUtils.buildPKey(entity, relationAttValues);
            if (pKey != null) {
                return entityManager.getReference(javaType, pKey);
            } else {
                // TODO do a query
            }
        }
        return null;
    }

    private static Object buildPKey(EntityType<Object> entity, Map<String, Object> relationAttValues) {
        Set<SingularAttribute<? super Object, ?>> singularAttributes = entity.getSingularAttributes();

        List<String> keys = new ArrayList<>();
        for (Object o : relationAttValues.keySet()) {
            keys.add(o.toString());
        }

        boolean allInkeys = true;
        Map<String, Object> realKeys = new HashMap<>();
        for (SingularAttribute<? super Object, ?> sattr : singularAttributes) {
            if (sattr.isId()) {
                if (!keys.contains(sattr.getName())) {
                    allInkeys = false;
                } else {
                    realKeys.put(sattr.getName(), relationAttValues.get(sattr.getName()));
                }
            }
        }

        if (allInkeys) {
            Type<?> idType = entity.getIdType();
            if (idType.getPersistenceType().equals(PersistenceType.BASIC)) {
                // then it should be one and just one id attribute
                final SingularAttribute<?, ?> idClassAttribute = entity.getId(idType.getJavaType());
                // TODO convertir tipos primitivos
                return DataConversorsUtil.convert(realKeys.get(idClassAttribute.getName()), idType.getJavaType());
            } else {
                Class<?> idTypeClass = idType.getJavaType();
                Object entityBean = BeanUtils.instantiate(idTypeClass);

                // we have to build a composite id
                Set<SingularAttribute<? super Object, ?>> idClassAttributes = entity.getIdClassAttributes();
                for (SingularAttribute<? super Object, ?> sattr : idClassAttributes) {

                    final Method meth = BeanUtils.findDeclaredMethodWithMinimalParameters(idTypeClass,
                            MappingInfoUtils.buildSetterMethodName(sattr.getName(), null));
                    if (meth == null) {
                        throw new RuntimeException(
                                "Id class '" + idTypeClass.getCanonicalName()
                                        + "' does not have a setter method declared on bean for attribute "
                                        + sattr.getName());
                    }
                    try {
                        final Class<?>[] parameterTypes = meth.getParameterTypes();
                        if (parameterTypes.length != 1) {
                            throw new IllegalArgumentException("this method has too many arguments: '" + meth + "'");
                        } else {
                            final Object converted = DataConversorsUtil.convert(realKeys.get(sattr.getName()),
                                    parameterTypes[0]);
                            meth.invoke(entityBean, converted);
                        }
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
                        throw new RuntimeException(e1);
                    }
                }
                return entityBean;
            }
        }
        return null;
    }

    /**
     * Organize dotted attributes into hierarchy. Those attributes not organized are returned inside
     * 'remaining' parameter
     * @param attributesValues the attributes values
     * @param remaining the remaining
     * @return the map
     */
    private static Map<String, Map<String, Object>> organizeDottedAttributesIntoHierarchy(
            Map<String, ?> attributesValues, Map<String, Object> remaining) {
        Map<String, Map<String, Object>> result = new HashMap<>();
        for (Entry<String, ?> entry : attributesValues.entrySet()) {
            String key = entry.getKey();
            int dotIdx = key.indexOf('.');
            if ((dotIdx > 0) && (dotIdx < (key.length() - 1))) {
                String keyPart1 = key.substring(0, dotIdx);
                Map<String, Object> innerAttributes = result.get(keyPart1);
                if (innerAttributes == null) {
                    innerAttributes = new HashMap<>();
                    result.put(keyPart1, innerAttributes);
                }

                String keyPart2 = key.substring(dotIdx + 1);
                innerAttributes.put(keyPart2, entry.getValue());
            } else {
                // they sure are not bean attributes that need to be created (it is possible that in value there is
                // an already created attribute)
                remaining.put(key, entry.getValue());
            }
        }
        return result;
    }

}

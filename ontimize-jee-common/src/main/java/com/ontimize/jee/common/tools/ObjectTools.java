/**
 * ObjectTools.java 31/07/2013
 *
 *
 *
 */
package com.ontimize.jee.common.tools;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

/**
 * Utilidades de objetos.
 *
 * @author <a href=""></a>
 */
public final class ObjectTools {

    private static final Logger logger = LoggerFactory.getLogger(ObjectTools.class.getName());

    /**
     * Instantiates a new object tools.
     */
    private ObjectTools() {
        super();
    }

    /**
     * Si dos objetos comparables son diferentes lanza excepción.
     * @param oba oba
     * @param obb obb
     * @throws ObjectNotEqualsException de object not equals exception
     */
    public static void isEquals(final Object oba, final Object obb) throws ObjectNotEqualsException {
        boolean equals = true;
        if (oba == null) {
            equals = obb == null;
        } else {
            equals = oba.equals(obb);
        }
        if (!equals) {
            throw new ObjectNotEqualsException();
        }
    }

    /**
     * Si dos objetos comparables son diferentes devuelve false.
     * @param oba oba
     * @param obb obb
     * @return true, if successful
     */
    public static boolean safeIsEquals(final Object oba, final Object obb) {
        boolean equals = true;
        if (oba == null) {
            equals = obb == null;
        } else {
            equals = oba.equals(obb);
        }
        return equals;
    }

    /**
     * returns first not null.
     * @param <T> the generic type
     * @param values the values
     * @return the t
     */
    public static <T> T coalesce(T... values) {
        if ((values == null) || (values.length == 0)) {
            return null;
        }
        for (T ob : values) {
            if (ob != null) {
                return ob;
            }
        }
        return null;
    }

    /**
     * Check if in this map exists this object (null included), case insensitive.
     * @param map the map
     * @param object the object
     * @return true, if successful
     */
    public static boolean containsIgnoreCase(Map<?, ?> map, String object) {
        if (map == null) {
            return false;
        }
        for (Object o : map.keySet()) {
            if (ObjectTools.safeIsEquals(o, object)
                    || ((o != null) && (object != null) && o.toString().toUpperCase().equals(object.toUpperCase()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if in this list exists this object (null included), case insensitice.
     * @param list the list
     * @param object the object
     * @return true, if successful
     */
    public static boolean containsIgnoreCase(List<?> list, String object) {
        if (list == null) {
            return false;
        }
        for (Object o : list) {
            if (ObjectTools.safeIsEquals(o, object)
                    || ((o != null) && (object != null) && o.toString().toUpperCase().equals(object.toUpperCase()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Decode.
     * @param values the values
     * @return the object
     */
    public static Object decode(Object... values) {
        if ((values == null) || (values.length < 2)) {
            return null;
        }
        Object check = values[0];
        for (int i = 1; i < (values.length - 1); i += 2) {
            if (ObjectTools.safeIsEquals(check, values[i])) {
                return values[i + 1];
            }
        }
        return values[values.length - 1];
    }

    /**
     * Checks if is in.
     * @param check the check
     * @param options the options
     * @return true, if is in
     */
    public static boolean isIn(Object check, Object... options) {
        for (Object op : options) {
            if (ObjectTools.safeIsEquals(check, op)) {
                return true;
            }
        }
        return false;
    }

    public static String getStringRepresentation(Object o, boolean expandInnerChilds) {
        StringBuilder sb = new StringBuilder();

        ObjectTools.getStringRepresentation(o, expandInnerChilds, sb, 0, "Object", new ArrayList<>());

        return sb.toString();
    }

    private static void getStringRepresentation(Object o, boolean expandInnerChilds, StringBuilder sb, int tabLevel,
            String fieldName, List<Object> visited) {
        if (visited.contains(o)) {
            return;
        }
        visited.add(o);
        sb.append(StringTools.toString(StringTools.replicate("\t", tabLevel)) + fieldName + " : "
                + StringTools.toString(o) + "\r\n");

        if (o instanceof List) {
            for (int i = 0; i < ((List) o).size(); i++) {
                Object o2 = ((List) o).get(i);
                StringBuilder sb2 = new StringBuilder();
                ObjectTools.getStringRepresentation(o2, expandInnerChilds, sb2, tabLevel + 1, "[" + i + "]", visited);
                sb.append(sb2.toString());
            }
        }

        if (o != null) {
            List<Field> allFields = ReflectionTools.getAllFields(o.getClass());
            for (Field field : allFields) {
                // Ignore static
                try {
                    Object fieldValue = ReflectionTools.getFieldValue(o, field.getName());
                    if (!expandInnerChilds || Modifier.isStatic(field.getModifiers())) {
                        sb.append(StringTools.toString(StringTools.replicate("\t", tabLevel + 1)) + field.getName()
                                + " : " + StringTools.toString(fieldValue) + "\r\n");
                    } else {
                        StringBuilder sb2 = new StringBuilder();
                        ObjectTools.getStringRepresentation(fieldValue, expandInnerChilds, sb2, tabLevel + 1,
                                field.getName(), visited);
                        sb.append(sb2.toString());
                    }
                } catch (OntimizeJEERuntimeException ex) {
                    ObjectTools.logger.trace(null, ex);
                }
            }
        }
    }

}

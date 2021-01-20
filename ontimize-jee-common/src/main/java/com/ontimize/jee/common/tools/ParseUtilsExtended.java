package com.ontimize.jee.common.tools;

import java.awt.Font;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.xml.bind.DatatypeConverter;

import com.ontimize.util.ParseTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.util.Base64Utils;

/**
 * The Class ParseUtilsExtended.
 */
public class ParseUtilsExtended extends ParseTools {

    private static final Logger logger = LoggerFactory.getLogger(ParseUtilsExtended.class);

    public final static int BASE64 = 6464;

    protected final static Pattern ISO8601 = Pattern.compile(
            "^([\\+-]?\\d{4}(?!\\d{2}\\b))((-?)((0[1-9]|1[0-2])(\\3([12]\\d|0[1-9]|3[01]))?|W([0-4]\\d|5[0-2])(-?[1-7])?|(00[1-9]|0[1-9]\\d|[12]\\d{2}|3([0-5]\\d|6[1-6])))([T\\s]((([01]\\d|2[0-3])((:?)[0-5]\\d)?|24\\:?00)([\\.,]\\d+(?!:))?)?(\\17[0-5]\\d([\\.,]\\d+)?)?([zZ]|([\\+-])([01]\\d|2[0-3]):?([0-5]\\d)?)?)?)?$");

    /**
     * Gets the boolean.
     * @param s the s
     * @return the boolean
     */
    public static boolean getBoolean(Object s) {
        return ParseUtilsExtended.getBoolean(s, false);
    }

    public static Boolean getBooleanOrNull(Object s) {
        if (s != null) {
            String value = s.toString();
            if (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1")
                    || value.equalsIgnoreCase("y") || value.equalsIgnoreCase("s")) {
                return true;
            } else if (value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false") || value.equalsIgnoreCase("0")
                    || value.equalsIgnoreCase("n")) {
                return false;
            }
        }
        return null;
    }

    /**
     * Gets the boolean.
     * @param s the s
     * @param defaultValue the default value
     * @return the boolean
     */
    public static boolean getBoolean(Object s, boolean defaultValue) {
        if (s != null) {
            String value = s.toString();
            if (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1")
                    || value.equalsIgnoreCase("y") || value.equalsIgnoreCase("s")) {
                return true;
            } else if (value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false") || value.equalsIgnoreCase("0")
                    || value.equalsIgnoreCase("n")) {
                return false;
            }
        }
        return defaultValue;
    }

    /**
     * Convert into a map a string separate with : and ; in this mode: textToParse =
     * "one:two;three:four" return value
     *
     * <pre>
     * (one,two)
     * (trhee,four)
     * </pre>
     *
     * @param textToParse the text to parse
     * @param defaultValue the default value
     * @return the map
     */
    public static Map<String, String> getMap(String textToParse, Map<String, String> defaultValue) {
        if ((textToParse == null) || "".equals(textToParse)) {
            return defaultValue;
        }
        Map<String, String> res = new HashMap<>();
        String[] split = textToParse.split(Pattern.quote(";"));
        for (String part : split) {
            String[] mids = part.split(Pattern.quote(":"));
            if (mids.length == 2) {
                res.put(mids[0], mids[1]);
            } else {
                res.put(mids[0], mids[0]);
            }
        }
        return res;
    }

    /**
     * Obtiene un constante de una clase.
     * @param clazz the clazz
     * @param fieldName the field name
     * @param defaultValue the default value
     * @return the constant of class
     */
    public static Object getConstantOfClass(final Class<?> clazz, final String fieldName, final Object defaultValue) {
        try {
            Field declaredField = clazz.getDeclaredField(fieldName);
            return declaredField.get(null);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Analiza el nombre de una clase.
     * @param clazzName the s
     * @param defaultValue the default value
     * @return the clazz
     * @throws ClassNotFoundException the class not found exception
     */
    public static Class<?> getClazz(final String clazzName, final Class<?> defaultValue) throws ClassNotFoundException {
        if ((clazzName != null) && !"".equals(clazzName)) {
            return Class.forName(clazzName);
        }
        return defaultValue;
    }

    /**
     * Gets the class.
     * @param className the class name
     * @param parameters the parameters
     * @param values the values
     * @param defaultValue the default value
     * @return the class
     */
    public static Object getClazz(String className, Class[] parameters, Object[] values, Object defaultValue) {
        if ((className == null) || "".equals(className)) {
            return defaultValue;
        }
        try {
            Class<?> cl = Class.forName(className);
            Constructor<?> constructor = cl.getConstructor(parameters);
            return constructor.newInstance(values);
        } catch (Exception ex) {
            ParseUtilsExtended.logger.error(null, ex);
            return defaultValue;
        }
    }

    /**
     * Crea una instancia de una clase. Necesita un constructor sin parametros
     * @param <T> the generic type
     * @param clazzName the clazz name
     * @param defaultValue the default value
     * @return the clazz
     * @throws ClassNotFoundException the class not found exception
     */
    public static <T> T getClazzInstance(final String clazzName, final T defaultValue) throws ClassNotFoundException {
        try {
            Class<?> clazz = ParseUtilsExtended.getClazz(clazzName, null);
            return (T) clazz.newInstance();
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    /**
     * Gets the clazz instance.
     * @param <T> the generic type
     * @param clazzName the clazz name
     * @param parameters the parameters
     * @param defaultValue the default value
     * @return the clazz instance
     */
    public static <T> T getClazzInstance(String clazzName, Object[] parameters, T defaultValue) {
        try {
            return (T) ReflectionTools.newInstance(clazzName, parameters);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public static <T> T getClazzInstance(String clazzName, String defaultClassName, Object... parameters) {
        if (" ".equals(clazzName)) {
            return null;
        }
        try {
            return (T) ReflectionTools.newInstance(clazzName, parameters);
        } catch (Exception ex) {
            return (T) ReflectionTools.newInstance(defaultClassName, parameters);
        }
    }

    /**
     * Analiza el nombre de un metodo de una clase.
     * @param defaultValue the default value
     * @param clazz the clazz
     * @param methodName the method name
     * @param methodParameterTypes the method parameter types
     * @return the method
     */
    public static Method getMethod(final Method defaultValue, final Class<?> clazz, final String methodName,
            final Class<?>... methodParameterTypes) {
        try {
            Method declaredMethod = clazz.getDeclaredMethod(methodName, methodParameterTypes);
            declaredMethod.setAccessible(true);
            return declaredMethod;
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    /**
     * Analiza un campo de una clase.
     * @param fieldName the field name
     * @param clazz the clazz
     * @param defaultValue the default value
     * @return the field
     * @throws SecurityException the security exception
     */
    public static Field getField(final String fieldName, final Class<?> clazz, final Field defaultValue)
            throws SecurityException {
        try {
            Field declaredField = clazz.getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            return declaredField;
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    /**
     * Trocea la entrada en tantas partes como separators encuentre.
     * @param s the s
     * @param separator the separator
     * @param defaultValue the default value
     * @return the string list
     */
    public static List<String> getStringList(String s, String separator, List defaultValue) {
        if ((s == null) || "".equals(s)) {
            return defaultValue;
        }
        s = s.trim();
        List<String> res = new ArrayList<>();
        if (separator == null) {
            res.add(s);
            return res;
        }
        StringTokenizer st = new StringTokenizer(s, separator);
        while (st.hasMoreTokens()) {
            res.add(st.nextToken());
        }
        return res;
    }

    public static boolean getBoolean(String s, boolean defaultValue) {
        if (s != null) {
            if (s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("Y")
                    || s.equalsIgnoreCase("S") || s.equalsIgnoreCase("1")) {
                return true;
            } else if (s.equalsIgnoreCase("no") || s.equalsIgnoreCase("false") || s.equalsIgnoreCase("N")
                    || s.equalsIgnoreCase("0")) {
                return false;
            }
        }
        return defaultValue;
    }

    public static int getInteger(String s, int defaultValue) {
        if ((s != null) && !"".equals(s)) {
            return Integer.parseInt(s);
        }
        return defaultValue;
    }

    public static long getLong(String s, long defaultValue) {
        if ((s != null) && !"".equals(s)) {
            return Long.parseLong(s);
        }
        return defaultValue;
    }

    public static String getString(String s, String defaultValue) {
        if ((s != null) && !"".equals(s)) {
            return s;
        }
        return defaultValue;
    }

    public static double getDouble(String s, double defaultValue) {
        if ((s != null) && !"".equals(s)) {
            return Double.parseDouble(s);
        }
        return defaultValue;
    }

    public static float getFloat(String s, float defaultValue) {
        if ((s != null) && !"".equals(s)) {
            return Float.parseFloat(s);
        }
        return defaultValue;
    }

    public static Font getFont(String string, Font defaultFont) {
        if ((string == null) || "".equals(string)) {
            return defaultFont;
        }
        return Font.decode(string);
    }

    public static Map<String, String> getMap(String value, String separator1, String separator2,
            Map<String, String> defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return ParseTools.getTokensAt(value, separator1, separator2);
    }

    public static String getMapString(Map<Object, Object> map, String separator1, String separator2,
            String defaultValue) {
        if (map == null) {
            return defaultValue;
        }
        StringBuilder sb = new StringBuilder();
        for (Entry<Object, Object> entry : map.entrySet()) {
            if (entry.getKey() != null) {
                sb.append(entry.getKey());
                sb.append(ObjectTools.coalesce(separator2, ":"));
                sb.append(ObjectTools.coalesce(entry.getValue(), entry.getKey()));
                sb.append(ObjectTools.coalesce(separator1, ";"));
            }
        }

        return sb.toString();
    }

    public static Map getParametersPreffixed(Map<Object, Object> parameters, String preffix) {
        return ParseUtilsExtended.getParametersPreffixed(parameters, preffix, true);
    }

    public static Map getParametersPreffixed(Map<Object, Object> parameters, String preffix,
            Map otherParams) {
        Map parametersPreffixed = ParseUtilsExtended.getParametersPreffixed(parameters, preffix, false);

        if (otherParams != null) {
            for (Object key : otherParams.keySet()) {
                MapTools.safePut(parametersPreffixed, key, otherParams.get(key), true);
            }
        }

        for (Entry<Object, Object> entry : parameters.entrySet()) {
            String param = entry.getKey().toString();
            if (!param.startsWith(preffix) && !param.contains(".")) {
                MapTools.safePut(parametersPreffixed, entry.getKey(), entry.getValue(), true);
            }
        }

        return parametersPreffixed;
    }

    public static Map getParametersPreffixed(Map<Object, Object> parameters, String preffix,
            boolean includeGenerics) {
        Map params = new HashMap<>();
        for (Entry<Object, Object> entry : parameters.entrySet()) {
            String param = entry.getKey().toString();
            if (param.startsWith(preffix)) {
                MapTools.safePut(params, entry.getKey().toString().substring(preffix.length()), entry.getValue(),
                        false);
            } else if (!param.contains(".") && includeGenerics) {
                MapTools.safePut(params, entry.getKey(), entry.getValue(), true);
            }
        }

        return params;
    }

    /**
     * Gets the horizontal align.
     * @param s the s
     * @param defaultValue the default value
     * @return the horizontal align
     */
    public static int getHorizontalAlign(String s, int defaultValue) {
        if ("left".equals(s)) {
            return SwingConstants.LEFT;
        } else if ("right".equals(s)) {
            return SwingConstants.RIGHT;
        } else if ("center".equals(s)) {
            return SwingConstants.CENTER;
        } else {
            return defaultValue;
        }
    }

    /**
     * Gets the vertical align.
     * @param s the s
     * @param defaultValue the default value
     * @return the vertical align
     */
    public static int getVerticalAlign(String s, int defaultValue) {
        if ("top".equals(s)) {
            return SwingConstants.TOP;
        } else if ("bottom".equals(s)) {
            return SwingConstants.BOTTOM;
        } else if ("center".equals(s)) {
            return SwingConstants.CENTER;
        } else {
            return defaultValue;
        }
    }

    /**
     * Gets the placement.
     * @param s the s
     * @param defaultValue the default value
     * @return the placement
     */
    public static int getPlacement(String s, int defaultValue) {
        if ("top".equals(s)) {
            return SwingConstants.TOP;
        } else if ("left".equals(s)) {
            return SwingConstants.LEFT;
        } else if ("bottom".equals(s)) {
            return SwingConstants.BOTTOM;
        } else if ("right".equals(s)) {
            return SwingConstants.RIGHT;
        } else {
            return defaultValue;
        }
    }

    public static Object getValueForSQLType(Object object, int sqlType) {
        switch (sqlType) {
            case java.sql.Types.DATE:
            case java.sql.Types.TIME:
                return ParseUtilsExtended.parseDate(object);
            case java.sql.Types.TIMESTAMP:
                return ParseUtilsExtended.parseTimpestamp(object);
            case ParseUtilsExtended.BASE64:
                return ParseUtilsExtended.parseBase64(object);
            default:
                return ParseTools.getValueForSQLType(object, sqlType);
        }
    }

    public static Date parseDate(Object date) {
        if (date instanceof Long) {
            return new Date(((Long) date).longValue());
        } else if (date instanceof String) {
            String sDate = (String) date;
            if (ParseUtilsExtended.ISO8601.matcher(sDate) != null) {
                Calendar calendar = DatatypeConverter.parseDate(sDate);
                return calendar.getTime();
            }
        } else if (date instanceof Date) {
            return (Date) date;
        }
        return null;
    }

    public static Timestamp parseTimpestamp(Object time) {
        if (time instanceof Long) {
            return new Timestamp((Long) time);
        } else if (time instanceof String) {
            String sTime = (String) time;
            Calendar calendar = DatatypeConverter.parseTime(sTime);
            return new Timestamp(calendar.getTimeInMillis());
        } else if (time instanceof Timestamp) {
            return (Timestamp) time;
        }
        return null;
    }

    public static byte[] parseBase64(Object base64) {
        if (base64 instanceof String) {
            try {
                return Base64Utils.decode(((String) base64).toCharArray());
            } catch (Exception error) {

            }
        }
        return null;
    }

}

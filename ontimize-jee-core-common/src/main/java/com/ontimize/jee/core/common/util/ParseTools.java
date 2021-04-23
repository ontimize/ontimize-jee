package com.ontimize.jee.core.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


public class ParseTools {

    private static final Logger logger = LoggerFactory.getLogger(ParseTools.class);

    /**
     * Splits a String into a List, using the separator as delimiter
     * @param s the String to split
     * @param separator the string that separates the components
     * @return a List with the separated elements
     */
    public static List getTokensAt(String s, String separator) {
        List v = new ArrayList();
        if (s == null) {
            return v;
        }
        if (separator == null) {
            v.add(s);
            return v;
        }
        StringTokenizer st = new StringTokenizer(s, separator);
        while (st.hasMoreTokens()) {
            v.add(st.nextToken());
        }
        return v;
    }

    /**
     * Returns a Map with key-value corresponding with result to apply two 'tokenizer' actions. For
     * example, <br>
     * <br>
     * s= "field1:equivalentfield1;field2:equivalentfield2;...;fieldn:equivalententfieldn" <br>
     * separator1=";" <br>
     * separator2=":" <br>
     * <br>
     * returns <code>Map</code>: <br>
     * <br>
     * { field1 equivalentfield1} <br>
     * { field2 equivalentfield2} <br>
     * { ... ... } <br>
     * { fieldn equivalentfieldn} <br>
     * <br>
     *
     * Note: It also accepts : string =
     * "formfieldpk1;formfieldpk2:equivalententityfieldpk2;formfieldpk3...;formfieldpkn:equivalententityfieldpkn"
     * <br>
     * returning: <br>
     * <br>
     *
     * { field1 field1} <br>
     * { field2 equivalentfield2} <br>
     * { field3 field3} <br>
     * { ... ... } <br>
     * { fieldn equivalentfieldn} <br>
     * <br>
     * @param sValue The <code>String</code> with values
     * @param separator1 Separator for first <code>Tokenizer</code>
     * @param separator2 Separator for second <code>Tokenizer</code> for each token obtained previously
     * @return <code>Map</code> with key-value
     */
    public static Map getTokensAt(String sValue, String separator1, String separator2) {
        Map hashTokens = new HashMap();
        if ((sValue.indexOf(separator1) == -1) && (sValue.indexOf(separator2) == -1)) {
            hashTokens.put(sValue, sValue);
            return hashTokens;
        }
        StringTokenizer stSeparator1 = new StringTokenizer(sValue, separator1);
        while (stSeparator1.hasMoreTokens()) {
            StringTokenizer stSeparator2 = new StringTokenizer(stSeparator1.nextToken(), separator2);
            String tokenValue = stSeparator2.nextToken();
            if (!stSeparator2.hasMoreTokens()) {
                hashTokens.put(tokenValue, tokenValue);
            } else {
                hashTokens.put(tokenValue, stSeparator2.nextToken());
            }

        }
        return hashTokens;
    }

    /**
     * Parses a String to convert it to a boolean. If the String value is 'yes' or 'true' the boolean
     * response will be true, and if the String value is 'no' or 'false' the result will be false. If
     * the String is not one of the previous one specified previously, the defaultValue will be
     * returned.
     * @param string the string to parse
     * @param defaultValue the default value to return if no coincidence found
     * @return
     */
    public static boolean parseStringValue(String string, boolean defaultValue) {
        if (string == null) {
            return defaultValue;
        }
        if (string.equalsIgnoreCase("yes") || string.equalsIgnoreCase("true")) {
            return true;
        } else if (string.equalsIgnoreCase("no") || string.equalsIgnoreCase("false")) {
            return false;
        }
        return defaultValue;
    }

    /**
     * Debug method that prints the thread methods from the current execution point. This allows the
     * developer to know the call hierarchy in each point.
     * @param lines the number of thread lines to print
     */
    public static void printCurrentThreadMethods(int lines) {
        printCurrentThreadMethods(new Throwable(), lines);
    }

    public static void printCurrentThreadMethods(Throwable thowable, PrintWriter pw) {
        try {
            java.lang.reflect.Method method = Throwable.class.getMethod("printStackTrace",
                    new Class[] { PrintWriter.class });
            method.invoke(thowable, new Object[] { pw });
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage(), ex);
        }
    }

    public static void printCurrentThreadMethods(Throwable thowable, PrintStream ps) {
        try {
            java.lang.reflect.Method method = Throwable.class.getMethod("printStackTrace",
                    new Class[] { PrintStream.class });
            method.invoke(thowable, new Object[] { ps });
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage(), ex);
        }
    }

    /**
     * Debug method that prints the thread methods from a determined {@link Throwable} object. This
     * allows the developer to know the call hierarchy in a determined point.
     * @param thowable the object to inspect
     * @param lines the number of thread lines to print
     */
    public static void printCurrentThreadMethods(Throwable thowable, int lines) {
        try {
            if (lines < 2) {
                lines = 2;
            }

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            printCurrentThreadMethods(thowable, pw);

            String sText = sw.toString();
            // 2lineas
            StringBuilder sb = new StringBuilder();
            int nLines = 0;
            for (int i = 0; i < sText.length(); i++) {
                sb.append(sText.charAt(i));
                if (sText.charAt(i) == '\n') {
                    nLines++;
                }
                if (nLines >= lines) {
                    break;
                }
            }
            logger.info("Trace: current thread methods: {}", sb.toString());
        } catch (Exception ex) {
            logger.error(null, ex);
        }
    }

    public static StringBuilder printStackTrace(Throwable thowable) {
        return printStackTrace(thowable, 15);
    }

    public static StringBuilder printStackTrace(Throwable thowable, int lines) {
        StringBuilder sb = new StringBuilder();

        try {
            if (lines < 2) {
                lines = 2;
            }
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            printCurrentThreadMethods(thowable, pw);
            String sText = sw.toString();

            int nLines = 0;
            for (int i = 0; i < sText.length(); i++) {
                sb.append(sText.charAt(i));
                if (sText.charAt(i) == '\n') {
                    nLines++;
                }
                if (nLines >= lines) {
                    break;
                }
            }

            return sb;
        } catch (Exception ex) {
            logger.error("Error printCurrentThreadMethods()", ex);
        }

        return sb;
    }

    /**
     * Returns a String containing the thread methods from the current execution point. This allows the
     * developer to know the call hierarchy in each point.
     * @param lines
     * @return a String containing the thread methods from the execution point
     */
    public static String getCurrentThreadMethods(int lines) {
        return getCurrentThreadMethods(new Throwable(), lines);
    }

    /**
     * Returns a String containing the thread methods from the current execution point. This allows the
     * developer to know the call hierarchy in each point.
     * @param throwable
     * @param lines
     * @return a String containing the thread methods from the execution point
     */
    public static String getCurrentThreadMethods(Throwable throwable, int lines) {
        try {
            if (lines < 2) {
                lines = 2;
            }
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            printCurrentThreadMethods(throwable, pw);
            String texto = sw.toString();
            // 2 lines
            StringBuilder sb = new StringBuilder();
            int nLineas = 0;
            for (int i = 0; i < texto.length(); i++) {
                sb.append(texto.charAt(i));
                if (texto.charAt(i) == '\n') {
                    nLineas++;
                }
                if (nLineas >= lines) {
                    break;
                }
            }
            pw.close();
            return sb.toString();
        } catch (Exception ex) {
            logger.error("Error printCurrentThreadMethods()", ex);
            return "Error";
        }
    }

    public static boolean getBoolean(String s, boolean defaultValue) {
        if (s != null) {
            if (s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true")) {
                return true;
            } else if (s.equalsIgnoreCase("no") || s.equalsIgnoreCase("false")) {
                return false;
            }
        }
        return defaultValue;
    }

    public static String getString(String s, String defaultValue) {
        if ((s != null) && !"".equals(s)) {
            return s;
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


    public static String getCamelCase(String token) {
        if ((token == null) || (token.length() == 0)) {
            return null;
        }

        String leftSide = token.substring(0, 1);
        String rigthSide = token.substring(1);

        StringBuilder buffer = new StringBuilder();
        buffer.append(leftSide.toUpperCase());
        buffer.append(rigthSide);

        String s = buffer.toString();
        return s;
    }


    /**
     * Replaces a piece of text for another
     * @param source the source
     * @param text the text to change
     * @param newText the text that will replace the match
     * @param forceWord forces the substitution of the text
     * @return the original text with the replacements
     */
    public static String replaceText(String source, String text, String newText, boolean forceWord) {
        // TODO complete the javadoc, to determine what is forceWord
        String sHTMLContent = source;
        // Now search and replace
        int textLength = text.length();
        StringBuilder sbResult = new StringBuilder(sHTMLContent.length());
        int i = 0;
        for (i = 0; i <= (sHTMLContent.length() - textLength); i++) {
            // Additional comprobation. Previous characters are not letters to
            // consider the complete word
            if ((sHTMLContent.regionMatches(i, text, 0, textLength))
                    && (!(forceWord) || ((!Character.isLetterOrDigit(sHTMLContent.charAt(i - 1)))
                            && (!Character.isLetterOrDigit(sHTMLContent.charAt(i + textLength)))))) {
                sbResult.append(newText);
                // Now update the index i
                i = (i + textLength) - 1;
                // Loop continue to allo more than one substitution
            } else { // If there is not match then add the character
                sbResult.append(sHTMLContent.charAt(i));
            }
        }

        // Now, from contenidoHTML.length()-textLength to the end append
        for (int j = Math.max(i, sHTMLContent.length() - textLength); j < sHTMLContent.length(); j++) {
            sbResult.append(sHTMLContent.charAt(j));
        }
        return sbResult.toString();
    }

    /**
     * Replaces a piece of text for another
     * @param source the source
     * @param text the text to change
     * @param newText the text that will replace the match
     * @return the original text with the replacements
     */
    public static String replaceText(String source, String text, String nexText) {
        return replaceText(source, text, nexText, true);
    }

    /**
     * Checks if a parameter is contained by a Map (or by an extended class as {@link Properties})
     * @param key the parameter to check
     * @param prop the Map that contains the properties
     * @return the value of the property if it is contained by the prop, or null if the property is not
     *         in the Map
     */
    public static Object getParameterValue(String key, Map prop) {
        Object value = null;
        value = prop.get(key);
        if (value == null) {
            value = prop.get(key.toLowerCase());
        }
        return value;
    }

    public static final String BIG_DECIMAL = "BigDecimal";

    public static final String BIG_INTEGER = "BigInteger";

    public static final String DOUBLE = "Double";

    public static final String LONG = "Long";

    public static final String SHORT = "Short";

    public static final String INTEGER = "Integer";

    public static final String STRING = "String";

    public static final String FLOAT = "Float";

    public static final int BIG_DECIMAL_ = 0;

    public static final int BIG_INTEGER_ = 1;

    public static final int DOUBLE_ = 2;

    public static final int LONG_ = 3;

    public static final int INTEGER_ = 4;

    public static final int SHORT_ = 5;

    public static final int STRING_ = 6;

    public static final int FLOAT_ = 7;

    public static int getTypeForName(String typeName, int defaultValue) {
        try {
            return ParseTools.getIntTypeForName(typeName);
        } catch (Exception e) {
            ParseTools.logger.trace(null, e);
            return defaultValue;
        }
    }

    public static int getIntTypeForName(String typeName) {
        if (ParseTools.BIG_DECIMAL.equalsIgnoreCase(typeName)) {
            return ParseTools.BIG_DECIMAL_;
        } else if (ParseTools.BIG_INTEGER.equalsIgnoreCase(typeName)) {
            return ParseTools.BIG_INTEGER_;
        } else if (ParseTools.DOUBLE.equalsIgnoreCase(typeName)) {
            return ParseTools.DOUBLE_;
        } else if (ParseTools.LONG.equalsIgnoreCase(typeName)) {
            return ParseTools.LONG_;
        } else if (ParseTools.SHORT.equalsIgnoreCase(typeName)) {
            return ParseTools.SHORT_;
        } else if (ParseTools.INTEGER.equalsIgnoreCase(typeName)) {
            return ParseTools.INTEGER_;
        } else if (ParseTools.STRING.equalsIgnoreCase(typeName)) {
            return ParseTools.STRING_;
        } else if (ParseTools.FLOAT.equalsIgnoreCase(typeName)) {
            return ParseTools.FLOAT_;
        }
        throw new IllegalArgumentException(
                ParseTools.class.getName() + ": type " + typeName + " is not one of the identified data types");
    }

    public static int getSQLType(String typeName) {
        int intTypeForName = ParseTools.getIntTypeForName(typeName);
        return ParseTools.getSQLType(intTypeForName, Types.VARCHAR);
    }

    public static int getSQLType(int parseType, int defaultType) {
        switch (parseType) {
            case BIG_INTEGER_:
                return Types.BIGINT;
            case BIG_DECIMAL_:
                return Types.DECIMAL;
            case DOUBLE_:
                return Types.DOUBLE;
            case LONG_:
                return Types.BIGINT;
            case INTEGER_:
                return Types.INTEGER;
            case SHORT_:
                return Types.SMALLINT;
            case STRING_:
                return Types.VARCHAR;
            case FLOAT_:
                return Types.FLOAT;
            default:
                return defaultType;
        }
    }

    public static Object getValueForSQLType(Object object, int sqlType) {
        if (object == null) {
            return null;
        }
        switch (sqlType) {
            case java.sql.Types.INTEGER:
                return new Integer(Integer.parseInt(object.toString()));
            case java.sql.Types.BIGINT:
                return new BigInteger(object.toString());
            case java.sql.Types.DOUBLE:
                return new Double(Double.parseDouble(object.toString()));
            case java.sql.Types.FLOAT:
                return new Float(Float.parseFloat(object.toString()));
            case java.sql.Types.DATE:
            case java.sql.Types.TIME:
            case java.sql.Types.TIMESTAMP:
                // TODO parse dates
                return new Date(object.toString());
            case java.sql.Types.VARCHAR:
                return object.toString();
            case java.sql.Types.LONGVARCHAR:
                return object.toString();
            case java.sql.Types.BOOLEAN:
            case java.sql.Types.BIT:
                return Boolean.valueOf(object.toString());
            case java.sql.Types.BINARY:
                return new Object();
            default:
                break;
        }
        return object;
    }

    public static Object getValueForClassType(Object object, int classType) {
        if ((object == null) || (object.toString().trim().length() == 0)) {
            return null;
        }
        try {
            switch (classType) {
                case BIG_INTEGER_:
                    ParseTools.logger.debug("Using BIGINTEGER");
                    if (object instanceof BigInteger) {
                        return object;
                    }
                    return new BigInteger(object.toString());
                case BIG_DECIMAL_:
                    ParseTools.logger.debug("Using BIGDECIMAL");
                    if (object instanceof BigDecimal) {
                        return object;
                    }
                    return new BigDecimal(object.toString());
                case DOUBLE_:
                    ParseTools.logger.debug("Using DOUBLE");
                    if (object instanceof Double) {
                        return object;
                    }
                    return new Double(object.toString());
                case LONG_:
                    ParseTools.logger.debug("Using LONG");
                    if (object instanceof Long) {
                        return object;
                    }
                    return new Long(object.toString());
                case INTEGER_:
                    if (object instanceof Integer) {
                        return object;
                    }
                    return new Integer(object.toString());
                case SHORT_:
                    ParseTools.logger.debug("Using SHORT");
                    if (object instanceof Short) {
                        return object;
                    }
                    return new Short(object.toString());
                case STRING_:
                    ParseTools.logger.debug("Using STRING");
                    if (object instanceof String) {
                        return object;
                    }
                    return object.toString();
                case FLOAT_:
                    ParseTools.logger.debug("Using FLOAT");
                    if (object instanceof Float) {
                        return object;
                    }
                    return new Float(object.toString());
                default:
                    if (object instanceof Integer) {
                        return object;
                    }
                    return new Integer(object.toString());
            }
        } catch (Exception e) {
            ParseTools.logger.debug(null, e);
            return object;
        }
    }

    public static Class getClassType(int classType) {
        try {
            switch (classType) {
                case BIG_INTEGER_:
                    ParseTools.logger.debug("Using BIGINTEGER");
                    return BigInteger.class;
                case BIG_DECIMAL_:
                    ParseTools.logger.debug("Using BIGDECIMAL");
                    return BigDecimal.class;
                case DOUBLE_:
                    ParseTools.logger.debug("Using DOUBLE");
                    return Double.class;
                case LONG_:
                    ParseTools.logger.debug("Using LONG");
                    return Long.class;
                case INTEGER_:
                    ParseTools.logger.debug("Using INTEGER");
                    return Integer.class;
                case SHORT_:
                    ParseTools.logger.debug("Using SHORT");
                    return Short.class;
                case STRING_:
                    ParseTools.logger.debug("Using STRING");
                    return String.class;
                case FLOAT_:
                    ParseTools.logger.debug("Using FLOAT");
                    return Float.class;
                default:
                    return Object.class;
            }
        } catch (Exception e) {
            ParseTools.logger.debug(null, e);
            return Object.class;
        }
    }

    public static final String SECOND = "second";

    public static final String MINUTE = "minute";

    public static final String HOUR = "hour";

    public static final String DAY = "day";

    public static final String MONTH = "month";

    public static final String YEAR = "year";

    public static int getCalendarField(String calendarField) {
        if (ParseTools.SECOND.equalsIgnoreCase(calendarField)) {
            return Calendar.SECOND;
        } else if (ParseTools.MINUTE.equalsIgnoreCase(calendarField)) {
            return Calendar.MINUTE;
        } else if (ParseTools.HOUR.equalsIgnoreCase(calendarField)) {
            return Calendar.HOUR_OF_DAY;
        } else if (ParseTools.DAY.equalsIgnoreCase(calendarField)) {
            return Calendar.DAY_OF_MONTH;
        } else if (ParseTools.MONTH.equalsIgnoreCase(calendarField)) {
            return Calendar.MONTH;
        } else if (ParseTools.YEAR.equalsIgnoreCase(calendarField)) {
            return Calendar.YEAR;
        } else {
            ParseTools.logger.debug(ParseTools.class.getName() + " : Default Calendar Field - day of month");
            return Calendar.DAY_OF_MONTH;
        }
    }

    /**
     * Creates a String with the elements contained by a List, separating the contents by the separator
     * passed as parameter
     * @param v
     * @param s the separator to use
     * @return the String with the List elements separated by the separator
     */
    public static String ListToStringSeparateBy(List v, String s) {
        if (v != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < v.size(); i++) {
                sb.append(v.get(i));
                if (i < (v.size() - 1)) {
                    sb.append(s);
                }
            }
            return sb.toString();
        }
        return null;
    }

}

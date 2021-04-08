/**
 *
 *
 *
 */
package com.ontimize.jee.common.tools;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Class StringTools.
 *
 * @author <a href=""></a>
 */
public final class StringTools {

    /** The Constant WEOL. */
    public final static String WEOL = "\r\n";

    public static final Object TAB = "\t";

    /**
     * Instantiates a new string utils.
     */
    private StringTools() {
        super();
    }

    public static String truncateString(String text, int maxLenght) {
        if ((text == null) || (maxLenght < 0)) {
            return text;
        }
        // Limit to maxLength - 1 because "." char consume less space than other characters
        if (text.length() > (maxLenght - 1)) {
            text = text.substring(0, maxLenght - 3) + "...";
        }
        return text;
    }

    public static String concat(String... toConcat) {
        StringBuilder sb = new StringBuilder();
        if ((toConcat == null) || (toConcat.length == 0)) {
            return "";
        }
        for (String part : toConcat) {
            sb.append(part);
        }
        return sb.toString();
    }

    public static boolean isEmpty(String in) {
        return (in == null) || in.trim().isEmpty();
    }

    /**
     * Return String result of concat all input Strings or empty String if non of then valid. Note: Null
     * Strings are ignored.
     * @param separator
     * @param strings
     * @return
     */
    public static String concatWithSeparator(String separator, String... strings) {
        return StringTools.concatWithSeparator(separator, false, Arrays.asList(strings));
    }

    public static String concatWithSeparator(String separator, boolean avoidEmpty, String... strings) {
        return StringTools.concatWithSeparator(separator, avoidEmpty, Arrays.asList(strings));
    }

    public static String concatWithSeparator(String separator, boolean avoidEmpty, List<String> strings) {
        StringBuilder sb = new StringBuilder();
        if ((strings == null) || strings.isEmpty()) {
            return "";
        }
        boolean someRecord = false;
        for (int i = 0; i < strings.size(); i++) {
            if (strings.get(i) != null) {
                if (StringTools.isEmpty(strings.get(i)) && avoidEmpty) {
                    continue;
                }
                if ((i > 0) && someRecord && (separator != null)) {
                    sb.append(separator);
                }
                sb.append(strings.get(i));

                someRecord = true;
            }
        }
        return sb.toString();
    }

    public static String toString(Object object) {
        return object == null ? null : object.toString();
    }

    public static String replicate(String string, int times) {
        if ((string == null) || (times <= 0)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(string);
        }
        return sb.toString();
    }

    public static Object avoidNull(String string) {
        return StringTools.avoidNull(string, "");
    }

    public static String avoidNull(String string, String defaultValue) {
        if (string == null) {
            return defaultValue;
        }
        return string;
    }

    public static Pair<String, Integer> replaceAll(String text, String repFrom, String repTo) {
        Pattern pattern = Pattern.compile(repFrom, Pattern.LITERAL);
        Matcher matcher = pattern.matcher(text);
        int counter = 0;
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            counter++;
            matcher.appendReplacement(sb, repTo);
        }
        matcher.appendTail(sb);

        return new Pair<>(sb.toString(), counter);
    }

}

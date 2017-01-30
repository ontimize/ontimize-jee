package com.ontimize.jee.common.tools;

import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.util.ParseUtils;

/**
 * The Class ParseUtilsExtended.
 */
public class ParseUtilsExtended extends ParseUtils {

	private static final Logger	logger	= LoggerFactory.getLogger(ParseUtilsExtended.class);
    /**
     * Gets the boolean.
     *
     * @param s
     *            the s
     * @return the boolean
     */
    public static boolean getBoolean(Object s) {
        return ParseUtilsExtended.getBoolean(s, false);
    }

    /**
     * Gets the boolean.
     *
     * @param s
     *            the s
     * @param defaultValue
     *            the default value
     * @return the boolean
     */
    public static boolean getBoolean(Object s, boolean defaultValue) {
        if ((s != null)) {
            String value = s.toString();
            if (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1")) {
                return true;
            } else if (value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false") || value.equalsIgnoreCase("0")) {
                return false;
            }
        }
        return defaultValue;
    }

	/**
	 * Convert into a map a string separate with : and ; in this mode:
	 * textToParse = "one:two;three:four"
	 * return value
	 * <pre> 
	 * (one,two)
	 * (trhee,four)
	 * </pre>
	 *
	 * @param textToParse
	 *            the text to parse
	 * @param defaultValue
	 *            the default value
	 * @return the map
	 */
	public static Map<String, String> getMap(String textToParse, Map<String, String> defaultValue) {
		if ((textToParse == null) || "".equals(textToParse)) {
			return defaultValue;
		}
		Map<String, String> res = new Hashtable<>();
		String[] split = textToParse.split(Pattern.quote(";"));
		for (String part : split) {
			String[] mids = part.split(Pattern.quote(":"));
			if (mids.length == 2) {
				res.put(mids[0], mids[1]);
			} else {
				ParseUtilsExtended.logger.warn("Error parsing map, must be 2 size");
			}
		}
		return res;
	}
}

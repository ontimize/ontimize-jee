package com.ontimize.jee.desktopclient.tools;

import java.awt.Font;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.xml.bind.DatatypeConverter;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.images.ImageManager;
import com.ontimize.jee.common.tools.MapTools;
import com.ontimize.jee.common.tools.ObjectTools;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.util.Base64Utils;
import com.ontimize.util.ParseTools;
import com.ontimize.util.ParseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ParseUtilsExtended.
 */
public class ParseUtilsExtended extends ParseUtils {

    private static final Logger logger = LoggerFactory.getLogger(ParseUtilsExtended.class);


    public static String getTranslatedString(String s, String defaultValue) {
        return ParseUtilsExtended.getTranslatedString(s, defaultValue, null);
    }

    public static String getTranslatedString(String s, String defaultValue, ResourceBundle bundle) {
        String value = ParseUtilsExtended.getString(s, defaultValue);
        return ApplicationManager.getTranslation(value,
                bundle == null ? ApplicationManager.getApplicationBundle() : bundle);
    }


    public static Icon getIcon(String icon, Icon defaultValue) {
        Icon toReturn = null;
        if (icon != null) {
            toReturn = ImageManager.getIcon(icon);
        }
        return ObjectTools.coalesce(toReturn, defaultValue);
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

}

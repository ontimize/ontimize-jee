/*
 * ColumnCellUtils.java 21-sep-2017
 *
 * Copyright 2017 Imatia.com
 */
package com.ontimize.jee.webclient.export.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author <a href=""></a>
 *
 */
public class ColumnCellUtils {

    private ColumnCellUtils() {
        // no-op
    }

    private static final Class<?>[] numericTypes = new Class[] { byte.class, Byte.class, short.class, Short.class,
            int.class, Integer.class, long.class, Long.class, float.class, Float.class, double.class, Double.class,
            BigInteger.class, BigDecimal.class };

    private static final Class<?>[] dateTypes = new Class[] { LocalDate.class,
            LocalDateTime.class,
            Timestamp.class, Time.class, Date.class, java.util.Date.class };

    private static final Class<?>[] booleanTypes = new Class[] { boolean.class, Boolean.class };

    /**
     * Chequea si number.
     * @param type type
     * @return true, si number
     */
    public static boolean isNumber(final Class<?> type) {
        if (type == null) {
            return false;
        }
        for (final Class<?> cls : numericTypes) {
            if (type == cls) {
                return true;
            }
        }
        return false;
    }

    /**
     * Chequea si date.
     * @param type type
     * @return true, si date
     */
    public static boolean isDate(final Class<?> type) {
        if (type == null) {
            return false;
        }
        for (final Class<?> cls : dateTypes) {
            if (type == cls) {
                return true;
            }
        }
        return false;
    }

    /**
     * Chequea si boolean.
     * @param type type
     * @return true, si boolean
     */
    public static boolean isBoolean(final Class<?> type) {
        if (type == null) {
            return false;
        }
        for (final Class<?> cls : booleanTypes) {
            if (type == cls) {
                return true;
            }
        }
        return false;
    }

}

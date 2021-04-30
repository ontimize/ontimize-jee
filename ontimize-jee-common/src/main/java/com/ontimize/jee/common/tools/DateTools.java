package com.ontimize.jee.common.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.common.gui.SearchValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Clase de utilidades para trabajar con {@link Date}.
 */
public final class DateTools {

    private static final Logger logger = LoggerFactory.getLogger(DateTools.class);

    public static final int MINUTES_PER_HOUR = 60;

    public static final long SECONDS_PER_MINUTE = 60;

    public static final long HOURS_PER_DAY = 24;

    public static final long MILLISECONDS_PER_SECOND = 1000;

    public static final int LAST_MILLISECOND_OF_SECOND = 999;

    public static final int LAST_SECOND_OF_MINUTE = 59;

    public static final int LAST_MINUTE_OF_HOUR = 59;

    public static final int LAST_HOUR_OF_DAY = 23;

    public static final int FIRST_MILLISECOND_OF_SECOND = 0;

    public static final int FIRST_SECOND_OF_MINUTE = 0;

    public static final int FIRST_MINUTE_OF_HOUR = 0;

    public static final int FIRST_HOUR_OF_DAY = 0;

    /** The Constant MILLISECONDS_PER_DAY. */
    public static final long MILLLISECONDS_PER_MINUTE = DateTools.SECONDS_PER_MINUTE
            * DateTools.MILLISECONDS_PER_SECOND;

    public static final long MILLLISECONDS_PER_HOUR = DateTools.MILLLISECONDS_PER_MINUTE * DateTools.MINUTES_PER_HOUR;

    public static final long MILLISECONDS_PER_DAY = DateTools.MILLLISECONDS_PER_HOUR * DateTools.HOURS_PER_DAY;

    /**
     * Instantiates a new date utils.
     */
    private DateTools() {
        super();
    }

    /**
     * Devuelve la fecha solo con la informacion de ano,mes y dia.
     * @param date the date
     * @return the date
     */
    public static Date truncate(Date date) {
        Calendar inputCalalendar = Calendar.getInstance();
        inputCalalendar.setTime(date);
        Calendar outputCalendar = Calendar.getInstance();
        outputCalendar.clear();
        outputCalendar.set(Calendar.YEAR, inputCalalendar.get(Calendar.YEAR));
        outputCalendar.set(Calendar.MONTH, inputCalalendar.get(Calendar.MONTH));
        outputCalendar.set(Calendar.DAY_OF_MONTH, inputCalalendar.get(Calendar.DAY_OF_MONTH));
        return outputCalendar.getTime();
    }

    /**
     * Crea un {@link Date} a partir del ano, mes y dia.
     * @param yyyy the yyyy
     * @param month the month
     * @param day the day
     * @return the date
     */
    public static synchronized Date createDate(int yyyy, int month, int day) {
        Calendar inputCalalendar = Calendar.getInstance();
        inputCalalendar.clear();
        inputCalalendar.set(yyyy, month - 1, day);
        return inputCalalendar.getTime();
    }

    /**
     * Crea un {@link Date} a partir del ano, mes dia, hora y minuto.
     * @param yyyy the yyyy
     * @param month the month
     * @param day the day
     * @param hour the hour
     * @param min the min
     * @return the date
     */
    public static synchronized Date createDate(int yyyy, int month, int day, int hour, int min) {
        Calendar inputCalalendar = Calendar.getInstance();
        inputCalalendar.clear();
        inputCalalendar.set(yyyy, month - 1, day, hour, min);
        return inputCalalendar.getTime();
    }

    /**
     * Devuelve un date a 31 de diciembre a la hora actual.
     * @param year the year
     * @return the date
     */
    public static Date lastDayOfYear(Integer year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 31);

        Date lastDay = cal.getTime();
        return lastDay;
    }

    /**
     * Devuelve un {@link Date} a 1 de enero a la hora actual.
     * @param year the year
     * @return the date
     */
    public static Date firstDayOfYear(Integer year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        Date lastDay = cal.getTime();
        return lastDay;
    }

    /**
     * Actualiza los campos de hora, minuto, segundo y milisegundo a las 23:59:59.999
     * @param d the d
     * @return the date
     */
    public static Date lastMilisecond(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, DateTools.LAST_HOUR_OF_DAY);
        cal.set(Calendar.MINUTE, DateTools.LAST_MINUTE_OF_HOUR);
        cal.set(Calendar.SECOND, DateTools.LAST_SECOND_OF_MINUTE);
        cal.set(Calendar.MILLISECOND, DateTools.LAST_MILLISECOND_OF_SECOND);

        Date zeroedDate = cal.getTime();
        return zeroedDate;
    }

    /**
     * Actualiza los campos de hora, minuto, segundo y milisegundo a las 00:00:00.000
     * @param d the d
     * @return the date
     */
    public static Date firstMilisecond(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, DateTools.FIRST_HOUR_OF_DAY);
        cal.set(Calendar.MINUTE, DateTools.FIRST_MINUTE_OF_HOUR);
        cal.set(Calendar.SECOND, DateTools.FIRST_SECOND_OF_MINUTE);
        cal.set(Calendar.MILLISECOND, DateTools.FIRST_MILLISECOND_OF_SECOND);

        Date zeroedDate = cal.getTime();
        return zeroedDate;
    }

    /**
     * Devuelve un date a fecha de hoy a las 00:00:00.000
     * @return the calendar
     */
    public static Calendar today() {
        Calendar c = Calendar.getInstance();
        c.setTime(DateTools.truncate(c.getTime()));
        return c;
    }

    /**
     * Devuelve si la fecha between esta entre before y after.
     * @param before the before
     * @param between the between
     * @param after the after
     * @return true, if is between
     */
    public static boolean isBetween(Date before, Date between, Date after) {
        return before.before(between) && after.after(between);
    }

    /**
     * Devuelve si la fecha es fin de semana.
     * @param day the day
     * @return true, if is weekend
     */
    public static boolean isWeekend(Date day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day);
        int dow = calendar.get(Calendar.DAY_OF_WEEK);
        return (dow == Calendar.SATURDAY) || (dow == Calendar.SUNDAY);
    }

    /**
     * This includes both start and end time, so the number of days between two consecutive days is 2.
     * @param start the start
     * @param end the end
     * @return the long
     */
    public static long countDaysBetween(Date start, Date end) {
        return DateTools.countDaysBetween(start, end, false, null)[0];
    }

    /**
     * This includes both start and end time, so the number of days between two consecutive days is 2.
     * @param start the start
     * @param end the end
     * @param exceptweekends the exceptweekends
     * @param except days to exclude. It can be null.
     * @return the long[]
     */
    public static long[] countDaysBetween(Date start, Date end, boolean exceptweekends, List<Date> except) {
        if (end.before(start)) {
            throw new IllegalArgumentException("The end date must be later than the start date");
        }

        long startTime = DateTools.truncate(start).getTime();
        long endTime = DateTools.lastMilisecond(end).getTime();
        List<Date> exceptDays = new ArrayList<>();
        List<Date> excludedDays = new ArrayList<>();

        /*
         * Truncate all the exceptions and remove the duplicated
         */
        if (except != null) {
            for (Date e : except) {
                Date ed = DateTools.truncate(e);
                if (!exceptDays.contains(ed)) {
                    exceptDays.add(ed);
                }
            }
        }

        long valid = 0;
        long weekends = 0;
        long holidays = 0;

        for (; startTime < endTime; startTime += DateTools.MILLISECONDS_PER_DAY) {
            Date day = DateTools.truncate(new Date(startTime));
            if (exceptweekends && !excludedDays.contains(day)) {
                if (DateTools.isWeekend(day)) {
                    weekends++;
                    excludedDays.add(day);
                    continue;
                }
                if (exceptDays.contains(day)) {
                    holidays++;
                    excludedDays.add(day);
                    continue;
                }
            }
            valid++;
        }

        return new long[] { valid, weekends, holidays };
    }

    /**
     * Date to cal.
     * @param d the d
     * @param locale
     * @return the calendar
     *
     */
    public static Calendar dateToCal(Date d, Locale locale) {
        Calendar c = Calendar.getInstance(locale);
        c.setTime(d);
        return c;
    }

    /**
     * Retorna el numero de minutos desde la medianoche de la fecha que le introducimos.
     * @param d Date
     * @return int
     */

    public static int minutesFromMidnigth(Date d) {
        Calendar c = new GregorianCalendar();
        c.setTimeZone(TimeZone.getTimeZone("GMT"));
        c.setTime(d);
        return c.get(Calendar.MINUTE) + (c.get(Calendar.HOUR_OF_DAY) * DateTools.MINUTES_PER_HOUR);
    }

    /**
     * Check if two dates are equals, accepting several dataTypes and analyzing long time
     * @param oba
     * @param obb
     * @return
     */
    public static boolean safeIsEquals(final Object oba, final Object obb) {
        boolean equals = true;
        if (oba == null) {
            equals = obb == null;
        } else if (obb == null) {
            equals = oba == null;
        } else {
            try {
                long timea = ((Date) oba).getTime();
                long timeb = ((Date) obb).getTime();
                equals = timea == timeb;
            } catch (Exception ex) {
                DateTools.logger.error("ERROR_CHECKING_DATES", ex);
                return ObjectTools.safeIsEquals(oba, obb);
            }
        }
        return equals;
    }

    public static Date addDays(Date date, int numDays) {
        return DateTools.add(date, Calendar.DAY_OF_MONTH, numDays);
    }

    public static Date add(Date date, int what, int num) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(what, num);
        return c.getTime();
    }

    public static Calendar createCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public static Calendar createCalendar(int year, int month, int day) {
        return DateTools.createCalendar(year, month, day, 0, 0, 0, 0);
    }

    public static Calendar createCalendar(int year, int month, int day, int hour, int minute, int second,
            int millisecond) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, millisecond);
        return cal;
    }

    public static SearchValue betweenDatesExpression(Object leftValue, Object rightValue) {
        if ((leftValue != null) && (rightValue != null)) {
            return new SearchValue(SearchValue.BETWEEN, new ArrayList(Arrays.asList(leftValue, rightValue)));
        } else {
            if (leftValue != null) {
                return new SearchValue(SearchValue.MORE_EQUAL, leftValue);
            } else if (rightValue != null) {
                return new SearchValue(SearchValue.LESS_EQUAL, rightValue);
            } else {
                return null;
            }
        }
    }

}

package com.ontimize.jee.common.util.calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;


public class BusinessCalendar implements java.io.Serializable {

    private static final Logger logger = LoggerFactory.getLogger(BusinessCalendar.class);

    public static boolean DEBUG = false;

    protected static String COMMON = "common";

    protected class Day implements java.io.Serializable {

        protected Date startDate = null;

        protected Date endDate = null;

        protected int day = 0;

        protected int month = 0;

        public Day(int day, int month, int year, GregorianCalendar c) {
            this.day = day;
            this.month = month;
            if ((year != -1) && (c != null)) {
                c.set(year, month, day, c.getActualMinimum(Calendar.HOUR), c.getActualMinimum(Calendar.MINUTE),
                        c.getActualMinimum(Calendar.SECOND));
                this.startDate = c.getTime();
                c.set(year, month, day + 1, c.getActualMinimum(Calendar.HOUR), c.getActualMinimum(Calendar.MINUTE),
                        c.getActualMinimum(Calendar.SECOND));
                this.endDate = c.getTime();
            }
        }

        public Date startDate() {
            return this.startDate;
        }

        public Date endDate() {
            return this.endDate;
        }

        @Override
        public String toString() {
            return this.day + "/" + (this.month + 1);
        }

        public boolean dateIsInThisDay(Date d) {
            if ((this.startDate.getTime() <= d.getTime()) && (this.endDate.getTime() > d.getTime())) {
                return true;
            } else {
                return false;
            }
        }

    }

    protected class CommonDay extends Day {

        public CommonDay(int day, int month) {
            super(day, month, -1, null);
        }

        @Override
        public boolean dateIsInThisDay(Date d) {
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            return (this.day == c.get(Calendar.DAY_OF_MONTH)) && (this.month == c.get(Calendar.MONTH));
        }

    }

    protected String propertiesFileName = null;

    protected transient ResourceBundle bundle = null;

    protected GregorianCalendar calendar = null;

    protected Locale locale = null;

    protected Map holidays = new HashMap();

    public BusinessCalendar(String propertiesFileName, Locale l) {
        super();
        this.propertiesFileName = propertiesFileName;
        this.locale = l;
        this.calendar = (GregorianCalendar) Calendar.getInstance(l);
        this.calendar.setTime(new Date());
        this.reload();
    }

    public void setLocale(Locale l) {
        this.locale = l;
        this.reload();
    }

    protected boolean currentYearLoaded() {
        int currentYear = this.calendar.get(Calendar.YEAR);
        String s = Integer.toString(currentYear);
        return this.holidays.containsKey(s);
    }

    public void setTime(Date d) {
        this.calendar.setTime(d);
        if (!this.currentYearLoaded()) {
            this.reloadYear();
        }
    }

    public void reload() {
        this.bundle = ResourceBundle.getBundle(this.propertiesFileName, this.locale);

        int sYear = this.calendar.get(Calendar.YEAR);
        Integer.toString(sYear);

        List vAllYearHolidays = new ArrayList();
        try {
            String sPropertiesHolidays = this.bundle.getString(BusinessCalendar.COMMON);
            vAllYearHolidays.addAll(this.parseHolidays(sPropertiesHolidays));
        } catch (Exception e) {
            BusinessCalendar.logger
                .error("Key " + BusinessCalendar.COMMON + " not found in calendar file " + this.propertiesFileName, e);
        }
        this.holidays.put(BusinessCalendar.COMMON, vAllYearHolidays);
        this.reloadYear();
    }

    protected void reloadYear() {
        this.bundle = ResourceBundle.getBundle(this.propertiesFileName, this.locale);

        int iYear = this.calendar.get(Calendar.YEAR);
        String s = Integer.toString(iYear);
        if (this.holidays.containsKey(s)) {
            this.holidays.remove(s);
        }
        List vCurrenYearHolidays = new ArrayList();
        try {
            String sPropertiesHolidays = this.bundle.getString(s);
            vCurrenYearHolidays.addAll(this.parseHolidays(sPropertiesHolidays, iYear));
        } catch (Exception e) {
            BusinessCalendar.logger.error("Key " + s + " not found in calendar file " + this.propertiesFileName, e);
        }
        this.holidays.put(s, vCurrenYearHolidays);
        if (BusinessCalendar.DEBUG) {
            this.printHolidays();
        }
    }

    public synchronized boolean isHoliday(int dayOfMonth, int month, int year) {
        Date d = this.calendar.getTime();
        try {
            this.calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            this.calendar.set(Calendar.MONTH, month);
            this.calendar.set(Calendar.YEAR, year);
            if (!this.currentYearLoaded()) {
                this.reloadYear();
            }
            boolean holiday = this.isHoliday();
            return holiday;
        } catch (Exception e) {
            BusinessCalendar.logger.error(null, e);
            return false;
        } finally {
            this.calendar.setTime(d);
        }
    }

    public synchronized boolean isHoliday(int dayOfMonth, int month) {
        Date d = this.calendar.getTime();
        try {
            this.calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            this.calendar.set(Calendar.MONTH, month);
            if (!this.currentYearLoaded()) {
                this.reloadYear();
            }
            boolean holiday = this.isHoliday();
            return holiday;
        } catch (Exception e) {
            BusinessCalendar.logger.error(null, e);
            return false;
        } finally {
            this.calendar.setTime(d);
        }
    }

    public synchronized boolean isHoliday(int dayOfMonth) {
        Date d = this.calendar.getTime();
        try {
            this.calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            if (!this.currentYearLoaded()) {
                this.reloadYear();
            }
            boolean holiday = this.isHoliday();
            return holiday;
        } catch (Exception e) {
            BusinessCalendar.logger.error(null, e);
            return false;
        } finally {
            this.calendar.setTime(d);
        }
    }

    public synchronized int workingDayOfMonthToDayOfMonth(int workingDayOfMonth) {
        // Checks the holidays for the current date (month and year)
        int minDay = this.calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
        int maxDay = this.calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int workingDaysCount = 0;
        for (int i = minDay; i <= maxDay; i++) {
            if (!this.isHoliday(i)) {
                workingDaysCount++;
            }
            if (workingDaysCount >= workingDayOfMonth) {
                return i;
            }
        }
        return -1;
    }

    public synchronized int workingDayOfMonthToDayOfMonth(int workingDayOfMonth, int month) {
        // Checks the holidays for the current date (month and year)
        int minDay = this.calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
        int maxDay = this.calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int workingDaysCount = 0;
        for (int i = minDay; i <= maxDay; i++) {
            if (!this.isHoliday(i, month)) {
                workingDaysCount++;
            }
            if (workingDaysCount >= workingDayOfMonth) {
                return i;
            }
        }
        return -1;
    }

    public synchronized int workingDayOfMonthToDayOfMonth(int workingDayOfMonth, int month, int year) {
        // Checks the holidays for the current date (month and year)
        int minDay = this.calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
        int maxDay = this.calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int workingDaysCount = 0;
        for (int i = minDay; i <= maxDay; i++) {
            if (!this.isHoliday(i, month, year)) {
                workingDaysCount++;
            }
            if (workingDaysCount >= workingDayOfMonth) {
                return i;
            }
        }
        return -1;
    }

    public synchronized int dayOfMonthToWorkingDayOfMonth(int dayOfMonth) {
        // Checks the holidays for the current date (month and year)
        int minDay = this.calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
        int maxDay = this.calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int workingDaysCount = 0;
        if ((minDay < dayOfMonth) && (dayOfMonth < maxDay)) {
            for (int i = minDay; i <= dayOfMonth; i++) {
                if (!this.isHoliday(i)) {
                    workingDaysCount++;
                }
            }
        }
        if (workingDaysCount > 0) {
            return workingDaysCount;
        }
        return -1;
    }

    public synchronized int dayOfMonthToWorkingDayOfMonth(int dayOfMonth, int month) {
        // Checks the holidays for the current date (month and year)
        int minDay = this.calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
        int maxDay = this.calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int workingDaysCount = 0;
        if ((minDay < dayOfMonth) && (dayOfMonth < maxDay)) {
            for (int i = minDay; i <= dayOfMonth; i++) {
                if (!this.isHoliday(i, month)) {
                    workingDaysCount++;
                }
            }
        }
        if (workingDaysCount > 0) {
            return workingDaysCount;
        }
        return -1;
    }

    public synchronized int dayOfMonthToWorkingDayOfMonth(int dayOfMonth, int month, int year) {
        // Checks the holidays for the current date (month and year)
        int minDay = this.calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
        int maxDay = this.calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int workingDaysCount = 0;
        if ((minDay < dayOfMonth) && (dayOfMonth < maxDay)) {
            for (int i = minDay; i <= dayOfMonth; i++) {
                if (!this.isHoliday(i, month, year)) {
                    workingDaysCount++;
                }
            }
        }
        if (workingDaysCount > 0) {
            return workingDaysCount;
        }
        return -1;
    }

    protected void printHolidays() {
        BusinessCalendar.logger.debug(this.holidays.toString());
    }

    /**
     * Returns true whether the current data is holiday.
     * @return true if is holiday
     */
    public synchronized boolean isHoliday() {
        Date d = this.calendar.getTime();
        if (d == null) {
            return false;
        }

        // Now checks if it is a weekend
        int weekDay = this.calendar.get(Calendar.DAY_OF_WEEK);
        if ((weekDay == Calendar.SATURDAY) || (weekDay == Calendar.SUNDAY)) {
            if (BusinessCalendar.DEBUG) {
                BusinessCalendar.logger.debug("Date " + d + " is not holiday (saturday or sunday)");
            }
            return true;
        }
        int currentYear = this.calendar.get(Calendar.YEAR);
        String s = Integer.toString(currentYear);
        List vAllYearsHolidays = (ArrayList) this.holidays.get(BusinessCalendar.COMMON);
        for (int i = 0; i < vAllYearsHolidays.size(); i++) {
            CommonDay dia = (CommonDay) vAllYearsHolidays.get(i);
            if (dia.dateIsInThisDay(d)) {
                if (BusinessCalendar.DEBUG) {
                    BusinessCalendar.logger.debug("Date " + d + " is common holiday");
                }
                return true;
            }
        }

        List vCurrenYearHolidays = (ArrayList) this.holidays.get(s);
        if (vCurrenYearHolidays != null) {
            for (int i = 0; i < vCurrenYearHolidays.size(); i++) {
                Day day = (Day) vCurrenYearHolidays.get(i);
                if (day.dateIsInThisDay(d)) {
                    if (BusinessCalendar.DEBUG) {
                        BusinessCalendar.logger.debug("Date " + d + " if holiday this year");
                    }
                    return true;
                }
            }
        }
        if (BusinessCalendar.DEBUG) {
            BusinessCalendar.logger.debug("Date " + d + " is not holiday (saturday or sunday)");
        }
        return false;
    }

    protected synchronized List parseHolidays(String s) {
        return this.parseHolidays(s, -1);
    }

    protected synchronized List parseHolidays(String s, int year) {
        Date currentTime = this.calendar.getTime();
        List vDays = new ArrayList();
        try {
            StringTokenizer st = new StringTokenizer(s, ";");

            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                if (token.indexOf("/") < 0) {
                    BusinessCalendar.logger.debug("Holiday format must be: 0/0;21/9 etc");
                } else {
                    String sDay = token.substring(0, token.indexOf("/"));
                    String sMonth = token.substring(token.indexOf("/") + 1);
                    try {
                        int d = Integer.parseInt(sDay);
                        int m = Integer.parseInt(sMonth);
                        // Months start in 0 in the gregorian calendar
                        m--;
                        Day day = null;
                        if (year != -1) {
                            day = new Day(d, m, year, this.calendar);
                        } else {
                            day = new CommonDay(d, m);
                        }
                        vDays.add(day);
                    } catch (Exception e) {
                        BusinessCalendar.logger.trace(null, e);
                    }
                }
            }
        } catch (Exception e) {
            BusinessCalendar.logger.trace(null, e);
        } finally {
            this.calendar.setTime(currentTime);
        }
        return vDays;
    }

}

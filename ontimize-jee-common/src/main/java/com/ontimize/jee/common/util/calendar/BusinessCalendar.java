package com.ontimize.jee.common.util.calendar;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusinessCalendar implements java.io.Serializable {

	private static final Logger logger = LoggerFactory.getLogger(BusinessCalendar.class);

	protected static final String COMMON = "common";

	protected class Day implements java.io.Serializable {

		protected Date startDate = null;

		protected Date endDate = null;

		protected int dayNumber = 0;

		protected int monthNumber = 0;

		public Day(int day, int month, int year, GregorianCalendar c) {
			this.dayNumber = day;
			this.monthNumber = month;
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
			return this.dayNumber + "/" + (this.monthNumber + 1);
		}

		public boolean dateIsInThisDay(Date d) {
			return ((this.startDate.getTime() <= d.getTime()) && (this.endDate.getTime() > d.getTime()));
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
			return (this.dayNumber == c.get(Calendar.DAY_OF_MONTH)) && (this.monthNumber == c.get(Calendar.MONTH));
		}

	}

	protected String propertiesFileName = null;

	protected transient ResourceBundle bundle = null;

	protected GregorianCalendar calendar = null;

	protected Locale locale = null;

	private Map<String, List<Day>> holidays = new HashMap<>();

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

		List<Day> vAllYearHolidays = new ArrayList<>();
		try {
			String sPropertiesHolidays = this.bundle.getString(BusinessCalendar.COMMON);
			vAllYearHolidays.addAll(this.parseHolidays(sPropertiesHolidays));
		} catch (Exception e) {
			BusinessCalendar.logger.error(
					"Key " + BusinessCalendar.COMMON + " not found in calendar file " + this.propertiesFileName, e);
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
		List<Day> vCurrenYearHolidays = new ArrayList<>();
		try {
			String sPropertiesHolidays = this.bundle.getString(s);
			vCurrenYearHolidays.addAll(this.parseHolidays(sPropertiesHolidays, iYear));
		} catch (Exception e) {
			BusinessCalendar.logger.error("Key " + s + " not found in calendar file " + this.propertiesFileName, e);
		}
		this.holidays.put(s, vCurrenYearHolidays);
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
			return this.isHoliday();
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
			return this.isHoliday();
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
			return this.isHoliday();
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
		String holiday = this.holidays.toString();
		BusinessCalendar.logger.debug(holiday);
	}

	/**
	 * Returns true whether the current data is holiday.
	 *
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
			BusinessCalendar.logger.debug("Date {} is not holiday (saturday or sunday)", d);
			return true;
		}
		int currentYear = this.calendar.get(Calendar.YEAR);
		String s = Integer.toString(currentYear);
		List<Day> vAllYearsHolidays = this.holidays.get(BusinessCalendar.COMMON);
		for (Day day : vAllYearsHolidays) {
			if (day.dateIsInThisDay(d)) {
				BusinessCalendar.logger.debug("Date {} is common holiday", d);
				return true;
			}
		}

		List<Day> vCurrenYearHolidays = this.holidays.get(s);
		if (vCurrenYearHolidays != null) {
			for (Day day : vCurrenYearHolidays) {
				if (day.dateIsInThisDay(d)) {
					BusinessCalendar.logger.debug("Date {} is holiday this year", d);
					return true;
				}
			}
		}
		BusinessCalendar.logger.debug("Date {} is not holiday (saturday or sunday)", d);
		return false;
	}

	protected synchronized List<Day> parseHolidays(String s) {
		return this.parseHolidays(s, -1);
	}

	protected synchronized List<Day> parseHolidays(String s, int year) {
		Date currentTime = this.calendar.getTime();
		List<Day> vDays = new ArrayList<>();
		try {
			StringTokenizer st = new StringTokenizer(s, ";");

			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (token.indexOf("/") < 0) {
					BusinessCalendar.logger.debug("Holiday format must be: 0/0;21/9 etc");
				} else {
					String sDay = token.substring(0, token.indexOf("/"));
					String sMonth = token.substring(token.indexOf("/") + 1);
					this.parseHolidaysExtracted(year, vDays, sDay, sMonth);
				}
			}
		} catch (Exception e) {
			BusinessCalendar.logger.trace(null, e);
		} finally {
			this.calendar.setTime(currentTime);
		}
		return vDays;
	}

	/**
	 * Method to reduce tryCath nesting
	 *
	 * @param year
	 * @param vDays
	 * @param sDay
	 * @param sMonth
	 */
	private void parseHolidaysExtracted(int year, List<Day> vDays, String sDay, String sMonth) {
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

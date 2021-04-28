package com.ontimize.jee.common.util.calendar;

import com.ontimize.jee.common.util.ParseTools;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.List;


public class AdvancedTimePeriod implements TimePeriod {

    protected Calendar calendar = null;

    protected BusinessCalendar businessCalendar = null;

    protected Locale locale;

    protected boolean isWorkingPeriod;

    protected int startDay;

    protected int endDay;

    protected int startMonth;

    protected int endMonth;

    public AdvancedTimePeriod(String periodDefinitionString, Locale locale, String businessCalendarProperties)
            throws Exception {
        this.isWorkingPeriod = false;
        this.locale = locale;

        this.businessCalendar = new BusinessCalendar(businessCalendarProperties, locale);
        this.calendar = Calendar.getInstance(locale);

        this.parseString(periodDefinitionString);
    }

    public AdvancedTimePeriod(int startDay, int endDay, int startMonth, int endMonth, boolean workingPeriod,
            Locale locale, BusinessCalendar businessCalendar) {
        this.startDay = startDay;
        this.endDay = endDay;
        this.startMonth = startMonth;
        this.endMonth = endMonth;
        this.isWorkingPeriod = workingPeriod;

        this.locale = locale;
        this.businessCalendar = businessCalendar;
        this.calendar = Calendar.getInstance(locale);
    }

    protected void parseString(String periodDefinitionString) {
        List daysMonthsYears = ParseTools.getTokensAt(periodDefinitionString,
                BasicTimePeriodParser.DAY_MONTH_SEPARATOR);
        // This List has to contain at least two values with the days and
        // months. Years are optional
        if (daysMonthsYears.size() < 2) {
            throw new IllegalArgumentException("Incorrect format: character / not found");
        } else if (daysMonthsYears.size() > 3) {
            throw new IllegalArgumentException(
                    "Incorrect format: character / only can be used to separate days months and years");
        }

        // Parse the day
        String dayDefinition = (String) daysMonthsYears.get(0);
        if (dayDefinition.startsWith(BasicTimePeriodParser.ALL)) {
            this.startDay = -1;
            this.endDay = -1;
            if (dayDefinition.endsWith(BasicTimePeriodParser.WORKING_DAY)) {
                this.isWorkingPeriod = true;
            }
        } else if (dayDefinition.indexOf(BasicTimePeriodParser.INTERVAL) > 0) {
            List intervalDays = ParseTools.getTokensAt(dayDefinition, BasicTimePeriodParser.INTERVAL);
            if (intervalDays.size() != 2) {
                throw new IllegalArgumentException(
                        "Incorrect format: character - must separate two numerical values to define the days");
            }

            String day1 = (String) intervalDays.get(0);
            if (day1.endsWith(BasicTimePeriodParser.WORKING_DAY)) {
                this.isWorkingPeriod = true;
                day1 = day1.substring(0, day1.length() - 1);
            }
            this.startDay = Integer.parseInt(day1);

            String day2 = (String) intervalDays.get(1);
            if (day2.endsWith(BasicTimePeriodParser.WORKING_DAY)) {
                this.isWorkingPeriod = true;
                day2 = day2.substring(0, day2.length() - 1);
            }
            this.endDay = Integer.parseInt(day2);
        } else {
            if (dayDefinition.endsWith(BasicTimePeriodParser.WORKING_DAY)) {
                this.isWorkingPeriod = true;
                dayDefinition = dayDefinition.substring(0, dayDefinition.length() - 1);
            }
            this.startDay = Integer.parseInt(dayDefinition);
            this.endDay = this.startDay;
        }

        // Parse the month
        String monthDefinition = (String) daysMonthsYears.get(1);
        if (monthDefinition.equals(BasicTimePeriodParser.ALL)) {
            this.startMonth = Calendar.JANUARY;
            this.endMonth = Calendar.DECEMBER;
        } else if (monthDefinition.indexOf(BasicTimePeriodParser.INTERVAL) > 0) {
            List intervalMonths = ParseTools.getTokensAt(monthDefinition, BasicTimePeriodParser.INTERVAL);
            if (intervalMonths.size() != 2) {
                throw new IllegalArgumentException(
                        "Incorrect format: character - must separate two numerical values to define the months");
            }
            this.startMonth = Integer.parseInt((String) intervalMonths.get(0)) - 1;
            this.endMonth = Integer.parseInt((String) intervalMonths.get(1)) - 1;
        } else {
            this.startMonth = Integer.parseInt(monthDefinition) - 1;
            this.endMonth = this.startMonth;
        }
    }

    @Override
    public boolean timeIsInPeriod(long time) {
        // Set the date to check in the calendar object
        Date d = new Date(time);
        this.calendar.setTime(d);

        boolean validate = this.validateMonth(this.calendar);
        if (!validate) {
            return false;
        }

        if (this.isWorkingPeriod) {
            return this.timeIsInWorkingPeriod(this.calendar);
        } else {
            return this.timeIsInNormalPeriod(this.calendar);
        }
    }

    protected boolean timeIsInNormalPeriod(Calendar cal) {
        if (this.startDay == -1) {
            // All days are in the period
            return true;
        } else if (this.startDay == this.endDay) {
            // There are only one day in the period
            if (this.calendar.get(Calendar.DAY_OF_MONTH) == this.startDay) {
                return true;
            }
            return false;
        } else {
            int calendarDay = this.calendar.get(Calendar.DAY_OF_MONTH);
            return (calendarDay >= this.startDay) && (calendarDay <= this.endDay);
        }
    }

    protected boolean timeIsInWorkingPeriod(Calendar cal) {

        if (this.startDay == -1) {
            // All working days are in the period
            return true;
        } else if (this.startDay == this.endDay) {
            // There are only one day in the period
            int iDay = 0;
            int workingDays = 0;
            while (workingDays < this.startDay) {
                iDay++;
                boolean holidays = this.businessCalendar.isHoliday(iDay, cal.get(Calendar.MONTH),
                        cal.get(Calendar.YEAR));
                if (!holidays) {
                    workingDays++;
                }
            }
            if (iDay == cal.get(Calendar.DAY_OF_MONTH)) {
                return true;
            }
            return false;
        } else {
            int iDay = this.businessCalendar.workingDayOfMonthToDayOfMonth(this.startDay, cal.get(Calendar.MONTH),
                    cal.get(Calendar.YEAR));
            int iLastDay = this.businessCalendar.workingDayOfMonthToDayOfMonth(this.endDay, cal.get(Calendar.MONTH),
                    cal.get(Calendar.YEAR));
            if ((cal.get(Calendar.DAY_OF_MONTH) >= iDay) && (cal.get(Calendar.DAY_OF_MONTH) <= iLastDay)) {
                return true;
            }
            return false;
        }
    }

    protected boolean validateMonth(Calendar cal) {
        if (this.startMonth == -1) {
            return true;
        } else if (this.startMonth == this.endMonth) {
            return cal.get(Calendar.MONTH) == this.startMonth;
        } else {
            int calendarMonth = cal.get(Calendar.MONTH);
            return (calendarMonth >= this.startMonth) && (calendarMonth <= this.endMonth);
        }
    }

    public boolean isWorkingPeriod() {
        return this.isWorkingPeriod;
    }

    public BusinessCalendar getBusinessCalendar() {
        return this.businessCalendar;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public int getStartDay() {
        return this.startDay;
    }

    public int getEndDay() {
        return this.endDay;
    }

    public int getStartMonth() {
        return this.startMonth;
    }

    public int getEndMonth() {
        return this.endMonth;
    }

    public String getPeriodString() {
        StringBuilder result = new StringBuilder();

        if (this.startDay == -1) {
            result.append(BasicTimePeriodParser.ALL);
        } else if (this.startDay == this.endDay) {
            result.append(this.startDay);
            if (this.isWorkingPeriod) {
                result.append(BasicTimePeriodParser.WORKING_DAY);
            }
        } else {
            result.append(this.startDay);
            if (this.isWorkingPeriod) {
                result.append(BasicTimePeriodParser.WORKING_DAY);
            }
            result.append(BasicTimePeriodParser.INTERVAL);
            result.append(this.endDay);
            if (this.isWorkingPeriod) {
                result.append(BasicTimePeriodParser.WORKING_DAY);
            }
        }

        result.append(BasicTimePeriodParser.DAY_MONTH_SEPARATOR);

        if (this.startMonth == -1) {
            result.append(BasicTimePeriodParser.ALL);
        } else if (this.startMonth == this.endMonth) {
            result.append(this.startMonth + 1);
        } else {
            result.append(this.startMonth + 1);
            result.append(BasicTimePeriodParser.INTERVAL);
            result.append(this.endMonth + 1);
        }

        return result.toString();

    }

}

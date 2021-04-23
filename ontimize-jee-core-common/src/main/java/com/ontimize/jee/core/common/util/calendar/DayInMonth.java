package com.ontimize.jee.core.common.util.calendar;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DayInMonth implements TimePeriod {

    protected int day = -1;

    protected int month = -1;

    protected Locale l = Locale.getDefault();

    protected GregorianCalendar calendar = null;

    protected java.text.DateFormatSymbols symbols = new java.text.DateFormatSymbols();

    protected String[] months = this.symbols.getShortMonths();

    public DayInMonth(int day, int month, Locale l) {
        this.day = day;
        this.month = month;
        this.l = l;
        this.calendar = (GregorianCalendar) Calendar.getInstance(l);
    }

    @Override
    public boolean timeIsInPeriod(long time) {
        Date d = new Date(time);
        this.calendar.setTime(d);
        if ((this.calendar.get(Calendar.DAY_OF_MONTH) == this.day)
                && (this.calendar.get(Calendar.MONTH) == this.month)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Day " + this.day + " from " + this.months[this.month];
    }

}

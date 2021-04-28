package com.ontimize.jee.common.util.calendar;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DayOfMonth implements TimePeriod {

    protected int day = -1;

    protected Locale l = Locale.getDefault();

    protected GregorianCalendar calendar = null;

    public DayOfMonth(int day, Locale l) {
        this.day = day;

        this.l = l;
        this.calendar = (GregorianCalendar) Calendar.getInstance(l);
    }

    @Override
    public boolean timeIsInPeriod(long time) {
        Date d = new Date(time);
        this.calendar.setTime(d);
        if (this.calendar.get(Calendar.DAY_OF_MONTH) == this.day) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Day " + this.day;
    }

}

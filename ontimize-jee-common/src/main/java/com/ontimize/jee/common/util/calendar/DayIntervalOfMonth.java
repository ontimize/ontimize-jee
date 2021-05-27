package com.ontimize.jee.common.util.calendar;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DayIntervalOfMonth implements TimePeriod {

    protected int firstDay = -1;

    protected int lastDay = -1;

    protected Locale l = Locale.getDefault();

    protected GregorianCalendar calendar = null;

    public DayIntervalOfMonth(int firstDay, int lastDay, Locale l) {
        this.firstDay = firstDay;
        this.lastDay = lastDay;
        this.l = l;
        this.calendar = (GregorianCalendar) Calendar.getInstance(l);
    }

    @Override
    public boolean timeIsInPeriod(long time) {
        Date d = new Date(time);
        this.calendar.setTime(d);
        if ((this.calendar.get(Calendar.DAY_OF_MONTH) >= this.firstDay)
                && (this.calendar.get(Calendar.DAY_OF_MONTH) <= this.lastDay)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "From " + this.firstDay + " to " + this.lastDay;
    }

}

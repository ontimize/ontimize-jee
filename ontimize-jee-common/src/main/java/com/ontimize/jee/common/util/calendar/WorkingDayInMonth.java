package com.ontimize.jee.common.util.calendar;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class WorkingDayInMonth implements TimePeriod {

    protected int day = -1;

    protected int month = -1;

    protected Locale l = Locale.getDefault();

    protected BusinessCalendar calendar = null;

    protected GregorianCalendar cAux = null;

    protected java.text.DateFormatSymbols symbols = new java.text.DateFormatSymbols();

    protected String[] months = this.symbols.getShortMonths();

    public WorkingDayInMonth(int day, int month, Locale l, String businessCalendarProperties) {
        this.day = day;
        this.month = month;
        this.l = l;
        this.calendar = new BusinessCalendar(businessCalendarProperties, l);
        this.cAux = (GregorianCalendar) Calendar.getInstance(l);
    }

    @Override
    public boolean timeIsInPeriod(long time) {
        Date d = new Date(time);
        this.cAux.setTime(d);
        int iDay = 0;
        int workingDays = 0;
        while (workingDays < this.day) {
            iDay++;
            boolean holidays = this.calendar.isHoliday(iDay, this.month, this.cAux.get(Calendar.YEAR));
            if (!holidays) {
                workingDays++;
            }

        }
        if (iDay == this.cAux.get(Calendar.DAY_OF_MONTH)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return this.day + " working day from " + this.months[this.month];
    }

}

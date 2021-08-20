package com.ontimize.jee.common.util.calendar;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class WorkingDayOfMonth implements TimePeriod {

    protected int day = -1;

    protected Locale l = Locale.getDefault();

    protected BusinessCalendar calendar = null;

    protected GregorianCalendar cAux = null;

    public WorkingDayOfMonth(int day, Locale l, String businessCalendarProperties) {
        this.day = day;
        this.l = l;
        this.calendar = new BusinessCalendar(businessCalendarProperties, l);
        this.cAux = (GregorianCalendar) Calendar.getInstance(l);
    }

    @Override
    public boolean timeIsInPeriod(long time) {
        Date d = new Date(time);
        this.cAux.setTime(d);
        int iDay = 0;
        int iWorkingDays = 0;
        while (iWorkingDays < this.day) {
            iDay++;
            boolean bHolidays = this.calendar.isHoliday(iDay, this.cAux.get(Calendar.MONTH),
                    this.cAux.get(Calendar.YEAR));
            if (!bHolidays) {
                iWorkingDays++;
            }
        }
        if (iDay == this.cAux.get(Calendar.DAY_OF_MONTH)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return this.day + " working day";
    }

}

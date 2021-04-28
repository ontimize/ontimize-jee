package com.ontimize.jee.common.util.calendar;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class WorkingDayIntervalOfMonth implements TimePeriod {

    protected int firstDay = -1;

    protected int lastDay = -1;

    protected Locale l = Locale.getDefault();

    protected BusinessCalendar calendar = null;

    protected GregorianCalendar cAux = null;

    public WorkingDayIntervalOfMonth(int firstDay, int lastDay, Locale l, String businessCalendarProperties) {
        this.firstDay = firstDay;
        this.lastDay = lastDay;
        this.l = l;
        this.calendar = new BusinessCalendar(businessCalendarProperties, l);
        this.cAux = (GregorianCalendar) Calendar.getInstance(l);
    }

    @Override
    public boolean timeIsInPeriod(long time) {
        Date d = new Date(time);
        this.cAux.setTime(d);

        int iDay = this.calendar.workingDayOfMonthToDayOfMonth(this.firstDay, this.cAux.get(Calendar.MONTH),
                this.cAux.get(Calendar.YEAR));
        int iLastDay = this.calendar.workingDayOfMonthToDayOfMonth(this.lastDay, this.cAux.get(Calendar.MONTH),
                this.cAux.get(Calendar.YEAR));

        if ((this.cAux.get(Calendar.DAY_OF_MONTH) >= iDay) && (this.cAux.get(Calendar.DAY_OF_MONTH) <= iLastDay)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "From " + this.firstDay + " working day to " + this.lastDay + " working day";
    }

}

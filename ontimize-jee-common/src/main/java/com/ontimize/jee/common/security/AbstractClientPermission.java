package com.ontimize.jee.common.security;

import com.ontimize.jee.common.util.calendar.TimePeriod;

import java.lang.reflect.Method;

public class AbstractClientPermission implements RestrictedClientPermission {

    protected boolean restricted = false;

    protected String name = null;

    protected String attr = null;

    protected TimePeriod period = null;

    @Override
    public String getAttribute() {
        return this.attr;
    }

    @Override
    public String getPermissionName() {
        return this.name;
    }

    public boolean isRestricted() {
        return this.restricted;
    }

    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }

    /**
     * Returns true if the permission is a period restricted permission
     * @return true if is a period restricted permission.
     */
    public boolean isPeriodRestricted() {
        if (this.period != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean hasPermission() {
        // If time is in period and restricted and period are enabled, it won't
        // permission
        if (this.restricted) {
            if (this.period != null) {
                if (this.period.timeIsInPeriod(getTime())) {
                    return false;
                }
                return true;
            } else {
                return !this.restricted;
            }
        } else {
            if (this.period != null) {
                if (this.period.timeIsInPeriod(getTime())) {
                    return true;
                }
                return false;
            } else {
                return !this.restricted;
            }
        }
    }

    @Override
    public void setAttribute(String attr) {
        this.attr = attr;
    }

    @Override
    public TimePeriod getPeriod() {
        return this.period;
    }

    public void setPeriod(TimePeriod period) {
        this.period = period;
    }

    @Override
    public String toString() {
        String aux = this.getClass().toString() + " " + this.attr;
        if (aux == null) {
            aux = "";
        }
        StringBuilder sb = new StringBuilder(aux);
        sb.append(" ");
        sb.append(this.name);
        sb.append(" " + this.period);
        return sb.toString();
    }


    public long getTime() {
        try {
            Class clazz = Class.forName("com.ontimize.jee.common.gui.ApplicationManager");
            Method method = clazz.getMethod("getTime", new Class[] {});
            long time = (Long) method.invoke(null, new Object[] {});
            return time;
        } catch (Exception e) {
        }

        return System.currentTimeMillis();
    }

}

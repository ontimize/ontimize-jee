package com.ontimize.jee.core.common.security;

import com.ontimize.jee.core.common.util.calendar.TimePeriod;

/**
 * Class to define permission information.
 *
 * @author Imatia Innovation
 */
public class PermissionInfo implements java.io.Serializable {

    private boolean restricted = false;

    private TimePeriod period = null;

    public PermissionInfo(boolean restricted, TimePeriod period) {
        this.restricted = restricted;
        this.period = period;
    }

    public boolean isRestricted() {
        return this.restricted;
    }

    public TimePeriod getPeriod() {
        return this.period;
    }

}

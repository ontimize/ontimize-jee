package com.ontimize.jee.common.security;

import com.ontimize.jee.common.util.calendar.TimePeriod;

public interface RestrictedClientPermission extends ClientPermission {

    public boolean isRestricted();

    public void setRestricted(boolean restricted);

    /**
     * Returns true if the permission is a period restricted permission
     * @return true if is a period restricted permission.
     */
    public boolean isPeriodRestricted();

    public void setPeriod(TimePeriod period);

}

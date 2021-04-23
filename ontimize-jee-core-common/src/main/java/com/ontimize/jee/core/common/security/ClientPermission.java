package com.ontimize.jee.core.common.security;

import com.ontimize.jee.core.common.util.calendar.TimePeriod;

/**
 * Interface that defines methods for client permission components. It is implemented in class
 * {@link AbstractClientPermission}:
 * <ul>
 * <li>getAttribute()
 * <li>getPermissionName()
 * <li>hasPermission()
 * <li>setAttribute(String atribute)
 * <li>getPeriod()
 * </ul>
 *
 * @author Imatia Innovation
 */
public interface ClientPermission extends java.io.Serializable {

    public String getAttribute();

    public String getPermissionName();

    public boolean hasPermission();

    public void setAttribute(String atribute);

    public TimePeriod getPeriod();

}

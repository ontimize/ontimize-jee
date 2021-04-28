package com.ontimize.jee.common.security;

import com.ontimize.jee.core.common.util.calendar.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to manage the menu permissions.
 *
 * @author Imatia Innovation
 */
public class MenuPermission extends AbstractClientPermission {

    private static final Logger logger = LoggerFactory.getLogger(MenuPermission.class);

    public MenuPermission(String permissionName, String componentAttr, boolean restricted) {
        this.name = permissionName;
        this.attr = componentAttr;
        this.restricted = restricted;
    }

    public MenuPermission(String permissionName, String componentAttr, boolean restricted, TimePeriod period) {
        this(permissionName, componentAttr, restricted);
        try {
            this.period = period;
        } catch (Exception e) {
            MenuPermission.logger.error(this.getClass().toString() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof MenuPermission) {
            if (this.name.equals(((MenuPermission) o).getPermissionName())
                    && this.attr.equals(((MenuPermission) o).getAttribute())) {

                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Integer.parseInt(this.name.hashCode() + "" + this.attr.hashCode());
    }

}

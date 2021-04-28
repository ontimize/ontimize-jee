package com.ontimize.jee.common.security;

import com.ontimize.jee.core.common.util.calendar.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that implements permissions for application toolbar.
 *
 * @author Imatia Innovation
 */
public class ToolBarPermission extends AbstractClientPermission {

    private static final Logger logger = LoggerFactory.getLogger(ToolBarPermission.class);

    public ToolBarPermission(String permissionName, String componentAttr, boolean restricted) {
        this.name = permissionName;
        this.attr = componentAttr;
        this.restricted = restricted;
    }

    public ToolBarPermission(String permissionName, String componentAttr, boolean restricted, TimePeriod period) {
        this(permissionName, componentAttr, restricted);
        try {
            this.period = period;
        } catch (Exception e) {
            ToolBarPermission.logger.error(this.getClass().toString() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof ToolBarPermission) {
            if (this.name.equals(((ToolBarPermission) o).getPermissionName())
                    && this.attr.equals(((ToolBarPermission) o).getAttribute())) {
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

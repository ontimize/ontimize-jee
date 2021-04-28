package com.ontimize.jee.common.security;

import com.ontimize.jee.common.util.calendar.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used in <code>Form</code> to define permissions.
 *
 * @author Imatia Innovation
 */
public class FormPermission extends AbstractClientPermission {

    private static final Logger logger = LoggerFactory.getLogger(FormPermission.class);

    protected String archive = null;

    public FormPermission(String fileName, String permissionName, String componentAttr, boolean restricted) {
        this.attr = componentAttr;
        this.name = permissionName;
        this.restricted = restricted;
        this.archive = fileName;
    }

    public FormPermission(String fileName, String permissionName, String componentAttr, boolean restricted,
            TimePeriod period) {
        this(fileName, permissionName, componentAttr, restricted);
        try {
            this.period = period;
        } catch (Exception e) {
            FormPermission.logger.error(this.getClass().toString() + ": " + e.getMessage(), e);
        }
    }

    public String getArchiveName() {
        return this.archive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof FormPermission) {
            if (this.name.equals(((FormPermission) o).getPermissionName())
                    && this.attr.equals(((FormPermission) o).getAttribute()) && this.archive
                        .equals(((FormPermission) o).getArchiveName())) {
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
        return Integer.parseInt(this.name.hashCode() + "" + this.attr.hashCode() + this.archive.hashCode());
    }

}

package com.ontimize.jee.common.security;

import com.ontimize.jee.common.util.calendar.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used in <code>FormManager</code> to define permissions.
 *
 * @author Imatia Innovation
 */
public class FMPermission extends AbstractClientPermission {

    private static final Logger logger = LoggerFactory.getLogger(FMPermission.class);

    protected String fmId = null;

    public FMPermission(String idGF, String permissionName, String formName, boolean restricted) {
        this.fmId = idGF;
        this.name = permissionName;
        this.attr = formName;
        this.restricted = restricted;
    }

    public FMPermission(String idGF, String permissionName, String formName, boolean restricted, TimePeriod period) {
        this(idGF, permissionName, formName, restricted);
        try {
            this.period = period;
        } catch (Exception e) {
            FMPermission.logger.error(this.getClass().toString() + ": " + e.getMessage(), e);
        }
    }

    public String getFMId() {
        return this.fmId;
    }

    @Override
    public String toString() {
        String aux = this.attr;
        if (aux == null) {
            aux = "";
        }
        StringBuilder sb = new StringBuilder(this.fmId + " " + aux);
        sb.append(" ");
        sb.append(this.name);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof FMPermission) {
            if (this.name.equals(((FMPermission) o).getPermissionName())
                    && this.attr.equals(((FMPermission) o).getAttribute())
                    && this.fmId.equals(((FMPermission) o).getFMId())) {

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
        return Integer.parseInt(this.name.hashCode() + "" + this.attr.hashCode() + this.fmId);
    }

}

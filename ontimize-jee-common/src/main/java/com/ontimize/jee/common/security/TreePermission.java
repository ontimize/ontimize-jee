package com.ontimize.jee.common.security;

import com.ontimize.jee.common.util.calendar.TimePeriod;

/**
 * Class that implements permissions for application tree.
 *
 * @author Imatia Innovation
 */
public class TreePermission extends AbstractClientPermission {

    protected String tree = null;

    public TreePermission(String tree, String permissionName, String nodeId, boolean restricted) {
        this.tree = tree;
        this.name = permissionName;
        this.attr = nodeId;
        this.restricted = restricted;
    }

    public TreePermission(String tree, String permissionName, String nodeId, boolean restricted, TimePeriod period) {
        this.tree = tree;
        this.name = permissionName;
        this.attr = nodeId;
        this.restricted = restricted;
        this.period = period;
    }

    public String getTree() {
        return this.tree;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof TreePermission) {
            if (this.name.equals(((TreePermission) o).getPermissionName())
                    && this.attr.equals(((TreePermission) o).getAttribute()) && this.tree
                        .equals(((TreePermission) o).getTree())) {

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
        return Integer.parseInt(this.name.hashCode() + "" + this.attr.hashCode() + this.tree);
    }

}

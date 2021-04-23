package com.ontimize.jee.core.common.security;

/**
 * Class to define get group identifier and description for group permissions.
 *
 * @author Imatia Innovation
 */
public class PermissionGroupInfo implements java.io.Serializable {

    private String groupId = null;

    private String description = null;

    public PermissionGroupInfo(String groupId, String description) {
        this.groupId = groupId;
        this.description = description;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public String getDescription() {
        return this.description;
    }

}

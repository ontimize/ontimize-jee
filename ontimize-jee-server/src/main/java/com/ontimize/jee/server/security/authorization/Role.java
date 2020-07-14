package com.ontimize.jee.server.security.authorization;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * The Class Role.
 */
public class Role {

    /** The server permission. */
    protected List<String> serverPermissions;

    /** The client permissions. */
    protected Map<String, ?> clientPermissions;

    /** The name. */
    protected String name;

    /**
     * Instantiates a new role.
     * @param name the name
     * @param serverPermission the permission
     * @param clientPermission the client permission
     */
    public Role(String name, List<String> serverPermission, Map<String, ?> clientPermission) {
        super();
        this.name = name;
        this.serverPermissions = serverPermission;
        this.clientPermissions = clientPermission;
        if (this.serverPermissions == null) {
            this.serverPermissions = new ArrayList<>();
        }
        if (this.clientPermissions == null) {
            // Hashtable en vez de hashmap porque el cliente ontimize no está adaptado
            this.clientPermissions = new Hashtable<>();
        }
    }

    /**
     * Gets the permission.
     * @return the permission
     */
    public List<String> getServerPermissions() {
        return this.serverPermissions;
    }

    /**
     * Adds the permission.
     * @param permission the permission
     */
    public void addServerPermission(String permission) {
        this.serverPermissions.add(permission);
    }

    /**
     * Gets the name.
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name.
     * @param value the new name
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Checks for permission.
     * @param permissionName the permission name
     * @return true, if successful
     */
    public boolean hasServerPermission(String permissionName) {
        if (this.serverPermissions == null) {
            return false;
        }
        return this.serverPermissions.contains(permissionName);
    }

    /**
     * Gets the client permissions.
     * @return the client permissions
     */
    public Map<String, ?> getClientPermissions() {
        return this.clientPermissions;
    }

}

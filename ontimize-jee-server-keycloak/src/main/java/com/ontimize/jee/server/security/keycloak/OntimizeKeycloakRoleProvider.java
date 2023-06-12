package com.ontimize.jee.server.security.keycloak;

import java.util.HashMap;
import java.util.Map;

import com.ontimize.jee.server.security.authorization.IRoleProvider;
import com.ontimize.jee.server.security.authorization.Role;

/**
 * The abstract class AbstractRoleProvider contains basic behavior about role management.
 */
public class OntimizeKeycloakRoleProvider implements IRoleProvider {

    /** The roles. */
    protected final Map<String, Role> roles;

    /**
     * Instantiates a new role permissions.
     * @param roles the roles
     */
    public OntimizeKeycloakRoleProvider() {
        super();
        this.roles = new HashMap<>();
    }

    /**
     * Gets the role.
     * @param roleName the role name
     * @return the role
     */
    @Override
    public Role getRole(String roleName) {
        return this.roles.get(roleName);
    }

    @Override
    public void invalidateCache() {
        this.roles.clear();
    }

    /**
     * Add the role.
     * @param role the new role
     */
    public void addRole(Role role) {
        this.roles.put(role.getName(), role);
    }
}

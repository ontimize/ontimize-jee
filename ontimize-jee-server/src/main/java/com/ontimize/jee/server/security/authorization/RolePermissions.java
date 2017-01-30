package com.ontimize.jee.server.security.authorization;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class RolePermissions.
 */
public class RolePermissions {

	/** The roles. */
	protected Map<String, Role>	roles;

	/**
	 * Instantiates a new role permissions.
	 *
	 * @param roles
	 *            the roles
	 */
	public RolePermissions(Map<String, Role> roles) {
		super();
		this.roles = roles;
		if (this.roles == null) {
			this.roles = new HashMap<String, Role>();
		}
	}

	/**
	 * Add the role.
	 *
	 * @param role
	 *            the new role
	 */
	public void addRole(Role role) {
		if (role != null) {
			this.roles.put(role.getName(), role);
		}
	}

	/**
	 * Gets the role.
	 *
	 * @param roleName
	 *            the role name
	 * @return the role
	 */
	public Role getRole(String roleName) {
		return this.roles.get(roleName);
	}

	public void clear() {
		this.roles.clear();
	}

}

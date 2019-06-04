package com.ontimize.jee.server.security.authorization;

import java.util.HashMap;
import java.util.Map;

import com.ontimize.jee.server.security.ISecurityRoleInformationService;

/**
 * The abstract class AbstractRoleProvider contains basic behavior about role management.
 */
public abstract class AbstractRoleProvider implements IRoleProvider {

	/** The roles. */
	protected final Map<String, Role>	roles;

	/**
	 * Instantiates a new role permissions.
	 *
	 * @param roles
	 *            the roles
	 */
	public AbstractRoleProvider() {
		super();
		this.roles = new HashMap<>();
	}

	/**
	 * Gets the role.
	 *
	 * @param roleName
	 *            the role name
	 * @return the role
	 */
	@Override
	public Role getRole(String roleName) {
		Role role = this.roles.get(roleName);
		if (role == null) {
			role = this.loadRoleAndCache(roleName);
		}
		return role;
	}

	@Override
	public void invalidateCache() {
		this.roles.clear();
	}

	protected Role loadRoleAndCache(String roleName) {
		Role role = this.getRoleService().loadRole(roleName);
		this.addRole(role);
		return role;
	}

	/**
	 * Add the role.
	 *
	 * @param role
	 *            the new role
	 */
	protected void addRole(Role role) {
		this.roles.put(role.getName(), role);
	}

	/**
	 * Abstract method that returns a reference to role service
	 *
	 * @return
	 */
	protected abstract ISecurityRoleInformationService getRoleService();
}

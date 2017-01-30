package com.ontimize.jee.server.security.authorization;

import java.util.List;

/**
 * The Interface ISecurityAuthorizator.
 */
public interface ISecurityAuthorizator {

	/**
	 * Devuelve true si alguno de los reoles del usuario tiene el permiso.
	 *
	 * @param permissionName
	 *            the permission name
	 * @param userRoles
	 *            the user roles
	 * @return true, if successful
	 */
	boolean hasPermission(String permissionName, List<String> userRoles);

	/**
	 * Gets the role.
	 *
	 * @param roleName
	 *            the role name
	 * @return the role
	 */
	Role getRole(String roleName);

	/**
	 * Refresh roles.
	 */
	void invalidateCache();
}

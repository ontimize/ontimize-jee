package com.ontimize.jee.server.security.authorization;

/**
 * Interface that public the role provider methods.
 */
public interface IRoleProvider {

	/**
	 * Returns the role configuration for given role name.
	 *
	 * @param roleName
	 * @return
	 */
	Role getRole(String roleName);

	/**
	 * Ensures to clean the current roles cache.
	 */
	void invalidateCache();
}

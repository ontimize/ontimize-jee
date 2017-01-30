/**
 * PermissionsProvider.java 18-abr-2013
 *
 *
 *
 */
package com.ontimize.jee.server.security.authorization;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.ontimize.jee.common.exceptions.PermissionValidationException;
import com.ontimize.jee.server.configuration.OntimizeConfiguration;
import com.ontimize.jee.server.security.ISecurityRoleInformationService;

/**
 * The Class PermissionsProvider.
 *
 * @author <a href="user@email.com">Author</a>
 */
public class DefaultOntimizeAuthorizator implements ISecurityAuthorizator, ApplicationContextAware {

	/** The Constant logger. */
	private static final Logger				logger	= LoggerFactory.getLogger(DefaultOntimizeAuthorizator.class);

	/** The role cache. */
	private final RolePermissions			roleCache;

	/** The role service. */
	private ISecurityRoleInformationService	roleService;

	private ApplicationContext				context;

	/**
	 * Instantiates a new default security authorizator.
	 */
	public DefaultOntimizeAuthorizator() {
		super();
		this.roleCache = new RolePermissions(null);
	}

	/**
	 * Check if this list of roles have the needed permission.
	 *
	 * @param permissionName
	 *            the permission name
	 * @param userRoles
	 *            the roles
	 * @return true, if successful
	 * @throws PermissionValidationException
	 *             the permission validation exception
	 */
	@Override
	public synchronized boolean hasPermission(final String permissionName, final List<String> userRoles) throws PermissionValidationException {
		DefaultOntimizeAuthorizator.logger.trace("Checking permission {} for roles {}", permissionName, StringUtils.join(userRoles, ","));

		for (final String roleName : userRoles) {
			final Role role = this.getRole(roleName);
			if ((role != null) && (role.hasServerPermission(permissionName))) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.security.authorization.ISecurityAuthorizator#getRole(java.lang.String)
	 */
	@Override
	public Role getRole(String roleName) {
		Role role = this.roleCache.getRole(roleName);
		if (role == null) {
			role = this.getRoleService().loadRole(roleName);
			this.roleCache.addRole(role);
		}
		return role;
	}

	private ISecurityRoleInformationService getRoleService() {
		if (this.roleService == null) {
			this.roleService = this.context.getBean(OntimizeConfiguration.class).getSecurityConfiguration().getRoleInformationService();
		}
		return this.roleService;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.security.authorization.ISecurityAuthorizator#invalidateCache()
	 */
	@Override
	public synchronized void invalidateCache() {
		this.roleCache.clear();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}
}

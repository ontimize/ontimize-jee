/**
 * PermissionsProvider.java
 */
package com.ontimize.jee.server.security.authorization;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.ontimize.jee.common.exceptions.PermissionValidationException;

/**
 * The Class PermissionsProvider.
 */
public class DefaultOntimizeAuthorizator implements ISecurityAuthorizator, InitializingBean {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(DefaultOntimizeAuthorizator.class);

    @Autowired
    private ApplicationContext context;

    /** The role provider. */
    @Autowired(required = false)
    private IRoleProvider roleProvider;

    /**
     * Instantiates a new default security authorizator.
     */
    public DefaultOntimizeAuthorizator() {
        super();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.roleProvider == null) {
            this.roleProvider = new DefaultRoleProvider(this.context);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.ontimize.jee.server.security.authorization.ISecurityAuthorizator#getRole(java.lang.String)
     */
    @Override
    public Role getRole(String roleName) {
        return this.roleProvider.getRole(roleName);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.server.security.authorization.ISecurityAuthorizator#invalidateCache()
     */
    @Override
    public synchronized void invalidateCache() {
        this.roleProvider.invalidateCache();
    }

    /**
     * Check if this list of roles have the needed permission.
     * @param permissionName the permission name
     * @param userRoles the roles
     * @return true, if successful
     * @throws PermissionValidationException the permission validation exception
     */
    @Override
    public synchronized boolean hasPermission(final String permissionName, final Collection<String> userRoles)
            throws PermissionValidationException {
        DefaultOntimizeAuthorizator.logger.trace("Checking permission {} for roles {}", permissionName,
                StringUtils.join(userRoles, ","));

        for (final String roleName : userRoles) {
            final Role role = this.getRole(roleName);
            if ((role != null) && role.hasServerPermission(permissionName)) {
                return true;
            }
        }
        return false;
    }

}

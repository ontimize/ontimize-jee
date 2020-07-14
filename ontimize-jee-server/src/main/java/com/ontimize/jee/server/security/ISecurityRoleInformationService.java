package com.ontimize.jee.server.security;

import com.ontimize.jee.server.security.authorization.Role;

/**
 * The Interface ISecurityRoleInformationService.
 */
public interface ISecurityRoleInformationService {

    /**
     * Load role.
     * @param roleName the role name
     * @return the role
     */
    Role loadRole(String roleName);

}

package com.ontimize.jee.server.security.keycloak;

import java.util.Map;

import com.ontimize.jee.common.multitenant.ITenantAuthenticationInfo;

public interface IOntimizeKeycloakTenantProvider {
	/**
	 * Allows to provide a map containing the tenants authentication info 
	 * @return the map containing the tenants authentication info
	 */
	Map<String, ITenantAuthenticationInfo> getTenantsAuthenticationInfo();

	/**
	 * Allows to add a tenant into the connection pool when it does not exists.
	 * @param tenantId the tenant id
	 */
	void ensureTenant(final String tenantId);
}

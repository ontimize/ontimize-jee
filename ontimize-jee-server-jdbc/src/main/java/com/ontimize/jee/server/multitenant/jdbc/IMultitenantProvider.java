package com.ontimize.jee.server.multitenant.jdbc;

public interface IMultitenantProvider extends IMultiTenantManager {
	/**
	 * Allows to add a tenant into the connection pool when it does not exists.
	 * @param tenantId the tenant id
	 */
	void ensureTenant(final String tenantId);
}

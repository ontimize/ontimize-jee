package com.ontimize.jee.server.security.keycloak.store;

import java.util.List;

public interface ITenantAuthenticationStore {

	TenantAuthenticationInfo get(String tenantId);

	List<TenantAuthenticationInfo> getAll();

	void addTenant(TenantAuthenticationInfo configuration);

	void removeTenant(String tenantId);
}

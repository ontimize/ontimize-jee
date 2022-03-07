package com.ontimize.jee.server.multitenant.store;

import java.util.List;

public interface ITenantStore {

	TenantConnectionInfo get(String tenantId);

	List<TenantConnectionInfo> getAll();

	void addTenant(TenantConnectionInfo configuration);

	void removeTenant(String tenantId);
}

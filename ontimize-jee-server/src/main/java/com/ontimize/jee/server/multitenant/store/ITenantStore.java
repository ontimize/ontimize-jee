package com.ontimize.jee.server.multitenant.store;

import java.util.List;

public interface ITenantStore {

	List<TenantSettings> getAll();

	void addTenant(TenantSettings configuration);

	void removeTenant(String tenantId);
}

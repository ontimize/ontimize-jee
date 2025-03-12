package com.ontimize.jee.server.security.keycloak.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TenantAuthenticationStore implements ITenantAuthenticationStore {
	protected final Map<String, TenantAuthenticationInfo> tenants = new HashMap<>();

	@Override
	public TenantAuthenticationInfo get(final String tenantId) {
		return this.tenants.get(tenantId);
	}

	@Override
	public List<TenantAuthenticationInfo> getAll() {
		return new ArrayList<>(this.tenants.values());
	}

	@Override
	public void addTenant(final TenantAuthenticationInfo settings) {
		this.tenants.put(settings.getTenantId(), settings);
	}

	@Override
	public void removeTenant(final String tenantId) {
		this.tenants.remove(tenantId);
	}
}

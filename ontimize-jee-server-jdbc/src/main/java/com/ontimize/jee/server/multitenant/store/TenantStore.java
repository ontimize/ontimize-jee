package com.ontimize.jee.server.multitenant.store;

import com.ontimize.jee.common.multitenant.ITenantStore;
import com.ontimize.jee.common.multitenant.TenantConnectionInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TenantStore implements ITenantStore {
	protected final Map<String, TenantConnectionInfo> tenants = new HashMap<>();

	@Override
	public TenantConnectionInfo get(final String tenantId) {
		return this.tenants.get(tenantId);
	}

	@Override
	public List<TenantConnectionInfo> getAll() {
		return new ArrayList<>(this.tenants.values());
	}

	@Override
	public void addTenant(final TenantConnectionInfo settings) {
		this.tenants.put(settings.getTenantId(), settings);
	}

	@Override
	public void removeTenant(final String tenantId) {
		this.tenants.remove(tenantId);
	}
}

package com.ontimize.jee.server.multitenant.jdbc;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.ontimize.jee.server.multitenant.MultiTenantContextHolder;

public class MultiTenantRoutingDataSource extends AbstractRoutingDataSource {
	private final IMultiTenantManager tenantManager;

	public MultiTenantRoutingDataSource(final IMultiTenantManager tenantManager) {
		super();

		this.setTargetDataSources(tenantManager.getDataSourceHashMap());

		tenantManager.setTenantRoutingDataSource(this);

		this.tenantManager = tenantManager;
	}

	@Override
	protected Object determineCurrentLookupKey() {
		final String currentTenant = MultiTenantContextHolder.getTenant();

		if (currentTenant != null && !this.getResolvedDataSources().containsKey(currentTenant) && this.tenantManager instanceof IMultitenantProvider) {
			((IMultitenantProvider)this.tenantManager).ensureTenant(currentTenant);
		}

		return currentTenant;
	}
}

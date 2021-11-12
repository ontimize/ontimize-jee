package com.ontimize.jee.server.multitenant.jdbc;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.ontimize.jee.server.multitenant.MultiTenantContextHolder;

public class MultiTenantRoutingDataSource extends AbstractRoutingDataSource {

	@Override
	protected Object determineCurrentLookupKey() {
		String currentTenant = MultiTenantContextHolder.getTenant();

		return currentTenant;
	}
}

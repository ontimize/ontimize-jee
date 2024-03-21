package com.ontimize.jee.server.multitenant.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.ontimize.jee.common.multitenant.ITenantStore;
import com.ontimize.jee.common.multitenant.TenantConnectionInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiTenantManager implements IMultiTenantManager {

	protected static final Logger logger = LoggerFactory.getLogger(MultiTenantManager.class);

	protected MultiTenantRoutingDataSource tenantRoutingDataSource;

	protected Map<Object, Object> dataSourceMap;

	@Autowired
	protected ITenantStore tenantStore;

	@Override
	public Map<Object, Object> getDataSourceHashMap() {
		if (this.dataSourceMap == null) {
			this.dataSourceMap = new HashMap<>();

			final List<TenantConnectionInfo> tenantList = this.tenantStore.getAll();

			for (final TenantConnectionInfo configuration : tenantList) {
				try {
					this.addTenant(configuration);
				} catch (final Exception ex) {
					logger.warn("Error creating datasource", ex);
				}
			}
		}
		return this.dataSourceMap;
	}

	@Override
	public void setTenantRoutingDataSource(final MultiTenantRoutingDataSource tenantRoutingDataSource) {
		this.tenantRoutingDataSource = tenantRoutingDataSource;
	}

	public void removeTenant(final String tenantId) {
		if (this.dataSourceMap.containsKey(tenantId)) {
			this.tenantStore.removeTenant(tenantId);
			this.dataSourceMap.remove(tenantId);
			this.tenantRoutingDataSource.afterPropertiesSet();
		}
	}

	public void addTenant(final String tenantId, final String driverClassName, final String jdbcUrl, final String username, final String password)
			throws SQLException {
		if (!this.dataSourceMap.containsKey(tenantId)) {
			final TenantConnectionInfo tenant = new TenantConnectionInfo();
			tenant.setTenantId(tenantId);
			tenant.setDriverClass(driverClassName);
			tenant.setJdbcUrl(jdbcUrl);
			tenant.setUsername(username);
			tenant.setPassword(password);

			this.addTenant(tenant);
		}
	}

	protected void addTenant(final TenantConnectionInfo tenant) throws SQLException {
		final DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(tenant.getDriverClass());
		dataSource.setUrl(tenant.getJdbcUrl());
		dataSource.setUsername(tenant.getUsername());
		dataSource.setPassword(tenant.getPassword());

		// Check that new connection is 'live'. If not - throw exception
		try (final Connection c = dataSource.getConnection()) {
			this.dataSourceMap.put(tenant.getTenantId(), dataSource);
		}
	}
}

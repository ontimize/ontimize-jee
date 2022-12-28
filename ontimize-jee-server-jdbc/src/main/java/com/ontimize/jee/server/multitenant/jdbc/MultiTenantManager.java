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

	Logger logger = LoggerFactory.getLogger(MultiTenantManager.class);

	private MultiTenantRoutingDataSource tenantRoutingDataSource;

	private Map<Object, Object> dataSourceMap;

	@Autowired
	private ITenantStore tenantStore;

	@Override
	public Map<Object, Object> getDataSourceHashMap() {
		if (dataSourceMap == null) {
			dataSourceMap = new HashMap();

			List<TenantConnectionInfo> tenantList = this.tenantStore.getAll();

			for (TenantConnectionInfo configuration : tenantList) {
				try {
					DriverManagerDataSource dataSource = new DriverManagerDataSource();
					dataSource.setDriverClassName(configuration.getDriverClass());
					dataSource.setUrl(configuration.getJdbcUrl());
					dataSource.setUsername(configuration.getUsername());
					dataSource.setPassword(configuration.getPassword());

					// Check that new connection is 'live'. If not - throw exception
					try (Connection c = dataSource.getConnection()) {
						dataSourceMap.put(configuration.getTenantId(), dataSource);
					} catch (Exception ex) {
						logger.warn("Error creating connection: {} ", configuration.getTenantId(), ex);
					}
				} catch (Exception ex) {
					logger.warn("Error creating datasource", ex);
				}
			}
		}
		return dataSourceMap;
	}

	@Override
	public void setTenantRoutingDataSource(MultiTenantRoutingDataSource tenantRoutingDataSource) {
		this.tenantRoutingDataSource = tenantRoutingDataSource;
	}

	public void removeTenant(String tenantId) {
		if (this.dataSourceMap.containsKey(tenantId)) {
			this.tenantStore.removeTenant(tenantId);
			this.dataSourceMap.remove(tenantId);
			this.tenantRoutingDataSource.afterPropertiesSet();
		}
	}

	public void addTenant(String tenantId, String driverClassName, String jdbcUrl, String username, String password)
			throws SQLException {
		if (!dataSourceMap.containsKey(tenantId)) {
			DriverManagerDataSource dataSource = new DriverManagerDataSource();
			dataSource.setDriverClassName(driverClassName);
			dataSource.setUrl(jdbcUrl);
			dataSource.setUsername(username);
			dataSource.setPassword(password);

			TenantConnectionInfo tenantConfiguration = new TenantConnectionInfo();
			tenantConfiguration.setTenantId(tenantId);
			tenantConfiguration.setDriverClass(driverClassName);
			tenantConfiguration.setJdbcUrl(jdbcUrl);
			tenantConfiguration.setUsername(username);
			tenantConfiguration.setPassword(password);

			this.tenantStore.addTenant(tenantConfiguration);

			// Check that new connection is 'live'. If not - throw exception
			try (Connection c = dataSource.getConnection()) {
				dataSourceMap.put(tenantId, dataSource);
				tenantRoutingDataSource.afterPropertiesSet();
			}
		}
	}
}

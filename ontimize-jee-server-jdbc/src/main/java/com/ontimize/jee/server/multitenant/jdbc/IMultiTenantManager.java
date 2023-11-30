package com.ontimize.jee.server.multitenant.jdbc;

import java.util.Map;

public interface IMultiTenantManager {
	/**
	 * Allows to provide a map containing the datasources of each tenant to conform the connection pool
	 * @return the map containing the datasources
	 */
	Map<Object, Object> getDataSourceHashMap();

	/**
	 * Sets a datasource with multitenant support
	 * @param tenantRoutingDataSource the datasource
	 */
	void setTenantRoutingDataSource(final MultiTenantRoutingDataSource tenantRoutingDataSource);
}

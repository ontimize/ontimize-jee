package com.ontimize.jee.server.multitenant.jdbc;

import java.util.Map;

public interface IMultiTenantManager {

	Map<Object, Object> getDataSourceHashMap();

	void setTenantRoutingDataSource(MultiTenantRoutingDataSource tenantRoutingDataSource);
}

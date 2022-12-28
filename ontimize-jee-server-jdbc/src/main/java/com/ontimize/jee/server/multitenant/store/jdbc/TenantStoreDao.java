package com.ontimize.jee.server.multitenant.store.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.multitenant.ITenantStore;
import com.ontimize.jee.common.multitenant.TenantConnectionInfo;
import com.ontimize.jee.server.dao.jdbc.OntimizeJdbcDaoSupport;

public class TenantStoreDao extends OntimizeJdbcDaoSupport implements ITenantStore {
	private static final String TENANT_ID_KEY = "${ontimize.multitenant.configuration.store-repository.tenant-id}";
	private static final String DRIVER_CLASS_KEY = "${ontimize.multitenant.configuration.store-repository.driver-class}";
	private static final String JDBC_URL_KEY = "${ontimize.multitenant.configuration.store-repository.jdbc-url}";
	private static final String USERNAME_KEY = "${ontimize.multitenant.configuration.store-repository.username}";
	private static final String PASSWORD_KEY = "${ontimize.multitenant.configuration.store-repository.password}";

	@Value(TENANT_ID_KEY)
	private String tenantIdColumnName;

	@Value(DRIVER_CLASS_KEY)
	private String driverClassColumnName;

	@Value(JDBC_URL_KEY)
	private String jdbcUrlColumnName;

	@Value(USERNAME_KEY)
	private String usernameColumnName;

	@Value(PASSWORD_KEY)
	private String passwordColumnName;

	@Override
	public TenantConnectionInfo get(String tenantId) {
		TenantConnectionInfo tci = null;
		Map<Object, Object> keys = new HashMap<>();
		keys.put(tenantIdColumnName, tenantId);
		List<Object> attributes = List.of(tenantIdColumnName, driverClassColumnName, jdbcUrlColumnName,
				usernameColumnName, passwordColumnName);
		EntityResult result = query(keys, attributes, (List) null, "default");

		if (result.calculateRecordNumber() > 0) {
			Map<Object, Object> rec = result.getRecordValues(0);

			tci = new TenantConnectionInfo();

			tci.setDriverClass((String) rec.get(driverClassColumnName));
			tci.setTenantId((String) rec.get(tenantIdColumnName));
			tci.setJdbcUrl((String) rec.get(jdbcUrlColumnName));
			tci.setUsername(((String) rec.get(usernameColumnName)));
			tci.setPassword(((String) rec.get(passwordColumnName)));
		}

		return tci;
	}

	@Override
	public List<TenantConnectionInfo> getAll() {
		List<TenantConnectionInfo> allTenants = new ArrayList<>();
		Map<Object, Object> keys = new HashMap<>();
		List<Object> attributes = List.of(tenantIdColumnName, driverClassColumnName, jdbcUrlColumnName,
				usernameColumnName, passwordColumnName);
		EntityResult result = query(keys, attributes, (List) null, "default");

		for (int i = 0; i < result.calculateRecordNumber(); i++) {
			Map<Object, Object> rec = result.getRecordValues(i);
			TenantConnectionInfo tci = new TenantConnectionInfo();

			tci.setDriverClass((String) rec.get(driverClassColumnName));
			tci.setTenantId((String) rec.get(tenantIdColumnName));
			tci.setJdbcUrl((String) rec.get(jdbcUrlColumnName));
			tci.setUsername(((String) rec.get(usernameColumnName)));
			tci.setPassword(((String) rec.get(passwordColumnName)));

			allTenants.add(tci);
		}

		return allTenants;
	}

	@Override
	public void addTenant(TenantConnectionInfo settings) {
		Map<Object, Object> data = new HashMap<>();
		data.put(driverClassColumnName, settings.getDriverClass());
		data.put(tenantIdColumnName, settings.getTenantId());
		data.put(jdbcUrlColumnName, settings.getJdbcUrl());
		data.put(usernameColumnName, settings.getUsername());
		data.put(passwordColumnName, settings.getPassword());

		EntityResult eRInsert = insert(data);
	}

	@Override
	public void removeTenant(String tenantId) {
		Map<Object, Object> data = new HashMap<>();
		data.put(tenantIdColumnName, tenantId);
		delete(data);
	}
}

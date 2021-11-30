package com.ontimize.jee.server.multitenant.store.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.server.dao.jdbc.OntimizeJdbcDaoSupport;
import com.ontimize.jee.server.multitenant.store.ITenantStore;
import com.ontimize.jee.server.multitenant.store.TenantSettings;

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
	public List<TenantSettings> getAll() {
		List<TenantSettings> allTenants = new ArrayList<>();
		Map<Object, Object> keys = new HashMap<>();
		List<Object> attributes = List.of(tenantIdColumnName, driverClassColumnName, jdbcUrlColumnName,
				usernameColumnName, passwordColumnName);
		EntityResult result = query(keys, attributes, (List) null, "default");

		for (int i = 0; i < result.calculateRecordNumber(); i++) {
			Map record = result.getRecordValues(i);
			TenantSettings tenantSettings = new TenantSettings();

			tenantSettings.setDriverClass((String) record.get(driverClassColumnName));
			tenantSettings.setTenantId((String) record.get(tenantIdColumnName));
			tenantSettings.setJdbcUrl((String) record.get(jdbcUrlColumnName));
			tenantSettings.setUsername(((String) record.get(usernameColumnName)));
			tenantSettings.setPassword(((String) record.get(passwordColumnName)));

			allTenants.add(tenantSettings);
		}

		return allTenants;
	}

	@Override
	public void addTenant(TenantSettings settings) {
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

package com.ontimize.jee.server.multitenant.store.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.multitenant.ITenantStore;
import com.ontimize.jee.common.multitenant.TenantConnectionInfo;
import com.ontimize.jee.server.dao.IOntimizeDaoSupport;

public class TenantStoreDao implements ITenantStore, InitializingBean {
	private static final String TENANT_REPOSITORY_KEY = "${ontimize.multitenant.configuration.tenant-repository}";
	private static final String TENANT_QUERY_ID_KEY = "${ontimize.multitenant.configuration.query-id:default}";
	private static final String TENANT_ID_KEY = "${ontimize.multitenant.configuration.tenant-id-column}";
	private static final String DRIVER_CLASS_KEY = "${ontimize.multitenant.configuration.driver-class-column}";
	private static final String JDBC_URL_KEY = "${ontimize.multitenant.configuration.jdbc-url-column}";
	private static final String USERNAME_KEY = "${ontimize.multitenant.configuration.username-column}";
	private static final String PASSWORD_KEY = "${ontimize.multitenant.configuration.password-column}";

	@Value(TENANT_REPOSITORY_KEY)
	private String tenantRepository;

	@Value(TENANT_QUERY_ID_KEY)
	private String queryId;

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

	@Autowired
	private ApplicationContext applicationContext;

	private IOntimizeDaoSupport dao;

	@Override
	public TenantConnectionInfo get(String tenantId) {
		TenantConnectionInfo tci = null;
		Map<Object, Object> keys = new HashMap<>();
		keys.put(this.tenantIdColumnName, tenantId);
		List<Object> attributes = List.of(this.tenantIdColumnName, this.driverClassColumnName, this.jdbcUrlColumnName,
				this.usernameColumnName, this.passwordColumnName);
		EntityResult result = this.dao.query(keys, attributes, (List) null, this.queryId);

		if (result.calculateRecordNumber() > 0) {
			Map<Object, Object> rec = result.getRecordValues(0);

			tci = new TenantConnectionInfo();

			tci.setDriverClass((String) rec.get(this.driverClassColumnName));
			tci.setTenantId((String) rec.get(this.tenantIdColumnName));
			tci.setJdbcUrl((String) rec.get(this.jdbcUrlColumnName));
			tci.setUsername(((String) rec.get(this.usernameColumnName)));
			tci.setPassword(((String) rec.get(this.passwordColumnName)));
		}

		return tci;
	}

	@Override
	public List<TenantConnectionInfo> getAll() {
		List<TenantConnectionInfo> allTenants = new ArrayList<>();
		Map<Object, Object> keys = new HashMap<>();
		List<Object> attributes = List.of(this.tenantIdColumnName, this.driverClassColumnName, this.jdbcUrlColumnName,
				this.usernameColumnName, this.passwordColumnName);
		EntityResult result = this.dao.query(keys, attributes, (List) null, this.queryId);

		for (int i = 0; i < result.calculateRecordNumber(); i++) {
			Map<Object, Object> rec = result.getRecordValues(i);
			TenantConnectionInfo tci = new TenantConnectionInfo();

			tci.setDriverClass((String) rec.get(this.driverClassColumnName));
			tci.setTenantId((String) rec.get(this.tenantIdColumnName));
			tci.setJdbcUrl((String) rec.get(this.jdbcUrlColumnName));
			tci.setUsername(((String) rec.get(this.usernameColumnName)));
			tci.setPassword(((String) rec.get(this.passwordColumnName)));

			allTenants.add(tci);
		}

		return allTenants;
	}

	@Override
	public void addTenant(TenantConnectionInfo settings) {
		Map<Object, Object> data = new HashMap<>();
		data.put(this.driverClassColumnName, settings.getDriverClass());
		data.put(this.tenantIdColumnName, settings.getTenantId());
		data.put(this.jdbcUrlColumnName, settings.getJdbcUrl());
		data.put(this.usernameColumnName, settings.getUsername());
		data.put(this.passwordColumnName, settings.getPassword());

		this.dao.insert(data);
	}

	@Override
	public void removeTenant(String tenantId) {
		Map<Object, Object> data = new HashMap<>();
		data.put(this.tenantIdColumnName, tenantId);
		this.dao.delete(data);
	}

	public void afterPropertiesSet() throws Exception {
		final Object repo = this.applicationContext.getBean(this.tenantRepository);
		if (repo instanceof IOntimizeDaoSupport) {
			this.dao = (IOntimizeDaoSupport) repo;
		} else {
			throw new OntimizeJEEException(String.format("The type of the bean %s is not IOntimizeDaoSupport", this.tenantRepository));
		}
	}
}

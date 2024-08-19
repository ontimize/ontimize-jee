package com.ontimize.jee.server.security.keycloak.store.jdbc;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import com.ontimize.jee.server.security.keycloak.store.ITenantAuthenticationStore;
import com.ontimize.jee.server.security.keycloak.store.TenantAuthenticationInfo;
import com.ontimize.jee.server.dao.IOntimizeDaoSupport;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TenantAuthenticationStoreDao implements ITenantAuthenticationStore, InitializingBean {
	private static final String TENANT_REPOSITORY_KEY = "${ontimize.security.keycloak.tenant-repository}";
	private static final String QUERY_ID_KEY = "${ontimize.security.keycloak.query-id:default}";
	private static final String TENANT_ID_KEY = "${ontimize.security.keycloak.tenant-id-column}";
	private static final String TENANT_NAME_KEY = "${ontimize.security.keycloak.tenant-name-column}";
	private static final String URL_COLUMN_KEY = "${ontimize.security.keycloak.url-column}";
	private static final String REALM_COLUMN_KEY = "${ontimize.security.keycloak.realm-column}";
	private static final String CLIENT_COLUMN_KEY = "${ontimize.security.keycloak.client-column}";

	@Value(TENANT_REPOSITORY_KEY)
	private String tenantRepository;

	@Value(QUERY_ID_KEY)
	private String queryId;

	@Value(TENANT_ID_KEY)
	private String tenantIdColumnName;

	@Value(TENANT_NAME_KEY)
	private String tenantNameColumnName;

	@Value(URL_COLUMN_KEY)
	private String urlColumnName;

	@Value(REALM_COLUMN_KEY)
	private String realmColumnName;

	@Value(CLIENT_COLUMN_KEY)
	private String clientColumnName;

	@Autowired
	private ApplicationContext applicationContext;

	private IOntimizeDaoSupport dao;

	@Override
	public TenantAuthenticationInfo get(final String tenantId) {
		TenantAuthenticationInfo tci = null;
		final Map<String, Object> keys = new HashMap<>();
		keys.put(this.tenantIdColumnName, tenantId);
		final List<Object> attributes = List.of(this.tenantIdColumnName, this.tenantNameColumnName, this.urlColumnName,
				this.realmColumnName, this.clientColumnName);
		final EntityResult result = this.dao.query(keys, attributes, null, this.queryId);

		if (result.calculateRecordNumber() > 0) {
			tci = this.getTenantAuthenticationInfo(result.getRecordValues(0));
		}

		return tci;
	}

	@Override
	public List<TenantAuthenticationInfo> getAll() {
		final List<TenantAuthenticationInfo> allTenants = new ArrayList<>();
		final Map<String, Object> keys = new HashMap<>();
		final List<Object> attributes = List.of(this.tenantIdColumnName, this.tenantNameColumnName, this.urlColumnName,
				this.realmColumnName, this.clientColumnName);
		final EntityResult result = this.dao.query(keys, attributes, null, this.queryId);

		for (int i = 0; i < result.calculateRecordNumber(); i++) {
			allTenants.add(this.getTenantAuthenticationInfo(result.getRecordValues(0)));
		}

		return allTenants;
	}

	@Override
	public void addTenant(final TenantAuthenticationInfo settings) {
		final Map<Object, Object> data = new HashMap<>();
		data.put(this.tenantIdColumnName, settings.getTenantId());
		data.put(this.tenantNameColumnName, settings.getTenantName());
		data.put(this.urlColumnName, settings.getUrl());
		data.put(this.realmColumnName, settings.getRealm());
		data.put(this.clientColumnName, settings.getClient());

		this.dao.insert(data);
	}

	@Override
	public void removeTenant(final String tenantId) {
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

	private TenantAuthenticationInfo getTenantAuthenticationInfo(final Map<String, Object> rec) {
		final TenantAuthenticationInfo tci = new TenantAuthenticationInfo();
		tci.setTenantId((String) rec.get(this.tenantIdColumnName));
		tci.setTenantName((String) rec.get(this.tenantNameColumnName));
		tci.setUrl((String) rec.get(this.urlColumnName));
		tci.setRealm((String) rec.get(this.realmColumnName));
		tci.setClient(((String) rec.get(this.clientColumnName)));
		return tci;
	}
}

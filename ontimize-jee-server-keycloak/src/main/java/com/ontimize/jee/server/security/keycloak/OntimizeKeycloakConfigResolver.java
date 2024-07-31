package com.ontimize.jee.server.security.keycloak;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.spi.HttpFacade.Request;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.springframework.beans.factory.annotation.Autowired;

import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.multitenant.ITenantAuthenticationInfo;

public class OntimizeKeycloakConfigResolver implements KeycloakConfigResolver {
	@Autowired
	private IOntimizeKeycloakConfiguration config;

	private Map<String, ITenantAuthenticationInfo> tenantsAuthenticationInfo = null;
	private final Map<String, KeycloakDeployment> cache = new ConcurrentHashMap<>();
	private IOntimizeKeycloakTenantProvider ontimizeKeycloakTenantProvider = null;

	@Override
	public KeycloakDeployment resolve(final Request request) {
		return this.resolve(request.getHeader("X-Tenant"));
	}

	public KeycloakDeployment resolve(final String tenantId) {
		KeycloakDeployment deployment = null;

		if (this.config instanceof IOntimizeKeycloakSingleTenantConfiguration) {
			final IOntimizeKeycloakSingleTenantConfiguration singleTenantConfig = (IOntimizeKeycloakSingleTenantConfiguration) this.config;

			if (cache.containsKey(singleTenantConfig.getRealm())) {
				deployment = cache.get(singleTenantConfig.getRealm());
			} else {
				deployment = this.getDeployment(singleTenantConfig, singleTenantConfig);

				cache.put(singleTenantConfig.getRealm(), deployment);
			}
		} else if (tenantId == null) {
			throw new OntimizeJEERuntimeException("No tenant provided");
		} else if (cache.containsKey(tenantId)) {
			deployment = cache.get(tenantId);
		} else if (this.config instanceof IOntimizeKeycloakMultiTenantConfiguration) {
			final IOntimizeKeycloakMultiTenantConfiguration multiTenantConfig = (IOntimizeKeycloakMultiTenantConfiguration) this.config;
			ITenantAuthenticationInfo auth = multiTenantConfig.getTenantStore().get(tenantId);

			if (auth != null) {
				deployment = this.getDeployment(multiTenantConfig, auth);

				cache.put(tenantId, deployment);
			} else {
				throw new OntimizeJEERuntimeException("No authentication info provided for tenant " + tenantId);
			}
		} else {
			final ITenantAuthenticationInfo auth = this.getTenant(tenantId);

			if (auth != null) {
				deployment = this.getDeployment(this.config, auth);

				cache.put(tenantId, deployment);
			} else {
				throw new OntimizeJEERuntimeException("No authentication info provided for tenant " + tenantId);
			}
		}

		return deployment;
	}

	public void setTenantsAuthenticationInfo(final Map<String, ITenantAuthenticationInfo> tenantsAuthenticationInfo) {
		this.tenantsAuthenticationInfo = tenantsAuthenticationInfo;
	}

	public void setTenantProvider(IOntimizeKeycloakTenantProvider ontimizeKeycloakTenantProvider) {
		this.ontimizeKeycloakTenantProvider = ontimizeKeycloakTenantProvider;
	}

	private KeycloakDeployment getDeployment(final IOntimizeKeycloakConfiguration keycloakConfiguration, final ITenantAuthenticationInfo authenticationInfo) {
		final AdapterConfig ac = new AdapterConfig();
		ac.setAuthServerUrl(authenticationInfo.getUrl());
		ac.setRealm(authenticationInfo.getRealm());
		ac.setResource(authenticationInfo.getClient());
		if (keycloakConfiguration.isPublicClient() != null) ac.setPublicClient(keycloakConfiguration.isPublicClient());
		if (keycloakConfiguration.isUseClientRoleMappings() != null) ac.setUseResourceRoleMappings(keycloakConfiguration.isUseClientRoleMappings());
		return KeycloakDeploymentBuilder.build(ac);
	}

	private ITenantAuthenticationInfo getTenant(final String tenantId) {
		ITenantAuthenticationInfo auth = null;
		final Map<String, ITenantAuthenticationInfo> tenants;

		if (this.ontimizeKeycloakTenantProvider != null) {
			tenants = this.ontimizeKeycloakTenantProvider.getTenantsAuthenticationInfo();

			if (tenants != null && !tenants.containsKey(tenantId)) {
				this.ontimizeKeycloakTenantProvider.ensureTenant(tenantId);
			}
		} else {
			tenants = this.tenantsAuthenticationInfo;
		}

		if (tenants == null) {
			throw new OntimizeJEERuntimeException("No tenants authentication info provided");
		} else if (tenants.containsKey(tenantId)) {
			auth = tenants.get(tenantId);
		}

		return auth;
	}
}

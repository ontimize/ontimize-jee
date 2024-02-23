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

public class OntimizeKeycloakConfigResolver
		implements IOntimizeMultitenantKeycloakConfigResolver, KeycloakConfigResolver {
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
				final AdapterConfig ac = new AdapterConfig();
				ac.setAuthServerUrl(singleTenantConfig.getAuthServerUrl());
				ac.setRealm(singleTenantConfig.getRealm()); // Realm
				ac.setResource(singleTenantConfig.getResource()); // Client Id
				ac.setPublicClient(singleTenantConfig.isPublicClient());
				ac.setUseResourceRoleMappings(singleTenantConfig.isUseResourceRoleMappings());

				deployment = KeycloakDeploymentBuilder.build(ac);

				cache.put(singleTenantConfig.getRealm(), deployment);
			}
		} else if (tenantId == null) {
			throw new OntimizeJEERuntimeException("No tenant provided");
		} else if (cache.containsKey(tenantId)) {
			deployment = cache.get(tenantId);
		} else if (this.config instanceof IOntimizeKeycloakMultiTenantConfiguration) {
			final IOntimizeKeycloakMultiTenantConfiguration multitTenantConfig = (IOntimizeKeycloakMultiTenantConfiguration) this.config;
			final AdapterConfig ac = new AdapterConfig();
			ac.setAuthServerUrl(multitTenantConfig.getAuthServerUrl());
			ac.setRealm(tenantId); // Realm
			ac.setResource(multitTenantConfig.getResource()); // Client Id
			ac.setPublicClient(multitTenantConfig.isPublicClient());
			ac.setUseResourceRoleMappings(multitTenantConfig.isUseResourceRoleMappings());

			deployment = KeycloakDeploymentBuilder.build(ac);

			cache.put(tenantId, deployment);
		} else {
			final ITenantAuthenticationInfo auth = this.getTenant(tenantId);

			if (auth != null) {
				final AdapterConfig ac = new AdapterConfig();
				ac.setAuthServerUrl(auth.getUrl());
				ac.setRealm(auth.getRealm()); // Realm
				ac.setResource(auth.getClient()); // Client Id
				ac.setPublicClient(this.config.isPublicClient());
				ac.setUseResourceRoleMappings(this.config.isUseResourceRoleMappings());
				deployment = KeycloakDeploymentBuilder.build(ac);

				cache.put(tenantId, deployment);
			} else {
				throw new OntimizeJEERuntimeException("No authentication info provided for tenant " + tenantId);
			}
		}

		return deployment;
	}

	@Override
	public void setTenantsAuthenticationInfo(final Map<String, ITenantAuthenticationInfo> tenantsAuthenticationInfo) {
		this.tenantsAuthenticationInfo = tenantsAuthenticationInfo;
	}

	@Override
	public void setTenantProvider(IOntimizeKeycloakTenantProvider ontimizeKeycloakTenantProvider) {
		this.ontimizeKeycloakTenantProvider = ontimizeKeycloakTenantProvider;
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

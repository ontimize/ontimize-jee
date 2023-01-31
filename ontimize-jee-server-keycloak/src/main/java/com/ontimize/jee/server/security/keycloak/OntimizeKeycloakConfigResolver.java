package com.ontimize.jee.server.security.keycloak;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.OIDCHttpFacade;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.springframework.beans.factory.annotation.Autowired;

import com.ontimize.jee.common.multitenant.ITenantAuthenticationInfo;

public class OntimizeKeycloakConfigResolver
		implements IOntimizeMultitenantKeycloakConfigResolver, KeycloakConfigResolver {
	@Autowired
	private IOntimizeKeycloakConfiguration config;

	private Map<String, ITenantAuthenticationInfo> tenantsAuthenticationInfo = null;
	private final Map<String, KeycloakDeployment> cache = new ConcurrentHashMap<String, KeycloakDeployment>();

	@Override
	public KeycloakDeployment resolve(OIDCHttpFacade.Request request) {
		KeycloakDeployment deployment = null;
		String tenantId = null;

		if (this.config instanceof IOntimizeKeycloakSingleTenantConfiguration) {
			final IOntimizeKeycloakSingleTenantConfiguration config = (IOntimizeKeycloakSingleTenantConfiguration) this.config;

			if (cache.containsKey(config.getRealm())) {
				deployment = cache.get(config.getRealm());
			} else {
				final AdapterConfig ac = new AdapterConfig();
				ac.setAuthServerUrl(config.getAuthServerUrl());
				ac.setRealm(config.getRealm()); // Realm
				ac.setResource(config.getResource()); // Client Id
				ac.setPublicClient(config.isPublicClient());
				ac.setUseResourceRoleMappings(config.isUseResourceRoleMappings());

				deployment = KeycloakDeploymentBuilder.build(ac);

				cache.put(config.getRealm(), deployment);
			}
		} else {
			tenantId = request.getHeader("X-Tenant");

			if (tenantId == null) {
				throw new RuntimeException("No tenant provided");
			} else if (cache.containsKey(tenantId)) {
				deployment = cache.get(tenantId);
			} else if (this.config instanceof IOntimizeKeycloakMultiTenantConfiguration) { 
				final IOntimizeKeycloakMultiTenantConfiguration config = (IOntimizeKeycloakMultiTenantConfiguration) this.config;
				final AdapterConfig ac = new AdapterConfig();
				ac.setAuthServerUrl(config.getAuthServerUrl());
				ac.setRealm(tenantId); // Realm
				ac.setResource(config.getResource()); // Client Id
				ac.setPublicClient(config.isPublicClient());
				ac.setUseResourceRoleMappings(config.isUseResourceRoleMappings());

				deployment = KeycloakDeploymentBuilder.build(ac);

				cache.put(tenantId, deployment);
			} else if (this.tenantsAuthenticationInfo == null) {
				throw new RuntimeException("No tenants authentication info provided");
			} else if (this.tenantsAuthenticationInfo.containsKey(tenantId)) {
				final ITenantAuthenticationInfo auth = this.tenantsAuthenticationInfo.get(tenantId);
				final AdapterConfig ac = new AdapterConfig();
				ac.setAuthServerUrl(auth.getUrl());
				ac.setRealm(auth.getRealm()); // Realm
				ac.setResource(auth.getClient()); // Client Id
				ac.setPublicClient(this.config.isPublicClient());
				ac.setUseResourceRoleMappings(this.config.isUseResourceRoleMappings());
				deployment = KeycloakDeploymentBuilder.build(ac);

				cache.put(tenantId, deployment);
			} else {
				throw new RuntimeException("No authentication info provided for tenant " + tenantId);
			}
		}

		return deployment;
	}

	@Override
	public void setTenantsAuthenticationInfo(Map<String, ITenantAuthenticationInfo> tenantsAuthenticationInfo) {
		this.tenantsAuthenticationInfo = tenantsAuthenticationInfo;
	}
}

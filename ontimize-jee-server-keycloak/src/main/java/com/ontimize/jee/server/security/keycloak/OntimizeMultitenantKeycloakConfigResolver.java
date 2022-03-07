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

public class OntimizeMultitenantKeycloakConfigResolver implements IOntimizeMultitenantKeycloakConfigResolver, KeycloakConfigResolver {
	@Autowired
	IOntimizeKeycloakConfiguration config;

	private Map<String, ITenantAuthenticationInfo> tenantsAuthenticationInfo = null;
	private final Map<String, KeycloakDeployment> cache = new ConcurrentHashMap<String, KeycloakDeployment>();

	@Override
	public KeycloakDeployment resolve(OIDCHttpFacade.Request request) {
		String tenantId = request.getHeader("X-Tenant");

		if (tenantId == null) {
			tenantId = this.config.getRealm(); // Default Tenant
		}

		KeycloakDeployment deployment = cache.get(tenantId);

		if (deployment == null) {
			final AdapterConfig ac = new AdapterConfig();

			if (tenantsAuthenticationInfo != null && tenantsAuthenticationInfo.containsKey(tenantId)) {
				ITenantAuthenticationInfo auth = tenantsAuthenticationInfo.get(tenantId);
				ac.setAuthServerUrl(auth.getUrl());
				ac.setRealm(auth.getRealm()); // Realm
				ac.setResource(auth.getClient()); // Client Id
			} else {
				ac.setAuthServerUrl(this.config.getAuthServerUrl());
				ac.setRealm(tenantId); // Realm
				ac.setResource(this.config.getResource()); // Client Id
			}

			ac.setPublicClient(this.config.getPublicClient());

			deployment = KeycloakDeploymentBuilder.build(ac);

			cache.put(tenantId, deployment);
		}

		return deployment;
	}

	@Override
	public void setTenantsAuthenticationInfo(Map<String, ITenantAuthenticationInfo> tenantsAuthenticationInfo) {
		this.tenantsAuthenticationInfo = tenantsAuthenticationInfo;
	}
}

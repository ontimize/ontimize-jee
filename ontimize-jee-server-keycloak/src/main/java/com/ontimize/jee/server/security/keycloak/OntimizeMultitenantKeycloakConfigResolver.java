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
		KeycloakDeployment deployment = null;

		if (tenantId != null && cache.containsKey(tenantId)) {
			deployment = cache.get(tenantId);
		} else {
			if (this.tenantsAuthenticationInfo != null) {
				if (tenantId != null && this.tenantsAuthenticationInfo.containsKey(tenantId)) {
					final ITenantAuthenticationInfo auth = this.tenantsAuthenticationInfo.get(tenantId);
					final AdapterConfig ac = new AdapterConfig();
					ac.setAuthServerUrl(auth.getUrl());
					ac.setRealm(auth.getRealm()); // Realm
					ac.setResource(auth.getClient()); // Client Id
					ac.setPublicClient(this.config.getPublicClient());

					deployment = KeycloakDeploymentBuilder.build(ac);

					cache.put(tenantId, deployment);
				} else {
					tenantId = this.config.getRealm(); // Default Tenant

					deployment = cache.get(tenantId);
				}
			} else {
				if (tenantId == null) {
					tenantId = this.config.getRealm(); // Default Tenant
				}

				deployment = cache.get(tenantId);
			}

			if (deployment == null) {
				final AdapterConfig ac = new AdapterConfig();
				ac.setAuthServerUrl(this.config.getAuthServerUrl());
				ac.setRealm(tenantId); // Realm
				ac.setResource(this.config.getResource()); // Client Id
				ac.setPublicClient(this.config.getPublicClient());

				deployment = KeycloakDeploymentBuilder.build(ac);

				cache.put(tenantId, deployment);
			}
		}

		return deployment;
	}

	@Override
	public void setTenantsAuthenticationInfo(Map<String, ITenantAuthenticationInfo> tenantsAuthenticationInfo) {
		this.tenantsAuthenticationInfo = tenantsAuthenticationInfo;
	}
}

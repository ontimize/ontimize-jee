package com.ontimize.jee.server.security.keycloak;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.OIDCHttpFacade;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.springframework.beans.factory.annotation.Autowired;

public class OntimizeMultitenantKeycloakConfigResolver implements KeycloakConfigResolver {
	@Autowired
	KeycloakConfiguration config;

	private final Map<String, KeycloakDeployment> cache = new ConcurrentHashMap<String, KeycloakDeployment>();

	@Override
	public KeycloakDeployment resolve(OIDCHttpFacade.Request request) {
		String realm = request.getHeader("xtenant");

		if (realm == null) {
			realm = this.config.getRealm(); // Default Tenant
		}
		
		KeycloakDeployment deployment = cache.get(realm);

		if (deployment == null) {
			final AdapterConfig ac = new AdapterConfig();

			ac.setAuthServerUrl(this.config.getAuthServerUrl());
			ac.setRealm(realm); // Tenant
			ac.setResource(this.config.getResource()); // Client Id
			ac.setPublicClient(this.config.getPublicClient());

			deployment = KeycloakDeploymentBuilder.build(ac);

			cache.put(realm, deployment);
		}

		return deployment;
	}
}
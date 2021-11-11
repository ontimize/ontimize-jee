package com.ontimize.jee.server.security.keycloak;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.OIDCHttpFacade;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.springframework.beans.factory.annotation.Value;

public class OntimizeMultitenantKeycloakConfigResolver implements KeycloakConfigResolver {
	@Value("${ontimize.security.keycloak.auth-server-url}")
	private String authServerUrl;

	@Value("${ontimize.security.keycloak.public-client}")
	private Boolean publicClient;

	@Value("${ontimize.security.keycloak.realm}")
	private String realm;

	@Value("${ontimize.security.keycloak.resource}")
	private String resource;

	private final Map<String, KeycloakDeployment> cache = new ConcurrentHashMap<String, KeycloakDeployment>();

	@Override
	public KeycloakDeployment resolve(OIDCHttpFacade.Request request) {
		String realm = request.getHeader("xtenant");

		if (realm == null) {
			realm = this.realm; // Default Tenant
		}
		
		KeycloakDeployment deployment = cache.get(realm);

		if (deployment == null) {
			final AdapterConfig ac = new AdapterConfig();

			ac.setAuthServerUrl(this.authServerUrl);
			ac.setRealm(realm); // Tenant
			ac.setResource(this.resource); // Client Id
			ac.setPublicClient(this.publicClient);

			deployment = KeycloakDeploymentBuilder.build(ac);

			cache.put(realm, deployment);
		}

		return deployment;
	}
}
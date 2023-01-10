package com.ontimize.jee.server.security.keycloak;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OntimizeKeycloakConfiguration implements IOntimizeKeycloakConfiguration {
	@Value("${ontimize.security.keycloak.auth-server-url}")
	private String authServerUrl;

	@Value("${ontimize.security.ignore-paths:}")
	private String[] ignorePaths;

	@Value("${ontimize.security.keycloak.public-client}")
	private Boolean publicClient;

	@Value("${ontimize.security.keycloak.realm}")
	private String realm;

	@Value("${ontimize.security.keycloak.resource}")
	private String resource;

	@Value("${ontimize.security.keycloak.use-resource-role-mappings:false}")
	private Boolean useResourceRoleMappings;

	@Override
	public String getAuthServerUrl() {
		return this.authServerUrl;
	}

	public void setAuthServerUrl(String authServerUrl) {
		this.authServerUrl = authServerUrl;
	}

	@Override
	public String[] getIgnorePaths() {
		return ignorePaths;
	}

	public void setIgnorePaths(String[] ignorePaths) {
		this.ignorePaths = ignorePaths;
	}

	@Override
	public Boolean isPublicClient() {
		return this.publicClient;
	}

	public void setPublicClient(Boolean publicClient) {
		this.publicClient = publicClient;
	}

	@Override
	public String getRealm() {
		return this.realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	@Override
	public String getResource() {
		return this.resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	@Override
	public Boolean isUseResourceRoleMappings() {
		return this.useResourceRoleMappings;
	}

	public void setUseResourceRoleMappings(Boolean useResourceRoleMappings) {
		this.useResourceRoleMappings = useResourceRoleMappings;
	}
}

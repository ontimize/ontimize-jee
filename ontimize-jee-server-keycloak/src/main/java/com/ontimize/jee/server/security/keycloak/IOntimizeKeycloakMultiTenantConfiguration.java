package com.ontimize.jee.server.security.keycloak;

public interface IOntimizeKeycloakMultiTenantConfiguration extends IOntimizeKeycloakConfiguration {
	public String getAuthServerUrl();
	public String getResource();
}

package com.ontimize.jee.server.security.keycloak;

public interface IOntimizeKeycloakConfiguration {
	public String getAuthServerUrl();
	public String[] getIgnorePaths();
	public Boolean isPublicClient();
	public String getRealm();
	public String getResource();
	public Boolean isUseResourceRoleMappings();
}

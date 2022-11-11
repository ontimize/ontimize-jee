package com.ontimize.jee.server.security.keycloak;

public interface IOntimizeKeycloakConfiguration {
	public String getAuthServerUrl();
	public String[] getIgnorePaths();
	public Boolean getPublicClient();
	public String getRealm();
	public String getResource();
	public Boolean getUseResourceRoleMappings();
}

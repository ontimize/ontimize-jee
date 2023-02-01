package com.ontimize.jee.server.security.keycloak;

public interface IOntimizeKeycloakConfiguration {
	public String[] getIgnorePaths();
	public Boolean isPublicClient();
	public Boolean isUseResourceRoleMappings();
}

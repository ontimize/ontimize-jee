package com.ontimize.jee.server.security.keycloak;

public interface IOntimizeKeycloakSingleTenantConfiguration extends IOntimizeKeycloakMultiTenantConfiguration {
	public String getRealm();
}

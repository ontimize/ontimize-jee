package com.ontimize.jee.server.security.keycloak;

import com.ontimize.jee.server.security.keycloak.store.ITenantAuthenticationStore;

public interface IOntimizeKeycloakMultiTenantConfiguration extends IOntimizeKeycloakConfiguration {
	public ITenantAuthenticationStore getTenantStore();
}

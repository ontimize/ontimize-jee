package com.ontimize.jee.server.security.keycloak;

import java.util.Map;

import com.ontimize.jee.common.multitenant.ITenantAuthenticationInfo;

public interface IOntimizeMultitenantKeycloakConfigResolver {
	public void setTenantsAuthenticationInfo(Map<String, ITenantAuthenticationInfo> tenantsAuthenticationInfo);
}

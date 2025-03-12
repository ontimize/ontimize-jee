package com.ontimize.jee.server.security.keycloak;

import com.ontimize.jee.common.multitenant.ITenantAuthenticationInfo;
import com.ontimize.jee.server.security.keycloak.store.TenantAuthenticationInfo;

public interface IOntimizeKeycloakSingleTenantConfiguration extends IOntimizeKeycloakConfiguration, ITenantAuthenticationInfo {
    /**
     * @see TenantAuthenticationInfo#getUrl
     * @deprecated Use:
     */
    @Deprecated(since = "3.15", forRemoval = true)
    public String getAuthServerUrl();

    /**
     * @see TenantAuthenticationInfo#setUrl
     * @deprecated Use:
     */
    @Deprecated(since = "3.15", forRemoval = true)
    public void setAuthServerUrl(final String authServerUrl);

    /**
     * @see TenantAuthenticationInfo#getClient
     * @deprecated Use:
     */
    @Deprecated(since = "3.15", forRemoval = true)
    public String getResource();

    /**
     * @see TenantAuthenticationInfo#setClient
     * @deprecated Use:
     */
    @Deprecated(since = "3.15", forRemoval = true)
    public void setResource(final String resource);
}

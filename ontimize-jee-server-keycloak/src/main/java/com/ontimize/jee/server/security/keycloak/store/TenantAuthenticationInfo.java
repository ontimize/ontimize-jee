package com.ontimize.jee.server.security.keycloak.store;

import com.ontimize.jee.common.multitenant.ITenantAuthenticationInfo;

public class TenantAuthenticationInfo implements ITenantAuthenticationInfo {
    private String tenantId;
    private String tenantName;
    private String url;
    private String realm;
    private String client;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(final String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(final String tenantName) {
        this.tenantName = tenantName;
    }

    @Override
    public String getUrl() {
        return this.url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    @Override
    public String getRealm() {
        return this.realm;
    }

    public void setRealm(final String realm) {
        this.realm = realm;
    }

    @Override
    public String getClient() {
        return this.client;
    }

    public void setClient(final String client) {
        this.client = client;
    }
}

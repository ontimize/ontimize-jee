package com.ontimize.jee.common.multitenant;

public interface ITenantAuthenticationInfo {
	public String getUrl();
	public String getRealm();
	public String getClient();
}
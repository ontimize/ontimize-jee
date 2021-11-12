package com.ontimize.jee.server.security.keycloak;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfiguration {
	@Value("${ontimize.security.keycloak.auth-server-url:#{null}}")
	private String authServerUrl;

	@Value("${ontimize.security.keycloak.public-client}")
	private Boolean publicClient;

	@Value("${ontimize.security.keycloak.realm}")
	private String realm;

	@Value("${ontimize.security.keycloak.resource}")
	private String resource;

	@Value("${ontimize.security.keycloak.admin.realm}")
	private String adminRealm;

	@Value("${ontimize.security.keycloak.admin.resource}")
	private String adminResource;

	@Value("${ontimize.security.keycloak.admin.username}")
	private String adminUserName;

	@Value("${ontimize.security.keycloak.admin.password}")
	private String adminPassword;

	@Value("${ontimize.security.keycloak.realm-defaults.role}")
	private String realmKeycloakRole;

	@Value("${ontimize.security.keycloak.realm-defaults.redirect-urls:}")
	private String[] redirectUrls;

	public String getAuthServerUrl() {
		return this.authServerUrl;
	}

	public void setAuthServerUrl(String authServerUrl) {
		this.authServerUrl = authServerUrl;
	}

	public Boolean getPublicClient() {
		return this.publicClient;
	}

	public void setPublicClient(Boolean publicClient) {
		this.publicClient = publicClient;
	}

	public String getRealm() {
		return this.realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	public String getResource() {
		return this.resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getAdminRealm() {
		return this.adminRealm;
	}

	public void setAdminRealm(String adminRealm) {
		this.adminRealm = adminRealm;
	}

	public String getAdminResource() {
		return this.adminResource;
	}

	public void setAdminResource(String adminResource) {
		this.adminResource = adminResource;
	}

	public String getAdminUserName() {
		return this.adminUserName;
	}

	public void setAdminUserName(String adminUserName) {
		this.adminUserName = adminUserName;
	}

	public String getAdminPassword() {
		return this.adminPassword;
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}

	public String getRealmKeycloakRole() {
		return this.realmKeycloakRole;
	}

	public void setRealmKeycloakRole(String realmKeycloakRole) {
		this.realmKeycloakRole = realmKeycloakRole;
	}

	public String[] getRedirectUrls() {
		return this.redirectUrls;
	}

	public void setRedirectUrls(String[] redirectUrls) {
		this.redirectUrls = redirectUrls;
	}
}

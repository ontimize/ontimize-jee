package com.ontimize.jee.server.security.authentication.oauth2;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class OAuth2ClientProperties implements InitializingBean {

    // Mandatory
    private String userAuthorizationUri = null;

    private Map<String, String> additionalAuthParams = null;

    private URI redirectUri = null;

    private String accessTokenUri = null;

    private String clientId = null;

    private String clientSecret = null;

    private String userInfoUri = null;

    private Map<String, String> additionalInfoParams = null;

    // Optional, defaults set here
    private String accessTokenName = "access_token";

    private final String clientSecretParamName = "client_secret";

    private String clientIdParamName = "client_id";

    private final String grantTypeParamName = "grant_type";

    private String grantType = "authorization_code";

    private String redirectUriParamName = "redirect_uri";

    private final String responseTypeParamName = "response_type";

    private final String responseType = "code";

    private final String stateParamName = "state";

    private final String codeParamName = "code";

    private String userIdName = "email";

    /**
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.userAuthorizationUri, "The userAuthorisationUri must be set");
        Assert.notNull(this.redirectUri, "The redirectUri must be set");
        Assert.notNull(this.accessTokenUri, "The accessTokenUri must be set");
        Assert.notNull(this.clientId, "The clientId must be set");
        Assert.notNull(this.clientSecret, "The clientSecret must be set");
        Assert.notNull(this.userInfoUri, "The userInfoUri must be set");
    }

    public String getUserAuthorizationUri() {
        return this.userAuthorizationUri;
    }

    public void setUserAuthorizationUri(String userAuthorizationUri) {
        this.userAuthorizationUri = userAuthorizationUri;
    }

    public Map<String, String> getAdditionalAuthParams() {
        return this.additionalAuthParams;
    }

    public void setAdditionalAuthParams(Map<String, String> additionalAuthParams) {
        this.additionalAuthParams = additionalAuthParams;
    }

    public URI getRedirectUri() {
        return this.redirectUri;
    }

    public void setRedirectUri(String redirectUri) throws URISyntaxException {
        this.redirectUri = new URI(redirectUri);
    }

    public String getAccessTokenUri() {
        return this.accessTokenUri;
    }

    public void setAccessTokenUri(String accessTokenUri) {
        this.accessTokenUri = accessTokenUri;
    }

    public String getClientId() {
        return this.clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return this.clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getAccessTokenName() {
        return this.accessTokenName;
    }

    public void setAccessTokenName(String accessTokenName) {
        this.accessTokenName = accessTokenName;
    }

    public String getClientIdParamName() {
        return this.clientIdParamName;
    }

    public void setClientIdParamName(String clientIdParamName) {
        this.clientIdParamName = clientIdParamName;
    }

    public String getGrantTypeParamName() {
        return this.grantTypeParamName;
    }

    public String getGrantType() {
        return this.grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getRedirectUriParamName() {
        return this.redirectUriParamName;
    }

    public void setRedirectUriParamName(String redirectUriParamName) {
        this.redirectUriParamName = redirectUriParamName;
    }

    public String getResponseTypeParamName() {
        return this.responseTypeParamName;
    }

    public String getResponseType() {
        return this.responseType;
    }

    public String getStateParamName() {
        return this.stateParamName;
    }

    public String getCodeParamName() {
        return this.codeParamName;
    }

    public String getClientSecretParamName() {
        return this.clientSecretParamName;
    }

    public String getUserInfoUri() {
        return this.userInfoUri;
    }

    public void setUserInfoUri(String userInfoUri) {
        this.userInfoUri = userInfoUri;
    }

    public Map<String, String> getAdditionalInfoParams() {
        return this.additionalInfoParams;
    }

    public void setAdditionalInfoParams(Map<String, String> additionalInfoParams) {
        this.additionalInfoParams = additionalInfoParams;
    }

    public String getUserIdName() {
        return this.userIdName;
    }

    public void setUserIdName(String userIdName) {
        this.userIdName = userIdName;
    }

}

package com.ontimize.jee.server.security.authentication.oauth2;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.Assert;

public class OAuth2ClientEntryPoint implements AuthenticationEntryPoint, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(OAuth2ClientEntryPoint.class);

    private static final int STATE_RANDOM_STRING_LENGTH = 10;

    private String typeName = "Bearer";

    private String realmName = "oauth";

    private OAuth2ClientProperties oAuth2ClientProperties = null;

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        String state = RandomStringUtils.randomAlphanumeric(OAuth2ClientEntryPoint.STATE_RANDOM_STRING_LENGTH);
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute(this.oAuth2ClientProperties.getStateParamName(), state);
        }

        StringBuilder authorizationUri = new StringBuilder()
            .append(this.oAuth2ClientProperties.getUserAuthorizationUri())
            .append("?")
            .append(this.oAuth2ClientProperties.getClientIdParamName())
            .append("=")
            .append(this.oAuth2ClientProperties.getClientId())
            .append("&")
            .append(this.oAuth2ClientProperties.getRedirectUriParamName())
            .append("=")
            .append(this.redirectUriUsing(request).toString())
            .append("&")
            .append(this.oAuth2ClientProperties.getResponseTypeParamName())
            .append("=")
            .append(this.oAuth2ClientProperties.getResponseType())
            .append("&")
            .append(this.oAuth2ClientProperties.getStateParamName())
            .append("=")
            .append(state);

        authorizationUri
            .append(this.constructAdditionalAuthParameters(this.oAuth2ClientProperties.getAdditionalAuthParams()));

        String url = authorizationUri.toString();

        OAuth2ClientEntryPoint.log.debug("authorizationUrl : {}", url);

        StringBuilder builder = new StringBuilder();
        builder.append(this.typeName + " ");
        builder.append("realm=\"" + this.realmName + "\"");
        response.addHeader("WWW-Authenticate", builder.toString());

        response.sendRedirect(url);
    }

    protected StringBuilder constructAdditionalAuthParameters(Map<String, String> additionalParameters) {
        StringBuilder result = new StringBuilder();

        if ((additionalParameters != null) && !additionalParameters.isEmpty()) {
            for (Map.Entry<String, String> entry : additionalParameters.entrySet()) {
                result.append("&").append(entry.getKey()).append("=").append(entry.getValue());
            }
        }

        return result;
    }

    private URI redirectUriUsing(HttpServletRequest request) {
        URI redirect;

        URI redirectUri = this.oAuth2ClientProperties.getRedirectUri();
        if (!redirectUri.isAbsolute()) {
            redirect = UriBuilder.fromPath(request.getContextPath())
                .path(redirectUri.toString())
                .scheme(request.getScheme())
                .host(request.getServerName())
                .port(request.getServerPort())
                .build();
        } else {
            redirect = redirectUri;
        }

        return redirect;
    }

    /**
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.oAuth2ClientProperties, "oAuth2ClientProperties must be set");
    }

    /**
     * @param oAuth2ClientProperties
     */
    public void setoAuth2ClientProperties(OAuth2ClientProperties oAuth2ClientProperties) {
        this.oAuth2ClientProperties = oAuth2ClientProperties;
    }

}

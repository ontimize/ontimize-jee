package com.ontimize.jee.server.security.authentication.oauth2;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.ontimize.jee.server.security.authentication.AuthenticationResult;
import com.ontimize.jee.server.security.authentication.IAuthenticationMechanism;
import com.ontimize.jee.server.security.authentication.OntimizeAuthenticationProvider;
import com.ontimize.jee.server.security.authentication.OntimizeWebAuthenticationDetails;

/**
 * @author
 */
public class OAuth2ClientAuthenticationMechanism implements IAuthenticationMechanism, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(OAuth2ClientAuthenticationMechanism.class);

    private OAuth2ClientProperties oAuth2ClientProperties;

    protected IOAuth2ClientLoginListener oAuth2ClientLoginListener;

    protected IOAuth2ClientUserInfoProvider oAuth2ClientUserInfoProvider;

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(this.oAuth2ClientProperties, "An oAuth2ClientProperties must be set");
        if (this.oAuth2ClientUserInfoProvider == null) {
            this.oAuth2ClientUserInfoProvider = new DefaultOAuth2ClientUserInfoProviderImpl();
        }
    }

    /**
     * @param request
     * @param response
     * @return
     * @throws org.springframework.security.core.AuthenticationException
     */
    @Override
    public AuthenticationResult authenticate(HttpServletRequest request, HttpServletResponse response,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService) {

        if (OAuth2ClientAuthenticationMechanism.log.isDebugEnabled()) {
            String url = request.getRequestURI();
            String queryString = request.getQueryString();
            OAuth2ClientAuthenticationMechanism.log.debug("attemptAuthentication on url {}?{}", url, queryString);
        }
        final Map<String, String[]> parameters = request.getParameterMap();
        OAuth2ClientAuthenticationMechanism.log.debug("Got Parameters: {}", parameters);

        String code = null;
        final String[] codeValues = parameters.get(this.oAuth2ClientProperties.getCodeParamName());
        if ((codeValues != null) && (codeValues.length > 0)) {
            code = codeValues[0];
            OAuth2ClientAuthenticationMechanism.log.debug("Got code {}", code);
        } else {
            return null;
        }

        this.checkForErrors(parameters);

        this.checkStateParameter(request.getSession(false), parameters);
        OntimizeWebAuthenticationDetails authenticationDetails = new OntimizeWebAuthenticationDetails(request);
        return this.authenticate(code, authenticationDetails);
    }

    /**
     * @param session
     * @param parameters
     * @throws AuthenticationException
     */
    protected void checkStateParameter(HttpSession session, Map<String, String[]> parameters) {
        // Al poder estar en cluster y ser stateless no se puede hacer esta comprobacion -> Ver SVN
    }

    /**
     * @param parameters
     * @throws AuthenticationException
     */
    protected void checkForErrors(Map<String, String[]> parameters) {
        final String[] errorValues = parameters.get("error");
        final String[] errorReasonValues = parameters.get("error_reason");
        final String[] errorDescriptionValues = parameters.get("error_description");

        if ((errorValues != null) && (errorValues.length > 0)) {
            final String error = errorValues[0];
            final String errorReason = (errorReasonValues != null) && (errorReasonValues.length > 0)
                    ? errorReasonValues[0] : null;
            final String errorDescription = (errorDescriptionValues != null) && (errorDescriptionValues.length > 0)
                    ? errorDescriptionValues[0] : null;
            OAuth2ClientAuthenticationMechanism.log.info(
                    "An error was returned by the OAuth Provider: error: %s error_reason: %s, error_description: %s",
                    error, errorReason,
                    errorDescription);
            throw new AuthenticationServiceException(
                    MessageFormat.format(
                            "An error was returned by the OAuth Provider: error: %s error_reason: %s, error_description: %s",
                            error, errorReason, errorDescription));
        }
    }

    /**
     * @param authenticationDetails
     * @param authentication
     * @return
     */
    protected AuthenticationResult authenticate(String token, OntimizeWebAuthenticationDetails authenticationDetails) {
        OAuth2ClientAuthenticationMechanism.log.debug("OAuth2Authentication authentication request: " + token);

        if (token == null) {
            OAuth2ClientAuthenticationMechanism.log.debug("No credentials found in request.");
            return null;
        }

        Map<String, Object> userInfo = this.oAuth2ClientUserInfoProvider
            .getUserInfoFromProvider(this.getAccessToken(token, authenticationDetails), this.oAuth2ClientProperties);
        String idAsString = (String) userInfo.get(this.oAuth2ClientProperties.getUserIdName());

        if (this.oAuth2ClientLoginListener != null) {
            this.oAuth2ClientLoginListener.onNewOauth2SuccesfullLogin(token, idAsString, userInfo);
        }

        return new AuthenticationResult(true, new UsernamePasswordAuthenticationToken(idAsString,
                OntimizeAuthenticationProvider.NO_AUTHENTICATION_TOKEN, null));
    }

    public void setoAuth2ClientLoginListener(IOAuth2ClientLoginListener oAuth2ClientLoginListener) {
        this.oAuth2ClientLoginListener = oAuth2ClientLoginListener;
    }

    public void setoAuth2ClientUserInfoProvider(IOAuth2ClientUserInfoProvider oAuth2ClientUserInfoProvider) {
        this.oAuth2ClientUserInfoProvider = oAuth2ClientUserInfoProvider;
    }

    /**
     * @param authenticationDetails
     * @param authentication
     * @return
     */
    protected String getAccessToken(String codeToken, OntimizeWebAuthenticationDetails authenticationDetails) {
        String accessToken = null;

        try {

            Map<String, Object> userData = this.getUserDataMapFrom(codeToken, authenticationDetails);
            if (userData.containsKey("error")) {
                OAuth2ClientAuthenticationMechanism.log.error("Got error response from the OAuth Provider {}",
                        userData);
                throw new AuthenticationServiceException(
                        MessageFormat.format("Credentials were rejected by the OAuth Provider %s", userData));
            }

            accessToken = (String) userData.get(this.oAuth2ClientProperties.getAccessTokenName());

        } catch (Exception e) {
            OAuth2ClientAuthenticationMechanism.log
                .error("Error thrown by RestTemplate client when exchanging code for token", e);
            throw new AuthenticationServiceException("Error when exchanging code for token", e);
        }

        return accessToken;
    }

    private Map<String, Object> getUserDataMapFrom(String codeToken,
            OntimizeWebAuthenticationDetails authenticationDetails) {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(this.oAuth2ClientProperties.getGrantTypeParamName(), this.oAuth2ClientProperties.getGrantType());
        map.add(this.oAuth2ClientProperties.getClientIdParamName(), this.oAuth2ClientProperties.getClientId());
        map.add(this.oAuth2ClientProperties.getClientSecretParamName(), this.oAuth2ClientProperties.getClientSecret());
        map.add(this.oAuth2ClientProperties.getCodeParamName(), codeToken);
        URI redirectUri = this.redirectUriUsing(codeToken, authenticationDetails);
        map.add(this.oAuth2ClientProperties.getRedirectUriParamName(), redirectUri.toString());

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> userInfoMapEntity = restTemplate.exchange(this.oAuth2ClientProperties.getAccessTokenUri(),
                HttpMethod.POST, entity, Map.class);
        if (userInfoMapEntity.getStatusCode() == HttpStatus.OK) {
            return userInfoMapEntity.getBody();
        } else {
            OAuth2ClientAuthenticationMechanism.log.error("cannot get user info");
            throw new AuthenticationServiceException("Error when exchanging code for token");
        }

    }

    /**
     * @param authentication
     * @return
     */
    private URI redirectUriUsing(String codeToken, OntimizeWebAuthenticationDetails details) {
        URI redirectUri;
        if ((details != null) && !this.oAuth2ClientProperties.getRedirectUri().isAbsolute()) {
            OntimizeWebAuthenticationDetails oAuth2ClientWebAuthenticationDetails = details;
            redirectUri = UriComponentsBuilder.fromPath(oAuth2ClientWebAuthenticationDetails.getContextPath())
                .path(this.oAuth2ClientProperties.getRedirectUri().toString())
                .scheme(oAuth2ClientWebAuthenticationDetails.getScheme())
                .host(oAuth2ClientWebAuthenticationDetails.getHost())
                .port(oAuth2ClientWebAuthenticationDetails.getPort())
                .build()
                .toUri();
        } else {
            redirectUri = this.oAuth2ClientProperties.getRedirectUri();
        }

        return redirectUri;
    }

    /**
     * @param oAuth2ClientProperties
     */
    public void setoAuth2ClientProperties(OAuth2ClientProperties oAuth2ClientProperties) {
        this.oAuth2ClientProperties = oAuth2ClientProperties;
    }

}

package com.ontimize.jee.server.security.authentication;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.util.Assert;

import com.ontimize.jee.server.security.CachedUserDetailsService;

public class OntimizeAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final Logger logger = LoggerFactory.getLogger(OntimizeAuthenticationFilter.class);

    public static final String DEFAULT_TOKEN_HEADER = "X-Auth-Token";

    private final String tokenHeader = OntimizeAuthenticationFilter.DEFAULT_TOKEN_HEADER;

    public static final String ORIGIN = "Origin";

    protected AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new OntimizeWebAuthenticationDetailsSource();

    private List<IAuthenticationMechanism> authenticationMechanismList;

    private UserCache userCache;

    private UserDetailsService userDetailsService;

    private boolean generateJwtHeader;

    private ISecurityJWTTokenGenerator tokenGenerator;

    private UserDetailsService cachedUserDetailsService;

    private AuthenticationEntryPoint authenticationEntryPoint;

    public OntimizeAuthenticationFilter() {
        this("/**");
    }

    public OntimizeAuthenticationFilter(String path) {
        super(path);
        this.generateJwtHeader = false;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        Assert.notNull(this.authenticationEntryPoint, "authenticationEntryPoint property is mandatory");
        this.cachedUserDetailsService = new CachedUserDetailsService(this.userCache, this.userDetailsService);
        this.setAuthenticationSuccessHandler(new OntimizeAuthenticationSuccessHandler());
    }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        return true;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (!this.requiresAuthentication(request, response)) {
            chain.doFilter(request, response);

            return;
        }

        if (OntimizeAuthenticationFilter.logger.isDebugEnabled()) {
            OntimizeAuthenticationFilter.logger.debug("Request is to process authentication");
        }

        Authentication authResult;

        try {
            authResult = this.attemptAuthentication(request, response);
            if (authResult != null) {
                this.successfulAuthentication(request, response, chain, authResult);
            }
            chain.doFilter(request, response);
        } catch (InternalAuthenticationServiceException failed) {
            OntimizeAuthenticationFilter.logger
                .error("An internal error occurred while trying to authenticate the user.", failed);
            this.unsuccessfulAuthentication(request, response, failed);
        } catch (AuthenticationException failed) {
            // Authentication failed
            this.unsuccessfulAuthentication(request, response, failed);
        }
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        try {
            AuthenticationResult authenticationResult = null;
            for (IAuthenticationMechanism authenticationMechanism : this.authenticationMechanismList) {
                authenticationResult = authenticationMechanism.authenticate(request, response,
                        this.getAuthenticationManager(), this.cachedUserDetailsService);
                if (authenticationResult != null) {
                    // success authentication create Token
                    // check authentication
                    Authentication authentication = authenticationResult.getAuthentication();
                    if (authentication instanceof AbstractAuthenticationToken) {
                        ((AbstractAuthenticationToken) authentication)
                            .setDetails(this.authenticationDetailsSource.buildDetails(request));
                    }
                    authentication = this.getAuthenticationManager().authenticate(authentication);
                    if (authenticationResult.isGenerateJWToken()) {
                        this.successfulLogin(request, response, authentication);
                    }
                    return authentication;
                }
            }
        } catch (AuthenticationException authEx) {
            this.authenticationEntryPoint.commence(request, response, authEx);
        }
        return null;
    }

    protected void successfulLogin(HttpServletRequest request, HttpServletResponse response, Authentication authResult)
            throws IOException, ServletException {
        OntimizeAuthenticationFilter.logger.debug("Authentication request success: {}", authResult);
        if (this.generateJwtHeader) {
            String token = this.generateToken(request, authResult);
            response.setHeader(OntimizeAuthenticationFilter.DEFAULT_TOKEN_HEADER, token);
        }
    }

    protected String generateToken(HttpServletRequest request, Authentication authResult) {
        return this.tokenGenerator.generateToken(request, authResult);
    }


    public void setGenerateJwtHeader(boolean generateJwtHeader) {
        this.generateJwtHeader = generateJwtHeader;
    }

    public boolean isGenerateJwtHeader() {
        return this.generateJwtHeader;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public UserDetailsService getUserDetailsService() {
        return this.userDetailsService;
    }

    public UserCache getUserCache() {
        return this.userCache;
    }

    public void setUserCache(UserCache userCache) {
        this.userCache = userCache;
    }

    public void setAuthenticationMechanismList(List<IAuthenticationMechanism> authenticationMechanismList) {
        this.authenticationMechanismList = authenticationMechanismList;
    }

    public List<IAuthenticationMechanism> getAuthenticationMechanismList() {
        return this.authenticationMechanismList;
    }

    public AuthenticationEntryPoint getAuthenticationEntryPoint() {
        return this.authenticationEntryPoint;
    }

    public void setAuthenticationEntryPoint(AuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    public void setTokenGenerator(ISecurityJWTTokenGenerator tokenGenerator) {
        this.tokenGenerator = tokenGenerator;
    }

    public ISecurityJWTTokenGenerator getTokenGenerator() {
        return this.tokenGenerator;
    }

}

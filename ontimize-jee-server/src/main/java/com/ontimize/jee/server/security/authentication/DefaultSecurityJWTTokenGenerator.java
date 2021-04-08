package com.ontimize.jee.server.security.authentication;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import com.ontimize.jee.server.security.authentication.jwt.IJwtService;
import com.ontimize.jee.server.security.authentication.jwt.JwtAuthenticationMechanism;

public class DefaultSecurityJWTTokenGenerator implements ISecurityJWTTokenGenerator {

    private IJwtService jwtService;

    public IJwtService getJwtService() {
        return this.jwtService;
    }

    public void setJwtService(IJwtService jwtService) {
        this.jwtService = jwtService;
    }

    public String generateToken(HttpServletRequest request, Authentication authResult) {
        Map<String, Object> claims = this.provideClaims(request, authResult);
        return this.generateToken(claims);
    }

    public String generateToken(Map<String, Object> claims) {
        return this.jwtService.sign(claims);
    }

    protected Map<String, Object> provideClaims(HttpServletRequest request, Authentication authResult) {
        UserDetails userDetails = (UserDetails) authResult.getPrincipal();
        String username = userDetails.getUsername();
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtAuthenticationMechanism.JWT_TOKEN_KEY_USERNAME, username);
        claims.put(JwtAuthenticationMechanism.JWT_TOKEN_KEY_CREATION_TIME, System.currentTimeMillis());
        return claims;
    }

}

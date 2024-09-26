package com.ontimize.jee.server.security.authentication;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;

public interface ISecurityJWTTokenGenerator {

    String generateToken(HttpServletRequest request, Authentication authResult);

    String generateToken(Map<String, Object> claims);

}

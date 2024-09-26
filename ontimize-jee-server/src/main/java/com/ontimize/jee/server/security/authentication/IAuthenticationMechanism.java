package com.ontimize.jee.server.security.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface IAuthenticationMechanism {

    AuthenticationResult authenticate(HttpServletRequest request, HttpServletResponse response,
            AuthenticationManager authenticationManager, UserDetailsService userDetailsService);

}

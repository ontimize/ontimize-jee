package com.ontimize.jee.server.security.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface IAuthenticationMechanism {

    AuthenticationResult authenticate(HttpServletRequest request, HttpServletResponse response,
            AuthenticationManager authenticationManager, UserDetailsService userDetailsService);

}

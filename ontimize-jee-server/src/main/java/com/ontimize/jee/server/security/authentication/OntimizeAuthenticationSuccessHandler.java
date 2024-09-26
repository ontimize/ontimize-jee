package com.ontimize.jee.server.security.authentication;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

public class OntimizeAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest arg0, HttpServletResponse arg1, Authentication arg2)
            throws IOException, ServletException {
        // We do not need to do anything extra on REST authentication success, because there is no page to
        // redirect to
    }

}

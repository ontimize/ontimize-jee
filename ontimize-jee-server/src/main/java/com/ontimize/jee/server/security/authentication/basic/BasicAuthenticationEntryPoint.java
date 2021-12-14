package com.ontimize.jee.server.security.authentication.basic;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class BasicAuthenticationEntryPoint
		extends org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {

		if (request.getHeader("user-agent") == null) {
			super.commence(request, response, authException);
		} else {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
		}
	}

	@Override
	public void afterPropertiesSet() {
		setRealmName("ONTIMIZE REALM");
		super.afterPropertiesSet();
	}
}
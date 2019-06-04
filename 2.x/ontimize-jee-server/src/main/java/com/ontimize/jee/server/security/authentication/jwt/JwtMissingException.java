package com.ontimize.jee.server.security.authentication.jwt;

import org.springframework.security.authentication.AuthenticationServiceException;

public class JwtMissingException extends AuthenticationServiceException {

	public JwtMissingException(String msg) {
		super(msg);
	}
}

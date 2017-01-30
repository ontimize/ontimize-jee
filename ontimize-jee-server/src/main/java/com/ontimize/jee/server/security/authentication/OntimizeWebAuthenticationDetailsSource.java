package com.ontimize.jee.server.security.authentication;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class OntimizeWebAuthenticationDetailsSource implements AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails> {

	/**
	 * @param context
	 * @return
	 */
	@Override
	public OntimizeWebAuthenticationDetails buildDetails(HttpServletRequest context) {
		return new OntimizeWebAuthenticationDetails(context);
	}
}
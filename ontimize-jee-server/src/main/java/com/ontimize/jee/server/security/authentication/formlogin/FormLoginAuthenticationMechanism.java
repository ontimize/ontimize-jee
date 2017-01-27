package com.ontimize.jee.server.security.authentication.formlogin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

import com.ontimize.jee.server.security.authentication.AuthenticationResult;
import com.ontimize.jee.server.security.authentication.IAuthenticationMechanism;

public class FormLoginAuthenticationMechanism implements IAuthenticationMechanism {

	public static final String	SPRING_SECURITY_FORM_USERNAME_KEY	= "username";
	public static final String	SPRING_SECURITY_FORM_PASSWORD_KEY	= "password";

	private String				usernameParameter					= FormLoginAuthenticationMechanism.SPRING_SECURITY_FORM_USERNAME_KEY;
	private String				passwordParameter					= FormLoginAuthenticationMechanism.SPRING_SECURITY_FORM_PASSWORD_KEY;
	private boolean				postOnly							= true;
	private RequestMatcher		loginRequestMatcher					= new AntPathRequestMatcher("/**/login", "POST");

	@Override
	public AuthenticationResult authenticate(HttpServletRequest request, HttpServletResponse response, AuthenticationManager authenticationManager,
			UserDetailsService userDetailsService) {
		if (!this.loginRequestMatcher.matches(request)) {
			return null;
		}

		if (this.postOnly && !request.getMethod().equals("POST")) {
			throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
		}

		String username = this.obtainUsername(request);
		String password = this.obtainPassword(request);

		if (username == null) {
			username = "";
		}

		if (password == null) {
			password = "";
		}

		username = username.trim();

		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

		return new AuthenticationResult(true, authRequest);
	}

	/**
	 * Enables subclasses to override the composition of the password, such as by including additional values and a separator. <p> This might be used for example if a
	 * postcode/zipcode was required in addition to the password. A delimiter such as a pipe (|) should be used to separate the password and extended value(s). The
	 * <code>AuthenticationDao</code> will need to generate the expected password in a corresponding manner. </p>
	 *
	 * @param request
	 *            so that request attributes can be retrieved
	 *
	 * @return the password that will be presented in the <code>Authentication</code> request token to the <code>AuthenticationManager</code>
	 */
	protected String obtainPassword(HttpServletRequest request) {
		return request.getParameter(this.passwordParameter);
	}

	/**
	 * Enables subclasses to override the composition of the username, such as by including additional values and a separator.
	 *
	 * @param request
	 *            so that request attributes can be retrieved
	 *
	 * @return the username that will be presented in the <code>Authentication</code> request token to the <code>AuthenticationManager</code>
	 */
	protected String obtainUsername(HttpServletRequest request) {
		return request.getParameter(this.usernameParameter);
	}

	/**
	 * Sets the parameter name which will be used to obtain the username from the login request.
	 *
	 * @param usernameParameter
	 *            the parameter name. Defaults to "username".
	 */
	public void setUsernameParameter(String usernameParameter) {
		Assert.hasText(usernameParameter, "Username parameter must not be empty or null");
		this.usernameParameter = usernameParameter;
	}

	/**
	 * Sets the parameter name which will be used to obtain the password from the login request..
	 *
	 * @param passwordParameter
	 *            the parameter name. Defaults to "password".
	 */
	public void setPasswordParameter(String passwordParameter) {
		Assert.hasText(passwordParameter, "Password parameter must not be empty or null");
		this.passwordParameter = passwordParameter;
	}

	/**
	 * Defines whether only HTTP POST requests will be allowed by this filter. If set to true, and an authentication request is received which is not a POST request, an exception
	 * will be raised immediately and authentication will not be attempted. The <tt>unsuccessfulAuthentication()</tt> method will be called as if handling a failed authentication.
	 * <p> Defaults to <tt>true</tt> but may be overridden by subclasses.
	 */
	public void setPostOnly(boolean postOnly) {
		this.postOnly = postOnly;
	}

	public final String getUsernameParameter() {
		return this.usernameParameter;
	}

	public final String getPasswordParameter() {
		return this.passwordParameter;
	}

	public void setLoginRequestMatcher(RequestMatcher loginRequestMatcher) {
		this.loginRequestMatcher = loginRequestMatcher;
	}

	public RequestMatcher getLoginRequestMatcher() {
		return this.loginRequestMatcher;
	}
}

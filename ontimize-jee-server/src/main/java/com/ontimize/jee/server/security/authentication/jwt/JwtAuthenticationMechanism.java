package com.ontimize.jee.server.security.authentication.jwt;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.www.NonceExpiredException;

import com.ontimize.jee.server.security.authentication.AuthenticationResult;
import com.ontimize.jee.server.security.authentication.IAuthenticationMechanism;
import com.ontimize.jee.server.security.authentication.OntimizeAuthenticationProvider;

public class JwtAuthenticationMechanism implements IAuthenticationMechanism {

	private static final Logger	logger						= LoggerFactory.getLogger(JwtAuthenticationMechanism.class);

	public static final String	JWT_TOKEN_KEY_CREATION_TIME	= "creation-time";
	public static final String	JWT_TOKEN_KEY_USERNAME		= "username";
	private long				tokenExpirationTime;
	private IJwtService			jwtService;
	protected boolean			refreshToken;

	public JwtAuthenticationMechanism() {
		super();
		this.tokenExpirationTime = 0;
		this.refreshToken = false;
	}

	@Override
	public AuthenticationResult authenticate(HttpServletRequest request, HttpServletResponse response, AuthenticationManager authenticationManager,
	        UserDetailsService userDetailsService) {
		String header = request.getHeader("Authorization");
		if ((header == null) || !header.startsWith("Bearer ")) {
			// throw new JwtTokenMissingException("No JWT token found in request headers");
			return null;
		}

		String authToken = header.substring(7);
		return new AuthenticationResult(this.refreshToken, this.composeAthenticationToken(authToken, userDetailsService));
	}

	protected Authentication composeAthenticationToken(String token, UserDetailsService userDetailsService) throws AuthenticationException {
		Map<String, Object> claims = this.jwtService.verify(token);
		String username = this.obtainUsernameFromClaims(claims);
		long tokenCreationTime = this.obtainTokenCreationTimeFromClaims(claims);
		// TODO marcar un tiempo de expiracion del token
		JwtAuthenticationMechanism.logger.debug("token info {} : {}", this.tokenExpirationTime, tokenCreationTime);
		if ((this.tokenExpirationTime > 0) && ((tokenCreationTime + this.tokenExpirationTime) < System.currentTimeMillis())) {
			JwtAuthenticationMechanism.logger.debug("token expired {} : {}", this.tokenExpirationTime, tokenCreationTime);
			throw new NonceExpiredException("Token expired");
		}
		if (username == null) {
			throw new IllegalStateException("Claims 'username' and/or 'authorities' cannot be null");
		}
		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		if (userDetails == null) {
			throw new IllegalStateException("JWT Username not found");
		}
		return new UsernamePasswordAuthenticationToken(username, OntimizeAuthenticationProvider.NO_AUTHENTICATION_TOKEN);
	}

	protected String obtainUsernameFromClaims(Map<String, Object> claims) {
		return (String) claims.get(JwtAuthenticationMechanism.JWT_TOKEN_KEY_USERNAME);
	}

	protected long obtainTokenCreationTimeFromClaims(Map<String, Object> claims) {
		return ((Number) claims.get(JwtAuthenticationMechanism.JWT_TOKEN_KEY_CREATION_TIME)).longValue();
	}

	public IJwtService getJwtService() {
		return this.jwtService;
	}

	public void setJwtService(IJwtService jwtService) {
		this.jwtService = jwtService;
	}

	public void setTokenExpirationTime(long tokenExpirationTime) {
		this.tokenExpirationTime = tokenExpirationTime;
	}

	public long getTokenExpirationTime() {
		return this.tokenExpirationTime;
	}

	/**
	 * @return the refreshToken
	 */
	public boolean isRefreshToken() {
		return this.refreshToken;
	}

	/**
	 * @param refreshToken
	 *            the refreshToken to set
	 */
	public void setRefreshToken(boolean refreshToken) {
		this.refreshToken = refreshToken;
	}

}

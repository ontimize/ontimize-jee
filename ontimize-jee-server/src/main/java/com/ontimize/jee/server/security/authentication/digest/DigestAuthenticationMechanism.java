package com.ontimize.jee.server.security.authentication.digest;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.authentication.www.DigestAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.NonceExpiredException;
import org.springframework.util.StringUtils;

import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.server.security.authentication.AuthenticationResult;
import com.ontimize.jee.server.security.authentication.IAuthenticationMechanism;
import com.ontimize.jee.server.security.authentication.OntimizeAuthenticationProvider;

public class DigestAuthenticationMechanism implements IAuthenticationMechanism {

	private static final Logger				logger					= LoggerFactory.getLogger(DigestAuthenticationMechanism.class);

	private DigestAuthenticationEntryPoint	authenticationEntryPoint;
	private boolean							passwordAlreadyEncoded	= false;
	protected MessageSourceAccessor			messages				= SpringSecurityMessageSource.getAccessor();

	@Override
	public AuthenticationResult authenticate(HttpServletRequest request, HttpServletResponse response, AuthenticationManager authenticationManager,
	        UserDetailsService userDetailsService) {
		String header = request.getHeader("Authorization");

		if ((header == null) || !header.startsWith("Digest ")) {
			return null;
		}

		DigestAuthenticationMechanism.logger.debug("Digest Authorization header received from user agent: {}", header);

		DigestData digestAuth = new DigestData(header);

		try {
			digestAuth.validateAndDecode(this.authenticationEntryPoint.getKey(), this.authenticationEntryPoint.getRealmName());
		} catch (BadCredentialsException error) {
			throw error;
		}

		// Lookup password for presented username
		// NB: DAO-provided password MUST be clear text - not encoded/salted
		// (unless this instance's passwordAlreadyEncoded property is 'false')
		UserDetails user = null;
		String serverDigestMd5;

		try {
			user = userDetailsService.loadUserByUsername(digestAuth.getUsername());
			CheckingTools.failIf(user == null, AuthenticationServiceException.class, "AuthenticationDao returned null, which is an interface contract violation");
			serverDigestMd5 = digestAuth.calculateServerDigest(user.getPassword(), request.getMethod());
		} catch (UsernameNotFoundException notFound) {
			throw new BadCredentialsException(
			        this.messages.getMessage("DigestAuthenticationFilter.usernameNotFound", new Object[] { digestAuth.getUsername() }, "Username {0} not found"));

		}

		// If digest is still incorrect, definitely reject authentication attempt
		if (!serverDigestMd5.equals(digestAuth.getResponse())) {
			DigestAuthenticationMechanism.logger.debug("Expected response: '{}' but received: '{}'; is AuthenticationDao returning clear text passwords?", serverDigestMd5,
			        digestAuth.getResponse());
			throw new BadCredentialsException(this.messages.getMessage("DigestAuthenticationFilter.incorrectResponse", "Incorrect response"));
		}

		// To get this far, the digest must have been valid
		// Check the nonce has not expired
		// We do this last so we can direct the user agent its nonce is stale
		// but the request was otherwise appearing to be valid
		if (digestAuth.isNonceExpired()) {
			throw new NonceExpiredException(this.messages.getMessage("DigestAuthenticationFilter.nonceExpired", "Nonce has expired/timed out"));
		}

		if (DigestAuthenticationMechanism.logger.isDebugEnabled()) {
			DigestAuthenticationMechanism.logger.debug("Authentication success for user: '{}' with response: '{}'", digestAuth.getUsername(), digestAuth.getResponse());
		}

		return new AuthenticationResult(true, this.createSuccessfulAuthentication(request, user));
	}

	private Authentication createSuccessfulAuthentication(HttpServletRequest request, UserDetails user) {
		return new UsernamePasswordAuthenticationToken(user, OntimizeAuthenticationProvider.NO_AUTHENTICATION_TOKEN, user.getAuthorities());
	}

	public DigestAuthenticationEntryPoint getAuthenticationEntryPoint() {
		return this.authenticationEntryPoint;
	}

	public void setAuthenticationEntryPoint(DigestAuthenticationEntryPoint authenticationEntryPoint) {
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

	public void setPasswordAlreadyEncoded(boolean passwordAlreadyEncoded) {
		this.passwordAlreadyEncoded = passwordAlreadyEncoded;
	}

	public boolean isPasswordAlreadyEncoded() {
		return this.passwordAlreadyEncoded;
	}

	private class DigestData {

		private final String	username;
		private final String	realm;
		private final String	nonce;
		private final String	uri;
		private final String	response;
		private final String	qop;
		private final String	nc;
		private final String	cnonce;
		private final String	section212response;
		private long			nonceExpiryTime;

		DigestData(String header) {
			this.section212response = header.substring(7);
			String[] headerEntries = DigestAuthUtils.splitIgnoringQuotes(this.section212response, ',');
			Map<String, String> headerMap = DigestAuthUtils.splitEachArrayElementAndCreateMap(headerEntries, "=", "\"");

			this.username = headerMap.get("username");
			this.realm = headerMap.get("realm");
			this.nonce = headerMap.get("nonce");
			this.uri = headerMap.get("uri");
			this.response = headerMap.get("response");
			this.qop = headerMap.get("qop"); // RFC 2617 extension
			this.nc = headerMap.get("nc"); // RFC 2617 extension
			this.cnonce = headerMap.get("cnonce"); // RFC 2617 extension

			if (DigestAuthenticationMechanism.logger.isDebugEnabled()) {
				DigestAuthenticationMechanism.logger.debug(
				        "Extracted username: '" + this.username + "'; realm: '" + this.realm + "'; nonce: '" + this.nonce + "'; uri: '" + this.uri + "'; response: '" + this.response + "'");
			}
		}

		void validateAndDecode(String entryPointKey, String expectedRealm) throws BadCredentialsException {
			// Check all required parameters were supplied (ie RFC 2069)
			if ((this.username == null) || (this.realm == null) || (this.nonce == null) || (this.uri == null) || (this.response == null)) {
				throw new BadCredentialsException(DigestAuthenticationMechanism.this.messages.getMessage("DigestAuthenticationFilter.missingMandatory",
				        new Object[] { this.section212response }, "Missing mandatory digest value; received header {0}"));
			}
			// Check all required parameters for an "auth" qop were supplied (ie RFC 2617)
			if ("auth".equals(this.qop)) {
				if ((this.nc == null) || (this.cnonce == null)) {
					if (DigestAuthenticationMechanism.logger.isDebugEnabled()) {
						DigestAuthenticationMechanism.logger.debug("extracted nc: '" + this.nc + "'; cnonce: '" + this.cnonce + "'");
					}

					throw new BadCredentialsException(DigestAuthenticationMechanism.this.messages.getMessage("DigestAuthenticationFilter.missingAuth",
					        new Object[] { this.section212response }, "Missing mandatory digest value; received header {0}"));
				}
			}

			// Check realm name equals what we expected
			if (!expectedRealm.equals(this.realm)) {
				throw new BadCredentialsException(DigestAuthenticationMechanism.this.messages.getMessage("DigestAuthenticationFilter.incorrectRealm",
				        new Object[] { this.realm, expectedRealm }, "Response realm name '{0}' does not match system realm name of '{1}'"));
			}

			// Check nonce was Base64 encoded (as sent by DigestAuthenticationEntryPoint)
			if (!Base64.isBase64(this.nonce.getBytes())) {
				throw new BadCredentialsException(DigestAuthenticationMechanism.this.messages.getMessage("DigestAuthenticationFilter.nonceEncoding", new Object[] { this.nonce },
				        "Nonce is not encoded in Base64; received nonce {0}"));
			}

			// Decode nonce from Base64
			// format of nonce is:
			// base64(expirationTime + ":" + md5Hex(expirationTime + ":" + key))
			String nonceAsPlainText = new String(Base64.decode(this.nonce.getBytes()));
			String[] nonceTokens = StringUtils.delimitedListToStringArray(nonceAsPlainText, ":");

			if (nonceTokens.length != 2) {
				throw new BadCredentialsException(DigestAuthenticationMechanism.this.messages.getMessage("DigestAuthenticationFilter.nonceNotTwoTokens",
				        new Object[] { nonceAsPlainText }, "Nonce should have yielded two tokens but was {0}"));
			}

			// Extract expiry time from nonce

			try {
				this.nonceExpiryTime = new Long(nonceTokens[0]).longValue();
			} catch (NumberFormatException nfe) {
				throw new BadCredentialsException(DigestAuthenticationMechanism.this.messages.getMessage("DigestAuthenticationFilter.nonceNotNumeric",
				        new Object[] { nonceAsPlainText }, "Nonce token should have yielded a numeric first token, but was {0}"));
			}

			// Check signature of nonce matches this expiry time
			String expectedNonceSignature = DigestAuthUtils.md5Hex(this.nonceExpiryTime + ":" + entryPointKey);

			if (!expectedNonceSignature.equals(nonceTokens[1])) {
				new BadCredentialsException(DigestAuthenticationMechanism.this.messages.getMessage("DigestAuthenticationFilter.nonceCompromised", new Object[] { nonceAsPlainText },
				        "Nonce token compromised {0}"));
			}
		}

		String calculateServerDigest(String password, String httpMethod) {
			// Compute the expected response-digest (will be in hex form)

			// Don't catch IllegalArgumentException (already checked validity)
			return DigestAuthUtils.generateDigest(DigestAuthenticationMechanism.this.passwordAlreadyEncoded, this.username, this.realm, password, httpMethod, this.uri, this.qop,
			        this.nonce, this.nc, this.cnonce);
		}

		boolean isNonceExpired() {
			long now = System.currentTimeMillis();
			return this.nonceExpiryTime < now;
		}

		String getUsername() {
			return this.username;
		}

		String getResponse() {
			return this.response;
		}
	}
}

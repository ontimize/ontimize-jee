package com.ontimize.jee.server.security.authentication.ldap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.ontimize.jee.server.security.authentication.AuthenticationResult;
import com.ontimize.jee.server.security.authentication.IAuthenticationMechanism;
import com.ontimize.jee.server.security.authentication.OntimizeAuthenticationProvider;

public class LdapAuthenticationMechanism implements IAuthenticationMechanism {

	private static final Logger LOGGER = LoggerFactory.getLogger(LdapAuthenticationMechanism.class);
	private String credentialsCharset = "UTF-8";

	public static final String HOST_PROPERTY = "${ontimize.security.ldap.host}";
	public static final String PORT_PROPERTY = "${ontimize.security.ldap.port}";
	public static final String LOGINTYPE_PROPERTY = "${ontimize.security.ldap.loginType}";
	public static final String BINDDN_PROPERTY = "${ontimize.security.ldap.binddn}";
	public static final String BASEDN_PROPERTY = "${ontimize.security.ldap.basedn}";
	public static final String DOMAIN_PROPERTY = "${ontimize.security.ldap.domain}";
	public static final String SSL_PROPERTY = "${ontimize.security.ldap.ssl:false}";


	private String host;
	private int port;
	private String loginType;
	private String bindDn;
	private String baseDn;
	private String domain;
	private boolean ssl;

	public LdapAuthenticationMechanism(@Value(value = LdapAuthenticationMechanism.HOST_PROPERTY) String hostProperty,
			@Value(value = LdapAuthenticationMechanism.PORT_PROPERTY) int portProperty,
			@Value(value = LdapAuthenticationMechanism.LOGINTYPE_PROPERTY) String loginTypeProperty,
			@Value(value = LdapAuthenticationMechanism.BINDDN_PROPERTY) String bindDnProperty,
			@Value(value = LdapAuthenticationMechanism.BASEDN_PROPERTY) String baseDnProperty,
			@Value(value = LdapAuthenticationMechanism.DOMAIN_PROPERTY) String domainProperty,
			@Value(value = LdapAuthenticationMechanism.SSL_PROPERTY) boolean sslProperty) {
		this.host = hostProperty;
		this.port = portProperty;
		this.loginType = loginTypeProperty;
		this.bindDn = bindDnProperty;
		this.baseDn = baseDnProperty;
		this.domain = domainProperty;
		this.ssl = sslProperty;
	}

	@Override
	public AuthenticationResult authenticate(final HttpServletRequest request, final HttpServletResponse response,

			final AuthenticationManager authenticationManager, final UserDetailsService userDetailsService) {

		try {

			final String header = request.getHeader("Authorization");
			if ((header == null) || !header.startsWith("Basic ")) {
				return null;
			}

			final String[] tokens = this.extractAndDecodeHeader(header, request);
			assert tokens.length == 2;
			final String username = tokens[0];
			final String password = tokens[1];

			LdapAuthenticationMechanism.LOGGER.trace("Validating access for user : '{}'", username);

			DirContext dirContext = null;

			if (this.loginType.equals("DN")) {
				String userDn = "uid=" + username + "," + this.bindDn;
				dirContext = LdapAuthenticationMechanism.connect(userDn, password, this.host, this.port, null, this.ssl);
			} else if (this.loginType.equals("simple")) {
				dirContext = LdapAuthenticationMechanism.connect(username, password, this.host, this.port, this.domain,
						this.ssl);
			}

			if (dirContext != null) {
				return new AuthenticationResult(true, new UsernamePasswordAuthenticationToken(username,
						OntimizeAuthenticationProvider.NO_AUTHENTICATION_TOKEN));
			}
			LdapAuthenticationMechanism.LOGGER.error("System authentication failed: no connect to LDAP");
			throw new BadCredentialsException(LdapError.NO_LDAP_CONNECTION.toString());
		} catch (NamingException e) {
			LdapAuthenticationMechanism.LOGGER
			.error("System authentication failed: NamingException searching into server LDAP", e);
			throw new BadCredentialsException(LdapError.ERROR_SEARCHING_IN_LDAP.toString());
		} catch (LoginException e) {
			LdapAuthenticationMechanism.LOGGER.error("System authentication failed: LoginException with server LDAP",
					e);
			throw new BadCredentialsException(LdapError.ERROR_LOGIN_LDAP.toString());
		} catch (IOException e) {
			LdapAuthenticationMechanism.LOGGER.error("System authentication failed: IOException with server LDAP", e);
			throw new BadCredentialsException(LdapError.ERROR_IO_LDAP.toString());
		}

	}

	public static synchronized DirContext connect(final String user, final String password, final String hosts,
			final int port, final String adddomain, final boolean ssl)
					throws NamingException, java.io.IOException, LoginException {

		if ((hosts == null) || (hosts.length() == 0)) {
			LdapAuthenticationMechanism.LOGGER.error("LDAP host cannot be neither null nor empty");
			throw new IllegalArgumentException(LdapError.EMPTY_LDAP_HOST.toString());
		}

		StringTokenizer st = new StringTokenizer(hosts, ";");

		if (st.hasMoreTokens()) {
			String host = st.nextToken();
			return LdapAuthenticationMechanism._connect(user, password, host, port, adddomain, ssl);
		}

		return null;

	}

	private static synchronized DirContext _connect(final String user, final String password, final String host,
			final int port, final String adddomain, final boolean ssl)
					throws NamingException, java.io.IOException, LoginException {

		Hashtable<String, String> props = new Hashtable<>();
		if ((user == null) || (user.length() == 0)) {
			LdapAuthenticationMechanism.LOGGER.error("user cannot be neither null nor empty");
			throw new IllegalArgumentException(LdapError.EMPTY_LDAP_USER.toString());

		}

		if ((password == null) || (password.length() == 0)) {
			LdapAuthenticationMechanism.LOGGER.error("password cannot be neither null nor empty");
			throw new IllegalArgumentException(LdapError.EMPTY_LDAP_PASSWORD.toString());

		}

		props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		props.put(Context.PROVIDER_URL, "ldap://" + host + ":" + port);
		props.put(Context.SECURITY_AUTHENTICATION, "simple");

		if (adddomain != null) {
			props.put(Context.SECURITY_PRINCIPAL, user + "@" + adddomain);
		} else {
			props.put(Context.SECURITY_PRINCIPAL, user);
		}

		props.put(Context.SECURITY_CREDENTIALS, password);

		if (ssl) {
			props.put(Context.SECURITY_PROTOCOL, "ssl");
		}

		props.put(Context.REFERRAL, "follow");
		DirContext ctx = null;

		try {
			ctx = new InitialDirContext(props);
			LdapAuthenticationMechanism.LOGGER.info("Authentication sucessfully in LDAP");

		} catch (Exception e) {
			LdapAuthenticationMechanism.LOGGER.error("System authentication failed: wrong user and/or pass in LDAP");
			throw new BadCredentialsException(LdapError.LDAP_AUTH_USER_PASS_NOT_VALID.toString());

		}

		return ctx;

	}

	private String[] extractAndDecodeHeader(final String header, final HttpServletRequest request) {

		try {
			final byte[] base64Token = header.substring(6).getBytes("UTF-8");
			byte[] decoded;
			decoded = Base64.getDecoder().decode(base64Token);

			final String token = new String(decoded, this.getCredentialsCharset(request));
			final int delim = token.indexOf(':');

			if (delim == -1) {
				throw new BadCredentialsException("Invalid basic authentication token");
			}
			return new String[] { token.substring(0, delim), token.substring(delim + 1) };
		} catch (IllegalArgumentException | UnsupportedEncodingException error) {
			throw new BadCredentialsException("Failed to decode basic authentication token", error);
		}
	}

	protected String getCredentialsCharset(final HttpServletRequest httpRequest) {
		return this.credentialsCharset;
	}

}
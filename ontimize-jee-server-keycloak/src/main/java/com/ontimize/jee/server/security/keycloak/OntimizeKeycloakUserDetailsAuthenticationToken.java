package com.ontimize.jee.server.security.keycloak;

import java.util.Collection;

import org.keycloak.adapters.OidcKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

public class OntimizeKeycloakUserDetailsAuthenticationToken extends KeycloakAuthenticationToken {
	private static final long serialVersionUID = 5422695669492358566L;
	private UserDetails userDetails;

	public OntimizeKeycloakUserDetailsAuthenticationToken(UserDetails userDetails, OidcKeycloakAccount account, boolean interactive,
			Collection<? extends GrantedAuthority> authorities) {
		super(account, interactive, authorities);
		Assert.notNull(userDetails, "UserDetails required");
		this.userDetails = userDetails;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof OntimizeKeycloakUserDetailsAuthenticationToken)) {
			return false;
		}
		OntimizeKeycloakUserDetailsAuthenticationToken test = (OntimizeKeycloakUserDetailsAuthenticationToken) obj;
		if (!this.userDetails.equals(test.userDetails)) {
			return false;
		}
		return super.equals(obj);
	}

	@Override
	public Object getPrincipal() {
		return this.userDetails;
	}

	@Override
	public int hashCode() {
		int code = super.hashCode();

		code ^= this.userDetails.hashCode();

		return code;
	}
}

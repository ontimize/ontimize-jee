package com.ontimize.jee.server.security.keycloak;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.keycloak.adapters.OidcKeycloakAccount;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.security.XMLClientUtilities;
import com.ontimize.jee.common.services.user.UserInformation;
import com.ontimize.jee.server.configuration.OntimizeConfiguration;
import com.ontimize.jee.server.security.authorization.ISecurityAuthorizator;
import com.ontimize.jee.server.security.authorization.Role;

public class OntimizeKeycloakUserDetailsAuthenticationProvider extends KeycloakAuthenticationProvider {
	private static final Logger logger = LoggerFactory
			.getLogger(OntimizeKeycloakUserDetailsAuthenticationProvider.class);

	@Autowired
	OntimizeConfiguration ontimizeConfiguration;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		final KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) super.authenticate(authentication);

		if (token == null) {
			return null;
		}

		final OidcKeycloakAccount account = token.getAccount();
		final AccessToken accessToken = account.getKeycloakSecurityContext().getToken();
		final String username = accessToken.getPreferredUsername();

		if (username == null) {
			throw new OntimizeJEERuntimeException("username was null");
		}

		final List<GrantedAuthority> authorities = new ArrayList<>();
		Map<String, ?> clientPermissions = null;
		final Collection<GrantedAuthority> kcAuthorities = token.getAuthorities();

		if (kcAuthorities != null) {
			final ISecurityAuthorizator authorizator = ontimizeConfiguration.getSecurityConfiguration().getAuthorizator();
			final List<Map<String, ?>> permissions = new ArrayList<>();

			for (final GrantedAuthority authority : kcAuthorities) {
				final Role role = authorizator.getRole(authority.getAuthority());

				if (role != null) {
					authorities.add(new SimpleGrantedAuthority(role.getName()));

					final Map<String, ?> roleClientPermission = role.getClientPermissions();

					if (roleClientPermission != null) {
						permissions.add(roleClientPermission);
					}
				}
			}

			try {
				clientPermissions = XMLClientUtilities.joinClientPermissions(permissions);
			} catch (Exception e) {
				OntimizeKeycloakUserDetailsAuthenticationProvider.logger
						.error("Error joining clientPermissions for authorities {}", authorities);
				throw new OntimizeJEERuntimeException("Error joining clientPermissions", e);
			}
		}

		final IDToken idtoken = account.getKeycloakSecurityContext().getIdToken();
		final UserInformation userInformation = this.createUserInformation(accessToken, idtoken , authorities, clientPermissions);

		return new OntimizeKeycloakUserDetailsAuthenticationToken(userInformation, account, token.isInteractive(),
				authorities);
	}

	private UserInformation createUserInformation(final AccessToken accessToken, final IDToken idtoken, 
			final List<GrantedAuthority> authorities, final Map<String, ?> clientPermissions) {
		final String username = accessToken.getPreferredUsername();
		final UserInformation userInformation = new UserInformation(username, null, authorities, clientPermissions);

		if (idtoken != null) {
			Map<String, Object> otherClaims = idtoken.getOtherClaims();
			otherClaims.entrySet().stream().forEach(claim ->
				userInformation.addOtherData(claim.getKey(), claim.getValue())
			);
		}

		Map<Object, Object> otherData = userInformation.getOtherData();

		if (!otherData.keySet().contains("usr_id")) {
			userInformation.addOtherData("usr_id", username);
		}

		final String name = accessToken.getName();
		if (!otherData.keySet().contains("usr_name")) {
			userInformation.addOtherData("usr_name", Objects.requireNonNullElse(name, username));
		}
		
		final String photo = accessToken.getPicture();
		if (!otherData.keySet().contains("usr_photo") && photo != null) {
			userInformation.addOtherData("usr_photo", photo);
		}
	
		return userInformation;
	}
}

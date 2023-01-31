package com.ontimize.jee.server.security.keycloak;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
import org.springframework.security.core.userdetails.UsernameNotFoundException;

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
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) super.authenticate(authentication);

		if (token == null) {
			return null;
		}

		final OidcKeycloakAccount account = token.getAccount();
		final AccessToken accessToken = account.getKeycloakSecurityContext().getToken();
		final String username = accessToken.getPreferredUsername();
		final String name = accessToken.getName();
		final String photo = accessToken.getPicture();

		if (username == null) {
			throw new UsernameNotFoundException("username was null");
		}

		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		Collection<GrantedAuthority> kcAuthorities = token.getAuthorities();
		Map<String, ?> clientPermissions = null;

		if (kcAuthorities != null) {
			ISecurityAuthorizator authorizator = ontimizeConfiguration.getSecurityConfiguration().getAuthorizator();

			List<Map<String, ?>> permissions = new ArrayList<>();
			for (GrantedAuthority authority : kcAuthorities) {
				Role role = authorizator.getRole(authority.getAuthority());

				if (role != null) {
					authorities.add(new SimpleGrantedAuthority(role.getName()));

					Map<String, ?> roleClientPermission = role.getClientPermissions();
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

		UserInformation userInformation = new UserInformation(username, null, authorities, clientPermissions);

		IDToken idtoken = account.getKeycloakSecurityContext().getIdToken();
		if (idtoken != null) {
			Map<String, Object> otherClaims = idtoken.getOtherClaims();
			for (String key : otherClaims.keySet()) {
				userInformation.addOtherData(key, otherClaims.get(key));
			}
		}

		Map<Object, Object> otherData = userInformation.getOtherData();

		if (!otherData.keySet().contains("usr_id")) {
			userInformation.addOtherData("usr_id", username);
		}

		if (!otherData.keySet().contains("usr_name")) {
			userInformation.addOtherData("usr_name", name == null ? username : name);
		}

		if (!otherData.keySet().contains("usr_photo") && photo != null) {
			userInformation.addOtherData("usr_photo", photo);
		}

		return new OntimizeKeycloakUserDetailsAuthenticationToken(userInformation, account, token.isInteractive(),
				authorities);
	}
}

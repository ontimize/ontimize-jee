package com.ontimize.jee.server.security.keycloak;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.ontimize.jee.server.security.keycloak.dto.RealmInfo;
import com.ontimize.jee.server.security.keycloak.dto.application.ApplicationRoles;
import com.ontimize.jee.server.security.keycloak.dto.application.RoleInfo;
import com.ontimize.jee.server.security.keycloak.dto.application.UserRoles;

/**
 * UserManagement implementation with keycloak. It uses the keycloak admin
 * client lib and builds with this lib the request to the keycloak admin rest
 * api.
 */
@Service
public class UserManagementKeycloakImpl implements IUserManagement {
	@Autowired
	private KeycloakConfiguration config;

	private static final Logger logger = LoggerFactory.getLogger(UserManagementKeycloakImpl.class);
	private static final ResteasyClient CONNECTION_POOL = new ResteasyClientBuilder().connectionPoolSize(10).build();
	private static final String DEFAULT_LANGUAGE = "en";

	protected Keycloak getInstance() {
		logger.debug("Auth at keycloak for rest admin api Server URL: {} Username: {} ClientId: {}",
				this.config.getAuthServerUrl(), this.config.getAdminUserName(), this.config.getAdminResource());

		return KeycloakBuilder.builder().serverUrl(this.config.getAuthServerUrl()).realm(this.config.getAdminRealm())
				.username(this.config.getAdminUserName()).password(this.config.getAdminPassword())
				.clientId(this.config.getAdminResource()).resteasyClient(CONNECTION_POOL).build();
	}

	/**
	 * Please see further details at UserManagementInterface
	 *
	 * @param realm realm name
	 * @return list of UserRepresentation instances
	 */
	@Override
	public List<UserRepresentation> searchUsers(final String realm) {
		List<UserRepresentation> userList = getInstance().realm(realm).users().list();

		return userList;
	}

	@Override
	public List<RealmInfo> getRealmsForUser(final String username) {
		List<RealmRepresentation> rreps = getInstance().realms().findAll();
		List<RealmInfo> result = new ArrayList<RealmInfo>();

		for (RealmRepresentation r : rreps) {
			List<UserRepresentation> users = this.searchUsers(r.getRealm());
			
			for (UserRepresentation ur : users) {
				if (ur.getUsername().equalsIgnoreCase(username)) {
					RealmInfo ti = new RealmInfo();
					
					ti.setId(r.getRealm());
					ti.setName(r.getDisplayName());
			
					result.add(ti);
					
					break;
				}
			}
		}

		return result;
	}

	@Override
	public RealmResource createRealmIfNotExists(final String realm, final String displayName, final String... clients) {
		List<RealmRepresentation> rreps = getInstance().realms().findAll();

		for (RealmRepresentation r : rreps) {
			if (r.getRealm().equalsIgnoreCase(realm))
				return getInstance().realm(realm);
		}

		RealmRepresentation rrep = new RealmRepresentation();

		rrep.setRealm(realm);
		rrep.setEnabled(true);
		rrep.setDisplayName(displayName);

		getInstance().realms().create(rrep);

		RealmResource rr = getInstance().realm(realm);

		ClientRepresentation cr = new ClientRepresentation();

		cr.setName(realm);
		cr.setClientId(realm);
		cr.setEnabled(true);
		cr.setPublicClient(true);

		Response resp = rr.clients().create(cr);

		if (resp.getStatus() != 201) {
			throw new RuntimeException("error creating realm client");
		}

		for (String client : clients) {
			rr = getInstance().realm(realm);

			cr = new ClientRepresentation();

			cr.setName(client);
			cr.setClientId(client);
			cr.setPublicClient(true);
			cr.setEnabled(true);

			List<String> redirectUris = new ArrayList<String>(Arrays.asList(clients));

			redirectUris.addAll(Arrays.asList(this.config.getRedirectUrls()));

			cr.setRedirectUris(redirectUris);
			cr.setDirectAccessGrantsEnabled(true);
			cr.setStandardFlowEnabled(true);

			resp = rr.clients().create(cr);

			if (resp.getStatus() != 201) {
				throw new RuntimeException("error creating realm client");
			}

			logger.debug("Create realm client response status: " + resp.getStatusInfo());
		}

		return rr;
	}

	@Override
	public void addClientToRealm(final String realm, final String client, final String... redirectUrls) {
		RealmResource rr = this.getRealm(realm);

		if (rr == null) {
			throw new RuntimeException("realm not found");
		}

		ClientRepresentation cr = new ClientRepresentation();

		cr.setName(client);
		cr.setClientId(client);
		cr.setPublicClient(true);
		cr.setEnabled(true);

		List<String> redirectUris = new ArrayList<String>(Arrays.asList(redirectUrls));

		redirectUris.addAll(Arrays.asList(this.config.getRedirectUrls()));

		cr.setRedirectUris(redirectUris);
		cr.setDirectAccessGrantsEnabled(true);
		cr.setStandardFlowEnabled(true);

		rr.clients().create(cr);
	}

	@Override
	public GroupRepresentation getGroup(final String groupName) {
		List<GroupRepresentation> groupList = getInstance().realm(this.config.getRealm()).groups().groups();

		return groupList.stream().filter(g -> groupName.equals(g.getName())).findAny().get();
	}

	@Override
	public void addUserToGroup(final String userId, final String groupId) {
		getInstance().realm(this.config.getRealm()).users().get(userId).joinGroup(groupId);
	}

	/**
	 * Please see further details at UserManagementInterface
	 *
	 * @param username user name
	 * @return list of UserRepresentation instances
	 */
	@Override
	public List<UserRepresentation> searchUserAccount(final String username) {
		logger.debug("Keycloak search for username: {}", username);

		// this should return just one result in the list
		List<UserRepresentation> userList = getInstance().realm(this.config.getRealm()).users().search(username);

		logger.debug("Keycloak user search result count: {}", userList.size());

		return userList;
	}

	/**
	 * Please see further details at UserManagementInterface
	 *
	 * @param userId user is
	 * @return list of GroupRepresentation instances
	 */
	@Override
	public List<GroupRepresentation> searchUserGroups(final String userId) {
		logger.debug("Keycloak user groups search for user id: {}", userId);

		// this should return just one result in the list
		List<GroupRepresentation> groupRepresentationList = getInstance().realm(this.config.getRealm()).users()
				.get(userId).groups();

		logger.debug("Keycloak user groups search result count: {}", groupRepresentationList.size());

		return groupRepresentationList;
	}

	/**
	 * Please see further details at UserManagementInterface
	 *
	 * @param userId keycloak user id
	 * @return UserRepresentation instance
	 */
	@Override
	public UserRepresentation getUserRepresentation(final String userId) {
		return getInstance().realm(this.config.getRealm()).users().get(userId).toRepresentation();
	}

	@Override
	public HttpStatus createGroup(final String groupName) {
		Keycloak keycloak = getInstance();

		GroupRepresentation groupRepresentation = new GroupRepresentation();

		groupRepresentation.setName(groupName);

		RealmResource keycloakImatiak8sResource = keycloak.realm(this.config.getRealm());
		GroupsResource groupsResource = keycloakImatiak8sResource.groups();

		groupsResource.add(groupRepresentation);

		return HttpStatus.CREATED;
	}

	/**
	 * Please see further details at UserManagementInterface
	 */
	@Override
	public HttpStatus createUserAccount(final String realm, final String username, final String firstName,
			final String lastName, final String email, final String password) {

		CredentialRepresentation credential = new CredentialRepresentation();

		credential.setType(CredentialRepresentation.PASSWORD);
		// credential.setTemporary(true); // TODO
		credential.setValue(password);

		UserRepresentation user = new UserRepresentation();

		user.setUsername(username);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setEmail(email);
		user.setEnabled(Boolean.TRUE);
		// user.setEmailVerified(Boolean.TRUE);
		user.setCredentials(Collections.singletonList(credential));
		user.setRequiredActions(Collections.singletonList("UPDATE_PASSWORD"));

		logger.debug("Create new User Account in Keycloak: {}", user.getUsername());

		Keycloak keycloak = getInstance();

		RealmResource keycloakCorporateResource = keycloak.realm(realm);// this.config.getRealm());
		UsersResource keycloakCorporateUserResource = keycloakCorporateResource.users();

		try (Response response = keycloakCorporateUserResource.create(user)) {
			final HttpStatus status = HttpStatus.valueOf(response.getStatus());

			logger.debug("Keycloak Create User Account Response Status: {}", response.getStatusInfo());

			List<UserRepresentation> userList = keycloakCorporateUserResource.search(user.getUsername());

			if (userList.isEmpty()) {
				logger.error("Creation of new Keycloak user account was not successful: Username: {}, Status: {}",
						username, response.getStatus());

				return status;
			}

			UserRepresentation createdUser = userList.get(0);

			UserResource keycloakUserResource = keycloakCorporateResource.users().get(createdUser.getId());

			logger.info("Keycloak User created with username[{}] and userId[{}]", createdUser.getUsername(),
					createdUser.getId());

			// set default language
			createdUser.singleAttribute("locale", DEFAULT_LANGUAGE);

			// persist changes
			keycloakUserResource.update(createdUser);
		}

		return HttpStatus.CREATED;
	}

	/**
	 * Please see further details at UserManagementInterface
	 */
	@Override
	public HttpStatus activateUserAccount(final String username) {
		HttpStatus status = HttpStatus.OK;

		logger.debug("Keycloak search for username: {}", username);

		// this should return just one result in the list
		List<UserRepresentation> userList = getInstance().realm(this.config.getRealm()).users().search(username);

		if (userList != null && !userList.isEmpty()) {
			logger.debug("Keycloak user search result count: {}", userList.size());

			UserRepresentation userRep = userList.get(0);

			logger.info("Get UserResource with user account id {}", userRep.getId());

			// Get the user resource through the user id
			UserResource userRes = getInstance().realm(this.config.getRealm()).users().get(userRep.getId());

			logger.debug("Returned UserResource {}", userRes);

			// Activate user account
			userRep.setEnabled(Boolean.TRUE);
			userRep.setEmailVerified(Boolean.TRUE);

			logger.debug("Activate existing Keycloak user account {}", userRep);

			try {
				userRes.update(userRep);
			} catch (Exception e) {
				logger.error("An error occured during Keycloak user update", e);
			}

			// Activation was successful return OK
			logger.info("Keycloak Activate User Account was successful for username {} ", username);
		} else {
			// Search with username did not find anything in keycloak
			logger.warn("No result for search of username {} in keycloak", username);
			status = HttpStatus.NOT_FOUND;
		}

		return status;
	}

	/**
	 * Please see further details at UserManagementInterface
	 */
	@Override
	public HttpStatus resetPassword(final UserRepresentation userRepresentation, final String password) {
		HttpStatus status = HttpStatus.NO_CONTENT;

		CredentialRepresentation credential = new CredentialRepresentation();
		// VERY IMPORTANT!!! DO NOT REMOVE!!!
		credential.setTemporary(false);
		credential.setType(CredentialRepresentation.PASSWORD);
		credential.setValue(password);

		try {
			RealmResource realmResource = getInstance().realm(this.config.getRealm());
			UsersResource userResource = realmResource.users();
			String userId = userRepresentation.getId();

			userResource.get(userId).resetPassword(credential);
		} catch (ClientErrorException ex) {
			logger.error("Keycloak - Reset password failed with client request error.", ex);

			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return status;
	}

	@Override
	public void updateEmailUserAccount(final String userId, final String email) {
		try {
			UserResource user = getInstance().realm(this.config.getRealm()).users().get(userId);
			UserRepresentation userRepresentation = user.toRepresentation();
			userRepresentation.setEmail(email);
			userRepresentation.setUsername(email.substring(0, email.indexOf("@")));

			user.update(userRepresentation);
		} catch (ClientErrorException ex) {
			logger.error("Keycloak - updateEmailUserAccount error", ex);
		}
	}

	@Override
	public void updateNameUserAccount(final String userId, final String name) {
		try {
			UserResource user = getInstance().realm(this.config.getRealm()).users().get(userId);
			UserRepresentation userRepresentation = user.toRepresentation();
			userRepresentation.setFirstName(name);

			user.update(userRepresentation);
		} catch (ClientErrorException ex) {
			logger.error("Keycloak - updateNameUserAccount error", ex);
		}
	}

	@Override
	public void updateSurnameUserAccount(final String userId, final String surname) {
		try {
			UserResource user = getInstance().realm(this.config.getRealm()).users().get(userId);
			UserRepresentation userRepresentation = user.toRepresentation();
			userRepresentation.setLastName(surname);

			user.update(userRepresentation);
		} catch (ClientErrorException ex) {
			logger.error("Keycloak - updateSurnameUserAccount error", ex);
		}
	}

	@Override
	public void disableUserAccount(final String userId) {
		try {
			UserResource user = getInstance().realm(this.config.getRealm()).users().get(userId);
			UserRepresentation userRepresentation = user.toRepresentation();
			userRepresentation.setEnabled(false);

			user.update(userRepresentation);
		} catch (ClientErrorException ex) {
			logger.error("Keycloak - updateSurnameUserAccount error", ex);
		}
	}

	// @TODO change methods to user uuids to avoid querying all to recover emails,
	// names, etc.

	@Override
	public void addRolesToRealm(final String realm, final String client, final List<RoleInfo> roles) {
		// add user roles to realm client
		RealmResource rr = this.getRealm(realm);

		if (rr == null) {
			throw new RuntimeException("realm not found");
		}

		ClientResource cr = getClient(client, rr);

		if (cr == null) {
			throw new RuntimeException("client not found");
		}

		for (RoleInfo role : roles) {
			RoleRepresentation rRep = new RoleRepresentation();

			rRep.setName(role.getName());
			rRep.setClientRole(true);
			rRep.setDescription(role.getDescription());

			cr.roles().create(rRep);
		}
	}

	private ClientResource getClient(String name, RealmResource realm) {
		List<ClientRepresentation> r = realm.clients().findAll().stream()
				.filter(cr -> cr.getName().equalsIgnoreCase(name)).collect(Collectors.toList());

		if (r.isEmpty()) {
			return null;
		} else {
			return realm.clients().get(r.get(0).getId());
		}
	}

	private RealmResource getRealm(String realm) {
		List<RealmRepresentation> rreps = getInstance().realms().findAll();
		RealmResource rr = null;

		for (RealmRepresentation r : rreps) {
			if (r.getRealm().equalsIgnoreCase(realm)) {
				rr = getInstance().realm(realm);

				break;
			}
		}

		return rr;
	}

	@Override
	public List<String> getUsersForRealm(final String realm) {
		RealmResource rr = this.getRealm(realm);

		if (rr == null) {
			return new ArrayList<String>(); // TODO throw error
		} else {
			return rr.users().list().stream().map(UserRepresentation::getEmail).collect(Collectors.toList());
		}
	}

	@Override
	public List<String> getRolesForUser(final String user, final String realm, final String client) {
		RealmResource rr = this.getRealm(realm);
		if (rr == null) {
			return new ArrayList<String>(); // TODO throw error
		}

		List<UserRepresentation> userR = rr.users().list().stream().filter((u) -> u.getEmail().equalsIgnoreCase(user))
				.collect(Collectors.toList());
		if (userR.isEmpty()) {
			return new ArrayList<String>();
		}

		List<ClientRepresentation> clientR = rr.clients().findAll().stream()
				.filter(cr -> cr.getName().equalsIgnoreCase(client)).collect(Collectors.toList());
		if (clientR.isEmpty()) {
			return new ArrayList<String>();
		}

		List<RoleRepresentation> rolesRep = rr.users().get(userR.get(0).getId()).roles()
				.clientLevel(clientR.get(0).getId()).listEffective();
		if (rolesRep == null || rolesRep.isEmpty()) {
			return new ArrayList<String>();
		}

		return rolesRep.stream().map(RoleRepresentation::getName).collect(Collectors.toList());
	}

	@Override
	public List<RoleInfo> getRolesForClient(final String realm, final String client) {
		RealmResource rr = this.getRealm(realm);

		if (rr == null) {
			return new ArrayList<RoleInfo>(); // TODO throw error
		}

		ClientResource cr = this.getClient(client, rr);

		return cr.roles().list().stream().map(r -> getRoleInfo(r.getName(), r.getDescription()))
				.collect(Collectors.toList());
	}

	private RoleInfo getRoleInfo(String name, String description) {
		RoleInfo ri = new RoleInfo();

		ri.setName(name);
		ri.setDescription(description);

		return ri;
	}

	@Override
	public void setRolesForUser(final String realm, final String client, final List<UserRoles> userRoles) {
		RealmResource rr = this.getRealm(realm);

		if (rr == null) {
			return; // TODO throw error
		}

		ClientResource cr = this.getClient(client, rr);
		if (cr == null) {
			return;
		}

		List<ClientRepresentation> r = rr.clients().findAll().stream()
				.filter(clientRep -> clientRep.getName().equalsIgnoreCase(client)).collect(Collectors.toList());
		if (r == null || r.isEmpty()) {
			return;
		}

		List<UserRepresentation> userR = rr.users().list();
		if (userR == null || userR.isEmpty()) {
			return;
		}

		// for users in realm, assign roles for those specified
		for (UserRepresentation userRep : userR) {
			Optional<UserRoles> ur = userRoles.stream()
					.filter(uroles -> uroles.getUser_().equalsIgnoreCase(userRep.getEmail())).findFirst();

			if (ur.isPresent()) {
				List<String> roles = Arrays.asList(ur.get().getAssignedRoles());
				RoleScopeResource rolesRes = rr.users().get(userRep.getId()).roles().clientLevel(r.get(0).getId());
				List<RoleRepresentation> availableRoles = rolesRes.listAvailable();
				List<RoleRepresentation> effectiveRoles = rolesRes.listEffective();
				List<RoleRepresentation> toRemove = effectiveRoles.stream().filter(er -> !roles.contains(er.getName()))
						.collect(Collectors.toList());
				UserResource userResource = rr.users().get(userRep.getId());

				if (!toRemove.isEmpty()) {
					userResource.roles().clientLevel(r.get(0).getId()).remove(toRemove);

					logger.info("removed roles from user: " + toRemove);
				}

				// add new
				List<RoleRepresentation> toAdd = availableRoles.stream().filter(er -> roles.contains(er.getName()))
						.collect(Collectors.toList());

				if (!toAdd.isEmpty()) {
					userResource.roles().clientLevel(r.get(0).getId()).add(toAdd);

					logger.info("added roles to user: " + toAdd);
				}
			}
		}
	}

	@Override
	public void setApplicationRolesForUser(final String realm, final String user,
			final List<ApplicationRoles> appRoles) {
		RealmResource rr = this.getRealm(realm);
		if (rr == null) {
			return; // TODO throw error
		}

		List<ClientRepresentation> clientsRep = rr.clients().findAll();
		List<UserRepresentation> userR = rr.users().list();
		Optional<UserRepresentation> ur = userR.stream().filter(u -> user.equalsIgnoreCase(u.getEmail())).findFirst();

		if (ur.isPresent()) {
			UserResource userResource = rr.users().get(ur.get().getId());

			for (ApplicationRoles appRole : appRoles) {
				Optional<ClientRepresentation> clientRep = clientsRep.stream()
						.filter(cr -> cr.getClientId().equals(appRole.getApplicationId())).findFirst();

				if (clientRep.isPresent()) {
					List<String> roles = appRole.getAssignedRoles();
					RoleScopeResource rolesRes = rr.users().get(ur.get().getId()).roles()
							.clientLevel(clientRep.get().getId());
					List<RoleRepresentation> availableRoles = rolesRes.listAvailable();
					List<RoleRepresentation> effectiveRoles = rolesRes.listEffective(); // TODO probably we should use
																						// assigned. method??
					List<RoleRepresentation> toRemove = effectiveRoles.stream()
							.filter(er -> !roles.contains(er.getName())).collect(Collectors.toList());

					if (!toRemove.isEmpty()) {
						userResource.roles().clientLevel(clientRep.get().getId()).remove(toRemove);

						logger.info("removed roles from user: " + toRemove);
					}

					// add new
					List<RoleRepresentation> toAdd = availableRoles.stream().filter(er -> roles.contains(er.getName()))
							.collect(Collectors.toList());

					if (!toAdd.isEmpty()) {
						userResource.roles().clientLevel(clientRep.get().getId()).add(toAdd);

						logger.info("added roles to user: " + toAdd);
					}
				}
			}
		}
	}
}

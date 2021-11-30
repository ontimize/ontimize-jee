package com.ontimize.jee.server.security.keycloak;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import org.springframework.beans.factory.annotation.Value;
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
	private IOntimizeKeycloakConfiguration config;

	@Value("${ontimize.security.keycloak.admin.realm}")
	private String adminRealm;

	@Value("${ontimize.security.keycloak.admin.resource}")
	private String adminResource;

	@Value("${ontimize.security.keycloak.admin.username}")
	private String adminUserName;

	@Value("${ontimize.security.keycloak.admin.password}")
	private String adminPassword;

	@Value("${ontimize.security.keycloak.realm-defaults.redirect-urls:}")
	private String[] redirectUrls;

	private static final Logger logger = LoggerFactory.getLogger(UserManagementKeycloakImpl.class);
	private static final ResteasyClient CONNECTION_POOL = new ResteasyClientBuilder().connectionPoolSize(10).build();
	private static final String DEFAULT_LANGUAGE = "en";
	private static final String TENANTS_KEY = "tenants";

	protected Keycloak getInstance() {
		logger.debug("Auth at keycloak for rest admin api Server URL: {} Username: {} ClientId: {}",
				this.config.getAuthServerUrl(), this.adminUserName, this.adminResource);

		return KeycloakBuilder.builder().serverUrl(this.config.getAuthServerUrl()).realm(this.adminRealm)
				.username(this.adminUserName).password(this.adminPassword)
				.clientId(this.adminResource).resteasyClient(CONNECTION_POOL).build();
	}

	/**
	 * Please see further details at UserManagementInterface
	 *
	 * @param realm realm name
	 * @return list of UserRepresentation instances
	 */
	@Override
	public List<UserRepresentation> getUsers(final String realm) {
		return this.getInstance().realm(realm).users().list();
	}

	@Override
	public List<RealmInfo> getRealmsForUser(final String username) {
		final Keycloak keycloak = this.getInstance();
		final List<RealmRepresentation> rreps = keycloak.realms().findAll();
		final List<RealmInfo> result = new ArrayList<RealmInfo>();

		for (RealmRepresentation r : rreps) {
			if (!r.getRealm().equalsIgnoreCase(adminRealm)) {
				final List<UserRepresentation> users = keycloak.realm(r.getRealm()).users().list();

				for (UserRepresentation ur : users) {
					if (ur.getUsername().equalsIgnoreCase(username)) {
						final RealmInfo ti = new RealmInfo();

						ti.setId(r.getRealm());
						ti.setName(r.getDisplayName());

						result.add(ti);

						break;
					}
				}
			}
		}

		return result;
	}

	@Override
	public RealmResource createRealmIfNotExists(final String realm, final String displayName, final String... clients) {
		final Keycloak keycloak = this.getInstance();

		RealmResource rr = this.searchRealm(realm, keycloak);
		if (rr != null) {
			return rr;
		}

		final RealmRepresentation rrep = new RealmRepresentation();

		rrep.setRealm(realm);
		rrep.setEnabled(true);
		rrep.setDisplayName(displayName);

		keycloak.realms().create(rrep);

		rr = keycloak.realm(realm);

		ClientRepresentation cr = new ClientRepresentation();
		cr.setName(realm);
		cr.setClientId(realm);
		cr.setEnabled(true);
		cr.setPublicClient(true);

		try (Response resp = rr.clients().create(cr)) {
			if (resp.getStatus() != 201) {
				throw new RuntimeException("error creating realm client");
			}

			for (String client : clients) {
				rr = keycloak.realm(realm);

				cr = new ClientRepresentation();

				cr.setName(client);
				cr.setClientId(client);
				cr.setPublicClient(true);
				cr.setEnabled(true);

				List<String> redirectUris = new ArrayList<String>(Arrays.asList(clients));

				redirectUris.addAll(Arrays.asList(this.redirectUrls));

				cr.setRedirectUris(redirectUris);
				cr.setDirectAccessGrantsEnabled(true);
				cr.setStandardFlowEnabled(true);

				try (Response resp2 = rr.clients().create(cr)) {
					if (resp2.getStatus() != 201) {
						throw new RuntimeException("error creating realm client");
					}

					logger.debug("Create realm client response status: " + resp.getStatusInfo());
				}
			}
		}

		return rr;
	}

	@Override
	public void addClientToRealm(final String realm, final String client, final String... redirectUrls) {
		final RealmResource rr = this.searchRealm(realm);

		if (rr == null) {
			throw new RuntimeException("realm not found");
		}

		final ClientRepresentation cr = new ClientRepresentation();

		cr.setName(client);
		cr.setClientId(client);
		cr.setPublicClient(true);
		cr.setEnabled(true);

		final List<String> redirectUris = new ArrayList<String>(Arrays.asList(redirectUrls));

		redirectUris.addAll(Arrays.asList(this.redirectUrls));

		cr.setRedirectUris(redirectUris);
		cr.setDirectAccessGrantsEnabled(true);
		cr.setStandardFlowEnabled(true);

		rr.clients().create(cr);
	}

	@Override
	public GroupRepresentation getGroup(final String groupName) {
		final List<GroupRepresentation> groupList = this.getRealmResource().groups().groups();

		Optional<GroupRepresentation> group = groupList.stream().filter(g -> groupName.equals(g.getName())).findAny();

		if (!group.isPresent()) {
			return null;
		}

		return group.get();
	}

	@Override
	public void addUserToGroup(final String userId, final String groupId) {
		this.getUserResource(userId).joinGroup(groupId);
	}

	@Override
	public void addRealmToUser(final String username, final String realm) {
		final RealmResource rr = this.getRealmResource();

		logger.debug("Keycloak search for username: {}", username);

		// this should return just one result in the list
		List<UserRepresentation> userList = rr.users().search(username);

		if (userList != null && !userList.isEmpty()) {
			logger.debug("Keycloak user search result count: {}", userList.size());

			UserRepresentation userRep = userList.get(0);

			logger.info("Get UserResource with user account id {}", userRep.getId());

			// Get the user resource through the user id
			UserResource userRes = rr.users().get(userRep.getId());
			Map<String, List<String>> attributes = userRep.getAttributes();

			if (attributes == null) {
				userRep.singleAttribute(TENANTS_KEY, realm);
			} else if (attributes.containsKey(TENANTS_KEY)) {
				List<String> attr = attributes.get(TENANTS_KEY);
				attr.add(realm);
				userRes.update(userRep);
			} else {
				attributes.put(TENANTS_KEY, Collections.singletonList(realm));
				userRes.update(userRep);
			}
		}
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
		final List<UserRepresentation> userList = this.getRealmResource().users().search(username);

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
	public List<GroupRepresentation> getUserGroups(final String userId) {
		logger.debug("Keycloak get user groups for user id: {}", userId);

		// this should return just one result in the list
		final List<GroupRepresentation> groupRepresentationList = this.getUserResource(userId).groups();

		logger.debug("Keycloak user groups count: {}", groupRepresentationList.size());

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
		return this.getUserResource(userId).toRepresentation();
	}

	@Override
	public HttpStatus createGroup(final String groupName) {
		final GroupRepresentation groupRepresentation = new GroupRepresentation();

		groupRepresentation.setName(groupName);

		final GroupsResource groupsResource = this.getRealmResource().groups();

		groupsResource.add(groupRepresentation);

		return HttpStatus.CREATED;
	}

	/**
	 * Please see further details at UserManagementInterface
	 */
	@Override
	public HttpStatus createUserAccount(final String realm, final String username, final String firstName,
			final String lastName, final String email, final String password) {

		final CredentialRepresentation credential = new CredentialRepresentation();

		credential.setType(CredentialRepresentation.PASSWORD);
		// credential.setTemporary(true); // TODO
		credential.setValue(password);

		final UserRepresentation user = new UserRepresentation();

		user.setUsername(username);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setEmail(email);
		user.setEnabled(Boolean.TRUE);
		// user.setEmailVerified(Boolean.TRUE);
		user.setCredentials(Collections.singletonList(credential));
		user.setRequiredActions(Collections.singletonList("UPDATE_PASSWORD"));

		logger.debug("Create new User Account in Keycloak: {}", user.getUsername());

		final RealmResource keycloakCorporateResource = this.getInstance().realm(realm);// this.config.getRealm());
		final UsersResource keycloakCorporateUserResource = keycloakCorporateResource.users();

		try (Response response = keycloakCorporateUserResource.create(user)) {
			final HttpStatus status = HttpStatus.valueOf(response.getStatus());

			logger.debug("Keycloak Create User Account Response Status: {}", response.getStatusInfo());

			final List<UserRepresentation> userList = keycloakCorporateUserResource.search(user.getUsername());

			if (userList.isEmpty()) {
				logger.error("Creation of new Keycloak user account was not successful: Username: {}, Status: {}",
						username, response.getStatus());

				return status;
			}

			final UserRepresentation createdUser = userList.get(0);
			final UserResource keycloakUserResource = keycloakCorporateResource.users().get(createdUser.getId());

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

		final RealmResource rr = this.getRealmResource();

		// this should return just one result in the list
		List<UserRepresentation> userList = rr.users().search(username);

		if (userList != null && !userList.isEmpty()) {
			logger.debug("Keycloak user search result count: {}", userList.size());

			final UserRepresentation userRep = userList.get(0);

			logger.info("Get UserResource with user account id {}", userRep.getId());

			// Get the user resource through the user id
			final UserResource userRes = rr.users().get(userRep.getId());

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

		final CredentialRepresentation credential = new CredentialRepresentation();
		// VERY IMPORTANT!!! DO NOT REMOVE!!!
		credential.setTemporary(false);
		credential.setType(CredentialRepresentation.PASSWORD);
		credential.setValue(password);

		try {
			this.getUserResource(userRepresentation.getId()).resetPassword(credential);
		} catch (ClientErrorException ex) {
			logger.error("Keycloak - Reset password failed with client request error.", ex);

			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return status;
	}

	@Override
	public void updateEmailUserAccount(final String userId, final String email) {
		try {
			final UserResource user = this.getUserResource(userId);
			final UserRepresentation userRepresentation = user.toRepresentation();

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
			final UserResource user = this.getUserResource(userId);
			final UserRepresentation userRepresentation = user.toRepresentation();

			userRepresentation.setFirstName(name);

			user.update(userRepresentation);
		} catch (ClientErrorException ex) {
			logger.error("Keycloak - updateNameUserAccount error", ex);
		}
	}

	@Override
	public void updateSurnameUserAccount(final String userId, final String surname) {
		try {
			final UserResource user = this.getUserResource(userId);
			final UserRepresentation userRepresentation = user.toRepresentation();

			userRepresentation.setLastName(surname);

			user.update(userRepresentation);
		} catch (ClientErrorException ex) {
			logger.error("Keycloak - updateSurnameUserAccount error", ex);
		}
	}

	@Override
	public void disableUserAccount(final String userId) {
		try {
			final UserResource user = this.getUserResource(userId);
			final UserRepresentation userRepresentation = user.toRepresentation();

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
		RealmResource rr = this.searchRealm(realm);

		if (rr == null) {
			throw new RuntimeException("realm not found");
		}

		ClientResource cr = this.searchClient(client, rr);

		if (cr == null) {
			throw new RuntimeException("client not found");
		}

		for (RoleInfo role : roles) {
			final RoleRepresentation rRep = new RoleRepresentation();

			rRep.setName(role.getName());
			rRep.setClientRole(true);
			rRep.setDescription(role.getDescription());

			cr.roles().create(rRep);
		}
	}

	@Override
	public List<String> getUsersForRealm(final String realm) {
		final RealmResource rr = this.searchRealm(realm);

		if (rr == null) {
			return new ArrayList<String>(); // TODO throw error
		} else {
			return rr.users().list().stream().map(UserRepresentation::getEmail).collect(Collectors.toList());
		}
	}

	@Override
	public List<String> getRolesForUser(final String user, final String realm, final String client) {
		final RealmResource rr = this.searchRealm(realm);
		if (rr == null) {
			return new ArrayList<String>(); // TODO throw error
		}

		final List<UserRepresentation> userR = rr.users().list().stream()
				.filter((u) -> u.getEmail().equalsIgnoreCase(user)).collect(Collectors.toList());
		if (userR.isEmpty()) {
			return new ArrayList<String>();
		}

		final List<ClientRepresentation> clientR = rr.clients().findAll().stream()
				.filter(cr -> cr.getName().equalsIgnoreCase(client)).collect(Collectors.toList());
		if (clientR.isEmpty()) {
			return new ArrayList<String>();
		}

		final List<RoleRepresentation> rolesRep = rr.users().get(userR.get(0).getId()).roles()
				.clientLevel(clientR.get(0).getId()).listEffective();
		if (rolesRep == null || rolesRep.isEmpty()) {
			return new ArrayList<String>();
		}

		return rolesRep.stream().map(RoleRepresentation::getName).collect(Collectors.toList());
	}

	@Override
	public List<RoleInfo> getRolesForClient(final String realm, final String client) {
		final RealmResource rr = this.searchRealm(realm);

		if (rr == null) {
			return new ArrayList<RoleInfo>(); // TODO throw error
		}

		final ClientResource cr = this.searchClient(client, rr);

		if (cr == null) {
			return new ArrayList<RoleInfo>();
		}

		return cr.roles().list().stream().map(r -> {
			RoleInfo ri = new RoleInfo();
			ri.setName(r.getName());
			ri.setDescription(r.getDescription());
			return ri;
		}).collect(Collectors.toList());
	}

	@Override
	public void setRolesForUser(final String realm, final String client, final List<UserRoles> userRoles) {
		final RealmResource rr = this.searchRealm(realm);
		if (rr == null) {
			return; // TODO throw error
		}

		final ClientResource cr = this.searchClient(client, rr);
		if (cr == null) {
			return;
		}

		final List<ClientRepresentation> r = rr.clients().findAll().stream()
				.filter(clientRep -> clientRep.getName().equalsIgnoreCase(client)).collect(Collectors.toList());
		if (r == null || r.isEmpty()) {
			return;
		}

		final List<UserRepresentation> userR = rr.users().list();
		if (userR == null || userR.isEmpty()) {
			return;
		}

		// for users in realm, assign roles for those specified
		for (UserRepresentation userRep : userR) {
			final Optional<UserRoles> ur = userRoles.stream()
					.filter(uroles -> uroles.getUser_().equalsIgnoreCase(userRep.getEmail())).findFirst();

			if (ur.isPresent()) {
				final List<String> roles = Arrays.asList(ur.get().getAssignedRoles());
				final RoleScopeResource rolesRes = rr.users().get(userRep.getId()).roles()
						.clientLevel(r.get(0).getId());
				final List<RoleRepresentation> availableRoles = rolesRes.listAvailable();
				final List<RoleRepresentation> effectiveRoles = rolesRes.listEffective();
				final List<RoleRepresentation> toRemove = effectiveRoles.stream()
						.filter(er -> !roles.contains(er.getName())).collect(Collectors.toList());
				final UserResource userResource = rr.users().get(userRep.getId());

				if (!toRemove.isEmpty()) {
					userResource.roles().clientLevel(r.get(0).getId()).remove(toRemove);

					logger.info("removed roles from user: " + toRemove);
				}

				// add new
				final List<RoleRepresentation> toAdd = availableRoles.stream()
						.filter(er -> roles.contains(er.getName())).collect(Collectors.toList());

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
		final RealmResource rr = this.searchRealm(realm);
		if (rr == null) {
			return; // TODO throw error
		}

		final List<ClientRepresentation> clientsRep = rr.clients().findAll();
		final List<UserRepresentation> userR = rr.users().list();
		final Optional<UserRepresentation> ur = userR.stream().filter(u -> user.equalsIgnoreCase(u.getEmail()))
				.findFirst();

		if (ur.isPresent()) {
			final UserResource userResource = rr.users().get(ur.get().getId());

			for (ApplicationRoles appRole : appRoles) {
				final Optional<ClientRepresentation> clientRep = clientsRep.stream()
						.filter(cr -> cr.getClientId().equals(appRole.getApplicationId())).findFirst();

				if (clientRep.isPresent()) {
					final List<String> roles = appRole.getAssignedRoles();
					final RoleScopeResource rolesRes = rr.users().get(ur.get().getId()).roles()
							.clientLevel(clientRep.get().getId());
					final List<RoleRepresentation> availableRoles = rolesRes.listAvailable();
					final List<RoleRepresentation> effectiveRoles = rolesRes.listEffective(); // TODO probably we should
																								// use
					// assigned. method??
					final List<RoleRepresentation> toRemove = effectiveRoles.stream()
							.filter(er -> !roles.contains(er.getName())).collect(Collectors.toList());

					if (!toRemove.isEmpty()) {
						userResource.roles().clientLevel(clientRep.get().getId()).remove(toRemove);

						logger.info("removed roles from user: " + toRemove);
					}

					// add new
					final List<RoleRepresentation> toAdd = availableRoles.stream()
							.filter(er -> roles.contains(er.getName())).collect(Collectors.toList());

					if (!toAdd.isEmpty()) {
						userResource.roles().clientLevel(clientRep.get().getId()).add(toAdd);

						logger.info("added roles to user: " + toAdd);
					}
				}
			}
		}
	}

	private ClientResource searchClient(String name, RealmResource realm) {
		final List<ClientRepresentation> r = realm.clients().findAll().stream()
				.filter(cr -> cr.getName().equalsIgnoreCase(name)).collect(Collectors.toList());

		if (r.isEmpty()) {
			return null;
		} else {
			return realm.clients().get(r.get(0).getId());
		}
	}

	private RealmResource searchRealm(String realm) {
		return this.searchRealm(realm, this.getInstance());
	}

	private RealmResource searchRealm(String realmName, Keycloak keycloak) {
		final List<RealmRepresentation> rreps = keycloak.realms().findAll();
		RealmResource rr = null;

		for (RealmRepresentation r : rreps) {
			if (r.getRealm().equalsIgnoreCase(realmName)) {
				rr = keycloak.realm(realmName);

				break;
			}
		}

		return rr;
	}

	private RealmResource getRealmResource() {
		return this.getInstance().realm(this.config.getRealm());
	}

	private UserResource getUserResource(final String userId) {
		return this.getRealmResource().users().get(userId);
	}
}

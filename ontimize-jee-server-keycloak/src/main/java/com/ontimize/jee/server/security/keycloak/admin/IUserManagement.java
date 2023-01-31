package com.ontimize.jee.server.security.keycloak.admin;

import java.util.List;

import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;

import com.ontimize.jee.server.security.keycloak.admin.dto.RealmInfo;
import com.ontimize.jee.server.security.keycloak.admin.dto.application.ApplicationRoles;
import com.ontimize.jee.server.security.keycloak.admin.dto.application.RoleInfo;
import com.ontimize.jee.server.security.keycloak.admin.dto.application.UserRoles;

/**
 * Interface for the UserManagement of the RDU Corporate UI. The UserManagement
 * is done in a separate system, there for an interface is required.
 */

public interface IUserManagement {

	/**
	 * Search users related to a realm It
	 *
	 * @param realm realm name
	 * @return Returns a List<UserRepresentation>
	 */
	List<UserRepresentation> getUsers(final String realm);

	/**
	 * Search of an created user account. It will search with overgiven username in
	 * the related user management system.
	 *
	 * @param username user name
	 * @return Returns a List<UserRepresentation> with 1 item, if the user is found,
	 *         otherwise 0
	 */
	List<UserRepresentation> searchUserAccount(final String username);

	/**
	 * Returns all groups for the given keycloak user
	 *
	 * @param userId user id
	 * @return Returns a List<GroupRepresentation>
	 */
	List<GroupRepresentation> getUserGroups(final String userId);

	/**
	 * Returns user representation object from keycloak
	 *
	 * @param userId keycloak user id
	 * @return UserRepresentation instance
	 */
	UserRepresentation getUserRepresentation(final String userId);

	/**
	 * Creates an user account with all parameters in the user account management
	 * system. The user account needs to be activated after creation before login.
	 *
	 * @param username  user name
	 * @param firstname first name
	 * @param lastname  last name
	 * @param email     email
	 * @param password  password
	 * @return HttpStatus CREATED, if creation was successful. In all other cases
	 *         the response of the original response.
	 */
	HttpStatus createUserAccount(final String realm, final String username, final String firstName,
			final String lastName, final String email, final String password);

	/**
	 * Creates a group account with all parameters in the user account management
	 * system.
	 *
	 * @param groupName group name
	 * @return HttpStatus CREATED, if creation was successful. In all other cases
	 *         the response of the original response.
	 */
	HttpStatus createGroup(final String groupName);

	/**
	 * Get a group representation data by groupName
	 *
	 * @param groupName group name
	 * @return GroupRepresentation instance
	 */
	GroupRepresentation getGroup(final String groupName);

	/**
	 * Add a user to a groupId
	 *
	 * @param user    user
	 * @param groupId groupId
	 */
	void addUserToGroup(final String userId, final String groupId);

	/**
	 * Add a realm to a user
	 *
	 * @param user    username
	 * @param realm   realm
	 */
	void addRealmToUser(final String username, final String realm);

	/**
	 * Add a realm to a user
	 *
	 * @param user    username
	 * @param realm   realm
	 */
	void addRealmToUserAccount(final String userId, final String realm);

	/**
	 * Activation of an created user account. It will search with overgiven username
	 * in the related user management system. If it finds the user account, it will
	 * activate that account. After this step the user can login.
	 *
	 * @param username user name
	 * @return HttpStatus OK, if activation was successful, NOT_FOUND if username
	 *         was not found in the system.
	 */
	HttpStatus activateUserAccount(final String username);

	/**
	 * Reset the password of an created user account. If it finds the user account,
	 * it will reset the password. After this step the user can login with the new
	 * password.
	 *
	 * @param userRepresentation UserRepresentation instance
	 * @param password           password
	 */
	HttpStatus resetPassword(final UserRepresentation userRepresentation, final String password);

	void updateEmailUserAccount(final String userId, final String email);

	void updateNameUserAccount(final String userId, final String name);

	void updateSurnameUserAccount(final String userId, final String surname);

	void disableUserAccount(final String userId);

	public RealmResource createRealmIfNotExists(final String realmName, final String displayName, final String... clients);

	public List<RealmInfo> getRealmsForUser(final String username);

	public void addClientToRealm(final String realm, final String client, final String... redirectUrls);

	void addRolesToRealm(final String realm, final String client, final List<RoleInfo> roles);

	List<String> getUsersForRealm(final String realm);

	List<String> getRolesForUser(final String user, final String realm, final String client);

	List<RoleInfo> getRolesForClient(final String realm, final String client);

	void setRolesForUser(final String realm, final String client, final List<UserRoles> userRoles);

	void setApplicationRolesForUser(final String realm, final String user, final List<ApplicationRoles> appRoles);
}

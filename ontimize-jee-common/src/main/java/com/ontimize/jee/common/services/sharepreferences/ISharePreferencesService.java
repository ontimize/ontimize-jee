package com.ontimize.jee.common.services.sharepreferences;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import com.ontimize.jee.common.util.share.SharedElement;

public interface ISharePreferencesService {

    public static final String REMOTE_NAME = "ShareOperations";

    public static final String SHARE_ENTITY_NAME_STRING = "ShareEntity";

    public static final String SHARE_TARGET_ENTITY_STRING = "ShareTargetEntity";

    public static final String SHARE_KEY_STRING = "ShareKey";

    public static final String SHARE_TARGET_KEY_STRING = "ShareTargetKey";

    public static final String SHARE_USER_SOURCE_STRING = "SourceUser";

    public static final String SHARE_TYPE_STRING = "ShareType";

    public static final String SHARE_CONTENT_SHARE_STRING = "ContentShare";

    public static final String SHARE_MESSAGE_STRING = "Message";

    public static final String SHARE_NAME_STRING = "Name";

    public static final String SHARE_TARGET_USERS_LIST = "TargetUsersList";

    public static final String SHARE_USER_TARGET_STRING = "TargetUsers";

    public static final String SHARE_KEY_SUFFIX = "shareKey";

    /**
     * Return a {@link List} of {@linkplain String} with all of the existing users in the application.
     * This list is necessary to fill the list of users when an element is shared or updated in
     * {@link FormAddUserSharedReference} or {@link FormUpdateSharedReference} .
     * @param sessionId
     * @return List<String> with all user list
     * @throws OntimizeJEEException
     */
    public List<String> getUserList() throws OntimizeJEEException;

    /* ============== With connection ============== */
    /**
     * Shares a {@link SharedElement} with a list of users. This {@link SharedElement} hasn't has an
     * identifier
     * @param sharedObject
     * @param targetList
     * @return An {@link EntityResult} with the result of the operation
     * @throws OntimizeJEEException
     */
    public EntityResult addSharedItem(SharedElement sharedObject, List<String> targetList) throws OntimizeJEEException;

    /**
     * Edit the receivers of the shared element. Receive as parameter the list of users, and the
     * identifier of a shared element stored in the database. Check the relation between receivers in
     * database and compare it with the list passed as parameter. If the user exist in database and in
     * the list, do nothing, if the user exist in database and not in the list, delete the relation
     * between this receiver and this shared element, and if the user don't exist in the database but
     * exist in the list, adds a new relation between the user and the shared element
     * @param idShare
     * @param targetList
     * @return An {@link EntityResult} with the result of the operation
     * @throws OntimizeJEEException
     */
    public EntityResult editTargetSharedElement(int idShare, List<String> targetList)
            throws OntimizeJEEException;

    /**
     * Retrieve a list of all {@link SharedElement} shared with the user. This method calls to
     * {@link #getSharedItemsWithUserAndKey(String, String, int, Connection)} passing in parameter
     * "shareKey" a null value
     * @param username
     * @return A {@link List} of {@link SharedElement}
     * @throws OntimizeJEEException
     */
    public List<SharedElement> getSharedItemsWithUser(String username) throws OntimizeJEEException;

    /**
     * Retrieve a list of all {@link SharedElement} shared with the user with a specific key. This
     * method performs a query to the table which stores the relation between the receivers and the
     * shared element, obtaining the identifiers of shared elements shared with the specific users. With
     * this list of identifiers, performs a query to the shared element table, checking which shared
     * elements has the requested share key. If the share key is <code>null</code> retrieve all elements
     * shared with the requested user, independently which share key has it
     * @param username
     * @param shareKey
     * @return A {@link List} of {@link SharedElement}
     * @throws OntimizeJEEException
     */
    public List<SharedElement> getSharedItemsWithUserAndKey(String username, String shareKey)
            throws OntimizeJEEException;

    /**
     * Return a {@link List} of {@link HashMap} with information related to the shared elements
     * ({@link SharedElement#idShare}, {@link SharedElement#name}) and an identifier of the specific
     * relation between the receiver and the shared element. This method performs a query to the table
     * which stores the relation between the receivers and the shared element, obtaining the identifier
     * for this relation. Then, check the shared element belonging to relations obtained previously, and
     * performs a query to obtain the element with the requested <code>share key</code>. Puts in the
     * HashMap to return the identifier of relation, the identifier of shared element and the name of
     * shared element.
     * @param username
     * @param shareKey
     * @return A {@link List} of {@link HashMap} with information related
     * @throws OntimizeJEEException
     */
    public List<HashMap<String, Object>> getTargetSharedElementMenuList(String username, String shareKey)
            throws OntimizeJEEException;

    /**
     * Returns a {@link List}<{@link String}> of the actual receivers of the shared element. This method
     * performs a query to the table which stores the relations between shared element and user which
     * has the requested identifier of the shared element, and add to the list to return the name of the
     * users in the relation
     * @param idShare
     * @return A {@link List} of {@link String} with the receivers of the shared element
     * @throws OntimizeJEEException
     */
    public List<String> getTargetSharedItemsList(int idShare) throws OntimizeJEEException;

    /**
     * Returns a {@link List} of {@link SharedElement} which shares the specific user name with a
     * specific key. This method perform a query to the table which stores the shared elements to
     * retrieve a list of which ones that has the specific user owner and share key.
     * @param username
     * @param shareKey
     * @return A {@link List} of {@link SharedElement}
     * @throws OntimizeJEEException
     */
    public List<SharedElement> getSourceSharedItemsList(String username, String shareKey)
            throws OntimizeJEEException;

    /**
     *
     * Return a {@link List} of {@link HashMap} with information related to the shared elements
     * ({@link SharedElement#idShare}, {@link SharedElement#name}), for any shared element with specific
     * share key and the specific user name as share owner. This method performs a query to the entity
     * which stores the shared data and retrieve a HashMap with the identifier of shared element and its
     * name for which ones that has the specific user name as owner and the specific share key
     * @param username
     * @param shareKey
     * @return A {@link List} of {@link HashMap}
     * @throws OntimizeJEEException
     */
    public List<HashMap<String, Object>> getSourceSharedElementMenuList(String username, String shareKey)
            throws OntimizeJEEException;

    /**
     * Return the message associated with this shared item. This method performs a query to the shared
     * element which the specific identifier passed as parameter and return its message
     * @param idShare
     * @return A {@link String} message
     * @throws OntimizeJEEException
     */
    public String getSharedElementMessage(int idShare) throws OntimizeJEEException;

    /**
     * Return the content of the shared element. This method performs a query to the shared element
     * which the specific identifier passed as parameter and return its message
     * @param idShare
     * @return The content of the share element as {@link Object}
     * @throws OntimizeJEEException
     */
    public Object getSharedElementValue(int idShare) throws OntimizeJEEException;

    /**
     * Return the number of elements with the specified name. This method performs a query to the entity
     * which store the shared element and search for how many elements has the same name that the name
     * passed as parameter.
     * @param shareName
     * @return Number of shared elements
     * @throws OntimizeJEEException
     */
    public int countSharedItemByNameAndUser(String shareName) throws OntimizeJEEException;

    /**
     * Return the {@link SharedElement} specified by the identifier. This method performs a query to the
     * table which stores the shared elements to return which one has the same identifier that the
     * passed as parameter.
     * @param idShare
     * @return A {@link SharedElement}
     * @throws OntimizeJEEException
     */
    public SharedElement getSharedItem(int idShare) throws OntimizeJEEException;

    /**
     *
     * Return the {@link SharedElement} associated with a receiver identifier. This method performs a
     * query to table which stores the relation between users and shared that has the specific
     * identifier passed as parameter. Then return the shared element associated to this relation.
     * @param idShareTarget
     * @return A {@link SharedElement}
     * @throws OntimizeJEEException
     */
    public SharedElement getTargetSharedItem(int idShareTarget) throws OntimizeJEEException;

    /**
     * Delete the shared element from the database and remove all receivers for this element. This
     * method perform a query to the table which store the relations between the receivers and the
     * shared element, checking for relations which shared element is the same that the identifier
     * passed as parameter. Delete all relations related and for last, delete from the stored share
     * element which one has the same identifier as the passed as parameter. The element only be removed
     * for the same user that shared the element, obtainer from the sessionId
     * @param idShare
     * @return An {@link EntityResult} with the result of the operation
     * @throws OntimizeJEEException
     */
    public EntityResult deleteSharedItem(int idShare) throws OntimizeJEEException;

    /**
     * Delete the association between an user and a shared element. This method perform a query to
     * obtain the shared element from the relation target. Then check the user name of the user who
     * shares the element, and only delete the relation if the relation user receiver is the same as the
     * user who has the ssesionId or the user that shares the element
     * @param idShareTarget
     * @return An {@link EntityResult} with the result of the operation
     * @throws OntimizeJEEException
     */
    public EntityResult deleteTargetSharedItem(int idShareTarget) throws OntimizeJEEException;

    /**
     * Update the shared element specified with id share with the content sent in variables content,
     * message, and name. This method performs a query to update the shared element with the identifier
     * passed as parameter. If the user was the same that shares the element, performs an update of the
     * shared element with the data passed as parameter
     * @param idShare
     * @param content
     * @param message
     * @param name
     * @return An {@link EntityResult} with the result of the operation
     * @throws OntimizeJEEException
     */
    public EntityResult updateSharedItem(int idShare, String content, String message, String name)
            throws OntimizeJEEException;

}

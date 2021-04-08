package com.ontimize.jee.server.services.sharepreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Map;
import java.util.List;
import java.util.Vector;

import com.ontimize.dto.EntityResultMapImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ontimize.dto.EntityResult;
import com.ontimize.db.SQLStatementBuilder;
import com.ontimize.db.SQLStatementBuilder.BasicExpression;
import com.ontimize.db.SQLStatementBuilder.BasicField;
import com.ontimize.db.SQLStatementBuilder.BasicOperator;
import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import com.ontimize.jee.common.services.sharepreferences.ISharePreferencesService;
import com.ontimize.jee.common.services.user.UserInformation;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;
import com.ontimize.jee.server.dao.IOntimizeDaoSupport;
import com.ontimize.jee.server.security.ISecurityUserInformationService;
import com.ontimize.util.share.IShareRemoteReference;
import com.ontimize.util.share.SharedElement;

public class SharePreferencesEngine implements ISharePreferencesService, InitializingBean {

    private String shareKeyColumn = "";

    private String shareTargetKeyColumn = "";

    private String shareUserColumn = "";

    private String shareTargetUserColumn = "";

    private String shareTypeColumn = "";

    private String shareContentColumn = "";

    private String shareMessageColumn = "";

    private String shareNameColumn = "";

    private IOntimizeDaoSupport daoSharePref;

    private IOntimizeDaoSupport daoSharePrefTarget;

    @Autowired
    private ISecurityUserInformationService userService;

    @Autowired
    private DefaultOntimizeDaoHelper daoHelper;

    /**
     * Gets the shared preferences key column
     * @return {@link String} the shared preferences key column
     */
    public String getShareKeyColumn() {
        return this.shareKeyColumn;
    }

    /**
     * Sets the shared preferences key column.
     * @param shareKeyColumn {@link String} the shared preferences key column.
     */
    public void setShareKeyColumn(String shareKeyColumn) {
        this.shareKeyColumn = shareKeyColumn;
    }

    /**
     * Gets the shared preferences target key column
     * @return {@link String} the shared preference target key column
     */
    public String getShareTargetKeyColumn() {
        return this.shareTargetKeyColumn;
    }

    /**
     * Sets the shared preference target key column
     * @param shareTargetKeyColumn {@link String} the shared preference target key column
     */
    public void setShareTargetKeyColumn(String shareTargetKeyColumn) {
        this.shareTargetKeyColumn = shareTargetKeyColumn;
    }

    /**
     * Gets the shared preferences user column
     * @return {@link String} the shared preferences user column
     */
    public String getShareUserColumn() {
        return this.shareUserColumn;
    }

    /**
     * Sets the shared preferences user column
     * @param shareUserColumn {@link String} the shared preferences user column
     */
    public void setShareUserColumn(String shareUserColumn) {
        this.shareUserColumn = shareUserColumn;
    }

    /**
     * Gets the shared preferences user column
     * @return {@link String} the shared preferences user column
     */
    public String getShareTargetUserColumn() {
        return this.shareTargetUserColumn;
    }

    /**
     * Sets the shared preferences user column
     * @param shareTargetUserColumn {@link String} the shared preferences user column
     */
    public void setShareTargetUserColumn(String shareTargetUserColumn) {
        this.shareTargetUserColumn = shareTargetUserColumn;
    }

    /**
     * Gets the shared preferences type column
     * @return {@link String} the shared preferences type column
     */
    public String getShareTypeColumn() {
        return this.shareTypeColumn;
    }

    /**
     * Sets the shared preferences type column
     * @param shareTypeColumn {@link String} the shared preferences type column
     */
    public void setShareTypeColumn(String shareTypeColumn) {
        this.shareTypeColumn = shareTypeColumn;
    }

    /**
     * Gets the shared preferences content column
     * @return {@link String} the shared preferences content column
     */
    public String getShareContentColumn() {
        return this.shareContentColumn;
    }

    /**
     * Sets the shared preferences content column
     * @param shareContentColumn {@link String} the shared preferences content column
     */
    public void setShareContentColumn(String shareContentColumn) {
        this.shareContentColumn = shareContentColumn;
    }

    /**
     * Gets the shared preferences message column
     * @return {@link String} the shared preferences message column
     */
    public String getShareMessageColumn() {
        return this.shareMessageColumn;
    }

    /**
     * Sets the shared preferences message column
     * @param shareMessageColumn {@link String} the shared preferences message column
     */
    public void setShareMessageColumn(String shareMessageColumn) {
        this.shareMessageColumn = shareMessageColumn;
    }

    /**
     * Gets the share preference name column
     * @return {@link String} the share preference name column
     */
    public String getShareNameColumn() {
        return this.shareNameColumn;
    }

    /**
     * Sets the share preference name column
     * @param shareNameColumn {@link String} the share preference name column
     */
    public void setShareNameColumn(String shareNameColumn) {
        this.shareNameColumn = shareNameColumn;
    }

    /**
     * Gets the DAO of the shared preferences
     * @return the DAO of the shared preferences
     */
    public IOntimizeDaoSupport getDaoSharePref() {
        return this.daoSharePref;
    }

    /**
     * Sets the DAO of the shared preferences
     * @param daoSharePref {@link IOntimizeDaoSupport} The DAO for shared preferences
     */
    public void setDaoSharePref(IOntimizeDaoSupport daoSharePref) {
        this.daoSharePref = daoSharePref;
    }

    /**
     * Gets the DAO of the shared preference targets
     * @return the DAO of the shared preference targets
     */
    public IOntimizeDaoSupport getDaoSharePrefTarget() {
        return this.daoSharePrefTarget;
    }

    /**
     * Sets the DAO of the shared preference targets
     * @param daoSharePref {@link IOntimizeDaoSupport} The DAO for shared preference targets
     */
    public void setDaoSharePrefTarget(IOntimizeDaoSupport daoSharePrefTarget) {
        this.daoSharePrefTarget = daoSharePrefTarget;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        CheckingTools.failIfEmptyString(this.shareKeyColumn,
                "The identifier column does not exist in the database for the share preferences table");
        CheckingTools.failIfEmptyString(this.shareTargetKeyColumn,
                "The target column key does not exist in the database for the share preferences target table");
        CheckingTools.failIfEmptyString(this.shareUserColumn,
                "The user column key does not exist in the database for the share preferences table");
        CheckingTools.failIfEmptyString(this.shareTargetUserColumn,
                "The target user column key does not exist in the database for the share target preferences table");
        CheckingTools.failIfEmptyString(this.shareTypeColumn,
                "The type column key does not exist in the database for the share preferences table");
        CheckingTools.failIfEmptyString(this.shareContentColumn,
                "The content column key does not exist in the database for the share preferences table");
        CheckingTools.failIfEmptyString(this.shareMessageColumn,
                "The message column key does not exist in the database for the share preferences table");
        CheckingTools.failIfEmptyString(this.shareNameColumn,
                "The name column key does not exist in the database for the share preferences table");
        CheckingTools.failIfNull(this.daoSharePref, "There is no class defined for the share preferences DAO");
        CheckingTools.failIfNull(this.daoSharePref, "There is no class defined for the share preferences target DAO");

    }

    @Override
    public List<String> getUserList() throws OntimizeJEEException {
        UserInformation principal = (UserInformation) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();
        List<String> userlist = this.userService.getAllUserInformationLogin();
        userlist.remove(principal.getLogin());
        return userlist;
    }

    @Override
    public EntityResult addSharedItem(SharedElement sharedObject, List<String> targetList) throws OntimizeJEEException {
        Map<String, Object> shareAttributes = new HashMap<>();
        shareAttributes.put(this.getShareUserColumn(), sharedObject.getUserSource());
        shareAttributes.put(this.getShareContentColumn(), sharedObject.getContentShare());
        shareAttributes.put(this.getShareTypeColumn(), sharedObject.getShareType());
        shareAttributes.put(this.getShareMessageColumn(), sharedObject.getMessage());
        shareAttributes.put(this.getShareNameColumn(), sharedObject.getName());

        EntityResult eRShare = this.daoHelper.insert(this.daoSharePref, shareAttributes);
        String keyColumn = this.getShareKeyColumn();
        if ((eRShare.getCode() != EntityResult.OPERATION_WRONG) && eRShare.containsKey(keyColumn)) {
            for (Object actualTarget : targetList) {
                Map<String, Object> attributesValuesTarget = new HashMap<>();
                attributesValuesTarget.put(keyColumn, eRShare.get(keyColumn));
                attributesValuesTarget.put(this.getShareTargetUserColumn(), actualTarget);

                EntityResult eRShareTarget = this.daoHelper.insert(this.daoSharePrefTarget, attributesValuesTarget);
                if (eRShareTarget.getCode() == EntityResult.OPERATION_WRONG) {
                    throw new OntimizeJEEException(eRShareTarget.getMessage());
                }
            }
        }

        if (eRShare.getCode() == EntityResult.OPERATION_WRONG) {
            throw new OntimizeJEEException(eRShare.getMessage());
        }

        return eRShare;
    }

    @Override
    public EntityResult editTargetSharedElement(int idShare, List<String> targetList) throws OntimizeJEEException {
        EntityResult toRet = new EntityResultMapImpl();

        if (targetList != null) {
            String keyColumn = this.getShareKeyColumn();
            Map<String, Object> sharedElementKey = new HashMap<>();
            List<String> attributeIdShare = new ArrayList<>();

            sharedElementKey.put(keyColumn, idShare);
            attributeIdShare.add(keyColumn);
            attributeIdShare.add(this.getShareUserColumn());

            EntityResult shareResult = this.daoHelper.query(this.daoSharePref, sharedElementKey, attributeIdShare);

            if ((shareResult.getCode() != EntityResult.OPERATION_WRONG) && (shareResult.calculateRecordNumber() == 1)) {
                this.editTargetUsers(shareResult, sharedElementKey, targetList);
            } else {
                throw new OntimizeJEEException("shareRemote.share_element_must_exist");
            }
        }

        return toRet;
    }

    /**
     * Method used for reduce the complexity of {@link #editTargetSharedElement(int, List)}
     * @param shareResult {@link EntityResult}
     * @param sharedElementKey {@link Map}
     * @param targetList {@link List}
     * @throws OntimizeJEEException
     *
     * @see {@link #editTargetSharedElement(int, List)}
     */
    protected void editTargetUsers(EntityResult shareResult, Map<String, Object> sharedElementKey,
            List<String> targetList) throws OntimizeJEEException {

        String userSessionName = ((UserInformation) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal()).getLogin();

        if (userSessionName.equalsIgnoreCase((String) shareResult.getRecordValues(0).get(this.getShareUserColumn()))) {
            List<String> attributeTargetList = new ArrayList<>();
            attributeTargetList.add(this.getShareTargetKeyColumn());
            attributeTargetList.add(this.getShareTargetUserColumn());

            EntityResult erTarget = this.daoHelper.query(this.daoSharePrefTarget, sharedElementKey,
                    attributeTargetList);

            if (erTarget.getCode() != EntityResult.OPERATION_WRONG) {
                List<String> targetNameToDelete = new ArrayList<>();
                List<String> targetToAdd = new ArrayList<>();

                @SuppressWarnings("unchecked")
                List<String> existingTargets = ((List<String>) erTarget.get(this.getShareTargetUserColumn()) == null)
                        ? new ArrayList<String>()
                        : (List<String>) erTarget.get(this.getShareTargetUserColumn());

                targetNameToDelete.addAll(existingTargets);
                targetToAdd.addAll(targetList);

                targetNameToDelete.removeAll(targetList);
                targetToAdd.removeAll(existingTargets);

                this.deleteTargetUsers(targetNameToDelete, erTarget);
                this.addTargetUsers(targetToAdd, sharedElementKey);

            } else {
                throw new OntimizeJEEException("shareRemote.not_authorized_to_add_target");
            }

        } else {
            throw new OntimizeJEEException("shareRemote.not_authorized_to_add_target");
        }
    }

    /**
     * Method used for reduce the complexity of {@link #editTargetUsers(EntityResult, Map, List)}
     * @param targetNameToDelete {@link List}
     * @param erTarget {@link EntityResult}
     * @throws OntimizeJEEException
     *
     * @see {@link #editTargetSharedElement(int, List)}
     */
    protected void deleteTargetUsers(List<String> targetNameToDelete, EntityResult erTarget)
            throws OntimizeJEEException {
        for (String target : targetNameToDelete) {
            Map<String, String> hDeleteTargetKey = new HashMap<>();
            hDeleteTargetKey.put(this.getShareTargetUserColumn(), target);
            int i = erTarget.getRecordIndex(new HashMap<String, String>(hDeleteTargetKey));
            int idTarget = (Integer) erTarget.getRecordValues(i).get(this.getShareTargetKeyColumn());
            EntityResult res = this.deleteTargetSharedItem(idTarget);
            if (res.getCode() == EntityResult.OPERATION_WRONG) {
                throw new OntimizeJEEException("shareRemote.error_deleting_target");
            }
        }
    }

    /**
     * Method used for reduce the complexity of {@link #editTargetUsers(EntityResult, Map, List)}
     * @param targetToAdd {@link List}
     * @param sharedElementKey {@link Map}
     * @throws OntimizeJEEException
     *
     * @see {@link #editTargetSharedElement(int, List)}
     */
    protected void addTargetUsers(List<String> targetToAdd, Map<String, Object> sharedElementKey)
            throws OntimizeJEEException {

        for (String target : targetToAdd) {
            Map<String, Object> targetValues = new HashMap<>();
            targetValues.putAll(sharedElementKey);
            targetValues.put(this.getShareTargetUserColumn(), target);
            EntityResult insertTarget = this.daoHelper.insert(this.daoSharePrefTarget, targetValues);
            if (insertTarget.getCode() == EntityResult.OPERATION_WRONG) {
                throw new OntimizeJEEException("shareRemote.error_adding_target");
            }
        }
    }

    @Override
    public List<SharedElement> getSharedItemsWithUser(String username) throws OntimizeJEEException {
        return this.getSharedItemsWithUserAndKey(username, null);
    }

    @Override
    public List<SharedElement> getSharedItemsWithUserAndKey(String username, String shareKey)
            throws OntimizeJEEException {

        String keyColumn = this.getShareKeyColumn();
        String columnMessage = this.getShareMessageColumn();
        String shareType = this.getShareTypeColumn();
        String contentShare = this.getShareContentColumn();
        String userColumn = this.getShareUserColumn();
        String nameColumn = this.getShareNameColumn();
        String userTargetColumn = this.getShareTargetUserColumn();
        String userTargetKey = this.getShareTargetKeyColumn();

        List<SharedElement> sharedList = new ArrayList<>();
        List<String> attributesSharedElement = new ArrayList<>();
        attributesSharedElement.add(keyColumn);
        attributesSharedElement.add(userTargetColumn);
        attributesSharedElement.add(userTargetKey);

        Map<String, Object> keysSharedElementValues = new HashMap<>();
        keysSharedElementValues.put(this.getShareTargetUserColumn(), username);

        EntityResult eRToRetTarget = this.daoHelper.query(this.daoSharePrefTarget, keysSharedElementValues,
                attributesSharedElement);

        @SuppressWarnings("unchecked")
        List<Object> keyList = Collections.list(((Vector<Object>) eRToRetTarget.get(keyColumn)).elements());
        BasicExpression bexp = new BasicExpression(new BasicField(keyColumn), BasicOperator.IN_OP, keyList);
        Map<String, Object> keyTargetValues = new HashMap<>();
        keyTargetValues.put(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY, bexp);
        if (shareKey != null) {
            keyTargetValues.put(this.getShareTypeColumn(), shareKey);
        }

        attributesSharedElement.clear();
        attributesSharedElement.add(keyColumn);
        attributesSharedElement.add(columnMessage);
        attributesSharedElement.add(shareType);
        attributesSharedElement.add(contentShare);
        attributesSharedElement.add(userColumn);
        attributesSharedElement.add(nameColumn);
        EntityResult eRToRet = this.daoHelper.query(this.daoSharePref, keyTargetValues, attributesSharedElement);

        for (int i = 0; i < eRToRet.calculateRecordNumber(); i++) {
            @SuppressWarnings("unchecked")
            Map<String, Object> actual = new HashMap<>(eRToRet.getRecordValues(i));
            int idShare = (Integer) actual.get(keyColumn);
            String message = (String) actual.get(columnMessage);
            String shareElementKey = (String) actual.get(shareType);
            String content = (String) actual.get(contentShare);
            String source = (String) actual.get(userColumn);
            String name = (String) actual.get(nameColumn);
            SharedElement shared = new SharedElement(idShare, message, shareElementKey, content, source, name);
            sharedList.add(shared);
        }

        return sharedList;
    }

    @Override
    public List<HashMap<String, Object>> getTargetSharedElementMenuList(String username, String shareKey)
            throws OntimizeJEEException {
        List<HashMap<String, Object>> sharedList = new ArrayList<>();

        String keyColumn = this.getShareKeyColumn();
        String keyTargetColumn = this.getShareTargetKeyColumn();

        List<String> attributesSharedElementTarget = new ArrayList<>();
        attributesSharedElementTarget.add(keyColumn);
        attributesSharedElementTarget.add(keyTargetColumn);

        Map<String, Object> keysSharedElementValues = new HashMap<>();

        keysSharedElementValues.put(this.getShareTargetUserColumn(), username);

        EntityResult erTargetList = this.daoHelper.query(this.daoSharePrefTarget, keysSharedElementValues,
                attributesSharedElementTarget);
        if ((erTargetList.getCode() != EntityResult.OPERATION_WRONG) && (erTargetList.calculateRecordNumber() > 0)) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            List<String> vTargetkey = Collections.list(((Vector) erTargetList.get(keyColumn)).elements());
            BasicExpression bexp = new BasicExpression(new BasicField(keyColumn), BasicOperator.IN_OP, vTargetkey);
            List<String> attributesSharedElement = new ArrayList<>();
            attributesSharedElement.add(this.getShareKeyColumn());
            attributesSharedElement.add(this.getShareNameColumn());
            Map<String, Object> keysValues = new HashMap<>();
            keysValues.put(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY, bexp);
            keysValues.put(this.getShareTypeColumn(), shareKey);

            EntityResult toRet = this.daoHelper.query(this.daoSharePref, keysValues, attributesSharedElement);

            for (int i = 0; i < toRet.calculateRecordNumber(); i++) {
                @SuppressWarnings("unchecked")
                Map<String, Object> actual = toRet.getRecordValues(i);
                Map<String, Object> mapToRet = new HashMap<>();
                int idShare = (Integer) actual.get(keyColumn);
                mapToRet.put(IShareRemoteReference.SHARE_KEY_STRING, idShare);
                String name = (String) actual.get(this.getShareNameColumn());
                mapToRet.put(IShareRemoteReference.SHARE_NAME_STRING, name);
                sharedList.add((HashMap<String, Object>) mapToRet);
                Map<String, Object> hTargetkey = new HashMap<>();
                hTargetkey.put(keyColumn, idShare);
                @SuppressWarnings({ "rawtypes", "unchecked" })
                int j = erTargetList.getRecordIndex(new HashMap(hTargetkey));
                @SuppressWarnings("unchecked")
                Map<String, Object> erTargetValue = erTargetList.getRecordValues(j);
                mapToRet.put(IShareRemoteReference.SHARE_TARGET_KEY_STRING, erTargetValue.get(keyTargetColumn));
            }
        }

        return sharedList;
    }

    @Override
    public List<String> getTargetSharedItemsList(int idShare) throws OntimizeJEEException {
        List<String> toRet = new ArrayList<>();

        String keyColumn = this.getShareKeyColumn();
        String targetColumn = this.getShareTargetUserColumn();

        Map<String, Object> targetQueryKeys = new HashMap<>();
        List<String> attributesTarget = new ArrayList<>();

        targetQueryKeys.put(keyColumn, idShare);
        attributesTarget.add(targetColumn);

        EntityResult targetListQuery = this.daoHelper.query(this.daoSharePrefTarget, targetQueryKeys, attributesTarget);
        if (targetListQuery.getCode() != EntityResult.OPERATION_WRONG) {
            for (int i = 0; i < targetListQuery.calculateRecordNumber(); i++) {
                toRet.add((String) targetListQuery.getRecordValues(i).get(targetColumn));
            }

        }
        return toRet;
    }

    @Override
    public List<SharedElement> getSourceSharedItemsList(String username, String shareKey) throws OntimizeJEEException {
        List<SharedElement> sharedList = new ArrayList<>();

        String sourceUser = this.getShareUserColumn();
        String shareType = this.getShareTypeColumn();
        String keyColumn = this.getShareKeyColumn();

        List<String> attributteList = new ArrayList<>();
        Map<String, Object> keysValues = new HashMap<>();

        keysValues.put(shareType, shareKey);
        keysValues.put(sourceUser, username);

        EntityResult toRet = this.daoHelper.query(this.daoSharePref, keysValues, attributteList);

        for (int i = 0; i < toRet.calculateRecordNumber(); i++) {
            @SuppressWarnings("unchecked")
            Map<String, Object> actual = toRet.getRecordValues(i);
            int idShare = (Integer) actual.get(keyColumn);
            String message = (String) actual.get(this.getShareMessageColumn());
            String shareElementKey = (String) actual.get(shareType);
            String content = (String) actual.get(this.getShareContentColumn());
            String source = (String) actual.get(sourceUser);
            String name = (String) actual.get(this.getShareNameColumn());
            SharedElement shared = new SharedElement(idShare, message, shareElementKey, content, source, name);
            sharedList.add(shared);
        }
        return sharedList;
    }

    @Override
    public List<HashMap<String, Object>> getSourceSharedElementMenuList(String username, String shareKey)
            throws OntimizeJEEException {
        List<HashMap<String, Object>> sharedList = new ArrayList<>();

        String sourceUser = this.getShareUserColumn();
        String shareType = this.getShareTypeColumn();
        String keyColumn = this.getShareKeyColumn();
        String nameColumn = this.getShareNameColumn();

        List<String> attributteList = new ArrayList<>();
        attributteList.add(keyColumn);
        attributteList.add(nameColumn);
        Map<String, String> keysValues = new HashMap<>();

        keysValues.put(shareType, shareKey);
        keysValues.put(sourceUser, username);

        EntityResult toRet = this.daoHelper.query(this.daoSharePref, keysValues, attributteList);

        for (int i = 0; i < toRet.calculateRecordNumber(); i++) {
            @SuppressWarnings("unchecked")
            Map<String, Object> actual = toRet.getRecordValues(i);
            HashMap<String, Object> mapToRet = new HashMap<>();
            int idShare = (Integer) actual.get(keyColumn);
            mapToRet.put(IShareRemoteReference.SHARE_KEY_STRING, idShare);
            String name = (String) actual.get(nameColumn);
            mapToRet.put(IShareRemoteReference.SHARE_NAME_STRING, name);
            sharedList.add(mapToRet);
        }

        return sharedList;
    }

    @Override
    public String getSharedElementMessage(int idShare) throws OntimizeJEEException {
        String toRet = "";
        SharedElement shareElement = this.getSharedItem(idShare);
        if (shareElement != null) {
            toRet = shareElement.getMessage();
        }
        return toRet;
    }

    @Override
    public Object getSharedElementValue(int idShare) throws OntimizeJEEException {
        Object toRet = null;
        SharedElement shareElement = this.getSharedItem(idShare);
        if (shareElement != null) {
            toRet = shareElement.getContentShare();
        }
        return toRet;
    }

    @Override
    public int countSharedItemByNameAndUser(String shareName) throws OntimizeJEEException {

        int count = 0;
        String userSessionName = ((UserInformation) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal()).getLogin();

        Map<String, Object> keysValues = new HashMap<>();
        List<String> attrValues = new ArrayList<>();

        keysValues.put(this.getShareNameColumn(), shareName);
        keysValues.put(this.getShareUserColumn(), userSessionName);
        attrValues.add(this.getShareKeyColumn());

        EntityResult queryResult = this.daoHelper.query(this.daoSharePref, keysValues, attrValues);
        if (queryResult.getCode() != EntityResult.OPERATION_WRONG) {
            count = queryResult.calculateRecordNumber();
        }

        return count;
    }

    @Override
    public SharedElement getSharedItem(int idShare) throws OntimizeJEEException {

        List<String> attributesSharedElement = new ArrayList<>();
        attributesSharedElement.add(this.getShareKeyColumn());
        attributesSharedElement.add(this.getShareUserColumn());
        attributesSharedElement.add(this.getShareMessageColumn());
        attributesSharedElement.add(this.getShareTypeColumn());
        attributesSharedElement.add(this.getShareNameColumn());
        attributesSharedElement.add(this.getShareContentColumn());
        Map<String, Object> keysSharedElementValues = new HashMap<>();
        keysSharedElementValues.put(this.getShareKeyColumn(), idShare);

        EntityResult eRToRet = this.daoHelper.query(this.daoSharePref, keysSharedElementValues,
                attributesSharedElement);

        if ((eRToRet.calculateRecordNumber() == 1) && (eRToRet.getCode() != EntityResult.OPERATION_WRONG)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> actual = eRToRet.getRecordValues(0);
            String message = actual.get(this.getShareMessageColumn()).toString();
            String shareElementKey = actual.get(this.getShareTypeColumn()).toString();
            String content = actual.get(this.getShareContentColumn()).toString();
            String source = actual.get(this.getShareUserColumn()).toString();
            String name = actual.get(this.getShareNameColumn()).toString();
            return new SharedElement(idShare, message, shareElementKey, content, source, name);
        }

        if (eRToRet.getCode() == EntityResult.OPERATION_WRONG) {
            throw new OntimizeJEEException(eRToRet.getMessage());
        }

        return null;
    }

    @Override
    public SharedElement getTargetSharedItem(int idShareTarget) throws OntimizeJEEException {
        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put(this.getShareTargetKeyColumn(), idShareTarget);
        List<String> attributes = new ArrayList<>();
        attributes.add(this.getShareKeyColumn());

        EntityResult eRToRet = this.daoHelper.query(this.daoSharePrefTarget, keyValues, attributes);

        if ((eRToRet.getCode() != EntityResult.OPERATION_WRONG) && (eRToRet.calculateRecordNumber() == 1)) {
            int idShareElement = (Integer) eRToRet.getRecordValues(0).get(this.getShareKeyColumn());
            return this.getSharedItem(idShareElement);
        }

        if (eRToRet.getCode() == EntityResult.OPERATION_WRONG) {
            throw new OntimizeJEEException(eRToRet.getMessage());
        }

        return null;
    }

    @Override
    public EntityResult deleteSharedItem(int idShare) throws OntimizeJEEException {

        EntityResult toRet = new EntityResultMapImpl();

        String targetKeyColumn = this.getShareTargetKeyColumn();

        Map<String, Object> shareDeleteKeys = new HashMap<>();
        List<String> vShareTarget = new ArrayList<>();

        shareDeleteKeys.put(this.getShareKeyColumn(), idShare);
        vShareTarget.add(targetKeyColumn);

        EntityResult idTargetShareList = this.daoHelper.query(this.daoSharePrefTarget, shareDeleteKeys, vShareTarget);
        if (idTargetShareList.getCode() != EntityResult.OPERATION_WRONG) {
            for (int i = 0; i < idTargetShareList.calculateRecordNumber(); i++) {
                Object actualTargetId = idTargetShareList.getRecordValues(i).get(targetKeyColumn);

                Map<String, Object> targetKey = new HashMap<>();
                targetKey.put(targetKeyColumn, actualTargetId);

                EntityResult eResultDeleteTarget = this.daoHelper.delete(this.daoSharePrefTarget, targetKey);
                if (eResultDeleteTarget.getCode() == EntityResult.OPERATION_WRONG) {
                    throw new OntimizeJEEException(eResultDeleteTarget.getMessage());
                }
            }

            EntityResult entityResultDeleteItem = this.daoHelper.delete(this.daoSharePref, shareDeleteKeys);
            if (entityResultDeleteItem.getCode() == EntityResult.OPERATION_WRONG) {
                throw new OntimizeJEEException(entityResultDeleteItem.getMessage());
            }

        } else {
            throw new OntimizeJEEException("shareRemote.id_share_item_not_found");
        }

        return toRet;
    }

    @Override
    public EntityResult deleteTargetSharedItem(int idShareTarget) throws OntimizeJEEException {
        // Check target element
        EntityResult toRet = new EntityResultMapImpl();

        String targetKeyColumn = this.getShareTargetKeyColumn();
        String keyColumn = this.getShareKeyColumn();
        String targetName = this.getShareTargetUserColumn();

        Map<String, Object> targetQueryKeys = new HashMap<>();
        List<String> targetQueryAttr = new ArrayList<>();

        targetQueryKeys.put(targetKeyColumn, idShareTarget);
        targetQueryAttr.add(targetKeyColumn);
        targetQueryAttr.add(keyColumn);
        targetQueryAttr.add(targetName);

        EntityResult targetQuery = this.daoHelper.query(this.daoSharePrefTarget, targetQueryKeys, targetQueryAttr);

        if ((targetQuery.getCode() != EntityResult.OPERATION_WRONG) && (targetQuery.calculateRecordNumber() == 1)) {

            // Check shared element
            Object shareKey = targetQuery.getRecordValues(0).get(keyColumn);
            Object shareTarget = targetQuery.getRecordValues(0).get(targetName);

            Map<String, Object> shareItemIdKey = new HashMap<>();
            shareItemIdKey.put(keyColumn, shareKey);
            List<String> attributesToQuery = new ArrayList<>();
            attributesToQuery.add(this.getShareKeyColumn());
            attributesToQuery.add(this.getShareUserColumn());

            EntityResult sharedItem = this.daoHelper.query(this.daoSharePref, shareItemIdKey, attributesToQuery);
            if ((sharedItem.getCode() != EntityResult.OPERATION_WRONG) && (sharedItem.calculateRecordNumber() == 1)) {
                this.deleteTargetSharedItem(targetQueryKeys, shareTarget, sharedItem);
            } else {
                throw new OntimizeJEEException(sharedItem.getMessage());
            }

        } else {
            throw new OntimizeJEEException("shareRemote.id_share_target_not_found");
        }
        return toRet;
    }

    /**
     * Method used for reduce the complexity of {@link #deleteTargetSharedItem(int)}
     * @param targetQueryKeys
     * @param shareTarget
     * @param sharedItem
     * @throws OntimizeJEEException
     *
     * @see {@link #deleteTargetSharedItem(int)}
     */
    protected void deleteTargetSharedItem(Map<String, Object> targetQueryKeys, Object shareTarget,
            EntityResult sharedItem) throws OntimizeJEEException {
        // Check if user is the owner or the target
        Object shareItemOwner = sharedItem.getRecordValues(0).get(this.getShareUserColumn());
        String userSessionName = ((UserInformation) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal()).getLogin();
        if (userSessionName.equalsIgnoreCase((String) shareItemOwner)
                || userSessionName.equalsIgnoreCase((String) shareTarget)) {
            // Delete target
            EntityResult targetDeleteResult = this.daoHelper.delete(this.daoSharePrefTarget, targetQueryKeys);
            if (targetDeleteResult.getCode() == EntityResult.OPERATION_WRONG) {
                throw new OntimizeJEEException(targetDeleteResult.getMessage());
            }
        } else {
            throw new OntimizeJEEException("shareRemote.not_authorized_to_delete_target");
        }
    }

    @Override
    public EntityResult updateSharedItem(int idShare, String content, String message, String name)
            throws OntimizeJEEException {

        EntityResult toRet = new EntityResultMapImpl();

        String keyColumn = this.getShareKeyColumn();

        Map<String, Object> keySharedElement = new HashMap<>();
        List<String> attr = new ArrayList<>();
        attr.add(this.getShareKeyColumn());
        attr.add(this.getShareUserColumn());

        keySharedElement.put(keyColumn, idShare);

        EntityResult erQueryShare = this.daoHelper.query(this.daoSharePref, keySharedElement, attr);

        if ((erQueryShare.getCode() != EntityResult.OPERATION_WRONG) && (erQueryShare.calculateRecordNumber() == 1)) {
            Object shareItemOwner = erQueryShare.getRecordValues(0).get(this.getShareUserColumn());
            String userSessionName = ((UserInformation) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal()).getLogin();
            if (userSessionName.equalsIgnoreCase((String) shareItemOwner)) {

                Map<String, Object> updateKeys = new HashMap<>();
                Map<String, Object> updateAttr = new HashMap<>();

                updateKeys.put(keyColumn, idShare);

                updateAttr.put(this.getShareNameColumn(), name);
                updateAttr.put(this.getShareContentColumn(), content);
                updateAttr.put(this.getShareMessageColumn(), message);

                EntityResult erUpdateShare = this.daoSharePref.update(updateAttr, updateKeys);

                if (erUpdateShare.getCode() == EntityResult.OPERATION_WRONG) {
                    throw new OntimizeJEEException("shareRemote.error_on_update");
                }
            } else {
                throw new OntimizeJEEException("shareRemote.not_authorized_to_update_target");
            }

        } else {
            throw new OntimizeJEEException("shareRemote.share_element_not_found");
        }

        return toRet;
    }

}

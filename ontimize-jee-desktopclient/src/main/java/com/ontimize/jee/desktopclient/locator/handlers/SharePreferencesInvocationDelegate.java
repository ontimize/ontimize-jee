package com.ontimize.jee.desktopclient.locator.handlers;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import com.ontimize.db.EntityResult;
import com.ontimize.jee.common.services.sharepreferences.ISharePreferencesService;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.common.tools.proxy.AbstractInvocationDelegate;
import com.ontimize.jee.desktopclient.spring.BeansFactory;
import com.ontimize.util.share.IShareRemoteReference;
import com.ontimize.util.share.SharedElement;

public class SharePreferencesInvocationDelegate extends AbstractInvocationDelegate implements IShareRemoteReference {

    /**
     * Gets the i18n service.
     * @return the i18n service
     */
    private ISharePreferencesService getSharePreferencesService() {
        ISharePreferencesService sharePreferencesService = BeansFactory.getBean(ISharePreferencesService.class);
        CheckingTools.failIfNull(sharePreferencesService, "No share preferences service configured");
        return sharePreferencesService;
    }

    @Override
    public List<String> getUserList(int sessionId) throws Exception {
        return this.getSharePreferencesService().getUserList();
    }

    @Override
    public EntityResult addSharedItem(SharedElement sharedObject, List<String> targetList, int sessionId)
            throws Exception {
        return this.getSharePreferencesService().addSharedItem(sharedObject, targetList);
    }

    @Override
    public EntityResult editTargetSharedElement(int idShare, List<String> targetList, int sessionId) throws Exception {
        return this.getSharePreferencesService().editTargetSharedElement(idShare, targetList);
    }

    @Override
    public List<SharedElement> getSharedItemsWithUser(String username, int sessionId) throws Exception {
        return this.getSharePreferencesService().getSharedItemsWithUser(username);
    }

    @Override
    public List<SharedElement> getSharedItemsWithUserAndKey(String username, String shareKey, int sessionId)
            throws Exception {
        return this.getSharePreferencesService().getSharedItemsWithUserAndKey(username, shareKey);
    }

    @Override
    public List<HashMap<String, Object>> getTargetSharedElementMenuList(String username, String shareKey, int sessionId)
            throws Exception {
        return this.getSharePreferencesService().getTargetSharedElementMenuList(username, shareKey);
    }

    @Override
    public List<String> getTargetSharedItemsList(int idShare, int sessionId) throws Exception {
        return this.getSharePreferencesService().getTargetSharedItemsList(idShare);
    }

    @Override
    public List<SharedElement> getSourceSharedItemsList(String username, String shareKey, int sessionId)
            throws Exception {
        return this.getSharePreferencesService().getSourceSharedItemsList(username, shareKey);
    }

    @Override
    public List<HashMap<String, Object>> getSourceSharedElementMenuList(String username, String shareKey, int sessionId)
            throws Exception {
        return this.getSharePreferencesService().getSourceSharedElementMenuList(username, shareKey);
    }

    @Override
    public String getSharedElementMessage(int idShare, int sessionId) throws Exception {
        return this.getSharePreferencesService().getSharedElementMessage(idShare);
    }

    @Override
    public Object getSharedElementValue(int idShare, int sessionId) throws Exception {
        return this.getSharePreferencesService().getSharedElementValue(idShare);
    }

    @Override
    public int countSharedItemByNameAndUser(String shareName, int sessionId) throws Exception {
        return this.getSharePreferencesService().countSharedItemByNameAndUser(shareName);
    }

    @Override
    public SharedElement getSharedItem(int idShare, int sessionId) throws Exception {
        return this.getSharePreferencesService().getSharedItem(idShare);
    }

    @Override
    public SharedElement getTargetSharedItem(int idShareTarget, int sessionId) throws Exception {
        return this.getSharePreferencesService().getTargetSharedItem(idShareTarget);
    }

    @Override
    public EntityResult deleteSharedItem(int idShare, int sessionId) throws Exception {
        return this.getSharePreferencesService().deleteSharedItem(idShare);
    }

    @Override
    public EntityResult deleteTargetSharedItem(int idShareTarget, int sessionId) throws Exception {
        return this.getSharePreferencesService().deleteTargetSharedItem(idShareTarget);
    }

    @Override
    public EntityResult updateSharedItem(int idShare, String content, String message, String name, int sessionId)
            throws Exception {
        return this.getSharePreferencesService().updateSharedItem(idShare, content, message, name);
    }

    @Override
    public EntityResult addSharedItem(SharedElement sharedObject, List<String> targetList, int sessionId,
            Connection con) throws Exception {
        return this.getSharePreferencesService().addSharedItem(sharedObject, targetList);
    }

    @Override
    public EntityResult editTargetSharedElement(int idShare, List<String> targetList, int sessionId, Connection con)
            throws Exception {
        return this.getSharePreferencesService().editTargetSharedElement(idShare, targetList);
    }

    @Override
    public List<SharedElement> getSharedItemsWithUser(String username, int sessionId, Connection con) throws Exception {
        return this.getSharePreferencesService().getSharedItemsWithUser(username);
    }

    @Override
    public List<SharedElement> getSharedItemsWithUserAndKey(String username, String shareKey, int sessionId,
            Connection con) throws Exception {
        return this.getSharePreferencesService().getSharedItemsWithUserAndKey(username, shareKey);
    }

    @Override
    public List<HashMap<String, Object>> getTargetSharedElementMenuList(String username, String shareKey, int sessionId,
            Connection con) throws Exception {
        return this.getSharePreferencesService().getTargetSharedElementMenuList(username, shareKey);
    }

    @Override
    public List<String> getTargetSharedItemsList(int idShare, int sessionId, Connection con) throws Exception {
        return this.getSharePreferencesService().getTargetSharedItemsList(idShare);
    }

    @Override
    public List<SharedElement> getSourceSharedItemsList(String username, String shareKey, int sessionId, Connection con)
            throws Exception {
        return this.getSharePreferencesService().getSourceSharedItemsList(username, shareKey);
    }

    @Override
    public List<HashMap<String, Object>> getSourceSharedElementMenuList(String username, String shareKey, int sessionId,
            Connection con) throws Exception {
        return this.getSharePreferencesService().getSourceSharedElementMenuList(username, shareKey);
    }

    @Override
    public String getSharedElementMessage(int idShare, int sessionId, Connection con) throws Exception {
        return this.getSharePreferencesService().getSharedElementMessage(idShare);
    }

    @Override
    public Object getSharedElementValue(int idShare, int sessionId, Connection con) throws Exception {
        return this.getSharePreferencesService().getSharedElementValue(idShare);
    }

    @Override
    public int countSharedItemByNameAndUser(String shareName, int sessionId, Connection con) throws Exception {
        return this.getSharePreferencesService().countSharedItemByNameAndUser(shareName);
    }

    @Override
    public SharedElement getSharedItem(int idShare, int sessionId, Connection con) throws Exception {
        return this.getSharePreferencesService().getSharedItem(idShare);
    }

    @Override
    public SharedElement getTargetSharedItem(int idShareTarget, int sessionId, Connection con) throws Exception {
        return this.getSharePreferencesService().getTargetSharedItem(idShareTarget);
    }

    @Override
    public EntityResult deleteSharedItem(int idShare, int sessionId, Connection con) throws Exception {
        return this.getSharePreferencesService().deleteSharedItem(idShare);
    }

    @Override
    public EntityResult deleteTargetSharedItem(int idShareTarget, int sessionId, Connection con) throws Exception {
        return this.getSharePreferencesService().deleteTargetSharedItem(idShareTarget);
    }

    @Override
    public EntityResult updateSharedItem(int idShare, String content, String message, String name, int sessionId,
            Connection con) throws Exception {
        return this.getSharePreferencesService().updateSharedItem(idShare, content, message, name);
    }

}

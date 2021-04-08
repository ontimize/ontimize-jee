/*
 *
 */
package com.ontimize.jee.desktopclient.locator.handlers;

import java.lang.reflect.Proxy;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import com.ontimize.db.Entity;
import com.ontimize.db.EntityResult;
import com.ontimize.gui.i18n.ExtendedPropertiesBundle;
import com.ontimize.gui.i18n.IDatabaseBundleManager;
import com.ontimize.jee.common.tools.proxy.AbstractInvocationDelegate;
import com.ontimize.jee.desktopclient.locator.remoteoperation.WebsocketRemoteOperationManager;
import com.ontimize.locator.ErrorAccessControl;
import com.ontimize.locator.InitialContext;
import com.ontimize.locator.UtilReferenceLocator;
import com.ontimize.util.operation.RemoteOperationManager;
import com.ontimize.util.share.IShareRemoteReference;

/**
 * The Class UtilLocatorInvocationDelegate.
 */
public class UtilInvocationDelegate extends AbstractInvocationDelegate implements UtilReferenceLocator {

    /** The remote operation handler. */
    protected WebsocketRemoteOperationManager remoteOperationHandler;

    protected IDatabaseBundleManager databaseBundleManager;

    protected IShareRemoteReference sharePreferencesReference;

    /**
     * Instantiates a new util locator invocation delegate.
     */
    public UtilInvocationDelegate() {
        super();
        this.remoteOperationHandler = new WebsocketRemoteOperationManager();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.locator.UtilReferenceLocator#getMessages(int, int)
     */
    @Override
    public Vector getMessages(int sessionIdTo, int sessionId) throws Exception {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.locator.UtilReferenceLocator#sendMessage(java.lang.String, java.lang.String,
     * int)
     */
    @Override
    public void sendMessage(String message, String user, int sessionId) throws Exception {
        // do nothing right now
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.ontimize.locator.UtilReferenceLocator#sendMessage(com.ontimize.locator.UtilReferenceLocator.
     * Message, java.lang.String, int)
     */
    @Override
    public void sendMessage(Message message, String user, int sessionId) throws Exception {
        // do nothing right now
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.locator.UtilReferenceLocator#sendMessageToAll(java.lang.String, int)
     */
    @Override
    public void sendMessageToAll(String message, int sessionId) throws Exception {
        // do nothing right now
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.locator.UtilReferenceLocator#getAttachmentEntity(int)
     */
    @Override
    public Entity getAttachmentEntity(int sessionId) throws Exception {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.locator.UtilReferenceLocator#getPrintingTemplateEntity(int)
     */
    @Override
    public Entity getPrintingTemplateEntity(int sessionId) throws Exception {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.locator.UtilReferenceLocator#getConnectedUsers(int)
     */
    @Override
    public List getConnectedUsers(int sessionId) throws Exception {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.locator.UtilReferenceLocator#getConnectedSessionIds(int)
     */
    @Override
    public List getConnectedSessionIds(int sessionid) throws Exception {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.locator.UtilReferenceLocator#getRemoteOperationManager(int)
     */
    @Override
    public RemoteOperationManager getRemoteOperationManager(int sessionId) throws Exception {
        return this.remoteOperationHandler;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.locator.UtilReferenceLocator#getRemoteReference(java.lang.String, int)
     */
    @Override
    public Object getRemoteReference(String name, int sessionId) throws Exception {
        if (name == null) {
            return null;
        }
        if (name.equals(ExtendedPropertiesBundle.getDbBundleManagerName())) {
            return this.getRemoteReferenceDatabaseBundle();
        }
        if (name.equals(IShareRemoteReference.REMOTE_NAME)) {
            return this.getRemoteReferenceSharePreferences();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.locator.UtilReferenceLocator#removeEntity(java.lang.String, int)
     */
    @Override
    public void removeEntity(String entityName, int sessionId) throws Exception {
        // do nothing right now
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.locator.UtilReferenceLocator#getLoadedEntities(int)
     */
    @Override
    public List getLoadedEntities(int sessionId) throws Exception {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.locator.UtilReferenceLocator#getServerTimeZone(int)
     */
    @Override
    public TimeZone getServerTimeZone(int sessionId) throws Exception {
        return TimeZone.getDefault();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.locator.UtilReferenceLocator#getLoginEntityName(int)
     */
    @Override
    public String getLoginEntityName(int sessionId) throws Exception {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.locator.UtilReferenceLocator#getToken()
     */
    @Override
    public String getToken() throws Exception {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.locator.UtilReferenceLocator#getUserFromCert(java.lang.String)
     */
    @Override
    public String getUserFromCert(String certificate) throws Exception {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.locator.UtilReferenceLocator#getPasswordFromCert(java.lang.String)
     */
    @Override
    public String getPasswordFromCert(String certificate) throws Exception {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.locator.UtilReferenceLocator#retrieveInitialContext(int, java.util.Hashtable)
     */
    @Override
    public InitialContext retrieveInitialContext(int sessionId, Hashtable params) throws Exception {
        // TODO By now, return empty initial context...
        return new InitialContext();
    }

    /**
     * Gets the remote reference database bundle.
     * @return the remote reference database bundle
     */
    protected IDatabaseBundleManager getRemoteReferenceDatabaseBundle() {
        if (this.databaseBundleManager != null) {
            return this.databaseBundleManager;
        }

        this.databaseBundleManager = (IDatabaseBundleManager) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { IDatabaseBundleManager.class }, new DatabaseBundleManagerInvocationDelegate());
        return this.databaseBundleManager;
    }

    protected IShareRemoteReference getRemoteReferenceSharePreferences() {
        if (this.sharePreferencesReference != null) {
            return this.sharePreferencesReference;
        }

        this.sharePreferencesReference = (IShareRemoteReference) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { IShareRemoteReference.class }, new SharePreferencesInvocationDelegate());
        return this.sharePreferencesReference;
    }

    @Override
    public Locale getLocale(int arg0) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSuffixString() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setLocale(int arg0, Locale arg1) throws Exception {
        // TODO Auto-generated method stub
    }

    @Override
    public String getLocaleEntity() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void blockUserDB(String arg0) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public EntityResult changePassword(String arg0, int arg1, Hashtable arg2, Hashtable arg3) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean checkBlockUserDB(String arg0) throws Exception {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getAccessControl() throws Exception {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public ErrorAccessControl getErrorAccessControl() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean supportChangePassword(String arg0, int arg1) throws Exception {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Vector getRemoteAdministrationMessages(int arg0, int arg1) throws Exception {
        // TODO Auto-generated method stub
        return new Vector();
    }

    @Override
    public void sendRemoteAdministrationMessages(String arg0, int arg1) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean supportIncidenceService() throws Exception {
        // TODO Auto-generated method stub
        return false;
    }

}

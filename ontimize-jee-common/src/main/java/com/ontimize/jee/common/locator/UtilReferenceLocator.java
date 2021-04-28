package com.ontimize.jee.common.locator;

import com.ontimize.jee.core.common.db.Entity;
import com.ontimize.jee.core.common.dto.EntityResult;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Map;

import com.ontimize.jee.core.common.util.operation.RemoteOperationManager;

public interface UtilReferenceLocator extends java.rmi.Remote {

    public interface Message extends java.io.Serializable {

        public String getUserFrom();

        public String getMessage();

        public long getCommunicationId();

        public List getUsers();

    }

    public List getMessages(int sessionIdTo, int sessionId) throws Exception;

    public void sendMessage(String message, String user, int sessionId) throws Exception;

    public void sendMessage(Message message, String user, int sessionId) throws Exception;

    public void sendMessageToAll(String message, int sessionId) throws Exception;

    public void sendRemoteAdministrationMessages(String message, int sessionId) throws Exception;

    public List getRemoteAdministrationMessages(int sessionIdTo, int sessionId) throws Exception;

    public Entity getAttachmentEntity(int sessionId) throws Exception;

    public Entity getPrintingTemplateEntity(int sessionId) throws Exception;

    public List getConnectedUsers(int sessionId) throws Exception;

    public List getConnectedSessionIds(int sessionid) throws Exception;

    public RemoteOperationManager getRemoteOperationManager(int sessionId) throws Exception;

    /**
     * Gets a remote reference.
     * @param name
     * @param sessionId
     * @return
     * @throws Exception
     * @see {@link SecureReferenceLocator#configureRemoteReferences(String)}
     */
    public Object getRemoteReference(String name, int sessionId) throws Exception;

    public void removeEntity(String entityName, int sessionId) throws Exception;

    public List getLoadedEntities(int sessionId) throws Exception;

    /**
     * Get the Server TimeZone. If is local mode get the Application Time Zone
     * @param sessionId
     * @return
     * @throws Exception
     */
    public TimeZone getServerTimeZone(int sessionId) throws Exception;

    /**
     * Get the name of the login entity (LoginEntity parameter)
     * @param sessionId
     * @return The name of the Login Entity
     * @throws Exception
     */
    public String getLoginEntityName(int sessionId) throws Exception;

    public String getToken() throws Exception;

    public String getUserFromCert(String certificate) throws Exception;

    public String getPasswordFromCert(String certificate) throws Exception;

    public InitialContext retrieveInitialContext(int sessionId, Map params) throws Exception;

    public Locale getLocale(int sessionId) throws Exception;

    public void setLocale(int sessionId, Locale locale) throws Exception;

    public String getSuffixString() throws Exception;

    public String getLocaleEntity() throws Exception;

    public boolean supportIncidenceService() throws Exception;

    public boolean supportChangePassword(String user, int sessionId) throws Exception;

    public EntityResult changePassword(String password, int sessionId, Map av, Map kv) throws Exception;

    public boolean getAccessControl() throws Exception;

    public ErrorAccessControl getErrorAccessControl() throws Exception;

    public void blockUserDB(String user) throws Exception;

    public boolean checkBlockUserDB(String user) throws Exception;

}

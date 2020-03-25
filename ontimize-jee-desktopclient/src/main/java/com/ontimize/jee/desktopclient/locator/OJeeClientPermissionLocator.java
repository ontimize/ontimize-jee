package com.ontimize.jee.desktopclient.locator;

import java.lang.reflect.InvocationHandler;
import java.net.ConnectException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import com.ontimize.db.Entity;
import com.ontimize.db.EntityResult;
import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.ClientWatch;
import com.ontimize.gui.ConnectionOptimizer;
import com.ontimize.gui.preferences.RemoteApplicationPreferenceReferencer;
import com.ontimize.gui.preferences.RemoteApplicationPreferences;
import com.ontimize.jee.common.exceptions.InvalidCredentialsException;
import com.ontimize.jee.common.security.ILoginProvider;
import com.ontimize.jee.common.services.formprovider.IFormProviderService;
import com.ontimize.jee.common.services.user.IUserInformationService;
import com.ontimize.jee.common.services.user.UserInformation;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.desktopclient.hessian.HessianSessionLocatorInvocationDelegate;
import com.ontimize.jee.desktopclient.locator.handlers.ClientPermissionInvocationDelegate;
import com.ontimize.jee.desktopclient.locator.handlers.ConnectionOptimizerInvocationDelegate;
import com.ontimize.jee.desktopclient.locator.handlers.LicenseLocatorInvocationDelegate;
import com.ontimize.jee.desktopclient.locator.handlers.RemoteApplicationPreferenceReferencerDelegate;
import com.ontimize.jee.desktopclient.locator.handlers.UtilInvocationDelegate;
import com.ontimize.jee.desktopclient.locator.handlers.XMLClientProviderInvocationDelegate;
import com.ontimize.jee.desktopclient.locator.security.OntimizeLoginProvider;
import com.ontimize.jee.desktopclient.spring.BeansFactory;
import com.ontimize.locator.ErrorAccessControl;
import com.ontimize.locator.InitialContext;
import com.ontimize.locator.SecureEntityReferenceLocator;
import com.ontimize.locator.UtilReferenceLocator;
import com.ontimize.ols.RemoteLOk;
import com.ontimize.security.ClientPermissionManager;
import com.ontimize.util.operation.RemoteOperationManager;
import com.ontimize.util.remote.BytesBlock;
import com.ontimize.xml.XMLClientProvider;

/**
 * Clase abstracta que define el comportamiento que deben tener los
 * ClientPermissionLocator contra un servidor JEE.
 *
 * @author joaquin.romero
 *
 */
public class OJeeClientPermissionLocator implements SecureEntityReferenceLocator, RemoteLOk, ConnectionOptimizer, XMLClientProvider, UtilReferenceLocator, ClientPermissionManager, RemoteApplicationPreferenceReferencer {

	private static final Logger logger = LoggerFactory.getLogger(OJeeClientPermissionLocator.class);
	private static final String REMOTE_LOCATOR_INVOCATION_HANDLER = "remoteLocatorInvocationHandler";
	public static final String CHECK_SERVER_MESSAGES_PERIOD = "CheckServerMessagePeriod";
	public static final String REMOTE_REFERENCE_LOCATOR_NAME = "RemoteLocatorName";
	public static final String CLIENT_PERMISSION_COLUMN = "ClientPermissionColumn";

	private static String REMOTE_LOCATOR_NAME_PROPERTY = "com.ontimize.locator.ReferenceLocator.RemoteLocatorName";
	protected String referenceLocatorServerName = "";
	protected String clientPermissionsColumn = "ClientPermissions";

	private boolean localLocator;
	private int messagesCheckTime;

	protected List<ISessionListener> sessionListeners;
	protected UserInformation userInformation;
	private boolean startSession = false;
	protected int userId = -1;
	
	private HessianSessionLocatorInvocationDelegate sessionLocatorInvocationDelegate = new HessianSessionLocatorInvocationDelegate();
	private LicenseLocatorInvocationDelegate licenseLocatorInvocationDelegate = new LicenseLocatorInvocationDelegate();
	private ConnectionOptimizerInvocationDelegate connectionOptimizerInvocationDelegate = new ConnectionOptimizerInvocationDelegate();
	private XMLClientProviderInvocationDelegate xmlClientProviderInvocationDelegate = new XMLClientProviderInvocationDelegate();
	private UtilInvocationDelegate utilInvocationDelegate = new UtilInvocationDelegate();
	private ClientPermissionInvocationDelegate clientPermissionInvocationDelegate = new ClientPermissionInvocationDelegate();
	private RemoteApplicationPreferenceReferencerDelegate remoteApplicationPreferenceReferencerDelegate = new RemoteApplicationPreferenceReferencerDelegate();
	

	public OJeeClientPermissionLocator(Hashtable params) {
		initializeParameters(params);
		this.sessionListeners = new ArrayList<>();

		InvocationHandler handler = BeansFactory.getBean(OJeeClientPermissionLocator.REMOTE_LOCATOR_INVOCATION_HANDLER,
				InvocationHandler.class);
		if (handler == null) {
			handler = ReflectionTools.newInstance(
					(String) params.get(OJeeClientPermissionLocator.REMOTE_LOCATOR_INVOCATION_HANDLER),
					InvocationHandler.class);
		}
	}

	/**
	 * Session start
	 */
	public int startSession(String user, String password, ClientWatch cw) throws Exception {
		ILoginProvider bean = null;
		try {
			try {
				bean = BeansFactory.getBean(ILoginProvider.class);
			} catch (NoSuchBeanDefinitionException ex) {
				OJeeClientPermissionLocator.logger.trace(null, ex);
				BeansFactory.registerBeanDefinition("loginProvider",
						new AnnotatedGenericBeanDefinition(OntimizeLoginProvider.class));
				// yes, duplicate line. seems spring bug... review when possible
				BeansFactory.registerBeanDefinition("loginProvider",
						new AnnotatedGenericBeanDefinition(OntimizeLoginProvider.class));
				bean = BeansFactory.getBean(ILoginProvider.class);
			}
			String url = System.getProperty("com.ontimize.services.baseUrl");
			URI uri = new URI(url);
			bean.doLogin(uri, user, password);
			IUserInformationService userService = BeansFactory.getBean(IUserInformationService.class);
			this.userInformation = userService.getUserInformation();
			if (this.userInformation == null) {
				throw new AuthenticationCredentialsNotFoundException(user);
			}
			// Save password in local
			this.userInformation.setPassword(password);
		} catch (InvalidCredentialsException ex) {
			throw new SecurityException("E_LOGIN__INVALID_CREDENTIALS", ex);
		} catch (ConnectException ex) {
			throw new SecurityException("E_CONNECT_SERVER", ex);
		} catch (Exception ex) {
			throw new SecurityException("E_LOGIN__ERROR", ex);
		}

		int sesId = this.initializeSession(user, password, cw);
		for (ISessionListener listener : this.sessionListeners) {
			listener.sessionStarted(sesId);
		}
		return sesId;
	}

	protected int initializeSession(String user, String password, ClientWatch cw) throws Exception {
		if (!this.startSession) {
			this.userId = this.startRemoteSession(user, password, cw);
		}
		return 1;
	}

	protected int startRemoteSession(String user, String password, ClientWatch cw) throws Exception {		
		return sessionLocatorInvocationDelegate.startSession(user, password, cw);
	}
	
	public UserInformation getUserInformation() {
		return this.userInformation;
	}

	public void registerSessionListener(ISessionListener listener) {
		this.sessionListeners.add(listener);
	}

	public void unregisterSessionListener(ISessionListener listener) {
		this.sessionListeners.remove(listener);
	}

	public String getXMLForm(String form, int userid) throws Exception {
		try {
			IFormProviderService current = BeansFactory.getBean(IFormProviderService.class);
			return current.getXMLForm(form);
		} catch (Exception error) {
			OJeeClientPermissionLocator.logger.debug("Form provider not available: {}", error.getMessage(), error);
			OJeeClientPermissionLocator.logger.info("Form provider not available");
			return null;
		}
	}

	public void initializeParameters(Map<String, Object> params) {
		// Check Message Server Period
		checkMessageServerPeriod(params);
		// Local locator
		this.localLocator = false;
		// Configure Remote Reference Locator
		configureRemoteReferenceLocatorName(params);
		// Configure client permission columns
		configureClientPermissionColumn(params);

	}

	private void configureClientPermissionColumn(Map<String, Object> params) {
		Object col = ApplicationManager.getParameterValue(OJeeClientPermissionLocator.CLIENT_PERMISSION_COLUMN,
				(Hashtable) params);
		if (col != null) {
			this.clientPermissionsColumn = col.toString();
		}
	}

	private void configureRemoteReferenceLocatorName(Map<String, Object> params) {
		Object oRemoteReferenceLocatorName = ApplicationManager
				.getParameterValue(OJeeClientPermissionLocator.REMOTE_REFERENCE_LOCATOR_NAME, (Hashtable) params);
		if (oRemoteReferenceLocatorName != null) {
			this.referenceLocatorServerName = oRemoteReferenceLocatorName.toString();
		} else {
			this.referenceLocatorServerName = "";
			OJeeClientPermissionLocator.logger.error("'{}' parameter not found",
					OJeeClientPermissionLocator.REMOTE_REFERENCE_LOCATOR_NAME);
		}

		String sRemoteProperty = System.getProperty(OJeeClientPermissionLocator.REMOTE_LOCATOR_NAME_PROPERTY);
		if ((sRemoteProperty != null) && (!this.localLocator)) {
			this.referenceLocatorServerName = sRemoteProperty;
			OJeeClientPermissionLocator.logger.info("Using '{}'.",
					OJeeClientPermissionLocator.REMOTE_LOCATOR_NAME_PROPERTY);
		}
	}

	private void checkMessageServerPeriod(Map<String, Object> params) {
		Object checkservermessagesperiod = ApplicationManager
				.getParameterValue(OJeeClientPermissionLocator.CHECK_SERVER_MESSAGES_PERIOD, (Hashtable) params);
		if (checkservermessagesperiod != null) {
			try {
				this.messagesCheckTime = Integer.parseInt(checkservermessagesperiod.toString());
				if ((this.messagesCheckTime != -1) && (this.messagesCheckTime < 10000)) {
					this.messagesCheckTime = 10000;
				}
			} catch (Exception e) {
				OJeeClientPermissionLocator.logger.error(
						"'" + OJeeClientPermissionLocator.CHECK_SERVER_MESSAGES_PERIOD + "' parameter error.", e);
			}
		}
	}

	@Override
	public Entity getEntityReference(String entityName) throws Exception {
		return this.sessionLocatorInvocationDelegate.getEntityReference(entityName);
	}

	@Override
	public int getSessionId() throws Exception {
		return this.sessionLocatorInvocationDelegate.getSessionId();
	}

	@Override
	public void endSession(int id) throws Exception {
		this.sessionLocatorInvocationDelegate.endSession(id);
		
	}

	@Override
	public Entity getEntityReference(String entity, String user, int sessionId) throws Exception {
		return this.sessionLocatorInvocationDelegate.getEntityReference(entity, user, sessionId);
	}

	@Override
	public boolean hasSession(String user, int id) throws Exception {
		return this.sessionLocatorInvocationDelegate.hasSession(user, id);
	}

	@Override
	public boolean ok(int sessionId) throws Exception {
		return this.licenseLocatorInvocationDelegate.ok(sessionId);
	}

	@Override
	public boolean ok(int sessionId, String number) throws Exception {
		return this.licenseLocatorInvocationDelegate.ok(sessionId, number);
	}

	@Override
	public boolean isDevelopementL(int sessionId) throws Exception {
		return this.licenseLocatorInvocationDelegate.isDevelopementL(sessionId);
	}

	@Override
	public String getLValue(int sessionId, String name) throws Exception {
		return this.licenseLocatorInvocationDelegate.getLValue(sessionId, name);
	}

	@Override
	public String getLContent(int sessionId) throws Exception {
		return this.licenseLocatorInvocationDelegate.getLContent(sessionId);
	}

	@Override
	public Object getLInfoObject(int sessionId) throws Exception {
		return this.licenseLocatorInvocationDelegate.getLInfoObject(sessionId);
	}

	@Override
	public EntityResult testConnectionSpeed(int sizeInBytes, boolean compressed) throws Exception {
		return this.connectionOptimizerInvocationDelegate.testConnectionSpeed(sizeInBytes, compressed);
	}

	@Override
	public void setDataCompressionThreshold(String user, int id, int compression) throws Exception {
		this.connectionOptimizerInvocationDelegate.setDataCompressionThreshold(user, id, compression);
		
	}

	@Override
	public int getDataCompressionThreshold(int sessionId) throws Exception {
		return this.connectionOptimizerInvocationDelegate.getDataCompressionThreshold(sessionId);
	}

	@Override
	public Hashtable getFormManagerParameters(String formManagerId, int userid) throws Exception {
		return this.xmlClientProviderInvocationDelegate.getFormManagerParameters(formManagerId, userid);
	}

	@Override
	public String getXMLRules(String form, int userid) throws Exception {
		return this.xmlClientProviderInvocationDelegate.getXMLRules(form, userid);
	}

	@Override
	public String getXMLMenu(int userid) throws Exception {
		return this.xmlClientProviderInvocationDelegate.getXMLMenu(userid);
	}

	@Override
	public void reloadXMLMenu(int userId) throws Exception {
		this.xmlClientProviderInvocationDelegate.reloadXMLMenu(userId);
	}

	@Override
	public String getXMLToolbar(int userid) throws Exception {
		return this.xmlClientProviderInvocationDelegate.getXMLToolbar(userid);
	}

	@Override
	public void reloadXMLToolbar(int userId) throws Exception {
		this.xmlClientProviderInvocationDelegate.reloadXMLToolbar(userId);
	}

	@Override
	public BytesBlock getImage(String image, int userId) throws Exception {
		return this.xmlClientProviderInvocationDelegate.getImage(image, userId);
	}

	@Override
	public Vector getMessages(int sessionIdTo, int sessionId) throws Exception {
		return this.utilInvocationDelegate.getMessages(sessionIdTo, sessionId);
	}

	@Override
	public void sendMessage(String message, String user, int sessionId) throws Exception {
		this.utilInvocationDelegate.sendMessage(message, user, sessionId);
	}

	@Override
	public void sendMessage(Message message, String user, int sessionId) throws Exception {
		this.utilInvocationDelegate.sendMessage(message, user, sessionId);
	}

	@Override
	public void sendMessageToAll(String message, int sessionId) throws Exception {
		this.utilInvocationDelegate.sendMessageToAll(message, sessionId);
	}

	@Override
	public void sendRemoteAdministrationMessages(String message, int sessionId) throws Exception {
		this.utilInvocationDelegate.sendRemoteAdministrationMessages(message, sessionId);
	}

	@Override
	public Vector getRemoteAdministrationMessages(int sessionIdTo, int sessionId) throws Exception {
		return this.utilInvocationDelegate.getRemoteAdministrationMessages(sessionIdTo, sessionId);
	}

	@Override
	public Entity getAttachmentEntity(int sessionId) throws Exception {
		return this.utilInvocationDelegate.getAttachmentEntity(sessionId);
	}

	@Override
	public Entity getPrintingTemplateEntity(int sessionId) throws Exception {
		return this.utilInvocationDelegate.getPrintingTemplateEntity(sessionId);
	}

	@Override
	public List getConnectedUsers(int sessionId) throws Exception {
		return this.utilInvocationDelegate.getConnectedUsers(sessionId);
	}

	@Override
	public List getConnectedSessionIds(int sessionid) throws Exception {
		return this.utilInvocationDelegate.getConnectedSessionIds(sessionid);
	}

	@Override
	public RemoteOperationManager getRemoteOperationManager(int sessionId) throws Exception {
		return this.utilInvocationDelegate.getRemoteOperationManager(sessionId);
	}

	@Override
	public Object getRemoteReference(String name, int sessionId) throws Exception {
		return this.utilInvocationDelegate.getRemoteReference(name, sessionId);
	}

	@Override
	public void removeEntity(String entityName, int sessionId) throws Exception {
		this.utilInvocationDelegate.removeEntity(entityName, sessionId);
	}

	@Override
	public List getLoadedEntities(int sessionId) throws Exception {
		return this.utilInvocationDelegate.getLoadedEntities(sessionId);
	}

	@Override
	public TimeZone getServerTimeZone(int sessionId) throws Exception {
		return this.utilInvocationDelegate.getServerTimeZone(sessionId);
	}

	@Override
	public String getLoginEntityName(int sessionId) throws Exception {
		return this.utilInvocationDelegate.getLoginEntityName(sessionId);
	}

	@Override
	public String getToken() throws Exception {
		return this.utilInvocationDelegate.getToken();
	}

	@Override
	public String getUserFromCert(String certificate) throws Exception {
		return this.utilInvocationDelegate.getUserFromCert(certificate);
	}

	@Override
	public String getPasswordFromCert(String certificate) throws Exception {
		return this.utilInvocationDelegate.getPasswordFromCert(certificate);
	}

	@Override
	public InitialContext retrieveInitialContext(int sessionId, Hashtable params) throws Exception {
		return this.utilInvocationDelegate.retrieveInitialContext(sessionId, params);
	}

	@Override
	public Locale getLocale(int sessionId) throws Exception {
		return this.utilInvocationDelegate.getLocale(sessionId);
	}

	@Override
	public void setLocale(int sessionId, Locale locale) throws Exception {
		this.utilInvocationDelegate.setLocale(sessionId, locale);
	}

	@Override
	public String getSuffixString() throws Exception {
		return this.utilInvocationDelegate.getSuffixString();
	}

	@Override
	public String getLocaleEntity() throws Exception {
		return this.utilInvocationDelegate.getLocaleEntity();
	}

	@Override
	public boolean supportIncidenceService() throws Exception {
		return this.utilInvocationDelegate.supportIncidenceService();
	}

	@Override
	public boolean supportChangePassword(String user, int sessionId) throws Exception {
		return this.utilInvocationDelegate.supportChangePassword(user, sessionId);
	}

	@Override
	public EntityResult changePassword(String password, int sessionId, Hashtable av, Hashtable kv) throws Exception {
		return this.utilInvocationDelegate.changePassword(password, sessionId, av, kv);
	}

	@Override
	public boolean getAccessControl() throws Exception {
		return this.utilInvocationDelegate.getAccessControl();
	}

	@Override
	public ErrorAccessControl getErrorAccessControl() throws Exception {
		return this.utilInvocationDelegate.getErrorAccessControl();
	}

	@Override
	public void blockUserDB(String user) throws Exception {
		this.utilInvocationDelegate.blockUserDB(user);
	}

	@Override
	public boolean checkBlockUserDB(String user) throws Exception {
		return this.utilInvocationDelegate.checkBlockUserDB(user);
	}

	@Override
	public EntityResult getClientPermissions(Hashtable userKeys, int sessionId) throws Exception {
		return this.clientPermissionInvocationDelegate.getClientPermissions(userKeys, sessionId);
	}

	@Override
	public void installClientPermissions(Hashtable userKeys, int sessionId) throws Exception {
		this.clientPermissionInvocationDelegate.installClientPermissions(userKeys, sessionId);
	}

	@Override
	public long getTime() throws Exception {
		return this.clientPermissionInvocationDelegate.getTime();
	}

	@Override
	public RemoteApplicationPreferences getRemoteApplicationPreferences(int sessionId) throws Exception {
		return this.remoteApplicationPreferenceReferencerDelegate.getRemoteApplicationPreferences(sessionId);
	}

}
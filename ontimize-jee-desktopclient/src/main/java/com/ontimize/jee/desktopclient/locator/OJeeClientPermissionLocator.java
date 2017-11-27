package com.ontimize.jee.desktopclient.locator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.ConnectException;
import java.net.URI;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import com.ontimize.db.EntityResult;
import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.ClientWatch;
import com.ontimize.gui.MessageDialog;
import com.ontimize.jee.common.exceptions.InvalidCredentialsException;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.security.ILoginProvider;
import com.ontimize.jee.common.services.user.IUserInformationService;
import com.ontimize.jee.common.services.user.UserInformation;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.desktopclient.i18n.I18NNaming;
import com.ontimize.jee.desktopclient.locator.security.OntimizeLoginProvider;
import com.ontimize.jee.desktopclient.spring.BeansFactory;
import com.ontimize.locator.PermissionReferenceLocator;
import com.ontimize.locator.PermissionSecureReferenceLocator;
import com.ontimize.security.ClientSecurityManager;

/**
 * Clase abstracta que define el comportamiento que deben tener los ClientPermissionLocator contra un servidor JEE.
 *
 * @author joaquin.romero
 *
 */
public class OJeeClientPermissionLocator extends PermissionReferenceLocator {

	private static final Logger			logger								= LoggerFactory.getLogger(OJeeClientPermissionLocator.class);
	private static final String			REMOTE_LOCATOR_INVOCATION_HANDLER	= "remoteLocatorInvocationHandler";

	protected List<ISessionListener>	sessionListeners;

	// protected String authorizationToken;
	protected UserInformation			userInformation;

	public OJeeClientPermissionLocator(Hashtable params) {
		super(params);
		this.sessionListeners = new ArrayList<>();

		InvocationHandler handler = BeansFactory.getBean(OJeeClientPermissionLocator.REMOTE_LOCATOR_INVOCATION_HANDLER, InvocationHandler.class);
		if (handler == null) {
			handler = ReflectionTools.newInstance((String) params.get(OJeeClientPermissionLocator.REMOTE_LOCATOR_INVOCATION_HANDLER), InvocationHandler.class);
		}
		this.setRemoteLocatorInvocationHandler(handler);
	}

	/**
	 * Session start
	 */
	@Override
	public int startSession(String user, String password, ClientWatch cw) throws Exception {
		ILoginProvider bean = null;
		try {
			try {
				bean = BeansFactory.getBean(ILoginProvider.class);
			} catch (NoSuchBeanDefinitionException ex) {
				OJeeClientPermissionLocator.logger.trace(null, ex);
				BeansFactory.registerBeanDefinition("loginProvider", new AnnotatedGenericBeanDefinition(OntimizeLoginProvider.class));
				// yes, duplicate line. seems spring bug... review when possible
				BeansFactory.registerBeanDefinition("loginProvider", new AnnotatedGenericBeanDefinition(OntimizeLoginProvider.class));
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

		int sesId = super.startSession(user, password, cw);
		for (ISessionListener listener : this.sessionListeners) {
			listener.sessionStarted(sesId);
		}
		return sesId;
	}

	@Override
	public boolean closeSession(String id, int sessionId) throws Exception {
		boolean closeSession = super.closeSession(id, sessionId);
		if (closeSession) {
			for (ISessionListener listener : this.sessionListeners) {
				listener.sessionClosed(sessionId);
			}
		}
		return closeSession;
	}

	/**
	 * Establish the server locator througth a proxy
	 *
	 * @param remoteLocatorInvocationHandler
	 *            the invocation handler
	 */
	public void setRemoteLocatorInvocationHandler(InvocationHandler remoteLocatorInvocationHandler) {
		if (remoteLocatorInvocationHandler == null) {
			throw new OntimizeJEERuntimeException(I18NNaming.E_REMOTE_LOCATOR_INVOCATIONHANDLER_MUST_BE_DEFINED);
		}
		this.referenceLocatorServer = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
				ReflectionTools.getInterfacesExtending(PermissionSecureReferenceLocator.class, Remote.class), remoteLocatorInvocationHandler);
	}

	@Override
	protected void ensureRemoteReferenceLocator() throws Exception {
		if (this.referenceLocatorServer == null) {
			throw new OntimizeJEERuntimeException(I18NNaming.E_REMOTE_LOCATOR_REFERENCE_NO_ESTABLISHED);
		}
		super.ensureRemoteReferenceLocator();
	}

	@Override
	public EntityResult getClientPermissions(Hashtable userKeys, int userId) throws Exception {
		return null;
	}

	@Override
	public void installClientPermissions(Hashtable keys, int sessionId) throws Exception {
		if (this.clientPermissionsInstalled) {
			OJeeClientPermissionLocator.logger.warn("Client permissions are already installed. No new permission can be installed.");
			return;
		}

		Map<String, ?> clientPermissions = this.userInformation.getClientPermissions();
		if (clientPermissions != null) {
			ApplicationManager.setClientSecurityManager(new ClientSecurityManager(Hashtable.class.cast(clientPermissions)));
			this.clientPermissionsInstalled = true;
			this.updateHourServerThread = ReflectionTools.newInstance(TimeThread.class, this);
			this.updateHourServerThread.start();
		} else {
			MessageDialog.showMessage((JDialog) null, "Error retrieving client permissions from column : " + this.clientPermissionsColumn + ", : Returned NULL value",
					JOptionPane.ERROR_MESSAGE, null);
			OJeeClientPermissionLocator.logger.error("Error retrieving client permissions from column : {} . Returned NULL value", this.clientPermissionsColumn);
		}
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

}
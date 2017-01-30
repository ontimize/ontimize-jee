package com.ontimize.jee.server.services.preferences;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.security.PermissionsProviderSecured;
import com.ontimize.jee.common.services.preferences.IRemoteApplicationPreferencesService;
import com.ontimize.jee.common.services.user.UserInformation;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.server.configuration.OntimizeConfiguration;

/**
 * The Class RemoteApplicationPreferencesServiceImpl.
 */
@Service("RemotePreferencesService")
public class RemoteApplicationPreferencesServiceImpl implements IRemoteApplicationPreferencesService, ApplicationContextAware {

	/** The implementation. */
	private IRemoteApplicationPreferencesEngine	implementation;

	/**
	 * (non-Javadoc)
	 *
	 * @see com.ontimize.jee.common.services.preferences.IRemoteApplicationPreferencesService#getRemotePreference(java.lang.String)
	 */
	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public String getPreference(String preferenceName) throws OntimizeJEERuntimeException {
		return this.getImplementation().getPreference(this.getUserLogin(), preferenceName);
	}

	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public String getDefaultPreference(String preferenceName) throws OntimizeJEERuntimeException {
		return this.getImplementation().getDefaultPreference(preferenceName);
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see com.ontimize.jee.common.services.preferences.IRemoteApplicationPreferencesService#setRemotePreference(java.lang.String, java.lang.String)
	 */
	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public void setPreference(String preferenceName, String value) throws OntimizeJEERuntimeException {
		this.getImplementation().setPreference(this.getUserLogin(), preferenceName, value);
	}

	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public void setDefaultPreference(String preferenceName, String value) throws OntimizeJEERuntimeException {
		this.getImplementation().setDefaultPreference(preferenceName, value);

	}

	/**
	 * (non-Javadoc)
	 *
	 * @see com.ontimize.jee.common.services.preferences.IRemoteApplicationPreferencesService#saveRemotePreferences()
	 */
	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public void savePreferences() throws OntimizeJEERuntimeException {
		this.getImplementation().savePreferences();
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see com.ontimize.jee.common.services.preferences.IRemoteApplicationPreferencesService#loadRemotePreferences()
	 */
	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public void loadPreferences() throws OntimizeJEERuntimeException {
		this.getImplementation().loadPreferences();
	}

	/**
	 * Gets the implementation.
	 *
	 * @return the implementation
	 */
	protected IRemoteApplicationPreferencesEngine getImplementation() {
		CheckingTools.failIfNull(this.implementation, "Not implementation defined for remote preferences");
		return this.implementation;
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.implementation = applicationContext.getBean(OntimizeConfiguration.class).getRemotePreferencesConfiguration().getEngine();
	}

	/**
	 * Gets the user login.
	 *
	 * @return the user login
	 */
	protected String getUserLogin() {
		UserInformation principal = (UserInformation) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return principal.getLogin();
	}

}

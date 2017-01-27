package com.ontimize.jee.server.configuration;

import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.server.security.SecurityConfiguration;
import com.ontimize.jee.server.services.i18n.I18nConfiguration;
import com.ontimize.jee.server.services.mail.MailConfiguration;
import com.ontimize.jee.server.services.preferences.RemotePreferencesConfiguration;
import com.ontimize.jee.server.services.remoteoperation.RemoteOperationConfiguration;

/**
 * The Class OntimizeConfiguration.
 */
public class OntimizeConfiguration {

	/** The remote operation configuration. */
	protected RemoteOperationConfiguration		remoteOperationConfiguration;

	/** The security configuration. */
	protected SecurityConfiguration				securityConfiguration;

	/** The security configuration. */
	protected RemotePreferencesConfiguration	remotePreferencesConfiguration;

	/** The i18n configuration. */
	protected I18nConfiguration					i18nConfiguration;

	/** The mail configuration. */
	protected MailConfiguration					mailConfiguration;

	/**
	 * Instantiates a new ontimize configuration.
	 */
	public OntimizeConfiguration() {
		super();
	}

	/**
	 * Gets the remote operation configuration.
	 *
	 * @return the remote operation configuration
	 */
	public RemoteOperationConfiguration getRemoteOperationConfiguration() {
		if (this.remoteOperationConfiguration == null) {
			throw new OntimizeJEERuntimeException("Remote operation is not configured");
		}
		return this.remoteOperationConfiguration;
	}

	/**
	 * Gets the remote preferences configuration.
	 *
	 * @return the remote preferences configuration
	 */
	public RemotePreferencesConfiguration getRemotePreferencesConfiguration() {
		if (this.remotePreferencesConfiguration == null) {
			throw new OntimizeJEERuntimeException("remote preferences is not configured");
		}
		return this.remotePreferencesConfiguration;
	}

	/**
	 * Sets the remote operation configuration.
	 *
	 * @param remoteOperationConfiguration
	 *            the new remote operation configuration
	 */
	public void setRemoteOperationConfiguration(RemoteOperationConfiguration remoteOperationConfiguration) {
		this.remoteOperationConfiguration = remoteOperationConfiguration;
	}

	/**
	 * Sets the security configuration.
	 *
	 * @param securityConfiguration
	 *            the new security configuration
	 */
	public void setSecurityConfiguration(SecurityConfiguration securityConfiguration) {
		this.securityConfiguration = securityConfiguration;
	}

	/**
	 * Gets the security configuration.
	 *
	 * @return the security configuration
	 */
	public SecurityConfiguration getSecurityConfiguration() {
		return this.securityConfiguration;
	}

	/**
	 * Gets the i18n configuration.
	 *
	 * @return the i18n configuration
	 */
	public I18nConfiguration getI18nConfiguration() {
		return this.i18nConfiguration;
	}

	/**
	 * Sets the i18n configuration.
	 *
	 * @param i18nConfiguration
	 *            the i18n configuration
	 */
	public void setI18nConfiguration(I18nConfiguration i18nConfiguration) {
		this.i18nConfiguration = i18nConfiguration;
	}

	/**
	 * Sets the remote preferences configuration.
	 *
	 * @param remotePreferencesConfiguration
	 *            the remote preferences configuration
	 */
	public void setRemotePreferencesConfiguration(RemotePreferencesConfiguration remotePreferencesConfiguration) {
		this.remotePreferencesConfiguration = remotePreferencesConfiguration;
	}

	/**
	 * Gets the mail configuration.
	 *
	 * @return the mail configuration
	 */
	public MailConfiguration getMailConfiguration() {
		return this.mailConfiguration;
	}

	/**
	 * Sets the mail configuration.
	 *
	 * @param mailConfiguration
	 *            the new mail configuration
	 */
	public void setMailConfiguration(MailConfiguration mailConfiguration) {
		this.mailConfiguration = mailConfiguration;
	}
}
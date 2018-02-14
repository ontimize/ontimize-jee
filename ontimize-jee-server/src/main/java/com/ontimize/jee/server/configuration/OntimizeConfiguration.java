package com.ontimize.jee.server.configuration;

import org.springframework.context.annotation.Lazy;

import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.server.security.SecurityConfiguration;
import com.ontimize.jee.server.services.formprovider.FormProviderConfiguration;
import com.ontimize.jee.server.services.i18n.I18nConfiguration;
import com.ontimize.jee.server.services.mail.MailConfiguration;
import com.ontimize.jee.server.services.preferences.RemotePreferencesConfiguration;
import com.ontimize.jee.server.services.remoteoperation.RemoteOperationConfiguration;
import com.ontimize.jee.server.services.sharepreferences.SharePreferencesConfiguration;

/**
 * The Class OntimizeConfiguration.
 */
public class OntimizeConfiguration {

	/** The remote operation configuration. */
	@Lazy protected RemoteOperationConfiguration remoteOperationConfiguration;

	/** The security configuration. */
	@Lazy protected SecurityConfiguration securityConfiguration;

	/** The security configuration. */
	@Lazy protected RemotePreferencesConfiguration remotePreferencesConfiguration;

	/** The i18n configuration. */
	@Lazy protected I18nConfiguration i18nConfiguration;

	/** The mail configuration. */
	@Lazy protected MailConfiguration mailConfiguration;

	/** The form provider configuration */
	@Lazy protected FormProviderConfiguration formProviderConfiguration;

	/** The form provider configuration */
	@Lazy protected SharePreferencesConfiguration sharePreferencesConfiguration;

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
	 * Gets the form provider configuration.
	 *
	 * @return the form provider configuration
	 */
	public FormProviderConfiguration getFormProviderConfiguration() {
		return this.formProviderConfiguration;
	}

	/**
	 * Sets the form provider configuration.
	 *
	 * @param formProviderConfiguration
	 *            the form provider configuration
	 */
	public void setFormProviderConfiguration(FormProviderConfiguration formProviderConfiguration) {
		this.formProviderConfiguration = formProviderConfiguration;
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

	/**
	 * Gets the share preferences configuration.
	 *
	 * @return {@link SharePreferencesConfiguration} the share preferences
	 *         configuration
	 */
	public SharePreferencesConfiguration getSharePreferencesConfiguration() {
		return this.sharePreferencesConfiguration;
	}

	/**
	 * Sets the share preferences configuration.
	 *
	 * @param sharePreferencesConfiguration
	 *            {@link SharePreferencesConfiguration} the new share
	 *            preferences configuration.
	 */
	public void setSharePreferencesConfiguration(SharePreferencesConfiguration sharePreferencesConfiguration) {
		this.sharePreferencesConfiguration = sharePreferencesConfiguration;
	}
}
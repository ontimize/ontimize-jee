package com.ontimize.jee.server.services.mail;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.security.PermissionsProviderSecured;
import com.ontimize.jee.common.services.user.UserInformation;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.server.configuration.OntimizeConfiguration;

/**
 * The Class RemoteApplicationPreferencesServiceImpl.
 */
@Service("MailService")
@Lazy(value = true)
public class MailServiceImpl implements IMailServiceServer, ApplicationContextAware {

	/** The implementation. */
	private IMailEngine implementation;

	/**
	 * Gets the implementation.
	 *
	 * @return the implementation
	 */
	protected IMailEngine getImplementation() {
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
		this.implementation = applicationContext.getBean(OntimizeConfiguration.class).getMailConfiguration().getEngine();
	}

	/**
	 * Gets the user login.
	 *
	 * @return the user login
	 */
	public String getUserLogin() {
		UserInformation principal = (UserInformation) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return principal.getLogin();
	}

	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public void sendMail(String from, List<String> to, List<String> cc, List<String> bcc, String subject, String body, Map<String, byte[]> attachments,
			Map<String, byte[]> inlineResources) throws OntimizeJEEException {
		try {
			this.getImplementation().sendMail(from, to, cc, bcc, subject, body, attachments, inlineResources);
		} catch (Exception error) {
			throw new OntimizeJEERuntimeException(error);
		}
	}

	@Secured({ PermissionsProviderSecured.SECURED })
	@Transactional(rollbackFor = Exception.class)
	@Override
	public void sendMailWithoutAttach(String from, List<String> to, String subject, String body) throws OntimizeJEEException {
		this.sendMail(from, to, null, null, subject, body, null, null);
	}

	@Override
	public void sendMailFromInputSteams(String from, List<String> to, List<String> cc, List<String> bcc, String subject, String body, Map<String, Path> attachments,
			Map<String, Path> inlineResources) throws OntimizeJEEException {
		try {
			this.getImplementation().sendMailFromInputSteams(from, to, cc, bcc, subject, body, attachments, inlineResources);
		} catch (Exception error) {
			throw new OntimizeJEERuntimeException(error);
		}
	}

	@Override
	public void updateSettings() throws OntimizeJEEException {
		this.getImplementation().updateSettings();
	}

}

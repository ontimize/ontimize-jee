package com.ontimize.jee.server.services.mail;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.exceptions.OntimizeJEEException;


/**
 * The Interface IMailEngine.
 */
public interface IMailEngine {


	/**
	 * Send mail.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param cc
	 *            the cc
	 * @param bcc
	 *            the bcc
	 * @param subject
	 *            the subject
	 * @param body
	 *            the body
	 * @param attachments
	 *            the attachments
	 * @param inlineResources
	 *            the inline resources
	 * @throws Exception
	 *             the exception
	 */
	void sendMail(String from, List<String> to, List<String> cc, List<String> bcc, String subject, String body, Map<String, byte[]> attachments,
			Map<String, byte[]> inlineResources) throws OntimizeJEEException;

	/**
	 * Send email with attachments and inlineresources from path, non loaded bytes.
	 *
	 * @param from
	 * @param to
	 * @param cc
	 * @param bcc
	 * @param subject
	 * @param body
	 * @param attachments
	 * @param inlineResources
	 * @throws Exception
	 */
	void sendMailFromInputSteams(String from, List<String> to, List<String> cc, List<String> bcc, String subject, String body, Map<String, Path> attachments,
			Map<String, Path> inlineResources) throws OntimizeJEEException;

	/**
	 * Update settings.
	 */
	void updateSettings() throws OntimizeJEEException;
}

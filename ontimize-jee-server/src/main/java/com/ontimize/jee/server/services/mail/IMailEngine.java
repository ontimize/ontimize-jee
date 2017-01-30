package com.ontimize.jee.server.services.mail;

import java.util.List;
import java.util.Map;


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
			Map<String, byte[]> inlineResources) throws Exception;

	/**
	 * Update settings.
	 */
	void updateSettings();
}

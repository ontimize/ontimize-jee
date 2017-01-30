package com.ontimize.jee.server.services.mail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * The Class SpringMailEngine.
 */
public class SpringMailEngine implements IMailEngine, InitializingBean {

	/** The mail sender. */
	private final JavaMailSenderImpl	mailSender;

	/** The configurator. */
	private IMailConfigurator			configurator;

	/**
	 * Instantiates a new spring mail engine.
	 */
	public SpringMailEngine() {
		super();
		this.mailSender = new JavaMailSenderImpl();
	}

	/**
	 * Send mail spring.
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
	 * @throws MessagingException
	 *             the messaging exception
	 */
	public void sendMailSpring(String from, List<String> to, List<String> cc, List<String> bcc, String subject, String body,
			Map<String, Resource> attachments, Map<String, Resource> inlineResources) throws MessagingException {
		MimeMessage message = this.mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, this.hasAttachments(inlineResources, attachments));
		// hay que establecer el texto antes de los adjuntos inline
		if (body != null) {
			helper.setText(body, this.isHtml(body));
		}
		if (to != null) {
			helper.setTo(to.toArray(new String[] {}));
		}
		if (cc != null) {
			helper.setCc(cc.toArray(new String[] {}));
		}
		if (bcc != null) {
			helper.setBcc(bcc.toArray(new String[] {}));
		}
		if (from != null) {
			helper.setFrom(from);
		}
		helper.setSubject(subject);
		if (attachments != null) {
			for (Entry<String, Resource> entry : attachments.entrySet()) {
				helper.addAttachment(entry.getKey(), entry.getValue());
			}
		}
		if (inlineResources != null) {
			for (Entry<String, Resource> entry : inlineResources.entrySet()) {
				helper.addInline(entry.getKey(), entry.getValue());
			}
		}
		this.mailSender.send(message);
	}

	/**
	 * Checks for attachments.
	 *
	 * @param inlineResources
	 *            the inline resources
	 * @param attachments
	 *            the attachments
	 * @return true, if successful
	 */
	private boolean hasAttachments(Map<String, Resource> inlineResources, Map<String, Resource> attachments) {
		return ((inlineResources != null) && !inlineResources.isEmpty()) || ((attachments != null) && !attachments.isEmpty());
	}

	/**
	 * Checks if is html.
	 *
	 * @param body
	 *            the body
	 * @return true, if is html
	 */
	protected boolean isHtml(String body) {
		return body.startsWith("<html") || body.startsWith("<Html") || body.startsWith("<HTML");
	}

	/**
	 * Gets the mail sender.
	 *
	 * @return the mail sender
	 */
	public JavaMailSenderImpl getMailSender() {
		return this.mailSender;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.mail.IMailEngine#updateSettings()
	 */
	@Override
	public void updateSettings() {
		if (this.configurator != null) {
			this.configurator.configure(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		this.updateSettings();
	}

	/**
	 * Sets the configurator.
	 *
	 * @param configurator
	 *            the new configurator
	 */
	public void setConfigurator(IMailConfigurator configurator) {
		this.configurator = configurator;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.mail.IMailEngine#sendMail(java.lang.String, java.util.List, java.util.List, java.util.List,
	 * java.lang.String, java.lang.String, java.util.Map, java.util.Map)
	 */
	@Override
	public void sendMail(String from, List<String> to, List<String> cc, List<String> bcc, String subject, String body,
			Map<String, byte[]> attachments, Map<String, byte[]> inlineResources) throws Exception {
		Map<String, Resource> inline = new HashMap<>();
		Map<String, Resource> attach = new HashMap<>();
		if (attachments != null) {
			for (Entry<String, byte[]> entry : attachments.entrySet()) {
				attach.put(entry.getKey(), new ByteArrayResource(entry.getValue()));
			}
		}
		if (inlineResources != null) {
			for (Entry<String, byte[]> entry : inlineResources.entrySet()) {
				inline.put(entry.getKey(), new ByteArrayResource(entry.getValue()));
			}
		}
		this.sendMailSpring(from, to, cc, bcc, subject, body, attach, inline);
	}
}

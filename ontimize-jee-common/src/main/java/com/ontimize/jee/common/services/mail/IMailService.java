package com.ontimize.jee.common.services.mail;

import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.exceptions.OntimizeJEEException;

/**
 * The Interface IMailService.
 */
public interface IMailService {

    /**
     * Send mail.
     * @param from the from
     * @param to the to
     * @param cc the cc
     * @param bcc the bcc
     * @param subject the subject
     * @param body the body
     * @param attachments the attachments
     * @param inlineResources the inline resources
     * @throws Exception the exception
     */
    void sendMail(String from, List<String> to, List<String> cc, List<String> bcc, String subject, String body,
            Map<String, byte[]> attachments,
            Map<String, byte[]> inlineResources) throws OntimizeJEEException;

    /**
     * Send mail.
     * @param from the from
     * @param to the to
     * @param subject the subject
     * @param body the body
     */
    void sendMailWithoutAttach(String from, List<String> to, String subject, String body) throws OntimizeJEEException;

}

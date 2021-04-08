package com.ontimize.jee.desktopclient.test.mail;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.common.services.mail.IMailService;
import com.ontimize.jee.desktopclient.test.AbstractIdentifiedOntimizeTest;

public class MailTest extends AbstractIdentifiedOntimizeTest {

    private static final Logger logger = LoggerFactory.getLogger(MailTest.class);

    public static void main(String[] args) {
        try {
            new MailTest().prepareTest(args);
        } catch (Exception error) {
            MailTest.logger.error(null, error);
        }
    }

    @Override
    protected void doTest() throws Exception {

        IMailService mailService = this.createService(IMailService.class, "/mailService");
        mailService.sendMailWithoutAttach("hola@caracola.es",
                Arrays.asList(new String[] { "joaquin.romero@imatia.com" }), "prueba", "esto es una prueba");
        Map<String, byte[]> attachemnts = new HashMap<>();
        attachemnts.put("opensc-pkcs11.dll", Files.readAllBytes(Paths.get("c:/opensc-pkcs11.dll")));
        mailService.sendMail("hola@caracola.es",
                Arrays.asList(new String[] { "joaquin.romero@imatia.com", "senen.dieguez@imatia.com" }), null, null,
                "prueba",
                "esto es una prueba", attachemnts, null);
    }

}

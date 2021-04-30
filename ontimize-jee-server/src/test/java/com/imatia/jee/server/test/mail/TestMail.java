package com.imatia.jee.server.test.mail;

import java.util.Arrays;

import com.ontimize.jee.common.spring.parser.FixedPropertyResolver;
import com.ontimize.jee.server.services.mail.SpringMailConfigurator;
import com.ontimize.jee.server.services.mail.SpringMailEngine;

public class TestMail {

    public static void main(String[] args) {
        SpringMailEngine engine = new SpringMailEngine();
        SpringMailConfigurator configurator = new SpringMailConfigurator();
        configurator.setEncodingResolver(new FixedPropertyResolver<>("UTF-8"));
        configurator.setHostResolver(new FixedPropertyResolver<>("smtp.server.com"));
        configurator.setPortResolver(new FixedPropertyResolver<>("25"));
        configurator.setProtocolResolver(new FixedPropertyResolver<>("smtp"));
        configurator.setPasswordResolver(new FixedPropertyResolver<>("password"));
        configurator.setUserResolver(new FixedPropertyResolver<>("user"));
        configurator.setJavaMailProperties(new FixedPropertyResolver<String>());
        engine.setConfigurator(configurator);

        engine.updateSettings();
        try {
            engine.sendMail("joaquin.romero", Arrays.asList(new String[] { "joaquin.romero@imatia.com" }), null, null,
                    "probando2", "cuermpo del mensaje", null, null);
            System.out.println("finalizado");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

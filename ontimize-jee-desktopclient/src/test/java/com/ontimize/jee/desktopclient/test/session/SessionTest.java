package com.ontimize.jee.desktopclient.test.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.common.services.session.ISessionService;
import com.ontimize.jee.common.services.user.IUserInformationService;
import com.ontimize.jee.common.services.user.UserInformation;
import com.ontimize.jee.desktopclient.test.AbstractIdentifiedOntimizeTest;

public class SessionTest extends AbstractIdentifiedOntimizeTest {

    private static final Logger logger = LoggerFactory.getLogger(SessionTest.class);

    public static void main(String[] args) {
        try {
            new SessionTest().prepareTest(args);
        } catch (Exception error) {
            SessionTest.logger.error(null, error);
        }
    }

    @Override
    protected void doTest() {

        IUserInformationService service = this.createService(IUserInformationService.class, "/userinformationservice");
        UserInformation userInformation = service.getUserInformation();
        System.out.println(userInformation);

        ISessionService serviceSession = this.createService(ISessionService.class, "/sessionService");
        serviceSession.closeSession();

        System.out.println("Finalizado");
    }

}

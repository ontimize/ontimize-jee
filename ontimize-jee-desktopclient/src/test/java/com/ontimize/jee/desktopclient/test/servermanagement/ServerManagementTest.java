package com.ontimize.jee.desktopclient.test.servermanagement;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.common.services.servermanagement.IServerManagementService;
import com.ontimize.jee.common.session.SessionDto;
import com.ontimize.jee.desktopclient.test.AbstractIdentifiedOntimizeTest;

public class ServerManagementTest extends AbstractIdentifiedOntimizeTest {

    private static final Logger logger = LoggerFactory.getLogger(ServerManagementTest.class);

    public static void main(String[] args) {
        try {
            new ServerManagementTest().prepareTest(args);
        } catch (Exception error) {
            ServerManagementTest.logger.error(null, error);
        }
    }

    @Override
    protected void doTest() throws Exception {

        IServerManagementService serverManagementService = this.createService(IServerManagementService.class,
                "/serverManagement");

        // String threadDump = serverManagementService.createThreadDump();
        // ServerManagementTest.logger.info(threadDump);
        //
        // InputStream heapDumpIs = serverManagementService.createHeapDump();
        // IOUtils.copy(heapDumpIs, Files.newOutputStream(Paths.get("c:/dump.out.bin")));
        //
        // List<String> availableDataSources = serverManagementService.getAvailableDataSources();
        // for (String ds : availableDataSources) {
        // System.out.println(ds);
        // }
        //
        // EntityResult er = serverManagementService.executeSql("select 2 from dual",
        // availableDataSources.get(0));
        // System.out.println(er);

        Collection<SessionDto> activeSessions = serverManagementService.getActiveSessions();
        System.out.println(String.format("Hay %d sesiones activas:", activeSessions.size()));
        for (SessionDto session : activeSessions) {
            System.out.println(session);
        }

        // serverManagementService.
        // InputStream openLogStream = serverManagementService.openLogStream();
        // IOUtils.copy(openLogStream, System.out);
    }

}

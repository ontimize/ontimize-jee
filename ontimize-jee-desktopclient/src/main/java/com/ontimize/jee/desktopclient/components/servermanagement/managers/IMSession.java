package com.ontimize.jee.desktopclient.components.servermanagement.managers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.annotation.FormComponent;
import com.ontimize.db.EntityResult;
import com.ontimize.gui.BasicInteractionManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.MessageDialog;
import com.ontimize.gui.button.Button;
import com.ontimize.gui.manager.IFormManager;
import com.ontimize.gui.table.Table;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.services.servermanagement.IServerManagementService;
import com.ontimize.jee.common.session.SessionDto;
import com.ontimize.jee.common.tools.EntityResultTools;
import com.ontimize.jee.desktopclient.spring.BeansFactory;

/**
 * The Class IMLiveLogConsole.
 */
public class IMSession extends BasicInteractionManager {

    private static final Logger logger = LoggerFactory.getLogger(IMSession.class);

    @FormComponent(attr = "RESULTS")
    protected Table tResults;

    @FormComponent(attr = "B_REQUEST")
    protected Button bRequest;

    private IServerManagementService serverManagement;

    public IMSession() {
        super();
    }

    @Override
    public void registerInteractionManager(Form f, IFormManager gf) {
        super.registerInteractionManager(f, gf);
        this.managedForm.setFormTitle("Session statistics");
        this.bRequest.addActionListener(new RefreshSessionStatisticsListener());
        this.serverManagement = BeansFactory.getBean(IServerManagementService.class);
    }

    protected Collection<SessionDto> getActiveSessions() throws Exception {
        this.ensureServerManagement();
        return this.serverManagement.getActiveSessions();
    }

    protected void ensureServerManagement() {
        if (this.serverManagement == null) {
            throw new OntimizeJEERuntimeException("No server management reference");
        }
    }

    public class RefreshSessionStatisticsListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        EntityResult res = new EntityResult();
                        EntityResultTools.initEntityResult(res,
                                Arrays.asList("USER", "IP", "BEGINDATE", "ENDDATE", "EXPIRATIONTIME"));
                        Collection<SessionDto> sessions = IMSession.this.getActiveSessions();
                        if (!sessions.isEmpty()) {

                            Iterator<SessionDto> sessionIter = sessions.iterator();
                            while (sessionIter.hasNext()) {
                                SessionDto nextSession = sessionIter.next();

                                String id = nextSession.getId();
                                long beginDate = nextSession.getCreationTime();
                                long lastAccessedTime = nextSession.getLastAccessedTime();
                                int maxInactiveIntervalInSeconds = nextSession.getMaxInactiveIntervalInSeconds();

                                Object attribute = nextSession.getAttribute("SPRING_SECURITY_SAVED_REQUEST");
                                res.addRecord(EntityResultTools.keysvalues("USER", id, "IP",
                                        attribute != null ? attribute : "", "BEGINDATE", new Date(beginDate), "ENDDATE",
                                        new Date(lastAccessedTime), "EXPIRATIONTIME", maxInactiveIntervalInSeconds));
                            }
                            IMSession.this.tResults.setValue(res);
                        }
                    } catch (Exception ex) {
                        IMSession.logger.trace(null, ex);
                        MessageDialog.showErrorMessage(IMSession.this.managedForm.getJDialog(), ex.getMessage());
                    }
                }
            }).start();
        }

    }

}

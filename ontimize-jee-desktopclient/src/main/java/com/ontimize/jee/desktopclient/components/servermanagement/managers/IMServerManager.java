package com.ontimize.jee.desktopclient.components.servermanagement.managers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.annotation.FormComponent;
import com.ontimize.gui.BasicInteractionManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.button.Button;
import com.ontimize.gui.manager.IFormManager;
import com.ontimize.jee.common.services.servermanagement.IServerManagementService;
import com.ontimize.jee.desktopclient.components.messaging.MessageManager;
import com.ontimize.jee.desktopclient.components.task.OSwingWorker;
import com.ontimize.jee.desktopclient.spring.BeansFactory;

/**
 * The Class IMMemory.
 */
public class IMServerManager extends BasicInteractionManager {

    private static final Logger logger = LoggerFactory.getLogger(IMServerManager.class);

    @FormComponent(attr = "reloadDaos")
    protected Button reloadDaos;

    protected IServerManagementService serverManagement;

    public IMServerManager() {
        super();
    }

    @Override
    public void registerInteractionManager(Form f, IFormManager gf) {
        super.registerInteractionManager(f, gf);
        this.managedForm.setFormTitle("Server management");
        this.reloadDaos.addActionListener(new ReloadDaosActionListener());
        this.serverManagement = BeansFactory.getBean(IServerManagementService.class);
    }

    public class ReloadDaosActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            new OSwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    IMServerManager.this.serverManagement.reloadDaos();
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        this.uget();
                    } catch (Exception ex) {
                        MessageManager.getMessageManager().showExceptionMessage(ex, IMServerManager.logger);
                    }
                }
            }.execute();
        }

    }

}

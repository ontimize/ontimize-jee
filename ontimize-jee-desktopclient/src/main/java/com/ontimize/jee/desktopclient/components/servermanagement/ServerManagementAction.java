package com.ontimize.jee.desktopclient.components.servermanagement;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.ontimize.jee.common.services.servermanagement.IServerManagementService;
import com.ontimize.jee.desktopclient.components.servermanagement.window.ServerManagementWindow;
import com.ontimize.jee.desktopclient.spring.BeansFactory;

public class ServerManagementAction extends AbstractAction {

    private static ServerManagementWindow managementWindow = null;

    IServerManagementService serverManagement = BeansFactory.getBean(IServerManagementService.class);

    @Override
    public void actionPerformed(ActionEvent e) {
        if (ServerManagementAction.isApplicationManagerWindowVisible()) {
            // Do nothing
        } else {
            // Login Window
            ServerManagementAction.setApplicationManagerWindowVisible(true);
        }
    }

    private static void setApplicationManagerWindowVisible(boolean visible) {
        ServerManagementAction.ensureApplicationMonitor();
        ServerManagementAction.managementWindow.setVisible(visible);
    }

    private static void ensureApplicationMonitor() {
        if (ServerManagementAction.managementWindow == null) {
            ServerManagementAction.managementWindow = new ServerManagementWindow();
        }
    }

    private static boolean isApplicationManagerWindowVisible() {
        ServerManagementAction.ensureApplicationMonitor();
        return ServerManagementAction.managementWindow.isVisible();
    }

}

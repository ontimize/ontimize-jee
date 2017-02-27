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
			// ServerManagementAction.setApplicationManagerWindowVisible(false);
		} else {
			// Login Window
			// new ServerManagementLogin();
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

	// private class ServerManagementLogin extends JDialog {
	// private final JPanel contentPanel = new JPanel();
	//
	// private JTextField tfUsername;
	// private JPasswordField pfPassword;
	//
	// public ServerManagementLogin() {
	// super();
	// this.setTitle("Autenticación");
	// this.setIconImage(ApplicationManager.getApplication().getFrame().getIconImage());
	// this.buildDialogContext();
	// this.setSize(300, 150);
	// this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	// this.setLocationRelativeTo(null);
	// this.setModal(true);
	// this.setResizable(false);
	// this.setVisible(true);
	// }
	//
	// private void buildDialogContext() {
	// this.getContentPane().setLayout(new BorderLayout());
	//
	// JPanel loginPanel = new JPanel(new GridBagLayout());
	// GridBagConstraints cs = new GridBagConstraints();
	//
	// cs.fill = GridBagConstraints.HORIZONTAL;
	//
	// JLabel lbUsername = new JLabel("Username: ");
	// cs.gridx = 0;
	// cs.gridy = 0;
	// cs.gridwidth = 1;
	// cs.weightx = 0.5;
	// loginPanel.add(lbUsername, cs);
	//
	// this.tfUsername = new JTextField(50);
	// cs.gridx = 1;
	// cs.gridy = 0;
	// cs.weightx = 1;
	// loginPanel.add(this.tfUsername, cs);
	//
	// JLabel lbPassword = new JLabel("Password: ");
	// cs.gridx = 0;
	// cs.gridy = 1;
	// cs.weightx = 0.5;
	// loginPanel.add(lbPassword, cs);
	//
	// this.pfPassword = new JPasswordField(50);
	// cs.gridx = 1;
	// cs.gridy = 1;
	// cs.weightx = 1;
	// loginPanel.add(this.pfPassword, cs);
	// loginPanel.setBorder(new LineBorder(Color.GRAY));
	//
	// JButton btnLogin = new JButton("Login");
	// btnLogin.addActionListener(new ActionListener() {
	// @Override
	// public void actionPerformed(ActionEvent e) {
	// try {
	// IServerManagementService service = BeansFactory.getBean(IServerManagementService.class);
	// boolean login = service.login(ServerManagementLogin.this.tfUsername.getText().trim(),
	// new String(ServerManagementLogin.this.pfPassword.getPassword()));
	// if (login) {
	// ServerManagementLogin.this.setVisible(false);
	// ServerManagementAction.setApplicationManagerWindowVisible(true);
	// } else {
	// throw new Exception();
	// }
	// } catch (Exception ex) {
	// JOptionPane.showMessageDialog(ServerManagementLogin.this, "INVALID_AUTHENTICATION", "Error", JOptionPane.ERROR_MESSAGE);
	// }
	// }
	// });
	// JButton btnCancel = new JButton("Cancel");
	// btnCancel.addActionListener(new ActionListener() {
	// @Override
	// public void actionPerformed(ActionEvent e) {
	// ServerManagementLogin.this.setVisible(false);
	// }
	// });
	//
	// JPanel buttonPanel = new JPanel();
	// buttonPanel.add(btnLogin);
	// buttonPanel.add(btnCancel);
	//
	// this.getContentPane().add(loginPanel, BorderLayout.CENTER);
	// this.getContentPane().add(buttonPanel, BorderLayout.PAGE_END);
	// }
	//
	// }

}

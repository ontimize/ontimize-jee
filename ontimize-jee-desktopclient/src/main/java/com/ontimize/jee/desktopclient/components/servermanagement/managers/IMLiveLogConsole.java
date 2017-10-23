package com.ontimize.jee.desktopclient.components.servermanagement.managers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.annotation.FormComponent;
import com.ontimize.gui.BasicInteractionManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.button.Button;
import com.ontimize.gui.field.MemoDataField;
import com.ontimize.gui.manager.IFormManager;
import com.ontimize.jee.common.services.servermanagement.IServerManagementService;
import com.ontimize.jee.desktopclient.spring.BeansFactory;

/**
 * The Class IMLiveLogConsole.
 */
public class IMLiveLogConsole extends BasicInteractionManager {

	private static final Logger	logger	= LoggerFactory.getLogger(IMLiveLogConsole.class);

	@FormComponent(attr = "CONSOLE")
	protected MemoDataField		console;

	@FormComponent(attr = "B_START")
	protected Button			bStart;
	@FormComponent(attr = "B_STOP")
	protected Button			bStop;

	public IMLiveLogConsole() {
		super();
	}

	@Override
	public void registerInteractionManager(Form f, IFormManager gf) {
		super.registerInteractionManager(f, gf);
		this.managedForm.setFormTitle("Live log console");
		new LiveLogConsoleListener(this.console, this.bStart, this.bStop);
	}

	public static class LiveLogConsoleListener implements ActionListener {

		private enum LiveLogConsoleActionCommands {
			start, stop
		};

		private LiveLogConsoleActionCommands	actionCommand;
		private final IServerManagementService	serverManagement;
		private final MemoDataField				console;

		public LiveLogConsoleListener(MemoDataField console, Button startButton, Button stopButton) {
			super();
			this.console = console;
			startButton.addActionListener(this);
			stopButton.addActionListener(this);
			this.serverManagement = BeansFactory.getBean(IServerManagementService.class);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			this.actionCommand = LiveLogConsoleActionCommands.valueOf(e.getActionCommand());
			new Thread(new Runnable() {

				@Override
				public void run() {
					// FIXME: Acceder al servicio
					switch (LiveLogConsoleListener.this.actionCommand) {
						case start:
							// Start live log
							LiveLogConsoleListener.this.console.setValue("START - Falta por hacer el servicio");
							// console.....
							break;
						case stop:
							// Stop live log
							LiveLogConsoleListener.this.console.setValue("STOP - Falta por hacer el servicio");
							break;
						default:
							break;
					}
				}
			}).start();
		}
	}

}

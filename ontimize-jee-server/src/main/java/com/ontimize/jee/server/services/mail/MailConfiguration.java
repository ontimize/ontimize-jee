package com.ontimize.jee.server.services.mail;

import com.ontimize.jee.common.tools.CheckingTools;

/**
 * The Class RemotePreferencesConfiguration.
 */
public class MailConfiguration {

	/** The engine. */
	private IMailEngine	engine;

	/**
	 * Gets the engine.
	 *
	 * @return the engine
	 */
	public IMailEngine getEngine() {
		CheckingTools.failIfNull(this.engine, "No email engine defined");
		return this.engine;
	}

	/**
	 * Sets the engine.
	 *
	 * @param engine
	 *            the engine
	 */
	public void setEngine(IMailEngine engine) {
		this.engine = engine;
	}

}

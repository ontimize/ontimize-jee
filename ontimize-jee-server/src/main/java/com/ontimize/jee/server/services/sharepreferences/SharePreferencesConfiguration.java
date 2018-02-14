package com.ontimize.jee.server.services.sharepreferences;

import com.ontimize.jee.common.services.sharepreferences.ISharePreferencesService;
import com.ontimize.jee.common.tools.CheckingTools;

public class SharePreferencesConfiguration {

	/** The engine. */
	private ISharePreferencesService engine;

	/**
	 * Gets the engine.
	 *
	 * @return the engine
	 */
	public ISharePreferencesService getEngine() {
		CheckingTools.failIfNull(this.engine, "No share preferences engine defined");
		return this.engine;
	}

	/**
	 * Sets the engine.
	 *
	 * @param engine
	 *            the engine
	 */
	public void setEngine(ISharePreferencesService engine) {
		this.engine = engine;
	}

}

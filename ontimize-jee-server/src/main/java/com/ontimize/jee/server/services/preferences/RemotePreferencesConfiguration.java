package com.ontimize.jee.server.services.preferences;

import com.ontimize.jee.common.tools.CheckingTools;

/**
 * The Class RemotePreferencesConfiguration.
 */
public class RemotePreferencesConfiguration {

    /** The engine. */
    private IRemoteApplicationPreferencesEngine engine;

    /**
     * Gets the engine.
     * @return the engine
     */
    public IRemoteApplicationPreferencesEngine getEngine() {
        CheckingTools.failIfNull(this.engine, "No remote preferences engine defined");
        return this.engine;
    }

    /**
     * Sets the engine.
     * @param engine the engine
     */
    public void setEngine(IRemoteApplicationPreferencesEngine engine) {
        this.engine = engine;
    }

}

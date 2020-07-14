package com.ontimize.jee.server.services.formprovider;

import com.ontimize.jee.common.services.formprovider.IFormProviderService;
import com.ontimize.jee.common.tools.CheckingTools;

/**
 * Class FormProviderConfiguration
 */
public class FormProviderConfiguration {

    /**
     * The FormProvider engine
     */
    private IFormProviderService engine;

    /**
     * Gets the engine.
     * @return The {@link IFormProviderService} engine
     */
    public IFormProviderService getEngine() {
        CheckingTools.failIfNull(this.engine, "No XML form provider engine defined.");
        return this.engine;
    }

    /**
     * Sets the engine
     * @param engine The {@link IFormProviderService} engine
     */
    public void setEngine(IFormProviderService engine) {
        this.engine = engine;
    }

}

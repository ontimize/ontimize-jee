package com.ontimize.jee.server.services.i18n;

import com.ontimize.jee.common.services.i18n.II18nService;
import com.ontimize.jee.common.tools.CheckingTools;

/**
 * The Class I18nConfiguration.
 */
public class I18nConfiguration {

    /** The engine. */
    private II18nService engine;

    /**
     * Gets the engine.
     * @return the engine
     */
    public II18nService getEngine() {
        CheckingTools.failIfNull(this.engine, "No i18n engine defined");
        return this.engine;
    }

    /**
     * Sets the engine.
     * @param engine the engine
     */
    public void setEngine(II18nService engine) {
        this.engine = engine;
    }

}

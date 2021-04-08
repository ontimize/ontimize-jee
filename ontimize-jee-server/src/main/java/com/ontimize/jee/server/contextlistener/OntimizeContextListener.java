package com.ontimize.jee.server.contextlistener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bootstrap listener to start up and shut down settings.
 *
 * @author <a href="user@email.com">Author</a>
 */
public class OntimizeContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(OntimizeContextListener.class);

    /**
     * Initialize the root web application context.
     * @param event event
     */
    @Override
    public void contextInitialized(final ServletContextEvent event) {
        // Do nothing
    }

    /**
     * Close the root web application context.
     * @param event event
     */
    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        // Do nothing
    }

}

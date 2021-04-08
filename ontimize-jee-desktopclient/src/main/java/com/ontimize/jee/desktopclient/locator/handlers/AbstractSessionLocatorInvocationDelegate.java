/*
 *
 */
package com.ontimize.jee.desktopclient.locator.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import com.ontimize.db.AdvancedEntity;
import com.ontimize.db.DirectSQLQueryEntity;
import com.ontimize.db.Entity;
import com.ontimize.gui.ClientWatch;
import com.ontimize.jee.common.services.session.ISessionService;
import com.ontimize.jee.common.tools.proxy.AbstractInvocationDelegate;
import com.ontimize.jee.desktopclient.spring.BeansFactory;
import com.ontimize.locator.SecureEntityReferenceLocator;

/**
 * The Class SessionLocatorInvocationDelegate.
 */
public abstract class AbstractSessionLocatorInvocationDelegate extends AbstractInvocationDelegate
        implements SecureEntityReferenceLocator {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSessionLocatorInvocationDelegate.class);

    @Override
    public int startSession(String user, String password, ClientWatch client) throws Exception {
        return 1;
    }

    @Override
    public int getSessionId() throws Exception {
        return 1;
    }

    @Override
    public void endSession(int id) throws Exception {
        try {
            BeansFactory.getBean(ISessionService.class).closeSession();
        } catch (NoSuchBeanDefinitionException error) {
            AbstractSessionLocatorInvocationDelegate.logger.info("No session service found", error);
        }
    }

    @Override
    public boolean hasSession(String user, int id) throws Exception {
        return true;
    }

    /**
     * Get ontimize entity interfaces.
     * @return the ontimize entity interfaces
     */
    protected Class<?>[] getOntimizeEntityInterfaces() {
        return new Class<?>[] { Entity.class, AdvancedEntity.class, DirectSQLQueryEntity.class };
    }

}

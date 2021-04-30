package com.ontimize.jee.desktopclient.hessian;

import java.lang.reflect.Proxy;

import com.ontimize.jee.common.db.Entity;
import com.ontimize.jee.desktopclient.locator.handlers.AbstractSessionLocatorInvocationDelegate;

/**
 * The Class HessianLocatorInvocationDelegate.
 */
public class HessianSessionLocatorInvocationDelegate extends AbstractSessionLocatorInvocationDelegate {

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.locator.SecureEntityReferenceLocator#getEntityReference(java.lang.String,
     * java.lang.String, int)
     */
    @Override
    public Entity getEntityReference(String entity, String user, int sessionId) throws Exception {
        return this.getEntityReference(entity);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.locator.EntityReferenceLocator#getEntityReference(java.lang.String)
     */
    @Override
    public Entity getEntityReference(String entityName) throws Exception {
        return (Entity) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                this.getOntimizeEntityInterfaces(), new HessianEntityInvocationHandler(entityName));
    }

}

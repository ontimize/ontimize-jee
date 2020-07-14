package com.ontimize.jee.common.tools.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AbstractInvocationDelegate.
 */
public class AbstractInvocationDelegate implements IInvocationDelegate {

    private static final Logger logger = LoggerFactory.getLogger(AbstractInvocationDelegate.class);

    /**
     * Instantiates a new abstract invocation delegate.
     */
    public AbstractInvocationDelegate() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.ontimize.jee.desktopclient.locator.proxyhandler.IInvocationDelegate#invoke(java.lang.Object,
     * java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(this, args);
        } catch (InvocationTargetException e) {
            logger.trace(null, e);
            throw e.getCause();
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new InvalidDelegateException(e);
        }
    }

}

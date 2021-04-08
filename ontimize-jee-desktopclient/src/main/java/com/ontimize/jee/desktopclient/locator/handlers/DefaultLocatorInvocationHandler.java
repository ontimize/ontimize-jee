/**
 *
 */
package com.ontimize.jee.desktopclient.locator.handlers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.common.tools.proxy.IInvocationDelegate;
import com.ontimize.jee.common.tools.proxy.InvalidDelegateException;

/**
 * The Class DefaultLocatorInvocationHandler.
 */
public class DefaultLocatorInvocationHandler implements InvocationHandler {

    /** the logger. */
    private static final Logger logger = LoggerFactory.getLogger(DefaultLocatorInvocationHandler.class);

    /** The invocation delegates. */
    protected List<IInvocationDelegate> invocationDelegates;

    /**
     * The constructor.
     */
    public DefaultLocatorInvocationHandler() {
        super();
        this.invocationDelegates = new ArrayList<>();
    }

    /**
     * Sets the invocation delegates.
     * @param invocationDelegates the invocation delegates
     */
    public void setInvocationDelegates(List<IInvocationDelegate> invocationDelegates) {
        this.invocationDelegates = invocationDelegates;
    }

    /**
     * Gets the invocation delegates.
     * @return the invocation delegates
     */
    public List<IInvocationDelegate> getInvocationDelegates() {
        return this.invocationDelegates;
    }

    /**
     * Adds the delegate.
     * @param delegate the delegate
     */
    public void addDelegate(IInvocationDelegate delegate) {
        this.invocationDelegates.add(delegate);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method,
     * java.lang.Object[])
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        DefaultLocatorInvocationHandler.logger.debug("#### Calling method --> {}", method.getName());
        for (IInvocationDelegate delegate : this.invocationDelegates) {
            try {
                return delegate.invoke(proxy, method, args);
            } catch (InvalidDelegateException e) {
                DefaultLocatorInvocationHandler.logger.trace(null, e);
            }
        }
        throw new InvalidDelegateException(method.getName());
    }

}

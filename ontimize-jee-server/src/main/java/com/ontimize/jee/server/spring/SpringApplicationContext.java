package com.ontimize.jee.server.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * The Class SpringApplicationContext provides support for acces Spring context outside Spring.
 */
@Component
@Lazy(value = true)
public class SpringApplicationContext implements ApplicationContextAware {

    /** The context. */
    private static ApplicationContext context;

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext
     * (org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(final ApplicationContext context) throws BeansException {
        SpringApplicationContext.context = context;
    }

    public static ApplicationContext getContext() {
        return SpringApplicationContext.context;
    }

}

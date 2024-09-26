package com.ontimize.jee.server.spring;

import jakarta.servlet.ServletContext;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.web.context.ServletContextAware;

/**
 * A factory for creating ServletContext objects.
 */
public class ServletContextFactory implements FactoryBean<ServletContext>, ServletContextAware {

    /** The servlet context. */
    private ServletContext servletContext;

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    @Override
    public ServletContext getObject() throws Exception {
        return this.servletContext;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    @Override
    public Class<?> getObjectType() {
        return ServletContext.class;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.
     * ServletContext)
     */
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

}

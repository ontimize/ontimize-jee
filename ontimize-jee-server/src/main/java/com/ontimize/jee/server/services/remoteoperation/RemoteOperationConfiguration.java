package com.ontimize.jee.server.services.remoteoperation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * The Class RemoteOperationConfiguration.
 */
public class RemoteOperationConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(RemoteOperationConfiguration.class);

    /** The remote operation manager. */
    protected IRemoteOperationEngine remoteOperationEngine;

    /** The max parallel threads. */
    protected int maxParallelThreads;

    @Autowired
    private ApplicationContext applicationContext;

    // private ConfigurableListableBeanFactory beanFactory;

    /**
     * Gets the remote opereation manager.
     * @return the remote opereation manager
     */
    public IRemoteOperationEngine getRemoteOpereationEngine() {
        if (this.remoteOperationEngine == null) {
            try {
                this.remoteOperationEngine = this.applicationContext.getBean(IRemoteOperationEngine.class);
            } catch (NoSuchBeanDefinitionException error) {
                RemoteOperationConfiguration.logger.debug("no bean definition found, using default", error);
                BeanDefinition definition = new RootBeanDefinition(DefaultRemoteOperationEngine.class);
                DefaultListableBeanFactory registry = (DefaultListableBeanFactory) ((ConfigurableApplicationContext) this.applicationContext)
                    .getBeanFactory();
                // lo correcto para obtener el registry seria implementar BeanDefinitionRegistryPostProcessor pero
                // no se invocan los metodos

                registry.registerBeanDefinition("remoteOperationEngine", definition);
            }
            this.remoteOperationEngine = this.applicationContext.getBean("remoteOperationEngine",
                    IRemoteOperationEngine.class);
            this.remoteOperationEngine.setMaxRunningThreadNumber(this.getMaxParallelThreads());
        }
        return this.remoteOperationEngine;
    }

    /**
     * Gets the max parallel threads.
     * @return the max parallel threads
     */
    public int getMaxParallelThreads() {
        return this.maxParallelThreads;
    }

    /**
     * Sets the max parallel threads.
     * @param maxParallelThreads the new max parallel threads
     */
    public void setMaxParallelThreads(int maxParallelThreads) {
        this.maxParallelThreads = maxParallelThreads;
    }

}

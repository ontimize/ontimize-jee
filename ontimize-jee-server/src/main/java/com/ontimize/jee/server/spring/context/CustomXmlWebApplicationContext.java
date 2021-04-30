package com.ontimize.jee.server.spring.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class CustomXmlWebApplicationContext extends XmlWebApplicationContext {

    private static final Logger log = LoggerFactory.getLogger(CustomXmlWebApplicationContext.class);

    public CustomXmlWebApplicationContext() {
        super();
    }

    @Override
    public void addBeanFactoryPostProcessor(final BeanFactoryPostProcessor beanFactoryPostProcessor) {
        super.addBeanFactoryPostProcessor(beanFactoryPostProcessor);
    }

    @Override
    public void refresh() throws BeansException, IllegalStateException {
        super.refresh();
    }

    @Override
    protected void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) {
        super.postProcessBeanFactory(beanFactory);
    }

    @Override
    public void setConfigLocation(final String location) {
        super.setConfigLocation(location);
    }

    @Override
    public void setConfigLocations(final String[] locations) {
        super.setConfigLocations(locations);
    }

    @Override
    protected DefaultListableBeanFactory createBeanFactory() {
        return new DefaultListableBeanFactory(this.getInternalParentBeanFactory()) {

            @Override
            public void addBeanPostProcessor(final BeanPostProcessor beanPostProcessor) {
                // TODO Auto-generated method stub
                super.addBeanPostProcessor(beanPostProcessor);
            }

            @Override
            public void registerBeanDefinition(final String beanName, final BeanDefinition beanDefinition)
                    throws BeanDefinitionStoreException {
                // TODO Auto-generated method stub
                if (beanName.equalsIgnoreCase(AnnotationConfigUtils.PERSISTENCE_ANNOTATION_PROCESSOR_BEAN_NAME)) {
                    CustomXmlWebApplicationContext.log
                        .warn("Skipping registration of bean ("
                                + AnnotationConfigUtils.PERSISTENCE_ANNOTATION_PROCESSOR_BEAN_NAME + "): "
                                + beanDefinition.getBeanClassName());
                } else {
                    super.registerBeanDefinition(beanName, beanDefinition);
                }
            }

        };
    }

}

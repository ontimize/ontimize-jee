package com.ontimize.jee.server.spring.namespace;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class OntimizeServerSpringHandler extends NamespaceHandlerSupport {

    public OntimizeServerSpringHandler() {
        super();
    }

    @Override
    public void init() {
        this.registerBeanDefinitionParser("ontimize-configuration", new OntimizeConfigurationBeanDefinitionParser());
        this.registerBeanDefinitionParser("cors", new CorsBeanDefinitionParser());
        this.registerBeanDefinitionParser("fixed-property", new FixedPropertyBeanDefinitionParser());
        this.registerBeanDefinitionParser("database-property", new DatabasePropertyBeanDefinitionParser());
        this.registerBeanDefinitionParser("ref-property", new PropertyBeanDefinitionParser());
        this.registerBeanDefinitionParser("remoteoperation-provider", new RemoteOperationBeanDefinitionParser());
    }

}

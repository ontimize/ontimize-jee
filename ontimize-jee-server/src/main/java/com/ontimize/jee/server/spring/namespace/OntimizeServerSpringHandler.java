package com.ontimize.jee.server.spring.namespace;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class OntimizeServerSpringHandler extends NamespaceHandlerSupport {

	public OntimizeServerSpringHandler() {
		super();
	}

	@Override
	public void init() {
		this.registerBeanDefinitionParser("ontimizeConfiguration", new OntimizeConfigurationBeanDefinitionParser());
		this.registerBeanDefinitionParser("cors", new CorsBeanDefinitionParser());
		this.registerBeanDefinitionParser("fixedProperty", new FixedPropertyBeanDefinitionParser());
		this.registerBeanDefinitionParser("databaseProperty", new DatabasePropertyBeanDefinitionParser());
		this.registerBeanDefinitionParser("remoteoperation-provider", new RemoteOperationBeanDefinitionParser());
	}

}

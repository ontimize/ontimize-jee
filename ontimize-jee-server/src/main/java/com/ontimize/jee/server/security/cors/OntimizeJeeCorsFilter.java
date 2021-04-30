package com.ontimize.jee.server.security.cors;

import java.util.LinkedHashMap;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.server.spring.namespace.CorsBeanDefinitionParser;

public class OntimizeJeeCorsFilter extends CorsFilter implements ApplicationContextAware {

    public OntimizeJeeCorsFilter() {
        super(new UrlBasedCorsConfigurationSource());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        LinkedHashMap<String, CorsConfiguration> bean = applicationContext
            .getBean(CorsBeanDefinitionParser.CORS_CONFIGURATION_BEAN_NAME, LinkedHashMap.class);
        UrlBasedCorsConfigurationSource configSource = (UrlBasedCorsConfigurationSource) ReflectionTools
            .getFieldValue(this, "configSource");
        configSource.setAlwaysUseFullPath(true);
        configSource.setCorsConfigurations(bean);
    }

}

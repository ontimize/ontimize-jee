package com.ontimize.jee.server.spring.namespace;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.w3c.dom.Element;

public class CorsBeanDefinitionParser implements BeanDefinitionParser {
	public static final String			CORS_CONFIGURATION_BEAN_NAME	= "ontimizeJeeCorsConfigurations";

	private static final List<String>	DEFAULT_ALLOWED_ORIGINS			= Arrays.asList("*");

	private static final List<String>	DEFAULT_ALLOWED_METHODS			= Arrays.asList(HttpMethod.GET.name(), HttpMethod.HEAD.name(), HttpMethod.POST.name());

	private static final List<String>	DEFAULT_ALLOWED_HEADERS			= Arrays.asList("*");

	private static final boolean		DEFAULT_ALLOW_CREDENTIALS		= true;

	private static final long			DEFAULT_MAX_AGE					= 1600;

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {

		Map<String, CorsConfiguration> corsConfigurations = new LinkedHashMap<String, CorsConfiguration>();
		List<Element> mappings = DomUtils.getChildElementsByTagName(element, "mapping");

		if (mappings.isEmpty()) {
			CorsConfiguration config = new CorsConfiguration();
			config.setAllowedOrigins(CorsBeanDefinitionParser.DEFAULT_ALLOWED_ORIGINS);
			config.setAllowedMethods(CorsBeanDefinitionParser.DEFAULT_ALLOWED_METHODS);
			config.setAllowedHeaders(CorsBeanDefinitionParser.DEFAULT_ALLOWED_HEADERS);
			config.setAllowCredentials(CorsBeanDefinitionParser.DEFAULT_ALLOW_CREDENTIALS);
			config.setMaxAge(CorsBeanDefinitionParser.DEFAULT_MAX_AGE);
			corsConfigurations.put("/**", config);
		} else {
			for (Element mapping : mappings) {
				CorsConfiguration config = new CorsConfiguration();
				if (mapping.hasAttribute("allowed-origins")) {
					String[] allowedOrigins = StringUtils.tokenizeToStringArray(mapping.getAttribute("allowed-origins"), ",");
					config.setAllowedOrigins(Arrays.asList(allowedOrigins));
				} else {
					config.setAllowedOrigins(CorsBeanDefinitionParser.DEFAULT_ALLOWED_ORIGINS);
				}
				if (mapping.hasAttribute("allowed-methods")) {
					String[] allowedMethods = StringUtils.tokenizeToStringArray(mapping.getAttribute("allowed-methods"), ",");
					config.setAllowedMethods(Arrays.asList(allowedMethods));
				} else {
					config.setAllowedMethods(CorsBeanDefinitionParser.DEFAULT_ALLOWED_METHODS);
				}
				if (mapping.hasAttribute("allowed-headers")) {
					String[] allowedHeaders = StringUtils.tokenizeToStringArray(mapping.getAttribute("allowed-headers"), ",");
					config.setAllowedHeaders(Arrays.asList(allowedHeaders));
				} else {
					config.setAllowedHeaders(CorsBeanDefinitionParser.DEFAULT_ALLOWED_HEADERS);
				}
				if (mapping.hasAttribute("exposed-headers")) {
					String[] exposedHeaders = StringUtils.tokenizeToStringArray(mapping.getAttribute("exposed-headers"), ",");
					config.setExposedHeaders(Arrays.asList(exposedHeaders));
				}
				if (mapping.hasAttribute("allow-credentials")) {
					config.setAllowCredentials(Boolean.parseBoolean(mapping.getAttribute("allow-credentials")));
				} else {
					config.setAllowCredentials(CorsBeanDefinitionParser.DEFAULT_ALLOW_CREDENTIALS);
				}
				if (mapping.hasAttribute("max-age")) {
					config.setMaxAge(Long.parseLong(mapping.getAttribute("max-age")));
				} else {
					config.setMaxAge(CorsBeanDefinitionParser.DEFAULT_MAX_AGE);
				}
				corsConfigurations.put(mapping.getAttribute("path"), config);
			}
		}

		CorsBeanDefinitionParser.registerCorsConfigurations(corsConfigurations, parserContext, parserContext.extractSource(element));
		return null;
	}

	/**
	 * Registers a {@code Map<String, CorsConfiguration>} (mapped {@code CorsConfiguration}s) under a well-known name unless already registered. The bean definition may be updated
	 * if a non-null CORS configuration is provided.
	 *
	 * @return a RuntimeBeanReference to this {@code Map<String, CorsConfiguration>} instance
	 */
	public static RuntimeBeanReference registerCorsConfigurations(Map<String, CorsConfiguration> corsConfigurations, ParserContext parserContext, Object source) {
		if (!parserContext.getRegistry().containsBeanDefinition(CorsBeanDefinitionParser.CORS_CONFIGURATION_BEAN_NAME)) {
			RootBeanDefinition corsConfigurationsDef = new RootBeanDefinition(LinkedHashMap.class);
			corsConfigurationsDef.setSource(source);
			corsConfigurationsDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			if (corsConfigurations != null) {
				corsConfigurationsDef.getConstructorArgumentValues().addIndexedArgumentValue(0, corsConfigurations);
			}
			parserContext.getReaderContext().getRegistry().registerBeanDefinition(CorsBeanDefinitionParser.CORS_CONFIGURATION_BEAN_NAME, corsConfigurationsDef);
			parserContext.registerComponent(new BeanComponentDefinition(corsConfigurationsDef, CorsBeanDefinitionParser.CORS_CONFIGURATION_BEAN_NAME));
		} else if (corsConfigurations != null) {
			BeanDefinition corsConfigurationsDef = parserContext.getRegistry().getBeanDefinition(CorsBeanDefinitionParser.CORS_CONFIGURATION_BEAN_NAME);
			corsConfigurationsDef.getConstructorArgumentValues().addIndexedArgumentValue(0, corsConfigurations);
		}
		return new RuntimeBeanReference(CorsBeanDefinitionParser.CORS_CONFIGURATION_BEAN_NAME);
	}

}
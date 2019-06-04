package com.ontimize.jee.server.spring;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.PropertySource;

import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

public final class SpringTools {

	private static final Logger logger = LoggerFactory.getLogger(SpringTools.class);

	private SpringTools() {
		super();
	}

	public static <T> T getTargetObject(Object proxy, Class<T> targetClass) {
		if (AopUtils.isJdkDynamicProxy(proxy)) {
			try {
				return (T) ((Advised) proxy).getTargetSource().getTarget();
			} catch (Exception error) {
				throw new OntimizeJEERuntimeException(error);
			}
		}
		return (T) proxy; // expected to be cglib proxy then, which is simply a specialized class
	}

	public static Map<String, Object> getAllEnvironmentProperties(EnvironmentCapable context) {
		Map<String, Object> map = new HashMap<>();
		for (Iterator<?> it = ((ConfigurableEnvironment) context.getEnvironment()).getPropertySources().iterator(); it.hasNext();) {
			PropertySource<?> propertySource = (PropertySource<?>) it.next();
			if (propertySource instanceof EnumerablePropertySource) {
				// In order to solve override of properties
				for (String key : ((EnumerablePropertySource) propertySource).getPropertyNames()) {
					try {
						String value = context.getEnvironment().getProperty(key);
						map.put(key, value);
					} catch (IllegalArgumentException error) { // This key includes an unresolveable placeholder, and will not be part of the map that we output
						// do nothing
						SpringTools.logger.debug("unresolveable placeholder", error);
					}
				}
			}
		}
		return map;

	}

}

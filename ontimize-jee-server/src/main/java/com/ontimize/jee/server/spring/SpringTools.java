package com.ontimize.jee.server.spring;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

public final class SpringTools {

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
		} else {
			return (T) proxy; // expected to be cglib proxy then, which is simply a specialized class
		}
	}

}

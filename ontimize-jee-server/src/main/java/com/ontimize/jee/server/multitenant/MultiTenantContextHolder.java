package com.ontimize.jee.server.multitenant;

public class MultiTenantContextHolder {

	private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

	public static void setTenant(final String tenantId) {
		contextHolder.set(tenantId);
	}

	public static String getTenant() {
		return contextHolder.get();
	}

	public static void clear() {
		contextHolder.remove();
	}
}

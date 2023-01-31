package com.ontimize.jee.server.multitenant;

public class MultiTenantContextHolder {

	private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

	public static void setTenant(String tenantId) {
		contextHolder.set(tenantId);
	}

	public static String getTenant() {
		return (String) contextHolder.get();
	}

	public static void clear() {
		contextHolder.remove();
	}
}

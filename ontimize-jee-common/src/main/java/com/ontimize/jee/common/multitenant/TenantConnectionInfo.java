package com.ontimize.jee.common.multitenant;

public class TenantConnectionInfo {
	private String tenantId;

	private String tenantName;

	private String driverClass;

	private String jdbcUrl;

	private String username;

	private String password;

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(final String tenantId) {
		this.tenantId = tenantId;
	}

	public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(final String tenantName) {
		this.tenantName = tenantName;
	}

	public String getDriverClass() {
		return driverClass;
	}

	public void setDriverClass(final String driverClass) {
		this.driverClass = driverClass;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public void setJdbcUrl(final String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}
}

package com.ontimize.jee.server.multitenant.store;

public class TenantConnectionInfo {
	private String tenantId;

	private String driverClass;

	private String jdbcUrl;

	private String username;

	private String password;

	public TenantConnectionInfo() {

	}

	public TenantConnectionInfo(String tenantId,String driverClass, String jdbcUrl, String username, String password) {
		this.tenantId = tenantId;
		this.driverClass = driverClass;
		this.jdbcUrl = jdbcUrl;
		this.username = username;
		this.password = password;
	}

	public String getDriverClass() {
		return driverClass;
	}

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}

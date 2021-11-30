package com.ontimize.jee.server.security.keycloak.dto.application;

import java.util.List;

public class ApplicationRoles {

	private String applicationId;
	private List<String> roles;
	private List<String> assignedRoles;
	private String applicationName;

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> assignedRoles) {
		this.roles = assignedRoles;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public List<String> getAssignedRoles() {
		return assignedRoles;
	}

	public void setAssignedRoles(List<String> assignedRoles) {
		this.assignedRoles = assignedRoles;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
}

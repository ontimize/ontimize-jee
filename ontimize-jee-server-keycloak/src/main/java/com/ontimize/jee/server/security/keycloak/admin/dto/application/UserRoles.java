package com.ontimize.jee.server.security.keycloak.admin.dto.application;

public class UserRoles {

	private String user;
	private String[] assignedRoles;

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String[] getAssignedRoles() {
		return assignedRoles;
	}

	public void setAssignedRoles(String[] assignedRoles) {
		this.assignedRoles = assignedRoles;
	}

}

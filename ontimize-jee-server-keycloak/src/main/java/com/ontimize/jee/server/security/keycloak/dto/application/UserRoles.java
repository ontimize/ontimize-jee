package com.ontimize.jee.server.security.keycloak.dto.application;

public class UserRoles {

	private String user_;
	private String[] assignedRoles;

	public String getUser_() {
		return user_;
	}

	public void setUser_(String user_) {
		this.user_ = user_;
	}

	public String[] getAssignedRoles() {
		return assignedRoles;
	}

	public void setAssignedRoles(String[] assignedRoles) {
		this.assignedRoles = assignedRoles;
	}

}

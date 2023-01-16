package com.ontimize.jee.common.security;

import com.ontimize.jee.common.util.calendar.TimePeriod;

/**
 * Class to define the application permissions.
 *
 * @author Imatia Innovation
 */
public class ApplicationPermission extends AbstractClientPermission {

	public ApplicationPermission(String permissionName, boolean restricted) {
		this.name = permissionName;
		this.restricted = restricted;
	}

	public ApplicationPermission(String permissionName, boolean restricted, TimePeriod period) {
		this.name = permissionName;
		this.restricted = restricted;
		this.period = period;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof ApplicationPermission) {
			return this.name.equals(((ApplicationPermission) o).getPermissionName());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

}

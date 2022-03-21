package com.ontimize.jee.common.security;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.common.util.calendar.TimePeriod;

public class AbstractClientPermission implements RestrictedClientPermission {

	private static final Logger logger = LoggerFactory.getLogger(AbstractClientPermission.class);

	protected boolean restricted = false;

	protected String name = null;

	protected String attr = null;

	protected TimePeriod period = null;

	@Override
	public String getAttribute() {
		return this.attr;
	}

	@Override
	public String getPermissionName() {
		return this.name;
	}

	@Override
	public boolean isRestricted() {
		return this.restricted;
	}

	@Override
	public void setRestricted(boolean restricted) {
		this.restricted = restricted;
	}

	/**
	 * Returns true if the permission is a period restricted permission
	 * @return true if is a period restricted permission.
	 */
	@Override
	public boolean isPeriodRestricted() {
		return this.period != null;
	}

	@Override
	public boolean hasPermission() {
		// If time is in period and restricted and period are enabled, it won't
		// permission
		if (this.restricted) {
			if (this.period == null) {
				return !this.restricted;
			}

			return !this.period.timeIsInPeriod(this.getTime());

		}
		if (this.period == null) {
			return !this.restricted;
		}

		return this.period.timeIsInPeriod(this.getTime());

	}

	@Override
	public void setAttribute(String attr) {
		this.attr = attr;
	}

	@Override
	public TimePeriod getPeriod() {
		return this.period;
	}

	@Override
	public void setPeriod(TimePeriod period) {
		this.period = period;
	}

	@Override
	public String toString() {
		String aux = this.getClass().toString() + " " + this.attr;
		StringBuilder sb = new StringBuilder(aux);
		sb.append(" ");
		sb.append(this.name);
		sb.append(" " + this.period);
		return sb.toString();
	}


	public long getTime() {
		try {
			Class<?> clazz = Class.forName("com.ontimize.jee.common.gui.ApplicationManager");
			Method method = clazz.getMethod("getTime");
			return (Long) method.invoke(null);
		} catch (Exception e) {
			AbstractClientPermission.logger.error(e.getMessage(), e);
		}

		return System.currentTimeMillis();
	}

}

package com.ontimize.jee.server.security.authorization;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ontimize.jee.server.configuration.OntimizeConfiguration;
import com.ontimize.jee.server.security.ISecurityRoleInformationService;

/**
 * An improved implementation of AbstractRoleProvider, with autorefresh for each .
 */
@Component("RoleProvider")
public class AutoRefreshRoleProvider extends AbstractRoleProvider {

	/** The CONSTANT logger */
	private static final Logger	logger	= LoggerFactory.getLogger(AutoRefreshRoleProvider.class);

	/**
	 * The application context (autowired).
	 */
	@Autowired
	private ApplicationContext	context;

	/**
	 * The delay to wait between refresh invocations;
	 */
	protected long				delay;

	/**
	 * Instantiates a new role permissions.
	 *
	 * @param roles
	 *            the roles
	 */
	public AutoRefreshRoleProvider() {
		this(0);
	}

	/**
	 * Improved constructor with delay configuration.
	 *
	 * @param delayMs
	 */
	public AutoRefreshRoleProvider(long delayMs) {
		super();
		this.delay = delayMs;
	}

	@Override
	protected ISecurityRoleInformationService getRoleService() {
		return this.context.getBean(OntimizeConfiguration.class).getSecurityConfiguration().getRoleInformationService();
	}

	/**
	 * Scheduled task to auto "refresh" roles configuration
	 */
	@Scheduled(fixedDelay = 10000)
	public void scheduleFixedDelayTask() {
		if (this.delay > 0) {
			try {
				Thread.sleep(this.delay);
			} catch (InterruptedException err) {
				AutoRefreshRoleProvider.logger.trace("Wait ignored");
			}
		}
		AutoRefreshRoleProvider.logger.trace("Role refresh scheduled task starts.");
		List<String> refreshRoles = new ArrayList<>(this.roles.keySet());
		for (String roleName : refreshRoles) {
			this.loadRoleAndCache(roleName);
		}
		AutoRefreshRoleProvider.logger.trace("Role refresh scheduled task ends.");
	}
}
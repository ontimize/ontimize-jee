package com.ontimize.jee.server.security.authorization;

import org.springframework.context.ApplicationContext;

import com.ontimize.jee.server.configuration.OntimizeConfiguration;
import com.ontimize.jee.server.security.ISecurityRoleInformationService;

/**
 * The default implementation of AbstractRoleProvider.
 */
public class DefaultRoleProvider extends AbstractRoleProvider {

	/**
	 * The application context.
	 */
	private ApplicationContext			context;

	/**
	 * Instantiates a new role permissions.
	 *
	 * @param roles
	 *            the roles
	 */
	public DefaultRoleProvider(ApplicationContext context) {
		super();
		this.context = context;
	}

	@Override
	protected ISecurityRoleInformationService getRoleService() {
		return this.context.getBean(OntimizeConfiguration.class).getSecurityConfiguration().getRoleInformationService();
	}
}

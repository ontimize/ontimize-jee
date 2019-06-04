package com.ontimize.jee.server.security.authorization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.FilterInvocation;

import com.ontimize.jee.common.exceptions.PermissionValidationException;
import com.ontimize.jee.common.naming.I18NNaming;
import com.ontimize.jee.common.security.PermissionsProviderSecured;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.server.configuration.OntimizeConfiguration;

/**
 * The Class PermissionsProviderAccessDecisionVoter.
 *
 * @author <a href="user@email.com">Author</a>
 */
public class OntimizeAccessDecisionVoter implements AccessDecisionVoter<Object>, ApplicationContextAware {

	/** The Constant logger. */
	private static final Logger		logger	= LoggerFactory.getLogger(OntimizeAccessDecisionVoter.class);

	/** The default voter. */
	private RoleVoter				defaultVoter;

	/** The authorizator. */
	private ISecurityAuthorizator	authorizator;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean supports(final ConfigAttribute configattribute) {
		return true;// & ((this.defaultVoter == null) || ((this.defaultVoter != null) && this.defaultVoter.supports(configattribute)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean supports(final Class<?> arg0) {
		if ((this.defaultVoter == null) || !this.defaultVoter.supports(arg0)) {
			return arg0.equals(ReflectiveMethodInvocation.class);
		}
		return true;
	}

	/**
	 * Vote default.
	 *
	 * @param arg0
	 *            arg0
	 * @param arg1
	 *            arg1
	 * @param arg2
	 *            arg2
	 * @return the int
	 */
	private int voteDefault(final Authentication arg0, final Object arg1, final Collection<ConfigAttribute> arg2) {
		if (this.defaultVoter == null) {
			return AccessDecisionVoter.ACCESS_ABSTAIN;
		} else {
			return this.defaultVoter.vote(arg0, arg1, arg2);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int vote(final Authentication authentication, final Object object, final Collection<ConfigAttribute> attributes) {
		if (object instanceof ReflectiveMethodInvocation) {
			final ReflectiveMethodInvocation rmi = (ReflectiveMethodInvocation) object;
			final Class<?> apiClass = rmi.getMethod().getDeclaringClass();
			Secured findAnnotation = AnnotationUtils.findAnnotation(rmi.getMethod(), Secured.class);
			boolean containsCustomRole = false;
			if (findAnnotation != null) {
				List<String> rolesToFilter = Arrays.asList(findAnnotation.value());
				containsCustomRole = rolesToFilter.contains(PermissionsProviderSecured.SECURED);
			}
			if ((findAnnotation == null) || containsCustomRole) {
				final String methodName = rmi.getMethod().getName();
				final String property = apiClass.getCanonicalName() + "/" + methodName;
				final List<String> roles = new ArrayList<>();
				for (final GrantedAuthority ga : authentication.getAuthorities()) {
					roles.add(ga.getAuthority());
				}
				try {
					if (this.authorizator.hasPermission(property, roles)) {
						return AccessDecisionVoter.ACCESS_GRANTED;
					}
				} catch (final PermissionValidationException e) {
					// ups it doesn't validate the 0 checkers I passed to it
					OntimizeAccessDecisionVoter.logger.trace(null, e);
				}
				OntimizeAccessDecisionVoter.logger.error("This roles:" + roles.toString() + " have not access to:" + property);
			}
			return this.voteDefault(authentication, object, attributes);
		} else if (object instanceof FilterInvocation) {
			return AccessDecisionVoter.ACCESS_GRANTED;
		}
		return this.voteDefault(authentication, object, attributes);
	}

	/**
	 * Sets the default voter.
	 *
	 * @param defaultVoter
	 *            the defaultVoter to set
	 */
	public void setDefaultVoter(final RoleVoter defaultVoter) {
		this.defaultVoter = defaultVoter;
	}

	/**
	 * Gets the default voter.
	 *
	 * @return the defaultVoter
	 */
	public RoleVoter getDefaultVoter() {
		return this.defaultVoter;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		OntimizeConfiguration ontimizeConfiguration = applicationContext.getBean(OntimizeConfiguration.class);
		CheckingTools.failIfNull(ontimizeConfiguration, I18NNaming.E_NO_ONTIMIZE_CONFIGURATION_DEFINED);
		CheckingTools.failIfNull(ontimizeConfiguration.getSecurityConfiguration(), I18NNaming.E_NO_ONTIMIZE_CONFIGURATION_DEFINED);
		this.authorizator = ontimizeConfiguration.getSecurityConfiguration().getAuthorizator();
		CheckingTools.failIfNull(this.authorizator, I18NNaming.E_NO_ONTIMIZE_SECURITY_AUTHORIZATOR_DEFINED);
	}

}

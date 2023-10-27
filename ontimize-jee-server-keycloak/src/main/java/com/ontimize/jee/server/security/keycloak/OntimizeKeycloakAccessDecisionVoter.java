package com.ontimize.jee.server.security.keycloak;

import java.util.Collection;

import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import com.ontimize.jee.server.security.authorization.OntimizeAccessDecisionVoter;

/**
 * The Class PermissionsProviderAccessDecisionVoter.
 *
 * @author <a href="user@email.com">Author</a>
 */
public class OntimizeKeycloakAccessDecisionVoter extends OntimizeAccessDecisionVoter {

	/**
	 * Vote default.
	 * 
	 * @param arg0 arg0
	 * @param arg1 arg1
	 * @param arg2 arg2
	 * @return the int
	 */
	private int voteDefault(final Authentication arg0, final Object arg1, final Collection<ConfigAttribute> arg2) {
		if (this.getDefaultVoter() == null) {
			return AccessDecisionVoter.ACCESS_ABSTAIN;
		}
		return this.getDefaultVoter().vote(arg0, arg1, arg2);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int vote(final Authentication authentication, final Object object,
			final Collection<ConfigAttribute> attributes) {
		if (object instanceof ReflectiveMethodInvocation) {
			return super.vote(authentication, object, attributes);
		}
		return this.voteDefault(authentication, object, attributes);
	}
}

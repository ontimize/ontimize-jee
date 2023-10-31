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
	 * {@inheritDoc}
	 */
	@Override
	public int vote(final Authentication authentication, final Object object,
			final Collection<ConfigAttribute> attributes) {
		if (object instanceof ReflectiveMethodInvocation) {
			return super.vote(authentication, object, attributes);
		}
		if (this.getDefaultVoter() == null) {
			return AccessDecisionVoter.ACCESS_ABSTAIN;
		}
		return this.getDefaultVoter().vote(authentication, object, attributes);
	}
}

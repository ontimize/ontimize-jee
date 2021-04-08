package com.ontimize.jee.server.security;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.userdetails.UserCache;

import com.ontimize.jee.common.cache.AbstractGenericCache;
import com.ontimize.jee.server.configuration.OntimizeConfiguration;
import com.ontimize.jee.server.security.authorization.ISecurityAuthorizator;

public final class SecurityTools {

    private static final Logger logger = LoggerFactory.getLogger(SecurityTools.class);

    private SecurityTools() {
        super();
    }

    /**
     * Invalidate the caches form authorization and autentication managers for role reload TODO perhaps
     * in a complete distributed environment someone should create some workaround to synchronize nodes
     */
    public static void invalidateSecurityManager(ApplicationContext applicationContext) {
        try {
            OntimizeConfiguration ontimizeConfiguration = applicationContext.getBean(OntimizeConfiguration.class);
            ISecurityAuthorizator authorizator = ontimizeConfiguration.getSecurityConfiguration().getAuthorizator();
            authorizator.invalidateCache();

            Map<String, AbstractUserDetailsAuthenticationProvider> authenticators = BeanFactoryUtils
                .beansOfTypeIncludingAncestors(applicationContext,
                        AbstractUserDetailsAuthenticationProvider.class);
            for (Entry<String, AbstractUserDetailsAuthenticationProvider> authenticator : authenticators.entrySet()) {
                UserCache userCache = authenticator.getValue().getUserCache();
                if (userCache instanceof AbstractGenericCache) {
                    ((AbstractGenericCache<?, ?>) userCache).invalidateCache();
                }
            }
        } catch (Exception ex) {
            SecurityTools.logger.error("Error invalidating roles", ex);
        }
    }

}

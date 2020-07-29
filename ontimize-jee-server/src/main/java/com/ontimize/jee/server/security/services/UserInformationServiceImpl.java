/**
 * UserServerInformationImpl.java 17/07/2013
 *
 *
 *
 */
package com.ontimize.jee.server.security.services;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ontimize.jee.common.security.PermissionsProviderSecured;
import com.ontimize.jee.common.services.user.IUserInformationService;
import com.ontimize.jee.common.services.user.UserInformation;

/**
 * Implementación del servicio.
 *
 * @author <a href=""></a>
 */
public class UserInformationServiceImpl implements IUserInformationService {

    /**
     *
     * {@inheritDoc}
     */
    @Secured({ PermissionsProviderSecured.SECURED })
    @Override
    public UserInformation getUserInformation() {
        UserInformation principal = (UserInformation) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();
        return principal;
    }

}

/**
 * ApplicationHttp403ForbiddenEntryPoint.java 18-abr-2013
 *
 *
 *
 */
package com.ontimize.jee.server.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;

import com.ontimize.jee.common.tools.ConcatTools;

/**
 * The Class ApplicationHttp403ForbiddenEntryPoint.
 *
 * @author <a href="user@email.com">Author</a>
 */
public class ApplicationHttp403ForbiddenEntryPoint extends Http403ForbiddenEntryPoint {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ApplicationHttp403ForbiddenEntryPoint.class);

    private String validApplicationRoles;

    /**
     * {@inheritDoc}
     */
    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response,
            final AuthenticationException arg2) throws IOException {
        ApplicationHttp403ForbiddenEntryPoint.logger.error(null, arg2);
        if (ApplicationHttp403ForbiddenEntryPoint.logger.isDebugEnabled()) {
            ApplicationHttp403ForbiddenEntryPoint.logger
                .debug("Pre-authenticated entry point called. Rejecting access");
        }
        final HttpServletResponse httpResponse = response;
        httpResponse.sendError(403, "Access Denied");
        ApplicationHttp403ForbiddenEntryPoint.logger.error("Filtro de preautenticacion de spring da error 403: ", arg2);
        if (this.validApplicationRoles == null) {
            ApplicationHttp403ForbiddenEntryPoint.logger.debug("Not valid application roles defined to check here");
        } else {
            final String[] split = this.validApplicationRoles.split(",");
            for (final String s : split) {
                if (s != null) {
                    if (request.isUserInRole(s)) {
                        ApplicationHttp403ForbiddenEntryPoint.logger.info(ConcatTools.concat("usuario '",
                                request.getUserPrincipal().getName(), "' tiene el rol '", s, "'"));
                    }
                }
            }
        }
    }

    /**
     * Obtiene valid application roles.
     * @return valid application roles
     */
    public String getValidApplicationRoles() {
        return this.validApplicationRoles;
    }

    /**
     * Establece valid application roles.
     * @param validApplicationRoles nuevo valid application roles
     */
    public void setValidApplicationRoles(final String validApplicationRoles) {
        this.validApplicationRoles = validApplicationRoles;
    }

}

/**
 * ServletFilter.java 29/08/2013
 *
 *
 *
 */
package com.ontimize.jee.server.requestfilter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href=""></a>
 *
 */
public class OntimizeExceptionTranslatorFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(OntimizeExceptionTranslatorFilter.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            try {
                filterChain.doFilter(request, response);
            } catch (Exception e) {
                OntimizeExceptionTranslatorFilter.logger.error(null, e);
                throw new ServletException("esto es un error");
            }
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // do nothing
    }

}

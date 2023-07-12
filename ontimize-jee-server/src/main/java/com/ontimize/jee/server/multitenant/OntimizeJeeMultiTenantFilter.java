package com.ontimize.jee.server.multitenant;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;

import com.ontimize.jee.server.requestfilter.OntimizePathMatcher;

public class OntimizeJeeMultiTenantFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(OntimizeJeeMultiTenantFilter.class);

	@Autowired
	@Qualifier("pathMatcherIgnorePaths")
	private OntimizePathMatcher pathMatcherIgnorePaths;

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain)
			throws IOException, ServletException {

		if (request instanceof HttpServletRequest) {
			final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
			final String tenantId = httpServletRequest.getHeader("X-Tenant");

			if (HttpMethod.OPTIONS.name().equals(httpServletRequest.getMethod()) || this.pathMatcherIgnorePaths.matches(httpServletRequest)) {
				if (tenantId != null) {
					MultiTenantContextHolder.setTenant(tenantId);
				}

				this.doFilter(filterChain, request, response);
			} else if (tenantId == null) {
				final HttpServletResponse httpServletResponse = (HttpServletResponse) response;
				httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No tenant provided");
			} else {
				MultiTenantContextHolder.setTenant(tenantId);

				this.doFilter(filterChain, request, response);
			}
		} else {
			this.doFilter(filterChain, request, response);
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

	private void doFilter(final FilterChain filterChain, final ServletRequest request, final ServletResponse response) {
		try {
			filterChain.doFilter(request, response);
		} catch (Exception e) {
			OntimizeJeeMultiTenantFilter.logger.error(null, e);
		}
	}
}

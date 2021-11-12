package com.ontimize.jee.server.multitenant;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OntimizeJeeMultiTenantFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(OntimizeJeeMultiTenantFilter.class);

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain)
			throws IOException, ServletException {

		if (request instanceof HttpServletRequest) {
			final String tenantId = ((HttpServletRequest) request).getHeader("xtenant");
			if (tenantId != null) {
				MultiTenantContextHolder.setTenant(tenantId);
			}
		}

		try {
			filterChain.doFilter(request, response);
		} catch (Exception e) {
			OntimizeJeeMultiTenantFilter.logger.error(null, e);
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
	}
}

/**
 * OntimizeRequestHeadersServletFilter.java 20-oct-2014
 *
 * Copyright 2014 Imatia.
 */
package com.ontimize.jee.server.requestfilter;

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

import com.ontimize.jee.server.spring.SpringApplicationContext;

/**
 * The Class OntimizeRequestHeadersServletFilter.
 *
 * @author sergio.padin
 */
public class OntimizeRequestHeadersServletFilter implements Filter {

	/** The logger. */
	private static final Logger logger = LoggerFactory.getLogger(OntimizeRequestHeadersServletFilter.class);

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
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			final RequestHeaderProvider requestHeaderProvider = new RequestHeaderProvider((HttpServletRequest) request);

			ServiceContext serviceContext = ServiceContextHolder.getInstance().getServiceContext();
			serviceContext.reset();

			final OntimizeRequestHeaderProcessorFilters filters = SpringApplicationContext.getContext().getBean(OntimizeRequestHeaderProcessorFilters.class);

			if ((filters != null) && (filters.getHeaderProcessors() != null)) {
				for (IOntimizeRequestHeaderProcessor headerProcessor : filters.getHeaderProcessors()) {
					headerProcessor.processHeader(requestHeaderProvider);
				}
			}

			long time = System.currentTimeMillis();
			try {
				filterChain.doFilter(request, response);
			} catch (Exception e) {
				OntimizeRequestHeadersServletFilter.logger.error(null, e);
			} finally {
				time = System.currentTimeMillis() - time;
				OntimizeRequestHeadersServletFilter.logger.debug("[APP TIME] Request headers Filter Processing time: " + time + " ms");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(final FilterConfig arg0) throws ServletException {
		// do nothing
	}

}

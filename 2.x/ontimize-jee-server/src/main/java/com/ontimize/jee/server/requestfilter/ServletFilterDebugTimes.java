/**
 * ServletFilter.java 29/08/2013
 *
 *
 *
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

/**
 *
 * @author <a href=""></a>
 *
 */
public class ServletFilterDebugTimes implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(ServletFilterDebugTimes.class);

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
			long time = System.currentTimeMillis();
			try {
				filterChain.doFilter(request, response);
			} catch (Exception e) {
				ServletFilterDebugTimes.logger.error(null, e);
			} finally {
				time = System.currentTimeMillis() - time;
				ServletFilterDebugTimes.logger.debug("[FULL TIME] Processing time: " + time + " ms");
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

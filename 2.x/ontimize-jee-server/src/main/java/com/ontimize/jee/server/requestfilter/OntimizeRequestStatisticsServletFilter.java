/**
 * ServletFilter.java 29/08/2013
 *
 *
 *
 */
package com.ontimize.jee.server.requestfilter;

import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.caucho.services.server.ServiceContext;
import com.ontimize.jee.common.services.servermanagement.IServerManagementService;

/**
 *
 * @author <a href=""></a>
 *
 */
public class OntimizeRequestStatisticsServletFilter implements Filter {

	static Logger								logger		= LoggerFactory.getLogger(OntimizeRequestStatisticsServletFilter.class);

	private static final int					DELAY		= 5000;
	private final ScheduledThreadPoolExecutor	executor	= new ScheduledThreadPoolExecutor(1);

	private IServerManagementService			service;

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
		request.getServletContext().getSessionCookieConfig();
		if (request instanceof HttpServletRequest) {
			boolean success = false;
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			long time = System.currentTimeMillis();

			// initialize the service context
			ServiceContext.begin(request, response, httpRequest.getPathInfo(), "0");
			try {
				filterChain.doFilter(request, response);
				success = true;
			} catch (Exception e) {
				OntimizeRequestStatisticsServletFilter.logger.error(null, e);
				throw e;
			} finally {
				time = System.currentTimeMillis() - time;

				String method = (String) ServiceContext.getContext().getHeader("Method");
				Object headerValues = ServiceContext.getContext().getHeader("Values");
				String serviceException = null;
				if (!success) {
					serviceException = (String) ServiceContext.getContext().getHeader("ServiceException");
				}
				Principal userPrincipal = httpRequest.getUserPrincipal();

				this.executor.schedule(new RequestStatisticsRunnable(httpRequest.getPathInfo(), method, headerValues, userPrincipal != null ? userPrincipal.getName() : null,
				        new Date(), time, serviceException), OntimizeRequestStatisticsServletFilter.DELAY, TimeUnit.MILLISECONDS);

				OntimizeRequestStatisticsServletFilter.logger.debug("[FULL TIME] Processing time: " + time + " ms");
			}
		}
	}

	protected class RequestStatisticsRunnable implements Runnable {

		private final String	serviceName;
		private final String	methodName;
		private final Object	params;
		private final String	user;
		private final String	exception;
		private final long		timeExecution;
		private final Date		date;

		public RequestStatisticsRunnable(String serviceName, String methodName, Object params, String user, Date date, long timeExecution, String exception) {
			this.serviceName = serviceName;
			this.methodName = methodName;
			this.params = params;
			this.user = user;
			this.date = date;
			this.timeExecution = timeExecution;
			this.exception = exception;
		}

		@Override
		public void run() {
			try {
				OntimizeRequestStatisticsServletFilter.this.service.setServiceStatistics(this.serviceName, this.methodName, this.params, this.user, this.date, this.timeExecution,
				        this.exception);
			} catch (Exception ex) {
				OntimizeRequestStatisticsServletFilter.logger.error(null, ex);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(final FilterConfig arg0) throws ServletException {
		// do nothing
		this.service = WebApplicationContextUtils.getWebApplicationContext(arg0.getServletContext()).getBean(IServerManagementService.class);
	}

}

/**
 * ServletFilter.java 29/08/2013
 *
 *
 *
 */
package com.ontimize.jee.server.requestfilter;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 *
 * @author <a href=""></a>
 *
 */
public class OntimizeServletFilter implements Filter {

	public static final String	LOCALE_COUNTRY	= "locale-country";
	public static final String	LOCALE_LANGUAGE	= "locale-language";
	public static final String	LOCALE			= "locale";
	static Logger				logger			= LoggerFactory.getLogger(OntimizeServletFilter.class);

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
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			String localeLanguage = httpRequest.getHeader(OntimizeServletFilter.LOCALE_LANGUAGE);
			String localeCountry = httpRequest.getHeader(OntimizeServletFilter.LOCALE_COUNTRY);
			localeLanguage = (localeLanguage == null) || "".equals(localeLanguage) ? "EN" : localeLanguage.toUpperCase();
			localeCountry = (localeCountry == null) || "".equals(localeCountry) ? "GB" : localeCountry.toUpperCase();
			request.setAttribute(OntimizeServletFilter.LOCALE_LANGUAGE, localeLanguage);
			request.setAttribute(OntimizeServletFilter.LOCALE_COUNTRY, localeCountry);
			request.setAttribute(OntimizeServletFilter.LOCALE, new Locale(localeLanguage, localeCountry));
			ServletRequestAttributes attributes = new ServletRequestAttributes(httpRequest);
			RequestContextHolder.setRequestAttributes(attributes, true);
			long time = System.currentTimeMillis();
			try {
				filterChain.doFilter(request, response);
				RequestContextHolder.getRequestAttributes().getAttribute("REQUEST_STATISTICS", RequestAttributes.SCOPE_REQUEST);
			} catch (Exception e) {
				OntimizeServletFilter.logger.error(null, e);
			} finally {
				time = System.currentTimeMillis() - time;
				HttpSession session = httpRequest.getSession(false);
				try {
					OntimizeServletFilter.logger.debug("[APP TIME] Processing session request {} time: {} ms",
							session == null ? SecurityContextHolder.getContext().getAuthentication().getPrincipal() : session.getId(), time);
				} catch (Exception ex) {
					OntimizeServletFilter.logger.debug("[APP TIME] Processing request time: {} ms", time, ex);
				}
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

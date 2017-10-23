/**
 * RequestHeaderProvider.java 20-oct-2014
 *
 * Copyright 2014 Imatia.
 */
package com.ontimize.jee.server.requestfilter;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

/**
 * The Class RequestHeaderProvider.
 *
 * @author <a href="luis.garcia@imatia.com">Luis Garcia</a>
 */
public class RequestHeaderProvider {

	/** The request. */
	private final HttpServletRequest request;

	/**
	 * Instantiates a new request header provider.
	 *
	 * @param request
	 *            the request
	 */
	public RequestHeaderProvider(final HttpServletRequest request) {
		this.request = request;
	}

	/**
	 * Gets the header.
	 *
	 * @param name
	 *            the name
	 * @return the header
	 */
	public String getHeader(final String name) {
		return this.request.getHeader(name);
	}

	/**
	 * Gets the headers.
	 *
	 * @param name
	 *            the name
	 * @return the headers
	 */
	public Enumeration<String> getHeaders(final String name) {
		return this.request.getHeaders(name);
	}

	/**
	 * Gets the header names.
	 *
	 * @return the header names
	 */
	public Enumeration<String> getHeaderNames() {
		return this.request.getHeaderNames();
	}

}

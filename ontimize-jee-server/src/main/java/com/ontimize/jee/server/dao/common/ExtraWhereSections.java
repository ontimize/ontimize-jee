/**
 *
 */
package com.ontimize.jee.server.dao.common;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class ExtraWhereSection.
 */
public class ExtraWhereSections {

	/** The Constant EXTRA_WHERE_SECTIONS. */
	public static final String			EXTRA_WHERE_SECTIONS	= "EXTRA_WHERE_SECTIONS";

	/** The key values. */
	private Map<String, WhereSection>	whereSections			= new HashMap<>();

	/**
	 * Instantiates a new extra where section.
	 */
	public ExtraWhereSections() {
		// do nothing
	}

	/**
	 * Instantiates a new extra where section.
	 *
	 * @param whereSections
	 *            the where sections
	 */
	public ExtraWhereSections(Map<String, WhereSection> whereSections) {
		super();
		this.whereSections = whereSections;
	}

	/**
	 * Gets the where sections.
	 *
	 * @return the where sections
	 */
	public Map<String, WhereSection> getWhereSections() {
		return this.whereSections;
	}

	/**
	 * Sets the where sections.
	 *
	 * @param whereSections
	 *            the where sections
	 */
	public void setWhereSections(Map<String, WhereSection> whereSections) {
		this.whereSections = whereSections;
	}
}

/**
 *
 */
package com.ontimize.jee.common.session;

import java.util.Collection;

/**
 * The Interface HeaderAttributesProvider.
 */
public interface HeaderAttributesProvider {

	/**
	 * Gets the header attributes.
	 *
	 * @param previousHeaderAttributes
	 *            the previous header attributes
	 * @return the header attributes
	 */
	Collection<HeaderAttribute> getHeaderAttributes(Collection<HeaderAttribute> previousHeaderAttributes);

	/**
	 * Gets the header attributes.
	 *
	 * @return the header attributes
	 */
	Collection<HeaderAttribute> getHeaderAttributes();

	/**
	 * Adds the header attribute.
	 *
	 * @param attribute
	 *            the attribute
	 */
	void addHeaderAttribute(HeaderAttribute attribute);

	/**
	 * Removes the header attribute.
	 *
	 * @param attribute
	 *            the attribute
	 * @return true, if successful
	 */
	boolean removeHeaderAttribute(HeaderAttribute attribute);
}

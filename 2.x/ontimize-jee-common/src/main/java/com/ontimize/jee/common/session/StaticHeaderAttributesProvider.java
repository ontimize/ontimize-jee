/**
 *
 */
package com.ontimize.jee.common.session;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The Interface HeaderAttributesProvider.
 */
public class StaticHeaderAttributesProvider extends HeaderAttributesProviderChain {

	/** The static attributes. */
	private final Collection<HeaderAttribute> staticAttributes;

	/**
	 * Instantiates a new static header attributes provider.
	 *
	 * @param childProvider
	 *            the child provider
	 */
	public StaticHeaderAttributesProvider(HeaderAttributesProvider childProvider) {
		super(childProvider);
		this.staticAttributes = new ArrayList<>();
	}

	/**
	 * Instantiates a new static header attributes provider.
	 */
	public StaticHeaderAttributesProvider() {
		super();
		this.staticAttributes = new ArrayList<>();
	}

	/**
	 * @see com.ontimize.jee.common.session.HeaderAttributesProviderChain#doGetHeaderAttributes(java.util.Collection)
	 */
	@Override
	protected Collection<HeaderAttribute> doGetHeaderAttributes(Collection<HeaderAttribute> previousHeaderAttributes) {
		Collection<HeaderAttribute> res = previousHeaderAttributes == null ? new ArrayList<HeaderAttribute>() : previousHeaderAttributes;
		if (this.staticAttributes != null) {
			res.addAll(this.staticAttributes);
		}
		return res;
	}

	@Override
	public void addHeaderAttribute(HeaderAttribute attribute) {
		this.staticAttributes.add(attribute);
	}

	@Override
	public boolean removeHeaderAttribute(HeaderAttribute attribute) {
		return this.staticAttributes.remove(attribute);
	}
}

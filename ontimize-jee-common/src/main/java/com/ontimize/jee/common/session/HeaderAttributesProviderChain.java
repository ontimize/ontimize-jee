/**
 *
 */
package com.ontimize.jee.common.session;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The Interface HeaderAttributesProvider.
 */
public abstract class HeaderAttributesProviderChain implements HeaderAttributesProvider {

    private HeaderAttributesProvider childProvider;

    public HeaderAttributesProviderChain(HeaderAttributesProvider childProvider) {
        this.childProvider = childProvider;
    }

    public HeaderAttributesProviderChain() {
        // do nothing
    }

    /**
     * Gets the header attributes.
     * @return the header attributes
     */
    @Override
    public final Collection<HeaderAttribute> getHeaderAttributes(Collection<HeaderAttribute> previousHeaderAttributes) {
        Collection<HeaderAttribute> res = previousHeaderAttributes == null ? new ArrayList<HeaderAttribute>()
                : previousHeaderAttributes;
        Collection<HeaderAttribute> current = this.doGetHeaderAttributes(res);
        if (this.childProvider != null) {
            current = this.childProvider.getHeaderAttributes(current);
        }
        return current;
    }

    /**
     * Gets the header attributes.
     * @return the header attributes
     */
    @Override
    public final Collection<HeaderAttribute> getHeaderAttributes() {
        return this.getHeaderAttributes(null);
    }

    protected abstract Collection<HeaderAttribute> doGetHeaderAttributes(
            Collection<HeaderAttribute> previousHeaderAttributes);

}

/**
 *
 */
package com.ontimize.jee.server.dao.jpa.ql.literalprocessors.common;

import java.util.List;

import com.ontimize.jee.server.dao.jpa.ql.ValueToQLLiteralProcessor;

/**
 * The Class ValueToQLLiteralProcessorChainBuilder.
 */
public final class ValueToQLLiteralProcessorChainBuilder {

    /**
     * Instantiates a new value to ql literal processor chain builder.
     */
    private ValueToQLLiteralProcessorChainBuilder() {
        // do nothing
    }

    /**
     * Builds the chain.
     * 
     * @param chains
     *            the chains
     * @return the value to ql literal processor
     */
    public static ValueToQLLiteralProcessor buildChain(List<AbstractValueToQLLiteralProcessor> chains) {
        if ((chains != null) && (chains.size() > 0)) {
            AbstractValueToQLLiteralProcessor result = chains.get(0);
            AbstractValueToQLLiteralProcessor next = result;
            for (int i = 1; i < chains.size(); i++) {
                next.setNext(chains.get(i));
                next = chains.get(i);
            }
            return result;
        }
        return null;
    }

}

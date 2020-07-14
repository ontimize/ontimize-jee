/**
 *
 */
package com.ontimize.jee.server.dao.jpa.ql.literalprocessors.common;

import java.util.List;

import com.ontimize.db.SQLStatementBuilder;
import com.ontimize.jee.server.dao.jpa.ql.ValueToQLLiteralProcessor;

/**
 * The Class DefaultValueListToQLLiteralProcessor.
 */
public class DefaultValueListToQLLiteralProcessor extends AbstractValueToQLLiteralProcessor {

    /** The processor delegate. */
    private ValueToQLLiteralProcessor processorDelegate;

    /**
     * Value to literal.
     * @param value the value
     * @return the string
     * @see com.ontimize.jee.server.dao.jpa.ql.literalprocessors.common.AbstractValueToQLLiteralProcessor#valueToLiteral(java.lang.Object)
     */
    @Override
    protected String valueToLiteral(Object value) {
        ValueToQLLiteralProcessor delegate = this.processorDelegate;
        if (delegate == null) {
            delegate = new DefaultValueToQLLiteralProcessor();
        }
        StringBuilder sbValue = new StringBuilder();
        for (int vs = 0; vs < ((List<?>) value).size(); vs++) {
            Object toTranslate = ((List<?>) value).get(vs);
            String delegatedString;
            if (toTranslate instanceof List) {
                delegatedString = this.valueToLiteral(toTranslate);
            } else {
                delegatedString = delegate.toQLLiteral(toTranslate);
            }
            sbValue = sbValue.append(delegatedString);
            if (vs < (((List<?>) value).size() - 1)) {
                sbValue.append(SQLStatementBuilder.COMMA);
            }
        }
        return sbValue.toString();
    }

    /**
     * Can process.
     * @param value the value
     * @return true, if successful
     * @see com.ontimize.jee.server.dao.jpa.ql.literalprocessors.common.AbstractValueToQLLiteralProcessor#canProcess(java.lang.Object)
     */
    @Override
    protected boolean canProcess(Object value) {
        return value instanceof List;
    }

    /**
     * Sets the processor delegate.
     * @param processorDelegate the new processor delegate
     */
    public void setProcessorDelegate(ValueToQLLiteralProcessor processorDelegate) {
        this.processorDelegate = processorDelegate;
    }

}

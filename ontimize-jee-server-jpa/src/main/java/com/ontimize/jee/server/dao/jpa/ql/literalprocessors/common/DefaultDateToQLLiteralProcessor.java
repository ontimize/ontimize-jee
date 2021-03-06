/**
 *
 */
package com.ontimize.jee.server.dao.jpa.ql.literalprocessors.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The Class DefaultDateToQLLiteralProcessor.
 */
public class DefaultDateToQLLiteralProcessor extends AbstractValueToQLLiteralProcessor {

    /**
     * Value to literal.
     * @param value the value
     * @return the string
     * @see com.ontimize.jee.server.dao.jpa.ql.literalprocessors.common.AbstractValueToQLLiteralProcessor#valueToLiteral(java.lang.Object)
     */
    @Override
    protected String valueToLiteral(Object value) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "'" + df.format((Date) value) + "'";
    }

    /**
     * Can process.
     * @param value the value
     * @return true, if successful
     * @see com.ontimize.jee.server.dao.jpa.ql.literalprocessors.common.AbstractValueToQLLiteralProcessor#canProcess(java.lang.Object)
     */
    @Override
    protected boolean canProcess(Object value) {
        return value instanceof Date;
    }

}

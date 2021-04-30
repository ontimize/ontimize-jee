/**
 *
 */
package com.ontimize.jee.server.dao.jpa.ql.literalprocessors.common;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * The Class DefaultValueToQLLiteralProcessor.
 */
public class DefaultValueToQLLiteralProcessor extends AbstractValueToQLLiteralProcessor {

    /**
     * Value to literal.
     * @param value the value
     * @return the string
     * @see com.ontimize.jee.server.dao.jpa.ql.literalprocessors.common.AbstractValueToQLLiteralProcessor#valueToLiteral(java.lang.Object)
     */
    @Override
    protected String valueToLiteral(Object value) {
        String literal = "";
        if (value == null) {
            literal = "null";
        } else if (value instanceof String) {
            literal = "'" + value + "'";
        } else if (value instanceof Timestamp) {
            literal = "to_timestamp('" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Timestamp) value)
                    + "', 'yyyy-mm-dd hh24:mi:ss')";
        } else if (value instanceof Date) {
            literal = "to_date('" + new SimpleDateFormat("yyyy-MM-dd").format((Date) value) + "', 'yyyy-mm-dd')";
        } else if (value instanceof Collection<?>) {
            literal = "";
            for (Object o : (Collection) value) {
                literal += this.valueToLiteral(o) + ", ";
            }
            if (literal.length() > 0) {
                literal = literal.substring(0, literal.length() - 2);
            }
        } else {
            literal = value.toString();
        }
        return literal;
    }

    /**
     * Can process.
     * @param value the value
     * @return true, if successful
     * @see com.ontimize.jee.server.dao.jpa.ql.literalprocessors.common.AbstractValueToQLLiteralProcessor#canProcess(java.lang.Object)
     */
    @Override
    protected boolean canProcess(Object value) {
        return true;
    }

}

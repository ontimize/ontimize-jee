/**
 *
 */
package com.ontimize.jee.server.dao.jpa.ql.literalprocessors.common;

/**
 * The Class DefaultStringToQLLiteralProcessor.
 */
public class DefaultStringToQLLiteralProcessor extends AbstractValueToQLLiteralProcessor {

    /**
     * Value to literal.
     * @param value the value
     * @return the string
     * @see com.ontimize.jee.server.dao.jpa.ql.literalprocessors.common.AbstractValueToQLLiteralProcessor#valueToLiteral(java.lang.Object)
     */
    @Override
    protected String valueToLiteral(Object value) {
        if (value == null) {
            return "null";
        }
        return "'" + this.scapeStringSpecialCharacters(value.toString()) + "'";
    }

    /**
     * Can process.
     * @param value the value
     * @return true, if successful
     * @see com.ontimize.jee.server.dao.jpa.ql.literalprocessors.common.AbstractValueToQLLiteralProcessor#canProcess(java.lang.Object)
     */
    @Override
    protected boolean canProcess(Object value) {
        return value instanceof String;
    }

    /**
     * Scape string special characters. By default it escapes ' character
     * @param value the value
     * @return the string
     */
    protected String scapeStringSpecialCharacters(String value) {
        return value.replace("'", "''");
    }

}

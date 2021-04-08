/**
 *
 */
package com.ontimize.jee.server.dao.jpa.ql;

/**
 * The Interface ValueToQLLiteralProcessor.
 */
public interface ValueToQLLiteralProcessor {

    /**
     * To ql literal.
     * @param value the value
     * @return the string
     */
    String toQLLiteral(Object value);

}

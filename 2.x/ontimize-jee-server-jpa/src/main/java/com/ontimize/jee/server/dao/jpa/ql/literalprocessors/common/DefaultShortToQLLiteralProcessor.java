/**
 *
 */
package com.ontimize.jee.server.dao.jpa.ql.literalprocessors.common;

/**
 * The Class DefaultShortToQLLiteralProcessor.
 */
public class DefaultShortToQLLiteralProcessor extends AbstractValueToQLLiteralProcessor {

	/**
	 * Value to literal.
	 *
	 * @param value
	 *            the value
	 * @return the string
	 * @see com.ontimize.jee.server.dao.jpa.ql.literalprocessors.common.AbstractValueToQLLiteralProcessor#valueToLiteral(java.lang.Object)
	 */
	@Override
	protected String valueToLiteral(Object value) {
		return Short.toString((Short) value);
	}

	/**
	 * Can process.
	 *
	 * @param value
	 *            the value
	 * @return true, if successful
	 * @see com.ontimize.jee.server.dao.jpa.ql.literalprocessors.common.AbstractValueToQLLiteralProcessor#canProcess(java.lang.Object)
	 */
	@Override
	protected boolean canProcess(Object value) {
		return value instanceof Short;
	}

}

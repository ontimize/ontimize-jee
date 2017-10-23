/**
 *
 */
package com.ontimize.jee.server.dao.jpa.ql.literalprocessors.common;

import com.ontimize.jee.server.dao.jpa.ql.ValueToQLLiteralProcessor;

/**
 * The Class AbstractValueToQLLiteralProcessor.
 */
public abstract class AbstractValueToQLLiteralProcessor implements ValueToQLLiteralProcessor {

	/** The next. */
	private ValueToQLLiteralProcessor next;

	/**
	 * Instantiates a new abstract value to ql literal processor.
	 */
	public AbstractValueToQLLiteralProcessor() {
		// do nothing
	}

	/**
	 * @see com.ontimize.jee.server.dao.jpa.ql.ValueToQLLiteralProcessor#toQLLiteral(java.lang.Object)
	 */
	@Override
	public String toQLLiteral(Object value) {
		if (this.canProcess(value)) {
			return this.valueToLiteral(value);
		} else {
			if (this.next != null) {
				return this.next.toQLLiteral(value);
			}
		}
		return null;
	}

	/**
	 * Gets the next.
	 *
	 * @return the next
	 */
	public ValueToQLLiteralProcessor getNext() {
		return this.next;
	}

	/**
	 * Sets the next.
	 *
	 * @param next
	 *            the new next
	 */
	public void setNext(ValueToQLLiteralProcessor next) {
		this.next = next;
	}

	/**
	 * Value to literal.
	 *
	 * @param value
	 *            the value
	 * @return the string
	 */
	protected abstract String valueToLiteral(Object value);

	/**
	 * Can process.
	 *
	 * @param value
	 *            the value
	 * @return true, if successful
	 */
	protected abstract boolean canProcess(Object value);

}

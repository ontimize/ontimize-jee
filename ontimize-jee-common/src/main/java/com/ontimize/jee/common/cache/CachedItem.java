/**
 * CachedItem.java 18-abr-2013
 *
 * 
 * 
 */
package com.ontimize.jee.common.cache;

/**
 * The Class CachedItem.
 *
 * @param <T>
 *            the generic type
 * @author <a href="user@email.com">Author</a>
 */
public class CachedItem<T> {
	private long	timestamp;
	private T		value;

	/**
	 * Gets the timestamp.
	 *
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return this.timestamp;
	}

	/**
	 * Sets the timestamp.
	 *
	 * @param timestamp
	 *            the new timestamp
	 */
	public void setTimestamp(final long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Sets the value.
	 *
	 * @param value
	 *            the new value
	 */
	public void setValue(final T value) {
		this.value = value;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public T getValue() {
		return this.value;
	}

}

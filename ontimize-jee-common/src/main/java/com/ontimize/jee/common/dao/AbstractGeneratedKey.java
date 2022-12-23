package com.ontimize.jee.common.dao;

import java.io.Serializable;
import java.util.Objects;

/**
 * The Class GeneratedKey.
 */
public abstract class AbstractGeneratedKey<T extends Serializable> implements Serializable {

	/** The generated key. */
	private T generatedKey;

	/**
	 * Simple constructor to support as bean.
	 */
	protected AbstractGeneratedKey() {
		// Do nothing
	}

	/**
	 * Instantiates a new generated key.
	 * @param generatedKey the generated key
	 */
	protected AbstractGeneratedKey(T generatedKey) {
		super();
		this.generatedKey = generatedKey;
	}


	/**
	 * Gets the generated key.
	 * @return the generated key
	 */
	public T getGeneratedKey() {
		return this.generatedKey;
	}

	/**
	 * Sets the generated key.
	 * @param generatedKey the new generated key
	 */
	public void setGeneratedKey(T generatedKey) {
		this.generatedKey = generatedKey;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		return (prime * result) + (this.generatedKey == null ? 0 : this.generatedKey.hashCode());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (this.getClass() != obj.getClass())) {
			return false;
		}
		AbstractGeneratedKey<T> other = AbstractGeneratedKey.class.cast(obj);

		return Objects.equals(this.generatedKey, other.generatedKey);
	}

}

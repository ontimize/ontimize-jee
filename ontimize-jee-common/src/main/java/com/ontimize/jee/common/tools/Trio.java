package com.ontimize.jee.common.tools;

import java.io.Serializable;

/**
 * The Class Pair.
 *
 * @param <K>
 *            the key type
 * @param <L>
 *            the generic type
 * @param <M>
 *            the generic type
 */
public class Trio<K, L, M> implements Serializable {

	/** The first. */
	private K	first;

	/** The second. */
	private L	second;
	/** The second. */
	private M	third;

	/**
	 * Instantiates a new pair.
	 */
	public Trio() {
		super();
	}

	/**
	 * Instantiates a new pair.
	 *
	 * @param first
	 *            the first
	 * @param second
	 *            the second
	 * @param third
	 *            the third
	 */
	public Trio(K first, L second, M third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}

	/**
	 * Gets the first.
	 *
	 * @return the first
	 */
	public K getFirst() {
		return this.first;
	}

	/**
	 * Sets the first.
	 *
	 * @param first
	 *            the new first
	 */
	public void setFirst(K first) {
		this.first = first;
	}

	/**
	 * Gets the second.
	 *
	 * @return the second
	 */
	public L getSecond() {
		return this.second;
	}

	/**
	 * Sets the second.
	 *
	 * @param second
	 *            the new second
	 */
	public void setSecond(L second) {
		this.second = second;
	}

	/**
	 * Gets the third.
	 *
	 * @return the third
	 */
	public M getThird() {
		return this.third;
	}

	/**
	 * Sets the third.
	 *
	 * @param third
	 *            the third
	 */
	public void setThird(M third) {
		this.third = third;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.first == null) ? 0 : this.first.hashCode());
		result = (prime * result) + ((this.second == null) ? 0 : this.second.hashCode());
		result = (prime * result) + ((this.third == null) ? 0 : this.third.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		Trio other = (Trio) obj;
		if (this.first == null) {
			if (other.first != null) {
				return false;
			}
		} else if (!this.first.equals(other.first)) {
			return false;
		}
		if (this.second == null) {
			if (other.second != null) {
				return false;
			}
		} else if (!this.second.equals(other.second)) {
			return false;
		}
		if (this.third == null) {
			if (other.third != null) {
				return false;
			}
		} else if (!this.third.equals(other.third)) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Trio{ first=%s, second=%s, third=%s}", this.first, this.second, this.third);
	}
}
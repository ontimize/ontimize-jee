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
 * @param <N>
 *            the generic type
 */
public class Tetra<K, L, M, N> implements Serializable {

	/** The first. */
	private K	first;

	/** The second. */
	private L	second;

	/** The third. */
	private M	third;

	/** The fourth. */
	private N	fourth;

	/**
	 * Instantiates a new pair.
	 */
	public Tetra() {
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
	public Tetra(K first, L second, M third, N fourth) {
		this.first = first;
		this.second = second;
		this.third = third;
		this.fourth = fourth;
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

	/**
	 * Gets the fourth.
	 *
	 * @return the third
	 */
	public N getFourth() {
		return this.fourth;
	}

	/**
	 * Sets the fourth.
	 *
	 * @param fourth
	 *            the fourth
	 */
	public void setFourth(N fourth) {
		this.fourth = fourth;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (this.first == null ? 0 : this.first.hashCode());
		result = (prime * result) + (this.second == null ? 0 : this.second.hashCode());
		result = (prime * result) + (this.third == null ? 0 : this.third.hashCode());
		result = (prime * result) + (this.fourth == null ? 0 : this.fourth.hashCode());
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
		Tetra other = (Tetra) obj;
		if (!ObjectTools.safeIsEquals(this.first, other.first)) {
			return false;
		}
		if (!ObjectTools.safeIsEquals(this.second, other.second)) {
			return false;
		}
		if (!ObjectTools.safeIsEquals(this.third, other.third)) {
			return false;
		}
		if (!ObjectTools.safeIsEquals(this.fourth, other.fourth)) {
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
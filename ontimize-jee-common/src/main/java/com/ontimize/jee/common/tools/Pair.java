package com.ontimize.jee.common.tools;

/**
 * The Class Pair.
 *
 * @param <K>
 *            the key type
 * @param <L>
 *            the generic type
 */
public class Pair<K, L> {

	/** The first. */
	private K	first;

	/** The second. */
	private L	second;

	/**
	 * Instantiates a new pair.
	 */
	public Pair() {
		super();
	}

	/**
	 * Instantiates a new pair.
	 *
	 * @param first
	 *            the first
	 * @param second
	 *            the second
	 */
	public Pair(K first, L second) {
		this.first = first;
		this.second = second;
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

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Pair{" + "key=" + this.first + " value=" + this.second + '}';
	}
}
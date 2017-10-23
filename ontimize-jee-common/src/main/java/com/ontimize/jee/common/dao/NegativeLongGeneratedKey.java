package com.ontimize.jee.common.dao;

/**
 * The Class GeneratedKey. Generated Number Key Objects with negative values
 */
public final class NegativeLongGeneratedKey extends AbstractGeneratedKey<Long> {

	/** The generator. */
	private static long generator = 0;

	/**
	 * Gets the next.
	 *
	 * @return the next
	 */
	public static NegativeLongGeneratedKey getNext() {
		NegativeLongGeneratedKey.generator--;
		return new NegativeLongGeneratedKey(NegativeLongGeneratedKey.generator);
	}

	/**
	 * Instantiates a new generated key.
	 *
	 * @param generatedKey
	 *            the generated key
	 */
	private NegativeLongGeneratedKey(Long generatedKey) {
		super(generatedKey);
	}

}

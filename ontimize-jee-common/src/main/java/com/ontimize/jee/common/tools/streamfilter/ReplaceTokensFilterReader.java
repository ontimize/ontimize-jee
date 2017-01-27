package com.ontimize.jee.common.tools.streamfilter;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

/**
 * Replaces tokens in the original input with user-supplied values.
 * 
 */
public final class ReplaceTokensFilterReader extends AbstractBaseFilterReader {
	/** Default "begin token" character. */
	private static final char	DEFAULT_IGNORE_TOKEN	= '$';
	/** Default "begin token" character. */
	private static final char	DEFAULT_BEGIN_TOKEN		= '{';

	/** Default "end token" character. */
	private static final char	DEFAULT_END_TOKEN		= '}';

	/** Data to be used before reading from stream again */
	private String				queuedData				= null;

	/** replacement test from a token */
	private String				replaceData				= null;

	/** Index into replacement data */
	private int					replaceIndex			= -1;

	/** Index into queue data */
	private int					queueIndex				= -1;

	/** Hashtable to hold the replacee-replacer pairs (String to String). */
	private Map<String, String>	hash;

	/** Character marking the beginning of a token. */
	private char				beginToken				= DEFAULT_BEGIN_TOKEN;

	/** Character marking the end of a token. */
	private char				endToken				= DEFAULT_END_TOKEN;
	/** Character marking the ignore token. */
	private char				ignoreToken				= DEFAULT_IGNORE_TOKEN;

	/**
	 * Creates a new filtered reader.
	 * 
	 * @param in
	 *            A Reader object providing the underlying stream. Must not be
	 *            <code>null</code>.
	 */
	public ReplaceTokensFilterReader(final Reader in, Map<String, String> hash) {
		super(in);
		this.hash = hash;
	}

	private int getNextChar() throws IOException {
		if (queueIndex != -1) {
			final int ch = queuedData.charAt(queueIndex++);
			if (queueIndex >= queuedData.length()) {
				queueIndex = -1;
			}
			return ch;
		}

		return in.read();
	}

	/**
	 * Returns the next character in the filtered stream, replacing tokens from
	 * the original stream.
	 * 
	 * @return the next character in the resulting stream, or -1 if the end of
	 *         the resulting stream has been reached
	 * 
	 * @exception IOException
	 *                if the underlying stream throws an IOException during
	 *                reading
	 */
	public int read() throws IOException {
		if (replaceIndex != -1) {
			final int ch = replaceData.charAt(replaceIndex++);
			if (replaceIndex >= replaceData.length()) {
				replaceIndex = -1;
			}
			return ch;
		}

		int ch = getNextChar();
		if (ch == ignoreToken) {
			ch = getNextChar();
		}

		if (ch == beginToken) {
			final StringBuffer key = new StringBuffer("");
			do {
				ch = getNextChar();
				if (ch != -1) {
					key.append((char) ch);
				} else {
					break;
				}
			} while (ch != endToken);

			if (ch == -1) {
				if (queuedData == null || queueIndex == -1) {
					queuedData = key.toString();
				} else {
					queuedData = key.toString() + queuedData.substring(queueIndex);
				}
				if (queuedData.length() > 0) {
					queueIndex = 0;
				} else {
					queueIndex = -1;
				}
				return beginToken;
			} else {
				key.setLength(key.length() - 1);

				final String replaceWith = (String) hash.get(key.toString());
				if (replaceWith != null) {
					if (replaceWith.length() > 0) {
						replaceData = replaceWith;
						replaceIndex = 0;
					}
					return read();
				} else {
					String newData = key.toString() + endToken;
					if (queuedData == null || queueIndex == -1) {
						queuedData = newData;
					} else {
						queuedData = newData + queuedData.substring(queueIndex);
					}
					queueIndex = 0;
					return beginToken;
				}
			}
		}
		return ch;
	}

	/**
	 * Sets the "begin token" character.
	 * 
	 * @param beginToken
	 *            the character used to denote the beginning of a token
	 */
	public void setBeginToken(final char beginToken) {
		this.beginToken = beginToken;
	}

	/**
	 * Sets the "end token" character.
	 * 
	 * @param endToken
	 *            the character used to denote the end of a token
	 */
	public void setEndToken(final char endToken) {
		this.endToken = endToken;
	}

	/**
	 * Adds a token element to the map of tokens to replace.
	 * 
	 * @param token
	 *            The token to add to the map of replacements. Must not be
	 *            <code>null</code>.
	 */
	public void addConfiguredToken(final Token token) {
		hash.put(token.getKey(), token.getValue());
	}

	/**
	 * Holds a token
	 */
	public static class Token {

		/** Token key */
		private String	key;

		/** Token value */
		private String	value;

		/**
		 * Sets the token key
		 * 
		 * @param key
		 *            The key for this token. Must not be <code>null</code>.
		 */
		public final void setKey(String key) {
			this.key = key;
		}

		/**
		 * Sets the token value
		 * 
		 * @param value
		 *            The value for this token. Must not be <code>null</code>.
		 */
		public final void setValue(String value) {
			this.value = value;
		}

		/**
		 * Returns the key for this token.
		 * 
		 * @return the key for this token
		 */
		public final String getKey() {
			return key;
		}

		/**
		 * Returns the value for this token.
		 * 
		 * @return the value for this token
		 */
		public final String getValue() {
			return value;
		}
	}
}

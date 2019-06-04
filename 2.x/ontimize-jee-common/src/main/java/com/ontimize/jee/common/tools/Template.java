package com.ontimize.jee.common.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Template.
 */
public class Template {

	/** The Constant logger. */
	private static final Logger	logger		= LoggerFactory.getLogger(Template.class);

	/** The template. */
	private final StringBuilder	template	= new StringBuilder();

	/**
	 * The Constructor.
	 *
	 * @param is
	 *            the is
	 */
	public Template(InputStream is) {
		super();
		this.loadTemplate(is);
	}

	/**
	 * The Constructor.
	 *
	 * @param classPath
	 *            the class path
	 */
	public Template(String classPath) {
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(classPath);
			try {
				this.loadTemplate(is);
			} finally {
				if (is != null) {
					is.close();
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Load template.
	 *
	 * @param is
	 *            the is
	 */
	protected void loadTemplate(InputStream is) {
		InputStreamReader isr = null;
		try {
			isr = new InputStreamReader(is);
			char[] buffer = new char[1024];
			int readed = 0;
			while ((readed = isr.read(buffer)) != -1) {
				this.template.append(buffer, 0, readed);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (isr != null) {
				try {
					isr.close();
				} catch (IOException e) {
					Template.logger.error(null, e);
				}
			}
		}
	}

	/**
	 * Fill template.
	 *
	 * @param mValues
	 *            the m values
	 * @return the string
	 */
	public String fillTemplate(Map<String, ?> mValues) {
		if ((mValues == null) || mValues.isEmpty()) {
			return this.template.toString();
		}
		StringBuilder sb = new StringBuilder(this.template);
		for (Entry<String, ?> entry : mValues.entrySet()) {
			this.replace(sb, entry.getKey(), entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
		}
		return sb.toString();
	}

	/**
	 * Fill template.
	 *
	 * @param pairs
	 *            the pairs
	 * @return the string
	 */
	public String fillTemplate(String... pairs) {
		if (pairs == null) {
			return this.template.toString();
		}
		CheckingTools.failIf((pairs.length % 2) != 0, "Template pairs length must be even");
		StringBuilder sb = new StringBuilder(this.template);
		for (int i = 0; i < pairs.length; i += 2) {
			this.replace(sb, pairs[i], pairs[i + 1]);
		}
		return sb.toString();
	}

	/**
	 * Replace.
	 *
	 * @param sb
	 *            the sb
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	private void replace(StringBuilder sb, String key, String value) {
		value = value == null ? "" : value;
		int idx = 0;
		while ((idx = sb.indexOf(key, idx)) >= 0) {
			sb.replace(idx, idx + key.length(), value);
			idx += key.length();
		}
	}

	/**
	 * Gets the template.
	 *
	 * @return the template
	 */
	public String getTemplate() {
		return this.template.toString();
	}
}

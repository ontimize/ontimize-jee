package com.ontimize.jee.server.exceptiontranslator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class DBErrorMessages.
 */
public class DBErrorMessagesTranslator {

	/** The Constant logger. */
	private static final Logger	logger	= LoggerFactory.getLogger(DBErrorMessagesTranslator.class);

	/** The props. */
	private Properties			props;

	/**
	 * Instantiates a new DB error messages.
	 *
	 * @param uriProperties
	 *            the uri properties
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public DBErrorMessagesTranslator(String uriProperties) throws IOException {
		super();

		URL url = Thread.currentThread().getContextClassLoader().getResource(uriProperties);
		if (url == null) {
			throw new IOException("Url " + uriProperties + " not found");
		} else {
			this.props = new Properties();
			try (InputStream input = url.openStream()) {
				this.props.load(input);
			}
		}
	}

	/**
	 * Gets the SQL state message.
	 *
	 * @param sqlState
	 *            the sql state
	 * @return the SQL state message
	 */
	public String getSQLStateMessage(String sqlState) {
		DBErrorMessagesTranslator.logger.debug("Message request for sqlstate: {}", sqlState);
		return this.props.getProperty(sqlState);
	}

	/**
	 * Gets the vendor code message.
	 *
	 * @param vendorCode
	 *            the vendor code
	 * @return the vendor code message
	 */
	public String getVendorCodeMessage(int vendorCode) {
		DBErrorMessagesTranslator.logger.debug("Message request for vendorCode: {}", vendorCode);
		return this.props.getProperty(Integer.toString(vendorCode));
	}
}
/*
 * Copyright (c) 2001-2004 Caucho Technology, Inc. All rights reserved. The Apache Software License, Version 1.1 Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 1. Redistributions of source code must retain the above copyright notice, this list of conditions and
 * the following disclaimer. 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution. 3. The end-user documentation included with the redistribution, if any, must include the following acknowlegement: "This
 * product includes software developed by the Caucho Technology (http://www.caucho.com/)." Alternately, this acknowlegement may appear in the software itself, if and wherever such
 * third-party acknowlegements normally appear. 4. The names "Burlap", "Resin", and "Caucho" must not be used to endorse or promote products derived from this software without
 * prior written permission. For written permission, please contact info@caucho.com. 5. Products derived from this software may not be called "Resin" nor may "Resin" appear in
 * their names without prior written permission of Caucho Technology. THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL CAUCHO TECHNOLOGY OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * @author Scott Ferguson
 */

package com.caucho.hessian.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal connection to a server. The default connection is based on java.net
 */
public class HessianURLConnection extends AbstractHessianConnection {

	private static final Logger	logger	= LoggerFactory.getLogger(HessianURLConnection.class);

	private final URI			url;
	private URLConnection		conn;

	private int					statusCode;
	private String				statusMessage;

	HessianURLConnection(URI url, URLConnection conn) {
		this.url = url;
		this.conn = conn;
	}

	/**
	 * Adds a HTTP header.
	 */
	@Override
	public void addHeader(String key, String value) {
		this.conn.setRequestProperty(key, value);
	}

	/**
	 * Returns the output stream for the request.
	 */
	@Override
	public OutputStream getOutputStream() throws IOException {
		return this.conn.getOutputStream();
	}

	/**
	 * Sends the request
	 */
	@Override
	public void sendRequest() throws IOException {
		if (this.conn instanceof HttpURLConnection) {
			HttpURLConnection httpConn = (HttpURLConnection) this.conn;

			this.statusCode = 500;

			try {
				this.statusCode = httpConn.getResponseCode();
			} catch (Exception e) {
				HessianURLConnection.logger.trace(null, e);
			}

			this.parseResponseHeaders(httpConn);

			InputStream is = null;

			if (this.statusCode != 200) {
				StringBuilder sb = new StringBuilder();
				int ch;

				try {
					is = httpConn.getInputStream();

					if (is != null) {
						while ((ch = is.read()) >= 0) {
							sb.append((char) ch);
						}

						is.close();
					}

					is = httpConn.getErrorStream();
					if (is != null) {
						while ((ch = is.read()) >= 0) {
							sb.append((char) ch);
						}
					}

					this.statusMessage = sb.toString();
				} catch (FileNotFoundException e) {
					throw new HessianConnectionException("HessianProxy cannot connect to '" + this.url, e);
				} catch (IOException e) {
					if (is == null) {
						throw new HessianConnectionException(this.statusCode + ": " + e, e);
					} else {
						throw new HessianConnectionException(this.statusCode + ": " + sb, e);
					}
				}

				if (is != null) {
					is.close();
				}

				throw new HessianConnectionException(this.statusCode + ": " + sb.toString());
			}
		}
	}

	protected void parseResponseHeaders(HttpURLConnection conn) throws IOException {}

	/**
	 * Returns the status code.
	 */
	@Override
	public int getStatusCode() {
		return this.statusCode;
	}

	/**
	 * Returns the status string.
	 */
	@Override
	public String getStatusMessage() {
		return this.statusMessage;
	}

	/**
	 * Returns the InputStream to the result
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		return this.conn.getInputStream();
	}

	@Override
	public String getContentEncoding() {
		return this.conn.getContentEncoding();
	}

	/**
	 * Close/free the connection
	 */
	@Override
	public void close() {
		this.close();

		URLConnection conn = this.conn;
		this.conn = null;

		if (conn instanceof HttpURLConnection) {
			((HttpURLConnection) conn).disconnect();
		}
	}
}

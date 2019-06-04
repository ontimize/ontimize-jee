/*
 * Copyright (c) 2001-2008 Caucho Technology, Inc. All rights reserved. The Apache Software License, Version 1.1 Redistribution and use in source and binary forms, with or without
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

package com.caucho.hessian.io;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Input stream for Hessian 2 streaming requests using WebSocket.
 *
 * For best performance, use HessianFactory:
 *
 * <pre>
 * <code> HessianFactory factory = new HessianFactory(); Hessian2StreamingInput hIn = factory.createHessian2StreamingInput(is); </code>
 * </pre>
 */
public class Hessian2StreamingInput {

	private static final Logger			log	= LoggerFactory.getLogger(Hessian2StreamingInput.class);
	private final StreamingInputStream	_is;
	private final Hessian2Input			_in;

	/**
	 * Creates a new Hessian input stream, initialized with an underlying input stream.
	 *
	 * @param is
	 *            the underlying output stream.
	 */
	public Hessian2StreamingInput(InputStream is) {
		this._is = new StreamingInputStream(is);
		this._in = new Hessian2Input(this._is);
	}

	public void setSerializerFactory(SerializerFactory factory) {
		this._in.setSerializerFactory(factory);
	}

	public boolean isDataAvailable() {
		StreamingInputStream is = this._is;

		return (is != null) && is.isDataAvailable();
	}

	public Hessian2Input startPacket() throws IOException {
		if (this._is.startPacket()) {
			this._in.resetReferences();
			this._in.resetBuffer(); // XXX:
			return this._in;
		} else {
			return null;
		}
	}

	public void endPacket() throws IOException {
		this._is.endPacket();
		this._in.resetBuffer(); // XXX:
	}

	public Hessian2Input getHessianInput() {
		return this._in;
	}

	/**
	 * Read the next object
	 */
	public Object readObject() throws IOException {
		this._is.startPacket();

		Object obj = this._in.readStreamingObject();

		this._is.endPacket();

		return obj;
	}

	/**
	 * Close the output.
	 */
	public void close() throws IOException {
		this._in.close();
	}

	static class StreamingInputStream extends InputStream {

		private final InputStream	_is;

		private long				_length;
		private boolean				_isPacketEnd;

		StreamingInputStream(InputStream is) {
			this._is = is;
		}

		public boolean isDataAvailable() {
			try {
				return (this._is != null) && (this._is.available() > 0);
			} catch (IOException e) {
				Hessian2StreamingInput.log.trace(e.toString(), e);

				return true;
			}
		}

		public boolean startPacket() throws IOException {
			// skip zero-length packets
			do {
				this._isPacketEnd = false;
			} while ((this._length = this.readChunkLength(this._is)) == 0);

			return this._length > 0;
		}

		public void endPacket() throws IOException {
			while (!this._isPacketEnd) {
				if (this._length <= 0) {
					this._length = this.readChunkLength(this._is);
				}

				if (this._length > 0) {
					this._is.skip(this._length);
					this._length = 0;
				}
			}

			if (this._length > 0) {
				this._is.skip(this._length);
				this._length = 0;
			}
		}

		@Override
		public int read() throws IOException {
			InputStream is = this._is;

			if (this._length == 0) {
				if (this._isPacketEnd) {
					return -1;
				}

				this._length = this.readChunkLength(is);

				if (this._length <= 0) {
					return -1;
				}
			}

			this._length--;

			return is.read();
		}

		@Override
		public int read(byte[] buffer, int offset, int length) throws IOException {
			InputStream is = this._is;

			if (this._length <= 0) {
				if (this._isPacketEnd) {
					return -1;
				}

				this._length = this.readChunkLength(is);

				if (this._length <= 0) {
					return -1;
				}
			}

			int sublen = (int) this._length;
			if (length < sublen) {
				sublen = length;
			}

			sublen = is.read(buffer, offset, sublen);

			if (sublen < 0) {
				return -1;
			}

			this._length -= sublen;

			return sublen;
		}

		private long readChunkLength(InputStream is) throws IOException {
			if (this._isPacketEnd) {
				return -1;
			}

			long length = 0;

			int code = is.read();

			if (code < 0) {
				this._isPacketEnd = true;
				return -1;
			}

			this._isPacketEnd = (code & 0x80) == 0;

			int len = is.read() & 0x7f;

			if (len < 0x7e) {
				length = len;
			} else if (len == 0x7e) {
				length = ((is.read() & 0xff) << 8) + (is.read() & 0xff);
			} else {
				length = (((long) (is.read() & 0xff)) << 56) + (((long) (is.read() & 0xff)) << 48) + (((long) (is.read() & 0xff)) << 40) + (((long) (is
						.read() & 0xff)) << 32) + (((long) (is.read() & 0xff)) << 24) + (((long) (is.read() & 0xff)) << 16) + (((long) (is.read() & 0xff)) << 8) + (is
								.read() & 0xff);
			}

			return length;
		}
	}
}

/*
 * The Apache Software License, Version 1.1 Copyright (c) 2001-2004 Caucho Technology, Inc. All rights reserved. Redistribution and use in source and
 * binary forms, with or without modification, are permitted provided that the following conditions are met: 1. Redistributions of source code must
 * retain the above copyright notice, this list of conditions and the following disclaimer. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. The end-user documentation included with the redistribution, if any, must include the following acknowlegement: "This product includes software
 * developed by the Caucho Technology (http://www.caucho.com/)." Alternately, this acknowlegement may appear in the software itself, if and wherever
 * such third-party acknowlegements normally appear. 4. The names "Hessian", "Resin", and "Caucho" must not be used to endorse or promote products
 * derived from this software without prior written permission. For written permission, please contact info@caucho.com. 5. Products derived from this
 * software may not be called "Resin" nor may "Resin" appear in their names without prior written permission of Caucho Technology. THIS SOFTWARE IS
 * PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL CAUCHO TECHNOLOGY OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * @author Scott Ferguson
 */

package com.caucho.hessian.mux;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Hessian Mux, a peer-to-peer protocol.
 */
public class MuxServer {
	private final Object		READ_LOCK		= new Object();
	private final Object		WRITE_LOCK		= new Object();

	private InputStream			is;
	private OutputStream		os;
	private boolean				isClient;

	private transient boolean	isClosed;

	// channels that have data ready.
	private final boolean		inputReady[]	= new boolean[4];

	// true if there's a thread already reading
	private boolean				isReadLocked;
	// true if there's a thread already writing
	private boolean				isWriteLocked;

	/**
	 * Null argument constructor.
	 */
	public MuxServer() {}

	/**
	 * Create a new multiplexor with input and output streams.
	 *
	 * @param is
	 *            the underlying input stream
	 * @param os
	 *            the underlying output stream
	 * @param isClient
	 *            true if this is the connection client.
	 */
	public MuxServer(InputStream is, OutputStream os, boolean isClient) {
		this.init(is, os, isClient);
	}

	/**
	 * Initialize the multiplexor with input and output streams.
	 *
	 * @param is
	 *            the underlying input stream
	 * @param os
	 *            the underlying output stream
	 * @param isClient
	 *            true if this is the connection client.
	 */
	public void init(InputStream is, OutputStream os, boolean isClient) {
		this.is = is;
		this.os = os;
		this.isClient = isClient;
	}

	/**
	 * Gets the raw input stream. Clients will normally not call this.
	 */
	public InputStream getInputStream() {
		return this.is;
	}

	/**
	 * Gets the raw output stream. Clients will normally not call this.
	 */
	public OutputStream getOutputStream() {
		return this.os;
	}

	/**
	 * Starts a client call.
	 */
	public boolean startCall(MuxInputStream in, MuxOutputStream out) throws IOException {
		int channel = this.isClient ? 2 : 3;

		return this.startCall(channel, in, out);
	}

	/**
	 * Starts a client call.
	 */
	public boolean startCall(int channel, MuxInputStream in, MuxOutputStream out) throws IOException {
		// XXX: Eventually need to check to see if the channel is used.
		// It's not clear whether this should cause a wait or an exception.

		in.init(this, channel);
		out.init(this, channel);

		return true;
	}

	/**
	 * Reads a server request.
	 */
	public boolean readRequest(MuxInputStream in, MuxOutputStream out) throws IOException {
		int channel = this.isClient ? 3 : 2;

		in.init(this, channel);
		out.init(this, channel);

		if (this.readChannel(channel) != null) {
			in.setInputStream(this.is);
			in.readToData(false);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Grabs the channel for writing.
	 *
	 * @param channel
	 *            the channel
	 *
	 * @return true if the channel has permission to write.
	 */
	OutputStream writeChannel(int channel) throws IOException {
		while (this.os != null) {
			boolean canWrite = false;
			synchronized (this.WRITE_LOCK) {
				if (!this.isWriteLocked) {
					this.isWriteLocked = true;
					canWrite = true;
				} else {
					try {
						this.WRITE_LOCK.wait(5000);
					} catch (Exception e) {
					}
				}
			}

			if (canWrite) {
				this.os.write('C');
				this.os.write(channel >> 8);
				this.os.write(channel);

				return this.os;
			}
		}

		return null;
	}

	void yield(int channel) throws IOException {
		this.os.write('Y');
		this.freeWriteLock();
	}

	void flush(int channel) throws IOException {
		this.os.write('Y');
		this.os.flush();
		this.freeWriteLock();
	}

	void close(int channel) throws IOException {
		if (this.os != null) {
			this.os.write('Q');
			this.os.flush();
			this.freeWriteLock();
		}
	}

	/**
	 * Frees the channel for writing.
	 */
	void freeWriteLock() {
		synchronized (this.WRITE_LOCK) {
			this.isWriteLocked = false;
			this.WRITE_LOCK.notifyAll();
		}
	}

	/**
	 * Reads data from a channel.
	 *
	 * @param channel
	 *            the channel
	 *
	 * @return true if the channel is valid.
	 */
	InputStream readChannel(int channel) throws IOException {
		while (!this.isClosed) {
			if (this.inputReady[channel]) {
				this.inputReady[channel] = false;
				return this.is;
			}

			boolean canRead = false;
			synchronized (this.READ_LOCK) {
				if (!this.isReadLocked) {
					this.isReadLocked = true;
					canRead = true;
				} else {
					try {
						this.READ_LOCK.wait(5000);
					} catch (Exception e) {
					}
				}
			}

			if (canRead) {
				try {
					this.readData();
				} catch (IOException e) {
					this.close();
				}
			}
		}

		return null;
	}

	boolean getReadLock() {
		synchronized (this.READ_LOCK) {
			if (!this.isReadLocked) {
				this.isReadLocked = true;
				return true;
			} else {
				try {
					this.READ_LOCK.wait(5000);
				} catch (Exception e) {
				}
			}
		}

		return false;
	}

	/**
	 * Frees the channel for reading.
	 */
	void freeReadLock() {
		synchronized (this.READ_LOCK) {
			this.isReadLocked = false;
			this.READ_LOCK.notifyAll();
		}
	}

	/**
	 * Reads data until a channel packet 'C' or error 'E' is received.
	 */
	private void readData() throws IOException {
		while (!this.isClosed) {
			int code = this.is.read();

			switch (code) {
				case ' ':
				case '\t':
				case '\n':
				case '\r':
					break;

				case 'C': {
					int channel = (this.is.read() << 8) + this.is.read();

					this.inputReady[channel] = true;
					return;
				}

				case 'E': {
					int channel = (this.is.read() << 8) + this.is.read();
					int status = (this.is.read() << 8) + this.is.read();

					this.inputReady[channel] = true;

					return;
				}

				case -1:
					this.close();
					return;

				default:
					// An error in the protocol. Kill the connection.
					this.close();
					return;
			}
		}

		return;
	}

	/**
	 * Close the mux
	 */
	public void close() throws IOException {
		this.isClosed = true;

		OutputStream os = this.os;
		this.os = null;

		InputStream is = this.is;
		this.is = null;

		if (os != null) {
			os.close();
		}

		if (is != null) {
			is.close();
		}
	}
}

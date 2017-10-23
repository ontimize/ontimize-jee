/*
 * Copyright (c) 2001-2004 Caucho Technology, Inc. All rights reserved. The Apache Software License, Version 1.1 Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 1. Redistributions of source code must retain the above copyright notice, this list of conditions and
 * the following disclaimer. 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution. 3. The end-user documentation included with the redistribution, if any, must include the following acknowlegement: "This
 * product includes software developed by the Caucho Technology (http://www.caucho.com/)." Alternately, this acknowlegement may appear in the software itself, if and wherever such
 * third-party acknowlegements normally appear. 4. The names "Hessian", "Resin", and "Caucho" must not be used to endorse or promote products derived from this software without
 * prior written permission. For written permission, please contact info@caucho.com. 5. Products derived from this software may not be called "Resin" nor may "Resin" appear in
 * their names without prior written permission of Caucho Technology. THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL CAUCHO TECHNOLOGY OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * @author Scott Ferguson
 */

package com.caucho.hessian.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Input stream for Hessian requests.
 *
 * <p>HessianInput is unbuffered, so any client needs to provide its own buffering.
 *
 * <pre>
 *  InputStream is = ...; // from http connection HessianInput in = new HessianInput(is); String value;
 *
 * in.startReply(); // read reply header value = in.readString(); // read string value in.completeReply(); // read reply footer
 * </pre>
 */
public class HessianInput extends AbstractHessianInput {

	private static final Logger	logger		= LoggerFactory.getLogger(HessianInput.class);

	private static int			END_OF_DATA	= -2;

	private static Field		detailMessageField;

	// factory for deserializing objects in the input stream
	protected SerializerFactory	serializerFactory;

	protected ArrayList<Object>	_refs;

	// the underlying input stream
	private InputStream			is;
	// a peek character
	protected int				peek		= -1;

	// the method for a call
	private String				method;

	private Throwable			_replyFault;

	private final StringBuilder	_sbuf		= new StringBuilder();

	// true if this is the last chunk
	private boolean				_isLastChunk;
	// the chunk length
	private int					_chunkLength;

	/**
	 * Creates an uninitialized Hessian input stream.
	 */
	public HessianInput() {}

	/**
	 * Creates a new Hessian input stream, initialized with an underlying input stream.
	 *
	 * @param is
	 *            the underlying input stream.
	 */
	public HessianInput(InputStream is) {
		this.init(is);
	}

	/**
	 * Sets the serializer factory.
	 */
	@Override
	public void setSerializerFactory(SerializerFactory factory) {
		this.serializerFactory = factory;
	}

	/**
	 * Gets the serializer factory.
	 */
	public SerializerFactory getSerializerFactory() {
		return this.serializerFactory;
	}

	/**
	 * Initialize the hessian stream with the underlying input stream.
	 */
	@Override
	public void init(InputStream is) {
		this.is = is;
		this.method = null;
		this._isLastChunk = true;
		this._chunkLength = 0;
		this.peek = -1;
		this._refs = null;
		this._replyFault = null;

		if (this.serializerFactory == null) {
			this.serializerFactory = new SerializerFactory();
		}
	}

	/**
	 * Returns the calls method
	 */
	@Override
	public String getMethod() {
		return this.method;
	}

	/**
	 * Returns any reply fault.
	 */
	public Throwable getReplyFault() {
		return this._replyFault;
	}

	/**
	 * Starts reading the call
	 *
	 * <pre>
	 *  c major minor
	 * </pre>
	 */
	@Override
	public int readCall() throws IOException {
		int tag = this.read();

		if (tag != 'c') {
			throw this.error("expected hessian call ('c') at " + this.codeName(tag));
		}

		int major = this.read();
		int minor = this.read();

		return (major << 16) + minor;
	}

	/**
	 * For backward compatibility with HessianSkeleton
	 */
	@Override
	public void skipOptionalCall() throws IOException {
		int tag = this.read();

		if (tag == 'c') {
			this.read();
			this.read();
		} else {
			this.peek = tag;
		}
	}

	/**
	 * Starts reading the call
	 *
	 * <p>A successful completion will have a single value:
	 *
	 * <pre>
	 *  m b16 b8 method
	 * </pre>
	 */
	@Override
	public String readMethod() throws IOException {
		int tag = this.read();

		if (tag != 'm') {
			throw this.error("expected hessian method ('m') at " + this.codeName(tag));
		}
		int d1 = this.read();
		int d2 = this.read();

		this._isLastChunk = true;
		this._chunkLength = (d1 * 256) + d2;
		this._sbuf.setLength(0);
		int ch;
		while ((ch = this.parseChar()) >= 0) {
			this._sbuf.append((char) ch);
		}

		this.method = this._sbuf.toString();

		return this.method;
	}

	/**
	 * Starts reading the call, including the headers.
	 *
	 * <p>The call expects the following protocol data
	 *
	 * <pre>
	 *  c major minor m b16 b8 method
	 * </pre>
	 */
	@Override
	public void startCall() throws IOException {
		this.readCall();

		while (this.readHeader() != null) {
			this.readObject();
		}

		this.readMethod();
	}

	/**
	 * Completes reading the call
	 *
	 * <p>A successful completion will have a single value:
	 *
	 * <pre>
	 * z
	 * </pre>
	 */
	@Override
	public void completeCall() throws IOException {
		int tag = this.read();

		if (tag == 'z') {
		} else {
			throw this.error("expected end of call ('z') at " + this.codeName(tag) + ".  Check method arguments and ensure method overloading is enabled if necessary");
		}
	}

	/**
	 * Reads a reply as an object. If the reply has a fault, throws the exception.
	 */
	@Override
	public Object readReply(Class<?> expectedClass) throws Throwable {
		int tag = this.read();

		if (tag != 'r') {
			this.error("expected hessian reply at " + this.codeName(tag));
		}

		int major = this.read();
		int minor = this.read();

		tag = this.read();
		if (tag == 'f') {
			throw this.prepareFault();
		} else {
			this.peek = tag;

			Object value = this.readObject(expectedClass);

			this.completeValueReply();

			return value;
		}
	}

	/**
	 * Starts reading the reply
	 *
	 * <p>A successful completion will have a single value:
	 *
	 * <pre>
	 * r
	 * </pre>
	 */
	@Override
	public void startReply() throws Throwable {
		int tag = this.read();

		if (tag != 'r') {
			this.error("expected hessian reply at " + this.codeName(tag));
		}

		int major = this.read();
		int minor = this.read();

		this.startReplyBody();
	}

	@Override
	public void startReplyBody() throws Throwable {
		int tag = this.read();

		if (tag == 'f') {
			throw this.prepareFault();
		} else {
			this.peek = tag;
		}
	}

	/**
	 * Prepares the fault.
	 */
	private Throwable prepareFault() throws IOException {
		HashMap<?, ?> fault = this.readFault();

		Object detail = fault.get("detail");
		String message = (String) fault.get("message");

		if (detail instanceof Throwable) {
			this._replyFault = (Throwable) detail;

			if ((message != null) && (HessianInput.detailMessageField != null)) {
				try {
					HessianInput.detailMessageField.set(this._replyFault, message);
				} catch (Exception e) {
					HessianInput.logger.trace(null, e);
				}
			}

			return this._replyFault;
		}

		else {
			String code = (String) fault.get("code");

			this._replyFault = new HessianServiceException(message, code, detail);

			return this._replyFault;
		}
	}

	/**
	 * Completes reading the call
	 *
	 * <p>A successful completion will have a single value:
	 *
	 * <pre>
	 * z
	 * </pre>
	 */
	@Override
	public void completeReply() throws IOException {
		int tag = this.read();

		if (tag != 'z') {
			this.error("expected end of reply at " + this.codeName(tag));
		}
	}

	/**
	 * Completes reading the call
	 *
	 * <p>A successful completion will have a single value:
	 *
	 * <pre>
	 * z
	 * </pre>
	 */
	public void completeValueReply() throws IOException {
		int tag = this.read();

		if (tag != 'z') {
			this.error("expected end of reply at " + this.codeName(tag));
		}
	}

	/**
	 * Reads a header, returning null if there are no headers.
	 *
	 * <pre>
	 *  H b16 b8 value
	 * </pre>
	 */
	@Override
	public String readHeader() throws IOException {
		int tag = this.read();

		if (tag == 'H') {
			this._isLastChunk = true;
			this._chunkLength = (this.read() << 8) + this.read();

			this._sbuf.setLength(0);
			int ch;
			while ((ch = this.parseChar()) >= 0) {
				this._sbuf.append((char) ch);
			}

			return this._sbuf.toString();
		}

		this.peek = tag;

		return null;
	}

	/**
	 * Reads a null
	 *
	 * <pre>
	 * N
	 * </pre>
	 */
	@Override
	public void readNull() throws IOException {
		int tag = this.read();

		switch (tag) {
			case 'N':
				return;

			default:
				throw this.expect("null", tag);
		}
	}

	/**
	 * Reads a boolean
	 *
	 * <pre>
	 *  T F
	 * </pre>
	 */
	@Override
	public boolean readBoolean() throws IOException {
		int tag = this.read();

		switch (tag) {
			case 'T':
				return true;
			case 'F':
				return false;
			case 'I':
				return this.parseInt() == 0;
			case 'L':
				return this.parseLong() == 0;
			case 'D':
				return this.parseDouble() == 0.0;
			case 'N':
				return false;

			default:
				throw this.expect("boolean", tag);
		}
	}

	/**
	 * Reads a byte
	 *
	 * <pre>
	 *  I b32 b24 b16 b8
	 * </pre>
	 */
	/*
	 * public byte readByte() throws IOException { return (byte) readInt(); }
	 */

	/**
	 * Reads a short
	 *
	 * <pre>
	 *  I b32 b24 b16 b8
	 * </pre>
	 */
	public short readShort() throws IOException {
		return (short) this.readInt();
	}

	/**
	 * Reads an integer
	 *
	 * <pre>
	 *  I b32 b24 b16 b8
	 * </pre>
	 */
	@Override
	public int readInt() throws IOException {
		int tag = this.read();

		switch (tag) {
			case 'T':
				return 1;
			case 'F':
				return 0;
			case 'I':
				return this.parseInt();
			case 'L':
				return (int) this.parseLong();
			case 'D':
				return (int) this.parseDouble();

			default:
				throw this.expect("int", tag);
		}
	}

	/**
	 * Reads a long
	 *
	 * <pre>
	 *  L b64 b56 b48 b40 b32 b24 b16 b8
	 * </pre>
	 */
	@Override
	public long readLong() throws IOException {
		int tag = this.read();

		switch (tag) {
			case 'T':
				return 1;
			case 'F':
				return 0;
			case 'I':
				return this.parseInt();
			case 'L':
				return this.parseLong();
			case 'D':
				return (long) this.parseDouble();

			default:
				throw this.expect("long", tag);
		}
	}

	/**
	 * Reads a float
	 *
	 * <pre>
	 *  D b64 b56 b48 b40 b32 b24 b16 b8
	 * </pre>
	 */
	public float readFloat() throws IOException {
		return (float) this.readDouble();
	}

	/**
	 * Reads a double
	 *
	 * <pre>
	 *  D b64 b56 b48 b40 b32 b24 b16 b8
	 * </pre>
	 */
	@Override
	public double readDouble() throws IOException {
		int tag = this.read();

		switch (tag) {
			case 'T':
				return 1;
			case 'F':
				return 0;
			case 'I':
				return this.parseInt();
			case 'L':
				return this.parseLong();
			case 'D':
				return this.parseDouble();

			default:
				throw this.expect("long", tag);
		}
	}

	/**
	 * Reads a date.
	 *
	 * <pre>
	 *  T b64 b56 b48 b40 b32 b24 b16 b8
	 * </pre>
	 */
	@Override
	public long readUTCDate() throws IOException {
		int tag = this.read();

		if (tag != 'd') {
			throw this.error("expected date at " + this.codeName(tag));
		}

		long b64 = this.read();
		long b56 = this.read();
		long b48 = this.read();
		long b40 = this.read();
		long b32 = this.read();
		long b24 = this.read();
		long b16 = this.read();
		long b8 = this.read();

		return (b64 << 56) + (b56 << 48) + (b48 << 40) + (b40 << 32) + (b32 << 24) + (b24 << 16) + (b16 << 8) + b8;
	}

	/**
	 * Reads a byte from the stream.
	 */
	public int readChar() throws IOException {
		if (this._chunkLength > 0) {
			this._chunkLength--;
			if ((this._chunkLength == 0) && this._isLastChunk) {
				this._chunkLength = HessianInput.END_OF_DATA;
			}

			int ch = this.parseUTF8Char();
			return ch;
		} else if (this._chunkLength == HessianInput.END_OF_DATA) {
			this._chunkLength = 0;
			return -1;
		}

		int tag = this.read();

		switch (tag) {
			case 'N':
				return -1;

			case 'S':
			case 's':
			case 'X':
			case 'x':
				this._isLastChunk = (tag == 'S') || (tag == 'X');
				this._chunkLength = (this.read() << 8) + this.read();

				this._chunkLength--;
				int value = this.parseUTF8Char();

				// special code so successive read byte won't
				// be read as a single object.
				if ((this._chunkLength == 0) && this._isLastChunk) {
					this._chunkLength = HessianInput.END_OF_DATA;
				}

				return value;

			default:
				throw new IOException("expected 'S' at " + (char) tag);
		}
	}

	/**
	 * Reads a byte array from the stream.
	 */
	public int readString(char[] buffer, int offset, int length) throws IOException {
		int readLength = 0;

		if (this._chunkLength == HessianInput.END_OF_DATA) {
			this._chunkLength = 0;
			return -1;
		} else if (this._chunkLength == 0) {
			int tag = this.read();

			switch (tag) {
				case 'N':
					return -1;

				case 'S':
				case 's':
				case 'X':
				case 'x':
					this._isLastChunk = (tag == 'S') || (tag == 'X');
					this._chunkLength = (this.read() << 8) + this.read();
					break;

				default:
					throw new IOException("expected 'S' at " + (char) tag);
			}
		}

		while (length > 0) {
			if (this._chunkLength > 0) {
				buffer[offset++] = (char) this.parseUTF8Char();
				this._chunkLength--;
				length--;
				readLength++;
			} else if (this._isLastChunk) {
				if (readLength == 0) {
					return -1;
				} else {
					this._chunkLength = HessianInput.END_OF_DATA;
					return readLength;
				}
			} else {
				int tag = this.read();

				switch (tag) {
					case 'S':
					case 's':
					case 'X':
					case 'x':
						this._isLastChunk = (tag == 'S') || (tag == 'X');
						this._chunkLength = (this.read() << 8) + this.read();
						break;

					default:
						throw new IOException("expected 'S' at " + (char) tag);
				}
			}
		}

		if (readLength == 0) {
			return -1;
		} else if ((this._chunkLength > 0) || !this._isLastChunk) {
			return readLength;
		} else {
			this._chunkLength = HessianInput.END_OF_DATA;
			return readLength;
		}
	}

	/**
	 * Reads a string
	 *
	 * <pre>
	 *  S b16 b8 string value
	 * </pre>
	 */
	@Override
	public String readString() throws IOException {
		int tag = this.read();

		switch (tag) {
			case 'N':
				return null;

			case 'I':
				return String.valueOf(this.parseInt());
			case 'L':
				return String.valueOf(this.parseLong());
			case 'D':
				return String.valueOf(this.parseDouble());

			case 'S':
			case 's':
			case 'X':
			case 'x':
				this._isLastChunk = (tag == 'S') || (tag == 'X');
				this._chunkLength = (this.read() << 8) + this.read();

				this._sbuf.setLength(0);
				int ch;

				while ((ch = this.parseChar()) >= 0) {
					this._sbuf.append((char) ch);
				}

				return this._sbuf.toString();

			default:
				throw this.expect("string", tag);
		}
	}

	/**
	 * Reads an XML node.
	 *
	 * <pre>
	 *  S b16 b8 string value
	 * </pre>
	 */
	@Override
	public org.w3c.dom.Node readNode() throws IOException {
		int tag = this.read();

		switch (tag) {
			case 'N':
				return null;

			case 'S':
			case 's':
			case 'X':
			case 'x':
				this._isLastChunk = (tag == 'S') || (tag == 'X');
				this._chunkLength = (this.read() << 8) + this.read();

				throw this.error("Can't handle string in this context");

			default:
				throw this.expect("string", tag);
		}
	}

	/**
	 * Reads a byte array
	 *
	 * <pre>
	 *  B b16 b8 data value
	 * </pre>
	 */
	@Override
	public byte[] readBytes() throws IOException {
		int tag = this.read();

		switch (tag) {
			case 'N':
				return null;

			case 'B':
			case 'b':
				this._isLastChunk = tag == 'B';
				this._chunkLength = (this.read() << 8) + this.read();

				ByteArrayOutputStream bos = new ByteArrayOutputStream();

				int data;
				while ((data = this.parseByte()) >= 0) {
					bos.write(data);
				}

				return bos.toByteArray();

			default:
				throw this.expect("bytes", tag);
		}
	}

	/**
	 * Reads a byte from the stream.
	 */
	public int readByte() throws IOException {
		if (this._chunkLength > 0) {
			this._chunkLength--;
			if ((this._chunkLength == 0) && this._isLastChunk) {
				this._chunkLength = HessianInput.END_OF_DATA;
			}

			return this.read();
		} else if (this._chunkLength == HessianInput.END_OF_DATA) {
			this._chunkLength = 0;
			return -1;
		}

		int tag = this.read();

		switch (tag) {
			case 'N':
				return -1;

			case 'B':
			case 'b':
				this._isLastChunk = tag == 'B';
				this._chunkLength = (this.read() << 8) + this.read();

				int value = this.parseByte();

				// special code so successive read byte won't
				// be read as a single object.
				if ((this._chunkLength == 0) && this._isLastChunk) {
					this._chunkLength = HessianInput.END_OF_DATA;
				}

				return value;

			default:
				throw new IOException("expected 'B' at " + (char) tag);
		}
	}

	/**
	 * Reads a byte array from the stream.
	 */
	public int readBytes(byte[] buffer, int offset, int length) throws IOException {
		int readLength = 0;

		if (this._chunkLength == HessianInput.END_OF_DATA) {
			this._chunkLength = 0;
			return -1;
		} else if (this._chunkLength == 0) {
			int tag = this.read();

			switch (tag) {
				case 'N':
					return -1;

				case 'B':
				case 'b':
					this._isLastChunk = tag == 'B';
					this._chunkLength = (this.read() << 8) + this.read();
					break;

				default:
					throw new IOException("expected 'B' at " + (char) tag);
			}
		}

		while (length > 0) {
			if (this._chunkLength > 0) {
				buffer[offset++] = (byte) this.read();
				this._chunkLength--;
				length--;
				readLength++;
			} else if (this._isLastChunk) {
				if (readLength == 0) {
					return -1;
				} else {
					this._chunkLength = HessianInput.END_OF_DATA;
					return readLength;
				}
			} else {
				int tag = this.read();

				switch (tag) {
					case 'B':
					case 'b':
						this._isLastChunk = tag == 'B';
						this._chunkLength = (this.read() << 8) + this.read();
						break;

					default:
						throw new IOException("expected 'B' at " + (char) tag);
				}
			}
		}

		if (readLength == 0) {
			return -1;
		} else if ((this._chunkLength > 0) || !this._isLastChunk) {
			return readLength;
		} else {
			this._chunkLength = HessianInput.END_OF_DATA;
			return readLength;
		}
	}

	/**
	 * Reads a fault.
	 */
	private HashMap<Object, Object> readFault() throws IOException {
		HashMap<Object, Object> map = new HashMap<>();

		int code = this.read();
		for (; (code > 0) && (code != 'z'); code = this.read()) {
			this.peek = code;

			Object key = this.readObject();
			Object value = this.readObject();

			if ((key != null) && (value != null)) {
				map.put(key, value);
			}
		}

		if (code != 'z') {
			throw this.expect("fault", code);
		}

		return map;
	}

	/**
	 * Reads an object from the input stream with an expected type.
	 */
	@Override
	public Object readObject(Class<?> cl) throws IOException {
		if ((cl == null) || (cl == Object.class)) {
			return this.readObject();
		}

		int tag = this.read();

		switch (tag) {
			case 'N':
				return null;

			case 'M': {
				String type = this.readType();

				// hessian/3386
				if ("".equals(type)) {
					Deserializer reader;
					reader = this.serializerFactory.getDeserializer(cl);

					return reader.readMap(this);
				} else {
					Deserializer reader;
					reader = this.serializerFactory.getObjectDeserializer(type);

					return reader.readMap(this);
				}
			}

			case 'V': {
				String type = this.readType();
				int length = this.readLength();

				Deserializer reader;
				reader = this.serializerFactory.getObjectDeserializer(type);

				if ((cl != reader.getType()) && cl.isAssignableFrom(reader.getType())) {
					return reader.readList(this, length);
				}

				reader = this.serializerFactory.getDeserializer(cl);

				Object v = reader.readList(this, length);

				return v;
			}

			case 'R': {
				int ref = this.parseInt();

				return this._refs.get(ref);
			}

			case 'r': {
				String type = this.readType();
				String url = this.readString();

				return this.resolveRemote(type, url);
			}
		}

		this.peek = tag;

		// hessian/332i vs hessian/3406
		// return readObject();

		Object value = this.serializerFactory.getDeserializer(cl).readObject(this);

		return value;
	}

	/**
	 * Reads an arbitrary object from the input stream when the type is unknown.
	 */
	@Override
	public Object readObject() throws IOException {
		int tag = this.read();

		switch (tag) {
			case 'N':
				return null;

			case 'T':
				return Boolean.valueOf(true);

			case 'F':
				return Boolean.valueOf(false);

			case 'I':
				return Integer.valueOf(this.parseInt());

			case 'L':
				return Long.valueOf(this.parseLong());

			case 'D':
				return Double.valueOf(this.parseDouble());

			case 'd':
				return new Date(this.parseLong());

			case 'x':
			case 'X': {
				this._isLastChunk = tag == 'X';
				this._chunkLength = (this.read() << 8) + this.read();

				return this.parseXML();
			}

			case 's':
			case 'S': {
				this._isLastChunk = tag == 'S';
				this._chunkLength = (this.read() << 8) + this.read();

				int data;
				this._sbuf.setLength(0);

				while ((data = this.parseChar()) >= 0) {
					this._sbuf.append((char) data);
				}

				return this._sbuf.toString();
			}

			case 'b':
			case 'B': {
				this._isLastChunk = tag == 'B';
				this._chunkLength = (this.read() << 8) + this.read();

				int data;
				ByteArrayOutputStream bos = new ByteArrayOutputStream();

				while ((data = this.parseByte()) >= 0) {
					bos.write(data);
				}

				return bos.toByteArray();
			}

			case 'V': {
				String type = this.readType();
				int length = this.readLength();

				return this.serializerFactory.readList(this, length, type);
			}

			case 'M': {
				String type = this.readType();

				return this.serializerFactory.readMap(this, type);
			}

			case 'R': {
				int ref = this.parseInt();

				return this._refs.get(ref);
			}

			case 'r': {
				String type = this.readType();
				String url = this.readString();

				return this.resolveRemote(type, url);
			}

			default:
				throw this.error("unknown code for readObject at " + this.codeName(tag));
		}
	}

	/**
	 * Reads a remote object.
	 */
	@Override
	public Object readRemote() throws IOException {
		String type = this.readType();
		String url = this.readString();

		return this.resolveRemote(type, url);
	}

	/**
	 * Reads a reference.
	 */
	@Override
	public Object readRef() throws IOException {
		return this._refs.get(this.parseInt());
	}

	/**
	 * Reads the start of a list.
	 */
	@Override
	public int readListStart() throws IOException {
		return this.read();
	}

	/**
	 * Reads the start of a list.
	 */
	@Override
	public int readMapStart() throws IOException {
		return this.read();
	}

	/**
	 * Returns true if this is the end of a list or a map.
	 */
	@Override
	public boolean isEnd() throws IOException {
		int code = this.read();

		this.peek = code;

		return (code < 0) || (code == 'z');
	}

	/**
	 * Reads the end byte.
	 */
	@Override
	public void readEnd() throws IOException {
		int code = this.read();

		if (code != 'z') {
			throw this.error("unknown code at " + this.codeName(code));
		}
	}

	/**
	 * Reads the end byte.
	 */
	@Override
	public void readMapEnd() throws IOException {
		int code = this.read();

		if (code != 'z') {
			throw this.error("expected end of map ('z') at " + this.codeName(code));
		}
	}

	/**
	 * Reads the end byte.
	 */
	@Override
	public void readListEnd() throws IOException {
		int code = this.read();

		if (code != 'z') {
			throw this.error("expected end of list ('z') at " + this.codeName(code));
		}
	}

	/**
	 * Adds a list/map reference.
	 */
	@Override
	public int addRef(Object ref) {
		if (this._refs == null) {
			this._refs = new ArrayList<>();
		}

		this._refs.add(ref);

		return this._refs.size() - 1;
	}

	/**
	 * Adds a list/map reference.
	 */
	@Override
	public void setRef(int i, Object ref) {
		this._refs.set(i, ref);
	}

	/**
	 * Resets the references for streaming.
	 */
	@Override
	public void resetReferences() {
		if (this._refs != null) {
			this._refs.clear();
		}
	}

	/**
	 * Resolves a remote object.
	 */
	public Object resolveRemote(String type, String url) throws IOException {
		HessianRemoteResolver resolver = this.getRemoteResolver();

		if (resolver != null) {
			return resolver.lookup(type, url);
		} else {
			return new HessianRemote(type, url);
		}
	}

	/**
	 * Parses a type from the stream.
	 *
	 * <pre>
	 *  t b16 b8
	 * </pre>
	 */
	@Override
	public String readType() throws IOException {
		int code = this.read();

		if (code != 't') {
			this.peek = code;
			return "";
		}

		this._isLastChunk = true;
		this._chunkLength = (this.read() << 8) + this.read();

		this._sbuf.setLength(0);
		int ch;
		while ((ch = this.parseChar()) >= 0) {
			this._sbuf.append((char) ch);
		}

		return this._sbuf.toString();
	}

	/**
	 * Parses the length for an array
	 *
	 * <pre>
	 *  l b32 b24 b16 b8
	 * </pre>
	 */
	@Override
	public int readLength() throws IOException {
		int code = this.read();

		if (code != 'l') {
			this.peek = code;
			return -1;
		}

		return this.parseInt();
	}

	/**
	 * Parses a 32-bit integer value from the stream.
	 *
	 * <pre>
	 *  b32 b24 b16 b8
	 * </pre>
	 */
	private int parseInt() throws IOException {
		int b32 = this.read();
		int b24 = this.read();
		int b16 = this.read();
		int b8 = this.read();

		return (b32 << 24) + (b24 << 16) + (b16 << 8) + b8;
	}

	/**
	 * Parses a 64-bit long value from the stream.
	 *
	 * <pre>
	 *  b64 b56 b48 b40 b32 b24 b16 b8
	 * </pre>
	 */
	private long parseLong() throws IOException {
		long b64 = this.read();
		long b56 = this.read();
		long b48 = this.read();
		long b40 = this.read();
		long b32 = this.read();
		long b24 = this.read();
		long b16 = this.read();
		long b8 = this.read();

		return (b64 << 56) + (b56 << 48) + (b48 << 40) + (b40 << 32) + (b32 << 24) + (b24 << 16) + (b16 << 8) + b8;
	}

	/**
	 * Parses a 64-bit double value from the stream.
	 *
	 * <pre>
	 *  b64 b56 b48 b40 b32 b24 b16 b8
	 * </pre>
	 */
	private double parseDouble() throws IOException {
		long b64 = this.read();
		long b56 = this.read();
		long b48 = this.read();
		long b40 = this.read();
		long b32 = this.read();
		long b24 = this.read();
		long b16 = this.read();
		long b8 = this.read();

		long bits = (b64 << 56) + (b56 << 48) + (b48 << 40) + (b40 << 32) + (b32 << 24) + (b24 << 16) + (b16 << 8) + b8;

		return Double.longBitsToDouble(bits);
	}

	org.w3c.dom.Node parseXML() throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Reads a character from the underlying stream.
	 */
	private int parseChar() throws IOException {
		while (this._chunkLength <= 0) {
			if (this._isLastChunk) {
				return -1;
			}

			int code = this.read();

			switch (code) {
				case 's':
				case 'x':
					this._isLastChunk = false;

					this._chunkLength = (this.read() << 8) + this.read();
					break;

				case 'S':
				case 'X':
					this._isLastChunk = true;

					this._chunkLength = (this.read() << 8) + this.read();
					break;

				default:
					throw this.expect("string", code);
			}

		}

		this._chunkLength--;

		return this.parseUTF8Char();
	}

	/**
	 * Parses a single UTF8 character.
	 */
	private int parseUTF8Char() throws IOException {
		int ch = this.read();

		if (ch < 0x80) {
			return ch;
		} else if ((ch & 0xe0) == 0xc0) {
			int ch1 = this.read();
			int v = ((ch & 0x1f) << 6) + (ch1 & 0x3f);

			return v;
		} else if ((ch & 0xf0) == 0xe0) {
			int ch1 = this.read();
			int ch2 = this.read();
			int v = ((ch & 0x0f) << 12) + ((ch1 & 0x3f) << 6) + (ch2 & 0x3f);

			return v;
		} else {
			throw this.error("bad utf-8 encoding at " + this.codeName(ch));
		}
	}

	/**
	 * Reads a byte from the underlying stream.
	 */
	private int parseByte() throws IOException {
		while (this._chunkLength <= 0) {
			if (this._isLastChunk) {
				return -1;
			}

			int code = this.read();

			switch (code) {
				case 'b':
					this._isLastChunk = false;

					this._chunkLength = (this.read() << 8) + this.read();
					break;

				case 'B':
					this._isLastChunk = true;

					this._chunkLength = (this.read() << 8) + this.read();
					break;

				default:
					throw this.expect("byte[]", code);
			}
		}

		this._chunkLength--;

		return this.read();
	}

	/**
	 * Reads bytes based on an input stream.
	 */
	@Override
	public InputStream readInputStream() throws IOException {
		int tag = this.read();

		switch (tag) {
			case 'N':
				return null;

			case 'B':
			case 'b':
				this._isLastChunk = tag == 'B';
				this._chunkLength = (this.read() << 8) + this.read();
				break;

			default:
				throw this.expect("inputStream", tag);
		}

		return new InputStream() {

			boolean _isClosed = false;

			@Override
			public int read() throws IOException {
				if (this._isClosed || (HessianInput.this.is == null)) {
					return -1;
				}

				int ch = HessianInput.this.parseByte();
				if (ch < 0) {
					this._isClosed = true;
				}

				return ch;
			}

			@Override
			public int read(byte[] buffer, int offset, int length) throws IOException {
				if (this._isClosed || (HessianInput.this.is == null)) {
					return -1;
				}

				int len = HessianInput.this.read(buffer, offset, length);
				if (len < 0) {
					this._isClosed = true;
				}

				return len;
			}

			@Override
			public void close() throws IOException {
				while (this.read() >= 0) {
				}

				this._isClosed = true;
			}
		};
	}

	/**
	 * Reads bytes from the underlying stream.
	 */
	int read(byte[] buffer, int offset, int length) throws IOException {
		int readLength = 0;

		while (length > 0) {
			while (this._chunkLength <= 0) {
				if (this._isLastChunk) {
					return readLength == 0 ? -1 : readLength;
				}

				int code = this.read();

				switch (code) {
					case 'b':
						this._isLastChunk = false;

						this._chunkLength = (this.read() << 8) + this.read();
						break;

					case 'B':
						this._isLastChunk = true;

						this._chunkLength = (this.read() << 8) + this.read();
						break;

					default:
						throw this.expect("byte[]", code);
				}
			}

			int sublen = this._chunkLength;
			if (length < sublen) {
				sublen = length;
			}

			sublen = this.is.read(buffer, offset, sublen);
			offset += sublen;
			readLength += sublen;
			length -= sublen;
			this._chunkLength -= sublen;
		}

		return readLength;
	}

	final int read() throws IOException {
		if (this.peek >= 0) {
			int value = this.peek;
			this.peek = -1;
			return value;
		}

		int ch = this.is.read();

		return ch;
	}

	@Override
	public void close() {
		this.is = null;
	}

	@Override
	public Reader getReader() {
		return null;
	}

	protected IOException expect(String expect, int ch) {
		return this.error("expected " + expect + " at " + this.codeName(ch));
	}

	protected String codeName(int ch) {
		if (ch < 0) {
			return "end of file";
		} else {
			return "0x" + Integer.toHexString(ch & 0xff) + " (" + (char) +ch + ")";
		}
	}

	protected IOException error(String message) {
		if (this.method != null) {
			return new HessianProtocolException(this.method + ": " + message);
		} else {
			return new HessianProtocolException(message);
		}
	}

	static {
		try {
			HessianInput.detailMessageField = Throwable.class.getDeclaredField("detailMessage");
			HessianInput.detailMessageField.setAccessible(true);
		} catch (Exception e) {
			HessianInput.logger.trace(null, e);
		}
	}
}

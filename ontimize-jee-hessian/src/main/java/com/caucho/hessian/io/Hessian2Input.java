/*
 * Copyright (c) 2001-2008 Caucho Technology, Inc. All rights reserved. The Apache Software License, Version 1.1 Redistribution and use in source and binary forms, with or without
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
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.io.deserializer.Deserializer;
import com.caucho.hessian.util.FlowControlTools;

// @formatter:off
/**
 * Input stream for Hessian requests.
 *
 * <p>HessianInput is unbuffered, so any client needs to provide its own buffering.
 *
 * <pre>
 * InputStream is = ...; // from http connection
 * HessianInput in = new HessianInput(is);
 * String value;
 *
 * in.startReply();         // read reply header
 * value = in.readString(); // read string value
 * in.completeReply();      // read reply footer
 * </pre>
 */
// @formatter:on
public class Hessian2Input extends AbstractHessianInput implements Hessian2Constants {

	private static final Logger				logger		= LoggerFactory.getLogger(Hessian2Input.class);
	private static final int				END_OF_DATA	= -2;

	private static Field					detailMessageField;

	private static final int				SIZE		= 1024;

	// standard, unmodified factory for deserializing objects
	protected SerializerFactory				defaultSerializerFactory;
	// factory for deserializing objects in the input stream
	protected SerializerFactory				serializerFactory;

	private static boolean					isCloseStreamOnClose;

	protected ArrayList<Object>				refs		= new ArrayList<>();
	protected ArrayList<ObjectDefinition>	classDefs	= new ArrayList<>();
	protected ArrayList<String>				types		= new ArrayList<>();

	// the underlying input stream
	private InputStream						is;
	private final byte[]					buffer		= new byte[Hessian2Input.SIZE];

	// a peek character
	private int								offset;
	private int								length;

	// the method for a call
	private String							method;
	private Throwable						replyFault;

	private final StringBuilder				sbuf		= new StringBuilder();

	// true if this is the last chunk
	private boolean							isLastChunk;
	// the chunk length
	private int								chunkLength;

	public Hessian2Input() {
		super();
	}

	/**
	 * Creates a new Hessian input stream, initialized with an underlying input stream.
	 *
	 * @param is
	 *            the underlying input stream.
	 */
	public Hessian2Input(InputStream is) {
		this();

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
		// the default serializer factory cannot be modified by external
		// callers
		if (this.serializerFactory == this.defaultSerializerFactory) {
			this.serializerFactory = new SerializerFactory();
		}

		return this.serializerFactory;
	}

	/**
	 * Gets the serializer factory.
	 */
	protected final SerializerFactory findSerializerFactory() {
		SerializerFactory factory = this.serializerFactory;

		if (factory == null) {
			factory = SerializerFactory.createDefault();
			this.defaultSerializerFactory = factory;
			this.serializerFactory = factory;
		}

		return factory;
	}

	public void setCloseStreamOnClose(boolean isClose) {
		Hessian2Input.isCloseStreamOnClose = isClose;
	}

	public boolean isCloseStreamOnClose() {
		return Hessian2Input.isCloseStreamOnClose;
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
		return this.replyFault;
	}

	@Override
	public void init(InputStream is) {
		this.is = is;

		this.reset();
	}

	public void initPacket(InputStream is) {
		this.is = is;

		this.resetReferences();
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

		if (tag != 'C') {
			throw this.error("expected hessian call ('C') at " + this.codeName(tag));
		}

		return 0;
	}

	/**
	 * Starts reading the envelope
	 *
	 * <pre>
	 *  E major minor
	 * </pre>
	 */
	public int readEnvelope() throws IOException {
		int tag = this.read();
		int version = 0;

		if (tag == 'H') {
			int major = this.read();
			int minor = this.read();

			version = (major << 16) + minor;

			tag = this.read();
		}

		if (tag != 'E') {
			throw this.error("expected hessian Envelope ('E') at " + this.codeName(tag));
		}

		return version;
	}

	/**
	 * Completes reading the envelope
	 *
	 * <p>A successful completion will have a single value:
	 *
	 * <pre>
	 * Z
	 * </pre>
	 */
	public void completeEnvelope() throws IOException {
		int tag = this.read();

		if (tag != 'Z') {
			this.error("expected end of envelope at " + this.codeName(tag));
		}
	}

	/**
	 * Starts reading the call
	 *
	 * <p>A successful completion will have a single value:
	 *
	 * <pre>
	 * string
	 * </pre>
	 */
	@Override
	public String readMethod() throws IOException {
		this.method = this.readString();

		return this.method;
	}

	/**
	 * Returns the number of method arguments
	 *
	 * <pre>
	 *  int
	 * </pre>
	 */
	@Override
	public int readMethodArgLength() throws IOException {
		return this.readInt();
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

		this.readMethod();
	}

	public Object[] readArguments() throws IOException {
		int len = this.readInt();

		Object[] args = new Object[len];

		for (int i = 0; i < len; i++) {
			args[i] = this.readObject();
		}

		return args;
	}

	/**
	 * Completes reading the call
	 *
	 * <p>A successful completion will have a single value:
	 *
	 * <pre></pre>
	 */
	@Override
	public void completeCall() throws IOException {}

	/**
	 * Reads a reply as an object. If the reply has a fault, throws the exception.
	 */
	@Override
	public Object readReply(Class<?> expectedClass) throws Throwable {
		int tag = this.read();

		if (tag == 'R') {
			return this.readObject(expectedClass);
		} else if (tag == 'F') {
			HashMap<String, Object> map = (HashMap<String, Object>) this.readObject(HashMap.class);

			throw this.prepareFault(map);
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append((char) tag);

			try {
				int ch;

				while ((ch = this.read()) >= 0) {
					sb.append((char) ch);
				}
			} catch (IOException e) {
				Hessian2Input.logger.info(e.toString(), e);
			}

			throw this.error("expected hessian reply at " + this.codeName(tag) + "\n" + sb);
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
		// XXX: for variable length (?)

		this.readReply(Object.class);
	}

	/**
	 * Prepares the fault.
	 */
	private Throwable prepareFault(HashMap<String, Object> fault) throws IOException {
		Object detail = fault.get("detail");
		String message = (String) fault.get("message");

		if (detail instanceof Throwable) {
			this.replyFault = (Throwable) detail;

			if ((message != null) && (Hessian2Input.detailMessageField != null)) {
				try {
					Hessian2Input.detailMessageField.set(this.replyFault, message);
				} catch (Exception e) {
					Hessian2Input.logger.trace(null, e);
				}
			}
			return this.replyFault;
		} else {
			String code = (String) fault.get("code");
			this.replyFault = new HessianServiceException(message, code, detail);
			return this.replyFault;
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
	public void completeReply() throws IOException {}

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

		if (tag != 'Z') {
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
		return null;
	}

	/**
	 * Starts reading a packet
	 *
	 * <pre>
	 *  p major minor
	 * </pre>
	 */
	public int startMessage() throws IOException {
		int tag = this.read();

		if (tag == 'p') {
		} else if (tag == 'P') {
		} else {
			throw this.error("expected Hessian message ('p') at " + this.codeName(tag));
		}

		int major = this.read();
		int minor = this.read();

		return (major << 16) + minor;
	}

	/**
	 * Completes reading the message
	 *
	 * <p>A successful completion will have a single value:
	 *
	 * <pre>
	 * z
	 * </pre>
	 */
	public void completeMessage() throws IOException {
		int tag = this.read();

		if (tag != 'Z') {
			this.error("expected end of message at " + this.codeName(tag));
		}
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
		int tag = this.offset < this.length ? this.buffer[this.offset++] & 0xff : this.read();
		if ('T' == tag) {
			return true;
		} else if ('F' == tag) {
			return false;
		} else if (FlowControlTools.isBetween(tag, 0x80, 0xbf)) {
			// direct integer
			return tag != Hessian2Constants.BC_INT_ZERO;
		} else if (0xc8 == tag) {
			// INT_BYTE = 0
			return this.read() != 0;
		} else if (FlowControlTools.isBetween(tag, 0xc0, 0xc7) || FlowControlTools.isBetween(tag, 0xc9, 0xcf)) {
			// INT_BYTE != 0
			this.read();
			return true;
		} else if (0xd4 == tag) {
			// INT_SHORT = 0
			return ((256 * this.read()) + this.read()) != 0;
		} else if (FlowControlTools.isBetween(tag, 0xd0, 0xd3) || FlowControlTools.isBetween(tag, 0xd5, 0xd7)) {
			// INT_SHORT != 0
			this.read();
			this.read();
			return true;
		} else if ('I' == tag) {
			return this.parseInt() != 0;
		} else if (FlowControlTools.isBetween(tag, 0xd8, 0xef)) {
			return tag != Hessian2Constants.BC_LONG_ZERO;
		} else if (0xf8 == tag) {
			// LONG_BYTE = 0
			return this.read() != 0;
		} else if (FlowControlTools.isBetween(tag, 0xf0, 0xff)) {
			// LONG_BYTE != 0
			this.read();
			return true;
		} else if (0x3c == tag) {
			// INT_SHORT = 0
			return ((256 * this.read()) + this.read()) != 0;
		} else if (FlowControlTools.isBetween(tag, 0x38, 0x3b) || FlowControlTools.isBetween(tag, 0x3d, 0x3f)) {
			// INT_SHORT != 0
			this.read();
			this.read();
			return true;
		} else if (Hessian2Constants.BC_LONG_INT == tag) {
			return ((0x1000000L * this.read()) + (0x10000L * this.read()) + (0x100 * this.read()) + this.read()) != 0;
		} else if ('L' == tag) {
			return this.parseLong() != 0;
		} else if (Hessian2Constants.BC_DOUBLE_ZERO == tag) {
			return false;
		} else if (Hessian2Constants.BC_DOUBLE_ONE == tag) {
			return true;
		} else if (Hessian2Constants.BC_DOUBLE_BYTE == tag) {
			return this.read() != 0;
		} else if (Hessian2Constants.BC_DOUBLE_SHORT == tag) {
			return ((0x100 * this.read()) + this.read()) != 0;
		} else if (Hessian2Constants.BC_DOUBLE_MILL == tag) {
			int mills = this.parseInt();
			return mills != 0;
		} else if ('D' == tag) {
			return this.parseDouble() != 0.0;
		} else if ('N' == tag) {
			return false;
		} else {
			throw this.expect("boolean", tag);
		}
	}

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
	public final int readInt() throws IOException {
		// int tag = _offset < _length ? (_buffer[_offset++] & 0xff) : read();
		int tag = this.read();

		switch (tag) {
			case 'N':
				return 0;

			case 'F':
				return 0;

			case 'T':
				return 1;

				// direct integer
			case 0x80:
			case 0x81:
			case 0x82:
			case 0x83:
			case 0x84:
			case 0x85:
			case 0x86:
			case 0x87:
			case 0x88:
			case 0x89:
			case 0x8a:
			case 0x8b:
			case 0x8c:
			case 0x8d:
			case 0x8e:
			case 0x8f:

			case 0x90:
			case 0x91:
			case 0x92:
			case 0x93:
			case 0x94:
			case 0x95:
			case 0x96:
			case 0x97:
			case 0x98:
			case 0x99:
			case 0x9a:
			case 0x9b:
			case 0x9c:
			case 0x9d:
			case 0x9e:
			case 0x9f:

			case 0xa0:
			case 0xa1:
			case 0xa2:
			case 0xa3:
			case 0xa4:
			case 0xa5:
			case 0xa6:
			case 0xa7:
			case 0xa8:
			case 0xa9:
			case 0xaa:
			case 0xab:
			case 0xac:
			case 0xad:
			case 0xae:
			case 0xaf:

			case 0xb0:
			case 0xb1:
			case 0xb2:
			case 0xb3:
			case 0xb4:
			case 0xb5:
			case 0xb6:
			case 0xb7:
			case 0xb8:
			case 0xb9:
			case 0xba:
			case 0xbb:
			case 0xbc:
			case 0xbd:
			case 0xbe:
			case 0xbf:
				return tag - Hessian2Constants.BC_INT_ZERO;

				/* byte int */
			case 0xc0:
			case 0xc1:
			case 0xc2:
			case 0xc3:
			case 0xc4:
			case 0xc5:
			case 0xc6:
			case 0xc7:
			case 0xc8:
			case 0xc9:
			case 0xca:
			case 0xcb:
			case 0xcc:
			case 0xcd:
			case 0xce:
			case 0xcf:
				return ((tag - Hessian2Constants.BC_INT_BYTE_ZERO) << 8) + this.read();

				/* short int */
			case 0xd0:
			case 0xd1:
			case 0xd2:
			case 0xd3:
			case 0xd4:
			case 0xd5:
			case 0xd6:
			case 0xd7:
				return ((tag - Hessian2Constants.BC_INT_SHORT_ZERO) << 16) + (256 * this.read()) + this.read();

			case 'I':
			case BC_LONG_INT:
				return (this.read() << 24) + (this.read() << 16) + (this.read() << 8) + this.read();

				// direct long
			case 0xd8:
			case 0xd9:
			case 0xda:
			case 0xdb:
			case 0xdc:
			case 0xdd:
			case 0xde:
			case 0xdf:

			case 0xe0:
			case 0xe1:
			case 0xe2:
			case 0xe3:
			case 0xe4:
			case 0xe5:
			case 0xe6:
			case 0xe7:
			case 0xe8:
			case 0xe9:
			case 0xea:
			case 0xeb:
			case 0xec:
			case 0xed:
			case 0xee:
			case 0xef:
				return tag - Hessian2Constants.BC_LONG_ZERO;

				/* byte long */
			case 0xf0:
			case 0xf1:
			case 0xf2:
			case 0xf3:
			case 0xf4:
			case 0xf5:
			case 0xf6:
			case 0xf7:
			case 0xf8:
			case 0xf9:
			case 0xfa:
			case 0xfb:
			case 0xfc:
			case 0xfd:
			case 0xfe:
			case 0xff:
				return ((tag - Hessian2Constants.BC_LONG_BYTE_ZERO) << 8) + this.read();

				/* short long */
			case 0x38:
			case 0x39:
			case 0x3a:
			case 0x3b:
			case 0x3c:
			case 0x3d:
			case 0x3e:
			case 0x3f:
				return ((tag - Hessian2Constants.BC_LONG_SHORT_ZERO) << 16) + (256 * this.read()) + this.read();

			case 'L':
				return (int) this.parseLong();

			case BC_DOUBLE_ZERO:
				return 0;

			case BC_DOUBLE_ONE:
				return 1;

				// case LONG_BYTE:
			case BC_DOUBLE_BYTE:
				return (byte) (this.offset < this.length ? this.buffer[this.offset++] : this.read());

				// case INT_SHORT:
				// case LONG_SHORT:
			case BC_DOUBLE_SHORT:
				return (short) ((256 * this.read()) + this.read());

			case BC_DOUBLE_MILL: {
				int mills = this.parseInt();

				return (int) (0.001 * mills);
			}

			case 'D':
				return (int) this.parseDouble();

			default:
				throw this.expect("integer", tag);
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
			case 'N':
				return 0;

			case 'F':
				return 0;

			case 'T':
				return 1;

				// direct integer
			case 0x80:
			case 0x81:
			case 0x82:
			case 0x83:
			case 0x84:
			case 0x85:
			case 0x86:
			case 0x87:
			case 0x88:
			case 0x89:
			case 0x8a:
			case 0x8b:
			case 0x8c:
			case 0x8d:
			case 0x8e:
			case 0x8f:

			case 0x90:
			case 0x91:
			case 0x92:
			case 0x93:
			case 0x94:
			case 0x95:
			case 0x96:
			case 0x97:
			case 0x98:
			case 0x99:
			case 0x9a:
			case 0x9b:
			case 0x9c:
			case 0x9d:
			case 0x9e:
			case 0x9f:

			case 0xa0:
			case 0xa1:
			case 0xa2:
			case 0xa3:
			case 0xa4:
			case 0xa5:
			case 0xa6:
			case 0xa7:
			case 0xa8:
			case 0xa9:
			case 0xaa:
			case 0xab:
			case 0xac:
			case 0xad:
			case 0xae:
			case 0xaf:

			case 0xb0:
			case 0xb1:
			case 0xb2:
			case 0xb3:
			case 0xb4:
			case 0xb5:
			case 0xb6:
			case 0xb7:
			case 0xb8:
			case 0xb9:
			case 0xba:
			case 0xbb:
			case 0xbc:
			case 0xbd:
			case 0xbe:
			case 0xbf:
				return tag - Hessian2Constants.BC_INT_ZERO;

				/* byte int */
			case 0xc0:
			case 0xc1:
			case 0xc2:
			case 0xc3:
			case 0xc4:
			case 0xc5:
			case 0xc6:
			case 0xc7:
			case 0xc8:
			case 0xc9:
			case 0xca:
			case 0xcb:
			case 0xcc:
			case 0xcd:
			case 0xce:
			case 0xcf:
				return ((tag - Hessian2Constants.BC_INT_BYTE_ZERO) << 8) + this.read();

				/* short int */
			case 0xd0:
			case 0xd1:
			case 0xd2:
			case 0xd3:
			case 0xd4:
			case 0xd5:
			case 0xd6:
			case 0xd7:
				return ((tag - Hessian2Constants.BC_INT_SHORT_ZERO) << 16) + (256 * this.read()) + this.read();

				// case LONG_BYTE:
			case BC_DOUBLE_BYTE:
				return (byte) (this.offset < this.length ? this.buffer[this.offset++] : this.read());

				// case INT_SHORT:
				// case LONG_SHORT:
			case BC_DOUBLE_SHORT:
				return (short) ((256 * this.read()) + this.read());

			case 'I':
			case BC_LONG_INT:
				return this.parseInt();

				// direct long
			case 0xd8:
			case 0xd9:
			case 0xda:
			case 0xdb:
			case 0xdc:
			case 0xdd:
			case 0xde:
			case 0xdf:

			case 0xe0:
			case 0xe1:
			case 0xe2:
			case 0xe3:
			case 0xe4:
			case 0xe5:
			case 0xe6:
			case 0xe7:
			case 0xe8:
			case 0xe9:
			case 0xea:
			case 0xeb:
			case 0xec:
			case 0xed:
			case 0xee:
			case 0xef:
				return tag - Hessian2Constants.BC_LONG_ZERO;

				/* byte long */
			case 0xf0:
			case 0xf1:
			case 0xf2:
			case 0xf3:
			case 0xf4:
			case 0xf5:
			case 0xf6:
			case 0xf7:
			case 0xf8:
			case 0xf9:
			case 0xfa:
			case 0xfb:
			case 0xfc:
			case 0xfd:
			case 0xfe:
			case 0xff:
				return ((tag - Hessian2Constants.BC_LONG_BYTE_ZERO) << 8) + this.read();

				/* short long */
			case 0x38:
			case 0x39:
			case 0x3a:
			case 0x3b:
			case 0x3c:
			case 0x3d:
			case 0x3e:
			case 0x3f:
				return ((tag - Hessian2Constants.BC_LONG_SHORT_ZERO) << 16) + (256 * this.read()) + this.read();

			case 'L':
				return this.parseLong();

			case BC_DOUBLE_ZERO:
				return 0;

			case BC_DOUBLE_ONE:
				return 1;

			case BC_DOUBLE_MILL: {
				int mills = this.parseInt();

				return (long) (0.001 * mills);
			}

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
			case 'N':
				return 0;

			case 'F':
				return 0;

			case 'T':
				return 1;

				// direct integer
			case 0x80:
			case 0x81:
			case 0x82:
			case 0x83:
			case 0x84:
			case 0x85:
			case 0x86:
			case 0x87:
			case 0x88:
			case 0x89:
			case 0x8a:
			case 0x8b:
			case 0x8c:
			case 0x8d:
			case 0x8e:
			case 0x8f:

			case 0x90:
			case 0x91:
			case 0x92:
			case 0x93:
			case 0x94:
			case 0x95:
			case 0x96:
			case 0x97:
			case 0x98:
			case 0x99:
			case 0x9a:
			case 0x9b:
			case 0x9c:
			case 0x9d:
			case 0x9e:
			case 0x9f:

			case 0xa0:
			case 0xa1:
			case 0xa2:
			case 0xa3:
			case 0xa4:
			case 0xa5:
			case 0xa6:
			case 0xa7:
			case 0xa8:
			case 0xa9:
			case 0xaa:
			case 0xab:
			case 0xac:
			case 0xad:
			case 0xae:
			case 0xaf:

			case 0xb0:
			case 0xb1:
			case 0xb2:
			case 0xb3:
			case 0xb4:
			case 0xb5:
			case 0xb6:
			case 0xb7:
			case 0xb8:
			case 0xb9:
			case 0xba:
			case 0xbb:
			case 0xbc:
			case 0xbd:
			case 0xbe:
			case 0xbf:
				return tag - 0x90;

				/* byte int */
			case 0xc0:
			case 0xc1:
			case 0xc2:
			case 0xc3:
			case 0xc4:
			case 0xc5:
			case 0xc6:
			case 0xc7:
			case 0xc8:
			case 0xc9:
			case 0xca:
			case 0xcb:
			case 0xcc:
			case 0xcd:
			case 0xce:
			case 0xcf:
				return ((tag - Hessian2Constants.BC_INT_BYTE_ZERO) << 8) + this.read();

				/* short int */
			case 0xd0:
			case 0xd1:
			case 0xd2:
			case 0xd3:
			case 0xd4:
			case 0xd5:
			case 0xd6:
			case 0xd7:
				return ((tag - Hessian2Constants.BC_INT_SHORT_ZERO) << 16) + (256 * this.read()) + this.read();

			case 'I':
			case BC_LONG_INT:
				return this.parseInt();

				// direct long
			case 0xd8:
			case 0xd9:
			case 0xda:
			case 0xdb:
			case 0xdc:
			case 0xdd:
			case 0xde:
			case 0xdf:

			case 0xe0:
			case 0xe1:
			case 0xe2:
			case 0xe3:
			case 0xe4:
			case 0xe5:
			case 0xe6:
			case 0xe7:
			case 0xe8:
			case 0xe9:
			case 0xea:
			case 0xeb:
			case 0xec:
			case 0xed:
			case 0xee:
			case 0xef:
				return tag - Hessian2Constants.BC_LONG_ZERO;

				/* byte long */
			case 0xf0:
			case 0xf1:
			case 0xf2:
			case 0xf3:
			case 0xf4:
			case 0xf5:
			case 0xf6:
			case 0xf7:
			case 0xf8:
			case 0xf9:
			case 0xfa:
			case 0xfb:
			case 0xfc:
			case 0xfd:
			case 0xfe:
			case 0xff:
				return ((tag - Hessian2Constants.BC_LONG_BYTE_ZERO) << 8) + this.read();

				/* short long */
			case 0x38:
			case 0x39:
			case 0x3a:
			case 0x3b:
			case 0x3c:
			case 0x3d:
			case 0x3e:
			case 0x3f:
				return ((tag - Hessian2Constants.BC_LONG_SHORT_ZERO) << 16) + (256 * this.read()) + this.read();

			case 'L':
				return this.parseLong();

			case BC_DOUBLE_ZERO:
				return 0;

			case BC_DOUBLE_ONE:
				return 1;

			case BC_DOUBLE_BYTE:
				return (byte) (this.offset < this.length ? this.buffer[this.offset++] : this.read());

			case BC_DOUBLE_SHORT:
				return (short) ((256 * this.read()) + this.read());

			case BC_DOUBLE_MILL: {
				int mills = this.parseInt();

				return 0.001 * mills;
			}

			case 'D':
				return this.parseDouble();

			default:
				throw this.expect("double", tag);
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

		if (tag == Hessian2Constants.BC_DATE) {
			return this.parseLong();
		} else if (tag == Hessian2Constants.BC_DATE_MINUTE) {
			return this.parseInt() * 60000L;
		} else {
			throw this.expect("date", tag);
		}
	}

	/**
	 * Reads a byte from the stream.
	 */
	public int readChar() throws IOException {
		if (this.chunkLength > 0) {
			this.chunkLength--;
			if ((this.chunkLength == 0) && this.isLastChunk) {
				this.chunkLength = Hessian2Input.END_OF_DATA;
			}

			int ch = this.parseUTF8Char();
			return ch;
		} else if (this.chunkLength == Hessian2Input.END_OF_DATA) {
			this.chunkLength = 0;
			return -1;
		}

		int tag = this.read();

		switch (tag) {
			case 'N':
				return -1;

			case 'S':
			case BC_STRING_CHUNK:
				this.isLastChunk = tag == 'S';
				this.chunkLength = (this.read() << 8) + this.read();

				this.chunkLength--;
				int value = this.parseUTF8Char();

				// special code so successive read byte won't
				// be read as a single object.
				if ((this.chunkLength == 0) && this.isLastChunk) {
					this.chunkLength = Hessian2Input.END_OF_DATA;
				}

				return value;

			default:
				throw this.expect("char", tag);
		}
	}

	/**
	 * Reads a byte array from the stream.
	 */
	public int readString(char[] buffer, int offset, int length) throws IOException {
		int readLength = 0;

		if (this.chunkLength == Hessian2Input.END_OF_DATA) {
			this.chunkLength = 0;
			return -1;
		} else if (this.chunkLength == 0) {
			int tag = this.read();

			switch (tag) {
				case 'N':
					return -1;

				case 'S':
				case BC_STRING_CHUNK:
					this.isLastChunk = tag == 'S';
					this.chunkLength = (this.read() << 8) + this.read();
					break;

				case 0x00:
				case 0x01:
				case 0x02:
				case 0x03:
				case 0x04:
				case 0x05:
				case 0x06:
				case 0x07:
				case 0x08:
				case 0x09:
				case 0x0a:
				case 0x0b:
				case 0x0c:
				case 0x0d:
				case 0x0e:
				case 0x0f:

				case 0x10:
				case 0x11:
				case 0x12:
				case 0x13:
				case 0x14:
				case 0x15:
				case 0x16:
				case 0x17:
				case 0x18:
				case 0x19:
				case 0x1a:
				case 0x1b:
				case 0x1c:
				case 0x1d:
				case 0x1e:
				case 0x1f:
					this.isLastChunk = true;
					this.chunkLength = tag - 0x00;
					break;

				case 0x30:
				case 0x31:
				case 0x32:
				case 0x33:
					this.isLastChunk = true;
					this.chunkLength = ((tag - 0x30) * 256) + this.read();
					break;

				default:
					throw this.expect("string", tag);
			}
		}

		while (length > 0) {
			if (this.chunkLength > 0) {
				buffer[offset++] = (char) this.parseUTF8Char();
				this.chunkLength--;
				length--;
				readLength++;
			} else if (this.isLastChunk) {
				if (readLength == 0) {
					return -1;
				} else {
					this.chunkLength = Hessian2Input.END_OF_DATA;
					return readLength;
				}
			} else {
				int tag = this.read();

				switch (tag) {
					case 'S':
					case BC_STRING_CHUNK:
						this.isLastChunk = tag == 'S';
						this.chunkLength = (this.read() << 8) + this.read();
						break;

					case 0x00:
					case 0x01:
					case 0x02:
					case 0x03:
					case 0x04:
					case 0x05:
					case 0x06:
					case 0x07:
					case 0x08:
					case 0x09:
					case 0x0a:
					case 0x0b:
					case 0x0c:
					case 0x0d:
					case 0x0e:
					case 0x0f:

					case 0x10:
					case 0x11:
					case 0x12:
					case 0x13:
					case 0x14:
					case 0x15:
					case 0x16:
					case 0x17:
					case 0x18:
					case 0x19:
					case 0x1a:
					case 0x1b:
					case 0x1c:
					case 0x1d:
					case 0x1e:
					case 0x1f:
						this.isLastChunk = true;
						this.chunkLength = tag - 0x00;
						break;

					case 0x30:
					case 0x31:
					case 0x32:
					case 0x33:
						this.isLastChunk = true;
						this.chunkLength = ((tag - 0x30) * 256) + this.read();
						break;

					default:
						throw this.expect("string", tag);
				}
			}
		}

		if (readLength == 0) {
			return -1;
		} else if ((this.chunkLength > 0) || !this.isLastChunk) {
			return readLength;
		} else {
			this.chunkLength = Hessian2Input.END_OF_DATA;
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
			case 'T':
				return "true";
			case 'F':
				return "false";

				// direct integer
			case 0x80:
			case 0x81:
			case 0x82:
			case 0x83:
			case 0x84:
			case 0x85:
			case 0x86:
			case 0x87:
			case 0x88:
			case 0x89:
			case 0x8a:
			case 0x8b:
			case 0x8c:
			case 0x8d:
			case 0x8e:
			case 0x8f:

			case 0x90:
			case 0x91:
			case 0x92:
			case 0x93:
			case 0x94:
			case 0x95:
			case 0x96:
			case 0x97:
			case 0x98:
			case 0x99:
			case 0x9a:
			case 0x9b:
			case 0x9c:
			case 0x9d:
			case 0x9e:
			case 0x9f:

			case 0xa0:
			case 0xa1:
			case 0xa2:
			case 0xa3:
			case 0xa4:
			case 0xa5:
			case 0xa6:
			case 0xa7:
			case 0xa8:
			case 0xa9:
			case 0xaa:
			case 0xab:
			case 0xac:
			case 0xad:
			case 0xae:
			case 0xaf:

			case 0xb0:
			case 0xb1:
			case 0xb2:
			case 0xb3:
			case 0xb4:
			case 0xb5:
			case 0xb6:
			case 0xb7:
			case 0xb8:
			case 0xb9:
			case 0xba:
			case 0xbb:
			case 0xbc:
			case 0xbd:
			case 0xbe:
			case 0xbf:
				return String.valueOf(tag - 0x90);

				/* byte int */
			case 0xc0:
			case 0xc1:
			case 0xc2:
			case 0xc3:
			case 0xc4:
			case 0xc5:
			case 0xc6:
			case 0xc7:
			case 0xc8:
			case 0xc9:
			case 0xca:
			case 0xcb:
			case 0xcc:
			case 0xcd:
			case 0xce:
			case 0xcf:
				return String.valueOf(((tag - Hessian2Constants.BC_INT_BYTE_ZERO) << 8) + this.read());

				/* short int */
			case 0xd0:
			case 0xd1:
			case 0xd2:
			case 0xd3:
			case 0xd4:
			case 0xd5:
			case 0xd6:
			case 0xd7:
				return String.valueOf(((tag - Hessian2Constants.BC_INT_SHORT_ZERO) << 16) + (256 * this.read()) + this.read());

			case 'I':
			case BC_LONG_INT:
				return String.valueOf(this.parseInt());

				// direct long
			case 0xd8:
			case 0xd9:
			case 0xda:
			case 0xdb:
			case 0xdc:
			case 0xdd:
			case 0xde:
			case 0xdf:

			case 0xe0:
			case 0xe1:
			case 0xe2:
			case 0xe3:
			case 0xe4:
			case 0xe5:
			case 0xe6:
			case 0xe7:
			case 0xe8:
			case 0xe9:
			case 0xea:
			case 0xeb:
			case 0xec:
			case 0xed:
			case 0xee:
			case 0xef:
				return String.valueOf(tag - Hessian2Constants.BC_LONG_ZERO);

				/* byte long */
			case 0xf0:
			case 0xf1:
			case 0xf2:
			case 0xf3:
			case 0xf4:
			case 0xf5:
			case 0xf6:
			case 0xf7:
			case 0xf8:
			case 0xf9:
			case 0xfa:
			case 0xfb:
			case 0xfc:
			case 0xfd:
			case 0xfe:
			case 0xff:
				return String.valueOf(((tag - Hessian2Constants.BC_LONG_BYTE_ZERO) << 8) + this.read());

				/* short long */
			case 0x38:
			case 0x39:
			case 0x3a:
			case 0x3b:
			case 0x3c:
			case 0x3d:
			case 0x3e:
			case 0x3f:
				return String.valueOf(((tag - Hessian2Constants.BC_LONG_SHORT_ZERO) << 16) + (256 * this.read()) + this.read());

			case 'L':
				return String.valueOf(this.parseLong());

			case BC_DOUBLE_ZERO:
				return "0.0";

			case BC_DOUBLE_ONE:
				return "1.0";

			case BC_DOUBLE_BYTE:
				return String.valueOf((byte) (this.offset < this.length ? this.buffer[this.offset++] : this.read()));

			case BC_DOUBLE_SHORT:
				return String.valueOf((short) ((256 * this.read()) + this.read()));

			case BC_DOUBLE_MILL: {
				int mills = this.parseInt();

				return String.valueOf(0.001 * mills);
			}

			case 'D':
				return String.valueOf(this.parseDouble());

			case 'S':
			case BC_STRING_CHUNK:
				this.isLastChunk = tag == 'S';
				this.chunkLength = (this.read() << 8) + this.read();

				this.sbuf.setLength(0);
				int ch;

				while ((ch = this.parseChar()) >= 0) {
					this.sbuf.append((char) ch);
				}

				return this.sbuf.toString();

				// 0-byte string
			case 0x00:
			case 0x01:
			case 0x02:
			case 0x03:
			case 0x04:
			case 0x05:
			case 0x06:
			case 0x07:
			case 0x08:
			case 0x09:
			case 0x0a:
			case 0x0b:
			case 0x0c:
			case 0x0d:
			case 0x0e:
			case 0x0f:

			case 0x10:
			case 0x11:
			case 0x12:
			case 0x13:
			case 0x14:
			case 0x15:
			case 0x16:
			case 0x17:
			case 0x18:
			case 0x19:
			case 0x1a:
			case 0x1b:
			case 0x1c:
			case 0x1d:
			case 0x1e:
			case 0x1f:
				this.isLastChunk = true;
				this.chunkLength = tag - 0x00;

				this.sbuf.setLength(0);

				while ((ch = this.parseChar()) >= 0) {
					this.sbuf.append((char) ch);
				}

				return this.sbuf.toString();

			case 0x30:
			case 0x31:
			case 0x32:
			case 0x33:
				this.isLastChunk = true;
				this.chunkLength = ((tag - 0x30) * 256) + this.read();

				this.sbuf.setLength(0);

				while ((ch = this.parseChar()) >= 0) {
					this.sbuf.append((char) ch);
				}

				return this.sbuf.toString();

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

			case BC_BINARY:
			case BC_BINARY_CHUNK:
				this.isLastChunk = tag == Hessian2Constants.BC_BINARY;
				this.chunkLength = (this.read() << 8) + this.read();

				ByteArrayOutputStream bos = new ByteArrayOutputStream();

				int data;
				while ((data = this.parseByte()) >= 0) {
					bos.write(data);
				}

				return bos.toByteArray();

			case 0x20:
			case 0x21:
			case 0x22:
			case 0x23:
			case 0x24:
			case 0x25:
			case 0x26:
			case 0x27:
			case 0x28:
			case 0x29:
			case 0x2a:
			case 0x2b:
			case 0x2c:
			case 0x2d:
			case 0x2e:
			case 0x2f: {
				this.isLastChunk = true;
				this.chunkLength = tag - 0x20;

				byte[] buffer = new byte[this.chunkLength];

				int offset = 0;
				while (offset < this.chunkLength) {
					int sublen = this.read(buffer, 0, this.chunkLength - offset);

					if (sublen <= 0) {
						break;
					}

					offset += sublen;
				}

				return buffer;
			}

			case 0x34:
			case 0x35:
			case 0x36:
			case 0x37: {
				this.isLastChunk = true;
				this.chunkLength = ((tag - 0x34) * 256) + this.read();

				byte[] buffer = new byte[this.chunkLength];

				int offset = 0;
				while (offset < this.chunkLength) {
					int sublen = this.read(buffer, 0, this.chunkLength - offset);

					if (sublen <= 0) {
						break;
					}

					offset += sublen;
				}

				return buffer;
			}

			default:
				throw this.expect("bytes", tag);
		}
	}

	/**
	 * Reads a byte from the stream.
	 */
	public int readByte() throws IOException {
		if (this.chunkLength > 0) {
			this.chunkLength--;
			if ((this.chunkLength == 0) && this.isLastChunk) {
				this.chunkLength = Hessian2Input.END_OF_DATA;
			}

			return this.read();
		} else if (this.chunkLength == Hessian2Input.END_OF_DATA) {
			this.chunkLength = 0;
			return -1;
		}

		int tag = this.read();

		switch (tag) {
			case 'N':
				return -1;

			case 'B':
			case BC_BINARY_CHUNK: {
				this.isLastChunk = tag == 'B';
				this.chunkLength = (this.read() << 8) + this.read();

				int value = this.parseByte();

				// special code so successive read byte won't
				// be read as a single object.
				if ((this.chunkLength == 0) && this.isLastChunk) {
					this.chunkLength = Hessian2Input.END_OF_DATA;
				}

				return value;
			}

			case 0x20:
			case 0x21:
			case 0x22:
			case 0x23:
			case 0x24:
			case 0x25:
			case 0x26:
			case 0x27:
			case 0x28:
			case 0x29:
			case 0x2a:
			case 0x2b:
			case 0x2c:
			case 0x2d:
			case 0x2e:
			case 0x2f: {
				this.isLastChunk = true;
				this.chunkLength = tag - 0x20;

				int value = this.parseByte();

				// special code so successive read byte won't
				// be read as a single object.
				if (this.chunkLength == 0) {
					this.chunkLength = Hessian2Input.END_OF_DATA;
				}

				return value;
			}

			case 0x34:
			case 0x35:
			case 0x36:
			case 0x37: {
				this.isLastChunk = true;
				this.chunkLength = ((tag - 0x34) * 256) + this.read();

				int value = this.parseByte();

				// special code so successive read byte won't
				// be read as a single object.
				if (this.chunkLength == 0) {
					this.chunkLength = Hessian2Input.END_OF_DATA;
				}

				return value;
			}

			default:
				throw this.expect("binary", tag);
		}
	}

	/**
	 * Reads a byte array from the stream.
	 */
	public int readBytes(byte[] buffer, int offset, int length) throws IOException {
		int readLength = 0;

		if (this.chunkLength == Hessian2Input.END_OF_DATA) {
			this.chunkLength = 0;
			return -1;
		} else if (this.chunkLength == 0) {
			int tag = this.read();

			switch (tag) {
				case 'N':
					return -1;

				case 'B':
				case BC_BINARY_CHUNK:
					this.isLastChunk = tag == 'B';
					this.chunkLength = (this.read() << 8) + this.read();
					break;

				case 0x20:
				case 0x21:
				case 0x22:
				case 0x23:
				case 0x24:
				case 0x25:
				case 0x26:
				case 0x27:
				case 0x28:
				case 0x29:
				case 0x2a:
				case 0x2b:
				case 0x2c:
				case 0x2d:
				case 0x2e:
				case 0x2f: {
					this.isLastChunk = true;
					this.chunkLength = tag - 0x20;
					break;
				}

				case 0x34:
				case 0x35:
				case 0x36:
				case 0x37: {
					this.isLastChunk = true;
					this.chunkLength = ((tag - 0x34) * 256) + this.read();
					break;
				}

				default:
					throw this.expect("binary", tag);
			}
		}

		while (length > 0) {
			if (this.chunkLength > 0) {
				buffer[offset++] = (byte) this.read();
				this.chunkLength--;
				length--;
				readLength++;
			} else if (this.isLastChunk) {
				if (readLength == 0) {
					return -1;
				} else {
					this.chunkLength = Hessian2Input.END_OF_DATA;
					return readLength;
				}
			} else {
				int tag = this.read();

				switch (tag) {
					case 'B':
					case BC_BINARY_CHUNK:
						this.isLastChunk = tag == 'B';
						this.chunkLength = (this.read() << 8) + this.read();
						break;

					default:
						throw this.expect("binary", tag);
				}
			}
		}

		if (readLength == 0) {
			return -1;
		} else if ((this.chunkLength > 0) || !this.isLastChunk) {
			return readLength;
		} else {
			this.chunkLength = Hessian2Input.END_OF_DATA;
			return readLength;
		}
	}

	/**
	 * Reads an object from the input stream with an expected type.
	 */
	@Override
	public Object readObject(Class<?> cl) throws IOException {
		if ((cl == null) || (cl == Object.class)) {
			return this.readObject();
		}

		int tag = this.offset < this.length ? this.buffer[this.offset++] & 0xff : this.read();
		Deserializer reader = null;
		int ref;
		int size;
		ObjectDefinition def;
		String type;
		switch (tag) {
			case 'N':
				return null;

			case 'H':
				reader = this.findSerializerFactory().getDeserializer(cl);
				return reader.readMap(this);

			case 'M':
				type = this.readType();
				// hessian/3bb3
				if ("".equals(type)) {
					reader = this.findSerializerFactory().getDeserializer(cl);
					return reader.readMap(this);
				}
				reader = this.findSerializerFactory().getObjectDeserializer(type, cl);
				return reader.readMap(this);

			case 'C':
				this.readObjectDefinition(cl);
				return this.readObject(cl);

			case 0x60:
			case 0x61:
			case 0x62:
			case 0x63:
			case 0x64:
			case 0x65:
			case 0x66:
			case 0x67:
			case 0x68:
			case 0x69:
			case 0x6a:
			case 0x6b:
			case 0x6c:
			case 0x6d:
			case 0x6e:
			case 0x6f:
				ref = tag - 0x60;
				size = this.classDefs.size();
				if ((ref < 0) || (size <= ref)) {
					throw new HessianProtocolException("'" + ref + "' is an unknown class definition");
				}
				def = this.classDefs.get(ref);
				return this.readObjectInstance(cl, def);

			case 'O':
				ref = this.readInt();
				size = this.classDefs.size();

				if ((ref < 0) || (size <= ref)) {
					throw new HessianProtocolException("'" + ref + "' is an unknown class definition");
				}
				def = this.classDefs.get(ref);
				return this.readObjectInstance(cl, def);

			case BC_LIST_VARIABLE:
				type = this.readType();
				reader = this.findSerializerFactory().getListDeserializer(type, cl);
				return reader.readList(this, -1);

			case BC_LIST_FIXED:
				type = this.readType();
				size = this.readInt();
				reader = this.findSerializerFactory().getListDeserializer(type, cl);
				return reader.readLengthList(this, size);

			case 0x70:
			case 0x71:
			case 0x72:
			case 0x73:
			case 0x74:
			case 0x75:
			case 0x76:
			case 0x77:
				size = tag - 0x70;
				type = this.readType();
				reader = this.findSerializerFactory().getListDeserializer(type, cl);
				return reader.readLengthList(this, size);

			case BC_LIST_VARIABLE_UNTYPED:
				reader = this.findSerializerFactory().getListDeserializer(null, cl);
				return reader.readList(this, -1);

			case BC_LIST_FIXED_UNTYPED:
				size = this.readInt();
				reader = this.findSerializerFactory().getListDeserializer(null, cl);
				return reader.readLengthList(this, size);

			case 0x78:
			case 0x79:
			case 0x7a:
			case 0x7b:
			case 0x7c:
			case 0x7d:
			case 0x7e:
			case 0x7f:
				size = tag - 0x78;
				reader = this.findSerializerFactory().getListDeserializer(null, cl);
				return reader.readLengthList(this, size);
			case BC_REF:
				ref = this.readInt();
				return this.refs.get(ref);
		}

		if (tag >= 0) {
			this.offset--;
		}

		// hessian/3b2i vs hessian/3406
		// return readObject();
		return this.findSerializerFactory().getDeserializer(cl).readObject(this);
	}

	/**
	 * Reads an arbitrary object from the input stream when the type is unknown.
	 */
	@Override
	public Object readObject() throws IOException {
		int tag = this.offset < this.length ? this.buffer[this.offset++] & 0xff : this.read();

		switch (tag) {
			case 'N':
				return null;

			case 'T':
				return Boolean.valueOf(true);

			case 'F':
				return Boolean.valueOf(false);

				// direct integer
			case 0x80:
			case 0x81:
			case 0x82:
			case 0x83:
			case 0x84:
			case 0x85:
			case 0x86:
			case 0x87:
			case 0x88:
			case 0x89:
			case 0x8a:
			case 0x8b:
			case 0x8c:
			case 0x8d:
			case 0x8e:
			case 0x8f:

			case 0x90:
			case 0x91:
			case 0x92:
			case 0x93:
			case 0x94:
			case 0x95:
			case 0x96:
			case 0x97:
			case 0x98:
			case 0x99:
			case 0x9a:
			case 0x9b:
			case 0x9c:
			case 0x9d:
			case 0x9e:
			case 0x9f:

			case 0xa0:
			case 0xa1:
			case 0xa2:
			case 0xa3:
			case 0xa4:
			case 0xa5:
			case 0xa6:
			case 0xa7:
			case 0xa8:
			case 0xa9:
			case 0xaa:
			case 0xab:
			case 0xac:
			case 0xad:
			case 0xae:
			case 0xaf:

			case 0xb0:
			case 0xb1:
			case 0xb2:
			case 0xb3:
			case 0xb4:
			case 0xb5:
			case 0xb6:
			case 0xb7:
			case 0xb8:
			case 0xb9:
			case 0xba:
			case 0xbb:
			case 0xbc:
			case 0xbd:
			case 0xbe:
			case 0xbf:
				return Integer.valueOf(tag - Hessian2Constants.BC_INT_ZERO);

				/* byte int */
			case 0xc0:
			case 0xc1:
			case 0xc2:
			case 0xc3:
			case 0xc4:
			case 0xc5:
			case 0xc6:
			case 0xc7:
			case 0xc8:
			case 0xc9:
			case 0xca:
			case 0xcb:
			case 0xcc:
			case 0xcd:
			case 0xce:
			case 0xcf:
				return Integer.valueOf(((tag - Hessian2Constants.BC_INT_BYTE_ZERO) << 8) + this.read());

				/* short int */
			case 0xd0:
			case 0xd1:
			case 0xd2:
			case 0xd3:
			case 0xd4:
			case 0xd5:
			case 0xd6:
			case 0xd7:
				return Integer.valueOf(((tag - Hessian2Constants.BC_INT_SHORT_ZERO) << 16) + (256 * this.read()) + this.read());

			case 'I':
				return Integer.valueOf(this.parseInt());

				// direct long
			case 0xd8:
			case 0xd9:
			case 0xda:
			case 0xdb:
			case 0xdc:
			case 0xdd:
			case 0xde:
			case 0xdf:

			case 0xe0:
			case 0xe1:
			case 0xe2:
			case 0xe3:
			case 0xe4:
			case 0xe5:
			case 0xe6:
			case 0xe7:
			case 0xe8:
			case 0xe9:
			case 0xea:
			case 0xeb:
			case 0xec:
			case 0xed:
			case 0xee:
			case 0xef:
				return Long.valueOf(tag - Hessian2Constants.BC_LONG_ZERO);

				/* byte long */
			case 0xf0:
			case 0xf1:
			case 0xf2:
			case 0xf3:
			case 0xf4:
			case 0xf5:
			case 0xf6:
			case 0xf7:
			case 0xf8:
			case 0xf9:
			case 0xfa:
			case 0xfb:
			case 0xfc:
			case 0xfd:
			case 0xfe:
			case 0xff:
				return Long.valueOf(((tag - Hessian2Constants.BC_LONG_BYTE_ZERO) << 8) + this.read());

				/* short long */
			case 0x38:
			case 0x39:
			case 0x3a:
			case 0x3b:
			case 0x3c:
			case 0x3d:
			case 0x3e:
			case 0x3f:
				return Long.valueOf(((tag - Hessian2Constants.BC_LONG_SHORT_ZERO) << 16) + (256 * this.read()) + this.read());

			case BC_LONG_INT:
				return Long.valueOf(this.parseInt());

			case 'L':
				return Long.valueOf(this.parseLong());

			case BC_DOUBLE_ZERO:
				return Double.valueOf(0);

			case BC_DOUBLE_ONE:
				return Double.valueOf(1);

			case BC_DOUBLE_BYTE:
				return Double.valueOf((byte) this.read());

			case BC_DOUBLE_SHORT:
				return Double.valueOf((short) ((256 * this.read()) + this.read()));

			case BC_DOUBLE_MILL: {
				int mills = this.parseInt();

				return Double.valueOf(0.001 * mills);
			}

			case 'D':
				return Double.valueOf(this.parseDouble());

			case BC_DATE:
				return new Date(this.parseLong());

			case BC_DATE_MINUTE:
				return new Date(this.parseInt() * 60000L);

			case BC_STRING_CHUNK:
			case 'S': {
				this.isLastChunk = tag == 'S';
				this.chunkLength = (this.read() << 8) + this.read();

				this.sbuf.setLength(0);

				this.parseString(this.sbuf);

				return this.sbuf.toString();
			}

			case 0x00:
			case 0x01:
			case 0x02:
			case 0x03:
			case 0x04:
			case 0x05:
			case 0x06:
			case 0x07:
			case 0x08:
			case 0x09:
			case 0x0a:
			case 0x0b:
			case 0x0c:
			case 0x0d:
			case 0x0e:
			case 0x0f:

			case 0x10:
			case 0x11:
			case 0x12:
			case 0x13:
			case 0x14:
			case 0x15:
			case 0x16:
			case 0x17:
			case 0x18:
			case 0x19:
			case 0x1a:
			case 0x1b:
			case 0x1c:
			case 0x1d:
			case 0x1e:
			case 0x1f: {
				this.isLastChunk = true;
				this.chunkLength = tag - 0x00;

				this.sbuf.setLength(0);

				this.parseString(this.sbuf);

				return this.sbuf.toString();
			}

			case 0x30:
			case 0x31:
			case 0x32:
			case 0x33: {
				this.isLastChunk = true;
				this.chunkLength = ((tag - 0x30) * 256) + this.read();

				this.sbuf.setLength(0);

				this.parseString(this.sbuf);

				return this.sbuf.toString();
			}

			case BC_BINARY_CHUNK:
			case 'B': {
				this.isLastChunk = tag == 'B';
				this.chunkLength = (this.read() << 8) + this.read();

				int data;
				ByteArrayOutputStream bos = new ByteArrayOutputStream();

				while ((data = this.parseByte()) >= 0) {
					bos.write(data);
				}

				return bos.toByteArray();
			}

			case 0x20:
			case 0x21:
			case 0x22:
			case 0x23:
			case 0x24:
			case 0x25:
			case 0x26:
			case 0x27:
			case 0x28:
			case 0x29:
			case 0x2a:
			case 0x2b:
			case 0x2c:
			case 0x2d:
			case 0x2e:
			case 0x2f: {
				this.isLastChunk = true;
				int len = tag - 0x20;
				this.chunkLength = 0;

				byte[] data = new byte[len];

				for (int i = 0; i < len; i++) {
					data[i] = (byte) this.read();
				}

				return data;
			}

			case 0x34:
			case 0x35:
			case 0x36:
			case 0x37: {
				this.isLastChunk = true;
				int len = ((tag - 0x34) * 256) + this.read();
				this.chunkLength = 0;

				byte[] buffer = new byte[len];

				for (int i = 0; i < len; i++) {
					buffer[i] = (byte) this.read();
				}

				return buffer;
			}

			case BC_LIST_VARIABLE: {
				// variable length list
				String type = this.readType();

				return this.findSerializerFactory().readList(this, -1, type);
			}

			case BC_LIST_VARIABLE_UNTYPED: {
				return this.findSerializerFactory().readList(this, -1, null);
			}

			case BC_LIST_FIXED: {
				// fixed length lists
				String type = this.readType();
				int length = this.readInt();

				Deserializer reader;
				reader = this.findSerializerFactory().getListDeserializer(type, null);

				return reader.readLengthList(this, length);
			}

			case BC_LIST_FIXED_UNTYPED: {
				// fixed length lists
				int length = this.readInt();

				Deserializer reader;
				reader = this.findSerializerFactory().getListDeserializer(null, null);

				return reader.readLengthList(this, length);
			}

			// compact fixed list
			case 0x70:
			case 0x71:
			case 0x72:
			case 0x73:
			case 0x74:
			case 0x75:
			case 0x76:
			case 0x77: {
				// fixed length lists
				String type = this.readType();
				int length = tag - 0x70;

				Deserializer reader;
				reader = this.findSerializerFactory().getListDeserializer(type, null);

				return reader.readLengthList(this, length);
			}

			// compact fixed untyped list
			case 0x78:
			case 0x79:
			case 0x7a:
			case 0x7b:
			case 0x7c:
			case 0x7d:
			case 0x7e:
			case 0x7f: {
				// fixed length lists
				int length = tag - 0x78;

				Deserializer reader;
				reader = this.findSerializerFactory().getListDeserializer(null, null);

				return reader.readLengthList(this, length);
			}

			case 'H': {
				return this.findSerializerFactory().readMap(this, null);
			}

			case 'M': {
				String type = this.readType();

				return this.findSerializerFactory().readMap(this, type);
			}

			case 'C': {
				this.readObjectDefinition(null);

				return this.readObject();
			}

			case 0x60:
			case 0x61:
			case 0x62:
			case 0x63:
			case 0x64:
			case 0x65:
			case 0x66:
			case 0x67:
			case 0x68:
			case 0x69:
			case 0x6a:
			case 0x6b:
			case 0x6c:
			case 0x6d:
			case 0x6e:
			case 0x6f: {
				int ref = tag - 0x60;

				if (this.classDefs.size() <= ref) {
					throw this.error("No classes defined at reference '" + Integer.toHexString(tag) + "'");
				}

				ObjectDefinition def = this.classDefs.get(ref);

				return this.readObjectInstance(null, def);
			}

			case 'O': {
				int ref = this.readInt();

				if (this.classDefs.size() <= ref) {
					throw this.error("Illegal object reference #" + ref);
				}

				ObjectDefinition def = this.classDefs.get(ref);

				return this.readObjectInstance(null, def);
			}

			case BC_REF: {
				int ref = this.readInt();

				return this.refs.get(ref);
			}

			default:
				if (tag < 0) {
					throw new EOFException("readObject: unexpected end of file");
				} else {
					throw this.error("readObject: unknown code " + this.codeName(tag));
				}
		}
	}

	/**
	 * Reads an object definition:
	 *
	 * <pre>
	 *  O string <int> (string)* <value>*
	 * </pre>
	 */
	private void readObjectDefinition(Class<?> cl) throws IOException {
		String type = this.readString();
		int len = this.readInt();

		SerializerFactory factory = this.findSerializerFactory();

		Deserializer reader = factory.getObjectDeserializer(type, null);

		Object[] fields = reader.createFields(len);
		String[] fieldNames = new String[len];

		for (int i = 0; i < len; i++) {
			String name = this.readString();

			fields[i] = reader.createField(name);
			fieldNames[i] = name;
		}

		ObjectDefinition def = new ObjectDefinition(type, reader, fields, fieldNames);

		this.classDefs.add(def);
	}

	private Object readObjectInstance(Class<?> cl, ObjectDefinition def) throws IOException {
		String type = def.getType();
		Deserializer reader = def.getReader();
		Object[] fields = def.getFields();

		SerializerFactory factory = this.findSerializerFactory();

		if ((cl != reader.getType()) && (cl != null)) {
			reader = factory.getObjectDeserializer(type, cl);

			return reader.readObject(this, def.getFieldNames());
		} else {
			return reader.readObject(this, fields);
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
		int value = this.parseInt();

		return this.refs.get(value);
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
		int code;

		if (this.offset < this.length) {
			code = this.buffer[this.offset] & 0xff;
		} else {
			code = this.read();

			if (code >= 0) {
				this.offset--;
			}
		}

		return (code < 0) || (code == 'Z');
	}

	/**
	 * Reads the end byte.
	 */
	@Override
	public void readEnd() throws IOException {
		int code = this.offset < this.length ? this.buffer[this.offset++] & 0xff : this.read();

		if (code == 'Z') {
			return;
		} else if (code < 0) {
			throw this.error("unexpected end of file");
		} else {
			throw this.error("unknown code:" + this.codeName(code));
		}
	}

	/**
	 * Reads the end byte.
	 */
	@Override
	public void readMapEnd() throws IOException {
		int code = this.offset < this.length ? this.buffer[this.offset++] & 0xff : this.read();

		if (code != 'Z') {
			throw this.error("expected end of map ('Z') at '" + this.codeName(code) + "'");
		}
	}

	/**
	 * Reads the end byte.
	 */
	@Override
	public void readListEnd() throws IOException {
		int code = this.offset < this.length ? this.buffer[this.offset++] & 0xff : this.read();

		if (code != 'Z') {
			throw this.error("expected end of list ('Z') at '" + this.codeName(code) + "'");
		}
	}

	/**
	 * Adds a list/map reference.
	 */
	@Override
	public int addRef(Object ref) {
		if (this.refs == null) {
			this.refs = new ArrayList<>();
		}

		this.refs.add(ref);

		return this.refs.size() - 1;
	}

	/**
	 * Adds a list/map reference.
	 */
	@Override
	public void setRef(int i, Object ref) {
		this.refs.set(i, ref);
	}

	/**
	 * Resets the references for streaming.
	 */
	@Override
	public void resetReferences() {
		this.refs.clear();
	}

	public void reset() {
		this.resetReferences();

		this.classDefs.clear();
		this.types.clear();
	}

	public void resetBuffer() {
		int offset = this.offset;
		this.offset = 0;

		int length = this.length;
		this.length = 0;

		if ((length > 0) && (offset != length)) {
			throw new IllegalStateException("offset=" + offset + " length=" + length);
		}
	}

	public Object readStreamingObject() throws IOException {
		if (this.refs != null) {
			this.refs.clear();
		}

		return this.readObject();
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
	 *  type ::= string type ::= int
	 * </pre>
	 */
	@Override
	public String readType() throws IOException {
		int code = this.offset < this.length ? this.buffer[this.offset++] & 0xff : this.read();
		this.offset--;

		switch (code) {
			case 0x00:
			case 0x01:
			case 0x02:
			case 0x03:
			case 0x04:
			case 0x05:
			case 0x06:
			case 0x07:
			case 0x08:
			case 0x09:
			case 0x0a:
			case 0x0b:
			case 0x0c:
			case 0x0d:
			case 0x0e:
			case 0x0f:

			case 0x10:
			case 0x11:
			case 0x12:
			case 0x13:
			case 0x14:
			case 0x15:
			case 0x16:
			case 0x17:
			case 0x18:
			case 0x19:
			case 0x1a:
			case 0x1b:
			case 0x1c:
			case 0x1d:
			case 0x1e:
			case 0x1f:

			case 0x30:
			case 0x31:
			case 0x32:
			case 0x33:
			case BC_STRING_CHUNK:
			case 'S': {
				String type = this.readString();

				if (this.types == null) {
					this.types = new ArrayList<>();
				}

				this.types.add(type);

				return type;
			}

			default: {
				int ref = this.readInt();

				if (this.types.size() <= ref) {
					throw new IndexOutOfBoundsException("type ref #" + ref + " is greater than the number of valid types (" + this.types.size() + ")");
				}

				return this.types.get(ref);
			}
		}
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
		throw new UnsupportedOperationException();
	}

	/**
	 * Parses a 32-bit integer value from the stream.
	 *
	 * <pre>
	 *  b32 b24 b16 b8
	 * </pre>
	 */
	private int parseInt() throws IOException {
		int offset = this.offset;

		if ((offset + 3) < this.length) {
			byte[] buffer = this.buffer;

			int b32 = buffer[offset + 0] & 0xff;
			int b24 = buffer[offset + 1] & 0xff;
			int b16 = buffer[offset + 2] & 0xff;
			int b8 = buffer[offset + 3] & 0xff;

			this.offset = offset + 4;

			return (b32 << 24) + (b24 << 16) + (b16 << 8) + b8;
		} else {
			int b32 = this.read();
			int b24 = this.read();
			int b16 = this.read();
			int b8 = this.read();

			return (b32 << 24) + (b24 << 16) + (b16 << 8) + b8;
		}
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
		long bits = this.parseLong();

		return Double.longBitsToDouble(bits);
	}

	org.w3c.dom.Node parseXML() throws IOException {
		throw new UnsupportedOperationException();
	}

	private void parseString(StringBuilder sbuf) throws IOException {
		while (true) {
			if (this.chunkLength <= 0) {
				if (!this.parseChunkLength()) {
					return;
				}
			}

			int length = this.chunkLength;
			this.chunkLength = 0;

			while (length-- > 0) {
				sbuf.append((char) this.parseUTF8Char());
			}
		}
	}

	/**
	 * Reads a character from the underlying stream.
	 */
	private int parseChar() throws IOException {
		while (this.chunkLength <= 0) {
			if (!this.parseChunkLength()) {
				return -1;
			}
		}

		this.chunkLength--;

		return this.parseUTF8Char();
	}

	private boolean parseChunkLength() throws IOException {
		if (this.isLastChunk) {
			return false;
		}

		int code = this.offset < this.length ? this.buffer[this.offset++] & 0xff : this.read();

		switch (code) {
			case BC_STRING_CHUNK:
				this.isLastChunk = false;

				this.chunkLength = (this.read() << 8) + this.read();
				break;

			case 'S':
				this.isLastChunk = true;

				this.chunkLength = (this.read() << 8) + this.read();
				break;

			case 0x00:
			case 0x01:
			case 0x02:
			case 0x03:
			case 0x04:
			case 0x05:
			case 0x06:
			case 0x07:
			case 0x08:
			case 0x09:
			case 0x0a:
			case 0x0b:
			case 0x0c:
			case 0x0d:
			case 0x0e:
			case 0x0f:

			case 0x10:
			case 0x11:
			case 0x12:
			case 0x13:
			case 0x14:
			case 0x15:
			case 0x16:
			case 0x17:
			case 0x18:
			case 0x19:
			case 0x1a:
			case 0x1b:
			case 0x1c:
			case 0x1d:
			case 0x1e:
			case 0x1f:
				this.isLastChunk = true;
				this.chunkLength = code - 0x00;
				break;

			case 0x30:
			case 0x31:
			case 0x32:
			case 0x33:
				this.isLastChunk = true;
				this.chunkLength = ((code - 0x30) * 256) + this.read();
				break;

			default:
				throw this.expect("string", code);
		}

		return true;
	}

	/**
	 * Parses a single UTF8 character.
	 */
	private int parseUTF8Char() throws IOException {
		int ch = this.offset < this.length ? this.buffer[this.offset++] & 0xff : this.read();

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
		while (this.chunkLength <= 0) {
			if (this.isLastChunk) {
				return -1;
			}

			int code = this.read();

			switch (code) {
				case BC_BINARY_CHUNK:
					this.isLastChunk = false;

					this.chunkLength = (this.read() << 8) + this.read();
					break;

				case 'B':
					this.isLastChunk = true;

					this.chunkLength = (this.read() << 8) + this.read();
					break;

				case 0x20:
				case 0x21:
				case 0x22:
				case 0x23:
				case 0x24:
				case 0x25:
				case 0x26:
				case 0x27:
				case 0x28:
				case 0x29:
				case 0x2a:
				case 0x2b:
				case 0x2c:
				case 0x2d:
				case 0x2e:
				case 0x2f:
					this.isLastChunk = true;

					this.chunkLength = code - 0x20;
					break;

				case 0x34:
				case 0x35:
				case 0x36:
				case 0x37:
					this.isLastChunk = true;
					this.chunkLength = ((code - 0x34) * 256) + this.read();
					break;

				default:
					throw this.expect("byte[]", code);
			}
		}

		this.chunkLength--;

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

			case BC_BINARY:
			case BC_BINARY_CHUNK:
				this.isLastChunk = tag == Hessian2Constants.BC_BINARY;
				this.chunkLength = (this.read() << 8) + this.read();
				break;

			case 0x20:
			case 0x21:
			case 0x22:
			case 0x23:
			case 0x24:
			case 0x25:
			case 0x26:
			case 0x27:
			case 0x28:
			case 0x29:
			case 0x2a:
			case 0x2b:
			case 0x2c:
			case 0x2d:
			case 0x2e:
			case 0x2f:
				this.isLastChunk = true;
				this.chunkLength = tag - 0x20;
				break;

			case 0x34:
			case 0x35:
			case 0x36:
			case 0x37:
				this.isLastChunk = true;
				this.chunkLength = ((tag - 0x34) * 256) + this.read();
				break;

			default:
				throw this.expect("binary", tag);
		}

		return new ReadInputStream();
	}

	/**
	 * Reads bytes from the underlying stream.
	 */
	int read(byte[] buffer, int offset, int length) throws IOException {
		int readLength = 0;

		while (length > 0) {
			while (this.chunkLength <= 0) {
				if (this.isLastChunk) {
					return readLength == 0 ? -1 : readLength;
				}

				int code = this.read();

				switch (code) {
					case BC_BINARY_CHUNK:
						this.isLastChunk = false;

						this.chunkLength = (this.read() << 8) + this.read();
						break;

					case BC_BINARY:
						this.isLastChunk = true;

						this.chunkLength = (this.read() << 8) + this.read();
						break;

					case 0x20:
					case 0x21:
					case 0x22:
					case 0x23:
					case 0x24:
					case 0x25:
					case 0x26:
					case 0x27:
					case 0x28:
					case 0x29:
					case 0x2a:
					case 0x2b:
					case 0x2c:
					case 0x2d:
					case 0x2e:
					case 0x2f:
						this.isLastChunk = true;
						this.chunkLength = code - 0x20;
						break;

					case 0x34:
					case 0x35:
					case 0x36:
					case 0x37:
						this.isLastChunk = true;
						this.chunkLength = ((code - 0x34) * 256) + this.read();
						break;

					default:
						throw this.expect("byte[]", code);
				}
			}

			int sublen = this.chunkLength;
			if (length < sublen) {
				sublen = length;
			}

			if ((this.length <= this.offset) && !this.readBuffer()) {
				return -1;
			}

			if ((this.length - this.offset) < sublen) {
				sublen = this.length - this.offset;
			}

			System.arraycopy(this.buffer, this.offset, buffer, offset, sublen);

			this.offset += sublen;

			offset += sublen;
			readLength += sublen;
			length -= sublen;
			this.chunkLength -= sublen;
		}

		return readLength;
	}

	/**
	 * Normally, shouldn't be called externally, but needed for QA, e.g. ejb/3b01.
	 */
	public final int read() throws IOException {
		if ((this.length <= this.offset) && !this.readBuffer()) {
			return -1;
		}

		return this.buffer[this.offset++] & 0xff;
	}

	protected void unread() {
		if (this.offset <= 0) {
			throw new IllegalStateException();
		}

		this.offset--;
	}

	private final boolean readBuffer() throws IOException {
		byte[] buffer = this.buffer;
		int offset = this.offset;
		int length = this.length;

		if (offset < length) {
			System.arraycopy(buffer, offset, buffer, 0, length - offset);
			offset = length - offset;
		} else {
			offset = 0;
		}

		int len = this.is.read(buffer, offset, Hessian2Input.SIZE - offset);

		if (len <= 0) {
			this.length = offset;
			this.offset = 0;

			return offset > 0;
		}

		this.length = offset + len;
		this.offset = 0;

		return true;
	}

	@Override
	public Reader getReader() {
		return null;
	}

	protected IOException expect(String expect, int ch) throws IOException {
		if (ch < 0) {
			return this.error("expected " + expect + " at end of file");
		} else {
			this.offset--;

			try {
				int offset = this.offset;
				String context = this.buildDebugContext(this.buffer, 0, this.length, offset);

				Object obj = this.readObject();

				if (obj != null) {
					return this.error("expected " + expect + " at 0x" + Integer.toHexString(ch & 0xff) + " " + obj.getClass().getName() + " (" + obj + ")" + "\n  " + context + "");
				} else {
					return this.error("expected " + expect + " at 0x" + Integer.toHexString(ch & 0xff) + " null");
				}
			} catch (Exception e) {
				Hessian2Input.logger.info(e.toString(), e);

				return this.error("expected " + expect + " at 0x" + Integer.toHexString(ch & 0xff));
			}
		}
	}

	private String buildDebugContext(byte[] buffer, int offset, int length, int errorOffset) {
		StringBuilder sb = new StringBuilder();

		sb.append("[");
		for (int i = 0; i < errorOffset; i++) {
			int ch = buffer[offset + i];
			this.addDebugChar(sb, ch);
		}
		sb.append("] ");
		this.addDebugChar(sb, buffer[offset + errorOffset]);
		sb.append(" [");
		for (int i = errorOffset + 1; i < length; i++) {
			int ch = buffer[offset + i];
			this.addDebugChar(sb, ch);
		}
		sb.append("]");

		return sb.toString();
	}

	private void addDebugChar(StringBuilder sb, int ch) {
		if ((ch >= 0x20) && (ch < 0x7f)) {
			sb.append((char) ch);
		} else if (ch == '\n') {
			sb.append((char) ch);
		} else {
			sb.append(String.format("\\x%02x", ch & 0xff));
		}
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

	public void free() {
		this.reset();
	}

	@Override
	public void close() throws IOException {
		InputStream is = this.is;
		this.is = null;

		if (Hessian2Input.isCloseStreamOnClose && (is != null)) {
			is.close();
		}
	}

	class ReadInputStream extends InputStream {

		boolean _isClosed = false;

		@Override
		public int read() throws IOException {
			if (this._isClosed) {
				return -1;
			}

			int ch = Hessian2Input.this.parseByte();
			if (ch < 0) {
				this._isClosed = true;
			}

			return ch;
		}

		@Override
		public int read(byte[] buffer, int offset, int length) throws IOException {
			if (this._isClosed) {
				return -1;
			}

			int len = Hessian2Input.this.read(buffer, offset, length);
			if (len < 0) {
				this._isClosed = true;
			}

			return len;
		}

		@Override
		public void close() throws IOException {
			while (this.read() >= 0) {
				// do nothing
			}
		}
	}

	final static class ObjectDefinition {

		private final String		_type;
		private final Deserializer	_reader;
		private final Object[]		_fields;
		private final String[]		_fieldNames;

		ObjectDefinition(String type, Deserializer reader, Object[] fields, String[] fieldNames) {
			this._type = type;
			this._reader = reader;
			this._fields = fields;
			this._fieldNames = fieldNames;
		}

		String getType() {
			return this._type;
		}

		Deserializer getReader() {
			return this._reader;
		}

		Object[] getFields() {
			return this._fields;
		}

		String[] getFieldNames() {
			return this._fieldNames;
		}
	}

	static {
		try {
			Hessian2Input.detailMessageField = Throwable.class.getDeclaredField("detailMessage");
			Hessian2Input.detailMessageField.setAccessible(true);
		} catch (Exception e) {
			Hessian2Input.logger.trace(null, e);
		}
	}
}

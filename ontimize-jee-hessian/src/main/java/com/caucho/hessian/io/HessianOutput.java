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
import java.io.OutputStream;
import java.util.IdentityHashMap;

/**
 * Output stream for Hessian requests, compatible with microedition Java. It only uses classes and types available in JDK.
 *
 * <p>Since HessianOutput does not depend on any classes other than in the JDK, it can be extracted independently into a smaller package.
 *
 * <p>HessianOutput is unbuffered, so any client needs to provide its own buffering.
 *
 * <pre>
 *  OutputStream os = ...; // from http connection HessianOutput out = new HessianOutput(os); String value;
 *
 * out.startCall("hello"); // start hello call out.writeString("arg1"); // write a string argument out.completeCall(); // complete the call
 * </pre>
 */
public class HessianOutput extends AbstractHessianOutput {

	// the output stream/
	protected OutputStream						os;
	// map of references
	private IdentityHashMap<Object, Integer>	refs;
	private int									version	= 1;

	/**
	 * Creates a new Hessian output stream, initialized with an underlying output stream.
	 *
	 * @param os
	 *            the underlying output stream.
	 */
	public HessianOutput(OutputStream os) {
		this.init(os);
	}

	/**
	 * Creates an uninitialized Hessian output stream.
	 */
	public HessianOutput() {}

	/**
	 * Initializes the output
	 */
	@Override
	public void init(OutputStream os) {
		this.os = os;

		this.refs = null;

		if (this.serializerFactory == null) {
			this.serializerFactory = new SerializerFactory();
		}
	}

	/**
	 * Sets the client's version.
	 */
	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * Writes a complete method call.
	 */
	@Override
	public void call(String method, Object[] args) throws IOException {
		int length = args != null ? args.length : 0;

		this.startCall(method, length);

		for (int i = 0; i < length; i++) {
			this.writeObject(args[i]);
		}

		this.completeCall();
	}

	/**
	 * Starts the method call. Clients would use <code>startCall</code> instead of <code>call</code> if they wanted finer control over writing the arguments, or needed to write
	 * headers.
	 *
	 * <pre>
	 * <code> c major minor m b16 b8 method-name </code>
	 * </pre>
	 *
	 * @param method
	 *            the method name to call.
	 */
	@Override
	public void startCall(String method, int length) throws IOException {
		this.os.write('c');
		this.os.write(this.version);
		this.os.write(0);

		this.os.write('m');
		int len = method.length();
		this.os.write(len >> 8);
		this.os.write(len);
		this.printString(method, 0, len);
	}

	/**
	 * Writes the call tag. This would be followed by the headers and the method tag.
	 *
	 * <pre>
	 * <code> c major minor </code>
	 * </pre>
	 *
	 * @param method
	 *            the method name to call.
	 */
	@Override
	public void startCall() throws IOException {
		this.os.write('c');
		this.os.write(0);
		this.os.write(1);
	}

	/**
	 * Writes the method tag.
	 *
	 * <pre>
	 * <code> m b16 b8 method-name </code>
	 * </pre>
	 *
	 * @param method
	 *            the method name to call.
	 */
	@Override
	public void writeMethod(String method) throws IOException {
		this.os.write('m');
		int len = method.length();
		this.os.write(len >> 8);
		this.os.write(len);
		this.printString(method, 0, len);
	}

	/**
	 * Completes.
	 *
	 * <pre>
	 * <code> z </code>
	 * </pre>
	 */
	@Override
	public void completeCall() throws IOException {
		this.os.write('z');
	}

	/**
	 * Starts the reply
	 *
	 * <p>A successful completion will have a single value:
	 *
	 * <pre>
	 * r
	 * </pre>
	 */
	@Override
	public void startReply() throws IOException {
		this.os.write('r');
		this.os.write(1);
		this.os.write(0);
	}

	/**
	 * Completes reading the reply
	 *
	 * <p>A successful completion will have a single value:
	 *
	 * <pre>
	 * z
	 * </pre>
	 */
	@Override
	public void completeReply() throws IOException {
		this.os.write('z');
	}

	/**
	 * Writes a header name. The header value must immediately follow.
	 *
	 * <pre>
	 * <code> H b16 b8 foo <em>value</em> </code>
	 * </pre>
	 */
	@Override
	public void writeHeader(String name) throws IOException {
		int len = name.length();

		this.os.write('H');
		this.os.write(len >> 8);
		this.os.write(len);

		this.printString(name);
	}

	/**
	 * Writes a fault. The fault will be written as a descriptive string followed by an object:
	 *
	 * <pre>
	 * <code> f &lt;string>code &lt;string>the fault code
	 *
	 * &lt;string>message &lt;string>the fault mesage
	 *
	 * &lt;string>detail mt\x00\xnnjavax.ejb.FinderException ... z z </code>
	 * </pre>
	 *
	 * @param code
	 *            the fault code, a three digit
	 */
	@Override
	public void writeFault(String code, String message, Object detail) throws IOException {
		// hessian/3525
		this.os.write('r');
		this.os.write(1);
		this.os.write(0);

		this.os.write('f');
		this.writeString("code");
		this.writeString(code);

		this.writeString("message");
		this.writeString(message);

		if (detail != null) {
			this.writeString("detail");
			this.writeObject(detail);
		}
		this.os.write('z');

		this.os.write('z');
	}

	/**
	 * Writes any object to the output stream.
	 */
	@Override
	public void writeObject(Object object) throws IOException {
		if (object == null) {
			this.writeNull();
			return;
		}

		Serializer serializer;

		serializer = this.serializerFactory.getSerializer(object.getClass());

		serializer.writeObject(object, this);
	}

	/**
	 * Writes the list header to the stream. List writers will call <code>writeListBegin</code> followed by the list contents and then call <code>writeListEnd</code>.
	 *
	 * <pre>
	 * <code> V t b16 b8 type l b32 b24 b16 b8 </code>
	 * </pre>
	 */
	@Override
	public boolean writeListBegin(int length, String type) throws IOException {
		this.os.write('V');

		if (type != null) {
			this.os.write('t');
			this.printLenString(type);
		}

		if (length >= 0) {
			this.os.write('l');
			this.os.write(length >> 24);
			this.os.write(length >> 16);
			this.os.write(length >> 8);
			this.os.write(length);
		}

		return true;
	}

	/**
	 * Writes the tail of the list to the stream.
	 */
	@Override
	public void writeListEnd() throws IOException {
		this.os.write('z');
	}

	/**
	 * Writes the map header to the stream. Map writers will call <code>writeMapBegin</code> followed by the map contents and then call <code>writeMapEnd</code>.
	 *
	 * <pre>
	 * <code> Mt b16 b8 (<key> <value>)z </code>
	 * </pre>
	 */
	@Override
	public void writeMapBegin(String type) throws IOException {
		this.os.write('M');
		this.os.write('t');
		this.printLenString(type);
	}

	/**
	 * Writes the tail of the map to the stream.
	 */
	@Override
	public void writeMapEnd() throws IOException {
		this.os.write('z');
	}

	/**
	 * Writes a remote object reference to the stream. The type is the type of the remote interface.
	 *
	 * <pre>
	 * <code> 'r' 't' b16 b8 type url </code>
	 * </pre>
	 */
	public void writeRemote(String type, String url) throws IOException {
		this.os.write('r');
		this.os.write('t');
		this.printLenString(type);
		this.os.write('S');
		this.printLenString(url);
	}

	/**
	 * Writes a boolean value to the stream. The boolean will be written with the following syntax:
	 *
	 * <pre>
	 * <code> T F </code>
	 * </pre>
	 *
	 * @param value
	 *            the boolean value to write.
	 */
	@Override
	public void writeBoolean(boolean value) throws IOException {
		if (value) {
			this.os.write('T');
		} else {
			this.os.write('F');
		}
	}

	/**
	 * Writes an integer value to the stream. The integer will be written with the following syntax:
	 *
	 * <pre>
	 * <code> I b32 b24 b16 b8 </code>
	 * </pre>
	 *
	 * @param value
	 *            the integer value to write.
	 */
	@Override
	public void writeInt(int value) throws IOException {
		this.os.write('I');
		this.os.write(value >> 24);
		this.os.write(value >> 16);
		this.os.write(value >> 8);
		this.os.write(value);
	}

	/**
	 * Writes a long value to the stream. The long will be written with the following syntax:
	 *
	 * <pre>
	 * <code> L b64 b56 b48 b40 b32 b24 b16 b8 </code>
	 * </pre>
	 *
	 * @param value
	 *            the long value to write.
	 */
	@Override
	public void writeLong(long value) throws IOException {
		this.os.write('L');
		this.os.write((byte) (value >> 56));
		this.os.write((byte) (value >> 48));
		this.os.write((byte) (value >> 40));
		this.os.write((byte) (value >> 32));
		this.os.write((byte) (value >> 24));
		this.os.write((byte) (value >> 16));
		this.os.write((byte) (value >> 8));
		this.os.write((byte) value);
	}

	/**
	 * Writes a double value to the stream. The double will be written with the following syntax:
	 *
	 * <pre>
	 * <code> D b64 b56 b48 b40 b32 b24 b16 b8 </code>
	 * </pre>
	 *
	 * @param value
	 *            the double value to write.
	 */
	@Override
	public void writeDouble(double value) throws IOException {
		long bits = Double.doubleToLongBits(value);

		this.os.write('D');
		this.os.write((byte) (bits >> 56));
		this.os.write((byte) (bits >> 48));
		this.os.write((byte) (bits >> 40));
		this.os.write((byte) (bits >> 32));
		this.os.write((byte) (bits >> 24));
		this.os.write((byte) (bits >> 16));
		this.os.write((byte) (bits >> 8));
		this.os.write((byte) bits);
	}

	/**
	 * Writes a date to the stream.
	 *
	 * <pre>
	 * <code> T b64 b56 b48 b40 b32 b24 b16 b8 </code>
	 * </pre>
	 *
	 * @param time
	 *            the date in milliseconds from the epoch in UTC
	 */
	@Override
	public void writeUTCDate(long time) throws IOException {
		this.os.write('d');
		this.os.write((byte) (time >> 56));
		this.os.write((byte) (time >> 48));
		this.os.write((byte) (time >> 40));
		this.os.write((byte) (time >> 32));
		this.os.write((byte) (time >> 24));
		this.os.write((byte) (time >> 16));
		this.os.write((byte) (time >> 8));
		this.os.write((byte) time);
	}

	/**
	 * Writes a null value to the stream. The null will be written with the following syntax
	 *
	 * <pre>
	 * <code> N </code>
	 * </pre>
	 *
	 * @param value
	 *            the string value to write.
	 */
	@Override
	public void writeNull() throws IOException {
		this.os.write('N');
	}

	/**
	 * Writes a string value to the stream using UTF-8 encoding. The string will be written with the following syntax:
	 *
	 * <pre>
	 * <code> S b16 b8 string-value </code>
	 * </pre>
	 *
	 * If the value is null, it will be written as
	 *
	 * <pre>
	 * <code> N </code>
	 * </pre>
	 *
	 * @param value
	 *            the string value to write.
	 */
	@Override
	public void writeString(String value) throws IOException {
		if (value == null) {
			this.os.write('N');
		} else {
			int length = value.length();
			int offset = 0;

			while (length > 0x8000) {
				int sublen = 0x8000;

				// chunk can't end in high surrogate
				char tail = value.charAt((offset + sublen) - 1);

				if ((0xd800 <= tail) && (tail <= 0xdbff)) {
					sublen--;
				}

				this.os.write('s');
				this.os.write(sublen >> 8);
				this.os.write(sublen);

				this.printString(value, offset, sublen);

				length -= sublen;
				offset += sublen;
			}

			this.os.write('S');
			this.os.write(length >> 8);
			this.os.write(length);

			this.printString(value, offset, length);
		}
	}

	/**
	 * Writes a string value to the stream using UTF-8 encoding. The string will be written with the following syntax:
	 *
	 * <pre>
	 * <code> S b16 b8 string-value </code>
	 * </pre>
	 *
	 * If the value is null, it will be written as
	 *
	 * <pre>
	 * <code> N </code>
	 * </pre>
	 *
	 * @param value
	 *            the string value to write.
	 */
	@Override
	public void writeString(char[] buffer, int offset, int length) throws IOException {
		if (buffer == null) {
			this.os.write('N');
		} else {
			while (length > 0x8000) {
				int sublen = 0x8000;

				// chunk can't end in high surrogate
				char tail = buffer[(offset + sublen) - 1];

				if ((0xd800 <= tail) && (tail <= 0xdbff)) {
					sublen--;
				}

				this.os.write('s');
				this.os.write(sublen >> 8);
				this.os.write(sublen);

				this.printString(buffer, offset, sublen);

				length -= sublen;
				offset += sublen;
			}

			this.os.write('S');
			this.os.write(length >> 8);
			this.os.write(length);

			this.printString(buffer, offset, length);
		}
	}

	/**
	 * Writes a byte array to the stream. The array will be written with the following syntax:
	 *
	 * <pre>
	 * <code> B b16 b18 bytes </code>
	 * </pre>
	 *
	 * If the value is null, it will be written as
	 *
	 * <pre>
	 * <code> N </code>
	 * </pre>
	 *
	 * @param value
	 *            the string value to write.
	 */
	@Override
	public void writeBytes(byte[] buffer) throws IOException {
		if (buffer == null) {
			this.os.write('N');
		} else {
			this.writeBytes(buffer, 0, buffer.length);
		}
	}

	/**
	 * Writes a byte array to the stream. The array will be written with the following syntax:
	 *
	 * <pre>
	 * <code> B b16 b18 bytes </code>
	 * </pre>
	 *
	 * If the value is null, it will be written as
	 *
	 * <pre>
	 * <code> N </code>
	 * </pre>
	 *
	 * @param value
	 *            the string value to write.
	 */
	@Override
	public void writeBytes(byte[] buffer, int offset, int length) throws IOException {
		if (buffer == null) {
			this.os.write('N');
		} else {
			while (length > 0x8000) {
				int sublen = 0x8000;

				this.os.write('b');
				this.os.write(sublen >> 8);
				this.os.write(sublen);

				this.os.write(buffer, offset, sublen);

				length -= sublen;
				offset += sublen;
			}

			this.os.write('B');
			this.os.write(length >> 8);
			this.os.write(length);
			this.os.write(buffer, offset, length);
		}
	}

	/**
	 * Writes a byte buffer to the stream.
	 *
	 * <pre>
	 * <code> </code>
	 * </pre>
	 */
	@Override
	public void writeByteBufferStart() throws IOException {}

	/**
	 * Writes a byte buffer to the stream.
	 *
	 * <pre>
	 * <code> b b16 b18 bytes </code>
	 * </pre>
	 */
	@Override
	public void writeByteBufferPart(byte[] buffer, int offset, int length) throws IOException {
		while (length > 0) {
			int sublen = length;

			if (0x8000 < sublen) {
				sublen = 0x8000;
			}

			this.os.write('b');
			this.os.write(sublen >> 8);
			this.os.write(sublen);

			this.os.write(buffer, offset, sublen);

			length -= sublen;
			offset += sublen;
		}
	}

	/**
	 * Writes a byte buffer to the stream.
	 *
	 * <pre>
	 * <code> b b16 b18 bytes </code>
	 * </pre>
	 */
	@Override
	public void writeByteBufferEnd(byte[] buffer, int offset, int length) throws IOException {
		this.writeBytes(buffer, offset, length);
	}

	/**
	 * Writes a reference.
	 *
	 * <pre>
	 * <code> R b32 b24 b16 b8 </code>
	 * </pre>
	 *
	 * @param value
	 *            the integer value to write.
	 */
	@Override
	public void writeRef(int value) throws IOException {
		this.os.write('R');
		this.os.write(value >> 24);
		this.os.write(value >> 16);
		this.os.write(value >> 8);
		this.os.write(value);
	}

	/**
	 * Writes a placeholder.
	 *
	 * <pre>
	 * <code> P </code>
	 * </pre>
	 */
	public void writePlaceholder() throws IOException {
		this.os.write('P');
	}

	/**
	 * If the object has already been written, just write its ref.
	 *
	 * @return true if we're writing a ref.
	 */
	@Override
	public boolean addRef(Object object) throws IOException {
		if (this.refs == null) {
			this.refs = new IdentityHashMap<>();
		}

		Integer ref = this.refs.get(object);

		if (ref != null) {
			int value = ref.intValue();

			this.writeRef(value);
			return true;
		} else {
			this.refs.put(object, Integer.valueOf(this.refs.size()));

			return false;
		}
	}

	@Override
	public int getRef(Object obj) {
		Integer value;

		if (this.refs == null) {
			return -1;
		}

		value = this.refs.get(obj);

		if (value == null) {
			return -1;
		} else {
			return value;
		}
	}

	/**
	 * Resets the references for streaming.
	 */
	@Override
	public void resetReferences() {
		if (this.refs != null) {
			this.refs.clear();
		}
	}

	/**
	 * Removes a reference.
	 */
	@Override
	public boolean removeRef(Object obj) throws IOException {
		if (this.refs != null) {
			this.refs.remove(obj);

			return true;
		} else {
			return false;
		}
	}

	/**
	 * Replaces a reference from one object to another.
	 */
	@Override
	public boolean replaceRef(Object oldRef, Object newRef) throws IOException {
		Integer value = this.refs.remove(oldRef);

		if (value != null) {
			this.refs.put(newRef, value);

			return true;
		} else {
			return false;
		}
	}

	/**
	 * Prints a string to the stream, encoded as UTF-8 with preceeding length
	 *
	 * @param v
	 *            the string to print.
	 */
	public void printLenString(String v) throws IOException {
		if (v == null) {
			this.os.write(0);
			this.os.write(0);
		} else {
			int len = v.length();
			this.os.write(len >> 8);
			this.os.write(len);

			this.printString(v, 0, len);
		}
	}

	/**
	 * Prints a string to the stream, encoded as UTF-8
	 *
	 * @param v
	 *            the string to print.
	 */
	public void printString(String v) throws IOException {
		this.printString(v, 0, v.length());
	}

	/**
	 * Prints a string to the stream, encoded as UTF-8
	 *
	 * @param v
	 *            the string to print.
	 */
	public void printString(String v, int offset, int length) throws IOException {
		for (int i = 0; i < length; i++) {
			char ch = v.charAt(i + offset);

			if (ch < 0x80) {
				this.os.write(ch);
			} else if (ch < 0x800) {
				this.os.write(0xc0 + ((ch >> 6) & 0x1f));
				this.os.write(0x80 + (ch & 0x3f));
			} else {
				this.os.write(0xe0 + ((ch >> 12) & 0xf));
				this.os.write(0x80 + ((ch >> 6) & 0x3f));
				this.os.write(0x80 + (ch & 0x3f));
			}
		}
	}

	/**
	 * Prints a string to the stream, encoded as UTF-8
	 *
	 * @param v
	 *            the string to print.
	 */
	public void printString(char[] v, int offset, int length) throws IOException {
		for (int i = 0; i < length; i++) {
			char ch = v[i + offset];

			if (ch < 0x80) {
				this.os.write(ch);
			} else if (ch < 0x800) {
				this.os.write(0xc0 + ((ch >> 6) & 0x1f));
				this.os.write(0x80 + (ch & 0x3f));
			} else {
				this.os.write(0xe0 + ((ch >> 12) & 0xf));
				this.os.write(0x80 + ((ch >> 6) & 0x3f));
				this.os.write(0x80 + (ch & 0x3f));
			}
		}
	}

	@Override
	public void flush() throws IOException {
		if (this.os != null) {
			this.os.flush();
		}
	}

	@Override
	public void close() throws IOException {
		if (this.os != null) {
			this.os.flush();
		}
	}
}

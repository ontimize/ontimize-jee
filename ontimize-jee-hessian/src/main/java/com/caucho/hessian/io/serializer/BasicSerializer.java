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

package com.caucho.hessian.io.serializer;

import java.io.IOException;
import java.util.Date;

import com.caucho.hessian.io.AbstractHessianOutput;

/**
 * Serializing an object for known object types.
 */
public class BasicSerializer extends AbstractSerializer implements ObjectSerializer {

	public static final int					NULL					= 0;
	public static final int					BOOLEAN					= BasicSerializer.NULL + 1;
	public static final int					BYTE					= BasicSerializer.BOOLEAN + 1;
	public static final int					SHORT					= BasicSerializer.BYTE + 1;
	public static final int					INTEGER					= BasicSerializer.SHORT + 1;
	public static final int					LONG					= BasicSerializer.INTEGER + 1;
	public static final int					FLOAT					= BasicSerializer.LONG + 1;
	public static final int					DOUBLE					= BasicSerializer.FLOAT + 1;
	public static final int					CHARACTER				= BasicSerializer.DOUBLE + 1;
	public static final int					CHARACTER_OBJECT		= BasicSerializer.CHARACTER + 1;
	public static final int					STRING					= BasicSerializer.CHARACTER_OBJECT + 1;
	public static final int					STRING_BUILDER			= BasicSerializer.STRING + 1;
	public static final int					DATE					= BasicSerializer.STRING_BUILDER + 1;
	public static final int					NUMBER					= BasicSerializer.DATE + 1;
	public static final int					OBJECT					= BasicSerializer.NUMBER + 1;

	public static final int					BOOLEAN_ARRAY			= BasicSerializer.OBJECT + 1;
	public static final int					BYTE_ARRAY				= BasicSerializer.BOOLEAN_ARRAY + 1;
	public static final int					SHORT_ARRAY				= BasicSerializer.BYTE_ARRAY + 1;
	public static final int					INTEGER_ARRAY			= BasicSerializer.SHORT_ARRAY + 1;
	public static final int					LONG_ARRAY				= BasicSerializer.INTEGER_ARRAY + 1;
	public static final int					FLOAT_ARRAY				= BasicSerializer.LONG_ARRAY + 1;
	public static final int					DOUBLE_ARRAY			= BasicSerializer.FLOAT_ARRAY + 1;
	public static final int					CHARACTER_ARRAY			= BasicSerializer.DOUBLE_ARRAY + 1;
	public static final int					STRING_ARRAY			= BasicSerializer.CHARACTER_ARRAY + 1;
	public static final int					OBJECT_ARRAY			= BasicSerializer.STRING_ARRAY + 1;

	public static final int					BYTE_HANDLE				= BasicSerializer.OBJECT_ARRAY + 1;
	public static final int					SHORT_HANDLE			= BasicSerializer.BYTE_HANDLE + 1;
	public static final int					FLOAT_HANDLE			= BasicSerializer.SHORT_HANDLE + 1;

	private static final BasicSerializer	BYTE_HANDLE_SERIALIZER	= new BasicSerializer(BasicSerializer.BYTE_HANDLE);

	private static final BasicSerializer	SHORT_HANDLE_SERIALIZER	= new BasicSerializer(BasicSerializer.SHORT_HANDLE);

	private static final BasicSerializer	FLOAT_HANDLE_SERIALIZER	= new BasicSerializer(BasicSerializer.FLOAT_HANDLE);

	private final int						code;

	public BasicSerializer(int code) {
		this.code = code;
	}

	@Override
	public Serializer getObjectSerializer() {
		switch (this.code) {
			case BYTE:
				return BasicSerializer.BYTE_HANDLE_SERIALIZER;
			case SHORT:
				return BasicSerializer.SHORT_HANDLE_SERIALIZER;
			case FLOAT:
				return BasicSerializer.FLOAT_HANDLE_SERIALIZER;
			default:
				return this;
		}
	}

	@Override
	public void writeObject(Object obj, AbstractHessianOutput out) throws IOException {
		switch (this.code) {
			case BOOLEAN:
				out.writeBoolean(((Boolean) obj).booleanValue());
				break;
			case BYTE:
			case SHORT:
			case INTEGER:
				out.writeInt(((Number) obj).intValue());
				break;

			case LONG:
				out.writeLong(((Number) obj).longValue());
				break;

			case FLOAT:
			case DOUBLE:
				out.writeDouble(((Number) obj).doubleValue());
				break;

			case CHARACTER:
			case CHARACTER_OBJECT:
				out.writeString(String.valueOf(obj));
				break;

			case STRING:
				out.writeString((String) obj);
				break;

			case STRING_BUILDER:
				out.writeString(((StringBuilder) obj).toString());
				break;

			case DATE:
				out.writeUTCDate(((Date) obj).getTime());
				break;

			case BOOLEAN_ARRAY: {
				if (out.addRef(obj)) {
					return;
				}
				this.writeObjectBooleanArray(obj, out);
				break;
			}

			case BYTE_ARRAY: {
				byte[] data = (byte[]) obj;
				out.writeBytes(data, 0, data.length);
				break;
			}

			case SHORT_ARRAY: {
				if (out.addRef(obj)) {
					return;
				}
				this.writeObjectShortArray(obj, out);
				break;
			}

			case INTEGER_ARRAY: {
				if (out.addRef(obj)) {
					return;
				}
				this.writeObjectIntegerArray(obj, out);
				break;
			}

			case LONG_ARRAY: {
				if (out.addRef(obj)) {
					return;
				}
				this.writeObjectLongArray(obj, out);
				break;
			}

			case FLOAT_ARRAY: {
				if (out.addRef(obj)) {
					return;
				}
				this.writeObjectFloatArray(obj, out);
				break;
			}

			case DOUBLE_ARRAY: {
				if (out.addRef(obj)) {
					return;
				}
				this.writeObjectDoubleArray(obj, out);
				break;
			}

			case STRING_ARRAY: {
				if (out.addRef(obj)) {
					return;
				}
				this.writeObjectStringArray(obj, out);
				break;
			}

			case CHARACTER_ARRAY: {
				char[] data = (char[]) obj;
				out.writeString(data, 0, data.length);
				break;
			}

			case OBJECT_ARRAY: {
				if (out.addRef(obj)) {
					return;
				}
				this.writeObjectObjectArray(obj, out);
				break;
			}

			case NULL:
				out.writeNull();
				break;

			case OBJECT:
				ObjectHandleSerializer.SER.writeObject(obj, out);
				break;

			case BYTE_HANDLE:
				out.writeObject(new ByteHandle((Byte) obj));
				break;

			case SHORT_HANDLE:
				out.writeObject(new ShortHandle((Short) obj));
				break;

			case FLOAT_HANDLE:
				out.writeObject(new FloatHandle((Float) obj));
				break;

			default:
				throw new RuntimeException(this.code + " unknown code for " + obj.getClass());
		}
	}

	private void writeObjectObjectArray(Object obj, AbstractHessianOutput out) throws IOException {
		Object[] data = (Object[]) obj;

		boolean hasEnd = out.writeListBegin(data.length, "[object");

		for (int i = 0; i < data.length; i++) {
			out.writeObject(data[i]);
		}

		if (hasEnd) {
			out.writeListEnd();
		}
	}

	private void writeObjectStringArray(Object obj, AbstractHessianOutput out) throws IOException {
		String[] data = (String[]) obj;

		boolean hasEnd = out.writeListBegin(data.length, "[string");

		for (int i = 0; i < data.length; i++) {
			out.writeString(data[i]);
		}

		if (hasEnd) {
			out.writeListEnd();
		}
	}

	private void writeObjectDoubleArray(Object obj, AbstractHessianOutput out) throws IOException {
		double[] data = (double[]) obj;
		boolean hasEnd = out.writeListBegin(data.length, "[double");

		for (int i = 0; i < data.length; i++) {
			out.writeDouble(data[i]);
		}

		if (hasEnd) {
			out.writeListEnd();
		}
	}

	private void writeObjectFloatArray(Object obj, AbstractHessianOutput out) throws IOException {
		float[] data = (float[]) obj;

		boolean hasEnd = out.writeListBegin(data.length, "[float");

		for (int i = 0; i < data.length; i++) {
			out.writeDouble(data[i]);
		}

		if (hasEnd) {
			out.writeListEnd();
		}
	}

	private void writeObjectLongArray(Object obj, AbstractHessianOutput out) throws IOException {
		long[] data = (long[]) obj;

		boolean hasEnd = out.writeListBegin(data.length, "[long");

		for (int i = 0; i < data.length; i++) {
			out.writeLong(data[i]);
		}

		if (hasEnd) {
			out.writeListEnd();
		}
	}

	private void writeObjectIntegerArray(Object obj, AbstractHessianOutput out) throws IOException {
		int[] data = (int[]) obj;

		boolean hasEnd = out.writeListBegin(data.length, "[int");

		for (int i = 0; i < data.length; i++) {
			out.writeInt(data[i]);
		}

		if (hasEnd) {
			out.writeListEnd();
		}
	}

	private void writeObjectShortArray(Object obj, AbstractHessianOutput out) throws IOException {
		short[] data = (short[]) obj;
		boolean hasEnd = out.writeListBegin(data.length, "[short");

		for (int i = 0; i < data.length; i++) {
			out.writeInt(data[i]);
		}

		if (hasEnd) {
			out.writeListEnd();
		}
	}

	private void writeObjectBooleanArray(Object obj, AbstractHessianOutput out) throws IOException {
		boolean[] data = (boolean[]) obj;
		boolean hasEnd = out.writeListBegin(data.length, "[boolean");
		for (int i = 0; i < data.length; i++) {
			out.writeBoolean(data[i]);
		}

		if (hasEnd) {
			out.writeListEnd();
		}
	}
}

/*
 * Copyright (c) 2001-2004 Caucho Technology, Inc. All rights reserved. The Apache Software License, Version 1.1 Redistribution and use in source and
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

package com.caucho.hessian.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Debugging input stream for Hessian requests.
 */
public class HessianDebugState implements Hessian2Constants {

	private static final Logger			log				= LoggerFactory.getLogger(HessianDebugState.class);
	private final PrintWriter			dbg;

	private State						state;
	private final ArrayList<State>		stateStack		= new ArrayList<State>();

	private final ArrayList<ObjectDef>	objectDefList	= new ArrayList<ObjectDef>();

	private final ArrayList<String>		typeDefList		= new ArrayList<String>();

	private int							refId;
	private boolean						isNewline		= true;
	private boolean						isObject		= false;
	private int							column;

	private int							depth			= 0;

	/**
	 * Creates an uninitialized Hessian input stream.
	 */
	public HessianDebugState(PrintWriter dbg) {
		this.dbg = dbg;

		this.state = new InitialState();
	}

	public void startTop2() {
		this.state = new Top2State();
	}

	public void startData1() {
		this.state = new InitialState1();
	}

	public void startStreaming() {
		this.state = new StreamingState(new InitialState(), false);
	}

	/**
	 * Reads a character.
	 */
	public void next(int ch) throws IOException {
		this.state = this.state.next(ch);
	}

	void pushStack(State state) {
		this.stateStack.add(state);
	}

	State popStack() {
		return this.stateStack.remove(this.stateStack.size() - 1);
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getDepth() {
		return this.depth;
	}

	void println() {
		if (!this.isNewline) {
			this.dbg.println();
			this.dbg.flush();
		}

		this.isNewline = true;
		this.column = 0;
	}

	static boolean isString(int ch) {
		switch (ch) {
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

			case 'R':
			case 'S':
				return true;

			default:
				return false;
		}
	}

	static boolean isInteger(int ch) {
		switch (ch) {
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

			case 0xd0:
			case 0xd1:
			case 0xd2:
			case 0xd3:
			case 0xd4:
			case 0xd5:
			case 0xd6:
			case 0xd7:

			case 'I':
				return true;

			default:
				return false;
		}
	}

	abstract class State {
		State	_next;

		State() {}

		State(State next) {
			this._next = next;
		}

		abstract State next(int ch);

		boolean isShift(Object value) {
			return false;
		}

		State shift(Object value) {
			return this;
		}

		int depth() {
			if (this._next != null) {
				return this._next.depth();
			} else {
				return HessianDebugState.this.getDepth();
			}
		}

		void printIndent(int depth) {
			if (HessianDebugState.this.isNewline) {
				for (int i = HessianDebugState.this.column; i < (this.depth() + depth); i++) {
					HessianDebugState.this.dbg.print(" ");
					HessianDebugState.this.column++;
				}
			}
		}

		void print(String string) {
			this.print(0, string);
		}

		void print(int depth, String string) {
			this.printIndent(depth);

			HessianDebugState.this.dbg.print(string);
			HessianDebugState.this.isNewline = false;
			HessianDebugState.this.isObject = false;

			int p = string.lastIndexOf('\n');
			if (p > 0) {
				HessianDebugState.this.column = string.length() - p - 1;
			} else {
				HessianDebugState.this.column += string.length();
			}
		}

		void println(String string) {
			this.println(0, string);
		}

		void println(int depth, String string) {
			this.printIndent(depth);

			HessianDebugState.this.dbg.println(string);
			HessianDebugState.this.dbg.flush();
			HessianDebugState.this.isNewline = true;
			HessianDebugState.this.isObject = false;
			HessianDebugState.this.column = 0;
		}

		void println() {
			if (!HessianDebugState.this.isNewline) {
				HessianDebugState.this.dbg.println();
				HessianDebugState.this.dbg.flush();
			}

			HessianDebugState.this.isNewline = true;
			HessianDebugState.this.isObject = false;
			HessianDebugState.this.column = 0;
		}

		void printObject(String string) {
			if (HessianDebugState.this.isObject) {
				this.println();
			}

			this.printIndent(0);

			HessianDebugState.this.dbg.print(string);
			HessianDebugState.this.dbg.flush();

			HessianDebugState.this.column += string.length();

			HessianDebugState.this.isNewline = false;
			HessianDebugState.this.isObject = true;
		}

		protected State nextObject(int ch) {
			switch (ch) {
				case -1:
					this.println();
					return this;

				case 'N':
					if (this.isShift(null)) {
						return this.shift(null);
					} else {
						this.printObject("null");
						return this;
					}

				case 'T':
					if (this.isShift(Boolean.TRUE)) {
						return this.shift(Boolean.TRUE);
					} else {
						this.printObject("true");
						return this;
					}

				case 'F':
					if (this.isShift(Boolean.FALSE)) {
						return this.shift(Boolean.FALSE);
					} else {
						this.printObject("false");
						return this;
					}

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
				case 0xbf: {
					Integer value = Integer.valueOf(ch - 0x90);

					if (this.isShift(value)) {
						return this.shift(value);
					} else {
						this.printObject(value.toString());
						return this;
					}
				}

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
					return new IntegerState(this, "int", ch - 0xc8, 3);

				case 0xd0:
				case 0xd1:
				case 0xd2:
				case 0xd3:
				case 0xd4:
				case 0xd5:
				case 0xd6:
				case 0xd7:
					return new IntegerState(this, "int", ch - 0xd4, 2);

				case 'I':
					return new IntegerState(this, "int");

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
				case 0xef: {
					Long value = Long.valueOf(ch - 0xe0);

					if (this.isShift(value)) {
						return this.shift(value);
					} else {
						this.printObject(value.toString() + "L");
						return this;
					}
				}

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
					return new LongState(this, "long", ch - 0xf8, 7);

				case 0x38:
				case 0x39:
				case 0x3a:
				case 0x3b:
				case 0x3c:
				case 0x3d:
				case 0x3e:
				case 0x3f:
					return new LongState(this, "long", ch - 0x3c, 6);

				case BC_LONG_INT:
					return new LongState(this, "long", 0, 4);

				case 'L':
					return new LongState(this, "long");

				case 0x5b:
				case 0x5c: {
					Double value = new Double(ch - 0x5b);

					if (this.isShift(value)) {
						return this.shift(value);
					} else {
						this.printObject(value.toString());
						return this;
					}
				}

				case 0x5d:
					return new DoubleIntegerState(this, 3);

				case 0x5e:
					return new DoubleIntegerState(this, 2);

				case 0x5f:
					return new MillsState(this);

				case 'D':
					return new DoubleState(this);

				case 'Q':
					return new RefState(this);

				case BC_DATE:
					return new DateState(this);

				case BC_DATE_MINUTE:
					return new DateState(this, true);

				case 0x00: {
					String value = "\"\"";

					if (this.isShift(value)) {
						return this.shift(value);
					} else {
						this.printObject(value.toString());
						return this;
					}
				}

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
					return new StringState(this, 'S', ch);

				case 0x30:
				case 0x31:
				case 0x32:
				case 0x33:
					return new StringState(this, 'S', ch - 0x30, true);

				case 'R':
					return new StringState(this, 'S', false);

				case 'S':
					return new StringState(this, 'S', true);

				case 0x20: {
					String value = "binary(0)";

					if (this.isShift(value)) {
						return this.shift(value);
					} else {
						this.printObject(value.toString());
						return this;
					}
				}

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
					return new BinaryState(this, 'B', ch - 0x20);

				case 0x34:
				case 0x35:
				case 0x36:
				case 0x37:
					return new BinaryState(this, 'B', ch - 0x34, true);

				case 'A':
					return new BinaryState(this, 'B', false);

				case 'B':
					return new BinaryState(this, 'B', true);

				case 'M':
					return new MapState(this, HessianDebugState.this.refId++);

				case 'H':
					return new MapState(this, HessianDebugState.this.refId++, false);

				case BC_LIST_VARIABLE:
					return new ListState(this, HessianDebugState.this.refId++, true);

				case BC_LIST_VARIABLE_UNTYPED:
					return new ListState(this, HessianDebugState.this.refId++, false);

				case BC_LIST_FIXED:
					return new CompactListState(this, HessianDebugState.this.refId++, true);

				case BC_LIST_FIXED_UNTYPED:
					return new CompactListState(this, HessianDebugState.this.refId++, false);

				case 0x70:
				case 0x71:
				case 0x72:
				case 0x73:
				case 0x74:
				case 0x75:
				case 0x76:
				case 0x77:
					return new CompactListState(this, HessianDebugState.this.refId++, true, ch - 0x70);

				case 0x78:
				case 0x79:
				case 0x7a:
				case 0x7b:
				case 0x7c:
				case 0x7d:
				case 0x7e:
				case 0x7f:
					return new CompactListState(this, HessianDebugState.this.refId++, false, ch - 0x78);

				case 'C':
					return new ObjectDefState(this);

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
					return new ObjectState(this, HessianDebugState.this.refId++, ch - 0x60);

				case 'O':
					return new ObjectState(this, HessianDebugState.this.refId++);

				default:
					return this;
			}
		}
	}

	abstract class State1 extends State {
		State1() {}

		State1(State next) {
			super(next);
		}

		@Override
		protected State nextObject(int ch) {
			switch (ch) {
				case -1:
					this.println();
					return this;

				case 'N':
					if (this.isShift(null)) {
						return this.shift(null);
					} else {
						this.printObject("null");
						return this;
					}

				case 'T':
					if (this.isShift(Boolean.TRUE)) {
						return this.shift(Boolean.TRUE);
					} else {
						this.printObject("true");
						return this;
					}

				case 'F':
					if (this.isShift(Boolean.FALSE)) {
						return this.shift(Boolean.FALSE);
					} else {
						this.printObject("false");
						return this;
					}

				case 'I':
					return new IntegerState(this, "int");

				case 'L':
					return new LongState(this, "long");

				case 'D':
					return new DoubleState(this);

				case 'Q':
					return new RefState(this);

				case 'd':
					return new DateState(this);

				case 's':
					return new StringState(this, 'S', false);

				case 'S':
					return new StringState(this, 'S', true);

				case 'b':
				case 'A':
					return new BinaryState(this, 'B', false);

				case 'B':
					return new BinaryState(this, 'B', true);

				case 'M':
					return new MapState1(this, HessianDebugState.this.refId++);

				case 'V':
					return new ListState1(this, HessianDebugState.this.refId++);

				case 'R':
					return new IntegerState(new RefState1(this), "ref");

				default:
					this.printObject("x" + String.format("%02x", ch));
					return this;
			}
		}
	}

	class InitialState extends State {
		@Override
		State next(int ch) {
			return this.nextObject(ch);
		}
	}

	class InitialState1 extends State1 {
		@Override
		State next(int ch) {
			return this.nextObject(ch);
		}
	}

	class Top1State extends State1 {
		@Override
		State next(int ch) {
			this.println();

			if (ch == 'r') {
				return new ReplyState1(this);
			} else if (ch == 'c') {
				return new CallState1(this);
			} else {
				return this.nextObject(ch);
			}
		}
	}

	class Top2State extends State {
		@Override
		State next(int ch) {
			this.println();

			if (ch == 'R') {
				return new Reply2State(this);
			} else if (ch == 'F') {
				return new Fault2State(this);
			} else if (ch == 'C') {
				return new Call2State(this);
			} else if (ch == 'H') {
				return new Hessian2State(this);
			} else if (ch == 'r') {
				return new ReplyState1(this);
			} else if (ch == 'c') {
				return new CallState1(this);
			} else {
				return this.nextObject(ch);
			}
		}
	}

	class IntegerState extends State {
		String	_typeCode;

		int		_length;
		int		_value;

		IntegerState(State next, String typeCode) {
			super(next);

			this._typeCode = typeCode;
		}

		IntegerState(State next, String typeCode, int value, int length) {
			super(next);

			this._typeCode = typeCode;

			this._value = value;
			this._length = length;
		}

		@Override
		State next(int ch) {
			this._value = (256 * this._value) + (ch & 0xff);

			if (++this._length == 4) {
				Integer value = Integer.valueOf(this._value);

				if (this._next.isShift(value)) {
					return this._next.shift(value);
				} else {
					this.printObject(value.toString());

					return this._next;
				}
			} else {
				return this;
			}
		}
	}

	class LongState extends State {
		String	_typeCode;

		int		_length;
		long	_value;

		LongState(State next, String typeCode) {
			super(next);

			this._typeCode = typeCode;
		}

		LongState(State next, String typeCode, long value, int length) {
			super(next);

			this._typeCode = typeCode;

			this._value = value;
			this._length = length;
		}

		@Override
		State next(int ch) {
			this._value = (256 * this._value) + (ch & 0xff);

			if (++this._length == 8) {
				Long value = Long.valueOf(this._value);

				if (this._next.isShift(value)) {
					return this._next.shift(value);
				} else {
					this.printObject(value.toString() + "L");

					return this._next;
				}
			} else {
				return this;
			}
		}
	}

	class DoubleIntegerState extends State {
		int		_length;
		int		_value;
		boolean	_isFirst	= true;

		DoubleIntegerState(State next, int length) {
			super(next);

			this._length = length;
		}

		@Override
		State next(int ch) {
			if (this._isFirst) {
				this._value = (byte) ch;
			} else {
				this._value = (256 * this._value) + (ch & 0xff);
			}

			this._isFirst = false;

			if (++this._length == 4) {
				Double value = new Double(this._value);

				if (this._next.isShift(value)) {
					return this._next.shift(value);
				} else {
					this.printObject(value.toString());

					return this._next;
				}
			} else {
				return this;
			}
		}
	}

	class RefState extends State {
		String	_typeCode;

		int		_length;
		int		_value;

		RefState(State next) {
			super(next);
		}

		RefState(State next, String typeCode) {
			super(next);

			this._typeCode = typeCode;
		}

		RefState(State next, String typeCode, int value, int length) {
			super(next);

			this._typeCode = typeCode;

			this._value = value;
			this._length = length;
		}

		@Override
		boolean isShift(Object o) {
			return true;
		}

		@Override
		State shift(Object o) {
			this.println("ref #" + o);

			return this._next;
		}

		@Override
		State next(int ch) {
			return this.nextObject(ch);
		}
	}

	class RefState1 extends State {
		String	_typeCode;

		RefState1(State next) {
			super(next);
		}

		@Override
		boolean isShift(Object o) {
			return true;
		}

		@Override
		State shift(Object o) {
			this.println("ref #" + o);

			return this._next;
		}

		@Override
		State next(int ch) {
			return this.nextObject(ch);
		}
	}

	class DateState extends State {
		int		_length;
		long	_value;
		boolean	_isMinute;

		DateState(State next) {
			super(next);
		}

		DateState(State next, boolean isMinute) {
			super(next);

			this._length = 4;
			this._isMinute = isMinute;
		}

		@Override
		State next(int ch) {
			this._value = (256 * this._value) + (ch & 0xff);

			if (++this._length == 8) {
				java.util.Date value;

				if (this._isMinute) {
					value = new java.util.Date(this._value * 60000L);
				} else {
					value = new java.util.Date(this._value);
				}

				if (this._next.isShift(value)) {
					return this._next.shift(value);
				} else {
					this.printObject(value.toString());

					return this._next;
				}
			} else {
				return this;
			}
		}
	}

	class DoubleState extends State {
		int		_length;
		long	_value;

		DoubleState(State next) {
			super(next);
		}

		@Override
		State next(int ch) {
			this._value = (256 * this._value) + (ch & 0xff);

			if (++this._length == 8) {
				Double value = Double.longBitsToDouble(this._value);

				if (this._next.isShift(value)) {
					return this._next.shift(value);
				} else {
					this.printObject(value.toString());

					return this._next;
				}
			} else {
				return this;
			}
		}
	}

	class MillsState extends State {
		int	_length;
		int	_value;

		MillsState(State next) {
			super(next);
		}

		@Override
		State next(int ch) {
			this._value = (256 * this._value) + (ch & 0xff);

			if (++this._length == 4) {
				Double value = 0.001 * this._value;

				if (this._next.isShift(value)) {
					return this._next.shift(value);
				} else {
					this.printObject(value.toString());

					return this._next;
				}
			} else {
				return this;
			}
		}
	}

	class StringState extends State {
		private static final int	TOP		= 0;
		private static final int	UTF_2_1	= 1;
		private static final int	UTF_3_1	= 2;
		private static final int	UTF_3_2	= 3;

		char						_typeCode;

		StringBuilder				_value	= new StringBuilder();
		int							_lengthIndex;
		int							_length;
		boolean						_isLastChunk;

		int							_utfState;
		char						_ch;

		StringState(State next, char typeCode, boolean isLastChunk) {
			super(next);

			this._typeCode = typeCode;
			this._isLastChunk = isLastChunk;
		}

		StringState(State next, char typeCode, int length) {
			super(next);

			this._typeCode = typeCode;
			this._isLastChunk = true;
			this._length = length;
			this._lengthIndex = 2;
		}

		StringState(State next, char typeCode, int length, boolean isLastChunk) {
			super(next);

			this._typeCode = typeCode;
			this._isLastChunk = isLastChunk;
			this._length = length;
			this._lengthIndex = 1;
		}

		@Override
		State next(int ch) {
			if (this._lengthIndex < 2) {
				this._length = (256 * this._length) + (ch & 0xff);

				if ((++this._lengthIndex == 2) && (this._length == 0) && this._isLastChunk) {
					if (this._next.isShift(this._value.toString())) {
						return this._next.shift(this._value.toString());
					} else {
						this.printObject("\"" + this._value + "\"");
						return this._next;
					}
				} else {
					return this;
				}
			} else if (this._length == 0) {
				if ((ch == 's') || (ch == 'x')) {
					this._isLastChunk = false;
					this._lengthIndex = 0;
					return this;
				} else if ((ch == 'S') || (ch == 'X')) {
					this._isLastChunk = true;
					this._lengthIndex = 0;
					return this;
				} else if (ch == 0x00) {
					if (this._next.isShift(this._value.toString())) {
						return this._next.shift(this._value.toString());
					} else {
						this.printObject("\"" + this._value + "\"");
						return this._next;
					}
				} else if ((0x00 <= ch) && (ch < 0x20)) {
					this._isLastChunk = true;
					this._lengthIndex = 2;
					this._length = ch & 0xff;
					return this;
				} else if ((0x30 <= ch) && (ch < 0x34)) {
					this._isLastChunk = true;
					this._lengthIndex = 1;
					this._length = (ch - 0x30);
					return this;
				} else {
					this.println(this + " " + String.valueOf((char) ch) + ": unexpected character");
					return this._next;
				}
			}

			switch (this._utfState) {
				case TOP:
					if (ch < 0x80) {
						this._length--;

						this._value.append((char) ch);
					} else if (ch < 0xe0) {
						this._ch = (char) ((ch & 0x1f) << 6);
						this._utfState = StringState.UTF_2_1;
					} else {
						this._ch = (char) ((ch & 0xf) << 12);
						this._utfState = StringState.UTF_3_1;
					}
					break;

				case UTF_2_1:
				case UTF_3_2:
					this._ch += ch & 0x3f;
					this._value.append(this._ch);
					this._length--;
					this._utfState = StringState.TOP;
					break;

				case UTF_3_1:
					this._ch += (char) ((ch & 0x3f) << 6);
					this._utfState = StringState.UTF_3_2;
					break;
			}

			if ((this._length == 0) && this._isLastChunk) {
				if (this._next.isShift(this._value.toString())) {
					return this._next.shift(this._value.toString());
				} else {
					this.printObject("\"" + this._value + "\"");

					return this._next;
				}
			} else {
				return this;
			}
		}
	}

	class BinaryState extends State {
		char	_typeCode;

		int		_totalLength;

		int		_lengthIndex;
		int		_length;
		boolean	_isLastChunk;

		BinaryState(State next, char typeCode, boolean isLastChunk) {
			super(next);

			this._typeCode = typeCode;
			this._isLastChunk = isLastChunk;
		}

		BinaryState(State next, char typeCode, int length) {
			super(next);

			this._typeCode = typeCode;
			this._isLastChunk = true;
			this._length = length;
			this._lengthIndex = 2;
		}

		BinaryState(State next, char typeCode, int length, boolean isLastChunk) {
			super(next);

			this._typeCode = typeCode;
			this._isLastChunk = isLastChunk;
			this._length = length;
			this._lengthIndex = 1;
		}

		@Override
		State next(int ch) {
			if (this._lengthIndex < 2) {
				this._length = (256 * this._length) + (ch & 0xff);

				if ((++this._lengthIndex == 2) && (this._length == 0) && this._isLastChunk) {
					String value = "binary(" + this._totalLength + ")";

					if (this._next.isShift(value)) {
						return this._next.shift(value);
					} else {
						this.printObject(value);
						return this._next;
					}
				} else {
					return this;
				}
			} else if (this._length == 0) {
				if ((ch == 'b') || (ch == 'A')) {
					this._isLastChunk = false;
					this._lengthIndex = 0;
					return this;
				} else if (ch == 'B') {
					this._isLastChunk = true;
					this._lengthIndex = 0;
					return this;
				} else if (ch == 0x20) {
					String value = "binary(" + this._totalLength + ")";

					if (this._next.isShift(value)) {
						return this._next.shift(value);
					} else {
						this.printObject(value);
						return this._next;
					}
				} else if ((0x20 <= ch) && (ch < 0x30)) {
					this._isLastChunk = true;
					this._lengthIndex = 2;
					this._length = (ch & 0xff) - 0x20;
					return this;
				} else {
					this.println(this + " 0x" + Integer.toHexString(ch) + " " + String.valueOf((char) ch) + ": unexpected character");
					return this._next;
				}
			}

			this._length--;
			this._totalLength++;

			if ((this._length == 0) && this._isLastChunk) {
				String value = "binary(" + this._totalLength + ")";

				if (this._next.isShift(value)) {
					return this._next.shift(value);
				} else {
					this.printObject(value);

					return this._next;
				}
			} else {
				return this;
			}
		}
	}

	class MapState extends State {
		private static final int	TYPE	= 0;
		private static final int	KEY		= 1;
		private static final int	VALUE	= 2;

		private final int			_refId;

		private int					_state;
		private int					_valueDepth;
		private boolean				_hasData;

		MapState(State next, int refId) {
			super(next);

			this._refId = refId;
			this._state = MapState.TYPE;
		}

		MapState(State next, int refId, boolean isType) {
			super(next);

			this._refId = refId;

			if (isType) {
				this._state = MapState.TYPE;
			} else {
				this.printObject("map (#" + this._refId + ")");
				this._state = MapState.VALUE;
			}
		}

		@Override
		boolean isShift(Object value) {
			return this._state == MapState.TYPE;
		}

		@Override
		State shift(Object type) {
			if (this._state == MapState.TYPE) {
				if (type instanceof String) {
					HessianDebugState.this.typeDefList.add((String) type);
				} else if (type instanceof Integer) {
					int iValue = (Integer) type;

					if ((iValue >= 0) && (iValue < HessianDebugState.this.typeDefList.size())) {
						type = HessianDebugState.this.typeDefList.get(iValue);
					}
				}

				this.printObject("map " + type + " (#" + this._refId + ")");

				this._state = MapState.VALUE;

				return this;
			} else {
				this.printObject(this + " unknown shift state= " + this._state + " type=" + type);

				return this;
			}
		}

		@Override
		int depth() {
			if (this._state == MapState.TYPE) {
				return this._next.depth();
			} else if (this._state == MapState.KEY) {
				return this._next.depth() + 2;
			} else {
				return this._valueDepth;
			}
		}

		@Override
		State next(int ch) {
			switch (this._state) {
				case TYPE:
					return this.nextObject(ch);

				case VALUE:
					if (ch == 'Z') {
						if (this._hasData) {
							this.println();
						}

						return this._next;
					} else {
						if (this._hasData) {
							this.println();
						}

						this._hasData = true;
						this._state = MapState.KEY;

						return this.nextObject(ch);
					}

				case KEY:
					this.print(" => ");
					HessianDebugState.this.isObject = false;
					this._valueDepth = HessianDebugState.this.column;

					this._state = MapState.VALUE;

					return this.nextObject(ch);

				default:
					throw new IllegalStateException();
			}
		}
	}

	class MapState1 extends State1 {
		private static final int	TYPE	= 0;
		private static final int	KEY		= 1;
		private static final int	VALUE	= 2;

		private final int			_refId;

		private int					_state;
		private int					_valueDepth;
		private boolean				_hasData;

		MapState1(State next, int refId) {
			super(next);

			this._refId = refId;
			this._state = MapState1.TYPE;
		}

		MapState1(State next, int refId, boolean isType) {
			super(next);

			this._refId = refId;

			if (isType) {
				this._state = MapState1.TYPE;
			} else {
				this.printObject("map (#" + this._refId + ")");
				this._state = MapState1.VALUE;
			}
		}

		@Override
		boolean isShift(Object value) {
			return this._state == MapState1.TYPE;
		}

		@Override
		State shift(Object type) {
			if (this._state == MapState1.TYPE) {
				if (type instanceof String) {
					HessianDebugState.this.typeDefList.add((String) type);
				} else if (type instanceof Integer) {
					int iValue = (Integer) type;

					if ((iValue >= 0) && (iValue < HessianDebugState.this.typeDefList.size())) {
						type = HessianDebugState.this.typeDefList.get(iValue);
					}
				}

				this.printObject("map " + type + " (#" + this._refId + ")");

				this._state = MapState1.VALUE;

				return this;
			} else {
				throw new IllegalStateException();
			}
		}

		@Override
		int depth() {
			if (this._state == MapState1.TYPE) {
				return this._next.depth();
			} else if (this._state == MapState1.KEY) {
				return this._next.depth() + 2;
			} else {
				return this._valueDepth;
			}
		}

		@Override
		State next(int ch) {
			switch (this._state) {
				case TYPE:
					if (ch == 't') {
						return new StringState(this, 't', true);
					} else if (ch == 'z') {
						this.println("map (#" + this._refId + ")");
						return this._next;
					} else {
						this.println("map (#" + this._refId + ")");
						this._hasData = true;
						this._state = MapState1.KEY;
						return this.nextObject(ch);
					}

				case VALUE:
					if (ch == 'z') {
						if (this._hasData) {
							this.println();
						}

						return this._next;
					} else {
						if (this._hasData) {
							this.println();
						}

						this._hasData = true;
						this._state = MapState1.KEY;

						return this.nextObject(ch);
					}

				case KEY:
					this.print(" => ");
					HessianDebugState.this.isObject = false;
					this._valueDepth = HessianDebugState.this.column;

					this._state = MapState1.VALUE;

					return this.nextObject(ch);

				default:
					throw new IllegalStateException();
			}
		}
	}

	class ObjectDefState extends State {
		private static final int		TYPE		= 1;
		private static final int		COUNT		= 2;
		private static final int		FIELD		= 3;
		private static final int		COMPLETE	= 4;

		private int						_state;
		private int						_count;

		private String					_type;
		private final ArrayList<String>	_fields		= new ArrayList<String>();

		ObjectDefState(State next) {
			super(next);

			this._state = ObjectDefState.TYPE;
		}

		@Override
		boolean isShift(Object value) {
			return true;
		}

		@Override
		State shift(Object object) {
			if (this._state == ObjectDefState.TYPE) {
				this._type = (String) object;

				this.print("/* defun " + this._type + " [");

				HessianDebugState.this.objectDefList.add(new ObjectDef(this._type, this._fields));

				this._state = ObjectDefState.COUNT;
			} else if (this._state == ObjectDefState.COUNT) {
				this._count = (Integer) object;

				this._state = ObjectDefState.FIELD;
			} else if (this._state == ObjectDefState.FIELD) {
				String field = (String) object;

				this._count--;

				this._fields.add(field);

				if (this._fields.size() == 1) {
					this.print(field);
				} else {
					this.print(", " + field);
				}
			} else {
				throw new UnsupportedOperationException();
			}

			return this;
		}

		@Override
		int depth() {
			if (this._state <= ObjectDefState.TYPE) {
				return this._next.depth();
			} else {
				return this._next.depth() + 2;
			}
		}

		@Override
		State next(int ch) {
			switch (this._state) {
				case TYPE:
					return this.nextObject(ch);

				case COUNT:
					return this.nextObject(ch);

				case FIELD:
					if (this._count == 0) {
						this.println("] */");
						this._next.printIndent(0);

						return this._next.nextObject(ch);
					} else {
						return this.nextObject(ch);
					}

				default:
					throw new IllegalStateException();
			}
		}
	}

	class ObjectState extends State {
		private static final int	TYPE	= 0;
		private static final int	FIELD	= 1;

		private final int			_refId;

		private int					_state;
		private ObjectDef			_def;
		private int					_count;
		private int					_fieldDepth;

		ObjectState(State next, int refId) {
			super(next);

			this._refId = refId;
			this._state = ObjectState.TYPE;
		}

		ObjectState(State next, int refId, int def) {
			super(next);

			this._refId = refId;
			this._state = ObjectState.FIELD;

			if ((def < 0) || (HessianDebugState.this.objectDefList.size() <= def)) {
				HessianDebugState.log.warn("{} {}  is an unknown object type", this, def);

				this.println(this + " object unknown  (#" + this._refId + ")");
			}

			this._def = HessianDebugState.this.objectDefList.get(def);

			if (HessianDebugState.this.isObject) {
				this.println();
			}

			this.println("object " + this._def.getType() + " (#" + this._refId + ")");
		}

		@Override
		boolean isShift(Object value) {
			if (this._state == ObjectState.TYPE) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		State shift(Object object) {
			if (this._state == ObjectState.TYPE) {
				int def = (Integer) object;

				this._def = HessianDebugState.this.objectDefList.get(def);

				this.println("object " + this._def.getType() + " (#" + this._refId + ")");

				this._state = ObjectState.FIELD;

				if (this._def.getFields().size() == 0) {
					return this._next;
				}
			}

			return this;
		}

		@Override
		int depth() {
			if (this._state <= ObjectState.TYPE) {
				return this._next.depth();
			} else {
				return this._fieldDepth;
			}
		}

		@Override
		State next(int ch) {
			switch (this._state) {
				case TYPE:
					return this.nextObject(ch);

				case FIELD:
					if (this._def.getFields().size() <= this._count) {
						return this._next.next(ch);
					}

					this._fieldDepth = this._next.depth() + 2;
					this.println();
					this.print(this._def.getFields().get(this._count++) + ": ");

					this._fieldDepth = HessianDebugState.this.column;

					HessianDebugState.this.isObject = false;
					return this.nextObject(ch);

				default:
					throw new IllegalStateException();
			}
		}
	}

	class ListState1 extends State1 {
		private static final int	TYPE	= 0;
		private static final int	LENGTH	= 1;
		private static final int	VALUE	= 2;

		private final int			_refId;

		private int					_state;
		private int					_count;
		private int					_valueDepth;

		ListState1(State next, int refId) {
			super(next);

			this._refId = refId;

			this._state = ListState1.TYPE;
		}

		@Override
		boolean isShift(Object value) {
			return (this._state == ListState1.TYPE) || (this._state == ListState1.LENGTH);
		}

		@Override
		State shift(Object object) {
			if (this._state == ListState1.TYPE) {
				Object type = object;

				if (type instanceof String) {
					HessianDebugState.this.typeDefList.add((String) type);
				} else if (object instanceof Integer) {
					int index = (Integer) object;

					if ((index >= 0) && (index < HessianDebugState.this.typeDefList.size())) {
						type = HessianDebugState.this.typeDefList.get(index);
					} else {
						type = "type-unknown(" + index + ")";
					}
				}

				this.printObject("list " + type + "(#" + this._refId + ")");

				this._state = ListState1.VALUE;

				return this;
			} else if (this._state == ListState1.LENGTH) {
				this._state = ListState1.VALUE;

				return this;
			} else {
				return this;
			}
		}

		@Override
		int depth() {
			if (this._state <= ListState1.LENGTH) {
				return this._next.depth();
			} else if (this._state == ListState1.VALUE) {
				return this._valueDepth;
			} else {
				return this._next.depth() + 2;
			}
		}

		@Override
		State next(int ch) {
			switch (this._state) {
				case TYPE:
					if (ch == 'z') {
						this.printObject("list (#" + this._refId + ")");

						return this._next;
					} else if (ch == 't') {
						return new StringState(this, 't', true);
					} else {
						this.printObject("list (#" + this._refId + ")");
						this.printObject("  " + this._count++ + ": ");
						this._valueDepth = HessianDebugState.this.column;
						HessianDebugState.this.isObject = false;
						this._state = ListState1.VALUE;

						return this.nextObject(ch);
					}

				case VALUE:
					if (ch == 'z') {
						if (this._count > 0) {
							this.println();
						}

						return this._next;
					} else {
						this._valueDepth = this._next.depth() + 2;
						this.println();
						this.printObject(this._count++ + ": ");
						this._valueDepth = HessianDebugState.this.column;
						HessianDebugState.this.isObject = false;

						return this.nextObject(ch);
					}

				default:
					throw new IllegalStateException();
			}
		}
	}

	class ListState extends State {
		private static final int	TYPE	= 0;
		private static final int	LENGTH	= 1;
		private static final int	VALUE	= 2;

		private final int			_refId;

		private int					_state;
		private int					_count;
		private int					_valueDepth;

		ListState(State next, int refId, boolean isType) {
			super(next);

			this._refId = refId;

			if (isType) {
				this._state = ListState.TYPE;
			} else {
				this.printObject("list (#" + this._refId + ")");
				this._state = ListState.VALUE;
			}
		}

		@Override
		boolean isShift(Object value) {
			return (this._state == ListState.TYPE) || (this._state == ListState.LENGTH);
		}

		@Override
		State shift(Object object) {
			if (this._state == ListState.TYPE) {
				Object type = object;

				if (type instanceof String) {
					HessianDebugState.this.typeDefList.add((String) type);
				} else if (object instanceof Integer) {
					int index = (Integer) object;

					if ((index >= 0) && (index < HessianDebugState.this.typeDefList.size())) {
						type = HessianDebugState.this.typeDefList.get(index);
					} else {
						type = "type-unknown(" + index + ")";
					}
				}

				this.printObject("list " + type + "(#" + this._refId + ")");

				this._state = ListState.VALUE;

				return this;
			} else if (this._state == ListState.LENGTH) {
				this._state = ListState.VALUE;

				return this;
			} else {
				return this;
			}
		}

		@Override
		int depth() {
			if (this._state <= ListState.LENGTH) {
				return this._next.depth();
			} else if (this._state == ListState.VALUE) {
				return this._valueDepth;
			} else {
				return this._next.depth() + 2;
			}
		}

		@Override
		State next(int ch) {
			switch (this._state) {
				case TYPE:
					return this.nextObject(ch);

				case VALUE:
					if (ch == 'Z') {
						if (this._count > 0) {
							this.println();
						}

						return this._next;
					} else {
						this._valueDepth = this._next.depth() + 2;
						this.println();
						this.printObject(this._count++ + ": ");
						this._valueDepth = HessianDebugState.this.column;
						HessianDebugState.this.isObject = false;

						return this.nextObject(ch);
					}

				default:
					throw new IllegalStateException();
			}
		}
	}

	class CompactListState extends State {
		private static final int	TYPE	= 0;
		private static final int	LENGTH	= 1;
		private static final int	VALUE	= 2;

		private final int			_refId;

		private final boolean		_isTyped;
		private boolean				_isLength;

		private int					_state;
		private int					_length;
		private int					_count;
		private int					_valueDepth;

		CompactListState(State next, int refId, boolean isTyped) {
			super(next);

			this._isTyped = isTyped;
			this._refId = refId;

			if (isTyped) {
				this._state = CompactListState.TYPE;
			} else {
				this._state = CompactListState.LENGTH;
			}
		}

		CompactListState(State next, int refId, boolean isTyped, int length) {
			super(next);

			this._isTyped = isTyped;
			this._refId = refId;
			this._length = length;

			this._isLength = true;

			if (isTyped) {
				this._state = CompactListState.TYPE;
			} else {
				this.printObject("list (#" + this._refId + ")");

				this._state = CompactListState.VALUE;
			}
		}

		@Override
		boolean isShift(Object value) {
			return (this._state == CompactListState.TYPE) || (this._state == CompactListState.LENGTH);
		}

		@Override
		State shift(Object object) {
			if (this._state == CompactListState.TYPE) {
				Object type = object;

				if (object instanceof Integer) {
					int index = (Integer) object;

					if ((index >= 0) && (index < HessianDebugState.this.typeDefList.size())) {
						type = HessianDebugState.this.typeDefList.get(index);
					} else {
						type = "type-unknown(" + index + ")";
					}
				} else if (object instanceof String) {
					HessianDebugState.this.typeDefList.add((String) object);
				}

				this.printObject("list " + type + " (#" + this._refId + ")");

				if (this._isLength) {
					this._state = CompactListState.VALUE;

					if (this._length == 0) {
						return this._next;
					}
				} else {
					this._state = CompactListState.LENGTH;
				}

				return this;
			} else if (this._state == CompactListState.LENGTH) {
				this._length = (Integer) object;

				if (!this._isTyped) {
					this.printObject("list (#" + this._refId + ")");
				}

				this._state = CompactListState.VALUE;

				if (this._length == 0) {
					return this._next;
				} else {
					return this;
				}
			} else {
				return this;
			}
		}

		@Override
		int depth() {
			if (this._state <= CompactListState.LENGTH) {
				return this._next.depth();
			} else if (this._state == CompactListState.VALUE) {
				return this._valueDepth;
			} else {
				return this._next.depth() + 2;
			}
		}

		@Override
		State next(int ch) {
			switch (this._state) {
				case TYPE:
					return this.nextObject(ch);

				case LENGTH:
					return this.nextObject(ch);

				case VALUE:
					if (this._length <= this._count) {
						return this._next.next(ch);
					} else {
						this._valueDepth = this._next.depth() + 2;
						this.println();
						this.printObject(this._count++ + ": ");
						this._valueDepth = HessianDebugState.this.column;
						HessianDebugState.this.isObject = false;

						return this.nextObject(ch);
					}

				default:
					throw new IllegalStateException();
			}
		}
	}

	class Hessian2State extends State {
		private static final int	MAJOR	= 0;
		private static final int	MINOR	= 1;

		private int					_state;
		private int					_major;
		private int					_minor;

		Hessian2State(State next) {
			super(next);
		}

		@Override
		int depth() {
			return this._next.depth() + 2;
		}

		@Override
		State next(int ch) {
			switch (this._state) {
				case MAJOR:
					this._major = ch;
					this._state = Hessian2State.MINOR;
					return this;

				case MINOR:
					this._minor = ch;
					this.println(-2, "Hessian " + this._major + "." + this._minor);
					return this._next;

				default:
					throw new IllegalStateException();
			}
		}
	}

	class CallState1 extends State1 {
		private static final int	MAJOR	= 0;
		private static final int	MINOR	= 1;
		private static final int	HEADER	= 2;
		private static final int	METHOD	= 3;
		private static final int	VALUE	= 4;
		private static final int	ARG		= 5;

		private int					_state;
		private int					_major;
		private int					_minor;

		CallState1(State next) {
			super(next);
		}

		@Override
		int depth() {
			return this._next.depth() + 2;
		}

		@Override
		State next(int ch) {
			switch (this._state) {
				case MAJOR:
					this._major = ch;
					this._state = CallState1.MINOR;
					return this;

				case MINOR:
					this._minor = ch;
					this._state = CallState1.HEADER;
					this.println(-2, "call " + this._major + "." + this._minor);
					return this;

				case HEADER:
					if (ch == 'H') {
						this.println();
						this.print("header ");
						HessianDebugState.this.isObject = false;
						this._state = CallState1.VALUE;
						return new StringState(this, 'H', true);
					} else if (ch == 'm') {
						this.println();
						this.print("method ");
						HessianDebugState.this.isObject = false;
						this._state = CallState1.ARG;
						return new StringState(this, 'm', true);
					} else {
						this.println((char) ch + ": unexpected char");
						return HessianDebugState.this.popStack();
					}

				case VALUE:
					this.print(" => ");
					HessianDebugState.this.isObject = false;
					this._state = CallState1.HEADER;
					return this.nextObject(ch);

				case ARG:
					if (ch == 'z') {
						this.println();
						return this._next;
					} else {
						return this.nextObject(ch);
					}

				default:
					throw new IllegalStateException();
			}
		}
	}

	class Call2State extends State {
		private static final int	METHOD	= 0;
		private static final int	COUNT	= 1;
		private static final int	ARG		= 2;

		private int					_state	= Call2State.METHOD;
		private int					_i;
		private int					_count;

		Call2State(State next) {
			super(next);
		}

		@Override
		int depth() {
			return this._next.depth() + 5;
		}

		@Override
		boolean isShift(Object value) {
			return this._state != Call2State.ARG;
		}

		@Override
		State shift(Object object) {
			if (this._state == Call2State.METHOD) {
				this.println(-5, "Call " + object);

				this._state = Call2State.COUNT;
				return this;
			} else if (this._state == Call2State.COUNT) {
				Integer count = (Integer) object;

				this._count = count;

				this._state = Call2State.ARG;

				if (this._count == 0) {
					return this._next;
				} else {
					return this;
				}
			} else {
				return this;
			}
		}

		@Override
		State next(int ch) {
			switch (this._state) {
				case COUNT:
					return this.nextObject(ch);

				case METHOD:
					return this.nextObject(ch);

				case ARG:
					if (this._count <= this._i) {
						this.println();
						return this._next.next(ch);
					} else {
						this.println();
						this.print(-3, this._i++ + ": ");

						return this.nextObject(ch);
					}

				default:
					throw new IllegalStateException();
			}
		}
	}

	class ReplyState1 extends State1 {
		private static final int	MAJOR	= 0;
		private static final int	MINOR	= 1;
		private static final int	HEADER	= 2;
		private static final int	VALUE	= 3;
		private static final int	END		= 4;

		private int					_state;
		private int					_major;
		private int					_minor;

		ReplyState1(State next) {
			this._next = next;
		}

		@Override
		int depth() {
			return this._next.depth() + 2;
		}

		@Override
		State next(int ch) {
			switch (this._state) {
				case MAJOR:
					if ((ch == 't') || (ch == 'S')) {
						return new RemoteState(this).next(ch);
					}

					this._major = ch;
					this._state = ReplyState1.MINOR;
					return this;

				case MINOR:
					this._minor = ch;
					this._state = ReplyState1.HEADER;
					this.println(-2, "reply " + this._major + "." + this._minor);
					return this;

				case HEADER:
					if (ch == 'H') {
						this._state = ReplyState1.VALUE;
						return new StringState(this, 'H', true);
					} else if (ch == 'f') {
						this.print("fault ");
						HessianDebugState.this.isObject = false;
						this._state = ReplyState1.END;
						return new MapState(this, 0);
					} else {
						this._state = ReplyState1.END;
						return this.nextObject(ch);
					}

				case VALUE:
					this._state = ReplyState1.HEADER;
					return this.nextObject(ch);

				case END:
					this.println();
					if (ch == 'z') {
						return this._next;
					} else {
						return this._next.next(ch);
					}

				default:
					throw new IllegalStateException();
			}
		}
	}

	class Reply2State extends State {
		Reply2State(State next) {
			super(next);

			this.println(-2, "Reply");
		}

		@Override
		int depth() {
			return this._next.depth() + 2;
		}

		@Override
		State next(int ch) {
			if (ch < 0) {
				this.println();
				return this._next;
			} else {
				return this.nextObject(ch);
			}
		}
	}

	class Fault2State extends State {
		Fault2State(State next) {
			super(next);

			this.println(-2, "Fault");
		}

		@Override
		int depth() {
			return this._next.depth() + 2;
		}

		@Override
		State next(int ch) {
			return this.nextObject(ch);
		}
	}

	class IndirectState extends State {
		IndirectState(State next) {
			super(next);
		}

		@Override
		boolean isShift(Object object) {
			return this._next.isShift(object);
		}

		@Override
		State shift(Object object) {
			return this._next.shift(object);
		}

		@Override
		State next(int ch) {
			return this.nextObject(ch);
		}
	}

	class RemoteState extends State {
		private static final int	TYPE	= 0;
		private static final int	VALUE	= 1;
		private static final int	END		= 2;

		private int					state;
		private int					major;
		private int					minor;

		RemoteState(State next) {
			super(next);
		}

		@Override
		State next(int ch) {
			switch (this.state) {
				case TYPE:
					this.println(-1, "remote");
					if (ch == 't') {
						this.state = RemoteState.VALUE;
						return new StringState(this, 't', false);
					} else {
						this.state = RemoteState.END;
						return this.nextObject(ch);
					}

				case VALUE:
					this.state = RemoteState.END;
					return this._next.nextObject(ch);

				case END:
					return this._next.next(ch);

				default:
					throw new IllegalStateException();
			}
		}
	}

	class StreamingState extends State {
		private long	_length;
		private int		_metaLength;
		private boolean	_isLast;
		private boolean	_isFirst	= true;

		private boolean	_isLengthState;

		private State	_childState;

		StreamingState(State next, boolean isLast) {
			super(next);

			this._isLast = isLast;
			this._childState = new InitialState();
		}

		@Override
		State next(int ch) {
			if (this._metaLength > 0) {
				this._length = (256 * this._length) + ch;
				this._metaLength--;

				if ((this._metaLength == 0) && this._isFirst) {
					if (this._isLast) {
						this.println(-1, "--- packet-start(" + this._length + ")");
					} else {
						this.println(-1, "--- packet-start(fragment)");
					}
					this._isFirst = false;
				}

				return this;
			}

			if (this._length > 0) {
				this._length--;
				this._childState = this._childState.next(ch);

				return this;
			}

			if (!this._isLengthState) {
				this._isLengthState = true;

				if (this._isLast) {
					this.println(-1, "");
					this.println(-1, "--- packet-end");
					HessianDebugState.this.refId = 0;

					this._isFirst = true;
				}

				this._isLast = (ch & 0x80) == 0x00;
				this._isLengthState = true;
			} else {
				this._isLengthState = false;
				this._length = (ch & 0x7f);

				if (this._length == 0x7e) {
					this._length = 0;
					this._metaLength = 2;
				} else if (this._length == 0x7f) {
					this._length = 0;
					this._metaLength = 8;
				} else {
					if (this._isFirst) {
						if (this._isLast) {
							this.println(-1, "--- packet-start(" + this._length + ")");
						} else {
							this.println(-1, "--- packet-start(fragment)");
						}
						this._isFirst = false;
					}
				}
			}

			return this;
		}
	}

	static class ObjectDef {
		private final String			_type;
		private final ArrayList<String>	_fields;

		ObjectDef(String type, ArrayList<String> fields) {
			this._type = type;
			this._fields = fields;
		}

		String getType() {
			return this._type;
		}

		ArrayList<String> getFields() {
			return this._fields;
		}
	}
}

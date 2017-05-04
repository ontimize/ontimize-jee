package com.caucho.hessian.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class HessianDebugOutputStream extends OutputStream {

	public HessianDebugOutputStream(OutputStream outputStream, PrintWriter code) {
		throw new UnsupportedOperationException();
	}

	public void startTop2() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void write(int b) throws IOException {
		throw new UnsupportedOperationException();
	}

}

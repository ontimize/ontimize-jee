package com.ontimize.jee.common.tools.streamfilter;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Base class for core filter readers.
 *
 */
public abstract class AbstractBaseFilterReader extends FilterReader {

    /**
     * Creates a new filtered reader.
     * @param in A Reader object providing the underlying stream. Must not be <code>null</code>.
     *
     */
    public AbstractBaseFilterReader(final Reader in) {
        super(in);
    }

    /**
     * Reads characters into a portion of an array. This method will block until some input is
     * available, an I/O error occurs, or the end of the stream is reached.
     * @param cbuf Destination buffer to write characters to. Must not be <code>null</code>.
     * @param off Offset at which to start storing characters.
     * @param len Maximum number of characters to read.
     * @return the number of characters read, or -1 if the end of the stream has been reached
     * @exception IOException If an I/O error occurs
     */
    @Override
    public final int read(final char[] cbuf, final int off, final int len) throws IOException {
        for (int i = 0; i < len; i++) {
            final int ch = this.read();
            if (ch == -1) {
                if (i == 0) {
                    return -1;
                } else {
                    return i;
                }
            }
            cbuf[off + i] = (char) ch;
        }
        return len;
    }

    /**
     * Skips characters. This method will block until some characters are available, an I/O error
     * occurs, or the end of the stream is reached.
     * @param n The number of characters to skip
     * @return the number of characters actually skipped
     * @exception IllegalArgumentException If <code>n</code> is negative.
     * @exception IOException If an I/O error occurs
     */
    @Override
    public final long skip(final long n) throws IOException, IllegalArgumentException {
        if (n < 0L) {
            throw new IllegalArgumentException("skip value is negative");
        }

        for (long i = 0; i < n; i++) {
            if (this.read() == -1) {
                return i;
            }
        }
        return n;
    }

    /**
     * Reads a line of text ending with '\n' (or until the end of the stream). The returned String
     * retains the '\n'.
     * @return the line read, or <code>null</code> if the end of the stream has already been reached
     * @exception IOException if the underlying reader throws one during reading
     */
    protected final String readLine() throws IOException {
        int ch = this.in.read();

        if (ch == -1) {
            return null;
        }

        StringBuilder line = new StringBuilder();

        while (ch != -1) {
            line.append((char) ch);
            if (ch == '\n') {
                break;
            }
            ch = this.in.read();
        }
        return line.toString();
    }

}

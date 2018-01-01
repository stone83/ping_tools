package com.jj.game.boost.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author myx
 *         by 2017-07-04
 */
@SuppressWarnings("WeakerAccess")
public class IOUtil {

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    private static final int EOF = -1;

    /**
     * This class implements an output stream in which the data is
     * written into a byte array. The buffer automatically grows as data
     * is written to it.
     * <p>
     * The data can be retrieved using <code>toByteArray()</code> and
     * <code>toString()</code>.
     * <p>
     * Closing a {@code ByteArrayOutputStream} has no effect. The methods in
     * this class can be called after the stream has been closed without
     * generating an {@code IOException}.
     * <p>
     * This is an alternative implementation of the {@link java.io.ByteArrayOutputStream}
     * class. The original implementation only allocates 32 bytes at the beginning.
     * As this class is designed for heavy duty it starts at 1024 bytes. In contrast
     * to the original it doesn't reallocate the whole memory block but allocates
     * additional buffers. This way no buffers need to be garbage collected and
     * the contents don't have to be copied to the new buffer. This class is
     * designed to behave exactly like the original. The only exception is the
     * deprecated toString(int) method that has been ignored.
     *
     * @version $Id: ByteArrayOutputStream.java 1612034 2014-07-20 06:35:19Z ggregory $
     */
    private static class ByteArrayOutputStream extends OutputStream {
        /**
         * A singleton empty byte array.
         */
        private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

        /**
         * The list of buffers, which grows and never reduces.
         */
        private final List<byte[]> buffers = new ArrayList<>();
        /**
         * The index of the current buffer.
         */
        private int currentBufferIndex;
        /**
         * The total count of bytes in all the filled buffers.
         */
        private int filledBufferSum;
        /**
         * The current buffer.
         */
        private byte[] currentBuffer;
        /**
         * The total count of bytes written.
         */
        private int count;
        /**
         * Flag to indicate if the buffers can be reused after reset
         */
        private boolean reuseBuffers = true;

        /**
         * Creates a new byte array output stream. The buffer capacity is
         * initially 1024 bytes, though its size increases if necessary.
         */
        ByteArrayOutputStream() {
            this(1024);
        }

        /**
         * Creates a new byte array output stream, with a buffer capacity of
         * the specified size, in bytes.
         *
         * @param size the initial size
         * @throws IllegalArgumentException if size is negative
         */
        ByteArrayOutputStream(final int size) {
            if (size < 0) {
                throw new IllegalArgumentException(
                        "Negative initial size: " + size);
            }
            synchronized (this) {
                needNewBuffer(size);
            }
        }

        /**
         * Makes a new buffer available either by allocating
         * a new one or re-cycling an existing one.
         *
         * @param newcount the size of the buffer if one is created
         */
        private void needNewBuffer(final int newcount) {
            if (currentBufferIndex < buffers.size() - 1) {
                //Recycling old buffer
                filledBufferSum += currentBuffer.length;

                currentBufferIndex++;
                currentBuffer = buffers.get(currentBufferIndex);
            } else {
                //Creating new buffer
                int newBufferSize;
                if (currentBuffer == null) {
                    newBufferSize = newcount;
                    filledBufferSum = 0;
                } else {
                    newBufferSize = Math.max(
                            currentBuffer.length << 1,
                            newcount - filledBufferSum);
                    filledBufferSum += currentBuffer.length;
                }

                currentBufferIndex++;
                currentBuffer = new byte[newBufferSize];
                buffers.add(currentBuffer);
            }
        }

        /**
         * Write the bytes to byte array.
         *
         * @param b   the bytes to write
         * @param off The start offset
         * @param len The number of bytes to write
         */
        @SuppressWarnings("NullableProblems")
        @Override
        public void write(final byte[] b, final int off, final int len) {
            if ((off < 0)
                    || (off > b.length)
                    || (len < 0)
                    || ((off + len) > b.length)
                    || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return;
            }
            synchronized (this) {
                final int newcount = count + len;
                int remaining = len;
                int inBufferPos = count - filledBufferSum;
                while (remaining > 0) {
                    final int part = Math.min(remaining, currentBuffer.length - inBufferPos);
                    System.arraycopy(b, off + len - remaining, currentBuffer, inBufferPos, part);
                    remaining -= part;
                    if (remaining > 0) {
                        needNewBuffer(newcount);
                        inBufferPos = 0;
                    }
                }
                count = newcount;
            }
        }

        /**
         * Write a byte to byte array.
         *
         * @param b the byte to write
         */
        @Override
        public synchronized void write(final int b) {
            int inBufferPos = count - filledBufferSum;
            if (inBufferPos == currentBuffer.length) {
                needNewBuffer(count + 1);
                inBufferPos = 0;
            }
            currentBuffer[inBufferPos] = (byte) b;
            count++;
        }

        /**
         * Writes the entire contents of the specified input stream to this
         * byte stream. Bytes from the input stream are read directly into the
         * internal buffers of this streams.
         *
         * @param in the input stream to read from
         * @return total number of bytes read from the input stream
         * (and written to this stream)
         * @throws IOException if an I/O error occurs while reading the input stream
         * @since 1.4
         */
        synchronized int write(final InputStream in) throws IOException {
            int readCount = 0;
            int inBufferPos = count - filledBufferSum;
            int n = in.read(currentBuffer, inBufferPos, currentBuffer.length - inBufferPos);
            while (n != EOF) {
                readCount += n;
                inBufferPos += n;
                count += n;
                if (inBufferPos == currentBuffer.length) {
                    needNewBuffer(currentBuffer.length);
                    inBufferPos = 0;
                }
                n = in.read(currentBuffer, inBufferPos, currentBuffer.length - inBufferPos);
            }
            return readCount;
        }

        /**
         * Return the current size of the byte array.
         *
         * @return the current size of the byte array
         */
        @SuppressWarnings("unused")
        public synchronized int size() {
            return count;
        }

        /**
         * Closing a {@code ByteArrayOutputStream} has no effect. The methods in
         * this class can be called after the stream has been closed without
         * generating an {@code IOException}.
         *
         * @throws IOException never (this method should not declare this exception
         *                     but it has to now due to backwards compatibility)
         */
        @Override
        public void close() throws IOException {
            // nop
        }

        /**
         * @see java.io.ByteArrayOutputStream#reset()
         */
        @SuppressWarnings("unused")
        synchronized void reset() {
            count = 0;
            filledBufferSum = 0;
            currentBufferIndex = 0;
            if (reuseBuffers) {
                currentBuffer = buffers.get(currentBufferIndex);
            } else {
                //Throw away old buffers
                currentBuffer = null;
                int size = buffers.get(0).length;
                buffers.clear();
                needNewBuffer(size);
                reuseBuffers = true;
            }
        }

        /**
         * Writes the entire contents of this byte stream to the
         * specified output stream.
         *
         * @param out the output stream to write to
         * @throws IOException if an I/O error occurs, such as if the stream is closed
         * @see java.io.ByteArrayOutputStream#writeTo(OutputStream)
         */
        @SuppressWarnings("unused")
        public synchronized void writeTo(final OutputStream out) throws IOException {
            int remaining = count;
            for (final byte[] buf : buffers) {
                final int c = Math.min(buf.length, remaining);
                out.write(buf, 0, c);
                remaining -= c;
                if (remaining == 0) {
                    break;
                }
            }
        }

        /**
         * Fetches entire contents of an <code>InputStream</code> and represent
         * same data as result InputStream.
         * <p>
         * This method is useful where,
         * <ul>
         * <li>Source InputStream is slow.</li>
         * <li>It has network resources associated, so we cannot keep it open for
         * long time.</li>
         * <li>It has network timeout associated.</li>
         * </ul>
         * It can be used in favor of {@link #toByteArray()}, since it
         * avoids unnecessary allocation and copy of byte[].<br>
         * This method buffers the input internally, so there is no need to use a
         * <code>BufferedInputStream</code>.
         *
         * @param input Stream to be fully buffered.
         * @return A fully buffered stream.
         * @throws IOException if an I/O error occurs
         * @since 2.0
         */
        @SuppressWarnings("unused")
        public static InputStream toBufferedInputStream(final InputStream input)
                throws IOException {
            return toBufferedInputStream(input, 1024);
        }

        /**
         * Fetches entire contents of an <code>InputStream</code> and represent
         * same data as result InputStream.
         * <p>
         * This method is useful where,
         * <ul>
         * <li>Source InputStream is slow.</li>
         * <li>It has network resources associated, so we cannot keep it open for
         * long time.</li>
         * <li>It has network timeout associated.</li>
         * </ul>
         * It can be used in favor of {@link #toByteArray()}, since it
         * avoids unnecessary allocation and copy of byte[].<br>
         * This method buffers the input internally, so there is no need to use a
         * <code>BufferedInputStream</code>.
         *
         * @param input Stream to be fully buffered.
         * @param size  the initial buffer size
         * @return A fully buffered stream.
         * @throws IOException if an I/O error occurs
         * @since 2.5
         */
        static InputStream toBufferedInputStream(final InputStream input, int size)
                throws IOException {
            // It does not matter if a ByteArrayOutputStream is not closed as close() is a no-op
            @SuppressWarnings("resource")
            final ByteArrayOutputStream output = new ByteArrayOutputStream(size);
            output.write(input);
            return output.toInputStream();
        }

        /**
         * Gets the current contents of this byte stream as a Input Stream. The
         * returned stream is backed by buffers of <code>this</code> stream,
         * avoiding memory allocation and copy, thus saving space and time.<br>
         *
         * @return the current contents of this output stream.
         * @see java.io.ByteArrayOutputStream#toByteArray()
         * @see #reset()
         * @since 2.5
         */
        synchronized InputStream toInputStream() {
            int remaining = count;
            if (remaining == 0) {
                return new ClosedInputStream();
            }
            final List<ByteArrayInputStream> list = new ArrayList<>(buffers.size());
            for (final byte[] buf : buffers) {
                final int c = Math.min(buf.length, remaining);
                list.add(new ByteArrayInputStream(buf, 0, c));
                remaining -= c;
                if (remaining == 0) {
                    break;
                }
            }
            reuseBuffers = false;
            return new SequenceInputStream(Collections.enumeration(list));
        }

        /**
         * Gets the curent contents of this byte stream as a byte array.
         * The result is independent of this stream.
         *
         * @return the current contents of this output stream, as a byte array
         * @see java.io.ByteArrayOutputStream#toByteArray()
         */
        synchronized byte[] toByteArray() {
            int remaining = count;
            if (remaining == 0) {
                return EMPTY_BYTE_ARRAY;
            }
            final byte newbuf[] = new byte[remaining];
            int pos = 0;
            for (final byte[] buf : buffers) {
                final int c = Math.min(buf.length, remaining);
                System.arraycopy(buf, 0, newbuf, pos, c);
                pos += c;
                remaining -= c;
                if (remaining == 0) {
                    break;
                }
            }
            return newbuf;
        }

        /**
         * Gets the curent contents of this byte stream as a string
         * using the platform default charset.
         *
         * @return the contents of the byte array as a String
         * @see java.io.ByteArrayOutputStream#toString()
         * @deprecated 2.5 use {@link #toString(String)} instead
         */
        @Override
        @Deprecated
        public String toString() {
            // make explicit the use of the default charset
            return new String(toByteArray(), Charset.defaultCharset());
        }

        /**
         * Gets the curent contents of this byte stream as a string
         * using the specified encoding.
         *
         * @param enc the name of the character encoding
         * @return the string converted from the byte array
         * @throws UnsupportedEncodingException if the encoding is not supported
         * @see java.io.ByteArrayOutputStream#toString(String)
         */
        @SuppressWarnings("unused")
        String toString(final String enc) throws UnsupportedEncodingException {
            return new String(toByteArray(), enc);
        }

        /**
         * Gets the curent contents of this byte stream as a string
         * using the specified encoding.
         *
         * @param charset the character encoding
         * @return the string converted from the byte array
         * @see java.io.ByteArrayOutputStream#toString(String)
         * @since 2.5
         */
        @SuppressWarnings("unused")
        public String toString(final Charset charset) {
            return new String(toByteArray(), charset);
        }
    }

    /**
     * Closed input stream. This stream returns EOF to all attempts to read
     * something from the stream.
     * <p>
     * Typically uses of this class include testing for corner cases in methods
     * that accept input streams and acting as a sentinel value instead of a
     * {@code null} input stream.
     *
     * @version $Id: ClosedInputStream.java 1586350 2014-04-10 15:57:20Z ggregory $
     * @since 1.4
     */
    private static class ClosedInputStream extends InputStream {
        /**
         * Returns -1 to indicate that the stream is closed.
         *
         * @return always -1
         */
        @Override
        public int read() {
            return EOF;
        }

    }

    public static byte[] toByteArray(final InputStream input) throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
//        long count = 0;
        int n;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
//            count += n;
        }
//        if (count > Integer.MAX_VALUE) {
//            return null;
//        }
        return output.toByteArray();
    }

}

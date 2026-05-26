/*
 *
 *    Copyright 1996-2026 Traction Software, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

// PLEASE DO NOT DELETE THIS LINE - make copyright depends on it.

package com.tractionsoftware.commons.io;

import com.google.common.annotations.Beta;
import com.google.common.collect.Iterators;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.tractionsoftware.commons.lang.Resource;
import com.tractionsoftware.commons.lang.ObjectUtil;
import jakarta.annotation.Nonnull;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.function.IORunnable;
import org.apache.commons.io.function.IOSupplier;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.io.input.CloseShieldReader;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.commons.io.output.CloseShieldWriter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * General I/O related helpers.
 *
 * @author Dave Shepperton, Andy Keller
 */
public final class IOUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(IOUtil.class.getName());

    public static int DEFAULT_IO_BUFFER_SIZE = 10240;

    /*
     * Not instantiable.
     */
    private IOUtil() {
    }

    /**
     * Closes the given {@link AutoCloseable}, gracefully handling null and preventing any Exceptions from propagating.
     *
     * @param closeMe
     *     to be closed.
     */
    public static void close(AutoCloseable closeMe) {
        if (closeMe != null) {
            try {
                closeMe.close();
            }
            catch (Exception e) {
                LOGGER.warn("Failed to close {} ", ObjectUtil.safeToString(closeMe, "(AutoCloseable?)"), e);
            }
        }
    }

    /**
     * Flushes the given {@link Flushable}, gracefully handling null and preventing any {@link Exception}s from
     * propagating.
     *
     * @param flushMe
     *     to be flushed.
     */
    public static void flush(Flushable flushMe) {
        if (flushMe != null) {
            try {
                flushMe.flush();
            }
            catch (IOException e) {
                LOGGER.warn("Failed to flush {} ", ObjectUtil.safeToString(flushMe, "(Flushable?)"), e);
            }
        }
    }

    public static class CopyResult {

        private final long bytesRead;

        private final long bytesWritten;

        private CopyResult(long bytesRead, long bytesWritten) {
            this.bytesRead = bytesRead;
            this.bytesWritten = bytesWritten;
        }

        @Override
        public String toString() {
            return "[read " + bytesRead + "B, wrote " + bytesWritten + "]";
//            return "[read " +
//                   NumberFormats.getFormattedByteSize(bytesRead) +
//                   ", wrote " +
//                   NumberFormats.getFormattedByteSize(bytesWritten) +
//                   "]";
        }

        public long getBytesRead() {
            return bytesRead;
        }

        public long getBytesWritten() {
            return bytesWritten;
        }

        public boolean inputWasTooLarge() {
            return (bytesWritten == -2L);
        }

        public boolean triedToCopy() {
            return (bytesWritten == -1L);
        }

    }

    private static abstract class CustomFilterInputStream extends FilterInputStream {

        CustomFilterInputStream(InputStream input) {
            super(input);
        }

        final InputStream getWrapped() {
            return in;
        }

        final boolean isBuffered() {
            return IOUtil.isBufferedInputStream(in);
        }

    }

    private static abstract class CustomFilterOutputStream extends FilterOutputStream {

        CustomFilterOutputStream(OutputStream output) {
            super(output);
        }

        final OutputStream getWrapped() {
            return out;
        }

        final boolean isBuffered() {
            return IOUtil.isBufferedOutputStream(out);
        }

    }

    private static class CloseNotifyingInputStream extends FilterInputStream {

        private final AtomicBoolean closed = new AtomicBoolean(false);

        private final String sourceIdentifier;

        private volatile IORunnable onBeforeClose;

        private volatile IORunnable onAfterClose;

        private CloseNotifyingInputStream(InputStream input, IORunnable onBeforeClose, IORunnable onAfterClose, String sourceIdentifier) {
            super(input);
            this.onBeforeClose = onBeforeClose;
            this.onAfterClose = onAfterClose;
            this.sourceIdentifier = sourceIdentifier;
        }

        @Override
        public final void close() throws IOException {
            if (closed.compareAndSet(false, true)) {
                try {
                    runMainIORunnable(onBeforeClose, super::close, onAfterClose);
                }
                finally {
                    if (sourceIdentifier != null) {
                        LOGGER.debug("Closing InputStream for {} ", sourceIdentifier);
                    }
                    onBeforeClose = null;
                    onAfterClose = null;
                }
            }
        }

    }

    private static class CloseNotifyingOutputStream extends CustomFilterOutputStream {

        private final AtomicBoolean closed = new AtomicBoolean(false);

        private final String sourceIdentifier;

        private volatile IORunnable onBeforeClose;

        private volatile IORunnable onAfterClose;

        private CloseNotifyingOutputStream(OutputStream output, IORunnable onBeforeClose, IORunnable onAfterClose, String sourceIdentifier) {
            super(output);
            this.onBeforeClose = onBeforeClose;
            this.onAfterClose = onAfterClose;
            this.sourceIdentifier = sourceIdentifier;
        }

        @Override
        public final void close() throws IOException {
            if (closed.compareAndSet(false, true)) {
                try {
                    runMainIORunnable(onBeforeClose, super::close, onAfterClose);
                }
                finally {
                    if (sourceIdentifier != null) {
                        LOGGER.debug("Closing OutputStream for {}", sourceIdentifier);
                    }
                    onBeforeClose = null;
                    onAfterClose = null;
                }
            }
        }

    }

    private static class ByteSizeLimitingOutputStream extends CustomFilterOutputStream {

        private final long maximumBytes;

        private long bytesAttempted;

        private ByteSizeLimitingOutputStream(OutputStream out, long maximumBytes) {
            super(out);
            this.maximumBytes = maximumBytes;
        }

        @Override
        public final void write(@Nonnull byte[] b, int off, int len) throws IOException {

            if (len == 0) {
                return;
            }

            if (bytesAttempted < maximumBytes) {
                long available = maximumBytes - bytesAttempted;
                int writeLen;
                if (available > len) {
                    writeLen = len;
                }
                else {
                    writeLen = (int) available;
                }
                out.write(b, off, writeLen);
            }

            bytesAttempted += len;

        }

        @Override
        public final void write(int b) throws IOException {
            if (bytesAttempted < maximumBytes) {
                out.write(b);
            }
            bytesAttempted++;
        }

        @Override
        public final void flush() throws IOException {
            try {
                super.flush();
            }
            finally {
                checkOverLimitX();
            }
        }

        private final void checkOverLimitX() throws IOException {
            if (bytesAttempted > maximumBytes) {
                throw new OutputStreamLimitExceededException(bytesAttempted, maximumBytes);
            }
        }

        @Override
        public final void close() throws IOException {

            RuntimeException suppressed = null;

            try {
                flush();
            }
            catch (IOException e) {
                IOUtil.close(out);
                throw e;
            }
            catch (RuntimeException e) {
                suppressed = e;
            }

            try {
                super.close();
            }
            catch (IOException e) {
                if (suppressed != null) {
                    e.addSuppressed(suppressed);
                }
                throw e;
            }

            if (suppressed != null) {
                throw new RuntimeException(suppressed);
            }

        }

    }

    private static class CompoundAutoCloseable implements AutoCloseable {

        private final Iterable<? extends AutoCloseable> resources;

        private CompoundAutoCloseable(Iterable<? extends AutoCloseable> resources) {
            this.resources = resources;
        }

        @Override
        public final void close() {
            for (AutoCloseable one : resources) {
                IOUtil.close(one);
            }
        }

    }

    private static class FlushInsteadOfClosePrintWriter extends PrintWriter {

        private FlushInsteadOfClosePrintWriter(OutputStream out, boolean autoFlush) {
            super(out, autoFlush);
        }

        private FlushInsteadOfClosePrintWriter(Writer out, boolean autoFlush) {
            super(out, autoFlush);
        }

        @Override
        public final void close() {
            this.flush();
        }

    }

    private static class PrintWriterOutputStream extends OutputStream {

        private final PrintWriter out;

        private final CharsetDecoder decoder;

        private final byte[] oneByte = new byte[] { 0 };

        private final boolean allowClose;

        private PrintWriterOutputStream(PrintWriter out, boolean allowClose) {
            this.out = out;
            this.decoder = StandardCharsets.UTF_8.newDecoder();
            this.allowClose = allowClose;
        }

        @Override
        public void write(int b) throws IOException {
            oneByte[0] = (byte) b;
            out.print(decoder.decode(ByteBuffer.wrap(oneByte)));
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            Objects.checkFromIndexSize(off, len, b.length);
            out.print(decoder.decode(ByteBuffer.wrap(b, off, len)));
        }

        @Override
        public void close() throws IOException {
            if (allowClose) {
                super.close();
            }
        }

    }

    public static byte[] readContentBytes(InputStream in) throws IOException {
        if (in == null) {
            return null;
        }
        return in.readAllBytes();
    }

    /**
     * Returns the content read from the given stream in the form of a String created using the Charset with the given
     * name.
     *
     * <p>
     * WARNING: This operation reads all data from the given InputStream into a StringBuilder, and then creates a
     * String. It is not advisable to use it for very large inputs.
     *
     * @param in
     *     the InputStream whose content should be read and converted to a String.
     * @param charsetName
     *     the name of the Charset to use to create the String from the InputStream content. UTF-8 will be used if the
     *     argument for this parameter is null or whitespace.
     * @return the content read as constructed using the Charset with the given name.
     * @throws IOException
     *     if there is a problem reading the given InputStream, or if the given Charset name is not recognized.
     */
    public static String readContent(InputStream in, String charsetName) throws IOException {
        if (StringUtils.isBlank(charsetName)) {
            return readContent(in, StandardCharsets.UTF_8);
        }
        return readContent(in, java.nio.charset.Charset.forName(charsetName));
    }

    /**
     * Returns the content read from the given stream in the form of a String created using the given Charset.
     *
     * <p>
     * WARNING: This operation reads all data from the given InputStream into a StringBuilder, and then creates a
     * String. It is not advisable to use it for very large inputs.
     *
     * @param in
     *     the InputStream whose content should be read and converted to a String.
     * @param charset
     *     the Charset to use to convert the content of the InputStream to a String. If this value is null, UTF-8 will
     *     be used.
     * @return the content read as constructed using the given Charset.
     * @throws IOException
     *     if there is a problem reading from the given InputStream.
     */
    public static String readContent(InputStream in, Charset charset) throws IOException {
        return readContent(new InputStreamReader(in, ObjectUtils.getIfNull(charset, StandardCharsets.UTF_8)));
    }

    /**
     * Returns the content read from the given {@link Reader} in the form of a String.
     *
     * <p>
     * WARNING: This operation reads all data from the given Reader into a StringBuilder, and then creates a String. It
     * is not advisable to use it for very large inputs.
     *
     * @param reader
     *     the {@link Reader} whose content should be read out as a String.
     * @return the content read from the given {@link Reader}.
     * @throws IOException
     *     if there is a problem reading from the given Reader.
     */
    public static String readContent(Reader reader) throws IOException {
        if (reader == null) {
            return "";
        }
        StringBuilder buff = new StringBuilder();
        IOUtils.copy(reader, buff);
        return buff.toString();
    }

    public static void copyContent(InputStream in, Charset charset, Writer writer) throws IOException {
        if (in == null || writer == null) {
            return;
        }
        charset = ObjectUtils.getIfNull(charset, StandardCharsets.UTF_8);
        IOUtils.copy(in, writer, charset);
    }

    public static long copyContent(Reader reader, Writer writer) throws IOException {
        if (reader == null || writer == null) {
            return 0;
        }
        return IOUtils.copy(reader, writer);
    }

    @CanIgnoreReturnValue
    public static CopyResult copyToEOF(InputStream in, OutputStream out) throws IOException {
        if (in == null || out == null) {
            return new CopyResult(0, -1L);
        }
        long readWritten = in.transferTo(out);
        out.flush();
        return new CopyResult(readWritten, readWritten);
    }

    public static CopyResult copyToEOF(InputStream in, OutputStream out, long maxBytes) throws IOException {

        if (in == null || out == null) {
            return new CopyResult(0, -1L);
        }

        if (maxBytes < 0) {
            maxBytes = Long.MAX_VALUE;
        }

        long totalBytesRead = 0;
        long remainingBytesToWrite = maxBytes;
        long totalBytesWritten = 0;
        int bytesRead;

        final byte[] data = new byte[DEFAULT_IO_BUFFER_SIZE];

        while ((bytesRead = in.read(data)) > 0) {

            totalBytesRead += bytesRead;

            // Already maxed on a previous iteration.
            if (remainingBytesToWrite < 0) {
                continue;
            }

            int bytesToWrite;

            // We have enough room left in the max to write all bytes.
            if (remainingBytesToWrite >= bytesRead) {
                bytesToWrite = bytesRead;
                remainingBytesToWrite -= bytesToWrite;
            }
            // Will be over the max on this iteration
            else {
                bytesToWrite = (int) remainingBytesToWrite;
                remainingBytesToWrite = -1L;
            }

            if (bytesToWrite > 0) {
                out.write(data, 0, bytesToWrite);
                totalBytesWritten += bytesToWrite;
            }

        }

        out.flush();

        if (remainingBytesToWrite < 0) {
            return new CopyResult(totalBytesRead, -2L);
        }

        return new CopyResult(totalBytesRead, totalBytesWritten);

    }

    public static int copyContent(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[DEFAULT_IO_BUFFER_SIZE];
        int ret = 0;
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
            ret += bytesRead;
        }
        return ret;
    }

    public static int copyContent(InputStream in, OutputStream out, StringBuffer sb) throws IOException {
        return copyContent(in, out, sb, false);
    }

    public static int copyContent(InputStream in, OutputStream out, StringBuffer sb, boolean waitForEOF)
        throws IOException {
        if (in == null || out == null) {
            return 0;
        }

        int ret = 0;

        byte[] b = new byte[DEFAULT_IO_BUFFER_SIZE];

        int bytesRead = 10;

        while (in.available() > 0 ||
               (waitForEOF && bytesRead > 0)) {

            bytesRead = in.read(b, 0, DEFAULT_IO_BUFFER_SIZE);

            if (sb != null && sb.length() < 10240) {  // don't record more than 10K
                sb.append(new String(b, 0, bytesRead));
            }

            if (bytesRead != -1) {

                ret += bytesRead;

                out.write(b, 0, bytesRead);
            }
        }

        out.flush();

        return ret;
    }

    /**
     * Copies the streams up to a specified limit. This version was added to implement Server6295 [ajm 02.Apr.2003]
     */
    public static long copyContent(InputStream in, OutputStream out, long numbytes) throws IOException {
        byte[] buf = new byte[DEFAULT_IO_BUFFER_SIZE];
        long remain = numbytes;
        int len;
        while (remain > 0 && (len = in.read(buf)) != -1) {
            if (len > remain) {
                len = (int) remain; // don't write the entire buffer
            }
            remain -= len;
            out.write(buf, 0, len);
        }
        return numbytes - remain;
    }

    /**
     * Provides a callback mechanism for {@link IORunnable}s to be run before and after an {@link InputStream} is
     * closed. The callbacks will be invoked exactly once, and will run regardless of whether any checked or unchecked
     * Exceptions are raised during any other part of the procedure, including the {@link InputStream#close()} method
     * and either one of the operations themselves.
     *
     * <p>
     * If both the before-close and after-close operations are null, the InputStream will be returned as-is.
     *
     * @param input
     *     the {@link InputStream} for which the given callbacks are to be invoked before and after closing.
     * @param onBeforeClose
     *     the callback to be invoked before closing the given {@link InputStream}, if any.
     * @param onAfterClose
     *     the callback to be invoked after closing the given {@link InputStream}, if any.
     * @param sourceIdentifier
     *     a simple identifier for the stream source to appear in diagnostic logging as necessary.
     * @return an {@link InputStream} that is identical to the given InputStream, but which will invoke the given
     *     callbacks before and after its {@link InputStream#close()} method.
     */
    public static InputStream getCloseNotifyingInputStream(InputStream input, IORunnable onBeforeClose, IORunnable onAfterClose, String sourceIdentifier) {
        return new CloseNotifyingInputStream(input, onBeforeClose, onAfterClose, sourceIdentifier);
    }

    /**
     * Returns an {@link OutputStream} wrapping the given stream which will only permit the given maximum number of
     * bytes to be written to the stream. If the limit is exceeded, the OutputStream's write methods will continue to
     * work normally, but its {@link OutputStream#flush()} and {@link OutputStream#close()} methods will throw an
     * {@link OutputStreamLimitExceededException} (while still flushing and closing the underlying stream).
     *
     * @param output
     *     the {@link OutputStream} to be wrapped.
     * @param maximumBytes
     *     the upper limit on the number of bytes that should be allowed to be written to the stream.
     * @return n {@link OutputStream} wrapping the given stream which will only permit the given maximum number of bytes
     *     to be written to the stream, if the given maximum byte count is 0 or greater, and not {@link Long#MAX_VALUE};
     *     the given OutputStream otherwise.
     */
    @Beta
    public static OutputStream getSizeLimitingOutputStream(OutputStream output, long maximumBytes) {
        if (maximumBytes < 0 || maximumBytes == Long.MAX_VALUE) {
            return output;
        }
        Objects.requireNonNull(output);
        return new ByteSizeLimitingOutputStream(output, maximumBytes);
    }

    public static InputStream getBufferedInputStream(InputStream input) {
        if (isBufferedInputStream(input)) {
            return input;
        }
        return new BufferedInputStream(input);
    }

    /**
     * Returns a new {@link InputStream} wrapping the given InputStream, which will retrieve a {@link Resource} from the
     * given {@link Supplier} when the new stream is created, which the new stream will be responsible for closing when
     * its {@link InputStream#close()} method is invoked. This is suitable for starting and stopping a resource tracker,
     * a timer, or handling some other associated resource which must be managed in conjunction with the same
     * InputStream.
     *
     * @param input
     *     the {@link InputStream} to be tracked.
     * @param getTracker
     *     supplies a {@link Resource} which should be closed when the returned {@link InputStream} is closed.
     * @return a wrapped version of the given {@link InputStream}.
     */
    public static InputStream getTrackedInputStream(InputStream input, Supplier<? extends Resource> getTracker) {
        return getTrackedInputStream(input, getTracker, null);
    }

    /**
     * Returns a new {@link InputStream} wrapping the given InputStream, which will retrieve a {@link Resource} from the
     * given {@link Supplier} when the new stream is created, which the new stream will be responsible for closing when
     * its {@link InputStream#close()} method is invoked. This is suitable for starting and stopping a resource tracker,
     * a timer, or handling some other associated resource which must be managed in conjunction with the same
     * InputStream.
     *
     * @param input
     *     the {@link InputStream} to be tracked.
     * @param getTracker
     *     supplies a {@link Resource} which should be closed when the returned {@link InputStream} is closed.
     * @param sourceIdentifier
     *     a simple identifier for the stream source to appear in diagnostic logging as necessary.
     * @return a wrapped version of the given {@link InputStream}.
     * @see #getCloseNotifyingInputStream(InputStream, IORunnable, IORunnable)
     */
    public static InputStream getTrackedInputStream(InputStream input, Supplier<? extends Resource> getTracker, String sourceIdentifier) {
        Resource tracker = getTracker.get();
        try {
            return getCloseNotifyingInputStream(input, null, tracker::close, sourceIdentifier);
        }
        catch (RuntimeException | Error e) {
            tracker.close();
            throw e;
        }
    }

    /**
     * Returns a wrapped version of the given {@link File}'s {@link FileInputStream} which incorporates buffering and
     * tracking.
     *
     * @param file
     *     the {@link File} to be read.
     * @return a wrapped version of the given {@link File}'s {@link FileInputStream} which incorporates buffering and
     *     tracking.
     * @throws IOException
     *     if one is raised while attempting to create a {@link FileInputStream}.
     * @see FileUtil#getBufferedInputStream(File)
     * @see IOUtil#getTrackedInputStream(InputStream, Supplier, String)
     */
    public static InputStream getBufferedTrackedInputStream(File file, Supplier<? extends Resource> getTracker)
        throws IOException {
        return getTrackedInputStream(FileUtil.getBufferedInputStream(file), getTracker, file.toString());
    }

    /**
     * Provides a callback mechanism for {@link IORunnable}s to be run before and after an {@link InputStream} is
     * closed. The callbacks will be invoked exactly once, and will run regardless of whether any checked or unchecked
     * Exceptions are raised during any other part of the procedure, including the {@link InputStream#close()} method
     * and either one of the operations themselves.
     *
     * <p>
     * If both the before-close and after-close operations are null, the InputStream will be returned as-is.
     *
     * @param input
     *     the {@link InputStream} for which the given callbacks are to be invoked before and after closing.
     * @param onBeforeClose
     *     the callback to be invoked before closing the given {@link InputStream}, if any.
     * @param onAfterClose
     *     the callback to be invoked after closing the given {@link InputStream}, if any.
     * @return an {@link InputStream} that is identical to the given InputStream, but which will invoke the given
     *     callbacks before and after its {@link InputStream#close()} method.
     */
    public static InputStream getCloseNotifyingInputStream(InputStream input, IORunnable onBeforeClose, IORunnable onAfterClose) {
        return getCloseNotifyingInputStream(input, onBeforeClose, onAfterClose, null);
    }

    /**
     *
     * @param output
     *     the {@link OutputStream} to be tracked.
     * @return a wrapped version of the given {@link OutputStream}.
     */
    public static OutputStream getTrackedOutputStream(OutputStream output, Supplier<? extends Resource> getTracker) {
        return getTrackedOutputStream(output, getTracker, null);
    }

    /**
     *
     * @param output
     *     the {@link OutputStream} to be tracked.
     * @param sourceIdentifier
     *     a simple identifier for the stream source to appear in diagnostic logging as necessary.
     * @return a wrapped version of the given {@link OutputStream}.
     */
    public static OutputStream getTrackedOutputStream(OutputStream output, Supplier<? extends Resource> getTracker, String sourceIdentifier) {
        Resource tracker = getTracker.get();
        try {
            return getCloseNotifyingOutputStream(output, null, tracker::close, sourceIdentifier);
        }
        catch (RuntimeException | Error e) {
            tracker.close();
            throw e;
        }
    }

    /**
     * Provides a callback mechanism for {@link IORunnable}s to be run before and after an {@link OutputStream} is
     * closed. The callbacks will be invoked exactly once, and will run regardless of whether any checked or unchecked
     * Exceptions are raised during any other part of the procedure, including the {@link OutputStream#close()} method
     * and either one of the operations themselves.
     *
     * <p>
     * If both the before-close and after-close operations are null, the OutputStream will be returned as-is.
     *
     * @param output
     *     the {@link OutputStream} for which the given callbacks are to be invoked before and after closing.
     * @param onBeforeClose
     *     the callback to be invoked before closing the given {@link OutputStream}, if any.
     * @param onAfterClose
     *     the callback to be invoked after closing the given {@link OutputStream}, if any.
     * @return an {@link OutputStream} that is identical to the given OutputStream, but which will invoke the given
     *     callbacks before and after its {@link OutputStream#close()} method.
     */
    public static OutputStream getCloseNotifyingOutputStream(OutputStream output, IORunnable onBeforeClose, IORunnable onAfterClose) {
        return getCloseNotifyingOutputStream(output, onBeforeClose, onAfterClose, null);
    }

    /**
     * Provides a callback mechanism for {@link IORunnable}s to be run before and after an {@link OutputStream} is
     * closed. The callbacks will be invoked exactly once, and will run regardless of whether any checked or unchecked
     * Exceptions are raised during any other part of the procedure, including the {@link OutputStream#close()} method
     * and either one of the operations themselves.
     *
     * <p>
     * If both the before-close and after-close operations are null, the OutputStream will be returned as-is.
     *
     * @param output
     *     the {@link OutputStream} for which the given callbacks are to be invoked before and after closing.
     * @param onBeforeClose
     *     the callback to be invoked before closing the given {@link OutputStream}, if any.
     * @param onAfterClose
     *     the callback to be invoked after closing the given {@link OutputStream}, if any.
     * @param sourceIdentifier
     *     a simple identifier for the stream source to appear in diagnostic logging as necessary.
     * @return an {@link OutputStream} that is identical to the given OutputStream, but which will invoke the given
     *     callbacks before and after its {@link OutputStream#close()} method.
     */
    public static OutputStream getCloseNotifyingOutputStream(OutputStream output, IORunnable onBeforeClose, IORunnable onAfterClose, String sourceIdentifier) {
        if (onBeforeClose == null && onAfterClose == null) {
            return output;
        }
        return new CloseNotifyingOutputStream(output, onBeforeClose, onAfterClose, sourceIdentifier);
    }

    /**
     * Helper method to produce a SequenceInputStream from varargs.
     *
     * @param streams
     *     the streams to be included in the SequenceInputStream.
     * @return a SequenceInputStream from the given InputStreams.
     */
    public static InputStream getSequenceInputStream(final InputStream... streams) {
        return new SequenceInputStream(Iterators.asEnumeration(Arrays.asList(streams).iterator()));
    }

    /**
     * Runs an {@link IORunnable}, wrapping any {@link IOException} raised in an {@link UncheckedIOException}.
     *
     * @param operation
     *     the {@link IORunnable} to run.
     * @throws UncheckedIOException
     *     if {@link IORunnable#run() running the given operation} raises an IOException.
     */
    public static void runIOOperation(IORunnable operation) {
        try {
            operation.run();
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Runs an {@link IORunnable} and prevents an {@link IOException} from propagating, instead wrapping it in an
     * {@link UncheckedIOException}.
     *
     * @param operation
     *     the {@link IORunnable} to run.
     */
    public static void runIOOperationSafe(IORunnable operation) {
        try {
            operation.run();
        }
        catch (IOException e) {
            LOGGER.warn("{} failed", ObjectUtil.safeToStringObject(operation), e);
        }
    }

    /**
     * Runs an {@link IOSupplier}, wrapping any {@link IOException} raised in an {@link UncheckedIOException}.
     *
     * @param supplier
     *     the {@link IOSupplier} to run.
     * @throws UncheckedIOException
     *     if {@link IOSupplier#get() running the given operation} raises an IOException.
     */
    public static <T> T runIOSupplier(IOSupplier<T> supplier) {
        try {
            return supplier.get();
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Runs an {@link IOSupplier} and prevents an {@link IOException} from propagating, instead wrapping it in an
     * {@link UncheckedIOException}.
     *
     * @param supplier
     *     the {@link IOSupplier} to run.
     */
    public static <T> T runIOSupplierSafe(IOSupplier<T> supplier, Supplier<? extends T> defaultValue) {
        try {
            return supplier.get();
        }
        catch (IOException e) {
            LOGGER.warn("{} failed", ObjectUtil.safeToStringObject(supplier), e);
        }
        return defaultValue.get();
    }

    public static Supplier<ByteArrayInputStream> byteArrayInputStreamSupplier(byte[] data) {
        Objects.requireNonNull(data, "bytes");
        return () -> new ByteArrayInputStream(data);
    }

    public static OutputStream getPrintWriterOutputStream(PrintWriter out, boolean allowClose) {
        return new PrintWriterOutputStream(out, allowClose);
    }
//            IOUtil.getNoCloseOutputStream(WriterOutputStream.builder().setWriter(out).get())

    private static void runMainIORunnable(IORunnable before, IORunnable main, IORunnable after)
        throws IOException {

        IOException mainIOE = null;
        RuntimeException mainRE = null;
        List<Exception> suppressed = new CopyOnWriteArrayList<>();

        if (before != null) {
            try {
                before.run();
            }
            catch (Exception e) {
                suppressed.add(e);
            }
        }

        try {
            main.run();
        }
        catch (IOException e) {
            mainIOE = e;
        }
        catch (RuntimeException e) {
            if (e.getCause() instanceof IOException ioe) {
                mainIOE = ioe;
            }
            else {
                mainRE = e;
            }
        }

        if (after != null) {
            try {
                after.run();
            }
            catch (Exception e) {
                suppressed.add(e);
            }
        }

        if (mainIOE != null) {
            for (Exception e : suppressed) {
                mainIOE.addSuppressed(e);
            }
            throw mainIOE;
        }

        if (mainRE != null) {
            for (Exception e : suppressed) {
                mainRE.addSuppressed(e);
            }
            throw mainRE;
        }

    }

    public static AutoCloseable createCompoundCloseable(Iterable<? extends AutoCloseable> resources) {
        Objects.requireNonNull(resources, "resources");
        return new CompoundAutoCloseable(resources);
    }

    public static BufferedReader getBufferedUtf8Reader(InputStream input) {
        return getBufferedReader(input, null);
    }

    public static BufferedReader getBufferedReader(InputStream input, Charset charset) {
        Objects.requireNonNull(input, "InputStream");
        return new BufferedReader(
            new InputStreamReader(input, charset == null ? StandardCharsets.UTF_8 : charset)
        );
    }

    public static BufferedReader getBufferedReader(Reader reader) {
        if (reader instanceof BufferedReader alreadyBuffered) {
            return alreadyBuffered;
        }
        return new BufferedReader(reader);
    }

    public static InputStream getStringAsUtf8InputStream(String str) {
        return getStringAsInputStream(str, StandardCharsets.UTF_8);
    }

    public static InputStream getStringAsInputStream(String str, Charset charset) {
        Objects.requireNonNull(str, "string");
        Objects.requireNonNull(charset, "charset");
        try {
            return ReaderInputStream.builder().setReader(new StringReader(str)).setCharset(charset).get();
        }
        catch (IOException e) {
            throw new IllegalStateException("This IOException should not be able to happen.", e);
        }
    }

    public static OutputStream getBufferedOutputStream(OutputStream output) {
        if (isBufferedOutputStream(output)) {
            return output;
        }
        return new BufferedOutputStream(output);
    }

    /**
     * Returns a {@link BufferedWriter} that will write UTF-8 character data to the given {@link OutputStream}.
     *
     * @param out
     *     the {@link OutputStream}
     * @return a {@link BufferedWriter} that will write UTF-8 character data to the given {@link OutputStream}.
     * @throws NullPointerException
     *     if the given {@link OutputStream} is null.
     */
    public static BufferedWriter getBufferedUtf8Writer(OutputStream out) {
        Objects.requireNonNull(out, "OutputStream");
        return new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
    }

    /**
     * Returns a new {@link PrintWriter} wrapping the given {@link OutputStream}, and whose {@link PrintWriter#close()}
     * method will not close it or the OutputStream, but will instead be equivalent to invoking
     * {@link PrintWriter#flush()}. This can be useful when a PrintWriter is needed as a temporary thin wrapper for an
     * OutputStream.
     *
     * @param out
     *     the {@link OutputStream} to wrap.
     * @return a new {@link PrintWriter} wrapping the given {@link OutputStream}, and whose {@link PrintWriter#close()}
     *     method will not close it or the OutputStream, but will instead be equivalent to invoking
     *     {@link PrintWriter#flush()}.
     * @throws NullPointerException
     *     if the given {@link OutputStream} is null.
     */
    public static PrintWriter getFlushInsteadOfClosePrintWriter(OutputStream out) {
        return getFlushInsteadOfClosePrintWriter(out, false);
    }

    /**
     * Returns a new {@link PrintWriter} wrapping the given {@link OutputStream}, and whose {@link PrintWriter#close()}
     * method will not close it or the OutputStream, but will instead be equivalent to invoking
     * {@link PrintWriter#flush()}. This can be useful when a PrintWriter is needed as a temporary thin wrapper for an
     * OutputStream.
     *
     * @param out
     *     the {@link OutputStream} to wrap.
     * @param autoFlush
     *     whether the returned {@link PrintWriter}'s {@code #println}, {@code printf}, and {@code format} methods will
     *     flush the output buffer.
     * @return a new {@link PrintWriter} wrapping the given {@link OutputStream}, and whose {@link PrintWriter#close()}
     *     method will not close it or the OutputStream, but will instead be equivalent to invoking
     *     {@link PrintWriter#flush()}.
     * @throws NullPointerException
     *     if the given {@link OutputStream} is null.
     */
    public static PrintWriter getFlushInsteadOfClosePrintWriter(OutputStream out, boolean autoFlush) {
        Objects.requireNonNull(out, "OutputStream");
        return new FlushInsteadOfClosePrintWriter(out, autoFlush);
    }

    /**
     * Returns a new {@link PrintWriter} wrapping the given {@link Writer}, and whose {@link PrintWriter#close()} method
     * will not close it or the Writer, but will instead be equivalent to invoking {@link PrintWriter#flush()}. This can
     * be useful when a PrintWriter is needed as a temporary thin wrapper for another Writer.
     *
     * @param out
     *     the {@link Writer} to wrap.
     * @return a new {@link PrintWriter} wrapping the given {@link OutputStream}, and whose {@link PrintWriter#close()}
     *     method will not close it or the Writer, but will instead be equivalent to invoking
     *     {@link PrintWriter#flush()}.
     * @throws NullPointerException
     *     if the given {@link Writer} is null.
     */
    public static PrintWriter getFlushInsteadOfClosePrintWriter(Writer out) {
        return getFlushInsteadOfClosePrintWriter(out, false);
    }

    /**
     * Returns a new {@link PrintWriter} wrapping the given {@link Writer}, and whose {@link PrintWriter#close()} method
     * will not close it or the Writer, but will instead be equivalent to invoking {@link PrintWriter#flush()}. This can
     * be useful when a PrintWriter is needed as a temporary thin wrapper for another Writer.
     *
     * @param out
     *     the {@link Writer} to wrap.
     * @param autoFlush
     *     whether the returned {@link PrintWriter}'s {@code #println}, {@code printf}, and {@code format} methods will
     *     flush the output buffer.
     * @return a new {@link PrintWriter} wrapping the given {@link OutputStream}, and whose {@link PrintWriter#close()}
     *     method will not close it or the Writer, but will instead be equivalent to invoking
     *     {@link PrintWriter#flush()}.
     * @throws NullPointerException
     *     if the given {@link Writer} is null.
     */
    public static PrintWriter getFlushInsteadOfClosePrintWriter(Writer out, boolean autoFlush) {
        Objects.requireNonNull(out, "Writer");
        return new FlushInsteadOfClosePrintWriter(out, autoFlush);
    }

    public static InputStream getNoCloseInputStream(InputStream in) {
        Objects.requireNonNull(in, "InputStream");
        return CloseShieldInputStream.wrap(in);
    }

    public static OutputStream getNoCloseOutputStream(OutputStream out) {
        Objects.requireNonNull(out, "OutputStream");
        return CloseShieldOutputStream.wrap(out);
    }

    public static Reader getNoCloseReader(Reader reader) {
        Objects.requireNonNull(reader, "Reader");
        return CloseShieldReader.wrap(reader);
    }

    public static Writer getNoCloseWriter(Writer writer) {
        Objects.requireNonNull(writer, "Writer");
        return CloseShieldWriter.wrap(writer);
    }

    /**
     * Exhausts (reads all data) and closes the given object if possible.
     *
     * @param object
     *     an object to try to exhaust and close. If the object is a {@link InputStream} or {@link Writer} object, it
     *     will be handled. Otherwise, this method will do nothing.
     * @throws IOException
     *     if one is raised attempting to exhaust the object.
     */
    public static void exhaustAndClose(Object object) throws IOException {
        if (object instanceof InputStream input) {
            exhaustAndClose(input);
        }
        else if (object instanceof Reader reader) {
            exhaustAndClose(reader);
        }
    }

    /**
     * Exhausts (reads all data) and closes the given {@link InputStream}.
     *
     * @param input
     *     an {@link InputStream}.
     * @throws IOException
     *     if one is raised attempting to exhaust the {@link InputStream}.
     */
    public static void exhaustAndClose(InputStream input) throws IOException {
        if (input != null) {
            try {
                input.transferTo(OutputStream.nullOutputStream());
            }
            finally {
                IOUtils.closeQuietly(input);
            }
        }
    }

    /**
     * Exhausts (reads all data) and closes the given {@link Reader}.
     *
     * @param reader
     *     an {@link Reader}.
     * @throws IOException
     *     if one is raised attempting to exhaust the {@link Reader}.
     */
    public static void exhaustAndClose(Reader reader) throws IOException {
        if (reader != null) {
            try {
                reader.transferTo(Writer.nullWriter());
            }
            finally {
                IOUtils.closeQuietly(reader);
            }
        }
    }

    private static boolean isBufferedInputStream(InputStream input) {
        if (input instanceof BufferedInputStream || input instanceof ByteArrayInputStream) {
            return true;
        }
        if (input instanceof CustomFilterInputStream custom) {
            return custom.isBuffered();
        }
        return false;
    }

    private static boolean isBufferedOutputStream(OutputStream output) {
        if (output instanceof BufferedOutputStream || output instanceof ByteArrayOutputStream) {
            return true;
        }
        if (output instanceof CustomFilterOutputStream custom) {
            return custom.isBuffered();
        }
        return false;
    }

}

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

import com.google.common.collect.Iterators;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.tractionsoftware.commons.lang.Resource;
import com.tractionsoftware.commons.lang.ObjectUtil;
import com.tractionsoftware.commons.text.NumberFormats;
import com.tractionsoftware.commons.util.AccumulatesCount;
import com.tractionsoftware.commons.util.MayHaveKnownSize;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.function.IORunnable;
import org.apache.commons.io.function.IOSupplier;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.io.input.CloseShieldReader;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.commons.io.output.CloseShieldWriter;
import org.apache.commons.lang3.ArrayUtils;
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
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * General I/O related helpers.
 *
 * @author Dave Shepperton, Andy Keller
 */
public final class IOUtil {

    /*
     * Not instantiable.
     */
    private IOUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(IOUtil.class.getName());

    public static final int DEFAULT_IO_BUFFER_SIZE = 10240;

    /**
     * Closes the given {@link AutoCloseable}, gracefully handling null and preventing any Exceptions from propagating.
     *
     * @param closeMe
     *     to be closed.
     */
    public static final void close(@Nullable AutoCloseable closeMe) {
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
    public static final void flush(Flushable flushMe) {
        if (flushMe != null) {
            try {
                flushMe.flush();
            }
            catch (IOException e) {
                LOGGER.warn("Failed to flush {} ", ObjectUtil.safeToString(flushMe, "(Flushable?)"), e);
            }
        }
    }

    public static final class CopyResult {

        public static final long UNCOPIED_BYTES_WRITTEN = Long.MIN_VALUE;

        public static final CopyResult NOT_COPIED = new CopyResult(0, UNCOPIED_BYTES_WRITTEN, false);

        /**
         * Returns a {@link CopyResult} representing a "full copy", in which the requested size limit was not exceeded.
         *
         * @param bytesCopied
         *     the number of bytes actually copied. This should always be the actual number of bytes read by the
         *     transfer operation.
         * @param bytesWritten
         *     the number of bytes actually written, based on the best available information. It is possible that the
         *     {@link OutputStream} is discarding some bytes, particularly if it is size limited, so the accuracy of
         *     this value isn't guaranteed.
         * @return a {@link CopyResult} representing a "full copy", in which the requested size limit was not exceeded.
         */
        @Nonnull
        public static final CopyResult getInstanceForFullCopy(long bytesCopied, long bytesWritten) {
            return new CopyResult(bytesCopied, bytesWritten, false);
        }

        /**
         * Returns a {@link CopyResult} representing a "partial copy", in which the requested size limit would have been
         * exceeded by copying all bytes.
         *
         * @param bytesCopied
         *     the number of bytes actually copied. On a best-efforts basis, this should always be the actual number of
         *     bytes read by the transfer operation.
         * @param bytesWritten
         *     the number of bytes actually written, based on the best available information. It is possible that the
         *     {@link OutputStream} is discarding some bytes, particularly if it is size limited, so the accuracy of
         *     this value isn't guaranteed.
         * @return a {@link CopyResult} representing a "full copy", in which the requested size limit was not exceeded.
         */
        @Nonnull
        public static final CopyResult getInstanceForPartialCopy(long bytesCopied, long bytesWritten) {
            return new CopyResult(bytesCopied, bytesWritten, true);
        }

        @Nonnull
        public static final CopyResult getInstance(long sizeLimit, long bytesCopied, long bytesWritten) {
            if (bytesCopied < sizeLimit) {
                return getInstanceForFullCopy(bytesCopied, bytesWritten);
            }
            return getInstanceForPartialCopy(bytesCopied, bytesWritten);
        }

        private final long bytesRead;

        private final long bytesWritten;

        private final boolean inputWasTooLarge;

        private CopyResult(long bytesRead, long bytesWritten, boolean inputWasTooLarge) {
            if (bytesRead != UNCOPIED_BYTES_WRITTEN && bytesRead < 0) {
                throw new IllegalArgumentException(String.format("bytes read %s < 0", bytesRead));
            }
            if (bytesWritten < 0 && bytesWritten != Long.MIN_VALUE) {
                throw new IllegalArgumentException(String.format("bytes written %s < 0", bytesWritten));
            }
            this.bytesRead = bytesRead;
            this.bytesWritten = bytesWritten;
            this.inputWasTooLarge = inputWasTooLarge;
        }

        @Nonnull
        @Override
        public final String toString() {
            return "[read " +
                   NumberFormats.getFormattedByteSize(bytesRead) +
                   ", wrote " +
                   NumberFormats.getFormattedByteSize(bytesWritten) +
                   "]";
        }

        public final long getBytesRead() {
            return bytesRead;
        }

        public final long getBytesWritten() {
            return bytesWritten;
        }

        public final boolean triedToCopy() {
            if (bytesWritten == UNCOPIED_BYTES_WRITTEN) {
                return false;
            }
            return true;
        }

        public final boolean inputWasTooLarge() {
            return inputWasTooLarge;
        }

    }

    private static abstract class CustomFilterInputStream extends FilterInputStream {

        CustomFilterInputStream(InputStream input) {
            super(input);
        }

        final boolean isBuffered() {
            return IOUtil.isBufferedInputStream(in);
        }

    }

    private static abstract class CustomFilterOutputStream extends FilterOutputStream {

        CustomFilterOutputStream(OutputStream output) {
            super(output);
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

    private static final class CloseNotifyingOutputStream extends CustomFilterOutputStream {

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

    private static final class SizeLimiter {

        private static enum Type {
            INPUT,
            OUTPUT
        }

        private final Type type;

        private final long sizeLimit;

        private final boolean errorOnLimitExceeded;

        private long bytesAttempted;

        public SizeLimiter(Type type, long sizeLimit, boolean errorOnLimitExceeded) {
            this.type = type;
            this.sizeLimit = sizeLimit;
            this.errorOnLimitExceeded = errorOnLimitExceeded;
            this.bytesAttempted = 0;
        }

        static final SizeLimiter createForInput(long sizeLimit, boolean errorOnLimitExceeded) {
            return new SizeLimiter(Type.INPUT, sizeLimit, errorOnLimitExceeded);
        }

        static final SizeLimiter createForOutput(long sizeLimit, boolean errorOnLimitExceeded) {
            return new SizeLimiter(Type.OUTPUT, sizeLimit, errorOnLimitExceeded);
        }

        public final boolean attemptOne() throws IOException {
            if (attemptUpTo(1) == 1) {
                return true;
            }
            return false;
        }

        public final int attemptUpTo(int requestedSize) throws IOException {
            int bytesAvailable = (int) (sizeLimit - bytesAttempted);
            bytesAttempted += requestedSize;
            if (requestedSize > bytesAvailable) {
                onOverLimit();
                return bytesAvailable;
            }
            return requestedSize;
        }

        private final void onOverLimit() throws IOException {
            if (errorOnLimitExceeded) {
                throw switch (type) {
                    case INPUT -> StreamSizeLimitExceededException.forRead(bytesAttempted, sizeLimit);
                    case OUTPUT -> StreamSizeLimitExceededException.forWrite(bytesAttempted, sizeLimit);
                };
            }
        }

    }

    private static final class ByteSizeLimitingInputStream extends CustomFilterInputStream {

        private final SizeLimiter limiter;

        private ByteSizeLimitingInputStream(InputStream in, long sizeLimit, boolean errorOnLimitExceeded) {
            super(in);
            this.limiter = SizeLimiter.createForInput(sizeLimit, errorOnLimitExceeded);
        }

        @Override
        public final int read() throws IOException {
            if (limiter.attemptOne()) {
                return in.read();
            }
            return -1;
        }

        @Override
        public final int read(@Nonnull byte[] b, int off, int len) throws IOException {

            if (len == 0) {
                return 0;
            }

            Objects.checkFromIndexSize(off, len, b.length);

            int available = limiter.attemptUpTo(len);
            if (available > 0) {
                return in.read(b, off, available);
            }
            return -1;

        }

        @Nonnull
        public final byte[] readNBytes(int len) throws IOException {
            if (len < 0) {
                throw new IllegalArgumentException("len < 0");
            }
            int max = limiter.attemptUpTo(len);
            if (max > 0) {
                return in.readNBytes(max);
            }
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }

    }

    private static final class ByteSizeLimitingOutputStream extends CustomFilterOutputStream {

        private final SizeLimiter limiter;

        private ByteSizeLimitingOutputStream(OutputStream out, long sizeLimit, boolean errorOnLimitExceeded) {
            super(out);
            this.limiter = SizeLimiter.createForOutput(sizeLimit, errorOnLimitExceeded);
        }

        @Override
        public final void write(@Nonnull byte[] b, int off, int len) throws IOException {
            if (len == 0) {
                return;
            }
            int available = limiter.attemptUpTo(len);
            if (available > 0) {
                out.write(b, off, available);
            }
        }

        @Override
        public final void write(int b) throws IOException {
            if (limiter.attemptOne()) {
                out.write(b);
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

    private static final class CompoundAutoCloseable implements AutoCloseable {

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

    private static final class FlushInsteadOfClosePrintWriter extends PrintWriter {

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

    private static final class PrintWriterOutputStream extends OutputStream {

        private final PrintWriter out;

        private final CharsetDecoder decoder;

        private final byte[] oneByte = new byte[] { 0 };

        private PrintWriterOutputStream(PrintWriter out) {
            this.out = out;
            this.decoder = StandardCharsets.UTF_8.newDecoder();
        }

        @Override
        public final void write(int b) throws IOException {
            oneByte[0] = (byte) b;
            out.print(decoder.decode(ByteBuffer.wrap(oneByte)));
        }

        @Override
        public final void write(byte[] b, int off, int len) throws IOException {
            Objects.checkFromIndexSize(off, len, b.length);
            out.print(decoder.decode(ByteBuffer.wrap(b, off, len)));
        }

        @Override
        public final void flush() {
            out.flush();
        }

        @Override
        public final void close() {
            out.close();
        }

    }

    @Nonnull
    public static final byte[] readContentBytes(@Nullable InputStream in) throws IOException {
        if (in == null) {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
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
    @Nonnull
    public static final String readContent(@Nullable InputStream in, @Nullable String charsetName) throws IOException {
        Charset charset;
        if (StringUtils.isBlank(charsetName)) {
            charset = StandardCharsets.UTF_8;
        }
        else {
            charset = Charset.forName(charsetName);
        }
        return readContent(in, charset);
    }

    /**
     * Returns the content read from the given stream in the form of a String created using the given Charset.
     *
     * <p>
     * WARNING: This operation reads all data from the given InputStream into a String. It is not advisable to use it
     * for very large inputs.
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
    @Nonnull
    public static final String readContent(@Nullable InputStream in, @Nullable Charset charset) throws IOException {
        if (in == null) {
            return "";
        }
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
    @Nonnull
    public static final String readContent(@Nullable Reader reader) throws IOException {
        if (reader == null) {
            return "";
        }
        StringBuilder buff = new StringBuilder();
        IOUtils.copy(reader, buff);
        return buff.toString();
    }

    public static final void copyText(@Nullable InputStream in, @Nullable Charset charset, @Nullable Writer writer)
        throws IOException {
        if (in != null && writer != null) {
            IOUtils.copy(in, writer, Objects.requireNonNullElse(charset, StandardCharsets.UTF_8));
        }
    }

    @CanIgnoreReturnValue
    public static final long copyText(Reader reader, Writer writer) throws IOException {
        if (reader == null || writer == null) {
            return 0;
        }
        return IOUtils.copy(reader, writer);
    }

    /**
     * Attempts to copy the given input to the given output, returning a {@link CopyResult} representing the result. If
     * the argument for either the {@link InputStream} or {@link OutputStream} is null, this method does nothing and
     * returns {@link CopyResult#NOT_COPIED}. Otherwise, this method will try to take into account how many bytes are
     * actually read and written when creating the CopyResult on a best-efforts basis. Ideally, this would mean an
     * OutputStream that implements {@link AccumulatesCount}. Clients that can't benefit from any of this special
     * handling or the additional information or don't need null-safe conditional copying should probably simply use
     * {@link InputStream#transferTo(OutputStream)}.
     *
     * @param input
     *     the {@link InputStream} to copy from.
     * @param output
     *     the {@link OutputStream} to copy to.
     * @return a {@link CopyResult} representing the result.
     * @throws IOException
     *     if one is raised during the copy operation.
     */
    @Nonnull
    public static final CopyResult copy(@Nullable InputStream input, @Nullable OutputStream output) throws IOException {
        if (input == null || output == null) {
            return CopyResult.NOT_COPIED;
        }
        return copyFull(input, output);
    }

    /**
     * Attempts to copy the given input to the given output, returning a {@link CopyResult} representing the result. If
     * the argument for either the {@link InputStream} or {@link OutputStream} is null, this method does nothing and
     * returns {@link CopyResult#NOT_COPIED}. Otherwise, this method will try to take into account how many bytes are
     * actually read and written when creating the CopyResult. It also will optimize handling of the size limit if
     * possible, so for best results, the InputStream should be an instance of {@link MayHaveKnownSize} (e.g.,
     * {@link ByteSizeLimitingInputStream}) and an OutputStream that implements {@link AccumulatesCount}.
     *
     * @param input
     *     the {@link InputStream} to copy from.
     * @param output
     *     the {@link OutputStream} to copy to.
     * @param sizeLimit
     *     the upper limit on the number of bytes that should be allowed to be copied. If this value is negative or
     *     {@link Long#MAX_VALUE}, or if the input is a {@link MayHaveKnownSize} and
     *     {@link MayHaveKnownSize#size() reports a size} within this limit, no limit will be applied.
     * @return a {@link CopyResult} representing the result.
     * @throws IOException
     *     if one is raised during the copy operation.
     */
    @CanIgnoreReturnValue
    @Nonnull
    public static final CopyResult copy(@Nullable InputStream input, @Nullable OutputStream output, long sizeLimit)
        throws IOException {

        if (input == null || output == null) {
            return CopyResult.NOT_COPIED;
        }

        if (sizeLimit < 0 || sizeLimit == Long.MAX_VALUE) {
            return copyFull(input, output);
        }

        if (input instanceof MayHaveKnownSize sized) {
            if (sized.hasSizeAtMost(sizeLimit)) {
                return copyFull(input, output);
            }
            return copyPartial(input, output, sizeLimit);
        }

        return copyLimited(input, output, sizeLimit);

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
     * @throws NullPointerException
     *     if the argument for the {@link InputStream} is null.
     */
    @Nonnull
    public static final InputStream getCloseNotifyingInputStream(@Nonnull InputStream input, @Nullable IORunnable onBeforeClose, @Nullable IORunnable onAfterClose, @Nullable String sourceIdentifier) {
        return new CloseNotifyingInputStream(input, onBeforeClose, onAfterClose, sourceIdentifier);
    }

    /**
     * Returns an {@link InputStream} wrapping the given stream which will only permit the given maximum number of bytes
     * to be read from the stream. Exceeding the limit can optionally cause an IOException to be thrown.
     *
     * <p>
     * This method has intelligent special casing for {@link SizedInputStream}, in that if the given InputStream is a
     * SizedInputStream, and {@link SizedInputStream#size() its size} is under the requested limit, the InputStream is
     * returned as-is.
     *
     * @param input
     *     the {@link InputStream} to be wrapped.
     * @param sizeLimit
     *     the upper limit on the number of bytes that should be allowed to be read from the stream. If this value is
     *     negative or {@link Long#MAX_VALUE}, no limit will be applied.
     * @param errorOnLimitExceeded
     *     indicates whether exceeding the limit should cause an {@link IOException} to be thrown.
     * @return an {@link InputStream} which will only permit the given maximum number of bytes to be read from the given
     *     stream.
     * @throws NullPointerException
     *     if the given {@link InputStream} is null.
     */
    @Nonnull
    public static final InputStream getSizeLimitingInputStream(@Nonnull InputStream input, long sizeLimit, boolean errorOnLimitExceeded) {
        Objects.requireNonNull(input, "input");
        if (sizeLimit < 0 || sizeLimit == Long.MAX_VALUE) {
            return input;
        }
        if (!errorOnLimitExceeded &&
            input instanceof MayHaveKnownSize sized &&
            sized.hasSizeBetween(1, sizeLimit)) {
            return input;
        }
        return new ByteSizeLimitingInputStream(input, sizeLimit, errorOnLimitExceeded);
    }

    /**
     * Returns an {@link OutputStream} wrapping the given stream which will only permit the given maximum number of
     * bytes to be written to the stream. Exceeding the limit can optionally cause an {@link IOException} to be thrown.
     *
     * @param output
     *     the {@link OutputStream} to be wrapped.
     * @param sizeLimit
     *     the upper limit on the number of bytes that should be allowed to be written to the stream. If this value is
     *     negative or {@link Integer#MAX_VALUE}, no limit will be applied.
     * @param errorOnLimitExceeded
     *     indicates whether exceeding the limit should cause an {@link IOException} to be thrown.
     * @return an {@link OutputStream} which will only permit the given maximum number of bytes to be written to the
     *     stream.
     * @throws NullPointerException
     *     if the given {@link OutputStream} is null.
     */
    @Nonnull
    public static final OutputStream getSizeLimitingOutputStream(@Nonnull OutputStream output, long sizeLimit, boolean errorOnLimitExceeded) {
        Objects.requireNonNull(output, "output");
        if (sizeLimit < 0 || sizeLimit == Long.MAX_VALUE) {
            return output;
        }
        return new ByteSizeLimitingOutputStream(output, sizeLimit, errorOnLimitExceeded);
    }

    /**
     * Returns a version of the {@link InputStream} that is known to be buffered. This is intended to be used when
     * performance requires buffering, and therefore <b>is not necessarily the same as creating a
     * {@link BufferedInputStream}</b>. If the given InputStream is already a BufferedInputStream, or a
     * {@link ByteArrayInputStream}, or some other type of InputStream from this library or elsewhere that is known to
     * already be buffered, then the InputStream will be returned as-is. Otherwise, it will return a new
     * BufferedInputStream wrapping the given InputStream.
     *
     * @param input
     *     the InputStream to buffer.
     * @return a version of the {@link InputStream} that is known to be buffered
     * @throws NullPointerException
     *     if the argument for the {@link InputStream} is null.
     */
    @Nonnull
    public static final InputStream getBufferedInputStream(@Nonnull InputStream input) {
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
     * @throws NullPointerException
     *     if either of the arguments is null.
     */
    public static final InputStream getTrackedInputStream(@Nonnull InputStream input, @Nonnull Supplier<? extends Resource> getTracker) {
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
     * @throws NullPointerException
     *     if the argument for the {@link InputStream} or {@link Resource} {@link Supplier} is null.
     * @see #getCloseNotifyingInputStream(InputStream, IORunnable, IORunnable)
     */
    public static final InputStream getTrackedInputStream(@Nonnull InputStream input, @Nonnull Supplier<? extends Resource> getTracker, @Nullable String sourceIdentifier) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(getTracker, "tracker provider");
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
     * @throws NullPointerException
     *     if either of the arguments is null.
     * @see FileUtil#getBufferedInputStream(File)
     * @see IOUtil#getTrackedInputStream(InputStream, Supplier, String)
     */
    public static final InputStream getBufferedTrackedInputStream(@Nonnull File file, @Nonnull Supplier<? extends Resource> getTracker)
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
     * @throws NullPointerException
     *     if the argument for the {@link InputStream} is null.
     */
    public static final InputStream getCloseNotifyingInputStream(@Nonnull InputStream input, @Nullable IORunnable onBeforeClose, @Nullable IORunnable onAfterClose) {
        return getCloseNotifyingInputStream(input, onBeforeClose, onAfterClose, null);
    }

    /**
     *
     * @param output
     *     the {@link OutputStream} to be tracked.
     * @return a wrapped version of the given {@link OutputStream}.
     * @throws NullPointerException
     *     if either of the arguments is null.
     */
    public static final OutputStream getTrackedOutputStream(@Nonnull OutputStream output, @Nonnull Supplier<? extends Resource> getTracker) {
        return getTrackedOutputStream(output, getTracker, null);
    }

    /**
     *
     * @param output
     *     the {@link OutputStream} to be tracked.
     * @param sourceIdentifier
     *     a simple identifier for the stream source to appear in diagnostic logging as necessary.
     * @return a wrapped version of the given {@link OutputStream}.
     * @throws NullPointerException
     *     if the argument for the {@link OutputStream} or {@link Resource} {@link Supplier} is null.
     */
    public static final OutputStream getTrackedOutputStream(@Nonnull OutputStream output, @Nonnull Supplier<? extends Resource> getTracker, @Nullable String sourceIdentifier) {
        Objects.requireNonNull(getTracker, "resource tracker provider");
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
    public static final OutputStream getCloseNotifyingOutputStream(OutputStream output, IORunnable onBeforeClose, IORunnable onAfterClose) {
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
    public static final OutputStream getCloseNotifyingOutputStream(OutputStream output, IORunnable onBeforeClose, IORunnable onAfterClose, String sourceIdentifier) {
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
    public static final InputStream getSequenceInputStream(final InputStream... streams) {
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
    public static final <T> T runIOSupplier(IOSupplier<T> supplier) {
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
     * @throws NullPointerException
     *     if either argument is null.
     */
    public static final <T> T runIOSupplierSafe(@Nonnull IOSupplier<T> supplier, @Nonnull Supplier<? extends T> defaultValue) {
        try {
            return supplier.get();
        }
        catch (IOException e) {
            LOGGER.warn("{} failed", ObjectUtil.safeToStringObject(supplier), e);
        }
        return defaultValue.get();
    }

    /**
     * Returns a {@link Supplier} that will return a {@link ByteArrayInputStream} for the given data.
     *
     * @param data
     *     the data.
     * @return a {@link Supplier} that will return a {@link ByteArrayInputStream} for the given data.
     * @throws NullPointerException
     *     if data argument is null.
     */
    public static final Supplier<ByteArrayInputStream> byteArrayInputStreamSupplier(byte[] data) {
        Objects.requireNonNull(data, "bytes");
        return () -> new ByteArrayInputStream(data);
    }

    public static final OutputStream getPrintWriterOutputStream(PrintWriter out, boolean allowClose) {
        OutputStream stream = new PrintWriterOutputStream(out);
        if (allowClose) {
            return stream;
        }
        return getNoCloseOutputStream(stream);
    }

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

    public static final AutoCloseable createCompoundCloseable(Iterable<? extends AutoCloseable> resources) {
        Objects.requireNonNull(resources, "resources");
        return new CompoundAutoCloseable(resources);
    }

    public static final BufferedReader getBufferedUtf8Reader(InputStream input) {
        return getBufferedReader(input, null);
    }

    public static final BufferedReader getBufferedReader(InputStream input, Charset charset) {
        Objects.requireNonNull(input, "InputStream");
        return new BufferedReader(
            new InputStreamReader(input, charset == null ? StandardCharsets.UTF_8 : charset)
        );
    }

    public static final BufferedReader getBufferedReader(Reader reader) {
        if (reader instanceof BufferedReader alreadyBuffered) {
            return alreadyBuffered;
        }
        return new BufferedReader(reader);
    }

    public static final InputStream getStringAsUtf8InputStream(String str) {
        return getStringAsInputStream(str, StandardCharsets.UTF_8);
    }

    public static final InputStream getStringAsInputStream(String str, Charset charset) {
        Objects.requireNonNull(str, "string");
        Objects.requireNonNull(charset, "charset");
        try {
            return ReaderInputStream.builder().setReader(new StringReader(str)).setCharset(charset).get();
        }
        catch (IOException e) {
            throw new IllegalStateException("This IOException should not be able to happen.", e);
        }
    }

    public static final OutputStream getBufferedOutputStream(OutputStream output) {
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
    public static final BufferedWriter getBufferedUtf8Writer(OutputStream out) {
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
    public static final PrintWriter getFlushInsteadOfClosePrintWriter(OutputStream out) {
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
    public static final PrintWriter getFlushInsteadOfClosePrintWriter(OutputStream out, boolean autoFlush) {
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
    public static final PrintWriter getFlushInsteadOfClosePrintWriter(Writer out) {
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
    public static final PrintWriter getFlushInsteadOfClosePrintWriter(Writer out, boolean autoFlush) {
        Objects.requireNonNull(out, "Writer");
        return new FlushInsteadOfClosePrintWriter(out, autoFlush);
    }

    public static final InputStream getNoCloseInputStream(InputStream in) {
        Objects.requireNonNull(in, "InputStream");
        return CloseShieldInputStream.wrap(in);
    }

    public static final OutputStream getNoCloseOutputStream(OutputStream out) {
        Objects.requireNonNull(out, "OutputStream");
        return CloseShieldOutputStream.wrap(out);
    }

    public static final Reader getNoCloseReader(Reader reader) {
        Objects.requireNonNull(reader, "Reader");
        return CloseShieldReader.wrap(reader);
    }

    public static final Writer getNoCloseWriter(Writer writer) {
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

    private static final boolean isBufferedInputStream(InputStream input) {
        if (input instanceof BufferedInputStream || input instanceof ByteArrayInputStream) {
            return true;
        }
        if (input instanceof CustomFilterInputStream custom) {
            return custom.isBuffered();
        }
        return false;
    }

    private static final boolean isBufferedOutputStream(OutputStream output) {
        if (output instanceof BufferedOutputStream || output instanceof ByteArrayOutputStream) {
            return true;
        }
        if (output instanceof CustomFilterOutputStream custom) {
            return custom.isBuffered();
        }
        return false;
    }

    /**
     * Copy implementation to use when all bytes can be copied.
     *
     * @param input
     *     the source {@link InputStream}.
     * @param output
     *     the destination {@link OutputStream}.
     * @return a {@link CopyResult} representing the result.
     * @throws IOException
     *     if one is raised during the copy operation.
     */
    private static final CopyResult copyFull(@Nonnull InputStream input, @Nonnull OutputStream output)
        throws IOException {
        return copyImpl(input, output, CopyResult::getInstanceForFullCopy);
    }

    /**
     * Copy implementation to use when it is known that not all bytes will be copied, but the input does not need to be
     * limited.
     *
     * @param input
     *     the source {@link InputStream}.
     * @param output
     *     the destination {@link OutputStream}.
     * @return a {@link CopyResult} representing the result.
     * @throws IOException
     *     if one is raised during the copy operation.
     */
    private static final CopyResult copyPartial(@Nonnull InputStream input, @Nonnull OutputStream output, long sizeLimit)
        throws IOException {
//        return copyImpl(
//            new ByteSizeLimitingInputStream(input, sizeLimit, false),
//            output,
//            (bytesCopied, bytesWritten) -> CopyResult.getInstance(sizeLimit, bytesCopied, bytesWritten)
//        );
        return copyImpl(
            new ByteSizeLimitingInputStream(input, sizeLimit, false), output, CopyResult::getInstanceForPartialCopy
        );
    }

    /**
     * Copy implementation to use when it is not known whether all bytes can be copied and still stay within the
     * requested size limit.
     *
     * @param input
     *     the source {@link InputStream}.
     * @param output
     *     the destination {@link OutputStream}.
     * @return a {@link CopyResult} representing the result.
     * @throws IOException
     *     if one is raised during the copy operation.
     */
    private static final CopyResult copyLimited(@Nonnull InputStream input, @Nonnull OutputStream output, long sizeLimit)
        throws IOException {
        return copyImpl(
            new ByteSizeLimitingInputStream(input, sizeLimit, false),
            output,
            (bytesCopied, bytesWritten) -> CopyResult.getInstance(sizeLimit, bytesCopied, bytesWritten)
        );
    }

    /**
     * Shared copy implementation.
     *
     * @param input
     *     the source {@link InputStream}.
     * @param output
     *     the destination {@link OutputStream}.
     * @param resultCreator
     *     to be invoked to create a {@link CopyResult}.
     * @return a {@link CopyResult} representing the result.
     * @throws IOException
     *     if one is raised during the copy operation.
     */
    private static final CopyResult copyImpl(@Nonnull InputStream input, @Nonnull OutputStream output, @Nonnull BiFunction<Long,Long,CopyResult> resultCreator)
        throws IOException {

        AccumulatesCount bytesWritten;
        if (output instanceof AccumulatesCount alreadyCounting) {
            bytesWritten = alreadyCounting.startingFromCurrentCount();
        }
        else {
            bytesWritten = null;
        }

        long copiedBytes = input.transferTo(output);
        if (bytesWritten == null) {
            return resultCreator.apply(copiedBytes, copiedBytes);
        }
        return resultCreator.apply(copiedBytes, bytesWritten.getCount());

    }

}

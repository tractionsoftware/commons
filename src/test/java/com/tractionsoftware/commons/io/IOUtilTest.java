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

import org.apache.commons.io.function.IORunnable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class IOUtilTest {

    static final class CapturingIOTask implements IORunnable {

        public boolean ran = false;

        @Override
        public final void run() {
            this.ran = true;
        }

    }

    // ---------------------------------------------------------------------------
    // close
    // ---------------------------------------------------------------------------

    @Test
    void close_null_doesNotThrow() {
        assertDoesNotThrow(() -> IOUtil.close(null));
    }

    @Test
    void close_closesResource() {
        AtomicBoolean closed = new AtomicBoolean(false);
        AutoCloseable resource = () -> closed.set(true);
        IOUtil.close(resource);
        assertTrue(closed.get());
    }

    @Test
    void close_exceptionDuringClose_doesNotPropagate() {
        AutoCloseable resource = () -> {
            throw new Exception("boom");
        };
        assertDoesNotThrow(() -> IOUtil.close(resource));
    }

    // ---------------------------------------------------------------------------
    // flush
    // ---------------------------------------------------------------------------

    @Test
    void flush_null_doesNotThrow() {
        assertDoesNotThrow(() -> IOUtil.flush(null));
    }

    @Test
    void flush_flushesResource() {
        AtomicBoolean flushed = new AtomicBoolean(false);
        Flushable resource = () -> flushed.set(true);
        IOUtil.flush(resource);
        assertTrue(flushed.get());
    }

    @Test
    void flush_exceptionDuringFlush_doesNotPropagate() {
        Flushable resource = () -> {
            throw new IOException("flush fail");
        };
        assertDoesNotThrow(() -> IOUtil.flush(resource));
    }

    // ---------------------------------------------------------------------------
    // readContentBytes
    // ---------------------------------------------------------------------------

    @Test
    void readContentBytes_empty_returnsEmptyArray() throws IOException {
        byte[] result = IOUtil.readContentBytes(new ByteArrayInputStream(new byte[0]));
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void readContentBytes_smallInput_returnsAllBytes() throws IOException {
        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
        byte[] result = IOUtil.readContentBytes(new ByteArrayInputStream(data));
        assertArrayEquals(data, result);
    }

    // ---------------------------------------------------------------------------
    // readContent(InputStream, Charset)
    // ---------------------------------------------------------------------------

    @Test
    void readContent_inputStreamWithCharset_returnsString() throws IOException {
        byte[] data = "hello world".getBytes(StandardCharsets.UTF_8);
        String result = IOUtil.readContent(new ByteArrayInputStream(data), StandardCharsets.UTF_8);
        assertEquals("hello world", result);
    }

    @Test
    void readContent_inputStreamWithCharsetName_returnsString() throws IOException {
        byte[] data = "hello world".getBytes(StandardCharsets.UTF_8);
        String result = IOUtil.readContent(new ByteArrayInputStream(data), "UTF-8");
        assertEquals("hello world", result);
    }

    // ---------------------------------------------------------------------------
    // readContent(Reader)
    // ---------------------------------------------------------------------------

    @Test
    void readContent_reader_returnsString() throws IOException {
        String result = IOUtil.readContent(new StringReader("hello reader"));
        assertEquals("hello reader", result);
    }

    @Test
    void readContent_emptyReader_returnsEmpty() throws IOException {
        String result = IOUtil.readContent(new StringReader(""));
        assertEquals("", result);
    }

    // ---------------------------------------------------------------------------
    // copyToEOF (no limit)
    // ---------------------------------------------------------------------------

    @Test
    void copyToEOF_nullInput_triedToCopy() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtil.CopyResult result = IOUtil.copyToEOF(null, out);
        assertTrue(result.triedToCopy());
        assertFalse(result.inputWasTooLarge());
        assertEquals(0, result.getBytesRead());
    }

    @Test
    void copyToEOF_nullOutput_triedToCopy() throws IOException {
        IOUtil.CopyResult result = IOUtil.copyToEOF(new ByteArrayInputStream(new byte[] { 1, 2 }), null);
        assertTrue(result.triedToCopy());
    }

    @Test
    void copyToEOF_copiesAllBytes() throws IOException {
        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtil.CopyResult result = IOUtil.copyToEOF(new ByteArrayInputStream(data), out);
        assertFalse(result.triedToCopy());
        assertFalse(result.inputWasTooLarge());
        assertEquals(data.length, result.getBytesRead());
        assertEquals(data.length, result.getBytesWritten());
        assertArrayEquals(data, out.toByteArray());
    }

    // ---------------------------------------------------------------------------
    // copyToEOF (with maxBytes limit)
    // ---------------------------------------------------------------------------

    @Test
    void copyToEOF_withLimit_nullInput_triedToCopy() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtil.CopyResult result = IOUtil.copyToEOF(null, out, 100);
        assertTrue(result.triedToCopy());
    }

    @Test
    void copyToEOF_withLimit_underLimit_copiesAll() throws IOException {
        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtil.CopyResult result = IOUtil.copyToEOF(new ByteArrayInputStream(data), out, 100);
        assertFalse(result.inputWasTooLarge());
        assertEquals(data.length, result.getBytesRead());
        assertEquals(data.length, result.getBytesWritten());
        assertArrayEquals(data, out.toByteArray());
    }

    @Test
    void copyToEOF_withLimit_overLimit_inputWasTooLarge() throws IOException {
        byte[] data = "hello world".getBytes(StandardCharsets.UTF_8); // 11 bytes
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtil.CopyResult result = IOUtil.copyToEOF(new ByteArrayInputStream(data), out, 5);
        assertTrue(result.inputWasTooLarge());
        // All bytes were read, but only 5 were written
        assertEquals(data.length, result.getBytesRead());
        assertEquals(5, out.toByteArray().length);
    }

    @Test
    void copyToEOF_withNegativeLimit_treatsAsUnlimited() throws IOException {
        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtil.CopyResult result = IOUtil.copyToEOF(new ByteArrayInputStream(data), out, -1);
        assertFalse(result.inputWasTooLarge());
        assertEquals(data.length, result.getBytesWritten());
    }

    // ---------------------------------------------------------------------------
    // CopyResult semantics
    // ---------------------------------------------------------------------------

    @Test
    void copyResult_normalCopy_notTooLarge_notTriedToCopy() throws IOException {
        byte[] data = { 1, 2, 3 };
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtil.CopyResult result = IOUtil.copyToEOF(new ByteArrayInputStream(data), out);
        assertFalse(result.triedToCopy());
        assertFalse(result.inputWasTooLarge());
        assertEquals(3, result.getBytesRead());
        assertEquals(3, result.getBytesWritten());
    }

    // ---------------------------------------------------------------------------
    // getSizeLimitingOutputStream
    // ---------------------------------------------------------------------------

    @Test
    void getSizeLimitingOutputStream_negativeLimit_returnsOriginal() {
        ByteArrayOutputStream underlying = new ByteArrayOutputStream();
        OutputStream result = IOUtil.getSizeLimitingOutputStream(underlying, -1, false);
        assertSame(underlying, result);
    }

    @Test
    void getSizeLimitingOutputStream_maxValueLimit_returnsOriginal() {
        ByteArrayOutputStream underlying = new ByteArrayOutputStream();
        OutputStream result = IOUtil.getSizeLimitingOutputStream(underlying, Long.MAX_VALUE, false);
        assertSame(underlying, result);
    }

    @Test
    void getSizeLimitingOutputStream_underLimit_writesSuccessfully() throws IOException {
        ByteArrayOutputStream underlying = new ByteArrayOutputStream();
        OutputStream limited = IOUtil.getSizeLimitingOutputStream(underlying, 10, false);
        limited.write("hello".getBytes(StandardCharsets.UTF_8));
        limited.flush();
        assertEquals("hello", underlying.toString(StandardCharsets.UTF_8));
    }

    @Test
    void getSizeLimitingOutputStream_overLimit_writeMultiBytesThrows() {
        ByteArrayOutputStream underlying = new ByteArrayOutputStream();
        OutputStream limited = IOUtil.getSizeLimitingOutputStream(underlying, 3, true);
        assertThrows(
            StreamSizeLimitExceededException.class,
            () -> limited.write("hello".getBytes(StandardCharsets.UTF_8))
        );
    }

    @Test
    void getSizeLimitingOutputStream_overLimit_writeOneByteThrows() {
        ByteArrayOutputStream underlying = new ByteArrayOutputStream();
        OutputStream limited = IOUtil.getSizeLimitingOutputStream(underlying, 100, true);
        byte[] data = new byte[100];
        Arrays.fill(data, (byte) 0x20);
        assertDoesNotThrow(() -> limited.write(data));
        assertThrows(StreamSizeLimitExceededException.class, () -> limited.write(0x55));
    }

    // ---------------------------------------------------------------------------
    // getCloseNotifyingInputStream
    // ---------------------------------------------------------------------------

    @Test
    void getCloseNotifyingInputStream_callsOnBeforeAndAfterClose() throws IOException {
        AtomicBoolean before = new AtomicBoolean();
        AtomicBoolean after = new AtomicBoolean();
        InputStream base = new ByteArrayInputStream("data".getBytes());
        InputStream notifying = IOUtil.getCloseNotifyingInputStream(
            base,
            () -> before.set(true),
            () -> after.set(true)
        );
        assertFalse(before.get());
        notifying.close();
        assertTrue(before.get());
        assertTrue(after.get());
    }

    // ---------------------------------------------------------------------------
    // getCloseNotifyingOutputStream
    // ---------------------------------------------------------------------------

    @Test
    void getCloseNotifyingOutputStream_callsOnBeforeAndAfterClose() throws IOException {
        AtomicBoolean before = new AtomicBoolean();
        AtomicBoolean after = new AtomicBoolean();
        ByteArrayOutputStream base = new ByteArrayOutputStream();
        OutputStream notifying = IOUtil.getCloseNotifyingOutputStream(
            base,
            () -> before.set(true),
            () -> after.set(true)
        );
        assertFalse(before.get());
        notifying.close();
        assertTrue(before.get());
        assertTrue(after.get());
    }

    // ---------------------------------------------------------------------------
    // getSequenceInputStream
    // ---------------------------------------------------------------------------

    @Test
    void getSequenceInputStream_readsAcrossStreams() throws IOException {
        InputStream a = new ByteArrayInputStream("hello ".getBytes(StandardCharsets.UTF_8));
        InputStream b = new ByteArrayInputStream("world".getBytes(StandardCharsets.UTF_8));
        InputStream seq = IOUtil.getSequenceInputStream(a, b);
        byte[] buf = seq.readAllBytes();
        assertEquals("hello world", new String(buf, StandardCharsets.UTF_8));
    }

    @Test
    void getSequenceInputStream_noStreams_returnsEmpty() throws IOException {
        InputStream seq = IOUtil.getSequenceInputStream();
        assertEquals(0, seq.readAllBytes().length);
    }

    // ---------------------------------------------------------------------------
    // runIOOperation / runIOOperationSafe
    // ---------------------------------------------------------------------------

    @Test
    void runIOOperation_success_runs() {
        AtomicBoolean ran = new AtomicBoolean();
        IOUtil.runIOOperation(() -> ran.set(true));
        assertTrue(ran.get());
    }

    @Test
    void runIOOperation_ioException_wrapsInUnchecked() {
        assertThrows(
            UncheckedIOException.class,
            () -> IOUtil.runIOOperation(() -> {
                throw new IOException("fail");
            })
        );
    }

    @Test
    void runIOOperationSafe_ioException_doesNotThrow() {
        assertDoesNotThrow(() ->
                               IOUtil.runIOOperationSafe(() -> {
                                   throw new IOException("safe fail");
                               }));
    }

    // ---------------------------------------------------------------------------
    // runIOSupplier / runIOSupplierSafe
    // ---------------------------------------------------------------------------

    @Test
    void runIOSupplier_success_returnsValue() {
        String result = IOUtil.runIOSupplier(() -> "hello");
        assertEquals("hello", result);
    }

    @Test
    void runIOSupplier_ioException_wrapsInUnchecked() {
        assertThrows(
            UncheckedIOException.class,
            () -> IOUtil.runIOSupplier(() -> {
                throw new IOException("fail");
            })
        );
    }

    @Test
    void runIOSupplierSafe_ioException_returnsDefault() {
        String result = IOUtil.runIOSupplierSafe(
            () -> {
                throw new IOException("fail");
            },
            () -> "default"
        );
        assertEquals("default", result);
    }

    @Test
    void runIOSupplierSafe_success_returnsValue() {
        String result = IOUtil.runIOSupplierSafe(() -> "ok", () -> "default");
        assertEquals("ok", result);
    }

    // ---------------------------------------------------------------------------
    // byteArrayInputStreamSupplier
    // ---------------------------------------------------------------------------

    @Test
    void byteArrayInputStreamSupplier_producesStreams() {
        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
        var supplier = IOUtil.byteArrayInputStreamSupplier(data);
        // Each invocation returns a fresh stream
        assertEquals("hello", new String(supplier.get().readAllBytes(), StandardCharsets.UTF_8));
        assertEquals("hello", new String(supplier.get().readAllBytes(), StandardCharsets.UTF_8));
    }

    @Test
    void byteArrayInputStreamSupplier_nullData_throwsNPE() {
        assertThrows(NullPointerException.class, () -> IOUtil.byteArrayInputStreamSupplier(null));
    }

    // ---------------------------------------------------------------------------
    // getPrintWriterOutputStream
    // ---------------------------------------------------------------------------

    @Test
    void getPrintWriterOutputStream_writesThrough() throws IOException {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        OutputStream os = IOUtil.getPrintWriterOutputStream(pw, false);
        os.write("test".getBytes(StandardCharsets.UTF_8));
        os.flush();
        assertEquals("test", sw.toString());
    }

    // ---------------------------------------------------------------------------
    // createCompoundCloseable
    // ---------------------------------------------------------------------------

    @Test
    void createCompoundCloseable_closesAll() throws Exception {
        AtomicInteger closedCount = new AtomicInteger(0);
        AutoCloseable a = closedCount::incrementAndGet;
        AutoCloseable b = closedCount::incrementAndGet;
        AutoCloseable compound = IOUtil.createCompoundCloseable(List.of(a, b));
        compound.close();
        assertEquals(2, closedCount.get());
    }

    @SuppressWarnings("resource")
    @Test
    void createCompoundCloseable_nullResources_throwsNPE() {
        assertThrows(NullPointerException.class, () -> IOUtil.createCompoundCloseable(null));
    }

    // ---------------------------------------------------------------------------
    // getStringAsUtf8InputStream / getStringAsInputStream
    // ---------------------------------------------------------------------------

    @Test
    void getStringAsUtf8InputStream_roundTrips() throws IOException {
        InputStream is = IOUtil.getStringAsUtf8InputStream("hello");
        assertEquals("hello", new String(is.readAllBytes(), StandardCharsets.UTF_8));
    }

    @Test
    void getStringAsInputStream_withCharset_roundTrips() throws IOException {
        InputStream is = IOUtil.getStringAsInputStream("hello", StandardCharsets.UTF_8);
        assertEquals("hello", new String(is.readAllBytes(), StandardCharsets.UTF_8));
    }

    // ---------------------------------------------------------------------------
    // getFlushInsteadOfClosePrintWriter (OutputStream)
    // ---------------------------------------------------------------------------

    @Test
    void getFlushInsteadOfClosePrintWriter_outputStream_closeDoesNotCloseUnderlying() throws IOException {
        ByteArrayOutputStream underlying = new ByteArrayOutputStream();
        PrintWriter pw = IOUtil.getFlushInsteadOfClosePrintWriter(underlying);
        pw.print("hello");
        pw.close(); // should flush, not close
        // Should still be able to write to underlying after pw.close()
        underlying.write("!".getBytes(StandardCharsets.UTF_8));
        assertTrue(underlying.toString(StandardCharsets.UTF_8).contains("hello"));
    }

    @Test
    void getFlushInsteadOfClosePrintWriter_outputStream_nullThrowsNPE() {
        assertThrows(
            NullPointerException.class,
            () -> IOUtil.getFlushInsteadOfClosePrintWriter((OutputStream) null)
        );
    }

    // ---------------------------------------------------------------------------
    // getFlushInsteadOfClosePrintWriter (Writer)
    // ---------------------------------------------------------------------------

    @Test
    void getFlushInsteadOfClosePrintWriter_writer_closeDoesNotCloseUnderlying() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = IOUtil.getFlushInsteadOfClosePrintWriter(sw);
        pw.print("world");
        pw.close(); // should flush, not close
        sw.write("!");
        assertTrue(sw.toString().contains("world"));
    }

    @Test
    void getFlushInsteadOfClosePrintWriter_writer_nullThrowsNPE() {
        assertThrows(
            NullPointerException.class,
            () -> IOUtil.getFlushInsteadOfClosePrintWriter((Writer) null)
        );
    }

    // ---------------------------------------------------------------------------
    // getNoCloseInputStream / OutputStream / Reader / Writer
    // ---------------------------------------------------------------------------

    @Test
    void getNoCloseInputStream_closeDoesNotCloseUnderlying() throws IOException {
        AtomicBoolean closed = new AtomicBoolean();
        InputStream base = new ByteArrayInputStream("data".getBytes()) {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        };
        InputStream noClose = IOUtil.getNoCloseInputStream(base);
        noClose.close();
        assertFalse(closed.get());
    }

    @Test
    void getNoCloseInputStream_nullThrowsNPE() {
        assertThrows(NullPointerException.class, () -> IOUtil.getNoCloseInputStream(null));
    }

    @Test
    void getNoCloseOutputStream_closeDoesNotCloseUnderlying() throws IOException {
        AtomicBoolean closed = new AtomicBoolean();
        ByteArrayOutputStream base = new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        };
        OutputStream noClose = IOUtil.getNoCloseOutputStream(base);
        noClose.close();
        assertFalse(closed.get());
    }

    @Test
    void getNoCloseReader_closeDoesNotCloseUnderlying() throws IOException {
        AtomicBoolean closed = new AtomicBoolean();
        Reader base = new StringReader("hello") {
            @Override
            public void close() {
                closed.set(true);
                super.close();
            }
        };
        Reader noClose = IOUtil.getNoCloseReader(base);
        noClose.close();
        assertFalse(closed.get());
    }

    @Test
    void getNoCloseWriter_closeDoesNotCloseUnderlying() throws IOException {
        AtomicBoolean closed = new AtomicBoolean();
        Writer base = new StringWriter() {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        };
        Writer noClose = IOUtil.getNoCloseWriter(base);
        noClose.close();
        assertFalse(closed.get());
    }

    // ---------------------------------------------------------------------------
    // exhaustAndClose(InputStream)
    // ---------------------------------------------------------------------------

    @Test
    void exhaustAndClose_inputStream_readsAndCloses() throws IOException {
        AtomicBoolean closed = new AtomicBoolean();
        InputStream is = new ByteArrayInputStream("data".getBytes()) {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        };
        IOUtil.exhaustAndClose(is);
        assertTrue(closed.get());
    }

    @Test
    void exhaustAndClose_nullInputStream_doesNotThrow() {
        assertDoesNotThrow(() -> IOUtil.exhaustAndClose((InputStream) null));
    }

    // ---------------------------------------------------------------------------
    // exhaustAndClose(Reader)
    // ---------------------------------------------------------------------------

    @Test
    void exhaustAndClose_reader_readsAndCloses() throws IOException {
        AtomicBoolean closed = new AtomicBoolean();
        Reader reader = new StringReader("data") {
            @Override
            public void close() {
                closed.set(true);
                super.close();
            }
        };
        IOUtil.exhaustAndClose(reader);
        assertTrue(closed.get());
    }

    @Test
    void exhaustAndClose_nullReader_doesNotThrow() {
        assertDoesNotThrow(() -> IOUtil.exhaustAndClose((Reader) null));
    }

    // ---------------------------------------------------------------------------
    // exhaustAndClose(Object)
    // ---------------------------------------------------------------------------

    @Test
    void exhaustAndClose_object_inputStream_exhaustsAndCloses() throws IOException {
        AtomicBoolean closed = new AtomicBoolean();
        Object obj = new ByteArrayInputStream("hello".getBytes()) {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        };
        IOUtil.exhaustAndClose(obj);
        assertTrue(closed.get());
    }

    @Test
    void exhaustAndClose_object_notInputStreamOrReader_doesNothing() {
        assertDoesNotThrow(() -> IOUtil.exhaustAndClose("not a stream"));
    }

    // ---------------------------------------------------------------------------
    // copyContent(InputStream, Charset, Writer)
    // ---------------------------------------------------------------------------

    @Test
    void copyContent_inputStreamCharsetWriter_copiesContent() throws IOException {
        byte[] data = "hello writer".getBytes(StandardCharsets.UTF_8);
        StringWriter sw = new StringWriter();
        IOUtil.copyContent(new ByteArrayInputStream(data), StandardCharsets.UTF_8, sw);
        assertEquals("hello writer", sw.toString());
    }

    // ---------------------------------------------------------------------------
    // copyContent(Reader, Writer)
    // ---------------------------------------------------------------------------

    @Test
    void copyContent_readerWriter_copiesContent() throws IOException {
        StringWriter sw = new StringWriter();
        long bytes = IOUtil.copyContent(new StringReader("from reader"), sw);
        assertEquals("from reader", sw.toString());
        assertEquals("from reader".length(), bytes);
    }

    // ---------------------------------------------------------------------------
    // getBufferedInputStream
    // ---------------------------------------------------------------------------

    @Test
    void getBufferedInputStream_nonBuffered_wraps() {
        InputStream base = new ByteArrayInputStream(new byte[0]);
        InputStream buffered = IOUtil.getBufferedInputStream(base);
        // ByteArrayInputStream is treated as already "buffered" by isBufferedInputStream,
        // so it is returned as-is rather than wrapped.
        assertSame(base, buffered);
    }

    @Test
    void getBufferedInputStream_plainFilterStream_wraps() {
        InputStream base = new FilterInputStream(new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8))) {
        };
        InputStream buffered = IOUtil.getBufferedInputStream(base);
        assertNotSame(base, buffered);
        assertTrue(buffered instanceof BufferedInputStream);
    }

    @Test
    void getBufferedInputStream_alreadyBuffered_returnsSame() {
        BufferedInputStream base = new BufferedInputStream(new ByteArrayInputStream(new byte[0]));
        InputStream result = IOUtil.getBufferedInputStream(base);
        assertSame(base, result);
    }

    // ---------------------------------------------------------------------------
    // getBufferedOutputStream
    // ---------------------------------------------------------------------------

    @Test
    void getBufferedOutputStream_wrapsStream() throws IOException {
        ByteArrayOutputStream underlying = new ByteArrayOutputStream();
        OutputStream buffered = IOUtil.getBufferedOutputStream(underlying);
        assertNotNull(buffered);
        buffered.write("data".getBytes(StandardCharsets.UTF_8));
        buffered.flush();
        assertEquals("data", underlying.toString(StandardCharsets.UTF_8));
    }

    // ---------------------------------------------------------------------------
    // getBufferedUtf8Reader
    // ---------------------------------------------------------------------------

    @Test
    void getBufferedUtf8Reader_readsUtf8Content() throws IOException {
        byte[] data = "hello utf8".getBytes(StandardCharsets.UTF_8);
        BufferedReader reader = IOUtil.getBufferedUtf8Reader(new ByteArrayInputStream(data));
        assertEquals("hello utf8", reader.readLine());
    }

    // ---------------------------------------------------------------------------
    // getBufferedUtf8Writer (via getBufferedUtf8Writer)
    // ---------------------------------------------------------------------------

    @Test
    void getBufferedUtf8Writer_writesUtf8Content() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedWriter writer = IOUtil.getBufferedUtf8Writer(out);
        writer.write("hello");
        writer.flush();
        assertEquals("hello", out.toString(StandardCharsets.UTF_8));
    }

    // ---------------------------------------------------------------------------
    // getBufferedReader(Reader)
    // ---------------------------------------------------------------------------

    @Test
    void getBufferedReader_alreadyBuffered_returnsSame() {
        BufferedReader base = new BufferedReader(new StringReader("hello"));
        BufferedReader result = IOUtil.getBufferedReader(base);
        assertSame(base, result);
    }

    @Test
    void getBufferedReader_notBuffered_wraps() throws IOException {
        Reader base = new StringReader("hello reader");
        BufferedReader result = IOUtil.getBufferedReader(base);
        assertNotSame(base, result);
        assertEquals("hello reader", result.readLine());
    }

    // ---------------------------------------------------------------------------
    // getSizeLimitingInputStream
    // ---------------------------------------------------------------------------

    @Test
    void getSizeLimitingInputStream_nullInput_throwsNPE() {
        assertThrows(NullPointerException.class, () -> IOUtil.getSizeLimitingInputStream(null, 10, false));
    }

    @Test
    void getSizeLimitingInputStream_negativeLimit_returnsOriginal() {
        InputStream base = new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8));
        InputStream result = IOUtil.getSizeLimitingInputStream(base, -1, false);
        assertSame(base, result);
    }

    @Test
    void getSizeLimitingInputStream_maxValueLimit_returnsOriginal() {
        InputStream base = new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8));
        InputStream result = IOUtil.getSizeLimitingInputStream(base, Integer.MAX_VALUE, false);
        assertSame(base, result);
    }

    @Test
    void getSizeLimitingInputStream_sizedStreamUnderLimitWithErrorOnLimitExceeded_returnsWrapped() {
        InputStream base = SizedInputStream.forInputStream(
            new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)), 5
        );
        // The SizedInputStream reports a size (5) that is under the requested limit (100),
        // but the invocation requests a.
        InputStream result = IOUtil.getSizeLimitingInputStream(base, 100, true);
        assertNotSame(base, result);
    }

    @Test
    void getSizeLimitingInputStream_sizedStreamUnderLimit_returnsOriginalUnwrapped() {
        InputStream base = SizedInputStream.forInputStream(
            new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)), 5
        );
        // The SizedInputStream reports a size (5) that is under the requested limit (100),
        // so it's returned as-is without an additional limiting wrapper.
        InputStream result = IOUtil.getSizeLimitingInputStream(base, 100, false);
        assertSame(base, result);
    }

    @Test
    void getSizeLimitingInputStream_sizedStreamAtOrOverLimit_wraps() throws IOException {
        InputStream base = SizedInputStream.forInputStream(
            new ByteArrayInputStream("hello world".getBytes(StandardCharsets.UTF_8)), 11
        );
        // size (11) is not strictly less than the limit (5), so the stream is wrapped and limited.
        InputStream result = IOUtil.getSizeLimitingInputStream(base, 5, false);
        assertNotSame(base, result);
        assertEquals("hello", new String(result.readAllBytes(), StandardCharsets.UTF_8));
    }

    @Test
    void getSizeLimitingInputStream_underLimit_readsAllBytes() throws IOException {
        InputStream base = new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8));
        InputStream limited = IOUtil.getSizeLimitingInputStream(base, 100, false);
        assertEquals("hello", new String(limited.readAllBytes(), StandardCharsets.UTF_8));
    }

    @Test
    void getSizeLimitingInputStream_overLimit_noError_truncatesSilently() throws IOException {
        InputStream base = new ByteArrayInputStream("0123456789".getBytes(StandardCharsets.UTF_8));
        InputStream limited = IOUtil.getSizeLimitingInputStream(base, 5, false);
        assertEquals("01234", new String(limited.readAllBytes(), StandardCharsets.UTF_8));
    }

    @Test
    void getSizeLimitingInputStream_overLimit_errorOnLimitExceeded_throws() {
        InputStream base = new ByteArrayInputStream("0123456789".getBytes(StandardCharsets.UTF_8));
        InputStream limited = IOUtil.getSizeLimitingInputStream(base, 5, true);
        assertThrows(StreamSizeLimitExceededException.class, limited::readAllBytes);
    }

    @Test
    void getSizeLimitingInputStream_byteAtATime_underLimit_returnsExpectedBytesUntilExhausted() throws IOException {

        InputStream base = new ByteArrayInputStream("abcdef".getBytes(StandardCharsets.UTF_8));
        InputStream limited = IOUtil.getSizeLimitingInputStream(base, 3, false);

        assertEquals((byte) 'a', limited.read());
        assertEquals((byte) 'b', limited.read());
        assertEquals((byte) 'c', limited.read());
        assertEquals(-1, limited.read());

        byte[] actualBuff = new byte[50];
        Arrays.fill(actualBuff, Byte.MIN_VALUE);

        byte[] expectedBuff = new byte[50];
        Arrays.fill(expectedBuff, Byte.MIN_VALUE);

        int readBytes = limited.read(actualBuff, 0, 50);
        assertEquals(-1, readBytes);
        assertArrayEquals(expectedBuff, actualBuff);

    }

    // ---------------------------------------------------------------------------
    // getPrintWriterOutputStream — unusual cases
    // ---------------------------------------------------------------------------

    @Test
    void getPrintWriterOutputStream_singleByteWrites_passThrough() throws IOException {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        OutputStream os = IOUtil.getPrintWriterOutputStream(pw, false);
        for (byte b : "abc".getBytes(StandardCharsets.UTF_8)) {
            os.write(b);
        }
        os.flush();
        assertEquals("abc", sw.toString());
    }

    @Test
    void getPrintWriterOutputStream_singleByteWrite_incompleteMultiByteUtf8Char_throwsIOException() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        OutputStream os = IOUtil.getPrintWriterOutputStream(pw, false);
        // 0xC3 is the lead byte of a 2-byte UTF-8 sequence; writing it alone (one byte at a time)
        // gives the decoder an incomplete sequence, which is reported as malformed input.
        assertThrows(IOException.class, () -> os.write(0xC3));
    }

    @Test
    void getPrintWriterOutputStream_multiByteUtf8CharInOneWrite_decodesCorrectly() throws IOException {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        OutputStream os = IOUtil.getPrintWriterOutputStream(pw, false);
        byte[] data = "café".getBytes(StandardCharsets.UTF_8);
        os.write(data, 0, data.length);
        os.flush();
        assertEquals("café", sw.toString());
    }

    @Test
    void getPrintWriterOutputStream_writeWithInvalidBounds_throwsIndexOutOfBounds() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        OutputStream os = IOUtil.getPrintWriterOutputStream(pw, false);
        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
        assertThrows(IndexOutOfBoundsException.class, () -> os.write(data, 0, data.length + 10));
    }

    @Test
    void getPrintWriterOutputStream_allowCloseFalse_closeDoesNotPreventFurtherWrites() throws IOException {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        OutputStream os = IOUtil.getPrintWriterOutputStream(pw, false);
        os.write("one".getBytes(StandardCharsets.UTF_8));
        os.close();
        // Can't write through the OutputStream,
        assertThrows(IOException.class, () -> os.write("two".getBytes(StandardCharsets.UTF_8)));
        // but can still write to the open PrintWriter.
        assertDoesNotThrow(() -> pw.print("three"));
        // Can still flush the open PrintWriter
        assertDoesNotThrow(pw::flush);
        // The final result is the allowed text.
        assertEquals("onethree", sw.toString());
    }

    @Test
    void getPrintWriterOutputStream_allowClose_hasErrorOnWriteAfterClose() throws IOException {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        OutputStream os = IOUtil.getPrintWriterOutputStream(pw, true);
        os.write("hi".getBytes(StandardCharsets.UTF_8));
        os.close();
        assertFalse(pw.checkError());
        pw.print("xyz");
        assertTrue(pw.checkError());
    }

    // ---------------------------------------------------------------------------
    // runMainIORunnable — unusual cases, exercised via getCloseNotifyingOutputStream /
    // getCloseNotifyingInputStream, since runMainIORunnable itself is private.
    // ---------------------------------------------------------------------------

    @Test
    void getCloseNotifyingOutputStream_onBeforeCloseThrows_doesNotPropagate_butAfterCloseStillRuns() {
        AtomicBoolean afterRan = new AtomicBoolean();
        ByteArrayOutputStream base = new ByteArrayOutputStream();
        OutputStream notifying = IOUtil.getCloseNotifyingOutputStream(
            base,
            () -> {
                throw new RuntimeException("before failure");
            },
            () -> afterRan.set(true)
        );
        // Since the underlying close() succeeds, the suppressed before-close exception is swallowed
        // entirely rather than propagated.
        assertDoesNotThrow(notifying::close);
        assertTrue(afterRan.get());
    }

    @Test
    void getCloseNotifyingOutputStream_onAfterCloseThrows_doesNotPropagate() {
        AtomicBoolean beforeRan = new AtomicBoolean();
        ByteArrayOutputStream base = new ByteArrayOutputStream();
        OutputStream notifying = IOUtil.getCloseNotifyingOutputStream(
            base,
            () -> beforeRan.set(true),
            () -> {
                throw new RuntimeException("after failure");
            }
        );
        assertDoesNotThrow(notifying::close);
        assertTrue(beforeRan.get());
    }

    @Test
    void getCloseNotifyingOutputStream_close_isIdempotent_callbacksRunOnce() throws IOException {
        AtomicInteger beforeCount = new AtomicInteger();
        AtomicInteger afterCount = new AtomicInteger();
        ByteArrayOutputStream base = new ByteArrayOutputStream();
        OutputStream notifying = IOUtil.getCloseNotifyingOutputStream(
            base,
            beforeCount::incrementAndGet,
            afterCount::incrementAndGet
        );
        notifying.close();
        notifying.close();
        assertEquals(1, beforeCount.get());
        assertEquals(1, afterCount.get());
    }

    @Test
    void getCloseNotifyingOutputStream_mainThrowsIOException_propagatesWithSuppressedBeforeCloseFailure() {
        OutputStream failingBase = new OutputStream() {
            @Override
            public void write(int b) {
            }

            @Override
            public void close() throws IOException {
                throw new IOException("close failed");
            }
        };
        OutputStream notifying = IOUtil.getCloseNotifyingOutputStream(
            failingBase,
            () -> {
                throw new RuntimeException("before failure");
            },
            null
        );
        IOException thrown = assertThrows(IOException.class, notifying::close);
        assertEquals("close failed", thrown.getMessage());
        assertTrue(thrown.getSuppressed().length >= 1);
    }

    @Test
    void getCloseNotifyingOutputStream_mainThrowsRuntimeExceptionWrappingIOException_unwrapsToIOException() {
        OutputStream failingBase = new OutputStream() {
            @Override
            public void write(int b) {
            }

            @Override
            public void close() {
                throw new RuntimeException("wrapper", new IOException("inner failure"));
            }
        };

        CapturingIOTask before = new CapturingIOTask();
        CapturingIOTask after = new CapturingIOTask();
        OutputStream notifying = IOUtil.getCloseNotifyingOutputStream(failingBase, before, after);

        IOException thrown = assertThrows(IOException.class, notifying::close);
        assertEquals("inner failure", thrown.getMessage());
        assertTrue(before.ran);
        assertTrue(after.ran);

    }

    @Test
    void getCloseNotifyingOutputStream_mainThrowsPlainRuntimeException_propagatesAsRuntimeException() {
        OutputStream failingBase = new OutputStream() {
            @Override
            public void write(int b) {
            }

            @Override
            public void close() {
                throw new IllegalStateException("plain failure");
            }
        };
        OutputStream notifying = IOUtil.getCloseNotifyingOutputStream(failingBase, null, null);
        RuntimeException thrown = assertThrows(RuntimeException.class, notifying::close);
        assertEquals("plain failure", thrown.getMessage());
    }

    @Test
    void getCloseNotifyingInputStream_close_isIdempotent_callbacksRunOnce() throws IOException {
        AtomicInteger beforeCount = new AtomicInteger();
        AtomicInteger afterCount = new AtomicInteger();
        InputStream base = new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8));
        InputStream notifying = IOUtil.getCloseNotifyingInputStream(
            base,
            beforeCount::incrementAndGet,
            afterCount::incrementAndGet
        );
        notifying.close();
        notifying.close();
        assertEquals(1, beforeCount.get());
        assertEquals(1, afterCount.get());
    }

    @Test
    void getCloseNotifyingInputStream_onBeforeCloseThrows_doesNotPropagate_butAfterCloseStillRuns() {
        AtomicBoolean afterRan = new AtomicBoolean();
        InputStream base = new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8));
        InputStream notifying = IOUtil.getCloseNotifyingInputStream(
            base,
            () -> {
                throw new RuntimeException("before failure");
            },
            () -> afterRan.set(true)
        );
        assertDoesNotThrow(notifying::close);
        assertTrue(afterRan.get());
    }

    @Test
    void getCloseNotifyingInputStream_withSourceIdentifier_callbacksStillRun() throws IOException {
        AtomicBoolean before = new AtomicBoolean();
        AtomicBoolean after = new AtomicBoolean();
        InputStream base = new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8));
        InputStream notifying = IOUtil.getCloseNotifyingInputStream(
            base,
            () -> before.set(true),
            () -> after.set(true),
            "my-source"
        );
        notifying.close();
        assertTrue(before.get());
        assertTrue(after.get());
    }

    @Test
    void getCloseNotifyingOutputStream_bothCallbacksNull_returnsOriginalUnwrapped() {
        ByteArrayOutputStream base = new ByteArrayOutputStream();
        OutputStream result = IOUtil.getCloseNotifyingOutputStream(base, null, null);
        assertSame(base, result);
    }

}

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

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class IOUtilTest {

    // ---------------------------------------------------------------------------
    // close
    // ---------------------------------------------------------------------------

    @Test
    void close_null_doesNotThrow() {
        assertDoesNotThrow(() -> IOUtil.close(null));
    }

    @Test
    void close_closesResource() throws Exception {
        AtomicBoolean closed = new AtomicBoolean(false);
        AutoCloseable resource = () -> closed.set(true);
        IOUtil.close(resource);
        assertTrue(closed.get());
    }

    @Test
    void close_exceptionDuringClose_doesNotPropagate() {
        AutoCloseable resource = () -> { throw new Exception("boom"); };
        assertDoesNotThrow(() -> IOUtil.close(resource));
    }

    // ---------------------------------------------------------------------------
    // flush
    // ---------------------------------------------------------------------------

    @Test
    void flush_null_doesNotThrow() {
        assertDoesNotThrow(() -> IOUtil.flush((Flushable) null));
    }

    @Test
    void flush_flushesResource() throws Exception {
        AtomicBoolean flushed = new AtomicBoolean(false);
        Flushable resource = () -> flushed.set(true);
        IOUtil.flush(resource);
        assertTrue(flushed.get());
    }

    @Test
    void flush_exceptionDuringFlush_doesNotPropagate() {
        Flushable resource = () -> { throw new IOException("flush fail"); };
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
        IOUtil.CopyResult result = IOUtil.copyToEOF(new ByteArrayInputStream(new byte[]{1, 2}), null);
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
        byte[] data = {1, 2, 3};
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtil.CopyResult result = IOUtil.copyToEOF(new ByteArrayInputStream(data), out);
        assertFalse(result.triedToCopy());
        assertFalse(result.inputWasTooLarge());
        assertEquals(3, result.getBytesRead());
        assertEquals(3, result.getBytesWritten());
    }
}

// PLEASE DO NOT DELETE THIS LINE - make copyright depends on it.
package com.tractionsoftware.commons.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ByteBufferInputStreamTest {

    // ---------------------------------------------------------------------------
    // createInstance(byte[])
    // ---------------------------------------------------------------------------

    @Test
    void createInstance_byteArray_readsAllBytes() throws IOException {
        byte[] data = {1, 2, 3, 4, 5};
        try (InputStream in = ByteBufferInputStream.createInstance(data)) {
            for (byte expected : data) {
                assertEquals(expected & 0xFF, in.read());
            }
            assertEquals(-1, in.read());
        }
    }

    @Test
    void createInstance_emptyByteArray_immediateEOF() throws IOException {
        try (InputStream in = ByteBufferInputStream.createInstance(new byte[0])) {
            assertEquals(-1, in.read());
        }
    }

    // ---------------------------------------------------------------------------
    // createInstance(String, Charset)
    // ---------------------------------------------------------------------------

    @Test
    void createInstance_string_readsUtf8Bytes() throws IOException {
        String text = "hello";
        byte[] expected = text.getBytes(StandardCharsets.UTF_8);
        try (InputStream in = ByteBufferInputStream.createInstance(text, StandardCharsets.UTF_8)) {
            byte[] actual = in.readAllBytes();
            assertArrayEquals(expected, actual);
        }
    }

    @Test
    void createInstance_string_nullText_throws() {
        assertThrows(NullPointerException.class,
            () -> ByteBufferInputStream.createInstance(null, StandardCharsets.UTF_8));
    }

    @Test
    void createInstance_string_nullCharset_throws() {
        assertThrows(NullPointerException.class,
            () -> ByteBufferInputStream.createInstance("hello", null));
    }

    // ---------------------------------------------------------------------------
    // createInstance(ByteBuffer)
    // ---------------------------------------------------------------------------

    @Test
    void createInstance_byteBuffer_readsRemaining() throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(new byte[]{10, 20, 30});
        try (InputStream in = ByteBufferInputStream.createInstance(buf)) {
            assertEquals(10, in.read());
            assertEquals(20, in.read());
            assertEquals(30, in.read());
            assertEquals(-1, in.read());
        }
    }

    // ---------------------------------------------------------------------------
    // bulk read(byte[], int, int)
    // ---------------------------------------------------------------------------

    @Test
    void read_bulkArray_readsCorrectCount() throws IOException {
        byte[] data = {1, 2, 3, 4, 5, 6};
        try (InputStream in = ByteBufferInputStream.createInstance(data)) {
            byte[] buf = new byte[4];
            int read = in.read(buf, 0, 4);
            assertEquals(4, read);
            assertArrayEquals(new byte[]{1, 2, 3, 4}, buf);
        }
    }

    @Test
    void read_bulkArray_atEOF_returnsMinusOne() throws IOException {
        try (InputStream in = ByteBufferInputStream.createInstance(new byte[0])) {
            byte[] buf = new byte[4];
            assertEquals(-1, in.read(buf, 0, 4));
        }
    }

    @Test
    void read_bulkArray_partiallyFills() throws IOException {
        byte[] data = {7, 8};
        try (InputStream in = ByteBufferInputStream.createInstance(data)) {
            byte[] buf = new byte[10];
            int read = in.read(buf, 0, 10);
            assertEquals(2, read);
        }
    }

    // ---------------------------------------------------------------------------
    // close behavior
    // ---------------------------------------------------------------------------

    @Test
    void close_thenRead_throwsIOException() throws IOException {
        ByteBufferInputStream in = ByteBufferInputStream.createInstance(new byte[]{1, 2, 3});
        in.close();
        assertThrows(IOException.class, in::read);
    }

    @Test
    void close_idempotent() throws IOException {
        ByteBufferInputStream in = ByteBufferInputStream.createInstance(new byte[]{1});
        in.close();
        assertDoesNotThrow(in::close);
    }

    // ---------------------------------------------------------------------------
    // custom creator
    // ---------------------------------------------------------------------------

    @Test
    void constructor_customCreator_evaluated_lazily() throws IOException {
        int[] callCount = {0};
        ByteBufferInputStream in = new ByteBufferInputStream(() -> {
            callCount[0]++;
            return ByteBuffer.wrap(new byte[]{42});
        });
        assertEquals(0, callCount[0], "creator should not be called until first read");
        assertEquals(42, in.read());
        assertEquals(1, callCount[0]);
        in.close();
    }

}

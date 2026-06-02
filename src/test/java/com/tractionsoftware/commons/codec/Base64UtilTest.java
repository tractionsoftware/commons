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

package com.tractionsoftware.commons.codec;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class Base64UtilTest {

    private static final String PLAIN = "Hello, World!";
    private static final String ENCODED = Base64.getEncoder().encodeToString(PLAIN.getBytes(StandardCharsets.UTF_8));

    // ---------------------------------------------------------------------------
    // getDecodedBytes(String)
    // ---------------------------------------------------------------------------

    @Test
    void getDecodedBytes_null_returnsNull() {
        assertNull(Base64Util.getDecodedBytes((String) null));
    }

    @Test
    void getDecodedBytes_validEncoding() {
        byte[] result = Base64Util.getDecodedBytes(ENCODED);
        assertArrayEquals(PLAIN.getBytes(StandardCharsets.UTF_8), result);
    }

    @Test
    void getDecodedBytes_invalidEncoding_returnsEmpty() {
        byte[] result = Base64Util.getDecodedBytes("!!!not-base64!!!");
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    // ---------------------------------------------------------------------------
    // getDecodedBytes(byte[])
    // ---------------------------------------------------------------------------

    @Test
    void getDecodedBytes_nullBytes_returnsNull() {
        assertNull(Base64Util.getDecodedBytes((byte[]) null));
    }

    @Test
    void getDecodedBytes_validBytes() {
        byte[] encodedBytes = ENCODED.getBytes(StandardCharsets.UTF_8);
        byte[] result = Base64Util.getDecodedBytes(encodedBytes);
        assertArrayEquals(PLAIN.getBytes(StandardCharsets.UTF_8), result);
    }

    // ---------------------------------------------------------------------------
    // getUtf8DecodedString
    // ---------------------------------------------------------------------------

    @Test
    void getUtf8DecodedString_null_returnsNull() {
        assertNull(Base64Util.getUtf8DecodedString((String) null));
    }

    @Test
    void getUtf8DecodedString_valid() {
        assertEquals(PLAIN, Base64Util.getUtf8DecodedString(ENCODED));
    }

    // ---------------------------------------------------------------------------
    // getDecodedString(byte[])
    // ---------------------------------------------------------------------------

    @Test
    void getDecodedString_nullBytes_returnsNull() {
        assertNull(Base64Util.getDecodedString((byte[]) null));
    }

    @Test
    void getDecodedString_validBytes() {
        byte[] encodedBytes = ENCODED.getBytes(StandardCharsets.UTF_8);
        assertEquals(PLAIN, Base64Util.getDecodedString(encodedBytes));
    }

    // ---------------------------------------------------------------------------
    // getEncodedString(byte[])
    // ---------------------------------------------------------------------------

    @Test
    void getEncodedString_null_returnsNull() {
        assertNull(Base64Util.getEncodedString((byte[]) null));
    }

    @Test
    void getEncodedString_valid() {
        String result = Base64Util.getEncodedString(PLAIN.getBytes(StandardCharsets.UTF_8));
        assertEquals(ENCODED, result);
    }

    // ---------------------------------------------------------------------------
    // getEncodedBytes(byte[])
    // ---------------------------------------------------------------------------

    @Test
    void getEncodedBytes_null_returnsNull() {
        assertNull(Base64Util.getEncodedBytes((byte[]) null));
    }

    @Test
    void getEncodedBytes_valid() {
        byte[] result = Base64Util.getEncodedBytes(PLAIN.getBytes(StandardCharsets.UTF_8));
        assertArrayEquals(ENCODED.getBytes(StandardCharsets.UTF_8), result);
    }

    // ---------------------------------------------------------------------------
    // getUtf8EncodedString
    // ---------------------------------------------------------------------------

    @Test
    void getUtf8EncodedString_null_returnsNull() {
        assertNull(Base64Util.getUtf8EncodedString(null));
    }

    @Test
    void getUtf8EncodedString_valid() {
        assertEquals(ENCODED, Base64Util.getUtf8EncodedString(PLAIN));
    }

    @Test
    void getUtf8EncodedString_roundTrip() {
        String encoded = Base64Util.getUtf8EncodedString(PLAIN);
        assertEquals(PLAIN, Base64Util.getUtf8DecodedString(encoded));
    }

    // ---------------------------------------------------------------------------
    // getEncodedString(byte[], int bytesPerLine)
    // ---------------------------------------------------------------------------

    @Test
    void getEncodedString_withBytesPerLine_null_returnsNull() {
        assertNull(Base64Util.getEncodedString((byte[]) null, 80));
    }

    @Test
    void getEncodedString_withBytesPerLine_noLineBreaks() {
        // bytesPerLine <= 0 means no line breaks
        String result = Base64Util.getEncodedString(PLAIN.getBytes(StandardCharsets.UTF_8), -1);
        assertFalse(result.contains("\n"));
        assertEquals(ENCODED, result);
    }

    @Test
    void getEncodedString_withBytesPerLine_hasLineBreaks() {
        // Use a very small bytesPerLine to force line breaks on a longer string
        String longPlain = "A".repeat(200);
        String result = Base64Util.getEncodedString(longPlain.getBytes(StandardCharsets.UTF_8), 10);
        assertTrue(result.contains("\n"), "Expected line breaks with small bytesPerLine");
    }

    // ---------------------------------------------------------------------------
    // printDecodedString / printUtf8DecodedString
    // ---------------------------------------------------------------------------

    @Test
    void printDecodedString_empty_writesNothing() {
        StringWriter sw = new StringWriter();
        Base64Util.printDecodedString("", StandardCharsets.UTF_8, false, sw);
        assertEquals("", sw.toString());
    }

    @Test
    void printUtf8DecodedString_valid() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Base64Util.printUtf8DecodedString(ENCODED, false, pw);
        pw.flush();
        assertEquals(PLAIN, sw.toString());
    }

    // ---------------------------------------------------------------------------
    // printEncodedString
    // ---------------------------------------------------------------------------

    @Test
    void printEncodedString_valid() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Base64Util.printUtf8EncodedString(PLAIN, -1, pw);
        pw.flush();
        assertEquals(ENCODED, sw.toString());
    }

    @Test
    void printEncodedString_null_writesNothing() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Base64Util.printEncodedString((String) null, StandardCharsets.UTF_8, -1, pw);
        pw.flush();
        assertEquals("", sw.toString());
    }

    // ---------------------------------------------------------------------------
    // mime decoding
    // ---------------------------------------------------------------------------

    @Test
    void mimeDecoder_roundTrip() {
        String mimeEncoded = Base64.getMimeEncoder().encodeToString(PLAIN.getBytes(StandardCharsets.UTF_8));
        assertEquals(PLAIN, Base64Util.getUtf8DecodedString(mimeEncoded, true));
    }

}

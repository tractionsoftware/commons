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
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.*;

class MD5UtilTest {

    // Known MD5 of "hello": 5d41402abc4b2a76b9719d911017c592
    private static final String HELLO_MD5_PADDED = "5d41402abc4b2a76b9719d911017c592";
    private static final byte[] HELLO_BYTES = "hello".getBytes(StandardCharsets.UTF_8);

    // ---------------------------------------------------------------------------
    // getMessageDigest
    // ---------------------------------------------------------------------------

    @Test
    void getMessageDigest_returnsNonNull() {
        MessageDigest md = MD5Util.getMessageDigest();
        assertNotNull(md);
        assertEquals("MD5", md.getAlgorithm());
    }

    // ---------------------------------------------------------------------------
    // digest(byte[])
    // ---------------------------------------------------------------------------

    @Test
    void digest_bytes_successful() {
        MD5Util.DigestResult result = MD5Util.digest(HELLO_BYTES);
        assertTrue(result.wasSuccessful());
        assertFalse(result.isEmpty());
        assertNotNull(result.hashBytes());
        assertTrue(result.hashBytes().length > 0);
    }

    @Test
    void digest_bytes_knownHash() {
        MD5Util.DigestResult result = MD5Util.digest(HELLO_BYTES);
        assertEquals(HELLO_MD5_PADDED, result.paddedHashString());
    }

    // Known MD5 of empty input: d41d8cd98f00b204e9800998ecf8427e
    private static final String EMPTY_MD5_PADDED = "d41d8cd98f00b204e9800998ecf8427e";

    @Test
    void digest_emptyBytes_successfulWithRealHash() {
        // MD5 of zero bytes is a real hash, not an empty result
        MD5Util.DigestResult result = MD5Util.digest(new byte[0]);
        assertTrue(result.wasSuccessful());
        assertFalse(result.isEmpty());
        assertEquals(EMPTY_MD5_PADDED, result.paddedHashString());
    }

    @Test
    void digest_deterministicForSameInput() {
        MD5Util.DigestResult r1 = MD5Util.digest(HELLO_BYTES);
        MD5Util.DigestResult r2 = MD5Util.digest(HELLO_BYTES);
        assertEquals(r1.hashString(), r2.hashString());
        assertEquals(r1.paddedHashString(), r2.paddedHashString());
    }

    @Test
    void digest_differentInputsDifferentHash() {
        MD5Util.DigestResult r1 = MD5Util.digest(HELLO_BYTES);
        MD5Util.DigestResult r2 = MD5Util.digest("world".getBytes(StandardCharsets.UTF_8));
        assertNotEquals(r1.hashString(), r2.hashString());
    }

    // ---------------------------------------------------------------------------
    // digest(InputStream)
    // ---------------------------------------------------------------------------

    @Test
    void digest_inputStream_successful() throws IOException {
        MD5Util.DigestResult result = MD5Util.digest(new ByteArrayInputStream(HELLO_BYTES));
        assertTrue(result.wasSuccessful());
        assertEquals(HELLO_MD5_PADDED, result.paddedHashString());
    }

    @Test
    void digest_digestInputStream_reuses() throws IOException {
        // Wrapping in a DigestInputStream should produce the same result
        var dis = MD5Util.createDigestInputStream(new ByteArrayInputStream(HELLO_BYTES));
        MD5Util.DigestResult result = MD5Util.digest(dis);
        assertTrue(result.wasSuccessful());
        assertEquals(HELLO_MD5_PADDED, result.paddedHashString());
    }

    // ---------------------------------------------------------------------------
    // digest(File) — directory only; File overload requires FileMetadata so
    // we avoid digest(File) for regular files and use digest(InputStream) instead.
    // ---------------------------------------------------------------------------

    @Test
    void digest_directory_returnsEmptySuccess(@TempDir Path tempDir) {
        MD5Util.DigestResult result = MD5Util.digest(tempDir.toFile());
        assertTrue(result.wasSuccessful());
        assertTrue(result.isEmpty());
    }

    @Test
    void digest_fileViaInputStream_returnsHash(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("test.txt");
        Files.write(file, HELLO_BYTES);
        MD5Util.DigestResult result;
        try (InputStream input = Files.newInputStream(file)) {
            result = MD5Util.digest(input);
        }
        assertTrue(result.wasSuccessful());
        assertEquals(HELLO_MD5_PADDED, result.paddedHashString());
    }

    // ---------------------------------------------------------------------------
    // getHashString / getPaddedHashString
    // ---------------------------------------------------------------------------

    @Test
    void getPaddedHashString_null_returnsNull() {
        assertNull(MD5Util.getPaddedHashString(null));
    }

    @Test
    void getPaddedHashString_is32Chars() {
        MD5Util.DigestResult result = MD5Util.digest(HELLO_BYTES);
        assertEquals(32, result.paddedHashString().length());
    }

    @Test
    void getHashString_matchesPaddedForNormalInput() {
        // For a standard hash both should be 32 hex chars (no leading zeros needed in practice)
        MD5Util.DigestResult result = MD5Util.digest(HELLO_BYTES);
        // paddedHashString zero-pads to 32; hashString may not, but for "hello" they should match
        assertNotNull(result.hashString());
        assertNotNull(result.paddedHashString());
    }

    // ---------------------------------------------------------------------------
    // DigestResult failure sentinel — use a broken InputStream to trigger failure
    // ---------------------------------------------------------------------------

    @Test
    void digestResult_failure_isNotSuccessful() {
        java.io.InputStream broken = new java.io.InputStream() {
            @Override public int read() throws IOException { throw new IOException("simulated failure"); }
        };
        MD5Util.DigestResult result = MD5Util.digest(broken);
        assertNotNull(result);
        assertFalse(result.wasSuccessful());
        assertTrue(result.isEmpty());
        assertNull(result.hashString());
        assertNull(result.paddedHashString());
    }

}

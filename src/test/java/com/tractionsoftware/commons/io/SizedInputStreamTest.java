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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public final class SizedInputStreamTest {

    private static final class NoSizeInputStream extends SizedInputStream {

        NoSizeInputStream(InputStream input) {
            super(input);
        }

    }

    // ---------------------------------------------------------------------------
    // base class default size()
    // ---------------------------------------------------------------------------

    @Test
    void size_baseClassDefault_returnsLongMinValue() {
        SizedInputStream stream = new NoSizeInputStream(new ByteArrayInputStream(new byte[0]));
        assertEquals(Long.MIN_VALUE, stream.size());
    }

    @Test
    void size_baseClassDefault_hasSizeAtLeastReturnsFalse() {
        SizedInputStream stream = new NoSizeInputStream(new ByteArrayInputStream(new byte[0]));
        assertFalse(stream.hasSizeAtLeast(0));
    }

    // ---------------------------------------------------------------------------
    // forInputStream
    // ---------------------------------------------------------------------------

    @Test
    void forInputStream_nullInput_throwsNPE() {
        assertThrows(NullPointerException.class, () -> SizedInputStream.forInputStream(null, 5));
    }

    @Test
    void forInputStream_reportsGivenByteSize() {
        SizedInputStream stream = SizedInputStream.forInputStream(new ByteArrayInputStream(new byte[0]), 42);
        assertEquals(42, stream.size());
    }

    @Test
    void forInputStream_byteSizeZero_isReportedAsKnown() {
        SizedInputStream stream = SizedInputStream.forInputStream(new ByteArrayInputStream(new byte[0]), 0);
        assertEquals(0, stream.size());
        assertTrue(stream.hasSizeAtMost(0));
    }

    @Test
    void forInputStream_delegatesReadsToWrappedStream() throws IOException {
        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
        SizedInputStream stream = SizedInputStream.forInputStream(new ByteArrayInputStream(data), data.length);
        assertArrayEquals(data, stream.readAllBytes());
    }

    @Test
    void forInputStream_delegatesCloseToWrappedStream() throws IOException {
        boolean[] closed = { false };
        InputStream base = new ByteArrayInputStream(new byte[0]) {
            @Override
            public void close() throws IOException {
                closed[0] = true;
                super.close();
            }
        };
        SizedInputStream stream = SizedInputStream.forInputStream(base, 0);
        stream.close();
        assertTrue(closed[0]);
    }

    @Test
    void forInputStream_isInstanceOfMayHaveKnownSize() {
        SizedInputStream stream = SizedInputStream.forInputStream(new ByteArrayInputStream(new byte[0]), 5);
        assertTrue(stream instanceof com.tractionsoftware.commons.util.MayHaveKnownSize);
    }

}

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

import jakarta.annotation.Nonnull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ForwardingTempFileResourceTest {

    /**
     * Concrete subclass of ForwardingTempFileResource that delegates to a real TempFileResource.
     */
    private static final class TestForwardingTempFileResource extends ForwardingTempFileResource {

        private final TempFileResource delegate;

        TestForwardingTempFileResource(TempFileResource delegate) {
            this.delegate = delegate;
        }

        @Nonnull
        @Override
        protected TempFileResource delegate() {
            return delegate;
        }

    }

    private TempFileResource delegate;
    private TestForwardingTempFileResource forwarding;

    @BeforeEach
    void setUp() {
        SimpleMutableFileMetadata metadata = SimpleMutableFileMetadata.createFromFileName("test.tmp");
        delegate = LocalTempFileService.DEFAULT_FACTORY.create(metadata, null, null);
        forwarding = new TestForwardingTempFileResource(delegate);
    }

    // ---------------------------------------------------------------------------
    // exists / delete
    // ---------------------------------------------------------------------------

    @Test
    void exists_afterWriteAndSave_returnsTrue() throws IOException {
        try (OutputStream os = forwarding.getOutputStream()) {
            os.write("test".getBytes(StandardCharsets.UTF_8));
        }
        forwarding.save();
        assertTrue(forwarding.exists());
    }

    @Test
    void delete_afterWriteAndSave_removesFile() throws IOException {
        try (OutputStream os = forwarding.getOutputStream()) {
            os.write("x".getBytes(StandardCharsets.UTF_8));
        }
        forwarding.save();
        assertTrue(forwarding.exists());
        forwarding.delete();
        assertFalse(forwarding.exists());
    }

    // ---------------------------------------------------------------------------
    // save
    // ---------------------------------------------------------------------------

    @Test
    void save_doesNotThrow() {
        assertDoesNotThrow(() -> {
            try (OutputStream os = forwarding.getOutputStream()) {
                os.write("hello".getBytes(StandardCharsets.UTF_8));
            }
            forwarding.save();
        });
    }

    // ---------------------------------------------------------------------------
    // getErrorMessage
    // ---------------------------------------------------------------------------

    @Test
    void getErrorMessage_normalResource_returnsNull() {
        assertNull(forwarding.getErrorMessage());
    }

    // ---------------------------------------------------------------------------
    // getOutputStream / getUtf8PrintWriter
    // ---------------------------------------------------------------------------

    @Test
    void getOutputStream_returnsWritableStream() throws IOException {
        try (OutputStream os = forwarding.getOutputStream()) {
            assertNotNull(os);
            os.write("data".getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
    void getUtf8PrintWriter_returnsWriter() throws IOException {
        try (PrintWriter writer = forwarding.getUtf8PrintWriter()) {
            assertNotNull(writer);
            writer.print("hello");
        }
    }

    // ---------------------------------------------------------------------------
    // flush / close
    // ---------------------------------------------------------------------------

    @Test
    void flush_delegatesToDelegate() {
        assertDoesNotThrow(() -> forwarding.flush());
    }

    @Test
    void close_delegatesToDelegate() {
        assertDoesNotThrow(() -> forwarding.close());
    }

    // ---------------------------------------------------------------------------
    // getPath
    // ---------------------------------------------------------------------------

    @Test
    void getPath_delegatesToDelegate() {
        String path = forwarding.getPath();
        assertNotNull(path);
        assertEquals(delegate.getPath(), path);
    }

    // ---------------------------------------------------------------------------
    // FileResource delegation (via ForwardingFileResource)
    // ---------------------------------------------------------------------------

    @Test
    void isValid_delegatesToDelegate() {
        assertEquals(delegate.isValid(), forwarding.isValid());
    }

    @Test
    void getFilename_delegatesToDelegate() {
        assertEquals(delegate.getFilename(), forwarding.getFilename());
    }

    @Test
    void getContentType_delegatesToDelegate() {
        assertEquals(delegate.getContentType(), forwarding.getContentType());
    }

    @Test
    void getType_delegatesToDelegate() {
        assertEquals(delegate.getType(), forwarding.getType());
    }

}

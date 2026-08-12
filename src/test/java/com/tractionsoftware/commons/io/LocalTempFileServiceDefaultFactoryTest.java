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
import org.junit.jupiter.api.function.Executable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link LocalTempFileService#DEFAULT_FACTORY}.
 */
class LocalTempFileServiceDefaultFactoryTest {

    private static final TempFileResource.Factory FACTORY = LocalTempFileService.DEFAULT_FACTORY;

    private static SimpleMutableFileMetadata metadata(String filename) {
        return SimpleMutableFileMetadata.createFromFileName(filename);
    }

    // ---------------------------------------------------------------------------
    // create — no content
    // ---------------------------------------------------------------------------

    @Test
    void create_noContent_createsValidResource() {
        TempFileResource temp = FACTORY.create(metadata("empty.txt"), null, null);
        try {
            assertFalse(temp.hadError(), "Should not have an error");
            assertTrue(temp.exists());
            assertTrue(temp.isValid());
        }
        finally {
            temp.delete();
        }
    }

    @Test
    void create_noContent_filenameReflectsMetadata() {
        TempFileResource temp = FACTORY.create(metadata("myfile.pdf"), null, null);
        try {
            assertEquals("myfile.pdf", temp.getFilename());
        }
        finally {
            temp.delete();
        }
    }

    @Test
    void create_noContent_uriSchemeIsTemp() {
        TempFileResource temp = FACTORY.create(metadata("scheme.txt"), null, null);
        try {
            assertEquals(TempFileResource.URI_SCHEME_NAME, temp.getURI().getScheme());
        }
        finally {
            temp.delete();
        }
    }

    // ---------------------------------------------------------------------------
    // create — with content
    // ---------------------------------------------------------------------------

    @Test
    void create_withContent_populatesFile() throws IOException {
        byte[] content = "factory content".getBytes(StandardCharsets.UTF_8);
        TempFileResource temp = FACTORY.create(
            metadata("content.txt"),
            () -> new ByteArrayInputStream(content),
            null
        );
        try {
            assertFalse(temp.hadError());
            byte[] read = temp.getInputStream().readAllBytes();
            assertArrayEquals(content, read);
        }
        finally {
            temp.delete();
        }
    }

    @Test
    void create_withContent_byteSizeMatchesContent() throws IOException {
        byte[] content = "1234567890".getBytes(StandardCharsets.UTF_8);
        TempFileResource temp = FACTORY.create(
            metadata("sized.txt"),
            () -> new ByteArrayInputStream(content),
            null
        );
        try {
            assertEquals(content.length, temp.getByteSize());
        }
        finally {
            temp.delete();
        }
    }

    @Test
    void create_withContent_emptyBytes_createsEmptyFile() throws IOException {
        TempFileResource temp = FACTORY.create(
            metadata("empty-content.txt"),
            () -> new ByteArrayInputStream(new byte[0]),
            null
        );
        try {
            assertFalse(temp.hadError());
            assertEquals(0L, temp.getByteSize());
        }
        finally {
            temp.delete();
        }
    }

    // ---------------------------------------------------------------------------
    // create — content stream failure cleans up
    // ---------------------------------------------------------------------------

    @Test
    void create_contentStreamThrows_returnsErrorResource() {
        TempFileResource temp = FACTORY.create(
            metadata("bad-stream.txt"),
            () -> {
                throw new IOException("simulated stream failure");
            },
            null
        );
        // The factory catches the IOException, deletes the temp file, and returns an ErrorTempFileResource
        assertTrue(temp.hadError(), "Expected hadError() after stream failure");
    }

    // ---------------------------------------------------------------------------
    // loadExisting
    // ---------------------------------------------------------------------------

    @Test
    void loadExisting_existingTempFile_returnsValidResource() throws IOException {
        // First create a real temp file via DEFAULT so we have a valid URI
        TempFileResource created = LocalTempFileService.DEFAULT.createNew(metadata("load-test.txt"));
        try {
            SimpleMutableFileMetadata m = SimpleMutableFileMetadata.createCopy(created.getMetadata());
            TempFileResource loaded = FACTORY.loadExisting(m, null);
            assertFalse(loaded.hadError());
            assertTrue(loaded.exists());
        }
        finally {
            created.delete();
        }
    }

    @Test
    void loadExisting_missingFile_returnsErrorResource() {
        SimpleMutableFileMetadata m = metadata("nonexistent.txt");
        m.setURI(URI.create(TempFileResource.URI_SCHEME_PREFIX + "nonexistent-xyz.txt"));
        TempFileResource result = FACTORY.loadExisting(m, null);
        assertTrue(result.hadError(), "Expected hadError() for missing file");
    }

    // ---------------------------------------------------------------------------
    // null metadata guard
    // ---------------------------------------------------------------------------

    @Test
    void create_nullMetadata_throws() {
        assertThrows(NullPointerException.class, () -> FACTORY.create(null, null, null));
    }

}

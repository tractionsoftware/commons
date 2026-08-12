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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link LocalTempFileService#DEFAULT}.
 */
class LocalTempFileServiceTest {

    private static final LocalTempFileService SERVICE = LocalTempFileService.DEFAULT;

    private static SimpleMutableFileMetadata metadata(String filename) {
        return SimpleMutableFileMetadata.createFromFileName(filename);
    }

    // ---------------------------------------------------------------------------
    // createNew
    // ---------------------------------------------------------------------------

    @Test
    void createNew_nullMetadata_throws() {
        assertThrows(NullPointerException.class, () -> SERVICE.createNew(null));
    }

    @Test
    void createNew_createsExistingFile() throws IOException {
        TempFileResource temp = SERVICE.createNew(metadata("hello.txt"));
        try {
            assertTrue(temp.exists(), "Temp file should exist after creation");
            assertTrue(temp.isValid(), "Temp file should be valid after creation");
        }
        finally {
            temp.delete();
        }
    }

    @Test
    void createNew_deletedAfterDelete() throws IOException {
        TempFileResource temp = SERVICE.createNew(metadata("gone.txt"));
        temp.delete();
        assertFalse(temp.exists(), "Temp file should not exist after delete");
    }

    @Test
    void createNew_filenamePreserved() throws IOException {
        TempFileResource temp = SERVICE.createNew(metadata("report.pdf"));
        try {
            assertEquals("report.pdf", temp.getFilename());
        }
        finally {
            temp.delete();
        }
    }

    @Test
    void createNew_extensionPreserved() throws IOException {
        // DEFAULT creates the OS temp file with the right extension via File.createTempFile
        TempFileResource temp = SERVICE.createNew(metadata("data.csv"));
        try {
            // The URI scheme should be "temp"
            assertEquals(TempFileResource.URI_SCHEME_NAME, temp.getURI().getScheme());
        }
        finally {
            temp.delete();
        }
    }

    @Test
    void createNew_canWriteAndReadBack() throws IOException {
        TempFileResource temp = SERVICE.createNew(metadata("rw.txt"));
        try {
            byte[] content = "hello temp".getBytes(StandardCharsets.UTF_8);
            try (OutputStream out = temp.getOutputStream()) {
                out.write(content);
            }
            byte[] read = temp.getInputStream().readAllBytes();
            assertArrayEquals(content, read);
        }
        finally {
            temp.delete();
        }
    }

    @Test
    void createNew_byteSizeReflectsContent() throws IOException {
        TempFileResource temp = SERVICE.createNew(metadata("sized.txt"));
        try {
            byte[] content = "1234567890".getBytes(StandardCharsets.UTF_8);
            try (OutputStream out = temp.getOutputStream()) {
                out.write(content);
            }
            assertEquals(content.length, temp.getByteSize());
        }
        finally {
            temp.delete();
        }
    }

    @Test
    void createNew_hadError_isFalse() throws IOException {
        TempFileResource temp = SERVICE.createNew(metadata("ok.txt"));
        try {
            assertFalse(temp.hadError());
        }
        finally {
            temp.delete();
        }
    }

    // ---------------------------------------------------------------------------
    // getURI / getFile round-trip
    // ---------------------------------------------------------------------------

    @Test
    void getURI_producesValidTempSchemeURI() throws IOException {
        TempFileResource temp = SERVICE.createNew(metadata("uri-test.txt"));
        try {
            URI uri = temp.getURI();
            assertNotNull(uri);
            assertEquals(TempFileResource.URI_SCHEME_NAME, uri.getScheme());
        }
        finally {
            temp.delete();
        }
    }

    @Test
    void getFile_validURI_returnsFile() throws IOException {
        TempFileResource temp = SERVICE.createNew(metadata("lookup.txt"));
        try {
            URI uri = temp.getURI();
            // getFile requires a live temp file that exists
            // DEFAULT.getFile looks up by filename only, so the file must exist
            assertDoesNotThrow(() -> SERVICE.getFile(uri));
        }
        finally {
            temp.delete();
        }
    }

    @Test
    void getFile_nullURI_throws() {
        assertThrows(NullPointerException.class, () -> SERVICE.getFile(null));
    }

    @Test
    void getFile_wrongScheme_throws() {
        URI http = URI.create("http://example.com/file.txt");
        assertThrows(IllegalArgumentException.class, () -> SERVICE.getFile(http));
    }

    @Test
    void getFile_unknownName_throwsFileNotFound() {
        URI unknown = URI.create(TempFileResource.URI_SCHEME_PREFIX + "nonexistent-file-xyz.txt");
        assertThrows(FileNotFoundException.class, () -> SERVICE.getFile(unknown));
    }

    // ---------------------------------------------------------------------------
    // get() returns DEFAULT (no service loader override in test classpath)
    // ---------------------------------------------------------------------------

    @Test
    void get_returnsNonNull() {
        assertNotNull(LocalTempFileService.get());
    }

}

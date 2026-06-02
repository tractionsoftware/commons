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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class ErrorTempFileResourceTest {

    private Exception error;
    private ErrorTempFileResource resource;

    @BeforeEach
    void setUp() {
        SimpleMutableFileMetadata metadata = new SimpleMutableFileMetadata();
        metadata.setFilename("test.txt");
        error = new RuntimeException("something went wrong");
        resource = ErrorTempFileResource.createInstance(metadata, error);
    }

    // ---------------------------------------------------------------------------
    // factory
    // ---------------------------------------------------------------------------

    @Test
    void createInstance_nullMetadata_throwsNPE() {
        assertThrows(NullPointerException.class, () -> ErrorTempFileResource.createInstance(null, error));
    }

    @Test
    void createInstance_metadataWithNullFilename_throwsNPE() {
        SimpleMutableFileMetadata noName = new SimpleMutableFileMetadata();
        // filename not set → null → should throw
        assertThrows(NullPointerException.class, () -> ErrorTempFileResource.createInstance(noName, error));
    }

    // ---------------------------------------------------------------------------
    // core contract
    // ---------------------------------------------------------------------------

    @Test
    void exists_returnsFalse() {
        assertFalse(resource.exists());
    }

    @Test
    void isValid_returnsFalse() {
        assertFalse(resource.isValid());
    }

    @Test
    void hadError_returnsTrue() {
        assertTrue(resource.hadError());
    }

    @Test
    void delete_returnsTrue() {
        assertTrue(resource.delete());
    }

    @Test
    void getByteSize_returnsZero() {
        assertEquals(0L, resource.getByteSize());
    }

    @Test
    void getLastModified_isRecentDate() {
        Date before = new Date(System.currentTimeMillis() - 1000);
        Date lastModified = resource.getLastModified();
        assertNotNull(lastModified);
        assertTrue(lastModified.after(before));
    }

    @Test
    void getErrorMessage_returnsExceptionMessage() {
        assertEquals("something went wrong", resource.getErrorMessage());
    }

    // ---------------------------------------------------------------------------
    // IO methods must throw FileNotFoundException
    // ---------------------------------------------------------------------------

    @Test
    void getInputStream_throwsFileNotFoundException() {
        assertThrows(FileNotFoundException.class, () -> resource.getInputStream());
    }

    @Test
    void getOutputStream_throwsFileNotFoundException() {
        assertThrows(FileNotFoundException.class, () -> resource.getOutputStream());
    }

    @Test
    void getUtf8PrintWriter_throwsFileNotFoundException() {
        assertThrows(FileNotFoundException.class, () -> resource.getUtf8PrintWriter());
    }

    @Test
    void save_throwsFileNotFoundException() {
        assertThrows(FileNotFoundException.class, () -> resource.save());
    }

    // ---------------------------------------------------------------------------
    // flush / close are no-ops
    // ---------------------------------------------------------------------------

    @Test
    void flush_doesNotThrow() {
        assertDoesNotThrow(() -> resource.flush());
    }

    @Test
    void close_doesNotThrow() {
        assertDoesNotThrow(() -> resource.close());
    }
}

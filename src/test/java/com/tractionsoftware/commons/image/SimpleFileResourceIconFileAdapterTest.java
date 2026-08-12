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

package com.tractionsoftware.commons.image;

import com.tractionsoftware.commons.io.CommonFileResourceType;
import com.tractionsoftware.commons.io.LocalFileResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public final class SimpleFileResourceIconFileAdapterTest {

    @TempDir
    Path tempDir;

    private Path imageFile;
    private SimpleFileResourceIconFileAdapter adapter;

    @BeforeEach
    void setUp() throws IOException {
        // Use a known PNG for testing
        imageFile = tempDir.resolve("icon.png");
        // Copy the test image from resources
        try (var in = getClass().getResourceAsStream("/com/tractionsoftware/commons/image/cat-normal.png")) {
            if (in != null) {
                Files.copy(in, imageFile);
            }
            else {
                // Fallback: write a minimal file
                Files.write(imageFile, new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47 });
            }
        }
        var fileResource = LocalFileResource.createInstance(imageFile.toFile());
        adapter = SimpleFileResourceIconFileAdapter.createInstance(fileResource, CommonFileResourceType.OTHER);
    }

    // =====================================================================
    // createInstance
    // =====================================================================

    @Test
    void createInstance_nullFileResource_throwsNPE() {
        assertThrows(
            NullPointerException.class, () ->
                SimpleFileResourceIconFileAdapter.createInstance(null, CommonFileResourceType.OTHER)
        );
    }

    @Test
    void createInstance_nullFileResourceType_throwsNPE() {
        var fileResource = LocalFileResource.createInstance(imageFile.toFile());
        assertThrows(
            NullPointerException.class, () ->
                SimpleFileResourceIconFileAdapter.createInstance(fileResource, null)
        );
    }

    // =====================================================================
    // basic accessors
    // =====================================================================

    @Test
    void getFilename_returnsIconPng() {
        assertEquals("icon.png", adapter.getFilename());
    }

    @Test
    void getPath_isNotNull() {
        assertNotNull(adapter.getPath());
    }

    @Test
    void getURI_isNotNull() {
        assertNotNull(adapter.getURI());
    }

    @Test
    void getLastModified_isNotNull() {
        assertNotNull(adapter.getLastModified());
    }

    @Test
    void getType_isImage() {
        assertNotNull(adapter.getType());
    }

    @Test
    void getImageResourceType_returnsCommonFileResourceType() {
        assertEquals(CommonFileResourceType.OTHER, adapter.getImageResourceType());
    }

    @Test
    void getInputStream_returnsNonNull() throws IOException {
        try (var stream = adapter.getInputStream()) {
            assertNotNull(stream);
        }
    }

    @Test
    void getByteSize_positive() {
        assertTrue(adapter.getByteSize() > 0);
    }

    // =====================================================================
    // equals / hashCode / toString
    // =====================================================================

    @Test
    void equals_sameFile_isEqual() {
        var fileResource2 = LocalFileResource.createInstance(imageFile.toFile());
        var adapter2 = SimpleFileResourceIconFileAdapter.createInstance(fileResource2, CommonFileResourceType.OTHER);
        assertEquals(adapter, adapter2);
    }

    @Test
    void equals_differentFile_notEqual() throws IOException {
        Path other = tempDir.resolve("other.png");
        Files.write(other, new byte[] { 1, 2, 3 });
        var fileResource2 = LocalFileResource.createInstance(other.toFile());
        var adapter2 = SimpleFileResourceIconFileAdapter.createInstance(fileResource2, CommonFileResourceType.OTHER);
        assertNotEquals(adapter, adapter2);
    }

    @Test
    void hashCode_sameFile_equal() throws IOException {
        var fileResource2 = LocalFileResource.createInstance(imageFile.toFile());
        var adapter2 = SimpleFileResourceIconFileAdapter.createInstance(fileResource2, CommonFileResourceType.OTHER);
        assertEquals(adapter.hashCode(), adapter2.hashCode());
    }

    @Test
    void toString_notNull() {
        assertNotNull(adapter.toString());
    }

    @Test
    void toDebugString_notNull() {
        assertNotNull(adapter.toDebugString());
    }

}

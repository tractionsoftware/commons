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
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LocalFileResourceTest {

    // ---------------------------------------------------------------------------
    // Helper
    // ---------------------------------------------------------------------------

    private static SimpleMutableFileMetadata metadataFor(File file) {
        SimpleMutableFileMetadata m = SimpleMutableFileMetadata.createFromFileName(file.getName());
        m.setURI(file.toURI());
        return m;
    }

    // ---------------------------------------------------------------------------
    // createInstance — null guards
    // ---------------------------------------------------------------------------

    @Test
    void createInstance_nullFile_throws() {
        assertThrows(NullPointerException.class, () -> LocalFileResource.createInstance(null, null));
    }

    @Test
    void createInstance_oneArg_createsMetadataFromFile(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("test.txt").toFile();
        Files.writeString(file.toPath(), "hello");
        LocalFileResource r = LocalFileResource.createInstance(file);
        assertEquals("test.txt", r.getFilename());
        assertEquals(file.toURI(), r.getURI());
        assertTrue(r.isValid());
    }

    // ---------------------------------------------------------------------------
    // isValid
    // ---------------------------------------------------------------------------

    @Test
    void isValid_existingReadableFile_returnsTrue(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("hello.txt").toFile();
        Files.writeString(file.toPath(), "hello");
        LocalFileResource r = LocalFileResource.createInstance(file, metadataFor(file));
        assertTrue(r.isValid());
    }

    @Test
    void isValid_nonExistentFile_returnsFalse(@TempDir Path tempDir) {
        File missing = tempDir.resolve("missing.txt").toFile();
        LocalFileResource r = LocalFileResource.createInstance(missing, metadataFor(missing));
        assertFalse(r.isValid());
    }

    // ---------------------------------------------------------------------------
    // isDirectory
    // ---------------------------------------------------------------------------

    @Test
    void isDirectory_regularFile_returnsFalse(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("f.txt").toFile();
        Files.createFile(file.toPath());
        LocalFileResource r = LocalFileResource.createInstance(file, metadataFor(file));
        assertFalse(r.isDirectory());
    }

    @Test
    void isDirectory_directory_returnsTrue(@TempDir Path tempDir) {
        File dir = tempDir.toFile();
        LocalFileResource r = LocalFileResource.createInstance(dir, metadataFor(dir));
        assertTrue(r.isDirectory());
    }

    // ---------------------------------------------------------------------------
    // getFilename
    // ---------------------------------------------------------------------------

    @Test
    void getFilename_returnsMetadataFilename(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("report.pdf").toFile();
        Files.createFile(file.toPath());
        SimpleMutableFileMetadata m = metadataFor(file);
        m.setFilename("my-report.pdf");
        LocalFileResource r = LocalFileResource.createInstance(file, m);
        assertEquals("my-report.pdf", r.getFilename());
    }

    // ---------------------------------------------------------------------------
    // getByteSize
    // ---------------------------------------------------------------------------

    @Test
    void getByteSize_returnsFileLength(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("data.txt").toFile();
        byte[] content = "hello world".getBytes(StandardCharsets.UTF_8);
        Files.write(file.toPath(), content);
        LocalFileResource r = LocalFileResource.createInstance(file, metadataFor(file));
        assertEquals(content.length, r.getByteSize());
    }

    @Test
    void getByteSize_emptyFile_returnsZero(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("empty.txt").toFile();
        Files.createFile(file.toPath());
        LocalFileResource r = LocalFileResource.createInstance(file, metadataFor(file));
        assertEquals(0L, r.getByteSize());
    }

    // ---------------------------------------------------------------------------
    // getLastModified
    // ---------------------------------------------------------------------------

    @Test
    void getLastModified_returnsRecentDate(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("ts.txt").toFile();
        Files.createFile(file.toPath());
        LocalFileResource r = LocalFileResource.createInstance(file, metadataFor(file));
        long now = System.currentTimeMillis();
        assertTrue(r.getLastModified().getTime() <= now);
        assertTrue(r.getLastModified().getTime() > now - 60_000);
    }

    // ---------------------------------------------------------------------------
    // getInputStream
    // ---------------------------------------------------------------------------

    @Test
    void getInputStream_readsContent(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("content.txt").toFile();
        Files.writeString(file.toPath(), "test content");
        LocalFileResource r = LocalFileResource.createInstance(file, metadataFor(file));
        try (InputStream in = r.getInputStream()) {
            String read = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals("test content", read);
        }
    }

    // ---------------------------------------------------------------------------
    // getURI / getContentType (from metadata)
    // ---------------------------------------------------------------------------

    @Test
    void getURI_reflectsMetadataURI(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("doc.txt").toFile();
        Files.createFile(file.toPath());
        SimpleMutableFileMetadata m = metadataFor(file);
        LocalFileResource r = LocalFileResource.createInstance(file, m);
        assertEquals(file.toURI(), r.getURI());
    }

    @Test
    void getContentType_reflectsMetadataContentType(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("page.html").toFile();
        Files.createFile(file.toPath());
        SimpleMutableFileMetadata m = SimpleMutableFileMetadata.createFromFileNameAndContentType(
            file.getName(), "text/html");
        m.setURI(file.toURI());
        LocalFileResource r = LocalFileResource.createInstance(file, m);
        assertEquals("text/html", r.getContentType());
    }

}

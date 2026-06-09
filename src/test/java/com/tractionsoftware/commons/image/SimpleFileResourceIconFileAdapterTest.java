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
import com.tractionsoftware.commons.io.FileResource;
import com.tractionsoftware.commons.io.LocalFileResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public final class SimpleFileResourceIconFileAdapterTest {

    @Test
    void createInstance_nullFile_throwsNPE() {
        assertThrows(
            NullPointerException.class, () ->
                SimpleFileResourceIconFileAdapter.createInstance(null, CommonFileResourceType.ICON_FILE_TYPE)
        );
    }

    @Test
    void createInstance_nullType_throwsNPE(@TempDir Path tmpDir) throws IOException {
        Path p = tmpDir.resolve("icon.png");
        Files.write(p, new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47 }); // PNG magic
        var file = LocalFileResource.createInstance(p.toFile());
        assertThrows(
            NullPointerException.class, () ->
                SimpleFileResourceIconFileAdapter.createInstance(file, null)
        );
    }

    @Test
    void getFilename_matchesUnderlyingFile(@TempDir Path tmpDir) throws IOException {
        Path p = tmpDir.resolve("myicon.png");
        Files.writeString(p, "fake png");
        var file = LocalFileResource.createInstance(p.toFile());
        var adapter = SimpleFileResourceIconFileAdapter.createInstance(file, CommonFileResourceType.ICON_FILE_TYPE);
        assertEquals("myicon.png", adapter.getFilename());
    }

    @Test
    void getImageResourceType_returnsGivenType(@TempDir Path tmpDir) throws IOException {
        Path p = tmpDir.resolve("icon.png");
        Files.writeString(p, "fake png");
        var file = LocalFileResource.createInstance(p.toFile());
        var adapter = SimpleFileResourceIconFileAdapter.createInstance(file, CommonFileResourceType.LOGO);
        assertEquals(CommonFileResourceType.LOGO, adapter.getImageResourceType());
    }

    @Test
    void getType_delegatesToFile(@TempDir Path tmpDir) throws IOException {
        Path p = tmpDir.resolve("icon.png");
        Files.writeString(p, "fake png");
        var file = LocalFileResource.createInstance(p.toFile());
        var adapter = SimpleFileResourceIconFileAdapter.createInstance(file, CommonFileResourceType.CONTENT);
        // getType() delegates to file.getType() — just verify it returns non-null and doesn't throw
        assertNotNull(adapter.getType());
    }

    @Test
    void getByteSize_matchesFileSize(@TempDir Path tmpDir) throws IOException {
        byte[] content = "test content for size".getBytes(StandardCharsets.UTF_8);
        Path p = tmpDir.resolve("sized.txt");
        Files.write(p, content);
        var file = LocalFileResource.createInstance(p.toFile());
        var adapter = SimpleFileResourceIconFileAdapter.createInstance(file, CommonFileResourceType.CONTENT);
        assertEquals(content.length, adapter.getByteSize());
    }

    @Test
    void getLastModified_notNull(@TempDir Path tmpDir) throws IOException {
        Path p = tmpDir.resolve("icon.png");
        Files.writeString(p, "fake");
        var file = LocalFileResource.createInstance(p.toFile());
        var adapter = SimpleFileResourceIconFileAdapter.createInstance(file, CommonFileResourceType.CONTENT);
        assertNotNull(adapter.getLastModified());
    }

    @Test
    void getPath_notBlank(@TempDir Path tmpDir) throws IOException {
        Path p = tmpDir.resolve("icon.png");
        Files.writeString(p, "fake");
        var file = LocalFileResource.createInstance(p.toFile());
        var adapter = SimpleFileResourceIconFileAdapter.createInstance(file, CommonFileResourceType.CONTENT);
        assertFalse(adapter.getPath().isBlank());
    }

    @Test
    void getURI_notNull(@TempDir Path tmpDir) throws IOException {
        Path p = tmpDir.resolve("icon.png");
        Files.writeString(p, "fake");
        var file = LocalFileResource.createInstance(p.toFile());
        var adapter = SimpleFileResourceIconFileAdapter.createInstance(file, CommonFileResourceType.CONTENT);
        assertNotNull(adapter.getURI());
    }

    @Test
    void toString_containsClassName(@TempDir Path tmpDir) throws IOException {
        Path p = tmpDir.resolve("icon.png");
        Files.writeString(p, "fake");
        var file = LocalFileResource.createInstance(p.toFile());
        var adapter = SimpleFileResourceIconFileAdapter.createInstance(file, CommonFileResourceType.CONTENT);
        assertTrue(adapter.toString().contains("SimpleFileResourceIconFileAdapter"), adapter.toString());
    }

    @Test
    void toDebugString_notNullOrBlank(@TempDir Path tmpDir) throws IOException {
        Path p = tmpDir.resolve("icon.png");
        Files.writeString(p, "fake");
        var file = LocalFileResource.createInstance(p.toFile());
        var adapter = SimpleFileResourceIconFileAdapter.createInstance(file, CommonFileResourceType.CONTENT);
        String s = adapter.toDebugString();
        assertNotNull(s);
        assertFalse(s.isBlank());
    }

    @Test
    void equals_sameFile_true(@TempDir Path tmpDir) throws IOException {
        Path p = tmpDir.resolve("icon.png");
        Files.writeString(p, "fake");
        var file = LocalFileResource.createInstance(p.toFile());
        var a = SimpleFileResourceIconFileAdapter.createInstance(file, CommonFileResourceType.CONTENT);
        var b = SimpleFileResourceIconFileAdapter.createInstance(file, CommonFileResourceType.LOGO);
        // equals is based on file equality
        assertEquals(a, b);
    }

    @Test
    void equals_differentFile_false(@TempDir Path tmpDir) throws IOException {
        Path p1 = tmpDir.resolve("icon1.png");
        Path p2 = tmpDir.resolve("icon2.png");
        Files.writeString(p1, "fake1");
        Files.writeString(p2, "fake2");
        var a = SimpleFileResourceIconFileAdapter.createInstance(
            LocalFileResource.createInstance(p1.toFile()), CommonFileResourceType.CONTENT);
        var b = SimpleFileResourceIconFileAdapter.createInstance(
            LocalFileResource.createInstance(p2.toFile()), CommonFileResourceType.CONTENT);
        assertNotEquals(a, b);
    }

    @Test
    void hashCode_equalAdapters_equal(@TempDir Path tmpDir) throws IOException {
        Path p = tmpDir.resolve("icon.png");
        Files.writeString(p, "fake");
        var file = LocalFileResource.createInstance(p.toFile());
        var a = SimpleFileResourceIconFileAdapter.createInstance(file, CommonFileResourceType.CONTENT);
        var b = SimpleFileResourceIconFileAdapter.createInstance(file, CommonFileResourceType.LOGO);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void getInputStream_readsContent(@TempDir Path tmpDir) throws IOException {
        byte[] content = "icon file content".getBytes(StandardCharsets.UTF_8);
        Path p = tmpDir.resolve("icon.png");
        Files.write(p, content);
        var file = LocalFileResource.createInstance(p.toFile());
        var adapter = SimpleFileResourceIconFileAdapter.createInstance(file, CommonFileResourceType.CONTENT);
        try (var is = adapter.getInputStream()) {
            byte[] read = is.readAllBytes();
            assertArrayEquals(content, read);
        }
    }

    @Test
    void isValid_existingFile_true() throws URISyntaxException {
        File file = ImageUtilTest.imageFile("icon.png");
        FileResource fileRes = LocalFileResource.createInstance(file);
        var adapter = SimpleFileResourceIconFileAdapter.createInstance(fileRes, CommonFileResourceType.ICON_OTHER);
        assertTrue(adapter.isValid());
    }

}

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

package com.tractionsoftware.commons.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

public final class CompressionUtilTest {

    // =====================================================================
    // gzipBytes
    // =====================================================================

    @Test
    void gzipBytes_byteArray_roundTrip() throws IOException {
        byte[] input = "Hello, GZIP world!".getBytes(StandardCharsets.UTF_8);
        byte[] compressed = CompressionUtil.gzipBytes(input);
        assertNotNull(compressed);
        assertTrue(compressed.length > 0);
        // Decompress and verify
        byte[] decompressed = ungzip(compressed);
        assertArrayEquals(input, decompressed);
    }

    @Test
    void gzipBytes_inputStream_roundTrip() throws IOException {
        byte[] input = "Stream-based GZIP test content.".getBytes(StandardCharsets.UTF_8);
        byte[] compressed = CompressionUtil.gzipBytes(new ByteArrayInputStream(input));
        assertNotNull(compressed);
        assertArrayEquals(input, ungzip(compressed));
    }

    @Test
    void gzipBytes_emptyInput_producesValidGzip() throws IOException {
        byte[] compressed = CompressionUtil.gzipBytes(new byte[0]);
        assertNotNull(compressed);
        byte[] decompressed = ungzip(compressed);
        assertEquals(0, decompressed.length);
    }

    // =====================================================================
    // zip / unzip
    // =====================================================================

    @Test
    void zip_singleFile_canBeUnzipped(@TempDir Path tmpDir) throws IOException {
        // Create a test file
        Path src = tmpDir.resolve("hello.txt");
        Files.writeString(src, "Hello ZIP");

        // Zip it
        Path zipPath = tmpDir.resolve("output.zip");
        CompressionUtil.zip(List.of(src.toFile()), zipPath.toFile());

        assertTrue(Files.exists(zipPath));
        assertTrue(Files.size(zipPath) > 0);

        // Verify the zip entry
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry = zis.getNextEntry();
            assertNotNull(entry);
            assertEquals("hello.txt", entry.getName());
            String content = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals("Hello ZIP", content);
        }
    }

    @Test
    void zip_multipleFiles_allPresent(@TempDir Path tmpDir) throws IOException {
        Path a = tmpDir.resolve("a.txt");
        Path b = tmpDir.resolve("b.txt");
        Files.writeString(a, "AAA");
        Files.writeString(b, "BBB");

        Path zipPath = tmpDir.resolve("multi.zip");
        CompressionUtil.zip(List.of(a.toFile(), b.toFile()), zipPath.toFile());

        int count = 0;
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            while (zis.getNextEntry() != null) {
                count++;
            }
        }
        assertEquals(2, count);
    }

    @Test
    void unzip_extractsFiles(@TempDir Path tmpDir) throws IOException {
        // First zip a file
        Path src = tmpDir.resolve("original.txt");
        Files.writeString(src, "unzip me");
        Path zipPath = tmpDir.resolve("test.zip");
        CompressionUtil.zip(List.of(src.toFile()), zipPath.toFile());

        // Now unzip to a different dir
        Path outDir = tmpDir.resolve("extracted");
        Files.createDirectories(outDir);
        CompressionUtil.unzip(zipPath.toFile(), outDir.toFile());

        Path extracted = outDir.resolve("original.txt");
        assertTrue(Files.exists(extracted));
        assertEquals("unzip me", Files.readString(extracted));
    }

    // =====================================================================
    // compress / decompress
    // =====================================================================

    @Test
    void compress_null_returnsNull() {
        assertNull(CompressionUtil.compress(null));
    }

    @Test
    void compress_nonExistentFile_returnsNull(@TempDir Path tmpDir) {
        File f = tmpDir.resolve("does_not_exist.txt").toFile();
        assertNull(CompressionUtil.compress(f));
    }

    @Test
    void compress_alreadyZip_returnsOriginal(@TempDir Path tmpDir) throws IOException {
        Path zipPath = tmpDir.resolve("already.zip");
        Files.writeString(zipPath, "fake zip content");
        File result = CompressionUtil.compress(zipPath.toFile());
        assertSame(zipPath.toFile().getAbsolutePath(), result.getAbsolutePath());
    }

    @Test
    void compress_normalFile_producesZipAndDeletesOriginal(@TempDir Path tmpDir) throws IOException {
        Path src = tmpDir.resolve("compress_me.txt");
        Files.writeString(src, "compress this content please");
        File result = CompressionUtil.compress(src.toFile());
        assertNotNull(result);
        assertTrue(result.getName().endsWith(".zip"));
        assertTrue(result.exists());
        assertTrue(result.length() > 0);
        // Original should be deleted
        assertFalse(src.toFile().exists());
    }

    @Test
    void decompress_null_returnsNull() {
        assertNull(CompressionUtil.decompress(null));
    }

    @Test
    void decompress_nonExistentFile_returnsNull(@TempDir Path tmpDir) {
        assertNull(CompressionUtil.decompress(tmpDir.resolve("no_such_file.zip").toFile()));
    }

    @Test
    void decompress_nonZipFile_returnsOriginal(@TempDir Path tmpDir) throws IOException {
        Path p = tmpDir.resolve("plain.txt");
        Files.writeString(p, "not a zip");
        File result = CompressionUtil.decompress(p.toFile());
        assertEquals(p.toFile(), result);
    }

    // =====================================================================
    // ZipEntryNameProvider constants
    // =====================================================================

    @Test
    void fileNameZipEntryNamer_returnsFileName() {
        File f = new File("/some/path/to/file.txt");
        assertEquals("file.txt", CompressionUtil.FILE_NAME_ZIP_ENTRY_NAMER.getName(f));
    }

    @Test
    void filePathZipEntryNamer_returnsFilePath() {
        File f = new File("/some/path/to/file.txt");
        assertTrue(CompressionUtil.FILE_PATH_ZIP_ENTRY_NAMER.getName(f).contains("file.txt"));
    }

    @Test
    void getRelativePathZipEntryNamer_returnsRelativePath(@TempDir Path tmpDir) throws IOException {
        Path base = tmpDir;
        Path child = base.resolve("sub").resolve("file.txt");
        Files.createDirectories(child.getParent());
        Files.writeString(child, "x");
        var namer = CompressionUtil.getRelativePathZipEntryNamer(base.toFile());
        String name = namer.getName(child.toFile());
        assertTrue(name.contains("file.txt"), name);
        assertFalse(name.startsWith("/"), name);
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private static byte[] ungzip(byte[] compressed) throws IOException {
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            gis.transferTo(out);
            return out.toByteArray();
        }
    }

}

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
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

public final class CompressionUtilTest {

    @TempDir
    Path tempDir;

    // =====================================================================
    // gzipBytes
    // =====================================================================

    @Test
    void gzipBytes_byteArray_canBeDecompressed() throws IOException {
        byte[] input = "Hello, world!".getBytes(StandardCharsets.UTF_8);
        byte[] gzipped = CompressionUtil.gzipBytes(input);
        assertNotNull(gzipped);
        assertTrue(gzipped.length > 0);

        // Decompress and verify round-trip
        try (GZIPInputStream gzin = new GZIPInputStream(new ByteArrayInputStream(gzipped))) {
            byte[] recovered = gzin.readAllBytes();
            assertArrayEquals(input, recovered);
        }
    }

    @Test
    void gzipBytes_inputStream_canBeDecompressed() throws IOException {
        byte[] input = "Compressed stream test".getBytes(StandardCharsets.UTF_8);
        byte[] gzipped = CompressionUtil.gzipBytes(new ByteArrayInputStream(input));
        assertNotNull(gzipped);

        try (GZIPInputStream gzin = new GZIPInputStream(new ByteArrayInputStream(gzipped))) {
            assertArrayEquals(input, gzin.readAllBytes());
        }
    }

    @Test
    void gzipBytes_largeInput_roundTrips() throws IOException {
        byte[] input = new byte[65536];
        Arrays.fill(input, (byte) 'X');
        byte[] gzipped = CompressionUtil.gzipBytes(input);
        assertNotNull(gzipped);
        try (GZIPInputStream gzin = new GZIPInputStream(new ByteArrayInputStream(gzipped))) {
            assertArrayEquals(input, gzin.readAllBytes());
        }
    }

    // =====================================================================
    // zip / unzip
    // =====================================================================

    @Test
    void zip_createAndUnzip_roundTrips() throws IOException {
        // Create two source files
        Path src1 = tempDir.resolve("file1.txt");
        Path src2 = tempDir.resolve("file2.txt");
        Files.writeString(src1, "content one");
        Files.writeString(src2, "content two");

        Path zipFile = tempDir.resolve("archive.zip");
        CompressionUtil.zip(List.of(src1.toFile(), src2.toFile()), zipFile.toFile());

        assertTrue(Files.exists(zipFile));
        assertTrue(Files.size(zipFile) > 0);

        // Unzip to a separate directory
        Path unzipDir = tempDir.resolve("unzipped");
        Files.createDirectories(unzipDir);
        CompressionUtil.unzip(zipFile.toFile(), unzipDir.toFile());

        assertEquals("content one", Files.readString(unzipDir.resolve("file1.txt")));
        assertEquals("content two", Files.readString(unzipDir.resolve("file2.txt")));
    }

    @Test
    void zip_entryNames_matchFileNames() throws IOException {
        Path src = tempDir.resolve("hello.txt");
        Files.writeString(src, "data");
        Path zipFile = tempDir.resolve("test.zip");
        CompressionUtil.zip(List.of(src.toFile()), zipFile.toFile());

        try (ZipInputStream zin = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry = zin.getNextEntry();
            assertNotNull(entry);
            assertEquals("hello.txt", entry.getName());
        }
    }

    // =====================================================================
    // compress / decompress
    // =====================================================================

    @Test
    void compress_nonZipFile_createsZip() throws IOException {
        Path original = tempDir.resolve("data.txt");
        Files.writeString(original, "some data to compress");

        File result = CompressionUtil.compress(original.toFile());
        assertNotNull(result);
        assertTrue(result.getName().endsWith(".zip"), result.getName());
        assertTrue(result.exists());
        // original should be deleted
        assertFalse(original.toFile().exists());
    }

    @Test
    void compress_null_returnsNull() {
        assertNull(CompressionUtil.compress(null));
    }

    @Test
    void compress_nonExistentFile_returnsNull() {
        File nonExistent = tempDir.resolve("does_not_exist.txt").toFile();
        assertNull(CompressionUtil.compress(nonExistent));
    }

    @Test
    void compress_alreadyZipFile_returnsSame() throws IOException {
        Path zipFile = tempDir.resolve("already.zip");
        Files.writeString(zipFile, "fake zip content");
        File result = CompressionUtil.compress(zipFile.toFile());
        assertNotNull(result);
        assertEquals(zipFile.toFile(), result);
    }

    @Test
    void decompress_nonZipFile_returnsSame() throws IOException {
        Path textFile = tempDir.resolve("text.txt");
        Files.writeString(textFile, "hello");
        File result = CompressionUtil.decompress(textFile.toFile());
        assertEquals(textFile.toFile(), result);
    }

    @Test
    void decompress_null_returnsNull() {
        assertNull(CompressionUtil.decompress(null));
    }

    @Test
    void decompress_nonExistent_returnsNull() {
        File nonExistent = tempDir.resolve("missing.zip").toFile();
        assertNull(CompressionUtil.decompress(nonExistent));
    }

    // =====================================================================
    // getRelativePathZipEntryNamer
    // =====================================================================

    @Test
    void getRelativePathZipEntryNamer_returnsRelativeName() {
        File baseDir = tempDir.toFile();
        File subFile = new File(baseDir, "subdir/file.txt");
        CompressionUtil.ZipEntryNameProvider namer = CompressionUtil.getRelativePathZipEntryNamer(baseDir);
        assertEquals("subdir/file.txt", namer.getName(subFile));
    }

    // =====================================================================
    // FILE_NAME_ZIP_ENTRY_NAMER / FILE_PATH_ZIP_ENTRY_NAMER
    // =====================================================================

    @Test
    void fileNameNamer_returnsFileName() {
        File f = new File("/some/path/file.txt");
        assertEquals("file.txt", CompressionUtil.FILE_NAME_ZIP_ENTRY_NAMER.getName(f));
    }

    @Test
    void filePathNamer_returnsFilePath() {
        File f = new File("/some/path/file.txt");
        assertEquals(f.getPath(), CompressionUtil.FILE_PATH_ZIP_ENTRY_NAMER.getName(f));
    }

}

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
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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

    // =====================================================================
    // decompress - real success path
    // =====================================================================

    @Test
    void decompress_zipFile_extractsContentAndDeletesOriginal() throws IOException {
        // Use a file with no extension so the compressed zip's single entry name (after stripping
        // ".zip") exactly matches the destination path that decompress() expects.
        Path original = tempDir.resolve("payload");
        Files.writeString(original, "payload content");

        File zipFile = CompressionUtil.compress(original.toFile());
        assertNotNull(zipFile);
        assertTrue(zipFile.getName().endsWith(".zip"));

        File result = CompressionUtil.decompress(zipFile);
        assertNotNull(result);
        assertEquals("payload", result.getName());
        assertTrue(result.exists());
        assertEquals("payload content", Files.readString(result.toPath()));

        // the .zip file itself should have been deleted after successful decompression
        assertFalse(zipFile.exists());
    }

    // =====================================================================
    // compress - overwriting an existing destination
    // =====================================================================

    @Test
    void compress_destinationAlreadyExists_overwritesIt() throws IOException {
        Path original = tempDir.resolve("overwrite-me.txt");
        Files.writeString(original, "new content");

        Path preexistingZip = tempDir.resolve("overwrite-me.txt.zip");
        Files.writeString(preexistingZip, "stale placeholder content that will be overwritten");

        File result = CompressionUtil.compress(original.toFile());
        assertNotNull(result);
        assertEquals(preexistingZip.toFile(), result);

        try (ZipInputStream zin = new ZipInputStream(Files.newInputStream(preexistingZip))) {
            ZipEntry entry = zin.getNextEntry();
            assertNotNull(entry);
            assertEquals("overwrite-me.txt", entry.getName());
            assertEquals("new content", new String(zin.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    // =====================================================================
    // unzip - directory entries and path-traversal protection
    // =====================================================================

    @Test
    void unzip_directoryEntry_createsDirectory() throws IOException {
        Path zipPath = tempDir.resolve("withdir.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            ZipEntry dirEntry = new ZipEntry("subdir/");
            zos.putNextEntry(dirEntry);
            zos.closeEntry();
        }

        Path destDir = tempDir.resolve("unzipped-dir-test");
        Files.createDirectories(destDir);
        CompressionUtil.unzip(zipPath.toFile(), destDir.toFile());

        assertTrue(Files.isDirectory(destDir.resolve("subdir")));
    }

    @Test
    void unzip_entryOutsideTargetDirectory_skipped() throws IOException {
        Path zipPath = tempDir.resolve("traversal.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            ZipEntry entry = new ZipEntry("../escaped.txt");
            zos.putNextEntry(entry);
            zos.write("should not escape".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        Path destDir = tempDir.resolve("unzip-target");
        Files.createDirectories(destDir);
        CompressionUtil.unzip(zipPath.toFile(), destDir.toFile());

        // the entry should have been skipped rather than written outside destDir
        assertFalse(Files.exists(destDir.resolveSibling("escaped.txt")));
        try (var stream = Files.list(destDir)) {
            assertEquals(0, stream.count());
        }
    }

    // =====================================================================
    // zip(Collection, ZipOutputStream) - direct 2-arg overload
    // =====================================================================

    @Test
    void zip_toZipOutputStream_directOverload_writesEntries() throws IOException {
        Path src = tempDir.resolve("direct.txt");
        Files.writeString(src, "direct content");

        Path zipPath = tempDir.resolve("direct.zip");
        try (OutputStream fileOut = Files.newOutputStream(zipPath);
             ZipOutputStream zos = new ZipOutputStream(fileOut)) {
            // zip(Collection, ZipOutputStream) does not call finish()/flush() itself; relies on
            // ZipOutputStream auto-finishing when closed.
            CompressionUtil.zip(List.of(src.toFile()), zos);
        }

        try (ZipInputStream zin = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry = zin.getNextEntry();
            assertNotNull(entry);
            assertEquals("direct.txt", entry.getName());
            assertEquals("direct content", new String(zin.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    // =====================================================================
    // jar family
    // =====================================================================

    @Test
    void jar_createAndRead_roundTrips() throws IOException {
        Path src = tempDir.resolve("jarred.txt");
        Files.writeString(src, "jar content");

        Path jarFile = tempDir.resolve("archive.jar");
        CompressionUtil.jar(List.of(src.toFile()), jarFile.toFile());

        assertTrue(Files.exists(jarFile));
        try (JarInputStream jin = new JarInputStream(Files.newInputStream(jarFile))) {
            JarEntry entry = jin.getNextJarEntry();
            assertNotNull(entry);
            assertEquals("jarred.txt", entry.getName());
            assertEquals("jar content", new String(jin.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void jar_withManifest_includesManifestEntry() throws IOException {
        Path src = tempDir.resolve("withmanifest.txt");
        Files.writeString(src, "data");

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "com.example.Main");

        Path jarFile = tempDir.resolve("withmanifest.jar");
        CompressionUtil.jar(List.of(src.toFile()), jarFile.toFile(), manifest);

        try (JarInputStream jin = new JarInputStream(Files.newInputStream(jarFile))) {
            Manifest readManifest = jin.getManifest();
            assertNotNull(readManifest);
            assertEquals("com.example.Main", readManifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS));
        }
    }

    @Test
    void jar_withNamer_usesCustomEntryNames() throws IOException {
        Path src = tempDir.resolve("custom.txt");
        Files.writeString(src, "data");

        Path jarFile = tempDir.resolve("customnamed.jar");
        CompressionUtil.ZipEntryNameProvider namer = file -> "renamed-" + file.getName();
        CompressionUtil.jar(List.of(src.toFile()), jarFile.toFile(), namer);

        try (JarInputStream jin = new JarInputStream(Files.newInputStream(jarFile))) {
            JarEntry entry = jin.getNextJarEntry();
            assertNotNull(entry);
            assertEquals("renamed-custom.txt", entry.getName());
        }
    }

    @Test
    void jar_toJarOutputStream_2argOverload_writesEntries() throws IOException {
        Path src = tempDir.resolve("direct.txt");
        Files.writeString(src, "jar direct content");

        Path jarPath = tempDir.resolve("direct.jar");
        try (JarOutputStream jos = CompressionUtil.getJarOutputStream(jarPath.toFile(), null)) {
            CompressionUtil.jar(List.of(src.toFile()), jos);
            jos.finish();
            jos.flush();
        }

        try (JarInputStream jin = new JarInputStream(Files.newInputStream(jarPath))) {
            JarEntry entry = jin.getNextJarEntry();
            assertNotNull(entry);
            assertEquals("direct.txt", entry.getName());
        }
    }

    // =====================================================================
    // addJarEntry
    // =====================================================================

    @Test
    void addJarEntry_fileOverload_addsEntryNamedAfterFile() throws IOException {
        Path src = tempDir.resolve("entrysource.txt");
        Files.writeString(src, "entry content");

        Path jarPath = tempDir.resolve("manual.jar");
        try (JarOutputStream jos = CompressionUtil.getJarOutputStream(jarPath.toFile(), null)) {
            CompressionUtil.addJarEntry(jos, src.toFile());
            jos.finish();
            jos.flush();
        }

        try (JarInputStream jin = new JarInputStream(Files.newInputStream(jarPath))) {
            JarEntry entry = jin.getNextJarEntry();
            assertNotNull(entry);
            assertEquals("entrysource.txt", entry.getName());
            assertEquals("entry content", new String(jin.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void addJarEntry_entryAndInputStreamOverload_addsGivenEntry() throws IOException {
        Path jarPath = tempDir.resolve("manual-entry.jar");
        byte[] content = "raw entry bytes".getBytes(StandardCharsets.UTF_8);
        try (JarOutputStream jos = CompressionUtil.getJarOutputStream(jarPath.toFile(), null)) {
            CompressionUtil.addJarEntry(jos, new JarEntry("custom-name.bin"), new ByteArrayInputStream(content));
            jos.finish();
            jos.flush();
        }

        try (JarInputStream jin = new JarInputStream(Files.newInputStream(jarPath))) {
            JarEntry entry = jin.getNextJarEntry();
            assertNotNull(entry);
            assertEquals("custom-name.bin", entry.getName());
            assertArrayEquals(content, jin.readAllBytes());
        }
    }

}

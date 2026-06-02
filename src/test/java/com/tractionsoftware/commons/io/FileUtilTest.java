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
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public final class FileUtilTest {

    static final File getCurrentDirectory() {
        return new File(".");
    }

    @Test
    public void testIsOrIsDescendant1() {
        File workingDir = getCurrentDirectory();
        assertTrue(FileUtil.isOrIsDescendant(workingDir, workingDir));
    }

    @Test
    public void testIsOrIsDescendant2() {
        File workingDir = getCurrentDirectory();
        try {
            File workingDirCanonical = workingDir.getCanonicalFile();
            assertTrue(FileUtil.isOrIsDescendant(workingDir, workingDirCanonical));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void testIsOrIsDescendant3() {
        File workingDir = getCurrentDirectory();
        File workingDirChild = new File(workingDir, "foo.bar");
        assertTrue(FileUtil.isOrIsDescendant(workingDir, workingDirChild));
    }

    @Test
    public void testIsOrIsDescendant4() {
        File workingDir = getCurrentDirectory();
        File workingDirSubDirChild = new File(new File(workingDir, "foo"), "bar.baz");
        assertTrue(FileUtil.isOrIsDescendant(workingDir, workingDirSubDirChild));
    }

    @Test
    public void testIsOrIsDescendant5() {
        File workingDir = getCurrentDirectory();
        File systemRoot = new File("/");
        assertFalse(FileUtil.isOrIsDescendant(workingDir, systemRoot));
    }

    @Test
    public void testIsOrIsDescendant6() {
        try {
            File workingDirCanonical = getCurrentDirectory().getCanonicalFile();
            String workingDirCanonicalPath = workingDirCanonical.getPath();
            File other;
            if (workingDirCanonicalPath.endsWith("/")) {
                other = new File(workingDirCanonicalPath.substring(0, workingDirCanonicalPath.length() - 1));
            }
            else {
                other = new File(workingDirCanonicalPath + "/");
            }
            assertTrue(FileUtil.isOrIsDescendant(workingDirCanonical, other));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void test_getRelativePathBothNull() {
        assertNull(FileUtil.getRelativePath(null, null));
    }

    @Test
    public void test_getRelativePathNullRoot() {
        String path = "/foo/bar/baz.txt";
        assertEquals(path, FileUtil.getRelativePath(null, new File(path)));
    }

    @Test
    public void test_getRelativePathDisjoint1() {
        String path = "/foo/bar/baz.txt";
        assertEquals(path, FileUtil.getRelativePath(new File("/gax/rax/max/"), new File(path)));
    }

    @Test
    public void test_getRelativePathDisjoint2() {
        String path = "/foo/bar/baz.txt";
        assertEquals(path, FileUtil.getRelativePath(new File("/foo/gax/"), new File(path)));
    }

    @Test
    public void test_getRelativePathMatch1() {
        String path = "/foo/bar/baz.txt";
        assertEquals("baz.txt", FileUtil.getRelativePath(new File("/foo/bar/"), new File(path)));
    }

    @Test
    public void test_getRelativePathMatch2() {
        String path = "/foo/bar/baz.txt";
        assertEquals("baz.txt", FileUtil.getRelativePath(new File("/foo/bar/"), new File(path)));
    }

    @Test
    public void test_getRelativePathMatch3() {
        String path = "/Users/ajm/servlet/config/user/directories/ad001.properties";
        assertEquals(
            "config/user/directories/ad001.properties",
            FileUtil.getRelativePath(new File("/Users/ajm/servlet/"), new File(path))
        );
    }

    // -------------------------------------------------------------------------
    // isValidFileForRead / isValidDirectoryForRead
    // -------------------------------------------------------------------------

    @Test
    public void isValidFileForRead_existingFile_returnsTrue(@TempDir Path tempDir) throws IOException {
        File f = tempDir.resolve("test.txt").toFile();
        f.createNewFile();
        assertTrue(FileUtil.isValidFileForRead(f));
    }

    @Test
    public void isValidFileForRead_directory_returnsFalse(@TempDir Path tempDir) {
        assertFalse(FileUtil.isValidFileForRead(tempDir.toFile()));
    }

    @Test
    public void isValidFileForRead_nonExistent_returnsFalse(@TempDir Path tempDir) {
        assertFalse(FileUtil.isValidFileForRead(tempDir.resolve("nope.txt").toFile()));
    }

    @Test
    public void isValidDirectoryForRead_existingDir_returnsTrue(@TempDir Path tempDir) {
        assertTrue(FileUtil.isValidDirectoryForRead(tempDir.toFile()));
    }

    @Test
    public void isValidDirectoryForRead_file_returnsFalse(@TempDir Path tempDir) throws IOException {
        File f = tempDir.resolve("x.txt").toFile();
        f.createNewFile();
        assertFalse(FileUtil.isValidDirectoryForRead(f));
    }

    @Test
    public void isValidDirectoryForRead_nonExistent_returnsFalse(@TempDir Path tempDir) {
        assertFalse(FileUtil.isValidDirectoryForRead(tempDir.resolve("nosuchdir").toFile()));
    }

    // -------------------------------------------------------------------------
    // existsAndReadable
    // -------------------------------------------------------------------------

    @Test
    public void existsAndReadable_existingFile_returnsTrue(@TempDir Path tempDir) throws IOException {
        File f = tempDir.resolve("exists.txt").toFile();
        f.createNewFile();
        assertTrue(FileUtil.existsAndReadable(f));
    }

    @Test
    public void existsAndReadable_nonExistent_returnsFalse(@TempDir Path tempDir) {
        assertFalse(FileUtil.existsAndReadable(tempDir.resolve("ghost.txt").toFile()));
    }

    // -------------------------------------------------------------------------
    // areSameFiles
    // -------------------------------------------------------------------------

    @Test
    public void areSameFiles_sameFile_returnsTrue(@TempDir Path tempDir) throws IOException {
        File f = tempDir.resolve("a.txt").toFile();
        f.createNewFile();
        assertTrue(FileUtil.areSameFiles(f, f));
    }

    @Test
    public void areSameFiles_differentFiles_returnsFalse(@TempDir Path tempDir) throws IOException {
        File a = tempDir.resolve("a.txt").toFile();
        File b = tempDir.resolve("b.txt").toFile();
        a.createNewFile();
        b.createNewFile();
        assertFalse(FileUtil.areSameFiles(a, b));
    }

    // -------------------------------------------------------------------------
    // checkAndCreateDir
    // -------------------------------------------------------------------------

    @Test
    public void checkAndCreateDir_newDir_createsIt(@TempDir Path tempDir) throws IOException {
        File newDir = tempDir.resolve("newsubdir").toFile();
        assertFalse(newDir.exists());
        FileUtil.checkAndCreateDir(newDir);
        assertTrue(newDir.isDirectory());
    }

    @Test
    public void checkAndCreateDir_existingDir_doesNotThrow(@TempDir Path tempDir) {
        assertDoesNotThrow(() -> FileUtil.checkAndCreateDir(tempDir.toFile()));
    }

}

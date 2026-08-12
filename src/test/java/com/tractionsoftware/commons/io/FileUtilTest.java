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

import com.tractionsoftware.commons.lang.Resource;
import jakarta.annotation.Nonnull;
import org.apache.commons.io.function.IOSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public final class FileUtilTest {

    @FunctionalInterface
    private static interface Setter {

        public boolean set(boolean value);

    }

    private static final void runWithSetValue(@Nonnull Setter setter, boolean newValue, @Nonnull Runnable test) {
        assumeTrue(setter.set(newValue));
        try {
            test.run();
        }
        finally {
            assumeTrue(setter.set(!newValue));
        }
    }

    static final void runWithSetUnreadable(@Nonnull File file, @Nonnull Runnable test) {
        // setReadable(false) is a no-op on some platforms/users (e.g. running as root); skip rather than
        // fail spuriously if the permission change didn't actually take effect.
        runWithSetValue(file::setReadable, false, test);
    }

    static final void runWithSetUnwritable(@Nonnull File file, @Nonnull Runnable test) {
        runWithSetValue(file::setWritable, false, test);
    }

    static final void withTemporaryFile(@Nonnull IOSupplier<File> tempFileCreator, @Nonnull Consumer<File> test)
        throws IOException {
        File tempFile = tempFileCreator.get();
        try {
            test.accept(tempFile);
        }
        finally {
            assumeTrue(tempFile.delete());
        }

    }

    @Test
    public void testIsOrIsDescendant1() {
        File workingDir = FileUtil.getCurrentDirectory();
        assertTrue(FileUtil.isOrIsDescendant(workingDir, workingDir));
    }

    @Test
    public void testIsOrIsDescendant2() {
        File workingDir = FileUtil.getCurrentDirectory();
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
        File workingDir = FileUtil.getCurrentDirectory();
        File workingDirChild = new File(workingDir, "foo.bar");
        assertTrue(FileUtil.isOrIsDescendant(workingDir, workingDirChild));
    }

    @Test
    public void testIsOrIsDescendant4() {
        File workingDir = FileUtil.getCurrentDirectory();
        File workingDirSubDirChild = new File(new File(workingDir, "foo"), "bar.baz");
        assertTrue(FileUtil.isOrIsDescendant(workingDir, workingDirSubDirChild));
    }

    @Test
    public void testIsOrIsDescendant5() {
        File workingDir = FileUtil.getCurrentDirectory();
        File systemRoot = new File("/");
        assertFalse(FileUtil.isOrIsDescendant(workingDir, systemRoot));
    }

    @Test
    public void testIsOrIsDescendant6() {
        try {
            File workingDirCanonical = FileUtil.getCurrentDirectory().getCanonicalFile();
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
    public void isDescendant_dotDotEscapeOneLevelUp_isNotDescendant(@TempDir Path tempDir) {
        File dir = tempDir.toFile();
        // resolves to a sibling of dir, not a descendant of it
        File escapedFile = new File(dir, "../escaped.txt");
        assertFalse(FileUtil.isDescendant(dir, escapedFile));
    }

    @Test
    public void isDescendant_dotDotEscapeFromNestedSubdirectory_isNotDescendant(@TempDir Path tempDir) {
        File uploadsDir = new File(tempDir.toFile(), "uploads");
        // mimics a malicious upload/zip-entry name like "../../etc/sensitive-config.properties"
        // intended to escape uploadsDir (and its parent) entirely
        File escapedFile = new File(uploadsDir, "../../etc/sensitive-config.properties");
        assertFalse(FileUtil.isDescendant(uploadsDir, escapedFile));
    }

    @Test
    public void isDescendant_manyDotDotSegments_simulatedMaliciousTraversal_isNotDescendant(@TempDir Path tempDir) {
        File webappUploadsDir = new File(tempDir.toFile(), "webapp/uploads");
        // simulates a classic zip-slip payload reaching for a sensitive system file far
        // outside the intended extraction directory
        File escapedFile = new File(webappUploadsDir, "../../../../../../../../etc/passwd");
        assertFalse(FileUtil.isDescendant(webappUploadsDir, escapedFile));
    }

    @Test
    public void isOrIsDescendant_dotDotEscapeOneLevelUp_isNotDescendant(@TempDir Path tempDir) {
        File dir = tempDir.toFile();
        File escapedFile = new File(dir, "../escaped.txt");
        assertFalse(FileUtil.isOrIsDescendant(dir, escapedFile));
    }

    @Test
    public void isOrIsDescendant_manyDotDotSegments_simulatedMaliciousTraversal_isNotDescendant(@TempDir Path tempDir) {
        File webappUploadsDir = new File(tempDir.toFile(), "webapp/uploads");
        File escapedFile = new File(webappUploadsDir, "../../../../../../../../etc/passwd");
        assertFalse(FileUtil.isOrIsDescendant(webappUploadsDir, escapedFile));
    }

    @Test
    public void isDescendantPath_dotDotEscapeOneLevelUp_isNotDescendant(@TempDir Path tempDir) {
        String dirPath = tempDir.toString();
        String escapedFilePath = dirPath + "/../escaped.txt";
        assertFalse(FileUtil.isDescendantPath(dirPath, escapedFilePath));
    }

    @Test
    public void isDescendantPath_manyDotDotSegments_simulatedMaliciousTraversal_isNotDescendant(@TempDir Path tempDir) {
        String dirPath = tempDir.toString() + "/webapp/uploads";
        String escapedFilePath = dirPath + "/../../../../../../../../etc/passwd";
        assertFalse(FileUtil.isDescendantPath(dirPath, escapedFilePath));
    }

    @Test
    public void isOrIsDescendantPath_dotDotEscapeOneLevelUp_isNotDescendant(@TempDir Path tempDir) {
        String dirPath = tempDir.toString();
        String escapedFilePath = dirPath + "/../escaped.txt";
        assertFalse(FileUtil.isOrIsDescendantPath(dirPath, escapedFilePath));
    }

    @Test
    public void isOrIsDescendantPath_manyDotDotSegments_simulatedMaliciousTraversal_isNotDescendant(@TempDir Path tempDir) {
        String dirPath = tempDir.toString() + "/webapp/uploads";
        String escapedFilePath = dirPath + "/../../../../../../../../etc/passwd";
        assertFalse(FileUtil.isOrIsDescendantPath(dirPath, escapedFilePath));
    }

    @Test
    public void isDescendant_dotDotThatResolvesBackInside_isDescendant(@TempDir Path tempDir) {
        File dir = new File(tempDir.toFile(), "dir");
        // resolves to dir/inside.txt, a genuine descendant of dir
        File insideFile = new File(dir, "sub/../inside.txt");
        assertTrue(FileUtil.isDescendant(dir, insideFile));
    }

    @Test
    public void isDescendant_dotDotElsewhereInPathThatStillResolvesInside_isDescendant(@TempDir Path tempDir) {
        File dir = new File(tempDir.toFile(), "dir");
        // resolves to dir/inside.txt; the literal (unresolved) path does NOT start with dir's
        // literal path, so this exercises the canonical-path fallback in isDescendant, which
        // already resolves ".." correctly
        File insideFile = new File(tempDir.toFile(), "otherdir/../dir/inside.txt");
        assertTrue(FileUtil.isDescendant(dir, insideFile));
    }

    @Test
    public void isDescendantForCanonicalFiles_givenTrulyAlreadyCanonicalFiles_correctlyRejectsEscape(@TempDir Path tempDir)
        throws IOException {
        File dir = new File(tempDir.toFile(), "dir");
        Files.createDirectories(dir.toPath());
        File escapedFile = new File(dir, "../escaped.txt");
        File dirCanonical = dir.getCanonicalFile();
        File escapedFileCanonical = escapedFile.getCanonicalFile();
        // once ".." has actually been resolved (as getCanonicalFile() does), the comparison
        // correctly identifies that escapedFile is NOT a descendant of dir
        assertFalse(FileUtil.isDescendantForAlreadyCanonicalFiles(dirCanonical, escapedFileCanonical));
    }

    @Test
    public void isDescendantForCanonicalFiles_fileWithRelativePathSegment_throwsIllegalArgumentException(@TempDir Path tempDir) {
        File dir = tempDir.toFile();
        File escapedFile = new File(dir, "../escaped.txt");
        assertThrows(
            IllegalArgumentException.class, () -> FileUtil.isDescendantForAlreadyCanonicalFiles(dir, escapedFile)
        );
    }

    @Test
    public void isOrIsDescendantForCanonicalFiles_givenTrulyAlreadyAlreadyCanonicalFiles_correctlyRejectsEscape(@TempDir Path tempDir)
        throws IOException {
        File dir = new File(tempDir.toFile(), "dir");
        Files.createDirectories(dir.toPath());
        File escapedFile = new File(dir, "../escaped.txt");
        File dirCanonical = dir.getCanonicalFile();
        File escapedFileCanonical = escapedFile.getCanonicalFile();
        // escapedFileCanonical is not equal to dirCanonical, and (once resolved) is not a
        // descendant of it either, so the "OrIs" equality short-circuit does not mask the
        // escape: this correctly returns false.
        assertFalse(FileUtil.isOrIsDescendantForAlreadyCanonicalFiles(dirCanonical, escapedFileCanonical));
    }

    @Test
    public void isOrIsDescendantForCanonicalFiles_fileWithRelativePathSegment_throwsIllegalArgumentException(@TempDir Path tempDir) {
        File dir = tempDir.toFile();
        File escapedFile = new File(dir, "../escaped.txt");
        assertThrows(
            IllegalArgumentException.class, () -> FileUtil.isOrIsDescendantForAlreadyCanonicalFiles(dir, escapedFile)
        );
    }

    @Test
    public void isDescendant_symlinkPathPassesThroughColonNamedDirectory_isDescendant(@TempDir Path tempDir)
        throws IOException {
        File baz = new File(tempDir.toFile(), "baz");
        Files.createDirectories(baz.toPath());
        File realFile = new File(baz, "realfile.txt");
        Files.writeString(realFile.toPath(), "hi");

        // "sub:dir" is a real, legal directory name on Unix-like systems - the colon is not a
        // separator here, it is just a character in the name.
        File subDirWithColon = new File(tempDir.toFile(), "sub:dir");
        Files.createDirectories(subDirWithColon.toPath());
        File link = new File(subDirWithColon, "link");
        Files.createSymbolicLink(link.toPath(), realFile.toPath());

        // link's lexical path does not start with baz's, so this exercises the canonical-path
        // fallback. Once the symlink is resolved, link genuinely IS a descendant of baz.
        assertTrue(FileUtil.isDescendant(baz, link));
    }

    @Test
    public void isOrIsDescendant_directoryNameContainsColon_symlinkIsStillDetectedAsDescendant(@TempDir Path tempDir)
        throws IOException {
        // "base:line" is a real, legal directory name on Unix-like systems.
        File baseLine = new File(tempDir.toFile(), "base:line");
        File sub = new File(baseLine, "sub");
        Files.createDirectories(sub.toPath());
        File realFile = new File(sub, "real.txt");
        Files.writeString(realFile.toPath(), "hi");

        File elsewhere = new File(tempDir.toFile(), "elsewhere");
        Files.createDirectories(elsewhere.toPath());
        File link = new File(elsewhere, "link");
        Files.createSymbolicLink(link.toPath(), realFile.toPath());

        assertTrue(FileUtil.isOrIsDescendant(baseLine, link));
    }

    @Test
    public void isDescendantCanonical_symlinkPathPassesThroughColonNamedDirectory_correctlyDetectsDescendant(@TempDir Path tempDir)
        throws IOException {
        File baz = new File(tempDir.toFile(), "baz");
        Files.createDirectories(baz.toPath());
        File realFile = new File(baz, "realfile.txt");
        Files.writeString(realFile.toPath(), "hi");

        File subDirWithColon = new File(tempDir.toFile(), "sub:dir");
        Files.createDirectories(subDirWithColon.toPath());
        File link = new File(subDirWithColon, "link");
        Files.createSymbolicLink(link.toPath(), realFile.toPath());

        // isDescendantCanonical never calls platformIndependentPath, so the colon in "sub:dir"
        // never gets mis-interpreted as a path separator, and the symlink is resolved correctly.
        assertTrue(FileUtil.isDescendantCanonical(baz, link));
    }

    @Test
    public void isOrIsDescendantCanonical_givenTheSameDirectoryForBothArguments_returnsTrue(@TempDir Path tempDir) {
        File dir = tempDir.toFile();
        assertTrue(FileUtil.isOrIsDescendantCanonical(dir, dir));
    }

    @Test
    public void areSameFiles_dotDotAliasedPathsThatResolveToSameFile_returnsTrue(@TempDir Path tempDir)
        throws IOException {
        File dir = new File(tempDir.toFile(), "dir");
        Files.createDirectories(dir.toPath());
        File direct = new File(dir, "file.txt");
        File viaDotDot = new File(dir, "sub/../file.txt");
        // areSameFiles compares canonical path *strings*, which correctly resolves ".." -
        // unlike isDescendant, it does not have the unresolved-startsWith flaw
        assertTrue(FileUtil.areSameFiles(direct, viaDotDot));
    }

    @Test
    public void areSameFiles_dotDotAliasedPathsThatResolveToDifferentFiles_returnsFalse(@TempDir Path tempDir)
        throws IOException {
        File dir = new File(tempDir.toFile(), "dir");
        Files.createDirectories(dir.toPath());
        File direct = new File(dir, "file.txt");
        File escaped = new File(dir, "../escaped.txt");
        assertFalse(FileUtil.areSameFiles(direct, escaped));
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
    // fileIsOrWouldBeReadable / directoryIsOrWouldBeReadable
    // -------------------------------------------------------------------------

    @Test
    public void fileIsOrWouldBeReadable_existingFile_returnsTrue(@TempDir Path tempDir) throws IOException {
        File f = tempDir.resolve("test.txt").toFile();
        assumeTrue(f.createNewFile());
        assertTrue(FileUtil.fileIsOrWouldBeReadable(f));
    }

    @Test
    public void fileIsOrWouldBeReadable_directory_returnsFalse(@TempDir Path tempDir) {
        assertFalse(FileUtil.fileIsOrWouldBeReadable(tempDir.toFile()));
    }

    @Test
    public void fileIsOrWouldBeReadable_nonExistent_returnsTrue(@TempDir Path tempDir) {
        assertTrue(FileUtil.fileIsOrWouldBeReadable(tempDir.resolve("nope.txt").toFile()));
    }

    @Test
    public void directoryIsOrWouldBeReadable_existingDir_returnsTrue(@TempDir Path tempDir) {
        assertTrue(FileUtil.directoryIsOrWouldBeReadable(tempDir.toFile()));
    }

    @Test
    public void directoryIsOrWouldBeReadable_file_returnsFalse(@TempDir Path tempDir) throws IOException {
        File f = tempDir.resolve("x.txt").toFile();
        assumeTrue(f.createNewFile());
        assertFalse(FileUtil.directoryIsOrWouldBeReadable(f));
    }

    @Test
    public void directoryIsOrWouldBeReadable_nonExistent_returnsTrue(@TempDir Path tempDir) {
        assertTrue(FileUtil.directoryIsOrWouldBeReadable(tempDir.resolve("nosuchdir").toFile()));
    }

    @Test
    public void fileIsOrWouldBeReadable_null_returnsFalse() {
        assertFalse(FileUtil.fileIsOrWouldBeReadable(null));
    }

    @Test
    public void directoryIsOrWouldBeReadable_null_returnsFalse() {
        assertFalse(FileUtil.directoryIsOrWouldBeReadable(null));
    }

    @Test
    public void fileIsOrWouldBeReadable_existingFileMadeUnreadable_returnsFalse(@TempDir Path tempDir)
        throws IOException {
        File f = tempDir.resolve("unreadable.txt").toFile();
        assumeTrue(f.createNewFile());
        runWithSetUnreadable(f, () -> assertFalse(FileUtil.fileIsOrWouldBeReadable(f)));
    }

    @Test
    public void directoryIsOrWouldBeReadable_existingDirMadeUnreadable_returnsFalse(@TempDir Path tempDir) {
        File dir = tempDir.resolve("unreadabledir").toFile();
        assumeTrue(dir.mkdir());
        runWithSetUnreadable(dir, () -> assertFalse(FileUtil.directoryIsOrWouldBeReadable(dir)));
    }

    @Test
    public void fileIsOrWouldBeWritable_nonExistentPathUnderNonDirectoryParent_returnsFalse(@TempDir Path tempDir)
        throws IOException {
        File parentThatIsActuallyAFile = tempDir.resolve("notadirectory.txt").toFile();
        assumeTrue(parentThatIsActuallyAFile.createNewFile());
        File nestedPath = new File(parentThatIsActuallyAFile, "nested.txt");
        assertFalse(nestedPath.exists());
        assertFalse(FileUtil.fileIsOrWouldBeWritable(nestedPath));
    }

    @Test
    public void directoryIsOrWouldBeReadable_nonExistentPathUnderNonDirectoryParent_returnsFalse(@TempDir Path tempDir)
        throws IOException {
        File parentThatIsActuallyAFile = tempDir.resolve("alsonotadirectory.txt").toFile();
        assertTrue(parentThatIsActuallyAFile.createNewFile());
        File nestedPath = new File(parentThatIsActuallyAFile, "nesteddir");
        assertFalse(nestedPath.exists());
        assertFalse(FileUtil.directoryIsOrWouldBeReadable(nestedPath));
    }

    // -------------------------------------------------------------------------
    // existsAndReadable
    // -------------------------------------------------------------------------

    @Test
    public void existsAndReadable_existingFile_returnsTrue(@TempDir Path tempDir) throws IOException {
        File f = tempDir.resolve("exists.txt").toFile();
        assumeTrue(f.createNewFile());
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
        assumeTrue(f.createNewFile());
        assertTrue(FileUtil.areSameFiles(f, f));
    }

    @Test
    public void areSameFiles_differentFiles_returnsFalse(@TempDir Path tempDir) throws IOException {
        File a = tempDir.resolve("a.txt").toFile();
        File b = tempDir.resolve("b.txt").toFile();
        assumeTrue(a.createNewFile());
        assumeTrue(b.createNewFile());
        assertFalse(FileUtil.areSameFiles(a, b));
    }

    // -------------------------------------------------------------------------
    // checkAndCreateDir
    // -------------------------------------------------------------------------

    @Test
    public void checkAndCreateDirectory_newDirectory_createsIt(@TempDir Path tempDir) throws IOException {
        File newDir = tempDir.resolve("newsubdir").toFile();
        assertFalse(newDir.exists());
        FileUtil.checkAndCreateDirectory(newDir);
        assertTrue(newDir.isDirectory());
    }

    @Test
    public void checkAndCreateDirectory_existingDirectory_doesNotThrow(@TempDir Path tempDir) {
        assertDoesNotThrow(() -> FileUtil.checkAndCreateDirectory(tempDir.toFile()));
    }

    // -------------------------------------------------------------------------
    // checkAndCreateDir - remaining overloads (String path, custom exception type)
    // -------------------------------------------------------------------------

    @Test
    public void checkAndCreateDirectory_stringPath_createsIt(@TempDir Path tempDir) throws IOException {
        File newDir = tempDir.resolve("fromstring").toFile();
        FileUtil.checkAndCreateDirectory(newDir.getPath());
        assertTrue(newDir.isDirectory());
    }

    @Test
    public void checkAndCreateDirectory_nullStringPath_doesNothing() {
        assertDoesNotThrow(() -> FileUtil.checkAndCreateDirectory((String) null));
    }

    @Test
    public void checkAndCreateDirectory_existingFileNotDirectory_throwsGivenExceptionType(@TempDir Path tempDir)
        throws IOException {
        File f = tempDir.resolve("plain.txt").toFile();
        assumeTrue(f.createNewFile());
        assertThrows(IllegalStateException.class, () -> FileUtil.checkAndCreateDirectory(f, IllegalStateException.class));
    }

    @Test
    public void checkAndCreateDirectory_existingFileNotDirectory_nullExceptionType_doesNotThrow(@TempDir Path tempDir)
        throws IOException {
        File f = tempDir.resolve("plain2.txt").toFile();
        assumeTrue(f.createNewFile());
        assertDoesNotThrow(() -> FileUtil.checkAndCreateDirectory(f, null));
    }

    @Test
    public void checkAndCreateDirectory_stringPathWithCustomExceptionType_createsIt(@TempDir Path tempDir)
        throws IllegalStateException {
        File newDir = tempDir.resolve("fromstring2").toFile();
        FileUtil.checkAndCreateDirectory(newDir.getPath(), IllegalStateException.class);
        assertTrue(newDir.isDirectory());
    }

    // -------------------------------------------------------------------------
    // fileIsOrWouldBeWritable / directoryIsOrWouldBeWritable
    // -------------------------------------------------------------------------

    @Test
    public void fileIsOrWouldBeWritable_nonExistentFile_returnsTrue(@TempDir Path tempDir) {
        File f = tempDir.resolve("new.txt").toFile();
        assertTrue(FileUtil.fileIsOrWouldBeWritable(f));
    }

    @Test
    public void fileIsOrWouldBeWritable_existingWritableFile_returnsTrue(@TempDir Path tempDir) throws IOException {
        File f = tempDir.resolve("existing.txt").toFile();
        assumeTrue(f.createNewFile());
        assertTrue(FileUtil.fileIsOrWouldBeWritable(f));
    }

    @Test
    public void fileIsOrWouldBeWritable_existingDirectory_returnsFalse(@TempDir Path tempDir) {
        assertFalse(FileUtil.fileIsOrWouldBeWritable(tempDir.toFile()));
    }

    @Test
    public void fileIsOrWouldBeWritable_null_returnsFalse() {
        assertFalse(FileUtil.fileIsOrWouldBeWritable(null));
    }

    @Test
    public void directoryIsOrWouldBeWritable_existingWritableDirectory_returnsTrue(@TempDir Path tempDir) {
        assertTrue(FileUtil.directoryIsOrWouldBeWritable(tempDir.toFile()));
    }

    @Test
    public void directoryIsOrWouldBeWritable_nonExistentPath_returnsTrue(@TempDir Path tempDir) {
        File f = tempDir.resolve("notyetcreated").toFile();
        assertTrue(FileUtil.directoryIsOrWouldBeWritable(f));
    }

    @Test
    public void directoryIsOrWouldBeWritable_existingFile_returnsFalse(@TempDir Path tempDir) throws IOException {
        File f = tempDir.resolve("afile.txt").toFile();
        assumeTrue(f.createNewFile());
        assertFalse(FileUtil.directoryIsOrWouldBeWritable(f));
    }

    @Test
    public void directoryIsOrWouldBeWritable_null_returnsFalse() {
        assertFalse(FileUtil.directoryIsOrWouldBeWritable(null));
    }

    @Test
    public void fileIsOrWouldBeWritable_existingFileMadeUnwritable_returnsFalse(@TempDir Path tempDir)
        throws IOException {
        File f = tempDir.resolve("readonly.txt").toFile();
        assumeTrue(f.createNewFile());
        runWithSetUnwritable(f, () -> assertFalse(FileUtil.fileIsOrWouldBeWritable(f)));
    }

    @Test
    public void directoryIsOrWouldBeWritable_existingDirMadeUnwritable_returnsFalse(@TempDir Path tempDir) {
        File dir = tempDir.resolve("readonlydir").toFile();
        assumeTrue(dir.mkdir());
        runWithSetUnwritable(dir, () -> assertFalse(FileUtil.directoryIsOrWouldBeWritable(dir)));
    }

    // -------------------------------------------------------------------------
    // delete(File) / delete(Iterable<File>)
    // -------------------------------------------------------------------------

    @Test
    public void delete_singleFile_removesIt(@TempDir Path tempDir) throws IOException {
        File f = tempDir.resolve("toDelete.txt").toFile();
        assumeTrue(f.createNewFile());
        FileUtil.delete(f);
        assertFalse(f.exists());
    }

    @Test
    public void delete_directoryWithContents_removesAll(@TempDir Path tempDir) throws IOException {
        File dir = tempDir.resolve("dirToDelete").toFile();
        Files.createDirectories(dir.toPath());
        File child = new File(dir, "child.txt");
        Files.writeString(child.toPath(), "hi");
        FileUtil.delete(dir);
        assertFalse(dir.exists());
    }

    @Test
    public void delete_nullFile_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> FileUtil.delete((File) null));
    }

    @Test
    public void delete_iterable_allSucceed_returnsEmptyList(@TempDir Path tempDir) throws IOException {
        File a = tempDir.resolve("a.txt").toFile();
        File b = tempDir.resolve("b.txt").toFile();
        assumeTrue(a.createNewFile());
        assumeTrue(b.createNewFile());
        List<File> notDeleted = FileUtil.delete(List.of(a, b));
        assertTrue(notDeleted.isEmpty());
        assertFalse(a.exists());
        assertFalse(b.exists());
    }

    @Test
    public void delete_iterable_missingFile_isReportedAsNotDeleted(@TempDir Path tempDir) {
        File missing = tempDir.resolve("missing.txt").toFile();
        List<File> notDeleted = FileUtil.delete(List.of(missing));
        assertEquals(List.of(missing), notDeleted);
    }

    @Test
    public void delete_iterable_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> FileUtil.delete((Iterable<File>) null));
    }

    // -------------------------------------------------------------------------
    // listDirs
    // -------------------------------------------------------------------------

    @Test
    public void listDirs_mixOfFilesAndDirs_returnsOnlyDirs(@TempDir Path tempDir) throws IOException {
        File subDir = new File(tempDir.toFile(), "sub");
        Files.createDirectories(subDir.toPath());
        File plainFile = new File(tempDir.toFile(), "plain.txt");
        assumeTrue(plainFile.createNewFile());
        List<File> dirs = FileUtil.listDirs(tempDir.toFile());
        assertEquals(1, dirs.size());
        assertEquals(subDir, dirs.getFirst());
    }

    @Test
    public void listDirs_notADirectory_returnsEmptyList(@TempDir Path tempDir) throws IOException {
        File f = tempDir.resolve("notadir.txt").toFile();
        assumeTrue(f.createNewFile());
        assertTrue(FileUtil.listDirs(f).isEmpty());
    }

    @Test
    public void listDirs_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> FileUtil.listDirs(null));
    }

    // -------------------------------------------------------------------------
    // findFilesByExtension
    // -------------------------------------------------------------------------

    @Test
    public void findFilesByExtension_nonRecursive_findsMatchingFilesInTopLevelOnly(@TempDir Path tempDir)
        throws IOException {
        File matching = new File(tempDir.toFile(), "a.txt");
        File nonMatching = new File(tempDir.toFile(), "b.csv");
        Files.writeString(matching.toPath(), "x");
        Files.writeString(nonMatching.toPath(), "x");
        File subDir = new File(tempDir.toFile(), "sub");
        Files.createDirectories(subDir.toPath());
        File nested = new File(subDir, "c.txt");
        Files.writeString(nested.toPath(), "x");

        Collection<File> found = FileUtil.findFilesByExtension(tempDir.toFile(), "txt", false);
        assertEquals(Set.of(matching), Set.copyOf(found));
    }

    @Test
    public void findFilesByExtension_recursive_findsMatchingFilesAtAnyDepth(@TempDir Path tempDir)
        throws IOException {
        File matching = new File(tempDir.toFile(), "a.txt");
        Files.writeString(matching.toPath(), "x");
        File subDir = new File(tempDir.toFile(), "sub");
        Files.createDirectories(subDir.toPath());
        File nested = new File(subDir, "c.txt");
        Files.writeString(nested.toPath(), "x");

        Collection<File> found = FileUtil.findFilesByExtension(tempDir.toFile(), "txt", true);
        assertEquals(Set.of(matching, nested), Set.copyOf(found));
    }

    // -------------------------------------------------------------------------
    // getCanonicalFile
    // -------------------------------------------------------------------------

    @Test
    public void getCanonicalFile_relativePath_returnsCanonicalForm() throws IOException {
        File relative = new File("./foo.bar");
        assertEquals(relative.getCanonicalFile(), FileUtil.getCanonicalFile(relative));
    }

    @Test
    public void getCanonicalFile_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> FileUtil.getCanonicalFile(null));
    }

    // -------------------------------------------------------------------------
    // move
    // -------------------------------------------------------------------------

    @Test
    public void move_distinctSourceAndDest_movesFileAndReturnsTrue(@TempDir Path tempDir) throws IOException {
        File source = tempDir.resolve("source.txt").toFile();
        Files.writeString(source.toPath(), "content");
        File dest = tempDir.resolve("dest.txt").toFile();
        assertTrue(FileUtil.move(source, dest));
        assertFalse(source.exists());
        assertEquals("content", Files.readString(dest.toPath()));
    }

    @Test
    public void move_sourceAndDestAreSameFile_doesNotMoveAndReturnsFalse(@TempDir Path tempDir) throws IOException {
        File source = tempDir.resolve("same.txt").toFile();
        Files.writeString(source.toPath(), "content");
        assertFalse(FileUtil.move(source, source));
        assertTrue(source.exists());
    }

    @Test
    public void move_nullSource_throwsNullPointerException(@TempDir Path tempDir) {
        File dest = tempDir.resolve("dest.txt").toFile();
        assertThrows(NullPointerException.class, () -> FileUtil.move(null, dest));
    }

    @Test
    public void move_nullDest_throwsNullPointerException(@TempDir Path tempDir) throws IOException {
        File source = tempDir.resolve("source.txt").toFile();
        Files.writeString(source.toPath(), "content");
        assertThrows(NullPointerException.class, () -> FileUtil.move(source, null));
    }

    // -------------------------------------------------------------------------
    // createSystemTempFile
    // -------------------------------------------------------------------------

    @Test
    public void createSystemTempFile_withExtension_createsFileWithThatExtension() throws IOException {
        withTemporaryFile(
            () -> FileUtil.createSystemTempFile("test", "tmp"),
            tempFile -> {
                assertTrue(tempFile.exists());
                assertTrue(tempFile.getName().endsWith(".tmp"));
            }
        );
    }

    @Test
    public void createSystemTempFile_blankExtension_createsFileWithDefaultExtension() throws IOException {
        withTemporaryFile(
            () -> FileUtil.createSystemTempFile("futest", ""),
            tempFile -> {
                assertTrue(tempFile.exists());
                assertTrue(tempFile.getName().startsWith("futest"));
            }
        );
    }

    // -------------------------------------------------------------------------
    // getCurrentDirectory
    // -------------------------------------------------------------------------

    @Test
    public void getCurrentDirectory_isJustDot() {
        assertEquals(FileNameUtil.CURRENT_PATH_INDICATOR,  FileUtil.getCurrentDirectory().getPath());
    }

    // -------------------------------------------------------------------------
    // getWorkingDirectory
    // -------------------------------------------------------------------------

    @Test
    public void getWorkingDirectory_matchesUserDirSystemProperty() {
        File workingDir = FileUtil.getWorkingDirectory();
        assertEquals(new File(System.getProperty("user.dir")), workingDir);
    }

    // -------------------------------------------------------------------------
    // deleteOrDeleteOnExit
    // -------------------------------------------------------------------------

    @Test
    public void deleteOrDeleteOnExit_nullArgument_returnsNoFileRequested() {
        assertEquals(FileUtil.DeleteRequestStatus.NO_FILE_REQUESTED, FileUtil.deleteOrDeleteOnExit(null));
    }

    @Test
    public void deleteOrDeleteOnExit_missingFile_returnsNoSuchFile(@TempDir Path tempDir) {
        File missing = tempDir.resolve("missing.txt").toFile();
        assertEquals(FileUtil.DeleteRequestStatus.NO_SUCH_FILE, FileUtil.deleteOrDeleteOnExit(missing));
    }

    @Test
    public void deleteOrDeleteOnExit_existingFile_deletesImmediately(@TempDir Path tempDir) throws IOException {
        File f = tempDir.resolve("toDelete.txt").toFile();
        assumeTrue(f.createNewFile());
        assertEquals(FileUtil.DeleteRequestStatus.SUCCESS_IMMEDIATE, FileUtil.deleteOrDeleteOnExit(f));
        assertFalse(f.exists());
    }

    @Test
    public void deleteOrDeleteOnExit_nonEmptyDirectory_marksForDeletionOnExit(@TempDir Path tempDir)
        throws IOException {
        File dir = tempDir.resolve("nonEmptyDir").toFile();
        Files.createDirectories(dir.toPath());
        Files.writeString(new File(dir, "child.txt").toPath(), "x");
        assertEquals(FileUtil.DeleteRequestStatus.SUCCESS_ON_EXIT, FileUtil.deleteOrDeleteOnExit(dir));
        // deleteOnExit() doesn't actually remove it yet
        assertTrue(dir.exists());
    }

    @Test
    public void deleteRequestStatus_failureStatuses_reportFailedTrue() {
        assertTrue(FileUtil.DeleteRequestStatus.FAILURE_SECURITY.failed());
        assertTrue(FileUtil.DeleteRequestStatus.FAILURE_NON_EMPTY_DIRECTORY.failed());
        assertTrue(FileUtil.DeleteRequestStatus.FAILURE_OTHER.failed());
    }

    @Test
    public void deleteRequestStatus_nonFailureStatuses_reportFailedFalse() {
        assertFalse(FileUtil.DeleteRequestStatus.NO_FILE_REQUESTED.failed());
        assertFalse(FileUtil.DeleteRequestStatus.NO_SUCH_FILE.failed());
        assertFalse(FileUtil.DeleteRequestStatus.SUCCESS_IMMEDIATE.failed());
        assertFalse(FileUtil.DeleteRequestStatus.SUCCESS_ON_EXIT.failed());
    }

    // -------------------------------------------------------------------------
    // getFileFilterByExtension
    // -------------------------------------------------------------------------

    @Test
    public void getFileFilterByExtension_singleExtension_matchesOnlyThatExtension(@TempDir Path tempDir)
        throws IOException {
        File txtFile = new File(tempDir.toFile(), "a.txt");
        File csvFile = new File(tempDir.toFile(), "b.csv");
        assumeTrue(txtFile.createNewFile());
        assumeTrue(csvFile.createNewFile());
        FileFilter filter = FileUtil.getFileFilterByExtension("txt");
        assertTrue(filter.accept(txtFile));
        assertFalse(filter.accept(csvFile));
    }

    @Test
    public void getFileFilterByExtension_neverMatchesDirectories(@TempDir Path tempDir) throws IOException {
        File subDir = new File(tempDir.toFile(), "sub.txt");
        Files.createDirectories(subDir.toPath());
        FileFilter filter = FileUtil.getFileFilterByExtension("txt");
        assertFalse(filter.accept(subDir));
    }

    @Test
    public void getFileFilterByExtension_setOfExtensions_matchesAnyOfThem(@TempDir Path tempDir) throws IOException {
        File txtFile = new File(tempDir.toFile(), "a.txt");
        File csvFile = new File(tempDir.toFile(), "b.csv");
        File jsonFile = new File(tempDir.toFile(), "c.json");
        assumeTrue(txtFile.createNewFile());
        assumeTrue(csvFile.createNewFile());
        assumeTrue(jsonFile.createNewFile());
        FileFilter filter = FileUtil.getFileFilterByExtension(Set.of("txt", "csv"));
        assertTrue(filter.accept(txtFile));
        assertTrue(filter.accept(csvFile));
        assertFalse(filter.accept(jsonFile));
    }

    @Test
    public void getFileFilterByExtension_extensionWithLeadingDot_isNormalized(@TempDir Path tempDir)
        throws IOException {
        File txtFile = new File(tempDir.toFile(), "a.txt");
        assumeTrue(txtFile.createNewFile());
        FileFilter filter = FileUtil.getFileFilterByExtension(Set.of(".txt"));
        assertTrue(filter.accept(txtFile));
    }

    @Test
    public void getFileFilterByExtension_emptySet_matchesNothing(@TempDir Path tempDir) throws IOException {
        File txtFile = new File(tempDir.toFile(), "a.txt");
        assumeTrue(txtFile.createNewFile());
        FileFilter filter = FileUtil.getFileFilterByExtension(Set.of());
        assertFalse(filter.accept(txtFile));
    }

    @Test
    public void getFileFilterByExtension_nullExtension_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> FileUtil.getFileFilterByExtension((String) null));
    }

    // -------------------------------------------------------------------------
    // getFileNameBlobFilter / getFileNamePatternFilter
    // -------------------------------------------------------------------------

    @Test
    public void getFileNameBlobFilter_wildcardMatchesExpectedNames(@TempDir Path tempDir) throws IOException {
        File matching = new File(tempDir.toFile(), "report-final.txt");
        File nonMatching = new File(tempDir.toFile(), "other.txt");
        assumeTrue(matching.createNewFile());
        assumeTrue(nonMatching.createNewFile());
        var filter = FileUtil.getFileNameBlobFilter("report-*.txt");
        assertTrue(filter.accept(matching));
        assertFalse(filter.accept(nonMatching));
    }

    @Test
    public void getFileNameBlobFilter_blobContainingColon_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> FileUtil.getFileNameBlobFilter("foo:bar"));
    }

    @Test
    public void getFileNameBlobFilter_blobContainingPathSeparator_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> FileUtil.getFileNameBlobFilter("foo/bar"));
    }

    @Test
    public void getFileNameBlobFilter_blankBlob_throwsException() {
        assertThrows(RuntimeException.class, () -> FileUtil.getFileNameBlobFilter("  "));
    }

    @Test
    public void getFileNamePatternFilter_regexMatchesExpectedNames(@TempDir Path tempDir) throws IOException {
        File matching = new File(tempDir.toFile(), "img42.png");
        File nonMatching = new File(tempDir.toFile(), "doc.png");
        assumeTrue(matching.createNewFile());
        assumeTrue(nonMatching.createNewFile());
        var filter = FileUtil.getFileNamePatternFilter("img\\d+\\.png");
        assertTrue(filter.accept(matching));
        assertFalse(filter.accept(nonMatching));
    }

    @Test
    public void getFileNamePatternFilter_blankPattern_throwsException() {
        assertThrows(RuntimeException.class, () -> FileUtil.getFileNamePatternFilter(""));
    }

    // -------------------------------------------------------------------------
    // getBufferedInputStream / getBufferedUtf8Reader / getBufferedOutputStream /
    // getBufferedUtf8Writer / getBufferedTrackedOutputStream / getBufferedUtf8PrintWriter
    // -------------------------------------------------------------------------

    @Test
    public void getBufferedInputStream_readsFileContents(@TempDir Path tempDir) throws IOException {
        File f = tempDir.resolve("data.bin").toFile();
        Files.writeString(f.toPath(), "binary content");
        try (BufferedInputStream in = FileUtil.getBufferedInputStream(f)) {
            assertEquals("binary content", new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    public void getBufferedInputStream_nullFile_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> FileUtil.getBufferedInputStream(null));
    }

    @Test
    public void getBufferedUtf8Reader_readsTextContents(@TempDir Path tempDir) throws IOException {
        File f = tempDir.resolve("text.txt").toFile();
        Files.writeString(f.toPath(), "héllo", StandardCharsets.UTF_8);
        try (BufferedReader reader = FileUtil.getBufferedUtf8Reader(f)) {
            assertEquals("héllo", reader.readLine());
        }
    }

    @Test
    public void getBufferedOutputStream_writesFileContents(@TempDir Path tempDir) throws IOException {
        File f = tempDir.resolve("out.bin").toFile();
        try (BufferedOutputStream out = FileUtil.getBufferedOutputStream(f)) {
            out.write("hello".getBytes(StandardCharsets.UTF_8));
        }
        assertEquals("hello", Files.readString(f.toPath()));
    }

    @Test
    public void getBufferedUtf8Writer_writesTextContents(@TempDir Path tempDir) throws IOException {
        File f = tempDir.resolve("out.txt").toFile();
        try (BufferedWriter writer = FileUtil.getBufferedUtf8Writer(f)) {
            writer.write("héllo");
        }
        assertEquals("héllo", Files.readString(f.toPath(), StandardCharsets.UTF_8));
    }

    @Test
    public void getBufferedTrackedOutputStream_writesContentsAndClosesTracker(@TempDir Path tempDir)
        throws IOException {
        File f = tempDir.resolve("tracked.txt").toFile();
        AtomicBoolean trackerClosed = new AtomicBoolean(false);
        Resource tracker = new Resource() {

            @Override
            public void close() {
                trackerClosed.set(true);
            }

            @Override
            public boolean isOpen() {
                return !trackerClosed.get();
            }

        };
        try (OutputStream out = FileUtil.getBufferedTrackedOutputStream(f, () -> tracker)) {
            out.write("tracked content".getBytes(StandardCharsets.UTF_8));
        }
        assertEquals("tracked content", Files.readString(f.toPath()));
        assertTrue(trackerClosed.get());
    }

    @Test
    public void getBufferedUtf8PrintWriter_writesTextContents(@TempDir Path tempDir) throws IOException {
        File f = tempDir.resolve("pw.txt").toFile();
        try (PrintWriter writer = FileUtil.getBufferedUtf8PrintWriter(f)) {
            writer.print("printed");
        }
        assertEquals("printed", Files.readString(f.toPath(), StandardCharsets.UTF_8));
    }

    // -------------------------------------------------------------------------
    // getMoveDestination
    // -------------------------------------------------------------------------

    @Test
    public void getMoveDestination_destinationIsExistingDirectory_appendsFileName(@TempDir Path tempDir) {
        File dir = tempDir.toFile();
        File result = FileUtil.getMoveDestination(dir, "report.pdf");
        assertEquals(new File(dir, "report.pdf"), result);
    }

    @Test
    public void getMoveDestination_destinationIsNonExistentPath_returnsAsIs(@TempDir Path tempDir) {
        File dest = tempDir.resolve("newname.pdf").toFile();
        File result = FileUtil.getMoveDestination(dest, "report.pdf");
        assertEquals(dest, result);
    }

    @Test
    public void getMoveDestination_destinationIsExistingFile_returnsAsIs(@TempDir Path tempDir) throws IOException {
        File dest = tempDir.resolve("existing.pdf").toFile();
        assumeTrue(dest.createNewFile());
        File result = FileUtil.getMoveDestination(dest, "report.pdf");
        assertEquals(dest, result);
    }

    @Test
    public void getMoveDestination_invalidFileName_throwsIllegalArgumentException(@TempDir Path tempDir) {
        File dir = tempDir.toFile();
        assertThrows(IllegalArgumentException.class, () -> FileUtil.getMoveDestination(dir, "../escape.txt"));
    }

    @Test
    public void getMoveDestination_nullDestination_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> FileUtil.getMoveDestination(null, "report.pdf"));
    }

    @Test
    public void getMoveDestination_nullFileName_throwsNullPointerException(@TempDir Path tempDir) {
        File dir = tempDir.toFile();
        assertThrows(NullPointerException.class, () -> FileUtil.getMoveDestination(dir, null));
    }

    // -------------------------------------------------------------------------
    // getDescendant
    // -------------------------------------------------------------------------

    @Test
    public void getDescendant_multipleComponents_buildsNestedPath(@TempDir Path tempDir) {
        File dir = tempDir.toFile();
        File result = FileUtil.getDescendant(dir, "a", "b", "c.txt");
        assertEquals(new File(new File(new File(dir, "a"), "b"), "c.txt"), result);
    }

    @Test
    public void getDescendant_noComponents_returnsDirectoryItself(@TempDir Path tempDir) {
        File dir = tempDir.toFile();
        assertEquals(dir, FileUtil.getDescendant(dir));
    }

    @Test
    public void getDescendant_invalidPathComponent_throwsIllegalArgumentException(@TempDir Path tempDir) {
        File dir = tempDir.toFile();
        assertThrows(IllegalArgumentException.class, () -> FileUtil.getDescendant(dir, "..", "escape.txt"));
    }

    @Test
    public void getDescendant_nullDirectory_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> FileUtil.getDescendant(null, "a"));
    }

    // -------------------------------------------------------------------------
    // getFindFilesByExtensionFilter
    // -------------------------------------------------------------------------

    @Test
    public void getFindFilesByExtensionFilter_nonRecursive_behavesLikeExtensionFilter(@TempDir Path tempDir)
        throws IOException {
        File txtFile = new File(tempDir.toFile(), "a.txt");
        File subDir = new File(tempDir.toFile(), "sub");
        assumeTrue(txtFile.createNewFile());
        Files.createDirectories(subDir.toPath());
        FileFilter filter = FileUtil.getFindFilesByExtensionFilter("txt", false);
        assertTrue(filter.accept(txtFile));
        assertFalse(filter.accept(subDir));
    }

    @Test
    public void getFindFilesByExtensionFilter_recursive_alsoAcceptsDirectories(@TempDir Path tempDir)
        throws IOException {
        File subDir = new File(tempDir.toFile(), "sub");
        Files.createDirectories(subDir.toPath());
        FileFilter filter = FileUtil.getFindFilesByExtensionFilter("txt", true);
        assertTrue(filter.accept(subDir));
    }

    // -------------------------------------------------------------------------
    // isDescendantPathCanonical / isOrIsDescendantPathCanonical
    // -------------------------------------------------------------------------

    @Test
    public void isDescendantPathCanonical_genuineDescendant_returnsTrue(@TempDir Path tempDir) throws IOException {
        File dir = new File(tempDir.toFile(), "dir");
        Files.createDirectories(dir.toPath());
        File child = new File(dir, "child.txt");
        Files.writeString(child.toPath(), "x");
        assertTrue(FileUtil.isDescendantPathCanonical(dir.getPath(), child.getPath()));
    }

    @Test
    public void isDescendantPathCanonical_dotDotEscape_returnsFalse(@TempDir Path tempDir) {
        String dirPath = tempDir.toString();
        String escapedFilePath = dirPath + "/../escaped.txt";
        assertFalse(FileUtil.isDescendantPathCanonical(dirPath, escapedFilePath));
    }

    @Test
    public void isDescendantPathCanonical_nullArguments_returnFalse() {
        assertFalse(FileUtil.isDescendantPathCanonical(null, "/foo"));
        assertFalse(FileUtil.isDescendantPathCanonical("/foo", null));
    }

    @Test
    public void isOrIsDescendantPathCanonical_sameDirectory_returnsTrue(@TempDir Path tempDir) {
        String dirPath = tempDir.toString();
        assertTrue(FileUtil.isOrIsDescendantPathCanonical(dirPath, dirPath));
    }

    @Test
    public void isOrIsDescendantPathCanonical_dotDotEscape_returnsFalse(@TempDir Path tempDir) {
        String dirPath = tempDir.toString();
        String escapedFilePath = dirPath + "/../escaped.txt";
        assertFalse(FileUtil.isOrIsDescendantPathCanonical(dirPath, escapedFilePath));
    }

    @Test
    public void isOrIsDescendantPathCanonical_nullArguments_returnFalse() {
        assertFalse(FileUtil.isOrIsDescendantPathCanonical(null, "/foo"));
        assertFalse(FileUtil.isOrIsDescendantPathCanonical("/foo", null));
    }

}

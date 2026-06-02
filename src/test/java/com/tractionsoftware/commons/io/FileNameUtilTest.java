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

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public final class FileNameUtilTest {

    @Test
    public void testGetValidNormalizedFileNameNull() {
        assertEquals("Untitled.txt", FileNameUtil.getValidNormalizedFileName(null, null, "text/plain", true));
    }

    @Test
    public void testGetValidNormalizedFileNameNullNoMimeType() {
        assertEquals("Untitled", FileNameUtil.getValidNormalizedFileName(null, null, null, true));
    }

    @Test
    public void testGetValidNormalizedFileNameEmpty() {
        assertEquals("Untitled.txt", FileNameUtil.getValidNormalizedFileName("", null, "text/plain", true));
    }

    @Test
    public void testGetValidNormalizedFileNameBlank() {
        assertEquals("Untitled.txt", FileNameUtil.getValidNormalizedFileName("  ", null, "text/plain", true));
    }

    @Test
    public void testGetValidNormalizedFileNameSimpleFixInvalidName() {
        assertEquals(
            "_my_-file.txt",
            FileNameUtil.getValidNormalizedFileName("<my>-file.txt", null, "text/plain", true)
        );
    }

    @Test
    public void testGetValidNormalizedFileNameSimpleFixAndAddExtension() {
        assertEquals("_my_-file.txt", FileNameUtil.getValidNormalizedFileName("<my>-file", null, "text/plain", true));
    }

    @Test
    public void testGetValidNormalizedFileNameWithAllInvalidCharactersReplaced() {
        assertEquals("______.txt", FileNameUtil.getValidNormalizedFileName("<????>", null, "text/plain", true));
    }

    @Test
    public void testGetValidNormalizedFileNameWithMultipleSpacesCollapsed() {
        assertEquals("foo bar.txt", FileNameUtil.getValidNormalizedFileName("foo  bar.txt", null, "text/plain", true));
    }

    @Test
    public void testGetValidNormalizedFileValidDotFileNotChanged() {
        assertEquals(".foo", FileNameUtil.getValidNormalizedFileName(".foo", null, "text/plain", true));
    }

    @Test
    public void test_isMinimallyValidFileName1a() {
        assertTrue(FileNameUtil.isMinimallyValidFileName("my  file.txt"));
    }

    @Test
    public void test_isMinimallyValidFileName1b() {
        assertFalse(FileNameUtil.isMinimallyValidFileName("my\tfile.txt"));
    }

    @Test
    public void test_isMinimallyValidFileName1c() {
        assertFalse(FileNameUtil.isMinimallyValidFileName("my\nfile.txt"));
    }

    @Test
    public void test_isMinimallyValidFileName1d() {
        assertFalse(FileNameUtil.isMinimallyValidFileName("my\rfile.txt"));
    }

    @Test
    public void test_isMinimallyValidFileName2() {
        assertTrue(FileNameUtil.isMinimallyValidFileName("my.file.txt"));
    }

    @Test
    public void test_isMinimallyValidFileName3a() {
        assertFalse(FileNameUtil.isMinimallyValidFileName("my\u0000file.txt"));
    }

    @Test
    public void test_isMinimallyValidFileName3b() {
        assertFalse(FileNameUtil.isMinimallyValidFileName("<my>-\u0000file.txt."));
    }

    @Test
    public void test_isMinimallyValidFileName4() {
        assertTrue(FileNameUtil.isMinimallyValidFileName("file"));
    }

    @Test
    public void test_isMinimallyValidFileName5a() {
        assertFalse(FileNameUtil.isMinimallyValidFileName("."));
    }

    @Test
    public void test_isMinimallyValidFileName5b() {
        assertFalse(FileNameUtil.isMinimallyValidFileName(".."));
    }

    @Test
    public void test_isMinimallyValidFileName5c() {
        assertFalse(FileNameUtil.isMinimallyValidFileName("...."));
    }

    @Test
    public void test_isMinimallyValidFileName6a() {
        assertFalse(FileNameUtil.isMinimallyValidFileName(null));
    }

    @Test
    public void test_isMinimallyValidFileName6b() {
        assertFalse(FileNameUtil.isMinimallyValidFileName(""));
    }

    @Test
    public void test_isMinimallyValidFileName6c() {
        assertFalse(FileNameUtil.isMinimallyValidFileName(" "));
    }

    @Test
    public void test_isMinimallyValidFileName6d() {
        assertFalse(FileNameUtil.isMinimallyValidFileName("\t\t   "));
    }

    @Test
    public void test_isMinimallyValidFileName7a() {
        assertFalse(FileNameUtil.isMinimallyValidFileName("CON"));
    }

    @Test
    public void test_isMinimallyValidFileName7b() {
        assertFalse(FileNameUtil.isMinimallyValidFileName("CON.txt"));
    }

    @Test
    public void test_isMinimallyValidFileName7c() {
        assertFalse(FileNameUtil.isMinimallyValidFileName("COM¹"));
    }

    @Test
    public void test_isMinimallyValidFileName7d() {
        assertFalse(FileNameUtil.isMinimallyValidFileName("COM¹.gif"));
    }

    @Test
    public void test_isMinimallyValidFileName7e() {
        assertFalse(FileNameUtil.isMinimallyValidFileName("lpt3"));
    }

    @Test
    public void test_getMinimallyValidFileName1a() {
        assertEquals("my  file.txt", FileNameUtil.getMinimallyValidFileName("my  file.txt", null, "text/plain"));
    }

    @Test
    public void test_getMinimallyValidFileName1b() {
        assertEquals("my file.txt", FileNameUtil.getMinimallyValidFileName("my\tfile.txt", null, "text/plain"));
    }

    @Test
    public void test_getMinimallyValidFileName1c() {
        assertEquals("my file.txt", FileNameUtil.getMinimallyValidFileName("my\nfile.txt", null, "text/plain"));
    }

    @Test
    public void test_getMinimallyValidFileName1d() {
        assertEquals("my file.txt", FileNameUtil.getMinimallyValidFileName("my\rfile.txt", null, "text/plain"));
    }

    @Test
    public void test_getMinimallyValidFileName1e() {
        assertEquals("my  file.txt", FileNameUtil.getMinimallyValidFileName("my\t file.txt", null, "text/plain"));
    }

    @Test
    public void test_getMinimallyValidFileName1f() {
        assertEquals("my   file.txt", FileNameUtil.getMinimallyValidFileName("my\t\n\rfile.txt", null, "text/plain"));
    }

    @Test
    public void test_getMinimallyValidFileName2() {
        assertEquals("my.file.txt", FileNameUtil.getMinimallyValidFileName("my.file.txt", null, "text/plain"));
    }

    @Test
    public void test_getMinimallyValidFileName3a() {
        assertEquals("my_file.txt", FileNameUtil.getMinimallyValidFileName("my\u0000file.txt", null, "text/plain"));
    }

    @Test
    public void test_getMinimallyValidFileName3b() {
        assertEquals(
            "_my_-_file.txt",
            FileNameUtil.getMinimallyValidFileName("<my>-\u0000file.txt.", null, "text/plain")
        );
    }

    @Test
    public void test_getMinimallyValidFileName4() {
        assertEquals("file", FileNameUtil.getMinimallyValidFileName("file", null, "image/gif"));
    }

    @Test
    public void test_getMinimallyValidFileName5() {
        assertEquals("Untitled.gif", FileNameUtil.getMinimallyValidFileName(".", null, "image/gif"));
    }

    @Test
    public void test_getMinimallyValidFileName6a() {
        assertEquals("Untitled.txt", FileNameUtil.getMinimallyValidFileName(null, null, "text/plain"));
    }

    @Test
    public void test_getMinimallyValidFileName6b() {
        assertEquals("Untitled.txt", FileNameUtil.getMinimallyValidFileName("", null, "text/plain"));
    }

    @Test
    public void test_getMinimallyValidFileName6c() {
        assertEquals("Untitled.txt", FileNameUtil.getMinimallyValidFileName(" ", null, "text/plain"));
    }

    @Test
    public void test_getMinimallyValidFileName6d() {
        assertEquals("Untitled.txt", FileNameUtil.getMinimallyValidFileName("\t\t   ", null, "text/plain"));
    }

    @Test
    public void test_getMinimallyValidFileName7a() {
        assertEquals("Untitled", FileNameUtil.getMinimallyValidFileName("CON", null, null));
    }

    @Test
    public void test_getMinimallyValidFileName7b() {
        assertEquals("Untitled.txt", FileNameUtil.getMinimallyValidFileName("CON.txt", null, "text/plain"));
    }

    @Test
    public void test_getMinimallyValidFileName7c() {
        assertEquals("Untitled", FileNameUtil.getMinimallyValidFileName("COM¹", null, null));
    }

    @Test
    public void test_getMinimallyValidFileName7d() {
        assertEquals("Untitled.gif", FileNameUtil.getMinimallyValidFileName("COM¹.gif", null, "image/gif"));
    }

    @Test
    public void test_getMinimallyValidFileName7e() {
        assertEquals("Untitled", FileNameUtil.getMinimallyValidFileName("lpt", null, "application/octet-stream"));
    }

    @Test
    public void test_getMinimallyValidFileName8a() {
        assertEquals("bar__.foo", FileNameUtil.getMinimallyValidFileName("bar\u007f\u0080.foo", null, "text/plain"));
    }

    @Test
    public void test_getMinimallyValidFileName8b() {
        assertEquals("bar__.foo", FileNameUtil.getMinimallyValidFileName("bar\u008f\u009f.foo", null, "text/plain"));
    }

    @Test
    public void testGetExtension_null() {
        assertNull(FileNameUtil.getExtension(null, null));
    }

    @Test
    public void testGetExtension_trailingSlashExpectsDefault() {
        assertNull(FileNameUtil.getExtension(
            File.separator + "foo" + File.separator + "bar.baz" + File.separator,
            null
        ));
    }

    @Test
    public void testGetExtension_withSimplePath1() {
        assertEquals("baz", FileNameUtil.getExtension(File.separator + "foo" + File.separator + "bar.baz", null));
    }

    @Test
    public void testGetExtension_withVariousPathSeparators() {
        assertEquals("doc", FileNameUtil.getExtension("/foo/bar\\baz\\gax.doc", null));
    }

    @Test
    public void testGetExtension_withIncoherentPathButValidExtension() {
        assertEquals("doc", FileNameUtil.getExtension("///foo/////bar/\\\\../gax.doc", null));
    }

    @Test
    public void testGetExtension_dotFileGivesAfterDotAsExtension() {
        assertEquals("zshrc", FileNameUtil.getExtension("dot/files/.zshrc", null));
    }

    @Test
    public void testGetExtension_emptyGivesEmptyNotDefault() {
        assertEquals("", FileNameUtil.getExtension("dot/files/foo.", null));
    }

    @Test
    public void testStripExtension_null() {
        assertNull(FileNameUtil.stripExtension(null));
    }

    @Test
    public void testStripExtension_trailingSlashExpectsSuppliedPath() {
        String path = File.separator + "foo" + File.separator + "bar.baz" + File.separator;
        assertEquals(path, FileNameUtil.stripExtension(path));
    }

    @Test
    public void testStripExtension_withSimplePath1() {
        assertEquals(
            File.separator + "foo" + File.separator + "bar",
            FileNameUtil.stripExtension(File.separator + "foo" + File.separator + "bar.baz")
        );
    }

    @Test
    public void testStripExtension_withVariousPathSeparators() {
        assertEquals("/foo/bar\\baz\\gax", FileNameUtil.stripExtension("/foo/bar\\baz\\gax.doc"));
    }

    @Test
    public void testStripExtension_withIncoherentPathButValidExtension() {
        assertEquals("///foo/////bar/\\\\../gax", FileNameUtil.stripExtension("///foo/////bar/\\\\../gax.doc"));
    }

    @Test
    public void testStripExtension_dotFileStripsDotAndSuffix() {
        assertEquals("dot/files/", FileNameUtil.stripExtension("dot/files/.zshrc"));
    }

    @Test
    public void testStripExtension_emptyExtensionGivesFullPathMinusDot() {
        assertEquals("dot/files/foo", FileNameUtil.stripExtension("dot/files/foo."));
    }

    @Test
    public void test_stripPathSimple1() {
        String input = "/pub/resources/something.png";
        String expected = "something.png";
        String actual = FileNameUtil.stripPath(input);
        assertEquals(expected, actual);
    }

    @Test
    public void test_stripPathVariousSeparators1() {
        String input = "/foo/bar\\a/b\\c/d/Oh Brother, Where Art Thou.doc";
        String expected = "Oh Brother, Where Art Thou.doc";
        String actual = FileNameUtil.stripPath(input);
        assertEquals(expected, actual);
    }

    @Test
    public void test_isSeparator1() {
        assertTrue(FileNameUtil.isSeparator('/'));
    }

    @Test
    public void test_isSeparator2() {
        assertTrue(FileNameUtil.isSeparator('\\'));
    }

    @Test
    public void test_isSeparator3() {
        assertFalse(FileNameUtil.isSeparator('.'));
    }

    @Test
    public void test_isSeparator4() {
        assertFalse(FileNameUtil.isSeparator(':'));
    }

    @Test
    public void test_isSeparator5() {
        assertFalse(FileNameUtil.isSeparator('\n'));
    }

    @Test
    public void test_isSeparator6() {
        assertFalse(FileNameUtil.isSeparator('\r'));
    }

    @Test
    public void test_removeLeadingSeparator1() {
        assertEquals("foo/bar/baz", FileNameUtil.removeLeadingSeparator("/foo/bar/baz"));
    }

    @Test
    public void test_removeLeadingSeparator2() {
        assertEquals("foo/bar/baz/", FileNameUtil.removeLeadingSeparator("/foo/bar/baz/"));
    }

    @Test
    public void test_removeLeadingSeparator3() {
        String path = "foo/bar/baz";
        assertSame(path, FileNameUtil.removeLeadingSeparator(path));
    }

    @Test
    public void test_removeTrailingSeparator1() {
        assertEquals("foo/bar/baz", FileNameUtil.removeTrailingSeparator("foo/bar/baz/"));
    }

    @Test
    public void test_removeTrailingSeparator2() {
        assertEquals("/foo/bar/baz", FileNameUtil.removeTrailingSeparator("/foo/bar/baz/"));
    }

    @Test
    public void test_removeTrailingSeparator3() {
        String path = "foo/bar/baz";
        assertSame(path, FileNameUtil.removeLeadingSeparator(path));
    }

    @Test
    public void test_removeLeadingAndTrailingSeparators1() {
        assertEquals("foo/bar/baz", FileNameUtil.removeLeadingAndTrailingSeparators("foo/bar/baz/"));
    }

    @Test
    public void test_removeLeadingAndTrailingSeparators2() {
        assertEquals("foo/bar/baz", FileNameUtil.removeLeadingAndTrailingSeparators("/foo/bar/baz"));
    }

    @Test
    public void test_removeLeadingAndTrailingSeparators3() {
        assertEquals("foo/bar/baz", FileNameUtil.removeLeadingAndTrailingSeparators("/foo/bar/baz/"));
    }

    @Test
    public void test_removeLeadingAndTrailingSeparators4() {
        String path = "foo/bar/baz";
        assertSame(path, FileNameUtil.removeLeadingAndTrailingSeparators(path));
    }

    @Test
    public void test_platformIndependentPath1() {
        String path = "/foo/bar/baz";
        assertSame(path, FileNameUtil.platformIndependentPath(path));
    }

    @Test
    public void test_platformIndependentPath2() {
        assertEquals("/foo/bar/baz", FileNameUtil.platformIndependentPath("\\foo\\bar\\baz"));
    }

    @Test
    public void test_platformIndependentPath3() {
        assertEquals("foo/bar/baz", FileNameUtil.platformIndependentPath("foo\\bar\\baz"));
    }

    @Test
    public void test_platformIndependentPath4() {
        assertEquals(
            "C:/Program Files/Traction/traction/server",
            FileNameUtil.platformIndependentPath("C:\\Program Files\\Traction\\traction\\server")
        );
    }

    @Test
    public void test_platformSpecificPath0() {
        assertNull(FileNameUtil.platformSpecificPath(null));
    }

    @Test
    public void test_platformSpecificPath1() {
        String path = File.separator + "foo" + File.separator + "bar" + File.separator + "baz";
        assertSame(path, FileNameUtil.platformSpecificPath(path));
    }

    /**
     * Effectively identical to {@link #test_platformIndependentPath1()}.
     */
    @Test
    public void test_filePathToUrlPath1() {
        String path = "/foo/bar/baz";
        assertSame(path, FileNameUtil.filePathToUrlPath(path));
    }

    /**
     * Effectively identical to {@link #test_platformIndependentPath2()}.
     */
    @Test
    public void test_filePathToUrlPath2() {
        assertEquals("/foo/bar/baz", FileNameUtil.filePathToUrlPath("\\foo\\bar\\baz"));
    }

    /**
     * Effectively identical to {@link #test_platformIndependentPath3()}.
     */
    @Test
    public void test_filePathToUrlPath3() {
        assertEquals("foo/bar/baz", FileNameUtil.filePathToUrlPath("foo\\bar\\baz"));
    }

    /**
     * Effectively identical to {@link #test_platformIndependentPath4()}, even with the : character.
     */
    @Test
    public void test_filePathToUrlPath4() {
        assertEquals(
            "C:/Program Files/Traction/traction/server",
            FileNameUtil.filePathToUrlPath("C:\\Program Files\\Traction\\traction\\server")
        );
    }

}

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
    public void testGetGoodNormalizedFileNameNull() {
        assertEquals("Untitled.txt", FileNameUtil.getGoodNormalizedFileName(null, null, "text/plain", true));
    }

    @Test
    public void testGetGoodNormalizedFileNameNullNoMimeType() {
        assertEquals("Untitled", FileNameUtil.getGoodNormalizedFileName(null, null, null, true));
    }

    @Test
    public void testGetGoodNormalizedFileNameEmpty() {
        assertEquals("Untitled.txt", FileNameUtil.getGoodNormalizedFileName("", null, "text/plain", true));
    }

    @Test
    public void testGetGoodNormalizedFileNameBlank() {
        assertEquals("Untitled.txt", FileNameUtil.getGoodNormalizedFileName("  ", null, "text/plain", true));
    }

    @Test
    public void testGetGoodNormalizedFileNameSimpleFixInvalidName() {
        assertEquals(
            "_my_-file.txt",
            FileNameUtil.getGoodNormalizedFileName("<my>-file.txt", null, "text/plain", true)
        );
    }

    @Test
    public void testGetGoodNormalizedFileNameSimpleFixAndAddExtension() {
        assertEquals("_my_-file.txt", FileNameUtil.getGoodNormalizedFileName("<my>-file", null, "text/plain", true));
    }

    @Test
    public void testGetGoodNormalizedFileNameWithAllInvalidCharactersReplaced() {
        assertEquals("______.txt", FileNameUtil.getGoodNormalizedFileName("<????>", null, "text/plain", true));
    }

    @Test
    public void testGetGoodNormalizedFileNameWithMultipleSpacesCollapsed() {
        assertEquals("foo bar.txt", FileNameUtil.getGoodNormalizedFileName("foo  bar.txt", null, "text/plain", true));
    }

    @Test
    public void testGetValidNormalizedFileValidDotFileNotChanged() {
        assertEquals(".foo", FileNameUtil.getGoodNormalizedFileName(".foo", null, "text/plain", true));
    }

    @Test
    public void test_checkFileName1A() {
        assertEquals(
            FileNameUtil.FileNameCheckResult.DISCOURAGED_WHITESPACE_SEQUENCE, FileNameUtil.checkFileName("my  file.txt")
        );
    }

    @Test
    public void test_checkFileName1B() {
        assertEquals(FileNameUtil.FileNameCheckResult.ILLEGAL_CHARACTERS, FileNameUtil.checkFileName("my\tfile.txt"));
    }

    @Test
    public void test_checkFileName1C() {
        assertEquals(FileNameUtil.FileNameCheckResult.ILLEGAL_CHARACTERS, FileNameUtil.checkFileName("my\nfile.txt"));
    }

    @Test
    public void test_checkFileName1D() {
        assertEquals(FileNameUtil.FileNameCheckResult.ILLEGAL_CHARACTERS, FileNameUtil.checkFileName("my\rfile.txt"));
    }

    @Test
    public void test_checkFileName2() {
        assertEquals(FileNameUtil.FileNameCheckResult.NO_PROBLEMS, FileNameUtil.checkFileName("my.file.txt"));
    }

    @Test
    public void test_checkFileName3A() {
        assertEquals(
            FileNameUtil.FileNameCheckResult.ILLEGAL_CHARACTERS,
            FileNameUtil.checkFileName("my\u0000file.txt")
        );
    }

    @Test
    public void test_checkFileName3B() {
        assertEquals(
            FileNameUtil.FileNameCheckResult.ILLEGAL_CHARACTERS,
            FileNameUtil.checkFileName("<my>-\u0000file.txt.")
        );
    }

    @Test
    public void test_checkFileName4() {
        assertEquals(FileNameUtil.FileNameCheckResult.NO_PROBLEMS, FileNameUtil.checkFileName("file"));
    }

    @Test
    public void test_checkFileName5A() {
        assertEquals(FileNameUtil.FileNameCheckResult.ILLEGAL_FILENAME, FileNameUtil.checkFileName("."));
    }

    @Test
    public void test_checkFileName5B() {
        assertEquals(FileNameUtil.FileNameCheckResult.ILLEGAL_FILENAME, FileNameUtil.checkFileName(".."));
    }

    @Test
    public void test_checkFileName5C() {
        assertEquals(FileNameUtil.FileNameCheckResult.ILLEGAL_DOT_SEQUENCE, FileNameUtil.checkFileName("...."));
    }

    @Test
    public void test_checkFileName5D() {
        assertEquals(FileNameUtil.FileNameCheckResult.ILLEGAL_DOT_SEQUENCE, FileNameUtil.checkFileName("file..txt"));
    }

    @Test
    public void test_checkFileName5E() {
        assertEquals(FileNameUtil.FileNameCheckResult.ILLEGAL_CHARACTERS, FileNameUtil.checkFileName("/"));
    }

    @Test
    public void test_checkFileName5F() {
        assertEquals(FileNameUtil.FileNameCheckResult.ILLEGAL_CHARACTERS, FileNameUtil.checkFileName("\\"));
    }

    @Test
    public void test_checkFileName8C() {
        assertEquals(FileNameUtil.FileNameCheckResult.ILLEGAL_CHARACTERS, FileNameUtil.checkFileName("\\"));
    }

    @Test
    public void test_checkFileName6A() {
        assertEquals(FileNameUtil.FileNameCheckResult.EMPTY_OR_BLANK, FileNameUtil.checkFileName(null));
    }

    @Test
    public void test_checkFileName6B() {
        assertEquals(FileNameUtil.FileNameCheckResult.EMPTY_OR_BLANK, FileNameUtil.checkFileName(""));
    }

    @Test
    public void test_checkFileName6C() {
        assertEquals(FileNameUtil.FileNameCheckResult.EMPTY_OR_BLANK, FileNameUtil.checkFileName(" "));
    }

    @Test
    public void test_checkFileName6D() {
        assertEquals(FileNameUtil.FileNameCheckResult.EMPTY_OR_BLANK, FileNameUtil.checkFileName("\t\t   "));
    }

    @Test
    public void test_checkFileName7A() {
        assertEquals(
            FileNameUtil.FileNameCheckResult.ILLEGAL_SPECIAL_WINDOWS_FILENAME, FileNameUtil.checkFileName("CON")
        );
    }

    @Test
    public void test_checkFileName7B() {
        assertEquals(
            FileNameUtil.FileNameCheckResult.ILLEGAL_SPECIAL_WINDOWS_FILENAME, FileNameUtil.checkFileName("CON.txt")
        );
    }

    @Test
    public void test_checkFileName7C() {
        assertEquals(
            FileNameUtil.FileNameCheckResult.ILLEGAL_SPECIAL_WINDOWS_FILENAME, FileNameUtil.checkFileName("COM¹")
        );
    }

    @Test
    public void test_checkFileName7D() {
        assertEquals(
            FileNameUtil.FileNameCheckResult.ILLEGAL_SPECIAL_WINDOWS_FILENAME, FileNameUtil.checkFileName("COM¹.gif")
        );
    }

    @Test
    public void test_checkFileName7E() {
        assertEquals(
            FileNameUtil.FileNameCheckResult.ILLEGAL_SPECIAL_WINDOWS_FILENAME, FileNameUtil.checkFileName("lpt3")
        );
    }

    @Test
    public void test_checkFileName8A() {
        assertEquals(
            FileNameUtil.FileNameCheckResult.ILLEGAL_CHARACTERS,
            FileNameUtil.checkFileName("my/file.txt")
        );
    }

    @Test
    public void test_checkFileName9A() {
        assertEquals(FileNameUtil.FileNameCheckResult.NO_PROBLEMS, FileNameUtil.checkFileName(".nvm"));
    }

    @Test
    public void test_checkFileName9B() {
        assertEquals(FileNameUtil.FileNameCheckResult.NO_PROBLEMS, FileNameUtil.checkFileName(".nvm"));
    }

    @Test
    public void test_checkFileName9C() {
        assertEquals(FileNameUtil.FileNameCheckResult.NO_PROBLEMS, FileNameUtil.checkFileName(".a"));
    }

    @Test
    public void test_getGoodFileName1A() {
        assertEquals("my  file.txt", FileNameUtil.getGoodFileName("my  file.txt", null, "text/plain"));
    }

    @Test
    public void test_getGoodFileName1B() {
        assertEquals("my file.txt", FileNameUtil.getGoodFileName("my\tfile.txt", null, "text/plain"));
    }

    @Test
    public void test_getGoodFileName1C() {
        assertEquals("my file.txt", FileNameUtil.getGoodFileName("my\nfile.txt", null, "text/plain"));
    }

    @Test
    public void test_getGoodFileName1D() {
        assertEquals("my file.txt", FileNameUtil.getGoodFileName("my\rfile.txt", null, "text/plain"));
    }

    @Test
    public void test_getGoodFileName1E() {
        assertEquals("my  file.txt", FileNameUtil.getGoodFileName("my\t file.txt", null, "text/plain"));
    }

    @Test
    public void test_getGoodFileName1F() {
        assertEquals("my   file.txt", FileNameUtil.getGoodFileName("my\t\n\rfile.txt", null, "text/plain"));
    }

    @Test
    public void test_getGoodFileName2() {
        assertEquals("my.file.txt", FileNameUtil.getGoodFileName("my.file.txt", null, "text/plain"));
    }

    @Test
    public void test_getGoodFileName3A() {
        assertEquals("my_file.txt", FileNameUtil.getGoodFileName("my\u0000file.txt", null, "text/plain"));
    }

    @Test
    public void test_getGoodFileName3B() {
        assertEquals(
            "_my_-_file.txt",
            FileNameUtil.getGoodFileName("<my>-\u0000file.txt.", null, "text/plain")
        );
    }

    @Test
    public void test_getGoodFileName4() {
        assertEquals("file", FileNameUtil.getGoodFileName("file", null, "image/gif"));
    }

    @Test
    public void test_getGoodFileName5() {
        assertEquals("Untitled.gif", FileNameUtil.getGoodFileName(".", null, "image/gif"));
    }

    @Test
    public void test_getGoodFileName6A() {
        assertEquals("Untitled.txt", FileNameUtil.getGoodFileName(null, null, "text/plain"));
    }

    @Test
    public void test_getGoodFileName6B() {
        assertEquals("Untitled.txt", FileNameUtil.getGoodFileName("", null, "text/plain"));
    }

    @Test
    public void test_getGoodFileName6C() {
        assertEquals("Untitled.txt", FileNameUtil.getGoodFileName(" ", null, "text/plain"));
    }

    @Test
    public void test_getGoodFileName6D() {
        assertEquals("Untitled.txt", FileNameUtil.getGoodFileName("\t\t   ", null, "text/plain"));
    }

    @Test
    public void test_getGoodFileName7A() {
        assertEquals("Untitled", FileNameUtil.getGoodFileName("CON", null, null));
    }

    @Test
    public void test_getGoodFileName7B() {
        assertEquals("Untitled.txt", FileNameUtil.getGoodFileName("CON.txt", null, "text/plain"));
    }

    @Test
    public void test_getGoodFileName7C() {
        assertEquals("Untitled", FileNameUtil.getGoodFileName("COM¹", null, null));
    }

    @Test
    public void test_getGoodFileName7D() {
        assertEquals("Untitled.gif", FileNameUtil.getGoodFileName("COM¹.gif", null, "image/gif"));
    }

    @Test
    public void test_getGoodFileName7E() {
        assertEquals("Untitled", FileNameUtil.getGoodFileName("lpt", null, "application/octet-stream"));
    }

    @Test
    public void test_getGoodFileName8A() {
        assertEquals("bar__.foo", FileNameUtil.getGoodFileName("bar\u007f\u0080.foo", null, "text/plain"));
    }

    @Test
    public void test_getGoodFileName8B() {
        assertEquals("bar__.foo", FileNameUtil.getGoodFileName("bar\u008f\u009f.foo", null, "text/plain"));
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

}

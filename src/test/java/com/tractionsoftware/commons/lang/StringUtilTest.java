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

package com.tractionsoftware.commons.lang;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link StringUtil}.
 *
 * <p>
 * For the tests below that need to use Unicode code points, we use various values, including the following:
 *
 * <dl>
 * <dd>U+1F602 (hex 0x1F602, dec 128514)</dd>
 * <dt>"FACE WITH TEARS OF JOY" 😂, Supplementary Multilingual Plane</dt>
 * <dd>U+1F603 (hex 0x1F603, dec 128515)</dd>
 * <dt>"SMILING FACE with OPEN MOUTH" 😃, Supplementary Multilingual Plane</dt>
 * <dd>U+1F605 (hex 0x1F605, dec 128517)</dd>
 * <dt>"Smiling Face With Open Mouth And Cold Sweat" 😅, Supplementary Multilingual Plane</dt>
 * <dd>U+4ECA (hex 0x4ECA, dec 20170)</dd>
 * <dt>今, Basic Multilingual Plane (CJK Unified Ideograph)</dt>
 * <dd>U+65E5 (hex 0x65E5, dec 26085)</dd>
 * <dt>日, Basic Multilingual Plane (CJK Unified Ideograph)</dt>
 * </dl>
 *
 * @author Andy Keller, Dave Shepperton
 */
public final class StringUtilTest {

    private static final String INDEX_OF_ANY_TEST1 = ": this is a :: ##test // string//that i'm using";
    //                                                01234567890123456789012345678901234567890123456789
    //                                                0         1         2         3         4

    private static final String INDEX_OF_ANY_TEST2 = "::: this is another :: test // string//that i'm using";
    //                                                01234567890123456789012345678901234567890123456789
    //                                                0         1         2         3         4

    private static final String TRIMMED_SUBSTRING_INPUT_1 = "foo";

    private static final String TRIMMED_SUBSTRING_INPUT_2 = "   ";

    private static final String TRIMMED_SUBSTRING_INPUT_3 = "food    ";

    private static final String TRIMMED_SUBSTRING_INPUT_4 = "  abc  ";

    private static final String TRIMMED_SUBSTRING_INPUT_5 = "abcdefghijklmnopqrstuvwxyz";

    private static final String TRIMMED_SUBSTRING_INPUT_6 = "re: I've fallen and I can't get up!";

    private static final int doIndexOfAnyCharTest1(int fromIndex) {
        return StringUtil.indexOfAny(INDEX_OF_ANY_TEST1, ":/#", fromIndex);
    }

    private static final int doIndexOfAnyStringTest1(int fromIndex) {
        return StringUtil.indexOfAny(INDEX_OF_ANY_TEST1, new String[] { "::", "/", "##" }, fromIndex);
    }

    private static final int doIndexOfAnyStringTest2(int fromIndex) {
        return StringUtil.indexOfAny(INDEX_OF_ANY_TEST2, new String[] { "::", "/", "##" }, fromIndex);
    }

    private static final void doJoinTest(Iterable<?> items, char separator, String expected) {
        assertEquals(expected, StringUtil.join(items, separator));
    }

    private static final void doJoinTest(Iterable<?> items, String separator, String expected) {
        assertEquals(expected, StringUtil.join(items, separator));
    }

    private static final void doJoinTest(String[] items, String separator, String expected) {
        assertEquals(expected, StringUtil.join(items, separator));
    }

    private static final void doIndexOfIgnoreCaseTestNotFound(String str, char c) {
        doIndexOfIgnoreCaseTest(str, c, StringUtils.INDEX_NOT_FOUND);
    }

    private static final void doIndexOfIgnoreCaseTest(String str, char c, int expectedIndex) {
        assertEquals(
            expectedIndex,
            StringUtil.indexOfIgnoreCase(str, c)
        );
    }

    private static final void doIndexOfIgnoreCaseTestNotFoundFromIndex(String str, char c, int fromIndex) {
        doIndexOfIgnoreCaseTestFromIndex(str, c, fromIndex, StringUtils.INDEX_NOT_FOUND);
    }

    private static final void doIndexOfIgnoreCaseTestFromIndex(String str, char c, int fromIndex, int expectedIndex) {
        assertEquals(
            expectedIndex,
            StringUtil.indexOfIgnoreCase(str, c, fromIndex)
        );
    }

    private static final void doContainsCharactersInOrderTestFound(String str, String search) {
        assertTrue(StringUtil.containsCharactersInOrder(str, search));
    }

    private static final void doContainsCharactersInOrderTestNotFound(String str, String search) {
        assertFalse(StringUtil.containsCharactersInOrder(str, search));
    }

    private static final void doContainsCharactersInOrderIgnoreCaseTestFound(String str, String search) {
        assertTrue(StringUtil.containsCharactersInOrderIgnoreCase(str, search));
    }

    private static final void doContainsCharactersInOrderIgnoreCaseTestNotFound(String str, String search) {
        assertFalse(StringUtil.containsCharactersInOrderIgnoreCase(str, search));
    }

    private static final void doGetTrimmedNonEmptyListElementsTest(String input, List<String> expectedAsList) {
        assertEquals(
            expectedAsList,
            StringUtil.getTrimmedNonEmptyListElements(input).toList()
        );
    }

    private static final void doGetTrimmedNonEmptyPartsTest(String input, List<String> expectedAsList) {
        assertEquals(
            expectedAsList,
            StringUtil.getTrimmedNonEmptyParts(input).toList()
        );
    }

    private static final void doTrimNotBlankXTestValue(String input, String expected) {
        assertEquals(expected, StringUtil.trimNotBlankX(input, "test"));
    }

    private static final void doTrimNotBlankXTestError(String input, Class<? extends Exception> errorType) {
        assertThrows(errorType, () -> StringUtil.trimNotBlankX(input, "test"));
    }

    private static final void doCaseInsensitiveBinarySearchFirstCodePointTest(List<String> input, int codePoint, int expected) {
        assertEquals(expected, StringUtil.caseInsensitiveBinarySearchFirstCodePoint(input, codePoint));
    }

    private static final void doCaseInsensitiveBinarySearchFirstCodePointTestError(List<String> input, int codePoint, Class<? extends Exception> errorType) {
        assertThrows(errorType, () -> StringUtil.caseInsensitiveBinarySearchFirstCodePoint(input, codePoint));
    }

    @Test
    public void testIndexOfAnyChar1() {
        assertEquals(0, doIndexOfAnyCharTest1(0));
    }

    @Test
    public void testIndexOfAnyChar2() {
        assertEquals(StringUtils.INDEX_NOT_FOUND, doIndexOfAnyCharTest1(80));
    }

    @Test
    public void testIndexOfAnyChar3() {
        assertEquals(12, doIndexOfAnyCharTest1(1));
    }

    @Test
    public void testIndexOfAnyChar4() {
        assertEquals(12, doIndexOfAnyCharTest1(12));
    }

    @Test
    public void testIndexOfAnyChar5() {
        assertEquals(13, doIndexOfAnyCharTest1(13));
    }

    @Test
    public void testIndexOfAnyChar6() {
        assertEquals(31, doIndexOfAnyCharTest1(30));
    }

    @Test
    public void testIndexOfAnyChar7() {
        assertEquals(0, doIndexOfAnyCharTest1(-1));
    }

    @Test
    public void testIndexOfAnyString1() {
        assertEquals(12, doIndexOfAnyStringTest1(0));
    }

    @Test
    public void testIndexOfAnyString2() {
        assertEquals(12, doIndexOfAnyStringTest1(-1));
    }

    @Test
    public void testIndexOfAnyString3() {
        assertEquals(15, doIndexOfAnyStringTest1(15));
    }

    @Test
    public void testIndexOfAnyString4() {
        assertEquals(22, doIndexOfAnyStringTest1(16));
    }

    @Test
    public void testIndexOfAnyString5() {
        assertEquals(23, doIndexOfAnyStringTest1(23));
    }

    @Test
    public void testIndexOfAnyString6() {
        assertEquals(StringUtils.INDEX_NOT_FOUND, doIndexOfAnyStringTest1(34));
    }

    @Test
    public void testIndexOfAnyString7() {
        assertEquals(0, doIndexOfAnyStringTest2(0));
    }

    @Test
    public void testIndexOfAnyString8() {
        assertEquals(1, doIndexOfAnyStringTest2(1));
    }

    @Test
    public void testIndexOfAnyString9() {
        assertEquals(20, doIndexOfAnyStringTest2(2));
    }

    @Test
    public void testRemoveLineBreaks_null() {
        assertNull(StringUtil.removeLineBreaks(null));
    }

    @Test
    public void testRemoveLineBreaks_empty() {
        assertEquals("", StringUtil.removeLineBreaks(""));
    }

    @Test
    public void testRemoveLineBreaks_noLineBreaks() {
        String a = "this has no line breaks or anything that should look like one";
        assertEquals(a, StringUtil.removeLineBreaks(a));
    }

    @Test
    public void testRemoveLineBreaks_singleLineBreak1() {
        String a = "a\n\rb";
        assertEquals("ab", StringUtil.removeLineBreaks(a));
    }

    @Test
    public void testRemoveLineBreaks_singleLineBreak2() {
        String a = "this has no one line break -\nthere it was";
        assertEquals("this has no one line break -there it was", StringUtil.removeLineBreaks(a));
    }

    @Test
    public void testRemoveLineBreaks_singleLineBreak3() {
        String a = "this has no one line break -\n\rthere it was";
        assertEquals("this has no one line break -there it was", StringUtil.removeLineBreaks(a));
    }

    @Test
    public void testRemoveLineBreaks_singleLineBreakAtStart() {
        String a = "\nJust one line break at the beginning";
        assertEquals("Just one line break at the beginning", StringUtil.removeLineBreaks(a));
    }

    @Test
    public void testRemoveLineBreaks_singleLineBreakAtEnd() {
        String a = "Just one line break at the end\r";
        assertEquals("Just one line break at the end", StringUtil.removeLineBreaks(a));
    }

    @Test
    public void testRemoveLineBreaks_lineBreaksAtBoundaries() {
        String a =
            "\n\r\n\r  There are multiple line breaks in here \n\rincluding\n\r\n\n\n at the end and beginning  \n\r";
        assertEquals(
            "  There are multiple line breaks in here including at the end and beginning  ",
            StringUtil.removeLineBreaks(a)
        );
    }

    @Test
    public void testRemoveLineBreaks_allLineBreaks() {
        String a = "\n\r\n\r\n\n\n\n\r\r\r\r\n\n\n\r\n\r";
        assertEquals("", StringUtil.removeLineBreaks(a));
    }

    @Test
    public void test_collapseAndNormalizeWhitespaceNull() {
        assertNull(StringUtil.collapseAndNormalizeWhitespace(null, false));
    }

    @Test
    public void test_collapseAndNormalizeWhitespace1() {
        assertEquals("foo bar baz", StringUtil.collapseAndNormalizeWhitespace("foo bar baz", false));
    }

    @Test
    public void test_collapseAndNormalizeWhitespace2() {
        assertEquals("foo bar baz", StringUtil.collapseAndNormalizeWhitespace("foo  bar  baz", false));
    }

    @Test
    public void test_collapseAndNormalizeWhitespace3() {
        assertEquals(" foo bar baz ", StringUtil.collapseAndNormalizeWhitespace(" foo  bar  baz ", false));
    }

    @Test
    public void test_collapseAndNormalizeWhitespace4() {
        assertEquals(
            " foo bar baz ",
            StringUtil.collapseAndNormalizeWhitespace("    foo      bar           baz ", false)
        );
    }

    @Test
    public void test_collapseAndNormalizeWhitespace5() {
        assertEquals(" foo bar baz ", StringUtil.collapseAndNormalizeWhitespace("\tfoo  bar  baz\n", false));
    }

    @Test
    public void test_collapseAndNormalizeWhitespace6() {
        assertEquals("\u00a0 foo bar baz ", StringUtil.collapseAndNormalizeWhitespace("\u00a0 foo  bar  baz\n", false));
    }

    @Test
    public void test_collapseAndNormalizeWhitespace7() {
        assertEquals(" foo bar baz ", StringUtil.collapseAndNormalizeWhitespace("\u00a0 foo  bar  baz\n", true));
    }

    @Test
    public void test_collapseAndNormalizeWhitespace8() {
        assertEquals(" foo bar baz ", StringUtil.collapseAndNormalizeWhitespace("\u200b foo  bar  baz\n", true));
    }

    @Test
    public void test_collapseAndNormalizeWhitespace9() {
        assertEquals(" foo bar baz ", StringUtil.collapseAndNormalizeWhitespace("\nfoo\nbar\n\nbaz\n", false));
    }

    @Test
    public void test_normalizeAlternativeWhitespaceNull() {
        assertNull(StringUtil.normalizeAlternativeWhitespace(null));
    }

    @Test
    public void test_normalizeAlternativeWhitespaceEmpty() {
        assertEquals("", StringUtil.normalizeAlternativeWhitespace(""));
    }

    @Test
    public void test_normalizeAlternativeWhitespaceBlankNormalSpaces() {
        assertEquals("   \t\t\t\n\n", StringUtil.normalizeAlternativeWhitespace("   \t\t\t\n\n"));
    }

    @Test
    public void test_normalizeAlternativeWhitespaceSomeZWSs() {
        assertEquals("     ", StringUtil.normalizeAlternativeWhitespace("\u200b\u200b\u200b\u200b\u200b"));
    }

    @Test
    public void test_normalizeAlternativeWhitespaceSomeNBSPs() {
        assertEquals("     ", StringUtil.normalizeAlternativeWhitespace("\u00a0\u00a0\u00a0\u00a0\u00a0"));
    }

    @Test
    public void test_normalizeAlternativeWhitespaceSomeOfEach() {
        assertEquals("       ", StringUtil.normalizeAlternativeWhitespace("\u00a0 \u200b \u200b \u00a0"));
    }

    @Test
    public void test_getLines1() {
        assertEquals(ImmutableList.of("a", "b", "c"), StringUtil.getLines("a\nb\nc").toList());
    }

    @Test
    public void test_getLines2() {
        assertEquals(ImmutableList.of("", "b", ""), StringUtil.getLines("\nb\n").toList());
    }

    @Test
    public void test_getLines3() {
        assertEquals(
            ImmutableList.of("", "", "", ""),
            StringUtil.getLines("\r\n\n\r\r\n").toList()
        );
    }

    @Test
    public void test_getLines4() {
        assertEquals(
            ImmutableList.of(" ", "  ", "   ", "    ", "     "),
            StringUtil.getLines(" \r\n  \n   \r    \r\n     ").toList()
        );
    }

    @Test
    public void test_getTrimmedNonEmptyLines1() {
        assertEquals(
            ImmutableList.of(),
            StringUtil.getTrimmedNonEmptyLines("\r\n\n\r\r\n").toList()
        );
    }

    @Test
    public void test_getTrimmedNonEmptyLines2() {
        assertEquals(
            ImmutableList.of("qrs", "tuv"),
            StringUtil.getTrimmedNonEmptyLines("\n     qrs\n\n\n\r\r\n\ntuv\n \n  \n     \n          \n      ").toList()
        );
    }

    @Test
    public void test_getTrimmedNonEmptyListElements0a() {
        doGetTrimmedNonEmptyListElementsTest(
            null,
            ImmutableList.of()
        );
    }

    @Test
    public void test_getTrimmedNonEmptyListElements0b() {
        doGetTrimmedNonEmptyListElementsTest(
            "",
            ImmutableList.of()
        );
    }

    @Test
    public void test_getTrimmedNonEmptyListElements0c() {
        doGetTrimmedNonEmptyListElementsTest(
            "    ,  , , , ,,,       \t\t\t\n\r    ,",
            ImmutableList.of()
        );
    }

    @Test
    public void test_getTrimmedNonEmptyListElements1a() {
        doGetTrimmedNonEmptyListElementsTest(
            "a,b,c",
            ImmutableList.of("a", "b", "c")
        );
    }

    @Test
    public void test_getTrimmedNonEmptyListElements1b() {
        doGetTrimmedNonEmptyListElementsTest(
            "  a,     b,c   ",
            ImmutableList.of("a", "b", "c")
        );
    }

    @Test
    public void test_getTrimmedNonEmptyListElements1c() {
        doGetTrimmedNonEmptyListElementsTest(
            "  a,,,,,,,,\t\n,\f\n\t   ,     b,c   ",
            ImmutableList.of("a", "b", "c")
        );
    }

    @Test
    public void test_getTrimmedNonEmptyParts0a() {
        doGetTrimmedNonEmptyPartsTest(
            null,
            ImmutableList.of()
        );
    }

    @Test
    public void test_getTrimmedNonEmptyParts0b() {
        doGetTrimmedNonEmptyPartsTest(
            "",
            ImmutableList.of()
        );
    }

    @Test
    public void test_getTrimmedNonEmptyParts0c() {
        doGetTrimmedNonEmptyPartsTest(
            "       \n\n\r\t",
            ImmutableList.of()
        );
    }

    @Test
    public void test_getTrimmedNonEmptyParts1a() {
        doGetTrimmedNonEmptyPartsTest(
            "a b c",
            ImmutableList.of("a", "b", "c")
        );
    }

    @Test
    public void test_getTrimmedNonEmptyParts1b() {
        doGetTrimmedNonEmptyPartsTest(
            "a\nb\n\nc",
            ImmutableList.of("a", "b", "c")
        );
    }

    @Test
    public void test_getTrimmedNonEmptyParts1c() {
        doGetTrimmedNonEmptyPartsTest(
            "         a\t\t  b\t\n\n\n\n\n\nc         ",
            ImmutableList.of("a", "b", "c")
        );
    }

    @Test
    public void test_getTrimmedNonEmptyParts1d() {
        doGetTrimmedNonEmptyPartsTest(
            "  a,b,c  ",
            ImmutableList.of("a,b,c")
        );
    }

    @Test
    public void test_getPrefixIntersectionNone1() {
        assertEquals(
            ImmutableSet.of(),
            StringUtil.getPrefixIntersection(
                ImmutableSet.of(),
                ImmutableSet.of()
            )
        );
    }

    @Test
    public void test_getPrefixIntersectionNone2() {
        assertEquals(
            ImmutableSet.of(),
            StringUtil.getPrefixIntersection(
                ImmutableSet.of(),
                ImmutableSet.of("tuv", "qrs")
            )
        );
    }

    @Test
    public void test_getPrefixIntersectionNone3() {
        assertEquals(
            ImmutableSet.of(),
            StringUtil.getPrefixIntersection(
                ImmutableSet.of("abc", "xyz"),
                ImmutableSet.of()
            )
        );
    }

    @Test
    public void test_getPrefixIntersectionNone4() {
        assertEquals(
            ImmutableSet.of(),
            StringUtil.getPrefixIntersection(
                ImmutableSet.of("abc", "xyz"),
                ImmutableSet.of("tuv", "qrs")
            )
        );
    }

    @Test
    public void test_getPrefixIntersection1() {
        assertEquals(
            ImmutableSet.of("a", "x"),
            StringUtil.getPrefixIntersection(
                ImmutableSet.of("abc", "xyz"),
                ImmutableSet.of("a", "x")
            )
        );
    }

    @Test
    public void test_getPrefixIntersection2() {
        assertEquals(
            ImmutableSet.of("a", "x"),
            StringUtil.getPrefixIntersection(
                ImmutableSet.of("a", "x"),
                ImmutableSet.of("abc", "xyz")
            )
        );
    }

    @Test
    public void test_getPrefixIntersection3() {
        assertEquals(
            ImmutableSet.of("p:4/foo/"),
            StringUtil.getPrefixIntersection(
                ImmutableSet.of("p:4/foo/"),
                ImmutableSet.of("p:4/foo/bar")
            )
        );
    }

    @Test
    public void test_getPrefixIntersectionTrivial1() {
        assertEquals(
            ImmutableSet.of(),
            StringUtil.getPrefixSpanningSet(ImmutableSet.of())
        );
    }

    @Test
    public void test_getPrefixIntersectionTrivial2() {
        assertEquals(
            ImmutableSet.of("tuv", "qrs"),
            StringUtil.getPrefixSpanningSet(ImmutableSet.of("tuv", "qrs"))
        );
    }

    @Test
    public void test_getPrefixIntersectionTrivial3() {
        assertEquals(
            ImmutableSet.of("abc", "xyz"),
            StringUtil.getPrefixSpanningSet(ImmutableSet.of("abc", "xyz"))
        );
    }

    @Test
    public void test_getPrefixIntersectionTrivial4() {
        assertEquals(
            ImmutableSet.of("abc", "xyz", "tuv", "qrs"),
            StringUtil.getPrefixSpanningSet(ImmutableSet.of("abc", "xyz", "tuv", "qrs"))
        );
    }

    @Test
    public void test_getPrefixUnion1() {
        assertEquals(
            ImmutableSet.of("a", "x"),
            StringUtil.getPrefixSpanningSet(ImmutableSet.of("abc", "xyz", "a", "x"))
        );
    }

    @Test
    public void test_getPrefixUnion2() {
        assertEquals(
            ImmutableSet.of("a", "x"),
            StringUtil.getPrefixSpanningSet(ImmutableSet.of("a", "x", "abc", "xyz"))
        );
    }

    @Test
    public void test_getPrefixUnion3() {
        assertEquals(
            ImmutableSet.of("p:4/foo/", "x:y"),
            StringUtil.getPrefixSpanningSet(ImmutableSet.of("p:4/foo/", "x:y", "p:4/foo/bar"))
        );
    }

    @Test
    public void test_getPrefixUnion4() {
        assertEquals(
            ImmutableSet.of("a", "x"),
            StringUtil.getPrefixSpanningSet(ImmutableSet.of("a", "ab", "abc", "abcd", "x"))
        );
    }

    @Test
    public void test_getPrefixUnion5() {
        assertEquals(
            ImmutableSet.of("a", "x"),
            StringUtil.getPrefixSpanningSet(ImmutableSet.of("abc", "a", "ab", "abcd", "x"))
        );
    }

    private static final boolean doSingleQuotationEnclosureTest(String text) {
        return StringUtil.isEnclosed(text, StringUtil.StandardTextEnclosureScheme.SINGLE_QUOTATION_MARKS);
    }

    private static final boolean doDoubleQuotationEnclosureTest(String text) {
        return StringUtil.isEnclosed(text, StringUtil.StandardTextEnclosureScheme.DOUBLE_QUOTATION_MARKS);
    }

    private static final boolean doQuotationEnclosureTest(String text) {
        return StringUtil.isEnclosed(
            text,
            StringUtil.StandardTextEnclosureScheme.SINGLE_QUOTATION_MARKS,
            StringUtil.StandardTextEnclosureScheme.DOUBLE_QUOTATION_MARKS
        );
    }

    private static final String doSingleQuotationEnclosureExtractionTest(String text) {
        return StringUtil.extractEnclosed(text, StringUtil.StandardTextEnclosureScheme.SINGLE_QUOTATION_MARKS);
    }

    private static final String doDoubleQuotationEnclosureExtractionTest(String text) {
        return StringUtil.extractEnclosed(text, StringUtil.StandardTextEnclosureScheme.DOUBLE_QUOTATION_MARKS);
    }

    private static final String doQuotationEnclosureExtractionTest(String text) {
        return StringUtil.extractEnclosed(
            text,
            StringUtil.StandardTextEnclosureScheme.SINGLE_QUOTATION_MARKS,
            StringUtil.StandardTextEnclosureScheme.DOUBLE_QUOTATION_MARKS
        );
    }

    @Test
    public void test_isEnclosed1() {
        assertFalse(doSingleQuotationEnclosureTest(null));
    }

    @Test
    public void test_isEnclosed2() {
        assertFalse(doSingleQuotationEnclosureTest(""));
    }

    @Test
    public void test_isEnclosed3() {
        assertFalse(doSingleQuotationEnclosureTest("  xyz   "));
    }

    @Test
    public void test_isEnclosed4() {
        assertFalse(doSingleQuotationEnclosureTest("xyz'"));
    }

    @Test
    public void test_isEnclosed5() {
        assertTrue(doSingleQuotationEnclosureTest("'abc'"));
    }

    @Test
    public void test_isEnclosed6() {
        assertTrue(doQuotationEnclosureTest("\"abc\""));
    }

    @Test
    public void test_isEnclosed7() {
        assertTrue(doQuotationEnclosureTest("   \"abc\"  "));
    }

    @Test
    public void test_isEnclosed8() {
        assertTrue(doQuotationEnclosureTest("   '\"\"'  "));
    }

    @Test
    public void test_isEnclosed9() {
        assertTrue(doQuotationEnclosureTest("   ''  "));
    }

    @Test
    public void test_extractEnclosed1() {
        assertNull(doSingleQuotationEnclosureExtractionTest(null));
    }

    @Test
    public void test_extractEnclosed2() {
        assertEquals("", doSingleQuotationEnclosureExtractionTest(""));
    }

    @Test
    public void test_extractEnclosed3() {
        assertEquals("  xyz   ", doSingleQuotationEnclosureExtractionTest("  xyz   "));
    }

    @Test
    public void test_extractEnclosed4() {
        assertEquals("xyz'", doSingleQuotationEnclosureExtractionTest("xyz'"));
    }

    @Test
    public void test_extractEnclosed5() {
        assertEquals("abc", doSingleQuotationEnclosureExtractionTest("'abc'"));
    }

    @Test
    public void test_extractEnclosed6() {
        assertEquals("abc", doQuotationEnclosureExtractionTest("\"abc\""));
    }

    @Test
    public void test_extractEnclosed7() {
        assertEquals("abc", doQuotationEnclosureExtractionTest("   \"abc\"  "));
    }

    @Test
    public void test_extractEnclosed8() {
        assertEquals("\"\"", doQuotationEnclosureExtractionTest("   '\"\"'  "));
    }

    @Test
    public void test_extractEnclosed9() {
        assertEquals("", doQuotationEnclosureExtractionTest("   ''  "));
    }

    @Test
    public void test_extractEnclosed10() {
        assertEquals(" ' ", doSingleQuotationEnclosureExtractionTest(" ' "));
    }

    @Test
    public void testJoin0a() {
        doJoinTest(
            null,
            ',',
            ""
        );
    }

    @Test
    public void testJoin0b() {
        doJoinTest(
            (Iterable<?>) null,
            ",",
            ""
        );
    }

    @Test
    public void testJoin0c() {
        doJoinTest(
            (String[]) null,
            ",",
            ""
        );
    }

    @Test
    public void testJoin1a() {
        doJoinTest(
            ImmutableList.of("a", "b", "c"),
            ',',
            "a,b,c"
        );
    }

    @Test
    public void testJoin1b() {
        doJoinTest(
            ImmutableList.of("a", "b", "c"),
            ",",
            "a,b,c"
        );
    }

    @Test
    public void testJoin1c() {
        doJoinTest(
            new String[] { "a", "b", "c" },
            ",",
            "a,b,c"
        );
    }

    @Test
    public void testJoin2a() {
        doJoinTest(
            ImmutableList.of("Fred", "Jack", "77", "e=mc^2"),
            ',',
            "Fred,Jack,77,e=mc^2"
        );
    }

    @Test
    public void testJoin2b() {
        doJoinTest(
            ImmutableList.of("Fred", "Jack", "77", "e=mc^2"),
            ",",
            "Fred,Jack,77,e=mc^2"
        );
    }

    @Test
    public void testJoin2c() {
        doJoinTest(
            new String[] { "Fred", "Jack", "77", "e=mc^2" },
            ",",
            "Fred,Jack,77,e=mc^2"
        );
    }

    @Test
    public void testJoin3a() {
        doJoinTest(
            ImmutableList.of("Public", "Private", "Comment"),
            '|',
            "Public|Private|Comment"
        );
    }

    @Test
    public void testJoin3b() {
        doJoinTest(
            new String[] { "Public", "Private", "Comment" },
            "|",
            "Public|Private|Comment"
        );
    }

    @Test
    public void testJoin3c() {
        doJoinTest(
            ImmutableList.of("Public", "Private", "Comment"),
            "|",
            "Public|Private|Comment"
        );
    }

    @Test
    public void testJoin4a() {
        doJoinTest(
            ImmutableList.of("123", "456", "789", "000", ""),
            "<BR>",
            "123<BR>456<BR>789<BR>000<BR>"
        );
    }

    @Test
    public void testJoin4b() {
        doJoinTest(
            new String[] { "123", "456", "789", "000", "" },
            "<BR>",
            "123<BR>456<BR>789<BR>000<BR>"
        );
    }

    @Test
    public void testJoin5a() {
        doJoinTest(
            ImmutableList.of(),
            ',',
            ""
        );
    }

    @Test
    public void testJoin5b() {
        doJoinTest(
            new String[0],
            ",",
            ""
        );
    }

    @Test
    public void testJoin5c() {
        doJoinTest(
            ImmutableList.of(),
            ",",
            ""
        );
    }

    @Test
    public void test_collapseConsecutiveCharacters1() {
        String input = "XXyyZZ";
        assertSame(
            input,
            StringUtil.collapseConsecutiveCharacters(input, 'x')
        );
    }

    @Test
    public void test_collapseConsecutiveCharacters2() {
        assertEquals(
            "xyyzz",
            StringUtil.collapseConsecutiveCharacters("xxyyzz", 'x')
        );
    }

    @Test
    public void test_collapseConsecutiveCharacters3() {
        assertEquals(
            "foo.doc",
            StringUtil.collapseConsecutiveCharacters("foo..doc", '.')
        );
    }

    @Test
    public void test_collapseConsecutiveCharacters4() {
        assertEquals(
            "foo.doc",
            StringUtil.collapseConsecutiveCharacters("foo...doc", '.')
        );
    }

    @Test
    public void test_collapseConsecutiveCharacters5() {
        assertEquals(
            "foo.doc",
            StringUtil.collapseConsecutiveCharacters("foo....doc", '.')
        );
    }

    @Test
    public void test_collapseConsecutiveCharacters6() {
        assertEquals(
            "/",
            StringUtil.collapseConsecutiveCharacters("///", '/')
        );
    }

    @Test
    public void test_collapseConsecutiveCharacters7() {
        assertEquals(
            "/x/",
            StringUtil.collapseConsecutiveCharacters("//x//", '/')
        );
    }

    @Test
    public void test_collapseConsecutiveCharacters8() {
        assertEquals(
            "/foo/bar/baz/",
            StringUtil.collapseConsecutiveCharacters("//////foo/bar/baz/////", '/')
        );
    }

    @Test
    public void test_collapseConsecutiveCharacters9() {
        assertEquals(
            "/x/",
            StringUtil.collapseConsecutiveCharacters("/x/", '/')
        );
    }

    @Test
    public void test_collapseConsecutiveCharacters10() {
        assertEquals(
            "/x/",
            StringUtil.collapseConsecutiveCharacters("///x///", '/')
        );
    }

    @Test
    public void test_collapseConsecutiveCharacters11() {
        assertEquals(
            ".ABC.ABC.",
            StringUtil.collapseConsecutiveCharacters("...ABC.....ABC.", '.')
        );
    }

    @Test
    public void test_getTrimmedSubstring1a() {
        assertSame(
            StringUtils.EMPTY,
            StringUtil.getTrimmedSubstring(TRIMMED_SUBSTRING_INPUT_1, 99, 1000)
        );
    }

    @Test
    public void test_getTrimmedSubstring1b() {
        assertSame(
            TRIMMED_SUBSTRING_INPUT_1,
            StringUtil.getTrimmedSubstring(TRIMMED_SUBSTRING_INPUT_1, 0, 3)
        );
    }

    @Test
    public void test_getTrimmedSubstring1c() {
        assertSame(
            TRIMMED_SUBSTRING_INPUT_1,
            StringUtil.getTrimmedSubstring(TRIMMED_SUBSTRING_INPUT_1, -77, 44)
        );
    }

    @Test
    public void test_getTrimmedSubstring2a() {
        assertSame(
            StringUtils.EMPTY,
            StringUtil.getTrimmedSubstring(TRIMMED_SUBSTRING_INPUT_2, 0, 3)
        );
    }

    @Test
    public void test_getTrimmedSubstring2b() {
        assertSame(
            StringUtils.EMPTY,
            StringUtil.getTrimmedSubstring(TRIMMED_SUBSTRING_INPUT_2, 0, 0)
        );
    }

    @Test
    public void test_getTrimmedSubstring2c() {
        assertSame(
            StringUtils.EMPTY,
            StringUtil.getTrimmedSubstring(TRIMMED_SUBSTRING_INPUT_2, -1, 10)
        );
    }

    @Test
    public void test_getTrimmedSubstring2d() {
        assertSame(
            StringUtils.EMPTY,
            StringUtil.getTrimmedSubstring(TRIMMED_SUBSTRING_INPUT_2, 0, 1)
        );
    }

    @Test
    public void test_getTrimmedSubstring3a() {
        assertEquals(
            TRIMMED_SUBSTRING_INPUT_3.substring(0, 3),
            StringUtil.getTrimmedSubstring(TRIMMED_SUBSTRING_INPUT_3, 0, 3)
        );
    }

    @Test
    public void test_getTrimmedSubstring3b() {
        assertEquals(
            TRIMMED_SUBSTRING_INPUT_3.substring(0, 4),
            StringUtil.getTrimmedSubstring(TRIMMED_SUBSTRING_INPUT_3, 0, 4)
        );
    }

    @Test
    public void test_getTrimmedSubstring3c() {
        assertEquals(
            TRIMMED_SUBSTRING_INPUT_3.trim(),
            StringUtil.getTrimmedSubstring(TRIMMED_SUBSTRING_INPUT_3, 0, 8)
        );
    }

    @Test
    public void test_getTrimmedSubstring3d() {
        assertEquals(
            TRIMMED_SUBSTRING_INPUT_3.substring(0, 1),
            StringUtil.getTrimmedSubstring(TRIMMED_SUBSTRING_INPUT_3, 0, 1)
        );
    }

    @Test
    public void test_getTrimmedSubstring4a() {
        assertEquals(
            TRIMMED_SUBSTRING_INPUT_4.trim(),
            StringUtil.getTrimmedSubstring(TRIMMED_SUBSTRING_INPUT_4, 0, 7)
        );
    }

    @Test
    public void test_getTrimmedSubstring4b() {
        assertEquals(
            TRIMMED_SUBSTRING_INPUT_4.trim(),
            StringUtil.getTrimmedSubstring(TRIMMED_SUBSTRING_INPUT_4, 0)
        );
    }

    @Test
    public void test_getTrimmedSubstring4c() {
        assertEquals(
            TRIMMED_SUBSTRING_INPUT_4.trim(),
            StringUtil.getTrimmedSubstring(TRIMMED_SUBSTRING_INPUT_4, 1, 6)
        );
    }

    @Test
    public void test_getTrimmedSubstring4d() {
        assertEquals(
            TRIMMED_SUBSTRING_INPUT_4.trim(),
            StringUtil.getTrimmedSubstring(TRIMMED_SUBSTRING_INPUT_4, 2, 5)
        );
    }

    @Test
    public void test_getTrimmedSubstring4e() {
        assertEquals(
            TRIMMED_SUBSTRING_INPUT_4.substring(4, 5),
            StringUtil.getTrimmedSubstring(TRIMMED_SUBSTRING_INPUT_4, 4, 5)
        );
    }

    @Test
    public void test_getTrimmedSubstring4f() {
        assertSame(
            StringUtils.EMPTY,
            StringUtil.getTrimmedSubstring(TRIMMED_SUBSTRING_INPUT_4, 4, 3)
        );
    }

    @Test
    public void test_getTrimmedSubstring4g() {
        assertSame(
            StringUtils.EMPTY,
            StringUtil.getTrimmedSubstring(TRIMMED_SUBSTRING_INPUT_4, Integer.MAX_VALUE, Integer.MAX_VALUE)
        );
    }

    @Test
    public void test_getTrimmedSubstring4h() {
        assertEquals(
            TRIMMED_SUBSTRING_INPUT_4.trim(),
            StringUtil.getTrimmedSubstring(TRIMMED_SUBSTRING_INPUT_4, Integer.MIN_VALUE, Integer.MAX_VALUE)
        );
    }

    @Test
    public void test_getTrimmedSubstring5a() {
        assertEquals(
            TRIMMED_SUBSTRING_INPUT_5.substring(23, 26),
            StringUtil.getTrimmedSubstring(TRIMMED_SUBSTRING_INPUT_5, 23, 26)
        );
    }

    @Test
    public void test_getTrimmedSubstring5b() {
        assertEquals(
            TRIMMED_SUBSTRING_INPUT_5.substring(23, 26),
            StringUtil.getTrimmedSubstring(TRIMMED_SUBSTRING_INPUT_5, 23, 30)
        );
    }

    @Test
    public void test_getTrimmedSubstring6a() {
        assertEquals(
            TRIMMED_SUBSTRING_INPUT_6.substring(4),
            StringUtil.getTrimmedSubstring(TRIMMED_SUBSTRING_INPUT_6, 3)
        );
    }

    @Test
    public void test_getTrimmedSubstring7a() {
        String headerPrefix = "Message-ID:";
        String id = "   <da552001-112e-41dc-ab9b-cc9915c6caec@example.com>    ";
        assertEquals(
            id.trim(),
            StringUtil.getTrimmedSubstring(
                headerPrefix + id, headerPrefix.length()
            )
        );
    }

    @Test
    public void test_getTrimmedSubstring7b() {
        String headerPrefix = "In-Reply-To:";
        String id = "\t<f5dced24-4df4-4b23-a596-c5b091b60b23@example.com>\t\t\t\t";
        assertEquals(
            id.trim(),
            StringUtil.getTrimmedSubstring(
                headerPrefix + id, headerPrefix.length()
            )
        );
    }

    @Test
    public void test_startsWithChar0a() {
        assertFalse(
            StringUtil.startsWith(null, '/')
        );
    }

    @Test
    public void test_startsWithChar0b() {
        assertFalse(
            StringUtil.startsWith("", 'a')
        );
    }

    @Test
    public void test_startsWithChar1() {
        assertTrue(
            StringUtil.startsWith("/Foo/bar/", '/')
        );
    }

    @Test
    public void test_startsWithChar2() {
        assertFalse(
            StringUtil.startsWith("Foo/bar/", '/')
        );
    }

    @Test
    public void test_startsWithAnyChar0a() {
        assertFalse(
            StringUtil.startsWithAny(null, '/', '\\')
        );
    }

    @Test
    public void test_startsWithAnyChar0b() {
        assertFalse(
            StringUtil.startsWithAny("", 'a', 'b', 'c')
        );
    }

    @Test
    public void test_startsWithAnyChar1() {
        assertTrue(
            StringUtil.startsWithAny("/Foo/bar/", '/', '\\')
        );
    }

    @Test
    public void test_startsWithAnyChar2() {
        assertFalse(
            StringUtil.startsWithAny("Foo/bar/", '/', '\\')
        );
    }

    @Test
    public void test_startsWithAnyChar3() {
        assertTrue(
            StringUtil.startsWithAny("\\\\xag-123\\zab", '/', '\\')
        );
    }

    @Test
    public void test_startsWithCodePoint0a() {
        assertFalse(
            StringUtil.startsWithCodePoint(null, 0x1F602)
        );
    }

    @Test
    public void test_startsWithCodePoint0b() {
        assertFalse(
            StringUtil.startsWithCodePoint("", 0x1F602)
        );
    }

    @Test
    public void test_startsWithCodePoint1() {
        assertTrue(
            StringUtil.startsWithCodePoint("😂今日", 0x1F602)
        );
    }

    @Test
    public void test_startsWithCodePoint2() {
        assertTrue(
            StringUtil.startsWithCodePoint("😂今日は元気ですか", 0x1F602)
        );
    }

    @Test
    public void test_startsWithCodePoint3() {
        assertFalse(
            StringUtil.startsWithCodePoint("😂😃今日は元気ですか", 0x1F603)
        );
    }

    @Test
    public void test_startsWithCodePoint4() {
        assertTrue(
            StringUtil.startsWithCodePoint("😂今日は元気ですか？", 0x1F602)
        );
    }

    @Test
    public void test_startsWithAnyCodePoint0a() {
        assertFalse(
            StringUtil.startsWithAnyCodePoint(null, 'a', 'b', 'c', 'か', '日', 0x1F602)
        );
    }

    @Test
    public void test_startsWithAnyCodePoint0b() {
        assertFalse(
            StringUtil.startsWithAnyCodePoint("", 'a', 'b', 'c', 'か', '日', 0x1F602)
        );
    }

    @Test
    public void test_startsWithAnyCodePoint1() {
        assertTrue(
            StringUtil.startsWithAnyCodePoint("😂今日", 'a', 'b', 'c', 'か', '日', 0x1F602)
        );
    }

    @Test
    public void test_startsWithAnyCodePoint2() {
        assertTrue(
            StringUtil.startsWithAnyCodePoint("😂今日は元気ですか", 'か', '？', 0x1F602)
        );
    }

    @Test
    public void test_startsWithAnyCodePoint3() {
        assertFalse(
            StringUtil.startsWithAnyCodePoint("😂今日は元気ですか", '今', '日')
        );
    }

    @Test
    public void test_startsWithAnyCodePoint4() {
        assertFalse(
            StringUtil.startsWithAnyCodePoint("😂今日は元気ですか？", 'か', '？')
        );
    }

    @Test
    public void test_startsWithAnyCodePoint5() {
        assertTrue(
            StringUtil.startsWithAnyCodePoint("A😂今日は元気ですか？", 'A', 'B', 'C')
        );
    }

    @Test
    public void test_endsWithChar0a() {
        assertFalse(
            StringUtil.endsWith(null, '/')
        );
    }

    @Test
    public void test_endsWithChar0b() {
        assertFalse(
            StringUtil.endsWith("", 'a')
        );
    }

    @Test
    public void test_endsWithChar1() {
        assertTrue(
            StringUtil.endsWith("/Foo/bar/", '/')
        );
    }

    @Test
    public void test_endsWithChar2() {
        assertFalse(
            StringUtil.endsWith("/Foo/bar", '/')
        );
    }

    @Test
    public void test_endsWithAnyChar0a() {
        assertFalse(
            StringUtil.endsWithAny(null, '/', '\\')
        );
    }

    @Test
    public void test_endsWithAnyChar0b() {
        assertFalse(
            StringUtil.endsWithAny("", 'a', 'b', 'c')
        );
    }

    @Test
    public void test_endsWithAnyChar1() {
        assertTrue(
            StringUtil.endsWithAny("/Foo/bar/", '/', '\\')
        );
    }

    @Test
    public void test_endsWithAnyChar2() {
        assertFalse(
            StringUtil.endsWithAny("/Foo/bar", '/', '\\')
        );
    }

    @Test
    public void test_endsWithAnyChar3() {
        assertTrue(
            StringUtil.endsWithAny("C:\\", '/', '\\')
        );
    }

    @Test
    public void test_endsWithCodePoint0a() {
        assertFalse(
            StringUtil.endsWithCodePoint(null, 0x1F602)
        );
    }

    @Test
    public void test_endsWithCodePoint0b() {
        assertFalse(
            StringUtil.endsWithCodePoint("", 0x1F602)
        );
    }

    @Test
    public void test_endsWithCodePoint1() {
        assertTrue(
            StringUtil.endsWithCodePoint("今日😂", 0x1F602)
        );
    }

    @Test
    public void test_endsWithCodePoint2() {
        assertTrue(
            StringUtil.endsWithCodePoint("今日は元気ですか😂", 0x1F602)
        );
    }

    @Test
    public void test_endsWithCodePoint3() {
        assertFalse(
            StringUtil.endsWithCodePoint("今日は元気ですか😂", 'か')
        );
    }

    @Test
    public void test_endsWithCodePoint4() {
        assertTrue(
            StringUtil.endsWithCodePoint("今日は元気ですか😂", 0x1F602)
        );
    }

    @Test
    public void test_endsWithAnyCodePoint0a() {
        assertFalse(
            StringUtil.endsWithAnyCodePoint(null, 'a', 'b', 'c', 'か', '日', 0x1F602, 0x1F603)
        );
    }

    @Test
    public void test_endsWithAnyCodePoint0b() {
        assertFalse(
            StringUtil.endsWithAnyCodePoint("", 'a', 'b', 'c', 'か', '日', 0x1F602, 0x1F603)
        );
    }

    @Test
    public void test_endsWithAnyCodePoint1a() {
        assertTrue(
            StringUtil.endsWithAnyCodePoint("今日😂", 'a', 'b', 'c', 'か', '日', 0x1F602, 0x1F603)
        );
    }

    @Test
    public void test_endsWithAnyCodePoint1b() {
        assertTrue(
            StringUtil.endsWithAnyCodePoint("今日😃", 'a', 'b', 'c', 'か', '日', 0x1F602, 0x1F603)
        );
    }

    @Test
    public void test_endsWithAnyCodePoint2a() {
        assertTrue(
            StringUtil.endsWithAnyCodePoint("今日は元気ですか😂", 'か', '？', 0x1F602, 0x1F603)
        );
    }

    @Test
    public void test_endsWithAnyCodePoint2b() {
        assertTrue(
            StringUtil.endsWithAnyCodePoint("今日は元気ですか😃", 'か', '？', 0x1F602, 0x1F603)
        );
    }

    @Test
    public void test_endsWithAnyCodePoint3() {
        assertFalse(
            StringUtil.endsWithAnyCodePoint("今日は元気ですか", 'す', 'で', 0x1F602, 0x1F603)
        );
    }

    @Test
    public void test_endsWithAnyCodePoint4a() {
        assertTrue(
            StringUtil.endsWithAnyCodePoint("今日は元気ですか？😂", 'か', '？', 0x1F602, 0x1F603)
        );
    }

    @Test
    public void test_endsWithAnyCodePoint4b() {
        assertTrue(
            StringUtil.endsWithAnyCodePoint("今日は元気ですか？😃", 'か', '？', 0x1F602, 0x1F603)
        );
    }

    @Test
    public void test_indexOfIgnoreCase0a() {
        doIndexOfIgnoreCaseTestNotFound(null, 'c');
    }

    @Test
    public void test_indexOfIgnoreCase0b() {
        doIndexOfIgnoreCaseTestNotFound("", 'c');
    }

    @Test
    public void test_indexOfIgnoreCase0c() {
        doIndexOfIgnoreCaseTestNotFoundFromIndex(null, 'c', -15);
    }

    @Test
    public void test_indexOfIgnoreCase0d() {
        doIndexOfIgnoreCaseTestNotFoundFromIndex("", 'c', -5);
    }

    @Test
    public void test_indexOfIgnoreCase1a() {
        doIndexOfIgnoreCaseTestNotFound("abc", '1');
    }

    @Test
    public void test_indexOfIgnoreCase1b() {
        doIndexOfIgnoreCaseTestNotFoundFromIndex("abc", '1', -15);
    }

    @Test
    public void test_indexOfIgnoreCase1c() {
        doIndexOfIgnoreCaseTestNotFoundFromIndex("abc", '1', 0);
    }

    @Test
    public void test_indexOfIgnoreCase1d() {
        doIndexOfIgnoreCaseTestNotFoundFromIndex("abc", '1', 7);
    }

    @Test
    public void test_indexOfIgnoreCase1e() {
        doIndexOfIgnoreCaseTestNotFoundFromIndex("abc", 'a', 1);
    }

    @Test
    public void test_indexOfIgnoreCase1f() {
        doIndexOfIgnoreCaseTestFromIndex("abc", 'b', 1, 1);
    }

    @Test
    public void test_indexOfIgnoreCase1g() {
        doIndexOfIgnoreCaseTest("abc", 'A', 0);
    }

    @Test
    public void test_indexOfIgnoreCase1h() {
        doIndexOfIgnoreCaseTest("abc", 'B', 1);
    }

    @Test
    public void test_indexOfIgnoreCase1i() {
        doIndexOfIgnoreCaseTestFromIndex("abc", 'B', 1, 1);
    }

    @Test
    public void test_indexOfIgnoreCase2a() {
        doIndexOfIgnoreCaseTestNotFound("abc", '/');
    }

    @Test
    public void test_indexOfIgnoreCase2b() {
        doIndexOfIgnoreCaseTestNotFoundFromIndex("abc", '/', 1);
    }

    @Test
    public void test_indexOfIgnoreCase2c() {
        doIndexOfIgnoreCaseTestNotFoundFromIndex("/abc", '/', 1);
    }

    @Test
    public void test_indexOfIgnoreCase2d() {
        doIndexOfIgnoreCaseTestFromIndex("a/bc", '/', 1, 1);
    }

    @Test
    public void test_indexOfIgnoreCase2e() {
        doIndexOfIgnoreCaseTest("a/bc", '/', 1);
    }

    @Test
    public void test_indexOfIgnoreCase2f() {
        doIndexOfIgnoreCaseTest("/abc", '/', 0);
    }

    @Test
    public void test_indexOfIgnoreCase2g() {
        doIndexOfIgnoreCaseTest("/abc/", '/', 0);
    }

    @Test
    public void test_indexOfIgnoreCase3a() {
        doIndexOfIgnoreCaseTest("/Â/Cap-A-Circumflex/", 'Â', 1);
    }

    @Test
    public void test_indexOfIgnoreCase3b() {
        doIndexOfIgnoreCaseTest("/â/Lowercase-a-Circumflex/", 'Â', 1);
    }

    @Test
    public void test_indexOfIgnoreCase3c() {
        doIndexOfIgnoreCaseTestNotFound("â", 'a');
    }

    @Test
    public void test_indexOfIgnoreCase3d() {
        doIndexOfIgnoreCaseTestNotFound("a", 'â');
    }

    @Test
    public void test_indexOfIgnoreCase3e() {
        doIndexOfIgnoreCaseTestNotFound("Â", 'A');
    }

    @Test
    public void test_indexOfIgnoreCase3f() {
        doIndexOfIgnoreCaseTestNotFound("A", 'Â');
    }

    @Test
    public void test_containsCharactersInOrder0a() {
        doContainsCharactersInOrderTestNotFound(null, null);
    }

    @Test
    public void test_containsCharactersInOrder0b() {
        doContainsCharactersInOrderTestNotFound("", null);
    }

    @Test
    public void test_containsCharactersInOrder0c() {
        doContainsCharactersInOrderTestNotFound(null, "");
    }

    @Test
    public void test_containsCharactersInOrder0d() {
        doContainsCharactersInOrderTestNotFound("", "");
    }

    @Test
    public void test_containsCharactersInOrder0e() {
        doContainsCharactersInOrderTestNotFound("food", "");
    }

    @Test
    public void test_containsCharactersInOrder0f() {
        doContainsCharactersInOrderTestNotFound("", "fod");
    }

    @Test
    public void test_containsCharactersInOrder0g() {
        doContainsCharactersInOrderTestNotFound("food", null);
    }

    @Test
    public void test_containsCharactersInOrder0h() {
        doContainsCharactersInOrderTestNotFound(null, "fod");
    }

    @Test
    public void test_containsCharactersInOrder1a() {
        doContainsCharactersInOrderTestFound("food", "foo");
    }

    @Test
    public void test_containsCharactersInOrder1b() {
        doContainsCharactersInOrderTestFound("food", "fod");
    }

    @Test
    public void test_containsCharactersInOrder1c() {
        doContainsCharactersInOrderTestFound("food", "ood");
    }

    @Test
    public void test_containsCharactersInOrder1d() {
        doContainsCharactersInOrderTestFound("food", "fd");
    }

    @Test
    public void test_containsCharactersInOrder1e() {
        doContainsCharactersInOrderTestNotFound("food", "foood");
    }

    @Test
    public void test_containsCharactersInOrderIgnoreCase0a() {
        doContainsCharactersInOrderIgnoreCaseTestNotFound(null, null);
    }

    @Test
    public void test_containsCharactersInOrderIgnoreCase0b() {
        doContainsCharactersInOrderIgnoreCaseTestNotFound("", null);
    }

    @Test
    public void test_containsCharactersInOrderIgnoreCase0c() {
        doContainsCharactersInOrderIgnoreCaseTestNotFound(null, "");
    }

    @Test
    public void test_containsCharactersInOrderIgnoreCase0d() {
        doContainsCharactersInOrderIgnoreCaseTestNotFound("", "");
    }

    @Test
    public void test_containsCharactersInOrderIgnoreCase0e() {
        doContainsCharactersInOrderIgnoreCaseTestNotFound("food", "");
    }

    @Test
    public void test_containsCharactersInOrderIgnoreCase0f() {
        doContainsCharactersInOrderIgnoreCaseTestNotFound("", "fod");
    }

    @Test
    public void test_containsCharactersInOrderIgnoreCase0g() {
        doContainsCharactersInOrderIgnoreCaseTestNotFound("food", null);
    }

    @Test
    public void test_containsCharactersInOrderIgnoreCase0h() {
        doContainsCharactersInOrderIgnoreCaseTestNotFound(null, "fod");
    }

    @Test
    public void test_containsCharactersInOrderIgnoreCase1a() {
        doContainsCharactersInOrderIgnoreCaseTestFound("food", "FoO");
    }

    @Test
    public void test_containsCharactersInOrderIgnoreCase1b() {
        doContainsCharactersInOrderIgnoreCaseTestFound("food", "FOD");
    }

    @Test
    public void test_containsCharactersInOrderIgnoreCase1c() {
        doContainsCharactersInOrderIgnoreCaseTestFound("food", "OOd");
    }

    @Test
    public void test_containsCharactersInOrderIgnoreCase1d() {
        doContainsCharactersInOrderIgnoreCaseTestFound("food", "fD");
    }

    @Test
    public void test_containsCharactersInOrderIgnoreCase1e() {
        doContainsCharactersInOrderIgnoreCaseTestNotFound("food", "FOOoD");
    }

    @Test
    public void test_containsCharactersInOrderIgnoreCase2a() {
        doContainsCharactersInOrderIgnoreCaseTestFound("ABC:GOOD", "a:g");
    }

    @Test
    public void test_containsCharactersInOrderIgnoreCase2b() {
        doContainsCharactersInOrderIgnoreCaseTestFound("ABC:GOOD", "a:G");
    }

    @Test
    public void test_containsCharactersInOrderIgnoreCase2c() {
        doContainsCharactersInOrderIgnoreCaseTestFound("ABC:GOOD", "A:g");
    }

    @Test
    public void test_containsCharactersInOrderIgnoreCase2d() {
        doContainsCharactersInOrderIgnoreCaseTestFound("ABC:GOOD", "A:G");
    }

    @Test
    public void test_containsCharactersInOrderIgnoreCase2e() {
        doContainsCharactersInOrderIgnoreCaseTestFound("abc:good", "a:g");
    }

    @Test
    public void test_containsCharactersInOrderIgnoreCase2f() {
        doContainsCharactersInOrderIgnoreCaseTestFound("abc:good", "a:G");
    }

    @Test
    public void test_containsCharactersInOrderIgnoreCase2g() {
        doContainsCharactersInOrderIgnoreCaseTestFound("abc:good", "A:g");
    }

    @Test
    public void test_containsCharactersInOrderIgnoreCase2h() {
        doContainsCharactersInOrderIgnoreCaseTestFound("abc:good", "A:G");
    }

    @Test
    public void test_trimNotBlankXNull() {
        doTrimNotBlankXTestError(null, NullPointerException.class);
    }

    @Test
    public void test_trimNotBlankXEmpty() {
        doTrimNotBlankXTestError("", IllegalArgumentException.class);
    }

    @Test
    public void test_trimNotBlankXBlank() {
        doTrimNotBlankXTestError("    ", IllegalArgumentException.class);
    }

    @Test
    public void test_trimNotBlankX1() {
        doTrimNotBlankXTestValue("foo", "foo");
    }

    @Test
    public void test_trimNotBlankX2() {
        doTrimNotBlankXTestValue("  foo", "foo");
    }

    @Test
    public void test_trimNotBlankX3() {
        doTrimNotBlankXTestValue("foo     ", "foo");
    }

    @Test
    public void test_trimNotBlankX4() {
        doTrimNotBlankXTestValue("\t\t foo     ", "foo");
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint0A() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(null, 'a', -1);
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint0B() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(ImmutableList.of(), 'a', -1);
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint1A() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(ImmutableList.of("alphabet", "soup"), 'a', 0);
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint1B() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(ImmutableList.of("alphabet", "soup"), 'A', 0);
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint1C() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(ImmutableList.of("Alphabet", "soup"), 'a', 0);
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint1D() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(ImmutableList.of("Alphabet", "soup"), 'A', 0);
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint2A() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(ImmutableList.of("alphabet", "soup"), 'b', -2);
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint2B() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(ImmutableList.of("alphabet", "soup"), 'B', -2);
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint2C() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(ImmutableList.of("Alphabet", "soup"), 'b', -2);
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint2D() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(ImmutableList.of("Alphabet", "soup"), 'B', -2);
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint3A() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(ImmutableList.of("alphabet", "soup"), 's', 1);
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint3B() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(ImmutableList.of("alphabet", "soup"), 'S', 1);
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint3C() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(ImmutableList.of("Alphabet", "Soup"), 's', 1);
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint3D() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(ImmutableList.of("Alphabet", "Soup"), 'S', 1);
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint4A() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(ImmutableList.of("Alphabet", "soup"), 0x1F602, -3);
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint5A() {
        doCaseInsensitiveBinarySearchFirstCodePointTestError(
            ImmutableList.of("Alphabet", "soup"), -1, IllegalArgumentException.class
        );
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint5B() {
        doCaseInsensitiveBinarySearchFirstCodePointTestError(
            ImmutableList.of("Alphabet", "soup"), -777, IllegalArgumentException.class
        );
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint6A() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(
            ImmutableList.of("\uD83D\uDE02", "\uD83D\uDE03"), 0x1F602, 0
        );
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint6B() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(
            ImmutableList.of("😂", "😃"), 0x1F603, 1
        );
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint6C() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(
            ImmutableList.of("😂", "😃", "😅"), 0x1F604, -3
        );
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint7A() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(
            ImmutableList.of("", "", "", "alphabet", "alphaville 今", "今日", "今日", "今日", "😂", "😂今", "😂今日"),
            0x4ECA,
            5
        );
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint8A() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(ImmutableList.of("Budvase", "soup"), 'b', 0);
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint8B() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(ImmutableList.of("budvase", "soup"), 'B', 0);
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint9A() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(
            ImmutableList.of(
                "brieflyNoted",
                "bulletin",
                "done",
                "erasure",
                "FAQ",
                "fun",
                "FYI",
                "headline",
                "issue",
                "meeting",
                "news",
                "reclassification",
                "report",
                "to do",
                "unrecognized"
            ),
            'b',
            0
        );
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint9B() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(
            ImmutableList.of("boo", "bug", "byte", "bzzz"), 'b', 0
        );
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint9C() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(
            ImmutableList.of("boo", "bug", "byte", "bzzz"), 'B', 0
        );
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint9D() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(
            ImmutableList.of("age", "boo", "bug", "byte", "bzzz"), 'a', 0
        );
    }

    @Test
    public void test_caseInsensitiveBinarySearchFirstCodePoint9E() {
        doCaseInsensitiveBinarySearchFirstCodePointTest(
            ImmutableList.of("age", "boo", "bug", "byte", "bzzz"), 'A', 0
        );
    }

    @Test
    public void test_startsWithIgnoringLeadingWhitespace0A() {
        assertFalse(StringUtil.startsWithIgnoringLeadingWhitespace(null, null));
    }

    @Test
    public void test_startsWithIgnoringLeadingWhitespace0B() {
        assertFalse(StringUtil.startsWithIgnoringLeadingWhitespace("", null));
    }

    @Test
    public void test_startsWithIgnoringLeadingWhitespace0C() {
        assertFalse(StringUtil.startsWithIgnoringLeadingWhitespace(null, ""));
    }

    @Test
    public void test_startsWithIgnoringLeadingWhitespace0D() {
        assertTrue(StringUtil.startsWithIgnoringLeadingWhitespace("", ""));
    }

    @Test
    public void test_startsWithIgnoringLeadingWhitespace1A() {
        assertTrue(StringUtil.startsWithIgnoringLeadingWhitespace("abcdef", "abcdef"));
    }

    @Test
    public void test_startsWithIgnoringLeadingWhitespace1B() {
        assertTrue(StringUtil.startsWithIgnoringLeadingWhitespace("  \t\n  abcdef", "abcdef"));
    }

    @Test
    public void test_startsWithIgnoringLeadingWhitespace2A() {
        assertFalse(StringUtil.startsWithIgnoringLeadingWhitespace("abcde", "abcdef"));
    }

    @Test
    public void test_startsWithIgnoringLeadingWhitespace2B() {
        assertFalse(StringUtil.startsWithIgnoringLeadingWhitespace("bcdef", "abcdef"));
    }

    @Test
    public void test_startsWithIgnoringLeadingWhitespace2C() {
        assertFalse(StringUtil.startsWithIgnoringLeadingWhitespace("  \t\n  abcde", "abcdef"));
    }

    @Test
    public void test_startsWithIgnoringLeadingWhitespace2D() {
        assertFalse(StringUtil.startsWithIgnoringLeadingWhitespace("  \t\n  bcdef", "abcdef"));
    }

    @Test
    public void test_startsWithIgnoringLeadingWhitespaceIgnoreCase0A() {
        assertFalse(StringUtil.startsWithIgnoringLeadingWhitespaceIgnoreCase(null, null));
    }

    @Test
    public void test_startsWithIgnoringLeadingWhitespaceIgnoreCase0B() {
        assertFalse(StringUtil.startsWithIgnoringLeadingWhitespaceIgnoreCase("", null));
    }

    @Test
    public void test_startsWithIgnoringLeadingWhitespaceIgnoreCase0C() {
        assertFalse(StringUtil.startsWithIgnoringLeadingWhitespaceIgnoreCase(null, ""));
    }

    @Test
    public void test_startsWithIgnoringLeadingWhitespaceIgnoreCase0D() {
        assertTrue(StringUtil.startsWithIgnoringLeadingWhitespaceIgnoreCase("", ""));
    }

    @Test
    public void test_startsWithIgnoringLeadingWhitespaceIgnoreCase1A() {
        assertTrue(StringUtil.startsWithIgnoringLeadingWhitespaceIgnoreCase("abcdef", "abcdef"));
    }

    @Test
    public void test_startsWithIgnoringLeadingWhitespaceIgnoreCase1B() {
        assertTrue(StringUtil.startsWithIgnoringLeadingWhitespaceIgnoreCase("  \t\n  ABCDEF", "ABCDEF"));
    }

    @Test
    public void test_startsWithIgnoringLeadingWhitespaceIgnoreCase1C() {
        assertTrue(StringUtil.startsWithIgnoringLeadingWhitespaceIgnoreCase("  \t\n  ABCdef", "abcDEF"));
    }

    @Test
    public void test_startsWithIgnoringLeadingWhitespaceIgnoreCase1D() {
        assertTrue(StringUtil.startsWithIgnoringLeadingWhitespaceIgnoreCase("  \t\n  ABCdef", "abcDEF"));
    }

    @Test
    public void test_startsWithIgnoringLeadingWhitespaceIgnoreCase2A() {
        assertFalse(StringUtil.startsWithIgnoringLeadingWhitespaceIgnoreCase("abcde", "abcdef"));
    }

    @Test
    public void test_startsWithIgnoringLeadingWhitespaceIgnoreCase2B() {
        assertFalse(StringUtil.startsWithIgnoringLeadingWhitespaceIgnoreCase("bcdef", "abcdef"));
    }

    @Test
    public void test_startsWithIgnoringLeadingWhitespaceIgnoreCase2C() {
        assertFalse(StringUtil.startsWithIgnoringLeadingWhitespaceIgnoreCase("  \t\n  abcde", "abcdef"));
    }

    @Test
    public void test_startsWithIgnoringLeadingWhitespaceIgnoreCase2D() {
        assertFalse(StringUtil.startsWithIgnoringLeadingWhitespaceIgnoreCase("  \t\n  bcdef", "abcdef"));
    }








    @Test
    public void test_endsWithIgnoringTrailingWhitespace0A() {
        assertFalse(StringUtil.endsWithIgnoringTrailingWhitespace(null, null));
    }

    @Test
    public void test_endsWithIgnoringTrailingWhitespace0B() {
        assertFalse(StringUtil.endsWithIgnoringTrailingWhitespace("", null));
    }

    @Test
    public void test_endsWithIgnoringTrailingWhitespace0C() {
        assertFalse(StringUtil.endsWithIgnoringTrailingWhitespace(null, ""));
    }

    @Test
    public void test_endsWithIgnoringTrailingWhitespace0D() {
        assertTrue(StringUtil.endsWithIgnoringTrailingWhitespace("", ""));
    }

    @Test
    public void test_endsWithIgnoringTrailingWhitespace1A() {
        assertTrue(StringUtil.endsWithIgnoringTrailingWhitespace("abcdef", "abcdef"));
    }

    @Test
    public void test_endsWithIgnoringTrailingWhitespace1B() {
        assertTrue(StringUtil.endsWithIgnoringTrailingWhitespace("abcdef  \t\n  ", "abcdef"));
    }

    @Test
    public void test_endsWithIgnoringTrailingWhitespace2A() {
        assertFalse(StringUtil.endsWithIgnoringTrailingWhitespace("abcde", "abcdef"));
    }

    @Test
    public void test_endsWithIgnoringTrailingWhitespace2B() {
        assertFalse(StringUtil.endsWithIgnoringTrailingWhitespace("bcdef", "abcdef"));
    }

    @Test
    public void test_endsWithIgnoringTrailingWhitespace2C() {
        assertFalse(StringUtil.endsWithIgnoringTrailingWhitespace("abcde  \t\n  ", "abcdef"));
    }

    @Test
    public void test_endsWithIgnoringTrailingWhitespace2D() {
        assertFalse(StringUtil.endsWithIgnoringTrailingWhitespace("bcdef  \t\n  ", "abcdef"));
    }

    @Test
    public void test_endsWithIgnoringTrailingWhitespaceIgnoreCase0A() {
        assertFalse(StringUtil.endsWithIgnoringTrailingWhitespaceIgnoreCase(null, null));
    }

    @Test
    public void test_endsWithIgnoringTrailingWhitespaceIgnoreCase0B() {
        assertFalse(StringUtil.endsWithIgnoringTrailingWhitespaceIgnoreCase("", null));
    }

    @Test
    public void test_endsWithIgnoringTrailingWhitespaceIgnoreCase0C() {
        assertFalse(StringUtil.endsWithIgnoringTrailingWhitespaceIgnoreCase(null, ""));
    }

    @Test
    public void test_endsWithIgnoringTrailingWhitespaceIgnoreCase0D() {
        assertTrue(StringUtil.endsWithIgnoringTrailingWhitespaceIgnoreCase("", ""));
    }

    @Test
    public void test_endsWithIgnoringTrailingWhitespaceIgnoreCase1A() {
        assertTrue(StringUtil.endsWithIgnoringTrailingWhitespaceIgnoreCase("abcdef", "abcdef"));
    }

    @Test
    public void test_endsWithIgnoringTrailingWhitespaceIgnoreCase1B() {
        assertTrue(StringUtil.endsWithIgnoringTrailingWhitespaceIgnoreCase("ABCDEF  \t\n  ", "ABCDEF"));
    }

    @Test
    public void test_endsWithIgnoringTrailingWhitespaceIgnoreCase1C() {
        assertTrue(StringUtil.endsWithIgnoringTrailingWhitespaceIgnoreCase("ABCdef  \t\n  ", "abcDEF"));
    }

    @Test
    public void test_endsWithIgnoringTrailingWhitespaceIgnoreCase1D() {
        assertTrue(StringUtil.endsWithIgnoringTrailingWhitespaceIgnoreCase("ABCdef  \t\n  ", "abcDEF"));
    }

    @Test
    public void test_endsWithIgnoringTrailingWhitespaceIgnoreCase2A() {
        assertFalse(StringUtil.endsWithIgnoringTrailingWhitespaceIgnoreCase("abcde", "abcdef"));
    }

    @Test
    public void test_endsWithIgnoringTrailingWhitespaceIgnoreCase2B() {
        assertFalse(StringUtil.endsWithIgnoringTrailingWhitespaceIgnoreCase("bcdef", "abcdef"));
    }

    @Test
    public void test_endsWithIgnoringTrailingWhitespaceIgnoreCase2C() {
        assertFalse(StringUtil.endsWithIgnoringTrailingWhitespaceIgnoreCase("abcde  \t\n  ", "abcdef"));
    }

    @Test
    public void test_endsWithIgnoringTrailingWhitespaceIgnoreCase2D() {
        assertFalse(StringUtil.endsWithIgnoringTrailingWhitespaceIgnoreCase("bcdef  \t\n  ", "abcdef"));
    }

    @Test
    public void test_removeCodePoint0a() {
        assertNull(StringUtil.remove(null, (int) 'a'));
    }

    @Test
    public void test_removeCodePoint0b() {
        assertEquals("", StringUtil.remove("", (int) 'a'));
    }

    @Test
    public void test_remove0a() {
        assertNull(StringUtil.remove(null, 'a'));
    }

    @Test
    public void test_remove0b() {
        String input = "";
        assertSame(input, StringUtil.remove(input, 'a'));
    }

    @Test
    public void test_removeCodePointNoOp1() {
        String input = "bcdef";
        assertSame(input, StringUtil.remove(input, (int) 'a'));
    }

    @Test
    public void test_removeCodePointNoOp2() {
        String input = "bcdef";
        assertSame(input, StringUtil.remove(input, 0x1F602));
    }

    @Test
    public void test_removeCodePoint1() {
        assertEquals("bcdef", StringUtil.remove("abcdef", (int) 'a'));
    }

    @Test
    public void test_removeCodePoint2a() {
        String input = "abcd😂ef";
        assertEquals("abcdef", StringUtil.remove(input, 0x1F602));
    }

    @Test
    public void test_removeCodePoint2b() {
        String input = "😂ab😂cdef😂";
        assertEquals("abcdef", StringUtil.remove(input, 0x1F602));
    }

    @Test
    public void test_removeCodePoint2c() {
        String input = "😂abcdef";
        assertEquals("abcdef", StringUtil.remove(input, 0x1F602));
    }

    @Test
    public void test_removeCodePoint2d() {
        String input = "abcdef😂";
        assertEquals("abcdef", StringUtil.remove(input, 0x1F602));
    }

    @Test
    public void test_removeChar2a() {
        String input = "bcdef";
        assertSame(input, StringUtil.remove(input, 'a'));
    }

    @Test
    public void test_removeChar2b() {
        assertEquals("bcdef", StringUtil.remove("abcdef", 'a'));
    }

    @Test
    public void test_removeChar2c() {
        assertEquals("bcdef", StringUtil.remove("abcdefa", 'a'));
    }

    @Test
    public void test_removeChar2d() {
        assertEquals("bcdef", StringUtil.remove("abcadefa", 'a'));
    }

    @Test
    public void test_removeChar3a() {
        String input = "abc";
        assertSame(input, StringUtil.remove(input, '"'));
    }

    @Test
    public void test_removeChar3b() {
        assertEquals("abc", StringUtil.remove("\"abc", '"'));
    }

    @Test
    public void test_removeChar3c() {
        assertEquals("abc", StringUtil.remove("\"abc\"", '"'));
    }

    @Test
    public void test_removeChar3d() {
        assertEquals("abc", StringUtil.remove("abc\"", '"'));
    }

    @Test
    public void test_removeChar3e() {
        assertEquals("abc", StringUtil.remove("a\"b\"c", '"'));
    }

    @Test
    public void test_removeChar3f() {
        assertEquals("abc", StringUtil.remove("\"a\"b\"c\"", '"'));
    }

    @Test
    public void test_removeWhitespace0A() {
        assertNull(StringUtil.removeWhitespace(null, true));
    }

    @Test
    public void test_removeWhitespace0B() {
        assertEquals("", StringUtil.removeWhitespace("", true));
    }

    @Test
    public void test_removeWhitespace1A() {
        assertEquals("", StringUtil.removeWhitespace(" ", true));
    }

    @Test
    public void test_removeWhitespace1B() {
        assertEquals("", StringUtil.removeWhitespace(" ", true));
    }

    @Test
    public void test_removeWhitespace1C() {
        assertEquals("", StringUtil.removeWhitespace(" \t  \t \u00a0   \u200b ", true));
    }

    @Test
    public void testTruncateWholeInWayTooMuchSpace() {
        assertEquals(
            "The quick brown fox jumps over the lazy dog.",
            StringUtil.truncate("The quick brown fox jumps over the lazy dog.", 500, "...")
        );
    }

    @Test
    public void testTruncateWholeInJustEnoughSpace() {
        assertEquals("foobar", StringUtil.truncate("foobar", 6, "..."));
    }

    @Test
    public void testTruncateFirst7PlusEllipses() {
        assertEquals("The qui...", StringUtil.truncate("The quick brown fox jumps over the lazy dog.", 10, "..."));
    }

    @Test
    public void testTruncateFirst3PlusEllipses() {
        assertEquals("foo...", StringUtil.truncate("foobarbaz", 6, "..."));
    }

    @Test
    public void testTruncate0() {
        assertEquals("", StringUtil.truncate("foobarbaz", 0, "..."));
    }

    @Test
    public void testTruncateNull() {
        assertEquals("", StringUtil.truncate(null, 25, "..."));
    }

}

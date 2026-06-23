// PLEASE DO NOT DELETE THIS LINE - make copyright depends on it.
package com.tractionsoftware.commons.text;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

class TextWrapUtilTest {

    private static final char ZWS = '​'; // zero-width space

    // ---------------------------------------------------------------------------
    // factory methods
    // ---------------------------------------------------------------------------

    @Test
    void createDefaultNonSpaceWrapInserter_returnsNonNull() {
        assertNotNull(TextWrapUtil.createDefaultNonSpaceWrapInserter());
    }

    @Test
    void createNonSpaceWrapInserter_string_returnsNonNull() {
        assertNotNull(TextWrapUtil.createNonSpaceWrapInserter("/"));
    }

    @Test
    void createNonSpaceWrapInserter_nullWrap_usesDefault() throws IOException {
        // null/blank wrap falls back to default "/."; dot triggers ZWS
        TextWrapUtil.ZeroWidthSpaceInserter inserter =
            TextWrapUtil.createNonSpaceWrapInserter(null);
        StringBuilder sb = new StringBuilder();
        inserter.printWithZeroWidthSpacesInserted(sb, "a.b");
        assertTrue(sb.toString().contains(String.valueOf(ZWS)), "expected ZWS: " + sb);
    }

    // ---------------------------------------------------------------------------
    // printWithZeroWidthSpacesInserted — basic behavior
    // ---------------------------------------------------------------------------

    @Test
    void noWrapCharsInText_passthrough() throws IOException {
        TextWrapUtil.ZeroWidthSpaceInserter inserter =
            TextWrapUtil.createNonSpaceWrapInserter("/.");
        StringBuilder sb = new StringBuilder();
        inserter.printWithZeroWidthSpacesInserted(sb, "hello");
        assertEquals("hello", sb.toString());
    }

    @Test
    void dotInText_zwsInsertedAfterDot() throws IOException {
        TextWrapUtil.ZeroWidthSpaceInserter inserter =
            TextWrapUtil.createNonSpaceWrapInserter(".");
        StringBuilder sb = new StringBuilder();
        inserter.printWithZeroWidthSpacesInserted(sb, "hello.world");
        // ZWS should appear after the dot
        String result = sb.toString();
        assertTrue(result.contains("." + ZWS), "expected ZWS after dot: " + result);
        // the rest of the text should be intact
        assertTrue(result.contains("hello"), result);
        assertTrue(result.contains("world"), result);
    }

    @Test
    void slashInText_zwsInsertedAfterSlash() throws IOException {
        TextWrapUtil.ZeroWidthSpaceInserter inserter =
            TextWrapUtil.createNonSpaceWrapInserter("/");
        StringBuilder sb = new StringBuilder();
        inserter.printWithZeroWidthSpacesInserted(sb, "path/to/file");
        String result = sb.toString();
        // both slashes should be followed by ZWS
        int count = 0;
        for (int i = 0; i < result.length() - 1; i++) {
            if (result.charAt(i) == '/' && result.charAt(i + 1) == ZWS) count++;
        }
        assertEquals(2, count, "expected ZWS after each slash: " + result);
    }

    @Test
    void wrapCancelledBeforeWhitespace() throws IOException {
        // If the next char after a wrap-char is whitespace, ZWS should NOT be inserted
        TextWrapUtil.ZeroWidthSpaceInserter inserter =
            TextWrapUtil.createNonSpaceWrapInserter(".");
        StringBuilder sb = new StringBuilder();
        inserter.printWithZeroWidthSpacesInserted(sb, "hello. world");
        String result = sb.toString();
        // space after dot → no ZWS inserted
        assertFalse(result.contains(String.valueOf(ZWS)),
            "ZWS should NOT appear before whitespace: " + result);
    }

    @Test
    void wrapCancelledBeforeQuote() throws IOException {
        TextWrapUtil.ZeroWidthSpaceInserter inserter =
            TextWrapUtil.createNonSpaceWrapInserter(".");
        StringBuilder sb = new StringBuilder();
        inserter.printWithZeroWidthSpacesInserted(sb, "hello.\"world\"");
        String result = sb.toString();
        assertFalse(result.contains(String.valueOf(ZWS)),
            "ZWS should NOT appear before quote: " + result);
    }

    @Test
    void emptyText_noOutput() throws IOException {
        TextWrapUtil.ZeroWidthSpaceInserter inserter =
            TextWrapUtil.createNonSpaceWrapInserter("/.");
        StringBuilder sb = new StringBuilder();
        inserter.printWithZeroWidthSpacesInserted(sb, "");
        assertEquals("", sb.toString());
    }

    @Test
    void multipleDots_zwsAfterEach() throws IOException {
        TextWrapUtil.ZeroWidthSpaceInserter inserter =
            TextWrapUtil.createNonSpaceWrapInserter(".");
        StringBuilder sb = new StringBuilder();
        inserter.printWithZeroWidthSpacesInserted(sb, "a.b.c");
        String result = sb.toString();
        long count = result.chars().filter(c -> c == ZWS).count();
        assertEquals(2, count, "expected 2 ZWS: " + result);
    }

    @Test
    void customZeroWidthSpace_usesCustomChar() throws IOException {
        String custom = "|";
        TextWrapUtil.ZeroWidthSpaceInserter inserter =
            TextWrapUtil.createNonSpaceWrapInserter(".", custom);
        StringBuilder sb = new StringBuilder();
        inserter.printWithZeroWidthSpacesInserted(sb, "a.b");
        assertTrue(sb.toString().contains("|"), "expected custom ZWS '|': " + sb);
    }

    @Test
    void printWithZeroWidthSpacesInserted_stringBuilder() {
        TextWrapUtil.ZeroWidthSpaceInserter inserter =
            TextWrapUtil.createNonSpaceWrapInserter(".");
        StringBuilder sb = new StringBuilder();
        inserter.printWithZeroWidthSpacesInserted(sb, "a.b");
        assertTrue(sb.toString().contains(String.valueOf(ZWS)));
    }

    @Test
    void printWithZeroWidthSpacesInserted_printWriter() {
        TextWrapUtil.ZeroWidthSpaceInserter inserter =
            TextWrapUtil.createNonSpaceWrapInserter(".");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        inserter.printWithZeroWidthSpacesInserted(pw, "a.b");
        pw.flush();
        assertTrue(sw.toString().contains(String.valueOf(ZWS)), "expected ZWS: " + sw);
    }

    // ---------------------------------------------------------------------------
    // non-BMP wrap characters - routes to CodePointBasedZeroWidthSpaceInserter
    // ---------------------------------------------------------------------------

    @Test
    void nonBmpWrapCharacter_zwsInsertedAfterEmoji() throws IOException {
        // An emoji (non-BMP codepoint) as the wrap character routes through the
        // codepoint-based implementation rather than the char-based one.
        String emoji = "😀"; // U+1F600 GRINNING FACE
        TextWrapUtil.ZeroWidthSpaceInserter inserter =
            TextWrapUtil.createNonSpaceWrapInserter(emoji);
        StringBuilder sb = new StringBuilder();
        inserter.printWithZeroWidthSpacesInserted(sb, "hello" + emoji + "world");
        String result = sb.toString();
        assertTrue(result.contains(emoji + ZWS), "expected ZWS after emoji: " + result);
        assertTrue(result.contains("hello"), result);
        assertTrue(result.contains("world"), result);
    }

    @Test
    void nonBmpWrapCharacter_cancelledBeforeWhitespace() throws IOException {
        String emoji = "😀";
        TextWrapUtil.ZeroWidthSpaceInserter inserter =
            TextWrapUtil.createNonSpaceWrapInserter(emoji);
        StringBuilder sb = new StringBuilder();
        inserter.printWithZeroWidthSpacesInserted(sb, "hello" + emoji + " world");
        assertFalse(sb.toString().contains(String.valueOf(ZWS)),
            "ZWS should NOT appear before whitespace: " + sb);
    }

}

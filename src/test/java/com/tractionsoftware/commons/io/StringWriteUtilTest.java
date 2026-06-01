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

import com.tractionsoftware.commons.text.StringEscapeUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class StringWriteUtilTest {

    private static final String getPrintEscapeMultipleCharactersResult(String input, String escape) {
        return StringWriteUtil.getPrintedString(
            out -> StringEscapeUtil.escapeMultipleCharacters(out, input, escape)
        );
    }

    private static final String getPrintUnescapeMultipleCharactersResult(String input, String escape) {
        return StringWriteUtil.getPrintedString(
            out -> StringEscapeUtil.unescapeMultipleCharacters(out, input, escape)
        );
    }

    @Test
    public void testUnescapeMultiChars1() {
        String input = "\\n\\a\\q\\t";
        String expected = "\n\\a\\q\t";
        String actual = StringEscapeUtil.unescapeMultipleCharacters(input, "\t\r\n\f");
        assertEquals(expected, actual);
    }

    @Test
    public void testUnescapeMultiChars2() {
        String input = "\\tThis is a sentence at the beginning of a new paragraph.\\n\\tAnd here's another one.";
        String expected = "\tThis is a sentence at the beginning of a new paragraph.\n\tAnd here's another one.";
        String actual = StringEscapeUtil.unescapeMultipleCharacters(input, "\t\r\n\f");
        assertEquals(expected, actual);
    }

    @Test
    public void testUnescapeMultiChars3() {
        String input = "I'm\\tall\\tdivided\\t.\\n";
        String expected = "I'm\tall\tdivided\t.\\n";
        String actual = StringEscapeUtil.unescapeMultipleCharacters(input, "\t");
        assertEquals(expected, actual);
    }

    @Test
    public void testUnescapeMultiChars4() {
        String input = "\\n\\rThe text is escaping!\\f\\nStop it!";
        String expected = "\n\\rThe text is escaping!\\f\nStop it!";
        String actual = StringEscapeUtil.unescapeMultipleCharacters(input, "\n");
        assertEquals(expected, actual);
    }

    @Test
    public void testPrintUnescapeMultiChars1() {
        String input = "\\n\\a\\q\\t";
        String expected = "\n\\a\\q\t";
        String actual = getPrintUnescapeMultipleCharactersResult(input, "\t\r\n\f");
        assertEquals(expected, actual);
    }

    @Test
    public void testPrintUnescapeMultiChars2() {
        String input = "\\tThis is a sentence at the beginning of a new paragraph.\\n\\tAnd here's another one.";
        String expected = "\tThis is a sentence at the beginning of a new paragraph.\n\tAnd here's another one.";
        String actual = getPrintUnescapeMultipleCharactersResult(input, "\t\r\n\f");
        assertEquals(expected, actual);
    }

    @Test
    public void testPrintUnescapeMultiChars3() {
        String input = "I'm\\tall\\tdivided\\t.\\n";
        String expected = "I'm\tall\tdivided\t.\\n";
        String actual = getPrintUnescapeMultipleCharactersResult(input, "\t");
        assertEquals(expected, actual);
    }

    @Test
    public void testPrintUnescapeMultiChars4() {
        String input = "\\n\\rThe text is escaping!\\f\\nStop it!";
        String expected = "\n\\rThe text is escaping!\\f\nStop it!";
        String actual = getPrintUnescapeMultipleCharactersResult(input, "\n");
        assertEquals(expected, actual);
    }

    @Test
    public void testEscapeMultiChars1() {
        String input = "Escape this, you jerk.\n\r\tNo, I'm just kidding, you're wonderful.";
        String expected = "Escape this, you jerk.\\n\\r\\tNo, I'm just kidding, you're wonderful.";
        String actual = StringEscapeUtil.escapeMultipleCharacters(input, "\t\r\n\f");
        assertEquals(expected, actual);
    }

    @Test
    public void testEscapeMultiChars2() {
        String input = "You can't escape from this sentence.\n\r\tUntil you finish reading it.";
        String expected = "You can't escape from this sentence.\n\\r\tUntil you finish reading it.";
        String actual = StringEscapeUtil.escapeMultipleCharacters(input, "\r\f");
        assertEquals(expected, actual);
    }

    /**
     * regex.replacement.escape uses this:
     *
     * <pre>
     * StringEscapeUtil.escapeMultipleCharacters(out, output, "\\$");
     * </pre>
     */
    @Test
    public void testEscapeMultiChars_forRegexReplacement1() {
        String input = "$ (\\wow!\\)"; // $ (\wow!\)
        String expected = "\\$ (\\\\wow!\\\\)";
        String actual = StringEscapeUtil.escapeMultipleCharacters(input, "\\$");
        assertEquals(expected, actual);
    }

    @Test
    public void testEscapeMultiChars_forRegexReplacement2() {
        String input = "$ (\\wow!\\)"; // $ (\wow!\)
        String expected = "\\$ (\\\\wow!\\\\)";
        String actual = getPrintEscapeMultipleCharactersResult(input, "\\$");
        assertEquals(expected, actual);
    }

    @Test
    public void test_escapeMultiCharacters_emailAddressFriendlyName1() {
        assertEquals(
            "Bob \\\"C:\\\\\\\" McGee",
            StringEscapeUtil.escapeMultipleCharacters(
                "Bob \"C:\\\" McGee",
                "\\\""
            )
        );
    }

    @Test
    public void test_escapeMultiCharacters_other1() {
        assertEquals(
            "\\\"C:\\\\Program Files\\\" is an important path on Windows machines.",
            StringEscapeUtil.escapeMultipleCharacters(
                "\"C:\\Program Files\" is an important path on Windows machines.",
                "\\\""
            )
        );
    }

}

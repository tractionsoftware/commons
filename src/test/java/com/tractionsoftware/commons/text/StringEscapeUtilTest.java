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

package com.tractionsoftware.commons.text;

import com.google.common.base.CharMatcher;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class StringEscapeUtilTest {

    // -------------------------------------------------------------------------
    // getEscapeSequence
    // -------------------------------------------------------------------------

    @Test
    void getEscapeSequence_tab_returnsBackslashT() {
        assertEquals("\\t", StringEscapeUtil.getEscapeSequence('\t'));
    }

    @Test
    void getEscapeSequence_carriageReturn_returnsBackslashR() {
        assertEquals("\\r", StringEscapeUtil.getEscapeSequence('\r'));
    }

    @Test
    void getEscapeSequence_newline_returnsBackslashN() {
        assertEquals("\\n", StringEscapeUtil.getEscapeSequence('\n'));
    }

    @Test
    void getEscapeSequence_formFeed_returnsBackslashF() {
        assertEquals("\\f", StringEscapeUtil.getEscapeSequence('\f'));
    }

    @Test
    void getEscapeSequence_comma_returnsBackslashComma() {
        assertEquals("\\,", StringEscapeUtil.getEscapeSequence(','));
    }

    @Test
    void getEscapeSequence_backslash_returnsDoubleBackslash() {
        assertEquals("\\\\", StringEscapeUtil.getEscapeSequence('\\'));
    }

    // -------------------------------------------------------------------------
    // getLiteralCharacter
    // -------------------------------------------------------------------------

    @Test
    void getLiteralCharacter_t_returnsTab() {
        assertEquals('\t', StringEscapeUtil.getLiteralCharacter('t'));
    }

    @Test
    void getLiteralCharacter_r_returnsCarriageReturn() {
        assertEquals('\r', StringEscapeUtil.getLiteralCharacter('r'));
    }

    @Test
    void getLiteralCharacter_n_returnsNewline() {
        assertEquals('\n', StringEscapeUtil.getLiteralCharacter('n'));
    }

    @Test
    void getLiteralCharacter_f_returnsFormFeed() {
        assertEquals('\f', StringEscapeUtil.getLiteralCharacter('f'));
    }

    @Test
    void getLiteralCharacter_unknown_returnsCharUnchanged() {
        assertEquals('x', StringEscapeUtil.getLiteralCharacter('x'));
    }

    @Test
    void getLiteralCharacter_comma_returnsComma() {
        assertEquals(',', StringEscapeUtil.getLiteralCharacter(','));
    }

    // -------------------------------------------------------------------------
    // unescapeMultipleCharacters(CharSequence, CharSequence)
    // -------------------------------------------------------------------------

    @Test
    void unescapeMultipleCharacters_charSeq_nullStr_returnsNull() {
        assertNull(StringEscapeUtil.unescapeMultipleCharacters((CharSequence) null, ","));
    }

    @Test
    void unescapeMultipleCharacters_charSeq_emptyStr_returnsEmpty() {
        assertEquals("", StringEscapeUtil.unescapeMultipleCharacters("", ","));
    }

    @Test
    void unescapeMultipleCharacters_charSeq_noEscapes_returnsOriginal() {
        String input = "hello world";
        String result = StringEscapeUtil.unescapeMultipleCharacters(input, ",");
        assertEquals(input, result);
    }

    @Test
    void unescapeMultipleCharacters_charSeq_escapedComma_unescaped() {
        // "hello\\,world" with escapeChars="," → "hello,world"
        assertEquals("hello,world", StringEscapeUtil.unescapeMultipleCharacters("hello\\,world", ","));
    }

    @Test
    void unescapeMultipleCharacters_charSeq_escapedNewlineSeq_unescaped() {
        // "hello\\nworld" where 'n' maps via getLiteralCharacter to '\n', and "\r\n," are in escapeChars
        assertEquals("hello\nworld", StringEscapeUtil.unescapeMultipleCharacters("hello\\nworld", "\r\n,"));
    }

    @Test
    void unescapeMultipleCharacters_charSeq_escapedTab_unescaped() {
        // "a\\tb" where 't' maps to '\t', and '\t' is in escapeChars
        assertEquals("a\tb", StringEscapeUtil.unescapeMultipleCharacters("a\\tb", "\t"));
    }

    @Test
    void unescapeMultipleCharacters_charSeq_unknownEscapeKept() {
        // "hello\\xworld" where 'x' is not in escapeChars — backslash is preserved
        assertEquals("hello\\xworld", StringEscapeUtil.unescapeMultipleCharacters("hello\\xworld", ","));
    }

    @Test
    void unescapeMultipleCharacters_charSeq_trailingBackslash_preserved() {
        // "hello\\" — trailing backslash with no following char — kept as-is
        assertEquals("hello\\", StringEscapeUtil.unescapeMultipleCharacters("hello\\", ","));
    }

    @Test
    void unescapeMultipleCharacters_charSeq_trailingBackslash_withSbAlreadyBuilt() {
        // Trigger the StringBuilder path first (escaped comma), then end with trailing backslash.
        // "a\\,b\\" → "a,b\\"
        assertEquals("a,b\\", StringEscapeUtil.unescapeMultipleCharacters("a\\,b\\", ","));
    }

    @Test
    void unescapeMultipleCharacters_charSeq_unknownEscape_withSbAlreadyBuilt() {
        // First escape triggers sb creation, second is unknown → sb appends backslash + char.
        // "a\\,b\\x" → "a,b\\x"
        assertEquals("a,b\\x", StringEscapeUtil.unescapeMultipleCharacters("a\\,b\\x", ","));
    }

    // -------------------------------------------------------------------------
    // unescapeMultipleCharacters(CharSequence, CharMatcher)
    // -------------------------------------------------------------------------

    @Test
    void unescapeMultipleCharacters_charMatcher_escapedComma_unescaped() {
        CharMatcher matcher = CharMatcher.is(',');
        assertEquals("a,b", StringEscapeUtil.unescapeMultipleCharacters("a\\,b", matcher));
    }

    @Test
    void unescapeMultipleCharacters_charMatcher_null_returnsNull() {
        assertNull(StringEscapeUtil.unescapeMultipleCharacters((CharSequence) null, CharMatcher.any()));
    }

    // -------------------------------------------------------------------------
    // unescapeMultipleCharacters(Appendable, CharSequence, String)
    // -------------------------------------------------------------------------

    @Test
    void unescapeMultipleCharacters_appendable_escapedComma_appended() throws IOException {
        StringBuilder sb = new StringBuilder();
        StringEscapeUtil.unescapeMultipleCharacters(sb, "hello\\,world", ",");
        assertEquals("hello,world", sb.toString());
    }

    @Test
    void unescapeMultipleCharacters_appendable_emptyStr_appendsNothing() throws IOException {
        StringBuilder sb = new StringBuilder("prefix");
        StringEscapeUtil.unescapeMultipleCharacters(sb, "", ",");
        assertEquals("prefix", sb.toString());
    }

    @Test
    void unescapeMultipleCharacters_appendable_nullStr_appendsNothing() throws IOException {
        StringBuilder sb = new StringBuilder("prefix");
        StringEscapeUtil.unescapeMultipleCharacters(sb, null, ",");
        assertEquals("prefix", sb.toString());
    }

    @Test
    void unescapeMultipleCharacters_appendable_trailingBackslash_preserved() throws IOException {
        StringBuilder sb = new StringBuilder();
        StringEscapeUtil.unescapeMultipleCharacters(sb, "hello\\", ",");
        assertEquals("hello\\", sb.toString());
    }

    @Test
    void unescapeMultipleCharacters_appendable_unknownEscapeKept() throws IOException {
        StringBuilder sb = new StringBuilder();
        StringEscapeUtil.unescapeMultipleCharacters(sb, "a\\xb", ",");
        assertEquals("a\\xb", sb.toString());
    }

    @Test
    void unescapeMultipleCharacters_appendable_escapedNewlineSeq() throws IOException {
        // "a\\nb" with escaped="\n" — 'n' via getLiteralCharacter → '\n', which is in escaped
        StringBuilder sb = new StringBuilder();
        StringEscapeUtil.unescapeMultipleCharacters(sb, "a\\nb", "\n");
        assertEquals("a\nb", sb.toString());
    }

    @Test
    void unescapeMultipleCharacters_appendable_noEscapes_passesThrough() throws IOException {
        StringBuilder sb = new StringBuilder();
        StringEscapeUtil.unescapeMultipleCharacters(sb, "abc", ",");
        assertEquals("abc", sb.toString());
    }

}

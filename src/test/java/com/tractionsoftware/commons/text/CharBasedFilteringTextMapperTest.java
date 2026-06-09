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

import com.tractionsoftware.commons.lang.StringUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

class CharBasedFilteringTextMapperTest {

    // ---------------------------------------------------------------------------
    // removeIf
    // ---------------------------------------------------------------------------

    @Test
    void removeIf_null_returnsNull() {
        assertNull(CharBasedFilteringTextMapper.removeIf(StringUtil.MATCHER_ASCII_DIGIT, null));
    }

    @Test
    void removeIf_emptyString_returnsEmpty() {
        assertEquals("", CharBasedFilteringTextMapper.removeIf(StringUtil.MATCHER_ASCII_DIGIT, ""));
    }

    @Test
    void removeIf_noMatch_returnsOriginal() {
        assertEquals("abc", CharBasedFilteringTextMapper.removeIf(StringUtil.MATCHER_ASCII_DIGIT, "abc"));
    }

    @Test
    void removeIf_allMatch_returnsEmpty() {
        assertEquals("", CharBasedFilteringTextMapper.removeIf(StringUtil.MATCHER_ASCII_DIGIT, "123"));
    }

    @Test
    void removeIf_someMatch_removesMatches() {
        assertEquals("abc", CharBasedFilteringTextMapper.removeIf(StringUtil.MATCHER_ASCII_DIGIT, "a1b2c"));
    }

    @Test
    void removeIf_toStringBuilder_appendsFiltered() {
        StringBuilder sb = new StringBuilder("pre:");
        CharBasedFilteringTextMapper.removeIf("a1b2c", sb, StringUtil.MATCHER_ASCII_DIGIT);
        assertEquals("pre:abc", sb.toString());
    }

    @Test
    void removeIf_toPrintWriter_writesFiltered() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        CharBasedFilteringTextMapper.removeIf("a1b2c", pw, StringUtil.MATCHER_ASCII_DIGIT);
        pw.flush();
        assertEquals("abc", sw.toString());
    }

    @Test
    void removeIf_fromReader_writesFiltered() throws IOException {
        StringWriter sw = new StringWriter();
        CharBasedFilteringTextMapper.removeIf(new StringReader("a1b2c"), sw, StringUtil.MATCHER_ASCII_DIGIT);
        assertEquals("abc", sw.toString());
    }

    // ---------------------------------------------------------------------------
    // retainIf
    // ---------------------------------------------------------------------------

    @Test
    void retainIf_null_returnsNull() {
        assertNull(CharBasedFilteringTextMapper.retainIf(null, StringUtil.MATCHER_ASCII_DIGIT));
    }

    @Test
    void retainIf_emptyString_returnsEmpty() {
        assertEquals("", CharBasedFilteringTextMapper.retainIf("", StringUtil.MATCHER_ASCII_DIGIT));
    }

    @Test
    void retainIf_noMatch_returnsEmpty() {
        assertEquals("", CharBasedFilteringTextMapper.retainIf("abc", StringUtil.MATCHER_ASCII_DIGIT));
    }

    @Test
    void retainIf_allMatch_returnsOriginal() {
        assertEquals("123", CharBasedFilteringTextMapper.retainIf("123", StringUtil.MATCHER_ASCII_DIGIT));
    }

    @Test
    void retainIf_someMatch_retainsMatches() {
        assertEquals("12", CharBasedFilteringTextMapper.retainIf("a1b2c", StringUtil.MATCHER_ASCII_DIGIT));
    }

    @Test
    void retainIf_toStringBuilder_appendsRetained() {
        StringBuilder sb = new StringBuilder();
        CharBasedFilteringTextMapper.retainIf("a1b2c", sb, StringUtil.MATCHER_ASCII_DIGIT);
        assertEquals("12", sb.toString());
    }

    @Test
    void retainIf_toPrintWriter_writesRetained() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        CharBasedFilteringTextMapper.retainIf("a1b2c", pw, StringUtil.MATCHER_ASCII_DIGIT);
        pw.flush();
        assertEquals("12", sw.toString());
    }

    @Test
    void retainIf_fromReader_writesRetained() throws IOException {
        StringWriter sw = new StringWriter();
        CharBasedFilteringTextMapper.retainIf(new StringReader("a1b2c"), sw, StringUtil.MATCHER_ASCII_DIGIT);
        assertEquals("12", sw.toString());
    }

    // ---------------------------------------------------------------------------
    // replace with CharMapper (char -> char)
    // ---------------------------------------------------------------------------

    @Test
    void replaceCharMapper_null_returnsNull() {
        assertNull(CharBasedFilteringTextMapper.replace(null, (StringUtil.CharMapper) c -> c));
    }

    @Test
    void replaceCharMapper_emptyString_returnsEmpty() {
        assertEquals("", CharBasedFilteringTextMapper.replace("", (StringUtil.CharMapper) c -> c));
    }

    @Test
    void replaceCharMapper_noChange_returnsOriginal() {
        // mapper returns same char → no modification
        String result = CharBasedFilteringTextMapper.replace("hello", (StringUtil.CharMapper) c -> c);
        assertEquals("hello", result);
    }

    @Test
    void replaceCharMapper_uppercaseAll() {
        String result = CharBasedFilteringTextMapper.replace("hello", (StringUtil.CharMapper) Character::toUpperCase);
        assertEquals("HELLO", result);
    }

    @Test
    void replaceCharMapper_partialChange() {
        // Replace 'l' with 'r'
        String result = CharBasedFilteringTextMapper.replace("hello", (StringUtil.CharMapper) c -> c == 'l' ? 'r' : c);
        assertEquals("herro", result);
    }

    @Test
    void replaceCharMapper_toStringBuilder() {
        StringBuilder sb = new StringBuilder();
        CharBasedFilteringTextMapper.replace("hello", sb, (StringUtil.CharMapper) Character::toUpperCase);
        assertEquals("HELLO", sb.toString());
    }

    @Test
    void replaceCharMapper_toPrintWriter() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        CharBasedFilteringTextMapper.replace("hello", pw, (StringUtil.CharMapper) Character::toUpperCase);
        pw.flush();
        assertEquals("HELLO", sw.toString());
    }

    @Test
    void replaceCharMapper_fromReader() throws IOException {
        StringWriter sw = new StringWriter();
        CharBasedFilteringTextMapper.replace(new StringReader("hello"), sw, (StringUtil.CharMapper) Character::toUpperCase);
        assertEquals("HELLO", sw.toString());
    }

    // ---------------------------------------------------------------------------
    // replace with CharToStringMapper (char -> CharSequence)
    // ---------------------------------------------------------------------------

    @Test
    void replaceCharToStringMapper_null_returnsNull() {
        assertNull(CharBasedFilteringTextMapper.replace(null, (StringUtil.CharToStringMapper) _ -> null));
    }

    @Test
    void replaceCharToStringMapper_emptyString_returnsEmpty() {
        assertEquals("", CharBasedFilteringTextMapper.replace("", (StringUtil.CharToStringMapper) _ -> null));
    }

    @Test
    void replaceCharToStringMapper_nullMeansKeep() {
        // null return → keep original char
        String result = CharBasedFilteringTextMapper.replace("abc", (StringUtil.CharToStringMapper) _ -> null);
        assertEquals("abc", result);
    }

    @Test
    void replaceCharToStringMapper_emptyMeansRemove() {
        // empty return → remove char
        String result = CharBasedFilteringTextMapper.replace("a1b2c", (StringUtil.CharToStringMapper) c ->
            Character.isDigit(c) ? "" : null);
        assertEquals("abc", result);
    }

    @Test
    void replaceCharToStringMapper_expansionReplacement() {
        // Replace each digit with "(N)"
        String result = CharBasedFilteringTextMapper.replace("a1b2", (StringUtil.CharToStringMapper) c ->
            Character.isDigit(c) ? "(" + c + ")" : null);
        assertEquals("a(1)b(2)", result);
    }

    @Test
    void replaceCharToStringMapper_toStringBuilder() {
        StringBuilder sb = new StringBuilder();
        CharBasedFilteringTextMapper.replace("a1b", sb, (StringUtil.CharToStringMapper) c ->
            Character.isDigit(c) ? "[" + c + "]" : null);
        assertEquals("a[1]b", sb.toString());
    }

    @Test
    void replaceCharToStringMapper_toPrintWriter() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        CharBasedFilteringTextMapper.replace("a1b", pw, (StringUtil.CharToStringMapper) c ->
            Character.isDigit(c) ? "[" + c + "]" : null);
        pw.flush();
        assertEquals("a[1]b", sw.toString());
    }

    @Test
    void replaceCharToStringMapper_fromReader() throws IOException {
        StringWriter sw = new StringWriter();
        CharBasedFilteringTextMapper.replace(new StringReader("a1b"), sw, (StringUtil.CharToStringMapper) c ->
            Character.isDigit(c) ? "[" + c + "]" : null);
        assertEquals("a[1]b", sw.toString());
    }

    // ---------------------------------------------------------------------------
    // TextTransformer factory methods
    // ---------------------------------------------------------------------------

    @Test
    void createRemovingTransformer_removesMatchingChars() {
        TextTransformer t = CharBasedFilteringTextMapper.createRemovingTransformer(StringUtil.MATCHER_ASCII_DIGIT);
        CharSequence result = t.transform("a1b2c");
        assertEquals("abc", result.toString());
    }

    @Test
    void createRetainingTransformer_retainsMatchingChars() {
        TextTransformer t = CharBasedFilteringTextMapper.createRetainingTransformer(StringUtil.MATCHER_ASCII_DIGIT);
        CharSequence result = t.transform("a1b2c");
        assertEquals("12", result.toString());
    }

    @Test
    void createReplacingTransformer_charMapper() {
        TextTransformer t = CharBasedFilteringTextMapper.createReplacingTransformer(
            (StringUtil.CharMapper) Character::toUpperCase);
        CharSequence result = t.transform("hello");
        assertEquals("HELLO", result.toString());
    }

    @Test
    void createReplacingTransformer_charToStringMapper() {
        TextTransformer t = CharBasedFilteringTextMapper.createReplacingTransformer(
            (StringUtil.CharToStringMapper) c -> c == 'x' ? "XX" : null);
        CharSequence result = t.transform("axb");
        assertEquals("aXXb", result.toString());
    }
}

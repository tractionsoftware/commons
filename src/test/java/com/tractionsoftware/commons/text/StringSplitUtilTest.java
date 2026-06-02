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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class StringSplitUtilTest {

    // ---------------------------------------------------------------------------
    // parseCommaSpaceSemicolonSeperatedList
    // ---------------------------------------------------------------------------

    @Test
    void parseCSS_null_returnsEmptyList() {
        List<String> result = StringSplitUtil.parseCommaSpaceSemicolonSeperatedList(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void parseCSS_empty_returnsEmptyList() {
        assertTrue(StringSplitUtil.parseCommaSpaceSemicolonSeperatedList("").isEmpty());
    }

    @Test
    void parseCSS_commaSeparated() {
        List<String> result = StringSplitUtil.parseCommaSpaceSemicolonSeperatedList("a,b,c");
        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    void parseCSS_spaceSeparated() {
        List<String> result = StringSplitUtil.parseCommaSpaceSemicolonSeperatedList("a b c");
        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    void parseCSS_semiSeparated() {
        List<String> result = StringSplitUtil.parseCommaSpaceSemicolonSeperatedList("a;b;c");
        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    void parseCSS_mixed() {
        List<String> result = StringSplitUtil.parseCommaSpaceSemicolonSeperatedList("a,b;c d");
        assertEquals(List.of("a", "b", "c", "d"), result);
    }

    // ---------------------------------------------------------------------------
    // parseCommaSpaceSemicolonSeparatedListSkippingQuoted
    // ---------------------------------------------------------------------------

    @Test
    void parseSkippingQuoted_null_doesNothing() {
        List<String> result = new ArrayList<>();
        StringSplitUtil.parseCommaSpaceSemicolonSeparatedListSkippingQuoted(null, result);
        assertTrue(result.isEmpty());
    }

    @Test
    void parseSkippingQuoted_simple() {
        List<String> result = StringSplitUtil.parseCommaSpaceSemicolonSeparatedListSkippingQuoted("a,b,c");
        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    void parseSkippingQuoted_quotedCommaNotSplit() {
        List<String> result = StringSplitUtil.parseCommaSpaceSemicolonSeparatedListSkippingQuoted("\"a,b\",c");
        assertEquals(List.of("\"a,b\"", "c"), result);
    }

    @Test
    void parseSkippingQuoted_unclosedQuote_closedAutomatically() {
        List<String> result = StringSplitUtil.parseCommaSpaceSemicolonSeparatedListSkippingQuoted("\"a,b");
        assertEquals(1, result.size());
        assertTrue(result.get(0).endsWith("\""));
    }

    // ---------------------------------------------------------------------------
    // parseQuotedGroupedList
    // ---------------------------------------------------------------------------

    @Test
    void parseQuotedGroupedList_nullInput_doesNothing() {
        List<String> result = new ArrayList<>();
        StringSplitUtil.parseQuotedGroupedList(null, result, ",");
        assertTrue(result.isEmpty());
    }

    @Test
    void parseQuotedGroupedList_nullSeparators_doesNothing() {
        List<String> result = new ArrayList<>();
        StringSplitUtil.parseQuotedGroupedList("a,b", result, null);
        assertTrue(result.isEmpty());
    }

    @Test
    void parseQuotedGroupedList_simple() {
        List<String> result = new ArrayList<>();
        StringSplitUtil.parseQuotedGroupedList("a,b,c", result, ",");
        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    void parseQuotedGroupedList_quotedCommaPreserved() {
        List<String> result = new ArrayList<>();
        StringSplitUtil.parseQuotedGroupedList("\"a,b\",c", result, ",");
        assertEquals(List.of("\"a,b\"", "c"), result);
    }

    @Test
    void parseQuotedGroupedList_parenthesesPreserved() {
        List<String> result = new ArrayList<>();
        StringSplitUtil.parseQuotedGroupedList("rgb(1,2,3),blue", result, ",");
        assertEquals(List.of("rgb(1,2,3)", "blue"), result);
    }

    @Test
    void parseQuotedGroupedList_singleQuote() {
        List<String> result = new ArrayList<>();
        StringSplitUtil.parseQuotedGroupedList("'a,b',c", result, ",");
        assertEquals(List.of("'a,b'", "c"), result);
    }

    // ---------------------------------------------------------------------------
    // splitString (Pattern overload)
    // ---------------------------------------------------------------------------

    @Test
    void splitString_pattern_nullValue_returnsFalse() {
        List<String> list = new ArrayList<>();
        assertFalse(StringSplitUtil.splitString(null, Pattern.compile(","), list));
    }

    @Test
    void splitString_pattern_nullList_returnsFalse() {
        assertFalse(StringSplitUtil.splitString("a,b", Pattern.compile(","), null));
    }

    @Test
    void splitString_pattern_simple() {
        List<String> list = new ArrayList<>();
        assertTrue(StringSplitUtil.splitString("a,b,c", Pattern.compile(","), list));
        assertEquals(List.of("a", "b", "c"), list);
    }

    @Test
    void splitString_pattern_emptyTokensSkipped() {
        List<String> list = new ArrayList<>();
        StringSplitUtil.splitString("a,,b", Pattern.compile(","), list);
        assertEquals(List.of("a", "b"), list);
    }

    // ---------------------------------------------------------------------------
    // splitString (Options overload with escaping)
    // ---------------------------------------------------------------------------

    @Test
    void splitString_options_nullValue_returnsFalse() {
        assertFalse(StringSplitUtil.splitString(null, s -> {}, StringSplitUtil.DEFAULT_OPTIONS));
    }

    @Test
    void splitString_options_simple() {
        List<String> result = new ArrayList<>();
        assertTrue(StringSplitUtil.splitString("a,b,c", result::add, StringSplitUtil.DEFAULT_OPTIONS));
        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    void splitString_options_trailingEmptyToken() {
        List<String> result = new ArrayList<>();
        StringSplitUtil.splitString("a,b,", result::add, StringSplitUtil.DEFAULT_OPTIONS);
        assertEquals(List.of("a", "b", ""), result);
    }

    @Test
    void splitString_options_singleValue() {
        List<String> result = new ArrayList<>();
        StringSplitUtil.splitString("hello", result::add, StringSplitUtil.DEFAULT_OPTIONS);
        assertEquals(List.of("hello"), result);
    }

    // ---------------------------------------------------------------------------
    // stringToStringList / stringToStringSet
    // ---------------------------------------------------------------------------

    @Test
    void stringToStringList_null_returnsDefault() {
        List<String> def = List.of("default");
        assertSame(def, StringSplitUtil.stringToStringList(null, def, StringSplitUtil.DEFAULT_LIST_SEPARATOR_PATTERN));
    }

    @Test
    void stringToStringList_empty_returnsDefault() {
        List<String> def = List.of("default");
        assertSame(def, StringSplitUtil.stringToStringList("", def, StringSplitUtil.DEFAULT_LIST_SEPARATOR_PATTERN));
    }

    @Test
    void stringToStringList_commaSeparated() {
        List<String> result = StringSplitUtil.stringToStringList("x,y,z", null, StringSplitUtil.DEFAULT_LIST_SEPARATOR_PATTERN);
        assertEquals(List.of("x", "y", "z"), result);
    }

    @Test
    void stringToStringSet_deduplicates() {
        var result = StringSplitUtil.stringToStringSet("a,b,a", null, StringSplitUtil.DEFAULT_LIST_SEPARATOR_PATTERN);
        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of("a", "b")));
    }

}

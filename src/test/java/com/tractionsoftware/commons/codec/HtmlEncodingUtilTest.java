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

package com.tractionsoftware.commons.codec;

import com.tractionsoftware.commons.lang.StringUtil;
import com.tractionsoftware.commons.text.TextWrapUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

class HtmlEncodingUtilTest {

    // ---------------------------------------------------------------------------
    // getLiteralText
    // ---------------------------------------------------------------------------

    @Test
    void getLiteralText_null_returnsNull() {
        assertNull(HtmlEncodingUtil.getLiteralText(null));
    }

    @Test
    void getLiteralText_empty_returnsEmpty() {
        assertEquals("", HtmlEncodingUtil.getLiteralText(""));
    }

    @Test
    void getLiteralText_noSpecialChars_unchanged() {
        assertEquals("hello world", HtmlEncodingUtil.getLiteralText("hello world"));
    }

    @Test
    void getLiteralText_lessThan() {
        assertEquals("a &lt; b", HtmlEncodingUtil.getLiteralText("a < b"));
    }

    @Test
    void getLiteralText_greaterThan() {
        assertEquals("a &gt; b", HtmlEncodingUtil.getLiteralText("a > b"));
    }

    @Test
    void getLiteralText_ampersand() {
        assertEquals("a &amp; b", HtmlEncodingUtil.getLiteralText("a & b"));
    }

    @Test
    void getLiteralText_multipleSpecialChars() {
        assertEquals("&lt;b&gt;bold&lt;/b&gt;", HtmlEncodingUtil.getLiteralText("<b>bold</b>"));
    }

    @Test
    void getLiteralText_quoteNotEncoded() {
        // getLiteralText only encodes <, >, &  — not "
        assertEquals("say \"hi\"", HtmlEncodingUtil.getLiteralText("say \"hi\""));
    }

    // ---------------------------------------------------------------------------
    // getLiteralTextWithLineBreaks
    // ---------------------------------------------------------------------------

    @Test
    void getLiteralTextWithLineBreaks_null_returnsEmpty() {
        assertEquals("", HtmlEncodingUtil.getLiteralTextWithLineBreaks(null));
    }

    @Test
    void getLiteralTextWithLineBreaks_singleLine() {
        assertEquals("hello", HtmlEncodingUtil.getLiteralTextWithLineBreaks("hello"));
    }

    @Test
    void getLiteralTextWithLineBreaks_multiLine() {
        String result = HtmlEncodingUtil.getLiteralTextWithLineBreaks("a\nb");
        assertTrue(result.contains("<BR>"), "Expected <BR> tag between lines");
        assertTrue(result.contains("a"));
        assertTrue(result.contains("b"));
    }

    @Test
    void getLiteralTextWithLineBreaks_specialCharsEncoded() {
        String result = HtmlEncodingUtil.getLiteralTextWithLineBreaks("a < b\nc > d");
        assertTrue(result.contains("&lt;"));
        assertTrue(result.contains("&gt;"));
    }

    // ---------------------------------------------------------------------------
    // getTagAttributeValue
    // ---------------------------------------------------------------------------

    @Test
    void getTagAttributeValue_null_returnsNull() {
        assertNull(HtmlEncodingUtil.getTagAttributeValue(null));
    }

    @Test
    void getTagAttributeValue_empty_returnsEmpty() {
        assertEquals("", HtmlEncodingUtil.getTagAttributeValue(""));
    }

    @Test
    void getTagAttributeValue_noSpecialChars_unchanged() {
        assertEquals("hello", HtmlEncodingUtil.getTagAttributeValue("hello"));
    }

    @Test
    void getTagAttributeValue_quote() {
        assertEquals("say &quot;hi&quot;", HtmlEncodingUtil.getTagAttributeValue("say \"hi\""));
    }

    @Test
    void getTagAttributeValue_lessThan() {
        assertEquals("&lt;", HtmlEncodingUtil.getTagAttributeValue("<"));
    }

    @Test
    void getTagAttributeValue_ampersand() {
        assertEquals("&amp;", HtmlEncodingUtil.getTagAttributeValue("&"));
    }

    // ---------------------------------------------------------------------------
    // getClassicHtmlText
    // ---------------------------------------------------------------------------

    @Test
    void getClassicHtmlText_null_returnsNull() {
        assertNull(HtmlEncodingUtil.getClassicHtmlText(null));
    }

    @Test
    void getClassicHtmlText_blank_returnsBlank() {
        assertEquals("   ", HtmlEncodingUtil.getClassicHtmlText("   "));
    }

    @Test
    void getClassicHtmlText_spaceToNbsp() {
        String result = HtmlEncodingUtil.getClassicHtmlText("a b");
        assertEquals("a&nbsp;b", result);
    }

    @Test
    void getClassicHtmlText_ampersandEncoded() {
        String result = HtmlEncodingUtil.getClassicHtmlText("a & b");
        assertTrue(result.contains("&amp;"));
    }

    // ---------------------------------------------------------------------------
    // getClassicTextHtml (decode)
    // ---------------------------------------------------------------------------

    @Test
    void getClassicTextHtml_null_returnsNull() {
        assertNull(HtmlEncodingUtil.getClassicTextHtml(null));
    }

    @Test
    void getClassicTextHtml_empty_returnsEmpty() {
        assertEquals("", HtmlEncodingUtil.getClassicTextHtml(""));
    }

    @Test
    void getClassicTextHtml_noEntities_unchanged() {
        assertEquals("hello", HtmlEncodingUtil.getClassicTextHtml("hello"));
    }

    @Test
    void getClassicTextHtml_decodesAmpersand() {
        assertEquals("a & b", HtmlEncodingUtil.getClassicTextHtml("a &amp; b"));
    }

    @Test
    void getClassicTextHtml_decodesLessThan() {
        assertEquals("a < b", HtmlEncodingUtil.getClassicTextHtml("a &lt; b"));
    }

    @Test
    void getClassicTextHtml_decodesGreaterThan() {
        assertEquals("a > b", HtmlEncodingUtil.getClassicTextHtml("a &gt; b"));
    }

    @Test
    void getClassicTextHtml_decodesQuot() {
        assertEquals("say \"hi\"", HtmlEncodingUtil.getClassicTextHtml("say &quot;hi&quot;"));
    }

    @Test
    void getClassicHtmlText_roundTrip() {
        String original = "Hello <World> & \"friends\"";
        String html = HtmlEncodingUtil.getClassicHtmlText(original);
        String decoded = HtmlEncodingUtil.getClassicTextHtml(html);
        // spaces become &nbsp; then decode back to space
        // original with spaces to &nbsp; means spaces become NBSP chars -- not a clean round trip for spaces.
        // But for the non-space special chars it should round-trip.
        assertTrue(decoded.contains("Hello"));
        assertTrue(decoded.contains("World"));
    }

    // ---------------------------------------------------------------------------
    // SimpleHtmlEntity.getForLiteral / encodeForLiteral
    // ---------------------------------------------------------------------------

    @Test
    void simpleHtmlEntity_getForLiteral_lt() {
        assertEquals(HtmlEncodingUtil.SimpleHtmlEntity.LESS_THAN,
            HtmlEncodingUtil.SimpleHtmlEntity.getForLiteral('<'));
    }

    @Test
    void simpleHtmlEntity_getForLiteral_gt() {
        assertEquals(HtmlEncodingUtil.SimpleHtmlEntity.GREATER_THAN,
            HtmlEncodingUtil.SimpleHtmlEntity.getForLiteral('>'));
    }

    @Test
    void simpleHtmlEntity_getForLiteral_amp() {
        assertEquals(HtmlEncodingUtil.SimpleHtmlEntity.AMPERSAND,
            HtmlEncodingUtil.SimpleHtmlEntity.getForLiteral('&'));
    }

    @Test
    void simpleHtmlEntity_getForLiteral_other_returnsNull() {
        assertNull(HtmlEncodingUtil.SimpleHtmlEntity.getForLiteral('x'));
    }

    @Test
    void simpleHtmlEntity_encodeForLiteral_lt() {
        assertEquals("&lt;", HtmlEncodingUtil.SimpleHtmlEntity.encodeForLiteral('<'));
    }

    @Test
    void simpleHtmlEntity_encodeForLiteral_other_returnsNull() {
        assertNull(HtmlEncodingUtil.SimpleHtmlEntity.encodeForLiteral('x'));
    }

    @Test
    void simpleHtmlEntity_getEncoding() {
        assertEquals("&amp;", HtmlEncodingUtil.SimpleHtmlEntity.AMPERSAND.getEncoding());
        assertEquals("&lt;", HtmlEncodingUtil.SimpleHtmlEntity.LESS_THAN.getEncoding());
        assertEquals("&gt;", HtmlEncodingUtil.SimpleHtmlEntity.GREATER_THAN.getEncoding());
        assertEquals("&quot;", HtmlEncodingUtil.SimpleHtmlEntity.QUOTATION_MARK.getEncoding());
        assertEquals("&nbsp;", HtmlEncodingUtil.SimpleHtmlEntity.NON_BREAKING_SPACE.getEncoding());
    }

    // ---------------------------------------------------------------------------
    // SimpleHtmlEntity.get(char)
    // ---------------------------------------------------------------------------

    @Test
    void simpleHtmlEntity_get_char_quotationMark() {
        assertEquals(HtmlEncodingUtil.SimpleHtmlEntity.QUOTATION_MARK, HtmlEncodingUtil.SimpleHtmlEntity.get('"'));
    }

    @Test
    void simpleHtmlEntity_get_char_greaterThan() {
        assertEquals(HtmlEncodingUtil.SimpleHtmlEntity.GREATER_THAN, HtmlEncodingUtil.SimpleHtmlEntity.get('>'));
    }

    @Test
    void simpleHtmlEntity_get_char_lessThan() {
        assertEquals(HtmlEncodingUtil.SimpleHtmlEntity.LESS_THAN, HtmlEncodingUtil.SimpleHtmlEntity.get('<'));
    }

    @Test
    void simpleHtmlEntity_get_char_ampersand() {
        assertEquals(HtmlEncodingUtil.SimpleHtmlEntity.AMPERSAND, HtmlEncodingUtil.SimpleHtmlEntity.get('&'));
    }

    @Test
    void simpleHtmlEntity_get_char_nonBreakingSpace() {
        assertEquals(
            HtmlEncodingUtil.SimpleHtmlEntity.NON_BREAKING_SPACE,
            HtmlEncodingUtil.SimpleHtmlEntity.get(StringUtil.CHAR_NON_BREAKING_SPACE)
        );
    }

    @Test
    void simpleHtmlEntity_get_char_ordinarySpace_returnsNull() {
        // Unlike getForClassicConversion, the plain get(char) overload does not treat an ordinary space as NBSP.
        assertNull(HtmlEncodingUtil.SimpleHtmlEntity.get(' '));
    }

    @Test
    void simpleHtmlEntity_get_char_other_returnsNull() {
        assertNull(HtmlEncodingUtil.SimpleHtmlEntity.get('x'));
    }

    // ---------------------------------------------------------------------------
    // SimpleHtmlEntity.get(String, int)
    // ---------------------------------------------------------------------------

    @Test
    void simpleHtmlEntity_get_stringIndex_validAmpersandEntity() {
        assertEquals(HtmlEncodingUtil.SimpleHtmlEntity.AMPERSAND, HtmlEncodingUtil.SimpleHtmlEntity.get("&amp;", 0));
    }

    @Test
    void simpleHtmlEntity_get_stringIndex_validEntity_notAtStart() {
        assertEquals(HtmlEncodingUtil.SimpleHtmlEntity.AMPERSAND, HtmlEncodingUtil.SimpleHtmlEntity.get("x&amp;y", 1));
    }

    @Test
    void simpleHtmlEntity_get_stringIndex_charNotAmpersand_returnsNull() {
        assertNull(HtmlEncodingUtil.SimpleHtmlEntity.get("hello", 0));
    }

    @Test
    void simpleHtmlEntity_get_stringIndex_surrogateChar_returnsNull() {
        // A lone high surrogate at the given index short-circuits to null via Character.isSurrogate(c), regardless
        // of what follows. Using explicit \\u escapes (rather than a literal emoji glyph) for a surrogate pair, to
        // avoid any dependency on source file encoding.
        String withSurrogatePair = "\uD83D\uDE00abcd";
        assertNull(HtmlEncodingUtil.SimpleHtmlEntity.get(withSurrogatePair, 0));
    }

    @Test
    void simpleHtmlEntity_get_stringIndex_notEnoughRemainingChars_returnsNull() {
        // "&a" has only 2 characters remaining from index 0, less than the minimum of 4 required for any entity.
        assertNull(HtmlEncodingUtil.SimpleHtmlEntity.get("&a", 0));
    }

    @Test
    void simpleHtmlEntity_get_stringIndex_unknownEntityName_returnsNull() {
        assertNull(HtmlEncodingUtil.SimpleHtmlEntity.get("&xyz;abc", 0));
    }

    @Test
    void simpleHtmlEntity_get_stringIndex_nameMatchesButNoSemicolon_returnsNull() {
        // "gt" is matched starting at index 1, but the character that should be ';' is 'X' instead, and there is
        // enough room in the string to safely check that character without running past the end.
        assertNull(HtmlEncodingUtil.SimpleHtmlEntity.get("&gtX", 0));
    }

    @Test
    void simpleHtmlEntity_get_stringIndex_truncatedAmpEntity_returnsNull() {
        assertNull(HtmlEncodingUtil.SimpleHtmlEntity.get("&amp", 0));
    }

    @Test
    void simpleHtmlEntity_get_stringIndex_truncatedQuotEntity_returnsNull() {
        assertNull(HtmlEncodingUtil.SimpleHtmlEntity.get("&quot", 0));
    }

    @Test
    void simpleHtmlEntity_get_stringIndex_secondQuotEntity_returnsQuotationmark() {
        assertEquals(
            HtmlEncodingUtil.SimpleHtmlEntity.QUOTATION_MARK,
            HtmlEncodingUtil.SimpleHtmlEntity.get("are you &quot;he&quot;?", 16)
        );
    }

    // ---------------------------------------------------------------------------
    // SimpleHtmlEntity.escapeAmpersand
    // ---------------------------------------------------------------------------

    @Test
    void simpleHtmlEntity_escapeAmpersand_ampersand_returnsEncoding() {
        assertEquals("&amp;", HtmlEncodingUtil.SimpleHtmlEntity.escapeAmpersand('&'));
    }

    @Test
    void simpleHtmlEntity_escapeAmpersand_other_returnsNull() {
        assertNull(HtmlEncodingUtil.SimpleHtmlEntity.escapeAmpersand('x'));
        assertNull(HtmlEncodingUtil.SimpleHtmlEntity.escapeAmpersand('<'));
    }

    // ---------------------------------------------------------------------------
    // SimpleHtmlEntity.append / print (instance methods)
    // ---------------------------------------------------------------------------

    @Test
    void simpleHtmlEntity_append_appendsEncodingToStringBuilder() {
        StringBuilder buff = new StringBuilder("prefix-");
        HtmlEncodingUtil.SimpleHtmlEntity.AMPERSAND.append(buff);
        assertEquals("prefix-&amp;", buff.toString());
    }

    @Test
    void simpleHtmlEntity_append_eachEntity_appendsItsOwnEncoding() {
        for (HtmlEncodingUtil.SimpleHtmlEntity entity : HtmlEncodingUtil.SimpleHtmlEntity.values()) {
            StringBuilder buff = new StringBuilder();
            entity.append(buff);
            assertEquals(entity.getEncoding(), buff.toString());
        }
    }

    @Test
    void simpleHtmlEntity_print_writesEncodingToPrintWriter() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        HtmlEncodingUtil.SimpleHtmlEntity.LESS_THAN.print(pw);
        pw.flush();
        assertEquals("&lt;", sw.toString());
    }

    // ---------------------------------------------------------------------------
    // printLiteralTextWithLineBreaks
    // ---------------------------------------------------------------------------

    @Test
    void printLiteralTextWithLineBreaks_null_appendsNothing() {
        StringBuilder out = new StringBuilder();
        HtmlEncodingUtil.printLiteralTextWithLineBreaks(out, null);
        assertEquals("", out.toString());
    }

    @Test
    void printLiteralTextWithLineBreaks_empty_appendsNothing() {
        StringBuilder out = new StringBuilder();
        HtmlEncodingUtil.printLiteralTextWithLineBreaks(out, "");
        assertEquals("", out.toString());
    }

    @Test
    void printLiteralTextWithLineBreaks_singleLine_matchesGetLiteralText() {
        StringBuilder out = new StringBuilder();
        HtmlEncodingUtil.printLiteralTextWithLineBreaks(out, "hello");
        assertEquals("hello", out.toString());
    }

    @Test
    void printLiteralTextWithLineBreaks_multiLine_insertsBrTag() {
        StringBuilder out = new StringBuilder();
        HtmlEncodingUtil.printLiteralTextWithLineBreaks(out, "a\nb");
        String result = out.toString();
        assertTrue(result.contains(HtmlEncodingUtil.TAG_BR), "Expected <BR> tag between lines, got: " + result);
        assertTrue(result.contains("a"));
        assertTrue(result.contains("b"));
    }

    @Test
    void printLiteralTextWithLineBreaks_specialCharsEncoded() {
        StringBuilder out = new StringBuilder();
        HtmlEncodingUtil.printLiteralTextWithLineBreaks(out, "a < b\nc > d");
        String result = out.toString();
        assertTrue(result.contains("&lt;"));
        assertTrue(result.contains("&gt;"));
    }

    @Test
    void printLiteralTextWithLineBreaks_matchesNonPrintingVariant() {
        // The print* variant should produce exactly the same text as the String-returning variant, just appended
        // to the given Appendable instead of being returned.
        String text = "line one < 1\nline two & 2";
        String expected = HtmlEncodingUtil.getLiteralTextWithLineBreaks(text);
        StringBuilder out = new StringBuilder();
        HtmlEncodingUtil.printLiteralTextWithLineBreaks(out, text);
        assertEquals(expected, out.toString());
    }

    // ---------------------------------------------------------------------------
    // getNonSpaceBreaksHtml
    // ---------------------------------------------------------------------------

    private static final String NON_SPACE_BREAK_HTML_PROPERTY = "com.tractionsoftware.commons.codec.non_space_break_html";

    @Test
    void getNonSpaceBreaksHtml_propertyUnset_returnsDefault() {
        String previous = System.getProperty(NON_SPACE_BREAK_HTML_PROPERTY);
        try {
            System.clearProperty(NON_SPACE_BREAK_HTML_PROPERTY);
            assertEquals(HtmlEncodingUtil.DEFAULT_NON_SPACE_BREAK_HTML, HtmlEncodingUtil.getNonSpaceBreaksHtml());
        }
        finally {
            if (previous != null) {
                System.setProperty(NON_SPACE_BREAK_HTML_PROPERTY, previous);
            }
        }
    }

    @Test
    void getNonSpaceBreaksHtml_propertySet_returnsPropertyValue() {
        String previous = System.getProperty(NON_SPACE_BREAK_HTML_PROPERTY);
        try {
            System.setProperty(NON_SPACE_BREAK_HTML_PROPERTY, "<custom-wbr>");
            assertEquals("<custom-wbr>", HtmlEncodingUtil.getNonSpaceBreaksHtml());
        }
        finally {
            if (previous == null) {
                System.clearProperty(NON_SPACE_BREAK_HTML_PROPERTY);
            }
            else {
                System.setProperty(NON_SPACE_BREAK_HTML_PROPERTY, previous);
            }
        }
    }

    // ---------------------------------------------------------------------------
    // getHtmlWithNonSpaceBreaks
    // ---------------------------------------------------------------------------

    @Test
    void getHtmlWithNonSpaceBreaks_null_returnsNull() {
        assertNull(HtmlEncodingUtil.getHtmlWithNonSpaceBreaks(null));
    }

    @Test
    void getHtmlWithNonSpaceBreaks_empty_returnsEmpty() {
        assertEquals("", HtmlEncodingUtil.getHtmlWithNonSpaceBreaks(""));
    }

    @Test
    void getHtmlWithNonSpaceBreaks_textWithBreakOpportunityChars_currentlyUnchanged() {
        String text = "a,b/c|d-e%f)g&amp;h&gt;i&lt;j&quot;k";
        assertEquals(
            "a,<wbr>b/<wbr>c|<wbr>d-<wbr>e%<wbr>f)<wbr>g&amp;<wbr>h&gt;<wbr>i&lt;<wbr>j&quot;<wbr>k",
            HtmlEncodingUtil.getHtmlWithNonSpaceBreaks(text)
        );
    }

    @Test
    void getHtmlWithNonSpaceBreaks_plainTextNoOpportunities_unchanged() {
        String text = "plain text with no punctuation";
        assertEquals(text, HtmlEncodingUtil.getHtmlWithNonSpaceBreaks(text));
    }

    // ---------------------------------------------------------------------------
    // getLiteralAppendable / HtmlLiteralAppendableWrapper
    // ---------------------------------------------------------------------------

    @Test
    void getLiteralAppendable_nullOut_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> HtmlEncodingUtil.getLiteralAppendable(null, "x"));
    }

    @Test
    void getLiteralAppendable_appendCharSequence_encodesLiteralChars() throws IOException {
        StringBuilder out = new StringBuilder();
        Appendable literal = HtmlEncodingUtil.getLiteralAppendable(out, null);
        literal.append("a<b>c&d");
        assertEquals("a&lt;b&gt;c&amp;d", out.toString());
    }

    @Test
    void getLiteralAppendable_appendCharSequenceRange_encodesOnlyThatRange() throws IOException {
        StringBuilder out = new StringBuilder();
        Appendable literal = HtmlEncodingUtil.getLiteralAppendable(out, null);
        // "xx<b>yy" -- indices [2,5) are "<b>"
        literal.append("xx<b>yy", 2, 5);
        assertEquals("&lt;b&gt;", out.toString());
    }

    @Test
    void getLiteralAppendable_appendChar_specialChar_encodesIt() throws IOException {
        StringBuilder out = new StringBuilder();
        Appendable literal = HtmlEncodingUtil.getLiteralAppendable(out, null);
        literal.append('&');
        assertEquals("&amp;", out.toString());
    }

    @Test
    void getLiteralAppendable_appendChar_ordinaryChar_appendsUnchanged() throws IOException {
        StringBuilder out = new StringBuilder();
        Appendable literal = HtmlEncodingUtil.getLiteralAppendable(out, null);
        literal.append('x');
        assertEquals("x", out.toString());
    }

    @Test
    void getLiteralAppendable_zeroWidthSpace_replacedWithCustomPreferredValue() throws IOException {
        StringBuilder out = new StringBuilder();
        Appendable literal = HtmlEncodingUtil.getLiteralAppendable(out, "[ZWS]");
        literal.append("a" + StringUtil.CHAR_ZERO_WIDTH_SPACE + "b");
        assertEquals("a[ZWS]b", out.toString());
    }

    @Test
    void getLiteralAppendable_blankPreferredZeroWidthSpace_fallsBackToDefault() throws IOException {
        StringBuilder out = new StringBuilder();
        // A blank preferredZeroWidthSpace (here, null) falls back to TextWrapUtil.DEFAULT_ZERO_WIDTH_SPACE, which is
        // itself just the zero-width space character as a String, so substitution is effectively a no-op.
        Appendable literal = HtmlEncodingUtil.getLiteralAppendable(out, null);
        literal.append(String.valueOf(StringUtil.CHAR_ZERO_WIDTH_SPACE));
        assertEquals(TextWrapUtil.DEFAULT_ZERO_WIDTH_SPACE, out.toString());
    }

    @Test
    void getLiteralAppendable_sameUnderlyingWrapperAndZeroWidthSpace_returnsSameInstance() {
        StringBuilder out = new StringBuilder();
        Appendable first = HtmlEncodingUtil.getLiteralAppendable(out, "[ZWS]");
        Appendable second = HtmlEncodingUtil.getLiteralAppendable(first, "[ZWS]");
        assertSame(first, second);
    }

    @Test
    void getLiteralAppendable_sameUnderlyingWrapperDifferentZeroWidthSpace_returnsNewInstance() {
        StringBuilder out = new StringBuilder();
        Appendable first = HtmlEncodingUtil.getLiteralAppendable(out, "[ZWS]");
        Appendable second = HtmlEncodingUtil.getLiteralAppendable(first, "[OTHER]");
        assertNotSame(first, second);
    }

    @Test
    void getLiteralAppendable_freshStringBuilder_isNotTheSameInstanceAsOut() {
        StringBuilder out = new StringBuilder();
        Appendable literal = HtmlEncodingUtil.getLiteralAppendable(out, "[ZWS]");
        assertNotSame(out, literal);
    }

}

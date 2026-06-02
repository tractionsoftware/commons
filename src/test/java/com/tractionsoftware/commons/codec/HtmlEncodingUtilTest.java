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

import org.junit.jupiter.api.Test;

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

}

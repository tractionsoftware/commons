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

class JavaScriptEncodingUtilTest {

    // ---------------------------------------------------------------------------
    // getJavascriptLiteral (htmlCompatible=false)
    // ---------------------------------------------------------------------------

    @Test
    void getJavascriptLiteral_null_returnsNull() {
        assertNull(JavaScriptEncodingUtil.getJavascriptLiteral(null));
    }

    @Test
    void getJavascriptLiteral_empty_returnsEmpty() {
        assertEquals("", JavaScriptEncodingUtil.getJavascriptLiteral(""));
    }

    @Test
    void getJavascriptLiteral_noSpecialChars_unchanged() {
        assertEquals("hello world", JavaScriptEncodingUtil.getJavascriptLiteral("hello world"));
    }

    @Test
    void getJavascriptLiteral_singleQuote() {
        assertEquals("\\'", JavaScriptEncodingUtil.getJavascriptLiteral("'"));
    }

    @Test
    void getJavascriptLiteral_doubleQuote() {
        assertEquals("\\\"", JavaScriptEncodingUtil.getJavascriptLiteral("\""));
    }

    @Test
    void getJavascriptLiteral_backslash() {
        assertEquals("\\\\", JavaScriptEncodingUtil.getJavascriptLiteral("\\"));
    }

    @Test
    void getJavascriptLiteral_slash() {
        assertEquals("\\/", JavaScriptEncodingUtil.getJavascriptLiteral("/"));
    }

    @Test
    void getJavascriptLiteral_newLine() {
        assertEquals("\\n", JavaScriptEncodingUtil.getJavascriptLiteral("\n"));
    }

    @Test
    void getJavascriptLiteral_carriageReturn() {
        assertEquals("\\r", JavaScriptEncodingUtil.getJavascriptLiteral("\r"));
    }

    @Test
    void getJavascriptLiteral_tab() {
        assertEquals("\\t", JavaScriptEncodingUtil.getJavascriptLiteral("\t"));
    }

    @Test
    void getJavascriptLiteral_formFeed() {
        assertEquals("\\f", JavaScriptEncodingUtil.getJavascriptLiteral("\f"));
    }

    @Test
    void getJavascriptLiteral_anglesNotEscaped() {
        // < and > are not escaped in non-HTML-compatible mode
        assertEquals("<>", JavaScriptEncodingUtil.getJavascriptLiteral("<>"));
    }

    @Test
    void getJavascriptLiteral_mixed() {
        assertEquals("He said \\'hello\\'\\nGoodbye", JavaScriptEncodingUtil.getJavascriptLiteral("He said 'hello'\nGoodbye"));
    }

    // ---------------------------------------------------------------------------
    // getHtmlCompatibleJavascriptLiteral
    // ---------------------------------------------------------------------------

    @Test
    void getHtmlCompatibleJavascriptLiteral_null_returnsNull() {
        assertNull(JavaScriptEncodingUtil.getHtmlCompatibleJavascriptLiteral(null));
    }

    @Test
    void getHtmlCompatibleJavascriptLiteral_empty_returnsEmpty() {
        assertEquals("", JavaScriptEncodingUtil.getHtmlCompatibleJavascriptLiteral(""));
    }

    @Test
    void getHtmlCompatibleJavascriptLiteral_lessThan() {
        assertEquals("&lt;", JavaScriptEncodingUtil.getHtmlCompatibleJavascriptLiteral("<"));
    }

    @Test
    void getHtmlCompatibleJavascriptLiteral_greaterThan() {
        assertEquals("&gt;", JavaScriptEncodingUtil.getHtmlCompatibleJavascriptLiteral(">"));
    }

    @Test
    void getHtmlCompatibleJavascriptLiteral_ampersand() {
        assertEquals("&amp;", JavaScriptEncodingUtil.getHtmlCompatibleJavascriptLiteral("&"));
    }

    @Test
    void getHtmlCompatibleJavascriptLiteral_singleQuoteStillEscaped() {
        assertEquals("\\'", JavaScriptEncodingUtil.getHtmlCompatibleJavascriptLiteral("'"));
    }

    @Test
    void getHtmlCompatibleJavascriptLiteral_mixed() {
        String result = JavaScriptEncodingUtil.getHtmlCompatibleJavascriptLiteral("<script>alert('xss')</script>");
        assertEquals("&lt;script&gt;alert(\\'xss\\')&lt;\\/script&gt;", result);
    }

    // ---------------------------------------------------------------------------
    // printJavascriptLiteral
    // ---------------------------------------------------------------------------

    @Test
    void printJavascriptLiteral_empty_writesNothing() {
        StringBuilder sb = new StringBuilder();
        JavaScriptEncodingUtil.printJavascriptLiteral(sb, "");
        assertEquals("", sb.toString());
    }

    @Test
    void printJavascriptLiteral_null_writesNothing() {
        StringBuilder sb = new StringBuilder();
        JavaScriptEncodingUtil.printJavascriptLiteral(sb, null);
        assertEquals("", sb.toString());
    }

    @Test
    void printJavascriptLiteral_escapesSpecialChars() {
        StringBuilder sb = new StringBuilder();
        JavaScriptEncodingUtil.printJavascriptLiteral(sb, "a'b");
        assertEquals("a\\'b", sb.toString());
    }

    @Test
    void printHtmlCompatibleJavascriptLiteral_encodesAngles() {
        StringBuilder sb = new StringBuilder();
        JavaScriptEncodingUtil.printHtmlCompatibleJavascriptLiteral(sb, "a<b");
        assertEquals("a&lt;b", sb.toString());
    }

    // ---------------------------------------------------------------------------
    // CharacterRequiringEscaping
    // ---------------------------------------------------------------------------

    @Test
    void characterRequiringEscaping_get_knownChars() {
        assertEquals(JavaScriptEncodingUtil.CharacterRequiringEscaping.APOSTROPHE,
            JavaScriptEncodingUtil.CharacterRequiringEscaping.get('\''));
        assertEquals(JavaScriptEncodingUtil.CharacterRequiringEscaping.QUOTATION_MARK,
            JavaScriptEncodingUtil.CharacterRequiringEscaping.get('"'));
        assertEquals(JavaScriptEncodingUtil.CharacterRequiringEscaping.BACKSLASH,
            JavaScriptEncodingUtil.CharacterRequiringEscaping.get('\\'));
        assertEquals(JavaScriptEncodingUtil.CharacterRequiringEscaping.NEW_LINE,
            JavaScriptEncodingUtil.CharacterRequiringEscaping.get('\n'));
    }

    @Test
    void characterRequiringEscaping_get_unknownChar_returnsNull() {
        assertNull(JavaScriptEncodingUtil.CharacterRequiringEscaping.get('x'));
    }

    @Test
    void characterRequiringEscaping_isQuotationMark() {
        assertTrue(JavaScriptEncodingUtil.CharacterRequiringEscaping.APOSTROPHE.isQuotationMark());
        assertTrue(JavaScriptEncodingUtil.CharacterRequiringEscaping.QUOTATION_MARK.isQuotationMark());
        assertFalse(JavaScriptEncodingUtil.CharacterRequiringEscaping.BACKSLASH.isQuotationMark());
        assertFalse(JavaScriptEncodingUtil.CharacterRequiringEscaping.NEW_LINE.isQuotationMark());
    }

    @Test
    void characterRequiringEscaping_getEscapeSequence() {
        assertEquals("\\'", JavaScriptEncodingUtil.CharacterRequiringEscaping.APOSTROPHE.getEscapeSequence());
        assertEquals("\\\"", JavaScriptEncodingUtil.CharacterRequiringEscaping.QUOTATION_MARK.getEscapeSequence());
        assertEquals("\\\\", JavaScriptEncodingUtil.CharacterRequiringEscaping.BACKSLASH.getEscapeSequence());
        assertEquals("\\n", JavaScriptEncodingUtil.CharacterRequiringEscaping.NEW_LINE.getEscapeSequence());
        assertEquals("\\r", JavaScriptEncodingUtil.CharacterRequiringEscaping.CARRIAGE_RETURN.getEscapeSequence());
        assertEquals("\\t", JavaScriptEncodingUtil.CharacterRequiringEscaping.TAB.getEscapeSequence());
        assertEquals("\\f", JavaScriptEncodingUtil.CharacterRequiringEscaping.FORM_FEED.getEscapeSequence());
        assertEquals("\\b", JavaScriptEncodingUtil.CharacterRequiringEscaping.BACKSPACE.getEscapeSequence());
    }

    @Test
    void characterRequiringEscaping_getRequiredEscapeSequence_unknown_returnsNull() {
        assertNull(JavaScriptEncodingUtil.CharacterRequiringEscaping.getRequiredEscapeSequence('z'));
    }

}

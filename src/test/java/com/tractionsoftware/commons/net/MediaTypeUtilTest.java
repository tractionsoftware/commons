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

package com.tractionsoftware.commons.net;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public final class MediaTypeUtilTest {

    @Test
    public void testIsTextContentType1() {
        assertFalse(MediaTypeUtil.isTextContentType((String) null));
    }

    @Test
    public void testIsTextContentType2() {
        assertTrue(MediaTypeUtil.isTextContentType("text/plain"));
    }

    @Test
    public void testIsTextContentType3() {
        assertTrue(MediaTypeUtil.isTextContentType("text/html"));
    }

    @Test
    public void testIsTextContentType4() {
        assertTrue(MediaTypeUtil.isTextContentType("text/foo"));
    }

    @Test
    public void testIsTextContentType5() {
        assertTrue(MediaTypeUtil.isTextContentType("application/xml"));
    }

    @Test
    public void testIsTextContentType6() {
        assertFalse(MediaTypeUtil.isTextContentType("application/xxxml"));
    }

    @Test
    public void testIsTextContentType7() {
        assertFalse(MediaTypeUtil.isTextContentType("application/octet-stream"));
    }

    @Test
    public void testIsTextContentType8() {
        assertTrue(MediaTypeUtil.isTextContentType("application/xml"));
    }

    @Test
    public void testIsTextContentType9() {
        assertTrue(MediaTypeUtil.isTextContentType("application/problems+json"));
    }

    @Test
    public void testIsTextContentType10() {
        assertTrue(MediaTypeUtil.isTextContentType("application/xhtml+xml"));
    }

    @Test
    public void testIsTextContentType11() {
        assertTrue(MediaTypeUtil.isTextContentType("application/json"));
    }

    @Test
    public void testIsTextContentType13() {
        assertTrue(MediaTypeUtil.isTextContentType("text/markdown"));
    }

    @Test
    public void testIsTextContentType14() {
        assertTrue(MediaTypeUtil.isTextContentType("text/whatever"));
    }

    @Test
    public void testIsTextContentType15() {
        assertTrue(MediaTypeUtil.isTextContentType("message/rfc822"));
    }

    @Test
    public void testIsTextContentType16() {
        // "application/json+[anything]" is not really a valid type.
        assertFalse(MediaTypeUtil.isTextContentType("application/json+foo"));
    }

    @Test
    public void testIsPlainTextContentType1() {
        assertFalse(MediaTypeUtil.isTextPlainContentType(null));
    }

    @Test
    public void testIsPlainTextContentType2() {
        // isTextPlainContentType checks for exactly this type, so this is the only one of these tests expecting true.
        assertTrue(MediaTypeUtil.isTextPlainContentType("text/plain"));
    }

    @Test
    public void testIsPlainTextContentType3() {
        assertFalse(MediaTypeUtil.isTextPlainContentType("text/html"));
    }

    @Test
    public void testIsPlainTextContentType4() {
        assertFalse(MediaTypeUtil.isTextPlainContentType("text/foo"));
    }

    @Test
    public void testIsPlainTextContentType5() {
        assertFalse(MediaTypeUtil.isTextPlainContentType("application/xml"));
    }

    @Test
    public void testIsPlainTextContentType6() {
        assertFalse(MediaTypeUtil.isTextPlainContentType("application/xxxml"));
    }

    @Test
    public void testIsPlainTextContentType7() {
        assertFalse(MediaTypeUtil.isTextPlainContentType("application/octet-stream"));
    }

    @Test
    public void testIsPlainTextContentType8() {
        assertFalse(MediaTypeUtil.isTextPlainContentType("application/xml"));
    }

    @Test
    public void testIsPlainTextContentType9() {
        assertFalse(MediaTypeUtil.isTextPlainContentType("application/problems+json"));
    }

    @Test
    public void testIsPlainTextContentType10() {
        assertFalse(MediaTypeUtil.isTextPlainContentType("application/xhtml+xml"));
    }

    @Test
    public void testIsPlainTextContentType11() {
        assertFalse(MediaTypeUtil.isTextPlainContentType("application/json"));
    }

    @Test
    public void testIsPlainTextContentType13() {
        assertFalse(MediaTypeUtil.isTextPlainContentType("text/markdown"));
    }

    @Test
    public void testIsPlainTextContentType14() {
        assertFalse(MediaTypeUtil.isTextPlainContentType("text/whatever"));
    }

    @Test
    public void testIsPlainTextContentType15() {
        assertFalse(MediaTypeUtil.isTextPlainContentType("message/rfc822"));
    }

    @Test
    public void testIsPlainTextContentType16() {
        assertFalse(MediaTypeUtil.isTextPlainContentType("application/json+foo"));
    }

    @Test
    public void testContainsCharsetParameter1() {
        assertFalse(MediaTypeUtil.hasCharsetParameter(null));
    }

    @Test
    public void testContainsCharsetParameter2() {
        assertFalse(MediaTypeUtil.hasCharsetParameter(""));
    }

    @Test
    public void testContainsCharsetParameter3() {
        assertTrue(MediaTypeUtil.hasCharsetParameter("text/html; charset=UTF-8"));
    }

    @Test
    public void testContainsCharsetParameter4() {
        assertTrue(MediaTypeUtil.hasCharsetParameter("text/html; charset=windows-1252"));
    }

    @Test
    public void testContainsCharsetParameter5() {
        assertFalse(MediaTypeUtil.hasCharsetParameter("text/html"));
    }

    @Test
    public void testContainsCharsetParameter6() {
        assertFalse(MediaTypeUtil.hasCharsetParameter("text/html; param=foo; charset="));
    }

    @Test
    public void testContainsCharsetParameter7() {
        assertTrue(MediaTypeUtil.hasCharsetParameter("text/html;charset=iso-8859-1"));
    }

    @Test
    public void testContainsCharsetParameter8() {
        assertFalse(MediaTypeUtil.hasCharsetParameter("text/html;charset="));
    }

    @Test
    public void testContainsCharsetParameter9() {
        assertFalse(MediaTypeUtil.hasCharsetParameter("charset=UTF-8"));
    }

    // -------------------------------------------------------------------------
    // parseMediaType
    // -------------------------------------------------------------------------

    @Test
    public void parseMediaType_null_returnsNull() {
        assertNull(MediaTypeUtil.parseMediaType(null));
    }

    @Test
    public void parseMediaType_blank_returnsNull() {
        assertNull(MediaTypeUtil.parseMediaType("  "));
    }

    @Test
    public void parseMediaType_valid_returnsType() {
        var result = MediaTypeUtil.parseMediaType("text/html");
        assertNotNull(result);
        assertEquals("text", result.type());
        assertEquals("html", result.subtype());
    }

    @Test
    public void parseMediaType_invalid_returnsDefault() {
        var fallback = com.google.common.net.MediaType.PLAIN_TEXT_UTF_8;
        var result = MediaTypeUtil.parseMediaType("not-a-media-type!!!", fallback);
        assertEquals(fallback, result);
    }

    // -------------------------------------------------------------------------
    // isTextHtmlContentType
    // -------------------------------------------------------------------------

    @Test
    public void isTextHtmlContentType_null_returnsFalse() {
        assertFalse(MediaTypeUtil.isTextHtmlContentType(null));
    }

    @Test
    public void isTextHtmlContentType_textHtml_returnsTrue() {
        assertTrue(MediaTypeUtil.isTextHtmlContentType("text/html"));
    }

    @Test
    public void isTextHtmlContentType_textPlain_returnsFalse() {
        assertFalse(MediaTypeUtil.isTextHtmlContentType("text/plain"));
    }

    // -------------------------------------------------------------------------
    // matchesMainAndSubTypes
    // -------------------------------------------------------------------------

    @Test
    public void matchesMainAndSubTypes_nullSpec_returnsFalse() {
        assertFalse(MediaTypeUtil.matchesMainAndSubTypes(null, com.google.common.net.MediaType.HTML_UTF_8));
    }

    @Test
    public void matchesMainAndSubTypes_nullType_returnsFalse() {
        assertFalse(MediaTypeUtil.matchesMainAndSubTypes("text/html", null));
    }

    @Test
    public void matchesMainAndSubTypes_match_returnsTrue() {
        assertTrue(MediaTypeUtil.matchesMainAndSubTypes("text/html; charset=UTF-8", com.google.common.net.MediaType.HTML_UTF_8));
    }

    @Test
    public void matchesMainAndSubTypes_noMatch_returnsFalse() {
        assertFalse(MediaTypeUtil.matchesMainAndSubTypes("text/plain", com.google.common.net.MediaType.HTML_UTF_8));
    }

    // -------------------------------------------------------------------------
    // getExtensionFromContentType / getContentTypeFromExtension
    // -------------------------------------------------------------------------

    @Test
    public void getExtensionFromContentType_null_returnsNull() {
        assertNull(MediaTypeUtil.getExtensionFromContentType((String) null));
    }

    @Test
    public void getExtensionFromContentType_textHtml_returnsHtml() {
        String ext = MediaTypeUtil.getExtensionFromContentType("text/html");
        assertNotNull(ext);
        // Expected to return "html" (or "htm") for text/html
        assertTrue(ext.equals("html") || ext.equals("htm"),
            "Expected html extension, got: " + ext);
    }

    @Test
    public void getContentTypeFromExtension_html_returnsTextHtml() {
        var type = MediaTypeUtil.getContentTypeFromExtension("html");
        assertNotNull(type);
        assertEquals("text", type.type());
        assertEquals("html", type.subtype());
    }

    @Test
    public void getContentTypeFromExtension_null_returnsNull() {
        assertNull(MediaTypeUtil.getContentTypeFromExtension(null));
    }

}

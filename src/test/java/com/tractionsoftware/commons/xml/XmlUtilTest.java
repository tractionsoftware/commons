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

package com.tractionsoftware.commons.xml;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.*;

public final class XmlUtilTest {

    // isValidXMLCharacter

    @Test
    void isValidXMLCharacter_normalAscii_true() {
        assertTrue(XmlUtil.isValidXMLCharacter('A'));
        assertTrue(XmlUtil.isValidXMLCharacter('z'));
        assertTrue(XmlUtil.isValidXMLCharacter('0'));
        assertTrue(XmlUtil.isValidXMLCharacter(' '));
    }

    @Test
    void isValidXMLCharacter_nul_false() {
        assertFalse(XmlUtil.isValidXMLCharacter(0x0));
    }

    @Test
    void isValidXMLCharacter_validRanges() {
        assertTrue(XmlUtil.isValidXMLCharacter(0x1));
        assertTrue(XmlUtil.isValidXMLCharacter(0xD7FF));
        assertTrue(XmlUtil.isValidXMLCharacter(0xE000));
        assertTrue(XmlUtil.isValidXMLCharacter(0xFFFD));
        assertTrue(XmlUtil.isValidXMLCharacter(0x10000));
        assertTrue(XmlUtil.isValidXMLCharacter(0x10FFFF));
    }

    @Test
    void isValidXMLCharacter_surrogateRange_false() {
        assertFalse(XmlUtil.isValidXMLCharacter(0xD800));
        assertFalse(XmlUtil.isValidXMLCharacter(0xDFFF));
        assertFalse(XmlUtil.isValidXMLCharacter(0xFFFE));
        assertFalse(XmlUtil.isValidXMLCharacter(0xFFFF));
    }

    // isRestrictedXMLCharacter

    @Test
    void isRestrictedXMLCharacter_controlChars_true() {
        assertTrue(XmlUtil.isRestrictedXMLCharacter(0x1));
        assertTrue(XmlUtil.isRestrictedXMLCharacter(0x8));
        assertTrue(XmlUtil.isRestrictedXMLCharacter(0x7F));
        assertTrue(XmlUtil.isRestrictedXMLCharacter(0x9F));
    }

    @Test
    void isRestrictedXMLCharacter_normalAscii_false() {
        assertFalse(XmlUtil.isRestrictedXMLCharacter('A'));
        assertFalse(XmlUtil.isRestrictedXMLCharacter('\n'));
        assertFalse(XmlUtil.isRestrictedXMLCharacter('\t'));
    }

    // isDiscouragedXMLCharacter

    @Test
    void isDiscouragedXMLCharacter_restrictedChar_true() {
        assertTrue(XmlUtil.isDiscouragedXMLCharacter(0x1));
    }

    @Test
    void isDiscouragedXMLCharacter_normalAscii_false() {
        assertFalse(XmlUtil.isDiscouragedXMLCharacter('A'));
    }

    // isCompletelySafeCharacter

    @Test
    void isCompletelySafeCharacter_normalText_true() {
        assertTrue(XmlUtil.isCompletelySafeCharacter('H'));
        assertTrue(XmlUtil.isCompletelySafeCharacter('e'));
    }

    @Test
    void isCompletelySafeCharacter_controlChar_false() {
        assertFalse(XmlUtil.isCompletelySafeCharacter(0x1));
    }

    // isPermanentlyUndefined

    @Test
    void isPermanentlyUndefined_knownRanges_true() {
        assertTrue(XmlUtil.isPermanentlyUndefined(0xFDD0));
        assertTrue(XmlUtil.isPermanentlyUndefined(0xFDDF));
        assertTrue(XmlUtil.isPermanentlyUndefined(0x10FFFF));
    }

    @Test
    void isPermanentlyUndefined_normalAscii_false() {
        assertFalse(XmlUtil.isPermanentlyUndefined('A'));
    }

    // isInDiscouragedUnicodeBlock

    @Test
    void isInDiscouragedUnicodeBlock_normalLatin_false() {
        assertFalse(XmlUtil.isInDiscouragedUnicodeBlock('A'));
        assertFalse(XmlUtil.isInDiscouragedUnicodeBlock('Z'));
    }

    // getLiteralText

    @Test
    void getLiteralText_null_returnsNull() {
        assertNull(XmlUtil.getLiteralText(null));
    }

    @Test
    void getLiteralText_empty_returnsEmpty() {
        assertEquals("", XmlUtil.getLiteralText(""));
    }

    @Test
    void getLiteralText_plainText_unchanged() {
        assertEquals("Hello World", XmlUtil.getLiteralText("Hello World"));
    }

    @Test
    void getLiteralText_lessThan_encoded() {
        String result = XmlUtil.getLiteralText("<tag>");
        assertFalse(result.contains("<"), result);
        assertTrue(result.contains("&lt;"), result);
    }

    @Test
    void getLiteralText_greaterThan_encoded() {
        String result = XmlUtil.getLiteralText("a>b");
        assertFalse(result.contains(">"), result);
        assertTrue(result.contains("&gt;"), result);
    }

    @Test
    void getLiteralText_ampersand_encoded() {
        String result = XmlUtil.getLiteralText("Tom & Jerry");
        assertFalse(result.contains(" & "), result);
        assertTrue(result.contains("&amp;"), result);
    }

    @Test
    void getLiteralText_quote_encoded() {
        String result = XmlUtil.getLiteralText("\"quoted\"");
        assertFalse(result.contains("\""), result);
        assertTrue(result.contains("&quot;"), result);
    }

    // getHexEncoding

    @Test
    void getHexEncoding_singleDigit_producesEncoding() {
        CharSequence enc = XmlUtil.getHexEncoding(0xA);
        assertNotNull(enc);
        String s = enc.toString();
        assertTrue(s.startsWith("&#x"), s);
        assertTrue(s.endsWith(";"), s);
    }

    @Test
    void getHexEncoding_largeCodePoint_producesEncoding() {
        CharSequence enc = XmlUtil.getHexEncoding(0x10000);
        assertNotNull(enc);
        String s = enc.toString();
        assertTrue(s.contains("10000") || s.contains("1000"), s);
    }

    // CharacterSafetyLevel

    @Test
    void characterSafetyLevel_avoidInvalid_acceptsNormalText() {
        assertTrue(XmlUtil.CharacterSafetyLevel.AVOID_INVALID.isSafe('A'));
    }

    @Test
    void characterSafetyLevel_avoidInvalid_rejectsNul() {
        assertFalse(XmlUtil.CharacterSafetyLevel.AVOID_INVALID.isSafe(0x0));
    }

    @Test
    void characterSafetyLevel_avoidRestricted_rejectsRestricted() {
        assertFalse(XmlUtil.CharacterSafetyLevel.AVOID_RESTRICTED.isSafe(0x1));
    }

    @Test
    void characterSafetyLevel_avoidDiscouraged_rejectsDiscouraged() {
        assertFalse(XmlUtil.CharacterSafetyLevel.AVOID_DISCOURAGED.isSafe(0x1));
    }

    @Test
    void characterSafetyLevel_getMinimal() {
        assertEquals(XmlUtil.CharacterSafetyLevel.AVOID_DISCOURAGED,
            XmlUtil.CharacterSafetyLevel.getMinimal(true, true));
        assertEquals(XmlUtil.CharacterSafetyLevel.AVOID_RESTRICTED,
            XmlUtil.CharacterSafetyLevel.getMinimal(true, false));
        assertEquals(XmlUtil.CharacterSafetyLevel.AVOID_INVALID,
            XmlUtil.CharacterSafetyLevel.getMinimal(false, false));
    }

    @Test
    void characterSafetyLevel_isUnsafe_oppositeOfIsSafe() {
        assertTrue(XmlUtil.CharacterSafetyLevel.AVOID_INVALID.isUnsafe(0x0));
        assertFalse(XmlUtil.CharacterSafetyLevel.AVOID_INVALID.isUnsafe('A'));
    }

    // createSafeReader

    @Test
    void createSafeReader_plainText_passesThrough() throws IOException {
        Reader r = XmlUtil.createSafeReader("Hello", XmlUtil.CharacterSafetyLevel.AVOID_INVALID);
        char[] buf = new char[10];
        int len = r.read(buf);
        assertEquals("Hello", new String(buf, 0, len));
    }

    @Test
    void createSafeReader_closeThenRead_throws() throws IOException {
        Reader r = XmlUtil.createSafeReader("Hello", XmlUtil.CharacterSafetyLevel.AVOID_INVALID);
        r.close();
        assertThrows(IOException.class, r::read);
    }

    @Test
    void createSafeReader_markNotSupported() throws IOException {
        Reader r = XmlUtil.createSafeReader("Hello", XmlUtil.CharacterSafetyLevel.AVOID_INVALID);
        assertFalse(r.markSupported());
        assertThrows(IOException.class, () -> r.mark(10));
        assertThrows(IOException.class, r::reset);
    }

    @Test
    void createSafeReader_skip_works() throws IOException {
        Reader r = XmlUtil.createSafeReader("Hello", XmlUtil.CharacterSafetyLevel.AVOID_INVALID);
        long skipped = r.skip(2);
        assertEquals(2, skipped);
        char[] buf = new char[10];
        int len = r.read(buf);
        assertEquals("llo", new String(buf, 0, len));
    }

    // createSafeWriter

    @Test
    void createSafeWriter_plainText_passesThrough() throws IOException {
        StringWriter sw = new StringWriter();
        Writer w = XmlUtil.createSafeWriter(sw);
        w.write("Hello");
        w.flush();
        assertEquals("Hello", sw.toString());
    }

    @Test
    void createSafeWriter_writeCharArray_works() throws IOException {
        StringWriter sw = new StringWriter();
        Writer w = XmlUtil.createSafeWriter(sw, XmlUtil.CharacterSafetyLevel.AVOID_RESTRICTED);
        char[] chars = "Hello".toCharArray();
        w.write(chars, 0, chars.length);
        w.flush();
        assertEquals("Hello", sw.toString());
    }

    @Test
    void createSafeWriter_writeStringSlice_works() throws IOException {
        StringWriter sw = new StringWriter();
        Writer w = XmlUtil.createSafeWriter(sw, XmlUtil.CharacterSafetyLevel.AVOID_RESTRICTED);
        w.write("Hello World", 6, 5);
        w.flush();
        assertEquals("World", sw.toString());
    }

    @Test
    void createSafeWriter_closeThenWrite_throws() throws IOException {
        StringWriter sw = new StringWriter();
        Writer w = XmlUtil.createSafeWriter(sw);
        w.close();
        assertThrows(IOException.class, () -> w.write('A'));
    }

    @Test
    void characterSafetyLevel_createSafeWriter_works() throws IOException {
        StringWriter sw = new StringWriter();
        Writer w = XmlUtil.CharacterSafetyLevel.AVOID_INVALID.createSafeWriter(sw, '*');
        w.write("Hello");
        w.flush();
        assertEquals("Hello", sw.toString());
    }

    @Test
    void createSafeWriter_nullWriter_throwsNPE() {
        assertThrows(NullPointerException.class, () ->
            XmlUtil.createSafeWriter(null, XmlUtil.CharacterSafetyLevel.AVOID_INVALID));
    }

    @Test
    void createSafeReader_nullReader_throwsNPE() {
        assertThrows(NullPointerException.class, () ->
            XmlUtil.createSafeReader((java.io.Reader) null, XmlUtil.CharacterSafetyLevel.AVOID_INVALID, ' '));
    }

}

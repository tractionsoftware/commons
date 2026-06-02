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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class StringWriteUtilTest {

    // ---------------------------------------------------------------------------
    // safeAppend(StringBuilder, Object)
    // ---------------------------------------------------------------------------

    @Test
    void safeAppend_nullBuffer_noOp() {
        StringWriteUtil.safeAppend(null, (Object) "hello");
    }

    @Test
    void safeAppend_nullValue_noOp() {
        StringBuilder sb = new StringBuilder("x");
        StringWriteUtil.safeAppend(sb, (Object) null);
        assertEquals("x", sb.toString());
    }

    @Test
    void safeAppend_appendsObject() {
        StringBuilder sb = new StringBuilder();
        StringWriteUtil.safeAppend(sb, 42);
        assertEquals("42", sb.toString());
    }

    // ---------------------------------------------------------------------------
    // safeAppend(Appendable, CharSequence)
    // ---------------------------------------------------------------------------

    @Test
    void safeAppend_appendable_nullOut_noOp() {
        StringWriteUtil.safeAppend((Appendable) null, "hello");
    }

    @Test
    void safeAppend_appendable_emptyStr_noOp() {
        StringBuilder sb = new StringBuilder("x");
        StringWriteUtil.safeAppend((Appendable) sb, "");
        assertEquals("x", sb.toString());
    }

    @Test
    void safeAppend_appendable_normal() {
        StringBuilder sb = new StringBuilder();
        StringWriteUtil.safeAppend((Appendable) sb, "hello");
        assertEquals("hello", sb.toString());
    }

    // ---------------------------------------------------------------------------
    // safeAppend(Appendable, CharSequence, int, int)
    // ---------------------------------------------------------------------------

    @Test
    void safeAppend_range_normal() {
        StringBuilder sb = new StringBuilder();
        StringWriteUtil.safeAppend((Appendable) sb, "hello world", 6, 11);
        assertEquals("world", sb.toString());
    }

    // ---------------------------------------------------------------------------
    // safeAppend(Appendable, char)
    // ---------------------------------------------------------------------------

    @Test
    void safeAppend_char_nullOut_noOp() {
        StringWriteUtil.safeAppend((Appendable) null, 'x');
    }

    @Test
    void safeAppend_char_normal() {
        StringBuilder sb = new StringBuilder();
        StringWriteUtil.safeAppend((Appendable) sb, 'Z');
        assertEquals("Z", sb.toString());
    }

    // ---------------------------------------------------------------------------
    // getPrintedString
    // ---------------------------------------------------------------------------

    @Test
    void getPrintedString_writesContent() {
        String result = StringWriteUtil.getPrintedString(pw -> pw.print("hello"));
        assertEquals("hello", result);
    }

    @Test
    void getPrintedString_empty() {
        String result = StringWriteUtil.getPrintedString(pw -> {});
        assertEquals("", result);
    }

    @Test
    void getPrintedString_withExceptionType_noException() throws Exception {
        String result = StringWriteUtil.getPrintedString(pw -> pw.print("ok"), Exception.class);
        assertEquals("ok", result);
    }

    // ---------------------------------------------------------------------------
    // getFullOrPartialPrintedString
    // ---------------------------------------------------------------------------

    @Test
    void getFullOrPartialPrintedString_normalOutput() {
        String result = StringWriteUtil.getFullOrPartialPrintedString(pw -> pw.print("done"));
        assertEquals("done", result);
    }

    @Test
    void getFullOrPartialPrintedString_exceptionSwallowed() {
        String result = StringWriteUtil.getFullOrPartialPrintedString(pw -> {
            pw.print("partial");
            throw new RuntimeException("oops");
        });
        assertEquals("partial", result);
    }

    // ---------------------------------------------------------------------------
    // getString
    // ---------------------------------------------------------------------------

    @Test
    void getString_callback() {
        String result = StringWriteUtil.getString(sb -> sb.append("built"));
        assertEquals("built", result);
    }

    // ---------------------------------------------------------------------------
    // getPrintedResultAndTee
    // ---------------------------------------------------------------------------

    @Test
    void getPrintedResultAndTee_writesToBothOutputs() {
        StringWriter tee = new StringWriter();
        String result = StringWriteUtil.getPrintedResultAndTee(pw -> pw.print("tee"), tee);
        assertEquals("tee", result);
        assertEquals("tee", tee.toString());
    }

    // ---------------------------------------------------------------------------
    // appendCodePoints (StringBuilder)
    // ---------------------------------------------------------------------------

    @Test
    void appendCodePoints_stringBuilder_basic() {
        int[] codePoints = "abc".codePoints().toArray();
        StringBuilder sb = new StringBuilder();
        StringWriteUtil.appendCodePoints(sb, codePoints, 0, codePoints.length);
        assertEquals("abc", sb.toString());
    }

    @Test
    void appendCodePoints_stringBuilder_range() {
        int[] codePoints = "abcde".codePoints().toArray();
        StringBuilder sb = new StringBuilder();
        StringWriteUtil.appendCodePoints(sb, codePoints, 1, 4);
        assertEquals("bcd", sb.toString());
    }

    @Test
    void appendCodePoints_stringBuilder_nonBmp() {
        int[] codePoints = new int[] { 0x1F600 }; // U+1F600 GRINNING FACE
        StringBuilder sb = new StringBuilder();
        StringWriteUtil.appendCodePoints(sb, codePoints, 0, 1);
        assertEquals("😀", sb.toString()); // surrogate pair for U+1F600
    }

    // ---------------------------------------------------------------------------
    // appendTrimmed
    // ---------------------------------------------------------------------------

    @Test
    void appendTrimmed_stringBuilder_trimsWhitespace() {
        StringBuilder sb = new StringBuilder();
        StringWriteUtil.appendTrimmed(sb, "  hello  ");
        assertEquals("hello", sb.toString());
    }

    @Test
    void appendTrimmed_stringBuilder_noWhitespace() {
        StringBuilder sb = new StringBuilder();
        StringWriteUtil.appendTrimmed(sb, "world");
        assertEquals("world", sb.toString());
    }

    // ---------------------------------------------------------------------------
    // appendUtf8Bytes
    // ---------------------------------------------------------------------------

    @Test
    void appendUtf8Bytes_normal() {
        StringBuilder sb = new StringBuilder();
        byte[] bytes = "hello".getBytes(StandardCharsets.UTF_8);
        StringWriteUtil.appendUtf8Bytes(sb, bytes, 0, bytes.length);
        assertEquals("hello", sb.toString());
    }

    @Test
    void appendUtf8Bytes_partial() {
        StringBuilder sb = new StringBuilder();
        byte[] bytes = "hello".getBytes(StandardCharsets.UTF_8);
        StringWriteUtil.appendUtf8Bytes(sb, bytes, 1, 3);
        assertEquals("ell", sb.toString());
    }

    // ---------------------------------------------------------------------------
    // appendTo / appendToSafe
    // ---------------------------------------------------------------------------

    @Test
    void appendTo_appendable_appendsAndReturnsTrue() throws Exception {
        StringBuilder sb = new StringBuilder("pre");
        boolean result = StringWriteUtil.appendTo(sb, "_suf", updated -> {});
        assertTrue(result);
        assertEquals("pre_suf", sb.toString());
    }

    @Test
    void appendTo_charSequence_callsOnUpdate() throws Exception {
        Object[] captured = { null };
        boolean result = StringWriteUtil.appendTo("hello", "_world", obj -> captured[0] = obj);
        assertTrue(result);
        assertEquals("hello_world", captured[0]);
    }

    @Test
    void appendTo_unknownType_returnsFalse() throws Exception {
        boolean result = StringWriteUtil.appendTo(new Object(), "x", obj -> {});
        assertFalse(result);
    }

    @Test
    void appendToSafe_ioExceptionSwallowed() {
        Appendable broken = new Appendable() {
            public Appendable append(CharSequence s) throws IOException { throw new IOException("fail"); }
            public Appendable append(CharSequence s, int start, int end) throws IOException { throw new IOException("fail"); }
            public Appendable append(char c) throws IOException { throw new IOException("fail"); }
        };
        boolean result = StringWriteUtil.appendToSafe(broken, "x", obj -> {});
        assertFalse(result);
    }

}

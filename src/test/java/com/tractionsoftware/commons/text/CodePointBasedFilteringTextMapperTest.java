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
import java.util.function.IntFunction;
import java.util.function.IntPredicate;

import static org.junit.jupiter.api.Assertions.*;

class CodePointBasedFilteringTextMapperTest {

    // A non-BMP emoji: U+1F600 GRINNING FACE (requires surrogate pair in Java)
    private static final String EMOJI = "😀";
    // A string mixing BMP chars and non-BMP emoji
    private static final String MIXED = "a" + EMOJI + "b";

    private static final IntPredicate IS_DIGIT = cp -> cp >= '0' && cp <= '9';
    private static final IntPredicate IS_EMOJI = cp -> cp > 0xFFFF; // non-BMP

    // ---------------------------------------------------------------------------
    // removeIf
    // ---------------------------------------------------------------------------

    @Test
    void removeIf_null_returnsNull() {
        assertNull(CodePointBasedFilteringTextMapper.removeIf(null, IS_DIGIT));
    }

    @Test
    void removeIf_emptyString_returnsEmpty() {
        assertEquals("", CodePointBasedFilteringTextMapper.removeIf("", IS_DIGIT));
    }

    @Test
    void removeIf_noMatch_returnsOriginal() {
        assertEquals("abc", CodePointBasedFilteringTextMapper.removeIf("abc", IS_DIGIT));
    }

    @Test
    void removeIf_allMatch_returnsEmpty() {
        assertEquals("", CodePointBasedFilteringTextMapper.removeIf("123", IS_DIGIT));
    }

    @Test
    void removeIf_someMatch_removesMatches() {
        assertEquals("abc", CodePointBasedFilteringTextMapper.removeIf("a1b2c", IS_DIGIT));
    }

    @Test
    void removeIf_nonBmpCodePoint_removed() {
        // Remove the emoji, keep BMP chars
        String result = CodePointBasedFilteringTextMapper.removeIf(MIXED, IS_EMOJI);
        assertEquals("ab", result);
    }

    @Test
    void removeIf_nonBmpCodePoint_kept() {
        // Remove BMP chars, keep emoji
        IntPredicate isBmp = cp -> cp <= 0xFFFF;
        String result = CodePointBasedFilteringTextMapper.removeIf(MIXED, isBmp);
        assertEquals(EMOJI, result);
    }

    @Test
    void removeIf_toStringBuilder_appendsFiltered() {
        StringBuilder sb = new StringBuilder("x:");
        CodePointBasedFilteringTextMapper.removeIf("a1b2c", sb, IS_DIGIT);
        assertEquals("x:abc", sb.toString());
    }

    @Test
    void removeIf_toPrintWriter_writesFiltered() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        CodePointBasedFilteringTextMapper.removeIf("a1b2c", pw, IS_DIGIT);
        pw.flush();
        assertEquals("abc", sw.toString());
    }

    @Test
    void removeIf_fromReader_writesFiltered() throws IOException {
        StringWriter sw = new StringWriter();
        CodePointBasedFilteringTextMapper.removeIf(new StringReader("a1b2c"), sw, IS_DIGIT);
        assertEquals("abc", sw.toString());
    }

    // ---------------------------------------------------------------------------
    // retainIf
    // ---------------------------------------------------------------------------

    @Test
    void retainIf_null_returnsNull() {
        assertNull(CodePointBasedFilteringTextMapper.retainIf(null, IS_DIGIT));
    }

    @Test
    void retainIf_emptyString_returnsEmpty() {
        assertEquals("", CodePointBasedFilteringTextMapper.retainIf("", IS_DIGIT));
    }

    @Test
    void retainIf_noMatch_returnsEmpty() {
        assertEquals("", CodePointBasedFilteringTextMapper.retainIf("abc", IS_DIGIT));
    }

    @Test
    void retainIf_allMatch_returnsOriginal() {
        assertEquals("123", CodePointBasedFilteringTextMapper.retainIf("123", IS_DIGIT));
    }

    @Test
    void retainIf_someMatch_retainsMatches() {
        assertEquals("12", CodePointBasedFilteringTextMapper.retainIf("a1b2c", IS_DIGIT));
    }

    @Test
    void retainIf_nonBmpCodePoint_retained() {
        String result = CodePointBasedFilteringTextMapper.retainIf(MIXED, IS_EMOJI);
        assertEquals(EMOJI, result);
    }

    @Test
    void retainIf_nonBmpCodePoint_notRetained() {
        IntPredicate isBmp = cp -> cp <= 0xFFFF;
        String result = CodePointBasedFilteringTextMapper.retainIf(MIXED, isBmp);
        assertEquals("ab", result);
    }

    @Test
    void retainIf_toStringBuilder_appendsRetained() {
        StringBuilder sb = new StringBuilder();
        CodePointBasedFilteringTextMapper.retainIf("a1b2c", sb, IS_DIGIT);
        assertEquals("12", sb.toString());
    }

    @Test
    void retainIf_toPrintWriter_writesRetained() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        CodePointBasedFilteringTextMapper.retainIf("a1b2c", pw, IS_DIGIT);
        pw.flush();
        assertEquals("12", sw.toString());
    }

    // ---------------------------------------------------------------------------
    // replace with CodePointMapper (int -> int)
    // ---------------------------------------------------------------------------

    @Test
    void replaceCodePointMapper_null_returnsNull() {
        assertNull(CodePointBasedFilteringTextMapper.replace(null, (StringUtil.CodePointMapper) cp -> cp));
    }

    @Test
    void replaceCodePointMapper_emptyString_returnsEmpty() {
        assertEquals("", CodePointBasedFilteringTextMapper.replace("", (StringUtil.CodePointMapper) cp -> cp));
    }

    @Test
    void replaceCodePointMapper_noChange_returnsOriginal() {
        String result = CodePointBasedFilteringTextMapper.replace("hello", (StringUtil.CodePointMapper) cp -> cp);
        assertEquals("hello", result);
    }

    @Test
    void replaceCodePointMapper_uppercaseAll() {
        String result =
            CodePointBasedFilteringTextMapper.replace("hello", (StringUtil.CodePointMapper) Character::toUpperCase);
        assertEquals("HELLO", result);
    }

    @Test
    void replaceCodePointMapper_partialChange() {
        String result =
            CodePointBasedFilteringTextMapper.replace("hello", (StringUtil.CodePointMapper) cp -> cp == 'l' ? 'r' : cp);
        assertEquals("herro", result);
    }

    @Test
    void replaceCodePointMapper_nonBmpReplaced() {
        // Replace emoji with 'X'
        String result =
            CodePointBasedFilteringTextMapper.replace(MIXED, (StringUtil.CodePointMapper) cp -> cp > 0xFFFF ? 'X' : cp);
        assertEquals("aXb", result);
    }

    // ---------------------------------------------------------------------------
    // replace with IntFunction<CharSequence> (int -> CharSequence)
    // ---------------------------------------------------------------------------

    @Test
    void replaceIntFunction_null_returnsNull() {
        assertNull(CodePointBasedFilteringTextMapper.replace(null, (IntFunction<CharSequence>) _ -> null));
    }

    @Test
    void replaceIntFunction_emptyString_returnsEmpty() {
        assertEquals("", CodePointBasedFilteringTextMapper.replace("", (IntFunction<CharSequence>) _ -> null));
    }

    @Test
    void replaceIntFunction_nullMeansKeep() {
        String result = CodePointBasedFilteringTextMapper.replace("abc", (IntFunction<CharSequence>) _ -> null);
        assertEquals("abc", result);
    }

    @Test
    void replaceIntFunction_emptyMeansRemove() {
        String result = CodePointBasedFilteringTextMapper.replace(
            "a1b2c",
            (IntFunction<CharSequence>) cp -> Character.isDigit(cp) ? "" : null
        );
        assertEquals("abc", result);
    }

    @Test
    void replaceIntFunction_expansionReplacement() {
        String result = CodePointBasedFilteringTextMapper.replace(
            "a1b2",
            (IntFunction<CharSequence>) cp -> Character.isDigit(cp) ? "(" +
                                                                      (char) cp +
                                                                      ")" : null
        );
        assertEquals("a(1)b(2)", result);
    }

    @Test
    void replaceIntFunction_nonBmpExpanded() {
        String result = CodePointBasedFilteringTextMapper.replace(
            MIXED,
            (IntFunction<CharSequence>) cp -> cp >
                                              0xFFFF ? "[emoji]" : null
        );
        assertEquals("a[emoji]b", result);
    }

    @Test
    void replaceIntFunction_toStringBuilder() {
        StringBuilder sb = new StringBuilder();
        CodePointBasedFilteringTextMapper.replace(
            "a1b",
            sb,
            (IntFunction<CharSequence>) cp -> Character.isDigit(cp) ? "[" +
                                                                      (char) cp +
                                                                      "]" : null
        );
        assertEquals("a[1]b", sb.toString());
    }

    @Test
    void replaceIntFunction_toPrintWriter() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        CodePointBasedFilteringTextMapper.replace(
            "a1b",
            pw,
            (IntFunction<CharSequence>) cp -> Character.isDigit(cp) ? "[" +
                                                                      (char) cp +
                                                                      "]" : null
        );
        pw.flush();
        assertEquals("a[1]b", sw.toString());
    }

    @Test
    void replaceIntFunction_fromReader() throws IOException {
        StringWriter sw = new StringWriter();
        CodePointBasedFilteringTextMapper.replace(
            new StringReader("a1b"),
            sw,
            (IntFunction<CharSequence>) cp -> Character.isDigit(cp) ? "[" +
                                                                      (char) cp +
                                                                      "]" : null
        );
        assertEquals("a[1]b", sw.toString());
    }

    // ---------------------------------------------------------------------------
    // TextTransformer factory methods
    // ---------------------------------------------------------------------------

    @Test
    void createRemovingTransformer_removesMatchingCodePoints() {
        TextTransformer t = CodePointBasedFilteringTextMapper.createRemovingTransformer(IS_DIGIT);
        CharSequence result = t.transform("a1b2c");
        assertEquals("abc", result.toString());
    }

    @Test
    void createRetainingTransformer_retainsMatchingCodePoints() {
        TextTransformer t = CodePointBasedFilteringTextMapper.createRetainingTransformer(IS_DIGIT);
        CharSequence result = t.transform("a1b2c");
        assertEquals("12", result.toString());
    }

    @Test
    void createReplacingTransformer_codePointMapper() {
        TextTransformer t =
            CodePointBasedFilteringTextMapper.createReplacingTransformer((StringUtil.CodePointMapper) Character::toUpperCase);
        CharSequence result = t.transform("hello");
        assertEquals("HELLO", result.toString());
    }

    @Test
    void createReplacingTransformer_intFunctionMapper() {
        TextTransformer t =
            CodePointBasedFilteringTextMapper.createReplacingTransformer((IntFunction<CharSequence>) cp -> cp ==
                                                                                                           'x' ? "XX" : null);
        CharSequence result = t.transform("axb");
        assertEquals("aXXb", result.toString());
    }

    // ---------------------------------------------------------------------------
    // TextTransformer factory methods - transform(CharSequence, Appendable) overload
    // ---------------------------------------------------------------------------

    @Test
    void createRemovingTransformer_toAppendable_removesMatchingCodePoints()
        throws IOException, TextTransformationException {
        TextTransformer t = CodePointBasedFilteringTextMapper.createRemovingTransformer(IS_DIGIT);
        StringBuilder sb = new StringBuilder();
        t.transform("a1b2c", sb);
        assertEquals("abc", sb.toString());
    }

    @Test
    void createRetainingTransformer_toAppendable_retainsMatchingCodePoints()
        throws IOException, TextTransformationException {
        TextTransformer t = CodePointBasedFilteringTextMapper.createRetainingTransformer(IS_DIGIT);
        StringBuilder sb = new StringBuilder();
        t.transform("a1b2c", sb);
        assertEquals("12", sb.toString());
    }

    @Test
    void createReplacingTransformer_codePointMapper_toAppendable() throws IOException, TextTransformationException {
        TextTransformer t =
            CodePointBasedFilteringTextMapper.createReplacingTransformer((StringUtil.CodePointMapper) Character::toUpperCase);
        StringBuilder sb = new StringBuilder();
        t.transform("hello", sb);
        assertEquals("HELLO", sb.toString());
    }

    @Test
    void createReplacingTransformer_intFunctionMapper_toAppendable() throws IOException, TextTransformationException {
        TextTransformer t =
            CodePointBasedFilteringTextMapper.createReplacingTransformer((IntFunction<CharSequence>) cp -> cp ==
                                                                                                           'x' ? "XX" : null);
        StringBuilder sb = new StringBuilder();
        t.transform("axb", sb);
        assertEquals("aXXb", sb.toString());
    }

    // ---------------------------------------------------------------------------
    // TextTransformer factory methods - transform(Reader, Writer) overload
    // ---------------------------------------------------------------------------

    @Test
    void createRemovingTransformer_readerWriter_removesMatchingCodePoints()
        throws IOException, TextTransformationException {
        TextTransformer t = CodePointBasedFilteringTextMapper.createRemovingTransformer(IS_DIGIT);
        StringWriter sw = new StringWriter();
        t.transform(new StringReader("a1b2c"), sw);
        assertEquals("abc", sw.toString());
    }

    @Test
    void createRetainingTransformer_readerWriter_retainsMatchingCodePoints()
        throws IOException, TextTransformationException {
        TextTransformer t = CodePointBasedFilteringTextMapper.createRetainingTransformer(IS_DIGIT);
        StringWriter sw = new StringWriter();
        t.transform(new StringReader("a1b2c"), sw);
        assertEquals("12", sw.toString());
    }

    @Test
    void createReplacingTransformer_codePointMapper_readerWriter() throws IOException, TextTransformationException {
        TextTransformer t =
            CodePointBasedFilteringTextMapper.createReplacingTransformer((StringUtil.CodePointMapper) Character::toUpperCase);
        StringWriter sw = new StringWriter();
        t.transform(new StringReader("hello"), sw);
        assertEquals("HELLO", sw.toString());
    }

    @Test
    void createReplacingTransformer_intFunctionMapper_readerWriter() throws IOException, TextTransformationException {
        TextTransformer t =
            CodePointBasedFilteringTextMapper.createReplacingTransformer((IntFunction<CharSequence>) cp -> cp ==
                                                                                                           'x' ? "XX" : null);
        StringWriter sw = new StringWriter();
        t.transform(new StringReader("axb"), sw);
        assertEquals("aXXb", sw.toString());
    }

    // ---------------------------------------------------------------------------
    // createInstanceForGenericAppend - instanceof branches (PrintWriter / StringBuilder / generic Appendable)
    // ---------------------------------------------------------------------------

    @Test
    void removeIf_appendableStaticType_runtimeStringBuilder_usesStringBuilderBranch() {
        StringBuilder sb = new StringBuilder();
        Appendable out = sb;
        CodePointBasedFilteringTextMapper.removeIf("a1b2c", out, IS_DIGIT);
        assertEquals("abc", sb.toString());
    }

    @Test
    void removeIf_appendableStaticType_runtimePrintWriter_usesPrintWriterBranch() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Appendable out = pw;
        CodePointBasedFilteringTextMapper.removeIf("a1b2c", out, IS_DIGIT);
        pw.flush();
        assertEquals("abc", sw.toString());
    }

    @Test
    void removeIf_genericAppendable_notPrintWriterOrStringBuilder_usesGenericWriter() {
        StringBuilder backing = new StringBuilder();
        Appendable customAppendable = new Appendable() {

            @Override
            public Appendable append(CharSequence csq) {
                backing.append(csq);
                return this;
            }

            @Override
            public Appendable append(CharSequence csq, int start, int end) {
                backing.append(csq, start, end);
                return this;
            }

            @Override
            public Appendable append(char c) {
                backing.append(c);
                return this;
            }

        };
        CodePointBasedFilteringTextMapper.removeIf("a1b2c", customAppendable, IS_DIGIT);
        assertEquals("abc", backing.toString());
    }

    // ---------------------------------------------------------------------------
    // manual iteration via next() / CodePointValue - covers FilteringTextMapper.Value and CodePointValue
    // ---------------------------------------------------------------------------

    @Test
    void manualIteration_removeAndKeep() {
        CodePointBasedFilteringTextMapper<StringBuilder,String> mapper =
            CodePointBasedFilteringTextMapper.createDefaultInstance("hello");
        StringBuilder seen = new StringBuilder();
        while (mapper.hasNext()) {
            var value = mapper.next();
            seen.appendCodePoint(value.codePoint);
            if (value.codePoint == 'l') {
                value.remove();
            }
            else {
                value.keep();
            }
        }
        assertEquals("hello", seen.toString());
        assertEquals("heo", mapper.finish());
    }

    @Test
    void manualIteration_removeOrKeepIf() {
        CodePointBasedFilteringTextMapper<StringBuilder,String> mapper =
            CodePointBasedFilteringTextMapper.createDefaultInstance("hello");
        while (mapper.hasNext()) {
            var value = mapper.next();
            value.removeOrKeepIf(value.codePoint == 'l');
        }
        assertEquals("heo", mapper.finish());
    }

    @Test
    void manualIteration_replaceChar() {
        CodePointBasedFilteringTextMapper<StringBuilder,String> mapper =
            CodePointBasedFilteringTextMapper.createDefaultInstance("hello");
        while (mapper.hasNext()) {
            var value = mapper.next();
            if (value.codePoint == 'l') {
                value.replace('L');
            }
            else {
                value.keep();
            }
        }
        assertEquals("heLLo", mapper.finish());
    }

    @Test
    void manualIteration_replaceString() {
        CodePointBasedFilteringTextMapper<StringBuilder,String> mapper =
            CodePointBasedFilteringTextMapper.createDefaultInstance("hello");
        while (mapper.hasNext()) {
            var value = mapper.next();
            if (value.codePoint == 'l') {
                value.replace("LL");
            }
            else {
                value.keep();
            }
        }
        assertEquals("heLLLLo", mapper.finish());
    }

    @Test
    void manualIteration_replaceStringVarargs() {
        CodePointBasedFilteringTextMapper<StringBuilder,String> mapper =
            CodePointBasedFilteringTextMapper.createDefaultInstance("hello");
        while (mapper.hasNext()) {
            var value = mapper.next();
            if (value.codePoint == 'l') {
                value.replace("[", "L", "]");
            }
            else {
                value.keep();
            }
        }
        assertEquals("he[L][L]o", mapper.finish());
    }

    @Test
    void manualIteration_replaceWithCodePoint_nonBmp() {
        CodePointBasedFilteringTextMapper<StringBuilder,String> mapper =
            CodePointBasedFilteringTextMapper.createDefaultInstance("a-b");
        int emoji = 0x1F600;
        while (mapper.hasNext()) {
            var value = mapper.next();
            if (value.codePoint == '-') {
                value.replaceWithCodePoint(emoji);
            }
            else {
                value.keep();
            }
        }
        assertEquals("a" + new String(Character.toChars(emoji)) + "b", mapper.finish());
    }

    @Test
    void manualIteration_appendReplacement() {
        CodePointBasedFilteringTextMapper<StringBuilder,String> mapper =
            CodePointBasedFilteringTextMapper.createDefaultInstance("a-b");
        while (mapper.hasNext()) {
            var value = mapper.next();
            if (value.codePoint == '-') {
                value.appendReplacement(sb -> sb.append("<<>>"));
            }
            else {
                value.keep();
            }
        }
        assertEquals("a<<>>b", mapper.finish());
    }

    @Test
    void manualIteration_overNonBmpInput_codePointFieldReflectsFullCodePoint() {
        CodePointBasedFilteringTextMapper<StringBuilder,String> mapper =
            CodePointBasedFilteringTextMapper.createDefaultInstance(MIXED);
        StringBuilder seenCodePoints = new StringBuilder();
        while (mapper.hasNext()) {
            var value = mapper.next();
            seenCodePoints.append(value.codePoint).append(',');
            value.keep();
        }
        assertEquals("97,128512,98,", seenCodePoints.toString());
        assertEquals(MIXED, mapper.finish());
    }

    @Test
    void manualIteration_consumingTwice_throwsIllegalStateException() {
        CodePointBasedFilteringTextMapper<StringBuilder,String> mapper =
            CodePointBasedFilteringTextMapper.createDefaultInstance("a");
        var value = mapper.next();
        value.keep();
        assertThrows(IllegalStateException.class, value::remove);
    }

}

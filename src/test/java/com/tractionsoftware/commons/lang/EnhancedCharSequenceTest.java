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

package com.tractionsoftware.commons.lang;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CharSequences created by {@link EnhancedCharSequence}.
 *
 * <p>
 * For the tests below that try to use Unicode in either the text input or the set of characters that can be wrap cues,
 * the sequence uD83D uDE02 represents U+1F602 (hex 0x1F602, dec 128514). This is <a
 * href="https://en.wikipedia.org/wiki/Face_with_Tears_of_Joy_emoji">the Unicode emoji "FACE WITH TEARS OF JOY" 😂,
 * Supplementary Multilingual Plane</a>.
 *
 * @author Dave Shepperton
 */
public final class EnhancedCharSequenceTest {

    private static final String NON_BMP_CODE_POINT_EXAMPLE_STRING = "\uD83D\uDE02";

    private static final String SIMPLE_SEQUENCE_STRING = "123456789";

    private static final EnhancedCharSequence singletonBmpCodePointExample() {
        return EnhancedCharSequence.getSingletonSequence('x');
    }

    private static final EnhancedCharSequence singletonNonBmpCodePointExample() {
        return EnhancedCharSequence.getSingletonSequence(0x1F602);
    }

    private static final EnhancedCharSequence repeatingSingleBmpCodePointExample() {
        return EnhancedCharSequence.getRepeatingSequence('x', 50);
    }

    private static final EnhancedCharSequence repeatingSingleNonBmpCodePointExample() {
        return EnhancedCharSequence.getRepeatingSequence(0x1F602, 50);
    }

    private static final EnhancedCharSequence repeatingMultiCharExample() {
        return EnhancedCharSequence.getRepeatingSequence("abc", 10);
    }

    private static final EnhancedCharSequence simpleSequenceExample() {
        return EnhancedCharSequence.getInstance(SIMPLE_SEQUENCE_STRING.toCharArray());
    }

    @Test
    public void test_enhancedNullToStringIsEmptyString() {
        assertSame(StringUtils.EMPTY, EnhancedCharSequence.enhance(null).toString());
    }

    @Test
    public void test_simpleEnhanceToString1() {
        assertEquals("x", EnhancedCharSequence.enhance("x").toString());
    }

    @Test
    public void test_simpleEnhanceToString2() {
        assertEquals("xxxx", EnhancedCharSequence.enhance("xxxx").toString());
    }

    @Test
    public void test_getSingletonBmpCodePointToString() {
        assertEquals("x", singletonBmpCodePointExample().toString());
    }

    @Test
    public void test_getSingletonBmpCodePointLength() {
        assertEquals(1, singletonBmpCodePointExample().length());
    }

    @Test
    public void test_getSingletonBmpCodePointNotEmpty() {
        assertFalse(singletonBmpCodePointExample().isEmpty());
    }

    @Test
    public void test_getSingletonBmpCodePointEmptySubSequenceIsEmptyStringWrapper() {
        assertSame(StringUtils.EMPTY, singletonBmpCodePointExample().subSequence(0, 0).toString());
    }

    @Test
    public void test_getSingletonSequenceNonBmpToString() {
        assertEquals(NON_BMP_CODE_POINT_EXAMPLE_STRING, singletonNonBmpCodePointExample().toString());
    }

    @Test
    public void test_getSingletonSequenceNonBmpLength() {
        assertEquals(2, singletonNonBmpCodePointExample().length());
    }

    @Test
    public void test_getSingletonSequenceNonBmpNotEmpty() {
        assertFalse(singletonNonBmpCodePointExample().isEmpty());
    }

    @Test
    public void test_getSingletonSequenceNonBmpEmptySubSequenceIsEmptyStringWrapper() {
        assertSame(StringUtils.EMPTY, singletonNonBmpCodePointExample().subSequence(0, 0).toString());
    }

    @Test
    public void test_getRepeatingSingleBmpCodePointSequenceToString() {
        assertEquals(StringUtils.repeat('x', 50), repeatingSingleBmpCodePointExample().toString());
    }

    @Test
    public void test_getRepeatingSingleBmpCodePointSequenceLength() {
        assertEquals(50, repeatingSingleBmpCodePointExample().length());
    }

    @Test
    public void test_getRepeatingSingleBmpCodePointSequenceNotEmpty() {
        assertFalse(repeatingSingleBmpCodePointExample().isEmpty());
    }

    @Test
    public void test_getRepeatingSingleBmpCodePointSequenceEmptySubSequenceIsEmptyStringWrapper() {
        assertSame(StringUtils.EMPTY, repeatingSingleBmpCodePointExample().subSequence(0, 0).toString());
    }

    @Test
    public void test_getRepeatingSingleBmpCodePointSequenceHalfSubSequenceToString() {
        assertEquals(StringUtils.repeat('x', 25), repeatingSingleBmpCodePointExample().subSequence(0, 25).toString());
    }

    @Test
    public void test_getRepeatingSingleNonBmpCodePointToString() {
        assertEquals(StringUtils.repeat("\uD83D\uDE02", 50), repeatingSingleNonBmpCodePointExample().toString());
    }

    @Test
    public void test_getRepeatingSingleNonBmpCodePointLength() {
        assertEquals(100, repeatingSingleNonBmpCodePointExample().length());
    }

    @Test
    public void test_getRepeatingSingleNonBmpCodePointNotEmpty() {
        assertFalse(repeatingSingleNonBmpCodePointExample().isEmpty());
    }

    @Test
    public void test_getRepeatingSingleNonBmpCodePointEmptySubSequenceIsEmptyStringWrapper() {
        assertSame(StringUtils.EMPTY, repeatingSingleNonBmpCodePointExample().subSequence(0, 0).toString());
    }

    @Test
    public void test_getRepeatingSingleNonBmpCodePointHalfSubSequenceToString() {
        assertEquals(
            StringUtils.repeat("\uD83D\uDE02", 25),
            repeatingSingleNonBmpCodePointExample().subSequence(0, 50).toString()
        );
    }

    @Test
    public void test_getRepeatingSequenceToString() {
        assertEquals(StringUtils.repeat("abc", 10), repeatingMultiCharExample().toString());
    }

    @Test
    public void test_getRepeatingSequenceLength() {
        assertEquals(30, repeatingMultiCharExample().length());
    }

    @Test
    public void test_getRepeatingSequenceNotEmpty() {
        assertFalse(repeatingMultiCharExample().isEmpty());
    }

    @Test
    public void test_getRepeatingSequenceEmptySubSequenceIsEmptyStringWrapper() {
        assertSame(StringUtils.EMPTY, repeatingMultiCharExample().subSequence(0, 0).toString());
    }

    // Spans exactly the first repetition.
    @Test
    public void test_getRepeatingSequenceAlignedSubSequenceToString1() {
        assertEquals("abc", repeatingMultiCharExample().subSequence(0, 3).toString());
    }

    // Spans exactly the first and second repetitions.
    @Test
    public void test_getRepeatingSequenceAlignedSubSequenceToString2() {
        assertEquals("abcabc", repeatingMultiCharExample().subSequence(3, 9).toString());
    }

    // Spans two complete realigned regions.
    @Test
    public void test_getRepeatingSequenceRealignedSubSequenceToString1() {
        assertEquals("bcabca", repeatingMultiCharExample().subSequence(1, 7).toString());
    }

    @Test
    public void test_getRepeatingSequenceRealignedSubSequenceToString2() {
        assertEquals("cabcab", repeatingMultiCharExample().subSequence(2, 8).toString());
    }

    // Within a single repetitions.
    @Test
    public void test_getRepeatingSequenceUnalignedSubSequenceToString1() {
        assertEquals("bc", repeatingMultiCharExample().subSequence(1, 3).toString());
    }

    // Spanning two repetitions. The second is complete.
    @Test
    public void test_getRepeatingSequenceUnalignedSubSequenceToString2() {
        assertEquals("cabc", repeatingMultiCharExample().subSequence(2, 6).toString());
    }

    // Spanning two repetitions. The first is complete.
    @Test
    public void test_getRepeatingSequenceUnalignedSubSequenceToString3() {
        assertEquals("abca", repeatingMultiCharExample().subSequence(3, 7).toString());
    }

    // Spanning three repetitions. The second and third repetitions are complete.
    @Test
    public void test_getRepeatingSequenceUnalignedSubSequenceToString4() {
        assertEquals("cabcabc", repeatingMultiCharExample().subSequence(2, 9).toString());
    }

    // Spanning four repetitions. The second and third repetitions are complete.
    @Test
    public void test_getRepeatingSequenceUnalignedSubSequenceToString5() {
        assertEquals("cabcabca", repeatingMultiCharExample().subSequence(2, 10).toString());
    }

    // Spans just part of the first repetition.
    @Test
    public void test_getRepeatingSequenceShortSubSequenceToString1To2() {
        assertEquals("b", repeatingMultiCharExample().subSequence(1, 2).toString());
    }

    // Spans just part of the first repetition.
    @Test
    public void test_getRepeatingSequenceShortSubSequenceToString1To3() {
        assertEquals("bc", repeatingMultiCharExample().subSequence(1, 3).toString());
    }

    // Spans just part of the last repetition.
    @Test
    public void test_getRepeatingSequenceShortSubSequenceToString28To29() {
        assertEquals("b", repeatingMultiCharExample().subSequence(28, 29).toString());
    }

    // Spans just part of the last repetition.
    @Test
    public void test_getRepeatingSequenceShortSubSequenceToString28To30() {
        assertEquals("bc", repeatingMultiCharExample().subSequence(28, 30).toString());
    }

    // Spans just part of the last repetition.
    @Test
    public void test_getRepeatingSequenceShortSubSequenceToString27To28() {
        assertEquals("a", repeatingMultiCharExample().subSequence(27, 28).toString());
    }

    // Spans just part of the last repetition.
    @Test
    public void test_getRepeatingSequenceShortSubSequenceToString27To29() {
        assertEquals("ab", repeatingMultiCharExample().subSequence(27, 29).toString());
    }

    // Spans exactly one repetition.
    @Test
    public void test_getRepeatingSequenceShortSubSequenceToString15To18() {
        assertEquals("abc", repeatingMultiCharExample().subSequence(15, 18).toString());
    }

    // Spans a couple of repetitions, with a head and tail.
    @Test
    public void test_getRepeatingSequenceShortSubSequenceToString11To20() {
        assertEquals("cabcabcab", repeatingMultiCharExample().subSequence(11, 20).toString());
    }

    @Test
    public void test_singletonBmpRepeatEqualsRepeatToString1() {
        assertEquals(
            singletonBmpCodePointExample().repeated(50).toString(),
            repeatingSingleBmpCodePointExample().toString()
        );
    }

    @Test
    public void test_getSimpleSequenceToString() {
        assertEquals(SIMPLE_SEQUENCE_STRING, simpleSequenceExample().toString());
    }

    @Test
    public void test_getSimpleSequenceNotEmpty() {
        assertFalse(simpleSequenceExample().isEmpty());
    }

    @Test
    public void test_getSimpleSequenceLength() {
        assertEquals(SIMPLE_SEQUENCE_STRING.length(), simpleSequenceExample().length());
    }

    @Test
    public void test_getSimpleSequenceEmptySubSequenceEmpty() {
        assertTrue(simpleSequenceExample().subSequence(0, 0).isEmpty());
    }

    @Test
    public void test_getSimpleSequenceRepeated10() {
        assertEquals(StringUtils.repeat(SIMPLE_SEQUENCE_STRING, 10), simpleSequenceExample().repeated(10).toString());
    }

    @Test
    public void test_getSimpleSequenceRepeated10And2() {
        assertEquals(
            StringUtils.repeat(SIMPLE_SEQUENCE_STRING, 20),
            simpleSequenceExample().repeated(10).repeated(2).toString()
        );
    }

    @Test
    public void test_getRepeated10() {
        assertEquals(
            StringUtils.repeat(SIMPLE_SEQUENCE_STRING, 10),
            EnhancedCharSequence.getRepeatingSequence(SIMPLE_SEQUENCE_STRING, 10).toString()
        );
    }

    @Test
    public void test_getRepeated10And2() {
        assertEquals(
            StringUtils.repeat(SIMPLE_SEQUENCE_STRING, 20),
            EnhancedCharSequence.getRepeatingSequence(SIMPLE_SEQUENCE_STRING, 10).repeated(2).toString()
        );
    }

    @Test
    public void test_getRepeated10And2SubSequence5To18ToString() {
        assertEquals(
            SIMPLE_SEQUENCE_STRING.substring(5) + SIMPLE_SEQUENCE_STRING,
            EnhancedCharSequence.getRepeatingSequence(SIMPLE_SEQUENCE_STRING, 10)
                .repeated(2)
                .subSequence(5, 18)
                .toString()
        );
    }

    @Test
    public void test_getRepeated10And2SubSequence5To19ToString() {
        assertEquals(
            SIMPLE_SEQUENCE_STRING.substring(5) + SIMPLE_SEQUENCE_STRING + SIMPLE_SEQUENCE_STRING.charAt(0),
            EnhancedCharSequence.getRepeatingSequence(SIMPLE_SEQUENCE_STRING, 10)
                .repeated(2)
                .subSequence(5, 19)
                .toString()
        );
    }

    @Test
    public void test_getSimpleSequenceEmptySubSequencIsEmptyStringWrapper() {
        assertSame(StringUtils.EMPTY, simpleSequenceExample().subSequence(0, 0).toString());
    }

    @Test
    public void test_getLeftRotatedNone() {
        assertEquals("abcdef", EnhancedCharSequence.getLeftRotatedSequence("abcdef", 0).toString());
    }

    @Test
    public void test_getLeftRotated1() {
        assertEquals("bcdefa", EnhancedCharSequence.getLeftRotatedSequence("abcdef", 1).toString());
    }

    @Test
    public void test_getLeftRotated2() {
        assertEquals("cdefab", EnhancedCharSequence.getLeftRotatedSequence("abcdef", 2).toString());
    }

    @Test
    public void test_getLeftRotated3() {
        assertEquals("defabc", EnhancedCharSequence.getLeftRotatedSequence("abcdef", 3).toString());
    }

    @Test
    public void test_getRightRotatedNone() {
        assertEquals("abcdef", EnhancedCharSequence.getRightRotatedSequence("abcdef", 0).toString());
    }

    @Test
    public void test_getRightRotated1() {
        assertEquals("fabcde", EnhancedCharSequence.getRightRotatedSequence("abcdef", 1).toString());
    }

    @Test
    public void test_getRightRotated2() {
        assertEquals("efabcd", EnhancedCharSequence.getRightRotatedSequence("abcdef", 2).toString());
    }

    @Test
    public void test_getRightRotated3() {
        assertEquals("defabc", EnhancedCharSequence.getRightRotatedSequence("abcdef", 3).toString());
    }

    @Test
    public void test_enhanceStringOps1() {
        assertEquals(
            "abcabcdefdef",
            EnhancedCharSequence.getRepeatingSequence("abc", 2).withRepeatingAppended("def", 2).toString()
        );
    }

    @Test
    public void test_enhanceStringOps2() {
        assertEquals(
            "defdefabcabc",
            EnhancedCharSequence.getRepeatingSequence("abc", 2).withRepeatingPrepended("def", 2).toString()
        );
    }

    @Test
    public void test_enhanceStringOps3a() {
        assertEquals(
            "f][abcdefabcde",
            EnhancedCharSequence.getRepeatingSequence("abcdef", 2)
                .withPrepended(EnhancedCharSequence.getInstance(new char[] { '[' }))
                .withAppended(EnhancedCharSequence.getInstance(new char[] { ']' }))
                .getRightRotated(2)
                .toString()
            // [abcdefabcdef] -> f][abcdefabcde
        );
    }

    @Test
    public void test_enhanceStringOps3b() {
        assertEquals(
            "[abcdefabcdef]",
            EnhancedCharSequence.getRepeatingSequence("abcdef", 2)
                .withPrepended(EnhancedCharSequence.getInstance(new char[] { '[' }))
                .withAppended(EnhancedCharSequence.getInstance(new char[] { ']' }))
                .getRightRotated(2)
                .getLeftRotated(2)
                .toString()
            // [abcdefabcdef] -> f][abcdefabcde -> [abcdefabcdef]
        );
    }

    @Test
    public void test_enhanceStringOps3c() {
        assertEquals(
            "abc",
            EnhancedCharSequence.getRepeatingSequence("abcdef", 2)
                .withPrepended(EnhancedCharSequence.getInstance(new char[] { '[' }))
                .withAppended(EnhancedCharSequence.getInstance(new char[] { ']' }))
                .getRightRotated(2)
                .getLeftRotated(2)
                .subSequence(1, 4)
                .toString()
            // [abcdefabcdef] -> f][abcdefabcde -> [abcdefabcdef] -> abc (chars at 1, 2 and 3)
        );
    }

    @Test
    public void test_enhanceStringOps3d() {
        assertEquals(
            "abc",
            EnhancedCharSequence.enhance("abcdef")
                .repeated(2)
                .withPrepended(EnhancedCharSequence.getInstance(new char[] { '[' }))
                .withAppended(EnhancedCharSequence.getInstance(new char[] { ']' }))
                .getRightRotated(2)
                .getLeftRotated(2)
                .subSequence(1, 4)
                .toString()
            // [abcdefabcdef] -> f][abcdefabcde -> [abcdefabcdef] -> abc (chars at 1, 2 and 3)
        );
    }

    @Test
    public void test_enhanceStringOps4a() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.enhance("bb").withPrepended("aa").withAppended("cc");
        assertEquals("aabbcc", enhanced.toString());
    }

    @Test
    public void test_enhanceStringOps7() {
        assertEquals(
            "8246",
            EnhancedCharSequence.enhance("2468").repeated(5).getLeftRotated(3).subSequence(0, 4).toString()
        );
    }

    @Test
    public void test_enhanceStringOps8() {
        assertEquals(
            "DEF0",
            EnhancedCharSequence.enhance("0123456789ABCDEF")
                .repeated(5) // -> 0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF
                .getLeftRotated(3) // -> 3456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF012
                .getRightRotated(6) // -> DEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABC
                .subSequence(0, 4) // -> DEF0
                .toString()
        );
    }

    @Test
    public void test_enhanceStringOps9a() {
        assertEquals(
            "3456789ABCDEF012",
            EnhancedCharSequence.enhance("0123456789ABCDEF")
                .repeated(5) // -> 0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF
                .getLeftRotated(3) // -> 3456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF012
                .subSequence(0, 16) // -> 3456789ABCDEF012
                .toString()
        );
    }

    @Test
    public void test_enhanceStringOps9b() {
        assertEquals(
            "3456789ABCDEF012",
            EnhancedCharSequence.getInstance("0123456789ABCDEF".toCharArray())
                .repeated(5) // -> 0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF
                .getLeftRotated(3) // -> 3456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF012
                .subSequence(0, 16) // -> 3456789ABCDEF012
                .toString()
        );
    }

    @Test
    public void test_enhanceStringOps9c() {
        assertEquals(
            "3456789ABCDEF012",
            EnhancedCharSequence.getInstance("xx0123456789ABCDEFxx".toCharArray(), 2, 16)
                .repeated(5) // -> 0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF
                .getLeftRotated(3) // -> 3456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF012
                .subSequence(0, 16) // -> 3456789ABCDEF012
                .toString()
        );
    }

    // Get back out the same EnhancedCharSequence object that we started with when we apply transformations that are
    // then effectively reversed.
    @Test
    public void test_enhanceStringOps10a() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.enhance("0123456789ABCDEF");
        assertSame(
            enhanced,
            enhanced.repeated(5) // -> 0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF
                .getLeftRotated(16) // -> 3456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF012
                .getRightRotated(16) // -> 3456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF012
                .subSequence(0, 16) // -> 3456789ABCDEF012
        );
    }

    @Test
    public void test_enhanceStringOps10b() {
        EnhancedCharSequence enhanced =
            EnhancedCharSequence.enhance(EnhancedCharSequence.enhance("xx0123456789ABCDEFxx").subSequence(2, 18));
        assertSame(
            enhanced,
            enhanced.repeated(5) // -> 0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF
                .getLeftRotated(16) // -> 3456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF012
                .getRightRotated(16) // -> 3456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF012
                .subSequence(0, 16) // -> 3456789ABCDEF012
        );
    }

    @Test
    public void test_enhanceStringOps10c() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.getInstance("0123456789ABCDEF".toCharArray());
        assertSame(
            enhanced,
            enhanced.repeated(5) // -> 0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF
                .getLeftRotated(16) // -> 3456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF012
                .getRightRotated(16) // -> 3456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF012
                .subSequence(0, 16) // -> 3456789ABCDEF012
        );
    }

    @Test
    public void test_enhanceStringOps10d() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.getInstance("xx0123456789ABCDEFxx".toCharArray(), 2, 16);
        assertSame(
            enhanced,
            enhanced.repeated(5) // -> 0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF
                .getLeftRotated(16) // -> 3456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF012
                .getRightRotated(16) // -> 3456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF012
                .subSequence(0, 16) // -> 3456789ABCDEF012
        );
    }

    @Test
    public void test_enhanceStringOps11() {
        assertEquals(
            EnhancedCharSequence.getLeftRotatedSequence("ZABCXY", 4).toString(),
            EnhancedCharSequence.getRightRotatedSequence("ZABCXY", 2).toString()
        );
    }

    @Test
    public void test_enhanceStringOps12a() {
        assertEquals(
            EnhancedCharSequence.getLeftRotatedSequence("ZABCDXY", 4).toString(),
            EnhancedCharSequence.getRightRotatedSequence("ZABCDXY", 3).toString()
        );
    }

    @Test
    public void test_enhanceStringOps12b() {
        assertEquals(
            EnhancedCharSequence.getInstance("ZABCDXY".toCharArray()).getLeftRotated(4).toString(),
            EnhancedCharSequence.getInstance("ZABCDXY".toCharArray()).getRightRotated(3).toString()
        );
    }

    @Test
    public void test_enhanceStringOps13a() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.enhance("ABCDXYZ");
        assertSame(enhanced, enhanced.getLeftRotated(4).getRightRotated(4));
    }

    @Test
    public void test_enhanceStringOps13b() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.enhance("ABCDXYZ");
        assertSame(enhanced, enhanced.getLeftRotated(4).getLeftRotated(3));
    }

    @Test
    public void test_enhanceStringOps13c() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.getInstance("ABCDXYZ".toCharArray());
        assertSame(enhanced, enhanced.getLeftRotated(4).getRightRotated(4));
    }

    @Test
    public void test_enhanceStringOps13d() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.getInstance("ABCDXYZ".toCharArray());
        assertSame(enhanced, enhanced.getLeftRotated(4).getLeftRotated(3));
    }

    @Test
    public void test_enhanceStringOps14a() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.getInstance("a,xyz,c,d".toCharArray(), 2, 3);
        assertEquals("xyz", enhanced.trim().toString());
    }

    @Test
    public void test_enhanceStringOps14b() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.getInstance("a,   xyz  ,c,d".toCharArray(), 2, 8);
        assertEquals("xyz", enhanced.trim().toString());
    }

    @Test
    public void test_enhanceStringOps14c() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.getInstance("a,   xyz  ,c,d".toCharArray(), 2, 5);
        assertEquals("xy", enhanced.trim().toString());
    }

    @Test
    public void test_enhanceStringOps15a() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.getInstance("DEF".toCharArray())
            .withPrepended(EnhancedCharSequence.getInstance("ABC".toCharArray()));
        assertEquals("ABCDEF", enhanced.toString());
    }

    @Test
    public void test_enhanceStringOps15b() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.getInstance("ABC".toCharArray())
            .withAppended(EnhancedCharSequence.getInstance("DEF".toCharArray()));
        assertEquals("ABCDEF", enhanced.toString());
    }

    @Test
    public void test_enhanceStringOps15c() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.getInstance("ABC".toCharArray())
            .withPrepended(EnhancedCharSequence.getInstance("DEF".toCharArray()))
            .getLeftRotated(3);
        assertEquals("ABCDEF", enhanced.toString());
    }

    @Test
    public void test_enhanceStringOps15d() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.getInstance("ABC".toCharArray())
            .withPrepended(EnhancedCharSequence.getInstance("DEF".toCharArray()))
            .getLeftRotated(2);
        assertEquals("FABCDE", enhanced.toString());
    }

    @Test
    public void test_enhanceStringOps15e() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.getInstance("ABC".toCharArray())
            .withPrepended(EnhancedCharSequence.getInstance("DEF".toCharArray()))
            .getLeftRotated(2)
            .getLeftRotated(1);
        assertEquals("ABCDEF", enhanced.toString());
    }

    @Test
    public void test_enhanceStringOps16a() {
        EnhancedCharSequence enhanced =
            EnhancedCharSequence.getInstance(" ABC".toCharArray()).repeated(3).withAppended(" XYZ   ").trim();
        assertEquals("ABC ABC ABC XYZ", enhanced.toString());
    }

    @Test
    public void test_enhanceStringOps16b() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.getInstance("    ".toCharArray());
        assertSame(StringUtils.EMPTY, enhanced.trim().toString());
    }

    @Test
    public void test_enhanceStringOps16c() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.getInstance("xxxx".toCharArray());
        assertEquals("xxxx", enhanced.trim().toString());
    }

    @Test
    public void test_enhanceStringOps16d() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.getInstance("xxxx".toCharArray(), 1, 3);
        assertSame(enhanced, enhanced.trim());
    }

    @Test
    public void test_enhanceStringOps16e() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.getInstance("xxxx".toCharArray(), 1, 3);
        assertSame(enhanced, enhanced.trim());
    }

    @Test
    public void test_enhanceStringOps16f() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.getInstance("x xxx ".toCharArray(), 1, 5);
        assertEquals("xxx", enhanced.trim().toString());
    }

    @Test
    public void test_enhanceStringOps16g() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.getInstance("xxx".toCharArray());
        assertEquals("xxx", enhanced.withPrepended("   ").withAppended("   ").trim().toString());
    }

    @Test
    public void test_enhanceStringOps16h() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.getInstance(" xxx ".toCharArray()).repeated(3);
        assertEquals("xxx  xxx  xxx", enhanced.trim().toString());
    }

    @Test
    public void test_enhanceStringOps16i() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.getInstance(" xxx ".toCharArray()).repeated(4);
        assertEquals("xxx  xxx  xxx  xxx", enhanced.trim().toString());
    }

    @Test
    public void test_enhanceStringOps16j() {
        EnhancedCharSequence enhanced =
            EnhancedCharSequence.getInstance("    ".toCharArray()).repeated(3).withAppended(" ").trim();
        assertSame(StringUtils.EMPTY, enhanced.toString());
    }

    @Test
    public void test_enhanceStringOps17a() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.enhance(" ");
        assertSame(StringUtils.EMPTY, enhanced.trim().toString());
    }

    @Test
    public void test_enhanceStringOps17b() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.enhance(" abc ");
        assertEquals("abc", enhanced.trim().toString());
    }

    @Test
    public void test_enhanceStringOps18a() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.enhance("def   abc");
        assertEquals("abcdef", enhanced.getLeftRotated(3).trim().toString());
    }

    @Test
    public void test_enhanceStringOps18b() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.enhance("def   abc");
        assertEquals("abcdef", enhanced.getRightRotated(3).trim().toString());
    }

    @Test
    public void test_enhanceStringOps18c() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.enhance("def   abc");
        assertEquals("f   abcde", enhanced.getLeftRotated(2).trim().toString());
    }

    @Test
    public void test_enhanceStringOps18d() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.enhance("def   abc");
        assertEquals("bcdef   a", enhanced.getRightRotated(2).trim().toString());
    }

    @Test
    public void test_enhanceStringOps18e() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.getInstance("def   abc".toCharArray());
        assertEquals("abcdef", enhanced.getLeftRotated(3).trim().toString());
    }

    @Test
    public void test_enhanceStringOps18f() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.getInstance("def   abc".toCharArray());
        assertEquals("abcdef", enhanced.getRightRotated(3).trim().toString());
    }

    @Test
    public void test_enhanceStringOps18g() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.getInstance("def   abc".toCharArray());
        assertEquals("f   abcde", enhanced.getLeftRotated(2).trim().toString());
    }

    @Test
    public void test_enhanceStringOps18h() {
        EnhancedCharSequence enhanced = EnhancedCharSequence.getInstance("def   abc".toCharArray());
        assertEquals("bcdef   a", enhanced.getRightRotated(2).trim().toString());
    }

    // -------------------------------------------------------------------------
    // matches
    // -------------------------------------------------------------------------

    @Test
    public void matches_sameContent_returnsTrue() {
        EnhancedCharSequence seq = EnhancedCharSequence.enhance("hello");
        assertTrue(seq.matches("hello"));
    }

    @Test
    public void matches_differentContent_returnsFalse() {
        EnhancedCharSequence seq = EnhancedCharSequence.enhance("hello");
        assertFalse(seq.matches("world"));
    }

    @Test
    public void matches_differentLength_returnsFalse() {
        EnhancedCharSequence seq = EnhancedCharSequence.enhance("hello");
        assertFalse(seq.matches("hell"));
    }

    @Test
    public void matches_null_emptySeqReturnsTrue() {
        EnhancedCharSequence seq = EnhancedCharSequence.getInstance(new char[0]);
        assertTrue(seq.matches(null));
    }

    @Test
    public void matches_null_nonEmptySeqReturnsFalse() {
        EnhancedCharSequence seq = EnhancedCharSequence.enhance("hello");
        assertFalse(seq.matches(null));
    }

    // -------------------------------------------------------------------------
    // isBlank
    // -------------------------------------------------------------------------

    @Test
    public void isBlank_emptyString_returnsTrue() {
        EnhancedCharSequence seq = EnhancedCharSequence.getInstance(new char[0]);
        assertTrue(seq.isBlank());
    }

    @Test
    public void isBlank_whitespaceOnly_returnsTrue() {
        EnhancedCharSequence seq = EnhancedCharSequence.getInstance("   ".toCharArray());
        assertTrue(seq.isBlank());
    }

    @Test
    public void isBlank_nonBlank_returnsFalse() {
        EnhancedCharSequence seq = EnhancedCharSequence.getInstance("  a  ".toCharArray());
        assertFalse(seq.isBlank());
    }

    // -------------------------------------------------------------------------
    // contains
    // -------------------------------------------------------------------------

    @Test
    public void contains_null_returnsFalse() {
        EnhancedCharSequence seq = EnhancedCharSequence.enhance("hello");
        assertFalse(seq.contains(null));
    }

    @Test
    public void contains_emptySequence_returnsTrue() {
        EnhancedCharSequence seq = EnhancedCharSequence.enhance("hello");
        assertTrue(seq.contains(""));
    }

    @Test
    public void contains_presentSubstring_returnsTrue() {
        EnhancedCharSequence seq = EnhancedCharSequence.enhance("hello world");
        assertTrue(seq.contains("world"));
    }

    @Test
    public void contains_absentSubstring_returnsFalse() {
        EnhancedCharSequence seq = EnhancedCharSequence.enhance("hello world");
        assertFalse(seq.contains("xyz"));
    }

    @Test
    public void contains_entireString_returnsTrue() {
        EnhancedCharSequence seq = EnhancedCharSequence.enhance("hello");
        assertTrue(seq.contains("hello"));
    }

}

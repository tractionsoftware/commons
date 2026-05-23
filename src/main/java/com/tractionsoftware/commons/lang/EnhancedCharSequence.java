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

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.tractionsoftware.commons.util.CollectionsUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.Strings;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A special kind of {@link CharSequence} which is immutable and offers an augmented API. The objects returned strive to
 * be as "lazy" as possible, attempting to use available information for efficiency and to avoid instantiating new and
 * possibly expensive objects or performing unnecessary copy operations.
 *
 * @author Dave Shepperton
 */
@Beta
public abstract class EnhancedCharSequence implements CharSequence {

    private static final boolean isBlankImpl(CharSequence sequence) {
        if (sequence instanceof String str) {
            return str.isBlank();
        }
        if (sequence instanceof EnhancedCharSequence enhanced) {
            return enhanced.isBlank();
        }
        return isBlankImpl(sequence, 0, sequence.length());
    }

    private static final boolean isBlankImpl(CharSequence sequence, int start, int stop) {
        for (int i = start; i < stop; i++) {
            if (!Character.isWhitespace(sequence.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static final boolean isBlankImpl(char[] data, int start, int stop) {
        for (int i = start; i < stop; i++) {
            if (!Character.isWhitespace(data[i])) {
                return false;
            }
        }
        return true;
    }

    private static final int firstNonWhitespaceIndex(CharSequence sequence, int start, int stop) {
        for (int i = start; i < stop; i++) {
            if (!Character.isWhitespace(sequence.charAt(i))) {
                return i;
            }
        }
        return stop;
    }

    private static final int lastNonWhitespaceIndex(CharSequence sequence, int start, int stop) {
        for (int i = stop - 1; i >= start; i--) {
            if (!Character.isWhitespace(sequence.charAt(i))) {
                return i;
            }
        }
        return start - 1;
    }

    private static final int firstNonWhitespaceIndex(char[] data, int start, int stop) {
        for (int i = start; i < stop; i++) {
            if (!Character.isWhitespace(data[i])) {
                return i;
            }
        }
        return stop;
    }

    private static final int lastNonWhitespaceIndex(char[] data, int start, int stop) {
        for (int i = stop - 1; i >= start; i--) {
            if (!Character.isWhitespace(data[i])) {
                return i;
            }
        }
        return start - 1;
    }

    private static final class WrappedSequence extends EnhancedCharSequence {

        static final WrappedSequence EMPTY = new WrappedSequence(StringUtils.EMPTY);

        private final CharSequence wrapped;

        private WrappedSequence(CharSequence wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public final int length() {
            return wrapped.length();
        }

        @Override
        public final char charAt(int index) {
            return wrapped.charAt(index);
        }

        @Override
        public final boolean isEmpty() {
            return wrapped.isEmpty();
        }

        @Nonnull
        @Override
        public final CharSequence subSequence(int start, int end) {
            if (start == 0 && end == length()) {
                return this;
            }
            return wrapped.subSequence(start, end);
        }

        @Nonnull
        @Override
        public final String toString() {
            return wrapped.toString();
        }

        @Override
        public final EnhancedCharSequence trim() {
            String trimmed = wrapped.toString().trim();
            if (wrapped.equals(trimmed)) {
                return this;
            }
            return EnhancedCharSequence.enhance(trimmed);
        }

        @Nonnull
        @Override
        public final IntStream chars() {
            return wrapped.chars();
        }

        @Nonnull
        @Override
        public final IntStream codePoints() {
            return wrapped.codePoints();
        }

        @Override
        protected final boolean matchesImpl(CharSequence otherSameLength) {
            if (otherSameLength instanceof WrappedSequence otherWrapped &&
                wrapped instanceof String s1 &&
                otherWrapped.wrapped instanceof String s2) {
                return s1.equals(s2);
            }
            return super.matchesImpl(otherSameLength);
        }

    }

    private static abstract class LazyEnhancedCharSequence extends EnhancedCharSequence {

        protected final int length;

        LazyEnhancedCharSequence(int length) {
            this.length = length;
        }

        @Override
        public final int length() {
            return length;
        }

        @Override
        public final char charAt(int index) {
            if (index < 0) {
                throw new IndexOutOfBoundsException(0);
            }
            if (index > length) {
                throw new IndexOutOfBoundsException(index + " > " + length);
            }
            return charAtImpl(index);
        }

        @Override
        public final boolean isEmpty() {
            if (length == 0) {
                return true;
            }
            return false;
        }

        @Nonnull
        @Override
        public final CharSequence subSequence(int start, int end) {
            if (start < 0) {
                throw new IndexOutOfBoundsException(start + " < 0");
            }
            if (start > end) {
                throw new IndexOutOfBoundsException(start + " > " + end);
            }
            if (end > length) {
                throw new IndexOutOfBoundsException(end + " > " + length);
            }
            if (start == end) {
                return WrappedSequence.EMPTY;
            }
            if (start == 0 && end == length) {
                return this;
            }
            return differentNonEmptySubSequence(start, end);
        }

        /**
         * Implements {@link #charAt(int)} only after is it known that the requested index is in bounds.
         *
         * @param index
         *     the index of the {@code char} value to be returned.
         * @return the specified {@code char} value.
         */
        protected abstract char charAtImpl(int index);

        /**
         * Implements {@link #subSequence(int, int)} only after it is known that the requested start and end indexes are
         * in bounds, the interval is not empty (start != end), and the interval is not the same as this instance's
         * interval already.
         *
         * @param start
         *     the start index, inclusive.
         * @param end
         *     the end index, exclusive.
         * @return a different {@code CharSequence} from this instance which is a subsequence of this sequence starting
         *     and ending at the requested indices.
         */
        protected abstract CharSequence differentNonEmptySubSequence(int start, int end);

    }

    private static final class CharArrayRangeSequence extends LazyEnhancedCharSequence {

        private final char[] data;

        private final int offset;

        private CharArrayRangeSequence(char[] data) {
            this(data, 0, data.length);
        }

        private CharArrayRangeSequence(char[] data, int offset, int length) {
            super(length);
            this.data = data;
            this.offset = offset;
        }

        @Override
        public final boolean isBlank() {
            return isBlankImpl(data, offset, stop());
        }

        @Override
        public final EnhancedCharSequence trim() {

            int originalEndExclusive = stop();
            int startInclusive = EnhancedCharSequence.firstNonWhitespaceIndex(data, offset, originalEndExclusive);

            if (startInclusive == originalEndExclusive) {
                return WrappedSequence.EMPTY;
            }

            int endInclusive = EnhancedCharSequence.lastNonWhitespaceIndex(
                data, startInclusive + 1, originalEndExclusive
            );
            int endExclusive = endInclusive + 1;
            if (startInclusive == offset && endExclusive == originalEndExclusive) {
                return this;
            }

            return new CharArrayRangeSequence(data, startInclusive, endExclusive - startInclusive);

        }

        @Nonnull
        @Override
        public final String toString() {
            return String.valueOf(data, offset, length);
        }

        @Override
        protected final boolean matchesImpl(CharSequence otherSameLength) {
            if (otherSameLength instanceof CharArrayRangeSequence otherRange) {
                return Arrays.equals(
                    data, offset, stop(),
                    otherRange.data, otherRange.offset, otherRange.stop()
                );
            }
            return super.matchesImpl(otherSameLength);
        }

        @Override
        protected final char charAtImpl(int index) {
            return data[index + offset];
        }

        @Override
        protected final CharSequence differentNonEmptySubSequence(int start, int end) {
            int newLength = end - start;
            if (newLength == 1) {
                return new SingleBmpCharSequence(data[start + offset]);
            }
            return new CharArrayRangeSequence(data, start + offset, newLength);
        }

        private final int stop() {
            return offset + length;
        }

    }

    private static abstract class LazySingleBmpCharSequence extends LazyEnhancedCharSequence {

        protected final char c;

        LazySingleBmpCharSequence(char c, int length) {
            super(length);
            this.c = c;
        }

        @Override
        public final boolean isBlank() {
            if (Character.isWhitespace(c)) {
                return true;
            }
            return false;
        }

        @Override
        protected final char charAtImpl(int index) {
            return c;
        }

        @Override
        protected EnhancedCharSequence prependToImpl(CharSequence other) {
            if (other instanceof LazySingleBmpCharSequence otherSingle) {
                if (c == otherSingle.c) {
                    return new RepeatingBmpCharSequence(c, length + otherSingle.length);
                }
                return LazyConcatenatedCharSequence.getInstance(other, this);
            }
            return super.prependToImpl(other);
        }

        @Override
        protected EnhancedCharSequence appendToImpl(CharSequence other) {
            if (other instanceof LazySingleBmpCharSequence otherSingle) {
                if (c == otherSingle.c) {
                    return new RepeatingBmpCharSequence(c, length + otherSingle.length);
                }
                return LazyConcatenatedCharSequence.getInstance(other, this);
            }
            return super.appendToImpl(other);
        }

        @Override
        public final EnhancedCharSequence trim() {
            if (Character.isWhitespace(c)) {
                return WrappedSequence.EMPTY;
            }
            return this;
        }

    }

    private static final class SingleBmpCharSequence extends LazySingleBmpCharSequence {

        private SingleBmpCharSequence(char c) {
            super(c, 1);
        }

        @Nonnull
        @Override
        public final String toString() {
            return String.valueOf(c);
        }

        @Override
        protected final boolean matchesImpl(CharSequence otherSameLength) {
            if (otherSameLength.charAt(0) == c) {
                return true;
            }
            return false;
        }

        @Override
        protected final EnhancedCharSequence differentNonEmptySubSequence(int start, int end) {
            throw new IllegalStateException();
        }

        @Override
        protected final EnhancedCharSequence prependToImpl(CharSequence other) {
            if (other instanceof SingleBmpCharSequence otherSingle && c != otherSingle.c) {
                return new CharArrayRangeSequence(new char[] { otherSingle.c, c });
            }
            return super.prependToImpl(other);
        }

        @Override
        protected final EnhancedCharSequence appendToImpl(CharSequence other) {
            if (other instanceof SingleBmpCharSequence otherSingle && c != otherSingle.c) {
                return new CharArrayRangeSequence(new char[] { c, otherSingle.c });
            }
            return super.appendToImpl(other);
        }

        @Override
        protected final EnhancedCharSequence repeatedImpl(int repetitions) {
            return new RepeatingBmpCharSequence(c, repetitions);
        }

    }

    private static final class RepeatingBmpCharSequence extends LazySingleBmpCharSequence {

        private RepeatingBmpCharSequence(char c, int repetitions) {
            super(c, repetitions);
        }

        @Nonnull
        @Override
        public final String toString() {
            StringBuilder buff = new StringBuilder(length);
            buff.repeat(c, length);
            return buff.toString();
        }

        @Override
        protected final boolean matchesImpl(CharSequence otherSameLength) {
            if (otherSameLength instanceof RepeatingBmpCharSequence otherSingletonRepeating) {
                if (c == otherSingletonRepeating.c) {
                    return true;
                }
                return false;
            }
            if (otherSameLength.chars().allMatch(codePoint -> codePoint == c)) {
                return true;
            }
            return false;
        }

        @Override
        protected final EnhancedCharSequence repeatedImpl(int repetitions) {
            return new RepeatingBmpCharSequence(c, this.length * repetitions);
        }

        @Override
        protected final CharSequence differentNonEmptySubSequence(int start, int end) {
            int newLength = end - start;
            if (newLength == 1) {
                return new SingleBmpCharSequence(c);
            }
            return new RepeatingBmpCharSequence(c, newLength);
        }

    }

    private static final class RepeatingCharSequence extends LazyEnhancedCharSequence {

        static final RepeatingCharSequence getInstance(CharSequence baseSequence, int repetitions) {
            if (repetitions < 2) {
                throw new IllegalArgumentException(repetitions + " < 2");
            }
            int sequenceLength = baseSequence.length();
            return new RepeatingCharSequence(baseSequence, sequenceLength, sequenceLength * repetitions, repetitions);
        }

        private final CharSequence baseSequence;

        private final int baseLength;

        private final int repetitions;

        private RepeatingCharSequence(CharSequence baseSequence, int baseLength, int fullLength, int repetitions) {
            super(fullLength);
            this.baseSequence = baseSequence;
            this.baseLength = baseLength;
            this.repetitions = repetitions;
        }

        @Nonnull
        @Override
        public final String toString() {
            StringBuilder buff = new StringBuilder(length);
            buff.repeat(baseSequence, repetitions);
            return buff.toString();
        }

        @Override
        public final boolean isBlank() {
            return isBlankImpl(baseSequence);
        }

        @Override
        public final EnhancedCharSequence trim() {

            int startInclusive = EnhancedCharSequence.firstNonWhitespaceIndex(baseSequence, 0, baseLength);
            if (startInclusive == baseLength) {
                return WrappedSequence.EMPTY;
            }

            int endInclusive = EnhancedCharSequence.lastNonWhitespaceIndex(
                baseSequence, startInclusive + 1, baseLength
            );

            int endExclusive = endInclusive + 1;

            if (startInclusive == 0) {
                if (endExclusive == baseLength) {
                    return this;
                }
                CharSequence first;
                if (repetitions == 2) {
                    first = baseSequence;
                }
                else {
                    first = new RepeatingCharSequence(baseSequence, baseLength, length, repetitions - 1);
                }
                CharSequence last = baseSequence.subSequence(0, endInclusive);
                return LazyConcatenatedCharSequence.getInstance(first, last);
            }

            if (endExclusive == length) {
                CharSequence first = baseSequence.subSequence(startInclusive, baseLength);
                CharSequence last;
                if (repetitions == 2) {
                    last = baseSequence;
                }
                else {
                    last = new RepeatingCharSequence(baseSequence, baseLength, length, repetitions - 1);
                }
                return LazyConcatenatedCharSequence.getInstance(first, last);
            }

            CharSequence first = baseSequence.subSequence(startInclusive, baseLength);
            CharSequence last = baseSequence.subSequence(0, endExclusive);
            if (repetitions == 2) {
                return LazyConcatenatedCharSequence.getInstance(first, last);
            }
            int newRepetitions = repetitions - 2;
            CharSequence middle;
            if (newRepetitions == 1) {
                middle = baseSequence;
            }
            else {
                middle = new RepeatingCharSequence(
                    baseSequence, baseLength, newRepetitions * baseLength, repetitions - 2
                );
            }
            return LazyConcatenatedCharSequence.getInstance(first, middle, last);

        }

        @Override
        protected final boolean matchesImpl(CharSequence otherSameLength) {
            if (otherSameLength instanceof RepeatingCharSequence otherRepeating &&
                baseSequence instanceof EnhancedCharSequence b1) {
                return b1.matches(otherRepeating);
            }
            return super.matchesImpl(otherSameLength);
        }

        @Override
        protected final EnhancedCharSequence getLeftRotatedImpl(int offset) {
            int baseOffset = offset % baseLength;
            if (baseOffset == 0) {
                return this;
            }
            return EnhancedCharSequence.enhance(getRepeatingRotated(baseOffset, length));
        }

        @Override
        protected final EnhancedCharSequence repeatedImpl(int repetitions) {
            int newRepetitions = this.repetitions * repetitions;
            return new RepeatingCharSequence(baseSequence, baseLength, newRepetitions * baseLength, newRepetitions);
        }

        @Override
        protected final char charAtImpl(int index) {
            return baseSequence.charAt(index % baseLength);
        }

        @Override
        protected final CharSequence differentNonEmptySubSequence(int start, int end) {

            // The sequence will be [head] + [middle] + [tail].
            // The head and tail are the sub-sequences of the base sequence.
            // The middle part contains a repeating set of the base sequence.
            // Any of these may be empty.

            int newLength = end - start;

            // This is where in the base sequence the head starts.
            int headStart = start % baseLength;
            // This is where in the base sequence the tail ends.
            int tailEnd = end % baseLength;

            int availableHeadLength = baseLength - headStart;
            if (newLength <= availableHeadLength) {
                // The sequence is so short that the head is the whole new sequence.
                return baseSequence.subSequence(headStart, tailEnd == 0 ? baseLength : tailEnd);
            }

            if (newLength % baseLength == 0) {
                // The new sequence can be represented as just a new set of repeating versions of the base sequence,
                // rotated left enough that the head start index becomes the first index (0).
                return getRepeatingRotated(headStart, newLength);
            }

            CharSequence head = getSubSequenceFrom(headStart);
            CharSequence tail = getSubSequenceTo(tailEnd);

            int remainingNewLength = newLength - head.length() - tail.length();

            if (remainingNewLength == 0) {
                // No middle section needed.
                return EnhancedCharSequence.getConcatenatedSequences(head, tail);
            }

            CharSequence middle = getRepeating(remainingNewLength / baseLength);
            remainingNewLength -= middle.length();
            if (remainingNewLength != 0) {
                throw new IllegalStateException();
            }

            return EnhancedCharSequence.getConcatenatedSequences(head, middle, tail);

        }

        private final CharSequence getRepeatingRotated(int offset, int newLength) {
            CharSequence newBaseSequence = EnhancedCharSequence.getLeftRotatedSequence(baseSequence, offset);
            int newRepetitions = newLength / baseLength;
            if (newRepetitions == 1) {
                // Only one repetition.
                return newBaseSequence;
            }
            return new RepeatingCharSequence(newBaseSequence, baseLength, newLength, newRepetitions);
        }

        private final CharSequence getSubSequenceFrom(int start) {
            if (start == 0) {
                return WrappedSequence.EMPTY;
            }
            return baseSequence.subSequence(start, baseLength);
        }

        private final CharSequence getSubSequenceTo(int end) {
            if (end == 0) {
                return WrappedSequence.EMPTY;
            }
            return baseSequence.subSequence(0, end);
        }

        private final CharSequence getRepeating(int newRepetitions) {
            return switch (newRepetitions) {
                case 0 -> WrappedSequence.EMPTY;
                case 1 -> baseSequence;
                default -> new RepeatingCharSequence(
                    baseSequence,
                    baseLength,
                    baseLength * newRepetitions,
                    newRepetitions
                );
            };
        }

    }

    private static final class RotatedCharSequence extends LazyEnhancedCharSequence {

        private final CharSequence sequence;

        private final int leftRotateOffset;

        private RotatedCharSequence(CharSequence sequence, int sequenceLength, int leftRotateOffset) {
            super(sequenceLength);
            this.sequence = sequence;
            this.leftRotateOffset = leftRotateOffset;
        }

        @Nonnull
        @Override
        public final String toString() {
            StringBuilder buff = new StringBuilder(length);
            buff.append(sequence, leftRotateOffset, length);
            buff.append(sequence, 0, leftRotateOffset);
            return buff.toString();
        }

        @Override
        public final boolean isBlank() {
            return isBlankImpl(sequence);
        }

        @Override
        public final EnhancedCharSequence trim() {

            int startInclusive = EnhancedCharSequence.firstNonWhitespaceIndex(sequence, leftRotateOffset, length);
            int endInclusive = EnhancedCharSequence.lastNonWhitespaceIndex(sequence, 0, leftRotateOffset);

            CharSequence head, tail;

            if (startInclusive == length) {
                // Head blank.
                head = null;
            }
            else {
                head = sequence.subSequence(startInclusive, length);
            }

            if (endInclusive == leftRotateOffset) {
                // Tail blank.
                tail = null;
            }
            else {
                tail = sequence.subSequence(0, endInclusive + 1);
            }

            if (head == null) {
                if (tail == null) {
                    return WrappedSequence.EMPTY;
                }
                return EnhancedCharSequence.enhance(tail);
            }
            if (tail == null) {
                return EnhancedCharSequence.enhance(head);
            }
            return LazyConcatenatedCharSequence.getInstance(head, tail);

        }

        @Override
        protected final boolean matchesImpl(CharSequence otherSameLength) {
            if (otherSameLength instanceof RotatedCharSequence otherRotated &&
                leftRotateOffset == otherRotated.leftRotateOffset &&
                sequence instanceof EnhancedCharSequence s1 &&
                otherRotated.sequence instanceof EnhancedCharSequence s2 &&
                s1.matches(s2)) {
                return true;
            }
            return super.matchesImpl(otherSameLength);
        }

        @Override
        protected final char charAtImpl(int index) {
            int realIndex = index + leftRotateOffset;
            if (realIndex >= length) {
                realIndex -= length;
            }
            return sequence.charAt(realIndex);
        }

        @Override
        protected final CharSequence differentNonEmptySubSequence(int start, int end) {
            int newLength = end - start;
            int headStart = leftRotateOffset + start;
            int availableHeadLength = length - headStart;
            if (newLength <= availableHeadLength) {
                return sequence.subSequence(headStart, headStart + newLength);
            }
            return getConcatenatedSequences(
                sequence.subSequence(headStart, length),
                sequence.subSequence(0, (leftRotateOffset + end) % length)
            );
        }

        @Override
        protected final EnhancedCharSequence getLeftRotatedImpl(int newOffset) {
            int effectiveOffset = (leftRotateOffset + newOffset) % length;
            if (effectiveOffset == 0) {
                // Undo the exact rotation applied by this object by returning the wrapped sequence as-is.
                return EnhancedCharSequence.enhance(sequence);
            }
            if (sequence instanceof EnhancedCharSequence enhanced) {
                // Delegate to the wrapped sequence's implementation.
                return enhanced.getLeftRotatedImpl(effectiveOffset);
            }
            return new RotatedCharSequence(sequence, length, effectiveOffset);
        }

    }

    private static final class LazyConcatenatedCharSequence extends LazyEnhancedCharSequence {

        private static final class Coordinate {

            private final int sequenceIndex;

            private final int charIndex;

            private Coordinate(int sequenceIndex, int charIndex) {
                this.sequenceIndex = sequenceIndex;
                this.charIndex = charIndex;
            }

            @Override
            public final String toString() {
                return sequenceIndex + ":" + charIndex;
            }

        }

        static final LazyConcatenatedCharSequence getInstance(CharSequence first, CharSequence last) {
            return getInstance(ImmutableList.of(first, last));
        }

        static final LazyConcatenatedCharSequence getInstance(CharSequence first, CharSequence middle, CharSequence last) {
            return getInstance(ImmutableList.of(first, middle, last));
        }

        static final LazyConcatenatedCharSequence getInstance(List<CharSequence> sequences) {
            int totalLength = 0;
            for (CharSequence sequence : sequences) {
                totalLength += sequence.length();
            }
            return new LazyConcatenatedCharSequence(sequences, totalLength);
        }

        private final List<CharSequence> sequences;

        private LazyConcatenatedCharSequence(List<CharSequence> sequences, int totalLength) {
            super(totalLength);
            this.sequences = sequences;
        }

        @Nonnull
        @Override
        public final String toString() {
            StringBuilder buff = new StringBuilder(length);
            for (CharSequence sequence : sequences) {
                buff.append(sequence);
            }
            return buff.toString();
        }

        @Override
        public final boolean isBlank() {
            for (CharSequence sequence : sequences) {
                if (sequence instanceof EnhancedCharSequence enhanced) {
                    if (!enhanced.isBlank()) {
                        return false;
                    }
                }
                else {
                    if (!StringUtils.isBlank(sequence)) {
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public final EnhancedCharSequence trim() {

            Coordinate start = getTrimStart();
            if (start == null) {
                return WrappedSequence.EMPTY;
            }
            Coordinate end = getTrimEnd();

            CharSequence first = sequences.get(start.sequenceIndex);

            int newSequenceCount = end.sequenceIndex - start.sequenceIndex + 1;

            if (newSequenceCount == 1) {
                return EnhancedCharSequence.enhance(
                    first.subSequence(start.charIndex, end.charIndex)
                );
            }

            first = first.subSequence(start.charIndex, first.length());
            CharSequence last = sequences.get(end.sequenceIndex).subSequence(0, end.charIndex);
            if (newSequenceCount == 2) {
                return getInstance(first, last);
            }

            if (newSequenceCount == 3) {
                return getInstance(first, sequences.get(start.sequenceIndex), last);
            }

            List<CharSequence> newList = new ArrayList<>(newSequenceCount);
            newList.add(first);
            newList.addAll(sequences.subList(start.sequenceIndex + 1, end.sequenceIndex));
            newList.add(last);
            return getInstance(newList);

        }

        @Override
        protected final char charAtImpl(int index) {
            for (CharSequence sequence : sequences) {
                int length = sequence.length();
                if (index < length) {
                    return sequence.charAt(index);
                }
                index -= length;
            }
            throw new IllegalStateException();
        }

        @Override
        protected final CharSequence differentNonEmptySubSequence(int start, int end) {

            Iterator<CharSequence> iter = sequences.iterator();

            CharSequence first = getSequenceStartingAt(start, iter);

            int newLength = end - start;

            int startWithLength = first.length();
            if (startWithLength >= newLength) {
                return first.subSequence(0, newLength);
            }

            ImmutableList.Builder<CharSequence> newSequences = ImmutableList.builder();
            newSequences.add(first);
            addNewSequencesToLength(newLength, iter, newSequences);

            return new LazyConcatenatedCharSequence(newSequences.build(), newLength);

        }

        /**
         * Creates a new LazyConcatenatedCharSequence that contains all this instance's sequences followed by the other
         * sequence. If the other sequence is also a LazyConcatenatedCharSequence, its members will be included
         * directly.
         */
        @Override
        protected final EnhancedCharSequence prependToImpl(CharSequence other) {
            List<CharSequence> newSequences;
            if (other instanceof LazyConcatenatedCharSequence concat) {
                newSequences = new ArrayList<>(sequences.size() + concat.sequences.size());
                newSequences.addAll(sequences);
                newSequences.addAll(concat.sequences);
            }
            else {
                newSequences = new ArrayList<>(sequences.size() + 1);
                newSequences.addAll(sequences);
                newSequences.add(other);
            }
            return getInstance(newSequences);
        }

        /**
         * Creates a new LazyConcatenatedCharSequence that contains the other sequence followed by all this instance's
         * sequences. If the other sequence is also a LazyConcatenatedCharSequence, its members will be included
         * directly.
         */
        @Override
        protected final EnhancedCharSequence appendToImpl(CharSequence other) {
            List<CharSequence> newSequences;
            if (other instanceof LazyConcatenatedCharSequence concat) {
                newSequences = new ArrayList<>(sequences.size() + concat.sequences.size());
                newSequences.addAll(concat.sequences);
                newSequences.addAll(sequences);
            }
            else {
                newSequences = new ArrayList<>(sequences.size() + 1);
                newSequences.add(other);
                newSequences.addAll(sequences);
            }
            return getInstance(newSequences);
        }

        protected final EnhancedCharSequence getLeftRotatedImpl(int offset) {
            Coordinate first = getLeftRotatedStart(offset);
            if (first.charIndex == 0) {
                int sequenceCount = sequences.size();
                List<CharSequence> newSequences = new ArrayList<>(sequenceCount);
                newSequences.addAll(sequences.subList(first.sequenceIndex, sequenceCount));
                newSequences.addAll(sequences.subList(0, first.sequenceIndex));
                return getInstance(newSequences);
            }
            return super.getLeftRotatedImpl(offset);
        }

        private static final CharSequence getSequenceStartingAt(int start, Iterator<CharSequence> iter) {
            int remaining = start;
            while (iter.hasNext()) {
                CharSequence candidate = iter.next();
                int candidateLen = candidate.length();
                if (candidateLen > remaining) {
                    return candidate.subSequence(remaining, candidateLen);
                }
                remaining -= candidateLen;
            }
            throw new IllegalStateException();
        }

        private static void addNewSequencesToLength(int newLength, Iterator<CharSequence> iter, ImmutableList.Builder<CharSequence> newSequences) {
            int remaining = newLength;
            while (iter.hasNext()) {
                CharSequence next = iter.next();
                int nextLen = next.length();
                if (nextLen > remaining) {
                    newSequences.add(next.subSequence(0, remaining));
                    remaining = 0;
                    break;
                }
                newSequences.add(next);
                remaining -= nextLen;
            }

            if (remaining > 0) {
                throw new IllegalStateException();
            }
        }

        private final Coordinate getTrimStart() {
            int sequenceIndex = 0;
            for (CharSequence sequence : sequences) {
                int len = sequence.length();
                int firstStartInclude = EnhancedCharSequence.firstNonWhitespaceIndex(sequence, 0, len);
                if (firstStartInclude == len) {
                    sequenceIndex++;
                    continue;
                }
                return new Coordinate(sequenceIndex, firstStartInclude);
            }
            return null;
        }

        private final Coordinate getTrimEnd() {
            int sequenceIndex = sequences.size() - 1;
            for (CharSequence sequence : sequences.reversed()) {
                int len = sequence.length();
                int lastEndInclude = EnhancedCharSequence.lastNonWhitespaceIndex(sequence, 0, len);
                if (lastEndInclude == -1) {
                    sequenceIndex--;
                    continue;
                }
                return new Coordinate(sequenceIndex, lastEndInclude + 1);
            }
            throw new IllegalStateException();
        }

        private final Coordinate getLeftRotatedStart(int offset) {
            int remainingOffset = offset;
            int sequenceIndex = 0;
            for (CharSequence sequence : sequences) {
                int len = sequence.length();
                if (remainingOffset >= len) {
                    sequenceIndex++;
                    remainingOffset -= len;
                    continue;
                }
                return new Coordinate(sequenceIndex, remainingOffset);
            }
            throw new IllegalStateException();
        }

    }

    /**
     * Returns an EnhancedCharSequence equivalent to the given sequence.
     *
     * @param sequence
     *     the sequence to be enhanced.
     * @return an EnhancedCharSequence wrapping the given sequence if it is not already an EnhancedCharSequence; or the
     *     already enhanced instance.
     */
    public static final EnhancedCharSequence enhance(CharSequence sequence) {
        if (sequence instanceof EnhancedCharSequence enhanced) {
            return enhanced;
        }
        return switch (StringUtils.length(sequence)) {
            case 0 -> WrappedSequence.EMPTY;
            case 1 -> getSingletonSequence(sequence.charAt(0));
            default -> new WrappedSequence(sequence);
        };
    }

    /**
     * Returns an EnhancedCharSequence containing the given characters.
     *
     * @param chars
     *     the characters.
     * @return an EnhancedCharSequence containing the given characters; or an empty sequence if the characters are empty
     *     or null.
     */
    public static final EnhancedCharSequence getInstance(char[] chars) {
        int length = ArrayUtils.getLength(chars);
        if (length == 0) {
            return WrappedSequence.EMPTY;
        }
        return new CharArrayRangeSequence(chars, 0, length);
    }

    /**
     * Returns an EnhancedCharSequence containing the range of the given characters starting at the given offset and
     * extending for the given length.
     *
     * @param chars
     *     the characters.
     * @param offset
     *     the starting offset for the region.
     * @param length
     *     the length of the region.
     * @return an EnhancedCharSequence containing the range of the given characters starting at the given offset and
     *     extending for the given length; or an empty sequence if the requested region is empty.
     * @throws IndexOutOfBoundsException
     *     if the offset is negative; if either the offset or the effective final index of the region is beyond the end
     *     of the array.
     */
    public static final EnhancedCharSequence getInstance(char[] chars, int offset, int length) {
        if (offset < 0) {
            throw new IndexOutOfBoundsException(offset + " < 0");
        }
        int charsLength = ArrayUtils.getLength(chars);
        if (offset > charsLength) {
            throw new IndexOutOfBoundsException(offset + " > " + charsLength);
        }
        int endIndexExclusive = offset + length;
        if (endIndexExclusive > charsLength) {
            throw new IndexOutOfBoundsException(offset + " + " + length + " >= " + charsLength);
        }
        if (length == 0) {
            return WrappedSequence.EMPTY;
        }
        return new CharArrayRangeSequence(chars, offset, length);
    }

    public static final EnhancedCharSequence empty() {
        return WrappedSequence.EMPTY;
    }

    public static final EnhancedCharSequence getSingletonSequence(char c) {
        return new SingleBmpCharSequence(c);
    }

    public static final EnhancedCharSequence getSingletonSequence(int codePoint) {
        char[] chars = Character.toChars(codePoint);
        if (chars.length == 1) {
            return new SingleBmpCharSequence(chars[0]);
        }
        return new CharArrayRangeSequence(chars);
    }

    public static final EnhancedCharSequence getRepeatingSequence(int codePoint, int repetitions) {
        if (repetitions < 0) {
            throw new IllegalArgumentException(repetitions + " < 0");
        }
        if (repetitions == 0) {
            return WrappedSequence.EMPTY;
        }
        if (repetitions == 1) {
            return getSingletonSequence(codePoint);
        }
        char[] chars = Character.toChars(codePoint);
        if (chars.length == 1) {
            return new RepeatingBmpCharSequence(chars[0], repetitions);
        }
        return RepeatingCharSequence.getInstance(new CharArrayRangeSequence(chars), repetitions);
    }

    public static final EnhancedCharSequence getRepeatingSequence(char c, int repetitions) {
        if (repetitions < 0) {
            throw new IllegalArgumentException(repetitions + " < 0");
        }
        if (repetitions == 0) {
            return WrappedSequence.EMPTY;
        }
        if (repetitions == 1) {
            return getSingletonSequence(c);
        }
        return new RepeatingBmpCharSequence(c, repetitions);
    }

    /**
     * Returns a CharSequence representing the requested number of repetitions of the given sequence.
     *
     * @param sequence
     *     the sequence to be repeated.
     * @param repetitions
     *     the number of times to repeat the sequence.
     * @return an EnhancedCharSequence representing the requested number of repetitions of the given sequence; or an
     *     empty sequence if the given sequence is empty or 0 repetitions were requested.
     * @throws IllegalArgumentException
     *     if the requested number of repetitions is negative.
     */
    public static final EnhancedCharSequence getRepeatingSequence(CharSequence sequence, int repetitions) {
        if (sequence instanceof EnhancedCharSequence enhanced) {
            return enhanced.repeated(repetitions);
        }
        if (repetitions < 0) {
            throw new IllegalArgumentException(repetitions + " < 0");
        }
        if (repetitions == 0) {
            return WrappedSequence.EMPTY;
        }
        return RepeatingCharSequence.getInstance(sequence, repetitions);
    }

    /**
     * Returns a CharSequence representing the concatenation of the given collection of sequences.
     *
     * @param sequences
     *     the sequences to be concatenated.
     * @return a CharSequence representing the concatenation of the given collection of sequences; or an empty sequence
     *     if a null or empty collection is supplied.
     */
    public static final CharSequence getConcatenatedSequences(Collection<? extends CharSequence> sequences) {
        if (CollectionsUtil.isNullOrEmpty(sequences)) {
            return WrappedSequence.EMPTY;
        }
        return concatSequencesImpl(sequences.stream());
    }

    /**
     * Returns an EnhancedCharSequence representing the concatenation of the given collection of sequences.
     *
     * @param sequences
     *     the sequences to be concatenated.
     * @return an EnhancedCharSequence representing the concatenation of the given collection of sequences; or an empty
     *     sequence if no sequences were supplied.
     */
    public static final CharSequence getConcatenatedSequences(CharSequence... sequences) {
        if (sequences == null) {
            return WrappedSequence.EMPTY;
        }
        return concatSequencesImpl(Arrays.stream(sequences));
    }

    /**
     * Returns a sequence representing the left rotation of the given sequence by the requested offset.
     *
     * @param sequence
     *     the sequence to be left-rotated.
     * @param offset
     *     the offset, representing how much left rotation is requested.
     * @return a sequence representing the left rotation of the given sequence by the requested offset.
     */
    public static final CharSequence getLeftRotatedSequence(CharSequence sequence, int offset) {
        if (sequence == null) {
            return WrappedSequence.EMPTY;
        }
        int len = sequence.length();
        if (len <= 1 || offset == 0) {
            return sequence;
        }
        offset %= len;
        if (offset == 0) {
            return sequence;
        }
        if (offset < 0) {
            offset += len;
        }
        if (sequence instanceof EnhancedCharSequence enhanced) {
            return enhanced.getLeftRotatedImpl(offset);
        }
        return new RotatedCharSequence(sequence, sequence.length(), offset);
    }

    /**
     * Returns a sequence representing the right rotation of the given sequence by the requested offset.
     *
     * @param sequence
     *     the sequence to be right-rotated.
     * @param offset
     *     the offset, representing how much left rotation is requested.
     * @return a sequence representing the left rotation of the given sequence by the requested offset.
     */
    public static final CharSequence getRightRotatedSequence(CharSequence sequence, int offset) {
        if (sequence == null) {
            return WrappedSequence.EMPTY;
        }
        return getLeftRotatedSequence(sequence, sequence.length() - offset);
    }

    private static final CharSequence concatSequencesImpl(Stream<? extends CharSequence> sequences) {
        ImmutableList<CharSequence> nonEmptySequences = ImmutableList.copyOf(
            sequences.filter(Objects::nonNull).filter(seq -> !seq.isEmpty()).iterator()
        );
        return switch (nonEmptySequences.size()) {
            case 0 -> WrappedSequence.EMPTY;
            case 1 -> nonEmptySequences.getFirst();
            default -> LazyConcatenatedCharSequence.getInstance(nonEmptySequences);
        };
    }

    private EnhancedCharSequence() {
    }

    /**
     * @inheritDoc
     */
    @Nonnull
    @Override
    public abstract String toString();

    /**
     * Tests whether this sequence and the other given sequence effectively match. "Match" here means that they contain
     * the same sequence of characters. A null sequence is treated the same as an empty sequence. This is meant to be as
     * close as possible to applying {@link String#equals(Object)} to the result of invoking
     * {@link CharSequence#toString()} on both this and the other sequence, but without having to do so.
     *
     * @param other
     *     the other sequence to test.
     * @return if this sequence and the other match, or if {@link #isEmpty() this sequence is empty} and the other
     *     sequence is null; false otherwise.
     */
    public final boolean matches(CharSequence other) {
        if (other == null) {
            return isEmpty();
        }
        if (length() != other.length()) {
            return false;
        }
        return matchesImpl(other);
    }

    /**
     * Tests whether this sequence is blank, i.e., containing only whitespace characters, just like
     * {@link String#isBlank()} or {@link StringUtils#isBlank(CharSequence)}.
     *
     * @return true if this sequence is blank; false otherwise.
     */
    public boolean isBlank() {
        return StringUtils.isBlank(this);
    }

    /**
     * Tests whether this sequence contains the other given sequence, just like {@link String#contains(CharSequence)}.
     *
     * @param other
     *     the sequence to search for in this sequence.
     * @return true if the given {@link CharSequence} is not null and appears within this sequence.
     */
    public final boolean contains(CharSequence other) {
        if (other == null) {
            return false;
        }
        if (other.isEmpty()) {
            return true;
        }
        return containsImpl(other);
    }

    /**
     * Returns an EnhancedCharSequence representing a trimmed version of this sequence, with leading and trailing
     * whitespace removed, just like {@link String#trim()}.
     *
     * @return an EnhancedCharSequence representing a trimmed version of this sequence.
     */
    public abstract EnhancedCharSequence trim();

    /**
     * Returns an EnhancedCharSequence representing the result of prepending this sequence to the other sequence.
     *
     * @param other
     *     the other sequence.
     * @return an EnhancedCharSequence representing the result of prepending this sequence to the other sequence.
     */
    public final EnhancedCharSequence prependTo(CharSequence other) {
        if (StringUtils.isEmpty(other)) {
            return this;
        }
        return prependToImpl(other);
    }

    /**
     * Returns an EnhancedCharSequence representing the result of appending this sequence to the other sequence.
     *
     * @param other
     *     the other sequence.
     * @return an EnhancedCharSequence representing the result of appending this sequence to the other sequence.
     */
    public final EnhancedCharSequence appendTo(CharSequence other) {
        if (StringUtils.isEmpty(other)) {
            return this;
        }
        return appendToImpl(other);
    }

    /**
     * Returns an EnhancedCharSequence representing the result of prepending the other sequence to this one.
     *
     * @param other
     *     the other sequence.
     * @return an EnhancedCharSequence representing the result of prepending the other sequence to this one.
     */
    public final EnhancedCharSequence withPrepended(CharSequence other) {
        return appendTo(other);
    }

    /**
     * Returns an EnhancedCharSequence representing the result of appending the other sequence to this one.
     *
     * @param other
     *     the other sequence.
     * @return an EnhancedCharSequence representing the result of appending the other sequence to this one.
     */
    public final EnhancedCharSequence withAppended(CharSequence other) {
        return prependTo(other);
    }

    /**
     * Returns an EnhancedCharSequence representing the result of prepending a repeated version of the other sequence to
     * this one. It is identical to invoking {@link #appendTo(CharSequence)} after creating a repeating sequence via
     * {@link #getRepeatingSequence(CharSequence, int)}.
     *
     * @param other
     *     the other sequence.
     * @return an EnhancedCharSequence representing the result of prepending the other sequence to this one.
     */
    public final EnhancedCharSequence withRepeatingPrepended(CharSequence other, int repetitions) {
        return appendTo(getRepeatingSequence(other, repetitions));
    }

    /**
     * Returns an EnhancedCharSequence representing the result of appending a repeated version of the other sequence to
     * this one. It is identical to invoking {@link #appendTo(CharSequence)} after creating a repeating sequence via
     * {@link #getRepeatingSequence(CharSequence, int)}.
     *
     * @param other
     *     the other sequence.
     * @return an EnhancedCharSequence representing the result of appending a repeated version of the other sequence to
     *     this one.
     */
    public final EnhancedCharSequence withRepeatingAppended(CharSequence other, int repetitions) {
        return prependTo(getRepeatingSequence(other, repetitions));
    }

    /**
     * Returns an EnhancedCharSequence representing a repetition of this sequence.
     *
     * @param repetitions
     *     the requested number of repetitions.
     * @return an EnhancedCharSequence representing a repetition of this sequence; or an empty sequence if the given
     *     sequence is empty or 0 repetitions were requested.
     * @throws IllegalArgumentException
     *     if the requested number of repetitions is negative.
     */
    public final EnhancedCharSequence repeated(int repetitions) {
        if (repetitions < 0) {
            throw new IllegalArgumentException(repetitions + " < 0");
        }
        if (repetitions == 0) {
            return WrappedSequence.EMPTY;
        }
        if (repetitions == 1) {
            return this;
        }
        return repeatedImpl(repetitions);
    }

    /**
     * Returns an EnhancedCharSequence representing the left rotation of this sequence by the requested offset.
     *
     * @param offset
     *     the offset, representing how much left rotation is requested.
     * @return an EnhancedCharSequence representing the left rotation of the given sequence by the requested offset.
     */
    public final EnhancedCharSequence getLeftRotated(int offset) {
        int len = length();
        offset %= len;
        if (offset == 0) {
            return this;
        }
        if (offset < 0) {
            offset += len;
        }
        return getLeftRotatedImpl(offset);
    }

    /**
     * Returns an EnhancedCharSequence representing the right rotation of the given sequence by the requested offset.
     *
     * @param offset
     *     the offset, representing how much right rotation is requested.
     * @return a sequence representing the right rotation of the given sequence by the requested offset.
     */
    public final EnhancedCharSequence getRightRotated(int offset) {
        return getLeftRotated(length() - offset);
    }

    /**
     * Implements {@link #matches(CharSequence)}, after it has been established that the other sequence is the same
     * length as this one.
     *
     * @param otherSameLength
     *     the other sequence to test, which is the same length as this sequence.
     * @return if this sequence and the other match; false otherwise.
     */
    protected boolean matchesImpl(CharSequence otherSameLength) {
        if (Arrays.equals(chars().toArray(), otherSameLength.chars().toArray())) {
            return true;
        }
        return false;
    }

    /**
     * Implements {@link #contains(CharSequence)}, after it has been established that the given sequence is not null or
     * empty. This default implementation uses {@code Strings.CS#contains(this, other)}.
     *
     * @param other
     *     the sequence to search for in this sequence.
     * @return true if the given {@link CharSequence} is appears within this sequence.
     */
    protected boolean containsImpl(CharSequence other) {
        return Strings.CS.contains(this, other);
    }

    /**
     * Implements {@link #prependTo(CharSequence)}, creating a new sequence with this sequence prepended to the other,
     * after it has been established that the other sequence is not null or empty.
     *
     * @param other
     *     the other non-null non-empty sequence.
     * @return a new {@link EnhancedCharSequence} representing the result of prepending this sequence to the other
     *     sequence.
     */
    protected EnhancedCharSequence prependToImpl(CharSequence other) {
        return LazyConcatenatedCharSequence.getInstance(this, other);
    }

    /**
     * Implements {@link #appendTo(CharSequence)}, creating a new sequence with this sequence appended to the other,
     * after it has been established that the other sequence is not null or empty.
     *
     * @param other
     *     the other non-null non-empty sequence.
     * @return a new {@link EnhancedCharSequence} representing the result of appending this sequence to the other
     *     sequence.
     */
    protected EnhancedCharSequence appendToImpl(CharSequence other) {
        return LazyConcatenatedCharSequence.getInstance(other, this);
    }

    /**
     * Implements {@link #getLeftRotated(int)} after the offset has been adjusted (modulo the length of this sequence)
     * and was not 0.
     *
     * @param offset
     *     a non-zero offset less than the length of this sequence.
     * @return a new EnhancedCharSequence representing the left rotation of the given sequence by the requested offset.
     */
    protected EnhancedCharSequence getLeftRotatedImpl(int offset) {
        return new RotatedCharSequence(this, length(), offset);
    }

    /**
     * Implements {@link #repeated(int)} after it has been established that the requested number of repetitions is at
     * least 2.
     *
     * @param repetitions
     *     the requested number of repetitions of this sequence, no less than 2.
     * @return a new EnhancedCharSequence representing the requested number of repetitions of this sequence.
     */
    protected EnhancedCharSequence repeatedImpl(int repetitions) {
        return RepeatingCharSequence.getInstance(this, repetitions);
    }

}

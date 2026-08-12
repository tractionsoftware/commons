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

import com.google.common.annotations.Beta;
import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableSet;
import com.tractionsoftware.commons.io.IOUtil;
import com.tractionsoftware.commons.io.StringWriteUtil;
import com.tractionsoftware.commons.lang.EnhancedCharSequence;
import com.tractionsoftware.commons.lang.StringUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

@Beta
public final class TextWrapUtil {

    /*
     * Not instantiable.
     */
    private TextWrapUtil() {
    }

    public static final String DEFAULT_WRAP_CHARACTERS = "/.";

    public static final String DEFAULT_ZERO_WIDTH_SPACE = String.valueOf(StringUtil.CHAR_ZERO_WIDTH_SPACE);

    public static interface ZeroWidthSpaceInserter {

        public default void printWithZeroWidthSpacesInserted(PrintWriter out, String text) {
            IOUtil.runIOOperationSafe(
                () -> printWithZeroWidthSpacesInserted((Appendable) out, text)
            );
        }

        public default void printWithZeroWidthSpacesInserted(StringBuilder buff, String text) {
            IOUtil.runIOOperationSafe(
                () -> printWithZeroWidthSpacesInserted((Appendable) buff, text)
            );
        }

        public void printWithZeroWidthSpacesInserted(Appendable out, String text) throws IOException;

    }

    private static abstract class AbstractZeroWidthSpaceInserter<M> implements ZeroWidthSpaceInserter {

        protected final M matcher;

        protected final String preferredZeroWidthSpace;

        private AbstractZeroWidthSpaceInserter(M matcher, String preferredZeroWidthSpace) {
            this.matcher = matcher;
            this.preferredZeroWidthSpace = preferredZeroWidthSpace;
        }

        protected final boolean shouldCancelWrap(char c) {
            if (Character.isWhitespace(c) ||
                StringUtil.isAlternativeWhitespaceChar(c) ||
                c == '\'' ||
                c == '"') {
                return true;
            }
            return false;
        }

        protected final boolean shouldCancelWrap(int codePoint) {
            if (Character.isWhitespace(codePoint) ||
                StringUtil.isAlternativeWhitespaceCodePoint(codePoint) ||
                codePoint == '\'' ||
                codePoint == '"') {
                return true;
            }
            return false;
        }

        protected final void appendZeroWidthSpace(Appendable out) throws IOException {
            out.append(preferredZeroWidthSpace);
        }

        static abstract class Builder {

            protected final String text;

            protected final int length;

            private int nextIndex;

            private int lastWrapped;

            private boolean wrapOnNextProcess;

            Builder(String text, int length) {
                this.text = text;
                this.length = length;
                this.nextIndex = 0;
                this.lastWrapped = -1;
                this.wrapOnNextProcess = false;
            }

            public final boolean hasNext() {
                if (nextIndex < length) {
                    return true;
                }
                return false;
            }

            public final boolean processNext(Appendable out) throws IOException {
                load(nextIndex);
                boolean wrap = processNextImpl(out);
                updateWrapNext();
                nextIndex++;
                return wrap;
            }

            public final void finish(Appendable out) throws IOException {
                if (lastWrapped == -1) {
                    out.append(text);
                }
                else {
                    appendImpl(out, lastWrapped, length);
                }
            }

            protected abstract void load(int index);

            protected abstract boolean shouldCancelWrap();

            protected abstract boolean shouldWrapNext();

            protected abstract void appendImpl(Appendable out, int start, int end) throws IOException;

            private final boolean processNextImpl(Appendable out) throws IOException {

                if (wrapOnNextProcess) {

                    if (shouldCancelWrap()) {
                        // Don't wrap now, and don't wrap next.
                        wrapOnNextProcess = false;
                        return false;
                    }

                    int startAppend;
                    if (lastWrapped == -1) {
                        startAppend = 0;
                    }
                    else {
                        startAppend = lastWrapped;
                    }
                    appendBeforeWrap(out, startAppend, nextIndex);
                    return true;

                }

                return false;

            }

            private final void appendBeforeWrap(Appendable out, int start, int end) throws IOException {
                appendImpl(out, start, end);
                lastWrapped = end;
            }

            private final void updateWrapNext() {
                this.wrapOnNextProcess = shouldWrapNext();
            }

        }

        @Override
        public final void printWithZeroWidthSpacesInserted(Appendable out, String text) throws IOException {
            Builder builder = createBuilder(text);
            while (builder.hasNext()) {
                boolean wrap = builder.processNext(out);
                if (wrap) {
                    appendZeroWidthSpace(out);
                }
            }
            builder.finish(out);
        }

        protected abstract Builder createBuilder(String text);

    }

    private static final class CharBasedZeroWidthSpaceInserter
        extends AbstractZeroWidthSpaceInserter<CharMatcher> {

        public static final CharBasedZeroWidthSpaceInserter createInstance(char[] wrapChars, String nonSpaceBreak) {
            return new CharBasedZeroWidthSpaceInserter(
                CharMatcher.anyOf(EnhancedCharSequence.getInstance(wrapChars)),
                nonSpaceBreak
            );
        }

        private final class BuilderImpl extends Builder {

            private char c;

            private BuilderImpl(String text) {
                super(text, text.length());
            }

            @Override
            protected final void load(int index) {
                c = text.charAt(index);
            }

            @Override
            protected final boolean shouldCancelWrap() {
                return CharBasedZeroWidthSpaceInserter.this.shouldCancelWrap(c);
            }

            @Override
            protected final boolean shouldWrapNext() {
                return CharBasedZeroWidthSpaceInserter.this.matcher.matches(c);
            }

            @Override
            protected final void appendImpl(Appendable out, int start, int end) throws IOException {
                out.append(text, start, end);
            }

        }

        private CharBasedZeroWidthSpaceInserter(CharMatcher charMatcher, String preferredZeroWidthSpace) {
            super(charMatcher, preferredZeroWidthSpace);
        }

        @Override
        protected final Builder createBuilder(String text) {
            return new BuilderImpl(text);
        }

    }

    private static final class CodePointBasedZeroWidthSpaceInserter
        extends AbstractZeroWidthSpaceInserter<IntPredicate> {

        private static final IntPredicate getMatcher(int[] wrapCodePoints) {
            if (wrapCodePoints.length > 10) {
                return ImmutableSet.copyOf(IntStream.of(wrapCodePoints).boxed().iterator())::contains;
            }
            return codePoint -> ArrayUtils.contains(wrapCodePoints, codePoint);
        }

        public static final CodePointBasedZeroWidthSpaceInserter createInstance(int[] wrapCodePoints, String nonSpaceBreak) {
            return new CodePointBasedZeroWidthSpaceInserter(getMatcher(wrapCodePoints), nonSpaceBreak);
        }

        private final class BuilderImpl extends Builder {

            private final int[] codePoints;

            private int codePoint;

            private BuilderImpl(String text, int[] codePoints) {
                super(text, codePoints.length);
                this.codePoints = codePoints;
            }

            @Override
            protected final void load(int index) {
                codePoint = codePoints[index];
            }

            @Override
            protected final boolean shouldCancelWrap() {
                return CodePointBasedZeroWidthSpaceInserter.this.shouldCancelWrap(codePoint);
            }

            @Override
            protected final boolean shouldWrapNext() {
                return CodePointBasedZeroWidthSpaceInserter.this.matcher.test(codePoint);
            }

            @Override
            protected final void appendImpl(Appendable out, int start, int end) throws IOException {
                StringWriteUtil.appendCodePoints(out, codePoints, start, end);
            }

        }

        private CodePointBasedZeroWidthSpaceInserter(IntPredicate codePointMatcher, String nonSpaceBreak) {
            super(codePointMatcher, nonSpaceBreak);
        }

        @Override
        protected final Builder createBuilder(String text) {
            return new BuilderImpl(text, text.codePoints().toArray());
        }

    }

    public static final ZeroWidthSpaceInserter createDefaultNonSpaceWrapInserter() {
        return createNonSpaceWrapInserterImpl(DEFAULT_WRAP_CHARACTERS, DEFAULT_ZERO_WIDTH_SPACE);
    }

    public static final ZeroWidthSpaceInserter createNonSpaceWrapInserter(String wrap) {
        return createNonSpaceWrapInserter(wrap, DEFAULT_ZERO_WIDTH_SPACE);
    }

    public static final ZeroWidthSpaceInserter createNonSpaceWrapInserter(String wrap, String preferredZeroWidthSpace) {
        return createNonSpaceWrapInserterImpl(
            StringUtils.defaultIfBlank(wrap, DEFAULT_WRAP_CHARACTERS),
            StringUtils.defaultIfBlank(preferredZeroWidthSpace, DEFAULT_ZERO_WIDTH_SPACE)
        );
    }

    private static final ZeroWidthSpaceInserter createNonSpaceWrapInserterImpl(String wrap, String nonSpaceBreak) {

        int[] wrapCodePoints = wrap.codePoints()
            .distinct()
            .filter(codePoint -> !Character.isWhitespace(codePoint))
            .toArray();

        if (wrapCodePoints.length == 0) {
            wrapCodePoints = DEFAULT_WRAP_CHARACTERS.codePoints().toArray();
        }

        char[] wrapChars = new char[wrapCodePoints.length];
        int i = 0;
        for (int codePoint : wrapCodePoints) {
            if (!Character.isBmpCodePoint(codePoint)) {
                return CodePointBasedZeroWidthSpaceInserter.createInstance(wrapCodePoints, nonSpaceBreak);
            }
            wrapChars[i++] = (char) codePoint;
        }
        return CharBasedZeroWidthSpaceInserter.createInstance(wrapChars, nonSpaceBreak);

    }

}

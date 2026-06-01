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
import com.tractionsoftware.commons.io.StringWriteUtil;
import com.tractionsoftware.commons.lang.ObjectUtil;
import com.tractionsoftware.commons.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * A model for a way to conservatively produce a transformed version of some text input that incorporates a pluggable
 * way to produce the result. The main idea is to support not performing additional work or object allocation until it
 * is sure that some modification is required to apply the mapping to the input.
 *
 * @param <T>
 *     the type of object used to write or contain the result -- e.g., a {@link PrintWriter}, a {@link StringBuilder},
 *     etc.
 * @param <R>
 *     the type of the final result -- e.g., a {@link String}, or nothing ({@link Void}, if the result is written to
 *     some other object), etc.
 */
@Beta
public abstract class FilteringTextMapper<T, R> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilteringTextMapper.class.getName());

    public interface ResultWriter<T, R> {

        void write(@Nonnull T out, char c);

        void write(@Nonnull T out, @Nonnull CharSequence str);

        void write(@Nonnull T out, @Nonnull CharSequence str, @Nonnull StringUtil.AnalyzableIndexRange keep);

        void writeCodePoint(@Nonnull T out, int codePoint);

        void writeCodePoints(@Nonnull T out, @Nonnull int[] codePoints, @Nonnull StringUtil.IndexRange keep);

        void write(@Nonnull T out, @Nonnull CharSequence... str);

        R finish(@Nonnull CharSequence original, T out, @Nullable StringUtil.IndexRange keep);

    }

    /**
     * A super-class that adapts a {@link FilteringTextMapper} to implement a {@link TextTransformer}.
     *
     * @param <C>
     *     the type of the operator used by the {@link FilteringTextMapper} implementation.
     */
    protected static abstract class TextTransformerAdapter<C> implements TextTransformer {

        protected final C operator;

        TextTransformerAdapter(C operator) {
            this.operator = operator;
        }

        @Override
        public final String transform(CharSequence text) {
            return Objects.toString(transformImpl(text), null);
        }

        @Override
        public final void transform(@Nullable CharSequence text, @Nonnull Appendable out)
            throws IOException, TextTransformationException {
            if (StringUtils.isNotEmpty(text)) {
                transformImpl(text, out);
            }
        }

        @Override
        public final void transform(@Nonnull Reader in, @Nonnull Writer out)
            throws IOException, TextTransformationException {
            transformImpl(in, out);
        }

        @Nonnull
        protected abstract CharSequence transformImpl(@Nonnull CharSequence text);

        protected abstract void transformImpl(@Nonnull CharSequence text, @Nonnull Appendable out);

        protected abstract void transformImpl(@Nonnull Reader in, @Nonnull Writer out) throws IOException;

    }

    static abstract class StringResultWriter<R> implements ResultWriter<StringBuilder,R> {

        @Override
        public final void write(@Nonnull StringBuilder buff, char c) {
            buff.append(c);
        }

        @Override
        public final void writeCodePoint(@Nonnull StringBuilder buff, int codePoint) {
            buff.appendCodePoint(codePoint);
        }

        @Override
        public final void write(@Nonnull StringBuilder buff, @Nonnull CharSequence str, @Nonnull StringUtil.AnalyzableIndexRange keep) {
            buff.append(str, keep.start(), keep.end());
        }

        @Override
        public final void writeCodePoints(@Nonnull StringBuilder buff, @Nonnull int[] codePoints, @Nonnull StringUtil.IndexRange keep) {
            StringWriteUtil.appendCodePoints(buff, codePoints, keep.start(), keep.end());
        }

        @Override
        public final void write(@Nonnull StringBuilder buff, @Nonnull CharSequence str) {
            buff.append(str);
        }

        @Override
        public final void write(@Nonnull StringBuilder buff, @Nonnull CharSequence... str) {
            for (CharSequence s : str) {
                buff.append(s);
            }
        }

    }

    static final StringResultWriter<String> ON_DEMAND_STRING_BUILDER_WRITER = new StringResultWriter<>() {

        @Override
        public final String finish(@Nonnull CharSequence original, @Nullable StringBuilder buff, @Nullable StringUtil.IndexRange keep) {
            if (buff == null) {
                return original.toString();
            }
            if (keep != null) {
                buff.append(original, keep.start(), keep.end());
            }
            return buff.toString();
        }

    };

    static final StringResultWriter<Void> EXISTING_STRING_BUILDER_WRITER = new StringResultWriter<>() {

        @Override
        public final Void finish(@Nonnull CharSequence original, @Nonnull StringBuilder out, @Nullable StringUtil.IndexRange keep) {
            if (keep != null) {
                out.append(original, keep.start(), keep.end());
            }
            return null;
        }

    };

    static final ResultWriter<PrintWriter,Void> PRINT_WRITER_RESULT_WRITER = new ResultWriter<>() {

        @Override
        public final void write(@Nonnull PrintWriter out, char c) {
            out.write(c);
        }

        @Override
        public final void write(@Nonnull PrintWriter out, @Nonnull CharSequence str, @Nonnull StringUtil.AnalyzableIndexRange keep) {
            if (str instanceof String s) {
                out.write(s, keep.start(), keep.length());
            }
            else {
                out.append(str, keep.start(), keep.end());
            }
        }

        @Override
        public final void writeCodePoint(@Nonnull PrintWriter out, int codePoint) {
            out.write(Character.toChars(codePoint));
        }

        @Override
        public final void writeCodePoints(@Nonnull PrintWriter out, @Nonnull int[] codePoints, @Nonnull StringUtil.IndexRange keep) {
            int start = keep.start();
            int end = keep.end();
            for (int i = start; i < end; i++) {
                out.write(Character.toChars(codePoints[i]));
            }
        }

        @Override
        public final void write(@Nonnull PrintWriter out, @Nonnull CharSequence str) {
            out.print(str);
        }

        @Override
        public final void write(@Nonnull PrintWriter out, @Nonnull CharSequence... str) {
            for (CharSequence s : str) {
                out.print(s);
            }
        }

        @Override
        public Void finish(@Nonnull CharSequence original, @Nullable PrintWriter out, @Nullable StringUtil.IndexRange keep) {
            if (out != null && keep != null) {
                out.append(original, keep.start(), keep.end());
            }
            return null;
        }

    };

    static final ResultWriter<Appendable,Void> GENERIC_APPENDABLE_RESULT_WRITER = new ResultWriter<>() {

        @Override
        public final void write(@Nonnull Appendable out, char c) {
            StringWriteUtil.safeAppend(out, c);
        }

        @Override
        public final void write(@Nonnull Appendable out, @Nonnull CharSequence str, @Nonnull StringUtil.AnalyzableIndexRange keep) {
            StringWriteUtil.safeAppend(out, str, keep.start(), keep.end());
        }

        @Override
        public final void writeCodePoint(@Nonnull Appendable out, int codePoint) {
            StringWriteUtil.safeAppend(out, Character.toString(codePoint));
        }

        @Override
        public final void writeCodePoints(@Nonnull Appendable out, @Nonnull int[] codePoints, @Nonnull StringUtil.IndexRange keep) {
            try {
                StringWriteUtil.appendCodePoints(out, codePoints, keep.start(), keep.end());
            }
            catch (IOException e) {
                // This should not happen.
                LOGGER.warn("Failed to write to {}", ObjectUtil.safeToStringObject(out), e);
            }
        }

        @Override
        public final void write(@Nonnull Appendable out, @Nonnull CharSequence str) {
            StringWriteUtil.safeAppend(out, str);
        }

        @Override
        public final void write(@Nonnull Appendable out, @Nonnull CharSequence... str) {
            for (CharSequence s : str) {
                StringWriteUtil.safeAppend(out, s);
            }
        }

        @Override
        public final Void finish(@Nonnull CharSequence original, @Nullable Appendable out, @Nullable StringUtil.IndexRange keep) {
            if (out != null && keep != null) {
                StringWriteUtil.safeAppend(out, original, keep.start(), keep.end());
            }
            return null;
        }

    };

    public abstract class Value {

        private boolean consumed = false;

        Value() {
        }

        public final void remove() {
            consume();
            FilteringTextMapper.this.removePrevious();
        }

        public final void keep() {
            consume();
            FilteringTextMapper.this.keepPrevious();
        }

        public final void removeOrKeepIf(boolean condition) {
            if (condition) {
                remove();
            }
            else {
                keep();
            }
        }

        public final void replace(char c) {
            consume();
            FilteringTextMapper.this.replacePrevious(c);
        }

        public final void replace(String s) {
            consume();
            FilteringTextMapper.this.replacePrevious(s);
        }

        public final void replace(String... s) {
            consume();
            FilteringTextMapper.this.replacePrevious(s);
        }

        public final void replaceWithCodePoint(int codePoint) {
            consume();
            FilteringTextMapper.this.replacePreviousWithCodePoint(codePoint);
        }

        public final void appendReplacement(Consumer<T> appender) {
            consume();
            FilteringTextMapper.this.replacePreviousWithAppend(appender);
        }

        private final void consume() {
            if (consumed) {
                throw new IllegalStateException();
            }
            consumed = true;
        }

    }

    static class IndexRangeImpl extends StringUtil.AbstractGrowableIndexRange {

        static IndexRangeImpl createEmpty(int start) {
            return new IndexRangeImpl(start);
        }

        static IndexRangeImpl createContainingStart(int start, boolean containsNonBmpCodePoint) {
            return new IndexRangeImpl(start, start + 1, containsNonBmpCodePoint);
        }

        private IndexRangeImpl(int start, int end, boolean containsNonBmpCodePoint) {
            super(start, end, containsNonBmpCodePoint);
        }

        private IndexRangeImpl(int start) {
            super(start);
        }

        final void expand(BooleanSupplier isNonBmpCodePoint) {
            expandImpl();
            setContainsNonBmpCodePoint(isNonBmpCodePoint);
        }

    }

    protected final CharSequence original;

    private final ResultWriter<T,R> writer;

    protected final int inputLength;

    protected int nextIndex = 0;

    private T out;

    private IndexRangeImpl currentKeepRange = null;

    FilteringTextMapper(CharSequence original, T out, ResultWriter<T,R> writer, int inputLength) {
        this.original = original;
        this.out = out;
        this.writer = writer;
        this.inputLength = inputLength;
    }

    public final boolean hasNext() {
        return (nextIndex < inputLength);
    }

    public final R finish() {
        return writer.finish(original, out, currentKeepRange);
    }

    final void removePrevious() {
        T out = getOrCreate();
        if (currentKeepRange != null) {
            commitInterval(out, writer, currentKeepRange);
            currentKeepRange = null;
        }
    }

    final void replacePrevious(char c) {
        T out = getOrCreate();
        if (currentKeepRange != null) {
            commitInterval(out, writer, currentKeepRange);
            currentKeepRange = null;
        }
        writer.write(out, c);
    }

    final void replacePrevious(CharSequence str) {
        T out = getOrCreate();
        if (currentKeepRange != null) {
            commitInterval(out, writer, currentKeepRange);
            currentKeepRange = null;
        }
        writer.write(out, str);
    }

    final void replacePrevious(CharSequence... str) {
        T out = getOrCreate();
        if (currentKeepRange != null) {
            commitInterval(out, writer, currentKeepRange);
            currentKeepRange = null;
        }
        writer.write(getOrCreate(), str);
    }

    final void replacePreviousWithCodePoint(int codePoint) {
        T out = getOrCreate();
        if (currentKeepRange != null) {
            commitInterval(out, writer, currentKeepRange);
            currentKeepRange = null;
        }
        writer.writeCodePoint(getOrCreate(), codePoint);
    }

    final void replacePreviousWithAppend(Consumer<T> appender) {
        T out = getOrCreate();
        if (currentKeepRange != null) {
            commitInterval(out, writer, currentKeepRange);
            currentKeepRange = null;
        }
        appender.accept(getOrCreate());
    }

    final void keepPrevious() {
        if (currentKeepRange == null) {
            currentKeepRange = IndexRangeImpl.createContainingStart(nextIndex - 1, previousWasNonBmpCodePoint());
        }
        else {
            currentKeepRange.expand(this::previousWasNonBmpCodePoint);
        }
    }

    private T getOrCreate() {
        if (out == null) {
            out = createOut();
        }
        return out;
    }

    abstract T createOut();

    abstract boolean previousWasNonBmpCodePoint();

    abstract void commitInterval(T out, ResultWriter<T,R> writer, IndexRangeImpl currentKeep);

}

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

@Beta
public abstract class FilteringTextMapper<T, R> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilteringTextMapper.class.getName());

    public interface ResultWriter<T, R> {

        void write(T out, char c);

        void write(T out, CharSequence str);

        void write(T out, CharSequence str, StringUtil.AnalyzableIndexRange keep);

        void writeCodePoint(T out, int codePoint);

        void writeCodePoints(T out, int[] codePoints, StringUtil.IndexRange keep);

        void write(T out, CharSequence... str);

        R finish(CharSequence original, T out, StringUtil.IndexRange keep);

    }

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
        public final void transform(CharSequence text, Appendable out) {
            if (text != null) {
                transformImpl(text, out);
            }
        }

        protected abstract CharSequence transformImpl(CharSequence text);

        protected abstract void transformImpl(CharSequence text, Appendable out);

    }

    static abstract class StringResultWriter<R> implements ResultWriter<StringBuilder,R> {

        @Override
        public final void write(StringBuilder buff, char c) {
            buff.append(c);
        }

        @Override
        public final void writeCodePoint(StringBuilder buff, int codePoint) {
            buff.appendCodePoint(codePoint);
        }

        @Override
        public final void write(StringBuilder buff, CharSequence str, StringUtil.AnalyzableIndexRange keep) {
            buff.append(str, keep.start(), keep.end());
        }

        @Override
        public final void writeCodePoints(StringBuilder buff, int[] codePoints, StringUtil.IndexRange keep) {
            StringWriteUtil.appendCodePoints(buff, codePoints, keep.start(), keep.end());
        }

        @Override
        public final void write(StringBuilder buff, CharSequence str) {
            buff.append(str);
        }

        @Override
        public final void write(StringBuilder buff, CharSequence... str) {
            for (CharSequence s : str) {
                buff.append(s);
            }
        }

    }

    static final StringResultWriter<String> ON_DEMAND_STRING_BUILDER_WRITER = new StringResultWriter<>() {

        @Override
        public String finish(CharSequence original, StringBuilder buff, StringUtil.IndexRange keep) {
            if (buff == null) {
                return Objects.toString(original, null);
            }
            if (keep != null) {
                buff.append(original, keep.start(), keep.end());
            }
            return buff.toString();
        }

    };

    static final StringResultWriter<Void> EXISTING_STRING_BUILDER_WRITER = new StringResultWriter<>() {

        @Override
        public Void finish(CharSequence original, StringBuilder buff, StringUtil.IndexRange keep) {
            if (keep != null) {
                buff.append(original, keep.start(), keep.end());
            }
            return null;
        }

    };

    static final ResultWriter<PrintWriter,Void> PRINT_WRITER_RESULT_WRITER = new ResultWriter<>() {

        @Override
        public void write(PrintWriter out, char c) {
            out.write(c);
        }

        @Override
        public void write(PrintWriter out, CharSequence str, StringUtil.AnalyzableIndexRange keep) {
            if (keep != null) {
                if (str instanceof String s) {
                    out.write(s, keep.start(), keep.length());
                }
                else {
                    out.append(str, keep.start(), keep.end());
                }
            }
        }

        @Override
        public void writeCodePoint(PrintWriter out, int codePoint) {
            out.write(Character.toChars(codePoint));
        }

        @Override
        public void writeCodePoints(PrintWriter out, int[] codePoints, StringUtil.IndexRange keep) {
            int start = keep.start();
            int end = keep.end();
            for (int i = start; i < end; i ++) {
                out.write(Character.toChars(codePoints[i]));
            }
        }

        @Override
        public void write(PrintWriter out, CharSequence str) {
            out.print(str);
        }

        @Override
        public void write(PrintWriter out, CharSequence... str) {
            for (CharSequence s : str) {
                out.print(s);
            }
        }

        @Override
        public Void finish(CharSequence original, PrintWriter out, StringUtil.IndexRange keep) {
            if (keep != null) {
                out.append(original, keep.start(), keep.end());
            }
            return null;
        }

    };

    static final ResultWriter<Appendable,Void> GENERIC_APPENDABLE_RESULT_WRITER = new ResultWriter<>() {

        @Override
        public void write(Appendable out, char c) {
            StringWriteUtil.safeAppend(out, c);
        }

        @Override
        public void write(Appendable out, CharSequence str, StringUtil.AnalyzableIndexRange keep) {

        }

        @Override
        public void writeCodePoint(Appendable out, int codePoint) {
            StringWriteUtil.safeAppend(out, Character.toString(codePoint));
        }

        @Override
        public void writeCodePoints(Appendable out, int[] codePoints, StringUtil.IndexRange keep) {
            try {
                StringWriteUtil.appendCodePoints(out, codePoints, keep.start(), keep.end());
            }
            catch (IOException e) {
                // This should not happen.
                LOGGER.warn("Failed to write to {}", ObjectUtil.safeToStringObject(out), e);
            }
        }

        @Override
        public void write(Appendable out, CharSequence str) {
            StringWriteUtil.safeAppend(out, str);
        }

        @Override
        public void write(Appendable out, CharSequence... str) {
            for (CharSequence s : str) {
                StringWriteUtil.safeAppend(out, s);
            }
        }

        @Override
        public Void finish(CharSequence original, Appendable out, StringUtil.IndexRange keep) {
            StringWriteUtil.safeAppend(out, original, keep.start(), keep.end());
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

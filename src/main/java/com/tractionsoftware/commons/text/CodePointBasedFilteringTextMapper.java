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
import com.tractionsoftware.commons.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;

/**
 * A {@link FilteringTextMapper} that works in terms of int values representing code points.
 *
 * @param <T>
 *     the type of object used to write or contain the result -- e.g., a {@link PrintWriter}, a {@link StringBuilder},
 *     etc.
 * @param <R>
 *     the type of the final result -- e.g., a {@link String}, or nothing ({@link Void}, if the result is written to
 *     some other object), etc.
 */
@Beta
public final class CodePointBasedFilteringTextMapper<T, R> extends FilteringTextMapper<T,R> {

    @FunctionalInterface
    private static interface Creator<T> {

        T create(int[] codePoints, int nextIndex);

    }

    private static final class RemovingTransformer extends TextTransformerAdapter<IntPredicate> {

        private RemovingTransformer(IntPredicate matcher) {
            super(matcher);
        }

        @Nonnull
        @Override
        protected final CharSequence transformImpl(@Nonnull CharSequence text) {
            return Objects.requireNonNull(removeIf(text, operator));
        }

        @Override
        protected final void transformImpl(@Nonnull CharSequence text, @Nonnull Appendable out) {
            removeIf(text, out, operator);
        }

        @Override
        protected final void transformImpl(@Nonnull Reader in, @Nonnull Writer out) throws IOException {
            removeIf(in, out, operator);
        }

    }

    private static final class RetainingTransformer extends TextTransformerAdapter<IntPredicate> {

        private RetainingTransformer(IntPredicate matcher) {
            super(matcher);
        }

        @Nonnull
        @Override
        protected final CharSequence transformImpl(@Nonnull CharSequence text) {
            return Objects.requireNonNull(retainIf(text, operator));
        }

        @Override
        protected final void transformImpl(@Nonnull CharSequence text, @Nonnull Appendable out) {
            retainIf(text, out, operator);
        }

        @Override
        protected final void transformImpl(@Nonnull Reader in, @Nonnull Writer out) throws IOException {
            retainIf(in, out, operator);
        }

    }

    private static final class CodePointMappingTransformer extends TextTransformerAdapter<StringUtil.CodePointMapper> {

        CodePointMappingTransformer(StringUtil.CodePointMapper replacer) {
            super(replacer);
        }

        @Nonnull
        @Override
        protected final CharSequence transformImpl(@Nonnull CharSequence text) {
            return Objects.requireNonNull(replace(text, operator));
        }

        @Override
        protected final void transformImpl(@Nonnull CharSequence text, @Nonnull Appendable out) {
            replace(text, out, operator);
        }

        @Override
        protected final void transformImpl(@Nonnull Reader in, @Nonnull Writer out) throws IOException {
            replace(in, out, operator);
        }

    }

    private static final class CodePointToStringMappingTransformer
        extends TextTransformerAdapter<IntFunction<CharSequence>> {

        CodePointToStringMappingTransformer(IntFunction<CharSequence> replacer) {
            super(replacer);
        }

        @Nonnull
        @Override
        protected final CharSequence transformImpl(@Nonnull CharSequence text) {
            return Objects.requireNonNull(replace(text, operator));
        }

        @Override
        protected final void transformImpl(@Nonnull CharSequence text, @Nonnull Appendable out) {
            replace(text, out, operator);
        }

        @Override
        protected final void transformImpl(@Nonnull Reader in, @Nonnull Writer out) throws IOException {
            replace(in, out, operator);
        }

    }

    public static final CodePointBasedFilteringTextMapper<StringBuilder,String> createDefaultInstance(@Nullable CharSequence str) {
        return createInstance(
            str,
            null,
            ON_DEMAND_STRING_BUILDER_WRITER,
            CodePointBasedFilteringTextMapper::createBuilder
        );
    }

    public static final CodePointBasedFilteringTextMapper<PrintWriter,Void> createInstanceForPrint(@Nullable CharSequence str, @Nonnull PrintWriter out) {
        return createInstance(str, out, PRINT_WRITER_RESULT_WRITER, null);
    }

    public static final CodePointBasedFilteringTextMapper<StringBuilder,Void> createInstanceForAppend(@Nullable CharSequence str, @Nonnull StringBuilder buff) {
        return createInstance(str, buff, EXISTING_STRING_BUILDER_WRITER, null);
    }

    public static final CodePointBasedFilteringTextMapper<? extends Appendable,Void> createInstanceForGenericAppend(@Nullable CharSequence str, @Nonnull Appendable out) {
        str = Objects.requireNonNullElse(str, "");
        Objects.requireNonNull(out, "output");
        if (out instanceof PrintWriter pw) {
            return createInstanceForPrint(str, pw);
        }
        if (out instanceof StringBuilder buff) {
            return createInstanceForAppend(str, buff);
        }
        return createInstance(str, out, GENERIC_APPENDABLE_RESULT_WRITER, null);
    }

    @Nullable
    public static final String removeIf(@Nullable CharSequence str, @Nonnull IntPredicate filter) {
        if (StringUtils.isEmpty(str)) {
            return Objects.toString(str, null);
        }
        return removeIf(createDefaultInstance(str), filter);
    }

    public static final void removeIf(@Nullable CharSequence str, @Nonnull PrintWriter out, @Nonnull IntPredicate filter) {
        if (str != null) {
            removeIf(createInstanceForPrint(str, out), filter);
        }
    }

    public static final void removeIf(@Nullable CharSequence str, @Nonnull StringBuilder buff, @Nonnull IntPredicate filter) {
        if (str != null) {
            removeIf(createInstanceForAppend(str, buff), filter);
        }
    }

    public static final void removeIf(@Nullable CharSequence str, @Nonnull Appendable out, @Nonnull IntPredicate filter) {
        if (str != null) {
            removeIf(createInstanceForGenericAppend(str, out), filter);
        }
    }

    public static final void removeIf(@Nonnull Reader in, @Nonnull Writer out, @Nonnull IntPredicate filter)
        throws IOException {
        Objects.requireNonNull(in, "input");
        removeIf(IOUtils.toString(in), out, filter);
    }

    @Nullable
    public static final String retainIf(@Nullable CharSequence str, @Nonnull IntPredicate filter) {
        if (StringUtils.isEmpty(str)) {
            return Objects.toString(str, null);
        }
        return retainIf(createDefaultInstance(str), filter);
    }

    public static final void retainIf(@Nullable CharSequence str, @Nonnull PrintWriter out, @Nonnull IntPredicate filter) {
        retainIf(createInstanceForPrint(str, out), filter);
    }

    public static final void retainIf(@Nullable CharSequence str, @Nonnull StringBuilder buff, @Nonnull IntPredicate filter) {
        retainIf(createInstanceForAppend(str, buff), filter);
    }

    public static final void retainIf(@Nullable CharSequence str, @Nonnull Appendable out, @Nonnull IntPredicate filter) {
        retainIf(createInstanceForGenericAppend(str, out), filter);
    }

    public static final void retainIf(@Nonnull Reader in, @Nonnull Writer out, @Nonnull IntPredicate filter)
        throws IOException {
        Objects.requireNonNull(in, "input");
        retainIf(IOUtils.toString(in), filter);
    }

    @Nullable
    public static final String replace(@Nullable CharSequence str, @Nonnull StringUtil.CodePointMapper replacer) {
        if (StringUtils.isEmpty(str)) {
            return Objects.toString(str, null);
        }
        return replace(createDefaultInstance(str), replacer);
    }

    @Nullable
    public static final String replace(@Nullable CharSequence str, @Nonnull IntFunction<CharSequence> replacer) {
        if (StringUtils.isEmpty(str)) {
            return Objects.toString(str, null);
        }
        return replace(createDefaultInstance(str), replacer);
    }

    public static final void replace(@Nullable CharSequence str, @Nonnull PrintWriter out, @Nonnull IntFunction<CharSequence> replacer) {
        if (StringUtils.isNotEmpty(str)) {
            replace(createInstanceForPrint(str, out), replacer);
        }
    }

    public static final void replace(@Nullable CharSequence str, @Nonnull StringBuilder buff, @Nonnull IntFunction<CharSequence> replacer) {
        if (StringUtils.isNotEmpty(str)) {
            replace(createInstanceForAppend(str, buff), replacer);
        }
    }

    public static final void replace(@Nullable CharSequence str, @Nonnull Appendable out, @Nonnull IntFunction<CharSequence> replacer) {
        if (StringUtils.isNotEmpty(str)) {
            replace(createInstanceForGenericAppend(str, out), replacer);
        }
    }

    public static final void replace(@Nonnull Reader in, @Nonnull Writer out, @Nonnull IntFunction<CharSequence> replacer)
        throws IOException {
        Objects.requireNonNull(in, "input");
        replace(IOUtils.toString(in), replacer);
    }

    public static final void replace(@Nullable CharSequence str, @Nonnull PrintWriter out, @Nonnull StringUtil.CodePointMapper replacer) {
        if (StringUtils.isNotEmpty(str)) {
            replace(createInstanceForPrint(str, out), replacer);
        }
    }

    public static final void replace(@Nullable CharSequence str, @Nonnull StringBuilder buff, @Nonnull StringUtil.CodePointMapper replacer) {
        if (StringUtils.isNotEmpty(str)) {
            replace(createInstanceForAppend(str, buff), replacer);
        }
    }

    public static final void replace(@Nullable CharSequence str, @Nonnull Appendable out, @Nonnull StringUtil.CodePointMapper replacer) {
        if (StringUtils.isNotEmpty(str)) {
            replace(createInstanceForGenericAppend(str, out), replacer);
        }
    }

    public static final void replace(@Nonnull Reader in, @Nonnull Writer out, @Nonnull StringUtil.CodePointMapper replacer)
        throws IOException {
        Objects.requireNonNull(in, "input");
        replace(IOUtils.toString(in), out, replacer);
    }

    public static final TextTransformer createRemovingTransformer(@Nonnull IntPredicate filter) {
        Objects.requireNonNull(filter, "filter");
        return new RemovingTransformer(filter);
    }

    public static final TextTransformer createRetainingTransformer(@Nonnull IntPredicate matcher) {
        Objects.requireNonNull(matcher, "matcher");
        return new RetainingTransformer(matcher);
    }

    public static final TextTransformer createReplacingTransformer(@Nonnull StringUtil.CodePointMapper mapper) {
        Objects.requireNonNull(mapper, "mapper");
        return new CodePointMappingTransformer(mapper);
    }

    public static final TextTransformer createReplacingTransformer(@Nonnull IntFunction<CharSequence> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        return new CodePointToStringMappingTransformer(mapper);
    }

    private static final <T, R> CodePointBasedFilteringTextMapper<T,R> createInstance(@Nullable CharSequence str, T out, @Nonnull ResultWriter<T,R> writer, @Nullable Creator<T> creator) {
        str = Objects.requireNonNullElse(str, "");
        return new CodePointBasedFilteringTextMapper<>(str, str.codePoints().toArray(), out, writer, creator);
    }

    @Nonnull
    private static final StringBuilder createBuilder(@Nonnull int[] codePoints, int currentIndex) {
        return new StringBuilder(codePoints.length + (codePoints.length / 2));
    }

    private static final <R> R removeIf(@Nonnull CodePointBasedFilteringTextMapper<?,R> mapper, @Nonnull IntPredicate filter) {
        while (mapper.hasNext()) {
            if (filter.test(mapper.nextImpl())) {
                mapper.removePrevious();
            }
            else {
                mapper.keepPrevious();
            }
        }
        return mapper.finish();
    }

    private static final <R> R retainIf(@Nonnull CodePointBasedFilteringTextMapper<?,R> mapper, @Nonnull IntPredicate filter) {
        while (mapper.hasNext()) {
            if (filter.test(mapper.nextImpl())) {
                mapper.keepPrevious();
            }
            else {
                mapper.removePrevious();
            }
        }
        return mapper.finish();
    }

    private static final <R> R replace(@Nonnull CodePointBasedFilteringTextMapper<?,R> mapper, @Nonnull IntFunction<CharSequence> replacer) {
        while (mapper.hasNext()) {
            int codePoint = mapper.nextImpl();
            CharSequence replacement = replacer.apply(codePoint);
            if (replacement == null) {
                mapper.keepPrevious();
            }
            else if (replacement.isEmpty()) {
                mapper.removePrevious();
            }
            else {
                mapper.replacePrevious(replacement);
            }
        }
        return mapper.finish();
    }

    private static final <R> R replace(@Nonnull CodePointBasedFilteringTextMapper<?,R> mapper, @Nonnull StringUtil.CodePointMapper replacer) {
        while (mapper.hasNext()) {
            int codePoint = mapper.nextImpl();
            int replacementCodePoint = replacer.getReplacement(codePoint);
            if (codePoint == replacementCodePoint) {
                mapper.keepPrevious();
            }
            else {
                mapper.replacePreviousWithCodePoint(replacementCodePoint);
            }
        }
        return mapper.finish();
    }

    public final class CodePointValue extends Value {

        public final int codePoint;

        private CodePointValue(int codePoint) {
            this.codePoint = codePoint;
        }

    }

    private final int[] codePoints;

    private final Creator<T> creator;

    private CodePointBasedFilteringTextMapper(CharSequence original, int[] codePoints, T out, ResultWriter<T,R> writer, Creator<T> creator) {
        super(original, out, writer, codePoints.length);
        this.codePoints = codePoints;
        this.creator = creator;
    }

    public final CodePointValue next() {
        return new CodePointValue(nextImpl());
    }

    @Override
    T createOut() {
        return creator.create(codePoints, nextIndex);
    }

    @Override
    boolean previousWasNonBmpCodePoint() {
        return StringUtil.isNotBmpCodePoint(codePoints[nextIndex - 1]);
    }

    @Override
    void commitInterval(T out, ResultWriter<T,R> writer, IndexRangeImpl currentKeep) {
        writer.writeCodePoints(out, codePoints, currentKeep);
    }

    private int nextImpl() {
        return codePoints[nextIndex++];
    }

}

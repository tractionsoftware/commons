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

/**
 * A {@link FilteringTextMapper} that works in terms of char values.
 *
 * @param <T>
 *     the type of object used to write or contain the result -- e.g., a {@link PrintWriter}, a {@link StringBuilder},
 *     etc.
 * @param <R>
 *     the type of the final result -- e.g., a {@link String}, or nothing ({@link Void}, if the result is written to
 *     some other object), etc.
 */
@Beta
public class CharBasedFilteringTextMapper<T, R> extends FilteringTextMapper<T,R> {

    @FunctionalInterface
    private static interface Creator<T> {

        T create(CharSequence original, int currentIndex);

    }

    private static final class RemovingTransformer extends TextTransformerAdapter<CharMatcher> {

        private RemovingTransformer(CharMatcher filter) {
            super(filter);
        }

        @Nonnull
        @Override
        protected final CharSequence transformImpl(@Nonnull CharSequence text) {
            return Objects.requireNonNull(removeIf(operator, text));
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

    private static final class RetainingTransformer extends TextTransformerAdapter<CharMatcher> {

        private RetainingTransformer(CharMatcher matcher) {
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

    private static final class CharToCharMappingTransformer extends TextTransformerAdapter<StringUtil.CharMapper> {

        private CharToCharMappingTransformer(StringUtil.CharMapper operator) {
            super(operator);
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

    private static final class CharToStringMappingTransformer
        extends TextTransformerAdapter<StringUtil.CharToStringMapper> {

        CharToStringMappingTransformer(StringUtil.CharToStringMapper operator) {
            super(operator);
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

    public static final CharBasedFilteringTextMapper<StringBuilder,String> createDefaultInstance(@Nullable CharSequence str) {
        return createInstance(str, null, ON_DEMAND_STRING_BUILDER_WRITER, CharBasedFilteringTextMapper::createBuilder);
    }

    public static final CharBasedFilteringTextMapper<PrintWriter,Void> createInstanceForPrint(@Nullable CharSequence str, @Nonnull PrintWriter out) {
        return createInstance(str, out, PRINT_WRITER_RESULT_WRITER, null);
    }

    public static final CharBasedFilteringTextMapper<StringBuilder,Void> createInstanceForAppend(@Nullable CharSequence str, @Nonnull StringBuilder buff) {
        return createInstance(str, buff, EXISTING_STRING_BUILDER_WRITER, null);
    }

    public static final CharBasedFilteringTextMapper<? extends Appendable,Void> createInstanceForGenericAppend(@Nullable CharSequence str, @Nonnull Appendable out) {
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
    public static final String removeIf(CharMatcher filter, CharSequence str) {
        if (StringUtils.isEmpty(str)) {
            return Objects.toString(str, null);
        }
        return removeIf(createDefaultInstance(str), filter);
    }

    public static final void removeIf(CharSequence str, PrintWriter out, CharMatcher filter) {
        removeIf(createInstanceForPrint(str, out), filter);
    }

    public static final void removeIf(CharSequence str, StringBuilder buff, CharMatcher filter) {
        removeIf(createInstanceForAppend(str, buff), filter);
    }

    public static final void removeIf(CharSequence str, Appendable out, CharMatcher filter) {
        removeIf(createInstanceForGenericAppend(str, out), filter);
    }

    public static final void removeIf(@Nonnull Reader in, @Nonnull Writer out, @Nonnull CharMatcher filter)
        throws IOException {
        Objects.requireNonNull(in, "input");
        removeIf(IOUtils.toString(in), out, filter);
    }

    @Nullable
    public static String retainIf(@Nullable CharSequence str, @Nonnull CharMatcher filter) {
        if (StringUtils.isEmpty(str)) {
            return Objects.toString(str, null);
        }
        return retainIf(createDefaultInstance(str), filter);
    }

    public static final void retainIf(CharSequence str, @Nonnull PrintWriter out, @Nonnull CharMatcher filter) {
        retainIf(createInstanceForPrint(str, out), filter);
    }

    public static final void retainIf(CharSequence str, @Nonnull StringBuilder buff, @Nonnull CharMatcher filter) {
        retainIf(createInstanceForAppend(str, buff), filter);
    }

    public static final void retainIf(CharSequence str, @Nonnull Appendable out, @Nonnull CharMatcher filter) {
        retainIf(createInstanceForGenericAppend(str, out), filter);
    }

    public static final void retainIf(@Nonnull Reader in, @Nonnull Writer out, @Nonnull CharMatcher filter)
        throws IOException {
        Objects.requireNonNull(in, "input");
        Objects.requireNonNull(out, "output");
        retainIf(IOUtils.toString(in), out, filter);
    }

    @Nullable
    public static String replace(@Nullable CharSequence str, @Nonnull StringUtil.CharMapper replacer) {
        if (StringUtils.isEmpty(str)) {
            return Objects.toString(str, null);
        }
        return replace(createDefaultInstance(str), replacer);
    }

    @Nullable
    public static String replace(@Nullable CharSequence str, @Nonnull StringUtil.CharToStringMapper replacer) {
        if (StringUtils.isEmpty(str)) {
            return Objects.toString(str, null);
        }
        return replace(createDefaultInstance(str), replacer);
    }

    public static final void replace(@Nullable CharSequence str, @Nonnull PrintWriter out, @Nonnull StringUtil.CharMapper replacer) {
        if (StringUtils.isNotEmpty(str)) {
            replace(createInstanceForPrint(str, out), replacer);
        }
    }

    public static final void replace(@Nullable CharSequence str, @Nonnull PrintWriter out, @Nonnull StringUtil.CharToStringMapper replacer) {
        if (StringUtils.isNotEmpty(str)) {
            replace(createInstanceForPrint(str, out), replacer);
        }
    }

    public static final void replace(@Nullable CharSequence str, @Nonnull StringBuilder out, @Nonnull StringUtil.CharMapper replacer) {
        if (StringUtils.isNotEmpty(str)) {
            replace(createInstanceForAppend(str, out), replacer);
        }
    }

    public static final void replace(@Nullable CharSequence str, @Nonnull StringBuilder out, @Nonnull StringUtil.CharToStringMapper replacer) {
        if (StringUtils.isNotEmpty(str)) {
            replace(createInstanceForAppend(str, out), replacer);
        }
    }

    public static final void replace(@Nullable CharSequence str, @Nonnull Appendable out, @Nonnull StringUtil.CharMapper replacer) {
        if (StringUtils.isNotEmpty(str)) {
            replace(createInstanceForGenericAppend(str, out), replacer);
        }
    }

    public static final void replace(@Nonnull Reader in, @Nonnull Writer out, @Nonnull StringUtil.CharMapper replacer)
        throws IOException {
        Objects.requireNonNull(in, "input");
        replace(IOUtils.toString(in), out, replacer);
    }

    public static final void replace(CharSequence str, @Nonnull Appendable out, @Nonnull StringUtil.CharToStringMapper replacer) {
        if (StringUtils.isNotEmpty(str)) {
            replace(createInstanceForGenericAppend(str, out), replacer);
        }
    }

    public static final void replace(@Nonnull Reader in, @Nonnull Writer out, @Nonnull StringUtil.CharToStringMapper replacer)
        throws IOException {
        Objects.requireNonNull(in, "input");
        replace(IOUtils.toString(in), out, replacer);
    }

    public static final TextTransformer createRemovingTransformer(@Nonnull CharMatcher filter) {
        Objects.requireNonNull(filter, "filter");
        return new RemovingTransformer(filter);
    }

    public static final TextTransformer createRetainingTransformer(@Nonnull CharMatcher filter) {
        Objects.requireNonNull(filter, "filter");
        return new RetainingTransformer(filter);
    }

    public static final TextTransformer createReplacingTransformer(@Nonnull StringUtil.CharMapper mapper) {
        Objects.requireNonNull(mapper, "mapper");
        return new CharToCharMappingTransformer(mapper);
    }

    public static final TextTransformer createReplacingTransformer(@Nonnull StringUtil.CharToStringMapper mapper) {
        Objects.requireNonNull(mapper, "mapper");
        return new CharToStringMappingTransformer(mapper);
    }

    private static final <T, R> CharBasedFilteringTextMapper<T,R> createInstance(@Nullable CharSequence str, T out, @Nonnull ResultWriter<T,R> writer, @Nullable Creator<T> creator) {
        str = Objects.requireNonNullElse(str, "");
        return new CharBasedFilteringTextMapper<>(str, out, writer, creator);
    }

    private static final StringBuilder createBuilder(@Nonnull CharSequence original, int currentIndex) {
        return new StringBuilder(original.length() + 25);
    }

    private static final <R> R removeIf(@Nonnull CharBasedFilteringTextMapper<?,R> mapper, @Nonnull CharMatcher filter) {
        while (mapper.hasNext()) {
            if (filter.matches(mapper.nextImpl())) {
                mapper.removePrevious();
            }
            else {
                mapper.keepPrevious();
            }
        }
        return mapper.finish();
    }

    private static final <R> R retainIf(@Nonnull CharBasedFilteringTextMapper<?,R> mapper, @Nonnull CharMatcher filter) {
        while (mapper.hasNext()) {
            if (filter.matches(mapper.nextImpl())) {
                mapper.keepPrevious();
            }
            else {
                mapper.removePrevious();
            }
        }
        return mapper.finish();
    }

    private static final <R> R replace(@Nonnull CharBasedFilteringTextMapper<?,R> mapper, @Nonnull StringUtil.CharToStringMapper replacer) {
        while (mapper.hasNext()) {
            char c = mapper.nextImpl();
            CharSequence replacement = replacer.getReplacement(c);
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

    private static final <R> R replace(@Nonnull CharBasedFilteringTextMapper<?,R> mapper, @Nonnull StringUtil.CharMapper replacer) {
        while (mapper.hasNext()) {
            char c = mapper.nextImpl();
            char replacement = replacer.getReplacement(c);
            if (c == replacement) {
                mapper.keepPrevious();
            }
            else {
                mapper.replacePrevious(replacement);
            }
        }
        return mapper.finish();
    }

    public final class CharValue extends Value {

        public final char c;

        private CharValue(char c) {
            this.c = c;
        }

    }

    private final Creator<T> creator;

    private CharBasedFilteringTextMapper(CharSequence original, T out, ResultWriter<T,R> writer, Creator<T> creator) {
        super(original, out, writer, original.length());
        this.creator = creator;
    }

    public final CharValue next() {
        return new CharValue(nextImpl());
    }

    @Override
    final T createOut() {
        return creator.create(original, nextIndex);
    }

    @Override
    final boolean previousWasNonBmpCodePoint() {
        return false;
    }

    @Override
    final void commitInterval(T out, ResultWriter<T,R> writer, IndexRangeImpl currentKeep) {
        writer.write(out, original, currentKeep);
    }

    private char nextImpl() {
        return original.charAt(nextIndex++);
    }

}

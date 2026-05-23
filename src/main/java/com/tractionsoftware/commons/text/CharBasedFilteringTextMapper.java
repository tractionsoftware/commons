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

import java.io.PrintWriter;
import java.util.Objects;

/**
 * A {@link FilteringTextMapper} that works in terms of char values.
 *
 * <p>
 * For cases when the input
 */
@Beta
public class CharBasedFilteringTextMapper<T, R> extends FilteringTextMapper<T,R> {

    @FunctionalInterface
    private interface Creator<T> {

        T create(CharSequence original, int currentIndex);

    }

    private static final class RemovingTransformer extends TextTransformerAdapter<CharMatcher> {

        private RemovingTransformer(CharMatcher filter) {
            super(filter);
        }

        @Override
        protected CharSequence transformImpl(CharSequence text) {
            return removeIf(text, operator);
        }

        @Override
        protected void transformImpl(CharSequence text, Appendable out) {
            removeIf(out, text, operator);
        }

    }

    private static final class RetainingTransformer extends TextTransformerAdapter<CharMatcher> {

        private RetainingTransformer(CharMatcher matcher) {
            super(matcher);
        }

        @Override
        protected CharSequence transformImpl(CharSequence text) {
            return retainIf(text, operator);
        }

        @Override
        protected void transformImpl(CharSequence text, Appendable out) {
            retainIf(out, text, operator);
        }

    }

    private static final class CharToCharMappingTransformer extends TextTransformerAdapter<StringUtil.CharMapper> {

        CharToCharMappingTransformer(StringUtil.CharMapper operator) {
            super(operator);
        }

        @Override
        protected CharSequence transformImpl(CharSequence text) {
            return replace(text, operator);
        }

        @Override
        protected void transformImpl(CharSequence text, Appendable out) {
            replace(out, text, operator);
        }

    }

    private static final class CharToStringMappingTransformer extends TextTransformerAdapter<StringUtil.CharToStringMapper> {

        CharToStringMappingTransformer(StringUtil.CharToStringMapper operator) {
            super(operator);
        }

        @Override
        protected CharSequence transformImpl(CharSequence text) {
            return replace(text, operator);
        }

        @Override
        protected void transformImpl(CharSequence text, Appendable out) {
            replace(out, text, operator);
        }

    }

    public static CharBasedFilteringTextMapper<StringBuilder,String> createDefaultInstance(CharSequence str) {
        return createInstance(str, null, ON_DEMAND_STRING_BUILDER_WRITER, CharBasedFilteringTextMapper::createBuilder);
    }

    public static CharBasedFilteringTextMapper<PrintWriter,Void> createInstanceForPrint(PrintWriter out, CharSequence str) {
        return createInstance(str, out, PRINT_WRITER_RESULT_WRITER, null);
    }

    public static CharBasedFilteringTextMapper<StringBuilder,Void> createInstanceForAppend(StringBuilder buff, CharSequence str) {
        return createInstance(str, buff, EXISTING_STRING_BUILDER_WRITER, null);
    }

    public static CharBasedFilteringTextMapper<? extends Appendable,Void> createInstanceForGenericAppend(Appendable out, CharSequence str) {
        if (out instanceof PrintWriter pw) {
            return createInstanceForPrint(pw, str);
        }
        if (out instanceof StringBuilder buff) {
            return createInstanceForAppend(buff, str);
        }
        return createInstance(str, out, GENERIC_APPENDABLE_RESULT_WRITER, null);
    }

    private static <T,R> CharBasedFilteringTextMapper<T,R> createInstance(CharSequence str, T out, ResultWriter<T,R> writer, Creator<T> creator) {
        Objects.requireNonNull(str, "input string");
        return new CharBasedFilteringTextMapper<>(str, out, writer, creator);
    }

    public static String removeIf(CharSequence str, CharMatcher filter) {
        return removeIf(createDefaultInstance(str), filter);
    }

    public static void removeIf(PrintWriter out, CharSequence str, CharMatcher filter) {
        removeIf(createInstanceForPrint(out, str), filter);
    }

    public static void removeIf(StringBuilder buff, CharSequence str, CharMatcher filter) {
        removeIf(createInstanceForAppend(buff, str), filter);
    }

    public static void removeIf(Appendable out, CharSequence str, CharMatcher filter) {
        removeIf(createInstanceForGenericAppend(out, str), filter);
    }

    public static String retainIf(CharSequence str, CharMatcher filter) {
        return retainIf(createDefaultInstance(str), filter);
    }

    public static void retainIf(PrintWriter out, CharSequence str, CharMatcher filter) {
        retainIf(createInstanceForPrint(out, str), filter);
    }

    public static void retainIf(StringBuilder buff, CharSequence str, CharMatcher filter) {
        retainIf(createInstanceForAppend(buff, str), filter);
    }

    public static void retainIf(Appendable out, CharSequence str, CharMatcher filter) {
        retainIf(createInstanceForGenericAppend(out, str), filter);
    }

    public static String replace(CharSequence str, StringUtil.CharMapper replacer) {
        return replace(createDefaultInstance(str), replacer);
    }

    public static String replace(CharSequence str, StringUtil.CharToStringMapper replacer) {
        return replace(createDefaultInstance(str), replacer);
    }

    public static void replace(PrintWriter out, CharSequence str, StringUtil.CharMapper replacer) {
        replace(createInstanceForPrint(out, str), replacer);
    }

    public static void replace(PrintWriter out, CharSequence str, StringUtil.CharToStringMapper replacer) {
        replace(createInstanceForPrint(out, str), replacer);
    }

    public static void replace(StringBuilder out, CharSequence str, StringUtil.CharMapper replacer) {
        replace(createInstanceForAppend(out, str), replacer);
    }

    public static void replace(StringBuilder out, CharSequence str, StringUtil.CharToStringMapper replacer) {
        replace(createInstanceForAppend(out, str), replacer);
    }

    public static void replace(Appendable out, CharSequence str, StringUtil.CharMapper replacer) {
        replace(createInstanceForGenericAppend(out, str), replacer);
    }

    public static void replace(Appendable out, CharSequence str, StringUtil.CharToStringMapper replacer) {
        replace(createInstanceForGenericAppend(out, str), replacer);
    }

    private static StringBuilder createBuilder(CharSequence original, int currentIndex) {
        return new StringBuilder(original.length() + 25);
    }

    private static <R> R removeIf(CharBasedFilteringTextMapper<?,R> mapper, CharMatcher filter) {
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

    private static <R> R retainIf(CharBasedFilteringTextMapper<?,R> mapper, CharMatcher filter) {
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

    private static <R> R replace(CharBasedFilteringTextMapper<?,R> mapper, StringUtil.CharToStringMapper replacer) {
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

    private static <R> R replace(CharBasedFilteringTextMapper<?,R> mapper, StringUtil.CharMapper replacer) {
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

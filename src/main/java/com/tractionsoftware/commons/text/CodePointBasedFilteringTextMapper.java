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

import java.io.PrintWriter;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;

@Beta
public final class CodePointBasedFilteringTextMapper<T, R>
    extends FilteringTextMapper<T,R> {

    @FunctionalInterface
    private interface Creator<T> {

        T create(int[] codePoints, int nextIndex);

    }

    private static final class RemovingTransformer extends TextTransformerAdapter<IntPredicate> {

        private RemovingTransformer(IntPredicate matcher) {
            super(matcher);
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

    private static final class RetainingTransformer extends TextTransformerAdapter<IntPredicate> {

        private RetainingTransformer(IntPredicate matcher) {
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

    private static final class CodePointMappingTransformer extends TextTransformerAdapter<StringUtil.CodePointMapper> {

        CodePointMappingTransformer(StringUtil.CodePointMapper replacer) {
            super(replacer);
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

    private static final class CodePointToStringMappingTransformer
        extends TextTransformerAdapter<IntFunction<CharSequence>> {

        CodePointToStringMappingTransformer(IntFunction<CharSequence> replacer) {
            super(replacer);
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

    public static CodePointBasedFilteringTextMapper<StringBuilder,String> createDefaultInstance(CharSequence str) {
        return createInstance(
            str,
            null,
            ON_DEMAND_STRING_BUILDER_WRITER,
            CodePointBasedFilteringTextMapper::createBuilder
        );
    }

    public static CodePointBasedFilteringTextMapper<PrintWriter,Void> createInstanceForPrint(PrintWriter out, CharSequence str) {
        return createInstance(str, out, PRINT_WRITER_RESULT_WRITER, null);
    }

    public static CodePointBasedFilteringTextMapper<StringBuilder,Void> createInstanceForAppend(StringBuilder buff, CharSequence str) {
        return createInstance(str, buff, EXISTING_STRING_BUILDER_WRITER, null);
    }

    public static CodePointBasedFilteringTextMapper<? extends Appendable,Void> createInstanceForGenericAppend(Appendable out, CharSequence str) {
        if (out instanceof PrintWriter pw) {
            return createInstanceForPrint(pw, str);
        }
        if (out instanceof StringBuilder buff) {
            return createInstanceForAppend(buff, str);
        }
        return createInstance(str, out, GENERIC_APPENDABLE_RESULT_WRITER, null);
    }

    private static <T, R> CodePointBasedFilteringTextMapper<T,R> createInstance(CharSequence str, T out, ResultWriter<T,R> writer, Creator<T> creator) {
        Objects.requireNonNull(str, "input string");
        return new CodePointBasedFilteringTextMapper<>(str, str.codePoints().toArray(), out, writer, creator);
    }

    public static String removeIf(CharSequence str, IntPredicate filter) {
        return removeIf(createDefaultInstance(str), filter);
    }

    public static void removeIf(PrintWriter out, CharSequence str, IntPredicate filter) {
        removeIf(createInstanceForPrint(out, str), filter);
    }

    public static void removeIf(StringBuilder buff, CharSequence str, IntPredicate filter) {
        removeIf(createInstanceForAppend(buff, str), filter);
    }

    public static void removeIf(Appendable out, CharSequence str, IntPredicate filter) {
        removeIf(createInstanceForGenericAppend(out, str), filter);
    }

    public static String retainIf(CharSequence str, IntPredicate filter) {
        return retainIf(createDefaultInstance(str), filter);
    }

    public static void retainIf(PrintWriter out, CharSequence str, IntPredicate filter) {
        retainIf(createInstanceForPrint(out, str), filter);
    }

    public static void retainIf(StringBuilder buff, CharSequence str, IntPredicate filter) {
        retainIf(createInstanceForAppend(buff, str), filter);
    }

    public static void retainIf(Appendable out, CharSequence str, IntPredicate filter) {
        retainIf(createInstanceForGenericAppend(out, str), filter);
    }

    public static String replace(CharSequence str, StringUtil.CodePointMapper replacer) {
        return replace(createDefaultInstance(str), replacer);
    }

    public static String replace(CharSequence str, IntFunction<CharSequence> replacer) {
        return replace(createDefaultInstance(str), replacer);
    }

    public static void replace(PrintWriter out, CharSequence str, IntFunction<CharSequence> replacer) {
        replace(createInstanceForPrint(out, str), replacer);
    }

    public static void replace(StringBuilder buff, CharSequence str, IntFunction<CharSequence> replacer) {
        replace(createInstanceForAppend(buff, str), replacer);
    }

    public static void replace(Appendable out, CharSequence str, IntFunction<CharSequence> replacer) {
        replace(createInstanceForGenericAppend(out, str), replacer);
    }

    public static void replace(PrintWriter out, CharSequence str, StringUtil.CodePointMapper replacer) {
        replace(createInstanceForPrint(out, str), replacer);
    }

    public static void replace(StringBuilder buff, CharSequence str, StringUtil.CodePointMapper replacer) {
        replace(createInstanceForAppend(buff, str), replacer);
    }

    public static void replace(Appendable out, CharSequence str, StringUtil.CodePointMapper replacer) {
        replace(createInstanceForGenericAppend(out, str), replacer);
    }

    public static TextTransformer createRemovingTransformer(IntPredicate filter) {
        Objects.requireNonNull(filter, "filter");
        return new RemovingTransformer(filter);
    }

    public static TextTransformer createRetainingTransformer(IntPredicate matcher) {
        Objects.requireNonNull(matcher, "matcher");
        return new RetainingTransformer(matcher);
    }

    public static TextTransformer createReplacingTransformer(StringUtil.CodePointMapper mapper) {
        Objects.requireNonNull(mapper, "mapper");
        return new CodePointMappingTransformer(mapper);
    }

    public static TextTransformer createReplacingTransformer(IntFunction<CharSequence> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        return new CodePointToStringMappingTransformer(mapper);
    }

    private static StringBuilder createBuilder(int[] codePoints, int currentIndex) {
        return new StringBuilder(codePoints.length + (codePoints.length / 2));
    }

    private static <R> R removeIf(CodePointBasedFilteringTextMapper<?,R> mapper, IntPredicate filter) {
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

    private static <R> R retainIf(CodePointBasedFilteringTextMapper<?,R> mapper, IntPredicate filter) {
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

    private static <R> R replace(CodePointBasedFilteringTextMapper<?,R> mapper, IntFunction<CharSequence> replacer) {
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

    private static <R> R replace(CodePointBasedFilteringTextMapper<?,R> mapper, StringUtil.CodePointMapper replacer) {
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

    public CodePointValue next() {
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

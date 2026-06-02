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
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import com.tractionsoftware.commons.io.StringWriteUtil;
import com.tractionsoftware.commons.text.CharBasedFilteringTextMapper;
import com.tractionsoftware.commons.text.CodePointBasedFilteringTextMapper;
import com.tractionsoftware.commons.text.StringSplitUtil;
import com.tractionsoftware.commons.util.ArraysUtil;
import com.tractionsoftware.commons.util.CollectionsUtil;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Helpers for {@link String}s and {@link CharSequence}s.
 *
 * @author Andy Keller, Dave Shepperton
 */
public final class StringUtil {

    /*
     * Not instantiable.
     */
    private StringUtil() {
    }

    public static final String PALETTE_ALPHA_NUMERIC =
        "ABCDEFGHIJIKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";

    public static final String ELLIPSES_MULTI_CHARACTER = "...";

    public static final String ELLIPSES_SINGLE_CHARACTER = "…";

    public static final Joiner JOINER_SPACE = Joiner.on(' ').skipNulls();

    public static final Joiner JOINER_COMMA = Joiner.on(',').skipNulls();

    public static final Joiner JOINER_COMMA_WITH_SPACE = Joiner.on(", ").skipNulls();

    public static final Joiner JOINER_NO_SEPARATOR = Joiner.on("").skipNulls();

    public static final Joiner JOINER_AMPERSAND = Joiner.on('&').skipNulls();

    public static final Joiner JOINER_INFIX_OR = Joiner.on(" OR ").skipNulls();

    public static final Joiner JOINER_INFIX_AND = Joiner.on(" AND ").skipNulls();

    public static final Joiner JOINER_SLASH = Joiner.on("/").skipNulls();

    public static final Joiner JOINER_BACKSLASH = Joiner.on("\\").skipNulls();

    private static final Random random = new SecureRandom();

    /**
     * The Unicode non-breaking space (U+00A0), which is not considered whitespace by
     * {@link Character#isWhitespace(char)}. See {@link #isAlternativeWhitespaceChar(char)}.
     */
    public static final char CHAR_NON_BREAKING_SPACE = '\u00a0';

    /**
     * The zero-width space (U+200B), which is not considered whitespace by {@link Character#isWhitespace(char)}. See
     * {@link #isAlternativeWhitespaceChar(char)}.
     */
    public static final char CHAR_ZERO_WIDTH_SPACE = '\u200b';

    public static final CharSequence LINE_BREAKS = EnhancedCharSequence.getInstance(new char[] { '\n', '\r' });

    public static final CharMatcher LINE_BREAK_MATCHER = CharMatcher.anyOf(LINE_BREAKS);

    /**
     * Matches either {@code \n} followed by an optional {@code \r}, or {@code \r} followed by an optional {@code \n}.
     */
    public static final Pattern LINE_BREAK_PATTERN = Pattern.compile("(\\n\\r?|\\r\\n?)");

    /**
     * Splits along any line breaks per {@link #LINE_BREAK_PATTERN}, producing results that may be blank or empty.
     */
    public static final Splitter LINE_SPLITTER = Splitter.on(LINE_BREAK_PATTERN);

    /**
     * Splits along any line break characters ({@code \n} or {@code \r}), and produces results that are trimmed and
     * non-empty.
     */
    public static final Splitter TRIMMED_NON_EMPTY_LINE_SPLITTER = Splitter.on(LINE_BREAK_PATTERN)
        .trimResults()
        .omitEmptyStrings();

    /**
     * Splits along any whitespace characters, and produces results that are trimmed and non-empty.
     */
    public static final Splitter TRIMMED_NON_EMPTY_PART_SPLITTER = Splitter.on(CharMatcher.whitespace())
        .trimResults()
        .omitEmptyStrings();

    public static final Splitter TRIMMED_NON_EMPTY_LIST_SPLITTER = Splitter
        .on(StringSplitUtil.DEFAULT_STRING_LIST_SEPARATOR_CHAR)
        .trimResults()
        .omitEmptyStrings();

    public static final Comparator<String> SAFE_CASE_INSENSITIVE_ORDER =
        Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER);

    public interface TextEnclosureScheme {

        char start();

        char end();

        default boolean matches(char start, char end) {
            if (start == start() && end == end()) {
                return true;
            }
            return false;
        }

    }

    public enum StandardTextEnclosureScheme implements TextEnclosureScheme {

        TAG_STYLE('<', '>'),

        SINGLE_QUOTATION_MARKS('\''),

        DOUBLE_QUOTATION_MARKS('"'),

        PARENTHESES('(', ')'),

        BRACKETS('[', ']'),

        BRACES('{', '}');

        private final char start;

        private final char end;

        StandardTextEnclosureScheme(char startAndEnd) {
            this(startAndEnd, startAndEnd);
        }

        StandardTextEnclosureScheme(char start, char end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public final char start() {
            return start;
        }

        @Override
        public final char end() {
            return end;
        }

    }

    /**
     * Used in the implementation of the {@link #collapseAndNormalizeWhitespace(String, boolean)} method.
     */
    private enum CollapsibleOrNormalizableWhitespaceScanningState {

        NO_WHITESPACE(false),

        SINGLE_REGULAR_SPACE(false),

        COLLAPSIBLE_OR_NORMALIZABLE_WHITESPACE(true);

        private final boolean collapsible;

        CollapsibleOrNormalizableWhitespaceScanningState(boolean collapsible) {
            this.collapsible = collapsible;
        }

        public boolean isCollapsible() {
            return collapsible;
        }

    }

    @FunctionalInterface
    public interface CodePointMapper {

        int getReplacement(int codePoint);

    }

    @FunctionalInterface
    public interface CharToStringMapper {

        CharSequence getReplacement(char c);

    }

    @FunctionalInterface
    public interface CharMapper {

        char getReplacement(char c);

    }

    public interface IndexRange {

        int start();

        int end();

        int length();

        default boolean is(int start, int end) {
            if (start() == start && end() == end) {
                return true;
            }
            return false;
        }

        default boolean is(IndexRange range) {
            if (start() == range.start() && end() == range.end()) {
                return true;
            }
            return false;
        }

        default boolean isEmpty() {
            return (length() == 0);
        }

        default CharSequence apply(CharSequence sequence) {
            if (isEmpty()) {
                return StringUtils.EMPTY;
            }
            if (start() == 0 && end() == sequence.length()) {
                return sequence.toString();
            }
            return sequence.subSequence(start(), end()).toString();
        }

        default String applyAsString(CharSequence sequence) {
            return apply(sequence).toString();
        }

        default void append(StringBuilder buff, CharSequence sequence) {
            if (isEmpty()) {
                return;
            }
            if (start() == 0 && end() == sequence.length()) {
                buff.append(sequence);
                return;
            }
            buff.append(sequence, start(), end());
        }

        default void print(PrintWriter out, CharSequence sequence) {
            if (isEmpty()) {
                return;
            }
            if (start() == 0 && end() == sequence.length()) {
                out.print(sequence);
                return;
            }
            if (sequence instanceof String s) {
                // Fastest way to do this for strings.
                out.write(s, start(), length());
            }
            else {
                out.append(sequence, start(), end());
            }
        }

        default void append(Appendable out, CharSequence sequence) throws IOException {
            if (isEmpty()) {
                return;
            }
            if (start() == 0 && end() == sequence.length()) {
                out.append(sequence);
                return;
            }
            out.append(sequence, start(), end());
        }

    }

    public interface AnalyzableIndexRange extends IndexRange {

        boolean containsNonBmpCodePoints();

    }

    public static final class SimpleImmutableIndexRange implements IndexRange {

        public static final SimpleImmutableIndexRange EMPTY = new SimpleImmutableIndexRange(0, 0);

        public static SimpleImmutableIndexRange getInstance(int start, int end) {
            if (start < 0) {
                throw new IndexOutOfBoundsException("start " + start + " < 0");
            }
            if (end < 0) {
                throw new IndexOutOfBoundsException("end " + end + " < start " + start);
            }
            if (start == 0 && end == 0) {
                return EMPTY;
            }
            return new SimpleImmutableIndexRange(start, end);
        }

        private final int start;

        private final int end;

        private SimpleImmutableIndexRange(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public int start() {
            return start;
        }

        @Override
        public int end() {
            return end;
        }

        @Override
        public int length() {
            return end - start;
        }

        @Override
        public CharSequence apply(CharSequence sequence) {
            if (start == end) {
                return StringUtils.EMPTY;
            }
            if (start == 0 && end == sequence.length()) {
                return sequence;
            }
            return sequence.subSequence(start, end);
        }

        @Override
        public void append(StringBuilder buff, CharSequence sequence) {
            if (isEmpty()) {
                return;
            }
            if (start == 0 && end == sequence.length()) {
                buff.append(sequence);
                return;
            }
            buff.append(sequence, start, end);
        }

        @Override
        public void print(PrintWriter out, CharSequence sequence) {
            if (isEmpty()) {
                return;
            }
            if (start == 0 && end == sequence.length()) {
                out.print(sequence);
                return;
            }
            if (sequence instanceof String s) {
                // Fastest way to do this for strings.
                out.write(s, start, length());
            }
            else {
                out.append(sequence, start, end);
            }
        }

        @Override
        public void append(Appendable out, CharSequence sequence) throws IOException {
            if (isEmpty()) {
                return;
            }
            if (start == 0 && end == sequence.length()) {
                out.append(sequence);
                return;
            }
            out.append(sequence, start, end);
        }

    }

    public static abstract class AbstractGrowableIndexRange implements AnalyzableIndexRange {

        /**
         * The start index, inclusive.
         */
        private final int start;

        /**
         * The end index, exclusive.
         */
        private int end;

        private boolean containsNonBmpCodePoints;

        /**
         * Constructs an empty range starting and ending at the given index.
         *
         * @param index
         *     the start and end index for the empty range.
         */
        public AbstractGrowableIndexRange(int index) {
            this(index, index, false);
        }

        /**
         * Initializes the range bounds and status used for {@link #containsNonBmpCodePoints()}. It is the subclass's
         * responsibility to check the bounds of the range for the start and end indices.
         *
         * @param start
         *     the start index of the range.
         * @param end
         *     the end index of the range.
         * @param containsNonBmpCodePoints
         *     whether the range covered by the given indices is known to contain any non-BMP code points.
         */
        protected AbstractGrowableIndexRange(int start, int end, boolean containsNonBmpCodePoints) {
            this.start = start;
            this.end = end;
            this.containsNonBmpCodePoints = containsNonBmpCodePoints;
        }

        @Override
        public final boolean equals(Object other) {
            if (!(other instanceof AbstractGrowableIndexRange otherRange)) {
                return false;
            }
            if (start == otherRange.start &&
                end == otherRange.end &&
                containsNonBmpCodePoints == otherRange.containsNonBmpCodePoints) {
                return true;
            }
            return false;
        }

        @Override
        public final int hashCode() {
            return Objects.hash(start, end, containsNonBmpCodePoints);
        }

        @Override
        public final String toString() {
            if (start == end) {
                return "[" + start + "]";
            }
            return "[" + start + "-" + end + "]";
        }

        @Override
        public final int start() {
            return start;
        }

        @Override
        public final int end() {
            return end;
        }

        @Override
        public final int length() {
            return end - start;
        }

        @Override
        public final boolean containsNonBmpCodePoints() {
            return containsNonBmpCodePoints;
        }

        protected final void expandImpl() {
            end++;
        }

        protected final void expandImpl(int bySize) {
            end += bySize;
        }

        protected final void setContainsNonBmpCodePoints() {
            if (!containsNonBmpCodePoints) {
                containsNonBmpCodePoints = true;
            }
        }

        protected final void setContainsNonBmpCodePoint(BooleanSupplier isNonBmpCodePoint) {
            if (!containsNonBmpCodePoints) {
                containsNonBmpCodePoints = isNonBmpCodePoint.getAsBoolean();
            }
        }

    }

    @FunctionalInterface
    private interface KnownLengthCharSequenceCharExtractor {

        char getChar(CharSequence str, int len);

    }

    @FunctionalInterface
    private interface KnownLengthStringCodePointExtractor {

        int getCodePoint(String str, int len);

    }

    @FunctionalInterface
    private interface CharIndexFinder {

        int indexOf(String str, char c, int startIndex);

    }

    private enum SpecialPosition {

        FIRST(
            (str, _) -> str.charAt(0),
            (str, _) -> str.codePointAt(0)
        ),

        LAST(
            (str, len) -> str.charAt(len - 1),
            String::codePointBefore
        );

        private final KnownLengthCharSequenceCharExtractor charExtractor;

        private final KnownLengthStringCodePointExtractor codePointExtractor;

        SpecialPosition(KnownLengthCharSequenceCharExtractor charExtractor, KnownLengthStringCodePointExtractor codePointExtractor) {
            this.charExtractor = charExtractor;
            this.codePointExtractor = codePointExtractor;
        }

        public final char getChar(CharSequence seq, int len) {
            return charExtractor.getChar(seq, len);
        }

        public final int getCodePoint(String str, int len) {
            return codePointExtractor.getCodePoint(str, len);
        }

    }

    private static final record TruncatedToString(Object object, int maxLength, String ellipses) {

        @Nonnull
        @Override
        public final String toString() {
            return truncate(ObjectUtil.safeToString(object, "?"), maxLength, ellipses);
        }

    }

    public static Joiner getNullSkippingJoiner(char separator) {
        return switch (separator) {
            case ',' -> JOINER_COMMA;
            case ' ' -> JOINER_SPACE;
            case '&' -> JOINER_AMPERSAND;
            case '/' -> JOINER_SLASH;
            case '\\' -> JOINER_BACKSLASH;
            default -> Joiner.on(separator).skipNulls();
        };
    }

    public static Joiner getNullSkippingJoiner(String separator) {
        return switch (separator) {
            case "," -> JOINER_COMMA;
            case ", " -> JOINER_COMMA_WITH_SPACE;
            case " " -> JOINER_SPACE;
            case "&" -> JOINER_AMPERSAND;
            case " OR " -> JOINER_INFIX_OR;
            case " AND " -> JOINER_INFIX_AND;
            case "/" -> JOINER_SLASH;
            case "\\" -> JOINER_BACKSLASH;
            case "" -> JOINER_NO_SEPARATOR;
            case null -> JOINER_NO_SEPARATOR;
            default -> Joiner.on(separator).skipNulls();
        };
    }

    /**
     * Join the elements of a {@link Stream} using the given separator character. null values are skipped.
     */
    public static final String join(Stream<?> elements, char separator) {
        if (elements == null) {
            return "";
        }
        return getNullSkippingJoiner(separator).join(elements.iterator());
    }

    /**
     * Join the elements of a {@link Stream} using the given separator {@link String}. null values are skipped, and if
     * the join separator is null, the empty String will be used instead.
     */
    public static final String join(Stream<?> elements, String separator) {
        if (elements == null) {
            return "";
        }
        return getNullSkippingJoiner(separator).join(elements.iterator());
    }

    /**
     * Join the elements of a {@link Iterator} using the given separator character. null values are skipped.
     */
    public static String join(Iterator<?> coll, char separator) {
        if (coll == null) {
            return "";
        }
        return getNullSkippingJoiner(separator).join(coll);
    }

    /**
     * Join the elements of a {@link Iterator} using the given separator {@link String}. null values are skipped, and if
     * the join separator is null, the empty String will be used instead.
     */
    public static String join(Iterator<?> coll, String separator) {
        if (coll == null) {
            return "";
        }
        return getNullSkippingJoiner(separator).join(coll);
    }

    /**
     * Join the elements of a {@link Iterable} using the given separator character. null values are skipped.
     */
    public static String join(Iterable<?> coll, char separator) {
        if (coll == null) {
            return "";
        }
        return getNullSkippingJoiner(separator).join(coll);
    }

    /**
     * Join the elements of a {@link Iterable} using the given separator {@link String}. null values are skipped, and if
     * the join separator is null, the empty String will be used instead.
     */
    public static String join(Iterable<?> coll, String separator) {
        if (coll == null) {
            return "";
        }
        return getNullSkippingJoiner(separator).join(coll);
    }

    /**
     * Join the elements of an array using the given separator.
     */
    public static String join(String[] arr, char separator) {
        if (arr == null) {
            return "";
        }
        return join(ArraysUtil.asList(arr), separator);
    }

    /**
     * Join the elements of an array using the given separator {@link String}.
     */
    public static String join(String[] arr, String separator) {
        if (arr == null) {
            return "";
        }
        return join(ArraysUtil.asList(arr), separator);
    }

    /**
     * A method similar to {@link StringUtils#abbreviate(String, int)}, but with some slight variation in behavior,
     * including tolerance of apparently invalid input parameters. This version uses empty ellipses.
     *
     * <p>
     * Note that this truncation also normalizes and collapses whitespace, per
     * {@link StringUtil#collapseAndNormalizeWhitespace(String, boolean)}.
     *
     * @param text
     *     the text to be truncated.
     * @param maximumLength
     *     the maximum length of the truncated output, including the space for the ellipses.
     * @return a truncated version of the input text which is as long as possible while still fitting into the requested
     *     maximum length.
     */
    public static final String truncate(String text, int maximumLength) {
        return truncate(text, maximumLength, "");
    }

    /**
     * A method similar to {@link StringUtils#abbreviate(String, int)}, but with some slight variation in behavior,
     * including tolerance of apparently invalid input parameters, and the ability to specify custom text for the
     * ellipses.
     *
     * <p>
     * Note that this truncation also normalizes and collapses whitespace, per
     * {@link StringUtil#collapseAndNormalizeWhitespace(String, boolean)}.
     *
     * @param text
     *     the text to be truncated. This really should be plain text (not HTML or even text with HTML entities), since
     *     this method does not do any of the parsing or tokenization that would be required for proper handling of
     *     HTML.
     * @param maximumLength
     *     the maximum length of the truncated output, including the space for the ellipses.
     * @param ellipses
     *     to use at the end of truncated output if necessary.
     * @return a truncated version of the input text which is as long as possible while still fitting into the requested
     *     maximum length, including any ellipses.
     */
    public static final String truncate(String text, int maximumLength, String ellipses) {
        return truncate(text, maximumLength, Suppliers.ofInstance(ellipses));
    }

    /**
     * A method similar to {@link StringUtils#abbreviate(String, int)}, but with some slight variation in behavior,
     * including tolerance of apparently invalid input parameters, and the ability to specify custom text for the
     * ellipses which is only retrieved when needed.
     *
     * <p>
     * Note that this truncation also normalizes and collapses whitespace, per
     * {@link StringUtil#collapseAndNormalizeWhitespace(String, boolean)}.
     *
     * @param text
     *     the text to be truncated. This really should be plain text (not HTML or even text with HTML entities), since
     *     this method does not do any of the parsing or tokenization that would be required for proper handling of
     *     HTML.
     * @param requestedMaximumLength
     *     the maximum length of the truncated output, including the space for the ellipses.
     * @param ellipsesProvider
     *     supplies the ellipses to use at the end of truncated output if necessary.
     * @return a truncated version of the input text which is as long as possible while still fitting into the requested
     *     maximum length, including any ellipses.
     */
    public static final String truncate(String text, int requestedMaximumLength, Supplier<String> ellipsesProvider) {

        if (requestedMaximumLength <= 0) {
            return "";
        }

        text = StringUtil.collapseAndNormalizeWhitespace(text, true);
        if (StringUtils.isEmpty(text)) {
            return "";
        }

        if (text.length() <= requestedMaximumLength) {
            return text;
        }

        String ellipses = StringUtils.defaultString(ellipsesProvider.get());
        int ellipsesLen = ellipses.length();
        if (ellipsesLen == requestedMaximumLength) {
            return ellipses;
        }
        if (ellipsesLen > requestedMaximumLength) {
            return ellipses.substring(requestedMaximumLength);
        }
        return text.substring(0, requestedMaximumLength - ellipses.length()) + ellipses;

    }

    /**
     * Truncates the given String such that the encoded bytes for the requested {@link Charset} will not exceed the
     * requested maximum length, adding the given ellipses if necessary. The idea is that the bytes required to encode
     * the resulting String in that Charset will definitely fit into the maximum byte length, including the ellipses.
     *
     * <p>
     * Unfortunately this requires scanning through the String, char value by char value. This is to ensure that all the
     * bytes required to encode the String will be properly counted; and also that the truncation doesn't happen between
     * two of the bytes required to represent a single logical character.
     *
     * @param text
     *     the String to truncate.
     * @param maximumByteSize
     *     the maximum number of bytes that are available to encode the given String using the given Charset.
     * @param charset
     *     an optional custom {@link Charset} to be used to encode the String. If the argument for this parameter is
     *     null, the method will use {@link StandardCharsets#UTF_8 UTF-8}.
     * @param ellipses
     *     the ellipses, if any, that should be appended to the resulting String at the point of truncation, if
     *     accommodated by the requested maximum byte length.
     * @return the given String such that the encoded bytes for the requested {@link Charset} will not exceed the
     *     requested maximum length, adding the given ellipses if necessary.
     */
    public static final String truncateEncodedBytes(String text, int maximumByteSize, Charset charset, String ellipses) {

        if (text == null) {
            return null;
        }

        if (maximumByteSize <= 0) {
            return "";
        }

        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        ellipses = StringUtils.defaultString(ellipses);

        int byteLen = 0;

        int ellipsesLen = ellipses.getBytes(charset).length;
        if (ellipsesLen > maximumByteSize) {
            ellipses = "";
            ellipsesLen = 0;
        }

        int totalMax = maximumByteSize - ellipsesLen;
        StringBuilder result = new StringBuilder(maximumByteSize);
        PrimitiveIterator.OfInt points = text.codePoints().iterator();

        while (points.hasNext()) {

            char[] oneCharacter = Character.toChars(points.nextInt());
            int characterByteLen = String.valueOf(oneCharacter).getBytes(charset).length;
            if (byteLen + characterByteLen > totalMax) {
                result.append(ellipses);
                // Un-comment this to do something with the total length outside of this loop.
//                byteLen += ellipsesLen;
                break;
            }

            byteLen += characterByteLen;
            result.append(oneCharacter);

        }

        return result.toString();

    }

    public static final Object truncatedToStringForLog(Object object) {
        return truncatedToStringForLog(object, 50);
    }

    public static final Object truncatedToStringForLog(Object object, int maxLength) {
        return truncatedToStringForLog(object, maxLength, ELLIPSES_MULTI_CHARACTER);
    }

    public static final Object truncatedToStringForLog(Object object, int maxLength, String ellipses) {
        return new TruncatedToString(object, maxLength, ellipses);
    }

    public static String findReplace(String str, String find, String replace) {
        if (Objects.equals(find, replace)) {
            return str;
        }
        return Strings.CS.replace(str, find, replace);
    }

    public static boolean isAsciiDigit(char c) {
        return switch (c) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> true;
            default -> false;
        };
    }

    public static boolean isNonNegativeNumber(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (!isAsciiDigit(str.charAt(0))) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsIgnoreCase(Collection<String> coll, String searchVal) {
        if (CollectionsUtil.isEmpty(coll)) {
            return false;
        }
        return coll.stream().anyMatch(str -> Strings.CI.equals(searchVal, str));
    }

    /**
     * Checks whether the given sequence contains any of the given search characters, starting at the given index. This
     * method is identical to {@link StringUtils#containsAny(CharSequence, CharSequence)}, but with the option of
     * specifying the starting index for the search.
     *
     * @param str
     *     the sequence in which to search.
     * @param searchChars
     *     the characters to search for.
     * @param fromIndex
     *     the starting index for the search, inclusive.
     * @return true if the given sequence contains any of the given search characters, starting at the given index;
     *     false otherwise.
     */
    public static boolean containsAny(CharSequence str, CharSequence searchChars, int fromIndex) {
        if (indexOfAny(str, searchChars, fromIndex) == StringUtils.INDEX_NOT_FOUND) {
            return false;
        }
        return true;
    }

    /**
     * Returns the first index of any of the specified characters. This method is similar to
     * {@link StringUtils#indexOfAny(CharSequence, char...)}, but with the option of specifying the starting index for
     * the search.
     *
     * @param str
     *     the sequence in which to search.
     * @param searchChars
     *     the characters to search for.
     * @param fromIndex
     *     the starting index for the search, inclusive.
     * @return the first index of any of the specified characters, starting at the given index, if any of them are
     *     present; -1 otherwise.
     */
    public static int indexOfAny(CharSequence str, CharSequence searchChars, int fromIndex) {

        if (StringUtils.isEmpty(str) || StringUtils.isEmpty(searchChars)) {
            return StringUtils.INDEX_NOT_FOUND;
        }

        int len = str.length();
        if (fromIndex >= len) {
            return StringUtils.INDEX_NOT_FOUND;
        }

        if (fromIndex < 0) {
            fromIndex = 0;
        }

        if (hasNonBmpCodePoints(searchChars)) {
            IntPredicate matcher = codePoint -> ArrayUtils.contains(searchChars.codePoints().toArray(), codePoint);
            int[] codePoints = str.codePoints().skip(fromIndex).toArray();
            for (int i = 0; i < codePoints.length; i++) {
                if (matcher.test(codePoints[i])) {
                    return i;
                }
            }
        }
        else {
            CharMatcher matcher = CharMatcher.anyOf(searchChars);
            for (int i = fromIndex; i < str.length(); i++) {
                if (matcher.matches(str.charAt(i))) {
                    return i;
                }
            }
        }

        return StringUtils.INDEX_NOT_FOUND;

    }

    /**
     * This isn't particularly efficient, but for our current usage, it's fine.
     *
     * <p>
     * Similar to {@link StringUtils#indexOfAny(CharSequence, CharSequence...)}, but with an offset index indicating
     * where to start the search in the subject String.
     *
     * @author [ajm 16.Jun.2010]
     */
    public static int indexOfAny(String str, String[] find, int fromIndex) {

        if (fromIndex < 0) {
            fromIndex = 0;
        }

        int min = Integer.MAX_VALUE;

        for (String f : find) {
            int index = str.indexOf(f, fromIndex);
            if (0 <= index && index < min) {
                min = index;
            }
        }

        if (min > str.length()) {
            min = -1;
        }

        return min;
    }

    /**
     * Returns true if the given string contains any non-BMP code points.
     *
     * @param str
     *     the string to examine.
     * @return true if the given string contains any non-BMP code points; false otherwise.
     * @see #isNotBmpCodePoint(int)
     */
    public static boolean hasNonBmpCodePoints(CharSequence str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        return str.codePoints().anyMatch(StringUtil::isNotBmpCodePoint);
    }

    /**
     * Removes the given code point from the given String. This method is a more efficient variation of
     * {@link StringUtils#remove(String, char)} which handles non-BMP code points.
     *
     * @param str
     *     the string from which the character should be removed.
     * @param remove
     *     the character to remove.
     * @return a String with all occurrences of the given code point removed, if any appear; the same String otherwise.a
     * @see #remove(String, char)
     */
    public static final String remove(String str, int remove) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        if (Character.isBmpCodePoint(remove)) {
            return removeImpl(str, (char) remove);
        }
        return removeImpl(str, remove);
    }

    /**
     * Removes the given char from the given String. This method is a more efficient variation of
     * {@link StringUtils#remove(String, char)}.
     *
     * @param str
     *     the string from which the character should be removed.
     * @param remove
     *     the character to remove.
     * @return a String with all occurrences of the given code point removed, if any appear; the same String otherwise.a
     */
    public static final String remove(String str, char remove) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        return removeImpl(str, remove);
    }

    /**
     * Returns a string representing the given string with all occurrences of the characters in the given remove string
     * from the subject string. This method examines the characters in the remove set to see if there are any non-BMP
     * code points, and handles the operation accordingly.
     *
     * @param str
     *     the string from which the characters are to be removed.
     * @param remove
     *     containing the characters / code points that should be removed.
     * @return a string representing the given string with all occurrences of the characters in the given remove string
     *     from the subject string.
     */
    public static String removeAll(CharSequence str, CharSequence remove) {
        if (StringUtils.isEmpty(str) || StringUtils.isEmpty(remove)) {
            return Objects.toString(str, null);
        }
        if (hasNonBmpCodePoints(remove)) {
            return CodePointBasedFilteringTextMapper.removeIf(
                str, codePoint -> ArrayUtils.contains(remove.codePoints().toArray(), codePoint)
            );
        }
        return CharBasedFilteringTextMapper.removeIf(CharMatcher.anyOf(remove), str);
    }

    /**
     * Returns true if the given integer is not a BMP code point. See {@link Character#isBmpCodePoint(int)}.
     *
     * @param codePoint
     *     the integer representing a purported code point.
     * @return true if the given integer is not a BMP code point; false otherwise.
     * @see Character#isBmpCodePoint(int)
     */
    public static boolean isNotBmpCodePoint(int codePoint) {
        return !Character.isBmpCodePoint(codePoint);
    }

    /**
     * Returns a string representing the given string with all occurrences of line breaks removed. This effectively
     * removes all line feeds and carriage returns.
     *
     * @param str
     *     the string to have its line breaks removed.
     * @return a string representing the given string with all occurrences of line breaks removed.
     */
    public static String removeLineBreaks(CharSequence str) {
        return removeAll(str, "\n\r");
    }

    public static String getTrimmedNonEmptyLinesJoinedWithSpaces(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        return join(getTrimmedNonEmptyLines(str).iterator(), ' ');
    }

    public static String lineBreaksToNewlines(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        return join(getLines(str).iterator(), '\n');
    }

    public static void randomize(CharBuffer buffer) {
        char c;
        int n;
        for (int i = buffer.position(); i < buffer.limit(); i++) {
            n = random.nextInt(buffer.length());
            c = buffer.get(n);
            buffer.put(n, buffer.get(i));
            buffer.put(i, c);
        }
    }

    public static String randomize(String str) {
        char[] ret = str.toCharArray();
        CharBuffer buffer = CharBuffer.wrap(ret);
        randomize(buffer);
        return String.valueOf(ret);
    }

    public static char getRandomCharacter(CharSequence palette) {
        return palette.charAt(random.nextInt(palette.length()));
    }

    public static String getRandomSequence(int length, CharSequence palette) {
        char[] ret = new char[length];
        CharBuffer buffer = CharBuffer.wrap(ret);
        fillRandom(palette, buffer);
        buffer.flip();
        randomize(buffer);
        return String.valueOf(ret);
    }

    public static String getRandomAlphaNumericSequence(int length) {
        return getRandomSequence(length, PALETTE_ALPHA_NUMERIC);
    }

    public static void fillRandom(CharSequence palette, CharBuffer destination) {
        fillRandom(palette, destination, destination.length());
    }

    public static void fillRandom(CharSequence palette, Appendable destination, int count) {
        for (int i = 0; i < count; i++) {
            StringWriteUtil.safeAppend(destination, StringUtil.getRandomCharacter(palette));
        }
    }

    public static int indexOfNotEscaped(String str, char ch) {
        return indexOfNotEscaped(str, ch, 0);
    }

    public static int indexOfNotEscaped(String str, char ch, int start) {
        for (int i = start; i < str.length(); i++) {
            if (str.charAt(i) == ch) {
                if (i == 0 || str.charAt(i - 1) != '\\') {
                    return i;
                }
            }
        }
        return StringUtils.INDEX_NOT_FOUND;
    }

    public static int indexOfIgnoreCase(String str, char c) {
        return indexOfIgnoreCase(str, c, 0);
    }

    public static int indexOfIgnoreCase(String str, char c, int fromIndex) {
        int len = StringUtils.length(str);
        if (len == 0 || fromIndex >= len) {
            return StringUtils.INDEX_NOT_FOUND;
        }
        return indexOfIgnoreCaseImpl(str, c, fromIndex);
    }

    public static boolean containsCharactersInOrder(String str, String search) {
        return containsCharactersInOrderImpl(str, search, String::indexOf);
    }

    public static boolean containsCharactersInOrderIgnoreCase(String str, String search) {
        return containsCharactersInOrderImpl(str, search, StringUtil::indexOfIgnoreCaseImpl);
    }

    /**
     * Returns a sequential {@link Stream} covering the lines in the input String, recognizing any standard line
     * separator pattern.
     *
     * @param str
     *     the input String.
     * @return a sequential {@link Stream} covering the lines in the input String; or an empty Stream if the input
     *     String is null or empty.
     */
    public static Stream<String> getLines(CharSequence str) {
        return split(str, LINE_SPLITTER);
    }

    /**
     * Returns a sequential {@link Stream} covering all lines in the input sequence, with each line
     * {@link String#trim() the trimmed}, and with {@link String#isBlank() blank} lines omitted.
     *
     * @param str
     *     the input sequence.
     * @return a sequential {@link Stream} covering all lines in the input sequence, with each line *
     *     {@link String#trim() the trimmed}, and with {@link String#isBlank() blank} lines omitted; or an empty Stream
     *     if the input String is null or empty.
     * @see #getLines(CharSequence)
     */
    public static Stream<String> getTrimmedNonEmptyLines(CharSequence str) {
        return split(str, TRIMMED_NON_EMPTY_LINE_SPLITTER);
    }

    public static Stream<String> getTrimmedNonEmptyListElements(CharSequence str) {
        return split(str, TRIMMED_NON_EMPTY_LIST_SPLITTER);
    }

    /**
     * Returns a sequential {@link Stream} covering all non-empty parts in the input sequence, split along whitespace,
     * with each part {@link String#trim() the trimmed}, and with {@link String#isBlank() blank} parts omitted.
     *
     * @param str
     *     the input sequence.
     * @return a sequential {@link Stream} covering all non-empty parts in the input sequence, split along whitespace, *
     *     with each part {@link String#trim() the trimmed}, and with {@link String#isBlank() blank} parts omitted; or
     *     an empty Stream if the input String is null or empty.
     * @see #getLines(CharSequence)
     */
    public static Stream<String> getTrimmedNonEmptyParts(CharSequence str) {
        return split(str, TRIMMED_NON_EMPTY_PART_SPLITTER);
    }

    /**
     * Returns a sequential {@link Stream} covering the results produced by the given {@link Splitter} as applied to the
     * given sequence.
     *
     * @param str
     *     the sequence to split.
     * @param splitter
     *     the {@link Splitter} to apply.
     * @return a sequential {@link Stream} covering the results produced by the given {@link Splitter} as applied to the
     *     given sequence; or an empty Stream if the sequence is null or empty.
     */
    public static Stream<String> split(CharSequence str, Splitter splitter) {
        if (StringUtils.isEmpty(str)) {
            return Stream.empty();
        }
        Objects.requireNonNull(splitter, "Splitter");
        return splitter.splitToStream(str);
    }

    /**
     * Tests whether a transformation could be applied to the input String to replace any runs of one or more
     * consecutive whitespace characters with a single ordinary space character.
     *
     * @param text
     *     the text to test.
     * @param alternativeWhitespace
     *     indicates whether "alternative whitespace" should be considered spaces for the purposes of this test. See
     *     {@link #isAlternativeWhitespaceChar(char)}.
     * @return true if the input text has any runs of one or more consecutive whitespace characters, if any appear and
     *     if the input text is not null; false otherwise.
     */
    public static boolean hasCollapsableOrNormalizableWhitespace(String text, boolean alternativeWhitespace) {
        if (StringUtils.isEmpty(text)) {
            return false;
        }
        if (findNextCollapsibleOrNormalizableWhitespaceBoundaries(text, alternativeWhitespace, 0) == null) {
            return false;
        }
        return true;
    }

    /**
     * Removes all whitespace characters from the given text.
     *
     * @param text
     *     the text to process.
     * @param alternativeWhitespace
     *     whether "alternative whitespace" should be considered whitespace for the purposes of removal.
     * @return the given String with all whitespace removed, if it was not already null or empty to begin with; else the
     *     supplied argument as-is.
     */
    public static final String removeWhitespace(String text, boolean alternativeWhitespace) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }
        StringBuilder ret = new StringBuilder(text.length());
        text.codePoints().filter(cp -> !isWhitespaceCodePoint(cp, alternativeWhitespace)).forEach(ret::appendCodePoint);
        return ret.toString();
    }

    /**
     * Applies a transformation to the input String that replaces any runs of one or more consecutive whitespace
     * characters with a single ordinary space character.
     *
     * <p>
     * This method is similar to {@link StringUtils#normalizeSpace(String)}, except that it doesn't trim the String.
     *
     * @param text
     *     the text whose whitespace should be collapsed and normalized.
     * @param alternativeWhitespace
     *     indicates whether "alternative whitespace" should be considered spaces for the purposes of this
     *     transformation. See {@link #isAlternativeWhitespaceChar(char)}.
     * @return the input text with any runs of one or more consecutive whitespace characters with a single ordinary
     *     space character, if any appear and if the input text is not null; otherwise, the input text as-is.
     * @implNote This code unavoidably examines every character in the input, but makes every effort not to create
     *     any objects or perform otherwise possibly unnecessary work until it identifies a run of whitespace characters
     *     in the input text that need to be collapsed or normalized.
     */
    public static String collapseAndNormalizeWhitespace(String text, boolean alternativeWhitespace) {

        if (StringUtils.isEmpty(text)) {
            return text;
        }

        int sz = text.length();
        int[] lastBoundaries = findNextCollapsibleOrNormalizableWhitespaceBoundaries(text, alternativeWhitespace, 0);
        if (lastBoundaries == null) {
            return text;
        }

        StringBuilder ret = new StringBuilder(sz);
        ret.append(text, 0, lastBoundaries[0]);
        ret.append(' ');

        int[] nextBoundaries =
            findNextCollapsibleOrNormalizableWhitespaceBoundaries(text, alternativeWhitespace, lastBoundaries[1] + 1);
        while (nextBoundaries != null) {
            ret.append(text, lastBoundaries[1] + 1, nextBoundaries[0]);
            ret.append(' ');
            lastBoundaries = nextBoundaries;
            nextBoundaries = findNextCollapsibleOrNormalizableWhitespaceBoundaries(
                text,
                alternativeWhitespace,
                lastBoundaries[1] + 1
            );
        }

        ret.append(text, lastBoundaries[1] + 1, sz);

        return ret.toString();

    }

    public static String collapseConsecutiveCharacters(String str, char c) {

        int len = StringUtils.length(str);
        if (len <= 1) {
            return str;
        }

        StringBuilder ret = null;
        int foundIndex = str.indexOf(c);
        int lastConsecutiveEnd = 0;

        while (foundIndex != -1) {

            int nextIndex = foundIndex + 1;
            while (nextIndex < len && str.charAt(nextIndex) == c) {
                nextIndex++;
            }

            int runLength = nextIndex - foundIndex;
            if (runLength > 1) {
                if (ret == null) {
                    ret = new StringBuilder(len - runLength + 1);
                }
                ret.append(str, lastConsecutiveEnd, foundIndex + 1);
                lastConsecutiveEnd = nextIndex;
            }

            foundIndex = str.indexOf(c, nextIndex);

        }

        if (ret == null) {
            return str;
        }
        if (lastConsecutiveEnd < len) {
            ret.append(str, lastConsecutiveEnd, len);
        }
        return ret.toString();

    }

    /**
     * Applies a transformation that replaces each character considered an "alternative whitespace" character with a
     * normal space character.
     *
     * <p>
     * At the time of writing, the "alternative whitespace" characters that are handled are the Unicode "non-breaking
     * space" (U+00A0) and "zero-width space" (U+200B). This may change in future.
     *
     * @param text
     *     the text whose alternative spaces should be normalized.
     * @return the input text with any "alternative whitespace" characters replaced by a normal space, if any appear and
     *     if the input text is not null; otherwise, the input text as-is.
     */
    public static String normalizeAlternativeWhitespace(String text) {

        if (StringUtils.isEmpty(text)) {
            return text;
        }

        int sz = text.length();
        int[] lastBoundaries = findNextNormalizableWhitespaceBoundaries(text, 0);
        if (lastBoundaries == null) {
            return text;
        }

        StringBuilder ret = new StringBuilder(sz);
        ret.append(text, 0, lastBoundaries[0]);
        ret.append(StringUtils.repeat(' ', lastBoundaries[1] - lastBoundaries[0] + 1));

        int[] nextBoundaries = findNextNormalizableWhitespaceBoundaries(text, lastBoundaries[1] + 1);
        while (nextBoundaries != null) {
            ret.append(text, lastBoundaries[1] + 1, nextBoundaries[0]);
            ret.append(StringUtils.repeat(' ', lastBoundaries[1] - lastBoundaries[0] + 1));
            lastBoundaries = nextBoundaries;
            nextBoundaries = findNextNormalizableWhitespaceBoundaries(text, lastBoundaries[1] + 1);
        }

        ret.append(text, lastBoundaries[1] + 1, sz);

        return ret.toString();

    }

    /**
     * Returns true if the given char is considered whitespace.
     *
     * <p>
     * This method simply returns true if {@link Character#isWhitespace(char)} returns true for the input char; or if
     * the client indicated that "alternative whitespace" characters are to be considered whitespace, returns true if
     * {@link #isAlternativeWhitespaceChar(char)} returns true for the input char; and returns false otherwise.
     *
     * @param c
     *     the char to be examined.
     * @param alternativeSpaces
     *     whether "alternative whitespace" characters, defined according to the
     *     {@link #isAlternativeWhitespaceChar(char)} method, are to be considered whitespace.
     * @return returns true if the given char is considered whitespace; false otherwise.
     */
    public static boolean isWhitespaceChar(char c, boolean alternativeSpaces) {
        if (Character.isWhitespace(c)) {
            return true;
        }
        if (alternativeSpaces && isAlternativeWhitespaceChar(c)) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the given char is considered an "alternative whitespace" character.
     *
     * <p>
     * At the time of writing, the "alternative whitespace" characters that are handled are the Unicode "non-breaking
     * space" (U+00A0) and "zero-width space" (U+200B). This may change in future.
     *
     * @param c
     *     the char to be examined.
     * @return true if the given char is considered an "alternative whitespace" character; false otherwise.
     */
    public static boolean isAlternativeWhitespaceChar(char c) {
        return switch (c) {
            case CHAR_NON_BREAKING_SPACE, CHAR_ZERO_WIDTH_SPACE -> true;
            default -> false;
        };
    }

    /**
     * Returns true if the given code point is considered whitespace.
     *
     * <p>
     * This method simply returns true if {@link Character#isWhitespace(int)} returns true for the input char; or if
     * the client indicated that "alternative whitespace" characters are to be considered whitespace, returns true if
     * {@link #isAlternativeWhitespaceChar(char)} returns true for the input char; and returns false otherwise.
     *
     * @param c
     *     the char to be examined.
     * @param alternativeSpaces
     *     whether "alternative whitespace" characters, defined according to the
     *     {@link #isAlternativeWhitespaceChar(char)} method, are to be considered whitespace.
     * @return returns true if the given char is considered whitespace; false otherwise.
     */
    public static final boolean isWhitespaceCodePoint(int c, boolean alternativeSpaces) {
        if (Character.isWhitespace(c)) {
            return true;
        }
        if (alternativeSpaces && isAlternativeWhitespaceCodePoint(c)) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the given char is considered an "alternative whitespace" character.
     *
     * <p>
     * At the time of writing, the "alternative whitespace" characters that are handled are the Unicode "non-breaking
     * space" (U+00A0) and "zero-width space" (U+200B). This may change in future.
     *
     * @param c
     *     the char to be examined, expressed as an int.
     * @return true if the given char is considered an "alternative whitespace" character; false otherwise.
     */
    public static boolean isAlternativeWhitespaceCodePoint(int c) {
        return switch (c) {
            case CHAR_NON_BREAKING_SPACE, CHAR_ZERO_WIDTH_SPACE -> true;
            default -> false;
        };
    }

    /**
     * Returns true if the given char is considered an "alternative whitespace" character.
     *
     * <p>
     * At the time of writing, the "alternative whitespace" characters that are handled are the Unicode "non-breaking
     * space" (U+00A0) and "zero-width space" (U+200B). This may change in future.
     *
     * @param str
     *     the CharSequence to be examined.
     * @return true if the given char is considered an "alternative whitespace" character; false otherwise.
     */
    public static boolean hasAlternativeWhitespaceChar(CharSequence str) {
        return str.chars().anyMatch(StringUtil::isAlternativeWhitespaceCodePoint);
    }

    public static boolean isEnclosed(String str, TextEnclosureScheme... possibleSchemes) {

        if (str == null) {
            return false;
        }

        int len = str.length();
        if (len == 0) {
            return false;
        }

        int startIndex;
        for (startIndex = 0; startIndex < len; startIndex++) {
            if (!Character.isWhitespace(str.charAt(startIndex))) {
                break;
            }
        }

        int lastIndex = len - 1;
        if (startIndex == lastIndex) {
            return false;
        }

        int endIndex;
        for (endIndex = lastIndex; endIndex > startIndex; endIndex--) {
            if (!Character.isWhitespace(str.charAt(endIndex))) {
                break;
            }
        }

        if (endIndex == startIndex) {
            return false;
        }

        char firstChar = str.charAt(startIndex);
        char lastChar = str.charAt(endIndex);
        for (TextEnclosureScheme scheme : possibleSchemes) {
            if (scheme.matches(firstChar, lastChar)) {
                return true;
            }
        }

        return false;

    }

    public static String extractEnclosed(String str, TextEnclosureScheme... possibleSchemes) {

        if (str == null) {
            return null;
        }

        int len = str.length();
        if (len == 0) {
            return str;
        }

        int startIndex;
        for (startIndex = 0; startIndex < len; startIndex++) {
            if (!Character.isWhitespace(str.charAt(startIndex))) {
                break;
            }
        }

        int lastIndex = len - 1;
        if (startIndex == lastIndex) {
            return str;
        }

        int endIndex;
        for (endIndex = lastIndex; endIndex > startIndex; endIndex--) {
            if (!Character.isWhitespace(str.charAt(endIndex))) {
                break;
            }
        }

        if (endIndex == startIndex) {
            return str;
        }

        char firstChar = str.charAt(startIndex);
        char lastChar = str.charAt(endIndex);
        for (TextEnclosureScheme scheme : possibleSchemes) {
            if (scheme.matches(firstChar, lastChar)) {
                return str.substring(startIndex + 1, endIndex);
            }
        }

        return str;

    }

    public static boolean isSingleOrDoubleQuoted(String str) {
        return isEnclosed(
            str, StandardTextEnclosureScheme.SINGLE_QUOTATION_MARKS, StandardTextEnclosureScheme.DOUBLE_QUOTATION_MARKS
        );
    }

    public static boolean isDoubleQuoted(String str) {
        return isEnclosed(str, StandardTextEnclosureScheme.DOUBLE_QUOTATION_MARKS);
    }

    public static String extractDoubleQuoted(String str) {
        return extractEnclosed(str, StandardTextEnclosureScheme.DOUBLE_QUOTATION_MARKS);
    }

    public static String extractSingleQuoted(String str) {
        return extractEnclosed(
            str, StandardTextEnclosureScheme.SINGLE_QUOTATION_MARKS, StandardTextEnclosureScheme.DOUBLE_QUOTATION_MARKS
        );
    }

    public static String extractQuotation(String str) {
        return extractEnclosed(
            str, StandardTextEnclosureScheme.SINGLE_QUOTATION_MARKS, StandardTextEnclosureScheme.DOUBLE_QUOTATION_MARKS
        );
    }

    public static Set<String> getPrefixIntersection(Set<String> some, Set<String> others) {

        ImmutableSet.Builder<String> intersection = ImmutableSet.builder();

        for (String p1 : some) {
            for (String p2 : others) {
                String commonPrefix = StringUtils.getCommonPrefix(p1, p2);
                if (!commonPrefix.isEmpty()) {
                    intersection.add(commonPrefix);
                }
            }
        }

        return intersection.build();

    }

    public static Set<String> getPrefixSpanningSet(Iterable<String> prefixes) {

        Set<String> spanning = new HashSet<>();

        for (String p1 : prefixes) {
            boolean redundant = false;
            for (String p2 : prefixes) {
                if (p1.equals(p2)) {
                    continue;
                }
                // e.g., with p1="abc", p2="ab": matches for p2
                // would always match p1, so matching p2 is
                // redundant.
                if (p1.startsWith(p2)) {
                    redundant = true;
                    break;
                }
            }
            if (!redundant) {
                spanning.add(p1);
            }
        }

        return spanning;

    }

    public static void checkNotBlankX(String str, String desc) {
        Objects.requireNonNull(str, desc);
        if (StringUtils.isBlank(str)) {
            throw new IllegalArgumentException(desc + " cannot be blank.");
        }
    }

    public static final String trimNotBlankX(String str, String desc) {
        Objects.requireNonNull(str, desc);
        str = str.trim();
        if (str.isEmpty()) {
            throw new IllegalArgumentException(desc + " cannot be blank.");
        }
        return str;
    }

    public static String toStringOrNull(Object obj) {
        return Objects.toString(obj, null);
    }

    public static boolean equalsIgnoreCaseAndWhitespace(String s1, String s2) {

        if (s1 == null) {
            if (s2 == null) {
                return true;
            }
            return false;
        }
        if (s2 == null) {
            return false;
        }

        PrimitiveIterator.OfInt s1c = s1.codePoints().filter(cp -> !Character.isWhitespace(cp)).iterator();
        PrimitiveIterator.OfInt s2c = s2.codePoints().filter(cp -> !Character.isWhitespace(cp)).iterator();
        while (s1c.hasNext() && s2c.hasNext()) {
            if (s1c.nextInt() != s2c.nextInt()) {
                return false;
            }
        }

        if (s1c.hasNext() || s2c.hasNext()) {
            return false;
        }
        return true;

    }

    /**
     * Equivalent to <code>String#valueOf(data, offset, count).substring()</code>, but hopefully more efficient, and
     * tolerant of null values and out-of-bounds conditions (in which case it returns an empty string).
     *
     * <p>
     * <b>IMPORTANT:</b> note the difference between this method whose parameters mirror those of
     * {@link String#valueOf(char[], int, int)}; and {@link #getTrimmedSubstring(CharSequence, int, int)} whose
     * parameters mirror those of {@link String#substring(int, int)}.
     *
     * @param data
     *     the characters in the whole string.
     * @param offset
     *     the offset for the requested sequence and substring.
     * @param count
     *     the requested number of characters to consider, starting at the offset.
     * @return a trimmed version of a String containing the characters from the requested region; or the empty string if
     *     the char[] is null or the offset and count refer to an undefined region or a region outside the data.
     */
    public static String getTrimmedString(char[] data, int offset, int count) {
        if (ArrayUtils.isEmpty(data)) {
            return StringUtils.EMPTY;
        }
        return getTrimmedSubstring(EnhancedCharSequence.getInstance(data), offset, offset + count);
    }

    /**
     * Equivalent to <code>data.toString().substring(startIndex).trim()</code>, but hopefully more efficient, and
     * tolerant of null values and out-of-bounds conditions (in which case it returns an empty string).
     *
     * @param sequence
     *     the sequence of characters.
     * @param startIndex
     *     the start index for the requested substring.
     * @return trimmed version of a String containing the characters from the requested region; or the empty string if
     *     the CharSequence is null or the start index starts in a region outside the sequence.
     */
    public static String getTrimmedSubstring(CharSequence sequence, int startIndex) {
        return getTrimmedSubstring(sequence, startIndex, Integer.MAX_VALUE);
    }

    /**
     * Equivalent to <code>data.toString().substring(startIndex, endIndex).trim()</code>, but hopefully more efficient,
     * and tolerant of null values and out-of-bounds conditions (in which case it returns an empty string).
     *
     * <p>
     * <b>IMPORTANT:</b> note the difference between this method whose parameters mirror those of
     * {@link String#substring(int, int)}; and {@link #getTrimmedString(char[], int, int)} whose parameters mirror those
     * of {@link String#valueOf(char[], int, int)}.
     *
     * @param sequence
     *     the sequence of characters.
     * @param startIndex
     *     the start index for the requested substring.
     * @param endIndex
     *     the end index (exclusive) for the requested substring.
     * @return trimmed version of a String containing the characters from the requested region; or the empty string if
     *     the sequence is null or the start index and end index refer to a region outside the sequence.
     */
    public static String getTrimmedSubstring(CharSequence sequence, int startIndex, int endIndex) {
        return getMatchingRange(sequence, startIndex, endIndex, CharMatcher.whitespace().negate())
            .applyAsString(sequence);
    }

    public static IndexRange getMatchingRange(CharSequence sequence, CharMatcher matcher) {
        Objects.requireNonNull(matcher, "matcher");
        int len = StringUtils.length(sequence);
        if (len == 0) {
            return SimpleImmutableIndexRange.EMPTY;
        }
        return getMatchingRangeImpl(sequence, 0, len, matcher);
    }

    public static IndexRange getMatchingRange(CharSequence sequence, int startIndex, int endIndex, CharMatcher matcher) {

        Objects.requireNonNull(matcher, "matcher");

        int len = StringUtils.length(sequence);
        if (len == 0) {
            // CharSequence is null or empty.
            return SimpleImmutableIndexRange.EMPTY;
        }

        if (startIndex >= endIndex) {
            // Requested region is empty.
            return SimpleImmutableIndexRange.EMPTY;
        }

        if (startIndex >= len || endIndex <= 0) {
            // Requested region is entirely outside the sequence.
            return SimpleImmutableIndexRange.EMPTY;
        }

        if (endIndex > len) {
            endIndex = len;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }

        return getMatchingRangeImpl(sequence, startIndex, endIndex, matcher);

    }

    public static boolean startsWith(CharSequence sequence, char c) {
        return matchCharAt(sequence, SpecialPosition.FIRST, c);
    }

    public static boolean startsWithAny(CharSequence sequence, char... chars) {
        return matchAnyCharAt(sequence, SpecialPosition.FIRST, chars);
    }

    public static boolean startsWithAny(CharSequence sequence, Set<Character> chars) {
        return matchAnyCharAt(sequence, SpecialPosition.FIRST, chars);
    }

    public static boolean endsWith(CharSequence sequence, char c) {
        return matchCharAt(sequence, SpecialPosition.LAST, c);
    }

    public static boolean endsWithAny(CharSequence sequence, char... chars) {
        return matchAnyCharAt(sequence, SpecialPosition.LAST, chars);
    }

    public static boolean endsWithAny(CharSequence sequence, Set<Character> chars) {
        return matchAnyCharAt(sequence, SpecialPosition.LAST, chars);
    }

    public static boolean startsWithCodePoint(String str, int codePoint) {
        return matchCodePointAt(str, SpecialPosition.FIRST, codePoint);
    }

    public static boolean startsWithAnyCodePoint(String str, int... codePoints) {
        return matchesAnyCodePointAt(str, SpecialPosition.FIRST, codePoints);
    }

    public static boolean startsWithAnyCodePoint(String str, Set<Integer> codePoints) {
        return matchesAnyCodePointAt(str, SpecialPosition.FIRST, codePoints);
    }

    public static boolean endsWithCodePoint(String str, int codePoint) {
        return matchCodePointAt(str, SpecialPosition.LAST, codePoint);
    }

    public static boolean endsWithAnyCodePoint(String str, int... codePoints) {
        return matchesAnyCodePointAt(str, SpecialPosition.LAST, codePoints);
    }

    public static boolean endsWithAnyCodePoint(String str, Set<Integer> codePoints) {
        return matchesAnyCodePointAt(str, SpecialPosition.LAST, codePoints);
    }

    public static final boolean isBasicLatin(char c) {
        return (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.BASIC_LATIN);
    }

    /**
     * Performs a binary search in a List of case-insensitive sorted strings to find the first one starting with the
     * given code point.
     *
     * <p>
     * The algorithm used in this method is was borrowed, with a few modifications, from the binary search used to
     * implement methods like {@link Arrays#binarySearch(long[], long)}. It is therefore subject to the GNU General
     * Public License version 2 license agreement as per the source code license notice in that file.
     *
     * <p>
     * If no value starts with a matching code point, the returned index will be a special value, as per the
     * {@link Arrays} implementations of binary searches,
     *
     * <blockquote>
     * <code>(-(<i>insertion point</i>) - 1)</code>. The <i>insertion point</i> is defined as the point at which the
     * key would be inserted into the list</blockquote>
     *
     * <p>
     * where such a string value would be inserted to maintain the ordering. Note that this means that for a null or
     * empty list, the return value would be -1 (insertion point of 0, minus 1 = -1).
     *
     * <p>
     * When considering a string value that is null or empty, the first code point will be considered to be -1, which
     * would place such a value before any actual code points, since code points are unsigned short values (and
     * therefore also positive).
     *
     * @param caseInsensitiveSortedStrings
     *     the case-insensitive sorted strings to search.
     * @param codePoint
     *     the code point prefix to find.
     * @return the index of the first list element whose values starts with the requested code point, if such a string
     *     value exists; otherwise the index representing the insertion point where such a string value would be
     *     inserted to maintain the order.
     */
    @Beta
    public static final int caseInsensitiveBinarySearchFirstCodePoint(List<String> caseInsensitiveSortedStrings, int codePoint) {

        if (!Character.isValidCodePoint(codePoint)) {
            throw new IllegalArgumentException(
                "Invalid code point " + codePoint + " (" + Integer.toHexString(codePoint) + ")."
            );
        }

        if (CollectionsUtil.isEmpty(caseInsensitiveSortedStrings)) {
            return -1;
        }

        codePoint = Character.toUpperCase(codePoint);

        int low = 0;
        int high = caseInsensitiveSortedStrings.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            String midStr = caseInsensitiveSortedStrings.get(mid);
            int midVal;
            if (StringUtils.isEmpty(midStr)) {
                midVal = -1;
            }
            else {
                midVal = Character.toUpperCase(midStr.codePointAt(0));
            }
            if (midVal < codePoint) {
                low = mid + 1;
            }
            else if (midVal > codePoint) {
                high = mid - 1;
            }
            else {
                // code point prefix found. search backwards to find the first matching word.
                if (mid > 0) {
                    Predicate<String> startsWithCaseInsensitive =
                        startsWithCodePointCaseInsensitiveCodePointMatcherForUpper(
                            codePoint
                        );
                    while (mid > 0 && startsWithCaseInsensitive.test(caseInsensitiveSortedStrings.get(mid - 1))) {
                        mid--;
                    }
                }
                return mid;
            }
        }
        // code point prefix not found.
        return -(low + 1);

    }

    public static final int indexOfIgnoringLeadingWhitespace(CharSequence str) {
        if (str == null) {
            return StringUtils.INDEX_NOT_FOUND;
        }
        return indexOfLeadingWhitespaceImpl(str, str.length() - 1);
    }

    public static final int indexOfIgnoringLeadingWhitespace(CharSequence str, int lastEligibleIndex) {
        if (str == null) {
            return StringUtils.INDEX_NOT_FOUND;
        }
        int len = str.length() - 1;
        if (lastEligibleIndex >= len) {
            return StringUtils.INDEX_NOT_FOUND;
        }
        return indexOfLeadingWhitespaceImpl(str, lastEligibleIndex);
    }

    public static final int lastIndexOfIgnoringTrailingWhitespace(CharSequence str) {
        if (str == null) {
            return StringUtils.INDEX_NOT_FOUND;
        }
        return lastIndexOfIgnoringTrailingWhitespaceImpl(str, 0);
    }

    public static final int lastIndexOfIgnoringTrailingWhitespace(CharSequence str, int firstEligibleIndex) {
        if (str == null || firstEligibleIndex < 0) {
            return StringUtils.INDEX_NOT_FOUND;
        }
        return lastIndexOfIgnoringTrailingWhitespaceImpl(str, firstEligibleIndex);
    }

    public static final boolean startsWithIgnoringLeadingWhitespace(String str, String search) {
        return startsWithIgnoringLeadingWhitespaceImpl(str, search, false);
    }

    public static final boolean startsWithIgnoringLeadingWhitespaceIgnoreCase(String str, String search) {
        return startsWithIgnoringLeadingWhitespaceImpl(str, search, true);
    }

    public static final boolean endsWithIgnoringTrailingWhitespace(String str, String search) {
        return endsWithIgnoringLeadingWhitespaceImpl(str, search, false);
    }

    public static final boolean endsWithIgnoringTrailingWhitespaceIgnoreCase(String str, String search) {
        return endsWithIgnoringLeadingWhitespaceImpl(str, search, true);
    }

    private static IndexRange getMatchingRangeImpl(CharSequence sequence, final int startSearchIndex, final int endSearchIndex, CharMatcher matcher) {

        int startRangeIndex = startSearchIndex;
        while (!matcher.matches(sequence.charAt(startRangeIndex))) {
            startRangeIndex++;
            if (startRangeIndex == endSearchIndex) {
                // Found a completely collapsed region of whitespace.
                return SimpleImmutableIndexRange.EMPTY;
            }
        }

        int endIncludedIndex = endSearchIndex - 1;
        while (!matcher.matches(sequence.charAt(endIncludedIndex))) {
            endIncludedIndex--;
        }

        int endRangeIndex = endIncludedIndex + 1;
        return SimpleImmutableIndexRange.getInstance(startRangeIndex, endRangeIndex);

    }

    private static int indexOfIgnoreCaseImpl(String str, char c, int fromIndex) {
        if (Character.isLowerCase(c)) {
            return indexOfEitherImpl(str, c, Character.toUpperCase(c), fromIndex);
        }
        if (Character.isUpperCase(c)) {
            return indexOfEitherImpl(str, c, Character.toLowerCase(c), fromIndex);
        }
        return str.indexOf(c, fromIndex);
    }

    private static int indexOfEitherImpl(String str, char c1, char c2, int fromIndex) {
        int len = str.length();
        for (int index = Math.max(0, fromIndex); index < len; index++) {
            char charAt = str.charAt(index);
            if (charAt == c1 || charAt == c2) {
                return index;
            }
        }
        return StringUtils.INDEX_NOT_FOUND;
    }

    private static boolean containsCharactersInOrderImpl(String str, String search, CharIndexFinder finder) {
        if (StringUtils.isEmpty(str) || StringUtils.isEmpty(search)) {
            return false;
        }
        int next = -1;
        for (int i = 0; i < search.length(); i++) {
            next = finder.indexOf(str, search.charAt(i), next + 1);
            if (next == StringUtils.INDEX_NOT_FOUND) {
                return false;
            }
        }
        return true;
    }

    private static int[] findNextCollapsibleOrNormalizableWhitespaceBoundaries(String input, boolean alternativeWhitespace, int fromIndex) {

        CollapsibleOrNormalizableWhitespaceScanningState state =
            CollapsibleOrNormalizableWhitespaceScanningState.NO_WHITESPACE;
        int runStartIndex = -1;
        int sz = input.length();

        for (int i = fromIndex; i < sz; i++) {

            char c = input.charAt(i);
            boolean curWs = isWhitespaceChar(c, alternativeWhitespace);

            switch (state) {
            case SINGLE_REGULAR_SPACE -> {
                if (curWs) {
                    state = CollapsibleOrNormalizableWhitespaceScanningState.COLLAPSIBLE_OR_NORMALIZABLE_WHITESPACE;
                }
                else {
                    state = CollapsibleOrNormalizableWhitespaceScanningState.NO_WHITESPACE;
                }
            }
            case COLLAPSIBLE_OR_NORMALIZABLE_WHITESPACE -> {
                if (!curWs) {
                    return new int[] { runStartIndex, i - 1 };
                }
            }
            default -> {
                if (curWs) {
                    runStartIndex = i;
                    if (c == ' ') {
                        state = CollapsibleOrNormalizableWhitespaceScanningState.SINGLE_REGULAR_SPACE;
                    }
                    else {
                        state = CollapsibleOrNormalizableWhitespaceScanningState.COLLAPSIBLE_OR_NORMALIZABLE_WHITESPACE;
                    }
                }
            }
            }

        }

        if (state.isCollapsible()) {
            return new int[] { runStartIndex, sz - 1 };
        }
        return null;

    }

    private static int[] findNextNormalizableWhitespaceBoundaries(String input, int fromIndex) {

        int runStartIndex = -1;
        int sz = input.length();

        for (int i = fromIndex; i < sz; i++) {

            if (isAlternativeWhitespaceChar(input.charAt(i))) {
                if (runStartIndex == -1) {
                    runStartIndex = i;
                }
                continue;
            }

            if (runStartIndex != -1) {
                return new int[] { runStartIndex, i - 1 };
            }

        }

        if (runStartIndex == -1) {
            return null;
        }
        return new int[] { runStartIndex, sz - 1 };

    }

    private static boolean matchCharAt(CharSequence sequence, SpecialPosition position, char c) {
        int len = StringUtils.length(sequence);
        if (len > 0 && position.getChar(sequence, len) == c) {
            return true;
        }
        return false;
    }

    private static boolean matchAnyCharAt(CharSequence sequence, SpecialPosition position, char... chars) {
        if (chars == null) {
            return false;
        }
        int len = StringUtils.length(sequence);
        if (len > 0) {
            char charAt = position.getChar(sequence, len);
            for (char c : chars) {
                if (charAt == c) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean matchAnyCharAt(CharSequence sequence, SpecialPosition position, Set<Character> chars) {
        if (chars == null) {
            return false;
        }
        int len = StringUtils.length(sequence);
        if (len > 0) {
            return chars.contains(position.getChar(sequence, len));
        }
        return false;
    }

    private static boolean matchCodePointAt(String str, SpecialPosition position, int codePoint) {
        int len = StringUtils.length(str);
        if (len > 0 && position.getCodePoint(str, len) == codePoint) {
            return true;
        }
        return false;
    }

    private static boolean matchesAnyCodePointAt(String str, SpecialPosition position, int... codePoints) {
        int len = StringUtils.length(str);
        if (len > 0) {
            int codePointAt = position.getCodePoint(str, len);
            for (int codePoint : codePoints) {
                if (codePointAt == codePoint) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final int indexOfLeadingWhitespaceImpl(CharSequence str, int lastEligibleIndex) {
        for (int i = 0; i <= lastEligibleIndex; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return i;
            }
        }
        return StringUtils.INDEX_NOT_FOUND;
    }

    private static final int lastIndexOfIgnoringTrailingWhitespaceImpl(CharSequence str, int firstEligibleIndex) {
        for (int i = str.length() - 1; i >= firstEligibleIndex; i--) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return i;
            }
        }
        return StringUtils.INDEX_NOT_FOUND;
    }

    private static final boolean startsWithIgnoringLeadingWhitespaceImpl(String str, String search, boolean ignoreCase) {

        if (str == null || search == null) {
            return false;
        }

        int searchLen = search.length();
        if (searchLen == 0) {
            return true;
        }

        int strLen = str.length();
        if (strLen == 0) {
            return false;
        }

        int maxStrIndexOfLastWhitespace = strLen - searchLen;
        if (maxStrIndexOfLastWhitespace < 0) {
            return false;
        }

        int startIndex = indexOfLeadingWhitespaceImpl(str, maxStrIndexOfLastWhitespace);
        if (startIndex == StringUtils.INDEX_NOT_FOUND) {
            return false;
        }

        return str.regionMatches(ignoreCase, startIndex, search, 0, searchLen);

    }

    private static final boolean endsWithIgnoringLeadingWhitespaceImpl(String str, String search, boolean ignoreCase) {

        if (str == null || search == null) {
            return false;
        }

        int searchLen = search.length();
        if (searchLen == 0) {
            return true;
        }

        int strLen = str.length();
        if (strLen == 0) {
            return false;
        }

        int diff = strLen - searchLen;
        if (diff < 0) {
            return false;
        }

        // searching for, e.g., "foo" needs length 3, so the first non-whitespace character can't be any earlier than
        // index = 3 - 1 = 2 in that case.
        int endIndex = lastIndexOfIgnoringTrailingWhitespaceImpl(str, searchLen - 1);
        if (endIndex == StringUtils.INDEX_NOT_FOUND) {
            return false;
        }

        // "abc foo  " - first non-whitespace index = 6
        //  0123456789
        // endIndex = 6
        // startIndex = 6 - 3 + 1 = 4
        // match candidate region covers index range 4, 5, 6
        return str.regionMatches(ignoreCase, endIndex - searchLen + 1, search, 0, searchLen);

    }

    private static boolean matchesAnyCodePointAt(String str, SpecialPosition position, Set<Integer> codePoints) {
        if (CollectionsUtil.isEmpty(codePoints)) {
            return false;
        }
        int len = StringUtils.length(str);
        if (len > 0) {
            return codePoints.contains(position.getCodePoint(str, len));
        }
        return false;
    }

    private static final String removeImpl(String str, int remove) {

        int nextIndex = str.indexOf(remove);
        if (nextIndex == StringUtils.INDEX_NOT_FOUND) {
            return str;
        }

        StringBuilder buff = new StringBuilder();
        int lastIndex = 0;
        do {
            buff.append(str, lastIndex, nextIndex);
            lastIndex = nextIndex + 2;
            nextIndex = str.indexOf(remove, lastIndex);
        }
        while (nextIndex != StringUtils.INDEX_NOT_FOUND);

        buff.append(str, lastIndex, str.length());
        return buff.toString();

    }

    private static final String removeImpl(String str, char remove) {
        int nextIndex = str.indexOf(remove);
        if (nextIndex == StringUtils.INDEX_NOT_FOUND) {
            return str;
        }
        StringBuilder buff = new StringBuilder();
        int lastIndex = 0;
        do {
            buff.append(str, lastIndex, nextIndex);
            lastIndex = nextIndex + 1;
            nextIndex = str.indexOf(remove, lastIndex);
        }
        while (nextIndex != StringUtils.INDEX_NOT_FOUND);

        buff.append(str, lastIndex, str.length());
        return buff.toString();

    }

    private static final Predicate<String> startsWithCodePointCaseInsensitiveCodePointMatcherForUpper(int codePointUpper) {
        if (Character.isBmpCodePoint(codePointUpper)) {
            char charUpper = (char) codePointUpper;
            char charLower = Character.toLowerCase(charUpper);
            if (charUpper == charLower) {
                return (str) -> matchCharAt(str, SpecialPosition.FIRST, charUpper);
            }
            return (str) -> matchAnyCharAt(str, SpecialPosition.FIRST, charUpper, charLower);
        }
        int codePointLower = Character.toLowerCase(codePointUpper);
        if (codePointUpper == codePointLower) {
            return (str) -> matchCodePointAt(str, SpecialPosition.FIRST, codePointUpper);
        }
        return (str) -> matchesAnyCodePointAt(str, SpecialPosition.FIRST, codePointLower, codePointUpper);
    }

}

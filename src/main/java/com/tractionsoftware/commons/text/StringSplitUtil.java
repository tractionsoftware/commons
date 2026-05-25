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
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.tractionsoftware.commons.lang.EnhancedCharSequence;
import com.tractionsoftware.commons.lang.StringUtil;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Utility methods for splitting strings based on various delimiters.
 */
@Beta
public final class StringSplitUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringSplitUtil.class);

    public static final String DEFAULT_STRING_LIST_SEPARATOR = ",";

    public static final char DEFAULT_STRING_LIST_SEPARATOR_CHAR = ',';

    public static final Pattern DEFAULT_LIST_SEPARATOR_PATTERN = Pattern.compile(DEFAULT_STRING_LIST_SEPARATOR);

    public static class Options {

        public boolean escapeValues() {
            return false;
        }

        public String getSeparator() {
            return DEFAULT_STRING_LIST_SEPARATOR;
        }

        public CharMatcher escapeCharacters() {
            return StringEscapeUtil.DEFAULT_ESCAPED_CHARS_MATCHER;
        }

        public Joiner getJoiner() {
            return StringUtil.getNullSkippingJoiner(
                Objects.requireNonNullElse(getSeparator(), DEFAULT_STRING_LIST_SEPARATOR)
            );
        }

    }

    public static final Options DEFAULT_OPTIONS = new Options();

    private static final CharMatcher COMMA_SPACE_SEMI = new CharMatcher() {

        @Nonnull
        @Override
        public final String toString() {
            return "comma/space/semi matcher";
        }

        @Override
        public final boolean matches(char c) {
            return switch (c) {
                case ',', ' ', ';' -> true;
                default -> false;
            };
        }

    };

    /**
     * Not instantiable.
     */
    private StringSplitUtil() {
    }

    public static final List<String> parseCommaSpaceSemicolonSeperatedList(String listSpec) {
        if (StringUtils.isEmpty(listSpec)) {
            return new ArrayList<>(0);
        }
        List<String> list = new ArrayList<>();
        Iterables.addAll(list, Splitter.on(COMMA_SPACE_SEMI).split(listSpec));
        return list;
    }

    public static final ArrayList<String> parseCommaSpaceSemicolonSeparatedListSkippingQuoted(String list) {
        ArrayList<String> ret = new ArrayList<>();
        parseCommaSpaceSemicolonSeparatedListSkippingQuoted(list, ret);
        return ret;
    }

    public static final void parseCommaSpaceSemicolonSeparatedListSkippingQuoted(String list, Collection<? super String> fill) {
        parseCommaSpaceSemicolonSeparatedListSkippingQuoted(list, fill, false);
    }

    /**
     * @param includeNextAfterQuoted
     *     for parsing email addresses, it helps to include the email address that appears after a quoted string as part
     *     of a single complete email address.
     */
    public static final void parseCommaSpaceSemicolonSeparatedListSkippingQuoted(String list, Collection<? super String> fill, boolean includeNextAfterQuoted) {

        if (list == null) {
            return;
        }

        try {

            char[] c = list.toCharArray();
            int sz = c.length;

            boolean skip = false;
            boolean jump = false;
            int s = 0;

            for (int i = 0; i < sz; i++) {
                switch (c[i]) {
                case '"':
                    skip = !skip;
                    if (includeNextAfterQuoted && !skip) {
                        jump = true;
                    }
                    break;

                case ' ':
                case ';':
                case ',':
                    if (!skip && !jump) {
                        // separators
                        if (s != i) {
                            // they'll be equal if there are two separators in a row
                            fill.add(String.copyValueOf(c, s, i - s));
                        }
                        s = i + 1;
                    }

                    //$FALL-THROUGH$
                default:
                    jump = false;
                    break;
                }

            }

            if (s < sz) {
                String last = StringUtil.getTrimmedString(c, s, sz - s);
                if (!last.isEmpty()) {
                    if (skip) {
                        // close the quote if it isn't
                        last += '"';
                    }
                    fill.add(last);
                }
            }

        }
        catch (RuntimeException e) {
            LOGGER.error("Unexpected failure in parseCommaSpaceSemicolonSeparatedList", e);
        }

    }

    /**
     * Similar to {@link #parseCommaSpaceSemicolonSeparatedListSkippingQuoted(String, Collection)}, but accepts custom
     * separators (e.g. ", ;").
     */
    public static final void parseQuotedGroupedList(String list, Collection<? super String> fill, String separators) {

        if (StringUtils.isEmpty(list) || StringUtils.isEmpty(separators)) {
            return;
        }

        CharMatcher matcher = CharMatcher.anyOf(separators);

        try {

            int sz = list.length();

            char quote = 0;
            boolean skip = false;
            int start = 0;
            int parens = 0;

            for (int i = 0; i < sz; i++) {
                char c = list.charAt(i);
                switch (c) {
                case '(':
                    if (quote == 0) {
                        parens++;
                    }
                    break;
                case ')':
                    if (quote == 0) {
                        parens--;
                    }
                    break;
                case '"':
                case '\'':
                    if (parens == 0 && (i <= 0 || list.charAt(i - 1) != '\\') && (quote == 0 || quote == c)) {
                        skip = !skip;
                        // remember the type of quote
                        quote = c;
                    }
                    break;
                default:
                    if (matcher.matches(c) && parens == 0) {
                        if (!skip) {
                            // separators
                            if (start != i) {
                                // they'll be equal if there are two separators in a row
                                fill.add(list.substring(start, i));
                                quote = 0;
                            }
                            start = i + 1;
                        }
                    }
                    break;
                }
            }

            if (start < sz) {
                String last = StringUtil.getTrimmedSubstring(list, start, sz);
                if (!last.isEmpty()) {
                    if (skip) {
                        last += quote;
                    }
                    else if (parens > 0) {
                        last += StringUtils.repeat(')', parens);
                    }
                    fill.add(last);
                }
            }

        }
        catch (Exception e) {
            LOGGER.error("Unexpected failure in parseQuotedGroupedList", e);
        }

    }

    public static final boolean splitString(String value, Pattern separatorPattern, Collection<? super String> list) {
        return splitString(value, Splitter.on(separatorPattern), list);
    }

    public static final boolean splitString(String value, Splitter splitter, Collection<? super String> list) {
        if (value == null || list == null) {
            return false;
        }
        for (String splitValue : splitter.split(value)) {
            if (!splitValue.isEmpty()) {
                list.add(splitValue);
            }
        }
        return true;
    }

    /**
     * For separator s and string v, converts v from an s-separated list into a collection of strings.
     *
     * @param value
     *     the String that should be converted
     * @param callback
     *     a callback function that will be called with converted Strings
     * @param options
     *     to use for handling splitting of the string into individual values.
     * @return false if the value, separator or list is null, or if the separator is the empty string; true otherwise
     */
    public static final boolean splitString(String value, Consumer<? super String> callback, Options options) {

        if (value == null || callback == null) {
            return false;
        }

        String separator = options.getSeparator();
        if (StringUtils.isEmpty(separator)) {
            return false;
        }

        char[] v = value.toCharArray();
        int len = v.length;
        int start = 0;
        char[] s = separator.toCharArray();

        for (int i = 0; i < len; i++) {
            if (v[i] == s[0]) {
                if (!options.escapeValues() || i == 0 || v[i - 1] != '\\') {
                    // we have to keep track of whether, while looking for the next
                    // character in the separator, we found something other than
                    // what we were looking for.
                    boolean falseAlarm = false;

                    // we start at 1 because we already know we've got s[0];
                    // and we stop either when we've found a char that tells us that
                    // we actually haven't found the separator, or until we go through
                    // the whole separator.
                    for (int j = 1; !falseAlarm && j < s.length; j++) {
                        // make sure there's another character before checking
                        if (i + j >= v.length || v[i + j] != s[j]) {
                            falseAlarm = true;
                        }
                    }

                    if (!falseAlarm) {
                        callback.accept(getStringValue(options, v, start, i - start));
                        // have to advance the main pointer the length of the rest of the marker
                        // (i.e., one for each character in the separator except for the first,
                        // because the main pointer already was pointing to the first character
                        // in s) so that we're pointing at the last character in the separator
                        i += (s.length - 1);
                        start = i + 1; // start after the separator
                    }
                }
            }
        }

        // grab the last one
        if (len > 0) {
            if (start == len) {
                callback.accept("");  // there was an empty string at the end [ajm 26.Nov.2001]
            }
            else if (start < len) {
                callback.accept(
                    getStringValue(options, v, start, len - start)
                );
            }
        }

        return true;
    }

    public static final List<String> stringToStringList(String value, List<String> defaultValue, Pattern splitPattern) {
        return stringToStringCollection(value, defaultValue, splitPattern, ArrayList::new);
    }

    public static final Set<String> stringToStringSet(String value, Set<String> defaultValue, Pattern splitPattern) {
        return stringToStringCollection(value, defaultValue, splitPattern, LinkedHashSet::new);
    }

    public static final <C extends Collection<String>> C stringToStringCollection(String value, C defaultValue, Pattern splitPattern, Supplier<? extends C> collectionCreator) {
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        C ret = collectionCreator.get();
        splitString(value, ret, splitPattern);
        return ret;
    }

    public static final <C extends Collection<String>> void splitString(String value, C collection, Pattern splitPattern) {
        for (String splitValue : splitPattern.split(value)) {
            if (!splitValue.isEmpty()) {
                collection.add(splitValue);
            }
        }
    }

    private static final String getStringValue(Options options, char[] chars, int start, int length) {
        if (options.escapeValues()) {
            return StringEscapeUtil.unescapeMultipleCharacters(
                EnhancedCharSequence.getInstance(chars, start, length).trim(), options.escapeCharacters()
            );
        }
        return StringUtil.getTrimmedString(chars, start, length);
    }

}

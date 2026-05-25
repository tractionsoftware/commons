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

import com.google.common.base.Joiner;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.tractionsoftware.commons.text.SnippetUtil;
import com.tractionsoftware.commons.text.StringEscapeUtil;
import com.tractionsoftware.commons.text.StringSplitUtil;
import com.tractionsoftware.commons.util.CollectionsUtil;
import com.tractionsoftware.commons.util.function.CacheSupportingFunction;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.stream.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

/**
 * @author Andy Keller, Dave Shepperton
 */
public final class NativeTypeConversion {

    private NativeTypeConversion() {
    }

    public static interface BooleanStrings {

        @Nonnull
        public String get(boolean value);

        public boolean get(@Nullable String value);

    }

    public static class CollectionToStringOptions {

        public boolean nullOrEmptyListToEmptyString() {
            return false;
        }

        public boolean escapeValues() {
            return false;
        }

        public StringSplitUtil.Options getJoinOptions() {
            return StringSplitUtil.DEFAULT_OPTIONS;
        }

    }

    public static class MapToStringOptions {

        public boolean nullOrEmptyMapToEmptyString() {
            return false;
        }

        public CollectionToStringOptions getNameValuePairListToStringOptions() {
            return DEFAULT_COLLECTION_TO_STRING_OPTIONS;
        }

        public boolean includeEmptyValues() {
            return false;
        }

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeTypeConversion.class);

    private static final Supplier<? extends BooleanStrings> booleanStrings = JavaUtil.<BooleanStrings>lazyServiceLoader(
        BooleanStrings.class, NativeTypeConversion::defaultBooleanStrings, LOGGER
    );

    public static abstract class String2NativeTypeConverter<T, R> implements CacheSupportingFunction<T,R> {

        protected final Function<? super T,String> wrapped;

        protected final R defaultValue;

        private String2NativeTypeConverter(Function<? super T,String> wrapped, R defaultValue) {
            this.wrapped = wrapped;
            this.defaultValue = defaultValue;
        }

        @Override
        public final boolean resultsAreCacheable() {
            if (wrapped instanceof CacheSupportingFunction<? super T,String> cacheSupporting) {
                return cacheSupporting.resultsAreCacheable();
            }
            return false;
        }

    }

    public static final boolean stringToBoolean(String value) {
        return stringToBoolean(value, false);
    }

    /**
     * Converts the given String to a Boolean if possible and returns the result.
     *
     * <p>
     * If the value is null, the given default value will be returned instead.
     *
     * <p>
     * If the given value is not null, this method will return true as long as the String is not a case-insensitive
     * variant of the literal "f" or "false". This is obviously covers a broader range of values than
     * {@link Boolean#valueOf(String)}, which only recognizes case-insensitive variants of the literal "true" as true.
     *
     * @param value
     *     the String to be converted.
     * @param defaultValue
     *     the default value to be used in case the given String is null.
     * @return the boolean value resulting from converting the given String or the given default.
     */
    public static final boolean stringToBoolean(String value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return booleanStrings.get().get(value);
    }

    public static final String booleanToString(boolean value) {
        return booleanStrings.get().get(value);
    }

    public static final byte stringToByte(String value) {
        return stringToByte(value, Byte.MIN_VALUE);
    }

    public static final byte stringToByte(String value, byte fallback) {
        if (StringUtils.isBlank(value)) {
            return fallback;
        }
        try {
            return Byte.parseByte(value);
        }
        catch (NumberFormatException e) {
            logNumberFormatException(value, "byte", e);
        }
        return fallback;
    }

    public static final short stringToShort(String value) {
        return stringToShort(value, Short.MIN_VALUE);
    }

    public static final short stringToShort(String value, short fallback) {
        if (StringUtils.isBlank(value)) {
            return fallback;
        }
        try {
            return Short.parseShort(value);
        }
        catch (NumberFormatException e) {
            logNumberFormatException(value, "short", e);
        }
        return fallback;
    }

    public static final int stringToInt(String value) {
        return stringToInt(value, Integer.MIN_VALUE);
    }

    /**
     * Converts the given String to an integer parsed as a base 10 number.
     *
     * @param value
     *     the value to convert to an int.
     * @param defaultValue
     *     the default value to return in case the conversion fails.
     * @return the given value converted to an int, or else the given default value if the conversion fails.
     */
    public static final int stringToInt(String value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e) {
            logNumberFormatException(value, "int", e);
        }
        return defaultValue;
    }

    public static final long stringToLong(String value) {
        return stringToLong(value, Long.MIN_VALUE);
    }

    public static final long stringToLong(String value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        }
        catch (NumberFormatException e) {
            logNumberFormatException(value, "long", e);
        }
        return defaultValue;
    }

    public static final float stringToFloat(String value) {
        return stringToFloat(value, Float.NaN);
    }

    public static final float stringToFloat(String value, float fallback) {
        if (StringUtils.isBlank(value)) {
            return fallback;
        }
        try {
            return Float.parseFloat(value);
        }
        catch (NumberFormatException e) {
            logNumberFormatException(value, "float", e);
        }
        return fallback;
    }

    public static final double stringToDouble(String value) {
        return stringToDouble(value, Double.NaN);
    }

    public static final double stringToDouble(String value, double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        }
        catch (NumberFormatException e) {
            logNumberFormatException(value, "double", e);
        }
        return defaultValue;
    }

    public static final BigDecimal stringToBigDecimal(String value, BigDecimal defaultValue) {
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return new BigDecimal(value);
        }
        catch (NumberFormatException e) {
            logNumberFormatException(value, "BigDecimal", e);
        }
        return defaultValue;
    }

    public static final MapToStringOptions DEFAULT_MAP_TO_STRING_OPTIONS = new MapToStringOptions();

    public static final CollectionToStringOptions DEFAULT_COLLECTION_TO_STRING_OPTIONS =
        new CollectionToStringOptions();

    public static final CollectionToStringOptions DEFAULT_COLLECTION_TO_STRING_MAP_SPLIT_STRING_OPTIONS =
        new CollectionToStringOptions() {
        };

    public static final String iterableToString(Iterable<?> list) {
        return iterableToString(list, DEFAULT_COLLECTION_TO_STRING_OPTIONS);
    }

    /**
     * This should only be used for comma separated lists of Objects where none of the values returned by calling
     * toString() on the objects contained in the vector contain commas.
     */
    public static final String iterableToString(Iterable<?> coll, @Nullable CollectionToStringOptions options) {
        if (coll == null) {
            if (options != null && options.nullOrEmptyListToEmptyString()) {
                return "";
            }
            return null;
        }
        return streamToString(Streams.of(coll), options);
    }

    public static final String streamToString(Stream<?> source, @Nullable CollectionToStringOptions options) {

        if (options == null) {
            options = DEFAULT_COLLECTION_TO_STRING_OPTIONS;
        }

        if (source == null) {
            if (options.nullOrEmptyListToEmptyString()) {
                return "";
            }
            return null;
        }

        Joiner joiner = options.getJoinOptions().getJoiner();
        Stream<String> strings = source.map(ObjectsUtil::safeToString);
        if (options.escapeValues()) {
            strings = strings.map(StringEscapeUtil.escaper(options.getJoinOptions().escapeCharacters()));
        }
        String result = joiner.join(strings.iterator());
        if (result.isEmpty()) {
            if (options.nullOrEmptyListToEmptyString()) {
                return "";
            }
            return null;
        }
        return result;

    }

//    private static final String escape(String str) {
//        // need to escape \ then , \n \r
//        return StringEscapeUtil.escapeMultipleCharacters(str, "\r\n,\\");
//    }

    public static final List<String> stringToList(String value) {
        ArrayList<String> list = new ArrayList<>();
        stringToList(value, list);
        return list;
    }

    @CanIgnoreReturnValue
    public static final boolean stringToList(String value, Collection<? super String> list) {
        return stringToList(value, list, DEFAULT_COLLECTION_TO_STRING_OPTIONS);
    }

    /**
     * For separator s and string v, converts v from an s-separated list into a collection of strings.
     *
     * @param value
     *     the String that should be converted
     * @param list
     *     the Collection object to be populated with converted Strings
     * @param options
     *     for loading
     * @return false if the value, separator or list is null, or if the separator is the empty string; true otherwise.
     */
    @CanIgnoreReturnValue
    public static final boolean stringToList(String value, Collection<? super String> list, CollectionToStringOptions options) {
        return StringSplitUtil.splitString(value, list::add, options.getJoinOptions());
    }

    @CanIgnoreReturnValue
    public static final <T> boolean stringToList(String value, Collection<? super T> list, Function<String,? extends T> converter, CollectionToStringOptions options) {
        return StringSplitUtil.splitString(
            value, getConvertingNonNullCollectionAdder(list, converter), options.getJoinOptions()
        );
    }

    private static final <T> Consumer<String> getConvertingNonNullCollectionAdder(Collection<? super T> list, Function<String,? extends T> converter) {
        return (s) -> {
            T converted = converter.apply(s);
            if (converted != null) {
                list.add(converted);
            }
        };
    }

    public static final <E> void stringList2list(Iterable<String> values, Collection<E> list, Function<String,? extends E> converter) {
        for (String value : values) {
            E converted = converter.apply(value);
            if (converted != null) {
                list.add(converted);
            }
        }
    }

    public static final String mapToString(Map<?,?> map) {
        return mapToString(map, DEFAULT_MAP_TO_STRING_OPTIONS);
    }

    /**
     * For this to work, the keys in the map cannot have '=' in them. All other characters are legal in keys and values.
     * If that limitation is unacceptable, this can be rewritten to escape = signs or = can be escaped in all keys
     * before passing in (and unescaped on the way out).
     */
    public static final String mapToString(Map<?,?> map, MapToStringOptions options) {

        if (map == null) {
            if (options != null && options.nullOrEmptyMapToEmptyString()) {
                return "";
            }
            return null;
        }

        ArrayList<String> nameValuePairs = new ArrayList<>();

        Set<? extends Map.Entry<?,?>> entries = map.entrySet();

        for (Map.Entry<?,?> entry : entries) {
            String val = String.valueOf(entry.getValue());
            // don't write anything we don't have to
            //
            // -- We're using String.valueOf on the entry. That method
            // never returns null -- it returns the literal "null". So
            // val will never be null here, and we will actually write
            // "null" (the test for != null is pointless, in other
            // words). I didn't want to change this behavior because
            // we don't know what code might be relying upon it. [shep 31.Oct.2011]
            if (val != null && (options.includeEmptyValues() || !val.isEmpty())) {
                nameValuePairs.add(entry.getKey() + "=" + val);
            }
        }

        Collections.sort(nameValuePairs);

        return iterableToString(nameValuePairs, options.getNameValuePairListToStringOptions());

    }

    @CanIgnoreReturnValue
    public static final boolean splitStringToMap(String value, Map<? super String,? super String> map) {
        return splitStringToMap(value, map, DEFAULT_COLLECTION_TO_STRING_MAP_SPLIT_STRING_OPTIONS);
    }

    /**
     * For this to work, the keys in the map cannot have '=' in them. All other characters are legal in keys and
     * values.
     */
    @CanIgnoreReturnValue
    public static final boolean splitStringToMap(String value, Map<? super String,? super String> map, CollectionToStringOptions options) {
        if (value == null || map == null) {
            return false;
        }
        ArrayList<String> nvPairs = new ArrayList<>();
        if (!stringToList(value, nvPairs, options)) {
            return false;
        }
        return CollectionsUtil.putMapEntryStrings(nvPairs, map);
    }

    public static final Map<String,String> stringToMap(String data) {
        Map<String,String> map = new LinkedHashMap<>();
        if (splitStringToMap(data, map, DEFAULT_COLLECTION_TO_STRING_MAP_SPLIT_STRING_OPTIONS)) {
            return map;
        }
        return null;
    }

    public static final List<String> stringToStringList(String value, List<String> defaultValue) {
        return StringSplitUtil.stringToStringList(value, defaultValue, StringSplitUtil.DEFAULT_LIST_SEPARATOR_PATTERN);
    }

    public static final Set<String> stringToStringSet(String value, Set<String> defaultValue) {
        return StringSplitUtil.stringToStringSet(value, defaultValue, StringSplitUtil.DEFAULT_LIST_SEPARATOR_PATTERN);
    }

    public static final <T> CacheSupportingFunction<T,Boolean> getStringToBooleanFunction(Function<T,String> function, boolean defaultValue) {
        return new String2NativeTypeConverter<>(function, defaultValue) {
            @Override
            public final Boolean apply(T input) {
                return NativeTypeConversion.stringToBoolean(wrapped.apply(input), defaultValue);
            }
        };
    }

    public static final <T> CacheSupportingFunction<T,Integer> getStringToIntFunction(Function<T,String> function, int defaultValue) {
        return new String2NativeTypeConverter<>(function, defaultValue) {
            @Override
            public final Integer apply(T input) {
                return NativeTypeConversion.stringToInt(wrapped.apply(input), defaultValue);
            }
        };
    }

    public static final <T> CacheSupportingFunction<T,Long> getStringToLongFunction(Function<T,String> function, long defaultValue) {
        return new String2NativeTypeConverter<>(function, defaultValue) {
            @Override
            public final Long apply(T input) {
                return NativeTypeConversion.stringToLong(wrapped.apply(input), defaultValue);
            }
        };
    }

    public static final <T> CacheSupportingFunction<T,Double> getStringToDoubleFunction(Function<T,String> function, double defaultValue) {
        return new String2NativeTypeConverter<>(function, defaultValue) {
            @Override
            public final Double apply(T input) {
                return NativeTypeConversion.stringToDouble(wrapped.apply(input), defaultValue);
            }
        };
    }

    public static final <T> CacheSupportingFunction<T,Pattern> getStringToPatternFunction(Function<T,String> function, Pattern defaultValue) {
        return new String2NativeTypeConverter<>(function, defaultValue) {
            @Override
            public final Pattern apply(T input) {
                try {
                    Pattern.compile(wrapped.apply(input));
                }
                catch (PatternSyntaxException e) {
                    // ignore
                }
                return defaultValue;
            }
        };
    }

    public static final Number stringToNumber(String numberType, String rawValue, Number defaultValue) {

        switch (Objects.toString(numberType, "int").toLowerCase()) {

        case "bigdecimal":
        case "decimal":
        case "dec":
            BigDecimal dec = stringToBigDecimal(rawValue, null);
            if (dec == null) {
                return defaultValue;
            }
            return dec;

        case "i":
        case "int":
        case "integer":
            int i = stringToInt(rawValue, Integer.MIN_VALUE);
            if (i == Integer.MIN_VALUE) {
                return defaultValue;
            }
            return i;

        case "s":
        case "short":
        case "b":
        case "byte":
            short s = stringToShort(rawValue, Short.MIN_VALUE);
            if (s == Short.MIN_VALUE) {
                return defaultValue;
            }
            return s;

        case "f":
        case "float":
        case "d":
        case "double":
        default:
            double d = NativeTypeConversion.stringToDouble(rawValue, Double.NaN);
            if (Double.isNaN(d)) {
                return defaultValue;
            }
            return d;

        }

    }

    public static final Class<?> stringToClass(String classSpec, Class<?> defaultClass) {
        if (StringUtils.isBlank(classSpec)) {
            return defaultClass;
        }
        try {
            return Class.forName(classSpec);
        }
        catch (ClassNotFoundException e) {
            LOGGER.warn("Failed to load class {}", classSpec, e);
        }
        return defaultClass;
    }

    /**
     * Tries to create a UUID from the given encoding.
     *
     * @param uuidSpec
     *     the String containing the UUID encoding.
     * @return the UUID corresponding to the given UUID encoding, if the given UUID encoding is valid; null otherwise.
     */
    public static final UUID stringToUuid(String uuidSpec, UUID defaultValue) {
        if (StringUtils.isBlank(uuidSpec)) {
            return null;
        }
        try {
            return UUID.fromString(uuidSpec);
        }
        catch (RuntimeException e) {
            LOGGER.warn("Invalid UUID {}", uuidSpec, e);
        }
        return defaultValue;
    }

    private static final BooleanStrings defaultBooleanStrings() {

        return new BooleanStrings() {

            @Nonnull
            public final String get(boolean value) {
                if (value) {
                    return Boolean.TRUE.toString();
                }
                return Boolean.FALSE.toString();
            }

            @Override
            public final boolean get(@Nullable String value) {
                if (Boolean.TRUE.toString().equalsIgnoreCase(value)) {
                    return true;
                }
                return false;
            }

        };

    }

    private static final void logNumberFormatException(String value, String numberType, NumberFormatException e) {
        LOGGER.info("Failed to parse {} as {}", SnippetUtil.truncatedToString(value), numberType, e);
    }

}

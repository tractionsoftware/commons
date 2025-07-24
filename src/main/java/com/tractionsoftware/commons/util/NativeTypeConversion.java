/*
 *
 *    Copyright 1996-2025 Traction Software, Inc.
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

package com.tractionsoftware.commons.util;

import com.tractionsoftware.commons.util.function.CacheSupportingFunction;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Andy Keller, Dave Shepperton
 */
public final class NativeTypeConversion {

    public static final String DEFAULT_STRING_LIST_SEPARATOR = ",";

    public static final Pattern DEFAULT_PATTERN = Pattern.compile(DEFAULT_STRING_LIST_SEPARATOR);

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
        return (!value.equalsIgnoreCase("f") && !value.equalsIgnoreCase("false"));
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
            // ignore
        }
        return defaultValue;
    }

    public static final long stringToLong(String value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        }
        catch (NumberFormatException e) {
            // ignore
        }
        return defaultValue;
    }

    public static final double stringToDouble(String value, double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        }
        catch (NumberFormatException e) {
            // ignore
        }
        return defaultValue;
    }

    public static final List<String> stringToStringList(String value, List<String> defaultValue) {
        return stringToStringList(value, defaultValue, DEFAULT_PATTERN);
    }

    public static final List<String> stringToStringList(String value, List<String> defaultValue, Pattern splitPattern) {
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        List<String> ret = new ArrayList<>();
        for (String splitValue : splitPattern.split(value)) {
            if (!splitValue.isEmpty()) {
                ret.add(splitValue);
            }
        }
        return ret;
    }

    public static abstract class String2NativeTypeConverter<T, R> implements CacheSupportingFunction<T,R> {

        protected final Function<? super T,String> wrapped;

        protected final R defaultValue;

        private String2NativeTypeConverter(Function<? super T,String> wrapped, R defaultValue) {
            this.wrapped = wrapped;
            this.defaultValue = defaultValue;
        }

        @Override
        public final boolean resultsAreCacheable() {
            if (wrapped instanceof CacheSupportingFunction) {
                return ((CacheSupportingFunction<? super T,String>) wrapped).resultsAreCacheable();
            }
            return false;
        }

    }

    public static final <T> CacheSupportingFunction<T,Boolean> getStringToBooleanFunction(Function<T,String> function, boolean defaultValue) {
        return new String2NativeTypeConverter<>(function, Boolean.valueOf(defaultValue)) {
            @Override
            public final Boolean apply(T input) {
                return Boolean.valueOf(NativeTypeConversion.stringToBoolean(
                    wrapped.apply(input),
                    defaultValue.booleanValue()
                ));
            }
        };
    }

    public static final <T> CacheSupportingFunction<T,Integer> getStringToIntFunction(Function<T,String> function, int defaultValue) {
        return new String2NativeTypeConverter<>(function, Integer.valueOf(defaultValue)) {
            @Override
            public final Integer apply(T input) {
                return Integer.valueOf(NativeTypeConversion.stringToInt(wrapped.apply(input), defaultValue.intValue()));
            }
        };
    }

    public static final <T> CacheSupportingFunction<T,Long> getStringToLongFunction(Function<T,String> function, long defaultValue) {
        return new String2NativeTypeConverter<>(function, Long.valueOf(defaultValue)) {
            @Override
            public final Long apply(T input) {
                return Long.valueOf(NativeTypeConversion.stringToLong(wrapped.apply(input), defaultValue.longValue()));
            }
        };
    }

    public static final <T> CacheSupportingFunction<T,Double> getStringToDoubleFunction(Function<T,String> function, double defaultValue) {
        return new String2NativeTypeConverter<>(function, Double.valueOf(defaultValue)) {
            @Override
            public final Double apply(T input) {
                return Double.valueOf(NativeTypeConversion.stringToDouble(
                    wrapped.apply(input),
                    defaultValue.doubleValue()
                ));
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

}

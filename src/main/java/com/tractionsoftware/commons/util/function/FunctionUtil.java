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

package com.tractionsoftware.commons.util.function;

import java.util.function.Function;

/**
 * Contains some useful methods and objects for use with Java's functional programming features.
 *
 * @author Dave Shepperton
 */
public final class FunctionUtil {

    private FunctionUtil() {
    }

    /**
     * Returns a {@link Function} which always produces the given statically supplied value.
     *
     * @param value
     *     the value to be returned by any invocation of the {@link Function} returned by this method.
     * @return a {@link Function} which always produces the given statically supplied value.
     */
    public static final <D, T> CacheSupportingFunction<D,T> forConstantValue(final T value) {
        return new CacheSupportingFunction<>() {
            @Override
            public final T apply(D data) {
                return value;
            }

            @Override
            public final boolean resultsAreCacheable() {
                return true;
            }

            @Override
            public final String toString() {
                return "constant value function (" + value + ")";
            }
        };
    }

    /**
     * Returns a typed {@link Function} that always returns null.
     *
     * @return a typed {@link Function} that always returns null.
     */
    public static final <D, T> CacheSupportingFunction<D,T> nullValueFunction() {
        return forConstantValue(null);
    }

    public static final <T, R> com.google.common.base.Function<T,R> asGuavaFunction(final Function<T,R> function) {

        if (function == null) {
            return null;
        }

        return new com.google.common.base.Function<>() {

            @Override
            public final String toString() {
                return "Guava function wrapper {" + function + "}";
            }

            @Override
            public final R apply(T value) {
                return function.apply(value);
            }

        };

    }

}

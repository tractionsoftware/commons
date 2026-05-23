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

import java.util.function.Supplier;

/**
 * @author Dave Shepperton
 */
public final class SuppliersUtil {

    /**
     * Returns a {@link Supplier} which always produces the given statically supplied value.
     *
     * @param value
     *     the value to be returned by any invocation of the {@link Supplier} returned by this method.
     * @return a {@link Supplier} which always produces the given statically supplied value.
     */
    public static final <T> CacheSupportingSupplier<T> forConstantValue(final T value) {
        return new CacheSupportingSupplier<>() {
            @Override
            public final T get() {
                return value;
            }

            @Override
            public final boolean resultsAreCacheable() {
                return true;
            }

            @Override
            public final String toString() {
                return "constant value supplier (" + value + ")";
            }
        };
    }

    public static final <T> CacheSupportingSupplier<T> nullValueSupplier() {
        return forConstantValue(null);
    }

}

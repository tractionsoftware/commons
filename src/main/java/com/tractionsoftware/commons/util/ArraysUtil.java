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

package com.tractionsoftware.commons.util;

import java.util.*;

/**
 * Utility methods for dealing with arrays.
 *
 * <p>
 * It is safe to pass null to these methods unless they declare otherwise.
 *
 * @author Dave Shepperton
 */
public final class ArraysUtil {

    private ArraysUtil() {
    }

    /**
     * Creates a {@link LinkedHashSet} containing all the elements in the given array.
     *
     * @param arr
     *     the array.
     * @return a {@link LinkedHashSet} containing all the elements in the given array, or an empty LinkedHashSet if the
     *     given array is null.
     */
    public static final <T> LinkedHashSet<T> toLinkedHashSet(T[] arr) {
        if (arr == null) {
            return new LinkedHashSet<T>(0);
        }
        return new LinkedHashSet<T>(Arrays.asList(arr));
    }

    /**
     * Returns the given array viewed as a {@link List}.
     *
     * @param arr
     *     the array from which a {@link List} is to be created.
     * @return the given array viewed as a {@link List}; or, if the array is null, an empty List.
     */
    public static final <T> List<T> asList(T[] arr) {
        if (arr == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(arr);
    }

    /**
     * "Safely" produces a {@link String} representation of the given array.
     *
     * @param array
     *     the array for which a {@link String} representation is to be produced.
     * @return a {@link String} representation of the given {@link Iterable}.
     */
    public static final String safeToString(Object[] array) {
        return CollectionUtil.safeToStringImpl(asList(array), "array");
    }
}

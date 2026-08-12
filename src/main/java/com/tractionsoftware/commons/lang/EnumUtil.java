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

import org.apache.commons.lang3.StringUtils;

public final class EnumUtil {

    private EnumUtil() {
    }

    /**
     * This method is similar to {@link Enum#valueOf(Class, String)}, but performs case-insensitive comparison on the
     * input string, handles null, and will return a default value if no match is found.
     *
     * @param enumType
     *     the {@link Class} representing the desired type of {@link Enum}'s.
     * @param str
     *     the {@link String} to be converted.
     * @param defaultValue
     *     the default value to return if no matching enum constant is found for the given {@link Enum} type.
     * @return the matching value from the given Enum values, as matched by case-insensitive lexical comparison on the
     *     name of each value, if one can be so identified; the given default value otherwise.
     */
    public static <E extends Enum<E>> E enumFromString(Class<E> enumType, String str, E defaultValue) {

        if (enumType == null || StringUtils.isBlank(str)) {
            return defaultValue;
        }

        for (E value : enumType.getEnumConstants()) {
            if (value.name().equalsIgnoreCase(str)) {
                return value;
            }
        }

        return defaultValue;

    }

}

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

import com.tractionsoftware.commons.util.CollectionsUtil;
import com.tractionsoftware.commons.util.function.SuppliersUtil;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Utility methods that apply to Objects in general.
 *
 * @author Dave Shepperton
 */
public final class ObjectsUtil {

    /**
     * Not instantiable.
     */
    private ObjectsUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectsUtil.class.getName());

    private static final record SafeToStringWrapper(Object object, String defaultValue) {

        @Nonnull
        @Override
        public final String toString() {
            return safeToString(object, defaultValue);
        }

    }

    private static final record ToStringDynamic(Supplier<String> getString) {

        @Nonnull
        @Override
        public final String toString() {
            return SuppliersUtil.safeGet(getString, "?");
        }

    }

    /**
     * Returns either the given value if it matches the given {@link Predicate}, or a default value produced by the
     * given {@link Supplier}.
     *
     * @param value
     *     the object to examine.
     * @param matcher
     *     the {@link Predicate} that will be used to test the given value.
     * @param defaultSupplier
     *     a {@link Supplier} that can be used to produce a default value in case the given value is null.
     * @return the given value if it matches the given {@link Predicate}; or a default value produced by the given
     *     {@link Supplier} otherwise.
     */
    public static <T> T getMatchedOrDefault(T value, Predicate<? super T> matcher, Supplier<? extends T> defaultSupplier) {
        if (matcher.test(value)) {
            return value;
        }
        return defaultSupplier.get();
    }

    /**
     * Returns the given Object cast to the given type using {@link Class#cast(Object)}, as long as the given Object is
     * not null and the {@link Class#isInstance(Object)} returns true for the Object.
     *
     * @param value
     *     the Object to be cast.
     * @param cls
     *     the {@link Class} representing the desired target type.
     * @return the given Object cast to the given type using {@link Class#cast(Object)}, as long as the given Object is
     *     not null and the {@link Class#isInstance(Object)} returns true for the Object; null otherwise.
     */
    public static <T> T castIfAssignmentCompatible(Object value, Class<T> cls) {
        if (value == null) {
            return null;
        }
        if (cls.isInstance(value)) {
            return cls.cast(value);
        }
        return null;
    }

    /**
     * Combines the existing hash with the hash code of the given Object, suitable for adding a hash
     *
     * <p>
     * Specifically, the existing hash is multiplied by 31, and the hash code of the given Object is added to the
     * result. This is exactly what {@link Objects#hash(Object...)} and {@link java.util.Arrays#hashCode(Object[])}
     * does.
     *
     * @param hash
     *     the existing hash code.
     * @param object
     *     the Object whose hash code is to be combined with the existing hash.
     * @return the combined value of the given hash code with that of the given Object.
     */
    public static int getCombinedHash(int hash, Object object) {
        hash *= 31;
        if (object != null) {
            return hash + object.hashCode();
        }
        return hash;
    }

    /**
     * Invoking this method is the same as invoking {@link #safeToString(Object, String)} and passing null as the
     * argument for the default value. Thus, if the given Object is null or if a {@link RuntimeException} is raised
     * while executing its {@link Object#toString()} method, this method will return null.
     *
     * @param object
     *     the Object whose {@link Object#toString()} method is to be invoked.
     * @return the result of invoking {@link Objects#toString(Object, String)}; or null if a {@link RuntimeException} is
     *     raised (presumably while executing the given Object's {@link Object#toString()} method).
     */
    public static String safeToString(Object object) {
        return safeToString(object, null);
    }

    /**
     * Invokes {@link Objects#toString(Object, String)} on the given Object without allowing any
     * {@link RuntimeException}s to propagate.
     *
     * <p>
     * This method is primarily intended for debug/diagnostic purposes, where it is very undesirable to have
     * RuntimeExceptions propagating, but where trapping such RuntimeExceptions is not likely to lead to hidden issues.
     *
     * @param object
     *     the Object whose {@link Object#toString()} method is to be invoked.
     * @return the result of invoking {@link Objects#toString(Object, String)}; or, if a {@link RuntimeException} is
     *     raised (presumably while executing the given Object's {@link Object#toString()} method), the given default
     *     value.
     */
    public static String safeToString(Object object, String defaultValue) {
        try {
            return Objects.toString(object, defaultValue);
        }
        catch (RuntimeException e) {
            LOGGER.warn("There was an unexpected error converting an {}", safeClassNameToString(object), e);
        }
        return defaultValue;
    }

    /**
     * Safely converts all the members of the given {@link Collection} to {@link String}s. In this case, "safely" means
     * that the conversion is done via {@link #safeToString(Object)}.
     *
     * @param coll
     *     the {@link Collection} whose members are to be converted to {@link String}s.
     * @return a String[] containing all members of the given {@link Collection} converted to {@link String}s.
     */
    public static String[] safeToStringArray(Collection<?> coll) {
        return toStringArray(coll, ObjectsUtil::safeToString);
    }

    /**
     * Converts all the members of the given {@link Collection} to {@link String}s. Null values are mapped to null.
     *
     * @param coll
     *     the {@link Collection} whose members are to be converted to {@link String}s.
     * @return a String[] containing all members of the given {@link Collection} converted to {@link String}s.
     */
    public static String[] toStringArray(Collection<?> coll) {
        return toStringArray(coll, ObjectsUtil::toStringOrNull);
    }

    public static String[] toStringArray(Collection<?> coll, Function<Object,String> converter) {

        if (CollectionsUtil.isNullOrEmpty(coll)) {
            return new String[0];
        }

        int sz = coll.size();

        List<String> list = new ArrayList<>(sz);
        coll.stream()
            .map(converter)
            .forEach(list::add);

        String[] ret = new String[sz];
        list.toArray(ret);
        return ret;

    }

    /**
     * Converts the given {@link Object} to a {@link String}, mapping null to null rather than to the String "null".
     */
    public static String toStringOrNull(Object value) {
        return Objects.toString(value, null);
    }

    public static Object safeToStringObject(Object obj) {
        return safeToStringObject(obj, "?");
    }

    public static Object safeToStringObject(Object obj, String defaultValue) {
        return new SafeToStringWrapper(obj, defaultValue);
    }

    public static final Object safeClassNameToString(Object object) {
        if (object == null) {
            return "[null value]";
        }
        return new ToStringDynamic(() -> object.getClass().getName());
    }

    public static final Object safeToStringObject(Supplier<String> toString) {
        return safeToStringObject(toString, "?");
    }

    public static final Object safeToStringObject(Supplier<String> toString, String defaultValue) {
        if (toString == null) {
            return defaultValue;
        }
        return new ToStringDynamic(() -> SuppliersUtil.safeGet(toString, defaultValue));
    }

    public static void requireInstanceOf(Object value, Class<?> superType) {
        requireInstanceOf(value, superType, null);
    }

    public static void requireInstanceOf(Object value, Class<?> superType, String message) {
        if (!superType.isInstance(value)) {
            if (message == null) {
                throw new IllegalArgumentException("Wrong type");
            }
            throw new IllegalArgumentException(message);
        }
    }

}

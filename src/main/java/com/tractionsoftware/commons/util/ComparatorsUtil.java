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

import java.util.*;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.logging.Level;

/**
 * Helpful methods for handling {@link Comparator}s.
 *
 * @author Dave Shepperton
 */
public final class ComparatorsUtil {

    private ComparatorsUtil() {}

    /**
     * A variation of {@link String#CASE_INSENSITIVE_ORDER} which
     * handles null values safely, and treats them as going before
     * (less than) non-null values.
     */
    public static final Comparator<String> STRING_CASE_INSENSITIVE_ORDER_WITH_NULLS_FIRST =
        Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER);

    private static final class CompositeComparator<T> implements Comparator<T> {

        private final List<Comparator<? super T>> comparators;

        private CompositeComparator(Collection<? extends Comparator<? super T>> comparators) {
            this.comparators = new ArrayList<>(comparators);
        }

        @Override
        public final int compare(T t1, T t2) {
            for (Comparator<? super T> comp : comparators) {
                int result = comp.compare(t1, t2);
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        }

        @Override
        public final String toString() {
            return "CompositeComparator[" + comparators.size() + "]";
        }

        @Override
        public final boolean equals(Object other) {
            if (!(other instanceof CompositeComparator<?> otherComp)) {
                return false;
            }
            if (comparators.equals(otherComp.comparators)) {
                return true;
            }
            return false;
        }

        @Override
        public final int hashCode() {
            return Objects.hash(comparators);
        }

    }

    public static final class SafeComparator<T> implements Comparator<T> {

        private final Comparator<T> comparator;

        private SafeComparator(Comparator<T> comparator) {
            this.comparator = comparator;
        }

        @Override
        public final int compare(T t1, T t2) {
            try {
                return comparator.compare(t1, t2);
            }
            catch (RuntimeException e) {
                ObjectsUtil.getLogger().log(Level.WARNING, "SafeComparator caught a RuntimeException", e);
            }
            return 0;
        }

        @Override
        public final String toString() {
            return "SafeComparator[" + comparator + "]";
        }

        @Override
        public final boolean equals(Object other) {
            if (!(other instanceof SafeComparator<?> otherSafe)) {
                return false;
            }
            if (comparator.equals(otherSafe.comparator)) {
                return true;
            }
            return false;
        }

        @Override
        public final int hashCode() {
            return Objects.hash(comparator);
        }

    }

    /**
     * Returns a {@link Comparator} representing a composite of the
     * given {@link Collection} of Comparators.
     *
     * <p>
     * This method offers a similar functionality to that offered by
     * {@link Comparator#thenComparing(Comparator)}, but accommodates
     * combining a Collection of Comparators for <em>super-types</em>
     * of the target type in a type-safe manner.
     *
     * <p>
     * Specifically, the composite Comparator's
     * {@link Comparator#compare(Object, Object)} method will try each
     * of the given Comparators' compare methods in turn until one
     * returns a non-zero value. It will return 0 as a last resort to
     * indicate that none of the supplied Comparators revealed any
     * difference in ordering between the two objects.
     *
     * <p>
     * The Comparators are used in the order in which they appear in
     * the given Collection, although the given Collection is copied
     * to a List so that the Comparator will remain the same if the
     * original Collection is modified. This means that the Collection
     * should generally be sorted in descending order of significance.
     *
     * @param comparators
     *            contains the {@link Comparator} delegates, with an
     *            ordering of elements (specifically, as obtained via
     *            its {@link Collection#toArray()} method) reflecting
     *            their relative precedence.
     * @return a {@link Comparator} which is a composite of the given
     *         {@link Collection} of Comparators; or null if the given
     *         Collection is null.
     */
    public static final <T> Comparator<T> createCompositeComparator(Collection<? extends Comparator<? super T>> comparators) {
        if (CollectionsUtil.isNullOrEmpty(comparators)) {
            return null;
        }
        return new CompositeComparator<>(comparators);
    }

    public static final <T> Comparator<T> createCompositeCaseInsensitiveStringComparator(Collection<? extends Function<T,String>> functions) {
        if (CollectionsUtil.isNullOrEmpty(functions)) {
            return null;
        }
        List<Comparator<T>> list = new ArrayList<>(functions.size());
        for (Function<T,String> function : functions) {
            list.add(Comparator.comparing(function, STRING_CASE_INSENSITIVE_ORDER_WITH_NULLS_FIRST));
        }
        return createCompositeComparator(list);
    }

    /**
     * Returns a {@link Comparator} whose
     * {@link Comparator#compare(Object, Object)} method will invoke
     * that of the supplied Comparator, and will return 0 and log a
     * warning if any {@link RuntimeException} is raised (thus
     * "safe").
     *
     * @param comparator
     *            the {@link Comparator} to be made "safe".
     * @return a {@link Comparator} whose
     *         {@link Comparator#compare(Object, Object)} method will
     *         invoke that of the supplied Comparator, and will return
     *         0 and log a warning if any {@link RuntimeException} is
     *         raised.
     */
    public static final <T> Comparator<T> safeComparator(Comparator<T> comparator) {
        if (comparator == null || comparator instanceof SafeComparator<?>) {
            return comparator;
        }
        return new SafeComparator<>(comparator);
    }

    /**
     * Returns a {@link Comparator} based upon the difference between
     * the scores assigned to the elements being ordered.
     */
    public static final <T> Comparator<T> scoringComparator(ToIntFunction<T> scoreProvider) {
        return Comparator.nullsLast(Comparator.comparingInt(scoreProvider).reversed());
    }

}

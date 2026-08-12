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

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.tractionsoftware.commons.util.CollectionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Dave Shepperton
 */
public final class PredicateUtil {

    private PredicateUtil() {
    }

    /**
     * A simple {@link Predicate} implementation whose {@link #test(Object)} method will return true or false if the
     * object is contained in the supplied {@link Collection} based upon whether the includeIfMatched constructor
     * parameter is true or false.
     *
     * @param <T>
     *     the type of elements to be matched by this {@link Predicate}.
     * @author Dave Shepperton
     */
    private static final class IncludeExcludeCollectionElementsPredicate<T> implements Predicate<T> {

        /**
         * Containing the elements to be included or excluded.
         */
        private final Collection<?> elements;

        private final boolean includeIfContains;

        private IncludeExcludeCollectionElementsPredicate(Collection<?> elements, boolean includeIfContains) {
            Objects.requireNonNull(elements);
            this.elements = elements;
            this.includeIfContains = includeIfContains;
        }

        /**
         * Returns the value of the {@link #includeIfContains} field if the given object appears in the
         * {@link Collection} of elements; and its inverse otherwise.
         */
        @Override
        public final boolean test(T object) {
            if (elements.contains(object)) {
                return includeIfContains;
            }
            return !includeIfContains;
        }

        @Override
        public final String toString() {
            return includeIfContains ? "matching only certain elements" : "matching all but certain elements";
        }

        @Override
        public final boolean equals(Object other) {
            if (!(other instanceof IncludeExcludeCollectionElementsPredicate<?> otherPredicate)) {
                return false;
            }
            if (includeIfContains == otherPredicate.includeIfContains &&
                elements.equals(otherPredicate.elements)) {
                return true;
            }
            return false;
        }

        @Override
        public final int hashCode() {
            return Objects.hash(includeIfContains, elements);
        }

    }

    public static final <T> CacheSupportingPredicate<T> cacheSupporting(final Predicate<T> predicate) {

        return new CacheSupportingPredicate<>() {

            @Override
            public final boolean test(T value) {
                return predicate.test(value);
            }

            @Override
            public final String toString() {
                return "CacheSupporting[" + predicate + "]";
            }

            @Override
            public final boolean resultsAreCacheable() {
                return true;
            }

        };

    }

    public static final <T> CacheSupportingPredicate<T> alwaysTrue() {
        return cacheSupporting(Predicates.alwaysTrue());
    }

    public static final <T> CacheSupportingPredicate<T> alwaysFalse() {
        return cacheSupporting(Predicates.alwaysFalse());
    }

    public static final Predicate<Object> onlyThese(Collection<?> these) {
        if (these == null) {
            return Predicates.alwaysFalse();
        }
        return new IncludeExcludeCollectionElementsPredicate<>(these, true);
    }

    public static final Predicate<Object> onlyOtherThanThese(Collection<?> these) {
        if (these == null) {
            return Predicates.alwaysTrue();
        }
        return new IncludeExcludeCollectionElementsPredicate<>(these, false);
    }

    /**
     * Returns a compound "filter" {@link Predicate} based upon the given Predicates.
     *
     * <p>
     * Specifically, its {@link Predicate#test(Object)} method will serially apply the given Predicates (in the order
     * they appear in an {@link java.util.Iterator} returns by the given {@link Collection}) and returns true if any
     * test method returns true. That is, the test method return value is equivalent to a lazily evaluated /
     * short-circuited boolean OR expression across all the given Predicates.
     *
     * <p>
     * This method could be helpful for use with a method such as
     * {@link CollectionUtil#filteredIterator(java.util.Iterator, Predicate)} .
     *
     * @param filters
     *     the {@link Collection} of {@link Predicates} which will be used to compose the Predicate returned by this
     *     method. This must not be null or empty.
     * @return a compound "filter" {@link Predicate} based upon the given Predicates.
     * @throws IllegalArgumentException
     *     if the given {@link Collection} is empty.
     */
    public static final <E> Predicate<E> getFirstOrMultiFilter(Collection<? extends Predicate<? super E>> filters)
        throws IllegalArgumentException {

        if (CollectionUtil.isEmpty(filters)) {
            throw new IllegalArgumentException();
        }

        final List<? extends Predicate<? super E>> list = new ArrayList<Predicate<? super E>>(filters);

        return value -> {
            for (Predicate<? super E> p : list) {
                if (p.test(value)) {
                    return true;
                }
            }
            return false;
        };

    }


    public static final <T> Predicate<? super T> and(Predicate<? super T> first, Predicate<? super T> second) {
        if (first == null) {
            if (second == null) {
                return Predicates.alwaysTrue();
            }
            return second;
        }
        if (second == null) {
            return first;
        }
        return and(ImmutableList.of(first, second));
    }

    public static final <T> Predicate<? super T> or(Predicate<? super T> first, Predicate<? super T> second) {
        if (first == null) {
            if (second == null) {
                return Predicates.alwaysFalse();
            }
            return second;
        }
        if (second == null) {
            return first;
        }
        return or(ImmutableList.of(first, second));
    }

    public static final <T> Predicate<? super T> and(Iterable<? extends Predicate<? super T>> components) {

        if (components == null || Iterables.isEmpty(components)) {
            return Predicates.alwaysTrue();
        }

        return new Predicate<>() {

            @Override
            public final String toString() {
                return "AND:" + components;
            }

            @Override
            public final boolean test(T object) {
                for (Predicate<? super T> p : components) {
                    if (!p.test(object)) {
                        return false;
                    }
                }
                return true;
            }

        };

    }

    public static final <T> Predicate<? super T> or(Iterable<? extends Predicate<? super T>> components) {

        if (components == null || Iterables.isEmpty(components)) {
            return Predicates.alwaysFalse();
        }

        return new Predicate<>() {

            @Override
            public final String toString() {
                return "OR:" + components;
            }

            @Override
            public final boolean test(T object) {
                for (Predicate<? super T> p : components) {
                    if (p.test(object)) {
                        return true;
                    }
                }
                return false;
            }

        };

    }

}

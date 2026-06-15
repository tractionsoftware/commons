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

import com.google.common.collect.*;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.tractionsoftware.commons.lang.ObjectUtil;
import com.tractionsoftware.commons.lang.StringUtil;
import com.tractionsoftware.commons.util.function.FunctionsUtil;
import com.tractionsoftware.commons.util.function.PredicateUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Some helpful methods for manipulating Collections and related types.
 *
 * <p>
 * It is safe to pass null arguments to all methods unless otherwise specified.
 *
 * <p>
 * Objects produced by the methods in this class should not be assumed to be thread-safe unless otherwise specified, or
 * unless the return type is intrinsically thread-safe.
 *
 * @author Dave Shepperton
 */
public final class CollectionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionUtil.class);

    /**
     * A simpler variation of the {@link Map} interface that supports get and remove only.
     *
     * @param <K>
     *     the type of the map's keys.
     * @param <V>
     *     the type of the map's values.
     * @author Dave Shepperton
     * @see CollectionUtil#mapValueIterator(MapIteratorAdapter, Iterable)
     * @see CollectionUtil#mapValueIterator(MapIteratorAdapter, Iterator)
     * @see CollectionUtil#mapValueKeyRangeIterator(MapIteratorAdapter, int, int)
     */
    public static interface MapIteratorAdapter<K, V> {

        /**
         * Retrieves the value corresponding to the given key. Similar to {@link Map#get(Object)}.
         *
         * @param key
         *     the key whose value should be retrieved.
         * @return the value corresponding to the given key.
         * @throws IllegalArgumentException
         *     may be thrown if the key is considered invalid.
         */
        public V get(K key);

        /**
         * Removes the key-value pair for the given key, if supported. Similar to {@link Map#remove(Object)}, without
         * returning the value from the removed key-value pair.
         *
         * @param key
         *     the key for the key-value that should be removed.
         * @throws IllegalArgumentException
         *     may be thrown if the key is considered invalid.
         * @throws UnsupportedOperationException
         *     may be thrown if the implementation doesn't support removal of key-value pairs.
         */
        public void remove(K key);

    }

    /**
     * An Iterator that covers the values from the given map (as a MapIteratorAdapter) corresponding to the keys covered
     * by the given Iterator.
     *
     * @param <K>
     *     the type of the map's keys.
     * @param <V>
     *     the type of the map's values.
     * @author Dave Shepperton
     */
    private static final class MapValueIterator<K, V> implements Iterator<V> {

        private final MapIteratorAdapter<? super K,? extends V> objects;

        private final Iterator<? extends K> indexIter;

        /**
         * The key corresponding to the value that was last returned by the {@link #next()} method.
         */
        private K lastIdx;

        private MapValueIterator(MapIteratorAdapter<? super K,? extends V> objects, Iterator<? extends K> indicesIter) {
            Objects.requireNonNull(objects, "objects");
            Objects.requireNonNull(indicesIter, "indices");
            this.objects = objects;
            this.indexIter = indicesIter;
        }

        /**
         * Returns true if the key iteration has further elements.
         */
        @Override
        public final boolean hasNext() {
            return indexIter.hasNext();
        }

        /**
         * Returns the value corresponding to the next key in the key iteration.
         */
        @Override
        public final V next() {
            lastIdx = indexIter.next();
            try {
                return objects.get(lastIdx);
            }
            catch (IllegalArgumentException e) {
                throw new NoSuchElementException(lastIdx + " (" + e.getMessage() + ")");
            }
        }

        /**
         * Removes the last key-value pair from the underlying Map whose value was returned by the {@link #next()}
         * method.
         */
        @Override
        public final void remove() {
            objects.remove(lastIdx);
        }

    }

    /**
     * A simple Iterator implementation that returns Integer instances for each int value in a given range.
     *
     * @author Dave Shepperton
     */
    private static final class IntegerRangeIterator implements Iterator<Integer> {

        /**
         * The first element in the range (used only for toString, equals and hashCode).
         */
        private final int first;

        /**
         * The last element in the range.
         */
        private final int last;

        /**
         * The current element, to be used, if it is not out of range, for the next value to be returned by
         * {@link #next()}.
         */
        private int current;

        private IntegerRangeIterator(int first, int last) {
            this.first = first;
            this.current = first;
            this.last = last;
        }

        @Override
        public final String toString() {
            return "IntegerRangeIterator[" + first + ":" + last + "] (next = " + current + ")";
        }

        @Override
        public final boolean equals(Object other) {
            if (!(other instanceof IntegerRangeIterator iter)) {
                return false;
            }
            if (this.first == iter.first && this.last == iter.last) {
                return true;
            }
            return false;
        }

        @Override
        public final int hashCode() {
            return Objects.hash(first, last);
        }

        /**
         * Returns false if the current element is greater than the last element.
         */
        @Override
        public final boolean hasNext() {
            if (current > last) {
                return false;
            }
            return true;
        }

        /**
         * Returns the Integer for the current int value, as long as it is not outside the requested range.
         *
         * @throws NoSuchElementException
         *     if the current element is outside the requested range.
         */
        @Override
        public final Integer next() {
            if (hasNext()) {
                Integer ret = current;
                current++;
                return ret;
            }
            throw new NoSuchElementException(String.valueOf(current));
        }

        /**
         * Throws an UnsupportedOperationException because this Iterator implementation does not support removal.
         */
        @Override
        public final void remove() {
            throw new UnsupportedOperationException();
        }

    }

    private static final class SafeIndex2ListValue<T> implements IntFunction<T> {

        private final List<? extends T> list;

        private SafeIndex2ListValue(List<? extends T> list) {
            Objects.requireNonNull(list, "list");
            this.list = list;
        }

        @Override
        public final T apply(int value) {
            if (value < 0 || value >= list.size()) {
                return null;
            }
            return list.get(value);
        }

        @Override
        public final String toString() {
            return "SafeIndex2ListValue[" + list.getClass().getName() + "]";
        }

        @Override
        public final boolean equals(Object other) {
            if (other instanceof SafeIndex2ListValue<?> otherListF &&
                list.equals(otherListF.list)) {
                return true;
            }
            return false;
        }

        @Override
        public final int hashCode() {
            return Objects.hash(list);
        }

    }

    private static final class SafeMapKey2Value<K, V> implements Function<K,V> {

        private final Map<? super K,? extends V> map;

        private SafeMapKey2Value(Map<? super K,? extends V> map) {
            Objects.requireNonNull(map, "map");
            this.map = map;
        }

        @Override
        public final V apply(K key) {
            try {
                return map.get(key);
            }
            catch (NullPointerException e) {
                LOGGER.debug(
                    "The Map {} ({}) may not support null values.",
                    StringUtil.truncatedToStringForLog(map),
                    ObjectUtil.safeClassNameToString(map),
                    e
                );
            }
            return null;
        }

        @Override
        public final String toString() {
            return "SafeMapKey2Value[" + map.getClass().getName() + "]";
        }

        @Override
        public final boolean equals(Object other) {
            if (other instanceof SafeMapKey2Value<?,?> otherMap &&
                map.equals(otherMap.map)) {
                return true;
            }
            return false;
        }

        @Override
        public final int hashCode() {
            return ~map.hashCode();
        }

    }

    private static abstract class AbstractUnmodifiableSequencedSet<T> implements SequencedSet<T> {

        @Override
        public final boolean add(T t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public final boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Nonnull
        @Override
        public SequencedSet<T> reversed() {
            return this;
        }

        @Override
        public final boolean addAll(@Nonnull Collection<? extends T> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public final boolean removeAll(@Nonnull Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public final boolean retainAll(@Nonnull Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public final void clear() {
            throw new UnsupportedOperationException();
        }

    }

    private static final SequencedSet<Object> EMPTY_SEQUENCED_SET = new AbstractUnmodifiableSequencedSet<>() {

        @Override
        public final int size() {
            return 0;
        }

        @Override
        public final boolean isEmpty() {
            return true;
        }

        @Override
        public final boolean contains(Object o) {
            return false;
        }

        @Nonnull
        @Override
        public final Iterator<Object> iterator() {
            return Collections.emptyIterator();
        }

        @Nonnull
        @Override
        public final Object[] toArray() {
            return ArrayUtils.EMPTY_OBJECT_ARRAY;
        }

        @Nonnull
        @Override
        public <T> T[] toArray(@Nonnull T[] a) {
            if (a.length > 0) {
                a[0] = null;
            }
            return a;
        }

        @Override
        public final boolean containsAll(Collection<?> c) {
            if (c.isEmpty()) {
                return true;
            }
            return false;
        }

    };

    private static final class SingletonSequencedSet<T> extends AbstractUnmodifiableSequencedSet<T> {

        private final T element;

        private SingletonSequencedSet(T element) {
            this.element = element;
        }

        @Override
        public final int size() {
            return 1;
        }

        @Override
        public final boolean isEmpty() {
            return false;
        }

        @Override
        public final boolean contains(Object o) {
            if (o == null) {
                return false;
            }
            return element.equals(o);
        }

        @Nonnull
        @Override
        public final Iterator<T> iterator() {
            return Iterators.singletonIterator(element);
        }

        @Nonnull
        @Override
        public final Object[] toArray() {
            return new Object[] { element };
        }

        @Nonnull
        @SuppressWarnings("unchecked")
        @Override
        public final <U> U[] toArray(@Nonnull U[] a) {

            U useElement;
            try {
                useElement = (U) element;
            }
            catch (ClassCastException e) {
                throw new ArrayStoreException(e.getMessage());
            }

            if (a.length >= 1) {
                a[0] = useElement;
                if (a.length > 1) {
                    a[1] = null;
                }
                return a;
            }

            return ArrayUtils.toArray(useElement);

        }

        @Override
        public final boolean containsAll(@Nonnull Collection<?> c) {
            if (CollectionUtil.isEmpty(c)) {
                return true;
            }
            for (Object o : c) {
                if (!contains(o)) {
                    return false;
                }
            }
            return true;
        }

    }

    private CollectionUtil() {
    }

    /**
     * Returns true if the given {@link Collection} is either null or empty.
     *
     * @param coll
     *     the {@link Collection} to test.
     * @return true if the given {@link Collection} is either null or empty; false otherwise.
     */
    public static final boolean isEmpty(Collection<?> coll) {
        if (coll == null || coll.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the given {@link Collection} is neither null or empty.
     *
     * @param coll
     *     the {@link Collection} to test.
     * @return true if the given {@link Collection} is neither null or empty; false otherwise.
     */
    public static final boolean isNotEmpty(Collection<?> coll) {
        return !isEmpty(coll);
    }

    /**
     * Returns true if the given {@link Collection} is not null and contains the given element.
     *
     * <p>
     * This method catches {@link NullPointerException} thrown by {@link Collection#contains(Object)} in case the
     * Collection implementation does not support null values.
     *
     * @param coll
     *     the {@link Collection} in which to search for the given element.
     * @param element
     *     the element to search for in the {@link Collection}.
     * @return true if the given {@link Collection} is not null and contains the given element; false otherwise.
     */
    public static final boolean contains(Collection<?> coll, Object element) {
        if (coll == null) {
            return false;
        }
        try {
            return coll.contains(element);
        }
        catch (NullPointerException e) {
            LOGGER.debug(
                "The Collection {} ({}) may not support null keys.",
                StringUtil.truncatedToStringForLog(coll),
                ObjectUtil.safeClassNameToString(coll),
                e
            );
        }
        return false;
    }

    /**
     * Returns true if the given {@link Map} is either null or empty.
     *
     * @param map
     *     the {@link Map} to test.
     * @return true if the given {@link Map} is either null or empty; false otherwise.
     */
    public static final boolean isEmpty(Map<?,?> map) {
        if (map == null || map.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the given {@link Multimap} is either null or empty.
     *
     * @param map
     *     the {@link Multimap} to test.
     * @return true if the given {@link Multimap} is either null or empty; false otherwise.
     */
    public static final boolean isEmpty(Multimap<?,?> map) {
        if (map == null || map.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the given {@link Map} is neither null or empty.
     *
     * @param map
     *     the {@link Map} to test.
     * @return true if the given {@link Map} is neither null or empty; false otherwise.
     */
    public static final boolean isNotEmpty(Map<?,?> map) {
        return !isEmpty(map);
    }

    /**
     * Returns a {@link Function} which maps an {@link Integer}s to the element at that index in the given {@link List},
     * or to null if the given index is out of range.
     *
     * @param list
     *     the {@link List} whose elements are to be mapped.
     * @return a {@link Function} which maps an {@link Integer}s to the element at that index in the given {@link List}.
     */
    public static final <T> IntFunction<T> getListIndex2ListValueFunction(List<? extends T> list) {
        if (list == null) {
            return i -> null;
        }
        return new SafeIndex2ListValue<>(list);
    }

    /**
     * Returns a {@link Function} which maps keys to values using the given {@link Map}.
     *
     * <p>
     * The returned Function will prevent any {@link NullPointerException} thrown by {@link Map#get(Object)} in case the
     * Map implementation does not support null keys.
     *
     * @param map
     *     the {@link Map} whose key-value pairs will be used to define the returned {@link Function}.
     * @return a {@link Function} which maps keys to values using the given {@link Map}.
     */
    public static final <K, V> Function<K,V> getMapKey2ValueFunction(final Map<? super K,? extends V> map) {
        if (map == null) {
            return FunctionsUtil.nullValueFunction();
        }
        return new SafeMapKey2Value<>(map);
    }

    /**
     * Adds all the elements from the given {@link Iterable} source to the given destination {@link Collection}.
     *
     * <p>
     * This method defers to {@link Iterables#addAll(Collection, Iterable)}. The only difference is that this method
     * gracefully handles a null source Iterator or destination Collection.
     *
     * @param source
     *     the {@link Iterable} whose elements are being added to the destination Collection.
     * @param destination
     *     the Collection to which the elements will be added.
     */
    public static final <T> boolean addFromIterable(Iterable<? extends T> source, Collection<? super T> destination) {
        if (source == null || destination == null) {
            return false;
        }
        return Iterables.addAll(destination, source);
    }

    /**
     * Adds all the elements from the given {@link Iterator} source to the given destination {@link Collection}.
     *
     * <p>
     * This method defers to {@link Iterators#addAll(Collection, Iterator)}. The only difference is that this method
     * gracefully handles a null source Iterator or destination Collection.
     *
     * @param source
     *     the {@link Iterator} whose elements are being added to the destination Collection.
     * @param destination
     *     the Collection to which the elements will be added.
     * @return true if any elements were added to the destination {@link Collection} from the given source
     *     {@link Iterator}; false otherwise.
     */
    public static final <E> boolean addFromIterator(Iterator<? extends E> source, Collection<? super E> destination) {
        if (source == null || destination == null) {
            return false;
        }
        return Iterators.addAll(destination, source);
    }

    /**
     * Returns the first element from the {@link Iterable}, if it has at least one element.
     *
     * <p>
     * This method defers to {@link Iterables#getOnlyElement(Iterable, Object)}. The only difference is that this method
     * gracefully handles a null Iterable.
     *
     * @param coll
     *     the {@link Iterable} whose first element should be returned.
     * @param defaultValue
     *     the value to be returned in case the given {@link Iterable} is null or empty.
     * @return the first element from the {@link Iterable}, if it has at least one element; the given default value
     *     otherwise.
     */
    public static final <E> E firstOrDefault(Iterable<? extends E> coll, E defaultValue) {
        if (coll == null) {
            return defaultValue;
        }
        if (coll instanceof SequencedCollection<? extends E> seq) {
            if (seq.isEmpty()) {
                return null;
            }
            return seq.getFirst();
        }
        return Iterables.getOnlyElement(coll, defaultValue);
    }

    /**
     * Returns a HashMap with the same key-value pairs contained by the given Map. If the given Map is an instance of a
     * HashMap, it will be returned as-is. Otherwise, a new HashMap instance will be created containing all the
     * key-value pairs from the given Map.
     *
     * @param map
     *     the Map to be returned or copied to a new HashMap.
     * @return the given Map if it is an instance of HashMap; null if the given Map is null; otherwise, a new HashMap
     *     containing the same key-value pairs as the given Map, if it is not null.
     */
    public static final <K, V> HashMap<K,V> hashMap(Map<K,V> map) {
        if (map == null) {
            return null;
        }
        if (map instanceof HashMap) {
            return (HashMap<K,V>) map;
        }
        return new HashMap<>(map);
    }

    /**
     * Returns an ArrayList that contains the same elements that the given Collection contains. If the given Collection
     * is an instance of a ArrayList, it will be returned as-is. Otherwise, a new ArrayList instance will be created
     * containing all the elements from the given Collection.
     *
     * @param coll
     *     the List to be returned or copied to a new ArrayList.
     * @return the given List if it is an instance of ArrayList; null if the given List is null; otherwise, a new
     *     ArrayList that contains the same elements that the given Collection contains.
     */
    public static final <T> ArrayList<T> arrayList(Collection<T> coll) {
        if (coll == null) {
            return null;
        }
        if (coll instanceof ArrayList<T> arrayList) {
            return arrayList;
        }
        return new ArrayList<>(coll);
    }

    /**
     * Returns a HashSet that contains the same elements that the given Collection contains. If the given Collection is
     * an instance of a HashSet, it will be returned as-is. Otherwise, a new HashSet instance will be created containing
     * all the elements from the given Collection.
     *
     * @param coll
     *     the List to be returned or copied to a new ArrayList.
     * @return the given Collection if it is an instance of HashSet; null if the given Collection is null; otherwise, a
     *     new HashSet that contains the same elements that the given Collection contains.
     */
    public static final <T> HashSet<T> hashSet(Collection<T> coll) {
        if (coll == null) {
            return null;
        }
        if (coll instanceof HashSet) {
            return (HashSet<T>) coll;
        }
        return new HashSet<>(coll);
    }

    /**
     * Returns a LinkedHashSet that contains the same elements that the given Collection contains. If the given
     * Collection is an instance of a LinkedHashSet, it will be returned as-is. Otherwise, a new LinkedHashSet instance
     * will be created containing all the elements from the given Collection.
     *
     * @param coll
     *     the List to be returned or copied to a new ArrayList.
     * @return the given Collection if it is an instance of LinkedHashSet; null if the given Collection is null;
     *     otherwise, a new LinkedHashSet that contains the same elements that the given Collection contains.
     */
    public static final <T> LinkedHashSet<T> linkedHashSet(Collection<T> coll) {
        if (coll == null) {
            return null;
        }
        if (coll instanceof LinkedHashSet) {
            return (LinkedHashSet<T>) coll;
        }
        return new LinkedHashSet<>(coll);
    }

    /**
     * Clears the destination Collection, and then copies all the elements from the source Collection into it.
     *
     * @param source
     *     the Collection whose elements are to be copied into the destination Collection.
     * @param destination
     *     the Collection to be cleared and then populated by the elements from the source Collection.
     */
    public static final <E> void clearAndCopy(Collection<? extends E> source, Collection<? super E> destination) {
        if (destination == null) {
            return;
        }
        destination.clear();
        copy(source, destination);
    }

    /**
     * Copies all elements from the source Collection into the destination Collection, as long as the source is not null
     * or empty, and the destination is not null.
     *
     * @param source
     *     the Collection whose elements are to be copied into the destination Collection.
     * @param destination
     *     the Collection to which the elements from the source Collection should be added.
     */
    public static final <E> void copy(Collection<? extends E> source, Collection<? super E> destination) {
        if (source == null || destination == null || source.isEmpty()) {
            return;
        }
        destination.addAll(source);
    }

    /**
     * Copies all non-null elements from the source Collection into the destination Collection.
     *
     * @param source
     *     the Collection whose non-null elements are to be copied into the destination Collection.
     * @param destination
     *     the Collection to which the non-null elements from the source Collection should be added.
     * @return true if any elements were added to the destination {@link Collection}; false otherwise.
     */
    public static final <E> boolean copyNonNull(Collection<? extends E> source, Collection<? super E> destination) {
        if (source == null || destination == null || source.isEmpty()) {
            return false;
        }
        int size = destination.size();
        source.stream().filter(Objects::nonNull).forEach(destination::add);
        if (destination.size() > size) {
            return true;
        }
        return false;
    }

    /**
     * Clears the destination Collection, and then copies all the elements from the source Collection into it.
     *
     * @param source
     *     the Collection whose elements are to be copied into the destination Collection.
     * @param destination
     *     the Collection to be cleared and then populated by the elements from the source Collection.
     */
    public static final <K, V> void clearAndCopy(Map<? extends K,? extends V> source, Map<? super K,? super V> destination) {
        if (destination == null) {
            return;
        }
        destination.clear();
        copy(source, destination);
    }

    /**
     * Copies all the key-value pairs from the source Map to the destination Map.
     *
     * @param source
     *     the Map whose key-value pairs should be copied.
     * @param destination
     *     the Map to which the key-value pairs should be copied.
     */
    public static final <K, V> void copy(Map<? extends K,? extends V> source, Map<? super K,? super V> destination) {
        if (destination == null || isEmpty(source)) {
            return;
        }
        destination.putAll(source);
    }

    /**
     * Returns either the given Collection or an empty List if the Collection is null.
     *
     * @param original
     *     the List to be examined.
     * @return the given Collection if it is not null; an empty List otherwise.
     */
    public static final <T> Collection<T> emptyInsteadOfNull(Collection<T> original) {
        return Objects.requireNonNullElseGet(original, Collections::emptyList);
    }

    /**
     * Returns either the given List or an empty List if the List is null.
     *
     * @param original
     *     the List to be examined.
     * @return the given List if it is not null; an empty List otherwise.
     */
    public static final <T> List<T> emptyListInsteadOfNull(List<T> original) {
        return Objects.requireNonNullElseGet(original, Collections::emptyList);
    }

    /**
     * Returns either the given Set or an empty Set if the Set is null.
     *
     * @param original
     *     the Set to be examined.
     * @return the given Set if it is not null; an empty Set otherwise.
     */
    public static final <T> Set<T> emptySetInsteadOfNull(Set<T> original) {
        return Objects.requireNonNullElseGet(original, Collections::emptySet);
    }

    /**
     * Returns either the given Set or an empty Set if the Set is null.
     *
     * @param original
     *     the Set to be examined.
     * @return the given Set if it is not null; an empty Set otherwise.
     */
    public static final <T> SequencedSet<T> emptySequencedSetInsteadOfNull(SequencedSet<T> original) {
        return Objects.requireNonNullElseGet(original, CollectionUtil::emptySequencedSet);
    }

    /**
     * Puts the given key-value mapping into the given Map, or removes the map entry for the given key if the value is
     * null.
     *
     * @param map
     *     the Map to be modified.
     * @param key
     *     the key whose map entry is to be modified or removed.
     * @param value
     *     the value for the map entry to be set or removed.
     */
    public static final <K, V> void putOrRemove(Map<? super K,? super V> map, K key, V value) {
        if (map == null) {
            return;
        }
        if (value == null) {
            map.remove(key);
        }
        else {
            map.put(key, value);
        }
    }

    /**
     * Returns the size of the given Collection, or 0 if the Collection is null.
     *
     * @param coll
     *     the Collection whose size is to be determined.
     * @return the size of the given Collection, or 0 if the Collection is null.
     */
    public static final int size(Collection<?> coll) {
        if (coll == null) {
            return 0;
        }
        return coll.size();
    }

    /**
     * Copies all elements from the given {@link Iterable} into the given {@link Collection}.
     *
     * @param source
     *     the {@link Iterable} whose elements should be copied.
     * @param destination
     *     the destination {@link Collection} to which all the array's elements should be added.
     * @return true if any source elements were copied to the destination; false otherwise.
     */
    public static final <T> boolean copy(Iterable<? extends T> source, Collection<? super T> destination) {
        if (source == null || destination == null) {
            return false;
        }
        return Iterables.addAll(destination, source);
    }

    /**
     * Adds the given value to the given {@link Collection} if both the value and the Collection are not null.
     *
     * <p>
     * This method catches {@link NullPointerException} thrown by {@link Collection#add(Object)} in case the Collection
     * implementation does not support null values.
     *
     * @param value
     *     the value to be added, if it is not null.
     * @param coll
     *     the {@link Collection} to which the given value should be added, if the value is not null.
     * @return true if the value was successfully added to the given {@link Collection}; false otherwise.
     */
    @CanIgnoreReturnValue
    public static final <E> boolean addIfNotNull(E value, Collection<? super E> coll) {
        if (value == null || coll == null) {
            return false;
        }
        coll.add(value);
        return true;
    }

    /**
     * Puts the given key-value pair into the given {@link Map}, if both the value and the Map are not null.
     *
     * <p>
     * A null key will not prevent the pair from being added, but this method catches {@link NullPointerException}
     * thrown by {@link Map#put(Object, Object)} in case the Map implementation does not support null keys.
     *
     * @param key
     *     the key for the put operation.
     * @param value
     *     the value to be put into the map, if it is not null.
     * @param map
     *     the {@link Map} to which the given key-value pair should be added, if the value is not null.
     * @return true if the key-value pair was successfully added to the given {@link Map}; false otherwise.
     */
    @CanIgnoreReturnValue
    public static final <K, V> boolean putIfNotNull(K key, V value, Map<? super K,? super V> map) {
        if (value == null || map == null) {
            return false;
        }
        try {
            map.put(key, value);
            return true;
        }
        catch (NullPointerException e) {
            LOGGER.debug(
                "The Map {} ({}) may not support null keys.",
                StringUtil.truncatedToStringForLog(map),
                ObjectUtil.safeClassNameToString(map),
                e
            );
        }
        return false;
    }

    /**
     * Sorts the given {@link Collection} if it is a {@link List}, according to the natural ordering of the elements.
     *
     * <p>
     * The sort operation is performed via {@link Collections#sort(List)}.
     *
     * @param coll
     *     the {@link Collection} to be sorted if it is a {@link List}.
     * @return true if the {@link Collection} was a {@link List}, and was sorted.
     */
    public static final <E extends Comparable<E>> boolean sortIfList(Collection<E> coll) {
        if (coll instanceof List) {
            Collections.sort((List<E>) coll);
            return true;
        }
        return false;
    }

    /**
     * Sorts the given {@link Collection} if it is a {@link List} using the given {@link Comparator}.
     *
     * <p>
     * The sort operation is performed via {@link List#sort(Comparator)}.
     *
     * @param coll
     *     the {@link Collection} to be sorted if it is a {@link List}.
     * @param comparator
     *     the {@link Comparator} that provides the ordering for the sort operation.
     * @return true if the {@link Collection} was a {@link List}, and was sorted.
     */
    public static final <E> boolean sortIfList(Collection<? extends E> coll, Comparator<? super E> comparator) {
        if (coll instanceof List) {
            ((List<? extends E>) coll).sort(comparator);
            return true;
        }
        return false;
    }

    /**
     * Returns an {@link Iterable} over the elements in the given Iterable that match the given {@link Predicate}.
     *
     * <P>
     * This Iterables produced by this method defer to {@link #filteredIterator(Iterator, Predicate)} for their
     * {@link Iterable#iterator()} method.
     *
     * @param iterable
     *     the {@link Iterable} whose elements are to be filtered by the returned Iterable's {@link Iterator} .
     * @param matcher
     *     the {@link Predicate} that should be used to match elements from the given {@link Iterable}.
     * @return an empty {@link Iterable} if the given Iterable is null; the given Iterable if the given Predicate is
     *     null; otherwise, an {@link Iterable} over the elements in the given Iterable that match the given
     *     {@link Predicate}.
     */
    public static final <E> Iterable<E> filteringIterable(final Iterable<E> iterable, final Predicate<? super E> matcher) {
        if (iterable == null) {
            return Collections.emptyList();
        }
        if (matcher == null) {
            return iterable;
        }
        return () -> filteredIterator(iterable.iterator(), matcher);
    }

    /**
     * Returns an {@link Iterator} covering all the elements in the given source Iterator that match the given
     * {@link Predicate}.
     *
     * <p>
     * This method defers to {@link Iterators#filter(Iterator, com.google.common.base.Predicate)} . The only difference
     * is that this method gracefully handles a null Iterator or a null Predicate.
     *
     * @param iter
     *     the source {@link Iterator} whose elements should be filtered.
     * @param matcher
     *     the {@link Predicate} whose {@link Predicate#test(Object)} method will be used to determine which elements
     *     from the original {@link Iterator} should be included in the returned Iterator.
     * @return an empty {@link Iterator} if the given Iterator is null; the given Iterator if the given Predicate is
     *     null; otherwise, an {@link Iterator} over the elements in the given Iterable that match the given
     *     {@link Predicate}.
     */
    public static final <E> Iterator<E> filteredIterator(Iterator<E> iter, Predicate<? super E> matcher) {
        if (iter == null) {
            return Collections.emptyIterator();
        }
        if (matcher == null) {
            return iter;
        }
        return Iterators.filter(iter, matcher::test);
    }

    /**
     * Returns an {@link Iterator} covering all the elements in the given source Iterator that <em>do not</em> matched
     * by the given {@link Predicate}.
     *
     * <p>
     * That is, elements from the source Iterator will be omitted from the resulting iteration if they match the
     * Predicate (thus "inverse").
     *
     * @param iter
     *     the source {@link Iterator} whose elements should be filtered.
     * @param matcher
     *     the {@link Predicate} whose {@link Predicate#test(Object)} method will be used to determine which elements in
     *     the original iteration should be included.
     * @return an empty {@link Iterator} if the given Iterator is null; the given Iterator if the given Predicate is
     *     null; otherwise, an {@link Iterator} over the elements in the given Iterable that <em>do not</em> match the
     *     given {@link Predicate}.
     */
    public static final <E> Iterator<E> inverseFilteredIterator(Iterator<E> iter, Predicate<? super E> matcher) {
        if (iter == null) {
            return Collections.emptyIterator();
        }
        if (matcher == null) {
            return iter;
        }
        return filteredIterator(iter, matcher.negate());
    }

    /**
     * Returns an {@link Iterable} covering all the elements in the given source Iterable if they do not appear in the
     * given {@link Collection}.
     *
     * <p>
     * That is, elements from the source Iterable will be filtered out of an iteration if they appear in the given
     * Collection.
     *
     * @param iterable
     *     the source {@link Iterable} whose elements should be filtered.
     * @param exclude
     *     the {@link Collection} containing the elements which, if they occur in the original {@link Iterable}, should
     *     be skipped.
     * @return an {@link Iterable} covering all the elements in the given source Iterable which are not contained in the
     *     given {@link Collection}.
     */
    public static final <E> Iterable<E> excluding(final Iterable<E> iterable, final Collection<?> exclude) {
        if (iterable == null) {
            return Collections.emptyList();
        }
        if (exclude == null) {
            return iterable;
        }
        return () -> excluding(iterable.iterator(), exclude);
    }

    /**
     * Returns an {@link Iterable} covering all the elements in the given source Iterable which are contained in the
     * given {@link Collection}.
     *
     * <p>
     * It is NOT safe to pass null arguments to this method.
     *
     * @param iterable
     *     the source {@link Iterable} whose elements should be filtered.
     * @param include
     *     the {@link Collection} containing the elements which, if they occur in the original {@link Iterable}, should
     *     not be skipped.
     * @return an {@link Iterable} covering all the elements in the given source Iterable which are contained in the
     *     given {@link Collection}.
     */
    public static final <E> Iterable<E> includingOnly(final Iterable<E> iterable, final Collection<?> include) {
        return () -> includingOnly(iterable.iterator(), include);
    }

    /**
     * Returns an {@link Iterator} covering all the elements in the given source Iterator which are not contained in the
     * given {@link Collection}.
     *
     * <p>
     * It is NOT safe to pass null arguments to this method.
     *
     * @param iter
     *     the source {@link Iterator} whose elements should be filtered.
     * @param exclude
     *     the {@link Collection} containing the elements which, if they occur in the original iteration, should be
     *     skipped.
     * @return an {@link Iterator} covering all the elements in the given source Iterator which are not contained in the
     *     given {@link Collection}.
     */
    public static final <E> Iterator<E> excluding(Iterator<E> iter, Collection<?> exclude) {
        if (iter == null) {
            return Collections.emptyIterator();
        }
        if (exclude == null) {
            return iter;
        }
        return filteredIterator(iter, PredicateUtil.onlyOtherThanThese(exclude));
    }

    /**
     * Returns an {@link Iterator} covering all the elements in the given source Iterator which are contained in the
     * given {@link Collection}.
     *
     * <p>
     * It is NOT safe to pass null arguments to this method.
     *
     * @param iter
     *     the source {@link Iterator} whose elements should be filtered.
     * @param include
     *     the {@link Collection} containing the elements which, if they do not occur in the original iteration, should
     *     be skipped.
     * @return an {@link Iterator} covering all the elements in the given source Iterator which are not contained in the
     *     given {@link Collection}.
     */
    public static final <E> Iterator<E> includingOnly(Iterator<E> iter, Collection<?> include) {
        if (iter == null || isEmpty(include)) {
            return Collections.emptyIterator();
        }
        return filteredIterator(iter, PredicateUtil.onlyThese(include));
    }

    /**
     * Removes all elements matching the given {@link Predicate} from the given {@link Iterator}.
     *
     * <p>
     * This method defers to {@link Iterators#removeIf(Iterator, com.google.common.base.Predicate)} . The only
     * difference is that this method gracefully handles a null Iterator or a null Predicate.
     *
     * @param iter
     *     the source {@link Iterator}.
     * @param matcher
     *     the {@link Predicate} whose {@link Predicate#test(Object)} method will be used to determine which elements
     *     should be removed from the given {@link Iterator} (via {@link Iterator#remove()}).
     * @return true if any elements were removed from the given {@link Iterator}; false otherwise.
     */
    public static final <E> boolean removeIf(Iterator<? extends E> iter, Predicate<? super E> matcher) {
        if (iter == null || matcher == null) {
            return false;
        }
        return Iterators.removeIf(iter, matcher::test);
    }

    /**
     * Returns an {@link Iterator} over {@link Integer}s in the range covered by the two int values, inclusive.
     *
     * <p>
     * If both numbers are the same, the Iterator will cover that single value; if the first value is greater than the
     * last value, the Iterator will not cover any values.
     *
     * <p>
     * Clients should expect the {@link Iterator#remove()} method of the returned Iterator to throw an
     * {@link UnsupportedOperationException}.
     *
     * @param first
     *     the first value to be returned by the {@link Iterator}.
     * @param last
     *     the last value to be returned by the {@link Iterator}.
     * @return an {@link Iterator} over {@link Integer}s in the range covered by the two int values, inclusive.
     */
    public static final Iterator<Integer> intRangeIterator(int first, int last) {
        return new IntegerRangeIterator(first, last);
    }

    /**
     * Returns an {@link Iterator} over the values of the map underlying the given {@link MapIteratorAdapter},
     * corresponding to (and in the same order as) the keys covered by the given {@link Iterable}.
     *
     * <p>
     * Clients should expect the {@link Iterator#remove()} method of the returned Iterator to defer to the
     * {@link MapIteratorAdapter#remove(Object)} method.
     *
     * <p>
     * It is NOT safe to pass null arguments to this method.
     *
     * @param objects
     *     the {@link MapIteratorAdapter} providing read/remove access to a map's key-value pairs.
     * @param keys
     *     the keys, in the order in which the map's corresponding values should be presented by the {@link Iterator}.
     * @return an {@link Iterator} over the values from the map underlying the given {@link MapIteratorAdapter},
     *     corresponding to (and in the same order as) the keys covered by the given Iterable.
     */
    public static final <K, V> Iterator<V> mapValueIterator(MapIteratorAdapter<? super K,? extends V> objects, Iterable<? extends K> keys) {
        return mapValueIterator(objects, keys.iterator());
    }

    /**
     * Returns an {@link Iterator} over the values of the map underlying the given {@link MapIteratorAdapter},
     * corresponding to (and in the same order as) the keys covered by the given Iterator.
     *
     * <p>
     * Clients should expect the {@link Iterator#remove()} method of the returned Iterator to defer to the
     * {@link MapIteratorAdapter#remove(Object)} method.
     *
     * <p>
     * It is NOT safe to pass null arguments to this method.
     *
     * @param objects
     *     the {@link MapIteratorAdapter} providing read and delete access to a map's key-value pairs.
     * @param keys
     *     the keys, in the order in which the map's corresponding values should be presented by the {@link Iterator}.
     * @return an {@link Iterator} over the values from the map underlying the given {@link MapIteratorAdapter},
     *     corresponding to (and in the same order as) the keys covered by the given Iterator.
     */
    public static final <K, V> Iterator<V> mapValueIterator(MapIteratorAdapter<? super K,? extends V> objects, Iterator<? extends K> keys) {
        return new MapValueIterator<K,V>(objects, keys);
    }

    /**
     * Returns an {@link Iterator} over the values of the map underlying the given {@link MapIteratorAdapter},
     * corresponding to the keys represented by an Iterator of Integer values starting with the first given int value
     * and ending with the second given int value.
     *
     * <p>
     * Clients should expect the {@link Iterator#remove()} method of the returned Iterator to defer to the
     * {@link MapIteratorAdapter#remove(Object)} method.
     *
     * <p>
     * It is NOT safe to pass a null argument to this method for the objects parameter.
     *
     * @param objects
     *     the {@link MapIteratorAdapter} providing read and delete access to a map's key-value pairs.
     * @param firstKey
     *     the int value to use for an {@link Integer} object to be the first key whose value will be returned by the
     *     returned {@link Iterator}.
     * @param lastKey
     *     the int value to use for an {@link Integer} object to be the last key whose value will be returned by the
     *     returned {@link Iterator}.
     * @return an Iterator over the values of the map underlying the given {@link MapIteratorAdapter}, corresponding to
     *     the keys represented by an {@link Iterator} of {@link Integer} values starting with the first given int value
     *     and ending with the second given int value.
     */
    public static final <V> Iterator<V> mapValueKeyRangeIterator(MapIteratorAdapter<Integer,? extends V> objects, int firstKey, int lastKey) {
        return mapValueIterator(objects, intRangeIterator(firstKey, lastKey));
    }

    /**
     * Returns an {@link Iterable} that can produce an {@link Iterator} that will return the given value an infinite
     * number of times.
     *
     * <p>
     * This may be useful in the context of the parallel iteration pattern when a constant value is required for one of
     * the iterations.
     *
     * <p>
     * Clients should expect the {@link Iterator#remove()} method of an Iterator produced by the returned Iterable to
     * throw an {@link UnsupportedOperationException}.
     *
     * @param value
     *     the value to be returned.
     * @return an {@link Iterable} that can produce an {@link Iterator} that will return the given value an infinite
     *     number of times.
     */
    public static final <E> Iterable<E> infiniteSingleValueIterable(final E value) {
        return () -> infiniteSingleValueIterator(value);
    }

    /**
     * Returns an {@link Iterator} that will return the given value an infinite number of times.
     *
     * <p>
     * This may be useful in the context of the parallel iteration pattern when a constant value is required for one of
     * the iterations.
     *
     * <p>
     * Clients should expect the {@link Iterator#remove()} method of the returned Iterator to throw an
     * {@link UnsupportedOperationException}.
     *
     * @param value
     *     the value to be returned.
     * @return an {@link Iterator} that will return the given value an infinite number of times.
     */
    public static final <E> Iterator<E> infiniteSingleValueIterator(final E value) {
        return Iterators.unmodifiableIterator(Iterators.cycle(value));
    }

    /**
     * Returns an {@link Iterator} that will represent an iteration over objects that are the result of applying the
     * given Function's {@link Function#apply(Object)} method to each element returned by the Iterator obtained by
     * calling {@link Iterable#iterator()} on the given source Iterable, automatically omitting null values produced by
     * the Function.
     *
     * <p>
     * NOTE: If the {@link Function} produces null for a given source object, the resulting Iterator will skip to the
     * next source element as many times as is necessary to produce a non-null result, or until no source elements
     * remain.
     *
     * <p>
     * It is NOT safe to pass null arguments to this method.
     *
     * @param source
     *     the Iterable that will provide an iteration over the source elements.
     * @param converter
     *     the Function that can convert elements of the source type into elements of the type to be returned by the
     *     Iterator.
     * @return the Iterator so produced.
     */
    public static final <D, E> Iterator<E> convertingIterator(final Iterable<? extends D> source, final Function<? super D,? extends E> converter) {
        return convertingIterator(source.iterator(), converter);
    }

    /**
     * Applies a transformation represented by the given {@link UnaryOperator} to each element in a given {@link List},
     * updating each element in the List with the new value.
     *
     * @param list
     *     the {@link List} of objects to transform.
     * @param transformer
     *     the {@link UnaryOperator} representing the transformation to be applied to each element of the given
     *     {@link List}.
     */
    public static final <E> void transform(List<E> list, UnaryOperator<E> transformer) {
        if (list == null || transformer == null) {
            return;
        }
        int sz = list.size();
        for (int i = 0; i < sz; i++) {
            list.set(i, transformer.apply(list.get(i)));
        }
    }

    /**
     * Returns an Iterator that will represent an iteration over objects that are the result of applying the given
     * Function's {@link Function#apply(Object)} method to each element returned by the given source Iterator. If the
     * Function produces null for a given source object, the resulting Iterator will skip to the next source element as
     * many times as is necessary to produce an non-null result, or until no source elements remain.
     *
     * <p>
     * It is NOT safe to pass null arguments to this method.
     *
     * @param source
     *     the Iterator that will provide the source elements.
     * @param converter
     *     the Function that can convert elements of the source type into elements of the type to be returned by the
     *     Iterator.
     * @return the Iterator so produced.
     */
    public static final <D, E> Iterator<E> convertingIterator(Iterator<D> source, Function<? super D,? extends E> converter) {
        return Iterators.transform(source, FunctionsUtil.asGuavaFunction(converter));
    }

    /**
     * Returns an Iterable whose {@link Iterable#iterator()} method defers to
     * {@link #convertingIterator(Iterable, Function)}. That is, it will produce an Iterable whose Iterator will
     * represent an iteration over objects that are the result of applying the given Function's
     * {@link Function#apply(Object)} method to each element returned by the given source Iterator. If the Function
     * produces null for a given source object, the resulting Iterator will skip to the next source element as many
     * times as is necessary to produce an non-null result, or until no source elements remain.
     *
     * <p>
     * It is NOT safe to pass null arguments to this method.
     *
     * @param source
     *     the Iterable that will provide an iteration over the source elements.
     * @param converter
     *     the Function that can convert elements of the source type into elements of the type to be returned by the
     *     Iterable's Iterator.
     * @return the Iterable so produced.
     */
    public static final <D, E> Iterable<E> convertingIterable(final Iterable<? extends D> source, final Function<? super D,? extends E> converter) {
        return () -> convertingIterator(source, converter);
    }

    /**
     * Uses {@link #convertingIterable(Iterable, Function)} to create an Iterable of a type compatible with the
     * destination collection, and then uses {@link #addFromIterable(Iterable, Collection)} to
     *
     * <p>
     * It is NOT safe to pass null arguments to this method.
     *
     * @param source
     *     the {@link Iterable} whose elements are to be converted and added to the destination {@link Collection}.
     * @param converter
     *     the {@link Function} that can convert elements of the source type into elements of a type that can be added
     *     to the destination {@link Collection}.
     * @param destination
     *     the {@link Collection} to which the elements will be added.
     */
    public static final <D, E> void addConvertedFromIterable(Iterable<? extends D> source, final Collection<? super E> destination, Function<? super D,? extends E> converter) {
        addFromIterable(convertingIterable(source, converter), destination);
    }

    /**
     * Uses {@link #convertingIterator(Iterator, Function)} to create an Iterator of a type compatible with the
     * destination collection, and then uses {@link #addFromIterator(Iterator, Collection)} to
     *
     * <p>
     * It is NOT safe to pass null arguments to this method.
     *
     * @param source
     *     the Iterator whose elements are to be converted and added to the destination Collection.
     * @param converter
     *     the Function that can convert elements of the source type into elements of a type that can be added to the
     *     destination Collection.
     * @param destination
     *     the Collection to which the elements will be added.
     */
    public static final <D, E> void addConvertedFromIterator(Iterator<? extends D> source, final Collection<? super E> destination, Function<? super D,? extends E> converter) {
        addFromIterator(convertingIterator(source, converter), destination);
    }

    /**
     * Iterates through the elements in the given Iterable, adding an entry to a new Map whose key is the String
     * representation of the index of the iteration and the value is the current value. The Map will have the same
     * iteration order as the original Iterator had.
     *
     * <p>
     * This is an easy way to create a map that is roughly equivalent to an array or list, indexed by String instead of
     * int.
     *
     * @param iter
     *     the Iterable whose iteration will be used
     * @param initialIndex
     *     the initial index to use for the first key. 0 will be the usual value, but another value may be used as
     *     necessary.
     * @return the indexed map so produced, or an empty map if the Iterable was null.
     */
    public static final <V> Map<String,V> indexedMap(Iterable<? extends V> iter, int initialIndex) {
        if (iter == null) {
            return new LinkedHashMap<>();
        }
        return indexedMap(iter.iterator(), initialIndex);
    }

    /**
     * Iterates through the elements in the given Iterator, adding an entry to a new Map whose key is the String
     * representation of the index of the iteration and the value is the current value. The Map will have the same
     * iteration order as the original Iterator had.
     *
     * <p>
     * This is an easy way to create a map that is roughly equivalent to an array or list, indexed by String instead of
     * int.
     *
     * @param iter
     *     the Iterator whose
     * @param initialIndex
     *     the initial index to use for the key.
     * @return the indexed map so produced, or an empty map if the Iterable was null.
     */
    public static final <V> Map<String,V> indexedMap(Iterator<? extends V> iter, int initialIndex) {
        Map<String,V> map = new LinkedHashMap<>();
        addIndexedEntriesToMap(iter, initialIndex, map);
        return map;
    }

    /**
     * Adds all the elements from the given Iterator to the given Map, using the String rendering of the numerical index
     * of each element as the key for each value from the Iterator.
     *
     * @param iter
     *     the Iterator whose elements will become the values of entries to be added to the map.
     * @param initialIndex
     *     the initial numeric index to use for the keys when adding to the map.
     * @param map
     *     the map to which the numerically indexed entries should be added.
     */
    public static final <V> void addIndexedEntriesToMap(Iterator<? extends V> iter, int initialIndex, Map<? super String,? super V> map) {
        if (iter == null) {
            return;
        }
        int index = initialIndex;
        while (iter.hasNext()) {
            map.put(String.valueOf(index), iter.next());
            index++;
        }
    }

    public static final <K, V> V getOrCreateAndPut(Map<? super K,V> map, K key, Function<? super K,? extends V> valueProvider) {
        V value = map.get(key);
        if (value != null) {
            return value;
        }
        value = valueProvider.apply(key);
        map.put(key, value);
        return value;
    }

    /**
     * Adds the elements that are unique to one of the first two {@link Collection}s to the third Collection. This is
     * also known as a disjunction or symmetric difference.
     *
     * @param a
     *     the first source {@link Collection}.
     * @param b
     *     the second source {@link Collection}.
     * @param result
     *     the {@link Collection} to which unique elements will be added.
     */
    public static final <E> void unique(Collection<? extends E> a, Collection<? extends E> b, Collection<? super E> result) {

        if (result == null) {
            return;
        }

        if (a == null) {
            if (b == null) {
                return;
            }
            result.addAll(b);
            return;
        }

        if (b == null) {
            result.addAll(a);
            return;
        }

        if (a instanceof Set && b instanceof Set) {
            result.addAll(Sets.symmetricDifference((Set<? extends E>) a, (Set<? extends E>) b));
        }
        else {
            Iterables.addAll(result, Iterables.filter(a, PredicateUtil.onlyOtherThanThese(b)::test));
            Iterables.addAll(result, Iterables.filter(b, PredicateUtil.onlyOtherThanThese(a)::test));
        }

    }

    /**
     * "Safely" produces a {@link String} representation of the given {@link Map}.
     *
     * <p>
     * This method is precisely identical to the implementation of {@link AbstractMap#toString()}, but uses
     * {@link ObjectUtil#safeToString(Object)} to produce String representations of the Map's keys and values.
     *
     * <p>
     * This may be useful, e.g., to produce diagnostic information related to some key-value pairs while ensuring that
     * any unexpected {@link RuntimeException}s produced by their {@link Object#toString()} methods do not cause any
     * problems.
     *
     * @param map
     *     the {@link Map} for which a {@link String} representation is to be produced.
     * @return a {@link String} representation of the given {@link Map}.
     */
    public static final <K, V> String safeToString(Map<K,V> map) {

        if (map == null || map.isEmpty()) {
            return "{}";
        }

        Iterator<Map.Entry<K,V>> i = map.entrySet().iterator();
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (; ; ) {
            Map.Entry<K,V> e = i.next();
            K key = e.getKey();
            V value = e.getValue();
            sb.append(safeToStringOrRecursionReplacement(key, map, "Map"));
            sb.append('=');
            sb.append(safeToStringOrRecursionReplacement(value, map, "Map"));
            if (!i.hasNext()) {
                return sb.append('}').toString();
            }
            sb.append(',').append(' ');
        }

    }

    /**
     * "Safely" produces a {@link String} representation of the given {@link Iterable}.
     *
     * <p>
     * This method is nearly precisely identical to the implementation of {@link AbstractCollection#toString()}, but
     * uses {@link ObjectUtil#safeToString(Object)} to produce String representations of the elements produced by an
     * {@link Iterator} from the given Iterable.
     *
     * <p>
     * This may be useful, e.g., to produce diagnostic information related to a series of objects while ensuring that
     * any unexpected {@link RuntimeException}s produced by their {@link Object#toString()} methods do not cause any
     * problems.
     *
     * @param iterable
     *     the {@link Iterable} for which a {@link String} representation is to be produced.
     * @return a {@link String} representation of the given {@link Iterable}.
     */
    public static final <E> String safeToString(Iterable<E> iterable) {
        return safeToStringImpl(iterable, "Iterable");
    }

    /**
     * An implementation of the code required for {@link #safeToString(Iterable)}, with a parameter indicating how to
     * handle a reference to the {@link Iterable} itself.
     *
     * @param iterable
     *     the {@link Iterable} for which a safe {@link String} is to be created.
     * @param selfReferenceSubstitution
     *     the {@link String} to substitute in the unlikely but not impossible case of a value produced at a particular
     *     iteration being a reference to the {@link Iterable} itself, to avoid infinite recursion.
     * @return a {@link String} representation of the given {@link Iterable}.
     */
    static final <E> String safeToStringImpl(Iterable<E> iterable, String selfReferenceSubstitution) {

        if (iterable == null) {
            return "[]";
        }

        Iterator<E> it = iterable.iterator();
        if (!it.hasNext()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (; ; ) {
            sb.append(safeToStringOrRecursionReplacement(it.next(), iterable, selfReferenceSubstitution));
            if (!it.hasNext()) {
                return sb.append(']').toString();
            }
            sb.append(',').append(' ');
        }

    }

    /**
     * Safely produces a {@link String} for the given Object, or a substitute of "this X" in order to avoid recursion
     * with respect to the given parent/container Object.
     *
     * <p>
     * Obviously, this only avoids recursion of one level.
     *
     * @param object
     *     the Object to render as a {@link String}.
     * @param parent
     *     the parent or container Object.
     * @param parentReference
     *     the value to return instead of attempting to invoke {@link Object#toString()} on the given Object if it is
     *     the same Object as the given parent Object.
     * @return a {@link String} for the given Object, or a substitute of "this X" in order to avoid recursion with
     *     respect to the given parent/container Object.
     */
    private static final String safeToStringOrRecursionReplacement(Object object, Object parent, String parentReference) {
        if (object == null) {
            return null;
        }
        if (object == parent) {
            return "this " + parentReference;
        }
        return ObjectUtil.safeToString(object);
    }

    public static final <K, V> MapIteratorAdapter<K,V> function2mapIteratorAdapter(final Function<K,V> provider) {

        Objects.requireNonNull(provider, "provider");

        return new MapIteratorAdapter<>() {

            @Override
            public final V get(K key) {
                return provider.apply(key);
            }

            @Override
            public final void remove(K key) {
                throw new UnsupportedOperationException();
            }

        };

    }

    public static final <T> Collection<T> unmodifiableCollection(@Nullable Collection<T> coll) {
        if (coll == null) {
            return Collections.emptyList();
        }
        if (coll instanceof ImmutableCollection<T> immutable) {
            return immutable;
        }
        return Collections.unmodifiableCollection(coll);
    }

    public static final <T> List<T> unmodifiableList(@Nullable List<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        if (list instanceof ImmutableList<T> immutable) {
            return immutable;
        }
        return Collections.unmodifiableList(list);
    }

    public static final <T> Set<T> unmodifiableSet(@Nullable Set<T> set) {
        if (set == null) {
            return Collections.emptySet();
        }
        if (set instanceof ImmutableSet<T> immutable) {
            return immutable;
        }
        return Collections.unmodifiableSet(set);
    }

    public static final <K, V> Map<K,V> unmodifiableMap(@Nullable Map<K,V> map) {
        if (map == null) {
            return Collections.emptyMap();
        }
        if (map instanceof ImmutableMap<K,V> immutable) {
            return immutable;
        }
        return Collections.unmodifiableMap(map);
    }

    @SuppressWarnings("unchecked")
    public static final <T> SequencedSet<T> emptySequencedSet() {
        return (SequencedSet<T>) EMPTY_SEQUENCED_SET;
    }

    public static final <T> SequencedSet<T> singletonOrEmptySequencedSet(@Nullable T o) {
        if (o == null) {
            return emptySequencedSet();
        }
        return singletonSequencedSet(o);
    }

    public static final <T> SequencedSet<T> singletonSequencedSet(@Nullable T o) {
        Objects.requireNonNull(o, "object");
        return new SingletonSequencedSet<>(o);
    }

    public static final <T> SequencedSet<T> firstNonNullElementSingletonOrEmptySequencedSet(@Nullable Collection<T> coll) {
        if (isEmpty(coll)) {
            return Collections.unmodifiableSequencedSet(Collections.emptySortedSet());
        }
        for (T o : coll) {
            if (o != null) {
                return new SingletonSequencedSet<>(o);
            }
        }
        return emptySequencedSet();
    }

    public static final <T> SequencedSet<T> unmodifiableSequencedSet(@Nullable SequencedSet<T> set) {
        if (set == null) {
            return emptySequencedSet();
        }
        if (set instanceof AbstractUnmodifiableSequencedSet<T> unmodifiable) {
            return unmodifiable;
        }
        return Collections.unmodifiableSequencedSet(set);
    }

    @SafeVarargs
    public static final <T> SequencedSet<T> unmodifiableSequencedSet(T... objects) {
        LinkedHashSet<T> set = new LinkedHashSet<>();
        Collections.addAll(set, objects);
        return Collections.unmodifiableSequencedSet(set);
    }

    public static final boolean putMapEntryStrings(Iterable<String> nvPairs, Map<? super String,? super String> map) {

        for (String nv : nvPairs) {

            if (StringUtils.isBlank(nv)) {
                continue;
            }

            int eq = nv.indexOf('=');
            if (eq != -1) {
                map.put(nv.substring(0, eq), nv.substring(eq + 1));
            }
            else {
                map.put(nv, "");
            }

        }

        return true;

    }

}

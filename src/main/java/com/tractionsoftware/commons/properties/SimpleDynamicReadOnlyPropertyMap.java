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

package com.tractionsoftware.commons.properties;

import com.google.common.collect.ImmutableMap;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A simple implementation of {@link ReadOnlyPropertyMap} which uses a {@link Map} from {@link String} to any type, plus
 * a {@link Function} capable of mapping the Map's values to String for the {@link #getValue(String)} method.
 *
 * <p>
 * The layer of indirection for property values is intended to support on-demand property evaluation of the sort needed
 * for runtime l10n, or other requirements for dynamically supplied named property values. To use a Map that contains
 * static {@link String} to String mappings, simply use {@link #ofSnapshottedStaticProperties(Map)} or
 * {@link #ofStaticProperties(Map)}.
 *
 * <p>
 * The factory constructors support either taking a snapshot a Map or copying the reference to the Map. Clients should
 * use the appropriate version depending upon applicable requirements.
 *
 * @param <V>
 *     the type of the values stored in the underlying {@link Map}.
 * @author Dave Shepperton
 */
public final class SimpleDynamicReadOnlyPropertyMap<V> implements ReadOnlyPropertyMap {

    /**
     * Creates a SimpleReadOnlyPropertyMap backed by a snapshot of the given {@link Map}'s name-value pairs.
     *
     * <p>
     * The values returned by the {@link #getValue(String)} method will be exactly the values that are in the given Map
     * at the time this method is invoked (thus the "snapshot" aspect).
     *
     * @param properties
     *     a {@link Map} representing the static name-value pairs. It will be copied to an {@link ImmutableMap}.
     * @return a SimpleReadOnlyPropertyMap backed by a snapshot of the given {@link Map}'s name-value pairs.
     */
    public static final SimpleDynamicReadOnlyPropertyMap<String> ofSnapshottedStaticProperties(Map<String,String> properties) {
        return createSnapshotInstance(properties, Function.identity());
    }

    /**
     * Creates a SimpleReadOnlyPropertyMap backed by the given {@link Map}'s name-value pairs.
     *
     * <p>
     * The values returned by the {@link #getValue(String)} method will be based upon the name-value pairs that are in
     * the given Map at the time the getValue method is invoked (i.e., not based upon a "snapshot" of the Map).
     *
     * @param properties
     *     a {@link Map} representing the static name-value pairs. It will be copied to an {@link ImmutableMap}.
     * @return a SimpleReadOnlyPropertyMap backed by the given {@link Map}'s name-value pairs.
     */
    public static final SimpleDynamicReadOnlyPropertyMap<String> ofStaticProperties(Map<String,String> properties) {
        return createInstance(properties, Function.identity());
    }

    /**
     * Creates a SimpleReadOnlyPropertyMap backed by a snapshot of the given {@link Map}'s name-value pairs.
     *
     * <p>
     * The values returned by the {@link #getValue(String)} method will be exactly the values that are in the given Map
     * at the time this method is invoked (thus the "snapshot" aspect).
     *
     * @param properties
     *     a {@link Map} representing the static name-value pairs. It will be copied to an {@link ImmutableMap}.
     * @return a SimpleReadOnlyPropertyMap backed by a snapshot of the given {@link Map}'s name-value pairs.
     */
    public static final SimpleDynamicReadOnlyPropertyMap<Supplier<String>> ofSnapshottedDynamicProperties(Map<String,? extends Supplier<String>> properties) {
        return createSnapshotInstance(properties, Supplier::get);
    }

    /**
     * Creates a SimpleReadOnlyPropertyMap backed by the given {@link Map}'s name-value pairs, using the
     * {@link Supplier} Map values to produce {@link String}s for the {@link #getValue(String)} method.
     *
     * <p>
     * The values returned by the {@link #getValue(String)} method will be based upon the name-value pairs that are in
     * the given Map at the time the getValue method is invoked (i.e., not based upon a "snapshot" of the Map).
     *
     * @param properties
     *     a {@link Map} representing the static name-value pairs. It will be copied to an {@link ImmutableMap}.
     * @return a SimpleReadOnlyPropertyMap backed by the given {@link Map}'s name-value pairs.
     */
    public static final SimpleDynamicReadOnlyPropertyMap<Supplier<String>> ofDynamicProperties(Map<String,? extends Supplier<String>> properties) {
        return createInstance(properties, Supplier::get);
    }

    /**
     * Creates a SimpleReadOnlyPropertyMap backed by a snapshot of the given {@link Map}'s name-value pairs and the
     * given value to {@link String} mapping {@link Function}.
     *
     * <p>
     * The values returned by the {@link #getValue(String)} method will be based upon exactly the values that are in the
     * given Map at the time this method is invoked (thus the "snapshot" aspect).
     *
     * @param properties
     *     a {@link Map} representing the static name-value pairs. It will be copied to an {@link ImmutableMap}.
     * @param evaluator
     *     the {@link Function} which will be used to map the given {@link Map}'s values to {@link String}s.
     * @return a SimpleReadOnlyPropertyMap backed by a snapshot of the given {@link Map}'s name-value pairs.
     */
    public static final <V> SimpleDynamicReadOnlyPropertyMap<V> createSnapshotInstance(Map<String,? extends V> properties, Function<? super V,String> evaluator) {
        Objects.requireNonNull(properties, "properties Map");
        return new SimpleDynamicReadOnlyPropertyMap<>(
            ImmutableMap.<String,V>builder().putAll(properties).build(),
            evaluator
        );
    }

    /**
     * Creates a SimpleReadOnlyPropertyMap backed by the given {@link Map}'s name-value pairs, using the given
     * {@link Function} to map the Map's values to {@link String}s for the {@link #getValue(String)} method.
     *
     * <p>
     * The values returned by the {@link #getValue(String)} method will be based upon the name-value pairs that are in
     * the given Map at the time the getValue method is invoked (i.e., not based upon a "snapshot" of the Map).
     *
     * @param properties
     *     a {@link Map} representing the static name-value pairs. It will be copied to an {@link ImmutableMap}.
     * @return a SimpleReadOnlyPropertyMap backed by the given {@link Map}'s name-value pairs.
     */
    public static final <V> SimpleDynamicReadOnlyPropertyMap<V> createInstance(Map<String,? extends V> properties, Function<? super V,String> evaluator) {
        Objects.requireNonNull(properties, "properties Map");
        return new SimpleDynamicReadOnlyPropertyMap<>(Collections.unmodifiableMap(properties), evaluator);
    }

    private final Map<String,? extends V> map;

    private final Function<? super V,String> evaluator;

    private SimpleDynamicReadOnlyPropertyMap(Map<String,? extends V> map, Function<? super V,String> evaluator) {
        this.map = map;
        Objects.requireNonNull(evaluator, "evaluator Function");
        this.evaluator = evaluator;
    }

    @Override
    public final String getValue(String name) {
        V value = map.get(name);
        if (value == null) {
            return null;
        }
        return evaluator.apply(value);
    }

    @Override
    public final Set<String> getNames() {
        return new LinkedHashSet<>(map.keySet());
    }

}

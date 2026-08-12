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

package com.tractionsoftware.commons.properties;

import com.tractionsoftware.commons.util.CollectionUtil;
import jakarta.annotation.Nonnull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A generic {@link PropStore} implementation, extending {@link AbstractMapGetProperty}, backed by any {@link Map} that
 * accepts {@link String}s as keys and values.
 *
 * @author Dave Shepperton
 */
public final class MapPropertyStore<R> extends AbstractMapGetProperty implements PropStore<R> {

    private final Map<? super String,? super String> map;

    private final String name;

    private final boolean removeOnNullValuePut;

    /**
     * Copies the key-value pairs from the given map into a new String-to-String {@link HashMap}, converting each key
     * and value to a String via its toString method, and returns a new MapPropertyStore backed by that new
     * {@link HashMap}.
     *
     * <p>
     * Instances created via this method will use the default behavior when the {@link #putProperty(String, String)}
     * method is invoked with a null value: the key-value pair is removed, if it exists, from the backing map.
     *
     * @param name
     *     the name to use for the MapPropertyStore.
     * @param map
     *     containing the values to use to initialize the MapPropertyStore.
     * @param <R>
     *     the type of record used to commit changes.
     * @return a new MapPropertyStore backed by a String-to-String {@link HashMap} that initially contains all the
     *     key-value pairs from the given Map with each key and value converted to a String via their toString methods.
     */
    public static final <R> MapPropertyStore<R> createInstanceFromUnknownMapCopy(String name, Map<?,?> map) {
        Map<String,String> useMap = new LinkedHashMap<>(map.size());
        for (Map.Entry<?,?> entry : map.entrySet()) {
            useMap.put(Objects.toString(entry.getKey(), null), Objects.toString(entry.getValue(), null));
        }
        return createNamedInstance(name, useMap);
    }

    /**
     * Returns a new MapPropertyStore backed by a String-to-String {@link LinkedHashMap}.
     *
     * <p>
     * Instances created via this factory constructor will use the default behavior when the
     * {@link #putProperty(String, String)} method is invoked with a null value: the key-value pair is removed, if it
     * exists, from the backing map.
     *
     * @param <R>
     *     the type of record used to commit changes.
     * @return a new MapPropertyStore backed by a String-to-String {@link LinkedHashMap}.
     */
    public static final <R> MapPropertyStore<R> createDefaultInstance() {
        return createInstance(new LinkedHashMap<>());
    }

    /**
     * Returns a new MapPropertyStore backed by the given Map.
     *
     * <p>
     * Instances created via this factory constructor will use the default behavior when the
     * {@link #putProperty(String, String)} method is invoked with a null value: the key-value pair is removed, if it
     * exists, from the backing map.
     *
     * @param map
     *     the Map that accepts String keys and values that will be used to back this MapPropertyStore.
     * @param <R>
     *     the type of record used to commit changes.
     * @return a new MapPropertyStore backed by the given Map.
     */
    public static final <R> MapPropertyStore<R> createInstance(Map<? super String,? super String> map) {
        return createInstance(map, true);
    }

    /**
     * Returns a new MapPropertyStore backed by the given Map.
     *
     * @param map
     *     the Map that accepts String keys and values that will be used to back this MapPropertyStore.
     * @param removeOnNullValuePut
     *     pass true to select the default behavior when the {@link #putProperty(String, String)} method is invoked with
     *     a null value: the key-value pair is removed, if it exists, from the backing map; pass false to cause the
     *     key-value pair to be written into the {@link Map}, with a null value.
     * @param <R>
     *     the type of record used to commit changes.
     * @return a new MapPropertyStore backed by the given Map.
     */
    public static final <R> MapPropertyStore<R> createInstance(Map<? super String,? super String> map, boolean removeOnNullValuePut) {
        return createNamedInstance(null, map, removeOnNullValuePut);
    }

    /**
     * Returns a new MapPropertyStore using the given name and backed by the given Map.
     *
     * <p>
     * Instances created via this factory constructor will use the default behavior when the
     * {@link #putProperty(String, String)} method is invoked with a null value: the key-value pair is removed, if it
     * exists, from the backing map.
     *
     * @param name
     *     an informative name which will be returned by {@link #getName()}.
     * @param map
     *     the Map that accepts String keys and values that will be used to back this MapPropertyStore.
     * @param <R>
     *     the type of record used to commit changes.
     * @return a new MapPropertyStore using the given name and backed by the given Map.
     */
    public static final <R> MapPropertyStore<R> createNamedInstance(String name, Map<? super String,? super String> map) {
        return createNamedInstance(name, map, true);
    }

    /**
     * Returns a new MapPropertyStore using the given name and backed by the given Map.
     *
     * @param name
     *     an informative name which will be returned by {@link #getName()}.
     * @param map
     *     the Map that accepts String keys and values that will be used to back this MapPropertyStore.
     * @param removeOnNullValuePut
     *     pass true to select the default behavior when the {@link #putProperty(String, String)} method is invoked with
     *     a null value: the key-value pair is removed, if it exists, from the backing map; pass false to cause the
     *     key-value pair to be written into the {@link Map}, with a null value.
     * @return a new MapPropertyStore using the given name and backed by the given Map.
     */
    public static final <R> MapPropertyStore<R> createNamedInstance(String name, Map<? super String,? super String> map, boolean removeOnNullValuePut) {
        Objects.requireNonNull(map, "MapPropertyStore: map cannot be null.");
        return new MapPropertyStore<>(Objects.toString(name, "MapPropertyStore"), map, removeOnNullValuePut);
    }

    /**
     * Constructs a new MapPropertyStore using the given name and backed by the given {@link Map}.
     *
     * @param name
     *     an informative name which will be returned by {@link #getName()}.
     * @param map
     *     the {@link Map} that accepts String keys and values that will be used to back this MapPropertyStore.
     * @param removeOnNullValuePut
     *     pass true to select the default behavior when the {@link #putProperty(String, String)} method is invoked with
     *     a null value: the key-value pair is removed, if it exists, from the backing map; pass false to cause the
     *     key-value pair to be written into the {@link Map}, with a null value.
     */
    private MapPropertyStore(String name, Map<? super String,? super String> map, boolean removeOnNullValuePut) {
        this.name = name;
        this.map = map;
        this.removeOnNullValuePut = removeOnNullValuePut;
    }

    @Nonnull
    @Override
    public final String toString() {
        return "PropStore: stat map '" + Objects.toString(name, "map") + "' {" + map.getClass().getName() + "}";
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final boolean hasProperty(String name) {
        if (name == null) {
            return false;
        }
        if (map.containsKey(name)) {
            return true;
        }
        return false;
    }

    @Override
    public final void putProperty(String name, String value) {
        if (removeOnNullValuePut) {
            CollectionUtil.putOrRemove(map, name, value);
        }
        else {
            map.put(name, value);
        }
    }

    /**
     * This implementation always removes the requested property, since {@link #putProperty(String, String)} will
     * sometimes store the property mapping (depending upon the value of {@link #removeOnNullValuePut}).
     */
    @Override
    public final void removeProperty(String name) {
        map.remove(name);
    }

    @Override
    public boolean clearLocalProperties() {
        map.clear();
        return true;
    }

    /**
     * This implementation always returns {@link CommitResult#wasSuccessful() a successful CommitResult}, because
     * MapPropertyStore does not have any backing store other than the {@link Map}, in which all changes made via
     * methods such as {@link #putProperty(String, String)} are immediately reflected.
     */
    @Override
    public final CommitResult commitChanges(R rec) {
        return CommitResults.RESULT_SUCCESSFUL;
    }

    /**
     * Clears all name-value pairs in the underlying {@link Map}, thus removing all properties.
     */
    public final void clearAll() {
        map.clear();
    }

    /**
     * This implementation includes all requested properties, even those will null values. This way, instances whose
     * {@link #removeOnNullValuePut} field is set to true will expose all the stored properties, which is presumably the
     * behavior that clients using such instances require.
     */
    @Override
    public final Map<String,String> getProperties(Iterable<String> names) {
        Map<String,String> ret = new LinkedHashMap<>();
        if (names != null) {
            for (String name : names) {
                ret.put(name, getProperty(name));
            }
        }
        return ret;
    }

    /**
     * Sets the given name-value pairs as the new current set, first clearing the existing name-value pairs. If this
     * MapPropertyStore removes null values on {@link #putProperty(String, String) put operations}, any name-value pairs
     * that have a null value will not be added.
     */
    public final void updateMap(Map<String,String> newMap) {
        clearAll();
        copyFrom(newMap);
    }

    @Override
    protected final Map<? super String,?> map() {
        return map;
    }

}

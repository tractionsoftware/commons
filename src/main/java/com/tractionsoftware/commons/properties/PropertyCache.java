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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Dave Shepperton
 */
public final class PropertyCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyCache.class);

    /**
     * A simple encapsulation of a cached object and the raw String from which it was loaded.
     *
     * @param <T>
     *     the type of object being cached.
     * @author Dave Shepperton
     */
    public static final class CacheEntry<T> {

        public static final CacheEntry<Object> NULL_INSTANCE = new CacheEntry<>(null, null);

        @SuppressWarnings("unchecked")
        public static final <T> CacheEntry<T> getInstance(T object, String rawValue) {
            if (object == null && rawValue == null) {
                return (CacheEntry<T>) NULL_INSTANCE;
            }
            return new CacheEntry<>(object, rawValue);
        }

        private final T object;

        private final String rawValue;

        private CacheEntry(T object, String rawValue) {
            this.object = object;
            this.rawValue = rawValue;
        }

        public final T get() {
            return object;
        }

        public final boolean isValid(String currentRawValue) {
            return Objects.equals(rawValue, currentRawValue);
        }

    }

    /**
     * The {@link Cache} interface's get method requires a {@link Callable} to provide the Object if it isn't already
     * cached.
     *
     * @param <T>
     *     the type of object being loaded.
     */
    private static final class ValueLoader<T> implements Callable<CacheEntry<T>> {

        /**
         * @param loader
         *     the Loader whose load method should be invoked to load a new object.
         * @param rawValue
         *     the raw String value to be passed to the Loader's load method.
         * @param <T>
         *     the type of object
         * @return a CacheEntry encapsulation the newly loaded object and the raw String value from which it was loaded.
         */
        static final <T> ValueLoader<T> createInstance(BiFunction<? super String,? super String,? extends T> loader, String name, String rawValue) {
            return new ValueLoader<>(loader, name, rawValue);
        }

        private final BiFunction<? super String,? super String,? extends T> loader;

        private final String name;

        private final String rawValue;

        private ValueLoader(BiFunction<? super String,? super String,? extends T> loader, String name, String rawValue) {
            this.loader = loader;
            this.name = name;
            this.rawValue = rawValue;
        }

        @Override
        public final CacheEntry<T> call() {
            return new CacheEntry<>(loader.apply(name, rawValue), rawValue);
        }

    }

    public static final PropertyCache createInstance() {
        return createInstance(false, false);
    }

    public static final PropertyCache createInstanceWithStatsCollection() {
        return createInstance(false, true);
    }

    public static final PropertyCache createInstanceValidatingOnRead() {
        return createInstance(true, false);
    }

    public static final PropertyCache createInstance(boolean validateOnRead, boolean recordStats) {
        return new PropertyCache(getCache(recordStats), validateOnRead);
    }

    private static final Cache<String,CacheEntry<Object>> getCache(boolean recordStats) {
        if (recordStats) {
            return CacheBuilder.newBuilder().recordStats().build();
        }
        return CacheBuilder.newBuilder().build();
    }

    /**
     * The cache for this store.
     */
    private final Cache<String,CacheEntry<Object>> cache;

    /**
     * This field represents whether cached objects are validated on read.
     */
    private final boolean validateOnRead;

    private PropertyCache(Cache<String,CacheEntry<Object>> cache, boolean validateOnRead) {
        this.cache = cache;
        this.validateOnRead = validateOnRead;
    }

    @Override
    public final String toString() {
        return "PropertyCache (" + cache + " [validate-on-read = " + validateOnRead + "])";
    }

    /**
     * Retrieves or freshly loads an Object corresponding to the raw String value of the property with the given name,
     * if such a property is defined, using the given Loader.
     *
     * <p>
     * If the given Loader allows the use of a cache, and the given named property is defined, the cached instance will
     * be used if it is present, and if a new instance must be loaded, that will be cached.
     *
     * <p>
     * If this instance is not configured to perform validation-on-read for cached Objects, it is the responsibility of
     * the clients of this class -- that is, where instances of this class are actually maintained -- to manually
     * invalidate cached Objects corresponding to named property values when those property values change, via the
     * appropriate invocations of one of the invalidate methods of the invalidateAll method. Therefore, if the given
     * named property is not defined at all, clients of the getProperty method SHOULD reasonably be able to expect that
     * cached Objects will only ever correspond to the very latest raw String value of the named property.
     *
     * @return if the given named property is defined, a new or cached Object of type T (as may be allowed by the
     *     Loader's cache usage policy, and as may be available in the cache).
     */
    public final <T> T getOrLoadProperty(Function<? super String,String> name2rawValue, String name, PropertyLoader<T> loader) {
        CacheEntry<?> alreadyCached = cache.getIfPresent(name);
        if (alreadyCached == null) {
            LOGGER.debug("Cache miss for property name {}", name);
            return load(name2rawValue, loader, name);
        }
        if (!validateOnRead || alreadyCached.isValid(name2rawValue.apply(name))) {
            LOGGER.debug("Cache hit for property name {}", name);
            return loader.cast(alreadyCached.get());
        }
        LOGGER.debug("Cache hit failed validation on read for property name {}", name);
        cache.invalidate(name);
        return load(name2rawValue, loader, name);

    }

    public final Supplier<CacheStats> getCacheStatsProvider() {
        return cache::stats;
    }

    /**
     * Invalidates all entries in the cache.
     */
    public final void invalidateAll() {
        cache.invalidateAll();
    }

    /**
     * Invalidates the cache entry for the given name, if one exists.
     *
     * @param name
     *     the name of the cached property object to be invalidated.
     */
    public final void invalidate(String name) {
        cache.invalidate(name);
    }

    /**
     * Invalidates the cache entries for the given names, if such entries exist.
     *
     * @param keys
     *     the {@link Set} containing the names of any cached property objects to be invalidated.
     */
    public final void invalidate(Iterable<String> keys) {
        cache.invalidateAll(keys);
    }

    /**
     * Attempts to retrieve and/or load an object for the given name using the given Loader. If there is no raw String
     * value in the store for the given name, no object will be retrieved or loaded.
     *
     * @param name2rawValue
     *     a {@link Function} that produces the value from a given name.
     * @param loader
     *     the Loader whose load method should be invoked to load a new object if one is required.
     * @param name
     *     the name of the property for which an object is to be retrieved or loaded.
     * @return the object already cached by another thread, or loaded using the given {@link PropertyLoader}, using the
     *     raw String value of the given named property, if one exists; null otherwise.
     * @throws RuntimeException
     *     wrapping an ExecutionException, if one is raised while attempting to load an object.
     */
    private final <T> T load(Function<? super String,String> name2rawValue, PropertyLoader<T> loader, String name) {
        Object result;
        try {
            result = cache.get(name, ValueLoader.createInstance(loader, name, name2rawValue.apply(name))).get();
        }
        catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        if (result == null) {
            return null;
        }
        return loader.cast(result);
    }

}

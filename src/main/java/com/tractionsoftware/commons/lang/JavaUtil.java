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

import com.google.common.annotations.Beta;
import com.google.common.base.Suppliers;
import com.tractionsoftware.commons.io.IOUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;

import java.lang.ref.Cleaner;
import java.util.*;
import java.util.function.Supplier;

/**
 * Helper methods pertaining to basic Java capabilities.
 *
 * @author Dave Shepperton
 */
@Beta
public final class JavaUtil {

    private JavaUtil() {
    }

    /**
     * A {@link Cleaner} that can be used application-wide. This hard reference ensures it will never become only
     * phantom-reachable, and will therefore never terminate.
     */
    public static final Cleaner RESOURCE_CLEANER = Cleaner.create();

    public static final char QUALIFIER_CHAR = '.';

    /**
     * A {@link Supplier} that implements {@link AutoCloseable}, which is
     * {@link JavaUtil#registerCloseCleanerAction(Object, AutoCloseable) registered to be closed as a cleanup action}
     * for a given reference object.
     *
     * @param <T>
     *     the type of object that is being supplied.
     */
    public static final class CleanupTargetWrapper<T extends AutoCloseable> implements Supplier<T>, AutoCloseable {

        /**
         * Returns a new CleanupTargetWrapper that provides access to the given instance, and which is
         * {@link JavaUtil#registerCloseCleanerAction(Object, AutoCloseable) registered to be closed as a cleanup
         * action} for the given reference object. Specifically, the returned instance's {@link #close()} method will be
         * invoked when the given reference object becomes "phantom reachable." The client code may also invoke the
         * close method, which will prevent the cleanup action from having to do so.
         *
         * @param object
         *     the reference object, to be monitored.
         * @param instance
         *     the object to be supplied.
         * @param <T>
         *     the type of object that is being supplied.
         * @return a new CleanupTargetWrapper that provides access to the given instance, and which is
         *     {@link JavaUtil#registerCloseCleanerAction(Object, AutoCloseable) registered to be closed as a cleanup
         *     action} for the given reference object.
         */
        @Nonnull
        public static final <T extends AutoCloseable> CleanupTargetWrapper<T> create(@Nonnull Object object, @Nonnull T instance) {
            Objects.requireNonNull(object, "object");
            Objects.requireNonNull(instance, "instance");
            return new CleanupTargetWrapper<>(instance, registerCloseCleanerAction(object, instance));
        }

        private T instance;

        private final Cleaner.Cleanable closer;

        private CleanupTargetWrapper(T instance, Cleaner.Cleanable closer) {
            this.instance = instance;
            this.closer = closer;
        }

        @Nonnull
        @Override
        public final T get() {
            return instance;
        }

        /**
         * Runs the cleanup closer action.
         */
        @Override
        public final void close() {
            try {
                closer.clean();
            }
            finally {
                instance = null;
            }
        }

    }

    /**
     * Returns a {@link Cleaner.Cleanable} that will {@link AutoCloseable#close() close the given resource} when the
     * given object becomes "phantom reachable."
     *
     * @param object
     *     the object to monitor.
     * @param resource
     *     the resource to be {@link AutoCloseable#close() closed} as the cleanup action for the given object.
     * @return a {@link Cleaner.Cleanable} that will {@link AutoCloseable#close() close the given resource} when the
     *     given object becomes "phantom reachable."
     */
    public static final Cleaner.Cleanable registerCloseCleanerAction(Object object, AutoCloseable resource) {
        return JavaUtil.RESOURCE_CLEANER.register(object, () -> IOUtil.close(resource));
    }

    /**
     * Returns the approximate internal byte size of the given {@link String}.
     *
     * @param s
     *     the {@link String} to examine.
     * @return the approximate internal byte size of the given {@link String}; or 0 if the given String is null.
     */
    public static final int getApproximateInternalByteSize(@Nullable String s) {
        if (s == null) {
            return 0;
        }
        // We estimate 8B of "static" overhead for the String (4B for
        // the char[], 4B the cached hash int); and n chars * 2B each.
        return 8 + (s.length() * 2);
    }

    /**
     * Returns the approximate internal byte size of the given {@link Collection}.
     *
     * @param list
     *     the {@link Collection} to examine.
     * @return the approximate internal byte size of the given {@link Collection}; or 0 if the given Collection is null.
     */
    public static final int getApproximateInternalByteSize(@Nullable Collection<String> list) {
        if (list == null) {
            return 0;
        }
        // We estimate 16B of "static" overhead of the collection's fields.
        // This can vary widely by implementation, so it is a rough
        // estimate at best.
        int ret = 16;
        for (String s : list) {
            ret += getApproximateInternalByteSize(s);
        }
        return ret;
    }

    /**
     * Returns the approximate internal byte size of the given {@link Map}.
     *
     * @param map
     *     the {@link Map} to examine.
     * @return the approximate internal byte size of the given {@link Map}; or 0 if the given Map is null.
     */
    public static final int getApproximateInternalByteSize(@Nullable Map<String,String> map) {
        if (map == null) {
            return 0;
        }
        // We estimate 16B of "static" overhead of the map's fields.
        // This can vary widely by implementation, so it is a rough
        // estimate at best.
        int ret = 16;
        for (Map.Entry<String,String> e : map.entrySet()) {
            // We estimate 16B of "static" overhead for each
            // Map.Entry. This can also vary widely with
            // implementation, so is also a rough estimate at best.
            ret += 16;
            ret += getApproximateInternalByteSize(e.getKey());
            ret += getApproximateInternalByteSize(e.getValue());
        }
        return ret;
    }

    /**
     * Returns a "lazy service loader," which will simply be a {@link Supplier} of the requested type of object loaded
     * on demand via {@code ServiceLoader.load(type).findFirst()}.
     *
     * @param type
     *     the requested type of service object to load.
     * @param defaultService
     *     an optional default service to be used if no service can be found, or if there is a problem encountered while
     *     loading it.
     * @param logger
     *     an optional {@link Logger} for logging errors or other diagnostics.
     * @param <T>
     *     the type of service object to be supplied.
     * @return a "lazy service loader," which will simply be a {@link Supplier} of the requested type of object loaded
     *     on demand.
     * @throws NullPointerException
     *     if the given service type is null
     */
    public static final <T> Supplier<? extends T> lazyServiceLoader(@Nonnull Class<T> type, @Nullable T defaultService, @Nullable Logger logger) {
        return lazyServiceLoader(type, Suppliers.ofInstance(defaultService), logger);
    }

    /**
     * Returns a "lazy service loader," which will simply be a {@link Supplier} of the requested type of object loaded
     * on demand via {@code ServiceLoader.load(type).findFirst()}.
     *
     * @param type
     *     the requested type of service object to load.
     * @param defaultService
     *     an optional supplier for a default service to be used if no service can be found, or if there is a problem
     *     encountered while loading it.
     * @param logger
     *     an optional {@link Logger} for logging errors or other diagnostics.
     * @param <T>
     *     the type of service object to be supplied.
     * @return a "lazy service loader," which will simply be a {@link Supplier} of the requested type of object loaded
     *     on demand.
     * @throws NullPointerException
     *     if the given service type is null
     */
    public static final <T> Supplier<? extends T> lazyServiceLoader(@Nonnull Class<T> type, @Nullable Supplier<? extends T> defaultService, @Nullable Logger logger) {
        Objects.requireNonNull(type, "service type");
        return Suppliers.memoize(() -> loadService(type, defaultService, logger));
    }

    /**
     * Loads a service of the given type without allowing any exceptions to propagate.
     *
     * @param type
     *     the requested type of service object to load.
     * @param defaultService
     *     an optional default service to be used if no service can be found, or if there is a problem encountered while
     *     loading it.
     * @param logger
     *     an optional {@link Logger} for logging errors or other diagnostics.
     * @param <T>
     *     the type of service object to be supplied.
     * @return a service of the given type if one can be loaded via {@code ServiceLoader.load(type).findFirst()}; else
     *     the given default.
     * @throws NullPointerException
     *     if the given service type is null
     */
    public static final <T> T loadService(@Nonnull Class<T> type, @Nullable T defaultService, @Nullable Logger logger) {
        return loadService(type, Suppliers.ofInstance(defaultService), logger);
    }

    public static final <T> T loadService(@Nonnull Class<T> type, @Nullable Supplier<? extends T> getDefault, @Nullable Logger logger) {
        Objects.requireNonNull(type, "type");
        Optional<T> loaded;
        try {
            loaded = ServiceLoader.load(type).findFirst();
        }
        catch (Exception e) {
            if (logger != null) {
                logger.error("Failed to load type", e);
            }
            loaded = Optional.empty();
        }
        if (getDefault == null) {
            return loaded.orElse(null);
        }
        return loaded.orElseGet(getDefault);
    }

}

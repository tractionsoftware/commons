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

import com.google.common.collect.ImmutableSet;
import jakarta.annotation.Nonnull;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A simple skeleton implementation of {@link GetProperty} based upon an underlying {@link Map}.
 *
 * <p>
 * This is similar to the decorator pattern, but the delegate in this case is the Map rather than another GetProperty
 * instance.
 *
 * @author Dave Shepperton
 */
public abstract class AbstractMapGetProperty implements GetProperty {

    public static final Set<String> keysToStrings(Set<?> keyObjects) {
        if (keyObjects.isEmpty()) {
            return ImmutableSet.of();
        }
        return keyObjects.stream()
            .map(keyObj -> Objects.toString(keyObj, null))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * This implementation compares all the underlying values from the backing map plus the name.
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof MapPropertyStore otherStore)) {
            return false;
        }
        if (Objects.equals(map(), otherStore.map()) &&
            Objects.equals(getName(), otherStore.getName())) {
            return true;
        }
        return false;
    }

    /**
     * This implementation takes the backing map and name into account.
     */
    @Override
    public int hashCode() {
        return Objects.hash(map(), getName());
    }

    @Nonnull
    @Override
    public String toString() {
        return "GetProperty: dyn map '" + Objects.toString(getName(), "map") + "' {" + map().getClass().getName() + "}";
    }

    @Override
    public final String getProperty(String name) {
        return Objects.toString(getPropertyRaw(name), null);
    }

    @Override
    public final Set<String> getPropertyNames() {
        return keysToStrings(map().keySet());
    }

    /**
     * This method defers to {@link Map#isEmpty()} for {@link #map() the underlying map}.
     */
    @Override
    public final boolean isEmpty() {
        return map().isEmpty();
    }

    /**
     * Returns the backing {@link Map} for all {@link GetProperty} operations. This method must never return null.
     *
     * @return the backing {@link Map} for all {@link GetProperty} operations.
     */
    protected abstract Map<? super String,?> map();

    private final Object getPropertyRaw(String name) {
        if (name == null) {
            try {
                return map().get(null);
            }
            catch (NullPointerException e) {
                // This can happen if the underlying Map does not support null keys.
            }
            return null;
        }
        return map().get(name);
    }

}

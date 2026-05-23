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

import jakarta.annotation.Nonnull;

import java.util.Objects;
import java.util.Set;

/**
 * @author Dave Shepperton
 */
public abstract class AbstractCachingGetProperty<G extends GetProperty> extends StaticForwardingPropertyCollection<G> implements GetProperty {

    private final PropertyCache cache;

    public AbstractCachingGetProperty(G props, PropertyCache cache) {
        super(props);
        Objects.requireNonNull(cache, "PropertyCache");
        this.cache = cache;
    }

    @Nonnull
    @Override
    public String toString() {
        return "cache {" + props + "}";
    }

    @Override
    public final String getProperty(String name) {
        return props.getProperty(name);
    }

    @Override
    public final <T> T getProperty(String name, PropertyLoader<? extends T> loader, boolean mayUseCache) {
        if (mayUseCache) {
            return cache.getOrLoadProperty(props.asFunction(), name, loader);
        }
        return props.getProperty(name, loader, false);
    }

    @Override
    public final Set<String> getPropertyNames() {
        return props.getPropertyNames();
    }

    @Override
    public final String getLocalProperty(String name) {
        return props.getLocalProperty(name);
    }

    @Override
    public final GetProperty getDefaults() {
        return props.getDefaults();
    }

    @Override
    public final GetProperty getLocals() {
        return props.getLocals();
    }

}

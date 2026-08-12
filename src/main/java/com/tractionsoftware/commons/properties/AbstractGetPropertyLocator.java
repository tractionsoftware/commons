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

import com.google.common.collect.Sets;
import jakarta.annotation.Nonnull;

import java.util.Objects;
import java.util.Set;

/**
 * @author Dave Shepperton
 */
public abstract class AbstractGetPropertyLocator<G extends GetProperty> implements GetProperty {

    public static final String getProperty(GetProperty locals, GetProperty defaults, String name) {
        String fromLocals = locals.getProperty(name);
        if (fromLocals != null) {
            return fromLocals;
        }
        if (defaults == null) {
            return null;
        }
        return defaults.getProperty(name);
    }

    public static final <T> T getProperty(GetProperty locals, GetProperty defaults, String name, PropertyLoader<? extends T> loader, boolean mayUseCache) {
        T fromLocals = locals.getProperty(name, loader, mayUseCache);
        if (fromLocals != null) {
            return fromLocals;
        }
        if (defaults == null) {
            return null;
        }
        return defaults.getProperty(name, loader, mayUseCache);
    }

    public static final Set<String> getPropertyNames(GetProperty locals, GetProperty defaults) {

        Set<String> localNames = locals.getPropertyNames();
        if (defaults == null) {
            return localNames;
        }

        Set<String> defaultNames = defaults.getPropertyNames();
        if (localNames.isEmpty()) {
            return defaultNames;
        }
        if (defaultNames.isEmpty()) {
            return localNames;
        }
        return Sets.union(localNames, defaultNames);

    }

    protected final G locals;

    protected final GetProperty defaults;

    public AbstractGetPropertyLocator(G locals, GetProperty defaults) {
        Objects.requireNonNull(locals, "Main");
        Objects.requireNonNull(defaults, "Defaults");
        this.locals = locals;
        this.defaults = defaults;
    }

    @Nonnull
    @Override
    public String toString() {
        return "locator {locals: " + locals + "; defaults: " + defaults + "}";
    }

    @Override
    public String getName() {
        return locals.getName();
    }

    @Override
    public final String getProperty(String name) {
        return getProperty(locals, defaults, name);
    }

    @Override
    public final <T> T getProperty(String propName, PropertyLoader<? extends T> loader, boolean mayUseCache) {
        return getProperty(locals, defaults, propName, loader, mayUseCache);
    }

    @Override
    public final Set<String> getPropertyNames() {
        return getPropertyNames(locals, defaults);
    }

    @Override
    public final boolean isEmpty() {
        if (locals.isEmpty() && defaults.isEmpty()) {
            return true;
        }
        return false;
    }

    @Override
    public final String getLocalProperty(String name) {
        return locals.getLocalProperty(name);
    }

    @Override
    public final GetProperty getDefaults() {
        return GetPropertyLocator.getSingleGetPropertyOrLocator(locals.getDefaults(), defaults);
    }

    @Override
    public final GetProperty getLocals() {
        return locals.getLocals();
    }

    @Override
    public final String fullyQualify(String name) {
        return locals.fullyQualify(name);
    }

}

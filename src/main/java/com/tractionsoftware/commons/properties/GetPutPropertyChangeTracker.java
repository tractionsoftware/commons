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

import java.util.*;
import java.util.function.BiPredicate;

/**
 * Tracks changes made to an underlying property store.
 *
 * @author Dave Shepperton
 */
public final class GetPutPropertyChangeTracker implements GetPutProperty {

    private static final GetPutProperty newChanges() {
        return MapPropertyStore.createNamedInstance("changes", new HashMap<>(), false).toReadWrite();
    }

    /**
     * The {@link GetPutProperty} for tracking changes. This will include mappings for null values. This is not final
     * because it is re-initialized if the
     */
    private GetPutProperty changes;

    /**
     * The underlying {@link GetProperty} that represents the existing properties for which changes are being tracked.
     */
    private final GetProperty currentProps;

    /**
     * Constructs a new PropStoreChanges using the given {@link GetProperty} as the existing data to be changed.
     *
     * @param currentProps
     *     the {@link GetProperty} representing the existing data to be changed.
     */
    public GetPutPropertyChangeTracker(GetProperty currentProps) {
        this.currentProps = currentProps;
        this.changes = newChanges();
    }

    @Nonnull
    @Override
    public final String toString() {
        return "GetPutProperty: change tracker {current: " + currentProps + "; changes: " + changes + "}";
    }

    @Override
    public final String getProperty(String name) {

        if (changes.hasProperty(name)) {
            String changed = changes.getProperty(name);
            if (changed == null) {
                // If the changed value is null -- i.e., that the value will be removed from the local store, then the
                // effective value should come from the defaults.
                GetProperty defaults = currentProps.getDefaults();
                if (defaults != null) {
                    return defaults.getProperty(name);
                }
            }
            return changed;
        }

        return currentProps.getProperty(name);

    }

    /**
     * Returns the names of all the properties from the underlying data source, with any properties that were added via
     * putProperty either subtracted or added (subtracted if the new value is null, added if the new value is not
     * null).
     */
    @Override
    public final Set<String> getPropertyNames() {

        Set<String> names = new HashSet<>();

        GetProperty defaults = currentProps.getDefaults();
        if (defaults != null) {
            names.addAll(defaults.getPropertyNames());
        }

        Set<String> localNames = new HashSet<>(currentProps.getLocals().getAllProperties().keySet());

        for (Map.Entry<String,String> entry : changes.getAllProperties().entrySet()) {
            if (entry.getValue() == null) {
                localNames.remove(entry.getKey());
            }
            else {
                names.add(entry.getKey());
            }
        }

        names.addAll(localNames);

        return names;

    }

    @Override
    public final <T> T getProperty(String name, PropertyLoader<? extends T> loader, boolean mayUseCache) {

        if (changes.hasProperty(name)) {
            T changed = changes.getProperty(name, loader, mayUseCache);
            if (changed == null) {
                GetProperty defaults = currentProps.getDefaults();
                if (defaults != null) {
                    return defaults.getProperty(name, loader, mayUseCache);
                }
            }
            return changed;
        }

        return currentProps.getProperty(name, loader, mayUseCache);
    }

    @Override
    public final boolean hasProperty(String name) {
        if (changes.hasProperty(name)) {
            return true;
        }
        return false;
    }

    @Override
    public final void putProperty(String name, String value) {
        changes.putProperty(name, value);
    }

    @Override
    public final GetPutProperty getLocals() {
        return changes;
    }

    @Override
    public final GetProperty getDefaults() {
        return currentProps.toReadOnly();
    }

    /**
     * Applies the correct change, if any, to set the value of the property with the given name.
     *
     * @param name
     *     the name of the property.
     * @param newValue
     *     the new value to be applied.
     * @param changeTest
     *     a {@link BiPredicate} representing a test for whether the supplied new value represents a change from the
     *     currently stored property value (not including any property value that may have been applied as change
     *     already).
     */
    public final void applyPropertyChange(String name, String newValue, BiPredicate<String,String> changeTest) {
        if (changed(name, newValue, changeTest)) {
            changes.putProperty(name, newValue);
        }
    }

    /**
     * Returns true if there are any changes that have been made.
     *
     * @return true if there are any changes that have been made; false otherwise.
     */
    public final boolean hasChanges() {
        if (changes.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Clears all changes.
     */
    public final void clear() {
        this.changes = newChanges();
    }

    private final boolean changed(String name, String newValue, BiPredicate<String,String> changeTest) {
        if (changes.hasProperty(name)) {
            if (Objects.equals(changes.getProperty(name), newValue)) {
                return false;
            }
            return true;
        }
        return changeTest.test(currentProps.getLocalProperty(name), newValue);
    }

}

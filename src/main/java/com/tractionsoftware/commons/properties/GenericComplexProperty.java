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

import java.util.HashMap;
import java.util.Set;

/**
 * A very simple object that defers to a {@link MapPropertyStore} for {@link ComplexProperty}, {@link GetProperty} and
 * {@link PutProperty} implementations. {@link #GENERIC_LOADER} and {@link #saveInstance(GetPutProperty)}
 * implementations simply provide an adapter for the MapPropertyStore's name-value pairs.
 *
 * <p>
 * This class is suitable to be used directly for a {@link ComplexProperty} that is "generic" -- i.e., doesn't have to
 * be any special type of object -- or to be extended to make a subclass which has some special behaviors but whose
 * state is entirely encapsulated in the underlying property map.
 *
 * <p>
 * While this class wraps a MapPropertyStore, it is still considered "local", and therefore does not override
 * {@link #getLocalProperty(String)} or {@link #getLocals()}.
 */
public class GenericComplexProperty implements GetPutProperty, ComplexProperty {

    public static final ComplexProperty.Loader<GenericComplexProperty> GENERIC_LOADER = new ComplexProperty.Loader<>() {

        /**
         * Attempts to load a new {@link GenericComplexProperty} containing all the local properties from the
         * given namespace, if the namespace has any local properties.
         *
         * @return a new {@link GenericComplexProperty} containing all the local properties from the
         * given namespace, if the namespace has any local properties; null otherwise.
         */
        @Override
        public final GenericComplexProperty loadInstance(GetProperty namespace) {
            GetProperty locals = namespace.getLocals();
            if (locals.isEmpty()) {
                return null;
            }
            GenericComplexProperty generic = new GenericComplexProperty();
            generic.putAllProperties(locals);
            return generic;
        }

    };

    private final MapPropertyStore<Void> store;

    /**
     * Indicates whether the {@link #saveInstance(GetPutProperty)} method should write null values in the given
     * {@link GetPutProperty}. This field would be final but this object may have to be cloned.
     */
    private final boolean writeNull;

    /**
     * Initializes the key/value map for this object.
     */
    public GenericComplexProperty() {
        this(true);
    }

    public GenericComplexProperty(boolean writeNull) {
        this.writeNull = writeNull;
        this.store = new MapPropertyStore<Void>("generic complex property", new HashMap<>(), !writeNull);
    }

    @Override
    public final Set<String> getPropertyNames() {
        return store.getPropertyNames();
    }

    @Override
    public final String getProperty(String name) {
        return store.getProperty(name);
    }

    @Override
    public final void putProperty(String name, String value) {
        store.putProperty(name, value);
    }

    /**
     * This implementation copies all key/value pairs from the underlying map into the given {@link GetPutProperty}. Any
     * name-value pairs that have a null value will be included or skipped depending upon the {@link #writeNull}
     * behavior specified at construction time.
     */
    @Override
    public void saveInstance(GetPutProperty namespace) {
        for (String key : getPropertyNames()) {
            String val = getProperty(key);
            if (val != null || writeNull) {
                namespace.putProperty(key, val);
            }
        }
    }

}

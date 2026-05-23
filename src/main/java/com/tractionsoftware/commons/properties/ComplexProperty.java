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

import java.util.HashMap;

/**
 * A simple interface for a serializable/deserializable object which, unlike the simpler types handled by
 * {@link SimpleProperties}, is "complex" (has multiple fields).
 */
public interface ComplexProperty {

    /**
     * Represents a loader/deserializer for a particular type of {@link ComplexProperty}.
     *
     * @param <T>
     *     the type of object that can be created by this Provider.
     */
    @FunctionalInterface
    public static interface Loader<T extends ComplexProperty> {

        /**
         * Loads properties from the given {@link GetProperty} to create a new instance of a compatible type of object.
         *
         * <p>
         * If this method returns false, the caller should assume that it is not in a valid state, and therefore not
         * safe to actually use. (The definition of "safe" may vary, but in general such objects should not be used.)
         *
         * @param properties
         *     from which the properties representing the state of this object are to be loaded.
         */
        public T loadInstance(GetProperty properties);

    }

    /**
     * Produces a helpful String representation of this {@link ComplexProperty}. Implementations should be careful to
     * omit possibly privileged information.
     */
    @Nonnull
    @Override
    public String toString();

    /**
     * Saves the state of this object by writing its state to the given {@link GetPutProperty}. This method serves as a
     * serialization mechanism.
     *
     * @param store
     *     in which the properties of this object are to be stored.
     */
    public void saveInstance(GetPutProperty store);

    /**
     * This helper method is an adapter so that a ComplexProperty's state can be saved in a {@link PutProperty} which is
     * not also a {@link GetPutProperty}.
     *
     * <p>
     * This implementation creates a new simple Map-backed GetPutProperty, invokes {@link #saveInstance(GetPutProperty)}
     * passing that object, and then uses {@link PutProperty#putAllProperties(GetProperty)} to effectively copy all
     * those properties to the given PutProperty. It should be adequate for all implementations.
     *
     * @param put
     *     the {@link PutProperty} to which the state of this object should be written.
     */
    public default void saveInstance(PutProperty put) {
        GetPutProperty store = SimpleProperties.asGetPutProperty(new HashMap<>());
        saveInstance(store);
        put.putAllProperties(store);
    }

}

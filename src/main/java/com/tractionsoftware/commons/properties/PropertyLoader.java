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

import java.util.function.BiFunction;

/**
 * A simple interface for loading an object from a raw String value for
 * {@link GetProperty#getProperty(String, PropertyLoader, boolean)}. This class also includes a method for casting an
 * existing Object to the appropriate type in case this capability is needed for an implementation that uses a cache.
 *
 * @param <T>
 *     the type being loaded or cast.
 * @author Dave Shepperton
 */
public interface PropertyLoader<T> extends BiFunction<String,String,T> {

    /**
     * This method should create or retrieve an instance of type T that corresponds to the given raw String value.
     *
     * @param name
     *     the name of the property being loaded.
     * @param rawValue
     *     the raw String value upon which the T instance should be based.
     * @return an instance of type T that corresponds to the given raw String value
     */
    @Override
    public T apply(String name, String rawValue);

    /**
     * This method should return the given Object cast to an appropriate type or subtype of T.
     *
     * @param o
     *     the Object to be cast.
     * @return the given Object cast to an appropriate type or subtype of T.
     */
    public T cast(Object o);

}

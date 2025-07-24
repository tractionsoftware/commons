/*
 *
 *    Copyright 1996-2025 Traction Software, Inc.
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

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * A simple read-only interface for property name-value pairs.
 *
 * <p>
 * Abstracting the underlying map source itself allows for variable property evaluation, which can be used for runtime
 * l10n.
 *
 * @author Dave Shepperton
 */
public interface ReadOnlyPropertyMap {

    /**
     * An "empty" ReadOnlyPropertyMap. Its {@link #getValue(String)} always returns null and its {@link #getNames()}
     * method returns an empty {@link Set}.
     */
    public static final ReadOnlyPropertyMap EMPTY = new ReadOnlyPropertyMap() {
        @Override
        public final String getValue(String name) {
            return null;
        }

        @Override
        public final Set<String> getNames() {
            return Collections.emptySet();
        }
    };

    /**
     * Returns the value of the named property.
     *
     * @param name
     *     the name of the property whose value is to be retrieved.
     * @return the value of the named property if one is defined; null otherwise.
     */
    public String getValue(String name);

    /**
     * Returns the names of the properties defined in this property map.
     *
     * @return the names of the properties defined in this property map.
     */
    public Set<String> getNames();

    /**
     * Returns a {@link Function} that maps the {@link String} form of {@link Object}s to the corresponding named value
     * from this ReadOnlyPropertyMap.
     *
     * <p>
     * This implementation defers to {@link #getValue(String)} so that instances of ReadOnlyPropertyMap can be used as
     * {@link Function}s.
     *
     * @return a {@link Function} that maps the {@link String} form of {@link Object}s to the corresponding named value
     *     from this ReadOnlyPropertyMap.
     */
    public default Function<Object,String> asFunction() {
        return new Function<Object,String>() {
            @Override
            public final String apply(Object o) {
                return ReadOnlyPropertyMap.this.getValue(Objects.toString(o, null));
            }
        };
    }

}

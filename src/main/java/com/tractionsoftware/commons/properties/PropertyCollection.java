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

/**
 * A simple interface encapsulating a collection of properties.
 *
 * <p>
 * A PropertyCollections is a "leaf" instance if it does not publish any layers of defaults.
 *
 * <p>
 * A PropertyCollection represents "local" values if it does not wrap any other PropertyCollections that may be
 * non-leaf.
 *
 * @author Dave Shepperton
 */
public interface PropertyCollection {

    /**
     * Produces a helpful String representation of this PropertyCollection. Implementors are discouraged from including
     * the actual property name-value pairs in the returned value if they may contain privileged information.
     */
    @Nonnull
    @Override
    public String toString();

    /**
     * Returns a name for this object. The significance of this name will be context-dependent.
     *
     * <p>
     * This default implementation returns null.
     *
     * @return a name for this object.
     */
    public default String getName() {
        return null;
    }

    /**
     * Returns the fully qualified version of the given property name. This is intended to support namespacing and
     * prefixing as applied to collections of properties.
     *
     * <p>
     * This default implementation returns the name as-is. This is an adequate implementation for any PropertyCollection
     * which does not perform property name transformations.
     *
     * @param name
     *     the name to be fully qualified.
     * @return the fully qualified version of the given property name.
     */
    public default String fullyQualify(String name) {
        return name;
    }

    /**
     * Returns a {@link PropertyCollection} -- which should generally be of the same type as the receiving Object --
     * representing a "subspace" of the underlying properties, using the default separator character ('_'). The subspace
     * provides access to all the properties in this store whose names start with the given prefix and the default
     * separator character. The property names will not include the space and separator.
     *
     * <p>
     * One special case is that in the resulting {@link PropertyCollection}, the name null or the empty String can be
     * used to refer to the property in the original store with the name exactly matching the requested space.
     *
     * @param space
     *     the requested namespace.
     * @return a {@link PropertyCollection} representing a "subspace" of the underlying properties, using the default
     *     separator character ('_').
     */
    public PropertyCollection getNamespace(String space);

    /**
     * Returns a {@link PropertyCollection} -- which should generally be of the same type as the receiving Object --
     * representing a "subspace" of the underlying properties. The subspace provides access to all the properties in
     * this store whose names start with the given prefix and the given separator character. The property names will not
     * include the space and separator.
     *
     * <p>
     * One special case is that in the resulting {@link PropertyCollection}, the name null or the empty String can be
     * used to refer to the property in the original store with the name exactly matching the requested space.
     *
     * @param space
     *     the requested namespace.
     * @param sep
     *     a separator character to use to separate the space name from the subspace's property names.
     * @return a {@link PropertyCollection} representing a "subspace" of the underlying properties, using the requested
     *     separator character.
     */
    public PropertyCollection getNamespace(String space, char sep);

    /**
     * Returns a {@link PropertyCollection} -- which should generally be of the same type as the receiving Object --
     * representing a prefix applied to names of the underlying properties, using the default separator character ('_').
     * This provides access to all the properties in this store as though their names had the given prefix and default
     * separator character prepended.
     *
     * <p>
     * One special case is that in the resulting {@link PropertyCollection}, the name null or the empty String can be
     * used to refer to the property in the original store with the name exactly matching the requested prefix.
     *
     * @param prefix
     *     the requested prefix.
     * @return a {@link PropertyCollection} representing a prefix applied to names of the underlying properties, using
     *     the default separator character ('_').
     */
    public PropertyCollection getPrefix(String prefix);

    /**
     * Returns a {@link PropertyCollection} -- which should generally be of the same type as the receiving Object --
     * representing a prefix applied to names of the underlying properties, using the given separator character. This
     * provides access to all the properties in this store as though their names had the given prefix and the given
     * separator character prepended.
     *
     * <p>
     * One special case is that in the resulting {@link PropertyCollection}, the name null or the empty String can be
     * used to refer to the property in the original store with the name exactly matching the requested prefix.
     *
     * @param prefix
     *     the requested prefix.
     * @param sep
     *     a separator character to use to separate the space name from the subspace's property names.
     * @return a {@link PropertyCollection} representing a prefix applied to names of the underlying properties, using
     *     the requested separator character.
     */
    public PropertyCollection getPrefix(String prefix, char sep);

}

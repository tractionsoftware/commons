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

/**
 * PropertyNameTransformer represents a transformation applied to
 * property names on read or write in order to provide a mapping from
 * a requested property name to an "internal" property name, as well
 * as the inverse mapping.
 *
 * @author Dave Shepperton
 */
public interface PropertyNameMapper {

    /**
     * Returns the "internal" name to be used to read a property
     * reading operation. {@link #getPublishedName(String)} is
     * effectively the inverse.
     *
     * @param requestedName
     *            the name that was supplied from the client.
     * @return the name to be used for a given property reading
     *         operation corresponding to the given requested name, if
     *         any; null otherwise.
     */
    public String getPropertyName(String requestedName);

    /**
     * Returns the name to be "published" for a given property name --
     * i.e., the name that the client should supply. This effectively
     * the inverse of {@link #getPropertyName(String)}.
     *
     * @param propertyName
     *            the "internal" name of the property.
     * @return the name to be "published" for a given property name --
     *         i.e., the name that the client should supply.
     */
    public String getPublishedName(String propertyName);

    /**
     * This method should return true only if this PropertyNameMapper
     * is definitely the inverse of the other PropertyNameMapper.
     * There's not always going to be a definite answer to this.
     *
     * @param otherNameMapper
     *            the other PropertyNameMapper.
     * @return true if this PropertyNameMapper is definitely the
     *         inverse of the other PropertyNameMapper; false
     *         otherwise.
     */
    public boolean isInverseOf(PropertyNameMapper otherNameMapper);

    /**
     * Returns a PropertyNameMapper that represents the combination
     * this PropertyNameMapper and the other PropertyNameMapper.
     *
     * <p>
     * This implementation uses
     * {@link SimpleCombinedPropertyNameMapper}. It also gracefully
     * handles a null argument by returning this PropertyNameMapper
     * instance itself. Subclasses that can more intelligently combine
     * themselves with other instances (especially with other
     * instances of the same class) should override it.
     *
     * @param otherNameMapper
     *            the PropertyNameMapper with which to combine this
     *            one.
     * @return a PropertyNameMapper that represents the combination
     *         this PropertyNameMapper and the other
     *         PropertyNameMapper.
     */
    public default PropertyNameMapper combine(PropertyNameMapper otherNameMapper) {
        if (otherNameMapper == null) {
            return this;
        }
        return new SimpleCombinedPropertyNameMapper(this, otherNameMapper);
    }

}

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
import jakarta.annotation.Nullable;

/**
 * PropertyNameTransformer represents a transformation applied to property names on read or write in order to provide a
 * mapping from a requested property name to an "internal" property name, as well as the inverse mapping.
 *
 * @author Dave Shepperton
 */
public interface PropertyNameMapper {

    /**
     * Returns the "actual" name for a given property name. This would be the name that would be used by a
     * {@link GetProperty} or {@link PutProperty} implementation for retrieving and setting properties in the underlying
     * store. {@link #getPublishedName(String)} is effectively the inverse of this method.
     *
     * @param requestedName
     *     the name that was supplied from the client.
     * @return the "actual" name for a given property name to be used to for property reading and writing on the store.
     */
    public String getActualPropertyName(String requestedName);

    /**
     * Returns the "published" name for a given property name. This would be the name that would be included in the
     * names in {@link GetProperty#getPropertyNames()}, and which the client should supply as the name to methods such
     * as {@link GetProperty#getProperty(String)} and {@link PutProperty#putProperty(String, String)} This effectively
     * the inverse of {@link #getActualPropertyName(String)}.
     *
     * @param propertyName
     *     the "internal" name of the property.
     * @return the "published" name for a given property name.
     */
    public String getPublishedName(String propertyName);

    /**
     * Returns true if this PropertyNameMapper is definitely the inverse of the given other PropertyNameMapper.
     *
     * @param otherNameMapper
     *     the other PropertyNameMapper.
     * @return true if this PropertyNameMapper is definitely the inverse of the given other PropertyNameMapper; false
     *     otherwise.
     */
    public boolean isInverseOf(PropertyNameMapper otherNameMapper);

    /**
     * Returns a PropertyNameMapper that represents the composition of the this instance with the other instance. The
     * effect is that this mapping will be applied before the other instance's mapping.
     *
     * <p>
     * This implementation uses {@link MultiPropertyNameMapper#getCombined(PropertyNameMapper...)}. Subclasses that can
     * more intelligently wrap other instances (especially with other instances of the same class) should override it.
     *
     * @param otherNameMapper
     *     the PropertyNameMapper to be composed with this instance.
     * @return a PropertyNameMapper that represents the composition of the this instance with the other instance.
     */
    @Nonnull
    public default PropertyNameMapper compose(@Nullable PropertyNameMapper otherNameMapper) {
        return MultiPropertyNameMapper.getCombined(this, otherNameMapper);
    }

}

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

import java.util.Objects;

/**
 * A very simple {@link PropertyNameMapper} which implements
 *
 * @author Dave Shepperton
 */
public final class SimpleCombinedPropertyNameMapper implements PropertyNameMapper {

    private final PropertyNameMapper inner;

    private final PropertyNameMapper outer;

    public SimpleCombinedPropertyNameMapper(PropertyNameMapper inner, PropertyNameMapper outer) {
        Objects.requireNonNull(inner, "First name mapper");
        Objects.requireNonNull(outer, "Second name mapper");
        this.inner = inner;
        this.outer = outer;
    }

    @Override
    public final boolean equals(Object other) {
        if (!(other instanceof SimpleCombinedPropertyNameMapper)) {
            return false;
        }
        SimpleCombinedPropertyNameMapper otherCombinedMapper = (SimpleCombinedPropertyNameMapper) other;
        if (inner.equals(otherCombinedMapper.inner) &&
            outer.equals(otherCombinedMapper.outer)) {
            return true;
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(inner, outer);
    }

    @Override
    public final String toString() {
        return "combined name-mapper:{" + inner + ';' + outer + '}';
    }

    @Override
    public final String getPropertyName(String requestedName) {
        return inner.getPropertyName(outer.getPropertyName(requestedName));
    }

    @Override
    public final String getPublishedName(String requestedName) {
        return outer.getPublishedName(inner.getPublishedName(requestedName));
    }

    @Override
    public final boolean isInverseOf(PropertyNameMapper otherNameMapper) {
        if (!(otherNameMapper instanceof SimpleCombinedPropertyNameMapper)) {
            return false;
        }
        SimpleCombinedPropertyNameMapper otherCombinedMapper = (SimpleCombinedPropertyNameMapper) otherNameMapper;
        if (inner.isInverseOf(otherCombinedMapper.outer) &&
            outer.isInverseOf(otherCombinedMapper.inner)) {
            return true;
        }
        return false;
    }

}

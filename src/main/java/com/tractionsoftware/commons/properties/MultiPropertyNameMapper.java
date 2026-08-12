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

import com.google.common.collect.ImmutableList;
import com.tractionsoftware.commons.lang.StringUtil;
import jakarta.annotation.Nonnull;

import java.util.*;

/**
 * A very simple {@link PropertyNameMapper} which implements
 *
 * @author Dave Shepperton
 */
public final class MultiPropertyNameMapper implements PropertyNameMapper {

    @Nonnull
    public static final PropertyNameMapper getCombined(@Nonnull PropertyNameMapper... mappers) {

        Objects.requireNonNull(mappers, "mappers");
        if (mappers.length == 0) {
            throw new IllegalArgumentException("No PropertyNameMappers.");
        }

        ImmutableList.Builder<PropertyNameMapper> builder = ImmutableList.builder();
        for (PropertyNameMapper mapper : mappers) {
            if (mapper == null) {
                continue;
            }
            addMapper(builder, mapper);
        }

        ImmutableList<PropertyNameMapper> useMappers = builder.build();
        int count = useMappers.size();
        if (count == 0) {
            throw new IllegalArgumentException("No PropertyNameMappers.");
        }
        if (count == 1) {
            return useMappers.getFirst();
        }
        return new MultiPropertyNameMapper(useMappers);

    }

    public static final MultiPropertyNameMapper createInstance(@Nonnull PropertyNameMapper first, @Nonnull PropertyNameMapper second) {
        Objects.requireNonNull(first, "first SimpleCombinedPropertyNameMapper");
        Objects.requireNonNull(second, "second SimpleCombinedPropertyNameMapper");
        ImmutableList.Builder<PropertyNameMapper> builder = ImmutableList.builder();
        addMapper(builder, first);
        addMapper(builder, second);
        return new MultiPropertyNameMapper(builder.build());
    }

    private static final void addMapper(ImmutableList.Builder<PropertyNameMapper> builder, PropertyNameMapper mapper) {
        if (mapper instanceof MultiPropertyNameMapper combined) {
            builder.addAll(combined.mappers);
        }
        else {
            builder.add(mapper);
        }
    }

    private final SequencedCollection<PropertyNameMapper> mappers;

    private MultiPropertyNameMapper(SequencedCollection<PropertyNameMapper> mappers) {
        this.mappers = mappers;
    }

    @Override
    public final boolean equals(Object other) {
        if (!(other instanceof MultiPropertyNameMapper otherCombinedMapper)) {
            return false;
        }
        if (mappers.equals(otherCombinedMapper.mappers)) {
            return true;
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(mappers);
    }

    @Override
    public final String toString() {
        return "combined name-mapper:{" + StringUtil.join(mappers, ',') + "}";
    }

    @Override
    public final String getActualPropertyName(String requestedName) {
        String result = requestedName;
        for (PropertyNameMapper mapper : mappers.reversed()) {
            result = mapper.getActualPropertyName(result);
        }
        return result;
    }

    @Override
    public final String getPublishedName(String requestedName) {
        String result = requestedName;
        for (PropertyNameMapper mapper : mappers) {
            result = mapper.getPublishedName(result);
        }
        return result;
    }

    @Override
    public final boolean isInverseOf(PropertyNameMapper otherNameMapper) {
        if (!(otherNameMapper instanceof MultiPropertyNameMapper otherCombinedMapper)) {
            return false;
        }
        int len = mappers.size();
        int otherLen = otherCombinedMapper.mappers.size();
        if (len != otherLen) {
            return false;
        }
        Iterator<PropertyNameMapper> iter = mappers.iterator();
        Iterator<PropertyNameMapper> otherIter = mappers.reversed().iterator();
        while (iter.hasNext()) {
            if (!iter.next().isInverseOf(otherIter.next())) {
                return false;
            }
        }
        return true;
    }

}

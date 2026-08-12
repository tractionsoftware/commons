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

import com.google.common.collect.BiMap;

public final class BiMapPropertyNameMapper implements PropertyNameMapper {

    private final BiMap<?,?> internalName2ExternalName;

    public BiMapPropertyNameMapper(BiMap<?,?> internalName2ExternalName) {
        this.internalName2ExternalName = internalName2ExternalName;
    }

    @Override
    public final String getActualPropertyName(String requestedName) {
        return Objects.toString(internalName2ExternalName.inverse().get(requestedName), null);
    }

    @Override
    public final String getPublishedName(String propertyName) {
        return Objects.toString(internalName2ExternalName.get(propertyName), null);
    }

    @Override
    public final boolean isInverseOf(PropertyNameMapper otherNameMapper) {
        if (!(otherNameMapper instanceof BiMapPropertyNameMapper)) {
            return false;
        }
        BiMapPropertyNameMapper otherMapPropertyNameMapper = (BiMapPropertyNameMapper) otherNameMapper;
        if (internalName2ExternalName.equals(otherMapPropertyNameMapper.internalName2ExternalName.inverse())) {
            return true;
        }
        return false;
    }

}

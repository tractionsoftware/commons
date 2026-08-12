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

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Dave Shepperton
 */
public abstract class AbstractPropertyNameMappingGetProperty<G extends GetProperty>
    extends AbstractPropertyNameMappingPropertyCollection<G>
    implements GetProperty {

    public AbstractPropertyNameMappingGetProperty(G props, PropertyNameMapper nameMapper) {
        super(props, nameMapper);
    }

    @Override
    public final String getProperty(String name) {
        return props.getProperty(nameMapper.getActualPropertyName(name));
    }

    @Override
    public final Set<String> getPropertyNames() {
        return props.getPropertyNames().stream()
            .map(nameMapper::getPublishedName)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public final boolean hasProperty(String name) {
        return props.hasProperty(nameMapper.getActualPropertyName(name));
    }

    @Override
    public final boolean hasNonBlankProperty(String name) {
        return props.hasNonBlankProperty(nameMapper.getActualPropertyName(name));
    }

    @Override
    public final <T> T getProperty(String name, PropertyLoader<? extends T> loader, boolean mayUseCache) {
        return props.getProperty(nameMapper.getActualPropertyName(name), loader, mayUseCache);
    }

    @Override
    public final String getLocalProperty(String name) {
        return props.getLocalProperty(nameMapper.getActualPropertyName(name));
    }

    @Override
    public final GetProperty getDefaults() {
        return PropertyNameMappingGetProperty.applyPropertyNameMapper(props.getDefaults(), nameMapper);
    }

    @Override
    public final GetProperty getLocals() {
        return PropertyNameMappingGetProperty.applyPropertyNameMapper(props.getLocals(), nameMapper);
    }

}

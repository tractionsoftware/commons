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

package com.tractionsoftware.commons.config;

import com.tractionsoftware.commons.properties.AbstractPropertyNameMappingGetProperty;
import com.tractionsoftware.commons.properties.PropertyNameMapper;
import com.tractionsoftware.commons.properties.SimplePropertyNameMapper;
import jakarta.annotation.Nonnull;

import java.util.Map;
import java.util.Objects;

public final class PropertyNameMappingConfiguration extends AbstractPropertyNameMappingGetProperty<Configuration>
    implements Configuration {

    public static final Configuration wrapInNamespace(Configuration props, String space) {
        return applyPropertyNameMapper(props, SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator(space));
    }

    public static final Configuration wrapInNamespace(Configuration props, String space, char separator) {
        return applyPropertyNameMapper(
            props,
            SimplePropertyNameMapper.getNamespaceInstanceWithSeparator(space, separator)
        );
    }

    public static final Configuration wrapInPrefix(Configuration props, String space) {
        return applyPropertyNameMapper(props, SimplePropertyNameMapper.getPrefixInstanceWithDefaultSeparator(space));
    }

    public static final Configuration wrapInPrefix(Configuration props, String space, char separator) {
        return applyPropertyNameMapper(
            props,
            SimplePropertyNameMapper.getPrefixInstanceWithSeparator(space, separator)
        );
    }

    public static final Configuration applyPropertyNameMapper(Configuration config, PropertyNameMapper nameMapper) {
        if (config == null || nameMapper == null) {
            return config;
        }
        if (config instanceof PropertyNameMappingConfiguration nameMappedProps) {
            if (nameMappedProps.nameMapper.isInverseOf(nameMapper)) {
                return nameMappedProps.props;
            }
            return new PropertyNameMappingConfiguration(
                nameMappedProps.props,
                nameMappedProps.nameMapper.compose(nameMapper)
            );
        }
        return new PropertyNameMappingConfiguration(config, nameMapper);
    }

    public PropertyNameMappingConfiguration(Configuration config, PropertyNameMapper nameMapper) {
        super(config, nameMapper);
    }

    @Nonnull
    @Override
    public final String toString() {
        return "Configuration: " + super.toString();
    }

    @Override
    public final String getPath() {
        return props.getPath();
    }

    @Override
    public final Map<String,String> getTemplateSettings() {
        return props.getTemplateSettings();
    }

    @Override
    public final Configuration withNewName(String newName) {
        if (Objects.equals(getName(), newName)) {
            return this;
        }
        return applyPropertyNameMapper(delegate().withNewName(newName), nameMapper);
    }

}

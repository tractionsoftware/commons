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

public final class PropertyNameMappingGetPutProperty extends AbstractPropertyNameMappingGetPutProperty<GetPutProperty> {

    public static final GetPutProperty wrapInNamespace(GetPutProperty props, String space) {
        return applyPropertyNameMapper(props, SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator(space));
    }

    public static final GetPutProperty wrapInNamespace(GetPutProperty props, String space, char separator) {
        return applyPropertyNameMapper(props, SimplePropertyNameMapper.getNamespaceInstanceWithSeparator(space, separator));
    }

    public static final GetPutProperty wrapInPrefix(GetPutProperty props, String space) {
        return applyPropertyNameMapper(props, SimplePropertyNameMapper.getPrefixInstanceWithDefaultSeparator(space));
    }

    public static final GetPutProperty wrapInPrefix(GetPutProperty props, String space, char separator) {
        return applyPropertyNameMapper(props, SimplePropertyNameMapper.getPrefixInstanceWithSeparator(space, separator));
    }

    public static final GetPutProperty applyPropertyNameMapper(GetPutProperty props, PropertyNameMapper nameMapper) {
        if (props == null || nameMapper == null) {
            return props;
        }
        if (props instanceof PropertyNameMappingGetPutProperty nameMappedProps) {
            if (nameMappedProps.nameMapper.isInverseOf(nameMapper)) {
                return nameMappedProps.props;
            }
            return new PropertyNameMappingGetPutProperty(nameMappedProps.props, nameMappedProps.nameMapper.compose(nameMapper));
        }
        return new PropertyNameMappingGetPutProperty(props, nameMapper);
    }

    public PropertyNameMappingGetPutProperty(GetPutProperty props, PropertyNameMapper nameMapper) {
        super(props, nameMapper);
    }

    @Nonnull
    @Override
    public final String toString() {
        return "GetPutProperty: " + super.toString();
    }

}

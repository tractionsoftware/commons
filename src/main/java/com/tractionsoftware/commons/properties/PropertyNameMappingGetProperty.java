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

public final class PropertyNameMappingGetProperty extends AbstractPropertyNameMappingGetProperty<GetProperty> {

    public static final GetProperty wrapInNamespace(GetProperty props, String space) {
        return applyPropertyNameMapper(props, SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator(space));
    }

    public static final GetProperty wrapInNamespace(GetProperty props, String space, char separator) {
        return applyPropertyNameMapper(
            props,
            SimplePropertyNameMapper.getNamespaceInstanceWithSeparator(space, separator)
        );
    }

    /**
     * Returns a {@link GetProperty} -- which should generally be of the same type as the receiving Object --
     * representing a prefix applied to names of the underlying properties, using the default separator character ('_').
     * This provides access to all the properties in this store as though their names had the given prefix and default
     * separator character prepended.
     *
     * <p>
     * Take the of this GetProperty's "bar" property. For the prefix "foo" and the separator character "_', the returned
     * prefixed GetProperty would then have that same property now called "foo_bar".
     *
     * <p>
     * One special case is that in the resulting GetProperty, the name null or the empty String can be used to refer to
     * the property in the original store with the name exactly matching the requested prefix.
     *
     * @param props
     *     the {@link GetProperty} to wrap.
     * @param prefix
     *     the requested prefix.
     * @return a {@link GetProperty} representing a prefix applied to names of the underlying properties, using the
     *     default separator character ('_').
     */
    public static final GetProperty wrapInPrefix(GetProperty props, String prefix) {
        return applyPropertyNameMapper(props, SimplePropertyNameMapper.getPrefixInstanceWithDefaultSeparator(prefix));
    }

    public static final GetProperty wrapInPrefix(GetProperty props, String prefix, char separator) {
        return applyPropertyNameMapper(
            props, SimplePropertyNameMapper.getPrefixInstanceWithSeparator(prefix, separator)
        );
    }

    public static final GetProperty applyPropertyNameMapper(GetProperty props, PropertyNameMapper nameMapper) {
        if (props == null || nameMapper == null) {
            return props;
        }
        if (props instanceof PropertyNameMappingGetProperty nameMappedProps) {
            if (nameMappedProps.nameMapper.isInverseOf(nameMapper)) {
                return nameMappedProps.props;
            }
            return new PropertyNameMappingGetProperty(
                nameMappedProps.props,
                                                      nameMappedProps.nameMapper.compose(nameMapper)
            );
        }
        return new PropertyNameMappingGetProperty(props, nameMapper);
    }

    public PropertyNameMappingGetProperty(GetProperty props, PropertyNameMapper nameMapper) {
        super(props, nameMapper);
    }

    @Nonnull
    @Override
    public String toString() {
        return "GetProperty: " + super.toString();
    }

}

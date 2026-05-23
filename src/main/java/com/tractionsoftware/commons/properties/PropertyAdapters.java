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

import com.google.common.collect.ImmutableSet;
import com.tractionsoftware.commons.config.Configuration;
import com.tractionsoftware.commons.lang.NativeTypeConversion;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * @author Dave Shepperton
 */
public final class PropertyAdapters {

    private PropertyAdapters() {
    }

    private static final UnaryOperator<String> NULL_INSTEAD_OF_EMPTY = new UnaryOperator<>() {

        @Override
        public final String toString() {
            return "empty-to-null";
        }

        @Override
        public final String apply(String value) {
            if (StringUtils.isEmpty(value)) {
                return null;
            }
            return value;
        }

    };

    private static final UnaryOperator<String> EMPTY_INSTEAD_OF_NULL = new UnaryOperator<String>() {

        @Override
        public final String toString() {
            return "null-to-empty";
        }

        @Override
        public final String apply(String value) {
            return StringUtils.defaultString(value);
        }

    };

    /**
     * Changes null values to empty.
     */
    public static final PutProperty wrapNullIsEmptyString(PutProperty props) {
        return PropertyValueMappingPutProperty.applyPropertyValueTransformer(props, EMPTY_INSTEAD_OF_NULL);
    }

    /**
     * Note that this maps null values to the empty string on read and write.
     */
    public static final PropStore wrapEmptyStringIsNull(PropStore props) {
        return PropertyValueMappingPropStore.applyPropertyValueTransformers(
            props,
            NULL_INSTEAD_OF_EMPTY,
            NULL_INSTEAD_OF_EMPTY
        );
    }

    public static final GetProperty functionToGetProperty(final Function<? super String,String> function) {
        return functionToGetProperty(function, ImmutableSet.of());
    }

    public static final GetProperty functionToGetProperty(final Function<? super String,String> function, Set<String> domain) {

        return new GetProperty() {

            @Override
            public final String getProperty(String name) {
                if (function == null) {
                    return null;
                }
                return function.apply(name);
            }

            @Override
            public final Set<String> getPropertyNames() {
                if (domain instanceof ImmutableSet<String> immutable) {
                    return immutable;
                }
                return Collections.unmodifiableSet(domain);
            }

        };

    }

    public static final GetProperty getPropertyAtAsGetProperty(final GetPropertyAt props) {

        return new GetProperty() {

            @Override
            public final @NonNull String toString() {
                return "GetProperty: from GetPropertyAt {" + props + "}";
            }

            @Override
            public final String getProperty(String name) {
                int index = NativeTypeConversion.stringToInt(name, -1);
                if (index >= 0) {
                    return Objects.toString(props.getPropertyAt(index), null);
                }
                return null;
            }

            @Override
            public final Set<String> getPropertyNames() {
                return props.getPropertyNumbers().stream()
                    .map(Object::toString)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            }

        };

    }

    public static final Configuration getPropertyAsConfiguration(GetProperty props) {
        return getPropertyAsConfiguration(props, null);
    }

    public static final Configuration getPropertyAsConfiguration(final GetProperty props, final String usePath) {

        Objects.requireNonNull(props, "GetProperty");

        return new Configuration() {

            @Override
            public final @NonNull String toString() {
                return Objects.toString(props.getName(), "[no name]");
            }

            @Override
            public final String getName() {
                return props.getName();
            }

            @Override
            public final String getProperty(String name) {
                return props.getProperty(name);
            }

            @Override
            public final Set<String> getPropertyNames() {
                return props.getPropertyNames();
            }

            @Override
            public final String getLocalProperty(String name) {
                return props.getLocalProperty(name);
            }

            @Override
            public final String getPath() {
                return usePath;
            }

            @Override
            public final Map<String,String> getTemplateSettings() {
                return null;
            }

        };

    }

}

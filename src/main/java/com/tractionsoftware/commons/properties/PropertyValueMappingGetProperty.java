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

import com.tractionsoftware.commons.text.TextTransformer;
import jakarta.annotation.Nonnull;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class PropertyValueMappingGetProperty extends AbstractPropertyValueMappingGetProperty<GetProperty> {

    public static final GetProperty wrapWithValueTransformer(GetProperty props, TextTransformer transformer) {
        if (props == null || transformer == null) {
            return props;
        }
        return new PropertyValueMappingGetProperty(props, getValueBiFunction(transformer));
    }

    public static final GetProperty applyPropertyValueTransformer(GetProperty props, BiFunction<String,String,String> readNameValueMapper) {
        if (props == null || readNameValueMapper == null) {
            return props;
        }
        return new PropertyValueMappingGetProperty(props, readNameValueMapper);
    }

    public static final GetProperty applyPropertyValueTransformer(GetProperty props, Function<String,String> readValueMapper) {
        if (props == null || readValueMapper == null) {
            return props;
        }
        return new PropertyValueMappingGetProperty(props, getValueBiFunction(readValueMapper));
    }

    public PropertyValueMappingGetProperty(GetProperty props, BiFunction<String,String,String> readValueMapper) {
        super(props, readValueMapper);
    }

    @Nonnull
    @Override
    public final String toString() {
        return "GetProperty: " + super.toString();
    }

}

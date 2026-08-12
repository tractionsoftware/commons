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

/**
 * A simple PutProperty that saves to another given PutProperty,
 * transforming the value to be saved with the given Transformer
 * before saving it.
 */
public final class PropertyValueMappingPutProperty extends AbstractPropertyValueMappingPropertyCollection<PutProperty> implements PutProperty {

    public static final PutProperty wrapWithValueTransformer(PutProperty props, TextTransformer transformer) {
        if (props == null || transformer == null) {
            return props;
        }
        return new PropertyValueMappingPutProperty(props, getValueBiFunction(transformer));
    }

    public static final PutProperty applyPropertyValueTransformer(PutProperty props, Function<String,String> writeValueMapper) {
        if (props == null || writeValueMapper == null) {
            return props;
        }
        return new PropertyValueMappingPutProperty(props, getValueBiFunction(writeValueMapper));
    }

    public static final PutProperty applyPropertyValueTransformer(PutProperty props, BiFunction<String,String,String> writeValueMapper) {
        if (props == null || writeValueMapper == null) {
            return props;
        }
        return new PropertyValueMappingPutProperty(props, writeValueMapper);
    }

    private PropertyValueMappingPutProperty(PutProperty props, BiFunction<String,String,String> writeNameValueMapper) {
        super(props, BI_VALUE_IDENTITY, writeNameValueMapper);
    }

    @Override
    public final @Nonnull String toString() {
        return "PutProperty: " + super.toString();
    }

    @Override
    public final void putProperty(String name, String val) {
        props.putProperty(name, writeNameValueMapper.apply(name, val));
    }

}

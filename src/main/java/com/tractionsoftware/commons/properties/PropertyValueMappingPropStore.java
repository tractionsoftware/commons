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
 *
 * @param <R>
 *     the type of object used to facilitate committing changes.
 * @author Dave Shepperton
 */
public final class PropertyValueMappingPropStore<R> extends AbstractPropertyValueMappingGetPutProperty<PropStore<R>>
    implements PropStore<R> {

    public static final <R> PropStore<R> wrapWithValueTransformers(PropStore<R> props, TextTransformer readValueMapper, TextTransformer writeValueMapper) {
        if (props == null || (readValueMapper == null && writeValueMapper == null)) {
            return props;
        }
        return new PropertyValueMappingPropStore<>(
            props, getValueBiFunction(readValueMapper), getValueBiFunction(writeValueMapper)
        );
    }

    public static final <R> PropStore<R> applyPropertyValueTransformers(PropStore<R> props, Function<String,String> readValueMapper, Function<String,String> writeValueMapper) {
        if (props == null || (readValueMapper == null && writeValueMapper == null)) {
            return props;
        }
        return new PropertyValueMappingPropStore<>(
            props, getValueBiFunction(readValueMapper), getValueBiFunction(writeValueMapper)
        );
    }

    public static final <R> PropStore<R> applyPropertyValueTransformers(PropStore<R> props, BiFunction<String,String,String> readNameValueMapper, BiFunction<String,String,String> writeNameValueMapper) {
        if (props == null || (readNameValueMapper == null && writeNameValueMapper == null)) {
            return props;
        }
        return new PropertyValueMappingPropStore<>(
            props,
            getNameValueBiFunctionOrValueIdentity(readNameValueMapper),
            getNameValueBiFunctionOrValueIdentity(writeNameValueMapper)
        );
    }

    private PropertyValueMappingPropStore(PropStore<R> props, BiFunction<String,String,String> readNameValueMapper, BiFunction<String,String,String> writeNameValueMapper) {
        super(props, readNameValueMapper, writeNameValueMapper);
    }

    @Nonnull
    @Override
    public final String toString() {
        return "PropStore: " + super.toString();
    }

    @Override
    public final CommitResult commitChanges(R rec) {
        return props.commitChanges(rec);
    }

}

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
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author Dave Shepperton
 */
public abstract class AbstractPropertyValueMappingPropertyCollection<P extends PropertyCollection>
    extends StaticForwardingPropertyCollection<P> {

    protected static final BiFunction<String,String,String> BI_VALUE_IDENTITY = (name, value) -> value;

    protected static final BiFunction<String,String,String> getValueBiFunction(TextTransformer transformer) {
        if (transformer == null) {
            return BI_VALUE_IDENTITY;
        }
        return (name, value) -> transformer.transform(value);
    }

    protected static final BiFunction<String,String,String> getValueBiFunction(Function<String,String> function) {
        if (function == null) {
            return BI_VALUE_IDENTITY;
        }
        return (name, value) -> function.apply(value);
    }

    protected static final BiFunction<String,String,String> getNameValueBiFunctionOrValueIdentity(BiFunction<String,String,String> nameValueFunction) {
        return Objects.requireNonNullElse(nameValueFunction, BI_VALUE_IDENTITY);
    }

    protected final BiFunction<String,String,String> readNameValueMapper;

    protected final BiFunction<String,String,String> writeNameValueMapper;

    public AbstractPropertyValueMappingPropertyCollection(P props, BiFunction<String,String,String> readNameValueMapper, BiFunction<String,String,String> writeNameValueMapper) {
        super(props);
        this.readNameValueMapper = readNameValueMapper;
        this.writeNameValueMapper = writeNameValueMapper;
    }

    @Nonnull
    @Override
    public String toString() {
        return "value-mapper [read: " + readNameValueMapper + "] [write: " + writeNameValueMapper + "] {" + props + "}";
    }

}

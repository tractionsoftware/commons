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

import java.util.Set;
import java.util.function.BiFunction;

/**
 * @author Dave Shepperton
 */
public abstract class AbstractPropertyValueMappingGetProperty<G extends GetProperty>
    extends AbstractPropertyValueMappingPropertyCollection<GetProperty>
    implements GetProperty {

    public AbstractPropertyValueMappingGetProperty(G props, BiFunction<String,String,String> readValueMapper) {
        super(props, readValueMapper, BI_VALUE_IDENTITY);
    }

    @Override
    public final String getProperty(String name) {
        return readNameValueMapper.apply(name, props.getProperty(name));
    }

    @Override
    public final Set<String> getPropertyNames() {
        return props.getPropertyNames();
    }

    @Override
    public final String getLocalProperty(String name) {
        return readNameValueMapper.apply(name, props.getLocalProperty(name));
    }

    @Override
    public final GetProperty getDefaults() {
        return PropertyValueMappingGetProperty.applyPropertyValueTransformer(props.getDefaults(), readNameValueMapper);
    }

    @Override
    public final GetProperty getLocals() {
        return PropertyValueMappingGetProperty.applyPropertyValueTransformer(props.getLocals(), readNameValueMapper);
    }

}

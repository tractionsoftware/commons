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
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.function.Supplier;

public final class CachingGetPutProperty extends AbstractCachingGetPutProperty<GetPutProperty> {

    public static final GetPutProperty wrapWithDefaultCache(GetPutProperty props) {
        return new CachingGetPutProperty(props, PropertyCache.createInstanceValidatingOnRead());
    }

    public static final GetPutProperty wrapWithDefaultCache(Supplier<? extends GetPutProperty> provider) {
        return wrapWithDefaultCache(DynamicForwardingGetPutProperty.wrap(provider));
    }

    public static final GetPutProperty wrapWithCache(GetPutProperty props, PropertyCache cache) {
        Objects.requireNonNull(cache, "PropertyCache");
        return new CachingGetPutProperty(props, cache);
    }

    public CachingGetPutProperty(GetPutProperty props, PropertyCache cache) {
        super(props, cache);
    }

    @Nonnull
    @Override
    public final String toString() {
        return "GetPutProperty: " + super.toString();
    }

}

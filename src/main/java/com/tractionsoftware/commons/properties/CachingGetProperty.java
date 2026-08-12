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

import java.util.function.Supplier;

public final class CachingGetProperty extends AbstractCachingGetProperty<GetProperty> {

    public static final GetProperty wrapWithDefaultCache(GetProperty props) {
        return new CachingGetProperty(props, PropertyCache.createInstanceValidatingOnRead());
    }

    public static final GetProperty wrapWithDefaultCache(Supplier<? extends GetProperty> provider) {
        return wrapWithDefaultCache(DynamicForwardingGetProperty.wrap(provider));
    }

    public static final GetProperty wrapWithCache(GetProperty props, PropertyCache cache) {
        return new CachingGetProperty(props, cache);
    }

    public CachingGetProperty(GetProperty props, PropertyCache cache) {
        super(props, cache);
    }

    @Nonnull
    @Override
    public final String toString() {
        return "GetProperty: " + super.toString();
    }

}

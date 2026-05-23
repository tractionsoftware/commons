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

import java.util.function.Supplier;

import jakarta.annotation.Nonnull;

/**
 *
 * @param <R>
 */
public final class CachingPropStore<R> extends AbstractCachingGetPutProperty<PropStore<R>> implements PropStore<R> {

    public static final <R> PropStore<R> wrapWithDefaultCache(PropStore<R> props) {
        return new CachingPropStore<>(props, PropertyCache.createInstanceValidatingOnRead());
    }

    public static final <R> PropStore<R> wrapWithDefaultCache(Supplier<? extends PropStore<R>> provider) {
        return wrapWithDefaultCache(DynamicForwardingPropStore.wrap(provider));
    }

    public static final <R> PropStore<R> wrapWithCache(PropStore<R> props, PropertyCache cache) {
        return new CachingPropStore<>(props, cache);
    }

    public CachingPropStore(PropStore<R> props, PropertyCache cache) {
        super(props, cache);
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

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

import java.util.Objects;
import java.util.function.Supplier;

public class DynamicForwardingPropStore<R> extends ForwardingPropStore<R> {

    /**
     * Creates a {@link PropStore} that defers to the PropStore provided by the given {@link Supplier}. This is perfect
     * for wrapping a dynamically provided object to ensure that only the PropStore functionality is exposed.
     *
     * @param provider
     *     the {@link Supplier} for the {@link PropStore} to which the returned PropStore will defer.
     * @return a {@link PropStore} that defers to the PropStore provided by the given {@link Supplier}.
     */
    public static final <R> PropStore<R> wrap(Supplier<? extends PropStore<R>> provider) {
        return new DynamicForwardingPropStore<>(provider);
    }

    protected final Supplier<? extends PropStore<R>> provider;

    public DynamicForwardingPropStore(Supplier<? extends PropStore<R>> provider) {
        this.provider = provider;
    }

    @Nonnull
    @Override
    public String toString() {
        return "PropStore: dyn fwd {" + provider + " -> " + provider.get() + "}";
    }

    @Nonnull
    @Override
    protected final PropStore<R> delegate() {
        PropStore<R> delegate = provider.get();
        Objects.requireNonNull(delegate, "dynamic delegate PropStore");
        return delegate;
    }

}

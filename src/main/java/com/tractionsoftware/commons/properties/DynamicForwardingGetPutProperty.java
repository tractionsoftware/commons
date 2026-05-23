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

public class DynamicForwardingGetPutProperty extends ForwardingGetPutProperty {

    /**
     * Creates a {@link GetPutProperty} that defers to the GetProperty
     * provided by the given {@link Supplier}. This is perfect for
     * wrapping a dynamically provided object to ensure that only the
     * GetPutProperty functionality is exposed.
     *
     * @param provider
     *            the {@link Supplier} for the {@link GetPutProperty}
     *            to which the returned GetProperty will defer.
     * @return a {@link GetPutProperty} that defers to the
     *         GetPutProperty provided by the given {@link Supplier}.
     */
    public static final GetPutProperty wrap(Supplier<? extends GetPutProperty> provider) {
        return new DynamicForwardingGetPutProperty(provider);
    }

    protected final Supplier<? extends GetPutProperty> provider;

    public DynamicForwardingGetPutProperty(Supplier<? extends GetPutProperty> provider) {
        this.provider = provider;
    }

    @Nonnull
    @Override
    public String toString() {
        return "GetPutProperty dyn fwd {" + provider + " -> " + provider.get() + "}";
    }

    @Nonnull
    @Override
    protected final GetPutProperty delegate() {
        GetPutProperty delegate = provider.get();
        Objects.requireNonNull(delegate, "dynamic delegate GetPutProperty");
        return delegate;
    }

}

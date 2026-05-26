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

import java.util.Objects;
import java.util.function.Supplier;

import jakarta.annotation.Nonnull;

public class DynamicForwardingGetProperty extends ForwardingGetProperty {

    /**
     * Creates a {@link GetProperty} that defers to the GetProperty provided by the given {@link Supplier}. This is
     * perfect for wrapping an object to ensure that only the GetProperty functionality is exposed.
     *
     * @param provider
     *     the {@link Supplier} for the {@link GetProperty} to which the returned GetProperty will defer.
     * @return a {@link GetProperty} that defers to the GetProperty provided by the given {@link Supplier}.
     */
    public static final GetProperty wrap(Supplier<? extends GetProperty> provider) {
        return new DynamicForwardingGetProperty(provider);
    }

    protected final Supplier<? extends GetProperty> provider;

    public DynamicForwardingGetProperty(Supplier<? extends GetProperty> provider) {
        this.provider = provider;
    }

    @Nonnull
    @Override
    public String toString() {
        return "GetProperty dyn fwd {" + provider + " -> " + provider.get() + "}";
    }

    @Nonnull
    @Override
    protected final GetProperty delegate() {
        return Objects.requireNonNullElseGet(provider.get(), SimpleProperties::emptyGetProperty);
    }

}

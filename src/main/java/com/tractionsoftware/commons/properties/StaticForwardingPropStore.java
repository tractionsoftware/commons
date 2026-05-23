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

/**
 * @param <R>
 *     the type of object used to facilitate committing changes.
 * @author Dave Shepperton
 */
public class StaticForwardingPropStore<R> extends ForwardingPropStore<R> {

    /**
     * Creates a {@link PropStore} that defers to the given statically specified instance. This is perfect for wrapping
     * an object to ensure that only the PropStore functionality is exposed.
     *
     * @param props
     *     the {@link PropStore} to which the PropStore will defer.
     * @return a {@link PropStore} that defers to the given statically specified instance.
     */
    public static <R> PropStore<R> wrap(PropStore<R> props) {
        return new StaticForwardingPropStore<>(props);
    }

    protected final PropStore<R> props;

    public StaticForwardingPropStore(PropStore<R> props) {
        this.props = props;
    }

    @Nonnull
    @Override
    public String toString() {
        return "PropStore: stat fwd {" + props + "}";
    }

    @Nonnull
    @Override
    protected final PropStore<R> delegate() {
        return props;
    }

    @Override
    protected final boolean isStaticallySpecifiedDelegate() {
        return true;
    }

}

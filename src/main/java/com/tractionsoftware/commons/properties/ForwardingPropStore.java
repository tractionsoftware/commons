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
 * A base {@link PropStore} implementation for implementing the decorator pattern. The {@link #delegate()} method
 * returns the PropStore being decorated.
 *
 * <p>
 * Methods that offer basic PropStore functionality, such as {@link #getProperty(String)} and {@link #commitChanges(R)},
 * defer directly the corresponding method of the backing PropStore.
 *
 * <p>
 * Methods that return instances of PropStore or its super-interfaces are guaranteed to never directly expose either the
 * delegate or any object obtained directly from the delegate. They are also guaranteed not to retrieve and use the
 * delegate instance until one of the basic methods is invoked. Each of these guarantees imply that such methods cannot
 * be implemented via direct deference to the delegate. The resulting implementations fall into two categories:
 *
 * <ul>
 * <li>methods that involve some sort of decoration of a PropStore instance, such as {@link #withDefaults(GetProperty)}
 * and {@link #getNamespace(String)}. ForwardingPropStore does not override these at all, instead using the default
 * implementations from PropStore, which already use wrappers, and by definition do not have access to the delegate.
 * <li>methods that must ultimately defer to the delegate. There are exactly two such methods: {@link #getDefaults()}
 * and {@link #getLocals()}.
 * </ul>
 *
 * @author Dave Shepperton
 */
public abstract class ForwardingPropStore<R> extends ForwardingGetPutProperty implements PropStore<R> {

    /**
     * Returns the {@link PropStore} that should be used for the implementation of any PropStore methods.
     *
     * @return the {@link PropStore} that should be used for the implementation of any PropStore methods.
     */
    @Nonnull
    @Override
    protected abstract PropStore<R> delegate();

    @Nonnull
    @Override
    public String toString() {
        return "PropStore fwd {" + delegate() + "}";
    }

    @Override
    public CommitResult commitChanges(R rec) {
        return delegate().commitChanges(rec);
    }

}

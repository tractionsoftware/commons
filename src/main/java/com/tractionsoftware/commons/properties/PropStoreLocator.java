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

import org.jspecify.annotations.NonNull;

import java.util.Objects;


/**
 * <pre>
 *        ^
 *        | fall back defaults (read-only)
 *        |
 *    ---------
 *   |         |
 *   |         |
 *   |   loc   |----> store (get, put, commit)
 *   |         |
 *    ---------
 *        ^
 *        |
 * </pre>
 *
 * @param <R>
 *     the type of object used to facilitate committing changes.
 */
public final class PropStoreLocator<R> extends AbstractGetPutPropertyLocator<PropStore<R>> implements PropStore<R> {

    public static final <R> PropStore<R> getSinglePropStoreOrLocator(PropStore<R> locals, GetProperty defaults) {
        Objects.requireNonNull(locals, "main PropStore");
        if (defaults == null) {
            return locals;
        }
        return new PropStoreLocator<>(locals, defaults);
    }

    public PropStoreLocator(PropStore<R> locals, GetProperty defaults) {
        super(locals, defaults);
    }

    @Override
    public final @NonNull String toString() {
        return "PropStore: " + super.toString();
    }

    @Override
    public final CommitResult commitChanges(R rec) {
        return locals.commitChanges(rec);
    }

}

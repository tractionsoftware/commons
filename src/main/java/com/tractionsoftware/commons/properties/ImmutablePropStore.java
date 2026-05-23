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

import java.util.Collection;
import java.util.Map;

/**
 * A {@link StaticForwardingPropStore} that does not permit any write or commit operations to be performed on the
 * underlying {@link PropStore}. Specifically, all write methods such as {@link #putProperty(String, String)} throw
 * {@link UnsupportedOperationException}s; and {@link #commitChanges(R)} does nothing and returns a {@link CommitResult}
 * that carries a {@link ReadOnlyPropStoreException}.
 *
 * <p>
 * This class is intended to provide a convenient way to produce PropStore instances for
 * {@link PropStore#toImmutable()}.
 *
 * @author Dave Shepperton
 */
public final class ImmutablePropStore<R> extends StaticForwardingPropStore<R> {

    /**
     * Wraps the given {@link PropStore} in another PropStore which overrides all write methods, such as
     * {@link PropStore#putProperty(String, String)} and {@link PropStore#commitChanges(R)}, to throw an
     * {@link UnsupportedOperationException}. It exists to facilitate {@link PropStore#toImmutable()}.
     *
     * @param props
     *     the {@link PropStore} for which an immutable version is to be created.
     * @return a {@link PropStore} identical to the supplied PropStore except that all write methods throw
     *     {@link UnsupportedOperationException}s.
     */
    public static final <R> PropStore<R> wrap(PropStore<R> props) {
        return new ImmutablePropStore<>(props);
    }

    public ImmutablePropStore(PropStore<R> store) {
        super(store);
    }

    @Nonnull
    @Override
    public final String toString() {
        return "PropStore: immutable {" + delegate() + "}";
    }

    /**
     * Throws an {@link UnsupportedOperationException}.
     */
    @Override
    public final void putProperty(String name, String value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an {@link UnsupportedOperationException}.
     */
    @Override
    public final void putBooleanProperty(String name, boolean value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an {@link UnsupportedOperationException}.
     */
    @Override
    public final void putIntProperty(String name, int value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an {@link UnsupportedOperationException}.
     */
    @Override
    public final void putLongProperty(String name, long value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an {@link UnsupportedOperationException}.
     */
    @Override
    public final void putDoubleProperty(String name, double value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an {@link UnsupportedOperationException}.
     */
    @Override
    public final void removeProperty(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean clearLocalProperties() {
        return false;
    }

    /**
     * Throws an {@link UnsupportedOperationException}.
     */
    @Override
    public final void putAllProperties(GetProperty source) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an {@link UnsupportedOperationException}.
     */
    @Override
    public final void putProperties(GetProperty source, Collection<?> exceptions) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an {@link UnsupportedOperationException}.
     */
    @Override
    public final void copyFrom(Map<? super String,? super String> source) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an {@link UnsupportedOperationException}.
     */
    @Override
    public final void copyFrom(Map<? super String,? super String> source, Collection<?> exceptions) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an {@link UnsupportedOperationException}.
     */
    @Override
    public final void accept(String name, String value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an {@link UnsupportedOperationException}.
     */
    @Override
    public final void appendToListProperty(String name, String value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an {@link UnsupportedOperationException}.
     */
    @Override
    public final void appendToProperty(String name, String value, String separator) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a {@link CommitResult} that carries a {@link ReadOnlyPropStoreException}, since this operation is not
     * allowed.
     */
    @Override
    public final CommitResult commitChanges(R rec) {
        return CommitResults.forFailure(false, new ReadOnlyPropStoreException());
    }

    /**
     * Returns this ImmutablePropStore itself, since it is already immutable.
     */
    @Override
    public final PropStore<R> toImmutable() {
        return this;
    }

}

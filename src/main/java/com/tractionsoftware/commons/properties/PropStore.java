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

import com.tractionsoftware.commons.text.TextTransformer;

import java.util.function.Function;

/**
 * Represents a {@link GetPutProperty} that is backed by a mutable store. Changes requested via methods such as
 * {@link #putProperty(String, String)} can be committed via {@link #commitChanges(R)}.
 *
 * @param <R>
 *     the type of object used to facilitate committing changes.
 * @author Andy Keller, Dave Shepperton
 */
public interface PropStore<R> extends GetPutProperty {

    /**
     * Commits the effect of any requested changes to the backing store for this PropStore. If this method returns a
     * {@link CommitResult} representing {@link CommitResult#wasSuccessful() success}, clients may be sure that any
     * required changes have been persisted.
     *
     * <p>
     * Implementations should maintain the guarantee that a commit operation either completely succeeded or completely
     * failed. This is to ensure that underlying property collections are not left in an inconsistent, invalid, or
     * poorly defined state. If an implementation has a good reason to depart from this guarantee, it may do so, but
     * must clearly explain this departure in its documentation.
     *
     * <p>
     * Implementations should, on a best-efforts basis, try to return a {@link CommitResult} that whose
     * {@link CommitResult#didCommitChanges()} method returns true if there were no effective changes made to the
     * backing store. This may happen, e.g., if a concurrent request has already persisted equivalent changes before the
     * changes from this PropStore could be committed.
     *
     * @return a {@link CommitResult} encapsulating the result of the operation.
     */
    public CommitResult commitChanges(R rec);

    /**
     * Returns a PropStore instance which is identical to this one in every respect, except that it does not permit any
     * write or commit operations. More specifically, any methods that implement write operations, such as
     * {@link #putProperty(String, String)} throw an {@link UnsupportedOperationException}; and
     * {@link #commitChanges(R)} should return a failed {@link CommitResult}.
     *
     * <p>
     * This method should be used sparingly. It exists to accommodate hopefully a very few unusual cases in which
     * {@link #toReadOnly()} is in adequate.
     *
     * <p>
     * This default implementation uses {@link ImmutablePropStore#wrap(PropStore)}, which should be adequate for all
     * PropStore implementations. Only subclasses that have a very good reason to prohibit this sort of transformation
     * should override it.
     *
     * @return a PropStore instance which is identical to this one in every respect, except that it is immutable.
     */
    public default PropStore<R> toImmutable() {
        return ImmutablePropStore.wrap(this);
    }

    /**
     * This implementation uses {@link PropStoreLocator}, which should be adequate for most implementations. It also
     * gracefully handles a null argument by returning this PropStore instance itself.
     */
    @Override
    public default PropStore<R> withDefaults(GetProperty defaults) {
        return PropStoreLocator.getSinglePropStoreOrLocator(this, defaults);
    }

    /**
     * This default implementation uses {@link CachingPropStore#wrapWithDefaultCache(PropStore)}. This should be
     * adequate for most implementations.
     */
    @Override
    public default PropStore<R> withCache() {
        return CachingPropStore.wrapWithDefaultCache(this);
    }

    /**
     * This default implementation uses {@link CachingPropStore#wrapWithCache(PropStore, PropertyCache)}. This should be
     * adequate for most implementations.
     */
    @Override
    public default PropStore<R> withCache(PropertyCache cache) {
        if (cache == null) {
            return this;
        }
        return CachingPropStore.wrapWithCache(this, cache);
    }

    /**
     * This implementation uses {@link PropertyNameMappingPropStore#wrapInNamespace(PropStore, String)} , which should
     * be adequate for all PropStore implementations that do not need to return another specific sub-type of PropStore.
     */
    @Override
    public default PropStore<R> getNamespace(String space) {
        return PropertyNameMappingPropStore.wrapInNamespace(this, space);
    }

    /**
     * This implementation uses {@link PropertyNameMappingPropStore#wrapInNamespace(PropStore, String, char)} , which
     * should be adequate for all PropStore implementations that do not need to return another specific sub-type of
     * PropStore.
     */
    @Override
    public default PropStore<R> getNamespace(String space, char separator) {
        return PropertyNameMappingPropStore.wrapInNamespace(this, space, separator);
    }

    /**
     * This implementation uses {@link PropertyNameMappingPropStore#wrapInPrefix(PropStore, String)} , which should be
     * adequate for all PropStore implementations that do not need to return another specific sub-type of PropStore.
     */
    @Override
    public default PropStore<R> getPrefix(String prefix) {
        return PropertyNameMappingPropStore.wrapInPrefix(this, prefix);
    }

    /**
     * This implementation uses {@link PropertyNameMappingPropStore#wrapInPrefix(PropStore, String, char)} , which
     * should be adequate for all PropStore implementations that do not need to return another specific sub-type of
     * PropStore.
     */
    @Override
    public default PropStore<R> getPrefix(String prefix, char separator) {
        return PropertyNameMappingPropStore.wrapInPrefix(this, prefix, separator);
    }

    /**
     * This implementation uses {@link StaticForwardingGetPutProperty#wrap(GetPutProperty)}, passing this PropStore,
     * which should be sufficient in most cases.
     */
    @Override
    public default GetPutProperty toReadWrite() {
        return StaticForwardingGetPutProperty.wrap(this);
    }

    /**
     * This implementation uses
     * {@link PropertyValueMappingPropStore#applyPropertyValueTransformers(PropStore, Function, Function)} , which
     * should be sufficient in most cases.
     */
    @Override
    public default PropStore<R> transformingValues(Function<String,String> readValueMapper, Function<String,String> writeValueMapper) {
        return PropertyValueMappingPropStore.applyPropertyValueTransformers(this, readValueMapper, writeValueMapper);
    }

    /**
     * This implementation uses
     * {@link PropertyValueMappingPropStore#wrapWithValueTransformers(PropStore, TextTransformer, TextTransformer)} ,
     * which should be sufficient in most cases.
     */
    @Override
    public default PropStore<R> transformingValues(TextTransformer readValueTransformer, TextTransformer writeValueTransformer) {
        return PropertyValueMappingPropStore.wrapWithValueTransformers(
            this, readValueTransformer, writeValueTransformer
        );
    }

}

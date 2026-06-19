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

package com.tractionsoftware.commons.util.function;

import com.google.common.collect.Iterables;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Some helper methods to simplify some handling of objects related to the {@link Stream} API.
 *
 * @author Dave Shepperton
 */
public final class StreamUtil {

    private StreamUtil() {
    }

    @Nonnull
    public static final <T> Stream<T> stream(@Nullable Iterable<T> iterable) {
        if (iterable == null || Iterables.isEmpty(iterable)) {
            return Stream.empty();
        }
        if (iterable instanceof Collection<T> coll) {
            return coll.stream();
        }
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iterable.iterator(), Spliterator.ORDERED), false
        );
    }

    @Nonnull
    public static final <T> Stream<T> streamOrElseEmpty(@Nullable Stream<T> stream) {
        return Objects.requireNonNullElseGet(stream, Stream::empty);
    }

}

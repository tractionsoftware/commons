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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The simplest possible implementation of GetPropertyAt backed by a list with the getPropertyAt method deferring to
 * {@link List#get(int)}.
 *
 * @author Dave Shepperton
 */
public final class SimplestGetPropertyAt<T> implements GetPropertyAt {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimplestGetPropertyAt.class);

    public static final <T> SimplestGetPropertyAt<T> getInstance(T[] arr) {
        List<T> list;
        if (arr == null) {
            list = Collections.emptyList();
        }
        else {
            list = Arrays.asList(arr);
        }
        return new SimplestGetPropertyAt<>(list);
    }

    public static final <T> SimplestGetPropertyAt<T> getInstance(List<T> list) {
        if (list == null) {
            list = Collections.emptyList();
        }
        return new SimplestGetPropertyAt<>(list);
    }

    /**
     * The backing list. The factory constructor methods ensure that this will never be null.
     */
    private final List<T> list;

    private SimplestGetPropertyAt(List<T> list) {
        this.list = list;
    }

    /**
     * Defers to {@link List#get(int)} for the underlying list. If an IndexOutOfBoundsException is raised, null is
     * returned instead.
     *
     * @return the element from the backing list at the given index, or null if the given index is not valid for the
     *     list.
     */
    @Override
    public final Object getPropertyAt(int i) {
        try {
            return list.get(i);
        }
        catch (IndexOutOfBoundsException e) {
            LOGGER.warn("Failed to get property at {}", i, e);
        }
        return null;
    }

    @Override
    public final boolean contains(Object object) {
        return list.contains(object);
    }

    @Override
    public final boolean hasPropertyAt(int i) {
        if (i >= 0 && i < list.size()) {
            return true;
        }
        return false;
    }

    @Override
    public final Set<Integer> getPropertyNumbers() {
        return IntStream.range(0, list.size()).boxed().collect(Collectors.toSet());
    }

    @Override
    public final int size() {
        return list.size();
    }

    @Override
    public final boolean isEmpty() {
        return list.isEmpty();
    }

}

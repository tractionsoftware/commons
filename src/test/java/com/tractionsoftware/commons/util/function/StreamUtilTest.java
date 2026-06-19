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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public final class StreamUtilTest {

    // =====================================================================
    // stream(Iterable)
    // =====================================================================

    @Test
    void stream_null_returnsEmpty() {
        assertEquals(0, StreamUtil.stream(null).count());
    }

    @Test
    void stream_emptyList_returnsEmpty() {
        assertEquals(0, StreamUtil.stream(List.of()).count());
    }

    @Test
    void stream_nonEmptyList_returnsElements() {
        List<String> list = List.of("a", "b", "c");
        assertEquals(3, StreamUtil.stream(list).count());
    }

    @Test
    void stream_collection_usesCollectionStream() {
        List<Integer> list = List.of(1, 2, 3);
        List<Integer> result = StreamUtil.stream(list).toList();
        assertEquals(list, result);
    }

    @Test
    void stream_nonCollectionIterable_works() {
        // An Iterable that is not a Collection
        Iterable<String> iterable = () -> List.of("x", "y").iterator();
        List<String> result = StreamUtil.stream(iterable).toList();
        assertEquals(List.of("x", "y"), result);
    }

    @Test
    void stream_preservesOrder() {
        List<String> list = List.of("first", "second", "third");
        List<String> result = StreamUtil.stream(list).toList();
        assertEquals(list, result);
    }

    // =====================================================================
    // streamOrElseEmpty(Stream)
    // =====================================================================

    @Test
    void streamOrElseEmpty_null_returnsEmpty() {
        assertEquals(0, StreamUtil.streamOrElseEmpty(null).count());
    }

    @Test
    void streamOrElseEmpty_nonNull_returnsSameStream() {
        Stream<String> s = Stream.of("a", "b");
        List<String> result = StreamUtil.streamOrElseEmpty(s).toList();
        assertEquals(List.of("a", "b"), result);
    }

    @Test
    void streamOrElseEmpty_emptyStream_returnsEmpty() {
        assertEquals(0, StreamUtil.streamOrElseEmpty(Stream.empty()).count());
    }

}

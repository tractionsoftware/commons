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
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public final class ConsumersUtilTest {

    @Test
    void combine_bothNonNull_callsBoth() {
        List<String> log = new ArrayList<>();
        Consumer<String> a = s -> log.add("a:" + s);
        Consumer<String> b = s -> log.add("b:" + s);
        Consumer<String> combined = ConsumersUtil.combine(a, b);
        combined.accept("x");
        assertEquals(List.of("a:x", "b:x"), log);
    }

    @Test
    void combine_firstNull_returnsSecond() {
        Consumer<String> b = s -> {};
        assertSame(b, ConsumersUtil.combine(null, b));
    }

    @Test
    void combine_secondNull_returnsFirst() {
        Consumer<String> a = s -> {};
        assertSame(a, ConsumersUtil.combine(a, null));
    }

    @Test
    void combine_bothNull_returnsNull() {
        assertNull(ConsumersUtil.combine(null, null));
    }

    @Test
    void combine_callOrder_firstThenSecond() {
        List<Integer> order = new ArrayList<>();
        Consumer<String> first = s -> order.add(1);
        Consumer<String> second = s -> order.add(2);
        ConsumersUtil.combine(first, second).accept("x");
        assertEquals(List.of(1, 2), order);
    }

    @Test
    void combine_secondThrows_propagatesException() {
        Consumer<String> first = s -> {};
        Consumer<String> second = s -> { throw new RuntimeException("oops"); };
        Consumer<String> combined = ConsumersUtil.combine(first, second);
        assertThrows(RuntimeException.class, () -> combined.accept("x"));
    }

}

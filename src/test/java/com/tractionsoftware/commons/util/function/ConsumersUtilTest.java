// PLEASE DO NOT DELETE THIS LINE - make copyright depends on it.
package com.tractionsoftware.commons.util.function;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class ConsumersUtilTest {

    @Test
    void combine_bothNull_returnsNull() {
        assertNull(ConsumersUtil.combine(null, null));
    }

    @Test
    void combine_firstNull_returnsSecond() {
        Consumer<String> second = s -> {};
        assertSame(second, ConsumersUtil.combine(null, second));
    }

    @Test
    void combine_secondNull_returnsFirst() {
        Consumer<String> first = s -> {};
        assertSame(first, ConsumersUtil.combine(first, null));
    }

    @Test
    void combine_bothNonNull_runsFirstThenSecond() {
        List<String> calls = new ArrayList<>();
        Consumer<String> first = s -> calls.add("first:" + s);
        Consumer<String> second = s -> calls.add("second:" + s);

        Consumer<String> combined = ConsumersUtil.combine(first, second);
        assertNotNull(combined);
        combined.accept("x");

        assertEquals(List.of("first:x", "second:x"), calls);
    }

    @Test
    void combine_bothNonNull_bothReceiveValue() {
        List<Integer> seen = new ArrayList<>();
        Consumer<Integer> c1 = seen::add;
        Consumer<Integer> c2 = seen::add;

        ConsumersUtil.combine(c1, c2).accept(42);
        assertEquals(List.of(42, 42), seen);
    }

}

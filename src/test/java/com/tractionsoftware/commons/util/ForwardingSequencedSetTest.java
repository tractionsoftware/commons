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

package com.tractionsoftware.commons.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.SequencedSet;

import static org.junit.jupiter.api.Assertions.*;

public final class ForwardingSequencedSetTest {

    private static final class TestSet<E> extends ForwardingSequencedSet<E> {
        private final LinkedHashSet<E> delegate = new LinkedHashSet<>();
        @Override
        protected SequencedSet<E> delegate() { return delegate; }
    }

    private TestSet<String> set;

    @BeforeEach
    void setUp() {
        set = new TestSet<>();
        set.add("a");
        set.add("b");
        set.add("c");
    }

    @Test
    void getFirst_returnsFirstInserted() {
        assertEquals("a", set.getFirst());
    }

    @Test
    void getLast_returnsLastInserted() {
        assertEquals("c", set.getLast());
    }

    @Test
    void addFirst_prependsElement() {
        set.addFirst("z");
        assertEquals("z", set.getFirst());
        assertEquals(4, set.size());
    }

    @Test
    void addLast_appendsElement() {
        set.addLast("z");
        assertEquals("z", set.getLast());
        assertEquals(4, set.size());
    }

    @Test
    void removeFirst_removesFirst() {
        String removed = set.removeFirst();
        assertEquals("a", removed);
        assertEquals("b", set.getFirst());
    }

    @Test
    void removeLast_removesLast() {
        String removed = set.removeLast();
        assertEquals("c", removed);
        assertEquals("b", set.getLast());
    }

    @Test
    void reversed_hasReverseOrder() {
        SequencedSet<String> rev = set.reversed();
        assertNotNull(rev);
        assertEquals("c", rev.getFirst());
        assertEquals("a", rev.getLast());
    }

    @Test
    void spliterator_notNull() {
        assertNotNull(set.spliterator());
    }

    @Test
    void noDuplicates_setSemantics() {
        set.add("a"); // duplicate
        assertEquals(3, set.size());
    }

    @Test
    void contains_correct() {
        assertTrue(set.contains("b"));
        assertFalse(set.contains("z"));
    }

}

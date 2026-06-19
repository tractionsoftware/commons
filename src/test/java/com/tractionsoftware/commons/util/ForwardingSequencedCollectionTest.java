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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.SequencedCollection;

import static org.junit.jupiter.api.Assertions.*;

public final class ForwardingSequencedCollectionTest {

    /** Minimal concrete subclass using a LinkedList delegate. */
    private static final class TestCollection<E> extends ForwardingSequencedCollection<E> {
        private final LinkedList<E> delegate = new LinkedList<>();
        @Override
        protected SequencedCollection<E> delegate() { return delegate; }
    }

    private TestCollection<String> coll;

    @BeforeEach
    void setUp() {
        coll = new TestCollection<>();
        coll.add("a");
        coll.add("b");
        coll.add("c");
    }

    @Test
    void getFirst_returnsHead() {
        assertEquals("a", coll.getFirst());
    }

    @Test
    void getLast_returnsTail() {
        assertEquals("c", coll.getLast());
    }

    @Test
    void addFirst_prependsElement() {
        coll.addFirst("z");
        assertEquals("z", coll.getFirst());
        assertEquals(4, coll.size());
    }

    @Test
    void addLast_appendsElement() {
        coll.addLast("z");
        assertEquals("z", coll.getLast());
    }

    @Test
    void removeFirst_removesHead() {
        String removed = coll.removeFirst();
        assertEquals("a", removed);
        assertEquals(2, coll.size());
        assertEquals("b", coll.getFirst());
    }

    @Test
    void removeLast_removesTail() {
        String removed = coll.removeLast();
        assertEquals("c", removed);
        assertEquals(2, coll.size());
        assertEquals("b", coll.getLast());
    }

    @Test
    void reversed_reverseOrder() {
        SequencedCollection<String> rev = coll.reversed();
        assertNotNull(rev);
        assertEquals("c", rev.getFirst());
        assertEquals("a", rev.getLast());
    }

    @Test
    void size_reflects_additions() {
        assertEquals(3, coll.size());
        coll.add("d");
        assertEquals(4, coll.size());
    }

    @Test
    void contains_existingElement() {
        assertTrue(coll.contains("b"));
        assertFalse(coll.contains("z"));
    }

}

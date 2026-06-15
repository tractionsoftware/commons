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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public final class MultiPropertyNameMapperTest {

    // inner: namespace "foo" → "foo_X"
    // outer: prefix "bar"  → "barX" (no separator)
    // combined: getPropertyName("foo_X") first applied by outer then inner
    //   outer.getPropertyName("foo_X") -- for a namespace mapper the inverse strips "foo_"
    //   then inner.getPropertyName(result)
    // To keep it simple, use two namespace mappers:
    // inner: "a" namespace  → "a_X"
    // outer: "b" namespace  → "b_X"
    // combined.getPropertyName("b_a_something") → strips "b_" → "a_something" → strips "a_" → "something"

    private final PropertyNameMapper mapperA = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("a");
    private final PropertyNameMapper mapperB = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("b");
    private final MultiPropertyNameMapper composed = MultiPropertyNameMapper.createInstance(mapperA, mapperB);

    @Test
    void getActualPropertyName_appliesGetActualPropertyNameInsideOut() {
        assertEquals("a_b_x", composed.getActualPropertyName("x"));
    }

    @Test
    void getPublishedName_appliesGetPublishedNameOutsideIn() {
        assertEquals("x", composed.getPublishedName("a_b_x"));
    }

    @Test
    void equals_sameMappers_true() {
        var other = MultiPropertyNameMapper.createInstance(mapperA, mapperB);
        assertEquals(composed, other);
    }

    @Test
    void equals_differentMappers_false() {
        var nsC = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("c");
        var other = MultiPropertyNameMapper.createInstance(mapperA, nsC);
        assertNotEquals(composed, other);
    }

    @Test
    void hashCode_sameMappers_equal() {
        var other = MultiPropertyNameMapper.createInstance(mapperA, mapperB);
        assertEquals(composed.hashCode(), other.hashCode());
    }

    @Test
    void toString_containsCombined() {
        String s = composed.toString();
        assertNotNull(s);
        assertTrue(s.contains("combined"), s);
    }

    @Test
    void isInverseOf_swappedMapper_false() {
        // Namespace mappers are not self-inverses (remove≠add), so swapping
        // inner↔outer does not satisfy the isInverseOf contract.
        var swapped = MultiPropertyNameMapper.createInstance(mapperB, mapperA);
        assertFalse(composed.isInverseOf(swapped));
    }

    @Test
    void isInverseOf_nonCombinedMapper_false() {
        assertFalse(composed.isInverseOf(mapperA));
    }

    @Test
    void nullInner_throwsNPE() {
        assertThrows(NullPointerException.class, () -> MultiPropertyNameMapper.createInstance(null, mapperB));
    }

    @Test
    void nullOuter_throwsNPE() {
        assertThrows(NullPointerException.class, () -> MultiPropertyNameMapper.createInstance(mapperA, null));
    }

}

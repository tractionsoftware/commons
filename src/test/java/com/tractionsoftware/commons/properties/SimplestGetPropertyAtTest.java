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

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SimplestGetPropertyAtTest {

    @Test
    void getInstance_fromArray_getPropertyAtReturnsElement() {
        SimplestGetPropertyAt<String> propAt = SimplestGetPropertyAt.getInstance(new String[] {"a", "b", "c"});
        assertEquals("b", propAt.getPropertyAt(1));
    }

    @Test
    void getInstance_fromNullArray_isEmpty() {
        SimplestGetPropertyAt<String> propAt = SimplestGetPropertyAt.getInstance((String[]) null);
        assertTrue(propAt.isEmpty());
    }

    @Test
    void getInstance_fromList_getPropertyAtReturnsElement() {
        SimplestGetPropertyAt<String> propAt = SimplestGetPropertyAt.getInstance(List.of("x", "y"));
        assertEquals("x", propAt.getPropertyAt(0));
    }

    @Test
    void getInstance_fromNullList_isEmpty() {
        SimplestGetPropertyAt<String> propAt = SimplestGetPropertyAt.getInstance((List<String>) null);
        assertTrue(propAt.isEmpty());
    }

    @Test
    void getPropertyAt_outOfBoundsIndex_returnsNullInsteadOfThrowing() {
        SimplestGetPropertyAt<String> propAt = SimplestGetPropertyAt.getInstance(List.of("a"));
        assertNull(propAt.getPropertyAt(5));
    }

    @Test
    void contains_existingValue_returnsTrue() {
        SimplestGetPropertyAt<String> propAt = SimplestGetPropertyAt.getInstance(List.of("a", "b"));
        assertTrue(propAt.contains("b"));
    }

    @Test
    void contains_missingValue_returnsFalse() {
        SimplestGetPropertyAt<String> propAt = SimplestGetPropertyAt.getInstance(List.of("a", "b"));
        assertFalse(propAt.contains("z"));
    }

    @Test
    void hasPropertyAt_validIndex_returnsTrue() {
        SimplestGetPropertyAt<String> propAt = SimplestGetPropertyAt.getInstance(List.of("a", "b"));
        assertTrue(propAt.hasPropertyAt(1));
    }

    @Test
    void hasPropertyAt_negativeIndex_returnsFalse() {
        SimplestGetPropertyAt<String> propAt = SimplestGetPropertyAt.getInstance(List.of("a"));
        assertFalse(propAt.hasPropertyAt(-1));
    }

    @Test
    void hasPropertyAt_indexAtOrBeyondSize_returnsFalse() {
        SimplestGetPropertyAt<String> propAt = SimplestGetPropertyAt.getInstance(List.of("a"));
        assertFalse(propAt.hasPropertyAt(1));
    }

    @Test
    void getPropertyNumbers_returnsRangeOfIndices() {
        SimplestGetPropertyAt<String> propAt = SimplestGetPropertyAt.getInstance(List.of("a", "b", "c"));
        assertEquals(Set.of(0, 1, 2), propAt.getPropertyNumbers());
    }

    @Test
    void size_returnsListSize() {
        SimplestGetPropertyAt<String> propAt = SimplestGetPropertyAt.getInstance(List.of("a", "b"));
        assertEquals(2, propAt.size());
    }

    @Test
    void isEmpty_nonEmptyList_returnsFalse() {
        SimplestGetPropertyAt<String> propAt = SimplestGetPropertyAt.getInstance(List.of("a"));
        assertFalse(propAt.isEmpty());
    }

}

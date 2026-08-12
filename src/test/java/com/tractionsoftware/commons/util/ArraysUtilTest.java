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

import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ArraysUtil}.
 */
class ArraysUtilTest {

    // -------------------------------------------------------------------------
    // toLinkedHashSet
    // -------------------------------------------------------------------------

    @Test
    void toLinkedHashSet_null_returnsEmptySet() {
        LinkedHashSet<String> result = ArraysUtil.toLinkedHashSet(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toLinkedHashSet_nonNull_containsAllElements() {
        LinkedHashSet<String> result = ArraysUtil.toLinkedHashSet(new String[]{"a", "b", "c"});
        assertEquals(3, result.size());
        assertTrue(result.contains("a"));
        assertTrue(result.contains("b"));
        assertTrue(result.contains("c"));
    }

    @Test
    void toLinkedHashSet_duplicates_deduplicates() {
        LinkedHashSet<String> result = ArraysUtil.toLinkedHashSet(new String[]{"x", "x", "y"});
        assertEquals(2, result.size());
    }

    // -------------------------------------------------------------------------
    // asList
    // -------------------------------------------------------------------------

    @Test
    void asList_null_returnsEmptyList() {
        List<String> result = ArraysUtil.asList(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void asList_nonNull_returnsListWithElements() {
        List<String> result = ArraysUtil.asList(new String[]{"x", "y"});
        assertEquals(List.of("x", "y"), result);
    }

    // -------------------------------------------------------------------------
    // safeToString
    // -------------------------------------------------------------------------

    @Test
    void safeToString_null_returnsString() {
        // asList(null) returns empty list; safeToString should not throw
        String result = ArraysUtil.safeToString(null);
        assertNotNull(result);
    }

    @Test
    void safeToString_nonNull_containsElements() {
        String result = ArraysUtil.safeToString(new String[]{"hello", "world"});
        assertNotNull(result);
        assertTrue(result.contains("hello"), "expected 'hello' in: " + result);
        assertTrue(result.contains("world"), "expected 'world' in: " + result);
    }

}

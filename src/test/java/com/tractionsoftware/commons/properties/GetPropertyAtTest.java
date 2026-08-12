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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link GetPropertyAt}'s default methods ({@code contains}, {@code hasPropertyAt}, {@code asGetProperty},
 * {@code size}, {@code isEmpty}) via a minimal test double that implements only the two abstract methods. This is
 * deliberately distinct from {@link SimplestGetPropertyAt}, which overrides all of these methods itself and therefore
 * would not exercise the interface's default implementations.
 */
class GetPropertyAtTest {

    private static final class MinimalGetPropertyAt implements GetPropertyAt {

        private final List<Object> values;

        MinimalGetPropertyAt(List<Object> values) {
            this.values = values;
        }

        @Override
        public Object getPropertyAt(int i) {
            if (i < 0 || i >= values.size()) {
                return null;
            }
            return values.get(i);
        }

        @Override
        public Set<Integer> getPropertyNumbers() {
            return IntStream.range(0, values.size()).boxed().collect(Collectors.toCollection(LinkedHashSet::new));
        }

    }

    @Test
    void contains_existingValue_returnsTrue() {
        GetPropertyAt propAt = new MinimalGetPropertyAt(List.of("a", "b", "c"));
        assertTrue(propAt.contains("b"));
    }

    @Test
    void contains_missingValue_returnsFalse() {
        GetPropertyAt propAt = new MinimalGetPropertyAt(List.of("a", "b"));
        assertFalse(propAt.contains("z"));
    }

    @Test
    void hasPropertyAt_validIndexWithValue_returnsTrue() {
        GetPropertyAt propAt = new MinimalGetPropertyAt(List.of("a"));
        assertTrue(propAt.hasPropertyAt(0));
    }

    @Test
    void hasPropertyAt_invalidIndex_returnsFalse() {
        GetPropertyAt propAt = new MinimalGetPropertyAt(List.of("a"));
        assertFalse(propAt.hasPropertyAt(5));
    }

    @Test
    void size_returnsPropertyNumbersCount() {
        GetPropertyAt propAt = new MinimalGetPropertyAt(List.of("a", "b", "c"));
        assertEquals(3, propAt.size());
    }

    @Test
    void isEmpty_emptyPropertyNumbers_returnsTrue() {
        GetPropertyAt propAt = new MinimalGetPropertyAt(List.of());
        assertTrue(propAt.isEmpty());
    }

    @Test
    void isEmpty_nonEmptyPropertyNumbers_returnsFalse() {
        GetPropertyAt propAt = new MinimalGetPropertyAt(List.of("a"));
        assertFalse(propAt.isEmpty());
    }

    @Test
    void asGetProperty_delegatesToPropertyAdaptersUsingIndexAsName() {
        GetPropertyAt propAt = new MinimalGetPropertyAt(List.of("a", "b"));
        GetProperty asProperty = propAt.asGetProperty();
        assertEquals("a", asProperty.getProperty("0"));
        assertEquals("b", asProperty.getProperty("1"));
    }

}

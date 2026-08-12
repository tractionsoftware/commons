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

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public final class ComparatorsUtilTest {

    // =====================================================================
    // STRING_CASE_INSENSITIVE_ORDER_WITH_NULLS_FIRST
    // =====================================================================

    @Test
    void stringCaseInsensitiveWithNullsFirst_nullBeforeNonNull() {
        assertTrue(ComparatorsUtil.STRING_CASE_INSENSITIVE_ORDER_WITH_NULLS_FIRST.compare(null, "a") < 0);
    }

    @Test
    void stringCaseInsensitiveWithNullsFirst_nonNullBeforeNull() {
        assertTrue(ComparatorsUtil.STRING_CASE_INSENSITIVE_ORDER_WITH_NULLS_FIRST.compare("a", null) > 0);
    }

    @Test
    void stringCaseInsensitiveWithNullsFirst_caseInsensitive() {
        assertEquals(0, ComparatorsUtil.STRING_CASE_INSENSITIVE_ORDER_WITH_NULLS_FIRST.compare("ABC", "abc"));
    }

    @Test
    void stringCaseInsensitiveWithNullsFirst_bothNull_zero() {
        assertEquals(0, ComparatorsUtil.STRING_CASE_INSENSITIVE_ORDER_WITH_NULLS_FIRST.compare(null, null));
    }

    // =====================================================================
    // createCompositeComparator
    // =====================================================================

    @Test
    void createCompositeComparator_null_returnsNull() {
        assertNull(ComparatorsUtil.createCompositeComparator(null));
    }

    @Test
    void createCompositeComparator_empty_returnsNull() {
        assertNull(ComparatorsUtil.createCompositeComparator(List.of()));
    }

    @Test
    void createCompositeComparator_singleComparator_works() {
        Comparator<String> comp = ComparatorsUtil.createCompositeComparator(
            List.of(String.CASE_INSENSITIVE_ORDER)
        );
        assertNotNull(comp);
        assertEquals(0, comp.compare("A", "a"));
    }

    @Test
    void createCompositeComparator_firstTieBreaker_usesSecond() {
        Comparator<String> byLength = Comparator.comparingInt(String::length);
        Comparator<String> natural = Comparator.naturalOrder();
        Comparator<String> comp = ComparatorsUtil.createCompositeComparator(List.of(byLength, natural));
        assertNotNull(comp);
        // "ab" vs "ba" – same length → falls to natural order
        assertTrue(comp.compare("ab", "ba") < 0);
    }

    @Test
    void createCompositeComparator_firstDiffers_shortCircuits() {
        Comparator<String> byLength = Comparator.comparingInt(String::length);
        Comparator<String> natural = Comparator.naturalOrder();
        Comparator<String> comp = ComparatorsUtil.createCompositeComparator(List.of(byLength, natural));
        assertNotNull(comp);
        // "a" (len 1) vs "bb" (len 2) – first comparator decides
        assertTrue(comp.compare("a", "bb") < 0);
    }

    @Test
    void createCompositeComparator_equals_byComponents() {
        Comparator<String> c1 = ComparatorsUtil.createCompositeComparator(List.of(String.CASE_INSENSITIVE_ORDER));
        Comparator<String> c2 = ComparatorsUtil.createCompositeComparator(List.of(String.CASE_INSENSITIVE_ORDER));
        assertEquals(c1, c2);
    }

    @Test
    void createCompositeComparator_toString_notNull() {
        Comparator<String> comp = ComparatorsUtil.createCompositeComparator(List.of(Comparator.naturalOrder()));
        assertNotNull(comp);
        assertNotNull(comp.toString());
    }

    // =====================================================================
    // createCompositeCaseInsensitiveStringComparator
    // =====================================================================

    @Test
    void createCompositeCaseInsensitive_null_returnsNull() {
        assertNull(ComparatorsUtil.createCompositeCaseInsensitiveStringComparator(null));
    }

    @Test
    void createCompositeCaseInsensitive_byField_sortsInsensitively() {
        record Person(String name) {}
        Comparator<Person> comp = ComparatorsUtil.createCompositeCaseInsensitiveStringComparator(
            List.of(Person::name)
        );
        assertNotNull(comp);
        assertEquals(0, comp.compare(new Person("Alice"), new Person("alice")));
    }

    // =====================================================================
    // safeComparator
    // =====================================================================

    @Test
    void safeComparator_null_returnsNull() {
        assertNull(ComparatorsUtil.safeComparator(null));
    }

    @Test
    void safeComparator_normalComparison_delegates() {
        Comparator<String> safe = ComparatorsUtil.safeComparator(Comparator.<String>naturalOrder());
        assertTrue(safe.compare("a", "b") < 0);
    }

    @Test
    void safeComparator_throwingComparator_returnsZero() {
        Comparator<String> throwing = (_, _) -> { throw new RuntimeException("boom"); };
        Comparator<String> safe = ComparatorsUtil.safeComparator(throwing);
        assertEquals(0, safe.compare("x", "y"));
    }

    @Test
    void safeComparator_alreadySafe_returnsSame() {
        Comparator<String> safe = ComparatorsUtil.safeComparator(Comparator.<String>naturalOrder());
        assertSame(safe, ComparatorsUtil.safeComparator(safe));
    }

    @Test
    void safeComparator_toString_notNull() {
        Comparator<String> safe = ComparatorsUtil.safeComparator(Comparator.<String>naturalOrder());
        assertNotNull(safe.toString());
    }

    @Test
    void safeComparator_equals_byInner() {
        Comparator<String> inner = Comparator.naturalOrder();
        Comparator<String> s1 = ComparatorsUtil.safeComparator(inner);
        Comparator<String> s2 = ComparatorsUtil.safeComparator(inner);
        assertEquals(s1, s2);
    }

    // =====================================================================
    // scoringComparator
    // =====================================================================

    @Test
    void scoringComparator_highScoreFirst() {
        Comparator<String> comp = ComparatorsUtil.scoringComparator(String::length);
        // "longer" (6) > "a" (1) → should sort "longer" before "a"
        assertTrue(comp.compare("longer", "a") < 0);
    }

    @Test
    void scoringComparator_equalScore_zero() {
        Comparator<String> comp = ComparatorsUtil.scoringComparator(_ -> 1);
        assertEquals(0, comp.compare("x", "y"));
    }

    @Test
    void scoringComparator_nullLast() {
        Comparator<String> comp = ComparatorsUtil.scoringComparator(String::length);
        assertTrue(comp.compare(null, "a") > 0);
    }

}

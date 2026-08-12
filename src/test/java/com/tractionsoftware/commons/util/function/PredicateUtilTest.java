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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class PredicateUtilTest {

    // ---------------------------------------------------------------------------
    // cacheSupporting / alwaysTrue / alwaysFalse
    // ---------------------------------------------------------------------------

    @Test
    void cacheSupporting_delegatesTestToWrappedPredicate() {
        CacheSupportingPredicate<String> p = PredicateUtil.cacheSupporting(s -> s.startsWith("a"));
        assertTrue(p.test("abc"));
        assertFalse(p.test("xyz"));
    }

    @Test
    void cacheSupporting_resultsAreCacheable_alwaysTrue() {
        CacheSupportingPredicate<String> p = PredicateUtil.cacheSupporting(s -> true);
        assertTrue(p.resultsAreCacheable());
    }

    @Test
    void alwaysTrue_testsTrueForAnyValue() {
        CacheSupportingPredicate<Object> p = PredicateUtil.alwaysTrue();
        assertTrue(p.test(null));
        assertTrue(p.test("anything"));
        assertTrue(p.resultsAreCacheable());
    }

    @Test
    void alwaysFalse_testsFalseForAnyValue() {
        CacheSupportingPredicate<Object> p = PredicateUtil.alwaysFalse();
        assertFalse(p.test(null));
        assertFalse(p.test("anything"));
        assertTrue(p.resultsAreCacheable());
    }

    // ---------------------------------------------------------------------------
    // onlyThese / onlyOtherThanThese
    // ---------------------------------------------------------------------------

    @Test
    void onlyThese_null_alwaysFalse() {
        Predicate<Object> p = PredicateUtil.onlyThese(null);
        assertFalse(p.test("a"));
        assertFalse(p.test(null));
    }

    @Test
    void onlyThese_collection_matchesOnlyContainedElements() {
        Predicate<Object> p = PredicateUtil.onlyThese(List.of("a", "b"));
        assertTrue(p.test("a"));
        assertTrue(p.test("b"));
        assertFalse(p.test("c"));
    }

    @Test
    void onlyOtherThanThese_null_alwaysTrue() {
        Predicate<Object> p = PredicateUtil.onlyOtherThanThese(null);
        assertTrue(p.test("a"));
        assertTrue(p.test(null));
    }

    @Test
    void onlyOtherThanThese_collection_matchesElementsNotContained() {
        Predicate<Object> p = PredicateUtil.onlyOtherThanThese(List.of("a", "b"));
        assertFalse(p.test("a"));
        assertFalse(p.test("b"));
        assertTrue(p.test("c"));
    }

    // ---------------------------------------------------------------------------
    // onlyThese / onlyOtherThanThese - equals() / hashCode()
    // ---------------------------------------------------------------------------

    @Test
    void onlyThese_equals_sameElementsAndMode_areEqual() {
        Predicate<Object> p1 = PredicateUtil.onlyThese(List.of("a", "b"));
        Predicate<Object> p2 = PredicateUtil.onlyThese(List.of("a", "b"));
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void onlyThese_equals_differentElements_areNotEqual() {
        Predicate<Object> p1 = PredicateUtil.onlyThese(List.of("a", "b"));
        Predicate<Object> p2 = PredicateUtil.onlyThese(List.of("a", "c"));
        assertNotEquals(p1, p2);
    }

    @Test
    void onlyThese_equals_differentMode_areNotEqual() {
        Predicate<Object> onlyThese = PredicateUtil.onlyThese(List.of("a", "b"));
        Predicate<Object> otherThanThese = PredicateUtil.onlyOtherThanThese(List.of("a", "b"));
        assertNotEquals(onlyThese, otherThanThese);
    }

    @Test
    void onlyThese_equals_unrelatedObject_returnsFalse() {
        Predicate<Object> p = PredicateUtil.onlyThese(List.of("a"));
        assertNotEquals(p, "not a predicate");
        assertFalse(p.equals(42));
    }

    // ---------------------------------------------------------------------------
    // getFirstOrMultiFilter
    // ---------------------------------------------------------------------------

    @Test
    void getFirstOrMultiFilter_null_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> PredicateUtil.getFirstOrMultiFilter(null));
    }

    @Test
    void getFirstOrMultiFilter_empty_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> PredicateUtil.getFirstOrMultiFilter(List.of()));
    }

    @Test
    void getFirstOrMultiFilter_anyMatch_returnsTrue() {
        Predicate<String> filter = PredicateUtil.getFirstOrMultiFilter(
            List.of(s -> s.startsWith("a"), s -> s.startsWith("b")));
        assertTrue(filter.test("apple"));
        assertTrue(filter.test("banana"));
        assertFalse(filter.test("cherry"));
    }

    @Test
    void getFirstOrMultiFilter_shortCircuits_onFirstMatch() {
        AtomicInteger secondCallCount = new AtomicInteger();
        Predicate<String> alwaysTrue = s -> true;
        Predicate<String> countingFalse = s -> {
            secondCallCount.incrementAndGet();
            return false;
        };
        Predicate<String> filter = PredicateUtil.getFirstOrMultiFilter(List.of(alwaysTrue, countingFalse));
        assertTrue(filter.test("x"));
        assertEquals(0, secondCallCount.get(), "second predicate should not be evaluated once the first matches");
    }

    // ---------------------------------------------------------------------------
    // and(Predicate, Predicate) - 2-arg overload
    // ---------------------------------------------------------------------------

    @Test
    void and_bothNull_returnsAlwaysTrue() {
        Predicate<? super String> p = PredicateUtil.and(null, null);
        assertTrue(p.test("anything"));
    }

    @Test
    void and_firstNull_returnsSecond() {
        Predicate<String> second = s -> s.equals("b");
        Predicate<? super String> p = PredicateUtil.and(null, second);
        assertTrue(p.test("b"));
        assertFalse(p.test("a"));
    }

    @Test
    void and_secondNull_returnsFirst() {
        Predicate<String> first = s -> s.equals("a");
        Predicate<? super String> p = PredicateUtil.and(first, null);
        assertTrue(p.test("a"));
        assertFalse(p.test("b"));
    }

    @Test
    void and_bothNonNull_requiresBothToMatch() {
        Predicate<String> startsWithA = s -> s.startsWith("a");
        Predicate<String> longerThanThree = s -> s.length() > 3;
        Predicate<? super String> p = PredicateUtil.and(startsWithA, longerThanThree);
        assertTrue(p.test("apple"));
        assertFalse(p.test("ax"));
        assertFalse(p.test("banana"));
    }

    // ---------------------------------------------------------------------------
    // or(Predicate, Predicate) - 2-arg overload
    // ---------------------------------------------------------------------------

    @Test
    void or_bothNull_returnsAlwaysFalse() {
        Predicate<? super String> p = PredicateUtil.or(null, null);
        assertFalse(p.test("anything"));
    }

    @Test
    void or_firstNull_returnsSecond() {
        Predicate<String> second = s -> s.equals("b");
        Predicate<? super String> p = PredicateUtil.or(null, second);
        assertTrue(p.test("b"));
        assertFalse(p.test("a"));
    }

    @Test
    void or_secondNull_returnsFirst() {
        Predicate<String> first = s -> s.equals("a");
        Predicate<? super String> p = PredicateUtil.or(first, null);
        assertTrue(p.test("a"));
        assertFalse(p.test("b"));
    }

    @Test
    void or_bothNonNull_matchesIfEitherMatches() {
        Predicate<String> startsWithA = s -> s.startsWith("a");
        Predicate<String> startsWithB = s -> s.startsWith("b");
        Predicate<? super String> p = PredicateUtil.or(startsWithA, startsWithB);
        assertTrue(p.test("apple"));
        assertTrue(p.test("banana"));
        assertFalse(p.test("cherry"));
    }

    // ---------------------------------------------------------------------------
    // and(Iterable)
    // ---------------------------------------------------------------------------

    @Test
    void andIterable_null_returnsAlwaysTrue() {
        Predicate<? super String> p = PredicateUtil.and(null);
        assertTrue(p.test("anything"));
    }

    @Test
    void andIterable_empty_returnsAlwaysTrue() {
        Predicate<? super String> p = PredicateUtil.and(List.<Predicate<? super String>>of());
        assertTrue(p.test("anything"));
    }

    @Test
    void andIterable_allMustMatch() {
        Predicate<String> startsWithA = s -> s.startsWith("a");
        Predicate<String> endsWithE = s -> s.endsWith("e");
        Predicate<? super String> p = PredicateUtil.and(List.of(startsWithA, endsWithE));
        assertTrue(p.test("apple"));
        assertFalse(p.test("apply"));
        assertFalse(p.test("banane"));
    }

    @Test
    void andIterable_shortCircuits_onFirstFailure() {
        AtomicInteger secondCallCount = new AtomicInteger();
        Predicate<String> alwaysFalse = s -> false;
        Predicate<String> countingTrue = s -> {
            secondCallCount.incrementAndGet();
            return true;
        };
        Predicate<? super String> p = PredicateUtil.and(List.of(alwaysFalse, countingTrue));
        assertFalse(p.test("x"));
        assertEquals(0, secondCallCount.get(), "second predicate should not be evaluated once the first fails");
    }

    // ---------------------------------------------------------------------------
    // or(Iterable)
    // ---------------------------------------------------------------------------

    @Test
    void orIterable_null_returnsAlwaysFalse() {
        Predicate<? super String> p = PredicateUtil.or(null);
        assertFalse(p.test("anything"));
    }

    @Test
    void orIterable_empty_returnsAlwaysFalse() {
        Predicate<? super String> p = PredicateUtil.or(List.<Predicate<? super String>>of());
        assertFalse(p.test("anything"));
    }

    @Test
    void orIterable_anyMayMatch() {
        Predicate<String> startsWithA = s -> s.startsWith("a");
        Predicate<String> startsWithB = s -> s.startsWith("b");
        Predicate<? super String> p = PredicateUtil.or(List.of(startsWithA, startsWithB));
        assertTrue(p.test("apple"));
        assertTrue(p.test("banana"));
        assertFalse(p.test("cherry"));
    }

    @Test
    void orIterable_shortCircuits_onFirstMatch() {
        AtomicInteger secondCallCount = new AtomicInteger();
        Predicate<String> alwaysTrue = s -> true;
        Predicate<String> countingFalse = s -> {
            secondCallCount.incrementAndGet();
            return false;
        };
        Predicate<? super String> p = PredicateUtil.or(List.of(alwaysTrue, countingFalse));
        assertTrue(p.test("x"));
        assertEquals(0, secondCallCount.get(), "second predicate should not be evaluated once the first matches");
    }

}

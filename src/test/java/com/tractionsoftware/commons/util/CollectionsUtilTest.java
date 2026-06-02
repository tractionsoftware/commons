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

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public final class CollectionsUtilTest {

    // -------------------------------------------------------------------------
    // isEmpty / isNotEmpty (Collection)
    // -------------------------------------------------------------------------

    @Test
    public void isEmpty_nullCollection_returnsTrue() {
        assertTrue(CollectionsUtil.isEmpty((Collection<?>) null));
    }

    @Test
    public void isEmpty_emptyCollection_returnsTrue() {
        assertTrue(CollectionsUtil.isEmpty(Collections.emptyList()));
    }

    @Test
    public void isEmpty_nonEmptyCollection_returnsFalse() {
        assertFalse(CollectionsUtil.isEmpty(List.of("a")));
    }

    @Test
    public void isNotEmpty_nullCollection_returnsFalse() {
        assertFalse(CollectionsUtil.isNotEmpty((Collection<?>) null));
    }

    @Test
    public void isNotEmpty_emptyCollection_returnsFalse() {
        assertFalse(CollectionsUtil.isNotEmpty(Collections.emptyList()));
    }

    @Test
    public void isNotEmpty_nonEmptyCollection_returnsTrue() {
        assertTrue(CollectionsUtil.isNotEmpty(List.of("a")));
    }

    // -------------------------------------------------------------------------
    // isEmpty / isNotEmpty (Map)
    // -------------------------------------------------------------------------

    @Test
    public void isEmpty_nullMap_returnsTrue() {
        assertTrue(CollectionsUtil.isEmpty((Map<?,?>) null));
    }

    @Test
    public void isEmpty_emptyMap_returnsTrue() {
        assertTrue(CollectionsUtil.isEmpty(Collections.emptyMap()));
    }

    @Test
    public void isEmpty_nonEmptyMap_returnsFalse() {
        assertFalse(CollectionsUtil.isEmpty(Map.of("k", "v")));
    }

    @Test
    public void isNotEmpty_nullMap_returnsFalse() {
        assertFalse(CollectionsUtil.isNotEmpty((Map<?,?>) null));
    }

    @Test
    public void isNotEmpty_nonEmptyMap_returnsTrue() {
        assertTrue(CollectionsUtil.isNotEmpty(Map.of("k", "v")));
    }

    // -------------------------------------------------------------------------
    // contains
    // -------------------------------------------------------------------------

    @Test
    public void contains_nullCollection_returnsFalse() {
        assertFalse(CollectionsUtil.contains(null, "x"));
    }

    @Test
    public void contains_presentElement_returnsTrue() {
        assertTrue(CollectionsUtil.contains(List.of("a", "b", "c"), "b"));
    }

    @Test
    public void contains_absentElement_returnsFalse() {
        assertFalse(CollectionsUtil.contains(List.of("a", "b"), "z"));
    }

    @Test
    public void contains_nullElement_withSupportingCollection_returnsFalse() {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add(null);
        assertTrue(CollectionsUtil.contains(list, null));
    }

    // -------------------------------------------------------------------------
    // size
    // -------------------------------------------------------------------------

    @Test
    public void size_null_returnsZero() {
        assertEquals(0, CollectionsUtil.size(null));
    }

    @Test
    public void size_emptyList_returnsZero() {
        assertEquals(0, CollectionsUtil.size(Collections.emptyList()));
    }

    @Test
    public void size_nonEmptyList_returnsCorrectSize() {
        assertEquals(3, CollectionsUtil.size(List.of(1, 2, 3)));
    }

    // -------------------------------------------------------------------------
    // addIfNotNull
    // -------------------------------------------------------------------------

    @Test
    public void addIfNotNull_nullValue_returnsFalse() {
        List<String> list = new ArrayList<>();
        assertFalse(CollectionsUtil.addIfNotNull(null, list));
        assertTrue(list.isEmpty());
    }

    @Test
    public void addIfNotNull_nullCollection_returnsFalse() {
        assertFalse(CollectionsUtil.addIfNotNull("x", null));
    }

    @Test
    public void addIfNotNull_nonNullValue_addsAndReturnsTrue() {
        List<String> list = new ArrayList<>();
        assertTrue(CollectionsUtil.addIfNotNull("hello", list));
        assertEquals(List.of("hello"), list);
    }

    // -------------------------------------------------------------------------
    // putIfNotNull
    // -------------------------------------------------------------------------

    @Test
    public void putIfNotNull_nullValue_returnsFalse() {
        Map<String,String> map = new HashMap<>();
        assertFalse(CollectionsUtil.putIfNotNull("k", null, map));
        assertTrue(map.isEmpty());
    }

    @Test
    public void putIfNotNull_nullMap_returnsFalse() {
        assertFalse(CollectionsUtil.putIfNotNull("k", "v", null));
    }

    @Test
    public void putIfNotNull_nonNullValue_putsAndReturnsTrue() {
        Map<String,String> map = new HashMap<>();
        assertTrue(CollectionsUtil.putIfNotNull("k", "v", map));
        assertEquals("v", map.get("k"));
    }

    // -------------------------------------------------------------------------
    // putOrRemove
    // -------------------------------------------------------------------------

    @Test
    public void putOrRemove_nullMap_doesNotThrow() {
        assertDoesNotThrow(() -> CollectionsUtil.putOrRemove(null, "k", "v"));
    }

    @Test
    public void putOrRemove_nullValue_removesKey() {
        Map<String,String> map = new HashMap<>();
        map.put("k", "v");
        CollectionsUtil.putOrRemove(map, "k", null);
        assertFalse(map.containsKey("k"));
    }

    @Test
    public void putOrRemove_nonNullValue_putsEntry() {
        Map<String,String> map = new HashMap<>();
        CollectionsUtil.putOrRemove(map, "k", "v");
        assertEquals("v", map.get("k"));
    }

    // -------------------------------------------------------------------------
    // copy (Collection)
    // -------------------------------------------------------------------------

    @Test
    public void copy_nullSource_noChange() {
        List<String> dest = new ArrayList<>(List.of("x"));
        CollectionsUtil.copy(null, dest);
        assertEquals(List.of("x"), dest);
    }

    @Test
    public void copy_nullDestination_noThrow() {
        assertDoesNotThrow(() -> CollectionsUtil.copy(List.of("a"), null));
    }

    @Test
    public void copy_copiesAllElements() {
        List<String> dest = new ArrayList<>();
        CollectionsUtil.copy(List.of("a", "b", "c"), dest);
        assertEquals(List.of("a", "b", "c"), dest);
    }

    // -------------------------------------------------------------------------
    // clearAndCopy (Collection)
    // -------------------------------------------------------------------------

    @Test
    public void clearAndCopy_clearsDestThenCopies() {
        List<String> dest = new ArrayList<>(List.of("old"));
        CollectionsUtil.clearAndCopy(List.of("new1", "new2"), dest);
        assertEquals(List.of("new1", "new2"), dest);
    }

    @Test
    public void clearAndCopy_nullDestination_noThrow() {
        assertDoesNotThrow(() -> CollectionsUtil.clearAndCopy(List.of("a"), null));
    }

    // -------------------------------------------------------------------------
    // copyNonNull
    // -------------------------------------------------------------------------

    @Test
    public void copyNonNull_filtersNulls() {
        List<String> src = new ArrayList<>();
        src.add("a");
        src.add(null);
        src.add("b");
        List<String> dest = new ArrayList<>();
        boolean changed = CollectionsUtil.copyNonNull(src, dest);
        assertTrue(changed);
        assertEquals(List.of("a", "b"), dest);
    }

    @Test
    public void copyNonNull_allNull_returnsFalse() {
        List<String> src = new ArrayList<>();
        src.add(null);
        List<String> dest = new ArrayList<>();
        assertFalse(CollectionsUtil.copyNonNull(src, dest));
        assertTrue(dest.isEmpty());
    }

    // -------------------------------------------------------------------------
    // copy (Map)
    // -------------------------------------------------------------------------

    @Test
    public void copyMap_copiesEntries() {
        Map<String,String> dest = new HashMap<>();
        CollectionsUtil.copy(Map.of("a", "1", "b", "2"), dest);
        assertEquals("1", dest.get("a"));
        assertEquals("2", dest.get("b"));
    }

    @Test
    public void copyMap_nullSource_noChange() {
        Map<String,String> dest = new HashMap<>(Map.of("k", "v"));
        CollectionsUtil.copy(null, dest);
        assertEquals(Map.of("k", "v"), dest);
    }

    // -------------------------------------------------------------------------
    // emptyInsteadOfNull / emptyListInsteadOfNull / emptySetInsteadOfNull
    // -------------------------------------------------------------------------

    @Test
    public void emptyInsteadOfNull_null_returnsEmpty() {
        assertNotNull(CollectionsUtil.emptyInsteadOfNull(null));
        assertTrue(CollectionsUtil.emptyInsteadOfNull(null).isEmpty());
    }

    @Test
    public void emptyInsteadOfNull_nonNull_returnsSame() {
        List<String> list = List.of("a");
        assertSame(list, CollectionsUtil.emptyInsteadOfNull(list));
    }

    @Test
    public void emptyListInsteadOfNull_null_returnsEmpty() {
        assertTrue(CollectionsUtil.emptyListInsteadOfNull(null).isEmpty());
    }

    @Test
    public void emptySetInsteadOfNull_null_returnsEmpty() {
        assertTrue(CollectionsUtil.emptySetInsteadOfNull(null).isEmpty());
    }

    // -------------------------------------------------------------------------
    // hashMap / arrayList / hashSet / linkedHashSet
    // -------------------------------------------------------------------------

    @Test
    public void hashMap_null_returnsNull() {
        assertNull(CollectionsUtil.hashMap(null));
    }

    @Test
    public void hashMap_alreadyHashMap_returnsSameRef() {
        HashMap<String,String> map = new HashMap<>(Map.of("k", "v"));
        assertSame(map, CollectionsUtil.hashMap(map));
    }

    @Test
    public void hashMap_otherMap_returnsHashMapCopy() {
        Map<String,String> other = new TreeMap<>(Map.of("k", "v"));
        HashMap<String,String> result = CollectionsUtil.hashMap(other);
        assertNotNull(result);
        assertEquals("v", result.get("k"));
    }

    @Test
    public void arrayList_null_returnsNull() {
        assertNull(CollectionsUtil.arrayList(null));
    }

    @Test
    public void arrayList_alreadyArrayList_returnsSameRef() {
        ArrayList<String> list = new ArrayList<>(List.of("a"));
        assertSame(list, CollectionsUtil.arrayList(list));
    }

    @Test
    public void hashSet_null_returnsNull() {
        assertNull(CollectionsUtil.hashSet(null));
    }

    @Test
    public void hashSet_alreadyHashSet_returnsSameRef() {
        HashSet<String> set = new HashSet<>(Set.of("a"));
        assertSame(set, CollectionsUtil.hashSet(set));
    }

    @Test
    public void linkedHashSet_null_returnsNull() {
        assertNull(CollectionsUtil.linkedHashSet(null));
    }

    // -------------------------------------------------------------------------
    // firstOrDefault
    // -------------------------------------------------------------------------

    @Test
    public void firstOrDefault_null_returnsDefault() {
        assertEquals("default", CollectionsUtil.firstOrDefault(null, "default"));
    }

    @Test
    public void firstOrDefault_nonEmpty_returnsFirst() {
        assertEquals("a", CollectionsUtil.firstOrDefault(List.of("a", "b", "c"), "default"));
    }

    // -------------------------------------------------------------------------
    // intRangeIterator
    // -------------------------------------------------------------------------

    @Test
    public void intRangeIterator_normalRange_iteratesCorrectly() {
        Iterator<Integer> iter = CollectionsUtil.intRangeIterator(3, 5);
        assertTrue(iter.hasNext());
        assertEquals(3, iter.next());
        assertEquals(4, iter.next());
        assertEquals(5, iter.next());
        assertFalse(iter.hasNext());
    }

    @Test
    public void intRangeIterator_singleValue_iteratesOnce() {
        Iterator<Integer> iter = CollectionsUtil.intRangeIterator(7, 7);
        assertTrue(iter.hasNext());
        assertEquals(7, iter.next());
        assertFalse(iter.hasNext());
    }

    @Test
    public void intRangeIterator_firstGreaterThanLast_isEmpty() {
        Iterator<Integer> iter = CollectionsUtil.intRangeIterator(5, 3);
        assertFalse(iter.hasNext());
    }

    @Test
    public void intRangeIterator_remove_throwsUnsupported() {
        Iterator<Integer> iter = CollectionsUtil.intRangeIterator(1, 2);
        iter.next();
        assertThrows(UnsupportedOperationException.class, iter::remove);
    }

    // -------------------------------------------------------------------------
    // filteredIterator
    // -------------------------------------------------------------------------

    @Test
    public void filteredIterator_null_returnsEmpty() {
        assertFalse(CollectionsUtil.filteredIterator(null, x -> true).hasNext());
    }

    @Test
    public void filteredIterator_nullPredicate_returnsOriginal() {
        Iterator<String> iter = List.of("a", "b").iterator();
        Iterator<String> result = CollectionsUtil.filteredIterator(iter, null);
        assertSame(iter, result);
    }

    @Test
    public void filteredIterator_filtersElements() {
        List<String> result = new ArrayList<>();
        Iterator<String> iter = CollectionsUtil.filteredIterator(
            List.of("a", "bb", "c", "ddd").iterator(),
            s -> s.length() == 1
        );
        iter.forEachRemaining(result::add);
        assertEquals(List.of("a", "c"), result);
    }

    // -------------------------------------------------------------------------
    // inverseFilteredIterator
    // -------------------------------------------------------------------------

    @Test
    public void inverseFilteredIterator_null_returnsEmpty() {
        assertFalse(CollectionsUtil.inverseFilteredIterator(null, x -> true).hasNext());
    }

    @Test
    public void inverseFilteredIterator_excludesMatches() {
        List<String> result = new ArrayList<>();
        Iterator<String> iter = CollectionsUtil.inverseFilteredIterator(
            List.of("a", "bb", "c").iterator(),
            s -> s.length() == 1
        );
        iter.forEachRemaining(result::add);
        assertEquals(List.of("bb"), result);
    }

    // -------------------------------------------------------------------------
    // filteringIterable
    // -------------------------------------------------------------------------

    @Test
    public void filteringIterable_null_returnsEmpty() {
        assertFalse(CollectionsUtil.filteringIterable(null, x -> true).iterator().hasNext());
    }

    @Test
    public void filteringIterable_nullPredicate_returnsOriginal() {
        Iterable<String> src = List.of("a", "b");
        assertSame(src, CollectionsUtil.filteringIterable(src, null));
    }

    @Test
    public void filteringIterable_filtersCorrectly() {
        Iterable<Integer> result = CollectionsUtil.filteringIterable(
            List.of(1, 2, 3, 4, 5),
            n -> n % 2 == 0
        );
        List<Integer> collected = new ArrayList<>();
        result.forEach(collected::add);
        assertEquals(List.of(2, 4), collected);
    }

    // -------------------------------------------------------------------------
    // excluding / includingOnly (Iterable)
    // -------------------------------------------------------------------------

    @Test
    public void excluding_iterable_null_returnsEmpty() {
        assertFalse(CollectionsUtil.excluding((Iterable<String>) null, List.of("x")).iterator().hasNext());
    }

    @Test
    public void excluding_iterable_excludesElements() {
        List<String> result = new ArrayList<>();
        CollectionsUtil.excluding(List.of("a", "b", "c", "d"), List.of("b", "d"))
            .forEach(result::add);
        assertEquals(List.of("a", "c"), result);
    }

    @Test
    public void includingOnly_iterable_retainsElements() {
        List<String> result = new ArrayList<>();
        CollectionsUtil.includingOnly(List.of("a", "b", "c", "d"), List.of("b", "d"))
            .forEach(result::add);
        assertEquals(List.of("b", "d"), result);
    }

    // -------------------------------------------------------------------------
    // removeIf (Iterator)
    // -------------------------------------------------------------------------

    @Test
    public void removeIf_nullIterator_returnsFalse() {
        assertFalse(CollectionsUtil.removeIf(null, x -> true));
    }

    @Test
    public void removeIf_removesMatchingElements() {
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        CollectionsUtil.removeIf(list.iterator(), n -> n % 2 == 0);
        assertEquals(List.of(1, 3, 5), list);
    }

    // -------------------------------------------------------------------------
    // sortIfList
    // -------------------------------------------------------------------------

    @Test
    public void sortIfList_naturalOrder_sortsList() {
        List<Integer> list = new ArrayList<>(List.of(3, 1, 2));
        assertTrue(CollectionsUtil.sortIfList(list));
        assertEquals(List.of(1, 2, 3), list);
    }

    @Test
    public void sortIfList_notAList_returnsFalse() {
        assertFalse(CollectionsUtil.sortIfList(new HashSet<>(Set.of(3, 1, 2))));
    }

    @Test
    public void sortIfList_withComparator_sortsReversed() {
        List<Integer> list = new ArrayList<>(List.of(1, 3, 2));
        assertTrue(CollectionsUtil.sortIfList(list, Comparator.reverseOrder()));
        assertEquals(List.of(3, 2, 1), list);
    }

    // -------------------------------------------------------------------------
    // transform
    // -------------------------------------------------------------------------

    @Test
    public void transform_nullList_noThrow() {
        assertDoesNotThrow(() -> CollectionsUtil.transform(null, s -> s + "!"));
    }

    @Test
    public void transform_appliesOperator() {
        List<String> list = new ArrayList<>(List.of("a", "b", "c"));
        CollectionsUtil.transform(list, String::toUpperCase);
        assertEquals(List.of("A", "B", "C"), list);
    }

    // -------------------------------------------------------------------------
    // indexedMap
    // -------------------------------------------------------------------------

    @Test
    public void indexedMap_null_returnsEmpty() {
        assertTrue(CollectionsUtil.indexedMap((Iterable<String>) null, 0).isEmpty());
    }

    @Test
    public void indexedMap_producesCorrectMap() {
        Map<String,String> map = CollectionsUtil.indexedMap(List.of("a", "b", "c"), 0);
        assertEquals("a", map.get("0"));
        assertEquals("b", map.get("1"));
        assertEquals("c", map.get("2"));
    }

    @Test
    public void indexedMap_nonZeroInitialIndex() {
        Map<String,String> map = CollectionsUtil.indexedMap(List.of("x", "y"), 5);
        assertEquals("x", map.get("5"));
        assertEquals("y", map.get("6"));
    }

    // -------------------------------------------------------------------------
    // getOrCreateAndPut
    // -------------------------------------------------------------------------

    @Test
    public void getOrCreateAndPut_existingKey_returnsExisting() {
        Map<String,String> map = new HashMap<>(Map.of("k", "existing"));
        String result = CollectionsUtil.getOrCreateAndPut(map, "k", _ -> "new");
        assertEquals("existing", result);
        assertEquals("existing", map.get("k"));
    }

    @Test
    public void getOrCreateAndPut_missingKey_createsAndPuts() {
        Map<String,String> map = new HashMap<>();
        String result = CollectionsUtil.getOrCreateAndPut(map, "k", _ -> "created");
        assertEquals("created", result);
        assertEquals("created", map.get("k"));
    }

    // -------------------------------------------------------------------------
    // unique (symmetric difference)
    // -------------------------------------------------------------------------

    @Test
    public void unique_bothNull_noChange() {
        List<String> result = new ArrayList<>();
        CollectionsUtil.unique(null, null, result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void unique_nullA_addsB() {
        List<String> result = new ArrayList<>();
        CollectionsUtil.unique(null, List.of("x", "y"), result);
        assertEquals(List.of("x", "y"), result);
    }

    @Test
    public void unique_nullB_addsA() {
        List<String> result = new ArrayList<>();
        CollectionsUtil.unique(List.of("x", "y"), null, result);
        assertEquals(List.of("x", "y"), result);
    }

    @Test
    public void unique_symmetricDifference_sets() {
        Set<String> a = Set.of("a", "b", "c");
        Set<String> b = Set.of("b", "c", "d");
        Set<String> result = new HashSet<>();
        CollectionsUtil.unique(a, b, result);
        assertEquals(Set.of("a", "d"), result);
    }

    @Test
    public void unique_symmetricDifference_lists() {
        List<String> a = List.of("a", "b", "c");
        List<String> b = List.of("b", "c", "d");
        List<String> result = new ArrayList<>();
        CollectionsUtil.unique(a, b, result);
        // "a" is in a but not b; "d" is in b but not a
        assertTrue(result.contains("a"));
        assertTrue(result.contains("d"));
        assertFalse(result.contains("b"));
        assertFalse(result.contains("c"));
    }

    // -------------------------------------------------------------------------
    // safeToString (Map)
    // -------------------------------------------------------------------------

    @Test
    public void safeToString_nullMap_returnsEmptyBraces() {
        assertEquals("{}", CollectionsUtil.safeToString((Map<?,?>) null));
    }

    @Test
    public void safeToString_emptyMap_returnsEmptyBraces() {
        assertEquals("{}", CollectionsUtil.safeToString(Collections.emptyMap()));
    }

    @Test
    public void safeToString_nonEmptyMap_containsKeyAndValue() {
        String result = CollectionsUtil.safeToString(Map.of("k", "v"));
        assertTrue(result.contains("k"), "Expected key in result: " + result);
        assertTrue(result.contains("v"), "Expected value in result: " + result);
    }

    // -------------------------------------------------------------------------
    // safeToString (Iterable)
    // -------------------------------------------------------------------------

    @Test
    public void safeToString_nullIterable_returnsEmptyBrackets() {
        assertEquals("[]", CollectionsUtil.safeToString((Iterable<?>) null));
    }

    @Test
    public void safeToString_emptyIterable_returnsEmptyBrackets() {
        assertEquals("[]", CollectionsUtil.safeToString(Collections.emptyList()));
    }

    @Test
    public void safeToString_nonEmptyIterable_containsElements() {
        String result = CollectionsUtil.safeToString(List.of("a", "b"));
        assertTrue(result.contains("a"));
        assertTrue(result.contains("b"));
    }

    // -------------------------------------------------------------------------
    // unmodifiableCollection / unmodifiableList / unmodifiableSet / unmodifiableMap
    // -------------------------------------------------------------------------

    @Test
    public void unmodifiableCollection_null_returnsEmpty() {
        assertTrue(CollectionsUtil.unmodifiableCollection(null).isEmpty());
    }

    @Test
    public void unmodifiableCollection_immutable_returnsSameRef() {
        ImmutableList<String> list = ImmutableList.of("a");
        assertSame(list, CollectionsUtil.unmodifiableCollection(list));
    }

    @Test
    public void unmodifiableList_null_returnsEmpty() {
        assertTrue(CollectionsUtil.unmodifiableList(null).isEmpty());
    }

    @Test
    public void unmodifiableSet_null_returnsEmpty() {
        assertTrue(CollectionsUtil.unmodifiableSet(null).isEmpty());
    }

    @Test
    public void unmodifiableMap_null_returnsEmpty() {
        assertTrue(CollectionsUtil.unmodifiableMap(null).isEmpty());
    }

    // -------------------------------------------------------------------------
    // emptySequencedSet / singletonSequencedSet / singletonOrEmptySequencedSet
    // -------------------------------------------------------------------------

    @Test
    public void emptySequencedSet_isEmpty() {
        SequencedSet<?> s = CollectionsUtil.emptySequencedSet();
        assertTrue(s.isEmpty());
        assertEquals(0, s.size());
    }

    @Test
    public void emptySequencedSet_mutationThrows() {
        SequencedSet<String> s = CollectionsUtil.emptySequencedSet();
        assertThrows(UnsupportedOperationException.class, () -> s.add("x"));
    }

    @Test
    public void singletonSequencedSet_containsElement() {
        SequencedSet<String> s = CollectionsUtil.singletonSequencedSet("hello");
        assertEquals(1, s.size());
        assertTrue(s.contains("hello"));
        assertFalse(s.contains("world"));
    }

    @Test
    public void singletonSequencedSet_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> CollectionsUtil.singletonSequencedSet(null));
    }

    @Test
    public void singletonOrEmptySequencedSet_null_returnsEmpty() {
        assertTrue(CollectionsUtil.singletonOrEmptySequencedSet(null).isEmpty());
    }

    @Test
    public void singletonOrEmptySequencedSet_nonNull_returnsSingleton() {
        SequencedSet<String> s = CollectionsUtil.singletonOrEmptySequencedSet("x");
        assertEquals(1, s.size());
        assertTrue(s.contains("x"));
    }

    // -------------------------------------------------------------------------
    // addFromIterable / addFromIterator
    // -------------------------------------------------------------------------

    @Test
    public void addFromIterable_null_returnsFalse() {
        assertFalse(CollectionsUtil.addFromIterable(null, new ArrayList<>()));
    }

    @Test
    public void addFromIterable_nullDest_returnsFalse() {
        assertFalse(CollectionsUtil.addFromIterable(List.of("a"), null));
    }

    @Test
    public void addFromIterable_addsElements() {
        List<String> dest = new ArrayList<>();
        assertTrue(CollectionsUtil.addFromIterable(List.of("a", "b"), dest));
        assertEquals(List.of("a", "b"), dest);
    }

    @Test
    public void addFromIterator_null_returnsFalse() {
        assertFalse(CollectionsUtil.addFromIterator(null, new ArrayList<>()));
    }

    @Test
    public void addFromIterator_addsElements() {
        List<String> dest = new ArrayList<>();
        CollectionsUtil.addFromIterator(List.of("x", "y").iterator(), dest);
        assertEquals(List.of("x", "y"), dest);
    }

    // -------------------------------------------------------------------------
    // putMapEntryStrings
    // -------------------------------------------------------------------------

    @Test
    public void putMapEntryStrings_withEquals_parsesKeyValue() {
        Map<String,String> map = new HashMap<>();
        CollectionsUtil.putMapEntryStrings(List.of("foo=bar", "baz=qux"), map);
        assertEquals("bar", map.get("foo"));
        assertEquals("qux", map.get("baz"));
    }

    @Test
    public void putMapEntryStrings_withoutEquals_emptyValue() {
        Map<String,String> map = new HashMap<>();
        CollectionsUtil.putMapEntryStrings(List.of("flagonly"), map);
        assertEquals("", map.get("flagonly"));
    }

    @Test
    public void putMapEntryStrings_emptyStrings_skipped() {
        Map<String,String> map = new HashMap<>();
        CollectionsUtil.putMapEntryStrings(List.of("", "  ", "k=v"), map);
        assertEquals(1, map.size());
        assertEquals("v", map.get("k"));
    }

    // -------------------------------------------------------------------------
    // getListIndex2ListValueFunction
    // -------------------------------------------------------------------------

    @Test
    public void getListIndex2ListValueFunction_null_returnsNullForAnyIndex() {
        var fn = CollectionsUtil.getListIndex2ListValueFunction(null);
        assertNull(fn.apply(0));
    }

    @Test
    public void getListIndex2ListValueFunction_validIndex_returnsElement() {
        var fn = CollectionsUtil.getListIndex2ListValueFunction(List.of("a", "b", "c"));
        assertEquals("b", fn.apply(1));
    }

    @Test
    public void getListIndex2ListValueFunction_outOfRange_returnsNull() {
        var fn = CollectionsUtil.getListIndex2ListValueFunction(List.of("a", "b"));
        assertNull(fn.apply(5));
        assertNull(fn.apply(-1));
    }

    // -------------------------------------------------------------------------
    // getMapKey2ValueFunction
    // -------------------------------------------------------------------------

    @Test
    public void getMapKey2ValueFunction_null_returnsNullForAnyKey() {
        Function<String,String> fn = CollectionsUtil.getMapKey2ValueFunction(null);
        assertNull(fn.apply("anything"));
    }

    @Test
    public void getMapKey2ValueFunction_presentKey_returnsValue() {
        Function<String,String> fn = CollectionsUtil.getMapKey2ValueFunction(Map.of("k", "v"));
        assertEquals("v", fn.apply("k"));
    }

    @Test
    public void getMapKey2ValueFunction_absentKey_returnsNull() {
        Function<String,String> fn = CollectionsUtil.getMapKey2ValueFunction(Map.of("k", "v"));
        assertNull(fn.apply("missing"));
    }

    // -------------------------------------------------------------------------
    // convertingIterator / convertingIterable
    // -------------------------------------------------------------------------

    @Test
    public void convertingIterator_transformsElements() {
        List<String> result = new ArrayList<>();
        CollectionsUtil.convertingIterator(List.of(1, 2, 3), Object::toString)
            .forEachRemaining(result::add);
        assertEquals(List.of("1", "2", "3"), result);
    }

    @Test
    public void convertingIterable_transformsElements() {
        List<String> result = new ArrayList<>();
        CollectionsUtil.convertingIterable(List.of(1, 2, 3), Object::toString)
            .forEach(result::add);
        assertEquals(List.of("1", "2", "3"), result);
    }

    // -------------------------------------------------------------------------
    // addConvertedFromIterable / addConvertedFromIterator
    // -------------------------------------------------------------------------

    @Test
    public void addConvertedFromIterable_addsConverted() {
        List<String> dest = new ArrayList<>();
        CollectionsUtil.addConvertedFromIterable(List.of(1, 2, 3), dest, Object::toString);
        assertEquals(List.of("1", "2", "3"), dest);
    }

    @Test
    public void addConvertedFromIterator_addsConverted() {
        List<String> dest = new ArrayList<>();
        CollectionsUtil.addConvertedFromIterator(List.of(1, 2, 3).iterator(), dest, Object::toString);
        assertEquals(List.of("1", "2", "3"), dest);
    }

    // -------------------------------------------------------------------------
    // firstNonNullElementSingletonOrEmptySequencedSet
    // -------------------------------------------------------------------------

    @Test
    public void firstNonNullElement_null_returnsEmpty() {
        assertTrue(CollectionsUtil.firstNonNullElementSingletonOrEmptySequencedSet(null).isEmpty());
    }

    @Test
    public void firstNonNullElement_allNull_returnsEmpty() {
        List<String> list = new ArrayList<>();
        list.add(null);
        list.add(null);
        assertTrue(CollectionsUtil.firstNonNullElementSingletonOrEmptySequencedSet(list).isEmpty());
    }

    @Test
    public void firstNonNullElement_firstIsNonNull_returnsSingleton() {
        SequencedSet<String> result = CollectionsUtil.firstNonNullElementSingletonOrEmptySequencedSet(
            List.of("first", "second")
        );
        assertEquals(1, result.size());
        assertTrue(result.contains("first"));
    }

    @Test
    public void firstNonNullElement_nullThenNonNull_returnsFirstNonNull() {
        List<String> list = new ArrayList<>();
        list.add(null);
        list.add("second");
        SequencedSet<String> result = CollectionsUtil.firstNonNullElementSingletonOrEmptySequencedSet(list);
        assertEquals(1, result.size());
        assertTrue(result.contains("second"));
    }
}

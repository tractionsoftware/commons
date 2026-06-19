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

public final class CollectionUtilTest {

    // -------------------------------------------------------------------------
    // isEmpty / isNotEmpty (Collection)
    // -------------------------------------------------------------------------

    @Test
    public void isEmpty_nullCollection_returnsTrue() {
        assertTrue(CollectionUtil.isEmpty((Collection<?>) null));
    }

    @Test
    public void isEmpty_emptyCollection_returnsTrue() {
        assertTrue(CollectionUtil.isEmpty(Collections.emptyList()));
    }

    @Test
    public void isEmpty_nonEmptyCollection_returnsFalse() {
        assertFalse(CollectionUtil.isEmpty(List.of("a")));
    }

    @Test
    public void isNotEmpty_nullCollection_returnsFalse() {
        assertFalse(CollectionUtil.isNotEmpty((Collection<?>) null));
    }

    @Test
    public void isNotEmpty_emptyCollection_returnsFalse() {
        assertFalse(CollectionUtil.isNotEmpty(Collections.emptyList()));
    }

    @Test
    public void isNotEmpty_nonEmptyCollection_returnsTrue() {
        assertTrue(CollectionUtil.isNotEmpty(List.of("a")));
    }

    // -------------------------------------------------------------------------
    // isEmpty / isNotEmpty (Map)
    // -------------------------------------------------------------------------

    @Test
    public void isEmpty_nullMap_returnsTrue() {
        assertTrue(CollectionUtil.isEmpty((Map<?,?>) null));
    }

    @Test
    public void isEmpty_emptyMap_returnsTrue() {
        assertTrue(CollectionUtil.isEmpty(Collections.emptyMap()));
    }

    @Test
    public void isEmpty_nonEmptyMap_returnsFalse() {
        assertFalse(CollectionUtil.isEmpty(Map.of("k", "v")));
    }

    @Test
    public void isNotEmpty_nullMap_returnsFalse() {
        assertFalse(CollectionUtil.isNotEmpty((Map<?,?>) null));
    }

    @Test
    public void isNotEmpty_nonEmptyMap_returnsTrue() {
        assertTrue(CollectionUtil.isNotEmpty(Map.of("k", "v")));
    }

    // -------------------------------------------------------------------------
    // contains
    // -------------------------------------------------------------------------

    @Test
    public void contains_nullCollection_returnsFalse() {
        assertFalse(CollectionUtil.contains(null, "x"));
    }

    @Test
    public void contains_presentElement_returnsTrue() {
        assertTrue(CollectionUtil.contains(List.of("a", "b", "c"), "b"));
    }

    @Test
    public void contains_absentElement_returnsFalse() {
        assertFalse(CollectionUtil.contains(List.of("a", "b"), "z"));
    }

    @Test
    public void contains_nullElement_withSupportingCollection_returnsFalse() {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add(null);
        assertTrue(CollectionUtil.contains(list, null));
    }

    // -------------------------------------------------------------------------
    // size
    // -------------------------------------------------------------------------

    @Test
    public void size_null_returnsZero() {
        assertEquals(0, CollectionUtil.size(null));
    }

    @Test
    public void size_emptyList_returnsZero() {
        assertEquals(0, CollectionUtil.size(Collections.emptyList()));
    }

    @Test
    public void size_nonEmptyList_returnsCorrectSize() {
        assertEquals(3, CollectionUtil.size(List.of(1, 2, 3)));
    }

    // -------------------------------------------------------------------------
    // addIfNotNull
    // -------------------------------------------------------------------------

    @Test
    public void addIfNotNull_nullValue_returnsFalse() {
        List<String> list = new ArrayList<>();
        assertFalse(CollectionUtil.addIfNotNull(null, list));
        assertTrue(list.isEmpty());
    }

    @Test
    public void addIfNotNull_nullCollection_returnsFalse() {
        assertFalse(CollectionUtil.addIfNotNull("x", null));
    }

    @Test
    public void addIfNotNull_nonNullValue_addsAndReturnsTrue() {
        List<String> list = new ArrayList<>();
        assertTrue(CollectionUtil.addIfNotNull("hello", list));
        assertEquals(List.of("hello"), list);
    }

    // -------------------------------------------------------------------------
    // putIfNotNull
    // -------------------------------------------------------------------------

    @Test
    public void putIfNotNull_nullValue_returnsFalse() {
        Map<String,String> map = new HashMap<>();
        assertFalse(CollectionUtil.putIfNotNull("k", null, map));
        assertTrue(map.isEmpty());
    }

    @Test
    public void putIfNotNull_nullMap_returnsFalse() {
        assertFalse(CollectionUtil.putIfNotNull("k", "v", null));
    }

    @Test
    public void putIfNotNull_nonNullValue_putsAndReturnsTrue() {
        Map<String,String> map = new HashMap<>();
        assertTrue(CollectionUtil.putIfNotNull("k", "v", map));
        assertEquals("v", map.get("k"));
    }

    // -------------------------------------------------------------------------
    // putOrRemove
    // -------------------------------------------------------------------------

    @Test
    public void putOrRemove_nullMap_doesNotThrow() {
        assertDoesNotThrow(() -> CollectionUtil.putOrRemove(null, "k", "v"));
    }

    @Test
    public void putOrRemove_nullValue_removesKey() {
        Map<String,String> map = new HashMap<>();
        map.put("k", "v");
        CollectionUtil.putOrRemove(map, "k", null);
        assertFalse(map.containsKey("k"));
    }

    @Test
    public void putOrRemove_nonNullValue_putsEntry() {
        Map<String,String> map = new HashMap<>();
        CollectionUtil.putOrRemove(map, "k", "v");
        assertEquals("v", map.get("k"));
    }

    // -------------------------------------------------------------------------
    // copy (Collection)
    // -------------------------------------------------------------------------

    @Test
    public void copy_nullSource_noChange() {
        List<String> dest = new ArrayList<>(List.of("x"));
        CollectionUtil.copy(null, dest);
        assertEquals(List.of("x"), dest);
    }

    @Test
    public void copy_nullDestination_noThrow() {
        assertDoesNotThrow(() -> CollectionUtil.copy(List.of("a"), null));
    }

    @Test
    public void copy_copiesAllElements() {
        List<String> dest = new ArrayList<>();
        CollectionUtil.copy(List.of("a", "b", "c"), dest);
        assertEquals(List.of("a", "b", "c"), dest);
    }

    // -------------------------------------------------------------------------
    // clearAndCopy (Collection)
    // -------------------------------------------------------------------------

    @Test
    public void clearAndCopy_clearsDestThenCopies() {
        List<String> dest = new ArrayList<>(List.of("old"));
        CollectionUtil.clearAndCopy(List.of("new1", "new2"), dest);
        assertEquals(List.of("new1", "new2"), dest);
    }

    @Test
    public void clearAndCopy_nullDestination_noThrow() {
        assertDoesNotThrow(() -> CollectionUtil.clearAndCopy(List.of("a"), null));
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
        boolean changed = CollectionUtil.copyNonNull(src, dest);
        assertTrue(changed);
        assertEquals(List.of("a", "b"), dest);
    }

    @Test
    public void copyNonNull_allNull_returnsFalse() {
        List<String> src = new ArrayList<>();
        src.add(null);
        List<String> dest = new ArrayList<>();
        assertFalse(CollectionUtil.copyNonNull(src, dest));
        assertTrue(dest.isEmpty());
    }

    // -------------------------------------------------------------------------
    // copy (Map)
    // -------------------------------------------------------------------------

    @Test
    public void copyMap_copiesEntries() {
        Map<String,String> dest = new HashMap<>();
        CollectionUtil.copy(Map.of("a", "1", "b", "2"), dest);
        assertEquals("1", dest.get("a"));
        assertEquals("2", dest.get("b"));
    }

    @Test
    public void copyMap_nullSource_noChange() {
        Map<String,String> dest = new HashMap<>(Map.of("k", "v"));
        CollectionUtil.copy(null, dest);
        assertEquals(Map.of("k", "v"), dest);
    }

    // -------------------------------------------------------------------------
    // emptyInsteadOfNull / emptyListInsteadOfNull / emptySetInsteadOfNull
    // -------------------------------------------------------------------------

    @Test
    public void emptyInsteadOfNull_null_returnsEmpty() {
        assertNotNull(CollectionUtil.emptyInsteadOfNull(null));
        assertTrue(CollectionUtil.emptyInsteadOfNull(null).isEmpty());
    }

    @Test
    public void emptyInsteadOfNull_nonNull_returnsSame() {
        List<String> list = List.of("a");
        assertSame(list, CollectionUtil.emptyInsteadOfNull(list));
    }

    @Test
    public void emptyListInsteadOfNull_null_returnsEmpty() {
        assertTrue(CollectionUtil.emptyListInsteadOfNull(null).isEmpty());
    }

    @Test
    public void emptySetInsteadOfNull_null_returnsEmpty() {
        assertTrue(CollectionUtil.emptySetInsteadOfNull(null).isEmpty());
    }

    // -------------------------------------------------------------------------
    // hashMap / arrayList / hashSet / linkedHashSet
    // -------------------------------------------------------------------------

    @Test
    public void hashMap_null_returnsNull() {
        assertNull(CollectionUtil.hashMap(null));
    }

    @Test
    public void hashMap_alreadyHashMap_returnsSameRef() {
        HashMap<String,String> map = new HashMap<>(Map.of("k", "v"));
        assertSame(map, CollectionUtil.hashMap(map));
    }

    @Test
    public void hashMap_otherMap_returnsHashMapCopy() {
        Map<String,String> other = new TreeMap<>(Map.of("k", "v"));
        HashMap<String,String> result = CollectionUtil.hashMap(other);
        assertNotNull(result);
        assertEquals("v", result.get("k"));
    }

    @Test
    public void arrayList_null_returnsNull() {
        assertNull(CollectionUtil.arrayList(null));
    }

    @Test
    public void arrayList_alreadyArrayList_returnsSameRef() {
        ArrayList<String> list = new ArrayList<>(List.of("a"));
        assertSame(list, CollectionUtil.arrayList(list));
    }

    @Test
    public void hashSet_null_returnsNull() {
        assertNull(CollectionUtil.hashSet(null));
    }

    @Test
    public void hashSet_alreadyHashSet_returnsSameRef() {
        HashSet<String> set = new HashSet<>(Set.of("a"));
        assertSame(set, CollectionUtil.hashSet(set));
    }

    @Test
    public void linkedHashSet_null_returnsNull() {
        assertNull(CollectionUtil.linkedHashSet(null));
    }

    // -------------------------------------------------------------------------
    // firstOrDefault
    // -------------------------------------------------------------------------

    @Test
    public void firstOrDefault_null_returnsDefault() {
        assertEquals("default", CollectionUtil.firstOrDefault(null, "default"));
    }

    @Test
    public void firstOrDefault_nonEmpty_returnsFirst() {
        assertEquals("a", CollectionUtil.firstOrDefault(List.of("a", "b", "c"), "default"));
    }

    // -------------------------------------------------------------------------
    // intRangeIterator
    // -------------------------------------------------------------------------

    @Test
    public void intRangeIterator_normalRange_iteratesCorrectly() {
        Iterator<Integer> iter = CollectionUtil.intRangeIterator(3, 5);
        assertTrue(iter.hasNext());
        assertEquals(3, iter.next());
        assertEquals(4, iter.next());
        assertEquals(5, iter.next());
        assertFalse(iter.hasNext());
    }

    @Test
    public void intRangeIterator_singleValue_iteratesOnce() {
        Iterator<Integer> iter = CollectionUtil.intRangeIterator(7, 7);
        assertTrue(iter.hasNext());
        assertEquals(7, iter.next());
        assertFalse(iter.hasNext());
    }

    @Test
    public void intRangeIterator_firstGreaterThanLast_isEmpty() {
        Iterator<Integer> iter = CollectionUtil.intRangeIterator(5, 3);
        assertFalse(iter.hasNext());
    }

    @Test
    public void intRangeIterator_remove_throwsUnsupported() {
        Iterator<Integer> iter = CollectionUtil.intRangeIterator(1, 2);
        iter.next();
        assertThrows(UnsupportedOperationException.class, iter::remove);
    }

    // -------------------------------------------------------------------------
    // filteredIterator
    // -------------------------------------------------------------------------

    @Test
    public void filteredIterator_null_returnsEmpty() {
        assertFalse(CollectionUtil.filteredIterator(null, x -> true).hasNext());
    }

    @Test
    public void filteredIterator_nullPredicate_returnsOriginal() {
        Iterator<String> iter = List.of("a", "b").iterator();
        Iterator<String> result = CollectionUtil.filteredIterator(iter, null);
        assertSame(iter, result);
    }

    @Test
    public void filteredIterator_filtersElements() {
        List<String> result = new ArrayList<>();
        Iterator<String> iter = CollectionUtil.filteredIterator(
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
        assertFalse(CollectionUtil.inverseFilteredIterator(null, x -> true).hasNext());
    }

    @Test
    public void inverseFilteredIterator_excludesMatches() {
        List<String> result = new ArrayList<>();
        Iterator<String> iter = CollectionUtil.inverseFilteredIterator(
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
        assertFalse(CollectionUtil.filteringIterable(null, x -> true).iterator().hasNext());
    }

    @Test
    public void filteringIterable_nullPredicate_returnsOriginal() {
        Iterable<String> src = List.of("a", "b");
        assertSame(src, CollectionUtil.filteringIterable(src, null));
    }

    @Test
    public void filteringIterable_filtersCorrectly() {
        Iterable<Integer> result = CollectionUtil.filteringIterable(
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
        assertFalse(CollectionUtil.excluding((Iterable<String>) null, List.of("x")).iterator().hasNext());
    }

    @Test
    public void excluding_iterable_excludesElements() {
        List<String> result = new ArrayList<>();
        CollectionUtil.excluding(List.of("a", "b", "c", "d"), List.of("b", "d"))
            .forEach(result::add);
        assertEquals(List.of("a", "c"), result);
    }

    @Test
    public void includingOnly_iterable_retainsElements() {
        List<String> result = new ArrayList<>();
        CollectionUtil.includingOnly(List.of("a", "b", "c", "d"), List.of("b", "d"))
            .forEach(result::add);
        assertEquals(List.of("b", "d"), result);
    }

    // -------------------------------------------------------------------------
    // removeIf (Iterator)
    // -------------------------------------------------------------------------

    @Test
    public void removeIf_nullIterator_returnsFalse() {
        assertFalse(CollectionUtil.removeIf(null, x -> true));
    }

    @Test
    public void removeIf_removesMatchingElements() {
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        CollectionUtil.removeIf(list.iterator(), n -> n % 2 == 0);
        assertEquals(List.of(1, 3, 5), list);
    }

    // -------------------------------------------------------------------------
    // sortIfList
    // -------------------------------------------------------------------------

    @Test
    public void sortIfList_naturalOrder_sortsList() {
        List<Integer> list = new ArrayList<>(List.of(3, 1, 2));
        assertTrue(CollectionUtil.sortIfList(list));
        assertEquals(List.of(1, 2, 3), list);
    }

    @Test
    public void sortIfList_notAList_returnsFalse() {
        assertFalse(CollectionUtil.sortIfList(new HashSet<>(Set.of(3, 1, 2))));
    }

    @Test
    public void sortIfList_withComparator_sortsReversed() {
        List<Integer> list = new ArrayList<>(List.of(1, 3, 2));
        assertTrue(CollectionUtil.sortIfList(list, Comparator.reverseOrder()));
        assertEquals(List.of(3, 2, 1), list);
    }

    // -------------------------------------------------------------------------
    // transform
    // -------------------------------------------------------------------------

    @Test
    public void transform_nullList_noThrow() {
        assertDoesNotThrow(() -> CollectionUtil.transform(null, s -> s + "!"));
    }

    @Test
    public void transform_appliesOperator() {
        List<String> list = new ArrayList<>(List.of("a", "b", "c"));
        CollectionUtil.transform(list, String::toUpperCase);
        assertEquals(List.of("A", "B", "C"), list);
    }

    // -------------------------------------------------------------------------
    // indexedMap
    // -------------------------------------------------------------------------

    @Test
    public void indexedMap_null_returnsEmpty() {
        assertTrue(CollectionUtil.indexedMap((Iterable<String>) null, 0).isEmpty());
    }

    @Test
    public void indexedMap_producesCorrectMap() {
        Map<String,String> map = CollectionUtil.indexedMap(List.of("a", "b", "c"), 0);
        assertEquals("a", map.get("0"));
        assertEquals("b", map.get("1"));
        assertEquals("c", map.get("2"));
    }

    @Test
    public void indexedMap_nonZeroInitialIndex() {
        Map<String,String> map = CollectionUtil.indexedMap(List.of("x", "y"), 5);
        assertEquals("x", map.get("5"));
        assertEquals("y", map.get("6"));
    }

    // -------------------------------------------------------------------------
    // getOrCreateAndPut
    // -------------------------------------------------------------------------

    @Test
    public void getOrCreateAndPut_existingKey_returnsExisting() {
        Map<String,String> map = new HashMap<>(Map.of("k", "existing"));
        String result = CollectionUtil.getOrCreateAndPut(map, "k", _ -> "new");
        assertEquals("existing", result);
        assertEquals("existing", map.get("k"));
    }

    @Test
    public void getOrCreateAndPut_missingKey_createsAndPuts() {
        Map<String,String> map = new HashMap<>();
        String result = CollectionUtil.getOrCreateAndPut(map, "k", _ -> "created");
        assertEquals("created", result);
        assertEquals("created", map.get("k"));
    }

    // -------------------------------------------------------------------------
    // unique (symmetric difference)
    // -------------------------------------------------------------------------

    @Test
    public void unique_bothNull_noChange() {
        List<String> result = new ArrayList<>();
        CollectionUtil.unique(null, null, result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void unique_nullA_addsB() {
        List<String> result = new ArrayList<>();
        CollectionUtil.unique(null, List.of("x", "y"), result);
        assertEquals(List.of("x", "y"), result);
    }

    @Test
    public void unique_nullB_addsA() {
        List<String> result = new ArrayList<>();
        CollectionUtil.unique(List.of("x", "y"), null, result);
        assertEquals(List.of("x", "y"), result);
    }

    @Test
    public void unique_symmetricDifference_sets() {
        Set<String> a = Set.of("a", "b", "c");
        Set<String> b = Set.of("b", "c", "d");
        Set<String> result = new HashSet<>();
        CollectionUtil.unique(a, b, result);
        assertEquals(Set.of("a", "d"), result);
    }

    @Test
    public void unique_symmetricDifference_lists() {
        List<String> a = List.of("a", "b", "c");
        List<String> b = List.of("b", "c", "d");
        List<String> result = new ArrayList<>();
        CollectionUtil.unique(a, b, result);
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
        assertEquals("{}", CollectionUtil.safeToString((Map<?,?>) null));
    }

    @Test
    public void safeToString_emptyMap_returnsEmptyBraces() {
        assertEquals("{}", CollectionUtil.safeToString(Collections.emptyMap()));
    }

    @Test
    public void safeToString_nonEmptyMap_containsKeyAndValue() {
        String result = CollectionUtil.safeToString(Map.of("k", "v"));
        assertTrue(result.contains("k"), "Expected key in result: " + result);
        assertTrue(result.contains("v"), "Expected value in result: " + result);
    }

    @Test
    public void safeToString_nonEmptyMap2_containsKeyAndValue() {
        String result = CollectionUtil.safeToString(Map.of("k", "v", "kay", "vee"));
        assertTrue(result.contains("k"), "Expected key in result: " + result);
        assertTrue(result.contains("kay"), "Expected key in result: " + result);
        assertTrue(result.contains("v"), "Expected value in result: " + result);
        assertTrue(result.contains("vee"), "Expected value in result: " + result);
   }

    // -------------------------------------------------------------------------
    // safeToString (Iterable)
    // -------------------------------------------------------------------------

    @Test
    public void safeToString_nullIterable_returnsEmptyBrackets() {
        assertEquals("[]", CollectionUtil.safeToString((Iterable<?>) null));
    }

    @Test
    public void safeToString_emptyIterable_returnsEmptyBrackets() {
        assertEquals("[]", CollectionUtil.safeToString(Collections.emptyList()));
    }

    @Test
    public void safeToString_nonEmptyIterable_containsElements() {
        String result = CollectionUtil.safeToString(List.of("a", "b"));
        assertTrue(result.contains("a"));
        assertTrue(result.contains("b"));
    }

    // -------------------------------------------------------------------------
    // unmodifiableCollection / unmodifiableList / unmodifiableSet / unmodifiableMap
    // -------------------------------------------------------------------------

    @Test
    public void unmodifiableCollection_null_returnsEmpty() {
        assertTrue(CollectionUtil.unmodifiableCollection(null).isEmpty());
    }

    @Test
    public void unmodifiableCollection_immutable_returnsSameRef() {
        ImmutableList<String> list = ImmutableList.of("a");
        assertSame(list, CollectionUtil.unmodifiableCollection(list));
    }

    @Test
    public void unmodifiableList_null_returnsEmpty() {
        assertTrue(CollectionUtil.unmodifiableList(null).isEmpty());
    }

    @Test
    public void unmodifiableSet_null_returnsEmpty() {
        assertTrue(CollectionUtil.unmodifiableSet(null).isEmpty());
    }

    @Test
    public void unmodifiableMap_null_returnsEmpty() {
        assertTrue(CollectionUtil.unmodifiableMap(null).isEmpty());
    }

    // -------------------------------------------------------------------------
    // emptySequencedSet / singletonSequencedSet / singletonOrEmptySequencedSet
    // -------------------------------------------------------------------------

    @Test
    public void emptySequencedSet_isEmpty() {
        SequencedSet<?> s = CollectionUtil.emptySequencedSet();
        assertTrue(s.isEmpty());
        assertEquals(0, s.size());
    }

    @Test
    public void emptySequencedSet_mutationThrows() {
        SequencedSet<String> s = CollectionUtil.emptySequencedSet();
        assertThrows(UnsupportedOperationException.class, () -> s.add("x"));
    }

    @Test
    public void singletonSequencedSet_containsElement() {
        SequencedSet<String> s = CollectionUtil.singletonSequencedSet("hello");
        assertEquals(1, s.size());
        assertTrue(s.contains("hello"));
        assertFalse(s.contains("world"));
    }

    @Test
    public void singletonSequencedSet_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> CollectionUtil.singletonSequencedSet(null));
    }

    @Test
    public void singletonOrEmptySequencedSet_null_returnsEmpty() {
        assertTrue(CollectionUtil.singletonOrEmptySequencedSet(null).isEmpty());
    }

    @Test
    public void singletonOrEmptySequencedSet_nonNull_returnsSingleton() {
        SequencedSet<String> s = CollectionUtil.singletonOrEmptySequencedSet("x");
        assertEquals(1, s.size());
        assertTrue(s.contains("x"));
    }

    // -------------------------------------------------------------------------
    // addFromIterable / addFromIterator
    // -------------------------------------------------------------------------

    @Test
    public void addFromIterable_null_returnsFalse() {
        assertFalse(CollectionUtil.addFromIterable(null, new ArrayList<>()));
    }

    @Test
    public void addFromIterable_nullDest_returnsFalse() {
        assertFalse(CollectionUtil.addFromIterable(List.of("a"), null));
    }

    @Test
    public void addFromIterable_addsElements() {
        List<String> dest = new ArrayList<>();
        assertTrue(CollectionUtil.addFromIterable(List.of("a", "b"), dest));
        assertEquals(List.of("a", "b"), dest);
    }

    @Test
    public void addFromIterator_null_returnsFalse() {
        assertFalse(CollectionUtil.addFromIterator(null, new ArrayList<>()));
    }

    @Test
    public void addFromIterator_addsElements() {
        List<String> dest = new ArrayList<>();
        CollectionUtil.addFromIterator(List.of("x", "y").iterator(), dest);
        assertEquals(List.of("x", "y"), dest);
    }

    // -------------------------------------------------------------------------
    // putMapEntryStrings
    // -------------------------------------------------------------------------

    @Test
    public void putMapEntryStrings_withEquals_parsesKeyValue() {
        Map<String,String> map = new HashMap<>();
        CollectionUtil.putMapEntryStrings(List.of("foo=bar", "baz=qux"), map);
        assertEquals("bar", map.get("foo"));
        assertEquals("qux", map.get("baz"));
    }

    @Test
    public void putMapEntryStrings_withoutEquals_emptyValue() {
        Map<String,String> map = new HashMap<>();
        CollectionUtil.putMapEntryStrings(List.of("flagonly"), map);
        assertEquals("", map.get("flagonly"));
    }

    @Test
    public void putMapEntryStrings_emptyStrings_skipped() {
        Map<String,String> map = new HashMap<>();
        CollectionUtil.putMapEntryStrings(List.of("", "  ", "k=v"), map);
        assertEquals(1, map.size());
        assertEquals("v", map.get("k"));
    }

    // -------------------------------------------------------------------------
    // getListIndex2ListValueFunction
    // -------------------------------------------------------------------------

    @Test
    public void getListIndex2ListValueFunction_null_returnsNullForAnyIndex() {
        var fn = CollectionUtil.getListIndex2ListValueFunction(null);
        assertNull(fn.apply(0));
    }

    @Test
    public void getListIndex2ListValueFunction_validIndex_returnsElement() {
        var fn = CollectionUtil.getListIndex2ListValueFunction(List.of("a", "b", "c"));
        assertEquals("b", fn.apply(1));
    }

    @Test
    public void getListIndex2ListValueFunction_outOfRange_returnsNull() {
        var fn = CollectionUtil.getListIndex2ListValueFunction(List.of("a", "b"));
        assertNull(fn.apply(5));
        assertNull(fn.apply(-1));
    }

    // -------------------------------------------------------------------------
    // getMapKey2ValueFunction
    // -------------------------------------------------------------------------

    @Test
    public void getMapKey2ValueFunction_null_returnsNullForAnyKey() {
        Function<String,String> fn = CollectionUtil.getMapKey2ValueFunction(null);
        assertNull(fn.apply("anything"));
    }

    @Test
    public void getMapKey2ValueFunction_presentKey_returnsValue() {
        Function<String,String> fn = CollectionUtil.getMapKey2ValueFunction(Map.of("k", "v"));
        assertEquals("v", fn.apply("k"));
    }

    @Test
    public void getMapKey2ValueFunction_absentKey_returnsNull() {
        Function<String,String> fn = CollectionUtil.getMapKey2ValueFunction(Map.of("k", "v"));
        assertNull(fn.apply("missing"));
    }

    // -------------------------------------------------------------------------
    // convertingIterator / convertingIterable
    // -------------------------------------------------------------------------

    @Test
    public void convertingIterator_transformsElements() {
        List<String> result = new ArrayList<>();
        CollectionUtil.convertingIterator(List.of(1, 2, 3), Object::toString)
            .forEachRemaining(result::add);
        assertEquals(List.of("1", "2", "3"), result);
    }

    @Test
    public void convertingIterable_transformsElements() {
        List<String> result = new ArrayList<>();
        CollectionUtil.convertingIterable(List.of(1, 2, 3), Object::toString)
            .forEach(result::add);
        assertEquals(List.of("1", "2", "3"), result);
    }

    // -------------------------------------------------------------------------
    // addConvertedFromIterable / addConvertedFromIterator
    // -------------------------------------------------------------------------

    @Test
    public void addConvertedFromIterable_addsConverted() {
        List<String> dest = new ArrayList<>();
        CollectionUtil.addConvertedFromIterable(List.of(1, 2, 3), dest, Object::toString);
        assertEquals(List.of("1", "2", "3"), dest);
    }

    @Test
    public void addConvertedFromIterator_addsConverted() {
        List<String> dest = new ArrayList<>();
        CollectionUtil.addConvertedFromIterator(List.of(1, 2, 3).iterator(), dest, Object::toString);
        assertEquals(List.of("1", "2", "3"), dest);
    }

    // -------------------------------------------------------------------------
    // firstNonNullElementSingletonOrEmptySequencedSet
    // -------------------------------------------------------------------------

    @Test
    public void firstNonNullElement_null_returnsEmpty() {
        assertTrue(CollectionUtil.firstNonNullElementSingletonOrEmptySequencedSet(null).isEmpty());
    }

    @Test
    public void firstNonNullElement_allNull_returnsEmpty() {
        List<String> list = new ArrayList<>();
        list.add(null);
        list.add(null);
        assertTrue(CollectionUtil.firstNonNullElementSingletonOrEmptySequencedSet(list).isEmpty());
    }

    @Test
    public void firstNonNullElement_firstIsNonNull_returnsSingleton() {
        SequencedSet<String> result = CollectionUtil.firstNonNullElementSingletonOrEmptySequencedSet(
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
        SequencedSet<String> result = CollectionUtil.firstNonNullElementSingletonOrEmptySequencedSet(list);
        assertEquals(1, result.size());
        assertTrue(result.contains("second"));
    }

    // -------------------------------------------------------------------------
    // mapValueIterator (Iterable overload)
    // -------------------------------------------------------------------------

    @Test
    public void mapValueIterator_iterable_returnsValuesInKeyOrder() {
        Map<String,String> map = new LinkedHashMap<>();
        map.put("a", "apple");
        map.put("b", "banana");
        CollectionUtil.MapIteratorAdapter<String,String> adapter =
            CollectionUtil.function2mapIteratorAdapter(map::get);
        List<String> keys = List.of("b", "a");
        Iterator<String> it = CollectionUtil.mapValueIterator(adapter, (Iterable<String>) keys);
        assertEquals("banana", it.next());
        assertEquals("apple", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void mapValueIterator_iterator_returnsMappedValues() {
        CollectionUtil.MapIteratorAdapter<Integer,String> adapter =
            CollectionUtil.function2mapIteratorAdapter(i -> "v" + i);
        Iterator<String> it = CollectionUtil.mapValueIterator(adapter, List.of(1, 2, 3).iterator());
        assertEquals("v1", it.next());
        assertEquals("v2", it.next());
        assertEquals("v3", it.next());
        assertFalse(it.hasNext());
    }

    // -------------------------------------------------------------------------
    // mapValueKeyRangeIterator
    // -------------------------------------------------------------------------

    @Test
    public void mapValueKeyRangeIterator_rangeProducesValues() {
        CollectionUtil.MapIteratorAdapter<Integer,String> adapter =
            CollectionUtil.function2mapIteratorAdapter(i -> "item" + i);
        Iterator<String> it = CollectionUtil.mapValueKeyRangeIterator(adapter, 3, 5);
        assertEquals("item3", it.next());
        assertEquals("item4", it.next());
        assertEquals("item5", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void mapValueKeyRangeIterator_singleKey_returnsOneValue() {
        CollectionUtil.MapIteratorAdapter<Integer,String> adapter =
            CollectionUtil.function2mapIteratorAdapter(i -> "x");
        Iterator<String> it = CollectionUtil.mapValueKeyRangeIterator(adapter, 7, 7);
        assertEquals("x", it.next());
        assertFalse(it.hasNext());
    }

    // -------------------------------------------------------------------------
    // infiniteSingleValueIterable / infiniteSingleValueIterator
    // -------------------------------------------------------------------------

    @Test
    public void infiniteSingleValueIterable_alwaysReturnsSameValue() {
        Iterable<String> inf = CollectionUtil.infiniteSingleValueIterable("hello");
        Iterator<String> it = inf.iterator();
        for (int i = 0; i < 100; i++) {
            assertTrue(it.hasNext());
            assertEquals("hello", it.next());
        }
    }

    @Test
    public void infiniteSingleValueIterator_removeThrows() {
        Iterator<String> it = CollectionUtil.infiniteSingleValueIterator("x");
        it.next();
        assertThrows(UnsupportedOperationException.class, it::remove);
    }

    @Test
    public void infiniteSingleValueIterator_nullValue_returnsNull() {
        Iterator<String> it = CollectionUtil.infiniteSingleValueIterator(null);
        assertNull(it.next());
        assertNull(it.next());
    }

    // -------------------------------------------------------------------------
    // transform — null guards
    // -------------------------------------------------------------------------

    @Test
    public void transform_nullList_doesNotThrow() {
        assertDoesNotThrow(() -> CollectionUtil.transform(null, s -> s + "!"));
    }

    @Test
    public void transform_nullTransformer_doesNotThrow() {
        List<String> list = new ArrayList<>(List.of("a", "b"));
        assertDoesNotThrow(() -> CollectionUtil.transform(list, null));
        // list unchanged
        assertEquals(List.of("a", "b"), list);
    }

    @Test
    public void transform_appliesTransformerToEachElement() {
        List<String> list = new ArrayList<>(List.of("a", "b", "c"));
        CollectionUtil.transform(list, String::toUpperCase);
        assertEquals(List.of("A", "B", "C"), list);
    }

    // -------------------------------------------------------------------------
    // unique — null guards
    // -------------------------------------------------------------------------

    @Test
    public void unique_nullResult_doesNothing() {
        assertDoesNotThrow(() ->
            CollectionUtil.unique(List.of("a"), List.of("b"), null));
    }

    @Test
    public void unique_nullA_addsAllOfB() {
        List<String> result = new ArrayList<>();
        CollectionUtil.unique(null, List.of("x", "y"), result);
        assertEquals(List.of("x", "y"), result);
    }

    @Test
    public void unique_nullB_addsAllOfA() {
        List<String> result = new ArrayList<>();
        CollectionUtil.unique(List.of("p", "q"), null, result);
        assertEquals(List.of("p", "q"), result);
    }

    @Test
    public void unique_bothNull_resultStaysEmpty() {
        List<String> result = new ArrayList<>();
        CollectionUtil.unique(null, null, result);
        assertTrue(result.isEmpty());
    }

    // -------------------------------------------------------------------------
    // safeToString(Map) — additional cases
    // -------------------------------------------------------------------------

    @Test
    public void safeToString_mapSingleEntry_formatsCorrectly() {
        Map<String,String> m = new LinkedHashMap<>();
        m.put("k", "v");
        assertEquals("{k=v}", CollectionUtil.safeToString(m));
    }

    @Test
    public void safeToString_mapMultiEntry_formatsWithCommas() {
        Map<String,String> m = new LinkedHashMap<>();
        m.put("a", "1");
        m.put("b", "2");
        String s = CollectionUtil.safeToString(m);
        assertTrue(s.startsWith("{"));
        assertTrue(s.endsWith("}"));
        assertTrue(s.contains("a=1"));
        assertTrue(s.contains("b=2"));
    }

    // -------------------------------------------------------------------------
    // safeToString(Iterable) — additional cases
    // -------------------------------------------------------------------------

    @Test
    public void safeToString_iterableSingleElement_formatsCorrectly() {
        assertEquals("[hello]", CollectionUtil.safeToString(List.of("hello")));
    }

    @Test
    public void safeToString_iterableMultipleElements_formatsCorrectly() {
        String s = CollectionUtil.safeToString(List.of("a", "b", "c"));
        assertEquals("[a, b, c]", s);
    }

    @Test
    public void safeToString_selfContainingList_doesNotRecurseInfinitely() {
        // A list that contains itself as an element
        @SuppressWarnings("unchecked")
        List<Object> selfRef = new ArrayList<>();
        selfRef.add("first");
        selfRef.add(selfRef);
        selfRef.add("last");
        // Must not throw a StackOverflowError; self-reference replaced with "this Iterable"
        String result = CollectionUtil.safeToString((Iterable<Object>) selfRef);
        assertNotNull(result);
        assertTrue(result.contains("this Iterable"), result);
    }

    // -------------------------------------------------------------------------
    // function2mapIteratorAdapter
    // -------------------------------------------------------------------------

    @Test
    public void function2mapIteratorAdapter_get_delegatesToFunction() {
        CollectionUtil.MapIteratorAdapter<String,Integer> adapter =
            CollectionUtil.function2mapIteratorAdapter(String::length);
        assertEquals(5, adapter.get("hello"));
        assertEquals(0, adapter.get(""));
    }

    @Test
    public void function2mapIteratorAdapter_remove_throwsUnsupported() {
        CollectionUtil.MapIteratorAdapter<String,Integer> adapter =
            CollectionUtil.function2mapIteratorAdapter(String::length);
        assertThrows(UnsupportedOperationException.class, () -> adapter.remove("key"));
    }

    @Test
    public void function2mapIteratorAdapter_nullFunction_throwsNPE() {
        assertThrows(NullPointerException.class,
            () -> CollectionUtil.function2mapIteratorAdapter(null));
    }

    // -------------------------------------------------------------------------
    // unmodifiableCollection / unmodifiableList / unmodifiableSet / unmodifiableMap
    // -------------------------------------------------------------------------

    @Test
    public void unmodifiableCollection_null_returnsEmptyList() {
        Collection<String> c = CollectionUtil.unmodifiableCollection(null);
        assertNotNull(c);
        assertTrue(c.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> c.add("x"));
    }

    @Test
    public void unmodifiableCollection_mutableCollection_isUnmodifiable() {
        Collection<String> c = CollectionUtil.unmodifiableCollection(new ArrayList<>(List.of("a")));
        assertThrows(UnsupportedOperationException.class, () -> c.add("b"));
    }

    @Test
    public void unmodifiableCollection_immutableCollection_returnsSame() {
        var immutable = com.google.common.collect.ImmutableList.of("a", "b");
        assertSame(immutable, CollectionUtil.unmodifiableCollection(immutable));
    }

    @Test
    public void unmodifiableList_null_returnsEmptyList() {
        List<String> l = CollectionUtil.unmodifiableList(null);
        assertNotNull(l);
        assertTrue(l.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> l.add("x"));
    }

    @Test
    public void unmodifiableList_mutableList_isUnmodifiable() {
        List<String> l = CollectionUtil.unmodifiableList(new ArrayList<>(List.of("a")));
        assertThrows(UnsupportedOperationException.class, () -> l.add("b"));
    }

    @Test
    public void unmodifiableList_immutableList_returnsSame() {
        var immutable = com.google.common.collect.ImmutableList.of("x");
        assertSame(immutable, CollectionUtil.unmodifiableList(immutable));
    }

    @Test
    public void unmodifiableSet_null_returnsEmptySet() {
        Set<String> s = CollectionUtil.unmodifiableSet(null);
        assertNotNull(s);
        assertTrue(s.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> s.add("x"));
    }

    @Test
    public void unmodifiableSet_mutableSet_isUnmodifiable() {
        Set<String> s = CollectionUtil.unmodifiableSet(new HashSet<>(Set.of("a")));
        assertThrows(UnsupportedOperationException.class, () -> s.add("b"));
    }

    @Test
    public void unmodifiableSet_immutableSet_returnsSame() {
        var immutable = com.google.common.collect.ImmutableSet.of("a");
        assertSame(immutable, CollectionUtil.unmodifiableSet(immutable));
    }

    @Test
    public void unmodifiableMap_null_returnsEmptyMap() {
        Map<String,String> m = CollectionUtil.unmodifiableMap(null);
        assertNotNull(m);
        assertTrue(m.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> m.put("k", "v"));
    }

    @Test
    public void unmodifiableMap_mutableMap_isUnmodifiable() {
        Map<String,String> m = CollectionUtil.unmodifiableMap(new HashMap<>(Map.of("k", "v")));
        assertThrows(UnsupportedOperationException.class, () -> m.put("x", "y"));
    }

    @Test
    public void unmodifiableMap_immutableMap_returnsSame() {
        var immutable = com.google.common.collect.ImmutableMap.of("k", "v");
        assertSame(immutable, CollectionUtil.unmodifiableMap(immutable));
    }

    // -------------------------------------------------------------------------
    // emptySequencedSet — additional cases
    // -------------------------------------------------------------------------

    @Test
    public void emptySequencedSet_isUnmodifiable() {
        SequencedSet<String> s = CollectionUtil.emptySequencedSet();
        assertThrows(UnsupportedOperationException.class, () -> s.add("x"));
    }

    @Test
    public void emptySequencedSet_sameInstanceEachCall() {
        assertSame(CollectionUtil.emptySequencedSet(), CollectionUtil.emptySequencedSet());
    }

    // -------------------------------------------------------------------------
    // singletonSequencedSet — additional cases
    // -------------------------------------------------------------------------

    @Test
    public void singletonSequencedSet_isUnmodifiable() {
        SequencedSet<String> s = CollectionUtil.singletonSequencedSet("one");
        assertThrows(UnsupportedOperationException.class, () -> s.add("two"));
    }

    @Test
    public void singletonSequencedSet_getFirst_returnsElement() {
        SequencedSet<String> s = CollectionUtil.singletonSequencedSet("elem");
        assertEquals("elem", s.getFirst());
        assertEquals("elem", s.getLast());
    }

    // -------------------------------------------------------------------------
    // firstNonNullElementSingletonOrEmptySequencedSet (additional cases)
    // -------------------------------------------------------------------------

    @Test
    public void firstNonNullElement_null_collection_returnsEmpty() {
        assertTrue(CollectionUtil.firstNonNullElementSingletonOrEmptySequencedSet(null).isEmpty());
    }

    // -------------------------------------------------------------------------
    // unmodifiableSequencedSet(SequencedSet)
    // -------------------------------------------------------------------------

    @Test
    public void unmodifiableSequencedSet_set_null_returnsEmpty() {
        SequencedSet<String> s = CollectionUtil.unmodifiableSequencedSet((SequencedSet<String>) null);
        assertTrue(s.isEmpty());
    }

    @Test
    public void unmodifiableSequencedSet_set_mutableSet_isUnmodifiable() {
        LinkedHashSet<String> base = new LinkedHashSet<>(List.of("a", "b"));
        SequencedSet<String> s = CollectionUtil.unmodifiableSequencedSet(base);
        assertThrows(UnsupportedOperationException.class, () -> s.add("c"));
    }

    @Test
    public void unmodifiableSequencedSet_set_alreadyUnmodifiable_returnsSame() {
        SequencedSet<String> singleton = CollectionUtil.singletonSequencedSet("x");
        assertSame(singleton, CollectionUtil.unmodifiableSequencedSet(singleton));
    }

    // -------------------------------------------------------------------------
    // unmodifiableSequencedSet(T... varargs)
    // -------------------------------------------------------------------------

    @Test
    public void unmodifiableSequencedSet_varargs_containsAllElements() {
        SequencedSet<String> s = CollectionUtil.unmodifiableSequencedSet("a", "b", "c");
        assertEquals(3, s.size());
        assertTrue(s.contains("a"));
        assertTrue(s.contains("b"));
        assertTrue(s.contains("c"));
    }

    @Test
    public void unmodifiableSequencedSet_varargs_deduplicates() {
        SequencedSet<String> s = CollectionUtil.unmodifiableSequencedSet("x", "x", "y");
        assertEquals(2, s.size());
    }

    @Test
    public void unmodifiableSequencedSet_varargs_isUnmodifiable() {
        SequencedSet<String> s = CollectionUtil.unmodifiableSequencedSet("a");
        assertThrows(UnsupportedOperationException.class, () -> s.add("b"));
    }

    @Test
    public void unmodifiableSequencedSet_varargs_empty_returnsEmpty() {
        SequencedSet<String> s = CollectionUtil.unmodifiableSequencedSet(new String[0]);
        assertTrue(s.isEmpty());
    }

    // -------------------------------------------------------------------------
    // putMapEntryStrings
    // -------------------------------------------------------------------------

    @Test
    public void putMapEntryStrings_keyEqualsValue_addsEntry() {
        Map<String,String> map = new LinkedHashMap<>();
        CollectionUtil.putMapEntryStrings(List.of("key=value"), map);
        assertEquals("value", map.get("key"));
    }

    @Test
    public void putMapEntryStrings_noEquals_addsEmptyStringValue() {
        Map<String,String> map = new LinkedHashMap<>();
        CollectionUtil.putMapEntryStrings(List.of("flagOnly"), map);
        assertEquals("", map.get("flagOnly"));
    }

    @Test
    public void putMapEntryStrings_blankEntry_isSkipped() {
        Map<String,String> map = new LinkedHashMap<>();
        CollectionUtil.putMapEntryStrings(List.of("  ", "k=v"), map);
        assertFalse(map.containsKey("  "));
        assertEquals("v", map.get("k"));
    }

    @Test
    public void putMapEntryStrings_multipleEntries_allAdded() {
        Map<String,String> map = new LinkedHashMap<>();
        CollectionUtil.putMapEntryStrings(List.of("a=1", "b=2", "c=3"), map);
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
        assertEquals("3", map.get("c"));
    }

    @Test
    public void putMapEntryStrings_valueContainsEquals_splitsOnFirstEquals() {
        Map<String,String> map = new LinkedHashMap<>();
        CollectionUtil.putMapEntryStrings(List.of("url=http://x.com?a=1"), map);
        assertEquals("http://x.com?a=1", map.get("url"));
    }

    @Test
    public void putMapEntryStrings_returnsTrue() {
        Map<String,String> map = new LinkedHashMap<>();
        assertTrue(CollectionUtil.putMapEntryStrings(List.of("k=v"), map));
    }

    // -------------------------------------------------------------------------
    // emptySequencedSetInsteadOfNull
    // -------------------------------------------------------------------------

    @Test
    public void emptySequencedSetInsteadOfNull_null_returnsEmptySequencedSet() {
        SequencedSet<String> result = CollectionUtil.emptySequencedSetInsteadOfNull(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void emptySequencedSetInsteadOfNull_nonNull_returnsSameInstance() {
        LinkedHashSet<String> base = new LinkedHashSet<>(List.of("a", "b"));
        SequencedSet<String> result = CollectionUtil.emptySequencedSetInsteadOfNull(base);
        assertSame(base, result);
    }

    // -------------------------------------------------------------------------
    // putIfNotNull — null key on a map that rejects null keys
    // -------------------------------------------------------------------------

    @Test
    public void putIfNotNull_nullKeyOnNullRejectingMap_returnsFalse() {
        // TreeMap with natural ordering throws NullPointerException for null keys
        Map<String,String> treeMap = new TreeMap<>();
        assertFalse(CollectionUtil.putIfNotNull(null, "value", treeMap));
        assertTrue(treeMap.isEmpty());
    }

    // -------------------------------------------------------------------------
    // sortIfList — non-list collection and null comparator
    // -------------------------------------------------------------------------

    @Test
    public void sortIfList_comparator_notAList_returnsFalse() {
        Set<String> set = new HashSet<>(Set.of("b", "a", "c"));
        assertFalse(CollectionUtil.sortIfList(set, Comparator.naturalOrder()));
    }

    @Test
    public void sortIfList_comparator_nullComparator_throwsNPE() {
        List<String> list = new ArrayList<>(List.of("b", "a"));
        assertThrows(NullPointerException.class,
            () -> CollectionUtil.sortIfList(list, null));
    }

}

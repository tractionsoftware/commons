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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GetPutPropertyChangeTrackerTest {

    @Test
    void getProperty_noChange_returnsCurrentValue() {
        GetProperty current = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetPutPropertyChangeTracker tracker = new GetPutPropertyChangeTracker(current);
        assertEquals("1", tracker.getProperty("a"));
    }

    @Test
    void getProperty_afterPut_returnsChangedValue() {
        GetProperty current = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetPutPropertyChangeTracker tracker = new GetPutPropertyChangeTracker(current);
        tracker.putProperty("a", "2");
        assertEquals("2", tracker.getProperty("a"));
    }

    @Test
    void getProperty_removedWithNoDefaults_returnsNull() {
        GetProperty current = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetPutPropertyChangeTracker tracker = new GetPutPropertyChangeTracker(current);
        tracker.putProperty("a", null);
        assertNull(tracker.getProperty("a"));
    }

    @Test
    void getProperty_removedWithDefaults_fallsBackToDefaultValue() {
        GetProperty defaults = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "default-1")));
        GetProperty current = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1"))).withDefaults(defaults);
        GetPutPropertyChangeTracker tracker = new GetPutPropertyChangeTracker(current);
        tracker.putProperty("a", null);
        assertEquals("default-1", tracker.getProperty("a"));
    }

    @Test
    void getProperty_unchangedName_fallsThroughToCurrentProps() {
        GetProperty current = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetPutPropertyChangeTracker tracker = new GetPutPropertyChangeTracker(current);
        tracker.putProperty("b", "2");
        assertEquals("1", tracker.getProperty("a"));
    }

    @Test
    void getPropertyNames_includesDefaultsLocalsAndAddedChanges() {
        GetProperty defaults = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1", "b", "2")));
        GetProperty current = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1"))).withDefaults(defaults);
        GetPutPropertyChangeTracker tracker = new GetPutPropertyChangeTracker(current);
        tracker.putProperty("c", "3");
        assertEquals(Set.of("a", "b", "c"), tracker.getPropertyNames());
    }

    @Test
    void getPropertyNames_removedLocalName_isExcluded() {
        GetProperty current = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1", "x", "9")));
        GetPutPropertyChangeTracker tracker = new GetPutPropertyChangeTracker(current);
        tracker.putProperty("x", null);
        assertEquals(Set.of("a"), tracker.getPropertyNames());
    }

    @Test
    void getPropertyWithLoader_noChange_delegatesToCurrentProps() {
        GetProperty current = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetPutPropertyChangeTracker tracker = new GetPutPropertyChangeTracker(current);
        PropertyLoader<String> identity = new BiFunctionPropertyLoader<>(String.class, (name, rawValue) -> rawValue);
        assertEquals("1", tracker.getProperty("a", identity, true));
    }

    @Test
    void getPropertyWithLoader_afterPut_returnsChangedValue() {
        GetProperty current = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetPutPropertyChangeTracker tracker = new GetPutPropertyChangeTracker(current);
        tracker.putProperty("a", "2");
        PropertyLoader<String> identity = new BiFunctionPropertyLoader<>(String.class, (name, rawValue) -> rawValue);
        assertEquals("2", tracker.getProperty("a", identity, true));
    }

    @Test
    void getPropertyWithLoader_removedWithDefaults_fallsBackToDefaultValue() {
        GetProperty defaults = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "default-1")));
        GetProperty current = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1"))).withDefaults(defaults);
        GetPutPropertyChangeTracker tracker = new GetPutPropertyChangeTracker(current);
        tracker.putProperty("a", null);
        PropertyLoader<String> identity = new BiFunctionPropertyLoader<>(String.class, (name, rawValue) -> rawValue);
        assertEquals("default-1", tracker.getProperty("a", identity, true));
    }

    @Test
    void getPropertyWithLoader_removedWithNoDefaults_returnsNull() {
        GetProperty current = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetPutPropertyChangeTracker tracker = new GetPutPropertyChangeTracker(current);
        tracker.putProperty("a", null);
        PropertyLoader<String> identity = new BiFunctionPropertyLoader<>(String.class, (name, rawValue) -> rawValue);
        assertNull(tracker.getProperty("a", identity, true));
    }

    @Test
    void hasProperty_noChange_returnsFalse() {
        GetProperty current = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetPutPropertyChangeTracker tracker = new GetPutPropertyChangeTracker(current);
        assertFalse(tracker.hasProperty("a"));
    }

    @Test
    void hasProperty_afterPut_returnsTrue() {
        GetProperty current = MapPropertyStore.createInstance(new HashMap<>());
        GetPutPropertyChangeTracker tracker = new GetPutPropertyChangeTracker(current);
        tracker.putProperty("a", "1");
        assertTrue(tracker.hasProperty("a"));
    }

    @Test
    void getLocals_returnsTheChangesStore() {
        GetProperty current = MapPropertyStore.createInstance(new HashMap<>());
        GetPutPropertyChangeTracker tracker = new GetPutPropertyChangeTracker(current);
        tracker.putProperty("a", "1");
        assertEquals("1", tracker.getLocals().getProperty("a"));
    }

    @Test
    void getDefaults_returnsReadOnlyViewOfCurrentProps() {
        GetProperty current = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetPutPropertyChangeTracker tracker = new GetPutPropertyChangeTracker(current);
        assertEquals("1", tracker.getDefaults().getProperty("a"));
    }

    @Test
    void applyPropertyChange_changeTestReturnsTrue_recordsChange() {
        GetProperty current = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetPutPropertyChangeTracker tracker = new GetPutPropertyChangeTracker(current);
        tracker.applyPropertyChange("a", "2", (oldValue, newValue) -> true);
        assertEquals("2", tracker.getProperty("a"));
    }

    @Test
    void applyPropertyChange_changeTestReturnsFalse_doesNotRecordChange() {
        GetProperty current = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetPutPropertyChangeTracker tracker = new GetPutPropertyChangeTracker(current);
        tracker.applyPropertyChange("a", "2", (oldValue, newValue) -> false);
        assertEquals("1", tracker.getProperty("a"));
        assertFalse(tracker.hasChanges());
    }

    @Test
    void applyPropertyChange_alreadyChangedToSameValue_doesNotReconsultChangeTest() {
        GetProperty current = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetPutPropertyChangeTracker tracker = new GetPutPropertyChangeTracker(current);
        tracker.putProperty("a", "2");
        tracker.applyPropertyChange("a", "2", (oldValue, newValue) -> {
            throw new AssertionError("changeTest should not be consulted");
        });
        assertEquals("2", tracker.getProperty("a"));
    }

    @Test
    void applyPropertyChange_alreadyChangedToDifferentValue_reappliesWithoutConsultingChangeTest() {
        GetProperty current = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetPutPropertyChangeTracker tracker = new GetPutPropertyChangeTracker(current);
        tracker.putProperty("a", "2");
        tracker.applyPropertyChange("a", "3", (oldValue, newValue) -> {
            throw new AssertionError("changeTest should not be consulted");
        });
        assertEquals("3", tracker.getProperty("a"));
    }

    @Test
    void hasChanges_noChanges_returnsFalse() {
        GetProperty current = MapPropertyStore.createInstance(new HashMap<>());
        GetPutPropertyChangeTracker tracker = new GetPutPropertyChangeTracker(current);
        assertFalse(tracker.hasChanges());
    }

    @Test
    void hasChanges_afterPut_returnsTrue() {
        GetProperty current = MapPropertyStore.createInstance(new HashMap<>());
        GetPutPropertyChangeTracker tracker = new GetPutPropertyChangeTracker(current);
        tracker.putProperty("a", "1");
        assertTrue(tracker.hasChanges());
    }

    @Test
    void clear_removesAllRecordedChanges() {
        GetProperty current = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetPutPropertyChangeTracker tracker = new GetPutPropertyChangeTracker(current);
        tracker.putProperty("a", "2");
        tracker.clear();
        assertFalse(tracker.hasChanges());
        assertEquals("1", tracker.getProperty("a"));
    }

    @Test
    void toString_includesCurrentAndChanges() {
        GetProperty current = MapPropertyStore.createInstance(new HashMap<>());
        GetPutPropertyChangeTracker tracker = new GetPutPropertyChangeTracker(current);
        String str = tracker.toString();
        assertTrue(str.contains("current:"));
        assertTrue(str.contains("changes:"));
    }

}
